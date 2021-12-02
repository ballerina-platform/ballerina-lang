/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utilities related to files.
 *
 * @since 2.0.0
 */
public class FileUtils {

    private static final String PNG_HEX_HEADER = "89504E470D0A1A0A";

    /**
     * Get the name of the without the extension.
     *
     * @param filePath Path of the file.
     * @return File name without extension.
     */
    public static String getFileNameWithoutExtension(String filePath) {
        Path fileName = Paths.get(filePath).getFileName();
        if (null != fileName) {
            int index = indexOfExtension(fileName.toString());
            return index == -1 ? fileName.toString() :
                    fileName.toString().substring(0, index);
        } else {
            return null;
        }
    }

    public static boolean hasExtension(Path filePath) {
        Path fileName = filePath.getFileName();
        if (null != fileName) {
            int index = indexOfExtension(fileName.toString());
            return index != -1;
        } else {
            return false;
        }
    }

    private static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int extensionPos = filename.lastIndexOf(46);
            int lastSeparator = indexOfLastSeparator(filename);
            return lastSeparator > extensionPos ? -1 : extensionPos;
        }
    }

    private static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int lastUnixPos = filename.lastIndexOf(47);
            int lastWindowsPos = filename.lastIndexOf(92);
            return Math.max(lastUnixPos, lastWindowsPos);
        }
    }

    /**
     * Read the content of the given file.
     *
     * @param path path of the file
     * @return content of the given file
     * @throws IOException if IO exception occurs
     */
    public static String readFileAsString(String path) throws IOException {
        InputStream is = FileUtils.class.getClassLoader().getResourceAsStream(path);
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            br = new BufferedReader(inputStreamReader);
            String content = br.readLine();
            if (content == null) {
                return sb.toString();
            }

            sb.append(content);

            while ((content = br.readLine()) != null) {
                sb.append('\n').append(content);
            }
            sb.append('\n');
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ignore) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                }
            }
        }
        return sb.toString();
    }

    /**
     * Deletes the provided path. If path is a directory, recursively deletes it. Else, delete the file.
     *
     * @param path Path to be deleted
     * @throws IOException On permission issues, concurrent access (on windows) and etc
     */
    public static void deletePath(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        if (Files.isDirectory(path)) {
            for (Path dir : Files.list(path).collect(Collectors.toList())) {
                deletePath(dir);
            }
        }

        Files.delete(path);
    }

    /**
     * Check if the given image is a PNG.
     *
     * @param filePath image file path
     * @return is valid PNG file
     * @throws IOException error when reading the given file path
     */
    public static boolean isValidPng(Path filePath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(String.valueOf(filePath))) {
            try {
                byte[] imgHeaderByteArray = new byte[8];
                int bytesRead = fileInputStream.read(imgHeaderByteArray, 0, 8);
                if (bytesRead != 8) {
                    return false;
                }
                byte[] pngHeaderByteArray =
                        Arrays.copyOfRange(new BigInteger(PNG_HEX_HEADER, 16).toByteArray(), 1, 9);
                if (Arrays.equals(imgHeaderByteArray, pngHeaderByteArray)) {
                    return true;
                }
            } catch (Exception e) {
                //Ignore
                return false;
            }
            return false;
        }
    }

    /**
     * Copy files to the given destination.
     */
    public static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;


        public Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        public Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {

            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {

            Files.copy(file, toPath.resolve(fromPath.relativize(file).toString()), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }
}
