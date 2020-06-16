/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.util.definition;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.exception.LSStdlibCacheException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Standard Library Cache Implementation for the Language Server.
 * This will be used by the Goto Definition and the Reference Feature Implementations
 *
 * @since 1.2.0
 */
public class LSStandardLibCache {
    private static final LSStandardLibCache STANDARD_LIB_CACHE;
    private volatile boolean cacheUpdating = false;
    private boolean cacheProjectRootSuccess;
    private LoadingCache<String, List<TopLevelNode>> topLevelNodeCache;

    static {
        STANDARD_LIB_CACHE = new LSStandardLibCache();
    }

    private LSStandardLibCache() {
        topLevelNodeCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, List<TopLevelNode>>() {
                    @Override
                    public List<TopLevelNode> load(@Nonnull String module) throws UnsupportedEncodingException,
                            LSStdlibCacheException {
                        // If the content is not in the cache then we need to extract the source content and compile
                        int versionSeparator = module.lastIndexOf("_");
                        int modNameSeparator = module.indexOf("_");
                        String version = module.substring(versionSeparator + 1);
                        String moduleName = module.substring(modNameSeparator + 1, versionSeparator);
                        String orgName = module.substring(0, modNameSeparator);
                        LSStdLibCacheUtil.extractSourceForModule(orgName, moduleName, version);
                        return getNodesForModule(module);
                    }
                });
        try {
            Files.createDirectories(CommonUtil.LS_STDLIB_CACHE_DIR);
            cacheProjectRootSuccess = true;
            populateLangLibs();
        } catch (IOException e) {
            cacheProjectRootSuccess = false;
        }
    }

    /**
     * Get the Cache Instance.
     *
     * @return {@link LSStandardLibCache} standard library cache instance
     */
    public static LSStandardLibCache getInstance() {
        return STANDARD_LIB_CACHE;
    }

    /**
     * Update the lib cache.
     *
     * @param importPackages import module to update
     */
    public void updateCache(List<BLangImportPackage> importPackages) throws LSStdlibCacheException {
        if (!cacheProjectRootSuccess) {
            throw new LSStdlibCacheException("Trying to access failed cache");
        }
        if (cacheUpdating || importPackages == null || importPackages.isEmpty()) {
            return;
        }
        cacheUpdating = true;
        Set<String> cachedModules = topLevelNodeCache.asMap().keySet();
        List<BLangImportPackage> evalModules = importPackages.parallelStream()
                .filter(importModule -> !cachedModules.contains(LSStdLibCacheUtil.getCacheableKey(importModule)))
                .collect(Collectors.toList());
        // Populate cache entries in a separate thread
        new Thread(() -> {
            try {
                evalModules.forEach(module -> {
                    String orgName = module.getOrgName().getValue();
                    String moduleName = LSStdLibCacheUtil.getCacheableKey(module);
                    try {
                        LSStdLibCacheUtil.extractSourceForImportModule(orgName, module);
                        topLevelNodeCache.put(moduleName, getNodesForModule(moduleName));
                    } catch (IOException | LSStdlibCacheException e) {
                        // Ignore the exception since the cache will not be polluted
                    }
                });
            } finally {
                cacheUpdating = false;
            }
        }).start();
    }

    /**
     * Get the top level nodes (Definitions) in a given module.
     *
     * @param pkgId PackageID
     * @return {@link List} List of module names
     */
    public List<TopLevelNode> getTopLevelNodesForModule(LSContext context, PackageID pkgId) {
        Boolean enabled = context.get(DocumentServiceKeys.ENABLE_STDLIB_DEFINITION_KEY);
        if (enabled == null || !enabled) {
            return new ArrayList<>();
        }
        String cachedModuleName = LSStdLibCacheUtil.getCacheableKey(pkgId);
        return topLevelNodeCache.getUnchecked(cachedModuleName);
    }

    /**
     * Get the standard library cache root which resides within the temp directory.
     *
     * @return {@link Path} Cache root
     * @throws LSStdlibCacheException If the cache root initialization has been failed
     */
    public Path getCachedStdlibRoot(String pkgName) throws LSStdlibCacheException {
        if (!cacheProjectRootSuccess) {
            throw new LSStdlibCacheException("Cache Root could not initialized");
        }
        return CommonUtil.LS_STDLIB_CACHE_DIR.resolve(pkgName);
    }

    private void populateLangLibs() {
        List<String> langLibs = Arrays.asList("array", "decimal", "error", "float", "future", "int", "map", "object",
                "stream", "string", "table", "typedesc", "value", "xml");
        if (cacheUpdating) {
            return;
        }
        cacheUpdating = true;
        new Thread(() -> {
            try {
                for (String langLib : langLibs) {
                    String orgName = "ballerina";
                    String moduleName = "lang." + langLib;
                    Path modulePath = LSStdLibCacheUtil.STD_LIB_SOURCE_ROOT.resolve(orgName).resolve(moduleName);
                    String version = LSStdLibCacheUtil.readModuleVersionFromDir(modulePath);
                    String cacheableKey = orgName + "_" + moduleName + "_" + version;
                    LSStdLibCacheUtil.extractSourceForModule(orgName, moduleName, version);
                    topLevelNodeCache.put(cacheableKey, getNodesForModule(cacheableKey));
                }
            } catch (LSStdlibCacheException | UnsupportedEncodingException e) {
                // Ignore
            } finally {
                cacheUpdating = false;
            }
        }).start();
    }

    private List<TopLevelNode> getNodesForModule(String moduleName) throws UnsupportedEncodingException {
        Compiler compiler = LSStdLibCacheUtil.getCompiler(CommonUtil.LS_STDLIB_CACHE_DIR.
                resolve(moduleName).toString());
        BLangPackage bLangPackage = compiler.compile(moduleName);
        List<TopLevelNode> nodes = new ArrayList<>();
        bLangPackage.getCompilationUnits().forEach(compilationUnit -> {
            List<TopLevelNode> cNodes = compilationUnit.topLevelNodes.stream()
                    .filter(topLevelNode -> {
                        BLangIdentifier nodeName;
                        switch (topLevelNode.getKind()) {
                            case FUNCTION:
                                nodeName = ((BLangFunction) topLevelNode).name;
                                break;
                            case TYPE_DEFINITION:
                                nodeName = ((BLangTypeDefinition) topLevelNode).name;
                                break;
                            case CONSTANT:
                                nodeName = ((BLangConstant) topLevelNode).name;
                                break;
                            // TODO: Handle XML Namespace Declarations
                            case ANNOTATION:
                                nodeName = ((BLangAnnotation) topLevelNode).name;
                                break;
                            default:
                                nodeName = null;
                                break;
                        }
                        return nodeName != null && !nodeName.getValue().contains("$");
                    })
                    .collect(Collectors.toList());
            nodes.addAll(cNodes);
        });
        return nodes;
    }


}
