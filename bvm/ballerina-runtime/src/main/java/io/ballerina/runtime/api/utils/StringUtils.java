/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api.utils;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.JsonGenerator;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.BmpStringValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.NonBmpStringValue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.ballerina.runtime.api.constants.RuntimeConstants.STRING_LANG_LIB;
import static io.ballerina.runtime.internal.errors.ErrorReasons.INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.errors.ErrorReasons.STRING_OPERATION_ERROR;
import static io.ballerina.runtime.internal.errors.ErrorReasons.getModulePrefixedReason;
import static io.ballerina.runtime.internal.util.StringUtils.getExpressionStringVal;
import static io.ballerina.runtime.internal.util.StringUtils.getStringVal;
import static io.ballerina.runtime.internal.util.StringUtils.parseExpressionStringVal;

/**
 * Common utility methods used for String manipulation.
 * 
 * @since 0.95.3
 */
public class StringUtils {

    /**
     * Convert input stream to String.
     *
     * @param in Input stream to be converted to string
     * @return Converted string
     */
    public static BString getStringFromInputStream(InputStream in) {
        String result;
        try (BufferedInputStream bis = new BufferedInputStream(in);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            int data;
            while ((data = bis.read()) != -1) {
                bos.write(data);
            }
            result = bos.toString();
        } catch (IOException ioe) {
            throw ErrorCreator.createError(StringUtils.fromString("Error occurred when reading input stream"), ioe);
        }
        return StringUtils.fromString(result);
    }

    public static BString getStringFromInputStream(InputStream inputStream, String charset) {
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new InputStreamReader(inputStream, Charset.forName(charset))) {
            int character;
            while ((character = reader.read()) != -1) {
                textBuilder.append((char) character);
            }
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString(
                    "Error occurred when reading input stream with the charset" + charset), e);
        }
        return StringUtils.fromString(textBuilder.toString());
    }

    public static BString getStringAt(BString s, long index) {
        if (index < 0 || index >= s.length()) {
            throw ErrorCreator.createError(getModulePrefixedReason(STRING_LANG_LIB,
                    INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    ErrorHelper.getErrorDetails(ErrorCodes.STRING_INDEX_OUT_OF_RANGE, index, s.length()));
        }

        return StringUtils.fromString(String.valueOf(Character.toChars(s.getCodePoint((int) index))));
    }

    public static BString fromString(String s) {
        if (s == null) {
            return null;
        }
        List<Integer> highSurrogates = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (highSurrogates == null) {
                    highSurrogates = new ArrayList<>();
                }
                highSurrogates.add(i - highSurrogates.size());
            }
        }
        if (highSurrogates == null) {
            return new BmpStringValue(s);
        }

        int[] highSurrogatesArr = new int[highSurrogates.size()];

        for (int i = 0; i < highSurrogates.size(); i++) {
            Integer highSurrogate = highSurrogates.get(i);
            highSurrogatesArr[i] = highSurrogate;
        }
        return new NonBmpStringValue(s, highSurrogatesArr);
    }

    public static BArray fromStringArray(String[] s) {
        BString[] bStringArray = new BString[s.length];
        for (int i = 0; i < s.length; i++) {
            bStringArray[i] = StringUtils.fromString(s[i]);
        }
        return new ArrayValueImpl(bStringArray, false);
    }

    public static BArray fromStringSet(Set<String> set) {
        BString[] bStringArray = new BString[set.size()];
        int i = 0;
        for (String s : set) {
            bStringArray[i] = StringUtils.fromString(s);
            i++;
        }
        return new ArrayValueImpl(bStringArray, false);
    }

    /**
     * Returns the human-readable string value of Ballerina values.
     *
     * @param value The value on which the function is invoked
     * @return      String value of the provided value
     */
    public static String getStringValue(Object value) {
        return getStringVal(value, null);
    }

    // TODO: remove this with https://github.com/ballerina-platform/ballerina-lang/issues/40175
    /**
     * Returns the human-readable string value of Ballerina values.
     *
     * @param value     The value on which the function is invoked
     * @param parent    The link to the parent node
     * @return          String value of the value
     * @deprecated      use {@link #getStringValue(Object)} instead.
     */
    @Deprecated(since = "2201.6.0", forRemoval = true)
    public static String getStringValue(Object value, BLink parent) {
        return getStringVal(value, parent);
    }

    /**
     * Returns the string value of Ballerina values in expression style.
     *
     * @param value The value on which the function is invoked
     * @return String value of the value in expression style
     */
    public static String getExpressionStringValue(Object value) {
        return getExpressionStringVal(value, null);
    }

    // TODO: remove this with https://github.com/ballerina-platform/ballerina-lang/issues/40175
    /**
     * Returns the string value of Ballerina values in expression style.
     *
     * @param value     The value on which the function is invoked
     * @param parent    The link to the parent node
     * @return          String value of the value in expression style
     * @deprecated      use {@link #getExpressionStringValue(Object)} instead.
     */
    @Deprecated(since = "2201.6.0", forRemoval = true)
    public static String getExpressionStringValue(Object value, BLink parent) {
        return getExpressionStringVal(value, parent);
    }

    /**
     * Returns the Ballerina value represented by Ballerina expression syntax.
     *
     * @param value The value on which the function is invoked
     * @return      Ballerina value represented by Ballerina expression syntax
     * @throws      BError for any parsing error
     */
    public static Object parseExpressionStringValue(String value) throws BError {
        try {
            return parseExpressionStringVal(value, null);
        } catch (BError bError) {
            throw ErrorCreator.createError(STRING_OPERATION_ERROR,
                    StringUtils.fromString(bError.getErrorMessage().getValue()));
        }
    }

    // TODO: remove this with https://github.com/ballerina-platform/ballerina-lang/issues/40175
    /**
     * Returns the Ballerina value represented by Ballerina expression syntax.
     *
     * @param value The value on which the function is invoked
     * @return      Ballerina value represented by Ballerina expression syntax
     * @throws      BError for any parsing error
     * @deprecated  use {@link #parseExpressionStringValue(String)} instead.
     */
    @Deprecated(since = "2201.6.0", forRemoval = true)
    public static Object parseExpressionStringValue(String value, BLink parent) throws BError {
        return parseExpressionStringVal(value, parent);
    }

    /**
     * Returns the json string value of Ballerina values.
     *
     * @param value The value on which the function is invoked
     * @return Json String value of the value
     */
    public static String getJsonString(Object value) {
        Object jsonValue = JsonUtils.convertToJson(value);

        Type type = TypeUtils.getReferredType(TypeChecker.getType(jsonValue));
        switch (type.getTag()) {
            case TypeTags.NULL_TAG:
                return "null";
            case TypeTags.STRING_TAG:
                return stringToJson((BString) jsonValue);
            case TypeTags.MAP_TAG:
                MapValueImpl mapValue = (MapValueImpl) jsonValue;
                return mapValue.getJSONString();
            case TypeTags.ARRAY_TAG:
                ArrayValue arrayValue = (ArrayValue) jsonValue;
                return arrayValue.getJSONString();
            default:
                return String.valueOf(jsonValue);
        }
    }

    private static String stringToJson(BString value) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             JsonGenerator gen = new JsonGenerator(byteOut)) {
            gen.writeString(value.getValue());
            gen.flush();
            return byteOut.toString();
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString(
                    "Error in converting string value to a json string: " + e.getMessage()), e);
        }
    }

    private StringUtils() {
    }
}
