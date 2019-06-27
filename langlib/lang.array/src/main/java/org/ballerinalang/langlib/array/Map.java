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
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native implementation of lang.array:map(Type[]).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", functionName = "map",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "func", type = TypeKind.FUNCTION)},
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class Map {

    public static ArrayValue map(Strand strand, ArrayValue arr, FPValue<Object, Object> func) {
        BType elemType = ((BFunctionType) func.getType()).retType;
        BType arrType = new BArrayType(elemType);
        ArrayValue newArr = new ArrayValue(arrType);
        Object[] args = new Object[]{strand, null};
        int size = arr.size();

        for (int i = 0; i < size; i++) {
            args[1] = arr.get(i);
            Object newVal = func.apply(args);
            newArr.add(i, newVal);
        }

        return newArr;
    }
}
