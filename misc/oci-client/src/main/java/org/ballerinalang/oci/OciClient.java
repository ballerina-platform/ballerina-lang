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
import com.google.cloud.tools.jib.registry.RegistryClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.oci.model.TagsListResponse;
import org.ballerinalang.oci.model.TokenResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client for pushing and pulling Ballerina packages to and from an OCI registry.
 *
 */
public class OciClient {

    private static final String BALA_EXTENSION = ".bala";
    private static final Logger LOGGER = Logger.getLogger(OciClient.class.getName());
    private static final int MAX_PULL_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    private static final Pattern VERSION_TAG_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[0-9A-Za-z.-]+)?$");
    private static final Pattern LINK_NEXT_PATTERN = Pattern.compile("<([^>]+)>;\\s*rel=\"next\"");
    private static final Pattern AUTH_CHALLENGE_PARAM_PATTERN = Pattern.compile("(\\w+)=\"([^\"]*)\"");
    private static final Pattern LOOPBACK_IPV4_PATTERN = Pattern.compile("127(?:\\.\\d{1,3}){3}");

    private String registryUrl;
    private String username;
    private String password;

    /**
     * Creates an OCI registry client.
     *
     * @param registryUrl registry host and base path (any URL scheme is stripped)
     * @param username    registry username
     * @param password    registry password or access token
     */
    public OciClient(String registryUrl, String username, String password) {
        if (registryUrl != null) {
            this.registryUrl = registryUrl.replaceFirst("^(http://|https://)", "");
        }
        this.username = username;
        this.password = password;
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
        setPropertyIfAbsent("http.proxyHost", host);
        setPropertyIfAbsent("http.proxyPort", String.valueOf(port));
        setPropertyIfAbsent("https.proxyHost", host);
        setPropertyIfAbsent("https.proxyPort", String.valueOf(port));
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            setPropertyIfAbsent("http.proxyUser", username);
            setPropertyIfAbsent("http.proxyPassword", password);
            setPropertyIfAbsent("https.proxyUser", username);
            setPropertyIfAbsent("https.proxyPassword", password);
        }
    }

    private static void setPropertyIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    /**
     * Runs an action with retries and exponential backoff.
     *
     * @param action        action to run
     * @param operationName operation name used in log messages
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
                LOGGER.log(Level.WARNING,
                        "[OciClient] {0} failed (attempt {1}/{2}), retrying in {3}ms: {4}",
                        new Object[]{operationName, attempt, MAX_PULL_RETRIES, delayMs, e.getMessage()});
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
            ImageReference imageRef = ImageReference.parse(repositoryReference);
            String imageReference = repositoryReference + ":" + version;
            Jib.fromScratch()
                    .setFormat(ImageFormat.OCI)
                    .addLayer(Collections.singletonList(balaFilePath), AbsoluteUnixPath.get("/"))
                    .containerize(
                            Containerizer.to(RegistryImage.named(imageReference)
                                            .addCredential(username, password))
                                    .setAllowInsecureRegistries(isLoopbackRegistry(imageRef))
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
     * Reports whether a registry is reachable only on the local machine (e.g. a local Nexus/Harbor instance).
     *
     *
     * @param imageRef the parsed image reference for the registry being contacted
     * @return true if the registry host is, or resolves only to, a loopback address
     */
    private static boolean isLoopbackRegistry(ImageReference imageRef) {
        return resolvesToLoopback(registryHost(imageRef.getRegistry()));
    }

    /**
     * Checks whether a host is a literal loopback address or resolves exclusively to loopback addresses.
     *
     * @param host the bare hostname or IP address
     * @return true if the host is, or resolves only to, a loopback address
     */
    private static boolean resolvesToLoopback(String host) {
        if (host == null || host.isEmpty()) {
            return false;
        }
        if (isLoopbackHost(host)) {
            return true;
        }
        try {
            return Arrays.stream(InetAddress.getAllByName(host)).allMatch(InetAddress::isLoopbackAddress);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Strips an optional {@code :port} suffix from a {@code host} or {@code [ipv6]:port} value.
     *
     * @param registry the registry authority, as returned by {@link ImageReference#getRegistry()}
     * @return the bare host, without a port
     */
    private static String registryHost(String registry) {
        if (registry == null) {
            return "";
        }
        if (registry.startsWith("[")) {
            int end = registry.indexOf(']');
            return end > 0 ? registry.substring(1, end) : registry;
        }
        int colon = registry.lastIndexOf(':');
        if (colon > 0 && registry.substring(colon + 1).chars().allMatch(Character::isDigit)) {
            return registry.substring(0, colon);
        }
        return registry;
    }

    /**
     * Checks whether a host refers to the local machine.
     *
     * @param host the bare hostname or IP address
     * @return true if {@code host} is {@code localhost}, an IPv4 127.0.0.0/8 address, or the
     *         IPv6 loopback address
     */
    private static boolean isLoopbackHost(String host) {
        return host.equalsIgnoreCase("localhost")
                || host.equals("::1")
                || host.equals("0:0:0:0:0:0:0:1")
                || LOOPBACK_IPV4_PATTERN.matcher(host).matches();
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
            if (isNotFoundError(exception)) {
                return false;
            }

            throw new OciClientException("failed to verify whether '" + org + "/" + pkg + ":" + version
                    + "' already exists in the registry: " + describeFailure(exception), exception);
        }
    }

    /**
     * Checks whether a failure was caused by an HTTP 404 response.
     *
     * @param throwable the failure to inspect
     * @return true if a 404 response is found in the cause chain
     */
    private static boolean isNotFoundError(Throwable throwable) {
        ResponseException responseException = findResponseException(throwable);
        return responseException != null && responseException.getStatusCode() == 404;
    }

    /**
     * Describes the deepest useful cause of a failure: HTTP status and body if any, otherwise the exception itself.
     *
     * @param throwable the failure to describe
     * @return a one-line description of the root cause
     */
    private static String describeFailure(Throwable throwable) {
        ResponseException responseException = findResponseException(throwable);
        if (responseException != null) {
            String content = responseException.getContent();
            if (content == null || content.isBlank()) {
                return "HTTP " + responseException.getStatusCode();
            }
            String snippet = content.length() > 200 ? content.substring(0, 200) + "..." : content;
            return "HTTP " + responseException.getStatusCode() + " - " + snippet;
        }
        Throwable last = throwable;
        while (last.getCause() != null) {
            last = last.getCause();
        }
        String message = last.getMessage();
        return last.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }

    /**
     * Finds the first {@link ResponseException} in a failure's cause chain.
     *
     * @param throwable the failure to inspect
     * @return the response exception, or null if none is found in the chain
     */
    private static ResponseException findResponseException(Throwable throwable) {
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause instanceof ResponseException responseException) {
                return responseException;
            }
        }
        return null;
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

        boolean insecure = isLoopbackRegistry(imageRef);
        FailoverHttpClient httpClient = new FailoverHttpClient(insecure, insecure, jibLogger);
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
            Path blobTempFile = Files.createTempFile("ballerina-oci-blob-", ".tmp");
            try {
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
                                            System.out,
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
                }

                Path outputPath = Paths.get(repoLocation);
                Path balaFilePath = outputPath.resolve(org).resolve(name).resolve(version)
                        .resolve(name + "-" + version + BALA_EXTENSION);
                Path balaFileDir = balaFilePath.getParent();
                if (balaFileDir != null) {
                    Files.createDirectories(balaFileDir);
                }
                // Jib stores each pushed file inside a gzipped tar layer, so the pulled blob is a
                // tar.gz wrapping the bala — unwrap it back to the raw bala (zip) before saving.
                OciClientUtils.extractBalaFromLayer(blobTempFile, balaFilePath);
            } finally {
                Files.deleteIfExists(blobTempFile);
            }
        } finally {
            try {
                httpClient.shutDown();
            } catch (IOException ignored) {
                // best effort — the connection pool is reclaimed on GC anyway
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

            boolean insecure = isLoopbackRegistry(imageRef);
            httpClient = new FailoverHttpClient(insecure, insecure, jibLogger);

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
                // Gson returns null (rather than throwing) for an empty or literal-null blob.
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
            boolean insecure = isLoopbackRegistry(imageRef);
            httpClient = new FailoverHttpClient(insecure, insecure, logEvent -> { });
            URL url = URI.create(
                    "https://" + imageRef.getRegistry() + "/v2/" + imageRef.getRepository()
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
                    url = nextPageUrl(response, url, registryOrigin);
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
        // The registry chooses the realm, so require HTTPS (or a loopback host) before sending
        // credentials to it: a compromised registry must not be able to redirect them over plaintext.
        if (!"https".equalsIgnoreCase(realmUri.getScheme()) && !resolvesToLoopback(realmUri.getHost())) {
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

    /**
     * Resolves the next page URL from a {@code Link} header.
     *
     * <p>The resolved URL is required to share the registry's scheme, host, and effective port:
     * a registry could otherwise point pagination at an arbitrary host via the {@code Link}
     * header, and the next request would carry the current Basic/Bearer authorization to it.
     *
     * @param response       the current tags list response
     * @param currentUrl     the URL of the current page
     * @param registryOrigin the registry URL the pull started from
     * @return the next page URL, or null when there are no more pages
     * @throws IOException if the pagination link is invalid, or points outside the registry's origin
     */
    private URL nextPageUrl(Response response, URL currentUrl, URL registryOrigin) throws IOException {
        for (String linkHeader : response.getHeader("Link")) {
            Matcher matcher = LINK_NEXT_PATTERN.matcher(linkHeader);
            if (matcher.find()) {
                URL nextUrl;
                try {
                    nextUrl = currentUrl.toURI().resolve(matcher.group(1)).toURL();
                } catch (URISyntaxException e) {
                    throw new IOException("invalid pagination link: " + matcher.group(1), e);
                }
                if (!isSameOrigin(registryOrigin, nextUrl)) {
                    throw new IOException("refusing to follow pagination link to a different origin: " + nextUrl);
                }
                return nextUrl;
            }
        }
        return null;
    }

    /**
     * Checks whether two URLs share the same scheme, host, and effective port.
     *
     * @param a the first URL
     * @param b the second URL
     * @return true if {@code a} and {@code b} are the same origin
     */
    private static boolean isSameOrigin(URL a, URL b) {
        return a.getProtocol().equalsIgnoreCase(b.getProtocol())
                && a.getHost().equalsIgnoreCase(b.getHost())
                && effectivePort(a) == effectivePort(b);
    }

    /**
     * Returns a URL's port, substituting the protocol's default port when none is specified.
     *
     * @param url the URL to inspect
     * @return the effective port
     */
    private static int effectivePort(URL url) {
        return url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    }

}
