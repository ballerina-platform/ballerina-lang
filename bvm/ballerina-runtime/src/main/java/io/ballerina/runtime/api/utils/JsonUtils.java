/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api.utils;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.JsonType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.StructureType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.JsonGenerator;
import io.ballerina.runtime.internal.JsonInternalUtils;
import io.ballerina.runtime.internal.JsonParser;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CONVERSION_ERROR;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * Class @{@link JsonParser} provides APIs to handle json values.
 *
 * @since 2.0.0
 */
public class JsonUtils {

    /**
     * Parses the contents in the given {@link InputStream} and returns a json.
     *
     * @param in input stream which contains the JSON content
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(InputStream in) throws BError {
        return JsonParser.parse(in);
    }

    /**
     * Parses the contents in the given {@link InputStream} and returns a json.
     *
     * @param in          input stream which contains the JSON content
     * @param charsetName the character set name of the input stream
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(InputStream in, String charsetName) throws BError {
        return JsonParser.parse(in, charsetName);
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(BString jsonStr) throws BError {
        return JsonParser.parse(jsonStr.getValue());
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @param mode    the mode to use when processing numeric values
     * @return JSON   value if parsing is successful
     * @throws BError for any parsing error
     */
    public static Object parse(BString jsonStr, NonStringValueProcessingMode mode) throws BError {
        return JsonParser.parse(jsonStr.getValue(), mode);
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(String jsonStr) throws BError {
        return JsonParser.parse(jsonStr);
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @param mode    the mode to use when processing numeric values
     * @return JSON   value if parsing is successful
     * @throws BError for any parsing error
     */
    public static Object parse(String jsonStr, NonStringValueProcessingMode mode) throws BError {
        return JsonParser.parse(jsonStr, mode);
    }

    /**
     * Parses the contents in the given {@link Reader} and returns a json.
     *
     * @param reader reader which contains the JSON content
     * @param mode   the mode to use when processing numeric values
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(Reader reader, NonStringValueProcessingMode mode) throws BError {
        return JsonParser.parse(reader, mode);
    }

    /**
     * Parses {@link BTable} to JSON.
     *
     * @param bTable {@link BTable} to be converted to JSON
     * @return JSON representation of the provided bTable
     */
    public static Object parse(BTable bTable) {
        return JsonInternalUtils.toJSON(bTable);
    }

    /**
     * Parses {@link BArray} to JSON.
     *
     * @param bArray {@link BArray} to be converted to JSON
     * @return JSON representation of the provided bArray
     */
    public static Object parse(BArray bArray) {
        return JsonInternalUtils.convertArrayToJSON(bArray);
    }

    /**
     * Parse map value to JSON.
     *
     * @param map        value {@link BMap} to be converted to JSON
     * @param targetType the target JSON type to be convert to
     * @return JSON representation of the provided array
     */
    public static Object parse(BMap<BString, ?> map, JsonType targetType) {
        return JsonInternalUtils.convertMapToJSON(map, targetType);
    }

    /**
     * Converts a JSON node to a map.
     *
     * @param json    JSON to convert
     * @param mapType MapType which the JSON is converted to.
     * @return If the provided JSON is of object-type, this method will return a {@link BMap} containing the
     * values of the JSON object. Otherwise a {@link BError} will be thrown.
     * @throws BError If conversion fails.
     */
    public static BMap<BString, ?> convertJSONToMap(Object json, MapType mapType) throws BError {
        return JsonInternalUtils.jsonToMap(json, mapType);
    }

    /**
     * Converts a BJSON to a user defined record.
     *
     * @param record     JSON to convert
     * @param structType Type (definition) of the target record
     * @return If the provided JSON is of object-type, this method will return a {@link BMap} containing the
     * values of the JSON object. Otherwise the method will throw a {@link BError}.
     * @throws BError If conversion fails.
     */
    public static BMap<BString, Object> convertJSONToRecord(Object record, StructureType structType) throws BError {
        return JsonInternalUtils.convertJSONToRecord(record, structType);
    }

    /**
     * Converts a BJSON to a user given target type.
     *
     * @param source     JSON to convert
     * @param targetType Type (definition) of the target record
     * @return If the conversion is successes this will return JSON object.
     * @throws BError If conversion fails.
     */
    public static Object convertJSON(Object source, Type targetType) throws BError {
        return JsonInternalUtils.convertJSON(source, targetType);
    }

    /**
     * Converts a union type value to a user given target type.
     *
     * @param source     Union type value to convert
     * @param targetType Type (definition).
     * @return If the conversion is successes this will return JSON object.
     * @throws BError If conversions fails.
     */
    public static Object convertUnionTypeToJSON(Object source, JsonType targetType) throws BError {
        return JsonInternalUtils.convertUnionTypeToJSON(source, targetType);
    }

    /**
     * Serialize the JSON constructs to be written out to a given {@link OutputStream}.
     *
     * @param json JSON construct
     * @param out  Output source
     * @throws BError If error occur while serialize json construct.
     */
    public static void serialize(Object json, OutputStream out) throws BError {
        try {
            JsonGenerator gen = new JsonGenerator(out);
            gen.serialize(json);
            gen.flush();
        } catch (IOException e) {
            throw new ErrorValue(StringUtils.fromString(e.getMessage()), e);
        }
    }

    /**
     * Serialize the JSON constructs to be written out to a given {@link OutputStream}.
     *
     * @param json    JSON construct
     * @param out     Output source
     * @param charset Character set
     * @throws BError If error occur while serialize json construct.
     */
    public static void serialize(Object json, OutputStream out, Charset charset) throws BError {
        try {
            JsonGenerator gen = new JsonGenerator(out, charset);
            gen.serialize(json);
            gen.flush();
        } catch (IOException e) {
            throw new ErrorValue(StringUtils.fromString(e.getMessage()), e);
        }
    }

    /**
     * Serialize the JSON constructs to be written out to a given {@link Writer}.
     *
     * @param json   JSON construct
     * @param writer Output writer
     * @throws BError If error occur while serialize json construct.
     */
    public static void serialize(Object json, Writer writer) throws BError {
        try {
            JsonGenerator gen = new JsonGenerator(writer);
            gen.serialize(json);
            gen.flush();
        } catch (IOException e) {
            throw new ErrorValue(StringUtils.fromString(e.getMessage()), e);
        }
    }

    public static Object convertToJson(Object value, List<TypeValuePair> unresolvedValues) {
        Type jsonType = PredefinedTypes.TYPE_JSON;
        if (value == null) {
            return null;
        }
        Type sourceType = TypeChecker.getType(value);
        if (TypeUtils.getReferredType(sourceType).getTag() <= TypeTags.BOOLEAN_TAG && TypeChecker.checkIsType(value,
                jsonType)) {
            return value;
        }
        TypeValuePair typeValuePair = new TypeValuePair(value, jsonType);
        if (unresolvedValues.contains(typeValuePair)) {
            throw createCyclicValueReferenceError(value);
        }
        unresolvedValues.add(typeValuePair);
        Object newValue = getJsonObject(value, unresolvedValues, jsonType, sourceType);
        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object getJsonObject(Object value, List<TypeValuePair> unresolvedValues, Type jsonType,
                                    Type sourceType) {
        Object newValue;
        switch (sourceType.getTag()) {
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
            case TypeTags.REG_EXP_TYPE_TAG:
                newValue = StringUtils.fromString(StringUtils.getStringValue(value, null));
                break;
            case TypeTags.TUPLE_TAG:
            case TypeTags.ARRAY_TAG:
                newValue = convertArrayToJson((BArray) value, unresolvedValues);
                break;
            case TypeTags.TABLE_TAG:
                BTable bTable = (BTable) value;
                Type constrainedType = ((TableType) sourceType).getConstrainedType();
                if (constrainedType.getTag() == TypeTags.MAP_TAG) {
                    newValue = convertMapConstrainedTableToJson((BTable) value, unresolvedValues);
                } else {
                    try {
                        newValue = JsonInternalUtils.toJSON(bTable);
                    } catch (Exception e) {
                        throw createConversionError(value, jsonType, e.getMessage());
                    }
                }
                break;
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
                newValue = convertMapToJson((BMap<?, ?>) value, unresolvedValues);
                break;
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                newValue = getJsonObject(value, unresolvedValues, jsonType,
                        ((ReferenceType) sourceType).getReferredType());
                break;
            case TypeTags.ERROR_TAG:
            default:
                throw createConversionError(value, jsonType);
        }
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
            Object newValue = convertToJson(entry.getValue(), unresolvedValues);
            newMap.put(StringUtils.fromString(entry.getKey().toString()), newValue);
        }
        return newMap;
    }

    private static Object convertArrayToJson(BArray array, List<TypeValuePair> unresolvedValues) {
        BArray newArray = ValueCreator.createArrayValue((ArrayType) PredefinedTypes.TYPE_JSON_ARRAY);
        for (int i = 0; i < array.size(); i++) {
            Object newValue = convertToJson(array.get(i), unresolvedValues);
            newArray.add(i, newValue);
        }
        return newArray;
    }

    private static BError createConversionError(Object inputValue, Type targetType) {
        return createError(VALUE_LANG_LIB_CONVERSION_ERROR,
                BLangExceptionHelper.getErrorDetails(INCOMPATIBLE_CONVERT_OPERATION,
                        TypeChecker.getType(inputValue), targetType));
    }

    private static BError createConversionError(Object inputValue, Type targetType, String detailMessage) {
        return createError(VALUE_LANG_LIB_CONVERSION_ERROR, BLangExceptionHelper.getErrorMessage(
                        INCOMPATIBLE_CONVERT_OPERATION, TypeChecker.getType(inputValue), targetType)
                .concat(StringUtils.fromString(": ".concat(detailMessage))));
    }

    private static BError createCyclicValueReferenceError(Object value) {
        return createError(VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR, BLangExceptionHelper.getErrorDetails(
                RuntimeErrors.CYCLIC_VALUE_REFERENCE, ((BRefValue) value).getType()));
    }

    /**
     * Represents the modes which process numeric values while converting a string to JSON.
     */
    public enum NonStringValueProcessingMode {
        /**
         * FROM_JSON_STRING converts a numeric value that
         * - starts with the negative sign (-) and is numerically equal to zero (0) to `-0.0f`
         * - is syntactically an integer to an `int`
         * - doesn't belong to the above to decimal.
         */
        FROM_JSON_STRING,

        /**
         * FROM_JSON_FLOAT_STRING converts all numerical values to float.
         */
        FROM_JSON_FLOAT_STRING,

        /**
         * FROM_JSON_DECIMAL_STRING converts all numerical values to decimal.
         */
        FROM_JSON_DECIMAL_STRING
    }

}
