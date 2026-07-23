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

import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

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

    public void remove(PackageID packageID) {
        packageMap.remove(getCacheID(packageID));
        String packageName = getPackageName(packageID);
        String packageVersion = getPackageVersion(packageID);
        Map<String, BPackageSymbol> versionMap = packageSymbolMap.get(packageName);
        if (versionMap == null) {
            return;
        }
        versionMap.remove(packageVersion);
        if (versionMap.isEmpty()) {
            packageSymbolMap.remove(packageName);
        }
    }

    public void flush() {
        packageMap.clear();
        Iterator<Map.Entry<String, Map<String, BPackageSymbol>>> pkgIterator = packageSymbolMap.entrySet().iterator();
        while (pkgIterator.hasNext()) {
            Map<String, BPackageSymbol> versionMap = pkgIterator.next().getValue();
            versionMap.entrySet().removeIf(entry -> !PackageID.isLangLibPackageID(entry.getValue().pkgID));
            if (versionMap.isEmpty()) {
                pkgIterator.remove();
            }
        }
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
        String[] packageElements = splitBvmAlias(bvmAlias);
        Map<String, BPackageSymbol> versionMap = packageSymbolMap.get(packageElements[0]);
        if (versionMap != null) {
            if (packageElements.length > 1) {
                BPackageSymbol exactMatch = versionMap.get(packageElements[1]);
                if (exactMatch != null) {
                    return exactMatch;
                }

                String lookupMajorVersion = getMajorVersion(packageElements[1]);
                BPackageSymbol packageSymbol = null;
                for (Map.Entry<String, BPackageSymbol> entry : versionMap.entrySet()) {
                    if (lookupMajorVersion.equals(getMajorVersion(entry.getKey()))) {
                        packageSymbol = entry.getValue();
                    }
                }
                return packageSymbol;
            } else {
                BPackageSymbol packageSymbol = null;
                for (BPackageSymbol symbol : versionMap.values()) {
                    packageSymbol = symbol;
                }
                return packageSymbol;
            }
        }
        return null;
    }

    public void putSymbol(PackageID packageID, BPackageSymbol packageSymbol) {
        String[] packageElements = splitBvmAlias(packageID.toString());
        Map<String, BPackageSymbol> versionMap =
                packageSymbolMap.computeIfAbsent(packageElements[0], k -> new LinkedHashMap<>());
        if (packageElements.length > 1) {
            versionMap.put(packageElements[1], packageSymbol);
        } else {
            versionMap.put(Names.DEFAULT_VERSION.value, packageSymbol);
        }
    }

    private static String getPackageName(PackageID packageID) {
        return splitBvmAlias(packageID.toString())[0];
    }

    private static String getPackageVersion(PackageID packageID) {
        String[] packageElements = splitBvmAlias(packageID.toString());
        return packageElements.length > 1 ? packageElements[1] : Names.DEFAULT_VERSION.value;
    }

    private static String[] splitBvmAlias(String bvmAlias) {
        int versionSeparatorIndex = bvmAlias.indexOf(Names.VERSION_SEPARATOR.value);
        if (versionSeparatorIndex == -1) {
            return new String[]{bvmAlias};
        }
        return new String[]{
                bvmAlias.substring(0, versionSeparatorIndex),
                bvmAlias.substring(versionSeparatorIndex + Names.VERSION_SEPARATOR.value.length())
        };
    }
}
