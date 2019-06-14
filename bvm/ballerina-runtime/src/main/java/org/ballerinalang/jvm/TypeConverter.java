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
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;

import java.util.function.Supplier;

import static org.ballerinalang.jvm.TypeChecker.checkIsLikeType;
import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MIN_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.BINT_MAX_VALUE_DOUBLE_RANGE_MAX;
import static org.ballerinalang.jvm.util.BLangConstants.BINT_MIN_VALUE_DOUBLE_RANGE_MIN;

/**
 * Provides utils methods for casting, stamping and conversion of values.
 *
 * @since 0.995.0
 */
public class TypeConverter {

    public static Object convertValues(BType targetType, Object inputValue) {
        BType inputType = TypeChecker.getType(inputValue);
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
                return anyToInt(inputValue, () ->
                        BallerinaErrors.createNumericConversionError(inputValue, BTypes.typeInt));
            case TypeTags.DECIMAL_TAG:
                return anyToDecimal(inputValue, () ->
                        BallerinaErrors.createNumericConversionError(inputValue, BTypes.typeDecimal));
            case TypeTags.FLOAT_TAG:
                return anyToFloat(inputValue, () ->
                        BallerinaErrors.createNumericConversionError(inputValue, BTypes.typeFloat));
            case TypeTags.STRING_TAG:
                return anyToString(inputValue);
            case TypeTags.BOOLEAN_TAG:
                return anyToBoolean(inputValue, () ->
                        BallerinaErrors.createNumericConversionError(inputValue, BTypes.typeBoolean));
            case TypeTags.BYTE_TAG:
                return anyToByte(inputValue, () ->
                        BallerinaErrors.createNumericConversionError(inputValue, BTypes.typeByte));
            default:
                throw new ErrorValue(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                     BLangExceptionHelper.getErrorMessage(
                                             RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                             inputType, inputValue, targetType));
        }
    }

    static long anyToInt(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal;
        } else if (sourceVal instanceof Double) {
            if (Double.isNaN((Double) sourceVal) || Double.isInfinite((Double) sourceVal)) {
                throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeInt);
            }
            if (!isFloatWithinIntRange((Double) sourceVal)) {
                throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeInt);
            }
            return Math.round((Double) sourceVal);
        } else if (sourceVal instanceof Byte) {
            return (long) sourceVal;
        } else if (sourceVal instanceof Integer) {
            return ((Integer) sourceVal).longValue();
        } else if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal ? 0 : 1;
        } else if (sourceVal instanceof DecimalValue) {
            if (!isFloatWithinIntRange(((DecimalValue) sourceVal).floatValue())) {
                throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeInt);
            }
            return ((DecimalValue) sourceVal).intValue();
        }
        throw errorFunc.get();
    }

    static double anyToFloat(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return ((Long) sourceVal).doubleValue();
        } else if (sourceVal instanceof Double) {
            return (Double) sourceVal;
        } else if (sourceVal instanceof Byte) {
            return (double) sourceVal;
        } else if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal ? 0 : 1;
        } else if (sourceVal instanceof DecimalValue) {
            if (!isFloatWithinIntRange(((DecimalValue) sourceVal).floatValue())) {
                throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeFloat);
            }
            return ((DecimalValue) sourceVal).floatValue();
        }
        throw errorFunc.get();
    }

    static boolean anyToBoolean(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal != 0;
        } else if (sourceVal instanceof Double) {
            return (Double) sourceVal != 0.0;
        } else if (sourceVal instanceof Byte) {
            return (Byte) sourceVal != 0;
        } else if (sourceVal instanceof Boolean) {
            return (boolean) sourceVal;
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).booleanValue();
        }
        throw errorFunc.get();
    }

    public static int intToByte(long sourceVal) {
        if (!isByteLiteral(sourceVal)) {
            throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeByte);
        }
        return ((Long) sourceVal).intValue();
    }

    static int anyToByte(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return intToByte((Long) sourceVal);
        } else if (sourceVal instanceof Double) {
            Double value = (Double) sourceVal;
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeByte);
            }
            long intVal = Math.round(value);
            if (!isByteLiteral(intVal)) {
                throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeByte);
            }
            return (int) intVal;

        } else if (sourceVal instanceof Byte) {
            return (byte) sourceVal;
        } else if (sourceVal instanceof Integer) {
            return (int) sourceVal;
        } else if (sourceVal instanceof Boolean) {
            return ((Boolean) sourceVal ? 0 : 1);
        } else if (sourceVal instanceof DecimalValue) {
            if (!isByteLiteral(((DecimalValue) sourceVal).intValue())) {
                throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeByte);
            }
            return ((DecimalValue) sourceVal).byteValue();
        }
        throw errorFunc.get();
    }

    private static String anyToString(Object sourceVal) {
        if (sourceVal instanceof Long) {
            return Long.toString((Long) sourceVal);
        } else if (sourceVal instanceof Double) {
            return Double.toString((Double) sourceVal);
        } else if (sourceVal instanceof Byte) {
            return Long.toString((Byte) sourceVal);
        } else if (sourceVal instanceof Boolean) {
            return Boolean.toString((Boolean) sourceVal);
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).stringValue();
        }
        throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeString);
    }

    static DecimalValue anyToDecimal(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return DecimalValue.valueOf((Long) sourceVal);
        } else if (sourceVal instanceof Double) {
            return DecimalValue.valueOf((Double) sourceVal);
        } else if (sourceVal instanceof Byte) {
            return DecimalValue.valueOf((Byte) sourceVal);
        } else if (sourceVal instanceof Boolean) {
            return (boolean) sourceVal ? DecimalValue.valueOf(1) : DecimalValue.valueOf(0);
        } else if (sourceVal instanceof String) {
            return new DecimalValue((String) sourceVal);
        }

        throw errorFunc.get();
    }

    private static boolean isByteLiteral(long longValue) {
        return (longValue >= BBYTE_MIN_VALUE && longValue <= BBYTE_MAX_VALUE);
    }

    private static boolean isFloatWithinIntRange(double doubleValue) {
        return doubleValue < BINT_MAX_VALUE_DOUBLE_RANGE_MAX && doubleValue > BINT_MIN_VALUE_DOUBLE_RANGE_MIN;
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
