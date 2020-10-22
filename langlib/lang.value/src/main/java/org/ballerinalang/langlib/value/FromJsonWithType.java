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

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.TypeConverter;
import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.commons.TypeValuePair;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.ErrorCreator.createError;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR;
import static io.ballerina.runtime.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * Extern function lang.values:fromJsonWithType.
 *
 * @since 2.0
 */
public class FromJsonWithType {
    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object fromJsonWithType(Object v, BTypedesc t) {
        Type describingType = t.getDescribingType();
        try {
            return convert(v, describingType, new ArrayList<>(), t);
        } catch (BError e) {
            return e;
        } catch (BallerinaException e) {
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                               StringUtils.fromString(e.getDetail()));
        }
    }

    private static Object convert(Object value, Type targetType, List<TypeValuePair> unresolvedValues,
                                  BTypedesc t) {

        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);
        Type sourceType = TypeChecker.getType(value);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR.getValue(),
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, sourceType).getValue());
        }

        unresolvedValues.add(typeValuePair);

        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            throw createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }

        List<Type> convertibleTypes = TypeConverter.getConvertibleTypesFromJson(value, targetType, new ArrayList<>());
        if (convertibleTypes.isEmpty()) {
            throw createConversionError(value, targetType);
        } else if (convertibleTypes.size() > 1) {
            throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
        }

        Type matchingType = convertibleTypes.get(0);

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
                if (TypeTags.isXMLTypeTag(matchingType.getTag())) {
                    try {
                        newValue = XMLFactory.parse(((BString) value).getValue());
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
                throw CloneUtils.createConversionError(value, targetType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convertMap(BMap<?, ?> map, Type targetType, List<TypeValuePair> unresolvedValues,
                                     BTypedesc t) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                BMap<BString, Object> newMap = ValueCreator.createMapValue(targetType);
                Type constraintType = ((MapType) targetType).getConstrainedType();
                for (Map.Entry entry : map.entrySet()) {
                    putToMap(newMap, entry, constraintType, unresolvedValues, t);
                }
                return newMap;
            case TypeTags.RECORD_TYPE_TAG:
                RecordType  recordType = (RecordType) targetType;
                BMap<BString, Object> newRecord;
                if (t.getDescribingType() == targetType) {
                    newRecord = (BMap<BString, Object>) t.instantiate(Scheduler.getStrand());
                } else {
                    newRecord = (BMap<BString, Object>) ValueCreator
                            .createRecordValue(recordType.getPackage(), recordType.getName());
                }

                Type restFieldType = recordType.getRestFieldType();
                Map<String, Type> targetTypeField = new HashMap<>();
                for (Field field : recordType.getFields().values()) {
                    targetTypeField.put(field.getFieldName(), field.getFieldType());
                }

                for (Map.Entry entry : map.entrySet()) {
                    Type fieldType = targetTypeField.getOrDefault(entry.getKey().toString(), restFieldType);
                    putToMap(newRecord, entry, fieldType, unresolvedValues, t);
                }
                return newRecord;
            case TypeTags.JSON_TAG:
                Type matchingType = TypeConverter.resolveMatchingTypeForUnion(map, targetType);
                return convert(map, matchingType, unresolvedValues, t);
        }
        // should never reach here
        throw CloneUtils.createConversionError(map, targetType);
    }


    private static Object convertArray(BArray array, Type targetType, List<TypeValuePair> unresolvedValues,
                                       BTypedesc t) {
        switch (targetType.getTag()) {
            case TypeTags.ARRAY_TAG:
                ArrayType arrayType = (ArrayType) targetType;
                BArray newArray = ValueCreator.createArrayValue(arrayType);
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), arrayType.getElementType(), unresolvedValues, t);
                    newArray.add(i, newValue);
                }
                return newArray;
            case TypeTags.TUPLE_TAG:
                TupleType tupleType = (TupleType) targetType;
                BArray newTuple = ValueCreator.createTupleValue(tupleType);
                int minLen = tupleType.getTupleTypes().size();
                for (int i = 0; i < array.size(); i++) {
                    Type elementType = (i < minLen) ? tupleType.getTupleTypes().get(i) : tupleType.getRestType();
                    Object newValue = convert(array.get(i), elementType, unresolvedValues, t);
                    newTuple.add(i, newValue);
                }
                return newTuple;
            case TypeTags.JSON_TAG:
                newArray = ValueCreator.createArrayValue((ArrayType) PredefinedTypes.TYPE_JSON_ARRAY);
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), targetType, unresolvedValues, t);
                    newArray.add(i, newValue);
                }
                return newArray;
            case TypeTags.TABLE_TAG:
                TableType tableType = (TableType) targetType;
                BTable newTable = ValueCreator.createTableValue(tableType);
                for (int i = 0; i < array.size(); i++) {
                    BMap bMap = (BMap) convert(array.get(i), tableType.getConstrainedType(),
                            unresolvedValues, t);
                    newTable.add(bMap);
                }
                return newTable;
        }
        // should never reach here
        throw CloneUtils.createConversionError(array, targetType);
    }

    private static void putToMap(BMap<BString, Object> map, Map.Entry entry, Type fieldType,
                                 List<TypeValuePair> unresolvedValues, BTypedesc t) {
        Object newValue = convert(entry.getValue(), fieldType, unresolvedValues, t);
        map.put(StringUtils.fromString(entry.getKey().toString()), newValue);
    }

    private static BError createConversionError(Object inputValue, Type targetType) {
        return createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                           BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                                                                TypeChecker.getType(inputValue), targetType));
    }

    private static BError createConversionError(Object inputValue, Type targetType, String detailMessage) {
        return createError(VALUE_LANG_LIB_CONVERSION_ERROR, BLangExceptionHelper.getErrorMessage(
                INCOMPATIBLE_CONVERT_OPERATION, TypeChecker.getType(inputValue), targetType)
                .concat(StringUtils.fromString(": ".concat(detailMessage))));
    }
}
