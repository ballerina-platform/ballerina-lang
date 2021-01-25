/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package io.ballerina.shell.utils;

/**
 * Utility functions required by invokers.
 * Static class.
 *
 * @since 2.0.0
 */
public class StringUtils {
    private static final int MAX_VAR_STRING_LENGTH = 78;
    private static final String QUOTE = "'";

    /**
     * Creates an quoted identifier to use for variable names.
     *
     * @param identifier Identifier without quote.
     * @return Quoted identifier.
     */
    public static String quoted(String identifier) {
        if (String.valueOf(identifier).startsWith(QUOTE)) {
            return identifier;
        }
        return QUOTE + identifier;
    }

    /**
     * Short a string to a certain length.
     *
     * @param input Input string to shorten.
     * @return Shortened string.
     */
    public static String shortenedString(Object input) {
        String value = String.valueOf(input);
        value = value.replaceAll("\n", "");
        if (value.length() > MAX_VAR_STRING_LENGTH) {
            int subStrLength = MAX_VAR_STRING_LENGTH / 2;
            return value.substring(0, subStrLength)
                    + "..." + value.substring(value.length() - subStrLength);
        }
        return value;
    }

    /**
     * Escapes the <code>String</code> with the escaping rules of Java language
     * string literals, so it's safe to insert the value into a string literal.
     * The resulting string will not be quoted.
     *
     * <p>All characters under UCS code point 0x20 will be escaped.
     * Where they have no dedicated escape sequence in Java, they will
     * be replaced with hexadecimal escape (<tt>\</tt><tt>u<i>XXXX</i></tt>).
     * <p>
     * Based off of freemarker j_string built-in.
     *
     * @param string String to encode.
     * @return encoded string.
     */
    public static String encodeJavaString(String string) {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            if (c == '"' || c == '\\' || c < 0x20) {
                StringBuilder b = new StringBuilder(length + 4);
                b.append(string, 0, i);
                while (true) {
                    if (c == '"') {
                        b.append("\\\"");
                    } else if (c == '\\') {
                        b.append("\\\\");
                    } else if (c < 0x20) {
                        if (c == '\n') {
                            b.append("\\n");
                        } else if (c == '\r') {
                            b.append("\\r");
                        } else if (c == '\f') {
                            b.append("\\f");
                        } else if (c == '\b') {
                            b.append("\\b");
                        } else if (c == '\t') {
                            b.append("\\t");
                        } else {
                            b.append("\\u00");
                            int x = c / 0x10;
                            b.append((char) (x + '0'));
                            x = c & 0xF;
                            b.append((char) (x < 0xA ? x + '0' : x - 0xA + 'a'));
                        }
                    } else {
                        b.append(c);
                    }
                    i++;
                    if (i >= length) {
                        return b.toString();
                    }
                    c = string.charAt(i);
                }
            }
        }
        return string;
    }
}
