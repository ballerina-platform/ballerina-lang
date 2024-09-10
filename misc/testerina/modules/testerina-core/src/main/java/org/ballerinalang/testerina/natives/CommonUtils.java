// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.testerina.natives;

import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BFunctionPointer;

/**
 * Common utility functions for the Testerina module.
 *
 * @since 2201.9.0
 */
public class CommonUtils {

    /**
     * Get the current time in milliseconds.
     *
     * @return The current time in milliseconds
     */
    public static BDecimal currentTimeInMillis() {
        return BDecimal.valueOf(System.currentTimeMillis());
    }

    /**
     * Check whether the parameters of a function are concurrency safe.
     *
     * @param func The function pointer
     * @return Whether the parameters are concurrency safe
     */
    public static Object isFunctionParamConcurrencySafe(BFunctionPointer func) {
        FunctionType functionType = (FunctionType) func.getType();
        Parameter[] functionParameters = functionType.getParameters();
        for (Parameter functionParameter : functionParameters) {
            Type parameterType = functionParameter.type;
            if (!isSubTypeOfReadOnly(parameterType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSubTypeOfReadOnly(Type type) {
        if (type.isReadOnly()) {
            return true;
        }

        if (!(type instanceof UnionType unionType)) {
            return false;
        }

        for (Type memberType : unionType.getMemberTypes()) {
            if (!isSubTypeOfReadOnly(memberType)) {
                return false;
            }
        }
        return true;
    }
}
