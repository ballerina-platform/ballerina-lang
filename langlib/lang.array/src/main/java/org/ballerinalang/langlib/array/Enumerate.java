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

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Arrays;

/**
 * Native implementation of lang.array:enumerate(Type[]).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", functionName = "enumerate",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.TUPLE)},
        isPublic = true
)
public class Enumerate {

    public static ArrayValue enumerate(Strand strand, ArrayValue arr) {
        BType elemType = new BTupleType(Arrays.asList(BTypes.typeInt, arr.elementType));
        BType arrType = new BArrayType(elemType);
        ArrayValue newArr = new ArrayValue(arrType);
        int size = arr.size();

        for (int i = 0; i < size; i++) {
            ArrayValue entry = new ArrayValue(elemType);
            entry.add(0, i);
            entry.add(1, arr.get(i));
            newArr.add(i, entry);
        }

        return newArr;
    }
}
