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

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;

import java.util.List;

/**
 * Native implementation of lang.internal:getMapFunc(func).
 *
 * @since 1.2.0
 */
public final class GetMapFunc {

    private GetMapFunc() {
    }

    public static BFunctionPointer getMapFunc(Object obj) {
        BFunctionPointer functionPointer = (BFunctionPointer) obj;
        FunctionType functionType = (FunctionType) TypeUtils.getImpliedType(functionPointer.getType());
        functionType.getParameters()[0].type = TypeCreator.createUnionType(List.of(PredefinedTypes.TYPE_ANY,
                PredefinedTypes.TYPE_ERROR), 0);
        return functionPointer;
    }
}
