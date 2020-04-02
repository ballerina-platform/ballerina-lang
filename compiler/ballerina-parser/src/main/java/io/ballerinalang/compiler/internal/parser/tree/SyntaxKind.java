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

    // Keywords

    PUBLIC_KEYWORD(50, "public"),
    PRIVATE_KEYWORD(51, "private"),
    REMOTE_KEYWORD(52, "remote"),
    ABSTRACT_KEYWORD(53, "abstract"),
    CLIENT_KEYWORD(54, "client"),
    IMPORT_KEYWORD(100, "import"),
    FUNCTION_KEYWORD(101, "function"),
    CONST_KEYWORD(102, "const"),
    LISTENER_KEYWORD(103, "listener"),
    SERVICE_KEYWORD(104, "service"),
    XMLNS_KEYWORD(105, "xmlns"),
    ANNOTATION_KEYWORD(106, "annotation"),
    TYPE_KEYWORD(107, "type"),
    RECORD_KEYWORD(108, "record"),
    OBJECT_KEYWORD(109, "object"),
    VERSION_KEYWORD(110, "version"),
    AS_KEYWORD(111, "as"),

    RETURNS_KEYWORD(200, "returns"),
    RETURN_KEYWORD(201, "return"),
    EXTERNAL_KEYWORD(202, "external"),
    TRUE_KEYWORD(203, "true"),
    FALSE_KEYWORD(204, "false"),
    IF_KEYWORD(205, "if"),
    ELSE_KEYWORD(206, "else"),
    WHILE_KEYWORD(207, "while"),
    CHECK_KEYWORD(208, "check"),
    CHECKPANIC_KEYWORD(209, "checkpanic"),
    PANIC_KEYWORD(210, "panic"),
    CONTINUE_KEYWORD(212,"continue"),
    BREAK_KEYWORD(213,"break"),

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

    // Operators
    EQUAL_TOKEN(550, "="),
    DOUBLE_EQUAL_TOKEN(551, "=="),
    TRIPPLE_EQUAL_TOKEN(552, "==="),
    PLUS_TOKEN(553, "+"),
    MINUS_TOKEN(554, "-"),
    SLASH_TOKEN(555, "/"),
    PERCENT_TOKEN(556, "%"),
    ASTERISK_TOKEN(557, "*"),
    LT_TOKEN(558, "<"),
    EQUAL_LT_TOKEN(559, "<="),
    GT_TOKEN(560, ">"),
    EQUAL_GT_TOKEN(561, "=>"),
    QUESTION_MARK_TOKEN(562, "?"),
    PIPE_TOKEN(563, "|"),

    IDENTIFIER_TOKEN(1000),
    STRING_LITERAL(1001),
    DECIMAL_INTEGER_LITERAL(1002),
    HEX_INTEGER_LITERAL(1003),
    DECIMAL_FLOATING_POINT_LITERAL(1004),
    HEX_FLOATING_POINT_LITERAL(1005),
    SIMPLE_TYPE(1006),

    // Trivia
    WHITESPACE_TRIVIA(1500),
    END_OF_LINE_TRIVIA(1501),
    COMMENT(1502),
    
    // module-level declarations
    IMPORT_DECLARATION(2000),
    FUNCTION_DEFINITION(2001),
    TYPE_DEFINITION(2002),

    // Statements
    BLOCK_STATEMENT(1200),
    LOCAL_VARIABLE_DECL(1201),
    ASSIGNMENT_STATEMENT(1202),
    IF_ELSE_STATEMENT(1203),
    ELSE_BLOCK(1204),
    WHILE_STATEMENT(1205),
    CALL_STATEMENT(1206),
    PANIC_STATEMENT(1207),
    RETURN_STATEMENT(1208),
    CONTINUE_STATEMENT(1209),
    BREAK_STATEMENT(1210),

    // Expressions
    BINARY_EXPRESSION(1300),
    BRACED_EXPRESSION(1301),
    FUNCTION_CALL(1302),
    QUALIFIED_IDENTIFIER(1303),
    MEMBER_ACCESS(1304),
    FIELD_ACCESS(1305),
    METHOD_CALL(1306),
    CHECK_EXPRESSION(1307),
    MAPPING_CONSTRUCTOR(1308),

    // Type descriptors
    RECORD_TYPE_DESCRIPTOR(2000),
    OBJECT_TYPE_DESCRIPTOR(2001),

    // Other
    RETURN_TYPE_DESCRIPTOR(3000),
    PARAMETER(3001),
    EXTERNAL_FUNCTION_BODY(3002),
    RECORD_FIELD(3003),
    RECORD_FIELD_WITH_DEFAULT_VALUE(3004),
    TYPE_REFERENCE(3005),
    RECORD_REST_TYPE(3006),
    POSITIONAL_ARG(3007),
    NAMED_ARG(3008),
    REST_ARG(3009),
    OBJECT_FIELD(3010),
    IMPORT_ORG_NAME(3011),
    MODULE_NAME(3012),
    SUB_MODULE_NAME(3013),
    IMPORT_VERSION(3014),
    IMPORT_SUB_VERSION(3015),
    IMPORT_PREFIX(3016),
    SPECIFIC_FIELD(3017),
    COMPUTED_NAME_FIELD(3018),
    SPREAD_FIELD(3019),

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
