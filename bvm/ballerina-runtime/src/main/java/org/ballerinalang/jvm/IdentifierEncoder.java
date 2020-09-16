/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.jvm;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.regex.Pattern;

/**
 * Identifier encoder to encode user defined identifiers with special characters.
 *
 * @since 2.0.0
 */
public class IdentifierEncoder {

    private static final String CHAR_PREFIX = "$";
    private static final String ESCAPE_PREFIX = "\\";
    private static final String ENCODABLE_CHAR_SET = "\\.:;[]/<>$";
    private static final Pattern ENCODING_PATTERN = Pattern.compile("\\$(\\d{4})");
    private static final Pattern UNESCAPED_SPECIAL_CHAR_SET =
            Pattern.compile("(?<!\\\\)(?:\\\\\\\\)*([$&+,:;=\\?@#|/' \\[\\}\\]<\\>.\"^*{}~`()%!-])");

    private IdentifierEncoder() {
    }

    private static String encodeSpecialCharacters(String identifier) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < identifier.length()) {
            if (identifier.charAt(index) == '\\' && (index + 1 < identifier.length()) &&
                    ENCODABLE_CHAR_SET.contains(Character.toString(identifier.charAt(index + 1)))) {
                String unicodePoint = CHAR_PREFIX + String.format("%04d", (int) identifier.charAt(index + 1));
                sb.append(unicodePoint);
                index += 2;
            } else {
                sb.append(identifier.charAt(index));
                index++;
            }
        }
        return sb.toString();
    }

    public static String escapeSpecialCharacters(String identifier) {
        return UNESCAPED_SPECIAL_CHAR_SET.matcher(identifier).replaceAll("\\\\$1");
    }

    /**
     * Encode the identifiers to avoid using jvm reserved characters.
     *
     * @param identifier identifier string
     * @return encoded identifier
     */
    public static String encodeIdentifier(String identifier) {
        if (identifier == null) {
            return identifier;
        }
        if (identifier.contains(ESCAPE_PREFIX)) {
            identifier = encodeSpecialCharacters(identifier);
        } else {
            identifier = ENCODING_PATTERN.matcher(identifier).replaceAll("\\$#$1");
        }
        return StringEscapeUtils.unescapeJava(identifier);
    }

    /**
     * Decode the encoded identifiers for runtime calls.
     *
     * @param encodedIdentifier encoded identifier string
     * @return decoded identifier
     */
    public static String decodeIdentifier(String encodedIdentifier) {
        if (encodedIdentifier == null) {
            return encodedIdentifier;
        }
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < encodedIdentifier.length()) {
            if (encodedIdentifier.charAt(index) == '$' && index + 4 < encodedIdentifier.length()) {
                if (isDollarHashPattern(encodedIdentifier, index)) {
                    sb.append("$").append(encodedIdentifier, index + 2, index + 6);
                    index += 6;
                } else if (isUnicodePoint(encodedIdentifier, index)) {
                    sb.append((char) Integer.parseInt(encodedIdentifier.substring(index + 1, index + 5)));
                    index += 5;
                } else {
                    sb.append(encodedIdentifier.charAt(index));
                    index++;
                }
            } else {
                sb.append(encodedIdentifier.charAt(index));
                index++;
            }
        }
        return sb.toString();
    }

    private static boolean isUnicodePoint(String encodedName, int index) {
        return (containsOnlyDigits(encodedName.substring(index + 1, index + 5)));
    }

    private static boolean isDollarHashPattern(String encodedName, int index) {
        return encodedName.charAt(index + 1) == '#' && index + 5 < encodedName.length() &&
                containsOnlyDigits(encodedName.substring(index + 2, index + 6));
    }

    private static boolean containsOnlyDigits(String digitString) {
        for (int i = 0; i < digitString.length(); i++) {
            if (!Character.isDigit(digitString.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
