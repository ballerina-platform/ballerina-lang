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
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.RemoteMethodType;

import java.util.StringJoiner;

/**
 * {@code BRemoteMethodType} represents a remote function in Ballerina.
 *
 * @since 2.0
 */
public class BRemoteMethodType extends BMethodType implements RemoteMethodType {

    public BRemoteMethodType(String funcName, BObjectType parent, BFunctionType type, long flags) {
        super(funcName, parent, type, flags);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",", "remote function (", ") returns (" + type.retType + ")");
        for (Parameter parameter : parameters) {
            sj.add(parameter.type.getName());
        }
        return sj.toString();
    }

    @Override
    public <T extends MethodType> MethodType duplicate() {
        return new BRemoteMethodType(funcName, parentObjectType, type, flags);
    }

}
