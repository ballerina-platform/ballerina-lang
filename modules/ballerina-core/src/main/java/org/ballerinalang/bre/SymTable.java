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

/**
 * {@code SymTable} represents a data structure which hold information about the program constructs.
 * <p>
 *
 * @since 0.8.0
 */
public class SymTable {

    private SymScope current;

    public SymTable(SymScope symScope) {
        current = symScope;
    }

    public void openScope(SymScope.Name name) {
        current = current.openScope(name);
    }

    public SymScope getCurrentScope() {
        return current;
    }

    public void closeScope() {
        // TODO check whether we are closing the global scope. Need to avoid this condition
        current = current.closeScope();
    }

    public void insert(SymbolName symName, Symbol symbol) {
        current.insert(symName, symbol);
    }

    public Symbol lookup(SymbolName symName) {
        return current.lookup(symName);
    }
    
    /**
     * Get the parent scope of this scope.
     * 
     * @return  Parent scope
     */
    public SymScope getParentScope() {
        return current.getParent();
    }
}
