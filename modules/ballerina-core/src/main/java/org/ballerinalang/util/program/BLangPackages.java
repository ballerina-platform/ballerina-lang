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
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.repository.BuiltinPackageRepository;
import org.ballerinalang.util.repository.PackageRepository;
import org.ballerinalang.util.repository.ProgramDirRepository;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * This class contains a set of static methods to operate on {@code BLangPackage} objects. This class contains methods
 * to load packages or files from the given package repository.
 *
 * @since 0.8.0
 */
public class BLangPackages {

    public static BLangPackage loadEntryPackage(Path programDirPath,
                                                Path sourcePath,
                                                BLangProgram bLangProgram,
                                                ProgramDirRepository programDirRepository) {

        Path resolvedSourcePath = BLangPrograms.validateAndResolveSourcePath(programDirPath, sourcePath);
        if (Files.isDirectory(resolvedSourcePath, LinkOption.NOFOLLOW_LINKS)) {
            Path packagePath = programDirPath.relativize(resolvedSourcePath);
            BLangPackage bLangPackage = BLangPackages.loadPackage(packagePath, programDirRepository, bLangProgram);
            bLangProgram.addEntryPoint(packagePath.toString());
            return bLangPackage;

        } else if (resolvedSourcePath.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
            BLangPackage bLangPackage = BLangPackages.loadFile(resolvedSourcePath, programDirRepository, bLangProgram);
            bLangProgram.addEntryPoint(resolvedSourcePath.getFileName().toString());
            return bLangPackage;
        }

        throw new IllegalArgumentException("invalid source file: " + sourcePath.toString());
    }

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
        if (pkgSource == null) {
            throw new RuntimeException("package not found: " + convertToPackageName(packagePath).toString());
        }

        if (pkgSource.getSourceFileStreamMap().isEmpty()) {
            throw new RuntimeException("no bal files in package: " + convertToPackageName(packagePath).toString());
        }

        String pkgPathStr = convertToPackageName(packagePath).toString();
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

    private static BLangPackage loadPackageInternal(PackageRepository.PackageSource pkgSource,
                                                    BLangPackage.PackageBuilder packageBuilder,
                                                    BLangProgram bLangProgram,
                                                    LinkedHashSet<SymbolName> currentDepPath) {

        Path packagePath = pkgSource.getPackagePath();
        String pkgPathStr = convertToPackageName(packagePath).toString();
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
            Path packagePath = convertToPackagePath(importPackage.getSymbolName().getName());

            BLangPackage dependentPkg = (BLangPackage) bLangProgram.resolve(importPackage.getSymbolName());

            if (dependentPkg == null) {
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

    public static String[] getBuiltinPackageNames() {
        Iterator<BuiltinPackageRepository> providerIterator =
                ServiceLoader.load(BuiltinPackageRepository.class).iterator();
        List<BuiltinPackageRepository> nameProviders = new ArrayList<>();
        while (providerIterator.hasNext()) {
            BuiltinPackageRepository constructLoader = providerIterator.next();
            nameProviders.add(constructLoader);
        }
        HashSet<String> pkgSet = new HashSet<>();
        if (!nameProviders.isEmpty()) {
            for (BuiltinPackageRepository provider : nameProviders) {
                String[] pkgs = provider.loadPackageNames();
                if (pkgs != null && pkgs.length > 0) {
                    for (String pkg : pkgs) {
                        pkgSet.add(pkg);
                    }
                }
            }
        }
        String[] pkgArray = new String[pkgSet.size()];
        return pkgSet.toArray(pkgArray);
    }

    public static Path convertToPackageName(Path packagePath) {
        int nameCount = packagePath.getNameCount();
        StringBuilder sb = new StringBuilder(nameCount);
        sb.append(packagePath.getName(0));
        for (int i = 1; i < nameCount; i++) {
            sb.append(".").append(packagePath.getName(i));
        }

        return Paths.get(sb.toString());
    }

    public static Path convertToPackagePath(String packagePath) {
        if (packagePath.equals(".")) {
            return Paths.get(packagePath);
        }

        String[] dirs = packagePath.split("\\.");
        return (dirs.length == 1) ? Paths.get(dirs[0]) :
                Paths.get(dirs[0], Arrays.copyOfRange(dirs, 1, dirs.length));
    }

    public static Path convertToPackagePath(Path packageName) {
        return convertToPackagePath(packageName.toString());
    }
}
