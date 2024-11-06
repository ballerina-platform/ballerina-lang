/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.array;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import org.ballerinalang.langlib.array.utils.GetFunction;

import static org.ballerinalang.langlib.array.utils.ArrayUtils.getElementAccessFunction;

/**
 * Native implementation of lang.array:forEach(Type[]).
 *
 * @since 1.0
 */
public class ForEach {

    public static void forEach(Environment env, BArray arr, BFunctionPointer func) {
        Type arrType = arr.getType();
        GetFunction getFn = getElementAccessFunction(arrType, "forEach()");
        int size = arr.size();
        for (int i = 0; i < size; i++) {
            func.call(env.getRuntime(), getFn.get(arr, i));
        }
    }
}
