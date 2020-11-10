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

import io.ballerina.runtime.api.types.JsonType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.StructureType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.JsonGenerator;
import io.ballerina.runtime.internal.JsonParser;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

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
    public static Object parse(BString jsonStr, JsonUtils.NonStringValueProcessingMode mode) throws BError {
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
    public static Object parse(String jsonStr, JsonUtils.NonStringValueProcessingMode mode) throws BError {
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
    public static Object parse(Reader reader, JsonUtils.NonStringValueProcessingMode mode) throws BError {
        return JsonParser.parse(reader, mode);
    }

    /**
     * Parses {@link BTable} to JSON.
     *
     * @param bTable {@link BTable} to be converted to JSON
     * @return JSON representation of the provided bTable
     */
    public static Object parse(BTable bTable) {
        return io.ballerina.runtime.internal.JsonUtils.toJSON(bTable);
    }

    /**
     * Parses {@link BArray} to JSON.
     *
     * @param bArray {@link BArray} to be converted to JSON
     * @return JSON representation of the provided bArray
     */
    public static Object parse(BArray bArray) {
        return io.ballerina.runtime.internal.JsonUtils.convertArrayToJSON(bArray);
    }

    /**
     * Parse map value to JSON.
     *
     * @param map        value {@link BMap} to be converted to JSON
     * @param targetType the target JSON type to be convert to
     * @return JSON representation of the provided array
     */
    public static Object parse(BMap<BString, ?> map, JsonType targetType) {
        return io.ballerina.runtime.internal.JsonUtils.convertMapToJSON(map, targetType);
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
        return io.ballerina.runtime.internal.JsonUtils.jsonToMap(json, mapType);
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
        return io.ballerina.runtime.internal.JsonUtils.convertJSONToRecord(record, structType);
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
        return io.ballerina.runtime.internal.JsonUtils.convertJSON(source, targetType);
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
        return io.ballerina.runtime.internal.JsonUtils.convertUnionTypeToJSON(source, targetType);
    }

    /**
     * Serialize the JSON constructs to be written out to a given {@link OutputStream}.
     *
     * @param json JSON construct
     * @param out  Output source
     * @throws BError If error occur while serialize json construct.
     */
    public void serialize(Object json, OutputStream out) throws BError {
        try {
            new JsonGenerator(out, Charset.defaultCharset()).serialize(json);
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
    public void serialize(Object json, OutputStream out, Charset charset) throws BError {
        try {
            new JsonGenerator(out, charset).serialize(json);
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
    public void serialize(Object json, Writer writer) throws BError {
        try {
            new JsonGenerator(writer).serialize(json);
        } catch (IOException e) {
            throw new ErrorValue(StringUtils.fromString(e.getMessage()), e);
        }
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
