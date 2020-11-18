/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeConstants;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.types.BAnyType;
import io.ballerina.runtime.types.BAnydataType;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BTableType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.types.BType;
import io.ballerina.runtime.types.BUnionType;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ArrayValueImpl;
import io.ballerina.runtime.values.MappingInitialValueEntry;
import io.ballerina.runtime.values.TableValueImpl;
import io.ballerina.runtime.values.TupleValueImpl;
import io.ballerina.runtime.values.XMLSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common utility methods used for Ballerina expression syntax manipulation.
 *
 * @since 2.0.0
 */
public class BalStringUtils {

    /**
     * Create an array from string literal.
     *
     * @param exprValue Ballerina expression syntax of the array
     * @return array value
     */
    public static Object parseArrayExpressionStringValue(String exprValue) {
        List<String> list = getElements(exprValue);
        Object[] arrayElements = new Object[list.size()];
        Set<Type> typeSet = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            String e = list.get(i);
            Object val = StringUtils.parseExpressionStringValue(e);
            Type type = TypeChecker.getType(val);
            typeSet.add(type);
            arrayElements[i] = val;
        }
        if (typeSet.size() > 1) {
            BUnionType bUnionType = new BUnionType(new ArrayList<>(typeSet));
            return ValueCreator.createArrayValue(arrayElements, new BArrayType(bUnionType));
        } else {
            Type type = typeSet.iterator().next();
            int tag = type.getTag();
            switch (tag) {
                case TypeTags.INT_TAG:
                case TypeTags.SIGNED32_INT_TAG:
                case TypeTags.SIGNED16_INT_TAG:
                case TypeTags.SIGNED8_INT_TAG:
                case TypeTags.UNSIGNED32_INT_TAG:
                case TypeTags.UNSIGNED16_INT_TAG:
                case TypeTags.UNSIGNED8_INT_TAG:
                    long[] intValues = new long[arrayElements.length];
                    for (int i = 0; i < arrayElements.length; i++) {
                        intValues[i] = Long.parseLong(arrayElements[i].toString());
                    }
                    return ValueCreator.createArrayValue(intValues);
                case TypeTags.BYTE_TAG:
                    byte[] byteValues = new byte[arrayElements.length];
                    for (int i = 0; i < arrayElements.length; i++) {
                        byteValues[i] = Byte.parseByte(arrayElements[i].toString());
                    }
                    return ValueCreator.createArrayValue(byteValues);
                case TypeTags.FLOAT_TAG:
                    double[] floatValues = new double[arrayElements.length];
                    for (int i = 0; i < arrayElements.length; i++) {
                        floatValues[i] = Double.parseDouble(arrayElements[i].toString());
                    }
                    return ValueCreator.createArrayValue(floatValues);
                case TypeTags.STRING_TAG:
                case TypeTags.CHAR_STRING_TAG:
                    BString[] bStringValues = new BString[arrayElements.length];
                    for (int i = 0; i < arrayElements.length; i++) {
                        bStringValues[i] = StringUtils.fromString(arrayElements[i].toString());
                    }
                    return ValueCreator.createArrayValue(bStringValues);
                case TypeTags.BOOLEAN_TAG:
                    boolean[] booleanValues = new boolean[arrayElements.length];
                    for (int i = 0; i < arrayElements.length; i++) {
                        booleanValues[i] = Boolean.parseBoolean(arrayElements[i].toString());
                    }
                    return ValueCreator.createArrayValue(booleanValues);
                default:
                    return ValueCreator.createArrayValue(arrayElements, new BArrayType(type));
            }
        }
    }

    /**
     * Create a map from string literal.
     *
     * @param exprValue Ballerina expression syntax of the map
     * @return map value
     */
    public static Object parseMapExpressionStringValue(String exprValue) {
        List<String> list = getElements(exprValue);
        Set<Type> typeSet = new HashSet<>();
        MappingInitialValueEntry.KeyValueEntry[] keyValuesList =
                new MappingInitialValueEntry.KeyValueEntry[list.size()];
        HashMap<Object, Object> keyValuePair = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            String e = list.get(i);
            int colonIndex = e.indexOf(':');
            int quotesCount = 0;
            for (int j = 0; j < e.length(); j++) {
                if (e.charAt(j) == '\"') {
                    quotesCount++;
                } else if (e.charAt(j) == ':' && quotesCount % 2 == 0) {
                    colonIndex = j;
                    break;
                }
            }
            String key = e.substring(1, colonIndex - 1);
            Object val = StringUtils.parseExpressionStringValue(e.substring(colonIndex + 1));
            Type type = TypeChecker.getType(val);
            typeSet.add(type);
            keyValuesList[i] = new MappingInitialValueEntry.KeyValueEntry(StringUtils.fromString(key), val);
        }
        if (typeSet.size() > 1) {
            BUnionType bUnionType = new BUnionType(new ArrayList<>(typeSet));
            return ValueCreator.createMapValue(new BMapType(bUnionType), keyValuesList);
        } else {
            Type type = typeSet.iterator().next();
            return ValueCreator.createMapValue(new BMapType(type), keyValuesList);
        }
    }

    /**
     * Create a table from string literal.
     *
     * @param exprValue Ballerina expression syntax of the array
     * @return table value
     */
    public static Object parseTableExpressionStringValue(String exprValue) {
        String[] keys = exprValue.substring
                (exprValue.indexOf('(') + 1, exprValue.indexOf(')')).split(",");
        ArrayValue fieldNames = (ArrayValue) ValueCreator.createArrayValue(StringUtils.fromStringArray(keys));
        ArrayValueImpl data = (ArrayValueImpl) StringUtils.parseExpressionStringValue(exprValue.substring
                (exprValue.indexOf(')') + 2));

        BType typeAnydata = new BAnydataType(TypeConstants.ANYDATA_TNAME, new Module(null, null, null),
                false);
        BType typeAny = new BAnyType(TypeConstants.ANY_TNAME, new Module(null, null, null), false);
        BType typeMap = new BMapType(TypeConstants.MAP_TNAME, typeAny, new Module(null, null, null));

        return new TableValueImpl<>(new BTableType(typeMap, typeAnydata, false), data, fieldNames);
    }

    /**
     * Create a tuple from string literal.
     *
     * @param exprValue Ballerina expression syntax of the tuple
     * @return tuple value
     */
    public static Object parseTupleExpressionStringValue(String exprValue) {
        String[] stringElements = exprValue.split(" ");
        Object[] elements = new Object[stringElements.length];
        List<Type> typeList = new ArrayList<>();
        for (int i = 0; i < stringElements.length; i++) {
            Object value = StringUtils.parseExpressionStringValue(stringElements[i].trim());
            elements[i] = value;
            Type type = TypeChecker.getType(value);
            typeList.add(type);
        }
        return new TupleValueImpl(elements, new BTupleType(typeList));
    }

    /**
     * Create an xml from string literal.
     *
     * @param exprValue Ballerina expression syntax of the xml
     * @return xml value
     */
    public static Object parseXmlExpressionStringValue(String exprValue) {
        if (exprValue.matches("<[\\!--](.*?)[\\-\\-\\!]>")) {
            String comment = exprValue.substring(exprValue.indexOf("<!--") + 4, exprValue.lastIndexOf("-->"));
            return XMLFactory.createXMLComment(comment);
        } else if (exprValue.matches("<\\?(.*?)\\?>")) {
            String pi = exprValue.substring(exprValue.indexOf("<?") + 2, exprValue.lastIndexOf("?>"));
            String[] piArgs = pi.split(" ", 2);
            return XMLFactory.createXMLProcessingInstruction(StringUtils.fromString(piArgs[0]),
                    StringUtils.fromString(piArgs[1]));
        } else if (exprValue.matches("<(\\S+?)(.*?)>(.*?)</\\1>")) {
            return XMLFactory.parse(exprValue);
        } else if (!exprValue.startsWith("<?") && !exprValue.startsWith("<!--") && !exprValue.startsWith("<")) {
            return XMLFactory.createXMLText(StringUtils.fromString(exprValue));
        } else {
            Pattern pattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>|<[\\!--](.*?)[\\-\\-\\!]>" +
                    "|<\\?(.*?)\\?>");
            Matcher matcher = pattern.matcher(exprValue);
            List<BXML> children = new ArrayList<>();
            String part = exprValue;
            while (matcher.find()) {
                String item = matcher.group();
                String[] splitParts = part.split(item, 2);
                String splitItem = splitParts[0];
                if (splitItem.isEmpty()) {
                    children.add((BXML) parseXmlExpressionStringValue(item));
                } else {
                    children.add((BXML) parseXmlExpressionStringValue(splitItem));
                    if (!item.equals(splitItem)) {
                        children.add((BXML) parseXmlExpressionStringValue(item));
                    }
                }
                if (splitParts.length == 2) {
                    part = splitParts[1];
                }
            }
            return new XMLSequence(children);
        }
    }

    /**
     * Identify elements of an array or map from a string.
     *
     * @param exprValue Ballerina expression syntax of the array/map
     * @return List of elements
     */
    public static List<String> getElements(String exprValue) {
        List<String> list = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        int balance = 0;
        int quotesCount = 0;
        char[] strChars = exprValue.substring(1, exprValue.length() - 1).toCharArray();
        for (char strChar : strChars) {
            part.append(strChar);
            if (strChar == '\"') {
                quotesCount++;
            } else if (strChar == '[' && quotesCount % 2 == 0) {
                balance++;
            } else if (strChar == '{' && quotesCount % 2 == 0) {
                balance++;
            } else if (strChar == '(' && quotesCount % 2 == 0) {
                balance++;
            } else if (strChar == ']' && quotesCount % 2 == 0) {
                balance--;
            } else if (strChar == '}' && quotesCount % 2 == 0) {
                balance--;
            } else if (strChar == ')' && quotesCount % 2 == 0) {
                balance--;
            } else if (strChar == ',' && balance == 0 && quotesCount % 2 == 0) {
                list.add(part.substring(0, part.length() - 1));
                part = new StringBuilder();
            }
        }
        if (part.length() > 0) {
            list.add(part.toString());
        }
        return list;
    }
}
