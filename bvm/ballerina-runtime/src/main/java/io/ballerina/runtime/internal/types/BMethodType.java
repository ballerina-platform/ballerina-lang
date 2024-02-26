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
package io.ballerina.runtime.internal.types;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;

import java.util.StringJoiner;

/**
 * {@code BMethodType} represents a attached function in Ballerina.
 *
 * @since 0.995.0
 */
public class BMethodType extends BFunctionType implements MethodType {

    public String funcName;
    public BFunctionType type;
    public BObjectType parentObjectType;

    public BMethodType(String funcName, Module pkg, BObjectType parent, BFunctionType type, long flags) {
        super(pkg);
        this.funcName = funcName;
        this.type = type;
        this.parentObjectType = parent;
        this.flags = flags;
        this.parameters = type.parameters;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",", "function " + funcName + "(", ") returns (" + type.retType + ")");
        for (Parameter parameter : parameters) {
            sj.add(parameter.type.getName());
        }
        return sj.toString();
    }

    @Override
    public String getName() {
        return this.funcName;
    }

    @Override
    public String getAnnotationKey() {
        return Utils.decodeIdentifier(parentObjectType.getAnnotationKey()) + "." + Utils.decodeIdentifier(funcName);
    }

    @Override
    public ObjectType getParentObjectType() {
        return parentObjectType;
    }

    public FunctionType getType() {
        return type;
    }

    public <T extends MethodType> MethodType duplicate() {
        return new BMethodType(funcName, pkg, parentObjectType, type, flags);
    }

    @Override
    public boolean isIsolated() {
        return SymbolFlags.isFlagOn(flags, SymbolFlags.ISOLATED);
    }
}
