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
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.errors.ErrorReasons;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.RegExpValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BINT_MAX_VALUE_DOUBLE_RANGE_MAX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BINT_MIN_VALUE_DOUBLE_RANGE_MIN;
import static io.ballerina.runtime.internal.TypeChecker.anyToSigned16;
import static io.ballerina.runtime.internal.TypeChecker.anyToSigned32;
import static io.ballerina.runtime.internal.TypeChecker.anyToSigned8;
import static io.ballerina.runtime.internal.TypeChecker.anyToUnsigned16;
import static io.ballerina.runtime.internal.TypeChecker.anyToUnsigned32;
import static io.ballerina.runtime.internal.TypeChecker.anyToUnsigned8;
import static io.ballerina.runtime.internal.TypeChecker.checkIsLikeType;
import static io.ballerina.runtime.internal.TypeChecker.getType;
import static io.ballerina.runtime.internal.TypeChecker.isCharLiteralValue;
import static io.ballerina.runtime.internal.TypeChecker.isNumericType;
import static io.ballerina.runtime.internal.TypeChecker.isSigned16LiteralValue;
import static io.ballerina.runtime.internal.TypeChecker.isSigned32LiteralValue;
import static io.ballerina.runtime.internal.TypeChecker.isSigned8LiteralValue;
import static io.ballerina.runtime.internal.TypeChecker.isSimpleBasicType;
import static io.ballerina.runtime.internal.TypeChecker.isUnsigned16LiteralValue;
import static io.ballerina.runtime.internal.TypeChecker.isUnsigned32LiteralValue;
import static io.ballerina.runtime.internal.TypeChecker.isUnsigned8LiteralValue;
import static io.ballerina.runtime.internal.values.DecimalValue.isDecimalWithinIntRange;

/**
 * Provides utils methods for casting and conversion of values.
 *
 * @since 0.995.0
 */
public class TypeConverter {

    private static final String NAN = "NaN";
    private static final String POSITIVE_INFINITY = "Infinity";
    private static final String NEGATIVE_INFINITY = "-Infinity";

