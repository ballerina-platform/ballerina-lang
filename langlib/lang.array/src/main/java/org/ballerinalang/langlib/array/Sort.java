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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.ValueComparisonUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INVALID_TYPE_TO_SORT;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.langlib.array.utils.ArrayUtils.checkIsArrayOnlyOperation;

/**
 * Native implementation of lang.array:sort((any|error)[], direction, function).
 *
 * @since 1.0
 */
public class Sort {

    public static BArray sort(BArray arr, Object direction, Object func) {
        checkIsArrayOnlyOperation(TypeUtils.getReferredType(arr.getType()), "sort()");
        BFunctionPointer<Object, Object> function = (BFunctionPointer<Object, Object>) func;

        Object[][] sortArr = new Object[arr.size()][2];
        Object[][] sortArrClone = new Object[arr.size()][2];

        if (function != null) {
            for (int i = 0; i < arr.size(); i++) {
                sortArr[i][0] = function.call(new Object[]{Scheduler.getStrand(), arr.get(i), true});
                sortArr[i][1] = arr.get(i);
            }
        } else {
            for (int i = 0; i < arr.size(); i++) {
                sortArr[i][0] = sortArr[i][1] = arr.get(i);
            }
        }

        mergesort(sortArr, sortArrClone, 0, sortArr.length - 1, direction.toString());

        BArray sortedArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(arr.getElementType()));

        for (int k = 0; k < sortArr.length; k++) {
            sortedArray.add(k, sortArr[k][1]);
        }

        return sortedArray;
    }

    // Adapted from https://algs4.cs.princeton.edu/22mergesort/Merge.java.html
    private static void mergesort(Object[][] input, Object[][] aux, int lo, int hi, String direction) {
        if (hi <= lo) {
            return;
        }

        int mid = lo + (hi - lo) / 2;

        mergesort(input, aux, lo, mid, direction);
        mergesort(input, aux, mid + 1, hi, direction);

        merge(input, aux, lo, mid, hi, direction);
    }

    private static void merge(Object[][] input, Object[][] aux, int lo, int mid, int hi, String direction) {
        if (hi + 1 - lo >= 0) {
            System.arraycopy(input, lo, aux, lo, hi + 1 - lo);
        }

        for (int i = lo, j = mid + 1, k = lo; k <= hi; k++) {
            int index;
            try {
                if (i > mid) {
                    index = j++;
                } else if (j > hi) {
                    index = i++;
                } else if (direction.equals("ascending") && ValueComparisonUtils.compareValues(aux[j][0],
                        aux[i][0], direction) < 0) {
                    index = j++;
                } else if (direction.equals("descending") && ValueComparisonUtils.compareValues(aux[i][0],
                        aux[j][0], direction) < 0) {
                    index = j++;
                } else {
                    index = i++;
                }
                input[k] = aux[index];

            } catch (BError error) {
                throw ErrorCreator.createError(getModulePrefixedReason(ARRAY_LANG_LIB, INVALID_TYPE_TO_SORT),
                        (BMap) error.getDetails());
            }
        }
    }
}
