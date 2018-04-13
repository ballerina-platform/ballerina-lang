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

import java.util.HashMap;
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
    protected Map<String, BPackageSymbol> packageSymbolMap;

    public static PackageCache getInstance(CompilerContext context) {
        PackageCache packageCache = context.get(PACKAGE_CACHE_KEY);
        if (packageCache == null) {
            packageCache = new PackageCache(context);
        }
        return packageCache;
    }

    protected PackageCache(CompilerContext context) {
        context.put(PACKAGE_CACHE_KEY, this);
        this.packageMap = new HashMap<>();
        this.packageSymbolMap = new HashMap<>();
    }

    public BLangPackage get(PackageID packageID) {
        return packageMap.get(packageID.bvmAlias());
    }

    public BLangPackage get(String pkgPath) {
        return packageMap.get(pkgPath);
    }

    public void put(PackageID packageID, BLangPackage bLangPackage) {
        if (bLangPackage != null) {
            bLangPackage.packageID = packageID;
        }
        packageMap.put(packageID.bvmAlias(), bLangPackage);
    }

    public BPackageSymbol getSymbol(PackageID packageID) {
        return this.packageSymbolMap.get(packageID.bvmAlias());
    }

    public void putSymbol(PackageID packageID, BPackageSymbol packageSymbol) {
        this.packageSymbolMap.put(packageID.bvmAlias(), packageSymbol);
    }
}
