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

package org.ballerinalang.packerina.buildcontext.cachecontext;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.MultiModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.model.ModuleArtifactPair;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_INSTALL_DIR_PROP;

/**
 *
 */
public class ArtifactsCache {
    private final BuildContext buildContext;
    private final Path jarCache;
    private final Path birCacheDir;
    
    public ArtifactsCache(BuildContext buildContext) {
        this.buildContext = buildContext;
        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
    
        // save '<target>/cache/bir_cache' dir
        this.birCacheDir = targetDir
                .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                .resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
        
        // save '<target>/cache/jar_cache' dir
        this.jarCache = targetDir
                .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
    
    }
    
    public Path getSystemRepoBirCache() {
        return Paths.get(System.getProperty(BALLERINA_INSTALL_DIR_PROP)).resolve("bir-cache");
    }
    
    public Path getHomeRepoDir() {
        return RepoUtils.createAndGetHomeReposPath();
    }
    
    public Path getBirFromHomeCache() {
        return RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
    }
    
    public Path getHomeRepoJarCacheDir() {
        return RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
    }
    
    public Map<PackageID, ModuleArtifactPair> getBirPathsFromTargetCache() {
        try {
            Files.createDirectories(birCacheDir);
            Map<PackageID, ModuleArtifactPair> moduleBirMap = new LinkedHashMap<>();
            switch (buildContext.getSourceType()) {
                case BAL_FILE:
                    SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    String birFileName = singleFileContext.getBalFileNameWithoutExtension() +
                                         BLANG_COMPILED_PKG_BIR_EXT;
                    moduleBirMap.put(singleFileContext.getModule().packageID,
                            new ModuleArtifactPair(singleFileContext.getModule(), birCacheDir.resolve(birFileName)));
                    break;
                case SINGLE_MODULE:
                    SingleModuleContext singleModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    PackageID moduleID = singleModuleContext.getModule().packageID;
                    Path moduleBirCacheDir = Files.createDirectories(birCacheDir
                            .resolve(moduleID.orgName.value)
                            .resolve(moduleID.name.value)
                            .resolve(moduleID.version.value));
                    moduleBirMap.put(singleModuleContext.getModule().packageID,
                            new ModuleArtifactPair(singleModuleContext.getModule(),
                            moduleBirCacheDir.resolve(moduleID.name.value + BLANG_COMPILED_PKG_BIR_EXT)));
                    break;
                case ALL_MODULES:
                    MultiModuleContext multiModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    multiModuleContext.getModules()
                            .forEach(m -> {
                                Path mBirCacheDir;
                                try {
                                    mBirCacheDir = Files.createDirectories(birCacheDir
                                            .resolve(m.packageID.orgName.value)
                                            .resolve(m.packageID.name.value)
                                            .resolve(m.packageID.version.value));
                                } catch (IOException e) {
                                    throw new BLangCompilerException("error creating bir_cache dir for module: " +
                                                                     m.packageID.toString());
                                }
                                moduleBirMap.put(m.packageID, new ModuleArtifactPair(m,
                                        mBirCacheDir.resolve(m.packageID.name.value + BLANG_COMPILED_PKG_BIR_EXT)));
                            });
                    break;
                default:
                    throw new BLangCompilerException("unknown source type found: " + buildContext.getSourceType());
            }
            
            return moduleBirMap;
        } catch (IOException e) {
            throw new BLangCompilerException("error creating bir_cache dir for module(s): " + birCacheDir);
        }
    }
    
    public Map<PackageID, ModuleArtifactPair> getJarPathsFromTargetCache() {
        try {
            Files.createDirectories(jarCache);
            Map<PackageID, ModuleArtifactPair> moduleJarMap = new LinkedHashMap<>();
            switch (buildContext.getSourceType()) {
                case BAL_FILE:
                    SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    String birFileName = singleFileContext.getBalFileNameWithoutExtension() +
                                         BLANG_COMPILED_PKG_BIR_EXT;
                    moduleJarMap.put(singleFileContext.getModule().packageID,
                            new ModuleArtifactPair(singleFileContext.getModule(), jarCache.resolve(birFileName)));
                    break;
                case SINGLE_MODULE:
                    SingleModuleContext singleModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    PackageID moduleID = singleModuleContext.getModule().packageID;
                    Path moduleBirCacheDir = Files.createDirectories(jarCache
                            .resolve(moduleID.orgName.value)
                            .resolve(moduleID.name.value)
                            .resolve(moduleID.version.value));
                    moduleJarMap.put(singleModuleContext.getModule().packageID,
                            new ModuleArtifactPair(singleModuleContext.getModule(),
                                    moduleBirCacheDir.resolve(moduleID.name.value + BLANG_COMPILED_PKG_BIR_EXT)));
                    break;
                case ALL_MODULES:
                    MultiModuleContext multiModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    multiModuleContext.getModules()
                            .forEach(m -> {
                                Path mBirCacheDir;
                                try {
                                    mBirCacheDir = Files.createDirectories(jarCache
                                            .resolve(m.packageID.orgName.value)
                                            .resolve(m.packageID.name.value)
                                            .resolve(m.packageID.version.value));
                                } catch (IOException e) {
                                    throw new BLangCompilerException("error creating jar_cache dir for module: " +
                                                                     m.packageID.toString());
                                }
                                moduleJarMap.put(m.packageID,new ModuleArtifactPair(m,
                                            mBirCacheDir.resolve(m.packageID.name.value + BLANG_COMPILED_PKG_BIR_EXT)));
                            });
                    break;
                default:
                    throw new BLangCompilerException("unknown source type found: " + buildContext.getSourceType());
            }
    
            return moduleJarMap;
        } catch (IOException e) {
            throw new BLangCompilerException("error creating bir_cache dir for module(s): " + jarCache);
        }
    }
}
