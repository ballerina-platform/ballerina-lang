/*
 * Copyright (c) 2026, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.oci;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import land.oras.Annotations;
import land.oras.ArtifactType;
import land.oras.ContainerRef;
import land.oras.Layer;
import land.oras.LocalPath;
import land.oras.Manifest;
import land.oras.ManifestDescriptor;
import land.oras.Referrers;
import land.oras.Registry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Client for pushing and pulling Ballerina packages to and from an OCI registry, backed by the
 * ORAS Java SDK.
 */
public class OciClient {

    private static final String BALA_EXTENSION = ".bala";
    private static final Pattern VERSION_TAG_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[0-9A-Za-z.-]+)?$");
    private static final String BALA_ARTIFACT_TYPE = "application/vnd.ballerina.package.v1+json";
    private static final String DEP_GRAPH_ARTIFACT_TYPE = "application/vnd.ballerina.dependency-graph.v1+json";
    public static final String PLATFORM_LABEL = "io.ballerina.platform";
    public static final String DISTRIBUTION_LABEL = "io.ballerina.distribution";
    public static final String DEPRECATED_LABEL = "io.ballerina.deprecated";
    public static final String DEPRECATION_MSG_LABEL = "io.ballerina.deprecation-message";


    private static final Logger ORAS_LOGGER = Logger.getLogger("land.oras");

    static {
        // ORAS logs registry-policy/blob-upload internals straight to the console via
        // java.util.logging; failures are surfaced through OciClientException instead, so this
        // keeps `bal push`/`bal pull` output as quiet as the previous Jib-based client's was.
        ORAS_LOGGER.setLevel(Level.OFF);
    }

    private final Registry registry;
    private final String registryHost;
    private final String repositoryPrefix;
    private final Map<String, Manifest> manifestCache = new ConcurrentHashMap<>();

    /**
     * Creates an OCI registry client.
     *
     * <p>The URL scheme selects the transport: {@code http://} marks the registry as insecure, so plain HTTP and
     * credentials over HTTP are permitted. Any other form, including a scheme-less URL, is treated as a secure
     * registry reached over HTTPS.
     *
     * @param registryUrl registry host, optional base path, and any URL scheme (stripped)
     * @param username    registry username
     * @param password    registry password or access token
     */
    public OciClient(String registryUrl, String username, String password) {
        boolean insecure = registryUrl != null && registryUrl.toLowerCase(Locale.ROOT).startsWith("http://");
        String stripped = registryUrl == null ? "" : registryUrl.replaceFirst("^(?i)(http://|https://)", "");
        int firstSlash = stripped.indexOf('/');
        this.registryHost = (firstSlash == -1 ? stripped : stripped.substring(0, firstSlash)).toLowerCase(
                Locale.ROOT);
        this.repositoryPrefix = firstSlash == -1 ? "" : stripped.substring(firstSlash + 1).toLowerCase(Locale.ROOT);
        Registry.Builder builder = insecure
                ? Registry.builder().insecure(registryHost, username, password)
                : Registry.builder().defaults(registryHost, username, password);
        this.registry = builder.withExecutorService(newDaemonExecutor()).build();
    }

    /**
     * Builds a fixed-size thread pool whose threads are marked daemon, so it never blocks JVM
     * exit even if left running.
     *
     * @return the executor service
     */
    private static ExecutorService newDaemonExecutor() {
        AtomicInteger threadCount = new AtomicInteger();
        return Executors.newFixedThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable, "oci-client-worker-" + threadCount.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Routes registry connections through an HTTP(S) proxy using the standard Java proxy system properties (JVM-wide).
     *
     * @param host     proxy host; no-op if empty
     * @param port     proxy port; no-op if zero
     * @param username proxy username, may be empty
     * @param password proxy password, may be empty
     */
    public void setProxy(String host, int port, String username, String password) {
        if (host == null || host.isEmpty() || port == 0) {
            return;
        }
        OciClientUtils.setPropertyIfAbsent("http.proxyHost", host);
        OciClientUtils.setPropertyIfAbsent("http.proxyPort", String.valueOf(port));
        OciClientUtils.setPropertyIfAbsent("https.proxyHost", host);
        OciClientUtils.setPropertyIfAbsent("https.proxyPort", String.valueOf(port));
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            OciClientUtils.setPropertyIfAbsent("http.proxyUser", username);
            OciClientUtils.setPropertyIfAbsent("http.proxyPassword", password);
            OciClientUtils.setPropertyIfAbsent("https.proxyUser", username);
            OciClientUtils.setPropertyIfAbsent("https.proxyPassword", password);
        }
    }

    /**
     * Pushes a bala file to the registry as a single-layer OCI artifact, with platform and
     * distribution recorded as manifest annotations.
     *
     * @param org                 package organization
     * @param pkg                 package name
     * @param version             package version, used as the image tag
     * @param platform            bala target platform
     * @param distributionVersion Ballerina distribution version the bala was built with
     * @param balaFilePath        path to the bala file
     */
    public void pushOCIArtifact(String org, String pkg, String version, String platform,
            String distributionVersion, Path balaFilePath) {
        if (!Files.exists(balaFilePath)) {
            throw new OciClientException("bala file does not exist: " + balaFilePath);
        }
        ContainerRef ref = ContainerRef.parse(repositoryReference(org, pkg) + ":" + version);
        if (versionExists(ref)) {
            throw new OciClientException("package '" + org + "/" + pkg + ":" + version
                    + "' already exists in the registry.");
        }
        try {
            Annotations annotations = Annotations.ofManifest(
                    Map.of(PLATFORM_LABEL, platform, DISTRIBUTION_LABEL, distributionVersion));
            registry.pushArtifact(ref, ArtifactType.from(BALA_ARTIFACT_TYPE), annotations,
                    LocalPath.of(balaFilePath, "application/octet-stream"));
        } catch (RuntimeException exception) {
            throw new OciClientException("failed to push OCI artifact to the registry", exception);
        }
    }

    /**
     * Publishes the dependency graph for a just-pushed package version as an OCI referrer
     * artifact (OCI Distribution Spec v1.1 reference types) — a manifest whose {@code subject}
     * points at the version manifest, with the graph JSON as its one layer. Callers can then
     * fetch the graph directly via the referrers API without ever downloading the bala.
     *
     * @param org                  package organization
     * @param pkg                  package name
     * @param version              package version, whose already-pushed manifest becomes the subject
     * @param dependencyGraphJson  the package's {@code dependency-graph.json} bytes, as published
     *                             in its bala
     */
    public void pushDependencyGraphReferrer(String org, String pkg, String version, byte[] dependencyGraphJson) {
        ContainerRef ref = ContainerRef.parse(repositoryReference(org, pkg) + ":" + version);
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("ballerina-dep-graph-", ".json");
            Files.write(tempFile, dependencyGraphJson);
            registry.attachArtifact(ref, ArtifactType.from(DEP_GRAPH_ARTIFACT_TYPE),
                    LocalPath.of(tempFile, DEP_GRAPH_ARTIFACT_TYPE));
        } catch (RuntimeException | IOException exception) {
            throw new OciClientException("failed to publish dependency graph referrer to the registry", exception);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Builds the lowercased registry repository reference for a package.
     *
     * @param org package organization
     * @param pkg package name
     * @return {@code <registry>[/<prefix>]/<org>/<pkg>} in lowercase
     */
    private String repositoryReference(String org, String pkg) {
        String path = (repositoryPrefix.isEmpty() ? "" : repositoryPrefix + "/") + org + "/" + pkg;
        return (registryHost + "/" + path).toLowerCase(Locale.ROOT);
    }

    /**
     * Fetches a version's manifest, reusing an already-fetched copy from earlier in this
     * client's lifetime (e.g. a prior compatibility check) instead of hitting the registry again.
     *
     * @param org     package organization
     * @param pkg     package name
     * @param version package version
     * @return the manifest
     */
    private Manifest fetchManifest(String org, String pkg, String version) {
        return manifestCache.computeIfAbsent(repositoryReference(org, pkg) + ":" + version,
                reference -> registry.getManifest(ContainerRef.parse(reference)));
    }

    /**
     * Checks whether a version tag already exists in the registry.
     *
     * @param ref reference to the version tag to check
     * @return true if the tag already exists
     */
    private boolean versionExists(ContainerRef ref) {
        try {
            return registry.getTags(ref).tags().contains(ref.getTag());
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * Pulls a bala from the registry into the given location.
     *
     * @param org          package organization
     * @param name         package name
     * @param version      package version
     * @param repoLocation directory to save the bala under
     */
    public void pullMetadata(String org, String name, String version, String repoLocation) {
        pullMetadata(org, name, version, repoLocation, repoLocation);
    }

    /**
     * Pulls a bala from the registry into the given location.
     *
     * @param org             package organization
     * @param name            package name
     * @param version         package version
     * @param repoLocation    directory to save the bala under
     * @param displayLocation path shown in progress output (unused; retained for API compatibility)
     */
    public void pullMetadata(String org, String name, String version, String repoLocation, String displayLocation) {
        try {
            doPullBala(org, name, version, repoLocation);
        } catch (RuntimeException | IOException exception) {
            throw new OciClientException("failed to pull bala from the repo", exception);
        }
    }

    /**
     * Downloads the bala layer of a package version and writes it to disk.
     *
     * @param org          package organization
     * @param name         package name
     * @param version      package version
     * @param repoLocation directory to save the bala under
     * @throws IOException on file system failures
     */
    private void doPullBala(String org, String name, String version, String repoLocation) throws IOException {
        ContainerRef ref = ContainerRef.parse(repositoryReference(org, name) + ":" + version);
        Manifest manifest = fetchManifest(org, name, version);
        List<Layer> layers = manifest.getLayers();
        if (layers.isEmpty()) {
            throw new OciClientException("no layers found in the OCI manifest for " + org + "/" + name + ":"
                    + version);
        }

        Path balaFilePath = Paths.get(repoLocation).resolve(org).resolve(name).resolve(version)
                .resolve(name + "-" + version + BALA_EXTENSION);
        Path balaFileDir = balaFilePath.getParent();
        if (balaFileDir != null) {
            Files.createDirectories(balaFileDir);
        }
        Path blobTempFile = Files.createTempFile("ballerina-oci-blob-", ".tmp");
        try {
            boolean balaExtracted = false;
            for (Layer layer : layers) {
                registry.fetchBlob(ref.withDigest(layer.getDigest()), blobTempFile);
                if (OciClientUtils.extractBalaFromLayer(blobTempFile, balaFilePath)) {
                    balaExtracted = true;
                    break;
                }
            }
            if (!balaExtracted) {
                throw new OciClientException("no bala layer found in the OCI manifest for "
                        + org + "/" + name + ":" + version);
            }
        } finally {
            Files.deleteIfExists(blobTempFile);
        }
    }

    /**
     * Reads the version index published under the {@code latest} tag of a package.
     *
     * @param org package organization
     * @param pkg package name
     * @return the list of available versions
     */
    public List<String> pullMetadata(String org, String pkg) {
        try {
            ContainerRef ref = ContainerRef.parse(repositoryReference(org, pkg) + ":latest");
            Manifest manifest = registry.getManifest(ref);
            List<Layer> layers = manifest.getLayers();
            if (layers.isEmpty()) {
                return Collections.emptyList();
            }
            byte[] blobBytes = registry.getBlob(ref.withDigest(layers.get(0).getDigest()));
            String text = new String(blobBytes, StandardCharsets.UTF_8);
            List<String> parsedVersions = new Gson().fromJson(text, new TypeToken<List<String>>() { }.getType());
            return parsedVersions != null ? parsedVersions : Collections.emptyList();
        } catch (RuntimeException exception) {
            throw new OciClientException("failed to pull metadata from the registry", exception);
        }
    }

    /**
     * Fetches the dependency graph for a package version via the OCI referrers API (OCI
     * Distribution Spec v1.1 reference types), if the registry publishes one.
     *
     * @param org     package organization
     * @param pkg     package name
     * @param version package version
     * @return the raw {@code dependency-graph.json} content published as a referrer, or empty if
     *         unavailable
     */
    public Optional<String> pullDependencyGraph(String org, String pkg, String version) {
        try {
            ContainerRef ref = ContainerRef.parse(repositoryReference(org, pkg) + ":" + version);
            Manifest subjectManifest = fetchManifest(org, pkg, version);
            String subjectDigest = subjectManifest.getDescriptor().getDigest();

            Referrers referrers = registry.getReferrers(ref.withDigest(subjectDigest),
                    ArtifactType.from(DEP_GRAPH_ARTIFACT_TYPE));
            Optional<ManifestDescriptor> dependencyGraphReferrer = referrers.getManifests().stream()
                    .filter(referrer -> DEP_GRAPH_ARTIFACT_TYPE.equals(referrer.getArtifactType()))
                    .findFirst();
            if (dependencyGraphReferrer.isEmpty()) {
                return Optional.empty();
            }

            Manifest referrerManifest = registry.getManifest(ref.withDigest(dependencyGraphReferrer.get()
                    .getDigest()));
            List<Layer> layers = referrerManifest.getLayers();
            if (layers.isEmpty()) {
                return Optional.empty();
            }
            byte[] blobBytes = registry.getBlob(ref.withDigest(layers.get(0).getDigest()));
            return Optional.of(new String(blobBytes, StandardCharsets.UTF_8));
        } catch (RuntimeException exception) {
            throw new OciClientException("failed to pull dependency graph from the registry", exception);
        }
    }

    /**
     * Reads a version's {@code io.ballerina.platform}/{@code io.ballerina.distribution}
     * annotations from its manifest, without downloading the bala or any extra blob — used to
     * filter candidate versions before pulling one.
     *
     * @param org     package organization
     * @param pkg     package name
     * @param version package version whose manifest to inspect
     * @return the version's annotations, or an empty map if the manifest carries none (e.g. it
     *         isn't a Ballerina package at all, or predates this labeling)
     */
    public Map<String, String> pullLabels(String org, String pkg, String version) {
        try {
            Map<String, String> annotations = fetchManifest(org, pkg, version).getAnnotations();
            return annotations == null ? Collections.emptyMap() : annotations;
        } catch (RuntimeException exception) {
            throw new OciClientException("failed to read labels from the registry", exception);
        }
    }

    /**
     * Lists the version tags of a package repository.
     *
     * @param org package organization
     * @param pkg package name
     * @return the SemVer tags
     */
    public List<String> listTags(String org, String pkg) {
        try {
            ContainerRef ref = ContainerRef.parse(repositoryReference(org, pkg));
            List<String> tags = registry.getTags(ref).tags();
            return tags.stream().filter(tag -> VERSION_TAG_PATTERN.matcher(tag).matches()).toList();
        } catch (RuntimeException exception) {
            throw new OciClientException("failed to list tags from the registry", exception);
        }
    }

}
