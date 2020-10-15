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

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;

import static io.ballerina.runtime.util.BLangConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static java.lang.String.format;

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
                                        StringUtils.fromString(format("%s not supported on type '%s'", op,
                                                                       type.getQualifiedName())));
    }
}
