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

    public static Object toJson(Strand strand, Object value) {
        try {
            return convert(value, new ArrayList<>(), strand);
        } catch (Exception e) {
            return e;
        }
    }

    private static Object convert(Object value, List<TypeValuePair> unresolvedValues, Strand strand) {
        BType jsonType = BTypes.typeJSON;

        if (value == null) {
            return null;
        }

        BType sourceType = TypeChecker.getType(value);

        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG && TypeChecker.checkIsType(value, jsonType)) {
            return value;
        }

        TypeValuePair typeValuePair = new TypeValuePair(value, jsonType);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE,
                            ((RefValue) value).getType()));
        }

        unresolvedValues.add(typeValuePair);

        Object newValue;
        switch (sourceType.getTag()) {
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
                    throw createConversionError(value, jsonType, e.getMessage());
                }
                break;
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
                newValue = convertMapToJson((MapValue<?, ?>) value, unresolvedValues, strand);
                break;
            case TypeTags.ERROR_TAG:
            default:
                throw createConversionError(value, jsonType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convertMapToJson(MapValue<?, ?> map, List<TypeValuePair> unresolvedValues,
                                           Strand strand) {
        MapValueImpl<BString, Object> newMap = new MapValueImpl<>(new BMapType(BTypes.typeJSON));
        for (Map.Entry entry : map.entrySet()) {
            Object newValue = convert(entry.getValue(), unresolvedValues, strand);
            newMap.put(StringUtils.fromString(entry.getKey().toString()), newValue);
        }
        return newMap;
    }

    private static Object convertArrayToJson(ArrayValue array, List<TypeValuePair> unresolvedValues,
                                             Strand strand) {
        ArrayValueImpl newArray = new ArrayValueImpl((BArrayType) BTypes.typeJsonArray);
        for (int i = 0; i < array.size(); i++) {
            Object newValue = convert(array.get(i), unresolvedValues, strand);
            newArray.add(i, newValue);
        }
        return newArray;
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
