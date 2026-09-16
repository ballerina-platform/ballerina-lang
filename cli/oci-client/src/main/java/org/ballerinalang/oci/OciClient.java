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

import com.google.cloud.tools.jib.api.CacheDirectoryCreationException;
import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.Credential;
import com.google.cloud.tools.jib.api.DescriptorDigest;
import com.google.cloud.tools.jib.api.ImageReference;
import com.google.cloud.tools.jib.api.InvalidImageReferenceException;
import com.google.cloud.tools.jib.api.Jib;
import com.google.cloud.tools.jib.api.LogEvent;
import com.google.cloud.tools.jib.api.RegistryException;
import com.google.cloud.tools.jib.api.RegistryImage;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;
import com.google.cloud.tools.jib.api.buildplan.ImageFormat;
import com.google.cloud.tools.jib.blob.Blob;
import com.google.cloud.tools.jib.blob.Blobs;
import com.google.cloud.tools.jib.event.EventHandlers;
import com.google.cloud.tools.jib.http.Authorization;
import com.google.cloud.tools.jib.http.FailoverHttpClient;
import com.google.cloud.tools.jib.http.Request;
import com.google.cloud.tools.jib.http.Response;
import com.google.cloud.tools.jib.http.ResponseException;
import com.google.cloud.tools.jib.image.json.BuildableManifestTemplate;
import com.google.cloud.tools.jib.image.json.OciManifestTemplate;
import com.google.cloud.tools.jib.registry.ManifestAndDigest;
import com.google.cloud.tools.jib.registry.RegistryClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.oci.model.ManifestDescriptor;
import org.ballerinalang.oci.model.ReferrersResponse;
import org.ballerinalang.oci.model.TagsListResponse;
import org.ballerinalang.oci.model.TokenResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client for pushing and pulling Ballerina packages to and from an OCI registry.
 *
 */
public class OciClient {

