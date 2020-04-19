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

    // Productions
    COMP_UNIT("comp-unit"),
    EOF("eof"),
    TOP_LEVEL_NODE("top-level-node"),
    TOP_LEVEL_NODE_WITHOUT_METADATA("top-level-node-without-metadata"),
    TOP_LEVEL_NODE_WITHOUT_MODIFIER("top-level-node-without-modifier"),
    FUNC_DEFINITION("func-definition"),
    PARAM_LIST("parameters"),
    PARAMETER("parameter"),
    PARAMETER_WITHOUT_ANNOTS("parameter-without-annots"),
    REQUIRED_PARAM("parameter"),
    DEFAULTABLE_PARAM("defaultable-parameter"),
    REST_PARAM("rest-parameter"),
    AFTER_PARAMETER_TYPE("after-parameter-type"),
    PARAMETER_RHS("parameter-rhs"),
    RETURN_TYPE_DESCRIPTOR("return-type-desc"),
    FUNC_BODY("func-body"),
    EXTERNAL_FUNC_BODY("external-func-body"),
    FUNC_BODY_BLOCK("func-body-block"),
    MODULE_TYPE_DEFINITION("type-definition"),
    FIELD_OR_REST_DESCIPTOR_RHS("field-or-rest-descriptor-rhs"),
    FIELD_DESCRIPTOR_RHS("field-descriptor-rhs"),
    RECORD_BODY_START("record-body-start"),
    RECORD_BODY_END("record-body-end"),
    RECORD_FIELD("record-field"),
    RECORD_FIELD_OR_RECORD_END("record-field-orrecord-end"),
    RECORD_FIELD_START("record-field-start"),
    RECORD_FIELD_WITHOUT_METADATA("record-field-without-metadata"),
    TYPE_DESCRIPTOR("type-descriptor"),
    RECORD_TYPE_DESCRIPTOR("record-type-desc"),
    TYPE_REFERENCE("type-reference"),
    ARG_LIST("arguments"),
    ARG("argument"),
    NAMED_OR_POSITIONAL_ARG_RHS("named-or-positional-arg"),
    OBJECT_TYPE_DESCRIPTOR("object-type-desc"),
    OBJECT_MEMBER("object-member"),
    OBJECT_MEMBER_WITHOUT_METADATA("object-member-without-metadata"),
    OBJECT_FUNC_OR_FIELD("object-func-or-field"),
    OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY("object-func-or-field-without-visibility"),
    OBJECT_METHOD_START("object-method-start"),
    OBJECT_FIELD_RHS("object-field-rhs"),
    OBJECT_TYPE_FIRST_QUALIFIER("object-type-qualifier"),
    OBJECT_TYPE_SECOND_QUALIFIER("object-type-second-qualifier"),
    OBJECT_TYPE_DESCRIPTOR_START("object-type-desc-start"),
    IMPORT_DECL("import-decl"),
    IMPORT_ORG_OR_MODULE_NAME("import-org-or-module-name"),
    IMPORT_MODULE_NAME("module-name"),
    IMPORT_VERSION_DECL("import-version-decl"),
    VERSION_NUMBER("sem-ver"),
    IMPORT_SUB_VERSION("import-sub-version"),
    MAJOR_VERSION("major-version"),
    MINOR_VERSION("minor-version"),
    PATCH_VERSION("patch-version"),
    IMPORT_PREFIX("import-prefix"),
    IMPORT_PREFIX_DECL("import-alias"),
    IMPORT_DECL_RHS("import-decl-rhs"),
    AFTER_IMPORT_MODULE_NAME("after-import-module-name"),
    MAJOR_MINOR_VERSION_END("major-minor-version-end"),
    SERVICE_DECL("service-decl"),
    OPTIONAL_SERVICE_NAME("service-rhs"),
    LISTENERS_LIST("listeners-list"),
    RESOURCE_DEF("resource-def"),
    LISTENER_DECL("listener-decl"),
    CONSTANT_DECL("const-decl"),
    CONST_DECL_TYPE("const-decl-type"),
    CONST_DECL_RHS("const-decl-rhs"),
    NIL_TYPE_DESCRIPTOR("nil-type-descriptor"),
    OPTIONAL_TYPE_DESCRIPTOR("optional-type-descriptor"),
    ARRAY_TYPE_DESCRIPTOR("array-type-descriptor"),
    ARRAY_LENGTH("array-length"),
    ANNOT_REFERENCE("annot-reference"),
    ANNOTATIONS("annots"),
    DOC_STRING("doc-string"),
    IDENTIFIER("identifier"),
    QUALIFIED_IDENTIFIER("qualified-identifier"),
    ANNOTATION_DECL("annotation-decl"),
    ANNOT_DECL_OPTIONAL_TYPE("annot-decl-optional-type"),
    ANNOT_DECL_RHS("annot-decl-rhs"),
    ANNOT_OPTIONAL_ATTACH_POINTS("annot-optional-attach-points"),
    ANNOT_ATTACH_POINTS_LIST("annot-attach-points-list"),
    ATTACH_POINT("attach-point"),
    ATTACH_POINT_IDENT("attach-point-ident"),
    SINGLE_KEYWORD_ATTACH_POINT_IDENT("single-keyword-attach-point-ident"),
    IDENT_AFTER_OBJECT_IDENT("ident-after-object-ident"),
    FUNCTION_IDENT("func-ident"),
    FIELD_IDENT("field-ident"),
    OBJECT_IDENT("object-ident"),
    RESOURCE_IDENT("resource-ident"),
    RECORD_IDENT("record-ident"),
    ANNOTATION_TAG("annotation-tag"),
    ATTACH_POINT_END("attach-point-end"),

    // Statements
    STATEMENT("statement"),
    STATEMENT_WITHOUT_ANNOTS("statement-without-annots"),
    ASSIGNMENT_STMT("assignment-stmt"),
    VAR_DECL_STMT("var-decl-stmt"),
    VAR_DECL_STMT_RHS("var-decl-rhs"),
    STATEMENT_START_IDENTIFIER("type-or-var-name"),
    ASSIGNMENT_OR_VAR_DECL_STMT("assign-or-var-decl"),
    ASSIGNMENT_OR_VAR_DECL_STMT_RHS("assign-or-var-decl-rhs"),
    IF_BLOCK("if-block"),
    BLOCK_STMT("block-stmt"),
    ELSE_BLOCK("else-block"),
    ELSE_BODY("else-body"),
    WHILE_BLOCK("while-block"),
    CALL_STMT("call-statement"),
    CALL_STMT_START("call-statement-start"),
    CONTINUE_STATEMENT("continue-statement"),
    BREAK_STATEMENT("break-statement"),
    PANIC_STMT("panic-statement"),
    RETURN_STMT("return-stmt"),
    RETURN_STMT_RHS("return-stmt-rhs"),
    COMPOUND_ASSIGNMENT_STMT("compound-assignment-statement"),
    LOCAL_TYPE_DEFINITION_STMT("local-type-definition-statement"),
    STMT_START_WITH_IDENTIFIER("stmt-start-with-identifier"),
    STMT_START_WITH_EXPR_RHS("stmt-start-with-expr-rhs"),
    EXPRESSION_STATEMENT("expression-statement"),
    EXPRESSION_STATEMENT_START("expression-statement-start"),

    // Keywords
    RETURNS_KEYWORD("returns"),
    TYPE_KEYWORD("type"),
    PUBLIC_KEYWORD("public"),
    PRIVATE_KEYWORD("private"),
    REMOTE_KEYWORD("remote"),
    FUNCTION_KEYWORD("function"),
    EXTERNAL_KEYWORD("external"),
    RECORD_KEYWORD("record"),
    OBJECT_KEYWORD("object"),
    ABSTRACT_KEYWORD("abstract"),
    CLIENT_KEYWORD("client"),
    IF_KEYWORD("if"),
    ELSE_KEYWORD("else"),
    WHILE_KEYWORD("while"),
    CONTINUE_KEYWORD("continue"),
    BREAK_KEYWORD("break"),
    PANIC_KEYWORD("panic"),
    IMPORT_KEYWORD("import"),
    VERSION_KEYWORD("version"),
    AS_KEYWORD("as"),
    RETURN_KEYWORD("return"),
    SERVICE_KEYWORD("service"),
    ON_KEYWORD("on"),
    RESOURCE_KEYWORD("resource"),
    FINAL_KEYWORD("final"),
    LISTENER_KEYWORD("listener"),
    CONST_KEYWORD("const"),
    TYPEOF_KEYWORD("typeof"),
    IS_KEYWORD("is"),
    ANNOTATION_KEYWORD("annotation"),
    SOURCE_KEYWORD("source"),

    // Syntax tokens
    OPEN_PARENTHESIS("("),
    CLOSE_PARENTHESIS(")"),
    OPEN_BRACE("{"),
    CLOSE_BRACE("}"),
    ASSIGN_OP("="),
    SEMICOLON(";"),
    COLON(":"),
    COMMA(","),
    ELLIPSIS("..."),
    QUESTION_MARK("?"),
    ASTERISK("*"),
    CLOSED_RECORD_BODY_START("{|"),
    CLOSED_RECORD_BODY_END("|}"),
    DOT("."),
    OPEN_BRACKET("["),
    CLOSE_BRACKET("]"),
    SLASH("/"),
    AT("@"),
    RIGHT_ARROW("->"),

    // Other terminals
    FUNC_NAME("function-name"),
    VARIABLE_NAME("variable"),
    SIMPLE_TYPE_DESCRIPTOR("simple-type-desc"),
    BINARY_OPERATOR("binary-operator"),
    TYPE_NAME("type-name"),
    FIELD_OR_FUNC_NAME("field-or-func-name"),
    BOOLEAN_LITERAL("boolean-literal"),
    CHECKING_KEYWORD("checking-keyword"),
    SERVICE_NAME("service-name"),
    COMPOUND_BINARY_OPERATOR("compound-binary-operator"),
    UNARY_OPERATOR("unary-operator"),

    // Expressions
    EXPRESSION("expression"),
    TERMINAL_EXPRESSION("terminal-expression"),
    EXPRESSION_RHS("expression-rhs"),
    FUNC_CALL("func-call"),
    BASIC_LITERAL("basic-literal"),
    ACCESS_EXPRESSION("access-expr"),   // method-call, field-access, member-access
    DECIMAL_INTEGER_LITERAL("decimal-int-literal"),
    VARIABLE_REF("var-ref"),
    STRING_LITERAL("string-literal"),
    MAPPING_CONSTRUCTOR("mapping-constructor"),
    MAPPING_FIELD("maping-field"),
    MAPPING_FIELD_NAME("maping-field-name"),
    SPECIFIC_FIELD_RHS("specific-field-rhs"),
    COMPUTED_FIELD_NAME("computed-field-name"),
    TYPEOF_EXPRESSION("typeof-expr"),
    UNARY_EXPRESSION("unary-expr"),
    HEX_INTEGER_LITERAL("hex-integer-literal"),
    IS_EXPRESSION("is-expr"),
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
