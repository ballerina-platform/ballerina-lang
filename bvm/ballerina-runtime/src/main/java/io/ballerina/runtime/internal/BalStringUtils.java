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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.TableValueImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.utils.StringUtils.fromString;
import static io.ballerina.runtime.internal.util.StringUtils.parseExpressionStringVal;

/**
 * Common utility methods used for Ballerina expression syntax manipulation.
 *
 * @since 2.0.0
 */
public class BalStringUtils {
    private static boolean hasCycles = false;

    private BalStringUtils() {}

    /**
     * Create an array from string literal.
     *
     * @param exprValue Ballerina expression syntax of the array
     * @return array value
     */
    public static Object parseArrayExpressionStringValue(String exprValue, BLink parent) {
        List<String> list = getElements(exprValue);
        ArrayValueImpl arr = new ArrayValueImpl(new BArrayType(TYPE_ANYDATA));
        if (list.isEmpty()) {
            return arr;
        }
        CycleUtils.Node node = new CycleUtils.Node(arr, parent);
        Set<Type> typeSet = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            String e = list.get(i);
            Object val = parseExpressionStringVal(e, node);
            Type type = TypeChecker.getType(val);
            typeSet.add(type);
            arr.add(i, val);
        }
        int size = arr.size();
        if (hasCycles) {
            return arr;
        }
        if (typeSet.size() > 1) {
            BUnionType type = new BUnionType(new ArrayList<>(typeSet));
            Object[] refValues = new Object[size];
            for (int i = 0; i < size; i++) {
                refValues[i] = arr.get(i);
            }
            return ValueCreator.createArrayValue(refValues, new BArrayType(type));
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
                    long[] intValues = new long[size];
                    for (int i = 0; i < size; i++) {
                        intValues[i] = Long.parseLong(arr.get(i).toString());
                    }
                    return ValueCreator.createArrayValue(intValues);
                case TypeTags.BYTE_TAG:
                    byte[] byteValues = new byte[size];
                    for (int i = 0; i < size; i++) {
                        byteValues[i] = Byte.parseByte(arr.get(i).toString());
                    }
                    return ValueCreator.createArrayValue(byteValues);
                case TypeTags.FLOAT_TAG:
                    double[] floatValues = new double[size];
                    for (int i = 0; i < size; i++) {
                        floatValues[i] = Double.parseDouble(arr.get(i).toString());
                    }
                    return ValueCreator.createArrayValue(floatValues);
                case TypeTags.STRING_TAG:
                case TypeTags.CHAR_STRING_TAG:
                    BString[] bStringValues = new BString[size];
                    for (int i = 0; i < size; i++) {
                        bStringValues[i] = StringUtils.fromString(arr.get(i).toString());
                    }
                    return ValueCreator.createArrayValue(bStringValues);
                case TypeTags.BOOLEAN_TAG:
                    boolean[] booleanValues = new boolean[size];
                    for (int i = 0; i < size; i++) {
                        booleanValues[i] = Boolean.parseBoolean(arr.get(i).toString());
                    }
                    return ValueCreator.createArrayValue(booleanValues);
                default:
                    Object[] refValues = new Object[size];
                    for (int i = 0; i < size; i++) {
                        refValues[i] = arr.get(i);
                    }
                    return ValueCreator.createArrayValue(refValues, new BArrayType(type));
            }
        }
    }

    /**
     * Create a map from string literal.
     *
     * @param exprValue Ballerina expression syntax of the map
     * @return map value
     */
    public static Object parseMapExpressionStringValue(String exprValue, BLink parent) {
        List<String> list = getElements(exprValue);
        MapValueImpl eleMap = new MapValueImpl(new BMapType(TYPE_ANYDATA));
        if (list.isEmpty()) {
            return eleMap;
        }
        CycleUtils.Node node = new CycleUtils.Node(eleMap, parent);
        Set<Type> typeSet = new HashSet<>();
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
            String key = getMapKey(e, colonIndex);
            String value = e.substring(colonIndex + 1);
            Object val = parseExpressionStringVal(value, node);
            eleMap.put(StringUtils.fromString(key), val);
            Type type = TypeChecker.getType(val);
            typeSet.add(type);
        }
        if (hasCycles) {
            return eleMap;
        }
        if (typeSet.size() > 1) {
            BUnionType type = new BUnionType(new ArrayList<>(typeSet));
            MapValueImpl result = new MapValueImpl(new BMapType(type));
            result.putAll(eleMap);
            return result;
        } else {
            Type type = typeSet.iterator().next();
            MapValueImpl result = new MapValueImpl(new BMapType(type));
            result.putAll(eleMap);
            return result;
        }
    }

    private static String getMapKey(String e, int colonIndex) {
        String key = e.substring(0, colonIndex).trim();
        if (key.startsWith("\"") && key.endsWith("\"")) {
            key = key.substring(1, key.length() - 1);
        } else {
            throw ErrorCreator.createError(fromString("invalid expression style string value: " +
                    "the map keys are not enclosed with '\"'."));
        }
        return key;
    }

    /**
     * Create a table from string literal.
     *
     * @param exprValue Ballerina expression syntax of the array
     * @return table value
     */
    public static Object parseTableExpressionStringValue(String exprValue, BLink parent) {
        // start index of table keys string = index of '(' + 1
        String[] keys = exprValue.substring
                (exprValue.indexOf('(') + 1, exprValue.indexOf(')')).split(",");
        ArrayValue keyFieldNames = keys[0].isEmpty() ? (ArrayValue) ValueCreator.createArrayValue(new BString[]{}) :
                (ArrayValue) StringUtils.fromStringArray(keys);
        // start index of table members string = index of ')' + 2
        ArrayValueImpl data = (ArrayValueImpl) parseExpressionStringVal(exprValue.substring(exprValue.indexOf(')') + 2),
                parent);

        MapType mapType = TypeCreator.createMapType(TYPE_ANYDATA, false);
        BTableType tableType;
        if (keyFieldNames.size() == 0) {
            tableType =  (BTableType) TypeCreator.createTableType(mapType, false);
        } else {
            tableType =  (BTableType) TypeCreator.createTableType(mapType, keys, false);
        }
        return new TableValueImpl<>(tableType, data, keyFieldNames);
    }

    /**
     * Create an xml from string literal.
     *
     * @param exprValue Ballerina expression syntax of the xml
     * @return xml value
     */
    public static Object parseXmlExpressionStringValue(String exprValue) {
        if (exprValue.matches("^<!--[\\s\\S]*?-->$")) {
            String comment = exprValue.substring(exprValue.indexOf("<!--") + 4, exprValue.lastIndexOf("-->"));
            return XmlFactory.createXMLComment(StringUtils.fromString(comment));
        } else if (exprValue.matches("^<\\?[\\w-]+ ([\\s\\S]*?)\\?>$")) {
            String pi = exprValue.substring(exprValue.indexOf("<?") + 2, exprValue.lastIndexOf("?>"));
            String[] piArgs = pi.split(" ", 2);
            return XmlFactory.createXMLProcessingInstruction(StringUtils.fromString(piArgs[0]),
                                                             StringUtils.fromString(piArgs[1]));
        } else if (!exprValue.startsWith("<")) {
            return XmlFactory.createXMLText(StringUtils.fromString(exprValue));
        } else {
            return TypeConverter.stringToXml(exprValue);
        }
    }

    /**
     * Create a value with cycles using the 0-based index.
     *
     * @param exprValue o-based index expression
     * @return value with cycles
     */
    public static Object parseCycleDetectedExpressionStringValue(String exprValue, BLink parent) {
        hasCycles = true;
        int index = Integer.parseInt(exprValue.substring(4, exprValue.length() - 1));
        CycleUtils.Node mapParent = (CycleUtils.Node) parent;
        Object value = mapParent.obj;
        if (index == 0) {
            return value;
        }
        for (int j = 0; j < index; j++) {
            value = mapParent.obj;
            if (mapParent.parent != null) {
                mapParent = (CycleUtils.Node) mapParent.parent;
            }
        }
        return value;
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
