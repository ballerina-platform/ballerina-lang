/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.scope;

import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.symbols.BLangSymbol;

import java.util.HashMap;
import java.util.Map;

/**
 * Scope class to support package listing in API level
 */
public class APIScope implements SymbolScope {
    private Map<SymbolName, BLangSymbol> symbolMap;

    public APIScope() {
        symbolMap = new HashMap<>();
    }

    /**
     * Get Symbol Map.
     *
     * @return symbol map {@code APIScope}
     */
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return symbolMap;
    }

    /**
     * Set symbol map.
     *
     * @param symbolMap symbol map {@code APIScope}
     */
    public void setSymbolMap(Map<SymbolName, BLangSymbol> symbolMap) {
        this.symbolMap = symbolMap;
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
}
