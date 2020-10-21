/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Compiler Context Aware Token Cache.
 *
 * @since 1.1
 */
class ParserCache {

    private static final CompilerContext.Key<ParserCache> PARSER_CACHE_KEY = new CompilerContext.Key<>();
    private boolean cacheEnabled;
    private NodeCloner nodeCloner;

    private Map<PackageID, Map<String, BLangCompilationUnit>> pkgCache = new HashMap<>();

    private ParserCache(CompilerContext context) {

        context.put(PARSER_CACHE_KEY, this);
        CompilerOptions options = CompilerOptions.getInstance(context);
        CompilerPhase compilerPhase = options.getCompilerPhase();
        this.cacheEnabled = compilerPhase.compareTo(CompilerPhase.DESUGAR) < 0;
        this.nodeCloner = NodeCloner.getInstance(context);
    }

    static ParserCache getInstance(CompilerContext context) {

        ParserCache cache = context.get(PARSER_CACHE_KEY);
        if (cache == null) {
            cache = new ParserCache(context);
        }

        return cache;
    }

    BLangCompilationUnit get(PackageID packageID, String entryName, int hash, int length) {

        if (!cacheEnabled) {
            return null;
        }
        Map<String, BLangCompilationUnit> sourceEntryCache;
        if ((sourceEntryCache = pkgCache.get(packageID)) == null) {
            return null;
        }

        BLangCompilationUnit compilationUnit = sourceEntryCache.get(entryName);
        if (compilationUnit == null || compilationUnit.hash != hash || compilationUnit.length != length) {
            return null;
        }
        return nodeCloner.cloneCUnit(compilationUnit);
    }

    void put(PackageID packageID, String entryName, int hash, int length, BLangCompilationUnit newCompUnit) {

        if (!cacheEnabled) {
            return;
        }
        Map<String, BLangCompilationUnit> sourceEntryCache;
        if ((sourceEntryCache = this.pkgCache.get(packageID)) == null || !this.pkgCache.containsKey(packageID)) {
            sourceEntryCache = new HashMap<>();
            this.pkgCache.put(packageID, sourceEntryCache);
        }

        newCompUnit.hash = hash;
        newCompUnit.length = length;
        sourceEntryCache.put(entryName, newCompUnit);
    }

}
