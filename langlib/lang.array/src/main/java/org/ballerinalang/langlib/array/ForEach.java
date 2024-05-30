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

import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import org.ballerinalang.langlib.array.utils.GetFunction;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.langlib.array.utils.ArrayUtils.getElementAccessFunction;
import static org.ballerinalang.langlib.array.utils.Constants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:forEach(Type[]).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.array", functionName = "forEach",
//        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "func", type = TypeKind.FUNCTION)},
//        isPublic = true
//)
public class ForEach {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, ARRAY_LANG_LIB,
                                                                      ARRAY_VERSION, "forEach");

    public static void forEach(BArray arr, BFunctionPointer<Object, Object> func) {
        int size = arr.size();
        Type arrType = arr.getType();
        GetFunction getFn = getElementAccessFunction(arrType, "forEach()");
        AtomicInteger index = new AtomicInteger(-1);
        AsyncUtils.invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                () -> new Object[]{getFn.get(arr, index.incrementAndGet()), true}, result -> {
                }, () -> null, Scheduler.getStrand().scheduler);
    }
}
