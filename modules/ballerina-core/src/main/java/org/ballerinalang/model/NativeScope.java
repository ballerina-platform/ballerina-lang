/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model;

import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.natives.NativePackageProxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent scope which registers native constructs.
 *
 * @since 0.87
 */
public class NativeScope implements SymbolScope {
    private Map<SymbolName, BLangSymbol> symbolMap;

    private static NativeScope instance = new NativeScope();

    private NativeScope() {
        symbolMap = new HashMap<>();
    }

    public static NativeScope getInstance() {
        return instance;
    }

    @Override
    public ScopeName getScopeName() {
        return ScopeName.NATIVE;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return null;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        if (name.getPkgPath() == null) {
            return resolve(symbolMap, name);
        }

        // resolve the package symbol first
        SymbolName pkgSymbolName = new SymbolName(name.getPkgPath());
        BLangSymbol pkgSymbol = resolve(symbolMap, pkgSymbolName);
        if (pkgSymbol == null) {
            return null;
        }

        if (pkgSymbol instanceof NativePackageProxy) {
            pkgSymbol = ((NativePackageProxy) pkgSymbol).load();
        }

        return ((BLangPackage) pkgSymbol).resolveMembers(name);
    }

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

}
