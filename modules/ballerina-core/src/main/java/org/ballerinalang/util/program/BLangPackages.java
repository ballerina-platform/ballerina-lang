/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.program;

import org.ballerinalang.util.repository.PackageRepository;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.nativeimpl.NativePackageProxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.8.0
 */
public class BLangPackages {

    private static BLangPackage loadPackageInternal(PackageRepository.PackageSource pkgSource,
                                                    BLangPackage bLangPackage,
                                                    BLangProgram bLangProgram) {

        Path packagePath = pkgSource.getPackagePath();
        String pkgPathStr = getPackagePathFromPath(packagePath);
        System.out.println(pkgPathStr);
        List<BallerinaFile> bFileList = pkgSource.getSourceFileStreamMap().entrySet()
                .stream()
                .map(entry -> BLangFiles.loadFile(entry.getKey(), packagePath, entry.getValue(), bLangPackage))
                .peek(bFile -> validatePackagePathInFile(pkgPathStr, packagePath, bFile))
                .collect(Collectors.toList());

        bLangPackage.setBallerinaFiles(bFileList.toArray(new BallerinaFile[bFileList.size()]));
        bLangPackage.setImportPackages(flattenImportPackages(bFileList));

        // Resolve dependent packages of this package
        resolveDependencies(bLangPackage, bLangProgram);
        return bLangPackage;
    }

    public static BLangPackage loadPackage(Path packagePath,
                                                   PackageRepository packageRepo,
                                                   BLangProgram bLangProgram) {

        // Load package details (input streams of source files) from the given package repository
        PackageRepository.PackageSource pkgSource = packageRepo.loadPackage(packagePath);
        if (pkgSource.getSourceFileStreamMap().isEmpty()) {
            throw new RuntimeException("no bal files in the package: " + packagePath.toString());
        }

        String pkgPathStr = getPackagePathFromPath(packagePath);
        BLangPackage bLangPackage = new BLangPackage(pkgPathStr, pkgSource.getPackageRepository(), bLangProgram);

        loadPackageInternal(pkgSource, bLangPackage, bLangProgram);
        return bLangPackage;
    }

    public static BLangPackage loadFile(Path filePath, PackageRepository packageRepo, BLangProgram bLangProgram) {
        InputStream inputStream;
        try {
            inputStream = Files.newInputStream(filePath, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            // TODO Handle errors
            throw new RuntimeException(e.getMessage(), e);
        }

        BLangPackage bLangPackage = new BLangPackage(null, packageRepo, bLangProgram);
        BallerinaFile bFile = BLangFiles.loadFile(filePath.getFileName().toString(), null, inputStream, bLangPackage);
        bLangPackage.setBallerinaFiles(new BallerinaFile[]{bFile});
        bLangPackage.setImportPackages(bFile.getImportPackages());

        // Resolve dependent packages of this package
        resolveDependencies(bLangPackage, bLangProgram);
        return bLangPackage;
    }

    private static void validatePackagePathInFile(String pkgPathStr, Path packagePath, BallerinaFile bFile) {
        if (!pkgPathStr.equals(bFile.getPackagePath())) {
            String actualPkgPath = (bFile.getPackagePath() != null) ? bFile.getPackagePath() : "";
            String filePath = packagePath.resolve(bFile.getFileName()).toString();
            throw new BallerinaException(filePath + ": incorrect package" +
                    ": expected '" + pkgPathStr + "', found '" + actualPkgPath + "'");
        }
    }

    private static void resolveDependencies(BLangPackage parentPackage, BLangProgram bLangProgram) {
        for (ImportPackage importPackage : parentPackage.getImportPackages()) {

            // Check whether this package is already resolved.
            BLangPackage dependentPkg = (BLangPackage) bLangProgram.resolve(importPackage.getSymbolName());
            if (dependentPkg != null && dependentPkg instanceof NativePackageProxy) {
                ((NativePackageProxy) dependentPkg).load();
                Path packagePath = getPathFromPackagePath(importPackage.getSymbolName().getName());
                PackageRepository.PackageSource pkgSource = dependentPkg.getPackageRepository().loadPackage(packagePath);
                dependentPkg = loadPackageInternal(pkgSource, dependentPkg, bLangProgram);
            } else {

                // TODO Detect cyclic dependencies
                // Remove redundant stuff using the Paths and Files API
                // This builder or loader should throw an error if the package cannot be found.
                // 1) If the parent package is loaded from the program repository (current directory), then follow this
                //    search order:
                //      i) Search the program repository
                //      ii) Search the system repository
                //      iii) Search the personal/user repository
                // 2) If the parent is loaded from the system directory, then all the children should be
                //    available in the system repository.  DO NOT Search other repositories.
                // 3) If the parent is loaded from the personal/user repository, then use following search order:
                //      i) Search the system repository
                //      ii) Search the personal/user repository
                // 4) None of the above applies if the package name starts with 'ballerina'
                Path packagePath = getPathFromPackagePath(importPackage.getSymbolName().getName());
                dependentPkg = loadPackage(packagePath, parentPackage.getPackageRepository(), bLangProgram);

                // Define package in the program scope
                bLangProgram.define(new SymbolName(dependentPkg.getPackagePath()), dependentPkg);
            }

            // Define main package
            parentPackage.addDependentPackage(dependentPkg);
        }
    }

    private static Path getPathFromPackagePath(String packagePath) {
        String[] dirs = packagePath.split("\\.");
        return (dirs.length == 1) ? Paths.get(dirs[0]) :
                Paths.get(dirs[0], Arrays.copyOfRange(dirs, 1, dirs.length));
    }

    private static String getPackagePathFromPath(Path path) {
        if (path.getNameCount() == 1) {
            return path.toString();
        }

        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < path.getNameCount() - 1; i++) {
            strBuilder.append(path.getName(i)).append(".");
        }

        strBuilder.append(path.getName(path.getNameCount() - 1));
        return strBuilder.toString();
    }

    private static ImportPackage[] flattenImportPackages(List<BallerinaFile> bFileList) {
        return bFileList.stream()
                .map(BallerinaFile::getImportPackages)
                .flatMap(Arrays::stream)
                .distinct()
                .toArray(ImportPackage[]::new);
    }
}
