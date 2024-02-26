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

package org.ballerinalang.langlib.array.utils;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.internal.errors.ErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.runtime.internal.errors.ErrorReasons.getModulePrefixedReason;

/**
 * Utility functions for dealing with ArrayValue.
 *
 * @since 1.0
 */
public class ArrayUtils {

    @Deprecated
    public static void add(BArray arr, int elemTypeTag, long index, Object value) {
        switch (elemTypeTag) {
            case TypeTags.INT_TAG:
                arr.add(index, (long) value);
                break;
            case TypeTags.BOOLEAN_TAG:
                arr.add(index, (boolean) value);
                break;
            case TypeTags.BYTE_TAG:
                arr.add(index, ((Integer) value).byteValue());
                break;
            case TypeTags.FLOAT_TAG:
                arr.add(index, (double) value);
                break;
            case TypeTags.STRING_TAG:
                arr.add(index, (String) value);
                break;
            default:
                arr.add(index, value);
        }
    }

    public static GetFunction getElementAccessFunction(Type arrType, String funcName) {
        switch (TypeUtils.getImpliedType(arrType).getTag()) {
            case TypeTags.ARRAY_TAG:
                return BArray::get;
            case TypeTags.TUPLE_TAG:
                return BArray::getRefValue;
            default:
                throw createOpNotSupportedError(arrType, funcName);
        }
    }

    public static void checkIsArrayOnlyOperation(Type arrType, String op) {
        if (TypeUtils.getImpliedType(arrType).getTag() != TypeTags.ARRAY_TAG) {
            throw createOpNotSupportedError(arrType, op);
        }
    }

    public static void checkIsClosedArray(ArrayType arrType, String op) {
        if (arrType.getState() == ArrayType.ArrayState.CLOSED) {
            throw createOpNotSupportedError(arrType, op);
        }
    }

    public static BError createOpNotSupportedError(Type type, String op) {
        return ErrorCreator.createError(getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                OPERATION_NOT_SUPPORTED_IDENTIFIER),
                ErrorHelper.getErrorDetails(ErrorCodes.OPERATION_NOT_SUPPORTED_ERROR, op, type));
    }

    public static BArray createEmptyArrayFromTuple(BArray arr) {
        Type arrType = TypeUtils.getImpliedType(arr.getType());
        TupleType tupleType = (TupleType) arrType;
        List<Type> tupleTypes = tupleType.getTupleTypes();
        Type restType = tupleType.getRestType();
        Set<Type> uniqueTypes = new HashSet<>(tupleTypes);
        if (restType != null) {
            uniqueTypes.add(restType);
        }
        if (uniqueTypes.isEmpty()) {
            // Return an array with never type
            return ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_NEVER));
        } else if (uniqueTypes.size() == 1) {
            // Return an array with the member type
            Type type = uniqueTypes.iterator().next();
            ArrayType arrayType = TypeCreator.createArrayType(type);
            return ValueCreator.createArrayValue(arrayType);
        }
        // Return an array with the union of member types
        UnionType unionType = TypeCreator.createUnionType(new ArrayList<>(uniqueTypes));
        ArrayType slicedArrType = TypeCreator.createArrayType(unionType);
        return ValueCreator.createArrayValue(slicedArrType);
    }

    private ArrayUtils() {
    }
}
