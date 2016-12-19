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
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.types.TypeC;

/**
 * {@code SymbolName} represents an identifier in Ballerina
 * *
 * @since 1.0.0
 */
public class SymbolName {

    private String name;
    private SymType symType;
//    private Symbol symbol;
    private TypeC[] parameters;

    public SymbolName(String name) {
        this.name = name;
    }

    public SymbolName(String name, SymType symType) {
        this.name = name;
        this.symType = symType;
    }

    public SymbolName(String name, SymType symType, TypeC[] parameters) {
        this.name = name;
        this.symType = symType;
        this.parameters = parameters;
    }

    /**
     * Get the name of the Identifier
     *
     * @return name of the Identifier
     */
    public String getName() {
        return name;
    }

    public void setSymType(SymType symType) {
        this.symType = symType;
    }

//    public void setSymbol(Symbol symbol) {
//        this.symbol = symbol;
//    }

    public TypeC[] getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object obj) {
        SymbolName other = (SymbolName) obj;
        return this.name.equals(other.getName());
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result;
        return result;
    }

    /**
     *
     */
    public enum SymType {
        VARIABLE,
        CALLABLE_UNIT,
        CALLABLE_UNIT_GROUP
    }
}
