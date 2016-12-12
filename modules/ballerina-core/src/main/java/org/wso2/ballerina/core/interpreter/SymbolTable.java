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

import org.wso2.ballerina.core.model.Identifier;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code SymbolTable} represents a data structure which hold information about the program constructs
 * <p>
 * TODO Please note that, this is the initial version of the implementation. We need to improve this.
 * TODO Refactor this implementation
 *
 * @since 1.0.0
 */
public class SymbolTable {

    private Map<Identifier, BValue> map;
    private Map<Identifier, VariableRefExpr> variableRefExprMap;
    private SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        map = new HashMap<>();
        variableRefExprMap = new HashMap<>();
        this.parent = parent;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public void put(Identifier identifier, BValue value) {
        map.put(identifier, value);
    }

    public BValue get(Identifier identifier) {
        for (SymbolTable t = this; t != null; t = t.parent) {
            BValue value = t.map.get(identifier);
            if (value != null) {
                return value;
            }
        }

        // TODO Implement proper error handling here.
        throw new RuntimeException("Value not found for identifier: " + identifier.getName());
    }

    public Identifier lookup(String identifier1) {
        Identifier identifier = new Identifier(identifier1);
        for (SymbolTable t = this; t != null; t = t.parent) {
            BValue value = t.map.get(identifier);
            if (value != null) {
                return identifier;
            }
        }

        // TODO Implement proper error handling here.
        throw new RuntimeException("Value not found for identifier: " + identifier.getName());
    }

    public void putVarRefExpr(Identifier identifier, VariableRefExpr variableRefExpr) {
        variableRefExprMap.put(identifier, variableRefExpr);
    }

    public VariableRefExpr lookupVarRefExpr(Identifier identifier) {
        for (SymbolTable t = this; t != null; t = t.parent) {
            VariableRefExpr variableRefExpr = t.variableRefExprMap.get(identifier);
            if (variableRefExpr != null) {
                return variableRefExpr;
            }
        }

        // TODO Implement proper error handling here.
        throw new RuntimeException("Variable reference '" + identifier.getName() + "'  is not declared.");
    }
}
