/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.ballerinalang.jvm.TypeChecker.checkIsLikeType;
import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MIN_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.BINT_MAX_VALUE_BIG_DECIMAL_RANGE_MAX;
import static org.ballerinalang.jvm.util.BLangConstants.BINT_MAX_VALUE_DOUBLE_RANGE_MAX;
import static org.ballerinalang.jvm.util.BLangConstants.BINT_MIN_VALUE_BIG_DECIMAL_RANGE_MIN;
import static org.ballerinalang.jvm.util.BLangConstants.BINT_MIN_VALUE_DOUBLE_RANGE_MIN;

/**
 * Holds utils methods required for performing casting, stamping and conversion of values.
 *
 * @since 0.995.0
 */
public class TypeConverter {

    public static Object convertValues(BType targetType, Object inputValue) {
        BType inputType = TypeChecker.getType(inputValue);
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
                return anyToInt(inputValue);
            case TypeTags.FLOAT_TAG:
                return anyToFloat(inputValue);
            case TypeTags.STRING_TAG:
                return anyToString(inputType);
            case TypeTags.BOOLEAN_TAG:
                return anyToBoolean(inputValue);
            case TypeTags.BYTE_TAG:
                return anyToByte(inputValue);
            default:
                throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                             BLangExceptionHelper.getErrorMessage(
                                                     RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                                     inputType, inputValue, targetType));
        }
    }

    public static long anyToInt(Object sourceVal) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal;
        } else if (sourceVal instanceof Double) {
            if (Double.isNaN((Double) sourceVal) || Double.isInfinite((Double) sourceVal)) {
                throw getNumericConversionError(sourceVal, BTypes.typeInt);
            }
            if (!isFloatWithinIntRange((Double) sourceVal)) {
                throw getNumericConversionError(sourceVal, BTypes.typeInt);
            }
            return Math.round((Double) sourceVal);
        } else if (sourceVal instanceof Byte) {
            return (long) sourceVal;
        } else if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal ? 0 : 1;
        }
        throw getNumericConversionError(sourceVal, BTypes.typeInt);

    }

    public static double anyToFloat(Object sourceVal) {
        if (sourceVal instanceof Long) {
            return ((Long) sourceVal).doubleValue();
        } else if (sourceVal instanceof Double) {
            return (Double) sourceVal;
        } else if (sourceVal instanceof Byte) {
            return (double) sourceVal;
        } else if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal ? 0 : 1;
        }
        throw getNumericConversionError(sourceVal, BTypes.typeFloat);
    }

    public static boolean anyToBoolean(Object sourceVal) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal != 0;
        } else if (sourceVal instanceof Double) {
            return (Double) sourceVal != 0.0;
        } else if (sourceVal instanceof Byte) {
            return (Byte) sourceVal != 0;
        } else if (sourceVal instanceof Boolean) {
            return (boolean) sourceVal;
        }
        throw getNumericConversionError(sourceVal, BTypes.typeBoolean);
    }

    public static long anyToByte(Object sourceVal) {
        if (sourceVal instanceof Long) {
            if (!isByteLiteral((Long) sourceVal)) {
                throw getNumericConversionError(sourceVal, BTypes.typeByte);
            }
            return (long) sourceVal;
        } else if (sourceVal instanceof Double) {

            Double value = (Double) sourceVal;
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw getNumericConversionError(sourceVal, BTypes.typeByte);
            }
            long intVal = Math.round(value);
            if (!isByteLiteral(intVal)) {
                throw getNumericConversionError(sourceVal, BTypes.typeByte);
            }
            return  intVal;

        } else if (sourceVal instanceof Byte) {
            return (long) sourceVal;
        } else if (sourceVal instanceof Boolean) {
            return ((Boolean) sourceVal ? 0 : 1);
        } 
        throw getNumericConversionError(sourceVal, BTypes.typeByte);
    }

    public static String anyToString(Object sourceVal) {
        if (sourceVal instanceof Long) {
           return Long.toString((Long) sourceVal);
        } else if (sourceVal instanceof Double) {
            return Double.toString((Double) sourceVal);
        } else if (sourceVal instanceof Byte) {
            return Long.toString((Byte) sourceVal);
        } else if (sourceVal instanceof Boolean) {
            return  Boolean.toString((Boolean) sourceVal);
        } 
        throw getNumericConversionError(sourceVal, BTypes.typeString);
    }

    public static boolean isByteLiteral(long longValue) {
        return (longValue >= BBYTE_MIN_VALUE && longValue <= BBYTE_MAX_VALUE);
    }

    public static boolean isFloatWithinIntRange(double doubleValue) {
        return doubleValue < BINT_MAX_VALUE_DOUBLE_RANGE_MAX && doubleValue > BINT_MIN_VALUE_DOUBLE_RANGE_MIN;
    }

    private static BLangRuntimeException getNumericConversionError(Object inputValue, BType targetType) {
        throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                     BLangExceptionHelper.getErrorMessage(
                                             RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                             TypeChecker.getType(inputValue), inputValue, targetType));
    }

    public static BType resolveMatchingTypeForUnion(Object value, BType type) {
        if (value instanceof ArrayValue && ((ArrayValue) value).getType().getTag() == TypeTags.ARRAY_TAG &&
                !isDeepStampingRequiredForArray(((ArrayValue) value).getArrayType())) {
            return ((ArrayValue) value).getArrayType();
        }

        if (value instanceof MapValue && ((MapValue) value).getType().getTag() == TypeTags.MAP_TAG &&
                !isDeepStampingRequiredForMap(((MapValue) value).getType())) {
            return ((MapValue) value).getType();
        }

        if (checkIsLikeType(value, BTypes.typeInt)) {
            return BTypes.typeInt;
        }

        if (checkIsLikeType(value, BTypes.typeFloat)) {
            return BTypes.typeFloat;
        }

        if (checkIsLikeType(value, BTypes.typeString)) {
            return BTypes.typeString;
        }

        if (checkIsLikeType(value, BTypes.typeBoolean)) {
            return BTypes.typeBoolean;
        }

        if (checkIsLikeType(value, BTypes.typeByte)) {
            return BTypes.typeByte;
        }

        BType anydataArrayType = new BArrayType(type);
        if (checkIsLikeType(value, anydataArrayType)) {
            return anydataArrayType;
        }

        if (checkIsLikeType(value, BTypes.typeXML)) {
            return BTypes.typeXML;
        }

        BType anydataMapType = new BMapType(type);
        if (checkIsLikeType(value, anydataMapType)) {
            return anydataMapType;
        }

        //not possible
        return null;
    }

    private static boolean isDeepStampingRequiredForArray(BType sourceType) {
        BType elementType = ((BArrayType) sourceType).getElementType();

        if (elementType != null) {
            if (BTypes.isValueType(elementType)) {
                return false;
            } else if (elementType instanceof BArrayType) {
                return isDeepStampingRequiredForArray(elementType);
            }
            return true;
        }
        return true;
    }

    private static boolean isDeepStampingRequiredForMap(BType sourceType) {
        BType constrainedType = ((BMapType) sourceType).getConstrainedType();

        if (constrainedType != null) {
            if (BTypes.isValueType(constrainedType)) {
                return false;
            } else if (constrainedType instanceof BMapType) {
                return isDeepStampingRequiredForMap(constrainedType);
            }
            return true;
        }
        return true;
    }

}
