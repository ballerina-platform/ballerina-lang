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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.math.BigDecimal;
import java.util.List;
import java.util.PrimitiveIterator;

import static org.ballerinalang.jvm.util.BLangConstants.ARRAY_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INVALID_TYPE_TO_SORT;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.jvm.values.utils.ArrayUtils.checkIsArrayOnlyOperation;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:sort((any|error)[], direction, function).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", version = ARRAY_VERSION, functionName = "sort",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "direction", type = TypeKind.OBJECT),
                @Argument(name = "func", type = TypeKind.OBJECT)},
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class Sort {

    public static ArrayValue sort(Strand strand, ArrayValue arr, Object direction, Object func) {
        checkIsArrayOnlyOperation(arr.getType(), "sort()");
        FPValue<Object, Object> function = (FPValue<Object, Object>) func;
        BType elemType = ((BArrayType) arr.getType()).getElementType();
        boolean isAscending = true;
        if (direction.toString().equals("descending")) {
            isAscending = false;
        }

        Object[][] sortArr = new Object[arr.size()][2];
        Object[][] sortArrClone = new Object[arr.size()][2];
        if (function != null) {
            elemType = ((BFunctionType) function.getType()).retType;
            for (int i = 0; i < arr.size(); i++) {
                sortArr[i][0] = function.call(new Object[]{strand, arr.get(i), true});
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
            BType type = ((BArrayType) elemType).getElementType();
            if (type.getTag() == TypeTags.UNION_TAG) {
                BType memberType = getMemberType((BUnionType) type);
                elemType = new BArrayType(memberType);
            }
        }

        mergesort(sortArr, sortArrClone, 0, sortArr.length - 1, isAscending, elemType);

        for (int k = 0; k < sortArr.length; k++) {
            arr.add(k, sortArr[k][1]);
        }

        return arr;
    }

    private static BType getMemberType(BUnionType unionType) {
        List<BType> memberTypes = unionType.getMemberTypes();
        for (BType type : memberTypes) {
            if (type.getTag() != TypeTags.NULL_TAG) {
                return type;
            }
        }
        return unionType;
    }

    // Adapted from https://algs4.cs.princeton.edu/22mergesort/Merge.java.html
    private static void mergesort(Object[][] input, Object[][] aux, int lo, int hi, boolean isAscending, BType type) {
        if (hi <= lo) {
            return;
        }

        int mid = lo + (hi - lo) / 2;

        mergesort(input, aux, lo, mid, isAscending, type);
        mergesort(input, aux, mid + 1, hi, isAscending, type);

        merge(input, aux, lo, mid, hi, isAscending, type);
    }

    private static void merge(Object[][] input, Object[][] aux, int lo, int mid, int hi, boolean isAscending,
                              BType type) {
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

    private static int sortFunc(Object value1, Object value2, BType type, boolean isAscending) {
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
        throw BallerinaErrors.createError(getModulePrefixedReason(ARRAY_LANG_LIB, INVALID_TYPE_TO_SORT),
                "expected an ordered type, but found '" + type.toString() + "'");
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
