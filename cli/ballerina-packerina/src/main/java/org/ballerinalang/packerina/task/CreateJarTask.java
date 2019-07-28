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
import org.ballerinalang.packerina.BuilderUtils;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.cachecontext.ArtifactsCache;
import org.ballerinalang.packerina.model.ModuleArtifactPair;
import org.ballerinalang.util.BootstrapRunner;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Task for creating jar file.
 */
public class CreateJarTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
        Path projectBIRCache = buildContext.get(BuildContextField.BIR_CACHE_DIR);
        ArtifactsCache artifactsCache = buildContext.get(BuildContextField.ARTIFACTS_CACHE);
        Path homeBIRCache = artifactsCache.getBirFromHomeCache();
        Path systemBIRCache = artifactsCache.getSystemRepoBirCache();

        Map<PackageID, ModuleArtifactPair> moduleBirMap = artifactsCache.getBirPathsFromTargetCache();
        Map<PackageID, ModuleArtifactPair> moduleJarMap = artifactsCache.getJarPathsFromTargetCache();
        
        for (Map.Entry<PackageID, ModuleArtifactPair> moduleArtifactEntry : moduleJarMap.entrySet()) {
            
            writeImportJar(moduleArtifactEntry.getValue().getModule().symbol.imports, sourceRoot, buildContext,
                    projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());

            // get the bir path of the module
            Path entryBir = moduleBirMap.get(moduleArtifactEntry.getKey()).getArtifactPath();
            
            // get the jar path of the module.
            Path jarOutput = moduleArtifactEntry.getValue().getArtifactPath();

            BootstrapRunner.generateJarBinary(entryBir.toString(), jarOutput.toString(), false,
                    projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
        }
    }
    
    private static void writeImportJar(List<BPackageSymbol> imports, Path sourceRoot, BuildContext buildContext,
                                       String... reps) {
        for (BPackageSymbol bimport : imports) {
            PackageID id = bimport.pkgID;
            if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
                continue;
            }
            Path jarFilePath;
            Path birFilePath;
            // If the module is part of the project write it to project jar cache check if file exist
            // If not write it to home jar cache
            // skip ballerina and ballerinax
            if (ProjectDirs.isModuleExist(sourceRoot, id.name.value)) {
                jarFilePath = BuilderUtils.resolveJarPath(buildContext, id);
                birFilePath = BuilderUtils.resolveBirPath(buildContext, id);
            } else {
                jarFilePath = BuilderUtils.resolveJarPath(buildContext,
                        buildContext.get(BuildContextField.HOME_JAR_CACHE_REPO), id);
                birFilePath = BuilderUtils.resolveJarPath(buildContext,
                        buildContext.get(BuildContextField.HOME_BIR_CACHE_REPO), id);
            }
            if (!Files.exists(jarFilePath)) {
                BootstrapRunner.generateJarBinary(birFilePath.toString(), jarFilePath.toString(), false, reps);
            }
            writeImportJar(bimport.imports, sourceRoot, buildContext);
        }
    }
}
