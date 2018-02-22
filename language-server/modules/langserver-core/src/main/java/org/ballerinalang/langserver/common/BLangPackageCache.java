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
package org.ballerinalang.langserver.common;

import org.ballerinalang.langserver.common.utils.BallerinaPackageLoader;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Package context to keep the builtin and the current package.
 */
public class BLangPackageCache {

    private Map<String, BLangPackage> packageMap = new HashMap<>();
    
    private ArrayList<PackageID> sdkPackages = new ArrayList<>();

    public BLangPackageCache() {
        List<BLangPackage> builtInPackages = BallerinaPackageLoader.getBuiltinPackages();
        builtInPackages.forEach(this::addPackage);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getItems(Class type) {
        if (type.equals(BLangFunction.class)) {
            List<BLangFunction> functions = new ArrayList<>();
            packageMap.forEach((pkgName, bLangPackage) -> functions.addAll(bLangPackage.getFunctions()));
            return (List<T>) functions;
        }
        return null;
    }

    /**
     * Get package by name.
     *
     * @param compilerContext compiler context
     * @param name            name of the package
     * @return ballerina lang package
     */
    public BLangPackage getPackageByName(CompilerContext compilerContext, Name name) {
        if (isPackageAvailable(name.getValue())) {
            return packageMap.get(name.getValue());
        } else {
            BLangPackage bLangPackage =
                    BallerinaPackageLoader.getPackageByName(compilerContext, name.getValue());
            addPackage(bLangPackage);
            return bLangPackage;
        }
    }

    /**
     * check whether the package is available or not.
     *
     * @param name name of the package
     * @return true if the package exist else false
     */
    private boolean isPackageAvailable(String name) {
        return packageMap.get(name) != null;
    }

    /**
     * get the package name by composing from given identifier list.
     *
     * @param compos list of path identifiers
     * @return string package name
     */
    private String getPackageName(List<BLangIdentifier> compos) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < compos.size(); i++) {
            stringBuilder.append(compos.get(i).getValue());
            if ((i + 1) != compos.size()) {
                stringBuilder.append('.');
            }
        }

        return stringBuilder.toString();
    }

    /**
     * add package to the package map.
     *
     * @param bLangPackage ballerina package to be added.
     */
    public void addPackage(BLangPackage bLangPackage) {
        if (bLangPackage.getPackageDeclaration() == null) {
            this.packageMap
                    .put(".", bLangPackage);
        } else {
            this.packageMap
                    .put(getPackageName(((BLangPackageDeclaration) bLangPackage.getPackageDeclaration()).pkgNameComps)
                            , bLangPackage);
        }
    }

    /**
     * Check whether the package exist in the current package context.
     * @param packageName       package name
     * @return {@link Boolean}  package exist or not
     */
    public boolean containsPackage(String packageName) {
        return this.packageMap.containsKey(packageName);
    }

    /**
     * Get the SDK packages.
     * @param context   CompletionContext
     * @return          {@link List} list of packages visible in the SDK
     */
    public ArrayList<PackageID> getSDKPackages(CompilerContext context) {
        if (sdkPackages.isEmpty()) {
            Set<PackageID> pkgList = BallerinaPackageLoader.getPackageList(context, -1);
            sdkPackages.addAll(pkgList);
        }
        return sdkPackages;
    }
}
