/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.common.constants;

/**
 * Holds constants related to regex patterns.
 *
 * @since 2.0.0
 */
public class PatternConstants {

    private PatternConstants() {
    }

    public static final String UNICODE_PATTERN_WHITESPACE_CHAR_PATTERN = "[\u200E\u200F\u2028\u2029]";
    public static final String UNICODE_PRIVATE_USE_CHAR =
            "[\uE000-\uF8FF]|[\\x{F0000}-\\x{FFFFD}]|[\\x{100000}-\\x{10FFFD}]";
    public static final String UNICODE_NON_IDENTIFIER_CHAR = "(" + UNICODE_PATTERN_WHITESPACE_CHAR_PATTERN + "|" +
            UNICODE_PRIVATE_USE_CHAR + ")";

    public static final String ASCII_CHAR_PATTERN = "[\u0000-\u007F]";
    public static final String ASCII_LETTER_PATTERN = "[A-Za-z]";
    public static final String UNICODE_IDENTIFIER_CHAR_PATTERN = "[^(" + ASCII_CHAR_PATTERN + "|" +
            UNICODE_NON_IDENTIFIER_CHAR + ")]";
    public static final String DIGIT_PATTERN = "[0-9]";
    public static final String NUMERIC_ESCAPE_PATTERN = "^\\\\u[A-Fa-f0-9]+";

    public static final String IDENTIFIER_INITIAL_CHAR_PATTERN = "(" + ASCII_LETTER_PATTERN + "|_|" +
            UNICODE_IDENTIFIER_CHAR_PATTERN + ")";
    public static final String IDENTIFIER_SINGLE_ESCAPE_PATTERN = "\\[^(" + ASCII_LETTER_PATTERN +
            "|\\x09|\\x0A|\\x0D|" + UNICODE_PATTERN_WHITESPACE_CHAR_PATTERN + ")]";

    public static final String IDENTIFIER_ESCAPE_PATTERN = "(" + IDENTIFIER_SINGLE_ESCAPE_PATTERN + "|" +
            NUMERIC_ESCAPE_PATTERN + ")";
    public static final String IDENTIFIER_FOLLOWING_CHAR_PATTERN = "(" + IDENTIFIER_INITIAL_CHAR_PATTERN + "|" +
            DIGIT_PATTERN + ")";
    public static final String UNQUOTED_IDENTIFIER_PATTERN = "^(" + IDENTIFIER_INITIAL_CHAR_PATTERN + "|" +
            IDENTIFIER_ESCAPE_PATTERN + ")(" + IDENTIFIER_FOLLOWING_CHAR_PATTERN + "|" +
            IDENTIFIER_ESCAPE_PATTERN + ")*";
    public static final String QUOTED_IDENTIFIER_PATTERN = "^'(" + IDENTIFIER_FOLLOWING_CHAR_PATTERN + "|" +
            IDENTIFIER_ESCAPE_PATTERN + ")+";
    
    /**
     * Regex to match a valid identifier name as per the ballerina specification.
     */
    public static final String IDENTIFIER_PATTERN = UNQUOTED_IDENTIFIER_PATTERN + "|" + QUOTED_IDENTIFIER_PATTERN;
}
