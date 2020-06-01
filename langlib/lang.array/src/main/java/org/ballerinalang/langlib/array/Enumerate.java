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
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.jvm.values.utils.GetFunction;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Arrays;

import static org.ballerinalang.jvm.values.utils.ArrayUtils.createOpNotSupportedError;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:enumerate(Type[]).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", version = ARRAY_VERSION, functionName = "enumerate",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.TUPLE)},
        isPublic = true
)
public class Enumerate {

    public static ArrayValue enumerate(Strand strand, ArrayValue arr) {
        BType arrType = arr.getType();
        int size = arr.size();
        BTupleType elemType;
        GetFunction getFn;

        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                elemType = new BTupleType(Arrays.asList(BTypes.typeInt, arr.getElementType()));
                getFn = ArrayValue::get;
                break;
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) arrType;
                BUnionType tupElemType = new BUnionType(tupleType.getTupleTypes(), tupleType.getTypeFlags());
                elemType = new BTupleType(Arrays.asList(BTypes.typeInt, tupElemType));
                getFn = ArrayValue::getRefValue;
                break;
            default:
                throw createOpNotSupportedError(arrType, "enumerate()");
        }

        BArrayType newArrType = new BArrayType(elemType);
        ArrayValue newArr = new ArrayValueImpl(newArrType); // TODO: 7/8/19 Verify whether this needs to be sealed

        for (int i = 0; i < size; i++) {
            TupleValueImpl entry = new TupleValueImpl(elemType);
            entry.add(0, Long.valueOf(i));
            entry.add(1, getFn.get(arr, i));
            newArr.add(i, entry);
        }

        return newArr;
    }
}
