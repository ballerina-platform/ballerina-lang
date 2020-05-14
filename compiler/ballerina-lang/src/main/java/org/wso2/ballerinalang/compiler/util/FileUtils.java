/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * This class contains a set of file manipulation utility methods.
 *
 * @since 0.970.0
 */
public class FileUtils {
    private static final String SRC_DIR = "src";

    public static void deleteFile(Path filePath) throws IOException {
        Files.deleteIfExists(filePath);
    }

    /**
     * Generates the balo/zip of the package.
     *
     * @param bLangPackage bLangPackage node
     * @param projectPath  project path
     * @param paths        paths of bal files inside the package
     */
    static void generateBalo(BLangPackage bLangPackage, String projectPath, Stream<Path> paths) {
        PackageID packageID = bLangPackage.packageID;
        Path destPath = Paths.get(projectPath, ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                  ProjectDirConstants.CACHES_DIR_NAME,
                                  ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME,
                                  packageID.getOrgName().getValue(),
                                  packageID.getName().getValue(),
                                  packageID.getPackageVersion().getValue());
        if (!Files.exists(destPath)) {
            try {
                Files.createDirectories(destPath);
            } catch (IOException e) {
                throw new BLangCompilerException("error creating directories in ./ballerina/repo/ to save the " +
                                                         "artifact");
            }
        }
        String fileName = packageID.getName() + ".zip";
        Path baloDirPath = destPath.resolve(fileName);
        deleteBalo(baloDirPath);
        createArchive(paths, baloDirPath);
    }

    /**
     * Delete the balo if it exist before creating a new balo.
     *
     * @param baloDirPath path of the balo
     */
    private static void deleteBalo(Path baloDirPath) {
        try {
            Files.deleteIfExists(baloDirPath);
        } catch (IOException ignore) {
            throw new BLangCompilerException("error deleting artifact : " + baloDirPath);
        }
    }

    /**
     * Create archive when creating the balo.
     *
     * @param filesToBeArchived files to be archived
     * @param outDirPath        output archive file path
     */
    private static void createArchive(Stream<Path> filesToBeArchived, Path outDirPath) {
        Map<String, String> zipFSEnv = new HashMap<>();
        zipFSEnv.put("create", "true");
        URI filepath = outDirPath.toUri();
        URI zipFileURI;
        try {
            zipFileURI = new URI("jar:" + filepath.getScheme(),
                                 filepath.getUserInfo(), filepath.getHost(), filepath.getPort(),
                                 filepath.getPath() + "!/",
                                 filepath.getQuery(), filepath.getFragment());
        } catch (URISyntaxException ignore) {
            throw new BLangCompilerException("error creating artifact: " + outDirPath.getFileName());
        }
        try (FileSystem zipFS = FileSystems.newFileSystem(zipFileURI, zipFSEnv)) {
            addFileToArchive(filesToBeArchived, zipFS, outDirPath);
        } catch (IOException e) {
            throw new BLangCompilerException("error creating artifact: " + outDirPath.getFileName());
        }
    }

    /**
     * Add file to archive.
     *
     * @param filesToBeArchived files to be archived
     * @param zipFS             Zip file system
     * @param outDirPath        output archive name
     */
    private static void addFileToArchive(Stream<Path> filesToBeArchived, FileSystem zipFS, Path outDirPath) {
        filesToBeArchived.forEach((path) -> {
            String fileName = path.getFileName().toString();
            Path dest = zipFS.getPath(SRC_DIR, fileName);
            if (fileName.equals("Module.md")) {
                dest = zipFS.getPath(zipFS.getSeparator(), fileName);
            }
            try {
                if (Files.exists(path)) {
                    copyFileToArchive(new FileInputStream(path.toFile()), dest);
                }
            } catch (IOException e) {
                throw new BLangCompilerException("error generating artifact: " + outDirPath.getFileName());
            }
        });
    }

    /**
     * Copy file to archive.
     *
     * @param srcInputStream inputstream of the file to be copied
     * @param destPath       output path
     * @throws IOException i/o exception thrown
     */
    private static void copyFileToArchive(InputStream srcInputStream, Path destPath) throws IOException {
        Path parent = destPath.getParent();
        if (parent != null) {
            if (Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            Files.copy(srcInputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String cleanupFileExtension(String targetFileName) {
        String updatedFileName = targetFileName;
        if (updatedFileName == null || updatedFileName.isEmpty()) {
            throw new IllegalArgumentException("invalid target file name");
        }

        if (updatedFileName.endsWith(BLANG_SOURCE_EXT)) {
            updatedFileName = updatedFileName.substring(0, updatedFileName.length() - BLANG_SOURCE_EXT.length());
        }
        return updatedFileName;
    }

    /**
     * Get the name of the without the extension.
     *
     * @param filePath Path of the file.
     * @return File name without extension.
     */
    public static String geFileNameWithoutExtension(Path filePath) {
        Path fileName = filePath.getFileName();
        if (null != fileName) {
            int index = indexOfExtension(fileName.toString());
            return index == -1 ? fileName.toString() :
                    fileName.toString().substring(0, index);
        } else {
            return null;
        }
    }

    public static String getExtension(Path filePath) {
        Path fileName = filePath.getFileName();
        if (null == fileName) {
            return "";
        }
        Optional<String> extension = Optional.ofNullable(fileName.toString())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.toString().lastIndexOf(".") + 1));
        return extension.orElse("");
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

}
