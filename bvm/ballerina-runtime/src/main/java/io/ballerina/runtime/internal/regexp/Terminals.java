/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal.regexp;

/**
 * Contains terminal nodes used by the TreeTraverser.
 *
 * @since 2201.3.0
 */
public class Terminals {
    public static final char MINUS = '-';
    public static final char BACKSLASH = '\\';
    public static final char BITWISE_XOR = '^';
    public static final char COLON = ':';
    public static final char DOT = '.';
    public static final char COMMA = ',';
    public static final char OPEN_PARENTHESIS = '(';
    public static final char CLOSE_PARENTHESIS = ')';
    public static final char OPEN_BRACE = '{';
    public static final char CLOSE_BRACE = '}';
    public static final char OPEN_BRACKET = '[';
    public static final char CLOSE_BRACKET = ']';
    public static final char PIPE = '|';
    public static final char QUESTION_MARK = '?';
    public static final char DOLLAR = '$';
    public static final char PLUS = '+';
    public static final char ASTERISK = '*';
}
