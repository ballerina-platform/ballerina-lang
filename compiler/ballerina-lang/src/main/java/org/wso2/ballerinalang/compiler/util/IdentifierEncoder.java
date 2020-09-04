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

package org.wso2.ballerinalang.compiler.util;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identifier encoder to encode user defined identifiers with special characters.
 *
 * @since 2.0.0
 */
public class IdentifierEncoder {

    private static final String CHAR_PREFIX = "$";
    private static final String ESCAPE_PREFIX = "\\";
    private static final String ENCODABLE_CHAR_SET = "(\\\\)[\\\\.:;\\[\\]/<>$]";
    private static final String ENCODING_PATTERN = "\\$(\\d{4})";

    private IdentifierEncoder() {
    }

    private static String encodeSpecialCharacters(String identifier) {
        Matcher matcher = Pattern.compile(ENCODABLE_CHAR_SET).matcher(identifier);
        StringBuffer buffer = new StringBuffer(identifier.length());
        while (matcher.find()) {
            String character = CHAR_PREFIX + String.format("%04d", (int) matcher.group().charAt(1));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(character));
        }
        matcher.appendTail(buffer);
        return String.valueOf(buffer);
    }

    public static String decodeIdentifiers(String encodedName) {
        return replaceStringPattern(encodedName, ENCODING_PATTERN, "").replaceAll("(\\$#)(\\d{4})", "\\$$2");
    }

    public static String escapeSpecialCharacters(String identifier) {
        String specialCharSet = "([$&+,:;=\\?@#|/' \\[\\}\\]<\\>.\"^*{}~`()%!-])";
        return identifier.replaceAll("(?<!\\\\)(?:\\\\\\\\)*" + specialCharSet, "\\\\$1");
    }

    private static String replaceStringPattern(String identifier, String pattern, String prefix) {
        Matcher matcher = Pattern.compile(pattern).matcher(identifier);
        StringBuffer buffer = new StringBuffer(identifier.length());
        while (matcher.find()) {
            String character = prefix + (char) Integer.parseInt(matcher.group(1));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(character));
        }
        matcher.appendTail(buffer);
        return String.valueOf(buffer);
    }

    public static String encodeIdentifier(String identifier) {
        if (identifier == null) {
            return identifier;
        }
        identifier = identifier.replaceAll(ENCODING_PATTERN, "\\$#$1");
        if (identifier.contains(ESCAPE_PREFIX)) {
            identifier = encodeSpecialCharacters(identifier);
        }
        return StringEscapeUtils.unescapeJava(identifier);
    }
}
