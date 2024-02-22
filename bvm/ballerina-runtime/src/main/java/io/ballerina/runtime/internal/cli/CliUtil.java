/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.cli;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.TypeHelper;

import java.util.List;

/**
 * Contains the util functions for CLI parsing.
 */
public class CliUtil {

    private static final String INVALID_ARGUMENT_ERROR = "invalid argument '%s' for parameter '%s', expected %s value";


    static boolean isLongOption(String arg) {
        return arg.startsWith("--");
    }

    private CliUtil() {
    }

    static Object getBValueWithUnionValue(Type type, String value, String parameterName) {
        if (TypeUtils.getImpliedType(type).getTag() == TypeTags.UNION_TAG) {
            return getUnionValue(type, value, parameterName);
        }
        return getBValue(type, value, parameterName);
    }

    static Object getUnionValue(Type type, String value, String parameterName) {
        List<Type> unionMemberTypes = TypeHelper.memberList(type);
        if (isUnionWithNil(unionMemberTypes)) {
            type = (TypeChecker.checkIsType(unionMemberTypes.get(0), PredefinedTypes.TYPE_NULL)) ?
                    unionMemberTypes.get(1) :
                    unionMemberTypes.get(0);
            return getBValue(type, value, parameterName);
        }
        throw getUnsupportedTypeException(type);
    }

    static Object getBValue(Type type, String value, String parameterName) {
        switch (TypeUtils.getImpliedType(type).getTag()) {
            case TypeTags.STRING_TAG:
                return StringUtils.fromString(value);
            case TypeTags.CHAR_STRING_TAG:
                return getCharValue(value, parameterName);
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                return getIntegerValue(value, parameterName);
            case TypeTags.BYTE_TAG:
                return getByteValue(value, parameterName);
            case TypeTags.FLOAT_TAG:
                return getFloatValue(value, parameterName);
            case TypeTags.DECIMAL_TAG:
                return getDecimalValue(value, parameterName);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return getBValue(TypeUtils.getImpliedType(type), value, parameterName);
            case TypeTags.BOOLEAN_TAG:
                throw ErrorCreator.createError(StringUtils.fromString("the option '" + parameterName + "' of type " +
                                                                              "'boolean' is expected without a value"));
            default:
                throw getUnsupportedTypeException(type);
        }
    }

    private static Object getCharValue(String argument, String parameterName) {
        try {
            return TypeConverter.stringToChar(StringUtils.fromString(argument));
        } catch (BError e) {
            throw getInvalidArgumentError(argument, parameterName, "string:Char");
        }
    }

    private static Object getByteValue(String argument, String parameterName) {
        try {
            return TypeConverter.stringToByte(argument);
        } catch (NumberFormatException | BError e) {
            throw getInvalidArgumentError(argument, parameterName, "byte");
        }
    }

    static BError getUnsupportedTypeException(Type type) {
        return ErrorCreator.createError(
                StringUtils.fromString("unsupported type expected with main function '" + type + "'"));
    }

    static boolean isUnionWithNil(Type fieldType) {
        if (TypeUtils.getImpliedType(fieldType).getTag() == TypeTags.UNION_TAG) {
            List<Type> unionMemberTypes = TypeHelper.memberList(fieldType);
            if (isUnionWithNil(unionMemberTypes)) {
                return true;
            }
            throw getUnsupportedTypeException(fieldType);
        }
        return false;
    }

    static boolean isUnionWithNil(List<Type> unionMemberTypes) {
        if (unionMemberTypes.size() != 2) {
            return false;
        }
        for (Type each : unionMemberTypes) {
            if (TypeChecker.checkIsType(each, PredefinedTypes.TYPE_NULL)) {
                return true;
            }
        }
        return false;
    }

    private static long getIntegerValue(String argument, String parameterName) {
        try {
            return TypeConverter.stringToInt(argument);
        } catch (NumberFormatException e) {
            throw getInvalidArgumentError(argument, parameterName, "integer");
        }
    }

    private static double getFloatValue(String argument, String parameterName) {
        try {
            return TypeConverter.stringToFloat(argument);
        } catch (NumberFormatException e) {
            throw getInvalidArgumentError(argument, parameterName, "float");
        }
    }

    private static BDecimal getDecimalValue(String argument, String parameterName) {
        try {
            return TypeConverter.stringToDecimal(argument);
        } catch (NumberFormatException | BError e) {
            throw getInvalidArgumentError(argument, parameterName, "decimal");
        }
    }

    private static BError getInvalidArgumentError(String argument, String parameterName, String type) {
        return ErrorCreator.createError(
                StringUtils.fromString(String.format(INVALID_ARGUMENT_ERROR, argument, parameterName, type)));
    }

    static boolean isSupportedType(int tag) {
        return tag == TypeTags.STRING_TAG || tag == TypeTags.INT_TAG || tag == TypeTags.FLOAT_TAG ||
                tag == TypeTags.DECIMAL_TAG || tag == TypeTags.BOOLEAN_TAG;
    }
}
