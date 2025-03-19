/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.function;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.types.BTupleType;

/**
 * Native implementation of lang.function:call(function func, any|error... args).
 *
 * @since 2201.2.0
 */
public final class Call {

    private Call() {
    }

    public static Object call(Environment env, BFunctionPointer func, Object... args) {
        return func.call(env.getRuntime(), args);
    }

    private static String removeBracketsFromStringFormatOfTuple(BTupleType tupleType) {
        String stringValue = tupleType.toString();
        return "(" + stringValue.substring(1, stringValue.length() - 1) + ")";
    }
}
