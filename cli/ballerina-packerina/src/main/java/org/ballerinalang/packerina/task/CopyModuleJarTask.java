/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.task;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.model.ExecutableJar;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Copy module jars to target/tmp.
 */
public class CopyModuleJarTask implements Task {

    private boolean skipCopyLibsFromDist;

    private boolean skipTests;

    public CopyModuleJarTask(boolean skipCopyLibsFromDist, boolean skipTests) {
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.skipTests = skipTests;
    }
    
    @Override
    public void execute(BuildContext buildContext) {

        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();

        // Copy module jar
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        // Copy imported jars.
        copyImportedJars(buildContext, moduleBirMap, sourceRootPath, balHomePath);
    }

    private void copyImportedJars(BuildContext buildContext, List<BLangPackage> moduleBirMap, Path sourceRootPath,
                                  String balHomePath) {
        // Imported jar
        Map<PackageID, Path> alreadyImportedMap = new HashMap<>();
        for (BLangPackage pkg : moduleBirMap) {
            ExecutableJar executableJar = buildContext.moduleDependencyPathMap.get(pkg.packageID);
            copyImportedJars(pkg.symbol.imports, buildContext, sourceRootPath, balHomePath,
                             executableJar.moduleLibs, alreadyImportedMap);
            if (skipTests || !pkg.hasTestablePackage()) {
                continue;
            }
            // Copy module jars imported by testable package
            for (BLangPackage testPkg : pkg.getTestablePkgs()) {
                copyImportedJars(testPkg.symbol.imports, buildContext, sourceRootPath, balHomePath,
                                 executableJar.testLibs, alreadyImportedMap);
            }
        }
    }

    private void copyImportedJars(List<BPackageSymbol> imports, BuildContext buildContext,
                                  Path sourceRootPath, String balHomePath, HashSet<Path> moduleDependencyList,
                                  Map<PackageID, Path> alreadyImportedMap) {
        for (BPackageSymbol importSymbol : imports) {
            PackageID pkgId = importSymbol.pkgID;
            Path importedPath = alreadyImportedMap.get(pkgId);
            if (importedPath != null) {
                moduleDependencyList.add(importedPath);
            } else {
                copyImportedJar(buildContext, importSymbol, sourceRootPath, balHomePath,
                                moduleDependencyList, alreadyImportedMap);
            }
            copyImportedJars(importSymbol.imports, buildContext, sourceRootPath, balHomePath, moduleDependencyList,
                             alreadyImportedMap);
        }
    }

    private void copyImportedJar(BuildContext buildContext, BPackageSymbol importz,
                                 Path project, String balHomePath, HashSet<Path> moduleDependencyList,
                                 Map<PackageID, Path> alreadyImportedMap) {
        PackageID id = importz.pkgID;
        String moduleJarName = id.orgName.value + "-" + id.name.value + "-" + id.version.value +
                BLANG_COMPILED_JAR_EXT;
        // Get the jar paths
        Path importJar;
        // Look if it is a project module.
        if (ProjectDirs.isModuleExist(project, id.name.value) || buildContext.getImportPathDependency(id).isPresent()) {
            // If so fetch from project balo cache
            importJar = buildContext.getJarPathFromTargetCache(id);
        } else {
            // If not fetch from home balo cache.
            importJar = buildContext.getJarPathFromHomeCache(id);
        }

        // If jar cannot be found in cache, read from distribution.
        if (importJar == null || !Files.exists(importJar)) {
            if (skipCopyLibsFromDist) {
                return;
            }
            importJar = Paths.get(balHomePath, "bre", "lib", moduleJarName);

            // todo following is a temporarty fix the proper fix is to version jars inside distribution.
            if (Files.notExists(importJar)) {
                importJar = Paths.get(balHomePath, "bre", "lib", id.orgName.value + "-" +
                        id.name.value + "-" + id.version.value + BLANG_COMPILED_JAR_EXT);
            }
        }
        moduleDependencyList.add(importJar);
        alreadyImportedMap.put(importz.pkgID, importJar);
    }
}
