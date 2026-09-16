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

import com.google.cloud.tools.jib.blob.Blobs;
import com.google.cloud.tools.jib.http.Authorization;
import com.google.cloud.tools.jib.http.BlobHttpContent;
import com.google.cloud.tools.jib.http.FailoverHttpClient;
import com.google.cloud.tools.jib.http.Request;
import com.google.cloud.tools.jib.http.Response;
import com.google.cloud.tools.jib.http.ResponseException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Shared helpers for reading/parsing OCI registry responses and publishing pulled balas to the bala cache.
 */
public final class OciClientUtils {

    static final String OCI_MANIFEST_MEDIA_TYPE = "application/vnd.oci.image.manifest.v1+json";

    private static final Pattern LINK_NEXT_PATTERN = Pattern.compile("<([^>]+)>;\\s*rel=\"next\"");

    private OciClientUtils() {
    }

    /**
     * Reads an HTTP response body fully into a string.
     *
     * @param response the response to read
     * @return the body as a UTF-8 string
     * @throws IOException if reading the body fails
     */
    static String readBody(Response response) throws IOException {
        try (InputStream body = response.getBody()) {
            return new String(body.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Recovers the bala (zip) from an OCI layer blob (unwrapping a gzipped tar), streaming it to balaOutputFile.
     *
     * @param blobFile       the layer blob content as pulled from the registry
     * @param balaOutputFile where to write the recovered bala; its parent directory must exist
     * @return true if a bala was found in the blob and written; false if the blob holds no bala
     * @throws IOException if reading the blob fails
     */
    static boolean extractBalaFromLayer(Path blobFile, Path balaOutputFile) throws IOException {
        byte[] magic = peekMagic(blobFile);
        if (isZip(magic)) {
            // already a raw bala (zip)
            Files.copy(blobFile, balaOutputFile, StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        try (InputStream fileStream = Files.newInputStream(blobFile);
                InputStream layerStream = isGzip(magic) ? new GZIPInputStream(fileStream) : fileStream;
                TarArchiveInputStream tar = new TarArchiveInputStream(layerStream)) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.isFile() && entry.getName().endsWith(".bala")) {
                    Files.copy(tar, balaOutputFile, StandardCopyOption.REPLACE_EXISTING);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Reads the first two bytes of a file, used to sniff its format.
     *
     * @param file the file to peek at
     * @return the leading two bytes, or fewer if the file is shorter
     * @throws IOException if the file cannot be read
     */
    private static byte[] peekMagic(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return in.readNBytes(2);
        }
    }

    private static boolean isZip(byte[] magic) {
        return magic.length == 2 && magic[0] == 'P' && magic[1] == 'K';
    }

    private static boolean isGzip(byte[] magic) {
        return magic.length == 2 && (magic[0] & 0xFF) == 0x1F && (magic[1] & 0xFF) == 0x8B;
    }

    /**
     * Extracts a .bala into a bala cache atomically, via a sibling temp directory renamed into place.
     *
     * @param balaFilePath .bala file path
     * @param versionDir   the {@code <org>/<name>/<version>} directory of the bala cache
     * @param platform     the platform directory name inside the version directory
     * @throws IOException if extraction or the rename fails
     */
    public static void extractBalaToBalaCache(Path balaFilePath, Path versionDir, String platform)
            throws IOException {
        Path versionTempDir = versionDir.resolveSibling(versionDir.getFileName() + "_temp");
        Path platformDir = versionTempDir.resolve(platform).normalize();
        if (!versionTempDir.equals(platformDir.getParent())) {
            throw new IOException("invalid platform in package metadata: " + platform);
        }
        Path versionBackupDir = versionDir.resolveSibling(versionDir.getFileName() + "_old");

        if (Files.exists(versionBackupDir) && !Files.exists(versionDir)) {
            moveDirectory(versionBackupDir, versionDir);
        }

        try {
            deleteDirectory(versionTempDir);
            deleteDirectory(versionBackupDir);
            extractZip(balaFilePath, platformDir);

            boolean hadExisting = Files.exists(versionDir);
            if (hadExisting) {
                moveDirectory(versionDir, versionBackupDir);
            }
            try {
                moveDirectory(versionTempDir, versionDir);
            } catch (IOException e) {
                if (hadExisting && !Files.exists(versionDir)) {
                    moveDirectory(versionBackupDir, versionDir);
                }
                throw e;
            }
        } finally {
            deleteDirectory(versionTempDir);
            deleteDirectory(versionBackupDir);
        }
    }

    /**
     * Renames a directory, falling back to a non-atomic move on filesystems that lack atomic rename.
     *
     * @param source the directory to move
     * @param target the destination path
     * @throws IOException if the move fails
     */
    private static void moveDirectory(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target);
        }
    }

    /**
     * Extracts a zip archive into the given directory.
     *
     * @param zipFilePath the zip file to extract
     * @param destDir     directory to extract into
     * @throws IOException if extraction fails or an entry escapes the destination directory
     */
    private static void extractZip(Path zipFilePath, Path destDir) throws IOException {
        Path normalizedDestDir = destDir.toAbsolutePath().normalize();
        Files.createDirectories(normalizedDestDir);
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path outputPath = normalizedDestDir.resolve(entry.getName()).normalize();
                if (!outputPath.startsWith(normalizedDestDir)) {
                    throw new IOException("zip entry escapes the extraction directory: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(outputPath);
                } else {
                    Path parent = outputPath.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(zipInputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Deletes a directory tree if it exists; best effort, failures are ignored and surface later instead.
     *
     * @param directory the directory to delete
     */
    private static void deleteDirectory(Path directory) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException ignored) {
                    // best effort
                }
            });
        } catch (IOException | UncheckedIOException ignored) {
            // best effort
        }
    }

    /**
     * Parses JSON into the given type, wrapping parse failures in an {@link OciClientException}.
     *
     * @param json    the JSON text
     * @param type    the target type
     * @param context description used in error messages
     * @param <T>     the target type
     * @return the parsed value
     */
    static <T> T parseJson(String json, Class<T> type, String context) {
        try {
            T value = new Gson().fromJson(json, type);
            if (value == null) {
                throw new OciClientException("empty response body while parsing " + context);
            }
            return value;
        } catch (JsonSyntaxException exception) {
            String snippet = json.length() > 200 ? json.substring(0, 200) + "..." : json;
            throw new OciClientException(
                    "unexpected (non-JSON) response while parsing " + context + ": " + snippet, exception);
        }
    }

    /**
     * Sets a system property only if it is not already set, so an explicit JVM-level override always wins.
     *
     * @param key   the system property name
     * @param value the value to set if the property is absent
     */
    static void setPropertyIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    /**
     * Computes the lowercase hex-encoded SHA-256 hash of the given bytes.
     *
     * @param content bytes to hash
     * @return the hex-encoded hash, without an algorithm prefix
     */
    static String sha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new OciClientException("SHA-256 algorithm is not available", exception);
        }
    }

