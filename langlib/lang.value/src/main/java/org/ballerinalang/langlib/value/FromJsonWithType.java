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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
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
import org.ballerinalang.jvm.values.StringValue;
import org.ballerinalang.jvm.values.TableValueImpl;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.jvm.BallerinaErrors.createError;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;
import static org.ballerinalang.util.BLangCompilerConstants.VALUE_VERSION;

/**
 * Extern function lang.values:fromJsonWithType.
 *
 * @since 2.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "lang.value", version = VALUE_VERSION,
        functionName = "fromJsonWithType",
        isPublic = true
)
public class FromJsonWithType {
    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object fromJsonWithType(Strand strand, Object v, TypedescValue t) {
        BType describingType = t.getDescribingType();
        try {
            return convert(v, describingType, new ArrayList<>(), t, strand);
        } catch (ErrorValue e) {
            return e;
        } catch (BallerinaException e) {
            return createError(VALUE_LANG_LIB_CONVERSION_ERROR, e.getDetail());
        }
    }

    private static Object convert(Object value, BType targetType, List<TypeValuePair> unresolvedValues,
                                  TypedescValue t, Strand strand) {

        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);
        BType sourceType = TypeChecker.getType(value);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, sourceType));
        }

        unresolvedValues.add(typeValuePair);

        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            throw createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }

        List<BType> convertibleTypes = TypeConverter.getConvertibleTypesFromJson(value, targetType, new ArrayList<>());
        if (convertibleTypes.isEmpty()) {
            throw createConversionError(value, targetType);
        } else if (convertibleTypes.size() > 1) {
            throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
        }

        BType matchingType = convertibleTypes.get(0);

        Object newValue;
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                newValue = convertMap((MapValue<?, ?>) value, matchingType, unresolvedValues, t, strand);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                newValue = convertArray((ArrayValue) value, matchingType, unresolvedValues, t, strand);
                break;
            default:
                if (TypeTags.isXMLTypeTag(matchingType.getTag())) {
                    try {
                        newValue = XMLFactory.parse(((StringValue) value).getValue());
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
                BType constraintType = ((BMapType) targetType).getConstrainedType();
                for (Map.Entry entry : map.entrySet()) {
                    putToMap(newMap, entry, constraintType, unresolvedValues, t, strand);
                }
                return newMap;
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) targetType;
                MapValueImpl<BString, Object> newRecord;
                if (t.getDescribingType() == targetType) {
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
                newArray = new ArrayValueImpl((BArrayType) BTypes.typeJsonArray);
                for (int i = 0; i < array.size(); i++) {
                    Object newValue = convert(array.get(i), targetType, unresolvedValues, t, strand);
                    newArray.add(i, newValue);
                }
                return newArray;
            case TypeTags.TABLE_TAG:
                BTableType tableType = (BTableType) targetType;
                TableValueImpl newTable = new TableValueImpl(tableType);
                for (int i = 0; i < array.size(); i++) {
                    MapValueImpl mapValue = (MapValueImpl) convert(array.get(i), tableType.getConstrainedType(),
                            unresolvedValues, t, strand);
                    newTable.add(mapValue);
                }
                return newTable;
        }
        // should never reach here
        throw BallerinaErrors.createConversionError(array, targetType);
    }

    private static void putToMap(MapValue<BString, Object> map, Map.Entry entry, BType fieldType,
                                 List<TypeValuePair> unresolvedValues, TypedescValue t, Strand strand) {
        Object newValue = convert(entry.getValue(), fieldType, unresolvedValues, t, strand);
        map.put(StringUtils.fromString(entry.getKey().toString()), newValue);
    }

    private static ErrorValue createConversionError(Object inputValue, BType targetType) {
        return createError(StringUtils.fromString(VALUE_LANG_LIB_CONVERSION_ERROR), StringUtils.fromString(
                BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                        TypeChecker.getType(inputValue), targetType)));
    }

    private static ErrorValue createConversionError(Object inputValue, BType targetType, String detailMessage) {
        return createError(StringUtils.fromString(VALUE_LANG_LIB_CONVERSION_ERROR),
                StringUtils.fromString(BLangExceptionHelper.getErrorMessage(
                        INCOMPATIBLE_CONVERT_OPERATION, TypeChecker.getType(inputValue), targetType)
                        .concat(": ".concat(detailMessage))));
    }
}
