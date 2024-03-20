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
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.errors.ErrorReasons;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        return convert(value, targetType, new HashSet<>());
    }

    private static Object convert(Object value, Type targetType, Set<TypeValuePair> unresolvedValues) {

        if (value == null) {
            if (getTargetFromTypeDesc(targetType).isNilable()) {
                return null;
            }
            throw createError(ErrorReasons.BALLERINA_PREFIXED_CONVERSION_ERROR,
                    ErrorHelper.getErrorDetails(ErrorCodes.CANNOT_CONVERT_NIL, targetType));
        }

        Type sourceType = TypeUtils.getImpliedType(TypeChecker.getType(value));

        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            throw createError(ErrorReasons.BALLERINA_PREFIXED_CYCLIC_VALUE_REFERENCE_ERROR,
                    ErrorHelper.getErrorMessage(ErrorCodes.CYCLIC_VALUE_REFERENCE, sourceType));
        }
        unresolvedValues.add(typeValuePair);

        List<String> errors = new ArrayList<>();
        Type convertibleType = TypeConverter.getConvertibleType(value, targetType,
                null, new HashSet<>(), errors, true);
        if (convertibleType == null) {
            throw CloneUtils.createConversionError(value, targetType, errors);
        }

        Object newValue;
        Type matchingType = TypeUtils.getImpliedType(convertibleType);
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                newValue = convertMap((BMap<?, ?>) value, matchingType, convertibleType, unresolvedValues);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                newValue = convertArray((BArray) value, matchingType, convertibleType, unresolvedValues);
                break;
            case TypeTags.TABLE_TAG:
                newValue = convertTable((BTable<?, ?>) value, matchingType, convertibleType, unresolvedValues);
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
        Type referredType = TypeUtils.getImpliedType(targetType);
        if (referredType.getTag() == TypeTags.TYPEDESC_TAG) {
            return ((TypedescType) referredType).getConstraint();
        }
        return targetType;
    }

    private static Object convertMap(BMap<?, ?> map, Type targetType, Type targetRefType,
                                     Set<TypeValuePair> unresolvedValues) {
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
                return new MapValueImpl<>(targetRefType, initialValues);
            case TypeTags.RECORD_TYPE_TAG:
                RecordType recordType = (RecordType) targetType;
                Type restFieldType = recordType.getRestFieldType();
                Map<String, Type> targetTypeField = new HashMap<>();
                for (Field field : recordType.getFields().values()) {
                    targetTypeField.put(field.getFieldName(), field.getFieldType());
                }
                return convertToRecord(map, unresolvedValues, targetRefType, restFieldType,
                        targetTypeField);
            default:
                break;
        }
        // should never reach here
        throw createConversionError(map, targetType);
    }

    private static BMap<BString, Object> convertToRecord(BMap<?, ?> map, Set<TypeValuePair> unresolvedValues,
                                                         Type recordRefType, Type restFieldType,
                                                         Map<String, Type> targetTypeField) {
        Map<String, Object> valueMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object newValue = convertRecordEntry(unresolvedValues, restFieldType, targetTypeField, entry);
            valueMap.put(entry.getKey().toString(), newValue);
        }
        Optional<IntersectionType> intersectionType = ((BRecordType) TypeUtils.getImpliedType(recordRefType))
                .getIntersectionType();
        if (recordRefType.isReadOnly() && intersectionType.isPresent() && !map.getType().isReadOnly()) {
            Type mutableType = ReadOnlyUtils.getMutableType((BIntersectionType) intersectionType.get());
            return ValueCreator.createReadonlyRecordValue(mutableType.getPackage(), mutableType.getName(), valueMap);
        }
        return ValueCreator.createRecordValue(recordRefType.getPackage(), recordRefType.getName(), valueMap);
    }

    private static Object convertRecordEntry(Set<TypeValuePair> unresolvedValues,
                                             Type restFieldType, Map<String, Type> targetTypeField,
                                             Map.Entry<?, ?> entry) {
        Type fieldType = targetTypeField.getOrDefault(entry.getKey().toString(), restFieldType);
        return convert(entry.getValue(), fieldType, unresolvedValues);
    }

    private static Object convertArray(BArray array, Type targetType, Type targetRefType,
                                       Set<TypeValuePair> unresolvedValues) {
        switch (targetType.getTag()) {
            case TypeTags.ARRAY_TAG:
                ArrayType arrayType = (ArrayType) targetType;
                BListInitialValueEntry[] arrayValues = new BListInitialValueEntry[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), arrayType.getElementType(), unresolvedValues);
                    arrayValues[i] = ValueCreator.createListInitialValueEntry(newValue);
                }
                return new ArrayValueImpl(targetRefType, arrayType.getSize(), arrayValues);
            case TypeTags.TUPLE_TAG:
                TupleType tupleType = (TupleType) targetType;
                int minLen = tupleType.getTupleTypes().size();
                BListInitialValueEntry[] tupleValues = new BListInitialValueEntry[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    Type elementType = (i < minLen) ? tupleType.getTupleTypes().get(i) : tupleType.getRestType();
                    Object newValue = convert(array.get(i), elementType, unresolvedValues);
                    tupleValues[i] = ValueCreator.createListInitialValueEntry(newValue);
                }
                return new TupleValueImpl(targetRefType, tupleValues);
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
                BArray fieldNames = StringUtils.fromStringArray(tableType.getFieldNames());
                return new TableValueImpl(targetRefType, (ArrayValue) data, (ArrayValue) fieldNames);
            default:
                break;
        }
        // should never reach here
        throw createConversionError(array, targetType);
    }

    private static Object convertTable(BTable<?, ?> bTable, Type targetType,
                                       Type targetRefType, Set<TypeValuePair> unresolvedValues) {
        TableType tableType = (TableType) targetType;
        Optional<IntersectionType> intersectionType = tableType.getIntersectionType();
        if (targetRefType.isReadOnly() && intersectionType.isPresent() && !bTable.getType().isReadOnly()) {
            tableType = (TableType) ReadOnlyUtils.getMutableType((BIntersectionType) intersectionType.get());
            TableValueImpl<?, ?> tableValue = getTableValue(bTable, unresolvedValues, tableType, tableType);
            tableValue.freezeDirect();
            return tableValue;
        }
        return  getTableValue(bTable, unresolvedValues, tableType, targetRefType);
    }

    private static TableValueImpl<?, ?> getTableValue(BTable<?, ?> bTable, Set<TypeValuePair> unresolvedValues,
                                                      TableType tableType, Type targetRefType) {
        Object[] tableValues = new Object[bTable.size()];
        int count = 0;
        for (Object tableValue : bTable.values()) {
            BMap<?, ?> bMap = (BMap<?, ?>) convert(tableValue, tableType.getConstrainedType(), unresolvedValues);
            tableValues[count++] = bMap;
        }
        BArray data = ValueCreator.createArrayValue(tableValues, TypeCreator.createArrayType(
                tableType.getConstrainedType()));
        BArray fieldNames = StringUtils.fromStringArray(tableType.getFieldNames());
        return new TableValueImpl<>(targetRefType, (ArrayValue) data, (ArrayValue) fieldNames);
    }

    public static Object getConvertedStringValue(BString bString, Type targetType)
            throws BError {
        if (TypeChecker.checkIsType(bString, targetType)) {
            return bString;
        } else {
            List<Type> xmlTargetTypes = TypeConverter.getXmlTargetTypes(targetType);
            if (xmlTargetTypes.isEmpty()) {
                throw ErrorUtils.createConversionError(bString, targetType);
            }
            BXml xmlValue;
            try {
                xmlValue = TypeConverter.stringToXml(bString.getValue());
            } catch (BError e) {
                throw ErrorUtils.createConversionError(bString, targetType);
            }
            for (Type xmlTargetType : xmlTargetTypes) {
                if (TypeChecker.checkIsLikeType(xmlValue, xmlTargetType)) {
                    if (xmlTargetType.isReadOnly()) {
                        xmlValue.freezeDirect();
                    }
                    return xmlValue;
                }
            }
            throw ErrorUtils.createConversionError(bString, targetType);
        }
    }
}
