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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.values.utils.ArrayUtils.add;
import static org.ballerinalang.jvm.values.utils.ArrayUtils.checkIsArrayOnlyOperation;

/**
 * Native implementation of lang.array:sort((any|error)[], function).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", functionName = "sort",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "func", type = TypeKind.FUNCTION)},
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class Sort {

    public static ArrayValue sort(Strand strand, ArrayValue arr, FPValue<Object, Long> func) {
        checkIsArrayOnlyOperation(arr.getType(), "sort()");
        return mergesort(arr, strand, func);
    }

    // Adapted from https://algs4.cs.princeton.edu/14analysis/Mergesort.java.html
    private static ArrayValue mergesort(ArrayValue input, Strand strand, FPValue<Object, Long> comparator) {
        int n = input.size();

        if (n <= 1) {
            return input;
        }

        ArrayValue leftArr = new ArrayValue(input.getType());
        ArrayValue rightArr = new ArrayValue(input.getType());
        int leftArrSize = n / 2;
        int rightArrSize = n - n / 2;
        int elemTypeTag = input.elementType.getTag();

        for (int i = 0; i < leftArrSize; i++) {
            add(leftArr, elemTypeTag, i, input.get(i));
        }

        for (int i = 0; i < rightArrSize; i++) {
            add(rightArr, elemTypeTag, i, input.get(i + n / 2));
        }

        return merge(mergesort(leftArr, strand, comparator),
                     mergesort(rightArr, strand, comparator), strand, comparator);
    }

    private static ArrayValue merge(ArrayValue leftArr, ArrayValue rightArr, Strand strand,
                                    FPValue<Object, Long> comparator) {
        ArrayValue mergedArr = new ArrayValue(leftArr.getType());
        int leftArrSize = leftArr.size();
        int rightArrSize = rightArr.size();
        int mergedArrSize = leftArrSize + rightArrSize;
        int elemTypeTag = mergedArr.elementType.getTag();

        for (int i = 0, j = 0, k = 0; k < mergedArrSize; k++) {
            if (i >= leftArrSize) {
                add(mergedArr, elemTypeTag, k, rightArr.get(j++));
            } else if (j >= rightArrSize) {
                add(mergedArr, elemTypeTag, k, leftArr.get(i++));
            } else if (comparator.apply(new Object[]{strand, leftArr.get(i), true, rightArr.get(j), true}) <= 0) {
                add(mergedArr, elemTypeTag, k, leftArr.get(i++));
            } else {
                add(mergedArr, elemTypeTag, k, rightArr.get(j++));
            }
        }

        return mergedArr;
    }
}
