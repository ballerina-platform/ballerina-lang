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

import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.repository.PackageRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * This class contains a set of static methods to operate on {@code BLangPackage} objects. This class contains methods
 * to load packages or files from the given package repository.
 *
 * @since 0.8.0
 */
public class BLangPackages {

    public static BLangPackage loadPackage(Path packagePath,
                                           PackageRepository packageRepo,
                                           BLangProgram bLangProgram) {
        return loadPackage(packagePath, packageRepo, bLangProgram, new LinkedHashSet<>());
    }

    private static BLangPackage loadPackage(Path packagePath,
                                            PackageRepository packageRepo,
                                            BLangProgram bLangProgram,
                                            LinkedHashSet<SymbolName> currentDepPath) {

        // Load package details (input streams of source files) from the given package repository
        PackageRepository.PackageSource pkgSource = packageRepo.loadPackage(packagePath);
        if (pkgSource.getSourceFileStreamMap().isEmpty()) {
            throw new RuntimeException("no bal files in the package: " + packagePath.toString());
        }

        String pkgPathStr = getPackagePathFromPath(packagePath);
        BLangPackage.PackageBuilder packageBuilder =
                new BLangPackage.PackageBuilder(pkgPathStr, pkgSource.getPackageRepository(), bLangProgram);

        return loadPackageInternal(pkgSource, packageBuilder, bLangProgram, currentDepPath);
    }

    public static BLangPackage loadFile(Path filePath, PackageRepository packageRepo, BLangProgram bLangProgram) {
        PackageRepository.PackageSource pkgSource = packageRepo.loadFile(filePath);
        BLangPackage.PackageBuilder packageBuilder =
                new BLangPackage.PackageBuilder(".", pkgSource.getPackageRepository(), bLangProgram);

        LinkedHashSet<SymbolName> currentDepPath = new LinkedHashSet<>();
        // Resolve dependent packages of this package
        return loadPackageInternal(pkgSource, packageBuilder, bLangProgram, currentDepPath);
    }

    public static Path getPathFromPackagePath(String packagePath) {
        if (packagePath.equals(".")) {
            return Paths.get(packagePath);
        }

        String[] dirs = packagePath.split("\\.");
        return (dirs.length == 1) ? Paths.get(dirs[0]) :
                Paths.get(dirs[0], Arrays.copyOfRange(dirs, 1, dirs.length));
    }

    public static String getPackagePathFromPath(Path path) {
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

    private static BLangPackage loadPackageInternal(PackageRepository.PackageSource pkgSource,
                                                    BLangPackage.PackageBuilder packageBuilder,
                                                    BLangProgram bLangProgram,
                                                    LinkedHashSet<SymbolName> currentDepPath) {

        Path packagePath = pkgSource.getPackagePath();
        String pkgPathStr = getPackagePathFromPath(packagePath);
        packageBuilder.setBallerinaFileList(pkgSource.getSourceFileStreamMap().entrySet()
                .stream()
                .map(entry -> BLangFiles.loadFile(entry.getKey(), packagePath, entry.getValue(), packageBuilder))
                .peek(bFile -> validatePackagePathInFile(pkgPathStr, packagePath, bFile))
                .collect(Collectors.toList()));

        BLangPackage bLangPackage = packageBuilder.build();
        // Check for a dependency cycle
        if (currentDepPath.contains(bLangPackage.getSymbolName())) {
            throw new BallerinaException("dependency cycle detected: " +
                    generateDepCycleString(currentDepPath, bLangPackage));
        }
        // Mark the node in the current path
        currentDepPath.add(bLangPackage.getSymbolName());
        // Resolve dependent packages of this package
        BLangPackage result = resolveDependencies(bLangPackage, bLangProgram, currentDepPath);
        // Remove the node marking from the current path
        currentDepPath.remove(bLangPackage.getSymbolName());
        return result;
    }

    private static void validatePackagePathInFile(String pkgPathStr, Path packagePath, BallerinaFile bFile) {
        if (!pkgPathStr.equals(bFile.getPackagePath())) {
            String actualPkgPath = (bFile.getPackagePath() != null) ? bFile.getPackagePath() : "";
            String expectedPkg = (!pkgPathStr.equals(".")) ? pkgPathStr : "";
            String filePath = (packagePath.toString().equals(".")) ? bFile.getFileName() :
                    packagePath.resolve(bFile.getFileName()).toString();
            throw new BallerinaException("incorrect package in '" + filePath + "'" +
                    ": expected '" + expectedPkg + "', found '" + actualPkgPath + "'");
        }
    }

    private static BLangPackage resolveDependencies(BLangPackage parentPackage, BLangProgram bLangProgram,
                                                    LinkedHashSet<SymbolName> currentDepPath) {
        for (ImportPackage importPackage : parentPackage.getImportPackages()) {

            // Check whether this package is already resolved.
            BLangPackage dependentPkg = (BLangPackage) bLangProgram.resolve(importPackage.getSymbolName());
            Path packagePath = getPathFromPackagePath(importPackage.getSymbolName().getName());

            if (dependentPkg != null && dependentPkg instanceof NativePackageProxy) {
                dependentPkg = ((NativePackageProxy) dependentPkg).load();
                PackageRepository.PackageSource pkgSource =
                        dependentPkg.getPackageRepository().loadPackage(packagePath);

                BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(dependentPkg);
                dependentPkg = loadPackageInternal(pkgSource, packageBuilder, bLangProgram, currentDepPath);

            } else if (dependentPkg == null) {

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
                dependentPkg = loadPackage(packagePath, parentPackage.getPackageRepository(),
                        bLangProgram, currentDepPath);

            }

            // Define package in the program scope
            bLangProgram.define(new SymbolName(dependentPkg.getPackagePath()), dependentPkg);
            parentPackage.addDependentPackage(dependentPkg);
        }

        return parentPackage;
    }

    private static String generateDepCycleString(LinkedHashSet<SymbolName> currentPath, BLangPackage targetPack) {
        StringBuilder builder = new StringBuilder();
        Iterator<SymbolName> itr = currentPath.iterator();
        // skip until the start of the cycle
        while (!itr.next().equals(targetPack.getSymbolName())) {
        }
        // add the first node
        builder.append(targetPack.getSymbolName().toString());
        while (itr.hasNext()) {
            // add the nodes in the middle of the cycle
            builder.append("->" + itr.next());
        }
        // add the last node
        builder.append("->" + targetPack.getSymbolName().toString());
        return builder.toString();
    }

}