    static final byte MAX_CONVERSION_ERROR_COUNT = 20;
    static final byte MAX_DISPLAYED_SOURCE_VALUE_LENGTH = 20;
    static final String ERROR_MESSAGE_UNION_START = "{";
    static final String ERROR_MESSAGE_UNION_END = "}";
    static final String ERROR_MESSAGE_UNION_SEPARATOR = "or";

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
                throw ErrorCreator.createError(ErrorReasons.NUMBER_CONVERSION_ERROR,
                        ErrorHelper.getErrorDetails(
                                                          ErrorCodes.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                                          inputType, inputValue, targetType));
        }
    }

    public static Object castValues(Type targetType, Object inputValue) {
        switch (targetType.getTag()) {
            case TypeTags.SIGNED32_INT_TAG:
                return anyToSigned32(inputValue);
            case TypeTags.SIGNED16_INT_TAG:
                return anyToSigned16(inputValue);
            case TypeTags.SIGNED8_INT_TAG:
                return anyToSigned8(inputValue);
            case TypeTags.UNSIGNED32_INT_TAG:
                return anyToUnsigned32(inputValue);
            case TypeTags.UNSIGNED16_INT_TAG:
                return anyToUnsigned16(inputValue);
            case TypeTags.UNSIGNED8_INT_TAG:
                return anyToUnsigned8(inputValue);
            case TypeTags.INT_TAG:
                return anyToIntCast(inputValue, () ->
                        ErrorUtils.createTypeCastError(inputValue, PredefinedTypes.TYPE_INT));
            case TypeTags.DECIMAL_TAG:
                return anyToDecimalCast(inputValue, () ->
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
                return isDecimalWithinIntRange((DecimalValue) value)
                        && TypeChecker.isByteLiteral(((DecimalValue) value).value().longValue());
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
                return isDecimalWithinIntRange((DecimalValue) value);
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
                if (!isDecimalWithinIntRange((DecimalValue) value)) {
                    return false;
                }
                val = ((DecimalValue) value).value().intValue();
                break;
            default:
                return false;
        }
        return isConvertibleToIntRange(targetType, val);
    }

    public static boolean isConvertibleToIntRange(Type targetType, long val) {
        return switch (targetType.getTag()) {
            case TypeTags.INT_TAG -> true;
            case TypeTags.SIGNED32_INT_TAG -> TypeChecker.isSigned32LiteralValue(val);
            case TypeTags.SIGNED16_INT_TAG -> TypeChecker.isSigned16LiteralValue(val);
            case TypeTags.SIGNED8_INT_TAG -> TypeChecker.isSigned8LiteralValue(val);
            case TypeTags.UNSIGNED32_INT_TAG -> TypeChecker.isUnsigned32LiteralValue(val);
            case TypeTags.UNSIGNED16_INT_TAG -> TypeChecker.isUnsigned16LiteralValue(val);
            case TypeTags.UNSIGNED8_INT_TAG -> TypeChecker.isUnsigned8LiteralValue(val);
            default -> false;
        };
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

    public static Type getConvertibleType(Object inputValue, Type targetType, String varName,
                                          Set<TypeValuePair> unresolvedValues, List<String> errors,
                                          boolean allowNumericConversion) {

        int targetTypeTag = targetType.getTag();
        switch (targetTypeTag) {
            case TypeTags.UNION_TAG:
                return getConvertibleTypeInTargetUnionType(inputValue, (BUnionType) targetType, varName,
                        errors, unresolvedValues, allowNumericConversion);
            case TypeTags.ARRAY_TAG:
                if (isConvertibleToArrayType(inputValue, (BArrayType) targetType, unresolvedValues, varName, errors,
                        allowNumericConversion)) {
                    return targetType;
                }
                break;
            case TypeTags.TUPLE_TAG:
                if (isConvertibleToTupleType(inputValue, (BTupleType) targetType, unresolvedValues, varName, errors,
                        allowNumericConversion)) {
                    return targetType;
                }
                break;
            case TypeTags.RECORD_TYPE_TAG:
                if (isConvertibleToRecordType(inputValue, (BRecordType) targetType, varName,
                        unresolvedValues, errors, allowNumericConversion)) {
                    return targetType;
                }
                break;
            case TypeTags.MAP_TAG:
                if (isConvertibleToMapType(inputValue, (BMapType) targetType, unresolvedValues, varName, errors,
                        allowNumericConversion)) {
                    return targetType;
                }
                break;
            case TypeTags.TABLE_TAG:
                if (isConvertibleToTableType(inputValue, ((BTableType) targetType), unresolvedValues, varName,
                        errors, allowNumericConversion)) {
                    return targetType;
                }
                break;
            case TypeTags.JSON_TAG:
                return getConvertibleTypeForJsonType(inputValue, targetType, allowNumericConversion);
            case TypeTags.ANYDATA_TAG:
                return resolveMatchingTypeForUnion(inputValue, targetType);
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((BIntersectionType) targetType).getEffectiveType();
                return getConvertibleType(inputValue, effectiveType, varName, unresolvedValues, errors,
                        allowNumericConversion);
            case TypeTags.FINITE_TYPE_TAG:
                return getConvertibleFiniteType(inputValue, (BFiniteType) targetType, varName,
                        errors, unresolvedValues, allowNumericConversion);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                Type referredType = ((BTypeReferenceType) targetType).getReferredType();
                Type convertibleType = getConvertibleType(inputValue, referredType, varName, unresolvedValues, errors,
                        allowNumericConversion);
                return referredType == convertibleType ? targetType : convertibleType;
            case TypeTags.TYPEDESC_TAG:
                return getConvertibleType(inputValue, ((BTypedescType) targetType).getConstraint(), varName,
                        unresolvedValues, errors, allowNumericConversion);
            default:
                if (TypeChecker.checkIsLikeType(inputValue, targetType, allowNumericConversion)
                        || (TypeTags.isXMLTypeTag(targetTypeTag) && isStringConvertibleToTargetXmlType(
                                inputValue, targetType))) {
                    return targetType;
                }
        }
        return null;
    }

    private static Type getConvertibleTypeForJsonType(Object inputValue, Type targetJsonType,
                                                      boolean allowNumericConversion) {
        if (!TypeChecker.checkIsLikeType(inputValue, targetJsonType, allowNumericConversion)) {
            return null;
        }
        return resolveMatchingTypeForUnion(inputValue, targetJsonType);
    }

    private static boolean isStringConvertibleToTargetXmlType(Object inputValue, Type targetType) {
        if (TypeChecker.getType(inputValue).getTag() != TypeTags.STRING_TAG) {
            return false;
        }
        BXml xmlValue;
        try {
            xmlValue = stringToXml(((BString) inputValue).getValue());
        } catch (BError e) {
            return false;
        }
        return TypeChecker.checkIsLikeType(xmlValue, targetType);
    }

    public static Type getConvertibleTypeInTargetUnionType(Object inputValue, BUnionType targetUnionType,
                                                           String varName, List<String> errors,
                                                           Set<TypeValuePair> unresolvedValues,
                                                           boolean allowNumericConversion) {
        List<Type> memberTypes = targetUnionType.getMemberTypes();
        if (TypeChecker.isStructuredType(getType(inputValue))) {
            return getConvertibleStructuredTypeInUnion(inputValue, varName, errors,
                    unresolvedValues, allowNumericConversion, memberTypes);
        }

        for (Type memType : memberTypes) {
            if (TypeChecker.checkIsLikeType(inputValue, memType, false)) {
                return getConvertibleType(inputValue, memType, varName, unresolvedValues, errors, false);
            }
        }
        for (Type memType : memberTypes) {
            Type convertibleTypeInUnion = getConvertibleType(inputValue, memType, varName, unresolvedValues, errors,
                    allowNumericConversion);
            if (convertibleTypeInUnion != null) {
                return convertibleTypeInUnion;
            }
        }
        return null;
    }

    private static Type getConvertibleStructuredTypeInUnion(Object inputValue, String varName, List<String> errors,
                                                            Set<TypeValuePair> unresolvedValues,
                                                            boolean allowNumericConversion, List<Type> memberTypes) {
        int initialErrorCount;
        errors.add(ERROR_MESSAGE_UNION_START);
        int initialErrorListSize = errors.size();
        int currentErrorListSize = initialErrorListSize;
        for (Type memType : memberTypes) {
            initialErrorCount = errors.size();
            Type convertibleTypeInUnion = getConvertibleType(inputValue, memType, varName, unresolvedValues, errors,
                    allowNumericConversion);
            currentErrorListSize = errors.size();
            if (convertibleTypeInUnion != null) {
                errors.subList(initialErrorListSize - 1, currentErrorListSize).clear();
                return convertibleTypeInUnion;
            }
            if (initialErrorCount != currentErrorListSize) {
                errors.add(ERROR_MESSAGE_UNION_SEPARATOR);
            }
        }
        errors.remove(errors.size() - 1);
        if (initialErrorListSize != currentErrorListSize) {
            errors.add(ERROR_MESSAGE_UNION_END);
        }
        return null;
    }

    public static Type getConvertibleFiniteType(Object inputValue, BFiniteType targetFiniteType,
                                                String varName, List<String> errors,
                                                Set<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        // only the first matching type is returned.
        if (targetFiniteType.valueSpace.size() == 1) {
            Type valueType = getType(targetFiniteType.valueSpace.iterator().next());
            if (!isSimpleBasicType(valueType) && valueType.getTag() != TypeTags.NULL_TAG) {
                return getConvertibleType(inputValue, valueType, varName, unresolvedValues,
                        errors, allowNumericConversion);
            }
        }

        // first we check whether the inputValue falls into one of the members of the finite type
        Type inputValueType = TypeChecker.getType(inputValue);
        Set<Object> finiteTypeValueSpace = targetFiniteType.valueSpace;
        for (Object valueSpaceItem : finiteTypeValueSpace) {
            if (inputValue == valueSpaceItem) {
                return inputValueType;
            }
            if (TypeChecker.isFiniteTypeValue(inputValue, inputValueType, valueSpaceItem, false)) {
                return TypeChecker.getType(valueSpaceItem);
            }
        }
        if (!allowNumericConversion) {
            return null;
        }

        // if not we check whether it can be converted into a member of the finite type
        for (Object valueSpaceItem : finiteTypeValueSpace) {
            if (TypeChecker.isFiniteTypeValue(inputValue, inputValueType, valueSpaceItem, true)) {
                return TypeChecker.getType(valueSpaceItem);
            }
        }
        return null;
    }

    private static boolean isConvertibleToRecordType(Object sourceValue, BRecordType targetType, String varName,
                                                     Set<TypeValuePair> unresolvedValues,
                                                     List<String> errors, boolean allowNumericConversion) {
        if (!(sourceValue instanceof MapValueImpl sourceMapValueImpl)) {
            return false;
        }

        TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            return true;
        }
        unresolvedValues.add(typeValuePair);

        Map<String, Type> targetFieldTypes = new HashMap<>();
        Type restFieldType = targetType.restFieldType;
        boolean returnVal = true;

        for (Map.Entry<String, Field> field : targetType.getFields().entrySet()) {
            targetFieldTypes.put(field.getKey(), field.getValue().getFieldType());
        }

        for (Map.Entry targetTypeEntry : targetFieldTypes.entrySet()) {
            String fieldName = targetTypeEntry.getKey().toString();
            String fieldNameLong = getLongFieldName(varName, fieldName);

            if (sourceMapValueImpl.containsKey(StringUtils.fromString(fieldName))) {
                continue;
            }
            Field targetField = targetType.getFields().get(fieldName);
            if (SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.REQUIRED)) {
                addErrorMessage(0, errors, "missing required field '" + fieldNameLong + "' of type '" +
                        targetField.getFieldType().toString() + "' in record '" + targetType + "'");
                if (errors.size() >= MAX_CONVERSION_ERROR_COUNT + 1) {
                    return false;
                }
                returnVal = false;
            }
        }

        for (Object object : sourceMapValueImpl.entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldName = valueEntry.getKey().toString();
            String fieldNameLong = getLongFieldName(varName, fieldName);
            int initialErrorCount = errors.size();

            if (targetFieldTypes.containsKey(fieldName)) {
                if (getConvertibleType(valueEntry.getValue(), targetFieldTypes.get(fieldName),
                        fieldNameLong, unresolvedValues, errors, allowNumericConversion) ==  null) {
                    addErrorMessage(errors.size() - initialErrorCount, errors, "field '" +
                            fieldNameLong + "' in record '" + targetType + "' should be of type '" +
                            targetFieldTypes.get(fieldName) + "', found '" +
                            getShortSourceValue(valueEntry.getValue()) + "'");
                    returnVal = false;
                }
            } else if (!targetType.sealed) {
                if (getConvertibleType(valueEntry.getValue(), restFieldType, fieldNameLong,
                        unresolvedValues, errors, allowNumericConversion) ==  null) {
                    addErrorMessage(errors.size() - initialErrorCount, errors, "value of field '" +
                            valueEntry.getKey() + "' adding to the record '" +
                            targetType + "' should be of type '" + restFieldType + "', found '" +
                            getShortSourceValue(valueEntry.getValue()) + "'");
                    returnVal = false;
                }
            } else {
                addErrorMessage(0 , errors, "field '" + fieldNameLong + "' cannot be added to the closed record '" +
                        targetType + "'");
                returnVal = false;
            }

            if ((!returnVal) && (errors.size() >= MAX_CONVERSION_ERROR_COUNT + 1)) {
                unresolvedValues.remove(typeValuePair);
                return false;
            }
        }
        if (!returnVal) {
            unresolvedValues.remove(typeValuePair);
        }
        return returnVal;
    }

    protected static String getShortSourceValue(Object sourceValue) {
        if (sourceValue == null) {
            return "()";
        }
        String sourceValueName = sourceValue.toString();
        if (TypeChecker.getType(sourceValue) == TYPE_STRING) {
            sourceValueName = "\"" + sourceValueName + "\"";
        }
        if (sourceValueName.length() > MAX_DISPLAYED_SOURCE_VALUE_LENGTH) {
            sourceValueName = sourceValueName.substring(0, MAX_DISPLAYED_SOURCE_VALUE_LENGTH).concat("...");
        }
        return sourceValueName;
    }

    protected static String getLongFieldName(String varName, String fieldName) {
        if (varName == null) {
            return fieldName;
        } else {
            return varName + "." + fieldName;
        }
    }

    private static void addErrorMessage(int errorCountDifference, List<String> errors, String errorMessage) {
        if ((errors.size() <= MAX_CONVERSION_ERROR_COUNT) && (errorCountDifference == 0)) {
            errors.add(errorMessage);
        }
    }

    private static boolean isConvertibleToTableType(Object sourceValue, BTableType tableType,
                                                    Set<TypeValuePair> unresolvedValues, String varName,
                                                    List<String> errors, boolean allowNumericConversion) {
        int initialErrorCount;
        String elementIndex;
        Type convertibleType;
        boolean returnVal = true;
        Type constrainedType = tableType.getConstrainedType();
        int sourceTypeReferredTypeTag = TypeUtils.getImpliedType(TypeChecker.getType(sourceValue)).getTag();
        switch (sourceTypeReferredTypeTag) {
            case TypeTags.TABLE_TAG:
                Collection<?> bTableValues = ((BTable<?, ?>) sourceValue).values();
                Iterator<?> bTableIterator = bTableValues.iterator();
                for (int i = 0; i < bTableValues.size(); i++) {
                    initialErrorCount = errors.size();
                    elementIndex = getElementIndex(varName, i);
                    Object bTableValue = bTableIterator.next();
                    convertibleType = getConvertibleType(bTableValue, constrainedType, elementIndex,
                            unresolvedValues, errors, allowNumericConversion);
                    if (convertibleType == null) {
                        addErrorMessage(errors.size() - initialErrorCount, errors, "table element '" +
                                elementIndex + "' should be of type '" + constrainedType + "', found '" +
                                getShortSourceValue(bTableValue) + "'");
                        if (errors.size() >= MAX_CONVERSION_ERROR_COUNT + 1) {
                            return false;
                        }
                        returnVal = false;
                    }
                }
                return returnVal;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                BArray array = (BArray) sourceValue;
                for (int i = 0; i < array.size(); i++) {
                    initialErrorCount = errors.size();
                    elementIndex = getElementIndex(varName, i);
                    convertibleType = getConvertibleType(array.get(i), constrainedType, elementIndex,
                            unresolvedValues, errors, allowNumericConversion);
                    if (convertibleType == null) {
                        addErrorMessage(errors.size() - initialErrorCount, errors, "list element '" +
                                elementIndex + "' should be of type '" + constrainedType + "', found '" +
                                getShortSourceValue(array.get(i)) + "'");
                        if (errors.size() >= MAX_CONVERSION_ERROR_COUNT + 1) {
                            return false;
                        }
                        returnVal = false;
                    }
                }
                return returnVal;
            default:
                return false;

        }
    }

    private static boolean isConvertibleToMapType(Object sourceValue, BMapType targetType,
                                                  Set<TypeValuePair> unresolvedValues, String varName,
                                                  List<String> errors, boolean allowNumericConversion) {
        if (!(sourceValue instanceof MapValueImpl)) {
            return false;
        }

        boolean returnVal = true;
        for (Object object : ((MapValueImpl) sourceValue).entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldNameLong = getLongFieldName(varName, valueEntry.getKey().toString());
            int initialErrorCount = errors.size();
            if (getConvertibleType(valueEntry.getValue(), targetType.getConstrainedType(), fieldNameLong,
                    unresolvedValues, errors, allowNumericConversion) ==  null) {
                addErrorMessage(errors.size() - initialErrorCount, errors, "map field '" +
                        fieldNameLong + "' should be of type '" + targetType.getConstrainedType() + "', found '"
                        + getShortSourceValue(valueEntry.getValue()) + "'");
                if (errors.size() >= MAX_CONVERSION_ERROR_COUNT + 1) {
                    return false;
                }
                returnVal = false;
            }
        }
        return returnVal;
    }

    private static boolean isConvertibleToArrayType(Object sourceValue, BArrayType targetType,
                                                    Set<TypeValuePair> unresolvedValues, String varName,
                                                    List<String> errors, boolean allowNumericConversion) {
        if (!(sourceValue instanceof ArrayValue source)) {
            return false;
        }
        int targetSize = targetType.getSize();
        long sourceSize = source.getLength();
        if (targetType.getState() == ArrayType.ArrayState.CLOSED && targetSize < sourceSize) {
            addErrorMessage(0, errors, "element count exceeds the target array size '" + targetSize + "'");
            return false;
        }
        Type targetTypeElementType = TypeUtils.getImpliedType(targetType.getElementType());
        Type sourceType = source.getType();
        if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            Type sourceElementType = TypeUtils.getImpliedType(((BArrayType) sourceType).getElementType());
            if (isNumericType(sourceElementType) && isNumericType(targetTypeElementType)) {
                return true;
            }
        }
        if (!TypeChecker.hasFillerValue(targetType) && sourceSize < targetSize) {
            addErrorMessage(0, errors, "array cannot be expanded to size '" + targetSize + "' because, the target " +
                    "type '" + targetType + "' does not have a filler value");
            return false;
        }

        int initialErrorCount;
        String elementIndex;
        boolean returnVal = true;
        Type convertibleType;
        for (int i = 0; i < source.size(); i++) {
            initialErrorCount = errors.size();
            elementIndex = getElementIndex(varName, i);
            convertibleType = getConvertibleType(source.get(i), targetTypeElementType, elementIndex,
                    unresolvedValues, errors, allowNumericConversion);
            if (convertibleType == null) {
                addErrorMessage(errors.size() - initialErrorCount, errors, "array element '" +
                        elementIndex + "' should be of type '" + targetTypeElementType + "', found '" +
                        getShortSourceValue(source.get(i)) + "'");
                if (errors.size() >= MAX_CONVERSION_ERROR_COUNT + 1) {
                    return false;
                }
                returnVal = false;
            }
        }
        return returnVal;
    }

    private static boolean isConvertibleToTupleType(Object sourceValue, BTupleType targetType,
                                                    Set<TypeValuePair> unresolvedValues, String varName,
                                                    List<String> errors, boolean allowNumericConversion) {
        if (!(sourceValue instanceof ArrayValue source)) {
            return false;
        }

        List<Type> targetTypes = targetType.getTupleTypes();
        int sourceTypeSize = source.size();
        int targetTypeSize = targetTypes.size();
        Type targetRestType = targetType.getRestType();

        if (sourceTypeSize < targetTypeSize || (targetRestType == null && sourceTypeSize > targetTypeSize)) {
            return false;
        }

        int initialErrorCount;
        String elementIndex;
        boolean returnVal = true;
        Type convertibleType;
        for (int i = 0; i < targetTypeSize; i++) {
            initialErrorCount = errors.size();
            elementIndex = getElementIndex(varName, i);
            convertibleType = getConvertibleType(source.getRefValue(i), targetTypes.get(i), elementIndex,
                    unresolvedValues, errors, allowNumericConversion);
            if (convertibleType == null) {
                addErrorMessage(errors.size() - initialErrorCount, errors, "tuple element '" +
                        elementIndex + "' should be of type '" + targetTypes.get(i).toString() + "', found '" +
                        getShortSourceValue(source.getRefValue(i)) + "'");
                if (errors.size() >= MAX_CONVERSION_ERROR_COUNT + 1) {
                    return false;
                }
                returnVal = false;
            }
        }

        for (int i = targetTypeSize; i < sourceTypeSize; i++) {
            initialErrorCount = errors.size();
            elementIndex = getElementIndex(varName, i);
            convertibleType = getConvertibleType(source.getRefValue(i), targetRestType, elementIndex,
                    unresolvedValues, errors, allowNumericConversion);
            if (convertibleType == null) {
                addErrorMessage(errors.size() - initialErrorCount, errors, "tuple element '" +
                        elementIndex + "' should be of type '" + targetRestType + "', found '" +
                        getShortSourceValue(source.getRefValue(i)) + "'");
                if (errors.size() >= MAX_CONVERSION_ERROR_COUNT + 1) {
                    return false;
                }
                returnVal = false;
            }
        }
        return returnVal;
    }

    private static String getElementIndex(String varName, int index) {
        if (varName == null) {
            return "[" + index + "]";
        } else {
            return varName + "[" + index + "]";
        }
    }

    static long anyToInt(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return l;
        } else if (sourceVal instanceof Double d) {
            return floatToInt(d);
        } else if (sourceVal instanceof Integer i) {
            return i.longValue();
        } else if (sourceVal instanceof Boolean b) {
            return b ? 1 : 0;
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue.intValue();
        } else if (sourceVal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw errorFunc.get();
            }
        }

        throw errorFunc.get();
    }

    static long anyToIntCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return l;
        } else if (sourceVal instanceof Double d) {
            return floatToInt(d);
        } else if (sourceVal instanceof Integer i) {
            return i.longValue();
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue.intValue();
        } else {
            throw errorFunc.get();
        }
    }

    static long anyToIntSubTypeCast(Object sourceVal, Type type, Supplier<BError> errorFunc) {
        long value = anyToIntCast(sourceVal, errorFunc);
        if (type == PredefinedTypes.TYPE_INT_SIGNED_32 && isSigned32LiteralValue(value)) {
            return value;
        }
        if ((type == PredefinedTypes.TYPE_INT_SIGNED_16 && isSigned16LiteralValue(value)) ||
                (type == PredefinedTypes.TYPE_INT_SIGNED_8 && isSigned8LiteralValue(value)) ||
                (type == PredefinedTypes.TYPE_INT_UNSIGNED_32 && isUnsigned32LiteralValue(value)) ||
                (type == PredefinedTypes.TYPE_INT_UNSIGNED_16 && isUnsigned16LiteralValue(value)) ||
                (type == PredefinedTypes.TYPE_INT_UNSIGNED_8 && isUnsigned8LiteralValue(value))) {
            return value;
        }
        throw errorFunc.get();
    }

    static double anyToFloat(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return l.doubleValue();
        } else if (sourceVal instanceof Double d) {
            return d;
        } else if (sourceVal instanceof Integer i) {
            return i.floatValue();
        } else if (sourceVal instanceof Boolean b) {
            return b ? 1.0 : 0.0;
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue.floatValue();
        } else if (sourceVal instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw errorFunc.get();
            }
        }

        throw errorFunc.get();
    }

    static double anyToFloatCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return l.doubleValue();
        } else if (sourceVal instanceof Double d) {
            return d;
        } else if (sourceVal instanceof Integer i) {
            return i.floatValue();
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue.floatValue();
        } else {
            throw errorFunc.get();
        }
    }

    static boolean anyToBoolean(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return l != 0;
        } else if (sourceVal instanceof Double d) {
            return d != 0.0;
        } else if (sourceVal instanceof Integer i) {
            return i != 0;
        } else if (sourceVal instanceof Boolean b) {
            return b;
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue.booleanValue();
        } else if (sourceVal instanceof String s) {
            try {
                return Boolean.parseBoolean(s);
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

    public static Long stringToInt(String value) throws NumberFormatException {
        return Long.parseLong(value);
    }

    public static int stringToByte(String value) throws NumberFormatException, BError {
        int byteValue = Integer.parseInt(value);
        return intToByte(byteValue);
    }

    public static Double stringToFloat(String value) throws NumberFormatException {
        if (hasFloatOrDecimalLiteralSuffix(value)) {
            throw new NumberFormatException();
        }
        return Double.parseDouble(value);
    }

    public static boolean hasFloatOrDecimalLiteralSuffix(String value) {
        int length = value.length();
        if (length == 0) {
            return false;
        }

        switch (value.charAt(length - 1)) {
            case 'F':
            case 'f':
            case 'D':
            case 'd':
                return true;
            default:
                return false;
        }
    }

    public static Boolean stringToBoolean(String value) throws NumberFormatException {
        if ("true".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value)) {
            return false;
        }
        throw new NumberFormatException();
    }

    public static BDecimal stringToDecimal(String value) throws NumberFormatException {
        return new DecimalValue(value);
    }

    public static BXml stringToXml(String value) throws BError {
        BXml item = XmlUtils.parse("<root>" + value + "</root>");
        return item.children();
    }

    public static RegExpValue stringToRegExp(String value) throws BError {
        return RegExpFactory.parse(value);
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
            throw ErrorUtils.createNumericConversionError(NAN, PredefinedTypes.TYPE_FLOAT, targetType);
        }

        if (Double.isInfinite(sourceVal)) {
            String value = sourceVal > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
            throw ErrorUtils.createNumericConversionError(value, PredefinedTypes.TYPE_FLOAT, targetType);
        }
    }

    static int anyToByte(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return intToByte(l);
        } else if (sourceVal instanceof Double d) {
            return floatToByte(d);
        } else if (sourceVal instanceof Integer i) {
            return i;
        } else if (sourceVal instanceof Boolean b) {
            return (b ? 1 : 0);
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue.byteValue();
        } else if (sourceVal instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw errorFunc.get();
            }
        }

        throw errorFunc.get();
    }

    static int anyToByteCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return intToByte(l);
        } else if (sourceVal instanceof Byte b) {
            return b.intValue();
        } else if (sourceVal instanceof Double d) {
            return floatToByte(d);
        } else if (sourceVal instanceof Integer i) {
            return i;
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue.byteValue();
        } else {
            throw errorFunc.get();
        }

    }

    private static String anyToString(Object sourceVal) {
        if (sourceVal instanceof Long l) {
            return Long.toString(l);
        } else if (sourceVal instanceof Double d) {
            return Double.toString(d);
        } else if (sourceVal instanceof Integer i) {
            return Long.toString(i);
        } else if (sourceVal instanceof Boolean b) {
            return Boolean.toString(b);
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue.stringValue(null);
        } else if (sourceVal instanceof String s) {
            return s;
        } else if (sourceVal == null) {
            return "()";
        }

        throw ErrorUtils.createNumericConversionError(sourceVal, PredefinedTypes.TYPE_STRING);
    }

    private static String anyToStringCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof String s) {
            return s;
        }

        throw errorFunc.get();
    }

    static DecimalValue anyToDecimal(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return DecimalValue.valueOf(l);
        } else if (sourceVal instanceof Double d) {
            return DecimalValue.valueOf(d);
        } else if (sourceVal instanceof Integer i) {
            return DecimalValue.valueOf(i);
        } else if (sourceVal instanceof Boolean b) {
            return DecimalValue.valueOf(b);
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue;
        }
        throw errorFunc.get();
    }

    static DecimalValue anyToDecimalCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long l) {
            return DecimalValue.valueOf(l);
        } else if (sourceVal instanceof Double d) {
            return DecimalValue.valueOf(d);
        } else if (sourceVal instanceof Integer i) {
            return DecimalValue.valueOf(i);
        } else if (sourceVal instanceof DecimalValue decimalValue) {
            return decimalValue;
        } else if (sourceVal instanceof String s) {
            return new DecimalValue(s);
        }
        throw errorFunc.get();
    }

    // JBallerina related casts

    static byte anyToJByteCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Byte bVal) {
            return bVal;
        } else {
            throw errorFunc.get();
        }
    }

    static char anyToJCharCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Character cVal) {
            return cVal;
        } else {
            throw errorFunc.get();
        }
    }

    static short anyToJShortCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Short sVal) {
            return sVal;
        } else {
            throw errorFunc.get();
        }
    }

    static int anyToJIntCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Integer iVal) {
            return iVal;
        } else {
            throw errorFunc.get();
        }
    }

    static long anyToJLongCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Long lVal) {
            return lVal;
        } else {
            throw errorFunc.get();
        }
    }

    static float anyToJFloatCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Float fVal) {
            return fVal;
        } else {
            throw errorFunc.get();
        }
    }

    static double anyToJDoubleCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Double dVal) {
            return dVal;
        } else {
            throw errorFunc.get();
        }
    }

    static boolean anyToJBooleanCast(Object sourceVal, Supplier<BError> errorFunc) {
        if (sourceVal instanceof Boolean bVal) {
            return bVal;
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

    public static Type resolveMatchingTypeForUnion(Object value, Type targetUnionType) {
        if (value == null && targetUnionType.isNilable()) {
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

        if (checkIsLikeType(value, PredefinedTypes.TYPE_DECIMAL)) {
            return PredefinedTypes.TYPE_DECIMAL;
        }

        boolean readOnlyTargetType = targetUnionType.isReadOnly();

        if (checkIsLikeType(value, PredefinedTypes.TYPE_XML)) {
            return readOnlyTargetType ? PredefinedTypes.TYPE_READONLY_XML : PredefinedTypes.TYPE_XML;
        }

        Type targetTypeArrayType = new BArrayType(targetUnionType, readOnlyTargetType);
        if (checkIsLikeType(value, targetTypeArrayType)) {
            return targetTypeArrayType;
        }

        Type targetTypeMapType = new BMapType(targetUnionType, readOnlyTargetType);
        if (checkIsLikeType(value, targetTypeMapType)) {
            return targetTypeMapType;
        }

        Type targetTypeMapTableType = new BTableType(targetTypeMapType, readOnlyTargetType);
        if (checkIsLikeType(value, targetTypeMapTableType)) {
            return targetTypeMapTableType;
        }

        //not possible
        return null;
    }

    private TypeConverter() {
    }

    static List<Type> getXmlTargetTypes(Type targetType) {
        List<Type> xmlTargetTypes = new ArrayList<>();
        return switch (targetType.getTag()) {
            case TypeTags.XML_TAG, TypeTags.XML_PI_TAG, TypeTags.XML_COMMENT_TAG, TypeTags.XML_ELEMENT_TAG,
                    TypeTags.XML_TEXT_TAG -> {
                xmlTargetTypes.add(targetType);
                yield xmlTargetTypes;
            }
            case TypeTags.TYPE_REFERENCED_TYPE_TAG -> {
                xmlTargetTypes.addAll(getXmlTargetTypes(((ReferenceType) targetType).getReferredType()));
                yield xmlTargetTypes;
            }
            case TypeTags.INTERSECTION_TAG -> {
                xmlTargetTypes.addAll(getXmlTargetTypes(((IntersectionType) targetType).getEffectiveType()));
                yield xmlTargetTypes;
            }
            case TypeTags.UNION_TAG -> {
                for (Type memberType : ((UnionType) targetType).getMemberTypes()) {
                    xmlTargetTypes.addAll(getXmlTargetTypes(memberType));
                }
                yield xmlTargetTypes;
            }
            case TypeTags.FINITE_TYPE_TAG -> {
                for (Object o : ((FiniteType) targetType).getValueSpace()) {
                    if (TypeTags.isXMLTypeTag(TypeChecker.getType(o).getTag())) {
                        xmlTargetTypes.add(targetType);
                        yield xmlTargetTypes;
                    }
                }
                yield xmlTargetTypes;
            }
            default -> xmlTargetTypes;
        };
    }
}
