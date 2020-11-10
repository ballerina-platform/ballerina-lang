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
package io.ballerina.runtime;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.commons.TypeValuePair;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.types.BTableType;
import io.ballerina.runtime.types.BUnionType;
import io.ballerina.runtime.util.Flags;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.MapValue;
import io.ballerina.runtime.values.MapValueImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static io.ballerina.runtime.TypeChecker.checkIsLikeType;
import static io.ballerina.runtime.TypeChecker.isCharLiteralValue;
import static io.ballerina.runtime.TypeChecker.isSigned16LiteralValue;
import static io.ballerina.runtime.TypeChecker.isSigned32LiteralValue;
import static io.ballerina.runtime.TypeChecker.isSigned8LiteralValue;
import static io.ballerina.runtime.TypeChecker.isUnsigned16LiteralValue;
import static io.ballerina.runtime.TypeChecker.isUnsigned32LiteralValue;
import static io.ballerina.runtime.TypeChecker.isUnsigned8LiteralValue;
import static io.ballerina.runtime.util.BLangConstants.BINT_MAX_VALUE_DOUBLE_RANGE_MAX;
import static io.ballerina.runtime.util.BLangConstants.BINT_MIN_VALUE_DOUBLE_RANGE_MIN;
import static io.ballerina.runtime.values.DecimalValue.isDecimalWithinIntRange;

/**
 * Provides utils methods for casting, stamping and conversion of values.
 *
 * @since 0.995.0
 */
public class TypeConverter {

    private static final String NaN = "NaN";
    private static final String POSITIVE_INFINITY = "Infinity";
    private static final String NEGATIVE_INFINITY = "-Infinity";

