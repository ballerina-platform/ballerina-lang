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
 * {@code Symbol} represents a data structure that simply defines the type of variables,
 * function invocation exprs etc
 *
 * @since 1.0.0
 */
public class Symbol {

    private TypeC type;
    private int offset;

    private TypeC[] paramTypes;
    private TypeC[] returnTypes;
    private Function function;

    private Action action;

    public Symbol(TypeC type, int offset) {
        this.type = type;
        this.offset = offset;
    }

    public Symbol(Function function, TypeC[] paramTypes, TypeC[] returnTypes) {
        this.function = function;
        this.paramTypes = paramTypes;
        this.returnTypes = returnTypes;
    }

    public Symbol(Action action, TypeC[] paramTypes, TypeC[] returnTypes) {
        this.action = action;
        this.paramTypes = paramTypes;
        this.returnTypes = returnTypes;
    }

    public TypeC getType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }

    public TypeC[] getParamTypes() {
        return paramTypes;
    }

    public TypeC[] getReturnTypes() {
        return returnTypes;
    }

    public Function getFunction() {
        return function;
    }

    public Action getAction() {
        return action;
    }
}

