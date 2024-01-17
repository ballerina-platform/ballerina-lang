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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;
import static io.ballerina.projects.util.ProjectConstants.BLANG_SOURCE_EXT;
import static io.ballerina.projects.util.ProjectConstants.COMPILER_PLUGIN_TOML;
import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.projects.util.ProjectConstants.EMPTY_STRING;
import static io.ballerina.projects.util.ProjectConstants.IMPORT_PREFIX;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TEST_DIR_NAME;

/**
 * Utilities related to files.
 *
 * @since 2.0.0
 */
public class FileUtils {

    private static final String PNG_HEX_HEADER = "89504E470D0A1A0A";
    private static final PathMatcher FILE_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**/Ballerina.toml");

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
        if (is == null) {
            throw new FileNotFoundException("Schema file not found: " + path);
        }
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
        return isMatchingImageFormat(filePath, PNG_HEX_HEADER, 8);
    }

    /**
     * Get last modified timestamp of a ballerina project.
     *
     * @param projectRoot project root path
     * @return last modified time of the ballerina project
     */
    public static long lastModifiedTimeOfBalProject(Path projectRoot) {
        File[] files = projectRoot.toAbsolutePath().toFile().listFiles();
        long latestDate = 0;
        if (files != null) {
            for (File file : files) {
                long fileModifiedDate = latestDate;
                Path filename = Optional.of(Optional.of(file.toPath()).orElseThrow()).orElseThrow();
                if (file.isDirectory()) {
                    if (file.toPath().equals(projectRoot.resolve(MODULES_ROOT))
                            || file.toPath().equals(projectRoot.resolve(TEST_DIR_NAME))
                            || file.toPath().equals(projectRoot.resolve(RESOURCE_DIR_NAME))) {
                        // for `modules`, `tests` and `resources` directories, not considering files inside
                        fileModifiedDate = file.lastModified();
                    }
                } else {
                    if (file.toPath().equals(projectRoot.resolve(BALLERINA_TOML))
                            || file.toPath().equals(projectRoot.resolve(COMPILER_PLUGIN_TOML))) {
                        // Ballerina.toml and CompilerPlugin.toml
                        fileModifiedDate = file.lastModified();
                    } else if (filename.toString().endsWith(BLANG_SOURCE_EXT)
                            && file.toPath().equals(projectRoot.resolve(filename))) {
                        // default module ballerina source files
                        fileModifiedDate = file.lastModified();
                    }
                }
                if (fileModifiedDate > latestDate) {
                    latestDate = fileModifiedDate;
                }
            }
        }
        return latestDate;
    }

    /**
     * Validate any image file against given image format header hex value.
     *
     * @param imgPath        image file path
     * @param formatHexValue image format header hex value
     * @param formatOffset   image format header hex value length
     * @return is matched with the given image format hex value
     * @throws IOException error when reading the given file path
     */
    private static boolean isMatchingImageFormat(Path imgPath, String formatHexValue, int formatOffset)
            throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(String.valueOf(imgPath))) {
            try {
                byte[] imgHeaderByteArray = new byte[formatOffset];
                int bytesRead = fileInputStream.read(imgHeaderByteArray, 0, formatOffset);
                if (bytesRead != 8) {
                    return false;
                }
                byte[] formatHeaderByteArray = Arrays.copyOfRange(new BigInteger(formatHexValue, 16)
                        .toByteArray(), 1, formatOffset + 1);
                if (Arrays.equals(imgHeaderByteArray, formatHeaderByteArray)) {
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
     * Add deprecated meta file.
     *
     * @param metaFilePath deprecated message meta file path
     * @param message deprecated message
     */
    public static void addDeprecatedMetaFile(Path metaFilePath, String message) {
        if (!metaFilePath.toFile().exists()) {
            try {
                Files.createFile(metaFilePath);
            } catch (IOException ignored) {
                // ignore and continue
                return;
            }
        }
        if (metaFilePath.toFile().exists()) {
            try (FileWriter fileWriter = new FileWriter(metaFilePath.toAbsolutePath().toString(),
                    Charset.defaultCharset());
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(message);
            } catch (IOException ignored) {
                // ignore and continue
            }
        }
    }

    /**
     * Delete deprecated meta file.
     *
     * @param metaFilePath deprecated message meta file path
     */
    public static void deleteDeprecatedMetaFile(Path metaFilePath) {
        try {
            Files.deleteIfExists(metaFilePath);
        } catch (IOException ignored) {
            // ignore and continue
        }
    }

    public static void replaceTemplateName(Path path, String templateName, String packageName) {
        Optional<Path> fileName = Optional.ofNullable(path.getFileName());
        if (fileName.isPresent() && fileName.get().toString().endsWith(BLANG_SOURCE_EXT)) {
            try {
                String content = Files.readString(path);
                String oldImportStatementStart = IMPORT_PREFIX + templateName + DOT;
                String newImportStatementStart = IMPORT_PREFIX + packageName + DOT;
                if (content.contains(oldImportStatementStart)) {
                    content = content.replaceAll(oldImportStatementStart, newImportStatementStart);
                    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while replacing template name in module import statements: " + path, e);
            }
        }
    }

    /**
     * Get the list of files and directories in a directory.
     *
     * @param directoryPath directory path
     * @return list of files
     */
    public static List<Path> getFilesInDirectory(Path directoryPath) {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> paths = Files.list(directoryPath)) {
            paths.forEach(files::add);
        } catch (IOException e) {
            // ignore
        }
        return files;
    }

    /**
     * Copy files to the given destination.
     */
    public static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private String templateName;
        private String packageName;
        private StandardCopyOption copyOption;


        public Copy(Path fromPath, Path toPath, String templateName, String packageName,
                    StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.templateName = templateName;
            this.packageName = packageName;
            this.copyOption = copyOption;
        }

        public Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, EMPTY_STRING, EMPTY_STRING, StandardCopyOption.REPLACE_EXISTING);
        }

        public Copy(Path fromPath, Path toPath, String templateName, String packageName) {
            this(fromPath, toPath, templateName, packageName, StandardCopyOption.REPLACE_EXISTING);
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
            if (!packageName.equals(EMPTY_STRING) && !templateName.equals(EMPTY_STRING) &&
                    !packageName.equals(templateName)) {
                replaceTemplateName(toPath.resolve(fromPath.relativize(file).toString()), templateName, packageName);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    public static boolean checkBallerinaTomlInExistingDir(Path startingDir) {
        BallerinaTomlChecker ballerinaTomlChecker = new BallerinaTomlChecker(startingDir);
        try {
           Files.walkFileTree(startingDir, ballerinaTomlChecker);
        } catch (IOException e) {
            // ignore
        }
        return ballerinaTomlChecker.isBallerinaTomlFound();
    }

    /**
     * Look for existing Ballerina.toml file in the given directory up to 10 levels.
     */
    public static class BallerinaTomlChecker extends SimpleFileVisitor<Path> {
        private Path startingPath;
        private boolean ballerinaTomlFound = false;

        public boolean isBallerinaTomlFound() {
            return ballerinaTomlFound;
        }

        public void setBallerinaTomlFound(boolean ballerinaTomlFound) {
            this.ballerinaTomlFound = ballerinaTomlFound;
        }

        public BallerinaTomlChecker(Path startingPath) {
            this.startingPath = startingPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {

            int depth = dir.getNameCount() - startingPath.getNameCount();
            if (depth >= 10) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {

            if (FILE_MATCHER.matches(file)) {
                setBallerinaTomlFound(true);
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
