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

import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.ballerinalang.jvm.TypeChecker.checkIsLikeType;
import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MIN_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.BINT_MAX_VALUE_DOUBLE_RANGE_MAX;
import static org.ballerinalang.jvm.util.BLangConstants.BINT_MIN_VALUE_DOUBLE_RANGE_MIN;
import static org.ballerinalang.jvm.values.DecimalValue.isDecimalWithinIntRange;

/**
 * Provides utils methods for casting, stamping and conversion of values.
 *
 * @since 0.995.0
 */
public class TypeConverter {

    private static final String NaN = "NaN";
    private static final String POSITIVE_INFINITY = "Infinity";
    private static final String NEGATIVE_INFINITY = "-Infinity";

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

    public static Object castValues(BType targetType, Object inputValue) {
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
                return anyToIntCast(inputValue, () ->
                        BallerinaErrors.createTypeCastError(inputValue, BTypes.typeInt));
            case TypeTags.DECIMAL_TAG:
                return anyToDecimal(inputValue, () ->
                        BallerinaErrors.createTypeCastError(inputValue, BTypes.typeDecimal));
            case TypeTags.FLOAT_TAG:
                return anyToFloatCast(inputValue, () ->
                        BallerinaErrors.createTypeCastError(inputValue, BTypes.typeFloat));
            case TypeTags.STRING_TAG:
                return anyToStringCast(inputValue, () ->
                        BallerinaErrors.createTypeCastError(inputValue, BTypes.typeString));
            case TypeTags.BOOLEAN_TAG:
                return anyToBooleanCast(inputValue, () ->
                        BallerinaErrors.createTypeCastError(inputValue, BTypes.typeBoolean));
            case TypeTags.BYTE_TAG:
                return anyToByteCast(inputValue, () ->
                        BallerinaErrors.createTypeCastError(inputValue, BTypes.typeByte));
            default:
                throw BallerinaErrors.createTypeCastError(inputValue, targetType);
        }
    }

    static boolean isConvertibleToByte(Object value) {
        BType inputType = TypeChecker.getType(value);
        switch (inputType.getTag()) {
            case TypeTags.BYTE_TAG:
                return true;
            case TypeTags.INT_TAG:
                return isByteLiteral((long) value);
            case TypeTags.FLOAT_TAG:
                Double doubleValue = (Double) value;
                return isFloatWithinIntRange(doubleValue) && isByteLiteral(doubleValue.longValue());
            case TypeTags.DECIMAL_TAG:
                return isDecimalWithinIntRange((BigDecimal) value) && isByteLiteral(((BigDecimal) value).longValue());
            default:
                return false;
        }
    }

    static boolean isConvertibleToInt(Object value) {
        BType inputType = TypeChecker.getType(value);
        switch (inputType.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                return true;
            case TypeTags.FLOAT_TAG:
                return isFloatWithinIntRange((double) value);
            case TypeTags.DECIMAL_TAG:
                return isDecimalWithinIntRange((BigDecimal) value);
            default:
                return false;
        }
    }

    static boolean isConvertibleToFloatingPointTypes(Object value) {
        BType inputType = TypeChecker.getType(value);
        switch (inputType.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
                return true;
            default:
                return false;
        }
    }

    public static List<BType> getConvertibleTypes(Object inputValue, BType targetType) {
        return getConvertibleTypes(inputValue, targetType, new ArrayList<>());
    }

    public static List<BType> getConvertibleTypes(Object inputValue, BType targetType,
                                                  List<TypeValuePair> unresolvedValues) {
        List<BType> convertibleTypes = new ArrayList<>();

        int targetTypeTag = targetType.getTag();

        switch (targetTypeTag) {
            case TypeTags.UNION_TAG:
                for (BType memType : ((BUnionType) targetType).getMemberTypes()) {
                    convertibleTypes.addAll(getConvertibleTypes(inputValue, memType, unresolvedValues));
                }
                break;
            case TypeTags.RECORD_TYPE_TAG:
                if (isConvertibleToRecordType(inputValue, (BRecordType) targetType, unresolvedValues)) {
                    convertibleTypes.add(targetType);
                }
                break;
            default:
                if (TypeChecker.checkIsLikeType(inputValue, targetType, true)) {
                    convertibleTypes.add(targetType);
                }
        }
        return convertibleTypes;
    }

    private static boolean isConvertibleToRecordType(Object sourceValue, BRecordType targetType,
                                                     List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof MapValueImpl)) {
            return false;
        }

        TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            return true;
        }
        unresolvedValues.add(typeValuePair);

        Map<String, BType> targetFieldTypes = new HashMap<>();
        BType restFieldType = targetType.restFieldType;

        for (BField field : targetType.getFields().values()) {
            targetFieldTypes.put(field.getFieldName(), field.type);
        }

        MapValueImpl sourceMapValueImpl = (MapValueImpl) sourceValue;
        for (Map.Entry targetTypeEntry : targetFieldTypes.entrySet()) {
            String fieldName = targetTypeEntry.getKey().toString();

            if (!sourceMapValueImpl.containsKey(fieldName)) {
                BField targetField = targetType.getFields().get(fieldName);
                if (Flags.isFlagOn(targetField.flags, Flags.REQUIRED)) {
                    return false;
                }
            }
        }

        for (Object object : sourceMapValueImpl.entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldName = valueEntry.getKey().toString();

            if (targetFieldTypes.containsKey(fieldName)) {
                if (getConvertibleTypes(valueEntry.getValue(), targetFieldTypes.get(fieldName),
                                        unresolvedValues).size() != 1) {
                    return false;
                }
            } else if (!targetType.sealed) {
                if (getConvertibleTypes(valueEntry.getValue(), restFieldType, unresolvedValues).size() != 1) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    static long anyToInt(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal;
        } else if (sourceVal instanceof Double) {
            return floatToInt((double) sourceVal);
        } else if (sourceVal instanceof Integer) {
            return ((Integer) sourceVal).longValue();
        } else if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal ? 1 : 0;
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).intValue();
        } else if (sourceVal instanceof String) {
            try {
                return Long.parseLong((String) sourceVal);
            } catch (NumberFormatException e) {
                throw errorFunc.get();
            }
        }

        throw errorFunc.get();
    }

    static long anyToIntCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal;
        } else if (sourceVal instanceof Double) {
            return floatToInt((double) sourceVal);
        } else if (sourceVal instanceof Integer) {
            return ((Integer) sourceVal).longValue();
        } else if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal ? 1 : 0;
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).intValue();
        } else {
            throw errorFunc.get();
        }
    }

    static double anyToFloat(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return ((Long) sourceVal).doubleValue();
        } else if (sourceVal instanceof Double) {
            return (Double) sourceVal;
        } else if (sourceVal instanceof Integer) {
            return ((Integer) sourceVal).floatValue();
        } else if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal ? 1.0 : 0.0;
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).floatValue();
        } else if (sourceVal instanceof String) {
            try {
                return Double.parseDouble((String) sourceVal);
            } catch (NumberFormatException e) {
                throw errorFunc.get();
            }
        }

        throw errorFunc.get();
    }

    static double anyToFloatCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return ((Long) sourceVal).doubleValue();
        } else if (sourceVal instanceof Double) {
            return (Double) sourceVal;
        } else if (sourceVal instanceof Integer) {
            return ((Integer) sourceVal).floatValue();
        } else if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal ? 1.0 : 0.0;
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).floatValue();
        } else {
            throw errorFunc.get();
        }
    }

    static boolean anyToBoolean(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return (long) sourceVal != 0;
        } else if (sourceVal instanceof Double) {
            return (Double) sourceVal != 0.0;
        } else if (sourceVal instanceof Integer) {
            return (int) sourceVal != 0;
        } else if (sourceVal instanceof Boolean) {
            return (boolean) sourceVal;
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).booleanValue();
        } else if (sourceVal instanceof String) {
            try {
                return Boolean.parseBoolean((String) sourceVal);
            } catch (NumberFormatException e) {
                throw errorFunc.get();
            }
        }

        throw errorFunc.get();
    }

    static boolean anyToBooleanCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Boolean) {
            return (boolean) sourceVal;
        }

        throw errorFunc.get();
    }

    public static int intToByte(long sourceVal) {
        if (!isByteLiteral(sourceVal)) {
            throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeByte);
        }
        return ((Long) sourceVal).intValue();
    }

    public static int floatToByte(double sourceVal) {
        checkIsValidFloat(sourceVal, BTypes.typeByte);

        long intVal = Math.round(sourceVal);
        if (!isByteLiteral(intVal)) {
            throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeByte);
        }

        return (int) intVal;
    }

    public static long floatToInt(double sourceVal) {
        checkIsValidFloat(sourceVal, BTypes.typeInt);

        if (!isFloatWithinIntRange(sourceVal)) {
            throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeInt);
        }

        return (long) Math.rint(sourceVal);
    }

    private static void checkIsValidFloat(double sourceVal, BType targetType) {
        if (Double.isNaN(sourceVal)) {
            throw BallerinaErrors.createNumericConversionError(NaN, BTypes.typeFloat, targetType);
        }

        if (Double.isInfinite(sourceVal)) {
            String value = sourceVal > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
            throw BallerinaErrors.createNumericConversionError(value, BTypes.typeFloat, targetType);
        }
    }

    static int anyToByte(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return intToByte((Long) sourceVal);
        } else if (sourceVal instanceof Double) {
            return floatToByte((Double) sourceVal);
        } else if (sourceVal instanceof Integer) {
            return (int) sourceVal;
        } else if (sourceVal instanceof Boolean) {
            return ((Boolean) sourceVal ? 1 : 0);
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).byteValue();
        } else if (sourceVal instanceof String) {
            try {
                return Integer.parseInt((String) sourceVal);
            } catch (NumberFormatException e) {
                throw errorFunc.get();
            }
        }

        throw errorFunc.get();
    }

    static int anyToByteCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return intToByte((Long) sourceVal);
        } else if (sourceVal instanceof Byte) {
            return ((Byte) sourceVal).intValue();
        } else if (sourceVal instanceof Double) {
            return floatToByte((Double) sourceVal);
        } else if (sourceVal instanceof Integer) {
            return (int) sourceVal;
        } else if (sourceVal instanceof Boolean) {
            return ((Boolean) sourceVal ? 1 : 0);
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).byteValue();
        } else {
            throw errorFunc.get();
        }

    }

    private static String anyToString(Object sourceVal) {
        if (sourceVal instanceof Long) {
            return Long.toString((Long) sourceVal);
        } else if (sourceVal instanceof Double) {
            return Double.toString((Double) sourceVal);
        } else if (sourceVal instanceof Integer) {
            return Long.toString((Integer) sourceVal);
        } else if (sourceVal instanceof Boolean) {
            return Boolean.toString((Boolean) sourceVal);
        } else if (sourceVal instanceof DecimalValue) {
            return ((DecimalValue) sourceVal).stringValue();
        } else if (sourceVal instanceof String) {
            return (String) sourceVal;
        } else if (sourceVal == null) {
            return "()";
        }

        throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeString);
    }

    private static String anyToStringCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof String) {
            return (String) sourceVal;
        }

        throw errorFunc.get();
    }

    static DecimalValue anyToDecimal(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return DecimalValue.valueOf((Long) sourceVal);
        } else if (sourceVal instanceof Double) {
            return DecimalValue.valueOf((Double) sourceVal);
        } else if (sourceVal instanceof Integer) {
            return DecimalValue.valueOf((Integer) sourceVal);
        } else if (sourceVal instanceof Boolean) {
            return DecimalValue.valueOf((Boolean) sourceVal);
        } else if (sourceVal instanceof DecimalValue) {
            return (DecimalValue) sourceVal;
        } else if (sourceVal instanceof String) {
            return new DecimalValue((String) sourceVal);
        } else if (sourceVal instanceof DecimalValue) {
            return (DecimalValue) sourceVal;
        }

        throw errorFunc.get();
    }

    // JBallerina related casts

    static byte anyToJByteCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Byte) {
            return (Byte) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static char anyToJCharCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Character) {
            return (Character) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static short anyToJShortCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Short) {
            return (Short) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static int anyToJIntCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Integer) {
            return (Integer) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static long anyToJLongCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static float anyToJFloatCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Float) {
            return (Float) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static double anyToJDoubleCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Double) {
            return (Double) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static boolean anyToJBooleanCast(Object sourceVal, Supplier<ErrorValue> errorFunc) {
        if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    public static long jFloatToBInt(float sourceVal) {
        checkIsValidFloat(sourceVal, BTypes.typeInt);

        if (!isFloatWithinIntRange(sourceVal)) {
            throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeInt);
        }

        return (long) Math.rint(sourceVal);
    }

    public static long jDoubleToBInt(double sourceVal) {
        checkIsValidFloat(sourceVal, BTypes.typeInt);

        if (!isFloatWithinIntRange(sourceVal)) {
            throw BallerinaErrors.createNumericConversionError(sourceVal, BTypes.typeInt);
        }

        return (long) Math.rint(sourceVal);
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
