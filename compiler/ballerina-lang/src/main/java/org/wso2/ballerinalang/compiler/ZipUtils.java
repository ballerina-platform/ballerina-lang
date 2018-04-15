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
package org.wso2.ballerinalang.compiler;

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
import java.util.stream.Stream;

/**
 * Zip Utils needed to zip the packages.
 */
class ZipUtils {
    private static final String SRC_DIR = "src";

    /**
     * Generates the balo/zip of the package.
     *
     * @param bLangPackage bLangPackage node
     * @param projectPath  project path
     * @param paths        paths of bal files inside the package
     */
    static void generateBalo(BLangPackage bLangPackage, String projectPath, Stream<Path> paths) {
        PackageID packageID = bLangPackage.packageID;
        Path destPath = Paths.get(projectPath).resolve(".ballerina").resolve("repo")
                             .resolve(packageID.getOrgName().getValue()).resolve(packageID.getName().getValue())
                             .resolve(packageID.getPackageVersion().getValue());
        if (!Files.exists(destPath)) {
            try {
                Files.createDirectories(destPath);
            } catch (IOException e) {
                throw new BLangCompilerException("Error occurred when creating directories in " +
                                                         "./ballerina/repo/ to save the generated balo");
            }
        }
        String fileName = packageID.getName() + ".zip";
        Path baloDirPath = destPath.resolve(fileName);
        createArchive(paths, baloDirPath);
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
            throw new BLangCompilerException("error creating artifact" + outDirPath.getFileName());
        }
        try (FileSystem zipFS = FileSystems.newFileSystem(zipFileURI, zipFSEnv)) {
            addFileToArchive(filesToBeArchived, zipFS, outDirPath);
        } catch (IOException e) {
            throw new BLangCompilerException("error creating artifact" + outDirPath.getFileName());
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
            if (fileName.equals("Package.md")) {
                dest = zipFS.getPath(zipFS.getSeparator(), fileName);
            }
            try {
                if (Files.exists(path)) {
                    copyFileToArchive(new FileInputStream(path.toFile()), dest);
                }
            } catch (IOException e) {
                throw new BLangCompilerException("error generating artifact " + outDirPath.getFileName());
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
}
