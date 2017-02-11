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

import org.wso2.ballerina.core.ProgramPackageRepository;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @since 0.8.0
 */
public class BLangPackageLoader {
    private Path basePathObj;
    private Path packagePathObj;

    private BLangProgram enclosingScope;
    private BLangPackage bLangPackage;

    private List<BallerinaFile> bFileList = new ArrayList<>();
    private List<SymbolName> impPkgSymNameList;

    // TODO Here we assume that these paths are normalized and resolved.. So there won't be FNF errors
    public BLangPackageLoader(BLangProgram bLangProgram, Path basePath, Path packagePath) {
        this.enclosingScope = bLangProgram;
        this.basePathObj = basePath;
        this.packagePathObj = packagePath;
        this.bLangPackage = new BLangPackage(enclosingScope);
    }

    public static BLangPackage load(BLangProgram bLangProgram, Path basePath, Path packagePath) {

//        ProgramPackageRepository progPkgRepository = new ProgramPackageRepository(basePath);


        // Remove redundant stuff using the Paths and Files API
        BLangPackageLoader packageBuilder = new BLangPackageLoader(bLangProgram, basePath, packagePath);
        BLangPackage mainPackage = packageBuilder.build();
        bLangProgram.setMainPackage(mainPackage);

        // Define main package
        bLangProgram.define(new SymbolName(mainPackage.getPackagePath()), mainPackage);

        resolveDependencies(mainPackage, bLangProgram);

        return null;
    }

    private static void resolveDependencies(BLangPackage parentPackage, BLangProgram bLangProgram) {
        for (SymbolName impPkgSymName : parentPackage.getDependentPackageNames()) {

            // Check whether this package is already resolved.
            BLangPackage dependentPkg = (BLangPackage) bLangProgram.resolve(impPkgSymName);
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

            Path packagePath = convertPkgSymbolNameToPath(impPkgSymName);
            BLangPackageLoader packageBuilder = new BLangPackageLoader(bLangProgram,
                    bLangProgram.getProgramFilePath(), packagePath);
            dependentPkg = packageBuilder.build();

            // Define main package
            bLangProgram.define(new SymbolName(dependentPkg.getPackagePath()), dependentPkg);
            parentPackage.addDependentPackage(dependentPkg);

            resolveDependencies(dependentPkg, bLangProgram);
        }
    }


    private static Path convertPkgSymbolNameToPath(SymbolName pkgSymbol) {
        String[] dirs = pkgSymbol.toString().split("\\.");
        return (dirs.length == 1) ? Paths.get(dirs[0]) :
                Paths.get(dirs[0], Arrays.copyOfRange(dirs, 1, dirs.length));
    }

    public BLangPackage build() {
        // TODO construct the package-path from the give Path object
        // E.g. org/sameera/calc -> org.sameera.calc
        String pkgPath = replaceDelimiterWithDots(packagePathObj);

        try {
            bFileList = Files.list(basePathObj.resolve(packagePathObj))
                    .filter(filePath -> filePath.toString().endsWith(".bal"))
                    .map(this::buildBLangFile)
                    .peek(bFile -> {
                        if (!pkgPath.equals(bFile.getPackagePath())) {
                            String actualPkgPath = (bFile.getPackagePath() != null) ? bFile.getPackagePath() : "";
                            throw new BallerinaException(bFile.getFileName() + ": incorrect package path" +
                                    ": expected '" + pkgPath + "', found '" + actualPkgPath + "'");
                        }
                    })
                    .collect(Collectors.toList());

            flattenImportPackages();

        } catch (IOException e) {
            // TODO
        }

        bLangPackage.setDependentPackageNames(impPkgSymNameList.toArray(new SymbolName[impPkgSymNameList.size()]));
        bLangPackage.setPackagePath(pkgPath);
        bLangPackage.setBallerinaFiles(bFileList.toArray(new BallerinaFile[bFileList.size()]));
        return bLangPackage;
    }

    private String replaceDelimiterWithDots(Path path) {
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

    private BallerinaFile buildBLangFile(Path bFilePath) {
        return LauncherUtils.buildBFile(bFilePath, basePathObj, bLangPackage);
    }

    private void flattenImportPackages() {
        impPkgSymNameList = bFileList.stream()
                .map(BallerinaFile::getImportPackages)
                .flatMap(Arrays::stream)
                .distinct()
                .map(impPkg -> new SymbolName(impPkg.getPath()))
                .collect(Collectors.toList());
    }
}
