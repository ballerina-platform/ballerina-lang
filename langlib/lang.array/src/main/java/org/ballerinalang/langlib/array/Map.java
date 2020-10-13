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

import io.ballerina.jvm.api.BValueCreator;
import io.ballerina.jvm.api.TypeTags;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.api.values.BArray;
import io.ballerina.jvm.api.values.BFunctionPointer;
import io.ballerina.jvm.runtime.AsyncUtils;
import io.ballerina.jvm.scheduling.Scheduler;
import io.ballerina.jvm.scheduling.Strand;
import io.ballerina.jvm.scheduling.StrandMetadata;
import io.ballerina.jvm.types.BArrayType;
import io.ballerina.jvm.types.BFunctionType;
import org.ballerinalang.langlib.array.utils.GetFunction;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.jvm.util.BLangConstants.ARRAY_LANG_LIB;
import static io.ballerina.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.langlib.array.utils.ArrayUtils.createOpNotSupportedError;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:map(Type[]).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.array", functionName = "map",
//        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "func", type = TypeKind.FUNCTION)},
//        returnType = {@ReturnType(type = TypeKind.ARRAY)},
//        isPublic = true
//)
public class Map {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, ARRAY_LANG_LIB,
                                                                      ARRAY_VERSION, "map");

    public static BArray map(BArray arr, BFunctionPointer<Object, Object> func) {
        Type elemType = ((BFunctionType) func.getType()).retType;
        Type retArrType = new BArrayType(elemType);
        BArray retArr = BValueCreator.createArrayValue((BArrayType) retArrType);
        int size = arr.size();
        GetFunction getFn;

        Type arrType = arr.getType();
        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                getFn = BArray::get;
                break;
            case TypeTags.TUPLE_TAG:
                getFn = BArray::getRefValue;
                break;
            default:
                throw createOpNotSupportedError(arrType, "map()");
        }
        AtomicInteger index = new AtomicInteger(-1);
        Strand parentStrand = Scheduler.getStrand();
        AsyncUtils
                .invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                                                       () -> new Object[]{parentStrand,
                                                               getFn.get(arr, index.incrementAndGet()), true},
                                                       result -> retArr.add(index.get(), result),
                                                       () -> retArr, Scheduler.getStrand().scheduler);

        return retArr;
    }
}
