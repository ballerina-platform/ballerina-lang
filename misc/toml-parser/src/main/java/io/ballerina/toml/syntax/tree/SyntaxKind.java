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
    INITIAL_TRIVIA(200),
    NEW_LINE(201, "\n"),
    CARRIAGE_RETURN(202, "\r"),

    MODULE_NAME(3014),

    TRUE_KEYWORD(203, "true"),
    FALSE_KEYWORD(204, "false"),

    // Separators
    OPEN_BRACKET_TOKEN(504, "["),
    CLOSE_BRACKET_TOKEN(505, "]"),
    DOT_TOKEN(507, "."),
    COMMA_TOKEN(509, ","),
    HASH_TOKEN(514, "#"),
    DOUBLE_QUOTE_TOKEN(516, "\""),
    SINGLE_QUOTE_TOKEN(517, "'"),
    TRIPLE_DOUBLE_QUOTE_TOKEN(518, "\"\"\""),
    TRIPLE_SINGLE_QUOTE_TOKEN(519, "'''"),

    // Operators
    EQUAL_TOKEN(550, "="),
    PLUS_TOKEN(553, "+"),
    MINUS_TOKEN(554, "-"),
    STRING_LITERAL_TOKEN(555),

    IDENTIFIER_LITERAL(1000),
    STRING_LITERAL(1001),

    // Minutiae kinds
    WHITESPACE_MINUTIAE(1500),
    END_OF_LINE_MINUTIAE(1501),
    COMMENT_MINUTIAE(1502),
    INVALID_NODE_MINUTIAE(1503),

    // Invalid nodes
    INVALID_TOKEN(1600),

    // Documentation
    MARKDOWN_DOCUMENTATION_LINE(4505),

    //TOML
    TABLE(4800),
    KEY_VALUE(4801),
    TABLE_ARRAY(4804),


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
