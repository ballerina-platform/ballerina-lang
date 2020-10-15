/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.types;

import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;

import java.util.StringJoiner;

/**
 * {@code AttachedFunction} represents a attached function in Ballerina.
 *
 * @since 0.995.0
 */
public class AttachedFunction extends BFunctionType implements AttachedFunctionType {

    public String funcName;
    public BFunctionType type;
    public BObjectType parentObjectType;

    public AttachedFunction(String funcName, BObjectType parent, BFunctionType type, int flags) {
        this.funcName = funcName;
        this.type = type;
        this.parentObjectType = parent;
        this.flags = flags;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",", "function " + funcName + "(", ") returns (" + type.retType + ")");
        for (Type type : type.paramTypes) {
            sj.add(type.getName());
        }
        return sj.toString();
    }

    @Override
    public Type[] getParameterTypes() {
        return type.paramTypes;
    }

    @Override
    public String getName() {
        return this.funcName;
    }

    @Override
    public String getAnnotationKey() {
        return parentObjectType.getAnnotationKey() + "." + funcName;
    }

    @Override
    public ObjectType getParentObjectType() {
        return parentObjectType;
    }

    public FunctionType getType() {
        return type;
    }
}
