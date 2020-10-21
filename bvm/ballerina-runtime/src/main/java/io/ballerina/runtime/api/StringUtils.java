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
package io.ballerina.runtime.api;

import io.ballerina.runtime.BalStringUtils;
import io.ballerina.runtime.CycleUtils;
import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.AbstractObjectValue;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.BmpStringValue;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.MapValueImpl;
import io.ballerina.runtime.values.NonBmpStringValue;
import io.ballerina.runtime.values.RefValue;

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

import static io.ballerina.runtime.util.BLangConstants.STRING_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Common utility methods used for String manipulation.
 * 
 * @since 0.95.3
 */
public class StringUtils {

    private static final String STR_CYCLE = "...";

    /**
     * Convert input stream to String.
     *
     * @param in Input stream to be converted to string
     * @return Converted string
     */
    public static BString getStringFromInputStream(InputStream in) {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String result;
        try {
            int data;
            while ((data = bis.read()) != -1) {
                bos.write(data);
            }
            result = bos.toString();
        } catch (IOException ioe) {
            throw new BallerinaException("Error occurred when reading input stream", ioe);
        } finally {
            try {
                bos.close();
            } catch (IOException ignored) {
            }
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
            throw new BallerinaException("Error occurred when reading input stream with the charset" + charset, e);
        }
        return StringUtils.fromString(textBuilder.toString());
    }

