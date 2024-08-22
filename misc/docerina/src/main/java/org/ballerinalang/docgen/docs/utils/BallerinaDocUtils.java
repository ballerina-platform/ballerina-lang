/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.docs.utils;

import org.ballerinalang.docgen.docs.BallerinaDocConstants;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Util methods used for doc generation.
 */
public class BallerinaDocUtils {

    private static final boolean debugEnabled = "true".equals(System.getProperty(
            BallerinaDocConstants.ENABLE_DEBUG_LOGS));
    private static final PrintStream out = System.out;

    public static void packageToZipFile(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            //Ignore
                        }
                    });
        }
    }

    /**
     * Calculates the hash of the file, which is represented as an array of bytes, using the specified algorithm.
     *
     * @param contentInBytes content to hash, provided as an array of bytes
     * @param algorithm hashing algorithm to use
     * @return hash value of the content
     * @throws IllegalArgumentException if the specified algorithm is unavailable.
     */
    public static byte[] getHash(byte[] contentInBytes, String algorithm) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        return digest.digest(contentInBytes);
    }

    /**
     * Converts an array of bytes to hexadecimal string.
     *
     * @param bytes byte array to convert
     * @return hexadecimal representation of the byte array
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static String getSummary(String description) {
        Pattern pattern = Pattern.compile("^(#+.*\\n+(\\[?!.*\\n+)*)?(.*\\n?)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(3);
        } else {
            return "";
        }
    }
}
