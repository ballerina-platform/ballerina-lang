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

    OPEN_BRACE_TOKEN(500, "{"),
    CLOSE_BRACE_TOKEN(501, "}"),
    OPEN_PAREN_TOKEN(502, "("),
    CLOSE_PAREN_TOKEN(503, ")"),
    OPEN_BRACKET_TOKEN(504, "["),
    CLOSE_BRACKET_TOKEN(505, "]"),
    SEMICOLON_TOKEN(506, ";"),
    DOT_TOKEN(507, "."),
    EQULS_TOKEN(508, "="),
    PLUS_TOKEN(509, "+"),
    MINUS_TOKEN(510, "-"),
    SLASH_TOKEN(511, "/"),
    PERCENT_TOKEN(512, "%"),
    ASTERISK_TOKEN(513, "*"),

    IDENTIFIER_TOKEN(1000),
    STRING_LITERAL_TOKEN(1001),
    NUMERIC_LITERAL_TOKEN(1002),


    WHITESPACE_TRIVIA(1500),
    END_OF_LINE_TRIVIA(1501),

    // module-level declarations
    FUNCTION_DEFINITION(2000),

    // Statements
    BLOCK_STATEMENT(1200),
    LOCAL_VARIABLE_DECL(1201),


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
