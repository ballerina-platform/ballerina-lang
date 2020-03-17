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
package io.ballerinalang.compiler.internal.parser;

/**
 * Parser rule contexts that represent each point in the grammar.
 * These represents the current scope during the parsing.
 * 
 * @since 1.2.0
 */
public enum ParserRuleContext {

    // productions
    COMP_UNIT("comp-unit"),
    TOP_LEVEL_NODE_WITH_MODIFIER("top-level-node-with-modifier"),
    TOP_LEVEL_NODE("top-level-node"),
    FUNC_DEFINITION("func-definition"),
    PARAM_LIST("parameters"),
    REQUIRED_PARAM("parameter"),
    DEFAULTABLE_PARAM("defaultable-parameter"),
    REST_PARAM("rest-parameter"),
    AFTER_PARAMETER_TYPE("after-parameter-type"),
    PARAMETER_RHS("parameter-rhs"),
    ANNOTATION_ATTACHMENT("annotation"),
    RETURN_TYPE_DESCRIPTOR("return-type-desc"),
    FUNC_BODY("func-body"),
    EXTERNAL_FUNC_BODY("external-func-body"),
    FUNC_BODY_BLOCK("func-body-block"),
    STATEMENT("statement"),
    ASSIGNMENT_STMT("assignment-stmt"),
    VAR_DECL_STMT("var-decl-stmt"),
    VAR_DECL_STMT_RHS("var-decl-rhs"),
    TYPE_OR_VAR_NAME("type-or-var-name"),
    ASSIGNMENT_OR_VAR_DECL_STMT_RHS("assign-or-var-decl-rhs"),
    MODULE_TYPE_DEFINITION("type-definition"),
    FIELD_OR_REST_DESCIPTOR_RHS("field-or-rest-descriptor-rhs"),
    FIELD_DESCRIPTOR_RHS("field-descriptor-rhs"),
    RECORD_BODY_START("record-body-start"),
    RECORD_BODY_END("record-body-end"),
    RECORD_FIELD("record-field"),
    TYPE_DESCRIPTOR("type-descriptor"),
    RECORD_TYPE_DESCRIPTOR("record-type-desc"),
    TYPE_REFERENCE("type-reference"),
    ARG_LIST("arguments"),
    ARG("argument"),
    NAMED_OR_POSITIONAL_ARG_RHS("named-or-positional-arg"),

    // terminals
    PUBLIC("public"),
    FUNCTION_KEYWORD("function"),
    FUNC_NAME("function-name"),
    OPEN_PARENTHESIS("("),
    CLOSE_PARENTHESIS(")"),
    RETURNS_KEYWORD("returns"),
    TYPE_KEYWORD("type"),
    SIMPLE_TYPE_DESCRIPTOR("simple-type-desc"),
    OPEN_BRACE("{"),
    CLOSE_BRACE("}"),
    ASSIGN_OP("="),
    SEMICOLON(";"),
    EXTERNAL_KEYWORD("external"), 
    VARIABLE_NAME("variable"),
    BINARY_OPERATOR("binary-operator"),
    COMMA(","),
    ELLIPSIS("..."),
    RECORD_KEYWORD("record"),
    QUESTION_MARK("?"),
    TYPE_NAME("type-name"),
    ASTERISK("*"),
    CLOSED_RECORD_BODY_START("{|"),
    CLOSED_RECORD_BODY_END("|}"),

    // expressions
    EXPRESSION("expression"),
    BINARY_EXPR_RHS("expression-rhs"),
    FUNC_CALL("func-call"),
    EXPRESSION_END("expr-end"),
    ;

    private String value;

    ParserRuleContext(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
