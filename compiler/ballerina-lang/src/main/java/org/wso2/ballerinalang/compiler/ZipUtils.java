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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zip Utils needed to zip the packages.
 */
class ZipUtils {

    /**
     * Zip all the bal files in the package.
     *
     * @param filesToBeZipped bal files to be zipped
     * @param zipFile         name of the zip file
     */
    private static void zipFile(Stream<Path> filesToBeZipped, String zipFile) {
        ZipOutputStream zipOut = null;
        try {
            zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipOutputStream finalZipOut = zipOut;
            filesToBeZipped.forEach((path) -> addToZip(path, "src", finalZipOut));
        } catch (FileNotFoundException e) {
            throw new BLangCompilerException("Unable to create balo " + zipFile);
        } finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Add file to the zipped folder.
     *
     * @param srcFilePath path of the folder/file to be zipped
     * @param includedDir directory to be included
     * @param zipOut      ZipOutputStream object
     */
    private static void addToZip(Path srcFilePath, String includedDir, ZipOutputStream zipOut) {
        String filePath = srcFilePath.getFileName().toString();
        if (!includedDir.isEmpty()) {
            filePath = includedDir + "/" + filePath;
        }
        try {
            zipOut.putNextEntry(new ZipEntry(filePath));
            Files.copy(srcFilePath, zipOut);
        } catch (IOException e) {
            throw new BLangCompilerException("Error occurred when generating the balo " + filePath);
        }
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
        String baloDirPath = destPath.resolve(fileName).toString();
        zipFile(paths, baloDirPath);
    }
}