    public static Object convertValues(Type targetType, Object inputValue) {
        Type inputType = TypeChecker.getType(inputValue);
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                return anyToInt(inputValue, () ->
                        ErrorUtils.createNumericConversionError(inputValue, PredefinedTypes.TYPE_INT));
            case TypeTags.DECIMAL_TAG:
                return anyToDecimal(inputValue, () ->
                        ErrorUtils.createNumericConversionError(inputValue, PredefinedTypes.TYPE_DECIMAL));
            case TypeTags.FLOAT_TAG:
                return anyToFloat(inputValue, () ->
                        ErrorUtils.createNumericConversionError(inputValue, PredefinedTypes.TYPE_FLOAT));
            case TypeTags.STRING_TAG:
                return StringUtils.fromString(anyToString(inputValue));
            case TypeTags.BOOLEAN_TAG:
                return anyToBoolean(inputValue, () ->
                        ErrorUtils.createNumericConversionError(inputValue, PredefinedTypes.TYPE_BOOLEAN));
            case TypeTags.BYTE_TAG:
                return anyToByte(inputValue, () ->
                        ErrorUtils.createNumericConversionError(inputValue, PredefinedTypes.TYPE_BYTE));
            default:
                throw ErrorCreator.createError(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                               BLangExceptionHelper.getErrorMessage(
                                                          RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                                          inputType, inputValue, targetType));
        }
    }

    public static Object castValues(Type targetType, Object inputValue) {
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                return anyToIntCast(inputValue, () ->
                        ErrorUtils.createTypeCastError(inputValue, PredefinedTypes.TYPE_INT));
            case TypeTags.DECIMAL_TAG:
                return anyToDecimal(inputValue, () ->
                        ErrorUtils.createTypeCastError(inputValue, PredefinedTypes.TYPE_DECIMAL));
            case TypeTags.FLOAT_TAG:
                return anyToFloatCast(inputValue, () ->
                        ErrorUtils.createTypeCastError(inputValue, PredefinedTypes.TYPE_FLOAT));
            case TypeTags.STRING_TAG:
                return anyToStringCast(inputValue, () ->
                        ErrorUtils.createTypeCastError(inputValue, PredefinedTypes.TYPE_STRING));
            case TypeTags.BOOLEAN_TAG:
                return anyToBooleanCast(inputValue, () ->
                        ErrorUtils.createTypeCastError(inputValue, PredefinedTypes.TYPE_BOOLEAN));
            case TypeTags.BYTE_TAG:
                return anyToByteCast(inputValue, () ->
                        ErrorUtils.createTypeCastError(inputValue, PredefinedTypes.TYPE_BYTE));
            default:
                throw ErrorUtils.createTypeCastError(inputValue, targetType);
        }
    }

    static boolean isConvertibleToByte(Object value) {
        Type inputType = TypeChecker.getType(value);
        switch (inputType.getTag()) {
            case TypeTags.BYTE_TAG:
                return true;
            case TypeTags.INT_TAG:
                return TypeChecker.isByteLiteral((long) value);
            case TypeTags.FLOAT_TAG:
                Double doubleValue = (Double) value;
                return isFloatWithinIntRange(doubleValue) && TypeChecker.isByteLiteral(doubleValue.longValue());
            case TypeTags.DECIMAL_TAG:
                return isDecimalWithinIntRange((BigDecimal) value)
                        && TypeChecker.isByteLiteral(((BigDecimal) value).longValue());
            default:
                return false;
        }
    }

    static boolean isConvertibleToInt(Object value) {
        Type inputType = TypeChecker.getType(value);
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

    static boolean isConvertibleToIntSubType(Object value, Type targetType) {
        Type inputType = TypeChecker.getType(value);
        long val;
        switch (inputType.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                val = ((Number) value).longValue();
                break;
            case TypeTags.FLOAT_TAG:
                if (!isFloatWithinIntRange((Double) value)) {
                    return false;
                }
                val = floatToInt((Double) value);
                break;
            case TypeTags.DECIMAL_TAG:
                if (!isDecimalWithinIntRange((BigDecimal) value)) {
                    return false;
                }
                val = ((BigDecimal) value).intValue();
                break;
            default:
                return false;
        }
        switch (targetType.getTag()) {
            case TypeTags.SIGNED32_INT_TAG:
                return TypeChecker.isSigned32LiteralValue(val);
            case TypeTags.SIGNED16_INT_TAG:
                return TypeChecker.isSigned16LiteralValue(val);
            case TypeTags.SIGNED8_INT_TAG:
                return TypeChecker.isSigned8LiteralValue(val);
            case TypeTags.UNSIGNED32_INT_TAG:
                return TypeChecker.isUnsigned32LiteralValue(val);
            case TypeTags.UNSIGNED16_INT_TAG:
                return TypeChecker.isUnsigned16LiteralValue(val);
            case TypeTags.UNSIGNED8_INT_TAG:
                return TypeChecker.isUnsigned8LiteralValue(val);
        }
        return false;
    }

    static boolean isConvertibleToChar(Object value) {
        Type inputType = TypeChecker.getType(value);
        if (inputType.getTag() == TypeTags.STRING_TAG) {
            return isCharLiteralValue(value);
        }
        return false;
    }

    static boolean isConvertibleToFloatingPointTypes(Object value) {
        Type inputType = TypeChecker.getType(value);
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

    // TODO: return only the first matching type
    public static List<Type> getConvertibleTypes(Object inputValue, Type targetType) {
        return getConvertibleTypes(inputValue, targetType, new ArrayList<>());
    }

    public static List<Type> getConvertibleTypes(Object inputValue, Type targetType,
                                                  List<TypeValuePair> unresolvedValues) {
        List<Type> convertibleTypes = new ArrayList<>();

        int targetTypeTag = targetType.getTag();

        switch (targetTypeTag) {
            case TypeTags.UNION_TAG:
                for (Type memType : ((BUnionType) targetType).getMemberTypes()) {
                    convertibleTypes.addAll(getConvertibleTypes(inputValue, memType, unresolvedValues));
                }
                break;
            case TypeTags.RECORD_TYPE_TAG:
                if (isConvertibleToRecordType(inputValue, (BRecordType) targetType, false, unresolvedValues)) {
                    convertibleTypes.add(targetType);
                }
                break;
            case TypeTags.ANYDATA_TAG:
                Type matchingType = TypeConverter.resolveMatchingTypeForUnion(inputValue, targetType);
                if (matchingType != null) {
                    convertibleTypes.add(matchingType);
                }
                break;
            default:
                if (TypeChecker.checkIsLikeType(inputValue, targetType, true)) {
                    convertibleTypes.add(targetType);
                }
        }
        return convertibleTypes;
    }

    public static List<Type> getConvertibleTypesFromJson(Object value, Type targetType,
                                                          List<TypeValuePair> unresolvedValues) {
        List<Type> convertibleTypes = new ArrayList<>();

        int targetTypeTag = targetType.getTag();

        convertibleTypes.addAll(TypeConverter.getConvertibleTypes(value, targetType));

        if (convertibleTypes.size() == 0) {
            switch (targetTypeTag) {
                case TypeTags.RECORD_TYPE_TAG:
                    if (isConvertibleToRecordType(value, (BRecordType) targetType, true, unresolvedValues)) {
                        convertibleTypes.add(targetType);
                    }
                    break;
                case TypeTags.TABLE_TAG:
                    if (((BTableType) targetType).getConstrainedType().getTag() == TypeTags.RECORD_TYPE_TAG ||
                            ((BTableType) targetType).getConstrainedType().getTag() == TypeTags.MAP_TAG) {
                        convertibleTypes.add(targetType);
                    }
                    break;
                case TypeTags.XML_TAG:
                case TypeTags.XML_ELEMENT_TAG:
                case TypeTags.XML_COMMENT_TAG:
                case TypeTags.XML_PI_TAG:
                case TypeTags.XML_TEXT_TAG:
                    if (TypeChecker.getType(value).getTag() == TypeTags.STRING_TAG) {
                        convertibleTypes.add(targetType);
                    }
                    break;
            }
        }
        return convertibleTypes;
    }

    private static boolean isConvertibleToRecordType(Object sourceValue, BRecordType targetType, boolean isFromJson,
                                                     List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof MapValueImpl)) {
            return false;
        }

        TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            return true;
        }
        unresolvedValues.add(typeValuePair);

        Map<String, Type> targetFieldTypes = new HashMap<>();
        Type restFieldType = targetType.restFieldType;

        for (Field field : targetType.getFields().values()) {
            targetFieldTypes.put(field.getFieldName(), field.getFieldType());
        }

        MapValueImpl sourceMapValueImpl = (MapValueImpl) sourceValue;
        for (Map.Entry targetTypeEntry : targetFieldTypes.entrySet()) {
            String fieldName = targetTypeEntry.getKey().toString();

            if (sourceMapValueImpl.containsKey(StringUtils.fromString(fieldName))) {
                continue;
            }
            Field targetField = targetType.getFields().get(fieldName);
            if (Flags.isFlagOn(targetField.getFlags(), Flags.REQUIRED)) {
                return false;
            }
        }

        for (Object object : sourceMapValueImpl.entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldName = valueEntry.getKey().toString();

            if (isFromJson) {
                if (targetFieldTypes.containsKey(fieldName)) {
                    if (getConvertibleTypesFromJson(valueEntry.getValue(), targetFieldTypes.get(fieldName),
                            unresolvedValues).size() != 1) {
                        return false;
                    }
                } else if (!targetType.sealed) {
                    if (getConvertibleTypesFromJson(valueEntry.getValue(), restFieldType,
                            unresolvedValues).size() != 1) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
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
        }
        return true;
    }

    static long anyToInt(Object sourceVal, Supplier<BError> errorFunc) {
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

    static long anyToIntCast(Object sourceVal, Supplier<BError> errorFunc) {
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

    static long anyToIntSubTypeCast(Object sourceVal, Type type, Supplier<BError> errorFunc) {
        long value = anyToIntCast(sourceVal, errorFunc);
        if (type == PredefinedTypes.TYPE_INT_SIGNED_32 && isSigned32LiteralValue(value)) {
            return value;
        } else if (type == PredefinedTypes.TYPE_INT_SIGNED_16 && isSigned16LiteralValue(value)) {
            return value;
        } else if (type == PredefinedTypes.TYPE_INT_SIGNED_8 && isSigned8LiteralValue(value)) {
            return value;
        } else if (type == PredefinedTypes.TYPE_INT_UNSIGNED_32 && isUnsigned32LiteralValue(value)) {
            return value;
        } else if (type == PredefinedTypes.TYPE_INT_UNSIGNED_16 && isUnsigned16LiteralValue(value)) {
            return value;
        } else if (type == PredefinedTypes.TYPE_INT_UNSIGNED_8 && isUnsigned8LiteralValue(value)) {
            return value;
        }
        throw errorFunc.get();
    }

    static double anyToFloat(Object sourceVal, Supplier<BError> errorFunc) {
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

    static double anyToFloatCast(Object sourceVal, Supplier<BError> errorFunc) {
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

    static boolean anyToBoolean(Object sourceVal, Supplier<BError> errorFunc) {
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

    static boolean anyToBooleanCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Boolean) {
            return (boolean) sourceVal;
        }

        throw errorFunc.get();
    }

    public static int intToByte(long sourceVal) {
        if (!TypeChecker.isByteLiteral(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_BYTE);
        }
        return ((Long) sourceVal).intValue();
    }

    public static long intToSigned32(long sourceVal) {
        if (!TypeChecker.isSigned32LiteralValue(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT_SIGNED_32);
        }
        return sourceVal;
    }

    public static long intToSigned16(long sourceVal) {
        if (!isSigned16LiteralValue(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT_SIGNED_16);
        }
        return sourceVal;
    }

    public static long intToSigned8(long sourceVal) {
        if (!isSigned8LiteralValue(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT_SIGNED_8);
        }
        return sourceVal;
    }

    public static long intToUnsigned32(long sourceVal) {
        if (!isUnsigned32LiteralValue(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT_UNSIGNED_32);
        }
        return sourceVal;
    }

    public static long intToUnsigned16(long sourceVal) {
        if (!isUnsigned16LiteralValue(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT_UNSIGNED_16);
        }
        return sourceVal;
    }

    public static long intToUnsigned8(long sourceVal) {
        if (!isUnsigned8LiteralValue(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT_UNSIGNED_8);
        }
        return sourceVal;
    }

    public static long floatToSigned32(double sourceVal) {
        return intToSigned32(floatToInt(sourceVal));
    }

    public static long floatToSigned16(double sourceVal) {
        return intToSigned16(floatToInt(sourceVal));
    }

    public static long floatToSigned8(double sourceVal) {
        return intToSigned8(floatToInt(sourceVal));
    }

    public static long floatToUnsigned32(double sourceVal) {
        return intToUnsigned32(floatToInt(sourceVal));
    }

    public static long floatToUnsigned16(double sourceVal) {
        return intToUnsigned16(floatToInt(sourceVal));
    }

    public static long floatToUnsigned8(double sourceVal) {
        return intToUnsigned8(floatToInt(sourceVal));
    }

    public static BString stringToChar(Object sourceVal) {
        if (!isCharLiteralValue(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_STRING_CHAR);
        }
        return StringUtils.fromString(Objects.toString(sourceVal));
    }

    public static BString anyToChar(Object sourceVal) {
        String value = Objects.toString(sourceVal);
        return stringToChar(value);
    }


    public static int floatToByte(double sourceVal) {
        checkIsValidFloat(sourceVal, PredefinedTypes.TYPE_BYTE);

        long intVal = Math.round(sourceVal);
        if (!TypeChecker.isByteLiteral(intVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_BYTE);
        }

        return (int) intVal;
    }

    public static long floatToInt(double sourceVal) {
        checkIsValidFloat(sourceVal, PredefinedTypes.TYPE_INT);

        if (!isFloatWithinIntRange(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT);
        }

        return (long) Math.rint(sourceVal);
    }

    private static void checkIsValidFloat(double sourceVal, Type targetType) {
        if (Double.isNaN(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(NaN, PredefinedTypes.TYPE_FLOAT, targetType);
        }

        if (Double.isInfinite(sourceVal)) {
            String value = sourceVal > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
            throw ErrorUtils.createNumericConversionError(value, PredefinedTypes.TYPE_FLOAT, targetType);
        }
    }

    static int anyToByte(Object sourceVal, Supplier<BError> errorFunc) {
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

    static int anyToByteCast(Object sourceVal, Supplier<BError> errorFunc) {
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
            return ((DecimalValue) sourceVal).stringValue(null);
        } else if (sourceVal instanceof String) {
            return (String) sourceVal;
        } else if (sourceVal == null) {
            return "()";
        }

        throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_STRING);
    }

    private static String anyToStringCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof String) {
            return (String) sourceVal;
        }

        throw errorFunc.get();
    }

    static DecimalValue anyToDecimal(Object sourceVal, Supplier<BError> errorFunc) {
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
        }
        throw errorFunc.get();
    }

    // JBallerina related casts

    static byte anyToJByteCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Byte) {
            return (Byte) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static char anyToJCharCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Character) {
            return (Character) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static short anyToJShortCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Short) {
            return (Short) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static int anyToJIntCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Integer) {
            return (Integer) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static long anyToJLongCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static float anyToJFloatCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Float) {
            return (Float) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static double anyToJDoubleCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Double) {
            return (Double) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    static boolean anyToJBooleanCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal;
        } else {
            throw errorFunc.get();
        }
    }

    public static long jFloatToBInt(float sourceVal) {
        checkIsValidFloat(sourceVal, PredefinedTypes.TYPE_INT);

        if (!isFloatWithinIntRange(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT);
        }

        return (long) Math.rint(sourceVal);
    }

    public static long jDoubleToBInt(double sourceVal) {
        checkIsValidFloat(sourceVal, PredefinedTypes.TYPE_INT);

        if (!isFloatWithinIntRange(sourceVal)) {
            throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_INT);
        }

        return (long) Math.rint(sourceVal);
    }


    private static boolean isFloatWithinIntRange(double doubleValue) {
        return doubleValue < BINT_MAX_VALUE_DOUBLE_RANGE_MAX && doubleValue > BINT_MIN_VALUE_DOUBLE_RANGE_MIN;
    }

    public static Type resolveMatchingTypeForUnion(Object value, Type type) {
        if (value instanceof ArrayValue && ((ArrayValue) value).getType().getTag() == TypeTags.ARRAY_TAG &&
                !isDeepStampingRequiredForArray(((ArrayValue) value).getType())) {
            return ((ArrayValue) value).getType();
        }

        if (value instanceof MapValue && ((MapValue) value).getType().getTag() == TypeTags.MAP_TAG &&
                !isDeepStampingRequiredForMap(((MapValue) value).getType())) {
            return ((MapValue) value).getType();
        }

        if (value == null && type.isNilable()) {
            return PredefinedTypes.TYPE_NULL;
        }

        if (checkIsLikeType(value, PredefinedTypes.TYPE_INT)) {
            return PredefinedTypes.TYPE_INT;
        }

        if (checkIsLikeType(value, PredefinedTypes.TYPE_FLOAT)) {
            return PredefinedTypes.TYPE_FLOAT;
        }

        if (checkIsLikeType(value, PredefinedTypes.TYPE_STRING)) {
            return PredefinedTypes.TYPE_STRING;
        }

        if (checkIsLikeType(value, PredefinedTypes.TYPE_BOOLEAN)) {
            return PredefinedTypes.TYPE_BOOLEAN;
        }

        if (checkIsLikeType(value, PredefinedTypes.TYPE_BYTE)) {
            return PredefinedTypes.TYPE_BYTE;
        }

        Type anydataArrayType = new BArrayType(type);
        if (checkIsLikeType(value, anydataArrayType)) {
            return anydataArrayType;
        }

        if (checkIsLikeType(value, PredefinedTypes.TYPE_XML)) {
            return PredefinedTypes.TYPE_XML;
        }

        Type anydataMapType = new BMapType(type);
        if (checkIsLikeType(value, anydataMapType)) {
            return anydataMapType;
        }

        //not possible
        return null;
    }

    private static boolean isDeepStampingRequiredForArray(Type sourceType) {
        Type elementType = ((BArrayType) sourceType).getElementType();

        if (elementType != null) {
            if (PredefinedTypes.isValueType(elementType)) {
                return false;
            } else if (elementType instanceof BArrayType) {
                return isDeepStampingRequiredForArray(elementType);
            }
            return true;
        }
        return true;
    }

    private static boolean isDeepStampingRequiredForMap(Type sourceType) {
        Type constrainedType = ((BMapType) sourceType).getConstrainedType();

        if (constrainedType != null) {
            if (PredefinedTypes.isValueType(constrainedType)) {
                return false;
            } else if (constrainedType instanceof BMapType) {
                return isDeepStampingRequiredForMap(constrainedType);
            }
            return true;
        }
        return true;
    }

}
