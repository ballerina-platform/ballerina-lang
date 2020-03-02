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
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.exception.LSStdlibCacheException;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.diagnotic.DefaultDiagnosticListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Standard Library Cache Implementation for the Language Server.
 * This will be used by the Goto Definition and the Reference Feature Implementations
 *
 * @since 1.2.0
 */
public class LSStandardLibCache {
    private static final LSStandardLibCache STANDARD_LIB_CACHE;
    private volatile boolean cacheUpdating = false;
    private Path stdlibSourceRoot;
    private boolean cacheProjectRootSuccess;
    private LoadingCache<String, List<TopLevelNode>> topLevelNodeCache;

    static {
        STANDARD_LIB_CACHE = new LSStandardLibCache();
    }

    private LSStandardLibCache() {
        topLevelNodeCache = CacheBuilder.newBuilder()
                .maximumSize(25)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, List<TopLevelNode>>() {
                    @Override
                    public List<TopLevelNode> load(@Nonnull String module) throws UnsupportedEncodingException {
                        return getNodesForModule(module);
                    }
                });
        try {
            Files.createDirectories(CommonUtil.LS_STDLIB_CACHE_DIR);
            cacheProjectRootSuccess = true;
        } catch (IOException e) {
            cacheProjectRootSuccess = false;
            return;
        }
        stdlibSourceRoot = Paths.get(CommonUtil.BALLERINA_HOME).resolve("lib").resolve("repo");
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
        Set<String> cachedModules = topLevelNodeCache.asMap().keySet();
        List<BLangImportPackage> evalModules = importPackages.parallelStream()
                .filter(importModule -> !cachedModules.contains(LSStdLibCacheUtil.getCacheableKey(importModule)))
                .collect(Collectors.toList());
        // Populate cache entries in a separate thread
        new Thread(() -> {
            try {
                cacheUpdating = true;
                evalModules.forEach(module -> {
                    String orgName = module.getOrgName().getValue();
                    String moduleName = LSStdLibCacheUtil.getCacheableKey(module);
                    try {
                        LSStdLibCacheUtil.extractSourceFromBalo(stdlibSourceRoot.resolve(orgName), module);
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

    private List<TopLevelNode> getNodesForModule(String moduleName) throws UnsupportedEncodingException {
        Compiler compiler = getCompiler(CommonUtil.LS_STDLIB_CACHE_DIR.resolve(moduleName).toString());
        BLangPackage bLangPackage = compiler.compile(moduleName);
        return bLangPackage.topLevelNodes.stream()
                .filter(topLevelNode -> {
                    Set<Flag> flagSet;
                    switch (topLevelNode.getKind()) {
                        case FUNCTION:
                            flagSet = ((BLangFunction) topLevelNode).flagSet;
                            break;
                        case TYPE_DEFINITION:
                            flagSet = ((BLangTypeDefinition) topLevelNode).flagSet;
                            break;
                        case CONSTANT:
                            flagSet = ((BLangConstant) topLevelNode).flagSet;
                            break;
                        // TODO: Handle XML Namespace Declarations
                        case ANNOTATION:
                            flagSet = ((BLangAnnotation) topLevelNode).flagSet;
                            break;
                        default:
                            flagSet = new HashSet<>();
                            break;
                    }
                    return flagSet.contains(Flag.PUBLIC);
                })
                .collect(Collectors.toList());
    }

    private CompilerContext createNewCompilerContext(String projectDir) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, projectDir);
        options.put(COMPILER_PHASE, CompilerPhase.DESUGAR.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.toString(false));
        options.put(OFFLINE, Boolean.toString(true));
        options.put(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(true));
        context.put(SourceDirectory.class, new FileSystemProjectDirectory(Paths.get(projectDir)));
        context.put(DiagnosticListener.class, new DefaultDiagnosticListener());
        return context;
    }

    private Compiler getCompiler(String projectDir) throws UnsupportedEncodingException {
        Compiler compiler = Compiler.getInstance(createNewCompilerContext(projectDir));
        compiler.setOutStream(new LSCompilerUtil.EmptyPrintStream());

        return compiler;
    }
}
