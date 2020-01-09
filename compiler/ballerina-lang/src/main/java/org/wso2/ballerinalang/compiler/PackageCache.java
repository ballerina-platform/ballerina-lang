/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A cache of parsed package nodes.
 *
 * @since 0.965.0
 */
public class PackageCache {

    private static final CompilerContext.Key<PackageCache> PACKAGE_CACHE_KEY =
            new CompilerContext.Key<>();

    protected Map<String, BLangPackage> packageMap;
    protected Map<String, Map<String, BPackageSymbol>> packageSymbolMap;

    public static PackageCache getInstance(CompilerContext context) {
        PackageCache packageCache = context.get(PACKAGE_CACHE_KEY);
        if (packageCache == null) {
            packageCache = new PackageCache(context);
        }
        return packageCache;
    }

    public static void setInstance(PackageCache packageCache, CompilerContext context) {
        context.put(PACKAGE_CACHE_KEY, packageCache);
    }

    protected PackageCache(CompilerContext context) {
        context.put(PACKAGE_CACHE_KEY, this);
        this.packageMap = new HashMap<>();
        this.packageSymbolMap = new HashMap<>();
    }

    public BLangPackage get(PackageID packageID) {
        return packageMap.get(getCacheID(packageID));
    }

    public BLangPackage get(String pkgPath) {
        return packageMap.get(pkgPath);
    }

    public void put(PackageID packageID, BLangPackage bLangPackage) {
        if (bLangPackage != null) {
            bLangPackage.packageID = packageID;
        }
        packageMap.put(getCacheID(packageID), bLangPackage);
    }

    public static String getCacheID(PackageID packageID) {
        String bvmAlias = packageID.toString();
        if (packageID.sourceFileName != null) {
            bvmAlias = bvmAlias + "-" + packageID.sourceFileName.getValue();
        }
        return bvmAlias;
    }

    public BPackageSymbol getSymbol(PackageID packageID) {
        return getSymbol(packageID.toString());
    }

    public BPackageSymbol getSymbol(String bvmAlias) {
        String[] packageElements = bvmAlias.split(Names.VERSION_SEPARATOR.value);
        Map<String, BPackageSymbol> versionMap = packageSymbolMap.get(packageElements[0]);
        if (versionMap != null) {
            if (packageElements.length > 1) {
                return versionMap.get(packageElements[1]);
            } else {
                Iterator<BPackageSymbol> itr = versionMap.values().iterator();
                if (itr.hasNext()) {
                    return itr.next();
                }
            }
        }
        return null;
    }

    public void putSymbol(PackageID packageID, BPackageSymbol packageSymbol) {
        String[] packageElements = packageID.toString().split(Names.VERSION_SEPARATOR.value);
        Map<String, BPackageSymbol> versionMap =
                packageSymbolMap.computeIfAbsent(packageElements[0], k -> new LinkedHashMap<>());
        if (packageElements.length > 1) {
            versionMap.put(packageElements[1], packageSymbol);
        } else {
            versionMap.put(Names.DEFAULT_VERSION.value, packageSymbol);
        }
    }
}
