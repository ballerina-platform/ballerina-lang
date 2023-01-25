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
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

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
        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                return BArray::get;
            case TypeTags.TUPLE_TAG:
                return BArray::getRefValue;
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return getElementAccessFunction(TypeUtils.getReferredType(arrType), funcName);
            default:
                throw createOpNotSupportedError(arrType, funcName);
        }
    }

    public static void checkIsArrayOnlyOperation(Type arrType, String op) {
        if (arrType.getTag() != TypeTags.ARRAY_TAG) {
            throw createOpNotSupportedError(arrType, op);
        }
    }

    public static BError createOpNotSupportedError(Type type, String op) {
        return ErrorCreator.createError(getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                OPERATION_NOT_SUPPORTED_IDENTIFIER),
                BLangExceptionHelper.getErrorDetails(RuntimeErrors.OPERATION_NOT_SUPPORTED_ERROR, op, type));
    }

    public static BArray createEmptyArrayFromTuple(BArray arr) {
        Type arrType = TypeUtils.getReferredType(arr.getType());
        TupleType tupleType = (TupleType) arrType;
        List<Type> memTypes = new ArrayList<>();
        List<Type> tupleTypes = tupleType.getTupleTypes();
        boolean isSameType = true;
        Type sameType = null;
        if (!tupleTypes.isEmpty()) {
            sameType = tupleTypes.get(0);
            memTypes.add(sameType);
        }
        for (int i = 1; i < tupleTypes.size(); i++) {
            isSameType &= sameType == tupleTypes.get(i);
            memTypes.add(tupleTypes.get(i));
        }
        Type restType = tupleType.getRestType();
        // If there's a tuple-rest-descriptor the array will not be of the same type even if other types are the same
        if (restType != null) {
            isSameType = false;
            memTypes.add(restType);
        }
        // Create an array of one type if the member-type-descriptors are the same
        if (isSameType) {
            ArrayType type = TypeCreator.createArrayType(sameType);
            return ValueCreator.createArrayValue(type);
        }
        UnionType unionType = TypeCreator.createUnionType(memTypes);
        ArrayType slicedArrType = TypeCreator.createArrayType(unionType);
        return ValueCreator.createArrayValue(slicedArrType);
    }

    private ArrayUtils() {
    }
}
