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

import io.ballerina.jvm.TypeChecker;
import io.ballerina.jvm.api.BErrorCreator;
import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.TypeTags;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.scheduling.Scheduler;
import io.ballerina.jvm.types.BArrayType;
import io.ballerina.jvm.types.BFunctionType;
import io.ballerina.jvm.types.BUnionType;
import io.ballerina.jvm.values.ArrayValue;
import io.ballerina.jvm.values.FPValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.PrimitiveIterator;

import static io.ballerina.jvm.util.BLangConstants.ARRAY_LANG_LIB;
import static io.ballerina.jvm.util.exceptions.BallerinaErrorReasons.INVALID_TYPE_TO_SORT;
import static io.ballerina.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static io.ballerina.jvm.values.utils.ArrayUtils.checkIsArrayOnlyOperation;

/**
 * Native implementation of lang.array:sort((any|error)[], direction, function).
 *
 * @since 1.0
 */
public class Sort {

    public static ArrayValue sort(ArrayValue arr, Object direction, Object func) {
        checkIsArrayOnlyOperation(arr.getType(), "sort()");
        FPValue<Object, Object> function = (FPValue<Object, Object>) func;
        Type elemType = ((BArrayType) arr.getType()).getElementType();
        boolean isAscending = true;
        if (direction.toString().equals("descending")) {
            isAscending = false;
        }

        Object[][] sortArr = new Object[arr.size()][2];
        Object[][] sortArrClone = new Object[arr.size()][2];
        if (function != null) {
            boolean elementTypeIdentified = false;
            elemType = ((BFunctionType) function.getType()).retType;
            for (int i = 0; i < arr.size(); i++) {
                sortArr[i][0] = function.call(new Object[]{Scheduler.getStrand(), arr.get(i), true});
                // Get the type of the sortArr elements when there is an arrow expression as the key function
                if (!elementTypeIdentified && elemType.getTag() == TypeTags.UNION_TAG &&
                        ((BUnionType) elemType).getMemberTypes().size() > 2) {
                    Type sortArrElemType = TypeChecker.getType(sortArr[i][0]);
                    if (sortArrElemType.getTag() != TypeTags.NULL_TAG) {
                        elemType = sortArrElemType;
                        elementTypeIdentified = true;
                    }
                }
                sortArr[i][1] = arr.get(i);
            }
        } else {
            for (int i = 0; i < arr.size(); i++) {
                sortArr[i][0] = sortArr[i][1] = arr.get(i);
            }
        }

        if (elemType.getTag() == TypeTags.UNION_TAG) {
            elemType = getMemberType((BUnionType) elemType);
        }
        if (elemType.getTag() == TypeTags.ARRAY_TAG) {
            Type type = ((BArrayType) elemType).getElementType();
            if (type.getTag() == TypeTags.UNION_TAG) {
                Type memberType = getMemberType((BUnionType) type);
                elemType = new BArrayType(memberType);
            }
        }

        mergesort(sortArr, sortArrClone, 0, sortArr.length - 1, isAscending, elemType);

        for (int k = 0; k < sortArr.length; k++) {
            arr.add(k, sortArr[k][1]);
        }

        return arr;
    }

    private static Type getMemberType(BUnionType unionType) {
        List<Type> memberTypes = unionType.getMemberTypes();
        for (Type type : memberTypes) {
            if (type.getTag() != TypeTags.NULL_TAG) {
                return type;
            }
        }
        return unionType;
    }

    // Adapted from https://algs4.cs.princeton.edu/22mergesort/Merge.java.html
    private static void mergesort(Object[][] input, Object[][] aux, int lo, int hi, boolean isAscending, Type type) {
        if (hi <= lo) {
            return;
        }

        int mid = lo + (hi - lo) / 2;

        mergesort(input, aux, lo, mid, isAscending, type);
        mergesort(input, aux, mid + 1, hi, isAscending, type);

        merge(input, aux, lo, mid, hi, isAscending, type);
    }

