/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.toml.internal.parser;

/**
 * Parser rule contexts that represent each point in the grammar.
 * These represents the current scope during the parsing.
 *
 * @since 1.2.0
 */
public enum ParserRuleContext {

    // Productions
    EOF("eof"),
    TOP_LEVEL_NODE("top-level-node"),
    NEWLINE("new-line"),
    NEW_LINE_OR_TOP_LEVEL("new-line-or-top-level-node"),

    ARRAY_VALUE_LIST_START("["),
    ARRAY_VALUE_LIST("array-values"),
    ARRAY_VALUE_START("array-value-start"), // parsing an a value of the array
    ARRAY_VALUE_END("array-value-end"), // parsing the end of the array comma or close
    ARRAY_VALUE_START_OR_VALUE_LIST_END("array-value-start-or-array-values-list-end"), //multi
    ARRAY_VALUE_LIST_END("]"),

    INLINE_TABLE_START("{"),
    INLINE_TABLE_LIST("inline-key-values"),
    INLINE_TABLE_ENTRY_START("inline-table-entry-start"),
    INLINE_TABLE_ENTRY_END("inline-table-entry-end"),
    INLINE_TABLE_START_OR_TABLE_ENTRY_LIST_END("inline-table-start-or-table-entry-list-end"),
    INLINE_TABLE_END("}"),

    KEY_LIST("key-list"),
    KEY_START("key-start"), // parsing an a value of the array
    KEY_END("key-end"), // parsing the end of the array comma or close
    KEY_LIST_END("key-list-end"),

    TABLE_START("["),
    TABLE_END("]"),

    ARRAY_TABLE_FIRST_START("["),
    ARRAY_TABLE_SECOND_START("["),
    ARRAY_TABLE_FIRST_END("]"),
    ARRAY_TABLE_SECOND_END("]"),

    // Syntax tokens
    ASSIGN_OP("="),
    COLON(":"),
    COMMA(","),
    DOT("."),
    SIGN_TOKEN("sign-token"),
    IDENTIFIER_LITERAL("identifier"),

    // Expressions
    DECIMAL_INTEGER_LITERAL("decimal-int-literal"),
    STRING_BODY("string-body"),
    MULTILINE_STRING_BODY("multiline-string-body"),
    LITERAL_STRING_BODY("literal-string-body"),
    MULTILINE_LITERAL_STRING_BODY("multiline-literal-string-body"),
    STRING_CONTENT("string-content"),
    MULTILINE_STRING_CONTENT("multiline-string-content"),
    LITERAL_STRING_CONTENT("literal-string-content"),
    MULTILINE_LITERAL_STRING_CONTENT("multiline-literal-string-content"),
    NIL_LITERAL("nil-literal"),
    DECIMAL_FLOATING_POINT_LITERAL("decimal-floating-point-literal"),
    HEX_INTEGER_LITERAL("hex-int-literal"),
    OCTAL_INTEGER_LITERAL("oct-int-literal"),
    BINARY_INTEGER_LITERAL("bin-int-literal"),
    BOOLEAN_LITERAL("boolean-literal"),
    NUMERICAL_LITERAL("numerical-literal"),

    //TOML
    VALUE("value-toml"),
    START_SQUARE_BRACES("start-square-braces"),
    END_SQUARE_BRACES("end-square-braces"),
    KEY_VALUE_PAIR ("key-value-pair"),
    TOML_TABLE("toml-table"),
    TOML_TABLE_ARRAY("toml-table-array"),

    STRING_START("\""),
    MULTILINE_STRING_START("\""),
    STRING_END("\""),
    MULTILINE_STRING_END("\""),

    LITERAL_STRING_START("'"),
    MULTILINE_LITERAL_STRING_START("'"),
    LITERAL_STRING_END("'"),
    MULTILINE_LITERAL_STRING_END("'"),
    ;

    private final String value;

    ParserRuleContext(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
