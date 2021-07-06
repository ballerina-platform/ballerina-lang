/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.Type;

import java.util.StringJoiner;

/**
 * {@code BRemoteMethodType} represents a remote function in Ballerina.
 *
 * @since 2.0
 */
public class BRemoteMethodType extends BMethodType implements RemoteMethodType {
    final String[] paramNames;
    final Boolean[] paramDefaultability;

    public BRemoteMethodType(String funcName, BObjectType parent, BFunctionType type, long flags, String[] paramNames,
                             Boolean[] paramDefaultability) {
        super(funcName, parent, type, flags);
        this.paramNames = paramNames;
        this.paramDefaultability = paramDefaultability;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",", "remote function (", ") returns (" + type.retType + ")");
        for (Type type : type.paramTypes) {
            sj.add(type.getName());
        }
        return sj.toString();
    }

    @Override
    public <T extends MethodType> MethodType duplicate() {
        return new BRemoteMethodType(funcName, parentObjectType, type, flags, paramNames, paramDefaultability);
    }

    @Override
    public String[] getParamNames() {
        return paramNames;
    }

    @Override
    public Boolean[] getParamDefaultability() {
        return paramDefaultability;
    }
}
