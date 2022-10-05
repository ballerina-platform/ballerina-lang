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
 * Define the kinds of regular expression tree nodes and tokens.
 *
 * @since 2201.3.0
 */
public enum TokenKind {
    EOF_TOKEN("eof"),
    OPEN_BRACE_TOKEN("{"),
    CLOSE_BRACE_TOKEN("}"),
    OPEN_PAREN_TOKEN("("),
    CLOSE_PAREN_TOKEN(")"),
    OPEN_BRACKET_TOKEN("["),
    CLOSE_BRACKET_TOKEN("]"),
    DOT_TOKEN("."),
    COLON_TOKEN(":"),
    COMMA_TOKEN(","),
    EQUAL_TOKEN("="),
    BITWISE_XOR_TOKEN("^"),
    BACK_SLASH_TOKEN("\\"),
    PIPE_TOKEN("|"),
    MINUS_TOKEN("-"),
    QUESTION_MARK_TOKEN("?"),

    RE_ASSERTION_VALUE(),
    RE_CHAR(),
    RE_ESCAPE(),
    RE_SYNTAX_CHAR(),
    RE_SIMPLE_CHAR_CLASS_CODE(),
    RE_PROPERTY(),
    RE_UNICODE_SCRIPT_START(),
    RE_UNICODE_PROPERTY_VALUE(),
    RE_UNICODE_GENERAL_CATEGORY_START(),
    RE_UNICODE_GENERAL_CATEGORY_NAME(),
    RE_CHAR_SET_ATOM(),
    RE_CHAR_SET_ATOM_NO_DASH(),
    RE_CHAR_SET_RANGE_LHS_CHAR_SET_ATOM(),
    RE_CHAR_SET_RANGE_NO_DASH_LHS_CHAR_SET_ATOM_NO_DASH(),
    RE_FLAGS_VALUE(),
    RE_BASE_QUANTIFIER_VALUE(),
    RE_BRACED_QUANTIFIER_DIGIT(),
    ;

    private String value;

    TokenKind(String value) {
        this.value = value;
    }

    TokenKind() {
    }

    @Override
    public String toString() {
        return value;
    }
}
