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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @since 0.8.0
 */
public class GlobalScope implements SymbolScope {
    private Map<SymbolName, BLangSymbol> symbolMap;

    private static GlobalScope instance = new GlobalScope();

    private GlobalScope() {
        symbolMap = new HashMap<>();
    }

    public static GlobalScope getInstance() {
        return instance;
    }

    @Override
    public ScopeName getScopeName() {
        return ScopeName.GLOBAL;
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
        return resolve(symbolMap, name);
    }

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }
}
