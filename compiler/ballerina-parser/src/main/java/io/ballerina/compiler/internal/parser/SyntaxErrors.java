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
package io.ballerina.compiler.internal.parser;

import io.ballerina.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.compiler.internal.diagnostics.DiagnosticWarningCode;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STNodeList;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.internal.syntax.NodeListUtils;
import io.ballerina.compiler.internal.syntax.SyntaxUtils;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class contains utilities to handle syntax errors.
 *
 * @since 2.0.0
 */
public final class SyntaxErrors {

    private SyntaxErrors() {
    }

    public static STNodeDiagnostic createDiagnostic(DiagnosticCode diagnosticCode, Object... args) {
        return STNodeDiagnostic.from(diagnosticCode, args);
    }

    public static <T extends STNode> T addDiagnostic(T node, DiagnosticCode diagnosticCode, Object... args) {
        return addSyntaxDiagnostic(node, createDiagnostic(diagnosticCode, args));
    }

    public static <T extends STNode> T addSyntaxDiagnostic(T node, STNodeDiagnostic diagnostic) {
        return addSyntaxDiagnostics(node, Collections.singletonList(diagnostic));
    }

    @SuppressWarnings("unchecked")
    public static <T extends STNode> T addSyntaxDiagnostics(T node, Collection<STNodeDiagnostic> diagnostics) {
        if (diagnostics.isEmpty()) {
            return node;
        }

        Collection<STNodeDiagnostic> newDiagnostics;
        Collection<STNodeDiagnostic> oldDiagnostics = node.diagnostics();
        if (oldDiagnostics.isEmpty()) {
            newDiagnostics = new ArrayList<>(diagnostics);
        } else {
            // Merge all diagnostics
            newDiagnostics = new ArrayList<>(oldDiagnostics);
            newDiagnostics.addAll(diagnostics);
        }
        return (T) node.modifyWith(newDiagnostics);
    }

    public static STToken createMissingToken(SyntaxKind expectedKind) {
        return STNodeFactory.createMissingToken(expectedKind);
    }

