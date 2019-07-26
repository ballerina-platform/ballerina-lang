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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.BuilderUtils;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.MultiModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.ballerinalang.util.BootstrapRunner;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Task for creating jar file.
 */
public class CreateJarTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        try {
            Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
            // create '<target>/cache/jar_cache' dir
            Path jarCache = targetDir
                    .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                    .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
        
            if (!Files.exists(jarCache)) {
                Files.createDirectories(jarCache);
            }
        
            // add jar_cache to build context
            buildContext.put(BuildContextField.JAR_CACHE_DIR, jarCache);
            
            Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
            Path projectBIRCache = buildContext.get(BuildContextField.BIR_CACHE_DIR);
            Path homeBIRCache = buildContext.get(BuildContextField.HOME_BIR_CACHE_REPO);
            Path systemBIRCache = buildContext.get(BuildContextField.SYSTEM_BIR_CACHE);
        
            if (buildContext.getSourceType() == SourceType.BAL_FILE) {
                SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                BLangPackage module = singleFileContext.getModule();
                // Iterate the imports and decide where to save.
                // - If it is a current project we save to Project
                // - If is is an import we save to Home
                writeImportJar(module.symbol.imports, sourceRoot, buildContext,
                        projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
                // Generate the jar of the package.
                Path entryBir = BuilderUtils.resolveBirPath(buildContext, module.packageID);
                Path jarOutput = BuilderUtils.resolveJarPath(buildContext, module.packageID);
                Files.createDirectories(jarOutput);
                
                BootstrapRunner.generateJarBinary(entryBir.toString(), jarOutput.toString(), false,
                        projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
            } else {
                List<BLangPackage> modules = new LinkedList<>();
                if (buildContext.getSourceType() == SourceType.SINGLE_MODULE) {
                    SingleModuleContext moduleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    modules.add(moduleContext.getModule());
                } else {
                    MultiModuleContext multiModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    modules = multiModuleContext.getModules();
                }
        
                for (BLangPackage module : modules) {
                    writeImportJar(module.symbol.imports, sourceRoot, buildContext,
                            projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
                    // Generate the jar of the package.
                    Path entryBir = BuilderUtils.resolveBirPath(buildContext, module.packageID);
                    Path jarOutput = BuilderUtils.resolveJarPath(buildContext, module.packageID);
                    Files.createDirectories(jarOutput);
        
                    BootstrapRunner.generateJarBinary(entryBir.toString(), jarOutput.toString(), false,
                            projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
                }
            }
        } catch (IOException e) {
            throw new BLangCompilerException("error occurred generating .jar file");
        }
    }
    
    private static void writeImportJar(List<BPackageSymbol> imports, Path sourceRoot, BuildContext buildContext,
                                       String... reps) {
        for (BPackageSymbol bimport : imports) {
            PackageID id = bimport.pkgID;
            try {
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
                    Files.createDirectories(jarFilePath);
                    BootstrapRunner.generateJarBinary(birFilePath.toString(), jarFilePath.toString(), false, reps);
                }
                writeImportJar(bimport.imports, sourceRoot, buildContext);
            } catch (IOException e) {
                String msg = "error writing the compiled module(jar) of '" +
                             id.name.value + "' to home repo jar-cache: " + e.getMessage();
                throw new BLangCompilerException(msg, e);
            }
        }
    }
}
