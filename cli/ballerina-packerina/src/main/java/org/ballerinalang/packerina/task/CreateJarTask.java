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

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.writer.JarFileWriter;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DIST_BIR_CACHE_DIR_NAME;

/**
 * Task for creating jar file.
 */
public class CreateJarTask implements Task {

    private boolean skipCopyLibsFromDist = false;

    public CreateJarTask() {

    }

    public CreateJarTask(boolean skipCopyLibsFromDist) {

        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
    }

    @Override
    public void execute(BuildContext buildContext) {
        // This will avoid initializing Config registry during jar creation.
        ConfigRegistry.getInstance().setInitialized(true);
        Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        Path runtimeJar = getRuntimeAllJar(buildContext);

        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        PackageCache packageCache = PackageCache.getInstance(context);

        JarFileWriter jarFileWriter = JarFileWriter.getInstance(context);

        List<BLangPackage> moduleBirMap = buildContext.getModules();
        Set<PackageID> alreadyImportedModuleSet = new HashSet<>();
        for (BLangPackage module : moduleBirMap) {

            BLangPackage bLangPackage = packageCache.get(module.packageID);
            if (bLangPackage == null) {
                continue;
            }

            PackageID packageID = bLangPackage.packageID;

            HashSet<Path> moduleDependencies = buildContext.moduleDependencyPathMap.get(packageID).moduleLibs;
            if (!skipCopyLibsFromDist) {
                moduleDependencies.add(runtimeJar);
            }
            // write module child imports jars
            writeImportJar(jarFileWriter, bLangPackage.symbol.imports, sourceRoot, buildContext, runtimeJar,
                    alreadyImportedModuleSet, balHomePath);

            // get the jar path of the module.
            Path jarOutput = buildContext.getJarPathFromTargetCache(module.packageID);
            if (!Files.exists(jarOutput)) {
                jarFileWriter.write(bLangPackage, jarOutput);
                alreadyImportedModuleSet.add(module.packageID);
            }

            // If there is a testable package we will create testable jar.
            if (!buildContext.skipTests() && bLangPackage.hasTestablePackage()) {
                for (BLangPackage testPkg : bLangPackage.getTestablePkgs()) {
                    // write its child imports jar file to cache
                    writeImportJar(jarFileWriter, testPkg.symbol.imports, sourceRoot, buildContext,
                            runtimeJar, alreadyImportedModuleSet, balHomePath);

                    // get the jar path of the module.
                    Path testJarOutput = buildContext.getTestJarPathFromTargetCache(testPkg.packageID);
                    if (!Files.exists(testJarOutput)) {
                        jarFileWriter.write(testPkg, testJarOutput);
                        alreadyImportedModuleSet.add(testPkg.packageID);
                    }
                }
            }
        }
        ConfigRegistry.getInstance().setInitialized(false);
    }

    private void writeImportJar(JarFileWriter jarFileWriter, List<BPackageSymbol> imports, Path sourceRoot,
                                BuildContext buildContext, Path runtimeJar, Set<PackageID> alreadyImportedModuleSet,
                                String balHomePath) {

        for (BPackageSymbol bimport : imports) {
            PackageID id = bimport.pkgID;
            String version = BLANG_PKG_DEFAULT_VERSION;
            if (!id.version.value.equals("")) {
                version = id.version.value;
            }
            // Skip ballerina and ballerinax for cached modules
            if (alreadyImportedModuleSet.contains(id) ||
                    Paths.get(balHomePath, DIST_BIR_CACHE_DIR_NAME, id.orgName.value,
                            id.name.value, version).toFile().exists()) {
                continue;
            }
            alreadyImportedModuleSet.add(id);
            Path jarFilePath;
            // If the module is part of the project write it to project jar cache check if file exist
            // If not write it to home jar cache
            // skip ballerina and ballerinax
            if (ProjectDirs.isModuleExist(sourceRoot, id.name.value) ||
                    buildContext.getImportPathDependency(id).isPresent()) {
                jarFilePath = buildContext.getJarPathFromTargetCache(id);
            } else {
                jarFilePath = buildContext.getJarPathFromHomeCache(id);
            }
            writeImportJar(jarFileWriter, bimport.imports, sourceRoot,
                    buildContext, runtimeJar, alreadyImportedModuleSet, balHomePath);
            if (bimport.bir != null && buildContext.moduleDependencyPathMap.containsKey(id)) {
                HashSet<Path> moduleDependencySet = buildContext.moduleDependencyPathMap.get(id).moduleLibs;
                if (!skipCopyLibsFromDist) {
                    moduleDependencySet.add(runtimeJar);
                }
                jarFileWriter.write(bimport, jarFilePath);
            }
        }
    }

    private Path getRuntimeAllJar(BuildContext buildContext) {

        if (skipCopyLibsFromDist) {
            return null;
        }
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        String ballerinaVersion = RepoUtils.getBallerinaVersion();
        String runtimeJarName = "ballerina-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return Paths.get(balHomePath, "bre", "lib", runtimeJarName);
    }
}
