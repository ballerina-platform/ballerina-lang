/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
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
 * Extern function lang.values:fromJsonWithType.
 *
 * @since 2.0
 */
public class FromJsonWithType {

    private FromJsonWithType() {
    }

    public static Object fromJsonWithType(Object value, BTypedesc t) {
        return convert(value, t.getDescribingType(), t);
    }

    public static Object convert(Object value, Type targetType) {
        return convert(value, targetType, null);
    }

    public static Object convert(Object value, Type targetType, BTypedesc t) {
        try {
            return convert(value, targetType, new ArrayList<>(), t);
        } catch (BError e) {
            return e;
        } catch (BallerinaException e) {
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR, StringUtils.fromString(e.getDetail()));
        }
    }

    private static Object convert(Object value, Type targetType, List<TypeValuePair> unresolvedValues, BTypedesc t) {
        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);

        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            throw createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }

        Type sourceType = TypeUtils.getReferredType(TypeChecker.getType(value));

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR.getValue(),
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, sourceType).getValue());
        }

        unresolvedValues.add(typeValuePair);

        List<String> errors = new ArrayList<>();
        Type convertibleType = TypeConverter.getConvertibleTypeFromJson(value, targetType,
                null, new ArrayList<>(), errors, true);
        if (convertibleType == null) {
            throw CloneUtils.createConversionError(value, targetType, errors);
        }

        Type matchingType = TypeUtils.getReferredType(convertibleType);

        Object newValue;
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                newValue = convertMap((BMap<?, ?>) value, matchingType, unresolvedValues, t);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                newValue = convertArray((BArray) value, matchingType, unresolvedValues, t);
                break;
            default:
                if (isRegExpType(targetType) && matchingType.getTag() == TypeTags.STRING_TAG) {
                    try {
                        newValue = RegExpFactory.parse(((BString) value).getValue());
                        break;
                    } catch (BError e) {
                        throw createConversionError(value, targetType, e.getMessage());
                    }
                }
                if (TypeTags.isXMLTypeTag(matchingType.getTag())) {
                    try {
                        newValue = XmlFactory.parse(((BString) value).getValue());
                        break;
                    } catch (Throwable e) {
                        throw createConversionError(value, targetType, e.getMessage());
                    }
                }

                // handle primitive values
                if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
                    if (TypeChecker.checkIsType(value, matchingType)) {
                        newValue = value;
                    } else {
                        // Has to be a numeric conversion.
                        newValue = TypeConverter.convertValues(matchingType, value);
                    }
                    break;
                }
                // should never reach here
                throw createConversionError(value, targetType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static boolean isRegExpType(Type targetType) {
        if (targetType.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            Type referredType = ((ReferenceType) targetType).getReferredType();
            if (referredType.getQualifiedName().equals("ballerina/lang.regexp:0:RegExp")) {
                return true;
            }
            return isRegExpType(referredType);
        }
        return false;
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
                if (t != null && t.getDescribingType() == targetType) {
                    return convertToRecordWithTypeDesc(map, unresolvedValues, t, restFieldType, targetTypeField);
                } else {
                    return convertToRecord(map, unresolvedValues, t, recordType, restFieldType,
                                           targetTypeField);
                }
            case TypeTags.JSON_TAG:
                Type matchingType = TypeConverter.resolveMatchingTypeForUnion(map, targetType);
                return convert(map, matchingType, unresolvedValues, t);
            case TypeTags.INTERSECTION_TAG:
                return convertMap(map, ((IntersectionType) targetType).getEffectiveType(), unresolvedValues, t);
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
            initialValues[count] = ValueCreator.createKeyFieldEntry(StringUtils.fromString(entry.getKey().toString()),
                                                                    newValue);
            count++;
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
                    Object newValue = convert(array.get(i), targetType, unresolvedValues, t);
                    jsonValues[i] = newValue;
                }
                return ValueCreator.createArrayValue(jsonValues,
                                                     TypeCreator.createArrayType(PredefinedTypes.TYPE_JSON));
            case TypeTags.TABLE_TAG:
                TableType tableType = (TableType) targetType;
                Object[] tableValues = new Object[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    BMap<?, ?> bMap = (BMap<?, ?>) convert(array.get(i), tableType.getConstrainedType(),
                                                           unresolvedValues, t);
                    tableValues[i] = bMap;
                }
                BArray data = ValueCreator
                        .createArrayValue(tableValues, TypeCreator.createArrayType(tableType.getConstrainedType()));
                BArray fieldNames;
                fieldNames = StringUtils.fromStringArray(tableType.getFieldNames());
                return ValueCreator.createTableValue(tableType, data, fieldNames);
            case TypeTags.INTERSECTION_TAG:
                return convertArray(array, ((IntersectionType) targetType).getEffectiveType(),
                                    unresolvedValues, t);
        }
        // should never reach here
        throw createConversionError(array, targetType);
    }
}
