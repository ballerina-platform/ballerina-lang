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

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.utils.GetFunction;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.jvm.values.utils.ArrayUtils.createOpNotSupportedError;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:map(Type[]).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", version = ARRAY_VERSION, functionName = "map",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "func", type = TypeKind.FUNCTION)},
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class Map {

    public static ArrayValue map(Strand strand, ArrayValue arr, FPValue<Object, Object> func) {
        BType elemType = ((BFunctionType) func.getType()).retType;
        BType retArrType = new BArrayType(elemType);
        ArrayValue retArr = new ArrayValueImpl((BArrayType) retArrType);
        int size = arr.size();
        GetFunction getFn;

        BType arrType = arr.getType();
        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                getFn = ArrayValue::get;
                break;
            case TypeTags.TUPLE_TAG:
                getFn = ArrayValue::getRefValue;
                break;
            default:
                throw createOpNotSupportedError(arrType, "map()");
        }
        AtomicInteger index = new AtomicInteger(-1);
        BRuntime.getCurrentRuntime()
                .invokeFunctionPointerAsyncIteratively(func, size,
                                                       () -> new Object[]{strand,
                                                               getFn.get(arr, index.incrementAndGet()), true},
                                                       result -> retArr.add(index.get(), result),
                                                       () -> retArr);

        return retArr;
    }
}