    private static void merge(Object[][] input, Object[][] aux, int lo, int mid, int hi, boolean isAscending,
                              Type type) {
        if (hi + 1 - lo >= 0) {
            System.arraycopy(input, lo, aux, lo, hi + 1 - lo);
        }

        for (int i = lo, j = mid + 1, k = lo; k <= hi; k++) {
            int index;
            if (i > mid) {
                index = j++;
            } else if (j > hi) {
                index = i++;
            } else if (isAscending && sortFunc(aux[j][0], aux[i][0], type, true) < 0) {
                index = j++;
            } else if (!isAscending && sortFunc(aux[i][0], aux[j][0], type, false) < 0) {
                index = j++;
            } else {
                index = i++;
            }
            input[k] = aux[index];
        }
    }

    private static int sortFunc(Object value1, Object value2, Type type, boolean isAscending) {
        // () should come last irrespective of the sort direction.
        if (value1 == null) {
            if (value2 == null) {
                return 0;
            }
            if (isAscending) {
                return 1;
            }
            return -1;
        }
        if (value2 == null) {
            if (isAscending) {
                return -1;
            }
            return 1;
        }
        if (TypeTags.isIntegerTypeTag(type.getTag())) {
            return Long.compare((long) value1, (long) value2);
        } else if (type.getTag() == TypeTags.FLOAT_TAG) {
            // NaN should be placed last or one before the last when () is present irrespective of the sort direction.
            if (Double.isNaN((double) value1)) {
                if (Double.isNaN((double) value2)) {
                    return 0;
                }
                if (isAscending) {
                    return 1;
                }
                return -1;
            }
            if (Double.isNaN((double) value2)) {
                if (isAscending) {
                    return -1;
                }
                return 1;
            }
            // -0.0 = +0.0
            if ((double) value1 == 0 && (double) value2 == 0) {
                return 0;
            }
            return Double.compare((double) value1, (double) value2);
        } else if (type.getTag() == TypeTags.DECIMAL_TAG) {
            return new BigDecimal(value1.toString()).compareTo(new BigDecimal(value2.toString()));
        } else if (type.getTag() == TypeTags.BOOLEAN_TAG) {
            return Boolean.compare((boolean) value1, (boolean) value2);
        } else if (TypeTags.isStringTypeTag(type.getTag())) {
            return codePointCompare(value1.toString(), value2.toString());
        } else if (type.getTag() == TypeTags.BYTE_TAG) {
            return Integer.compare((int) value1, (int) value2);
        } else if (type.getTag() == TypeTags.ARRAY_TAG) {
            int lengthVal1 = ((ArrayValue) value1).size();
            int lengthVal2 = ((ArrayValue) value2).size();
            if (lengthVal1 == 0) {
                if (lengthVal2 == 0) {
                    return 0;
                }
                return -1;
            }
            if (lengthVal2 == 0) {
                return 1;
            }
            int len = Math.min(lengthVal1, lengthVal2);
            int c = 0;
            for (int i = 0; i < len; i++) {
                c = sortFunc(((ArrayValue) value1).get(i), ((ArrayValue) value2).get(i),
                        ((BArrayType) type).getElementType(), isAscending);
                if (c != 0) {
                    break;
                } else {
                    if (i == len - 1 && lengthVal1 < lengthVal2) {
                        return -1;
                    }
                }
            }
            return c;
        }
        throw BErrorCreator.createError(getModulePrefixedReason(ARRAY_LANG_LIB, INVALID_TYPE_TO_SORT),
                                        BStringUtils.fromString("expected an ordered type, but found '" +
                                                                       type.toString() + "'"));
    }

    private static int codePointCompare(String str1, String str2) {
        PrimitiveIterator.OfInt iterator1 = str1.codePoints().iterator();
        PrimitiveIterator.OfInt iterator2 = str2.codePoints().iterator();
        while (iterator1.hasNext()) {
            if (!iterator2.hasNext()) {
                return 1;
            }
            Integer codePoint1 = iterator1.next();
            Integer codePoint2 = iterator2.next();

            int cmp = codePoint1.compareTo(codePoint2);
            if (cmp != 0) {
                return cmp;
            }
        }
        if (iterator2.hasNext()) {
            return -1;
        }
        return 0;
    }
}
