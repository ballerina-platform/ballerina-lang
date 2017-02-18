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

import java.util.Map;

/**
 * {@code SymbolScope} represents the base interface of a structure which allows us to manage scopes.
 *
 * @since 0.8.0
 */
public interface SymbolScope {

    ScopeName getScopeName();

    SymbolScope getEnclosingScope();

    void define(SymbolName name, BLangSymbol symbol);

    BLangSymbol resolve(SymbolName name);

    Map<SymbolName, BLangSymbol> getSymbolMap();

    default BLangSymbol resolve(Map<SymbolName, BLangSymbol> symbolMap, SymbolName name) {
        BLangSymbol symbol = symbolMap.get(name);
        if (symbol == null && getEnclosingScope() != null) {
            return getEnclosingScope().resolve(name);
        }

        return symbol;
    }

    /**
     * {@code ScopeName} is used to name symbol scopes in Ballerina.
     *
     * @since 0.8.0
     */
    enum ScopeName {
        GLOBAL,
        PROGRAM,
        PACKAGE,
        SERVICE,
        CONNECTOR,
        WORKER,
        LOCAL  // The term 'LOCAL' represents a function, an action or a resource scope.
    }
}
