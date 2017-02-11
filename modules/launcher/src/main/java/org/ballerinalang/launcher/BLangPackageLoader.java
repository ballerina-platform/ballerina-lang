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

package org.ballerinalang.launcher;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.PackageRepository;
import org.wso2.ballerina.core.model.SymbolName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.8.0
 */
public class BLangPackageLoader {

    public static BLangPackage load(Path packagePath, PackageRepository packageRepo, BLangProgram bLangProgram) {

        // Load package details (input streams of source files) from the given package repository
        PackageRepository.PackageSource pkgSource = packageRepo.loadPackage(packagePath);

        String pkgPathStr = replaceDelimiterWithDots(packagePath);
        BLangPackage bLangPackage = new BLangPackage(pkgPathStr, pkgSource.getPackageRepository(), bLangProgram);
        List<BallerinaFile> bFileList = pkgSource.getSourceFileStreamMap().entrySet()
                .stream()
                .map(entry -> LauncherUtils.buildBFile(entry.getKey(), packagePath, entry.getValue(), bLangPackage))
                .peek(bFile -> validatePackagePathInFile(pkgPathStr, packagePath, bFile))
                .collect(Collectors.toList());

        bLangPackage.setImportPackages(flattenImportPackages(bFileList));

        // Define package in the program scope
        bLangProgram.define(new SymbolName(bLangPackage.getPackagePath()), bLangPackage);

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
            if (dependentPkg != null) {
                parentPackage.addDependentPackage(dependentPkg);
                return;
            }

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

            Path packagePath = convertPkgSymbolNameToPath(importPackage.getSymbolName());
            dependentPkg = load(packagePath, parentPackage.getPackageRepository(), bLangProgram);

            // Define main package
            parentPackage.addDependentPackage(dependentPkg);
        }
    }

    private static Path convertPkgSymbolNameToPath(SymbolName pkgSymbol) {
        String[] dirs = pkgSymbol.toString().split("\\.");
        return (dirs.length == 1) ? Paths.get(dirs[0]) :
                Paths.get(dirs[0], Arrays.copyOfRange(dirs, 1, dirs.length));
    }

    private static String replaceDelimiterWithDots(Path path) {
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
