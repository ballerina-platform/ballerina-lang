/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.toml.syntax.tree;

/**
 * Define various kinds of syntax tree nodes, tokens and minutiae.
 *
 * @since 2.0.0
 */
public enum SyntaxKind {
    NEWLINE(100, "\n"),
    HASH_TOKEN(101, "#"),

    TRUE_KEYWORD(200, "true"),
    FALSE_KEYWORD(201, "false"),
    STRING_LITERAL_TOKEN(203),
    DECIMAL_INT_TOKEN(204),
    DECIMAL_FLOAT_TOKEN(205),
    HEX_INTEGER_LITERAL_TOKEN(206),
    OCTAL_INTEGER_LITERAL_TOKEN(207),
    BINARY_INTEGER_LITERAL_TOKEN(208),
    
    //Separators
    OPEN_BRACKET_TOKEN(500, "["),
    CLOSE_BRACKET_TOKEN(501, "]"),
    DOUBLE_QUOTE_TOKEN(502, "\""),
    SINGLE_QUOTE_TOKEN(503, "'"),
    TRIPLE_DOUBLE_QUOTE_TOKEN(504, "\"\"\""),
    TRIPLE_SINGLE_QUOTE_TOKEN(505, "'''"),

    DOT_TOKEN(520, "."),
    COMMA_TOKEN(521, ","),
    EQUAL_TOKEN(522, "="),
    PLUS_TOKEN(523, "+"),
    MINUS_TOKEN(524, "-"),

    IDENTIFIER_LITERAL(1000),
    STRING_LITERAL(1001),
    LITERAL_STRING(1002),

    // Minutiae kinds
    WHITESPACE_MINUTIAE(1500),
    END_OF_LINE_MINUTIAE(1501),
    COMMENT_MINUTIAE(1502),
    INVALID_NODE_MINUTIAE(1503),

    // Invalid nodes
    INVALID_TOKEN(1600),
    MISSING_VALUE(1601),
    INVALID_TOKEN_MINUTIAE_NODE(1602),

    // Documentation
    MARKDOWN_DOCUMENTATION_LINE(1504),

    KEY(2000),
    KEY_VALUE(2001),
    TABLE(2002),
    TABLE_ARRAY(2003),

    //Int
    DEC_INT(2010),
    HEX_INT(2011),
    OCT_INT(2012),
    BINARY_INT(2013),

    //Float
    FLOAT(2020),
    INF_TOKEN(2021, "inf"),
    NAN_TOKEN(2022, "nan"),

    //String
    ML_STRING_LITERAL(2030),

    //Bool
    BOOLEAN (2040),

    //Date and Time
    OFFSET_DATE_TIME (2050),
    LOCAL_DATE_TIME (2051),
    LOCAL_DATE (2052),
    LOCAL_TIME (2053),

    ARRAY(2060),

    INVALID(4),
    MODULE_PART(3),
    EOF_TOKEN(2),
    LIST(1),
    NONE(0);

    final int tag;
    final String strValue;

    SyntaxKind(int tag, String strValue) {
        this.tag = tag;
        this.strValue = strValue;
    }

    SyntaxKind(int tag) {
        this.tag = tag;
        this.strValue = "";
    }

    public String stringValue() {
        return strValue;
    }
}
