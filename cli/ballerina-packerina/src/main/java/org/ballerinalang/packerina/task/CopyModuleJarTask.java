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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 *  Copy module jars to target/tmp.
 */
public class CopyModuleJarTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {

        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        Path tmpDir = targetDir.resolve(ProjectDirConstants.TARGET_TMP_DIRECTORY);
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);

        try {
            Files.createDirectories(tmpDir);
        } catch (IOException e) {
            throw createLauncherException("unable to create tmp directory in target :" + e.getMessage());
        }

        List<BLangPackage> moduleBirMap = buildContext.getModules();
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
        // Imported jar
        for (BLangPackage pkg : moduleBirMap) {
            for (BPackageSymbol importz : pkg.symbol.imports) {
                Path importJar = findImportJarPath(buildContext, importz, sourceRootPath);
                Path jarTarget = tmpDir.resolve(importz.pkgID.name.value + BLANG_COMPILED_JAR_EXT);
                if (importJar != null && Files.exists(importJar)) {
                    try {
                        Files.copy(importJar, jarTarget, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw createLauncherException("unable to copy the imported jar :" + e.getMessage());
                    }
                }
            }
        }
    }

    private static Path findImportJarPath(BuildContext buildContext, BPackageSymbol importz, Path project) {
        // Get the jar paths
        PackageID id = importz.pkgID;

        // Skip ballerina and ballerinax
        if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
            // Need to handle this to copy jar from distribution lib
            return null;
        }
        // Look if it is a project module.
        if (ProjectDirs.isModuleExist(project, id.name.value)) {
            // If so fetch from project balo cache
            return buildContext.getJarPathFromTargetCache(id);
        } else {
            // If not fetch from home balo cache.
            return buildContext.getJarPathFromHomeCache(id);
        }
        // return the path
    }
}
