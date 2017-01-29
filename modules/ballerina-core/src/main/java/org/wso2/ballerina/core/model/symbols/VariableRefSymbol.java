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
package org.wso2.ballerina.core.model.symbols;

import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDef;

/**
 * {@code VariableRefSymbol} represents a variable reference symbol.
 *
 * @since 0.8.0
 */
public class VariableRefSymbol implements BLangSymbol {
    private SymbolName name;
    private SymbolCategory category;
    private SymbolScope scope;
    private VariableDef variableDef;

    public VariableRefSymbol(SymbolName name, SymbolScope scope) {
        this.name = name;
        this.category = SymbolCategory.VARIABLE_REF;
        this.scope = scope;
    }

    public void setVariableDef(VariableDef variableDef) {
        this.variableDef = variableDef;
    }

    public VariableDef getVariableDef() {
        return variableDef;
    }

    @Override
    public SymbolName getSymbolName() {
        return name;
    }

    @Override
    public SymbolCategory getSymbolCategory() {
        return category;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return scope;
    }
}
