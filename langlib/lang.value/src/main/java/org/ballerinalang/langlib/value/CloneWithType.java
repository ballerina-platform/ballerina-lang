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

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.TypeConverter;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypedescType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.commons.TypeValuePair;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.ErrorCreator.createError;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CONVERSION_ERROR;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR;
import static io.ballerina.runtime.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * Extern function lang.values:cloneWithType.
 *
 * @since 2.0
 */
public class CloneWithType {

    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object cloneWithType(Object v, BTypedesc t) {
        Type describingType = t.getDescribingType();
        // typedesc<json>.constructFrom like usage
        if (describingType.getTag() == TypeTags.TYPEDESC_TAG) {
            return convert(((TypedescType) t.getDescribingType()).getConstraint(), v, t, Scheduler.getStrand());
        }
        // json.constructFrom like usage
        return convert(describingType, v, t, Scheduler.getStrand());
    }

    public static Object convert(Type convertType, Object inputValue) {
        return convert(convertType, inputValue, null, null);
    }

    public static Object convert(Type convertType, Object inputValue, BTypedesc t, Strand strand) {
        try {
            return convert(inputValue, convertType, new ArrayList<>(), t, strand);
        } catch (BError e) {
            return e;
        } catch (BallerinaException e) {
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR, StringUtils.fromString(e.getDetail()));
        }
    }

    private static Object convert(Object value, Type targetType, List<TypeValuePair> unresolvedValues) {
        return convert(value, targetType, unresolvedValues, false, null, null);
    }

    private static Object convert(Object value, Type targetType, List<TypeValuePair> unresolvedValues,
                                  boolean allowAmbiguity) {
        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                              BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }

        List<Type> convertibleTypes = TypeConverter.getConvertibleTypes(value, targetType);
        if (convertibleTypes.size() == 0) {
            throw createConversionError(value, targetType);
        } else if (!allowAmbiguity && convertibleTypes.size() > 1) {
            throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
        }

        Type sourceType = TypeChecker.getType(value);
        Type matchingType = convertibleTypes.get(0);
        // handle primitive values
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
            if (TypeChecker.checkIsType(value, matchingType)) {
                return value;
            } else {
                // Has to be a numeric conversion.
                return TypeConverter.convertValues(matchingType, value);
            }
        }

        return convert(value, matchingType, unresolvedValues);
    }

    private static Object convert(Object value, Type targetType, List<TypeValuePair> unresolvedValues,
                                  BTypedesc t, Strand strand) {
        return convert(value, targetType, unresolvedValues, false, t, strand);
    }


    private static Object convert(Object value, Type targetType, List<TypeValuePair> unresolvedValues,
                                  boolean allowAmbiguity, BTypedesc t, Strand strand) {
        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }
        List<Type> convertibleTypes;
        convertibleTypes = TypeConverter.getConvertibleTypes(value, targetType);
        if (convertibleTypes.isEmpty()) {
            throw createConversionError(value, targetType);
        } else if (!allowAmbiguity && convertibleTypes.size() > 1) {
            throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
        }

        Type sourceType = TypeChecker.getType(value);
        Type matchingType = convertibleTypes.get(0);
        // handle primitive values
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
            if (TypeChecker.checkIsType(value, matchingType)) {
                return value;
            } else {
                // Has to be a numeric conversion.
                return TypeConverter.convertValues(matchingType, value);
            }
        }

        return convert((BRefValue) value, matchingType, unresolvedValues, t, strand);
    }

    private static Object convert(BRefValue value, Type targetType, List<TypeValuePair> unresolvedValues,
                                  BTypedesc t, Strand strand) {
        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR.getValue(),
                                         BLangExceptionHelper
                                                 .getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, value.getType())
                                                 .getValue());
        }

        unresolvedValues.add(typeValuePair);

        Object newValue;
        switch (value.getType().getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                newValue = convertMap((BMap<?, ?>) value, targetType, unresolvedValues, t, strand);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                newValue = convertArray((BArray) value, targetType, unresolvedValues, t, strand);
                break;
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
            case TypeTags.ERROR_TAG:
                newValue = value.copy(new HashMap<>());
                break;
            default:
                // should never reach here
                throw CloneUtils.createConversionError(value, targetType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convertMap(BMap<?, ?> map, Type targetType, List<TypeValuePair> unresolvedValues,
                                     BTypedesc t, Strand strand) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                BMap<BString, Object> newMap = ValueCreator.createMapValue(targetType);
                for (Map.Entry entry : map.entrySet()) {
                    Type constraintType = ((MapType) targetType).getConstrainedType();
                    putToMap(newMap, entry, constraintType, unresolvedValues, t, strand);
                }
                return newMap;
            case TypeTags.RECORD_TYPE_TAG:
                RecordType  recordType = (RecordType) targetType;
                BMap<BString, Object> newRecord;
                if (t != null && t.getDescribingType() == targetType) {
                    newRecord = (BMap<BString, Object>) t.instantiate(strand);
                } else {
                    newRecord = ValueCreator
                            .createRecordValue(recordType.getPackage(), recordType.getName());
                }

                Type restFieldType = recordType.getRestFieldType();
                Map<String, Type> targetTypeField = new HashMap<>();
                for (Field field : recordType.getFields().values()) {
                    targetTypeField.put(field.getFieldName(), field.getFieldType());
                }

                for (Map.Entry entry : map.entrySet()) {
                    Type fieldType = targetTypeField.getOrDefault(entry.getKey().toString(), restFieldType);
                    putToMap(newRecord, entry, fieldType, unresolvedValues, t, strand);
                }
                return newRecord;
            case TypeTags.JSON_TAG:
                Type matchingType = TypeConverter.resolveMatchingTypeForUnion(map, targetType);
                return convert(map, matchingType, unresolvedValues, t, strand);
            default:
                break;
        }
        // should never reach here
        throw CloneUtils.createConversionError(map, targetType);
    }


    private static Object convertArray(BArray array, Type targetType, List<TypeValuePair> unresolvedValues,
                                       BTypedesc t, Strand strand) {
        switch (targetType.getTag()) {
            case TypeTags.ARRAY_TAG:
                ArrayType arrayType = (ArrayType) targetType;
                BArray newArray = ValueCreator.createArrayValue(arrayType);
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), arrayType.getElementType(), unresolvedValues, t, strand);
                    newArray.add(i, newValue);
                }
                return newArray;
            case TypeTags.TUPLE_TAG:
                TupleType tupleType = (TupleType) targetType;
                BArray newTuple = ValueCreator.createTupleValue(tupleType);
                int minLen = tupleType.getTupleTypes().size();
                for (int i = 0; i < array.size(); i++) {
                    Type elementType = (i < minLen) ? tupleType.getTupleTypes().get(i) : tupleType.getRestType();
                    Object newValue = convert(array.get(i), elementType, unresolvedValues, t, strand);
                    newTuple.add(i, newValue);
                }
                return newTuple;
            case TypeTags.JSON_TAG:
                newArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_JSON));
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), PredefinedTypes.TYPE_JSON, unresolvedValues, t, strand);
                    newArray.add(i, newValue);
                }
                return newArray;
            default:
                break;
        }
        // should never reach here
        throw CloneUtils.createConversionError(array, targetType);
    }

    private static void putToMap(BMap<BString, Object> map, Map.Entry entry, Type fieldType,
                                 List<TypeValuePair> unresolvedValues, BTypedesc t, Strand strand) {
        Object newValue = convert(entry.getValue(), fieldType, unresolvedValues, true, t, strand);
        map.put(StringUtils.fromString(entry.getKey().toString()), newValue);
    }

    private static BError createConversionError(Object inputValue, Type targetType) {
        return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                           BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                        TypeChecker.getType(inputValue), targetType));
    }

    private static BError createConversionError(Object inputValue, Type targetType, String detailMessage) {
        return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                           BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                                                                TypeChecker.getType(inputValue), targetType)
                                   .concat(StringUtils.fromString(": ".concat(detailMessage))));
    }

}
