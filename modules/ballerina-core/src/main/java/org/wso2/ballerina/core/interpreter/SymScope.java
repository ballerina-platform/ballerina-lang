/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code SymScope} represents a data structure which allows check the scope of a symbol.
 * <p>
 *
 * @since 1.0.0
 */
public class SymScope {
    private SymScope parent;
    private Map<SymbolName, Symbol> symbolMap;

    public SymScope() {
        this.parent = null;
        this.symbolMap = new HashMap<>();
    }

    public SymScope(SymScope parent) {
        this.parent = parent;
        this.symbolMap = new HashMap<>();
    }

    public SymScope openScope() {
        return new SymScope(this);
    }

    public SymScope closeScope() {
        return parent;
    }

    public void insert(SymbolName symName, Symbol symbol) {
        symbolMap.put(symName, symbol);
    }

    // TODO implement a recursive lookup method
    public Symbol lookup(SymbolName symName) {
        for (SymScope t = this; t != null; t = t.parent) {
            Symbol symbol = t.symbolMap.get(symName);
            if (symbol != null) {
                return symbol;
            }
        }

        // TODO Implement proper error handling here.
//        throw new RuntimeException("Variable reference '" + symName.getName() + "'  is not declared.");
        return null;
    }
}
