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

import org.wso2.ballerina.core.interpreter.MemoryLocation;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.types.BType;

/**
 * {@code Symbol} represents a data structure that simply defines the type of variables,
 * function invocation exprs etc
 *
 * @since 1.0.0
 */
public class Symbol {

    private BType type;
    private int offset;
    private SymScope.Name scopeName;
    private MemoryLocation location;

    private BType[] paramTypes;
    private BType[] returnTypes;
    private Function function;

    private Action action;
    private Connector connector;

    public Symbol(BType type, SymScope.Name scopeName, MemoryLocation location) {
        this.type = type;
        this.scopeName = scopeName;
        this.location = location;
    }

    public Symbol(Function function, BType[] paramTypes, BType[] returnTypes) {
        this.function = function;
        this.paramTypes = paramTypes;
        this.returnTypes = returnTypes;
    }

    public Symbol(Action action, BType[] paramTypes, BType[] returnTypes) {
        this.action = action;
        this.paramTypes = paramTypes;
        this.returnTypes = returnTypes;
    }

    public Symbol(Connector connector, BType[] paramTypes) {
        this.connector = connector;
        this.paramTypes = paramTypes;
    }


    public BType getType() {
        return type;
    }

    public SymScope.Name getScopeName() {
        return scopeName;
    }

    public int getOffset() {
        return offset;
    }

    public MemoryLocation getLocation() {
        return location;
    }

    public BType[] getParamTypes() {
        return paramTypes;
    }

    public BType[] getReturnTypes() {
        return returnTypes;
    }

    public Function getFunction() {
        return function;
    }

    public Action getAction() {
        return action;
    }

    public Connector getConnector() {
        return connector;
    }
}

