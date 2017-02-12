/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.symbols.BLangSymbol;

import java.util.HashMap;
import java.util.Map;

/**
 * Catch Scope for invoking catch block statement.
 */
public class CatchScope implements SymbolScope {

    private final SymbolScope enclosingScope;
    private ParameterDef parameterDef;
    private Map<SymbolName, BLangSymbol> symbolMap;

    public CatchScope(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
        this.symbolMap = new HashMap<>();
    }

    public ParameterDef getParameterDef() {
        return parameterDef;
    }

    public void setParameterDef(ParameterDef parameterDef) {
        this.parameterDef = parameterDef;
    }

    @Override
    public ScopeName getScopeName() {
        return ScopeName.LOCAL;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return this.enclosingScope;
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
