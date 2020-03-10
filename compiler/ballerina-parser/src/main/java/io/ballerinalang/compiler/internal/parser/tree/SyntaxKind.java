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
package io.ballerinalang.compiler.internal.parser.tree;

public enum SyntaxKind {

    PUBLIC_KEYWORD(100, "public"),
    PRIVATE_KEYWORD(101, "private"),

    IMPORT_KEYWORD(100, "import"),
    FUNCTION_KEYWORD(101, "function"),
    CONST_KEYWORD(102, "const"),
    LISTENER_KEYWORD(103, "listener"),
    SERVICE_KEYWORD(104, "service"),
    XMLNS_KEYWORD(105, "xmlns"),
    ANNOTATION_KEYWORD(106, "annotation"),
    TYPE_KEYWORD(107, "type"),

    RETURNS_KEYWORD(200, "returns"),
    RETURN_KEYWORD(201, "return"),
    EXTERNAL_KEYWORD(202, "external"),

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

    // Operators
    EQUAL_TOKEN(550, "="),
    DOUBLE_EQUAL_TOKEN(550, "=="),
    TRIPPLE_EQUAL_TOKEN(551, "==="),
    PLUS_TOKEN(552, "+"),
    MINUS_TOKEN(553, "-"),
    SLASH_TOKEN(554, "/"),
    PERCENT_TOKEN(555, "%"),
    ASTERISK_TOKEN(556, "*"),
    LT_TOKEN(557, "<"),
    EQUAL_LT_TOKEN(558, "<="),
    GT_TOKEN(559, "<"),
    EQUAL_GT_TOKEN(560, "=>"),
    
    IDENTIFIER_TOKEN(1000),
    STRING_LITERAL_TOKEN(1001),
    NUMERIC_LITERAL_TOKEN(1002),
    TYPE_TOKEN(1003),


    // Trivia
    WHITESPACE_TRIVIA(1500),
    END_OF_LINE_TRIVIA(1501),
    COMMENT(1502),
    
    // module-level declarations
    IMPORT_DECLARATION(2000),
    FUNCTION_DEFINITION(2001),

    // Statements
    BLOCK_STATEMENT(1200),
    LOCAL_VARIABLE_DECL(1201),
    ASSIGNMENT_STATEMENT(1202),

    // Expressions
    BINARY_EXPRESSION(1300),
    BRACED_EXPRESSION(1301),

    // Other
    RETURN_TYPE_DESCRIPTOR(3000),
    PARAMETER(3001),
    EXTERNAL_FUNCTION_BODY(3002),
    
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


}
