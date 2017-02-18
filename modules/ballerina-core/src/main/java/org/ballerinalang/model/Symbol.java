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
package org.ballerinalang.model;

import org.ballerinalang.bre.MemoryLocation;
import org.ballerinalang.bre.SymScope;
import org.ballerinalang.model.types.BType;

/**
 * {@code Symbol} represents a data structure that simply defines the type of variables,
 * function invocation exprs etc.
 *
 * @since 0.8.0
 */
public class Symbol {

    private BType type;
    private SymScope.Name scopeName;
    private MemoryLocation location;

    private Function function;
    private TypeMapper typeMapper;
    private Action action;
    private Connector connector;
    private StructDef structDef;

    public Symbol(BType type, SymScope.Name scopeName, MemoryLocation location) {
        this.type = type;
        this.scopeName = scopeName;
        this.location = location;
    }

    public Symbol(Function function) {
        this.function = function;
    }

    public Symbol(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    public Symbol(Action action) {
        this.action = action;
    }

    public Symbol(Connector connector) {
        this.connector = connector;
    }

    public Symbol(StructDef structDef) {
        this.structDef = structDef;
    }

    public BType getType() {
        return type;
    }

    public SymScope.Name getScopeName() {
        return scopeName;
    }

    public MemoryLocation getLocation() {
        return location;
    }

    public Function getFunction() {
        return function;
    }

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    public Action getAction() {
        return action;
    }

    public Connector getConnector() {
        return connector;
    }
    
    public StructDef getStructDef() {
        return structDef;
    }
}

