/**
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Map;

/**
 * Package context to keep the builtin and the current package.
 */
public class LSPackageCache {

    private static final CompilerContext.Key<LSPackageCache> LS_PACKAGE_CACHE_KEY =
            new CompilerContext.Key<>();

    private static final Object LOCK = new Object();

    private final ExtendedPackageCache packageCache;
    private static final Logger logger = LoggerFactory.getLogger(LSPackageCache.class);

    public static LSPackageCache getInstance(CompilerContext context) {
        LSPackageCache lsPackageCache = context.get(LS_PACKAGE_CACHE_KEY);
        if (lsPackageCache == null) {
            synchronized (LOCK) {
                lsPackageCache = context.get(LS_PACKAGE_CACHE_KEY);
                if (lsPackageCache == null) {
                    lsPackageCache = new LSPackageCache(context);
                }
            }
        }
        return lsPackageCache;
    }

    private LSPackageCache(CompilerContext context) {
        packageCache = new ExtendedPackageCache(context);
        context.put(LS_PACKAGE_CACHE_KEY, this);
    }

    /**
     * Find the package by Package ID.
     * @param compilerContext       compiler context
     * @param pkgId                 Package Id to lookup
     * @return {@link BLangPackage} BLang Package resolved
     */
    public BLangPackage findPackage(CompilerContext compilerContext, PackageID pkgId) {
        BLangPackage bLangPackage = packageCache.get(pkgId);
        if (bLangPackage != null) {
            return bLangPackage;
        } else {
            bLangPackage = LSPackageLoader.getPackageById(compilerContext, pkgId);
            this.put(bLangPackage.packageID, bLangPackage);
            return bLangPackage;
        }
    }

    /**
     * removes package from the package map.
     *
     * @param packageID ballerina package id to be removed.
     */
    public void invalidate(PackageID packageID) {
        packageCache.remove(packageID);
    }
    
    public void clearCache() {
        packageCache.clearCache();
    }

    /**
     * add package to the package map.
     *
     * @param bLangPackage ballerina package to be added.
     */
    public void put(PackageID packageID, BLangPackage bLangPackage) {
        if (bLangPackage != null) {
            bLangPackage.packageID = packageID;
            packageCache.put(packageID, bLangPackage);
        }
    }

    public ExtendedPackageCache getPackageCache() {
        return packageCache;
    }

    public Map<String, BLangPackage> getPackageMap() {
        return packageCache.getMap();
    }

    static class ExtendedPackageCache extends PackageCache {

        private static final long MAX_CACHE_COUNT = 100L;

        private ExtendedPackageCache(CompilerContext context) {
            super(context);
            Cache<String, BLangPackage> cache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_COUNT).build();
            this.packageMap = cache.asMap();
        }

        public Map<String, BLangPackage> getMap() {
            return this.packageMap;
        }

        public void remove(PackageID packageID) {
            if (packageID != null) {
                this.packageMap.remove(packageID.bvmAlias());
            }
        }
        
        public void clearCache() {
            this.packageMap.clear();
        }
    }
}
