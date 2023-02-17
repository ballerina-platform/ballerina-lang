/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.AnydataType;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypedescType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.ErrorUtils.createConversionError;

/**
 * Responsible for performing the conversion of values between subtypes of {@link AnydataType} at runtime.
 *
 * @since 2201.5.0
 */
public class ValueConverter {

    private ValueConverter() {}

    public static Object convert(Object value, BTypedesc t) {
        return convert(value, t.getDescribingType());
    }

    public static Object convert(Object value, Type targetType) {
        try {
            return convert(value, targetType, new HashSet<>());
        } catch (BallerinaException e) {
            throw createError(BallerinaErrorReasons.BALLERINA_PREFIXED_CONVERSION_ERROR,
                    StringUtils.fromString(e.getDetail()));
        }
    }

    private static Object convert(Object value, Type targetType, Set<TypeValuePair> unresolvedValues) {

        if (value == null) {
            if (getTargetFromTypeDesc(targetType).isNilable()) {
                return null;
            }
            throw createError(BallerinaErrorReasons.BALLERINA_PREFIXED_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }

        Type sourceType = TypeUtils.getReferredType(TypeChecker.getType(value));

        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            throw createError(BallerinaErrorReasons.BALLERINA_PREFIXED_CYCLIC_VALUE_REFERENCE_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, sourceType));
        }
        unresolvedValues.add(typeValuePair);

        List<String> errors = new ArrayList<>();
        Type convertibleType = TypeConverter.getConvertibleType(value, targetType,
                null, new HashSet<>(), errors, true);
        if (convertibleType == null) {
            throw CloneUtils.createConversionError(value, targetType, errors);
        }

        Object newValue;
        Type matchingType = TypeUtils.getReferredType(convertibleType);
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                newValue = convertMap((BMap<?, ?>) value, matchingType, unresolvedValues);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                newValue = convertArray((BArray) value, matchingType, unresolvedValues);
                break;
            case TypeTags.TABLE_TAG:
                newValue = convertTable((BTable<?, ?>) value, matchingType, unresolvedValues);
                break;
            default:
                if (TypeChecker.isRegExpType(targetType) && matchingType.getTag() == TypeTags.STRING_TAG) {
                    try {
                        newValue = RegExpFactory.parse(((BString) value).getValue());
                        break;
                    } catch (BError e) {
                        throw createConversionError(value, targetType, e.getMessage());
                    }
                }

                if (TypeTags.isXMLTypeTag(matchingType.getTag())) {
                    String xmlString = value.toString();
                    try {
                        newValue = BalStringUtils.parseXmlExpressionStringValue(xmlString);
                    } catch (BError e) {
                        throw createConversionError(value, targetType);
                    }
                    if (matchingType.isReadOnly()) {
                        newValue = CloneUtils.cloneReadOnly(newValue);
                    }
                    break;
                }

                // can't put the below, above handling xml because for selectively mutable types when the provided value
                // is readonly, if the target type is non readonly, checkIsType provides true, but we can't just clone
                // the value as it should be non readonly.
                if (TypeChecker.checkIsType(value, matchingType)) {
                    newValue = CloneUtils.cloneValue(value);
                    break;
                }

