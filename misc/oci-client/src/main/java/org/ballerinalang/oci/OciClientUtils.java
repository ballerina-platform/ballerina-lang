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

import com.google.cloud.tools.jib.http.Response;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Shared helpers for reading/parsing OCI registry responses and publishing pulled balas to the bala cache.
 */
public final class OciClientUtils {

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
}
