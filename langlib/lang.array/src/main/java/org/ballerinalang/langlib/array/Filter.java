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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import org.ballerinalang.langlib.array.utils.ArrayUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.langlib.array.utils.ArrayUtils.createOpNotSupportedError;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:filter(Type[], function).
 *
 * @since 1.0
 */
public class Filter {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, ARRAY_LANG_LIB,
                                                                      ARRAY_VERSION, "filter");

    public static BArray filter(BArray arr, BFunctionPointer<Object, Boolean> func) {
        BArray newArr;
        Type arrType = TypeUtils.getReferredType(arr.getType());
        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                newArr = ValueCreator.createArrayValue(TypeCreator.createArrayType(arr.getElementType()));
                break;
            case TypeTags.TUPLE_TAG:
                newArr = ArrayUtils.createEmptyArrayFromTuple(arr);
                break;
            default:
                throw createOpNotSupportedError(arrType, "filter()");

        }
        int size = arr.size();
        AtomicInteger newArraySize = new AtomicInteger(-1);
        AtomicInteger index = new AtomicInteger(-1);
        AsyncUtils.invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                () -> new Object[]{arr.get(index.incrementAndGet()), true},
                result -> {
                    if ((boolean) result) {
                        newArr.add(newArraySize.incrementAndGet(),
                                arr.get(index.get()));
                    }
                }, () -> newArr, Scheduler.getStrand().scheduler);
        return newArr;
    }

    private Filter() {
    }
}