    private static final String BALA_EXTENSION = ".bala";
    private static final int MAX_PULL_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    private static final Pattern VERSION_TAG_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[0-9A-Za-z.-]+)?$");
    private static final Pattern AUTH_CHALLENGE_PARAM_PATTERN = Pattern.compile("(\\w+)=\"([^\"]*)\"");
    private static final String DEP_GRAPH_ARTIFACT_TYPE = "application/vnd.ballerina.dependency-graph.v1+json";
    private static final String OCI_EMPTY_CONFIG_DIGEST = "sha256:44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a";

    private String registryUrl;
    private String username;
    private String password;
    private final boolean insecureRegistry;
    private final PrintStream outStream;

    /**
     * Creates an OCI registry client.
     *
     *
     * @param registryUrl registry host and base path (any URL scheme is stripped)
     * @param username    registry username
     * @param password    registry password or access token
     */
    public OciClient(String registryUrl, String username, String password) {
        this.insecureRegistry = registryUrl != null
                && registryUrl.toLowerCase(Locale.ROOT).startsWith("http://");
        if (registryUrl != null) {
            this.registryUrl = registryUrl.replaceFirst("^(?i)(http://|https://)", "");
        }
        this.username = username;
        this.password = password;
        this.outStream = System.out;
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
     * Runs an action with retries and exponential backoff.
     *
     * @param action        action to run
     * @param operationName operation name used in retry messages
     * @throws Exception the last failure once retries are exhausted
     */
    private void withRetry(Callable<Void> action, String operationName) throws Exception {
        int attempt = 0;
        long delayMs = INITIAL_RETRY_DELAY_MS;
        while (true) {
            try {
                action.call();
                return;
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_PULL_RETRIES) {
                    throw e;
                }
                outStream.println(operationName + " failed (attempt " + attempt + "/"
                        + MAX_PULL_RETRIES + "), retrying in " + delayMs + "ms: " + e.getMessage());
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                delayMs = Math.min(delayMs * 2, 8000);
            }
        }
    }

    /**
     * Pushes a bala file to the registry as a single-layer OCI artifact.
     *
     * @param org          package organization
     * @param pkg          package name
     * @param version      package version, used as the image tag
     * @param platform     bala target platform
     * @param balaFilePath path to the bala file
     */
    public void pushOCIArtifact(String org, String pkg, String version, String platform, Path balaFilePath) {
        if (!balaFilePath.toFile().exists()) {
            throw new OciClientException("bala file does not exist: " + balaFilePath);
        }
        if (versionExists(org, pkg, version)) {
            throw new OciClientException("package '" + org + "/" + pkg + ":" + version
                    + "' already exists in the registry.");
        }
        try {
            String repositoryReference = repositoryReference(org, pkg);
            String imageReference = repositoryReference + ":" + version;
            Jib.fromScratch()
                    .setFormat(ImageFormat.OCI)
                    .addLayer(Collections.singletonList(balaFilePath), AbsoluteUnixPath.get("/"))
                    .containerize(
                            Containerizer.to(RegistryImage.named(imageReference)
                                            .addCredential(username, password))
                                    .setAllowInsecureRegistries(insecureRegistry)
                                    .setToolName("OciClient")

                    );
        } catch (InvalidImageReferenceException exception) {
            throw new OciClientException("invalid registry reference for " + org + "/" + pkg, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OciClientException("failed to push OCI artifact to the registry", exception);
        } catch (RegistryException | IOException | CacheDirectoryCreationException | ExecutionException exception) {
            throw new OciClientException("failed to push OCI artifact to the registry", exception);
        }
    }

    /**
     * Publishes the dependency graph for a just-pushed package version as an OCI referrer
     * artifact (OCI Distribution Spec v1.1 reference types) — a manifest whose {@code subject}
     * points at the version manifest, with the graph JSON as its one layer. Callers can then
     * fetch the graph directly via the referrers API without ever downloading the bala.
     *
     *
     * @param org                package organization
     * @param pkg                package name
     * @param version            package version, whose already-pushed manifest becomes the subject
     * @param dependencyGraphJson the package's {@code dependency-graph.json} bytes, as published
     *                            in its bala
     */
    public void pushDependencyGraphReferrer(String org, String pkg, String version, byte[] dependencyGraphJson) {
        FailoverHttpClient httpClient = null;
        try {
            ImageReference imageRef = ImageReference.parse(repositoryReference(org, pkg));
            httpClient = new FailoverHttpClient(insecureRegistry, insecureRegistry, logEvent -> { });

            RegistryClient registryClient = RegistryClient.factory(EventHandlers.NONE, imageRef.getRegistry(),
                        imageRef.getRepository(), httpClient)
                        .setCredential(Credential.from(username, password))
                        .newRegistryClient();
            registryClient.configureBasicAuth();

            byte[] subjectManifestBytes = fetchManifestBytes(imageRef, httpClient, version);
            String subjectDigest = "sha256:" + OciClientUtils.sha256Hex(subjectManifestBytes);

            DescriptorDigest emptyConfigDigest = DescriptorDigest.fromDigest(OCI_EMPTY_CONFIG_DIGEST);
            if (registryClient.checkBlob(emptyConfigDigest).isEmpty()) {
                registryClient.pushBlob(emptyConfigDigest, Blobs.from("{}"), null, count -> { });
            }
            DescriptorDigest layerDigest = DescriptorDigest.fromHash(OciClientUtils.sha256Hex(dependencyGraphJson));
            if (registryClient.checkBlob(layerDigest).isEmpty()) {
                registryClient.pushBlob(layerDigest, Blobs.from(new ByteArrayInputStream(dependencyGraphJson)),
                        null, count -> { });
            }

            String manifestText = OciClientUtils.buildManifestWithSubjectText(DEP_GRAPH_ARTIFACT_TYPE,
                    OCI_EMPTY_CONFIG_DIGEST, subjectDigest, subjectManifestBytes.length,
                    layerDigest.toString(), dependencyGraphJson.length);
            pushDependencyGraphManifest(imageRef, httpClient, manifestText);
        } catch (IOException | RegistryException | InvalidImageReferenceException | DigestException exception) {
            throw new OciClientException("failed to publish dependency graph referrer to the registry", exception);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.shutDown();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Fetches a manifest's raw bytes, exactly as stored on the registry — needed to compute its
     * own digest and size for use as a referrer's {@code subject} descriptor.
     *
     * @param imageRef   parsed repository reference to query
     * @param httpClient client to issue the request with
     * @param reference  tag or digest identifying the manifest
     * @return the manifest body bytes
     * @throws IOException on registry or connection failures
     */
    private byte[] fetchManifestBytes(ImageReference imageRef, FailoverHttpClient httpClient, String reference)
            throws IOException {
        URL url = URI.create(registryScheme() + imageRef.getRegistry() + "/v2/" + imageRef.getRepository()
                + "/manifests/" + reference).toURL();
        Authorization authorization = Authorization.fromBasicCredentials(username, password);
        Response response;
        try {
            response = httpClient.get(url, OciClientUtils.manifestRequest(authorization));
        } catch (ResponseException responseException) {
            if (responseException.getStatusCode() != 401) {
                throw responseException;
            }
            authorization = resolveBearerAuthorization(
                    responseException.getHeaders().getFirstHeaderStringValue("WWW-Authenticate"), httpClient);
            response = httpClient.get(url, OciClientUtils.manifestRequest(authorization));
        }
        try (Response ignored = response) {
            return OciClientUtils.readBody(response).getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Pushes a manifest by its own (self) digest — the standard, untagged form for a referrer
     * artifact, so it doesn't clutter the repository's tag list.
     *
     * @param imageRef     parsed repository reference to push to
     * @param httpClient   client to issue the request with
     * @param manifestText the manifest JSON text to push
     * @throws IOException on registry or connection failures
     */
    private void pushDependencyGraphManifest(ImageReference imageRef, FailoverHttpClient httpClient,
            String manifestText) throws IOException {
        byte[] body = manifestText.getBytes(StandardCharsets.UTF_8);
        String manifestDigest = "sha256:" + OciClientUtils.sha256Hex(body);
        URL url = URI.create(registryScheme() + imageRef.getRegistry() + "/v2/" + imageRef.getRepository()
                + "/manifests/" + manifestDigest).toURL();
        Authorization authorization = Authorization.fromBasicCredentials(username, password);

        try (Response ignored = OciClientUtils.putManifest(httpClient, url, authorization, body)) {
            return;
        } catch (ResponseException responseException) {
            if (responseException.getStatusCode() != 401) {
                throw responseException;
            }
            authorization = resolveBearerAuthorization(
                    responseException.getHeaders().getFirstHeaderStringValue("WWW-Authenticate"), httpClient);
            try (Response ignored = OciClientUtils.putManifest(httpClient, url, authorization, body)) {
                return;
            }
        }
    }

    /**
     * Builds the lowercased registry repository reference for a package.
     *
     * @param org package organization
     * @param pkg package name
     * @return {@code <registry>/<org>/<pkg>} in lowercase
     */
    private String repositoryReference(String org, String pkg) {
        return (registryUrl + "/" + org + "/" + pkg).toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the URL scheme to contact the registry with.
     *
     * @return {@code http://} for an insecure registry, {@code https://} otherwise
     */
    private String registryScheme() {
        return insecureRegistry ? "http://" : "https://";
    }

    /**
     * Checks whether a version tag already exists in the registry.
     *
     * @param org     package organization
     * @param pkg     package name
     * @param version version tag to check
     * @return true if the tag already exists
     */
    private boolean versionExists(String org, String pkg, String version) {
        try {
            return listTags(org, pkg).contains(version);
        } catch (OciClientException exception) {
            if (OciClientUtils.isNotFoundError(exception)) {
                return false;
            }

            throw new OciClientException("failed to verify whether '" + org + "/" + pkg + ":" + version
                    + "' already exists in the registry: " + OciClientUtils.describeFailure(exception), exception);
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
     * Pulls a bala from the registry into the given location, retrying on failure.
     *
     * @param org             package organization
     * @param name            package name
     * @param version         package version
     * @param repoLocation    directory to save the bala under
     * @param displayLocation path shown in the progress bar
     */
    public void pullMetadata(String org, String name, String version, String repoLocation, String displayLocation) {
        try {
            withRetry(() -> {
                doPullBala(org, name, version, repoLocation, displayLocation);
                return null;
            }, "pull bala [" + org + "/" + name + ":" + version + "]");
        } catch (Exception exception) {
            throw new OciClientException("failed to pull bala from the repo", exception);
        }
    }


    /**
     * Downloads the bala layer of a package version and writes it to disk.
     *
     * @param org             package organization
     * @param name            package name
     * @param version         package version
     * @param repoLocation    directory to save the bala under
     * @param displayLocation path shown in the progress bar
     * @throws Exception on registry or file system failures
     */
    private void doPullBala(String org, String name, String version, String repoLocation, String displayLocation)
            throws Exception {
        ImageReference imageRef = ImageReference.parse(repositoryReference(org, name));
        Consumer<LogEvent> jibLogger = logEvent -> { };

        FailoverHttpClient httpClient = new FailoverHttpClient(insecureRegistry, insecureRegistry, jibLogger);
        try {
            RegistryClient registryClient = RegistryClient.factory(EventHandlers.NONE, imageRef.getRegistry(),
                        imageRef.getRepository(), httpClient)
                        .setCredential(Credential.from(username, password))
                        .newRegistryClient();

            registryClient.configureBasicAuth();

            OciManifestTemplate manifestTemplate = registryClient
                    .pullManifest(version, OciManifestTemplate.class).getManifest();
            List<BuildableManifestTemplate.ContentDescriptorTemplate> layers = manifestTemplate.getLayers();
            if (layers.isEmpty()) {
                throw new OciClientException("no layers found in the OCI manifest for "
                        + org + "/" + name + ":" + version);
            }

            boolean enableOutputStream = Boolean.parseBoolean(
                    System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
            Path balaFilePath = Paths.get(repoLocation).resolve(org).resolve(name).resolve(version)
                    .resolve(name + "-" + version + BALA_EXTENSION);
            Path balaFileDir = balaFilePath.getParent();
            if (balaFileDir != null) {
                Files.createDirectories(balaFileDir);
            }
            Path blobTempFile = Files.createTempFile("ballerina-oci-blob-", ".tmp");
            try {
                boolean balaExtracted = false;
                for (BuildableManifestTemplate.ContentDescriptorTemplate layer : layers) {
                    final ProgressBar[] progressBar = {null};
                    Blob blob = registryClient.pullBlob(
                            layer.getDigest(),
                            size -> {
                                if (enableOutputStream) {

                                    long totalSizeInKB = size > 0 ? (size + 1023) / 1024 : -1;
                                    progressBar[0] = new ProgressBar(
                                            org + "/" + name + ":" + version + " [OCI Registry -> "
                                                    + displayLocation + "]",
                                            totalSizeInKB,
                                            1000,
                                            outStream,
                                            ProgressBarStyle.ASCII,
                                            " KB",
                                            1
                                    );
                                }
                            },
                            count -> {
                                if (enableOutputStream && progressBar[0] != null && count > 0) {
                                    long deltaKB = (count + 1023) / 1024;
                                    progressBar[0].stepBy(deltaKB);
                                }
                            }
                    );
                    try (OutputStream blobOutputStream = Files.newOutputStream(blobTempFile)) {
                        // Streamed straight to disk so an oversized blob can't exhaust the heap.
                        blob.writeTo(blobOutputStream);
                    } finally {
                        if (progressBar[0] != null) {
                            progressBar[0].close();
                        }
                    }
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
        } finally {
            try {
                httpClient.shutDown();
            } catch (IOException ignored) {
            }
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
        FailoverHttpClient httpClient = null;
        try {
            ImageReference imageRef = ImageReference.parse(repositoryReference(org, pkg));
            Consumer<LogEvent> jibLogger = logEvent -> { };

            httpClient = new FailoverHttpClient(insecureRegistry, insecureRegistry, jibLogger);

            RegistryClient registryClient = RegistryClient.factory(EventHandlers.NONE, imageRef.getRegistry(),
                        imageRef.getRepository(), httpClient)
                        .setCredential(Credential.from(username, password))
                        .newRegistryClient();

            registryClient.configureBasicAuth();

            OciManifestTemplate manifestTemplate = registryClient
                    .pullManifest("latest", OciManifestTemplate.class).getManifest();
            List<BuildableManifestTemplate.ContentDescriptorTemplate> layers = manifestTemplate.getLayers();
            for (BuildableManifestTemplate.ContentDescriptorTemplate layer : layers) {
                Blob blob = registryClient.pullBlob(
                        layer.getDigest(),
                        size -> { },
                        count -> { }
                );

                byte[] blobBytes = Blobs.writeToByteArray(blob);
                String text = new String(blobBytes, StandardCharsets.UTF_8);
                List<String> parsedVersions = new Gson().fromJson(text, new TypeToken<List<String>>() { }.getType());
                return parsedVersions != null ? parsedVersions : Collections.emptyList();
            }
            return Collections.emptyList();
        } catch (IOException | RegistryException | InvalidImageReferenceException
                | JsonSyntaxException exception) {
            throw new OciClientException("failed to pull metadata from the registry", exception);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.shutDown();
                } catch (IOException ignored) {
                    // best effort — the connection pool is reclaimed on GC anyway
                }
            }
        }
    }


    /**
     * Fetches the dependency graph for a package version via the OCI referrers API (OCI
     * Distribution Spec v1.1 reference types), if the registry publishes one.
     *
     *
     * @param org     package organization
     * @param pkg     package name
     * @param version package version
     * @return the raw {@code dependency-graph.json} content published as a referrer, or empty if
     *         unavailable
     */
    public Optional<String> pullDependencyGraph(String org, String pkg, String version) {
        FailoverHttpClient httpClient = null;
        try {
            ImageReference imageRef = ImageReference.parse(repositoryReference(org, pkg));
            httpClient = new FailoverHttpClient(insecureRegistry, insecureRegistry, logEvent -> { });

            RegistryClient registryClient = RegistryClient.factory(EventHandlers.NONE, imageRef.getRegistry(),
                        imageRef.getRepository(), httpClient)
                        .setCredential(Credential.from(username, password))
                        .newRegistryClient();
            registryClient.configureBasicAuth();

            ManifestAndDigest<OciManifestTemplate> subjectManifest = registryClient
                    .pullManifest(version, OciManifestTemplate.class);
            String subjectDigest = subjectManifest.getDigest().toString();

            List<ManifestDescriptor> referrers = pullReferrers(imageRef, httpClient, subjectDigest,
                    DEP_GRAPH_ARTIFACT_TYPE);
            Optional<ManifestDescriptor> dependencyGraphReferrer = referrers.stream()
                    .filter(referrer -> DEP_GRAPH_ARTIFACT_TYPE.equals(referrer.artifactType()))
                    .findFirst();
            if (dependencyGraphReferrer.isEmpty()) {
                return Optional.empty();
            }

            OciManifestTemplate referrerManifest = registryClient
                    .pullManifest(dependencyGraphReferrer.get().digest(), OciManifestTemplate.class).getManifest();
            List<BuildableManifestTemplate.ContentDescriptorTemplate> layers = referrerManifest.getLayers();
            if (layers.isEmpty()) {
                return Optional.empty();
            }

            Blob blob = registryClient.pullBlob(layers.get(0).getDigest(), size -> { }, count -> { });
            return Optional.of(new String(Blobs.writeToByteArray(blob), StandardCharsets.UTF_8));
        } catch (IOException | RegistryException | InvalidImageReferenceException exception) {
            throw new OciClientException("failed to pull dependency graph from the registry", exception);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.shutDown();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Queries the OCI referrers API ({@code GET /v2/{name}/referrers/{digest}}) for artifacts
     * whose manifest {@code subject} field points at {@code subjectDigest}.
     *
     * @param imageRef      parsed repository reference to query
     * @param httpClient    client to issue the request with
     * @param subjectDigest digest of the manifest to find referrers for
     * @param artifactType  filters the results to this artifact type
     * @return the matching referrer descriptors, or empty if none are published
     * @throws IOException on registry or connection failures other than a 404
     */
    private List<ManifestDescriptor> pullReferrers(ImageReference imageRef, FailoverHttpClient httpClient,
            String subjectDigest, String artifactType) throws IOException {
        List<ManifestDescriptor> referrers = new ArrayList<>();
        String path = "/v2/" + imageRef.getRepository() + "/referrers/" + subjectDigest
                + "?artifactType=" + URLEncoder.encode(artifactType, StandardCharsets.UTF_8);
        URL url = URI.create(registryScheme() + imageRef.getRegistry() + path).toURL();
        URL registryOrigin = url;
        Authorization authorization = Authorization.fromBasicCredentials(username, password);

        while (url != null) {
            Response response;
            try {
                response = httpClient.get(url, Request.builder().setAuthorization(authorization).build());
            } catch (ResponseException responseException) {
                if (responseException.getStatusCode() == 404) {
                    return Collections.emptyList();
                }
                if (responseException.getStatusCode() != 401) {
                    throw responseException;
                }
                authorization = resolveBearerAuthorization(
                        responseException.getHeaders().getFirstHeaderStringValue("WWW-Authenticate"), httpClient);
                try {
                    response = httpClient.get(url, Request.builder().setAuthorization(authorization).build());
                } catch (ResponseException retryException) {
                    if (retryException.getStatusCode() == 404) {
                        return Collections.emptyList();
                    }
                    throw retryException;
                }
            }

            try (Response ignored = response) {
                String responseBody = OciClientUtils.readBody(response);
                ReferrersResponse referrersResponse = OciClientUtils.parseJson(responseBody, ReferrersResponse.class,
                        "referrers response for " + imageRef.getRepository());
                referrers.addAll(referrersResponse.manifests());
                url = OciClientUtils.nextPageUrl(response, url, registryOrigin);
            }
        }
        return referrers;
    }

    /**
     * Lists the version tags of a package repository, following pagination.
     *
     * @param org package organization
     * @param pkg package name
     * @return the SemVer tags across all result pages
     */
    public List<String> listTags(String org, String pkg) {
        List<String> versions = new ArrayList<>();
        FailoverHttpClient httpClient = null;
        try {
            ImageReference imageRef = ImageReference.parse(repositoryReference(org, pkg));
            httpClient = new FailoverHttpClient(insecureRegistry, insecureRegistry, logEvent -> { });
            URL url = URI.create(
                    registryScheme() + imageRef.getRegistry() + "/v2/" + imageRef.getRepository()
                            + "/tags/list").toURL();
            URL registryOrigin = url;
            Authorization authorization = Authorization.fromBasicCredentials(username, password);

            while (url != null) {
                Response response;
                try {
                    response = httpClient.get(url, Request.builder().setAuthorization(authorization).build());
                } catch (ResponseException responseException) {
                    if (responseException.getStatusCode() != 401) {
                        throw responseException;
                    }
                    authorization = resolveBearerAuthorization(
                            responseException.getHeaders().getFirstHeaderStringValue("WWW-Authenticate"),
                            httpClient);
                    response = httpClient.get(url, Request.builder().setAuthorization(authorization).build());
                }

                try (Response ignored = response) {
                    String responseBody = OciClientUtils.readBody(response);
                    TagsListResponse tagsList = OciClientUtils.parseJson(responseBody, TagsListResponse.class,
                            "tags list response for " + org + "/" + pkg);
                    if (tagsList.tags() != null) {
                        tagsList.tags().stream()
                                .filter(tag -> VERSION_TAG_PATTERN.matcher(tag).matches())
                                .forEach(versions::add);
                    }
                    url = OciClientUtils.nextPageUrl(response, url, registryOrigin);
                }
            }
            return versions;
        } catch (OciClientException exception) {
            throw exception;
        } catch (IOException | InvalidImageReferenceException exception) {
            throw new OciClientException("failed to list tags from the registry", exception);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.shutDown();
                } catch (IOException ignored) {
                }
            }
        }
    }


    /**
     * Exchanges a bearer challenge for a token authorization.
     *
     * @param wwwAuthenticate the {@code WWW-Authenticate} challenge header
     * @param httpClient      client used to call the token endpoint
     * @return bearer authorization for retrying the request
     * @throws IOException if the token endpoint cannot be reached
     */
    private Authorization resolveBearerAuthorization(String wwwAuthenticate, FailoverHttpClient httpClient)
            throws IOException {
        if (wwwAuthenticate == null || !wwwAuthenticate.regionMatches(true, 0, "Bearer", 0, "Bearer".length())) {
            throw new OciClientException("unsupported or missing authentication challenge: " + wwwAuthenticate);
        }
        Map<String, String> challengeParams = new HashMap<>();
        Matcher matcher = AUTH_CHALLENGE_PARAM_PATTERN.matcher(wwwAuthenticate);
        while (matcher.find()) {
            challengeParams.put(matcher.group(1), matcher.group(2));
        }
        String realm = challengeParams.get("realm");
        if (realm == null) {
            throw new OciClientException("bearer challenge is missing 'realm': " + wwwAuthenticate);
        }
        URI realmUri;
        try {
            realmUri = URI.create(realm);
        } catch (IllegalArgumentException e) {
            throw new OciClientException("invalid token realm in bearer challenge: " + realm, e);
        }
        // Credentials may only leave over plain HTTP when the registry was explicitly configured as insecure.
        if (!"https".equalsIgnoreCase(realmUri.getScheme()) && !insecureRegistry) {
            throw new OciClientException("refusing to send credentials to a non-HTTPS token realm: " + realm);
        }

        StringBuilder tokenUrl = new StringBuilder(realm).append(realm.contains("?") ? '&' : '?');
        if (challengeParams.containsKey("service")) {
            tokenUrl.append("service=")
                    .append(URLEncoder.encode(challengeParams.get("service"), StandardCharsets.UTF_8))
                    .append('&');
        }
        if (challengeParams.containsKey("scope")) {
            tokenUrl.append("scope=")
                    .append(URLEncoder.encode(challengeParams.get("scope"), StandardCharsets.UTF_8));
        }

        Request tokenRequest = Request.builder()
                .setAuthorization(Authorization.fromBasicCredentials(username, password))
                .build();
        try (Response tokenResponse = httpClient.get(URI.create(tokenUrl.toString()).toURL(), tokenRequest)) {
            TokenResponse token = OciClientUtils.parseJson(
                    OciClientUtils.readBody(tokenResponse), TokenResponse.class,
                    "token response from " + realm);
            String bearerToken = token.token() != null ? token.token() : token.accessToken();
            if (bearerToken == null) {
                throw new OciClientException("token endpoint returned no token: " + realm);
            }
            return Authorization.fromBearerToken(bearerToken);
        }
    }

}
