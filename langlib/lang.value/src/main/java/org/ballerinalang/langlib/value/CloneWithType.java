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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypedescType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.jvm.BallerinaErrors.createError;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CONVERSION_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;
import static org.ballerinalang.util.BLangCompilerConstants.VALUE_VERSION;

/**
 * Extern function lang.values:cloneWithType.
 *
 * @since 2.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "lang.value", version = VALUE_VERSION,
        functionName = "cloneWithType",
        args = {
                @Argument(name = "t", type = TypeKind.TYPEDESC),
        },
        returnType = {
                @ReturnType(type = TypeKind.ANYDATA),
                @ReturnType(type = TypeKind.ERROR)
        },
        isPublic = true
)
public class CloneWithType {

    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object cloneWithType(Strand strand, Object v, TypedescValue t) {
        BType describingType = t.getDescribingType();
        // typedesc<json>.constructFrom like usage
        if (describingType.getTag() == TypeTags.TYPEDESC_TAG) {
            return convert(((BTypedescType) t.getDescribingType()).getConstraint(), v, t, strand);
        }
        // json.constructFrom like usage
        return convert(describingType, v, t, strand);
    }

    public static Object convert(BType convertType, Object inputValue) {
        return convert(convertType, inputValue, null, null);
    }

    public static Object convert(BType convertType, Object inputValue, TypedescValue t, Strand strand) {
        try {
            return convert(inputValue, convertType, new ArrayList<>(), t, strand);
        } catch (ErrorValue e) {
            return e;
        } catch (BallerinaException e) {
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR, e.getDetail());
        }
    }

    private static Object convert(Object value, BType targetType, List<TypeValuePair> unresolvedValues) {
        return convert(value, targetType, unresolvedValues, false, null, null);
    }

    private static Object convert(Object value, BType targetType, List<TypeValuePair> unresolvedValues,
                                  boolean allowAmbiguity) {
        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            return createError(StringUtils.fromString(CONSTRUCT_FROM_CONVERSION_ERROR),
                    StringUtils.fromString(
                            BLangExceptionHelper
                                    .getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType)));
        }

        List<BType> convertibleTypes = TypeConverter.getConvertibleTypes(value, targetType);
        if (convertibleTypes.size() == 0) {
            throw createConversionError(value, targetType);
        } else if (!allowAmbiguity && convertibleTypes.size() > 1) {
            throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
        }

        BType sourceType = TypeChecker.getType(value);
        BType matchingType = convertibleTypes.get(0);
        // handle primitive values
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
            if (TypeChecker.checkIsType(value, matchingType)) {
                return value;
            } else {
                // Has to be a numeric conversion.
                return TypeConverter.convertValues(matchingType, value);
            }
        }

        return convert((RefValue) value, matchingType, unresolvedValues);
    }

    private static Object convert(Object value, BType targetType, List<TypeValuePair> unresolvedValues,
                                  TypedescValue t, Strand strand) {
        return convert(value, targetType, unresolvedValues, false, t, strand);
    }


    private static Object convert(Object value, BType targetType, List<TypeValuePair> unresolvedValues,
                                  boolean allowAmbiguity, TypedescValue t, Strand strand) {
        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }
        List<BType> convertibleTypes;
        convertibleTypes = TypeConverter.getConvertibleTypes(value, targetType);
        if (convertibleTypes.isEmpty()) {
            throw createConversionError(value, targetType);
        } else if (!allowAmbiguity && convertibleTypes.size() > 1) {
            throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
        }

        BType sourceType = TypeChecker.getType(value);
        BType matchingType = convertibleTypes.get(0);
        // handle primitive values
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
            if (TypeChecker.checkIsType(value, matchingType)) {
                return value;
            } else {
                // Has to be a numeric conversion.
                return TypeConverter.convertValues(matchingType, value);
            }
        }

        return convert((RefValue) value, matchingType, unresolvedValues, t, strand);
    }

    private static Object convert(RefValue value, BType targetType, List<TypeValuePair> unresolvedValues,
                                  TypedescValue t, Strand strand) {
        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, value.getType()));
        }

        unresolvedValues.add(typeValuePair);

        Object newValue;
        switch (value.getType().getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                newValue = convertMap((MapValue<?, ?>) value, targetType, unresolvedValues, t, strand);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                newValue = convertArray((ArrayValue) value, targetType, unresolvedValues, t, strand);
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
                throw BallerinaErrors.createConversionError(value, targetType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convertMap(MapValue<?, ?> map, BType targetType, List<TypeValuePair> unresolvedValues,
                                     TypedescValue t, Strand strand) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                MapValueImpl<BString, Object> newMap = new MapValueImpl<>(targetType);
                for (Map.Entry entry : map.entrySet()) {
                    BType constraintType = ((BMapType) targetType).getConstrainedType();
                    putToMap(newMap, entry, constraintType, unresolvedValues, t, strand);
                }
                return newMap;
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) targetType;
                MapValueImpl<BString, Object> newRecord;
                if (t != null && t.getDescribingType() == targetType) {
                    newRecord = (MapValueImpl<BString, Object>) t.instantiate(strand);
                } else {
                    newRecord = (MapValueImpl<BString, Object>) BallerinaValues
                            .createRecordValue(recordType.getPackage(), recordType.getName());
                }

                BType restFieldType = recordType.restFieldType;
                Map<String, BType> targetTypeField = new HashMap<>();
                for (BField field : recordType.getFields().values()) {
                    targetTypeField.put(field.getFieldName(), field.getFieldType());
                }

                for (Map.Entry entry : map.entrySet()) {
                    BType fieldType = targetTypeField.getOrDefault(entry.getKey().toString(), restFieldType);
                    putToMap(newRecord, entry, fieldType, unresolvedValues, t, strand);
                }
                return newRecord;
            case TypeTags.JSON_TAG:
                BType matchingType = TypeConverter.resolveMatchingTypeForUnion(map, targetType);
                return convert(map, matchingType, unresolvedValues, t, strand);
            default:
                break;
        }
        // should never reach here
        throw BallerinaErrors.createConversionError(map, targetType);
    }


    private static Object convertArray(ArrayValue array, BType targetType, List<TypeValuePair> unresolvedValues,
                                       TypedescValue t, Strand strand) {
        switch (targetType.getTag()) {
            case TypeTags.ARRAY_TAG:
                BArrayType arrayType = (BArrayType) targetType;
                ArrayValueImpl newArray = new ArrayValueImpl(arrayType);
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), arrayType.getElementType(), unresolvedValues, t, strand);
                    newArray.add(i, newValue);
                }
                return newArray;
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) targetType;
                TupleValueImpl newTuple = new TupleValueImpl(tupleType);
                int minLen = tupleType.getTupleTypes().size();
                for (int i = 0; i < array.size(); i++) {
                    BType elementType = (i < minLen) ? tupleType.getTupleTypes().get(i) : tupleType.getRestType();
                    Object newValue = convert(array.get(i), elementType, unresolvedValues, t, strand);
                    newTuple.add(i, newValue);
                }
                return newTuple;
            case TypeTags.JSON_TAG:
                newArray = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), BTypes.typeJSON, unresolvedValues, t, strand);
                    newArray.add(i, newValue);
                }
                return newArray;
            default:
                break;
        }
        // should never reach here
        throw BallerinaErrors.createConversionError(array, targetType);
    }

    private static void putToMap(MapValue<BString, Object> map, Map.Entry entry, BType fieldType,
                                 List<TypeValuePair> unresolvedValues, TypedescValue t, Strand strand) {
        Object newValue = convert(entry.getValue(), fieldType, unresolvedValues, true, t, strand);
        map.put(StringUtils.fromString(entry.getKey().toString()), newValue);
    }

    private static ErrorValue createConversionError(Object inputValue, BType targetType) {
        return createError(StringUtils.fromString(CONSTRUCT_FROM_CONVERSION_ERROR), StringUtils.fromString(
                BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                        TypeChecker.getType(inputValue), targetType)));
    }

    private static ErrorValue createConversionError(Object inputValue, BType targetType, String detailMessage) {
        return createError(StringUtils.fromString(CONSTRUCT_FROM_CONVERSION_ERROR),
                StringUtils.fromString(BLangExceptionHelper.getErrorMessage(
                        INCOMPATIBLE_CONVERT_OPERATION, TypeChecker.getType(inputValue), targetType)
                        .concat(": ".concat(detailMessage))));
    }

}
