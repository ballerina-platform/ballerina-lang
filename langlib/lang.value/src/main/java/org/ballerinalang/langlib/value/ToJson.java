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

import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
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
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TableValueImpl;
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
 * Extern function lang.values:toJson.
 * Converts a value of type `anydata` to `json`.
 *
 * @since 2.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "lang.value", version = VALUE_VERSION,
        functionName = "toJson",
        isPublic = true
)
public class ToJson {

    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object toJson(Strand strand, Object value) {
        BType targetJsonType = BTypes.typeJSON;
        try {
            return convert(value, targetJsonType, new ArrayList<>(), strand);
        } catch (Exception e) {
            return e;
        }
    }

    private static Object convert(Object value, BType targetType, List<TypeValuePair> unresolvedValues, Strand strand) {
        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            throw createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }

        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE,
                            ((RefValue) value).getType()));
        }

        unresolvedValues.add(typeValuePair);

        if (!TypeChecker.isConvertibleToJson(value, new ArrayList<>())) {
            return createConversionError(value, targetType);
        }

        Object newValue;
        switch (TypeChecker.getType(value).getTag()) {
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                newValue = ToString.toString(strand, value);
                break;
            case TypeTags.TUPLE_TAG:
            case TypeTags.ARRAY_TAG:
                newValue = convertArrayToJson((ArrayValue) value, unresolvedValues, strand);
                break;
            case TypeTags.TABLE_TAG:
                try {
                    newValue = JSONUtils.toJSON((TableValueImpl) value);
                } catch (Exception e) {
                    throw createConversionError(value, targetType, e.getMessage());
                }
                break;
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
                newValue = convertMapToJson((MapValue<?, ?>) value, new BMapType(BTypes.typeJSON),
                        unresolvedValues, strand);
                break;
            case TypeTags.ERROR_TAG:
                newValue = ((RefValue) value).copy(new HashMap<>());
                break;
            default:
                List<BType> convertibleTypes = TypeConverter.getConvertibleTypes(value, BTypes.typeJSON);
                if (convertibleTypes.size() == 0) {
                    throw createConversionError(value, targetType);
                } else if (convertibleTypes.size() > 1) {
                    throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
                }

                BType sourceType = TypeChecker.getType(value);
                BType matchingType = convertibleTypes.get(0);

                // handle primitive values
                if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
                    if (TypeChecker.checkIsType(value, matchingType)) {
                        newValue = value;
                    } else {
                        // Has to be a numeric conversion.
                        newValue = TypeConverter.convertValues(matchingType, value);
                    }
                } else {
                    throw createConversionError(value, targetType);
                }
                break;
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convertMapToJson(MapValue<?, ?> map, BType targetType, List<TypeValuePair> unresolvedValues,
                                           Strand strand) {
        MapValueImpl<BString, Object> newMap = new MapValueImpl<>(targetType);
        for (Map.Entry entry : map.entrySet()) {
            BType constraintType = ((BMapType) targetType).getConstrainedType();
            putToMap(newMap, entry, constraintType, unresolvedValues, strand);
        }
        return newMap;
    }

    private static Object convertArrayToJson(ArrayValue array, List<TypeValuePair> unresolvedValues,
                                             Strand strand) {
        ArrayValueImpl newArray = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < array.size(); i++) {
            Object newValue = convert(array.get(i), BTypes.typeJSON, unresolvedValues, strand);
            newArray.add(i, newValue);
        }
        return newArray;
    }

    private static void putToMap(MapValue<BString, Object> map, Map.Entry entry, BType fieldType,
                                 List<TypeValuePair> unresolvedValues, Strand strand) {
        Object newValue = convert(entry.getValue(), fieldType, unresolvedValues, strand);
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
