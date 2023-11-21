/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.profiler.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A map that keeps profile stack the index values against the stack-trace.
 *
 * @since 2201.8.0
 */
public class StackTraceMap {

    private static int localVarIndex = 0;

    private StackTraceMap() {
    }

    private static final Map<String, Integer> stackTraceIndexMap = new HashMap<>();

    static String getStackKey(ArrayList<String> stacks) {
         StringBuilder keyBuilder = new StringBuilder();
         for (String stack : stacks) {
             if (stackTraceIndexMap.containsKey(stack)) {
                 keyBuilder.append(stackTraceIndexMap.get(stack));
             } else {
                 stackTraceIndexMap.put(stack, localVarIndex);
                 keyBuilder.append(localVarIndex++);
             }
         }
        return keyBuilder.toString();
    }
}