    /**
     * Builds the JSON text of a referrer manifest whose {@code subject} points at another manifest.
     *
     * @param artifactType      the referrer artifact type, used for both the manifest and its one layer
     * @param emptyConfigDigest digest of the shared empty OCI config blob
     * @param subjectDigest     digest of the subject manifest
     * @param subjectSize       byte size of the subject manifest
     * @param layerDigest       digest of the referrer's payload blob
     * @param layerSize         byte size of the referrer's payload blob
     * @return the manifest JSON text
     */
    static String buildManifestWithSubjectText(String artifactType, String emptyConfigDigest,
            String subjectDigest, int subjectSize, String layerDigest, int layerSize) {
        return "{"
                + "\"schemaVersion\":2,"
                + "\"mediaType\":\"" + OCI_MANIFEST_MEDIA_TYPE + "\","
                + "\"artifactType\":\"" + artifactType + "\","
                + "\"config\":{"
                + "\"mediaType\":\"application/vnd.oci.empty.v1+json\","
                + "\"size\":2,"
                + "\"digest\":\"" + emptyConfigDigest + "\"},"
                + "\"layers\":[{"
                + "\"mediaType\":\"" + artifactType + "\","
                + "\"size\":" + layerSize + ","
                + "\"digest\":\"" + layerDigest + "\"}],"
                + "\"subject\":{"
                + "\"mediaType\":\"" + OCI_MANIFEST_MEDIA_TYPE + "\","
                + "\"digest\":\"" + subjectDigest + "\","
                + "\"size\":" + subjectSize + "}"
                + "}";
    }

    /**
     * Builds a GET request for an OCI manifest.
     *
     * <p>Some registries (e.g. Harbor) refuse to serve an OCI manifest unless it is explicitly accepted — a bare
     * "*&#47;*" Accept header (FailoverHttpClient's default) gets a 404 back with "accept header does not support
     * OCI manifests", even though the manifest exists.
     *
     * @param authorization authorization to send with the request
     * @return the manifest GET request
     */
    static Request manifestRequest(Authorization authorization) {
        return Request.builder()
                .setAuthorization(authorization)
                .setAccept(Collections.singletonList(OCI_MANIFEST_MEDIA_TYPE))
                .build();
    }

    /**
     * Puts a manifest body to the registry.
     *
     * @param httpClient    client to issue the request with
     * @param url           the manifest URL to push to
     * @param authorization authorization to send with the request
     * @param body          the manifest JSON bytes
     * @return the registry's response; the caller is responsible for closing it
     * @throws IOException on registry or connection failures
     */
    static Response putManifest(FailoverHttpClient httpClient, URL url, Authorization authorization, byte[] body)
            throws IOException {
        Request request = Request.builder()
                .setAuthorization(authorization)
                .setBody(new BlobHttpContent(Blobs.from(new ByteArrayInputStream(body)), OCI_MANIFEST_MEDIA_TYPE))
                .build();
        return httpClient.put(url, request);
    }

    /**
     * Checks whether a failure was caused by an HTTP 404 response.
     *
     * @param throwable the failure to inspect
     * @return true if a 404 response is found in the cause chain
     */
    static boolean isNotFoundError(Throwable throwable) {
        ResponseException responseException = findResponseException(throwable);
        return responseException != null && responseException.getStatusCode() == 404;
    }

    /**
     * Describes the deepest useful cause of a failure: HTTP status and body if any, otherwise the exception itself.
     *
     * @param throwable the failure to describe
     * @return a one-line description of the root cause
     */
    static String describeFailure(Throwable throwable) {
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
     * Resolves the next page URL from a {@code Link} header.
     *
     *
     * @param response       the current paginated response
     * @param currentUrl     the URL of the current page
     * @param registryOrigin the registry URL the pull started from
     * @return the next page URL, or null when there are no more pages
     * @throws IOException if the pagination link is invalid, or points outside the registry's origin
     */
    static URL nextPageUrl(Response response, URL currentUrl, URL registryOrigin) throws IOException {
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
