/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.value;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.ReferenceType;
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
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.ErrorUtils.createConversionError;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR;

/**
 * Extern function lang.values:cloneWithType.
 *
 * @since 2.0
 */
public class CloneWithType {

    private CloneWithType() {}

    public static Object cloneWithType(Object v, BTypedesc t) {
        return convert(t.getDescribingType(), v, t);
    }

    public static Object convert(Type convertType, Object inputValue) {
        return convert(convertType, inputValue, null);
    }

    public static Object convert(Type convertType, Object inputValue, BTypedesc t) {
        try {
            return convert(inputValue, convertType, new ArrayList<>(), t);
        } catch (BError e) {
            return e;
        } catch (BallerinaException e) {
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR, StringUtils.fromString(e.getDetail()));
        }
    }

    private static Object convert(Object value, Type targetType, List<TypeValuePair> unresolvedValues,
                                  BTypedesc t) {
        if (value == null) {
            if (getTargetFromTypeDesc(targetType).isNilable()) {
                return null;
            }
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }

        List<String> errors = new ArrayList<>();
        Type convertibleType = TypeConverter.getConvertibleType(value, targetType, null, false,
                new ArrayList<>(), errors, true);

        Type sourceType = TypeChecker.getType(value);
        if (convertibleType == null) {
            throw CloneUtils.createConversionError(value, targetType, errors);
        }

        // handle primitive values
        if (TypeUtils.getReferredType(sourceType).getTag() <= TypeTags.BOOLEAN_TAG) {
            if (TypeChecker.checkIsType(value, convertibleType)) {
                return value;
            } else {
                // Has to be a numeric conversion.
                return TypeConverter.convertValues(convertibleType, value);
            }
        }

        return convert((BRefValue) value, convertibleType, unresolvedValues, t);
    }

    private static Type getTargetFromTypeDesc(Type targetType) {
        Type referredType = TypeUtils.getReferredType(targetType);
        if (referredType.getTag() == TypeTags.TYPEDESC_TAG) {
            return ((TypedescType) referredType).getConstraint();
        }
        return targetType;
    }

    private static Object convert(BRefValue value, Type targetType, List<TypeValuePair> unresolvedValues,
                                  BTypedesc t) {
        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR.getValue(),
                                         BLangExceptionHelper
                                                 .getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, value.getType())
                                                 .getValue());
        }

        unresolvedValues.add(typeValuePair);

        Object newValue = getConvertedObject(value, value.getType(), targetType, unresolvedValues, t);
        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object getConvertedObject(BRefValue value, Type sourceType, Type targetType,
                                             List<TypeValuePair> unresolvedValues, BTypedesc t) {
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return convertMap((BMap<?, ?>) value, targetType, unresolvedValues, t);
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                return convertArray((BArray) value, targetType, unresolvedValues, t);
            case TypeTags.TABLE_TAG:
                return convertTable((BTable<?, ?>) value, targetType, unresolvedValues, t);
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
            case TypeTags.ERROR_TAG:
                return value.copy(new HashMap<>());
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return getConvertedObject(value, ((ReferenceType) sourceType).getReferredType(), targetType,
                        unresolvedValues, t);
            default:
                // should never reach here
                throw createConversionError(value, targetType);
        }
    }

    private static Object convertMap(BMap<?, ?> map, Type targetType, List<TypeValuePair> unresolvedValues,
                                     BTypedesc t) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                BMapInitialValueEntry[] initialValues = new BMapInitialValueEntry[map.entrySet().size()];
                Type constraintType = ((MapType) targetType).getConstrainedType();
                int count = 0;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object newValue = convert(entry.getValue(), constraintType, unresolvedValues, t);
                    initialValues[count++] = ValueCreator
                            .createKeyFieldEntry(StringUtils.fromString(entry.getKey().toString()), newValue);
                }
                return ValueCreator.createMapValue((MapType) targetType, initialValues);
            case TypeTags.RECORD_TYPE_TAG:
                RecordType recordType = (RecordType) targetType;

                Type restFieldType = recordType.getRestFieldType();
                Map<String, Type> targetTypeField = new HashMap<>();
                for (Field field : recordType.getFields().values()) {
                    targetTypeField.put(field.getFieldName(), field.getFieldType());
                }
                if (t != null && t.getDescribingType() == targetType) {
                    return convertToRecordWithTypeDesc(map, unresolvedValues, t, restFieldType,
                                                       targetTypeField);
                } else {
                    return convertToRecord(map, unresolvedValues, t, recordType, restFieldType,
                                           targetTypeField);
                }
            case TypeTags.JSON_TAG:
                Type matchingType = TypeConverter.resolveMatchingTypeForUnion(map, targetType);
                return convert(map, matchingType, unresolvedValues, t);
            case TypeTags.INTERSECTION_TAG:
                return convertMap(map, ((IntersectionType) targetType).getEffectiveType(), unresolvedValues, t);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return convertMap(map, ((ReferenceType) targetType).getReferredType(), unresolvedValues, t);
            default:
                break;
        }
        // should never reach here
        throw createConversionError(map, targetType);
    }

    private static BMap<BString, Object> convertToRecord(BMap<?, ?> map, List<TypeValuePair> unresolvedValues,
                                                         BTypedesc t, RecordType recordType,
                                                         Type restFieldType, Map<String, Type> targetTypeField) {
        Map<String, Object> valueMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object newValue = convertRecordEntry(unresolvedValues, t, restFieldType, targetTypeField, entry);
            valueMap.put(entry.getKey().toString(), newValue);
        }
        return ValueCreator.createRecordValue(recordType.getPackage(), recordType.getName(), valueMap);
    }

    private static BMap<?, ?> convertToRecordWithTypeDesc(BMap<?, ?> map, List<TypeValuePair> unresolvedValues,
                                                          BTypedesc t, Type restFieldType,
                                                          Map<String, Type> targetTypeField) {
        BMapInitialValueEntry[] initialValues = new BMapInitialValueEntry[map.entrySet().size()];
        int count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object newValue = convertRecordEntry(unresolvedValues, t, restFieldType, targetTypeField, entry);
            initialValues[count++] =
                    ValueCreator.createKeyFieldEntry(StringUtils.fromString(entry.getKey().toString()), newValue);
        }
        return (BMap<?, ?>) t.instantiate(Scheduler.getStrand(), initialValues);
    }

    private static Object convertRecordEntry(List<TypeValuePair> unresolvedValues, BTypedesc t,
                                             Type restFieldType, Map<String, Type> targetTypeField,
                                             Map.Entry<?, ?> entry) {
        Type fieldType = targetTypeField.getOrDefault(entry.getKey().toString(), restFieldType);
        return convert(entry.getValue(), fieldType, unresolvedValues, t);
    }

    private static Object convertArray(BArray array, Type targetType, List<TypeValuePair> unresolvedValues,
                                       BTypedesc t) {
        switch (targetType.getTag()) {
            case TypeTags.ARRAY_TAG:
                ArrayType arrayType = (ArrayType) targetType;
                BListInitialValueEntry[] arrayValues = new BListInitialValueEntry[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), arrayType.getElementType(), unresolvedValues, t);
                    arrayValues[i] = ValueCreator.createListInitialValueEntry(newValue);
                }
                return ValueCreator.createArrayValue(arrayType, arrayValues);
            case TypeTags.TUPLE_TAG:
                TupleType tupleType = (TupleType) targetType;
                int minLen = tupleType.getTupleTypes().size();
                BListInitialValueEntry[] tupleValues = new BListInitialValueEntry[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    Type elementType = (i < minLen) ? tupleType.getTupleTypes().get(i) : tupleType.getRestType();
                    Object newValue = convert(array.get(i), elementType, unresolvedValues, t);
                    tupleValues[i] = ValueCreator.createListInitialValueEntry(newValue);
                }
                return ValueCreator.createTupleValue(tupleType, array.size(), tupleValues);
            case TypeTags.JSON_TAG:
                Object[] jsonValues = new Object[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), PredefinedTypes.TYPE_JSON, unresolvedValues, t);
                    jsonValues[i] = newValue;
                }
                return ValueCreator.createArrayValue(jsonValues,
                                                     TypeCreator.createArrayType(PredefinedTypes.TYPE_JSON));
            case TypeTags.INTERSECTION_TAG:
                return convertArray(array, ((IntersectionType) targetType).getEffectiveType(),
                                    unresolvedValues, t);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return convertArray(array, ((ReferenceType) targetType).getReferredType(), unresolvedValues, t);
            default:
                break;
        }
        // should never reach here
        throw createConversionError(array, targetType);
    }

    private static Object convertTable(BTable<?, ?> bTable, Type targetType,
                                       List<TypeValuePair> unresolvedValues, BTypedesc t) {
        if (targetType.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            return convertTable(bTable, ((ReferenceType) targetType).getReferredType(), unresolvedValues, t);
        }

        TableType tableType = (TableType) targetType;
        Object[] tableValues = new Object[bTable.size()];
        int count = 0;
        for (Object tableValue : bTable.values()) {
            BMap<?, ?> bMap = (BMap<?, ?>) convert(tableValue, tableType.getConstrainedType(), unresolvedValues, t);
            tableValues[count++] = bMap;
        }
        BArray data = ValueCreator.createArrayValue(tableValues,
                                                    TypeCreator.createArrayType(tableType.getConstrainedType()));
        BArray fieldNames;
        fieldNames = StringUtils.fromStringArray(tableType.getFieldNames());
        return ValueCreator.createTableValue(tableType, data, fieldNames);
    }
}
