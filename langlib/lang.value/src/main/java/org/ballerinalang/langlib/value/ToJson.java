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

import io.ballerina.runtime.JSONUtils;
import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.commons.TypeValuePair;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.ErrorCreator.createError;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR;
import static io.ballerina.runtime.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * Extern function lang.values:toJson.
 * Converts a value of type `anydata` to `json`.
 *
 * @since 2.0
 */
public class ToJson {

    public static Object toJson(Object value) {
        try {
            return convert(value, new ArrayList<>());
        } catch (Exception e) {
            return e;
        }
    }

    private static Object convert(Object value, List<TypeValuePair> unresolvedValues) {
        Type jsonType = PredefinedTypes.TYPE_JSON;

        if (value == null) {
            return null;
        }

        Type sourceType = TypeChecker.getType(value);

        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG && TypeChecker.checkIsType(value, jsonType)) {
            return value;
        }

        TypeValuePair typeValuePair = new TypeValuePair(value, jsonType);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR.getValue(),
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE,
                            ((BRefValue) value).getType()).getValue());
        }

        unresolvedValues.add(typeValuePair);

        Object newValue;
        switch (sourceType.getTag()) {
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                newValue = ToString.toString(value);
                break;
            case TypeTags.TUPLE_TAG:
            case TypeTags.ARRAY_TAG:
                newValue = convertArrayToJson((BArray) value, unresolvedValues);
                break;
            case TypeTags.TABLE_TAG:
                try {
                    newValue = JSONUtils.toJSON((BTable) value);
                } catch (Exception e) {
                    throw createConversionError(value, jsonType, e.getMessage());
                }
                break;
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
                newValue = convertMapToJson((BMap<?, ?>) value, unresolvedValues);
                break;
            case TypeTags.ERROR_TAG:
            default:
                throw createConversionError(value, jsonType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convertMapToJson(BMap<?, ?> map, List<TypeValuePair> unresolvedValues) {
        BMap<BString, Object> newMap =
                ValueCreator.createMapValue(TypeCreator.createMapType(PredefinedTypes.TYPE_JSON));
        for (Map.Entry entry : map.entrySet()) {
            Object newValue = convert(entry.getValue(), unresolvedValues);
            newMap.put(StringUtils.fromString(entry.getKey().toString()), newValue);
        }
        return newMap;
    }

    private static Object convertArrayToJson(BArray array, List<TypeValuePair> unresolvedValues) {
        BArray newArray = ValueCreator.createArrayValue((ArrayType) PredefinedTypes.TYPE_JSON_ARRAY);
        for (int i = 0; i < array.size(); i++) {
            Object newValue = convert(array.get(i), unresolvedValues);
            newArray.add(i, newValue);
        }
        return newArray;
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