    public static STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind, ParserRuleContext currentCtx) {
        return createMissingTokenWithDiagnostics(expectedKind, getErrorCode(currentCtx));
    }

    public static STToken createMissingDocTokenWithDiagnostics(SyntaxKind expectedKind) {
        return createMissingTokenWithDiagnostics(expectedKind, getDocWarningCode(expectedKind));
    }

    public static STToken createMissingRegExpTokenWithDiagnostics(SyntaxKind expectedKind) {
        return createMissingTokenWithDiagnostics(expectedKind, getRegExpErrorCode(expectedKind));
    }

    public static STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind,
                                                            DiagnosticCode diagnosticCode) {
        List<STNodeDiagnostic> diagnosticList = new ArrayList<>();
        diagnosticList.add(createDiagnostic(diagnosticCode));
        return STNodeFactory.createMissingToken(expectedKind, diagnosticList);
    }

    private static DiagnosticCode getErrorCode(ParserRuleContext currentCtx) {
        return switch (currentCtx) {
            case EXTERNAL_FUNC_BODY -> DiagnosticErrorCode.ERROR_MISSING_EQUAL_TOKEN;
            case FUNC_BODY_BLOCK -> DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACE_TOKEN;
            case FUNC_DEF,
                 FUNC_DEF_OR_FUNC_TYPE,
                 FUNC_TYPE_DESC,
                 FUNC_TYPE_DESC_OR_ANON_FUNC,
                 IDENT_AFTER_OBJECT_IDENT,
                 FUNC_DEF_FIRST_QUALIFIER,
                 FUNC_DEF_SECOND_QUALIFIER,
                 FUNC_TYPE_FIRST_QUALIFIER,
                 FUNC_TYPE_SECOND_QUALIFIER,
                 OBJECT_METHOD_FIRST_QUALIFIER,
                 OBJECT_METHOD_SECOND_QUALIFIER,
                 OBJECT_METHOD_THIRD_QUALIFIER,
                 OBJECT_METHOD_FOURTH_QUALIFIER -> DiagnosticErrorCode.ERROR_MISSING_FUNCTION_KEYWORD;
            case SINGLE_KEYWORD_ATTACH_POINT_IDENT -> DiagnosticErrorCode.ERROR_MISSING_ATTACH_POINT_NAME;
            case SIMPLE_TYPE_DESCRIPTOR -> DiagnosticErrorCode.ERROR_MISSING_BUILTIN_TYPE;
            case REQUIRED_PARAM,
                 VAR_DECL_STMT,
                 ASSIGNMENT_OR_VAR_DECL_STMT,
                 DEFAULTABLE_PARAM,
                 REST_PARAM,
                 TYPE_DESCRIPTOR,
                 OPTIONAL_TYPE_DESCRIPTOR,
                 ARRAY_TYPE_DESCRIPTOR,
                 SIMPLE_TYPE_DESC_IDENTIFIER -> DiagnosticErrorCode.ERROR_MISSING_TYPE_DESC;
            // return var-ref for any kind of terminal expression
            case TYPE_REFERENCE -> DiagnosticErrorCode.ERROR_MISSING_TYPE_REFERENCE;
            case TYPE_NAME,
                 TYPE_REFERENCE_IN_TYPE_INCLUSION,
                 FIELD_ACCESS_IDENTIFIER,
                 CLASS_NAME,
                 FUNC_NAME,
                 VARIABLE_NAME,
                 IMPORT_MODULE_NAME,
                 IMPORT_ORG_OR_MODULE_NAME,
                 IMPORT_PREFIX,
                 VARIABLE_REF,
                 BASIC_LITERAL,
                 IDENTIFIER,
                 QUALIFIED_IDENTIFIER_START_IDENTIFIER,
                 NAMESPACE_PREFIX,
                 IMPLICIT_ANON_FUNC_PARAM,
                 METHOD_NAME,
                 PEER_WORKER_NAME,
                 RECEIVE_FIELD_NAME,
                 WAIT_FIELD_NAME,
                 FIELD_BINDING_PATTERN_NAME,
                 XML_ATOMIC_NAME_IDENTIFIER,
                 MAPPING_FIELD_NAME,
                 WORKER_NAME,
                 NAMED_WORKERS,
                 ANNOTATION_TAG,
                 AFTER_PARAMETER_TYPE,
                 MODULE_ENUM_NAME,
                 ENUM_MEMBER_NAME,
                 TYPED_BINDING_PATTERN_TYPE_RHS,
                 ASSIGNMENT_STMT,
                 XML_NAME,
                 ACCESS_EXPRESSION,
                 BINDING_PATTERN_STARTING_IDENTIFIER,
                 COMPUTED_FIELD_NAME,
                 SIMPLE_BINDING_PATTERN,
                 ERROR_FIELD_BINDING_PATTERN,
                 ERROR_CAUSE_SIMPLE_BINDING_PATTERN,
                 PATH_SEGMENT_IDENT,
                 NAMED_ARG_BINDING_PATTERN,
                 MODULE_VAR_FIRST_QUAL,
                 MODULE_VAR_SECOND_QUAL,
                 MODULE_VAR_THIRD_QUAL,
                 OBJECT_MEMBER_VISIBILITY_QUAL -> DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER;
            case EXPRESSION,
                 TERMINAL_EXPRESSION -> DiagnosticErrorCode.ERROR_MISSING_EXPRESSION;
            case STRING_LITERAL_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_STRING_LITERAL;
            case DECIMAL_INTEGER_LITERAL_TOKEN,
                 SIGNED_INT_OR_FLOAT_RHS -> DiagnosticErrorCode.ERROR_MISSING_DECIMAL_INTEGER_LITERAL;
            case HEX_INTEGER_LITERAL_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_HEX_INTEGER_LITERAL;
            case OBJECT_FIELD_RHS,
                 BINDING_PATTERN_OR_VAR_REF_RHS -> DiagnosticErrorCode.ERROR_MISSING_SEMICOLON_TOKEN;
            case NIL_LITERAL,
                 ERROR_MATCH_PATTERN -> DiagnosticErrorCode.ERROR_MISSING_ERROR_KEYWORD;
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN ->
                    DiagnosticErrorCode.ERROR_MISSING_DECIMAL_FLOATING_POINT_LITERAL;
            case HEX_FLOATING_POINT_LITERAL_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_HEX_FLOATING_POINT_LITERAL;
            case STATEMENT,
                 STATEMENT_WITHOUT_ANNOTS -> DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACE_TOKEN;
            case XML_COMMENT_CONTENT,
                 XML_PI_DATA -> DiagnosticErrorCode.ERROR_MISSING_XML_TEXT_CONTENT;
            default -> getSeperatorTokenErrorCode(currentCtx);
        };
    }

    private static DiagnosticCode getSeperatorTokenErrorCode(ParserRuleContext ctx) {
        return switch (ctx) {
            case BITWISE_AND_OPERATOR -> DiagnosticErrorCode.ERROR_MISSING_BITWISE_AND_TOKEN;
            case EQUAL_OR_RIGHT_ARROW,
                 ASSIGN_OP -> DiagnosticErrorCode.ERROR_MISSING_EQUAL_TOKEN;
            case BINARY_OPERATOR,
                 UNARY_OPERATOR,
                 COMPOUND_BINARY_OPERATOR,
                 UNARY_EXPRESSION,
                 EXPRESSION_RHS,
                 PLUS_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_BINARY_OPERATOR;
            case CLOSE_BRACE -> DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACE_TOKEN;
            case CLOSE_PARENTHESIS,
                 ARG_LIST_CLOSE_PAREN -> DiagnosticErrorCode.ERROR_MISSING_CLOSE_PAREN_TOKEN;
            case COMMA,
                 ERROR_MESSAGE_BINDING_PATTERN_END_COMMA,
                 ERROR_MESSAGE_MATCH_PATTERN_END_COMMA -> DiagnosticErrorCode.ERROR_MISSING_COMMA_TOKEN;
            case OPEN_BRACE -> DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACE_TOKEN;
            case OPEN_PARENTHESIS,
                 ARG_LIST_OPEN_PAREN,
                 PARENTHESISED_TYPE_DESC_START -> DiagnosticErrorCode.ERROR_MISSING_OPEN_PAREN_TOKEN;
            case SEMICOLON,
                 OBJECT_FIELD_RHS -> DiagnosticErrorCode.ERROR_MISSING_SEMICOLON_TOKEN;
            case ASTERISK -> DiagnosticErrorCode.ERROR_MISSING_ASTERISK_TOKEN;
            case CLOSED_RECORD_BODY_END -> DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACE_PIPE_TOKEN;
            case CLOSED_RECORD_BODY_START -> DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACE_PIPE_TOKEN;
            case ELLIPSIS -> DiagnosticErrorCode.ERROR_MISSING_ELLIPSIS_TOKEN;
            case QUESTION_MARK -> DiagnosticErrorCode.ERROR_MISSING_QUESTION_MARK_TOKEN;
            case CLOSE_BRACKET -> DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACKET_TOKEN;
            case DOT,
                 METHOD_CALL_DOT -> DiagnosticErrorCode.ERROR_MISSING_DOT_TOKEN;
            case OPEN_BRACKET,
                 TUPLE_TYPE_DESC_START -> DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACKET_TOKEN;
            case SLASH,
                 ABSOLUTE_PATH_SINGLE_SLASH,
                 RESOURCE_METHOD_CALL_SLASH_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_SLASH_TOKEN;
            case COLON,
                 VAR_REF_COLON,
                 TYPE_REF_COLON -> DiagnosticErrorCode.ERROR_MISSING_COLON_TOKEN;
            case AT -> DiagnosticErrorCode.ERROR_MISSING_AT_TOKEN;
            case RIGHT_ARROW -> DiagnosticErrorCode.ERROR_MISSING_RIGHT_ARROW_TOKEN;
            case GT,
                 GT_TOKEN,
                 XML_START_OR_EMPTY_TAG_END,
                 XML_ATTRIBUTES,
                 INFERRED_TYPEDESC_DEFAULT_END_GT -> DiagnosticErrorCode.ERROR_MISSING_GT_TOKEN;
            case LT,
                 LT_TOKEN,
                 XML_START_OR_EMPTY_TAG,
                 XML_END_TAG,
                 INFERRED_TYPEDESC_DEFAULT_START_LT,
                 STREAM_TYPE_PARAM_START_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_LT_TOKEN;
            case SYNC_SEND_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_SYNC_SEND_TOKEN;
            case ANNOT_CHAINING_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_ANNOT_CHAINING_TOKEN;
            case OPTIONAL_CHAINING_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_OPTIONAL_CHAINING_TOKEN;
            case DOT_LT_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_DOT_LT_TOKEN;
            case SLASH_LT_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_SLASH_LT_TOKEN;
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN ->
                    DiagnosticErrorCode.ERROR_MISSING_DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN;
            case SLASH_ASTERISK_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_SLASH_ASTERISK_TOKEN;
            case MINUS_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_MINUS_TOKEN;
            case LEFT_ARROW_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_LEFT_ARROW_TOKEN;
            case TEMPLATE_END,
                 TEMPLATE_START,
                 XML_CONTENT,
                 XML_TEXT -> DiagnosticErrorCode.ERROR_MISSING_BACKTICK_TOKEN;
            case XML_COMMENT_START -> DiagnosticErrorCode.ERROR_MISSING_XML_COMMENT_START_TOKEN;
            case XML_COMMENT_END -> DiagnosticErrorCode.ERROR_MISSING_XML_COMMENT_END_TOKEN;
            case XML_PI,
                 XML_PI_START -> DiagnosticErrorCode.ERROR_MISSING_XML_PI_START_TOKEN;
            case XML_PI_END -> DiagnosticErrorCode.ERROR_MISSING_XML_PI_END_TOKEN;
            case XML_QUOTE_END,
                 XML_QUOTE_START -> DiagnosticErrorCode.ERROR_MISSING_DOUBLE_QUOTE_TOKEN;
            case INTERPOLATION_START_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_INTERPOLATION_START_TOKEN;
            case EXPR_FUNC_BODY_START,
                 RIGHT_DOUBLE_ARROW -> DiagnosticErrorCode.ERROR_MISSING_RIGHT_DOUBLE_ARROW_TOKEN;
            case XML_CDATA_END -> DiagnosticErrorCode.ERROR_MISSING_XML_CDATA_END_TOKEN;
            default -> getKeywordErrorCode(ctx);
        };
    }

    private static DiagnosticCode getKeywordErrorCode(ParserRuleContext ctx) {
        return switch (ctx) {
            case PUBLIC_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_PUBLIC_KEYWORD;
            case PRIVATE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_PRIVATE_KEYWORD;
            case ABSTRACT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_ABSTRACT_KEYWORD;
            case CLIENT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_CLIENT_KEYWORD;
            case IMPORT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_IMPORT_KEYWORD;
            case FUNCTION_KEYWORD,
                 FUNCTION_IDENT,
                 OPTIONAL_PEER_WORKER,
                 DEFAULT_WORKER_NAME_IN_ASYNC_SEND -> DiagnosticErrorCode.ERROR_MISSING_FUNCTION_KEYWORD;
            case CONST_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_CONST_KEYWORD;
            case LISTENER_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_LISTENER_KEYWORD;
            case SERVICE_KEYWORD,
                 SERVICE_IDENT,
                 SERVICE_DECL_QUALIFIER -> DiagnosticErrorCode.ERROR_MISSING_SERVICE_KEYWORD;
            case XMLNS_KEYWORD,
                 XML_NAMESPACE_DECLARATION -> DiagnosticErrorCode.ERROR_MISSING_XMLNS_KEYWORD;
            case ANNOTATION_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_ANNOTATION_KEYWORD;
            case TYPE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_TYPE_KEYWORD;
            case RECORD_KEYWORD,
                 RECORD_FIELD,
                 RECORD_IDENT -> DiagnosticErrorCode.ERROR_MISSING_RECORD_KEYWORD;
            case OBJECT_KEYWORD,
                 OBJECT_IDENT,
                 OBJECT_TYPE_DESCRIPTOR,
                 FIRST_OBJECT_CONS_QUALIFIER,
                 SECOND_OBJECT_CONS_QUALIFIER,
                 FIRST_OBJECT_TYPE_QUALIFIER,
                 SECOND_OBJECT_TYPE_QUALIFIER -> DiagnosticErrorCode.ERROR_MISSING_OBJECT_KEYWORD;
            case AS_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_AS_KEYWORD;
            case ON_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_ON_KEYWORD;
            case FINAL_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_FINAL_KEYWORD;
            case SOURCE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_SOURCE_KEYWORD;
            case WORKER_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_WORKER_KEYWORD;
            case FIELD_IDENT -> DiagnosticErrorCode.ERROR_MISSING_FIELD_KEYWORD;
            case RETURNS_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_RETURNS_KEYWORD;
            case RETURN_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_RETURN_KEYWORD;
            case EXTERNAL_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_EXTERNAL_KEYWORD;
            case BOOLEAN_LITERAL -> DiagnosticErrorCode.ERROR_MISSING_TRUE_KEYWORD;
            case IF_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_IF_KEYWORD;
            case ELSE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_ELSE_KEYWORD;
            case WHILE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_WHILE_KEYWORD;
            case CHECKING_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_CHECK_KEYWORD;
            case PANIC_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_PANIC_KEYWORD;
            case CONTINUE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_CONTINUE_KEYWORD;
            case BREAK_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_BREAK_KEYWORD;
            case TYPEOF_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_TYPEOF_KEYWORD;
            case IS_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_IS_KEYWORD;
            case NULL_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_NULL_KEYWORD;
            case LOCK_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_LOCK_KEYWORD;
            case FORK_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_FORK_KEYWORD;
            case TRAP_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_TRAP_KEYWORD;
            case IN_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_IN_KEYWORD;
            case FOREACH_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_FOREACH_KEYWORD;
            case TABLE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_TABLE_KEYWORD;
            case KEY_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_KEY_KEYWORD;
            case LET_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_LET_KEYWORD;
            case NEW_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_NEW_KEYWORD;
            case FROM_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_FROM_KEYWORD;
            case WHERE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_WHERE_KEYWORD;
            case SELECT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_SELECT_KEYWORD;
            case START_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_START_KEYWORD;
            case FLUSH_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_FLUSH_KEYWORD;
            case WAIT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_WAIT_KEYWORD;
            case DO_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_DO_KEYWORD;
            case TRANSACTION_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_TRANSACTION_KEYWORD;
            case TRANSACTIONAL_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_TRANSACTIONAL_KEYWORD;
            case COMMIT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_COMMIT_KEYWORD;
            case ROLLBACK_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_ROLLBACK_KEYWORD;
            case RETRY_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_RETRY_KEYWORD;
            case ENUM_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_ENUM_KEYWORD;
            case BASE16_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_BASE16_KEYWORD;
            case BASE64_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_BASE64_KEYWORD;
            case MATCH_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_MATCH_KEYWORD;
            case CONFLICT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_CONFLICT_KEYWORD;
            case LIMIT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_LIMIT_KEYWORD;
            case ORDER_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_ORDER_KEYWORD;
            case BY_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_BY_KEYWORD;
            case GROUP_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_GROUP_KEYWORD;
            case ORDER_DIRECTION -> DiagnosticErrorCode.ERROR_MISSING_ASCENDING_KEYWORD;
            case JOIN_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_JOIN_KEYWORD;
            case OUTER_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_OUTER_KEYWORD;
            case FAIL_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_FAIL_KEYWORD;
            case PIPE,
                 UNION_OR_INTERSECTION_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_PIPE_TOKEN;
            case EQUALS_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_EQUALS_KEYWORD;
            case REMOTE_IDENT -> DiagnosticErrorCode.ERROR_MISSING_REMOTE_KEYWORD;

            // Type keywords
            case STRING_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_STRING_KEYWORD;
            case XML_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_XML_KEYWORD;
            case RE_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_RE_KEYWORD;
            case VAR_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_VAR_KEYWORD;
            case MAP_KEYWORD,
                 NAMED_WORKER_DECL,
                 MAP_TYPE_DESCRIPTOR -> DiagnosticErrorCode.ERROR_MISSING_MAP_KEYWORD;
            case ERROR_KEYWORD,
                 ERROR_BINDING_PATTERN,
                 PARAMETERIZED_TYPE -> DiagnosticErrorCode.ERROR_MISSING_ERROR_KEYWORD;
            case STREAM_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_STREAM_KEYWORD;
            case READONLY_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_READONLY_KEYWORD;
            case DISTINCT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_DISTINCT_KEYWORD;
            case CLASS_KEYWORD,
                 FIRST_CLASS_TYPE_QUALIFIER,
                 SECOND_CLASS_TYPE_QUALIFIER,
                 THIRD_CLASS_TYPE_QUALIFIER,
                 FOURTH_CLASS_TYPE_QUALIFIER -> DiagnosticErrorCode.ERROR_MISSING_CLASS_KEYWORD;
            case COLLECT_KEYWORD -> DiagnosticErrorCode.ERROR_MISSING_COLLECT_KEYWORD;
            default -> DiagnosticErrorCode.ERROR_SYNTAX_ERROR;
        };
    }

    private static DiagnosticCode getDocWarningCode(SyntaxKind expectedKind) {
        return switch (expectedKind) {
            case HASH_TOKEN -> DiagnosticWarningCode.WARNING_MISSING_HASH_TOKEN;
            case BACKTICK_TOKEN -> DiagnosticWarningCode.WARNING_MISSING_SINGLE_BACKTICK_TOKEN;
            case DOUBLE_BACKTICK_TOKEN -> DiagnosticWarningCode.WARNING_MISSING_DOUBLE_BACKTICK_TOKEN;
            case TRIPLE_BACKTICK_TOKEN -> DiagnosticWarningCode.WARNING_MISSING_TRIPLE_BACKTICK_TOKEN;
            case IDENTIFIER_TOKEN -> DiagnosticWarningCode.WARNING_MISSING_IDENTIFIER_TOKEN;
            case OPEN_PAREN_TOKEN -> DiagnosticWarningCode.WARNING_MISSING_OPEN_PAREN_TOKEN;
            case CLOSE_PAREN_TOKEN -> DiagnosticWarningCode.WARNING_MISSING_CLOSE_PAREN_TOKEN;
            case MINUS_TOKEN -> DiagnosticWarningCode.WARNING_MISSING_HYPHEN_TOKEN;
            case PARAMETER_NAME -> DiagnosticWarningCode.WARNING_MISSING_PARAMETER_NAME;
            case CODE_CONTENT -> DiagnosticWarningCode.WARNING_MISSING_CODE_REFERENCE;
            default -> DiagnosticWarningCode.WARNING_SYNTAX_WARNING;
        };
    }

    private static DiagnosticCode getRegExpErrorCode(SyntaxKind expectedKind) {
        return switch (expectedKind) {
            case CLOSE_PAREN_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_CLOSE_PAREN_TOKEN;
            case CLOSE_BRACKET_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACKET_TOKEN;
            case OPEN_BRACE_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACE_TOKEN;
            case CLOSE_BRACE_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACE_TOKEN;
            case COLON_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_COLON_TOKEN;
            case RE_UNICODE_PROPERTY_VALUE -> DiagnosticErrorCode.ERROR_MISSING_RE_UNICODE_PROPERTY_VALUE;
            case DIGIT -> DiagnosticErrorCode.ERROR_MISSING_RE_QUANTIFIER_DIGIT;
            case BITWISE_XOR_TOKEN -> DiagnosticErrorCode.ERROR_INVALID_RE_SYNTAX_CHAR;
            case BACK_SLASH_TOKEN -> DiagnosticErrorCode.ERROR_MISSING_BACKSLASH;
            default -> DiagnosticErrorCode.ERROR_SYNTAX_ERROR;
        };
    }

    /**
     * Update the all nodes inside {@code STNodeList} with a given diagnostic.
     *
     * @param nodeList  the STNodeList to be updated
     * @param errorCode the invalid node
     * @return updated STNodeList as a STNode
     */
    public static STNode updateAllNodesInNodeListWithDiagnostic(STNodeList nodeList, DiagnosticErrorCode errorCode) {
        List<STNode> newList = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            STNode updatedNode = addDiagnostic(nodeList.get(i), errorCode);
            newList.add(updatedNode);
        }

        return STNodeFactory.createNodeList(newList);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as leading minutiae.
     *
     * @param toClone     the node to be cloned
     * @param invalidNode the invalid node
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithLeadingInvalidNodeMinutiae(STNode toClone, STNode invalidNode) {
        return cloneWithLeadingInvalidNodeMinutiae(toClone, invalidNode, null);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as leading minutiae.
     *
     * @param toClone        the node to be cloned
     * @param invalidNode    the invalid node
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithLeadingInvalidNodeMinutiae(STNode toClone,
                                                             STNode invalidNode,
                                                             @Nullable DiagnosticCode diagnosticCode,
                                                             Object... args) {
        STToken firstToken = toClone.firstToken();
        STToken firstTokenWithInvalidNodeMinutiae = cloneWithLeadingInvalidNodeMinutiae(firstToken,
                invalidNode, diagnosticCode, args);
        return toClone.replace(firstToken, firstTokenWithInvalidNodeMinutiae);
    }

    /**
     * Clone the given {@code STToken} with the invalid node as leading minutiae.
     *
     * @param toClone        the token to be cloned
     * @param invalidNode    the invalid node
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a cloned token with the given invalidNode minutiae
     */
    public static STToken cloneWithLeadingInvalidNodeMinutiae(STToken toClone,
                                                              STNode invalidNode,
                                                              DiagnosticCode diagnosticCode,
                                                              Object... args) {
        List<STNode> minutiaeList = convertInvalidNodeToMinutiae(invalidNode, diagnosticCode, args);
        STNodeList leadingMinutiae = (STNodeList) toClone.leadingMinutiae();
        leadingMinutiae = leadingMinutiae.addAll(0, minutiaeList);
        return toClone.modifyWith(leadingMinutiae, toClone.trailingMinutiae());
    }

    /**
     * Clone the given {@code STNode} with the invalid node as trailing minutiae.
     *
     * @param toClone     the node to be cloned
     * @param invalidNode the invalid node
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithTrailingInvalidNodeMinutiae(STNode toClone, STNode invalidNode) {
        return cloneWithTrailingInvalidNodeMinutiae(toClone, invalidNode, null);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as trailing minutiae.
     *
     * @param toClone        the node to be cloned
     * @param invalidNode    the invalid node
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithTrailingInvalidNodeMinutiae(STNode toClone,
                                                              STNode invalidNode,
                                                              @Nullable DiagnosticCode diagnosticCode,
                                                              Object... args) {
        STToken lastToken = toClone.lastToken();
        STToken lastTokenWithInvalidNodeMinutiae = cloneWithTrailingInvalidNodeMinutiae(lastToken,
                invalidNode, diagnosticCode, args);
        return toClone.replace(lastToken, lastTokenWithInvalidNodeMinutiae);
    }

    /**
     * Clone the given {@code STToken} with the invalid node as trailing minutiae.
     *
     * @param toClone        the token to be cloned
     * @param invalidNode    the invalid node
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a cloned token with the given invalidNode minutiae
     */
    public static STToken cloneWithTrailingInvalidNodeMinutiae(STToken toClone,
                                                               STNode invalidNode,
                                                               DiagnosticCode diagnosticCode,
                                                               Object... args) {
        List<STNode> minutiaeList = convertInvalidNodeToMinutiae(invalidNode, diagnosticCode, args);
        STNodeList trailingMinutiae = (STNodeList) toClone.trailingMinutiae();
        trailingMinutiae = trailingMinutiae.addAll(minutiaeList);
        return toClone.modifyWith(toClone.leadingMinutiae(), trailingMinutiae);
    }

    /**
     * Converts the invalid node into a list of {@code STMinutiae} nodes.
     * <p>
     * Here are the steps:
     * <br/>
     * 1) Iterates through all the tokens in the invalid node.
     * <br/>
     * 2) For the first token, add invalid node diagnostic.
     * <br/>
     * For each token:
     * <br/>
     * 3) Add the leading minutiae to the list.
     * <br/>
     * 4) Create a new token without leading or trailing minutiae and add it to the list.
     * <br/>
     * 5) Add the trailing minutiae to the list.
     *
     * @param invalidNode    the invalid node to be converted
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a lit of {@code STMinutiae} nodes
     */
    private static List<STNode> convertInvalidNodeToMinutiae(STNode invalidNode,
                                                             DiagnosticCode diagnosticCode,
                                                             Object... args) {
        List<STNode> minutiaeList = new ArrayList<>();
        List<STToken> tokens = invalidNode.tokens();
        for (STToken token : tokens) {
            addMinutiaeToList(minutiaeList, token.leadingMinutiae());
            if (!token.isMissing()) {
                if (diagnosticCode != null) {
                    // Add diagnostic to the first invalid token
                    token = addDiagnostic(token, diagnosticCode, args);
                    diagnosticCode = null;
                }
                STToken tokenWithNoMinutiae = token.modifyWith(
                        STNodeFactory.createEmptyNodeList(), STNodeFactory.createEmptyNodeList());
                minutiaeList.add(STNodeFactory.createInvalidNodeMinutiae(tokenWithNoMinutiae));
            }
            addMinutiaeToList(minutiaeList, token.trailingMinutiae());
        }
        return minutiaeList;
    }

    private static void addMinutiaeToList(List<STNode> list, STNode minutiae) {
        if (!NodeListUtils.isSTNodeList(minutiae)) {
            list.add(minutiae);
            return;
        }

        STNodeList minutiaeList = (STNodeList) minutiae;
        for (int index = 0; index < minutiaeList.size(); index++) {
            STNode element = minutiaeList.get(index);
            if (SyntaxUtils.isSTNodePresent(element)) {
                list.add(element);
            }
        }
    }
}
