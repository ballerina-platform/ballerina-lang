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
package org.ballerinalang.bre;

import org.ballerinalang.model.Symbol;
import org.ballerinalang.model.SymbolName;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code SymScope} represents a data structure which allows us to manage scopes.
 *
 * @since 0.8.0
 */
public class SymScope {
    private Name scopeName;
    private SymScope parent;
    private Map<SymbolName, Symbol> symbolMap;

    public SymScope(Name scopeName) {
        this(scopeName, null);
    }

    public SymScope(Name scopeName, SymScope parent) {
        this.scopeName = scopeName;
        this.parent = parent;
        this.symbolMap = new HashMap<>();
    }

    public Name getScopeName() {
        return scopeName;
    }

    public void setParent(SymScope parent) {
        this.parent = parent;
    }

    public SymScope getParent() {
        return parent;
    }

    public SymScope openScope(Name scopeName) {
        return new SymScope(scopeName, this);
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

        return null;
    }
    
    /**
     * {@code Name} represents a name of an scope.
     * <p>
     * These names defined names and cannot add random names
     *
     * @since 0.8.0
     */
    public enum Name {
        GLOBAL("_global"),
        PACKAGE("_package"),
        SERVICE("_service"),
        CONNECTOR("_connector"),
        FUNCTION("_function"),
        RESOURCE("_resource"),
        ACTION("_action"),
        STRUCT("_struct"),
        TYPEMAPPER("_typemapper");

        private String name;

        Name(String name) {
            this.name = name;
        }
    }
}
