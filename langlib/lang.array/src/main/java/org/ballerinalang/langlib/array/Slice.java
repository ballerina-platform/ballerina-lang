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

import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.langlib.array.utils.ArrayUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.array:slice((any|error)[]).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.array", functionName = "slice",
//        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "startIndex", type = TypeKind.INT),
//                @Argument(name = "endIndex", type = TypeKind.INT)},
//        returnType = {@ReturnType(type = TypeKind.ARRAY)},
//        isPublic = true
//)
public class Slice {

    public static BArray slice(BArray arr, long startIndex, long endIndex) {
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

        Type arrType = arr.getType();
        BArray slicedArr;

        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                slicedArr = ((BArray) arr).slice(startIndex, endIndex);
                break;
            case TypeTags.TUPLE_TAG:
                TupleType tupleType = (TupleType) arrType;

                List<Type> memTypes = new ArrayList<>(tupleType.getTupleTypes());

                Type restType = tupleType.getRestType();
                if (restType != null) {
                    memTypes.add(restType);
                }

                UnionType unionType = TypeCreator.createUnionType(memTypes);
                ArrayType slicedArrType = TypeCreator.createArrayType(unionType, (int) (endIndex - startIndex));
                slicedArr = ValueCreator.createArrayValue(slicedArrType);

                for (long i = startIndex, j = 0; i < endIndex; i++, j++) {
                    slicedArr.add(j, arr.getRefValue(i));
                }
                break;
            default:
                throw createOpNotSupportedError(arrType, "slice()");
        }

        return slicedArr;
    }
}
