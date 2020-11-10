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

    MODULE_NAME(3014),

    TRUE_KEYWORD(203, "true"),
    FALSE_KEYWORD(204, "false"),

    // Separators
    OPEN_BRACE_TOKEN(500, "{"),
    CLOSE_BRACE_TOKEN(501, "}"),
    OPEN_PAREN_TOKEN(502, "("),
    CLOSE_PAREN_TOKEN(503, ")"),
    OPEN_BRACKET_TOKEN(504, "["),
    CLOSE_BRACKET_TOKEN(505, "]"),
    SEMICOLON_TOKEN(506, ";"),
    DOT_TOKEN(507, "."),
    COLON_TOKEN(508, ":"),
    COMMA_TOKEN(509, ","),
    ELLIPSIS_TOKEN(510, "..."),
    OPEN_BRACE_PIPE_TOKEN(511, "{|"),
    CLOSE_BRACE_PIPE_TOKEN(512, "|}"),
    AT_TOKEN(513, "@"),
    HASH_TOKEN(514, "#"),
    BACKTICK_TOKEN(515, "`"),
    DOUBLE_QUOTE_TOKEN(516, "\""),
    SINGLE_QUOTE_TOKEN(517, "'"),
    TRIPLE_DOUBLE_QUOTE_TOKEN(518, "\"\"\""),
    TRIPLE_SINGLE_QUOTE_TOKEN(519, "'''"),

    // Operators
    EQUAL_TOKEN(550, "="),
    PLUS_TOKEN(553, "+"),
    MINUS_TOKEN(554, "-"),

    IDENTIFIER_LITERAL(1000),
    STRING_LITERAL(1001),
    DECIMAL_INTEGER_LITERAL(1002),
    HEX_INTEGER_LITERAL(1003),
    DECIMAL_FLOATING_POINT_LITERAL(1004),
    HEX_FLOATING_POINT_LITERAL(1005),
    XML_TEXT_CONTENT(1006),
    TEMPLATE_STRING(1007),

    // Minutiae kinds
    WHITESPACE_MINUTIAE(1500),
    END_OF_LINE_MINUTIAE(1501),
    COMMENT_MINUTIAE(1502),
    INVALID_NODE_MINUTIAE(1503),

    // Invalid nodes
    INVALID_TOKEN(1600),


    // Documentation
    MARKDOWN_DOCUMENTATION_LINE(4505),
    DOCUMENTATION_DESCRIPTION(4506),
    DOCUMENTATION_REFERENCE(4507),
    PARAMETER_NAME(4508),
    BACKTICK_CONTENT(4509),
    DEPRECATION_LITERAL(4510),
    DOCUMENTATION_STRING(4511),


    //TOML
    TABLE(4800),
    KEY_VALUE(4801),
    TABLE_ARRAY(4804),
    DATATYPE(4805),


    //NEW

    SIGNED_NUMERICAL(4806),
    UNSIGNED_NUMERICAL(4807),

    BASIC_LITERAL(4808),
    KEY(4809),

    //Int
    DEC_INT(4810),
    HEX_INT(4811),
    OCT_INT(4812),
    BIN_INT(4813),

    //Float
    FLOAT(4814),
    INF_TOKEN(4815, "inf"),
    NAN_TOKEN(4816, "nan"),

    //String
    ML_STRING_LITERAL(4818),

    DECIMAL_INT_TOKEN(4819),
    DECIMAL_FLOAT_TOKEN(4819),

    //Bool
    BOOLEAN (4821),

    //Date and Time
    OFFSET_DATE_TIME (4822),
    LOCAL_DATE_TIME (4823),
    LOCAL_DATE (4824),
    LOCAL_TIME (4825),

    ARRAY(4830),

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
