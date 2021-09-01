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
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.JsonUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * Extern function lang.values:toJson.
 * Converts a value of type `anydata` to `json`.
 *
 * @since 2.0
 */
public class ToJson {

    public static Object toJson(Object value) {
        return convert(value, new ArrayList<>());
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
            throw createCyclicValueReferenceError(value);
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
                BTable bTable = (BTable) value;
                Type constrainedType = ((TableType) bTable.getType()).getConstrainedType();
                if (constrainedType.getTag() == TypeTags.MAP_TAG) {
                    newValue = convertMapConstrainedTableToJson((BTable) value, unresolvedValues);
                } else {
                    try {
                        newValue = JsonUtils.toJSON(bTable);
                    } catch (Exception e) {
                        throw createConversionError(value, jsonType, e.getMessage());
                    }
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

    private static Object convertMapConstrainedTableToJson(BTable value, List<TypeValuePair> unresolvedValues) {
        BArray membersArray = ValueCreator.createArrayValue(PredefinedTypes.TYPE_JSON_ARRAY);
        BIterator itr = value.getIterator();
        while (itr.hasNext()) {
            BArray tupleValue = (BArray) itr.next();
            BMap mapValue = ((BMap) tupleValue.get(0));
            Object member = convertMapToJson(mapValue, unresolvedValues);
            membersArray.append(member);
        }
        return membersArray;
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

    private static BError createCyclicValueReferenceError(Object value) {
        return createError(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR, BLangExceptionHelper.getErrorMessage(
                RuntimeErrors.CYCLIC_VALUE_REFERENCE, ((BRefValue) value).getType()));
    }
}
