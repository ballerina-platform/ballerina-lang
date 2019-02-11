/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.util.diagnostic.DiagnosticListener;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides a thin caching layer on-top of the LSCompiler.
 * <p>
 * This Cache heavily depends on the LSP protocol for the cache eviction such that didChange, didOpen and didClose
 * clears the related entries from the cache.
 */
public class LSCompilerCache {
    private static final long MAX_CACHE_COUNT = 10L;
    private static final LSCompilerCache INSTANCE = new LSCompilerCache();
    private Map<Key, BallerinaFile> packageMap;

    private LSCompilerCache() {
        Cache<Key, BallerinaFile> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_COUNT).build();
        this.packageMap = cache.asMap();
    }

    public static LSCompilerCache getInstance() {
        return INSTANCE;
    }

    /**
     * Returns cached BLangPackage.
     *
     * @param key unique {@link Key}
     * @return {@link BallerinaFile}
     */
    public BallerinaFile get(Key key) {
        return packageMap.get(key);
    }

    /**
     * Adds BLangPackage into cache.
     *
     * @param key   unique {@link Key}
     * @param value {@link BallerinaFile}
     */
    public void put(Key key, BallerinaFile value) {
        packageMap.put(key, value);
    }

    /**
     * Clears all cache entries with this source root.
     *
     * @param sourceRoot source root
     * @param moduleName module name
     */
    public void clearAll(String sourceRoot, String moduleName) {
        // Remove matching entries in parallel #threadSafe
        packageMap.keySet().parallelStream()
                .filter(p -> p.sourceRoot.equals(sourceRoot) && p.moduleName.equals(moduleName))
                .forEach(k -> packageMap.remove(k));
    }

    /**
     * Clears all cache entries.
     */
    public void clearAll() {
        packageMap.clear();
    }

    /**
     * Represents a composite cache key.
     */
    public static class Key {
        private final String sourceRoot;
        private final String diagnosticListener;
        private final String errorStrategy;
        private final String moduleName;

        public Key(String sourceRoot, String moduleName, DiagnosticListener diagnosticListener,
                   Class<? extends ANTLRErrorStrategy> errorStrategy) {
            this.sourceRoot = sourceRoot;
            this.moduleName = moduleName;
            this.diagnosticListener = (diagnosticListener != null) ? diagnosticListener.getClass().getName() : null;
            this.errorStrategy = (errorStrategy != null) ? errorStrategy.getName() : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o || !(o instanceof Key)) {
                return false;
            }
            Key key = (Key) o;
            return (key.sourceRoot.equals(sourceRoot) && key.moduleName.equals(moduleName)
                    && diagnosticListener != null && diagnosticListener.equals(key.diagnosticListener)
                    && errorStrategy != null && errorStrategy.equals(key.errorStrategy));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new String[]{sourceRoot, moduleName, diagnosticListener, errorStrategy});
        }

        @Override
        public String toString() {
            return String.format("%s, %s, %s, %s", sourceRoot, moduleName,
                                 diagnosticListener != null ?
                                         diagnosticListener.substring(diagnosticListener.lastIndexOf(".") + 1) : "",
                                 errorStrategy != null ? errorStrategy.substring(errorStrategy.lastIndexOf(".") + 1) :
                                         "");
        }
    }
}
