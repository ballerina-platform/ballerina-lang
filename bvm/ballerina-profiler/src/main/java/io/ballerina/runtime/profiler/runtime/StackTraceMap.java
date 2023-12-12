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

import io.ballerina.identifier.Utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A map that keeps profile stack the index values against the stack-trace.
 *
 * @since 2201.8.0
 */
public class StackTraceMap {

    private static final AtomicInteger localVarIndex = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, Integer> stackTraceIndexMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> indexStackTraceMap = new ConcurrentHashMap<>();

    private StackTraceMap() {
    }

    static String getStackIndex(String stackElement) {
        Integer stackIndex = stackTraceIndexMap.get(stackElement);
        if (stackIndex != null) {
            return String.valueOf(stackIndex);
        }
        int index = localVarIndex.getAndIncrement();
        stackTraceIndexMap.put(stackElement, index);
        String indexStr = String.valueOf(index);
        indexStackTraceMap.put(indexStr, stackElement);
        return indexStr;
    }

    static String getCallStackString(String stackKey) {
        String[] stackElements = stackKey.split("\\$");
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < (stackElements.length - 1); i++) {
            sb.append("\"").append(decodeStackElement(indexStackTraceMap.get(stackElements[i]))).append("\",");
        }
        sb.append("\"").append(decodeStackElement(indexStackTraceMap.get(stackElements[stackElements.length - 1])))
                .append("\"]");
        return sb.toString();
    }

    private static String decodeStackElement(String stackElement) {
        return Utils.decodeIdentifier(stackElement.replaceAll("\\$value\\$", ""));
    }
}
