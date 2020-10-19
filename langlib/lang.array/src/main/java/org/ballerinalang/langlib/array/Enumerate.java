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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.langlib.array.utils.GetFunction;

import java.util.Arrays;

import static org.ballerinalang.langlib.array.utils.ArrayUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.array:enumerate(Type[]).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.array", functionName = "enumerate",
//        args = {@Argument(name = "arr", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.TUPLE)},
//        isPublic = true
//)
public class Enumerate {

    public static BArray enumerate(BArray arr) {
        Type arrType = arr.getType();
        int size = arr.size();
        TupleType elemType;
        GetFunction getFn;

        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                elemType = TypeCreator.createTupleType(Arrays.asList(PredefinedTypes.TYPE_INT, arr.getElementType()));
                getFn = BArray::get;
                break;
            case TypeTags.TUPLE_TAG:
                TupleType tupleType = (TupleType) arrType;
                UnionType tupElemType = TypeCreator.createUnionType(tupleType.getTupleTypes(),
                                                                    tupleType.getTypeFlags());
                elemType = TypeCreator.createTupleType(Arrays.asList(PredefinedTypes.TYPE_INT, tupElemType));
                getFn = BArray::getRefValue;
                break;
            default:
                throw createOpNotSupportedError(arrType, "enumerate()");
        }

        ArrayType newArrType = TypeCreator.createArrayType(elemType);
        BArray newArr = ValueCreator.createArrayValue(newArrType); // TODO: 7/8/19 Verify whether this needs to be
        // sealed

        for (int i = 0; i < size; i++) {
            BArray entry = ValueCreator.createTupleValue(elemType);
            entry.add(0, Long.valueOf(i));
            entry.add(1, getFn.get(arr, i));
            newArr.add(i, entry);
        }

        return newArr;
    }
}