                // handle primitive values
                if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
                    // has to be a numeric conversion.
                    newValue = TypeConverter.convertValues(matchingType, value);
                    break;
                }
                // should never reach here
                throw createConversionError(value, targetType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Type getTargetFromTypeDesc(Type targetType) {
        Type referredType = TypeUtils.getReferredType(targetType);
        if (referredType.getTag() == TypeTags.TYPEDESC_TAG) {
            return ((TypedescType) referredType).getConstraint();
        }
        return targetType;
    }

    private static Object convertMap(BMap<?, ?> map, Type targetType, Set<TypeValuePair> unresolvedValues) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                BMapInitialValueEntry[] initialValues = new BMapInitialValueEntry[map.entrySet().size()];
                Type constraintType = ((MapType) targetType).getConstrainedType();
                int count = 0;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object newValue = convert(entry.getValue(), constraintType, unresolvedValues);
                    initialValues[count] = ValueCreator
                            .createKeyFieldEntry(StringUtils.fromString(entry.getKey().toString()), newValue);
                    count++;
                }
                return ValueCreator.createMapValue((MapType) targetType, initialValues);
            case TypeTags.RECORD_TYPE_TAG:
                RecordType recordType = (RecordType) targetType;
                Type restFieldType = recordType.getRestFieldType();
                Map<String, Type> targetTypeField = new HashMap<>();
                for (Field field : recordType.getFields().values()) {
                    targetTypeField.put(field.getFieldName(), field.getFieldType());
                }
                return convertToRecord(map, unresolvedValues, recordType, restFieldType,
                        targetTypeField);
            case TypeTags.INTERSECTION_TAG:
                return convertMap(map, ((IntersectionType) targetType).getEffectiveType(), unresolvedValues);
            default:
                break;
        }
        // should never reach here
        throw createConversionError(map, targetType);
    }

    private static BMap<BString, Object> convertToRecord(BMap<?, ?> map, Set<TypeValuePair> unresolvedValues,
                                                         RecordType recordType,
                                                         Type restFieldType, Map<String, Type> targetTypeField) {
        Map<String, Object> valueMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object newValue = convertRecordEntry(unresolvedValues, restFieldType, targetTypeField, entry);
            valueMap.put(entry.getKey().toString(), newValue);
        }
        return ValueCreator.createRecordValue(recordType.getPackage(), recordType.getName(), valueMap);
    }

    private static Object convertRecordEntry(Set<TypeValuePair> unresolvedValues,
                                             Type restFieldType, Map<String, Type> targetTypeField,
                                             Map.Entry<?, ?> entry) {
        Type fieldType = targetTypeField.getOrDefault(entry.getKey().toString(), restFieldType);
        return convert(entry.getValue(), fieldType, unresolvedValues);
    }

    private static Object convertArray(BArray array, Type targetType, Set<TypeValuePair> unresolvedValues) {
        switch (targetType.getTag()) {
            case TypeTags.ARRAY_TAG:
                ArrayType arrayType = (ArrayType) targetType;
                BListInitialValueEntry[] arrayValues = new BListInitialValueEntry[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), arrayType.getElementType(), unresolvedValues);
                    arrayValues[i] = ValueCreator.createListInitialValueEntry(newValue);
                }
                return ValueCreator.createArrayValue(arrayType, arrayValues);
            case TypeTags.TUPLE_TAG:
                TupleType tupleType = (TupleType) targetType;
                int minLen = tupleType.getTupleTypes().size();
                BListInitialValueEntry[] tupleValues = new BListInitialValueEntry[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    Type elementType = (i < minLen) ? tupleType.getTupleTypes().get(i) : tupleType.getRestType();
                    Object newValue = convert(array.get(i), elementType, unresolvedValues);
                    tupleValues[i] = ValueCreator.createListInitialValueEntry(newValue);
                }
                return ValueCreator.createTupleValue(tupleType, array.size(), tupleValues);
            case TypeTags.TABLE_TAG:
                TableType tableType = (TableType) targetType;
                Object[] tableValues = new Object[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    BMap<?, ?> bMap = (BMap<?, ?>) convert(array.get(i), tableType.getConstrainedType(),
                            unresolvedValues);
                    tableValues[i] = bMap;
                }
                BArray data = ValueCreator
                        .createArrayValue(tableValues, TypeCreator.createArrayType(tableType.getConstrainedType()));
                BArray fieldNames;
                fieldNames = StringUtils.fromStringArray(tableType.getFieldNames());
                return ValueCreator.createTableValue(tableType, data, fieldNames);
            case TypeTags.INTERSECTION_TAG:
                return convertArray(array, ((IntersectionType) targetType).getEffectiveType(),
                        unresolvedValues);
            default:
                break;
        }
        // should never reach here
        throw createConversionError(array, targetType);
    }

    private static Object convertTable(BTable<?, ?> bTable, Type targetType,
                                       Set<TypeValuePair> unresolvedValues) {
        TableType tableType = (TableType) targetType;
        Object[] tableValues = new Object[bTable.size()];
        int count = 0;
        for (Object tableValue : bTable.values()) {
            BMap<?, ?> bMap = (BMap<?, ?>) convert(tableValue, tableType.getConstrainedType(), unresolvedValues);
            tableValues[count++] = bMap;
        }
        BArray data = ValueCreator.createArrayValue(tableValues,
                TypeCreator.createArrayType(tableType.getConstrainedType()));
        BArray fieldNames;
        fieldNames = StringUtils.fromStringArray(tableType.getFieldNames());
        return ValueCreator.createTableValue(tableType, data, fieldNames);
    }
}
