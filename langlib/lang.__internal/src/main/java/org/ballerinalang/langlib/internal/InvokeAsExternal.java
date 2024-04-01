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

import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BFunctionPointer;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Native implementation of lang.internal:invokeAsExternal(func, args).
 *
 * @since 2.0.0
 */
public class InvokeAsExternal {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, "lang.internal",
            "0.1.0", "invokeAsExternal");

    public static Object invokeAsExternal(Object func, Object[] args) {

        BFunctionPointer function = (BFunctionPointer) func;
        List<Object> argList = new ArrayList<>();
        for (Object arg : args) {
            argList.add(arg);
            argList.add(true);
        }
        return function.asyncCall(argList.toArray(), o -> o, METADATA);
    }
}
