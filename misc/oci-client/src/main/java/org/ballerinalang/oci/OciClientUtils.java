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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * Shared helpers for reading and parsing HTTP responses from an OCI registry.
 *
 */
final class OciClientUtils {

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
     * Recovers the raw bala (zip) bytes from an OCI layer blob, unwrapping a (gzipped) tar if needed.
     *
     * @param blobBytes the layer blob content as pulled from the registry
     * @return the bala file bytes
     * @throws IOException if the blob contains no bala
     */
    static byte[] extractBalaFromLayer(byte[] blobBytes) throws IOException {
        if (blobBytes.length >= 2 && blobBytes[0] == 'P' && blobBytes[1] == 'K') {
            // already raw bala (zip) bytes
            return blobBytes;
        }
        byte[] tarBytes = blobBytes;
        if (blobBytes.length >= 2 && (blobBytes[0] & 0xFF) == 0x1F && (blobBytes[1] & 0xFF) == 0x8B) {
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(blobBytes))) {
                tarBytes = gzipInputStream.readAllBytes();
            }
        }
        byte[] balaBytes = firstRegularFileFromTar(tarBytes);
        if (balaBytes == null) {
            throw new IOException("OCI layer blob is not a bala: expected a zip, or a (gzipped) tar "
                    + "archive containing the bala file, but no file entry was found");
        }
        return balaBytes;
    }

    /**
     * Returns the first regular file entry in a tar archive, preferring a {@code .bala} entry.
     *
     * @param tarBytes the tar archive bytes
     * @return the file content, or null if none is found
     */
    private static byte[] firstRegularFileFromTar(byte[] tarBytes) {
        byte[] firstRegularFile = null;
        int offset = 0;
        while (offset + 512 <= tarBytes.length) {
            if (isZeroBlock(tarBytes, offset)) {
                break;
            }
            String entryName = readTarString(tarBytes, offset, 100);
            long entrySize;
            try {
                entrySize = readTarOctal(tarBytes, offset + 124, 12);
            } catch (NumberFormatException e) {
                return firstRegularFile; // not a tar header — stop walking
            }
            byte typeFlag = tarBytes[offset + 156];
            int dataStart = offset + 512;
            boolean isRegularFile = (typeFlag == '0' || typeFlag == 0) && !entryName.endsWith("/");
            if (isRegularFile && entrySize > 0 && dataStart + entrySize <= tarBytes.length) {
                byte[] content = Arrays.copyOfRange(tarBytes, dataStart, (int) (dataStart + entrySize));
                if (entryName.endsWith(".bala")) {
                    return content;
                }
                if (firstRegularFile == null) {
                    firstRegularFile = content;
                }
            }
            offset = dataStart + (int) ((entrySize + 511) / 512) * 512;
        }
        return firstRegularFile;
    }

    /**
     * Checks whether a 512-byte tar block is all zeros.
     *
     * @param bytes  the archive bytes
     * @param offset block start offset
     * @return true if the block is all zeros
     */
    private static boolean isZeroBlock(byte[] bytes, int offset) {
        for (int i = 0; i < 512; i++) {
            if (bytes[offset + i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads a NUL-terminated string from a tar header field.
     *
     * @param bytes     the archive bytes
     * @param offset    field start offset
     * @param maxLength maximum field length
     * @return the decoded string
     */
    private static String readTarString(byte[] bytes, int offset, int maxLength) {
        int end = offset;
        while (end < offset + maxLength && bytes[end] != 0) {
            end++;
        }
        return new String(bytes, offset, end - offset, StandardCharsets.UTF_8);
    }

    /**
     * Parses an octal number from a tar header field.
     *
     * @param bytes  the archive bytes
     * @param offset field start offset
     * @param length field length
     * @return the parsed value
     */
    private static long readTarOctal(byte[] bytes, int offset, int length) {
        String value = new String(bytes, offset, length, StandardCharsets.US_ASCII)
                .replace("\0", "").trim();
        if (value.isEmpty()) {
            return 0;
        }
        return Long.parseLong(value, 8);
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
