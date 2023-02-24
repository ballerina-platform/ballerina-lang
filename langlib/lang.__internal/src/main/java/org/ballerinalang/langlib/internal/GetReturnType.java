/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BTypedesc;

/**
 * Native implementation of lang.internal:getReturnType(func).
 *
 * @since 1.2.0
 */
public class GetReturnType {

    public static BTypedesc getReturnType(Object obj) {
        BFunctionPointer bFunctionPointer = (BFunctionPointer) obj;
        FunctionType functionType = (FunctionType) TypeUtils.getReferredType(bFunctionPointer.getType());
        return ValueCreator.createTypedescValue(functionType.getReturnType());
    }
}
