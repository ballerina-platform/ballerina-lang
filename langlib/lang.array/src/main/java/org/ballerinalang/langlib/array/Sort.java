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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.values.utils.ArrayUtils.checkIsArrayOnlyOperation;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:sort((any|error)[], function).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", version = ARRAY_VERSION, functionName = "sort",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "func", type = TypeKind.FUNCTION)},
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class Sort {

    public static ArrayValue sort(Strand strand, ArrayValue arr, FPValue<Object, Long> func) {
        checkIsArrayOnlyOperation(arr.getType(), "sort()");
        ArrayValue aux = new ArrayValueImpl((BArrayType) arr.getType());
        mergesort(arr, aux, 0, arr.size() - 1, strand, func);
        return arr;
    }

    // Adapted from https://algs4.cs.princeton.edu/22mergesort/Merge.java.html
    private static void mergesort(ArrayValue input, ArrayValue aux, int lo, int hi, Strand strand,
                                  FPValue<Object, Long> comparator) {
        if (hi <= lo) {
            return;
        }

        int mid = lo + (hi - lo) / 2;

        mergesort(input, aux, lo, mid, strand, comparator);
        mergesort(input, aux, mid + 1, hi, strand, comparator);

        merge(input, aux, lo, mid, hi, strand, comparator);
    }

    private static void merge(ArrayValue input, ArrayValue aux, int lo, int mid, int hi, Strand strand,
                              FPValue<Object, Long> comparator) {
        int elemTypeTag = input.getElementType().getTag();

        for (int i = lo; i <= hi; i++) {
            aux.add(i, input.get(i));
        }

        for (int i = lo, j = mid + 1, k = lo; k <= hi; k++) {
            if (i > mid) {
                input.add(k, aux.get(j++));
            } else if (j > hi) {
                input.add(k, aux.get(i++));
            } else if (comparator.call(new Object[]{strand, aux.get(j), true, aux.get(i), true}) < 0) {
                input.add(k, aux.get(j++));
            } else {
                input.add(k, aux.get(i++));
            }
        }
    }

}
