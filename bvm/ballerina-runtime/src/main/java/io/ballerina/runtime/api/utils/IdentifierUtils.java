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

package io.ballerina.runtime.api.utils;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identifier encoder to encode user defined identifiers with special characters.
 *
 * @since 2.0.0
 */
public class IdentifierUtils {

    private static final String UNICODE_REGEX = "\\\\u\\{([a-fA-F0-9]+)\\}";
    public static final Pattern UNICODE_PATTERN = Pattern.compile(UNICODE_REGEX);

    private static final String CHAR_PREFIX = "$";
    private static final String ESCAPE_PREFIX = "\\";
    private static final String JVM_RESERVED_CHAR_SET = "\\.:;[]/<>";
    private static final String ENCODABLE_CHAR_SET = JVM_RESERVED_CHAR_SET + CHAR_PREFIX;
    private static final Pattern UNESCAPED_SPECIAL_CHAR_SET =
            Pattern.compile("(?<!\\\\)(?:\\\\\\\\)*([$&+,:;=\\?@#|/' \\[\\}\\]<\\>.\"^*{}~`()%!-])");
    private static final String GENERATED_METHOD_PREFIX = "$gen$";

    private IdentifierUtils() {
    }

    private static String encodeSpecialCharacters(String identifier) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < identifier.length()) {
            if (isQuotedIdentifier(identifier, index)) {
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

    private static boolean isEncodableGeneratedIdentifer(String identifier, int index) {
        return JVM_RESERVED_CHAR_SET.contains(Character.toString(identifier.charAt(index)));
    }

    private static boolean isQuotedIdentifier(String identifier, int index) {
        return identifier.charAt(index) == '\\' && (index + 1 < identifier.length()) &&
                ENCODABLE_CHAR_SET.contains(Character.toString(identifier.charAt(index + 1)));
    }

    /**
     * Escape the special characters in an identifier with a preceding `\`.
     *
     * @param identifier encoded identifier string
     * @return decoded identifier
     */
    public static String escapeSpecialCharacters(String identifier) {
        return UNESCAPED_SPECIAL_CHAR_SET.matcher(identifier).replaceAll("\\\\$1");
    }

    private static String encodeIdentifier(String identifier) {
        if (identifier.contains(ESCAPE_PREFIX)) {
            identifier = encodeSpecialCharacters(identifier);
        }
        return StringEscapeUtils.unescapeJava(identifier);
    }

    private static Identifier encodeGeneratedName(String identifier) {
        StringBuilder sb = new StringBuilder();
        boolean isEncoded = false;
        int index = 0;
        while (index < identifier.length()) {
            if (isEncodableGeneratedIdentifer(identifier, index)) {
                String unicodePoint = CHAR_PREFIX + String.format("%04d", (int) identifier.charAt(index));
                sb.append(unicodePoint);
                isEncoded = true;
            } else {
                sb.append(identifier.charAt(index));
            }
            index++;
        }
        return new Identifier(sb.toString(), isEncoded);
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
                if (isUnicodePoint(encodedIdentifier, index)) {
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
        return decodeGeneratedMethodName(sb.toString());
    }

    private static String decodeGeneratedMethodName(String decodedName) {
        return decodedName.startsWith(GENERATED_METHOD_PREFIX) ?
                decodedName.substring(GENERATED_METHOD_PREFIX.length()) : decodedName;
    }

    /**
     * Replace the unicode patterns in identifiers into respective unicode characters.
     *
     * @param identifier  identifier string
     * @return modified identifier with unicode character
     */
    public static String unescapeUnicodeCodepoints(String identifier) {
        Matcher matcher = UNICODE_PATTERN.matcher(identifier);
        StringBuffer buffer = new StringBuffer(identifier.length());
        while (matcher.find()) {
            String ch = String.valueOf((char) Integer.parseInt(matcher.group(1), 16));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(ch));
        }
        matcher.appendTail(buffer);
        return String.valueOf(buffer);
    }

    private static boolean isUnicodePoint(String encodedName, int index) {
        return (containsOnlyDigits(encodedName.substring(index + 1, index + 5)));
    }

    private static boolean containsOnlyDigits(String digitString) {
        for (int i = 0; i < digitString.length(); i++) {
            if (!Character.isDigit(digitString.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Encode the function identifiers to avoid using jvm reserved characters.
     *
     * @param functionName  function identifier string
     * @return encoded identifier
     */
    public static String encodeFunctionIdentifier(String functionName) {
        functionName = encodeIdentifier(functionName);
        Identifier encodedName = encodeGeneratedName(functionName);
        return encodedName.isEncoded ? GENERATED_METHOD_PREFIX + encodedName.name : functionName;
    }

    /**
     * Encode the non-function identifiers to avoid using jvm reserved characters.
     *
     * @param pkgName  non-function identifier string
     * @return encoded identifier
     */
    public static String encodeNonFunctionIdentifier(String pkgName) {
        pkgName = encodeIdentifier(pkgName);
        Identifier encodedName = encodeGeneratedName(pkgName);
        return encodedName.name;
    }

    private static class Identifier {
        boolean isEncoded;
        String name;

        Identifier(String name, boolean isEncoded) {
            this.name = name;
            this.isEncoded = isEncoded;
        }
    }
}
