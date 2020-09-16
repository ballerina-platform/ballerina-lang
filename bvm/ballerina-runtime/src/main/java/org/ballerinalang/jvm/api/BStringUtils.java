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
package org.ballerinalang.jvm.api;

import org.ballerinalang.jvm.CycleUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.api.values.BLink;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.AbstractObjectValue;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.BmpStringValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.NonBmpStringValue;
import org.ballerinalang.jvm.values.RefValue;

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

import static org.ballerinalang.jvm.util.BLangConstants.STRING_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Common utility methods used for String manipulation.
 * 
 * @since 0.95.3
 */
public class BStringUtils {

    private static final String STR_CYCLE = "...";

    /**
     * Check whether two strings are equal in value.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return flag indicating whether the two string values are equal
     */
    public static boolean isEqual(String s1, String s2) {
        if (s1 == s2) {
            return true;
        } else if (s1 == null || s2 == null) {
            return false;
        } else {
            return s1.equals(s2);
        }
    }

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
        return BStringUtils.fromString(result);
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
        return BStringUtils.fromString(textBuilder.toString());
    }

    public static String getStringAt(String s, long index) {
        if (index < 0 || index >= s.length()) {
            throw BErrorCreator.createError(getModulePrefixedReason(STRING_LANG_LIB,
                                                                    INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                                            fromString("string index out of range: index: " + index + ", size: " +
                                                               s.length()));
        }

        return String.valueOf(s.charAt((int) index));
    }

    public static BString getStringAt(BString s, long index) {
        if (index < 0 || index >= s.length()) {
            throw BErrorCreator.createError(getModulePrefixedReason(STRING_LANG_LIB,
                                                                    INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                                            fromString("string index out of range: index: " + index + ", size: " +
                                                               s.length()));
        }

        return BStringUtils.fromString(String.valueOf(Character.toChars(s.getCodePoint((int) index))));
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
            bStringArray[i] = BStringUtils.fromString(s[i]);
        }
        return bStringArray;
    }

    public static BString[] fromStringSet(Set<String> set) {
        BString[] bStringArray = new BString[set.size()];
        int i = 0;
        for (String s : set) {
            bStringArray[i] = BStringUtils.fromString(s);
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

        BType type = TypeChecker.getType(value);

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
            BObjectType objectType = objectValue.getType();
            for (AttachedFunction func : objectType.getAttachedFunctions()) {
                if (func.funcName.equals("toString") && func.paramTypes.length == 0 &&
                        func.type.retType.getTag() == TypeTags.STRING_TAG) {
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
     * Returns the json string value of Ballerina values.
     *
     * @param value The value on which the function is invoked
     * @return Json String value of the value
     */
    public static String getJsonString(Object value) {
        if (value == null) {
            return "null";
        }

        BType type = TypeChecker.getType(value);

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
