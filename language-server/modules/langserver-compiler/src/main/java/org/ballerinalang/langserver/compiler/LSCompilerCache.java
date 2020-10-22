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
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

/**
 * Provides a thin caching layer on-top of the LSCompiler.
 * <p>
 * This Cache heavily depends on the LSP protocol for the cache eviction such that didChange, didOpen and didClose
 * clears the related entries from the cache.
 *
 * @since 1.0.0
 */
public class LSCompilerCache {
    private static final long MAX_CACHE_COUNT = 10L;
    private static Map<Key, CacheEntry> packageMap;

    static {
        Cache<Key, CacheEntry> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(MAX_CACHE_COUNT).build();
        LSCompilerCache.packageMap = cache.asMap();
    }

    private LSCompilerCache() {
    }

    /**
     * Returns cached BLangPackage.
     *
     * @param key     unique {@link Key}
     * @param context {@link LSContext}
     * @return {@link BallerinaFile}
     */
    public static CacheEntry getPackage(Key key, LSContext context) {
        return get(key, context, true);
    }

    /**
     * Returns cached BLangPackages.
     *
     * @param key     unique {@link Key}
     * @param context {@link LSContext}
     * @return {@link BallerinaFile}
     */
    public static CacheEntry getPackages(Key key, LSContext context) {
        return get(key, context, false);
    }

    /**
     * Returns cached BLangPackages.
     *
     * @param key     unique {@link Key}
     * @param context {@link LSContext}
     * @return {@link BallerinaFile}
     */
    private static CacheEntry get(Key key, LSContext context, boolean isSinglePkg) {
        CacheEntry cacheEntry = packageMap.get(key);
        if (cacheEntry == null || cacheEntry.get() == null ||
                ((isSinglePkg) ? !cacheEntry.get().isLeft() : !cacheEntry.get().isRight())) {
            return null;
        }

        context.put(DocumentServiceKeys.COMPILER_CONTEXT_KEY, cacheEntry.compilerContext);
        return cacheEntry;
    }

    /**
     * Adds BLangPackage into cache.
     *
     * @param key          unique {@link Key}
     * @param bLangPackage {@link org.wso2.ballerinalang.compiler.tree.BLangPackage}
     * @param context      {@link LSContext}
     */
    public static void putPackage(Key key, BLangPackage bLangPackage, LSContext context) {
        put(key, EitherPair.forLeft(bLangPackage), context);
    }

    /**
     * Adds BLangPackage into cache.
     *
     * @param key           unique {@link Key}
     * @param bLangPackages a list of {@link org.wso2.ballerinalang.compiler.tree.BLangPackage}
     * @param context       {@link LSContext}
     */
    public static void putPackages(Key key, List<BLangPackage> bLangPackages, LSContext context) {
        put(key, EitherPair.forRight(bLangPackages), context);
    }

    /**
     * Adds BLangPackage into cache.
     *
     * @param key           unique {@link Key}
     * @param bLangPackages either a list or a single {@link org.wso2.ballerinalang.compiler.tree.BLangPackage}
     * @param context       {@link LSContext}
     */
    private static void put(Key key, EitherPair<BLangPackage, List<BLangPackage>> bLangPackages, LSContext context) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        String sourceRoot = key.sourceRoot;
        packageMap.put(key, new CacheEntry(bLangPackages, compilerContext));
        LSClientLogger.logTrace("Operation '" + context.getOperation().getName() + "' {projectRoot: '" + sourceRoot +
                "'} added cache entry with {key: " + key + "}");
    }

    /**
     * Clears all cache entries with this source root.
     *
     * @param context    {@link LSContext}
     * @param sourceRoot source root
     */
    public static synchronized void clear(LSContext context, String sourceRoot) {
        // Remove matching entries in parallel #threadSafe
        AtomicInteger count = new AtomicInteger(0);
        packageMap.keySet().stream().filter(p -> p.sourceRoot.equals(sourceRoot)).forEach(k -> {
            packageMap.remove(k);
            count.getAndIncrement();
        });
        LSClientLogger.logTrace("Operation '" + context.getOperation().getName() + "' {projectRoot: '" + sourceRoot +
                "'} cleared " + count + " cached entries for the project");
    }

    /**
     * Clears all cache entries.
     */
    public static void clearAll() {
        packageMap.clear();
    }

    public static void markOutDated(Key key) {
        CacheEntry cacheEntry = packageMap.get(key);
        if (cacheEntry != null) {
            cacheEntry.isOutdated = true;
            packageMap.put(key, cacheEntry);
        }
    }

    /**
     * Represents a composite cache key.
     */
    public static class Key {
        private final String sourceRoot;
        private final String compilerPhase;
        private final String preserveWhitespace;
        private final String testEnabled;
        private final String skipTests;
        private final String sourceDirectory;

        public Key(String sourceRoot, LSContext context) {
            this.sourceRoot = sourceRoot;
            CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
            CompilerOptions options = CompilerOptions.getInstance(compilerContext);
            this.compilerPhase = options.get(COMPILER_PHASE);
            this.preserveWhitespace = options.get(PRESERVE_WHITESPACE);
            this.testEnabled = options.get(TEST_ENABLED);
            this.skipTests = options.get(SKIP_TESTS);
            SourceDirectory sourceDirectory = compilerContext.get(SourceDirectory.class);
            this.sourceDirectory = sourceDirectory != null ? sourceDirectory.getClass().getName() : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o || !(o instanceof Key)) {
                return false;
            }
            Key key = (Key) o;
            return (key.sourceRoot.equals(sourceRoot)
                    && compilerPhase != null && compilerPhase.equals(key.compilerPhase)
                    && preserveWhitespace != null && preserveWhitespace.equals(key.preserveWhitespace)
                    && testEnabled != null && testEnabled.equals(key.testEnabled)
                    && skipTests != null && skipTests.equals(key.skipTests)
                    && sourceDirectory != null && sourceDirectory.equals(key.sourceDirectory));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(
                    new String[]{sourceRoot, compilerPhase, preserveWhitespace, testEnabled, skipTests,
                            sourceDirectory});
        }

        @Override
        public String toString() {
            return String.format(
                    "sourceRoot %s, compilerPhase: %s, preserveWS: %s, testEnabled: %s, " +
                            "skipTests: %s, sourceDirectory: %s",
                    sourceRoot,
                    compilerPhase != null ? compilerPhase : "",
                    preserveWhitespace != null ? preserveWhitespace : "",
                    testEnabled != null ? testEnabled : "",
                    skipTests != null ? skipTests : "",
                    sourceDirectory != null ? sourceDirectory.substring(sourceDirectory.lastIndexOf(".") + 1) : "");
        }
    }

    /**
     * Represents a cache entry.
     */
    public static class CacheEntry {
        private EitherPair<BLangPackage, List<BLangPackage>> bLangPackages;
        private CompilerContext compilerContext;
        private boolean isOutdated = false;

        CacheEntry(EitherPair<BLangPackage, List<BLangPackage>> bLangPackages,
                   CompilerContext compilerContext) {
            this.bLangPackages = bLangPackages;
            this.compilerContext = compilerContext;
        }

        /**
         * Returns cached BLangPackages.
         *
         * @return {@link BLangPackage}
         */
        public EitherPair<BLangPackage, List<BLangPackage>> get() {
            return bLangPackages;
        }

        /**
         * Returns True, if cache entry is outdated.
         *
         * @return True, if cache entry is outdated, False otherwise
         */
        public boolean isOutdated() {
            return isOutdated;
        }
    }
}
