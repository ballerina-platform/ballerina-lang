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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.utils.GetFunction;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.values.utils.ArrayUtils.add;
import static org.ballerinalang.jvm.values.utils.ArrayUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.array:slice((any|error)[]).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", functionName = "slice",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "startIndex", type = TypeKind.INT),
                @Argument(name = "endIndex", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class Slice {

    public static ArrayValue slice(Strand strand, ArrayValue arr, long startIndex, long endIndex) {
        int size = arr.size();

        if (startIndex < 0) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, startIndex, size);
        }

        if (endIndex > size) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, endIndex, size);
        }

        long sliceSize = endIndex - startIndex;
        if (sliceSize < 0) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, sliceSize, size);
        }

        BType arrType = arr.getType();
        ArrayValue slicedArr;
        int elemTypeTag;
        GetFunction getFn;

        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                slicedArr = new ArrayValueImpl((BArrayType) arrType);
                elemTypeTag = ((BArrayType) arrType).getElementType().getTag();
                getFn = ArrayValue::get;
                break;
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) arrType;
                BUnionType unionType = new BUnionType(tupleType.getTupleTypes(), tupleType.getTypeFlags());
                BArrayType slicedArrType = new BArrayType(unionType, (int) (endIndex - startIndex));
                slicedArr = new ArrayValueImpl(slicedArrType);
                elemTypeTag = -1; // To ensure additions go to ref value array in ArrayValue
                getFn = ArrayValue::getRefValue;
                break;
            default:
                throw createOpNotSupportedError(arrType, "slice()");
        }

        for (long i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(slicedArr, elemTypeTag, j, getFn.get(arr, i));
        }

        return slicedArr;
    }
}
