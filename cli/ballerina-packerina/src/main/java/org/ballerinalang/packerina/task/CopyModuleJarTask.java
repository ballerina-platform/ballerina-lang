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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TARGET_TMP_DIRECTORY;

/**
 * Copy module jars to target/tmp.
 */
public class CopyModuleJarTask implements Task {

    private boolean skipCopyLibsFromDist = false;

    public CopyModuleJarTask(boolean skipCopyLibsFromDist) {
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
    }

    public CopyModuleJarTask() {
    }
    
    @Override
    public void execute(BuildContext buildContext) {

        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        Path tmpDir = targetDir.resolve(TARGET_TMP_DIRECTORY);
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();

        try {
            if (!tmpDir.toFile().exists()) {
                Files.createDirectory(tmpDir);
            }
        } catch (IOException e) {
            throw createLauncherException("unable to create tmp directory in target :" + e.getMessage());
        }
        // Copy module jar
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        copyModuleJar(buildContext, moduleBirMap, tmpDir);
        // Copy imported jars.
        copyImportedJars(buildContext, moduleBirMap, sourceRootPath, tmpDir, balHomePath);
    }

    private void copyModuleJar(BuildContext buildContext, List<BLangPackage> moduleBirMap, Path tmpDir) {
        for (BLangPackage module : moduleBirMap) {
            // get the jar path of the module.
            Path jarOutput = buildContext.getJarPathFromTargetCache(module.packageID);
            Path jarTarget = tmpDir.resolve(jarOutput.getFileName());
            try {
                Files.copy(jarOutput, jarTarget, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw createLauncherException("unable to copy the module jar :" + e.getMessage());
            }
        }
    }

    private void copyImportedJars(BuildContext buildContext, List<BLangPackage> moduleBirMap, Path sourceRootPath,
                                  Path tmpDir, String balHomePath) {
        // Imported jar
        HashSet<String> alreadyImportedSet = new HashSet<>();
        for (BLangPackage pkg : moduleBirMap) {
            copyImportedJars(pkg.symbol.imports, buildContext, sourceRootPath, tmpDir, balHomePath, alreadyImportedSet);
        }
    }

    private void copyImportedJars(List<BPackageSymbol> imports, BuildContext buildContext, Path sourceRootPath,
                                  Path tmpDir, String balHomePath, HashSet<String> alreadyImportedSet) {
        for (BPackageSymbol importSymbol : imports) {
            PackageID pkgId = importSymbol.pkgID;
            String id = pkgId.toString();
            if (alreadyImportedSet.contains(id)) {
                continue;
            }
            alreadyImportedSet.add(id);
            copyImportedJar(buildContext, importSymbol, sourceRootPath, tmpDir, balHomePath);
            copyImportedJars(importSymbol.imports, buildContext, sourceRootPath,
                             tmpDir, balHomePath, alreadyImportedSet);
        }
    }

    private void copyImportedJar(BuildContext buildContext, BPackageSymbol importz,
                                 Path project, Path tmpDir, String balHomePath) {
        PackageID id = importz.pkgID;
        String moduleJarName = id.orgName.value + "-" + id.name.value + "-" + id.version.value +
                BLANG_COMPILED_JAR_EXT;
        // Get the jar paths
        Path importJar;
        // Look if it is a project module.
        if (ProjectDirs.isModuleExist(project, id.name.value)) {
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
            if (!Files.exists(importJar)) {
                importJar = Paths.get(balHomePath, "bre", "lib", id.name.value + BLANG_COMPILED_JAR_EXT);
            }
        }
        try {
            Path jarTarget = tmpDir.resolve(moduleJarName);
            Files.copy(importJar, jarTarget, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw createLauncherException("unable to find the imported jar from the distribution: " + e.getMessage());
        }
    }
}
