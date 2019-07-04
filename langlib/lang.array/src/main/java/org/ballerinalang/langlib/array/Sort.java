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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

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

    public static ArrayValue sort(Strand strand, ArrayValue arr, FPValue<Object, Integer> func) {
        ArrayValue dest = new ArrayValue(arr.getType());
        mergeSort(arr, dest, 0, arr.size(), strand, func);
        return arr;
    }

    private static void mergeSort(ArrayValue arr, ArrayValue dest, int left, int right, Strand strand,
                                  FPValue<Object, Integer> func) {
        if (left < right) {
            int mid = left + (right - 1) / 2;

            mergeSort(arr, dest, left, mid, strand, func);
            mergeSort(arr, dest, mid + 1, right, strand, func);

            merge(arr, dest, left, mid, right, strand, func);
        }
    }

    private static void merge(ArrayValue arr, ArrayValue dest, int left, int mid, int right, Strand strand,
                              FPValue<Object, Integer> func) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        ArrayValue leftHalf = new ArrayValue(arr.getType());
        ArrayValue rightHalf = new ArrayValue(arr.getType());

        for (int i = 0; i < n1; i++) {
            leftHalf.add(i, arr.get(left + i));
        }

        for (int i = 0; i < n2; i++) {
            rightHalf.add(i, arr.get(mid + 1 + i));
        }

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            Object lVal = leftHalf.get(i);
            Object rVal = rightHalf.get(j);

            if (func.apply(new Object[]{strand, lVal, rVal}) <= 0) {
                dest.add(k, lVal);
                i++;
            } else {
                dest.add(k, rVal);
                j++;
            }

            k++;
        }

        while (i < n1) {
            dest.add(k++, leftHalf.get(i++));
        }

        while (j < n2) {
            dest.add(k++, rightHalf.get(j++));
        }
    }
}
