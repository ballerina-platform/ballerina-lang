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

import io.ballerina.jvm.runtime.AsyncUtils;
import io.ballerina.jvm.scheduling.Scheduler;
import io.ballerina.jvm.scheduling.Strand;
import io.ballerina.jvm.scheduling.StrandMetadata;
import io.ballerina.jvm.types.BArrayType;
import io.ballerina.jvm.values.ArrayValue;
import io.ballerina.jvm.values.ArrayValueImpl;
import io.ballerina.jvm.values.FPValue;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.jvm.util.BLangConstants.ARRAY_LANG_LIB;
import static io.ballerina.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:filter(Type[], function).
 *
 * @since 1.0
 */
public class Filter {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, ARRAY_LANG_LIB,
                                                                      ARRAY_VERSION, "filter");

    public static ArrayValue filter(ArrayValue arr, FPValue<Object, Boolean> func) {
        ArrayValue newArr = new ArrayValueImpl((BArrayType) arr.getType());
        int size = arr.size();
        AtomicInteger newArraySize = new AtomicInteger(-1);
        AtomicInteger index = new AtomicInteger(-1);
        Strand parentStrand = Scheduler.getStrand();
        AsyncUtils.invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                                                         () -> new Object[]{parentStrand,
                                                                 arr.get(index.incrementAndGet()),
                                                                 true},
                                                         result -> {
                                                             if ((Boolean) result) {
                                                                 newArr.add(newArraySize.incrementAndGet(),
                                                                            arr.get(index.get()));
                                                             }
                                                         }, () -> newArr, Scheduler.getStrand().scheduler);
        return newArr;
    }
}