    public static BString getStringAt(BString s, long index) {
        if (index < 0 || index >= s.length()) {
            throw ErrorCreator.createError(getModulePrefixedReason(STRING_LANG_LIB,
                                                                   INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                                           fromString("string index out of range: index: " + index + ", size: " +
                                                               s.length()));
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

    public static BString[] fromStringArray(String[] s) {
        BString[] bStringArray = new BString[s.length];
        for (int i = 0; i < s.length; i++) {
            bStringArray[i] = StringUtils.fromString(s[i]);
        }
        return bStringArray;
    }

    public static BString[] fromStringSet(Set<String> set) {
        BString[] bStringArray = new BString[set.size()];
        int i = 0;
        for (String s : set) {
            bStringArray[i] = StringUtils.fromString(s);
            i++;
        }
        return bStringArray;
    }

    /**
     * Returns the human-readable string value of Ballerina values.
     *
     * @param value The value on which the function is invoked
     * @param parent The link to the parent node
     * @return String value of the value
     */
    public static String getStringValue(Object value, BLink parent) {
        if (value == null) {
            return "";
        }

        Type type = TypeChecker.getType(value);

        //TODO: bstring - change to type tag check
        if (value instanceof BString) {
            return ((BString) value).getValue();
        }

        if (type.getTag() < TypeTags.JSON_TAG) {
            return String.valueOf(value);
        }

        CycleUtils.Node node = new CycleUtils.Node(value, parent);

        if (node.hasCyclesSoFar()) {
            return STR_CYCLE;
        }

        if (type.getTag() == TypeTags.MAP_TAG || type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            MapValueImpl mapValue = (MapValueImpl) value;
            return mapValue.stringValue(parent);
        }

        if (type.getTag() == TypeTags.ARRAY_TAG || type.getTag() == TypeTags.TUPLE_TAG) {
            ArrayValue arrayValue = (ArrayValue) value;
            return arrayValue.stringValue(parent);
        }

        if (type.getTag() == TypeTags.TABLE_TAG) {
            return ((RefValue) value).informalStringValue(parent);
        }

        if (type.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            AbstractObjectValue objectValue = (AbstractObjectValue) value;
            ObjectType objectType = objectValue.getType();
            for (AttachedFunctionType func : objectType.getAttachedFunctions()) {
                if (func.getName().equals("toString") && func.getParameterTypes().length == 0 &&
                        func.getType().getReturnType().getTag() == TypeTags.STRING_TAG) {
                    return objectValue.call(Scheduler.getStrand(), "toString").toString();
                }
            }
        }

        if (type.getTag() == TypeTags.ERROR_TAG) {
            RefValue errorValue = (RefValue) value;
            return errorValue.stringValue(parent);
        }

        RefValue refValue = (RefValue) value;
        return refValue.stringValue(parent);
    }

    /**
     * Returns the string value of Ballerina values in expression style.
     *
     * @param value The value on which the function is invoked
     * @param parent The link to the parent node
     * @return String value of the value in expression style
     */
    public static String getExpressionStringValue(Object value, BLink parent) {
        if (value == null) {
            return "()";
        }

        Type type = TypeChecker.getType(value);

        //TODO: bstring - change to type tag check
        if (value instanceof BString) {
            return "\"" + ((BString) value).getValue() + "\"";
        }

        if (type.getTag() == TypeTags.DECIMAL_TAG) {
            DecimalValue decimalValue = (DecimalValue) value;
            return decimalValue.expressionStringValue(parent);
        }

        if (type.getTag() == TypeTags.FLOAT_TAG) {
            if (Double.isNaN((Double) value)) {
                return "float:" + Double.toString((Double) value);
            }
            if (Double.isInfinite((Double) value)) {
                return "float:" + Double.toString((Double) value);
            }
        }

        if (type.getTag() < TypeTags.JSON_TAG) {
            return String.valueOf(value);
        }

        CycleUtils.Node node = new CycleUtils.Node(value, parent);

        if (node.hasCyclesSoFar()) {
            return STR_CYCLE + "[" + node.getIndex() + "]";
        }

        if (type.getTag() == TypeTags.MAP_TAG || type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            MapValueImpl mapValue = (MapValueImpl) value;
            return mapValue.expressionStringValue(parent);
        }

        if (type.getTag() == TypeTags.ARRAY_TAG || type.getTag() == TypeTags.TUPLE_TAG) {
            ArrayValue arrayValue = (ArrayValue) value;
            return arrayValue.expressionStringValue(parent);
        }

        if (type.getTag() == TypeTags.TABLE_TAG) {
            return ((RefValue) value).expressionStringValue(parent);
        }

        if (type.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            AbstractObjectValue objectValue = (AbstractObjectValue) value;
            ObjectType objectType = objectValue.getType();
            for (AttachedFunctionType func : objectType.getAttachedFunctions()) {
                if (func.getName().equals("toString") && func.getParameterTypes().length == 0 &&
                        func.getType().getReturnType().getTag() == TypeTags.STRING_TAG) {
                    return "object " + objectValue.call(Scheduler.getStrand(), "toString").toString();
                }
            }
        }

        if (type.getTag() == TypeTags.ERROR_TAG) {
            RefValue errorValue = (RefValue) value;
            return errorValue.expressionStringValue(parent);
        }

        RefValue refValue = (RefValue) value;
        return refValue.expressionStringValue(parent);
    }

    /**
     * Returns the Ballerina value represented by Ballerina expression syntax.
     *
     * @param value The value on which the function is invoked
     * @return Ballerina value represented by Ballerina expression syntax
     */
    public static Object parseExpressionStringValue(String value) {
        String exprValue = value.trim();
        int endIndex = exprValue.length() - 1;
        if (exprValue.equals("()")) {
            return null;
        }
        if (exprValue.startsWith("\"") && exprValue.endsWith("\"")) {
            return StringUtils.fromString(exprValue.substring(1, endIndex));
        }
        if (exprValue.matches("[+-]?[0-9][0-9]*")) {
            return Long.parseLong(exprValue);
        }
        if (exprValue.equals("float:Infinity") || exprValue.equals("float:NaN")) {
            return Double.parseDouble(exprValue.substring(6));
        }
        if (exprValue.matches("[+-]?[0-9]+([.][0-9]+)?([Ee][+-]?[0-9]+)?")) {
            return Double.parseDouble(exprValue);
        }
        if (exprValue.matches("[+-]?[0-9]+(.[0-9]+)?([Ee][+-]?[0-9]+)?[d]")) {
            return new DecimalValue(exprValue.substring(0, endIndex));
        }
        if (exprValue.equals("true") || exprValue.equals("false")) {
            return Boolean.parseBoolean(exprValue);
        }
        if (exprValue.startsWith("[") && exprValue.endsWith("]")) {
            return BalStringUtils.parseArrayExpressionStringValue(exprValue);
        }
        if (exprValue.startsWith("{") && exprValue.endsWith("}")) {
            return BalStringUtils.parseMapExpressionStringValue(exprValue);
        }
        if (exprValue.startsWith("table key")) {
            return BalStringUtils.parseTableExpressionStringValue(exprValue);
        }
        if (exprValue.startsWith("xml")) {
            String xml = exprValue.substring(exprValue.indexOf('`') + 1,
                    exprValue.lastIndexOf('`')).trim();
            return BalStringUtils.parseXmlExpressionStringValue(xml);
        }
        if (!exprValue.startsWith("error") && !exprValue.startsWith("object") && !exprValue.startsWith("...")) {
            return BalStringUtils.parseTupleExpressionStringValue(exprValue);
        }
        return ErrorCreator.createError(fromString("Cannot parse Ballerina expression syntax"),
                fromString("fromBalString supported only for Ballerina expression syntax of anydata value"));
    }

    /**
     * Returns the json string value of Ballerina values.
     *
     * @param value The value on which the function is invoked
     * @return Json String value of the value
     */
    public static String getJsonString(Object value) {
        if (value == null) {
            return "null";
        }

        Type type = TypeChecker.getType(value);

        if (type.getTag() < TypeTags.JSON_TAG) {
            return String.valueOf(value);
        }

        if (type.getTag() == TypeTags.MAP_TAG) {
            MapValueImpl mapValue = (MapValueImpl) value;
            return mapValue.getJSONString();
        }

        if (type.getTag() == TypeTags.ARRAY_TAG) {
            ArrayValue arrayValue = (ArrayValue) value;
            return arrayValue.getJSONString();
        }

        RefValue refValue = (RefValue) value;
        return refValue.stringValue(null);
    }

}
