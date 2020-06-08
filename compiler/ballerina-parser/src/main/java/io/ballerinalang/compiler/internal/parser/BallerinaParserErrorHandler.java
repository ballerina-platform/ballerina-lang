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

import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayDeque;

/**
 * <p>
 * Responsible for recovering from a parser error.
 *
 * When an unexpected token is reached, error handler will try inserting/removing a token from the current head, and see
 * how far the parser can successfully progress. After fixing the current head and trying to progress, if it encounters
 * more errors, then it will try to fix those as well. All possible combinations of insertions and deletions will be
 * tried out for such errors. Once all possible paths are discovered, pick the optimal combination that leads to the
 * best recovery. Finally, apply the best solution and continue the parsing.
 * </p>
 * e.g.:
 * If the best combination of fixes was <code>[insert, insert, remove, remove]</code>, then apply only the first
 * fix and continue.
 * <ul>
 * <li>
 * If the fix was a ‘remove’ - then consume the token stream once, and continue from the same rule again.
 * </li>
 * <li>
 * If the fix was an ‘insert’ - then insert the missing node, and continue from the next rule, without consuming the
 * token stream.
 * </li>
 * </ul>
 *
 * @since 1.2.0
 */
public class BallerinaParserErrorHandler extends AbstractParserErrorHandler {

    /**
     * FUNC_DEF_OR_FUNC_TYPE --> When a func-def and func-type-desc are possible.
     * e.g: start of a module level construct that starts with 'function' keyword.
     */
    private static final ParserRuleContext[] FUNC_TYPE_OR_DEF_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.FUNC_BODY_OR_TYPE_DESC_RHS };

    private static final ParserRuleContext[] FUNC_BODY_OR_TYPE_DESC_RHS =
            { ParserRuleContext.FUNC_BODY, ParserRuleContext.MODULE_LEVEL_AMBIGUOUS_FUNC_TYPE_DESC_RHS };

    /**
     * FUNC_DEF --> When only function definitions are possible. eg: resource function.
     */
    private static final ParserRuleContext[] FUNC_DEF_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.FUNC_BODY };

    private static final ParserRuleContext[] FUNC_BODY =
            { ParserRuleContext.FUNC_BODY_BLOCK, ParserRuleContext.EXTERNAL_FUNC_BODY };

    private static final ParserRuleContext[] OBJECT_FUNC_BODY =
            { ParserRuleContext.SEMICOLON, ParserRuleContext.EXTERNAL_FUNC_BODY };

    /**
     * ANNON_FUNC--> When a anonymous function is possible.
     */
    private static final ParserRuleContext[] ANNON_FUNC_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.ANON_FUNC_BODY };

    private static final ParserRuleContext[] ANON_FUNC_BODY =
            { ParserRuleContext.FUNC_BODY_BLOCK, ParserRuleContext.EXPLICIT_ANON_FUNC_EXPR_BODY_START };

    /**
     * FUNC_TYPE --> When a only function type is possible.
     */
    private static final ParserRuleContext[] FUNC_TYPE_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.FUNC_TYPE_DESC_END };

    private static final ParserRuleContext[] FUNC_TYPE_OR_ANON_FUNC_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY };

    private static final ParserRuleContext[] FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY =
            { ParserRuleContext.ANON_FUNC_BODY, ParserRuleContext.STMT_LEVEL_AMBIGUOUS_FUNC_TYPE_DESC_RHS };

    private static final ParserRuleContext[] WORKER_NAME_RHS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.BLOCK_STMT };

    // We add named-worker-decl also as a statement. This is because we let having a named-worker
    // in all places a statement can be added during parsing, but then validates it based on the
    // context after the parsing the node is complete. This is to provide better error messages.
    private static final ParserRuleContext[] STATEMENTS = { ParserRuleContext.CLOSE_BRACE,
            ParserRuleContext.ASSIGNMENT_STMT, ParserRuleContext.VAR_DECL_STMT, ParserRuleContext.IF_BLOCK,
            ParserRuleContext.WHILE_BLOCK, ParserRuleContext.CALL_STMT, ParserRuleContext.PANIC_STMT,
            ParserRuleContext.CONTINUE_STATEMENT, ParserRuleContext.BREAK_STATEMENT, ParserRuleContext.RETURN_STMT,
            ParserRuleContext.COMPOUND_ASSIGNMENT_STMT, ParserRuleContext.LOCAL_TYPE_DEFINITION_STMT,
            ParserRuleContext.EXPRESSION_STATEMENT, ParserRuleContext.LOCK_STMT, ParserRuleContext.BLOCK_STMT,
            ParserRuleContext.NAMED_WORKER_DECL, ParserRuleContext.FORK_STMT, ParserRuleContext.FOREACH_STMT,
            ParserRuleContext.XML_NAMESPACE_DECLARATION, ParserRuleContext.TRANSACTION_STMT,
            ParserRuleContext.RETRY_STMT, ParserRuleContext.ROLLBACK_STMT };

    private static final ParserRuleContext[] VAR_DECL_RHS =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] TOP_LEVEL_NODE = { ParserRuleContext.DOC_STRING,
            ParserRuleContext.ANNOTATIONS, ParserRuleContext.PUBLIC_KEYWORD, ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE,
            ParserRuleContext.MODULE_TYPE_DEFINITION, ParserRuleContext.IMPORT_DECL, ParserRuleContext.LISTENER_DECL,
            ParserRuleContext.CONSTANT_DECL, ParserRuleContext.VAR_DECL_STMT, ParserRuleContext.SERVICE_DECL,
            ParserRuleContext.ANNOTATION_DECL, ParserRuleContext.XML_NAMESPACE_DECLARATION,
            ParserRuleContext.MODULE_ENUM_DECLARATION, ParserRuleContext.EOF };

    private static final ParserRuleContext[] TOP_LEVEL_NODE_WITHOUT_METADATA = new ParserRuleContext[] {
            ParserRuleContext.PUBLIC_KEYWORD, ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE,
            ParserRuleContext.MODULE_TYPE_DEFINITION, ParserRuleContext.IMPORT_DECL, ParserRuleContext.SERVICE_DECL,
            ParserRuleContext.LISTENER_DECL, ParserRuleContext.CONSTANT_DECL, ParserRuleContext.VAR_DECL_STMT,
            ParserRuleContext.ANNOTATION_DECL, ParserRuleContext.XML_NAMESPACE_DECLARATION,
            ParserRuleContext.MODULE_ENUM_DECLARATION, ParserRuleContext.EOF };

    private static final ParserRuleContext[] TOP_LEVEL_NODE_WITHOUT_MODIFIER =
            { ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE, ParserRuleContext.MODULE_TYPE_DEFINITION,
                    ParserRuleContext.IMPORT_DECL, ParserRuleContext.SERVICE_DECL, ParserRuleContext.LISTENER_DECL,
                    ParserRuleContext.CONSTANT_DECL, ParserRuleContext.ANNOTATION_DECL, ParserRuleContext.VAR_DECL_STMT,
                    ParserRuleContext.XML_NAMESPACE_DECLARATION, ParserRuleContext.MODULE_ENUM_DECLARATION,
                    ParserRuleContext.EOF };

    private static final ParserRuleContext[] TYPE_OR_VAR_NAME =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN };

    private static final ParserRuleContext[] ASSIGNMENT_OR_VAR_DECL_SECOND_TOKEN =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] FIELD_DESCRIPTOR_RHS =
            { ParserRuleContext.SEMICOLON, ParserRuleContext.QUESTION_MARK, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] FIELD_OR_REST_DESCIPTOR_RHS =
            { ParserRuleContext.ELLIPSIS, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] RECORD_BODY_START =
            { ParserRuleContext.CLOSED_RECORD_BODY_START, ParserRuleContext.OPEN_BRACE };

    private static final ParserRuleContext[] RECORD_BODY_END =
            { ParserRuleContext.CLOSED_RECORD_BODY_END, ParserRuleContext.CLOSE_BRACE };

    // Give object the higher priority over records, since record body is a subset of object body.
    // Array, optional and union type descriptors are not added to the list since they are left recursive.
    private static final ParserRuleContext[] TYPE_DESCRIPTORS = { ParserRuleContext.SIMPLE_TYPE_DESCRIPTOR,
            ParserRuleContext.OBJECT_TYPE_DESCRIPTOR, ParserRuleContext.RECORD_TYPE_DESCRIPTOR,
            ParserRuleContext.NIL_TYPE_DESCRIPTOR, ParserRuleContext.PARAMETERIZED_TYPE,
            ParserRuleContext.ERROR_KEYWORD, ParserRuleContext.STREAM_KEYWORD, ParserRuleContext.TABLE_KEYWORD,
            ParserRuleContext.FUNC_TYPE_DESC, ParserRuleContext.PARENTHESISED_TYPE_DESC_START,
            ParserRuleContext.CONSTANT_EXPRESSION, ParserRuleContext.TUPLE_TYPE_DESC_START };

    private static final ParserRuleContext[] RECORD_FIELD_OR_RECORD_END =
            { ParserRuleContext.RECORD_BODY_END, ParserRuleContext.RECORD_FIELD };

    private static final ParserRuleContext[] RECORD_FIELD_START =
            { ParserRuleContext.ANNOTATIONS, ParserRuleContext.ASTERISK, ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD };

    private static final ParserRuleContext[] RECORD_FIELD_WITHOUT_METADATA =
            { ParserRuleContext.ASTERISK, ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD };

    private static final ParserRuleContext[] ARG_START_OR_ARG_LIST_END =
            { ParserRuleContext.ARG_LIST_END, ParserRuleContext.ARG_START };

    private static final ParserRuleContext[] ARG_START =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.ELLIPSIS, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] ARG_END = { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] NAMED_OR_POSITIONAL_ARG_RHS =
            { ParserRuleContext.ARG_END, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] OBJECT_FIELD_RHS =
            { ParserRuleContext.SEMICOLON, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] OBJECT_MEMBER_START =
            { ParserRuleContext.DOC_STRING, ParserRuleContext.ANNOTATIONS, ParserRuleContext.ASTERISK,
                    ParserRuleContext.OBJECT_FUNC_OR_FIELD, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] OBJECT_MEMBER_WITHOUT_METADATA =
            { ParserRuleContext.ASTERISK, ParserRuleContext.OBJECT_FUNC_OR_FIELD, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] OBJECT_FUNC_OR_FIELD = { ParserRuleContext.PUBLIC_KEYWORD,
            ParserRuleContext.PRIVATE_KEYWORD, ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY };

    private static final ParserRuleContext[] OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY =
            { ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER, ParserRuleContext.OBJECT_METHOD_START };

    private static final ParserRuleContext[] OBJECT_METHOD_START =
            { ParserRuleContext.REMOTE_KEYWORD, ParserRuleContext.FUNCTION_KEYWORD };

    private static final ParserRuleContext[] OBJECT_TYPE_DESCRIPTOR_START =
            { ParserRuleContext.OBJECT_TYPE_FIRST_QUALIFIER, ParserRuleContext.OBJECT_KEYWORD };

    private static final ParserRuleContext[] ELSE_BODY = { ParserRuleContext.IF_BLOCK, ParserRuleContext.OPEN_BRACE };

    private static final ParserRuleContext[] ELSE_BLOCK =
            { ParserRuleContext.ELSE_KEYWORD, ParserRuleContext.STATEMENT };

    private static final ParserRuleContext[] CALL_STATEMENT =
            { ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] IMPORT_PREFIX_DECL =
            { ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] IMPORT_VERSION =
            { ParserRuleContext.VERSION_KEYWORD, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] IMPORT_DECL_RHS = { ParserRuleContext.SLASH, ParserRuleContext.DOT,
            ParserRuleContext.VERSION_KEYWORD, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] AFTER_IMPORT_MODULE_NAME = { ParserRuleContext.DOT,
            ParserRuleContext.VERSION_KEYWORD, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] MAJOR_MINOR_VERSION_END =
            { ParserRuleContext.DOT, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] RETURN_RHS = { ParserRuleContext.SEMICOLON, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] EXPRESSION_START = { ParserRuleContext.BASIC_LITERAL,
            ParserRuleContext.NIL_LITERAL, ParserRuleContext.VARIABLE_REF, ParserRuleContext.ACCESS_EXPRESSION,
            ParserRuleContext.TYPEOF_EXPRESSION, ParserRuleContext.TRAP_KEYWORD, ParserRuleContext.UNARY_EXPRESSION,
            ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.LIST_CONSTRUCTOR, ParserRuleContext.TYPE_CAST,
            ParserRuleContext.OPEN_PARENTHESIS, ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION,
            ParserRuleContext.LET_EXPRESSION, ParserRuleContext.TEMPLATE_START, ParserRuleContext.XML_KEYWORD,
            ParserRuleContext.STRING_KEYWORD, ParserRuleContext.BASE64_KEYWORD, ParserRuleContext.BASE64_KEYWORD,
            ParserRuleContext.ANON_FUNC_EXPRESSION, ParserRuleContext.ERROR_KEYWORD, ParserRuleContext.NEW_KEYWORD,
            ParserRuleContext.START_KEYWORD, ParserRuleContext.FLUSH_KEYWORD, ParserRuleContext.LEFT_ARROW_TOKEN,
            ParserRuleContext.WAIT_KEYWORD, ParserRuleContext.COMMIT_KEYWORD, ParserRuleContext.TRANSACTIONAL_KEYWORD,
            ParserRuleContext.SERVICE_CONSTRUCTOR_EXPRESSION };

    private static final ParserRuleContext[] FIRST_MAPPING_FIELD_START =
            { ParserRuleContext.MAPPING_FIELD, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] MAPPING_FIELD_START = { ParserRuleContext.SPECIFIC_FIELD,
            ParserRuleContext.COMPUTED_FIELD_NAME, ParserRuleContext.ELLIPSIS, ParserRuleContext.READONLY_KEYWORD };

    private static final ParserRuleContext[] SPECIFIC_FIELD =
            { ParserRuleContext.MAPPING_FIELD_NAME, ParserRuleContext.STRING_LITERAL };

    private static final ParserRuleContext[] SPECIFIC_FIELD_RHS =
            { ParserRuleContext.COLON, ParserRuleContext.MAPPING_FIELD_END };

    private static final ParserRuleContext[] MAPPING_FIELD_END =
            { ParserRuleContext.CLOSE_BRACE, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] OPTIONAL_SERVICE_NAME =
            { ParserRuleContext.SERVICE_NAME, ParserRuleContext.ON_KEYWORD };

    private static final ParserRuleContext[] RESOURCE_DEF_START =
            { ParserRuleContext.RESOURCE_KEYWORD, ParserRuleContext.FUNC_DEF, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] CONST_DECL_RHS =
            { ParserRuleContext.TYPE_NAME_OR_VAR_NAME, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] ARRAY_LENGTH =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.DECIMAL_INTEGER_LITERAL,
                    ParserRuleContext.HEX_INTEGER_LITERAL, ParserRuleContext.ASTERISK, ParserRuleContext.VARIABLE_REF };

    private static final ParserRuleContext[] PARAM_LIST =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.REQUIRED_PARAM };

    private static final ParserRuleContext[] PARAMETER_START =
            { ParserRuleContext.ANNOTATIONS, ParserRuleContext.PUBLIC_KEYWORD, ParserRuleContext.TYPE_DESC_IN_PARAM };

    private static final ParserRuleContext[] PARAMETER_WITHOUT_ANNOTS =
            { ParserRuleContext.PUBLIC_KEYWORD, ParserRuleContext.TYPE_DESC_IN_PARAM };

    private static final ParserRuleContext[] REQUIRED_PARAM_NAME_RHS =
            { ParserRuleContext.PARAM_END, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] PARAM_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] STMT_START_WITH_EXPR_RHS = { ParserRuleContext.ASSIGN_OP,
            ParserRuleContext.RIGHT_ARROW, ParserRuleContext.COMPOUND_BINARY_OPERATOR, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] EXPRESSION_STATEMENT_START =
            { ParserRuleContext.VARIABLE_REF, ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.OPEN_PARENTHESIS,
                    ParserRuleContext.START_KEYWORD, ParserRuleContext.FLUSH_KEYWORD };

    private static final ParserRuleContext[] ANNOT_DECL_OPTIONAL_TYPE =
            { ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER, ParserRuleContext.ANNOTATION_TAG };

    private static final ParserRuleContext[] CONST_DECL_TYPE =
            { ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] ANNOT_DECL_RHS =
            { ParserRuleContext.ANNOTATION_TAG, ParserRuleContext.ON_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] ANNOT_OPTIONAL_ATTACH_POINTS =
            { ParserRuleContext.ON_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] ATTACH_POINT =
            { ParserRuleContext.SOURCE_KEYWORD, ParserRuleContext.ATTACH_POINT_IDENT };

    private static final ParserRuleContext[] ATTACH_POINT_IDENT = { ParserRuleContext.SINGLE_KEYWORD_ATTACH_POINT_IDENT,
            ParserRuleContext.OBJECT_IDENT, ParserRuleContext.RESOURCE_IDENT, ParserRuleContext.RECORD_IDENT };

    private static final ParserRuleContext[] ATTACH_POINT_END =
            { ParserRuleContext.COMMA, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] XML_NAMESPACE_PREFIX_DECL =
            { ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] CONSTANT_EXPRESSION =
            { ParserRuleContext.BASIC_LITERAL, ParserRuleContext.VARIABLE_REF, ParserRuleContext.PLUS_TOKEN,
                    ParserRuleContext.MINUS_TOKEN, ParserRuleContext.NIL_LITERAL };

    private static final ParserRuleContext[] LIST_CONSTRUCTOR_RHS =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] TYPE_CAST_PARAM =
            { ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS, ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] TYPE_CAST_PARAM_RHS =
            { ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS, ParserRuleContext.GT };

    private static final ParserRuleContext[] TABLE_KEYWORD_RHS =
            { ParserRuleContext.KEY_SPECIFIER, ParserRuleContext.TABLE_CONSTRUCTOR };

    private static final ParserRuleContext[] ROW_LIST_RHS =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.MAPPING_CONSTRUCTOR };

    private static final ParserRuleContext[] TABLE_ROW_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] KEY_SPECIFIER_RHS =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] TABLE_KEY_RHS =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] ERROR_TYPE_PARAMS =
            { ParserRuleContext.INFERRED_TYPE_DESC, ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS };

    private static final ParserRuleContext[] LET_VAR_DECL_START =
            { ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] STREAM_TYPE_FIRST_PARAM_RHS =
            { ParserRuleContext.COMMA, ParserRuleContext.GT };

    private static final ParserRuleContext[] TEMPLATE_MEMBER = { ParserRuleContext.TEMPLATE_STRING,
            ParserRuleContext.INTERPOLATION_START_TOKEN, ParserRuleContext.TEMPLATE_END };

    private static final ParserRuleContext[] TEMPLATE_STRING_RHS =
            { ParserRuleContext.INTERPOLATION_START_TOKEN, ParserRuleContext.TEMPLATE_END };

    private static final ParserRuleContext[] KEY_CONSTRAINTS_RHS =
            { ParserRuleContext.OPEN_PARENTHESIS, ParserRuleContext.LT };

    private static final ParserRuleContext[] FUNCTION_KEYWORD_RHS =
            { ParserRuleContext.FUNC_NAME, ParserRuleContext.OPEN_PARENTHESIS };

    private static final ParserRuleContext[] TYPEDESC_RHS = { ParserRuleContext.END_OF_TYPE_DESC,
            ParserRuleContext.ARRAY_TYPE_DESCRIPTOR, ParserRuleContext.OPTIONAL_TYPE_DESCRIPTOR, ParserRuleContext.PIPE,
            ParserRuleContext.BITWISE_AND_OPERATOR };

    private static final ParserRuleContext[] TABLE_TYPE_DESC_RHS =
            { ParserRuleContext.KEY_KEYWORD, ParserRuleContext.TYPEDESC_RHS };

    private static final ParserRuleContext[] NEW_KEYWORD_RHS =
            { ParserRuleContext.TYPE_DESC_IN_NEW_EXPR, ParserRuleContext.EXPRESSION_RHS };

    private static final ParserRuleContext[] TABLE_CONSTRUCTOR_OR_QUERY_START =
            { ParserRuleContext.TABLE_KEYWORD, ParserRuleContext.STREAM_KEYWORD, ParserRuleContext.QUERY_EXPRESSION };

    private static final ParserRuleContext[] TABLE_CONSTRUCTOR_OR_QUERY_RHS =
            { ParserRuleContext.TABLE_CONSTRUCTOR, ParserRuleContext.QUERY_EXPRESSION };

    private static final ParserRuleContext[] QUERY_EXPRESSION_RHS =
            { ParserRuleContext.SELECT_CLAUSE, ParserRuleContext.WHERE_CLAUSE, ParserRuleContext.FROM_CLAUSE,
                    ParserRuleContext.LET_CLAUSE, ParserRuleContext.DO_CLAUSE, ParserRuleContext.QUERY_EXPRESSION_END };

    private static final ParserRuleContext[] BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] ANNOTATION_REF_RHS =
            { ParserRuleContext.OPEN_PARENTHESIS, ParserRuleContext.ANNOTATION_END };

    private static final ParserRuleContext[] INFER_PARAM_END_OR_PARENTHESIS_END =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.EXPR_FUNC_BODY_START };

    private static final ParserRuleContext[] OPTIONAL_PEER_WORKER =
            { ParserRuleContext.PEER_WORKER_NAME, ParserRuleContext.EXPRESSION_RHS };

    private static final ParserRuleContext[] TYPE_DESC_IN_TUPLE_RHS =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.COMMA, ParserRuleContext.ELLIPSIS };

    private static final ParserRuleContext[] LIST_CONSTRUCTOR_MEMBER_END =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] NIL_OR_PARENTHESISED_TYPE_DESC_RHS =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.TYPE_DESCRIPTOR };

    private static final ParserRuleContext[] BINDING_PATTERN = { ParserRuleContext.CAPTURE_BINDING_PATTERN,
            ParserRuleContext.LIST_BINDING_PATTERN, ParserRuleContext.MAPPING_BINDING_PATTERN };

    private static final ParserRuleContext[] LIST_BINDING_PATTERN_CONTENTS =
            { ParserRuleContext.REST_BINDING_PATTERN, ParserRuleContext.BINDING_PATTERN };

    private static final ParserRuleContext[] MAPPING_BINDING_PATTERN_MEMBER =
            { ParserRuleContext.REST_BINDING_PATTERN, ParserRuleContext.FIELD_BINDING_PATTERN };

    private static final ParserRuleContext[] LIST_BINDING_PATTERN_END_OR_CONTINUE =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] MAPPING_BINDING_PATTERN_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] FIELD_BINDING_PATTERN_END =
            { ParserRuleContext.COMMA, ParserRuleContext.COLON, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] REMOTE_CALL_OR_ASYNC_SEND_RHS =
            { ParserRuleContext.WORKER_NAME_OR_METHOD_NAME, ParserRuleContext.DEFAULT_WORKER_NAME_IN_ASYNC_SEND };

    private static final ParserRuleContext[] REMOTE_CALL_OR_ASYNC_SEND_END =
            { ParserRuleContext.ARG_LIST_START, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] RECEIVE_WORKERS =
            { ParserRuleContext.PEER_WORKER_NAME, ParserRuleContext.MULTI_RECEIVE_WORKERS };

    private static final ParserRuleContext[] RECEIVE_FIELD =
            { ParserRuleContext.PEER_WORKER_NAME, ParserRuleContext.RECEIVE_FIELD_NAME };

    private static final ParserRuleContext[] RECEIVE_FIELD_END =
            { ParserRuleContext.CLOSE_BRACE, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] WAIT_KEYWORD_RHS =
            { ParserRuleContext.MULTI_WAIT_FIELDS, ParserRuleContext.ALTERNATE_WAIT_EXPRS };

    private static final ParserRuleContext[] WAIT_FIELD_NAME_RHS =
            { ParserRuleContext.COLON, ParserRuleContext.WAIT_FIELD_END };

    private static final ParserRuleContext[] WAIT_FIELD_END =
            { ParserRuleContext.CLOSE_BRACE, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] WAIT_FUTURE_EXPR_END =
            { ParserRuleContext.ALTERNATE_WAIT_EXPR_LIST_END, ParserRuleContext.PIPE };

    private static final ParserRuleContext[] ENUM_MEMBER_START =
            { ParserRuleContext.DOC_STRING, ParserRuleContext.ANNOTATIONS, ParserRuleContext.ENUM_MEMBER_NAME };

    private static final ParserRuleContext[] ENUM_MEMBER_RHS =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.ENUM_MEMBER_END };

    private static final ParserRuleContext[] ENUM_MEMBER_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] MEMBER_ACCESS_KEY_EXPR_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] ROLLBACK_RHS =
            { ParserRuleContext.SEMICOLON, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] RETRY_KEYWORD_RHS =
            { ParserRuleContext.LT, ParserRuleContext.RETRY_TYPE_PARAM_RHS };

    private static final ParserRuleContext[] RETRY_TYPE_PARAM_RHS =
            { ParserRuleContext.ARG_LIST_START, ParserRuleContext.RETRY_BODY };

    private static final ParserRuleContext[] RETRY_BODY =
            { ParserRuleContext.BLOCK_STMT, ParserRuleContext.TRANSACTION_STMT };

    private static final ParserRuleContext[] LIST_BP_OR_TUPLE_TYPE_MEMBER =
            { ParserRuleContext.TYPE_DESCRIPTOR, ParserRuleContext.LIST_BINDING_PATTERN_CONTENTS };

    private static final ParserRuleContext[] LIST_BP_OR_TUPLE_TYPE_DESC_RHS =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] BRACKETED_LIST_MEMBER_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] BRACKETED_LIST_MEMBER =
            // array length is also an expression
            { ParserRuleContext.EXPRESSION, ParserRuleContext.BINDING_PATTERN };

    private static final ParserRuleContext[] LIST_BINDING_MEMBER_OR_ARRAY_LENGTH =
            { ParserRuleContext.ARRAY_LENGTH, ParserRuleContext.BINDING_PATTERN };

    private static final ParserRuleContext[] BRACKETED_LIST_RHS = { ParserRuleContext.ASSIGN_OP,
            ParserRuleContext.VARIABLE_NAME, ParserRuleContext.BINDING_PATTERN, ParserRuleContext.EXPRESSION_RHS };

    private static final ParserRuleContext[] XML_NAVIGATE_EXPR =
            { ParserRuleContext.XML_FILTER_EXPR, ParserRuleContext.XML_STEP_EXPR };

    private static final ParserRuleContext[] XML_NAME_PATTERN_RHS = { ParserRuleContext.GT, ParserRuleContext.PIPE };

    private static final ParserRuleContext[] XML_ATOMIC_NAME_PATTERN_START =
            { ParserRuleContext.ASTERISK, ParserRuleContext.XML_ATOMIC_NAME_IDENTIFIER };

    private static final ParserRuleContext[] XML_ATOMIC_NAME_IDENTIFIER_RHS =
            { ParserRuleContext.ASTERISK, ParserRuleContext.IDENTIFIER };

    private static final ParserRuleContext[] XML_STEP_START = { ParserRuleContext.SLASH_ASTERISK_TOKEN,
            ParserRuleContext.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN, ParserRuleContext.SLASH_LT_TOKEN };

    public BallerinaParserErrorHandler(AbstractTokenReader tokenReader) {
        super(tokenReader);
    }

    @Override
    protected boolean isProductionWithAlternatives(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case TOP_LEVEL_NODE:
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case FUNC_BODY_OR_TYPE_DESC_RHS:
            case VAR_DECL_STMT_RHS:
            case EXPRESSION_RHS:
            case PARAMETER_NAME_RHS:
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case AFTER_PARAMETER_TYPE:
            case FIELD_DESCRIPTOR_RHS:
            case RECORD_BODY_START:
            case RECORD_BODY_END:
            case TYPE_DESCRIPTOR:
            case NAMED_OR_POSITIONAL_ARG_RHS:
            case OBJECT_FIELD_RHS:
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
            case OBJECT_MEMBER:
            case OBJECT_TYPE_FIRST_QUALIFIER:
            case OBJECT_TYPE_SECOND_QUALIFIER:
            case ELSE_BODY:
            case IMPORT_DECL_RHS:
            case IMPORT_SUB_VERSION:
            case VERSION_NUMBER:
            case IMPORT_VERSION_DECL:
            case IMPORT_PREFIX_DECL:
            case MAPPING_FIELD:
            case FIRST_MAPPING_FIELD:
            case SPECIFIC_FIELD_RHS:
            case RESOURCE_DEF:
            case PARAMETER_WITHOUT_ANNOTS:
            case PARAMETER_START:
            case STMT_START_WITH_EXPR_RHS:
            case RECORD_FIELD_OR_RECORD_END:
            case CONST_DECL_TYPE:
            case CONST_DECL_RHS:
            case ANNOT_OPTIONAL_ATTACH_POINTS:
            case XML_NAMESPACE_PREFIX_DECL:
            case ANNOT_DECL_OPTIONAL_TYPE:
            case ANNOT_DECL_RHS:
            case TABLE_KEYWORD_RHS:
            case ARRAY_LENGTH:
            case TYPEDESC_RHS:
            case ERROR_TYPE_PARAMS:
            case STREAM_TYPE_FIRST_PARAM_RHS:
            case KEY_CONSTRAINTS_RHS:
            case TABLE_TYPE_DESC_RHS:
            case FUNC_BODY:
            case FUNC_OPTIONAL_RETURNS:
            case TERMINAL_EXPRESSION:
            case TABLE_CONSTRUCTOR_OR_QUERY_START:
            case TABLE_CONSTRUCTOR_OR_QUERY_RHS:
            case QUERY_PIPELINE_RHS:
            case ANON_FUNC_BODY:
            case BINDING_PATTERN:
            case LIST_BINDING_PATTERN_CONTENTS:
            case LIST_BINDING_PATTERN_END_OR_CONTINUE:
            case MAPPING_BINDING_PATTERN_MEMBER:
            case MAPPING_BINDING_PATTERN_END:
            case FIELD_BINDING_PATTERN_END:
            case REMOTE_CALL_OR_ASYNC_SEND_RHS:
            case REMOTE_CALL_OR_ASYNC_SEND_END:
            case RECEIVE_FIELD_END:
            case RECEIVE_WORKERS:
            case WAIT_FIELD_NAME:
            case WAIT_FIELD_NAME_RHS:
            case WAIT_FIELD_END:
            case WAIT_FUTURE_EXPR_END:
            case MAPPING_FIELD_END:
            case ENUM_MEMBER_START:
            case ENUM_MEMBER_RHS:
            case STMT_START_BRACKETED_LIST_MEMBER:
            case STMT_START_BRACKETED_LIST_RHS:
            case ENUM_MEMBER_END:
            case BINDING_PATTERN_OR_EXPR_RHS:
            case BRACKETED_LIST_RHS:
            case BRACKETED_LIST_MEMBER:
            case BRACKETED_LIST_MEMBER_END:
            case AMBIGUOUS_STMT:
            case TYPED_BINDING_PATTERN_TYPE_RHS:
            case TYPE_DESC_IN_TUPLE_RHS:
            case LIST_BINDING_MEMBER_OR_ARRAY_LENGTH:
            case FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY:
                return true;
            default:
                return false;
        }
    }

    private boolean isEndOfObjectTypeNode(int nextLookahead) {
        STToken nextToken = this.tokenReader.peek(nextLookahead);
        switch (nextToken.kind) {
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
            case TYPE_KEYWORD:
            case SERVICE_KEYWORD:
                return true;
            default:
                STToken nextNextToken = this.tokenReader.peek(nextLookahead + 1);
                switch (nextNextToken.kind) {
                    case CLOSE_BRACE_TOKEN:
                    case EOF_TOKEN:
                    case CLOSE_BRACE_PIPE_TOKEN:
                    case TYPE_KEYWORD:
                    case SERVICE_KEYWORD:
                        return true;
                    default:
                        return false;
                }
        }
    }

    /**
     * Search for a solution.
     * Terminals are directly matched and Non-terminals which have alternative productions are seekInAlternativesPaths()
     *
     * @param currentCtx Current context
     * @param lookahead Position of the next token to consider, relative to the position of the original error.
     * @param currentDepth Amount of distance traveled so far.
     * @return Recovery result
     */
    @Override
    protected Result seekMatch(ParserRuleContext currentCtx, int lookahead, int currentDepth, boolean isEntryPoint) {
        boolean hasMatch;
        boolean skipRule;
        int matchingRulesCount = 0;

        while (currentDepth < lookaheadLimit) {
            hasMatch = true;
            skipRule = false;
            STToken nextToken = this.tokenReader.peek(lookahead);

            switch (currentCtx) {
                case EOF:
                    hasMatch = nextToken.kind == SyntaxKind.EOF_TOKEN;
                    break;
                case PUBLIC_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.PUBLIC_KEYWORD;
                    break;
                case PRIVATE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.PRIVATE_KEYWORD;
                    break;
                case REMOTE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.REMOTE_KEYWORD;
                    break;
                case FUNCTION_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FUNCTION_KEYWORD;
                    break;
                case FUNC_NAME:
                case VARIABLE_NAME:
                case TYPE_NAME:
                case IMPORT_ORG_OR_MODULE_NAME:
                case IMPORT_MODULE_NAME:
                case IMPORT_PREFIX:
                case MAPPING_FIELD_NAME:
                case SERVICE_NAME:
                case QUALIFIED_IDENTIFIER:
                case IDENTIFIER:
                case ANNOTATION_TAG:
                case NAMESPACE_PREFIX:
                case WORKER_NAME:
                case IMPLICIT_ANON_FUNC_PARAM:
                case WORKER_NAME_OR_METHOD_NAME:
                case RECEIVE_FIELD_NAME:
                case WAIT_FIELD_NAME:
                case FIELD_BINDING_PATTERN_NAME:
                case XML_ATOMIC_NAME_IDENTIFIER:
                    hasMatch = nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case OPEN_PARENTHESIS:
                case PARENTHESISED_TYPE_DESC_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN;
                    break;
                case CLOSE_PARENTHESIS:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN;
                    break;
                case RETURNS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RETURNS_KEYWORD;
                    break;
                case SIMPLE_TYPE_DESCRIPTOR:
                    hasMatch = BallerinaParser.isSimpleType(nextToken.kind) ||
                            nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case OPEN_BRACE:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACE_TOKEN;
                    break;
                case CLOSE_BRACE:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACE_TOKEN;
                    break;
                case ASSIGN_OP:
                    hasMatch = nextToken.kind == SyntaxKind.EQUAL_TOKEN;
                    break;
                case EXTERNAL_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.EXTERNAL_KEYWORD;
                    break;
                case SEMICOLON:
                    hasMatch = nextToken.kind == SyntaxKind.SEMICOLON_TOKEN;
                    break;

                case BINARY_OPERATOR:
                    hasMatch = isBinaryOperator(nextToken);
                    break;
                case COMMA:
                    hasMatch = nextToken.kind == SyntaxKind.COMMA_TOKEN;
                    break;
                case CLOSED_RECORD_BODY_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
                    break;
                case CLOSED_RECORD_BODY_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
                    break;
                case ELLIPSIS:
                    hasMatch = nextToken.kind == SyntaxKind.ELLIPSIS_TOKEN;
                    break;
                case QUESTION_MARK:
                    hasMatch = nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN;
                    break;
                case RECORD_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RECORD_KEYWORD;
                    break;
                case TYPE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TYPE_KEYWORD;
                    break;

                case ARG_LIST_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN;
                    break;
                case ARG_LIST_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN;
                    break;
                case OBJECT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.OBJECT_KEYWORD;
                    break;
                case OBJECT_TYPE_FIRST_QUALIFIER:
                case OBJECT_TYPE_SECOND_QUALIFIER:
                    // If currentDepth == 0 means its the very next token after the error. If that erroneous
                    // token is a correct match, then that means we have reached here because of a duplicate
                    // modifier. Therefore treat it as a mismatch.
                    if (currentDepth == 0) {
                        hasMatch = false;
                        break;
                    }

                    hasMatch = nextToken.kind == SyntaxKind.ABSTRACT_KEYWORD ||
                            nextToken.kind == SyntaxKind.CLIENT_KEYWORD;
                    break;
                case ABSTRACT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ABSTRACT_KEYWORD;
                    break;
                case CLIENT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.CLIENT_KEYWORD;
                    break;
                case OPEN_BRACKET:
                case TUPLE_TYPE_DESC_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN;
                    break;
                case CLOSE_BRACKET:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN;
                    break;
                case DOT:
                    hasMatch = nextToken.kind == SyntaxKind.DOT_TOKEN;
                    break;
                case IF_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IF_KEYWORD;
                    break;
                case ELSE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ELSE_KEYWORD;
                    break;
                case WHILE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.WHILE_KEYWORD;
                    break;
                case PANIC_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.PANIC_KEYWORD;
                    break;
                case AS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.AS_KEYWORD;
                    break;
                case LOCK_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.LOCK_KEYWORD;
                    break;
                case BOOLEAN_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.TRUE_KEYWORD || nextToken.kind == SyntaxKind.FALSE_KEYWORD;
                    break;
                case DECIMAL_INTEGER_LITERAL:
                case MAJOR_VERSION:
                case MINOR_VERSION:
                case PATCH_VERSION:
                    hasMatch = nextToken.kind == SyntaxKind.DECIMAL_INTEGER_LITERAL;
                    break;
                case IMPORT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IMPORT_KEYWORD;
                    break;
                case SLASH:
                    hasMatch = nextToken.kind == SyntaxKind.SLASH_TOKEN;
                    break;
                case VERSION_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.VERSION_KEYWORD;
                    break;
                case CONTINUE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.CONTINUE_KEYWORD;
                    break;
                case BREAK_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.BREAK_KEYWORD;
                    break;
                case RETURN_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RETURN_KEYWORD;
                    break;
                case BASIC_LITERAL:
                    hasMatch = isBasicLiteral(nextToken.kind);
                    break;
                case COLON:
                    hasMatch = nextToken.kind == SyntaxKind.COLON_TOKEN;
                    break;
                case STRING_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.STRING_LITERAL;
                    break;
                case SERVICE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.SERVICE_KEYWORD;
                    break;
                case ON_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ON_KEYWORD;
                    break;
                case RESOURCE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RESOURCE_KEYWORD;
                    break;
                case LISTENER_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.LISTENER_KEYWORD;
                    break;
                case CONST_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.CONST_KEYWORD;
                    break;
                case FINAL_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FINAL_KEYWORD;
                    break;
                case TYPEOF_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TYPEOF_KEYWORD;
                    break;
                case UNARY_OPERATOR:
                    hasMatch = isUnaryOperator(nextToken);
                    break;
                case HEX_INTEGER_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.HEX_INTEGER_LITERAL;
                    break;
                case AT:
                    hasMatch = nextToken.kind == SyntaxKind.AT_TOKEN;
                    break;
                case IS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IS_KEYWORD;
                    break;
                case RIGHT_ARROW:
                    hasMatch = nextToken.kind == SyntaxKind.RIGHT_ARROW_TOKEN;
                    break;
                case PARAMETERIZED_TYPE:
                    hasMatch = isParameterizedTypeToken(nextToken.kind);
                    break;
                case LT:
                    hasMatch = nextToken.kind == SyntaxKind.LT_TOKEN;
                    break;
                case GT:
                    hasMatch = nextToken.kind == SyntaxKind.GT_TOKEN;
                    break;
                case NULL_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.NULL_KEYWORD;
                    break;
                case ANNOTATION_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ANNOTATION_KEYWORD;
                    break;
                case FIELD_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.FIELD_KEYWORD;
                    break;
                case FUNCTION_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.FUNCTION_KEYWORD;
                    break;
                case IDENT_AFTER_OBJECT_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.TYPE_KEYWORD ||
                            nextToken.kind == SyntaxKind.FUNCTION_KEYWORD || nextToken.kind == SyntaxKind.FIELD_KEYWORD;
                    break;
                case SOURCE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.SOURCE_KEYWORD;
                    break;
                case SINGLE_KEYWORD_ATTACH_POINT_IDENT:
                    hasMatch = isSingleKeywordAttachPointIdent(nextToken.kind);
                    break;
                case OBJECT_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.OBJECT_KEYWORD;
                    break;
                case RECORD_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.RECORD_KEYWORD;
                    break;
                case RESOURCE_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.RESOURCE_KEYWORD;
                    break;
                case XMLNS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.XMLNS_KEYWORD;
                    break;
                case WORKER_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.WORKER_KEYWORD;
                    break;
                case FORK_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FORK_KEYWORD;
                    break;
                case DECIMAL_FLOATING_POINT_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL;
                    break;
                case HEX_FLOATING_POINT_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.HEX_FLOATING_POINT_LITERAL;
                    break;
                case TRAP_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TRAP_KEYWORD;
                    break;
                case FOREACH_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FOREACH_KEYWORD;
                    break;
                case IN_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IN_KEYWORD;
                    break;
                case PIPE:
                    hasMatch = nextToken.kind == SyntaxKind.PIPE_TOKEN;
                    break;
                case TABLE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TABLE_KEYWORD;
                    break;
                case KEY_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.KEY_KEYWORD;
                    break;
                case ERROR_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ERROR_KEYWORD;
                    break;
                case LET_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.LET_KEYWORD;
                    break;
                case STREAM_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.STREAM_KEYWORD;
                    break;
                case TEMPLATE_START:
                case TEMPLATE_END:
                    hasMatch = nextToken.kind == SyntaxKind.BACKTICK_TOKEN;
                    break;
                case XML_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.XML_KEYWORD;
                    break;
                case STRING_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.STRING_KEYWORD;
                    break;
                case BASE16_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.BASE16_KEYWORD;
                    break;
                case BASE64_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.BASE64_KEYWORD;
                    break;
                case ASTERISK:
                case INFERRED_TYPE_DESC:
                    hasMatch = nextToken.kind == SyntaxKind.ASTERISK_TOKEN;
                    break;
                case NEW_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.NEW_KEYWORD;
                    break;
                case SELECT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.SELECT_KEYWORD;
                    break;
                case WHERE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.WHERE_KEYWORD;
                    break;
                case FROM_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FROM_KEYWORD;
                    break;
                case BITWISE_AND_OPERATOR:
                    hasMatch = nextToken.kind == SyntaxKind.BITWISE_AND_TOKEN;
                    break;
                case EXPR_FUNC_BODY_START:
                    hasMatch = nextToken.kind == SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN;
                    break;
                case START_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.START_KEYWORD;
                    break;
                case FLUSH_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FLUSH_KEYWORD;
                    break;
                case DEFAULT_KEYWORD:
                case DEFAULT_WORKER_NAME_IN_ASYNC_SEND:
                    hasMatch = nextToken.kind == SyntaxKind.DEFAULT_KEYWORD;
                    break;
                case PLUS_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.PLUS_TOKEN;
                    break;
                case MINUS_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.MINUS_TOKEN;
                    break;
                case SIGNED_INT_OR_FLOAT_RHS:
                    hasMatch = BallerinaParser.isIntOrFloat(nextToken);
                    break;
                case SYNC_SEND_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.SYNC_SEND_TOKEN;
                    break;
                case PEER_WORKER_NAME:
                    hasMatch = nextToken.kind == SyntaxKind.DEFAULT_KEYWORD ||
                            nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case LEFT_ARROW_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.LEFT_ARROW_TOKEN;
                    break;
                case WAIT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.WAIT_KEYWORD;
                    break;
                case CHECKING_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.CHECK_KEYWORD ||
                            nextToken.kind == SyntaxKind.CHECKPANIC_KEYWORD;
                    break;
                case DO_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.DO_KEYWORD;
                    break;
                case ANNOT_CHAINING_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.ANNOT_CHAINING_TOKEN;
                    break;
                case OPTIONAL_CHAINING_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.OPTIONAL_CHAINING_TOKEN;
                    break;
                case TRANSACTION_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TRANSACTION_KEYWORD;
                    break;
                case COMMIT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.COMMIT_KEYWORD;
                    break;
                case RETRY_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RETRY_KEYWORD;
                    break;
                case ROLLBACK_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ROLLBACK_KEYWORD;
                    break;
                case TRANSACTIONAL_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TRANSACTIONAL_KEYWORD;
                    break;
                case ENUM_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ENUM_KEYWORD;
                    break;
                case MODULE_ENUM_NAME:
                case ENUM_MEMBER_NAME:
                    hasMatch = nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case UNION_OR_INTERSECTION_TOKEN:
                    hasMatch =
                            nextToken.kind == SyntaxKind.PIPE_TOKEN || nextToken.kind == SyntaxKind.BITWISE_AND_TOKEN;
                    break;
                case DOT_LT_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.DOT_LT_TOKEN;
                    break;
                case SLASH_LT_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.SLASH_LT_TOKEN;
                    break;
                case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.SLASH_ASTERISK_TOKEN;
                    break;
                case SLASH_ASTERISK_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.SLASH_ASTERISK_TOKEN;
                    break;

                // start a context, so that we know where to fall back, and continue
                // having the qualified-identifier as the next rule.
                case VARIABLE_REF:
                case TYPE_REFERENCE:
                case ANNOT_REFERENCE:
                case FIELD_ACCESS_IDENTIFIER:

                    // Contexts that expect a type
                case TYPE_DESC_IN_ANNOTATION_DECL:
                case TYPE_DESC_BEFORE_IDENTIFIER:
                case TYPE_DESC_IN_RECORD_FIELD:
                case TYPE_DESC_IN_PARAM:
                case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
                case TYPE_DESC_IN_TYPE_DEF:
                case TYPE_DESC_IN_ANGLE_BRACKETS:
                case TYPE_DESC_IN_RETURN_TYPE_DESC:
                case TYPE_DESC_IN_EXPRESSION:
                case TYPE_DESC_IN_STREAM_TYPE_DESC:
                case TYPE_DESC_IN_PARENTHESIS:
                case TYPE_DESC_IN_NEW_EXPR:
                default:
                    if (hasAlternativePaths(currentCtx)) {
                        return seekMatchInAlternativePaths(currentCtx, lookahead, currentDepth, matchingRulesCount,
                                isEntryPoint);
                    }

                    // Stay at the same place
                    skipRule = true;
                    hasMatch = true;
                    break;
            }

            if (!hasMatch) {
                return fixAndContinue(currentCtx, lookahead, currentDepth, matchingRulesCount, isEntryPoint);
            }

            currentCtx = getNextRule(currentCtx, lookahead + 1);
            if (!skipRule) {
                // Try the next token with the next rule
                currentDepth++;
                matchingRulesCount++;
                lookahead++;
                isEntryPoint = false;
            }
        }

        Result result = new Result(new ArrayDeque<>(), matchingRulesCount);
        result.solution = new Solution(Action.KEEP, currentCtx, SyntaxKind.NONE, currentCtx.toString());
        return result;
    }

    private boolean hasAlternativePaths(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case TOP_LEVEL_NODE:
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
            case FUNC_OPTIONAL_RETURNS:
            case FUNC_BODY_OR_TYPE_DESC_RHS:
            case ANON_FUNC_BODY:
            case FUNC_BODY:
            case OBJECT_FUNC_BODY:
            case EXPRESSION:
            case TERMINAL_EXPRESSION:
            case VAR_DECL_STMT_RHS:
            case EXPRESSION_RHS:
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case PARAM_LIST:
            case REQUIRED_PARAM_NAME_RHS:
            case TYPE_NAME_OR_VAR_NAME:
            case ASSIGNMENT_OR_VAR_DECL_STMT_RHS:
            case FIELD_DESCRIPTOR_RHS:
            case FIELD_OR_REST_DESCIPTOR_RHS:
            case RECORD_BODY_END:
            case RECORD_BODY_START:
            case TYPE_DESCRIPTOR:
            case RECORD_FIELD_OR_RECORD_END:
            case RECORD_FIELD_START:
            case RECORD_FIELD_WITHOUT_METADATA:
            case ARG_START:
            case ARG_START_OR_ARG_LIST_END:
            case NAMED_OR_POSITIONAL_ARG_RHS:
            case ARG_END:
            case OBJECT_MEMBER_START:
            case OBJECT_MEMBER_WITHOUT_METADATA:
            case OBJECT_FIELD_RHS:
            case OBJECT_METHOD_START:
            case OBJECT_FUNC_OR_FIELD:
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
            case OBJECT_TYPE_DESCRIPTOR_START:
            case ELSE_BLOCK:
            case ELSE_BODY:
            case CALL_STMT_START:
            case IMPORT_PREFIX_DECL:
            case IMPORT_VERSION_DECL:
            case IMPORT_DECL_RHS:
            case AFTER_IMPORT_MODULE_NAME:
            case MAJOR_MINOR_VERSION_END:
            case RETURN_STMT_RHS:
            case ACCESS_EXPRESSION:
            case FIRST_MAPPING_FIELD:
            case MAPPING_FIELD:
            case SPECIFIC_FIELD:
            case SPECIFIC_FIELD_RHS:
            case MAPPING_FIELD_END:
            case OPTIONAL_SERVICE_NAME:
            case RESOURCE_DEF:
            case CONST_DECL_TYPE:
            case CONST_DECL_RHS:
            case ARRAY_LENGTH:
            case PARAMETER_START:
            case PARAMETER_WITHOUT_ANNOTS:
            case STMT_START_WITH_EXPR_RHS:
            case EXPRESSION_STATEMENT_START:
            case ANNOT_DECL_OPTIONAL_TYPE:
            case ANNOT_DECL_RHS:
            case ANNOT_OPTIONAL_ATTACH_POINTS:
            case ATTACH_POINT:
            case ATTACH_POINT_IDENT:
            case ATTACH_POINT_END:
            case XML_NAMESPACE_PREFIX_DECL:
            case CONSTANT_EXPRESSION_START:
            case TYPEDESC_RHS:
            case LIST_CONSTRUCTOR_FIRST_MEMBER:
            case TYPE_CAST_PARAM:
            case TYPE_CAST_PARAM_RHS:
            case TABLE_KEYWORD_RHS:
            case ROW_LIST_RHS:
            case TABLE_ROW_END:
            case KEY_SPECIFIER_RHS:
            case TABLE_KEY_RHS:
            case ERROR_TYPE_PARAMS:
            case LET_VAR_DECL_START:
            case STREAM_TYPE_FIRST_PARAM_RHS:
            case TEMPLATE_MEMBER:
            case TEMPLATE_STRING_RHS:
            case FUNCTION_KEYWORD_RHS:
            case WORKER_NAME_RHS:
            case BINDING_PATTERN:
            case LIST_BINDING_PATTERN_END_OR_CONTINUE:
            case FIELD_BINDING_PATTERN_END:
            case LIST_BINDING_PATTERN_CONTENTS:
            case MAPPING_BINDING_PATTERN_END:
            case MAPPING_BINDING_PATTERN_MEMBER:
            case KEY_CONSTRAINTS_RHS:
            case TABLE_TYPE_DESC_RHS:
            case NEW_KEYWORD_RHS:
            case TABLE_CONSTRUCTOR_OR_QUERY_START:
            case TABLE_CONSTRUCTOR_OR_QUERY_RHS:
            case QUERY_PIPELINE_RHS:
            case BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS:
            case ANON_FUNC_PARAM_RHS:
            case PARAM_END:
            case ANNOTATION_REF_RHS:
            case INFER_PARAM_END_OR_PARENTHESIS_END:
            case TYPE_DESC_IN_TUPLE_RHS:
            case LIST_CONSTRUCTOR_MEMBER_END:
            case NIL_OR_PARENTHESISED_TYPE_DESC_RHS:
            case REMOTE_CALL_OR_ASYNC_SEND_RHS:
            case REMOTE_CALL_OR_ASYNC_SEND_END:
            case RECEIVE_WORKERS:
            case RECEIVE_FIELD:
            case RECEIVE_FIELD_END:
            case WAIT_KEYWORD_RHS:
            case WAIT_FIELD_NAME_RHS:
            case WAIT_FIELD_END:
            case WAIT_FUTURE_EXPR_END:
            case OPTIONAL_PEER_WORKER:
            case ENUM_MEMBER_START:
            case ENUM_MEMBER_RHS:
            case ENUM_MEMBER_END:
            case MEMBER_ACCESS_KEY_EXPR_END:
            case ROLLBACK_RHS:
            case RETRY_KEYWORD_RHS:
            case RETRY_TYPE_PARAM_RHS:
            case RETRY_BODY:
            case STMT_START_BRACKETED_LIST_MEMBER:
            case STMT_START_BRACKETED_LIST_RHS:
            case BINDING_PATTERN_OR_EXPR_RHS:
            case BRACKETED_LIST_RHS:
            case BRACKETED_LIST_MEMBER:
            case BRACKETED_LIST_MEMBER_END:
            case AMBIGUOUS_STMT:
            case LIST_BINDING_MEMBER_OR_ARRAY_LENGTH:
            case XML_NAVIGATE_EXPR:
            case XML_NAME_PATTERN_RHS:
            case XML_ATOMIC_NAME_PATTERN_START:
            case XML_ATOMIC_NAME_IDENTIFIER_RHS:
            case XML_STEP_START:
            case FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY:
                return true;
            default:
                return false;
        }
    }

    private Result seekMatchInAlternativePaths(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                               int matchingRulesCount, boolean isEntryPoint) {
        ParserRuleContext[] alternativeRules;
        switch (currentCtx) {
            case TOP_LEVEL_NODE:
                alternativeRules = TOP_LEVEL_NODE;
                break;
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
                alternativeRules = TOP_LEVEL_NODE_WITHOUT_MODIFIER;
                break;
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
                alternativeRules = TOP_LEVEL_NODE_WITHOUT_METADATA;
                break;
            case FUNC_OPTIONAL_RETURNS:
                ParserRuleContext parentCtx = getParentContext();
                ParserRuleContext[] alternatives;
                if (parentCtx == ParserRuleContext.FUNC_DEF) {
                    alternatives = FUNC_DEF_OPTIONAL_RETURNS;
                } else if (parentCtx == ParserRuleContext.ANON_FUNC_EXPRESSION) {
                    alternatives = ANNON_FUNC_OPTIONAL_RETURNS;
                } else if (parentCtx == ParserRuleContext.FUNC_TYPE_DESC) {
                    alternatives = FUNC_TYPE_OPTIONAL_RETURNS;
                } else if (parentCtx == ParserRuleContext.FUNC_TYPE_DESC_OR_ANON_FUNC) {
                    alternatives = FUNC_TYPE_OR_ANON_FUNC_OPTIONAL_RETURNS;
                } else {
                    alternatives = FUNC_TYPE_OR_DEF_OPTIONAL_RETURNS;
                }

                alternativeRules = alternatives;
                break;
            case FUNC_BODY_OR_TYPE_DESC_RHS:
                alternativeRules = FUNC_BODY_OR_TYPE_DESC_RHS;
                break;
            case FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY:
                alternativeRules = FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY;
                break;
            case ANON_FUNC_BODY:
                alternativeRules = ANON_FUNC_BODY;
                break;
            case FUNC_BODY:
            case OBJECT_FUNC_BODY:
                if (getGrandParentContext() == ParserRuleContext.OBJECT_MEMBER) {
                    alternativeRules = OBJECT_FUNC_BODY;
                } else {
                    alternativeRules = FUNC_BODY;
                }
                break;
            case EXPRESSION:
            case TERMINAL_EXPRESSION:
                alternativeRules = EXPRESSION_START;
                break;
            case VAR_DECL_STMT_RHS:
                alternativeRules = VAR_DECL_RHS;
                break;
            case EXPRESSION_RHS:
                return seekMatchInExpressionRhs(lookahead, currentDepth, matchingRulesCount, isEntryPoint);
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
                return seekInStatements(currentCtx, lookahead, currentDepth, matchingRulesCount, isEntryPoint);
            case PARAM_LIST:
                alternativeRules = PARAM_LIST;
                break;
            case REQUIRED_PARAM_NAME_RHS:
                alternativeRules = REQUIRED_PARAM_NAME_RHS;
                break;
            case TYPE_NAME_OR_VAR_NAME:
                alternativeRules = TYPE_OR_VAR_NAME;
                break;
            case ASSIGNMENT_OR_VAR_DECL_STMT_RHS:
                alternativeRules = ASSIGNMENT_OR_VAR_DECL_SECOND_TOKEN;
                break;
            case FIELD_DESCRIPTOR_RHS:
                alternativeRules = FIELD_DESCRIPTOR_RHS;
                break;
            case FIELD_OR_REST_DESCIPTOR_RHS:
                alternativeRules = FIELD_OR_REST_DESCIPTOR_RHS;
                break;
            case RECORD_BODY_END:
                alternativeRules = RECORD_BODY_END;
                break;
            case RECORD_BODY_START:
                alternativeRules = RECORD_BODY_START;
                break;
            case TYPE_DESCRIPTOR:
                alternativeRules = TYPE_DESCRIPTORS;
                break;
            case RECORD_FIELD_OR_RECORD_END:
                alternativeRules = RECORD_FIELD_OR_RECORD_END;
                break;
            case RECORD_FIELD_START:
                alternativeRules = RECORD_FIELD_START;
                break;
            case RECORD_FIELD_WITHOUT_METADATA:
                alternativeRules = RECORD_FIELD_WITHOUT_METADATA;
                break;
            case ARG_START:
                alternativeRules = ARG_START;
                break;
            case ARG_START_OR_ARG_LIST_END:
                alternativeRules = ARG_START_OR_ARG_LIST_END;
                break;
            case NAMED_OR_POSITIONAL_ARG_RHS:
                alternativeRules = NAMED_OR_POSITIONAL_ARG_RHS;
                break;
            case ARG_END:
                alternativeRules = ARG_END;
                break;
            case OBJECT_MEMBER_START:
                alternativeRules = OBJECT_MEMBER_START;
                break;
            case OBJECT_MEMBER_WITHOUT_METADATA:
                alternativeRules = OBJECT_MEMBER_WITHOUT_METADATA;
                break;
            case OBJECT_FIELD_RHS:
                alternativeRules = OBJECT_FIELD_RHS;
                break;
            case OBJECT_METHOD_START:
                alternativeRules = OBJECT_METHOD_START;
                break;
            case OBJECT_FUNC_OR_FIELD:
                alternativeRules = OBJECT_FUNC_OR_FIELD;
                break;
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
                alternativeRules = OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY;
                break;
            case OBJECT_TYPE_DESCRIPTOR_START:
                alternativeRules = OBJECT_TYPE_DESCRIPTOR_START;
                break;
            case ELSE_BLOCK:
                alternativeRules = ELSE_BLOCK;
                break;
            case ELSE_BODY:
                alternativeRules = ELSE_BODY;
                break;
            case CALL_STMT_START:
                alternativeRules = CALL_STATEMENT;
                break;
            case IMPORT_PREFIX_DECL:
                alternativeRules = IMPORT_PREFIX_DECL;
                break;
            case IMPORT_VERSION_DECL:
                alternativeRules = IMPORT_VERSION;
                break;
            case IMPORT_DECL_RHS:
                alternativeRules = IMPORT_DECL_RHS;
                break;
            case AFTER_IMPORT_MODULE_NAME:
                alternativeRules = AFTER_IMPORT_MODULE_NAME;
                break;
            case MAJOR_MINOR_VERSION_END:
                alternativeRules = MAJOR_MINOR_VERSION_END;
                break;
            case RETURN_STMT_RHS:
                alternativeRules = RETURN_RHS;
                break;
            case ACCESS_EXPRESSION:
                return seekInAccessExpression(currentCtx, lookahead, currentDepth, matchingRulesCount, isEntryPoint);
            case FIRST_MAPPING_FIELD:
                alternativeRules = FIRST_MAPPING_FIELD_START;
                break;
            case MAPPING_FIELD:
                alternativeRules = MAPPING_FIELD_START;
                break;
            case SPECIFIC_FIELD:
                alternativeRules = SPECIFIC_FIELD;
                break;
            case SPECIFIC_FIELD_RHS:
                alternativeRules = SPECIFIC_FIELD_RHS;
                break;
            case MAPPING_FIELD_END:
                alternativeRules = MAPPING_FIELD_END;
                break;
            case OPTIONAL_SERVICE_NAME:
                alternativeRules = OPTIONAL_SERVICE_NAME;
                break;
            case RESOURCE_DEF:
                alternativeRules = RESOURCE_DEF_START;
                break;
            case CONST_DECL_TYPE:
                alternativeRules = CONST_DECL_TYPE;
                break;
            case CONST_DECL_RHS:
                alternativeRules = CONST_DECL_RHS;
                break;
            case ARRAY_LENGTH:
                alternativeRules = ARRAY_LENGTH;
                break;
            case PARAMETER_START:
                alternativeRules = PARAMETER_START;
                break;
            case PARAMETER_WITHOUT_ANNOTS:
                alternativeRules = PARAMETER_WITHOUT_ANNOTS;
                break;
            case STMT_START_WITH_EXPR_RHS:
                alternativeRules = STMT_START_WITH_EXPR_RHS;
                break;
            case EXPRESSION_STATEMENT_START:
                alternativeRules = EXPRESSION_STATEMENT_START;
                break;
            case ANNOT_DECL_OPTIONAL_TYPE:
                alternativeRules = ANNOT_DECL_OPTIONAL_TYPE;
                break;
            case ANNOT_DECL_RHS:
                alternativeRules = ANNOT_DECL_RHS;
                break;
            case ANNOT_OPTIONAL_ATTACH_POINTS:
                alternativeRules = ANNOT_OPTIONAL_ATTACH_POINTS;
                break;
            case ATTACH_POINT:
                alternativeRules = ATTACH_POINT;
                break;
            case ATTACH_POINT_IDENT:
                alternativeRules = ATTACH_POINT_IDENT;
                break;
            case ATTACH_POINT_END:
                alternativeRules = ATTACH_POINT_END;
                break;
            case XML_NAMESPACE_PREFIX_DECL:
                alternativeRules = XML_NAMESPACE_PREFIX_DECL;
                break;
            case CONSTANT_EXPRESSION_START:
                alternativeRules = CONSTANT_EXPRESSION;
                break;
            case TYPEDESC_RHS:
                alternativeRules = TYPEDESC_RHS;
                break;
            case LIST_CONSTRUCTOR_FIRST_MEMBER:
                alternativeRules = LIST_CONSTRUCTOR_RHS;
                break;
            case TYPE_CAST_PARAM:
                alternativeRules = TYPE_CAST_PARAM;
                break;
            case TYPE_CAST_PARAM_RHS:
                alternativeRules = TYPE_CAST_PARAM_RHS;
                break;
            case TABLE_KEYWORD_RHS:
                alternativeRules = TABLE_KEYWORD_RHS;
                break;
            case ROW_LIST_RHS:
                alternativeRules = ROW_LIST_RHS;
                break;
            case TABLE_ROW_END:
                alternativeRules = TABLE_ROW_END;
                break;
            case KEY_SPECIFIER_RHS:
                alternativeRules = KEY_SPECIFIER_RHS;
                break;
            case TABLE_KEY_RHS:
                alternativeRules = TABLE_KEY_RHS;
                break;
            case ERROR_TYPE_PARAMS:
                alternativeRules = ERROR_TYPE_PARAMS;
                break;
            case LET_VAR_DECL_START:
                alternativeRules = LET_VAR_DECL_START;
                break;
            case STREAM_TYPE_FIRST_PARAM_RHS:
                alternativeRules = STREAM_TYPE_FIRST_PARAM_RHS;
                break;
            case TEMPLATE_MEMBER:
                alternativeRules = TEMPLATE_MEMBER;
                break;
            case TEMPLATE_STRING_RHS:
                alternativeRules = TEMPLATE_STRING_RHS;
                break;
            case FUNCTION_KEYWORD_RHS:
                alternativeRules = FUNCTION_KEYWORD_RHS;
                break;
            case WORKER_NAME_RHS:
                alternativeRules = WORKER_NAME_RHS;
                break;
            case BINDING_PATTERN:
                alternativeRules = BINDING_PATTERN;
                break;
            case LIST_BINDING_PATTERN_END_OR_CONTINUE:
                alternativeRules = LIST_BINDING_PATTERN_END_OR_CONTINUE;
                break;
            case LIST_BINDING_PATTERN_CONTENTS:
                alternativeRules = LIST_BINDING_PATTERN_CONTENTS;
                break;
            case MAPPING_BINDING_PATTERN_END:
                alternativeRules = MAPPING_BINDING_PATTERN_END;
                break;
            case FIELD_BINDING_PATTERN_END:
                alternativeRules = FIELD_BINDING_PATTERN_END;
                break;
            case MAPPING_BINDING_PATTERN_MEMBER:
                alternativeRules = MAPPING_BINDING_PATTERN_MEMBER;
                break;
            case KEY_CONSTRAINTS_RHS:
                alternativeRules = KEY_CONSTRAINTS_RHS;
                break;
            case TABLE_TYPE_DESC_RHS:
                alternativeRules = TABLE_TYPE_DESC_RHS;
                break;
            case NEW_KEYWORD_RHS:
                alternativeRules = NEW_KEYWORD_RHS;
                break;
            case TABLE_CONSTRUCTOR_OR_QUERY_START:
                alternativeRules = TABLE_CONSTRUCTOR_OR_QUERY_START;
                break;
            case TABLE_CONSTRUCTOR_OR_QUERY_RHS:
                alternativeRules = TABLE_CONSTRUCTOR_OR_QUERY_RHS;
                break;
            case QUERY_PIPELINE_RHS:
                alternativeRules = QUERY_EXPRESSION_RHS;
                break;
            case BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS:
            case ANON_FUNC_PARAM_RHS:
                alternativeRules = BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS;
                break;
            case PARAM_END:
                alternativeRules = PARAM_END;
                break;
            case ANNOTATION_REF_RHS:
                alternativeRules = ANNOTATION_REF_RHS;
                break;
            case INFER_PARAM_END_OR_PARENTHESIS_END:
                alternativeRules = INFER_PARAM_END_OR_PARENTHESIS_END;
                break;
            case TYPE_DESC_IN_TUPLE_RHS:
                alternativeRules = TYPE_DESC_IN_TUPLE_RHS;
                break;
            case LIST_CONSTRUCTOR_MEMBER_END:
                alternativeRules = LIST_CONSTRUCTOR_MEMBER_END;
                break;
            case NIL_OR_PARENTHESISED_TYPE_DESC_RHS:
                alternativeRules = NIL_OR_PARENTHESISED_TYPE_DESC_RHS;
                break;
            case REMOTE_CALL_OR_ASYNC_SEND_RHS:
                alternativeRules = REMOTE_CALL_OR_ASYNC_SEND_RHS;
                break;
            case REMOTE_CALL_OR_ASYNC_SEND_END:
                alternativeRules = REMOTE_CALL_OR_ASYNC_SEND_END;
                break;
            case RECEIVE_WORKERS:
                alternativeRules = RECEIVE_WORKERS;
                break;
            case RECEIVE_FIELD:
                alternativeRules = RECEIVE_FIELD;
                break;
            case RECEIVE_FIELD_END:
                alternativeRules = RECEIVE_FIELD_END;
                break;
            case WAIT_KEYWORD_RHS:
                alternativeRules = WAIT_KEYWORD_RHS;
                break;
            case WAIT_FIELD_NAME_RHS:
                alternativeRules = WAIT_FIELD_NAME_RHS;
                break;
            case WAIT_FIELD_END:
                alternativeRules = WAIT_FIELD_END;
                break;
            case WAIT_FUTURE_EXPR_END:
                alternativeRules = WAIT_FUTURE_EXPR_END;
                break;
            case OPTIONAL_PEER_WORKER:
                alternativeRules = OPTIONAL_PEER_WORKER;
                break;
            case ENUM_MEMBER_START:
                alternativeRules = ENUM_MEMBER_START;
                break;
            case ENUM_MEMBER_RHS:
                alternativeRules = ENUM_MEMBER_RHS;
                break;
            case ENUM_MEMBER_END:
                alternativeRules = ENUM_MEMBER_END;
                break;
            case MEMBER_ACCESS_KEY_EXPR_END:
                alternativeRules = MEMBER_ACCESS_KEY_EXPR_END;
                break;
            case ROLLBACK_RHS:
                alternativeRules = ROLLBACK_RHS;
                break;
            case RETRY_KEYWORD_RHS:
                alternativeRules = RETRY_KEYWORD_RHS;
                break;
            case RETRY_TYPE_PARAM_RHS:
                alternativeRules = RETRY_TYPE_PARAM_RHS;
                break;
            case RETRY_BODY:
                alternativeRules = RETRY_BODY;
                break;
            case STMT_START_BRACKETED_LIST_MEMBER:
                alternativeRules = LIST_BP_OR_TUPLE_TYPE_MEMBER;
                break;
            case STMT_START_BRACKETED_LIST_RHS:
                alternativeRules = LIST_BP_OR_TUPLE_TYPE_DESC_RHS;
                break;
            case BRACKETED_LIST_MEMBER_END:
                alternativeRules = BRACKETED_LIST_MEMBER_END;
                break;
            case BRACKETED_LIST_MEMBER:
                alternativeRules = BRACKETED_LIST_MEMBER;
                break;
            case BRACKETED_LIST_RHS:
            case BINDING_PATTERN_OR_EXPR_RHS:
                alternativeRules = BRACKETED_LIST_RHS;
                break;
            case LIST_BINDING_MEMBER_OR_ARRAY_LENGTH:
                alternativeRules = LIST_BINDING_MEMBER_OR_ARRAY_LENGTH;
                break;
            case XML_NAVIGATE_EXPR:
                alternativeRules = XML_NAVIGATE_EXPR;
                break;
            case XML_NAME_PATTERN_RHS:
                alternativeRules = XML_NAME_PATTERN_RHS;
                break;
            case XML_ATOMIC_NAME_PATTERN_START:
                alternativeRules = XML_ATOMIC_NAME_PATTERN_START;
                break;
            case XML_ATOMIC_NAME_IDENTIFIER_RHS:
                alternativeRules = XML_ATOMIC_NAME_IDENTIFIER_RHS;
                break;
            case XML_STEP_START:
                alternativeRules = XML_STEP_START;
                break;
            default:
                throw new IllegalStateException(currentCtx.toString());
        }

        return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules, isEntryPoint);
    }

    /**
     * Search for matching token sequences within different kinds of statements and returns the most optimal solution.
     *
     * @param currentCtx Current context
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param fixes Fixes made so far
     * @return Recovery result
     */
    private Result seekInStatements(ParserRuleContext currentCtx, int lookahead, int currentDepth, int currentMatches,
                                    boolean isEntryPoint) {
        STToken nextToken = this.tokenReader.peek(lookahead);
        if (nextToken.kind == SyntaxKind.SEMICOLON_TOKEN) {
            // Semicolon at the start of a statement is a special case. This is equivalent to an empty
            // statement. So assume the fix for this is a REMOVE operation and continue from the next token.
            Result result = seekMatchInSubTree(ParserRuleContext.STATEMENT, lookahead + 1, currentDepth, isEntryPoint);
            result.fixes.push(new Solution(Action.REMOVE, currentCtx, nextToken.kind, nextToken.toString()));
            return getFinalResult(currentMatches, result);
        }

        return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, STATEMENTS, isEntryPoint);
    }

    /**
     * Search for matching token sequences within access expressions and returns the most optimal solution.
     * Access expression can be one of: method-call, field-access, member-access.
     *
     * @param currentCtx Current context
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param fixes Fixes made so far
     * @param isEntryPoint
     * @return Recovery result
     */
    private Result seekInAccessExpression(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                          int currentMatches, boolean isEntryPoint) {
        // TODO: Remove this method
        STToken nextToken = this.tokenReader.peek(lookahead);
        currentDepth++;
        if (nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
            Result fixedPathResult = fixAndContinue(currentCtx, lookahead, currentDepth);
            return getFinalResult(currentMatches, fixedPathResult);
        }

        ParserRuleContext nextContext;
        STToken nextNextToken = this.tokenReader.peek(lookahead + 1);
        switch (nextNextToken.kind) {
            case OPEN_PAREN_TOKEN:
                nextContext = ParserRuleContext.OPEN_PARENTHESIS;
                break;
            case DOT_TOKEN:
                nextContext = ParserRuleContext.DOT;
                break;
            case OPEN_BRACKET_TOKEN:
                nextContext = ParserRuleContext.MEMBER_ACCESS_KEY_EXPR;
                break;
            default:
                nextContext = getNextRuleForExpr();
                break;
        }

        currentMatches++;
        lookahead++;
        Result result = seekMatch(nextContext, lookahead, currentDepth, isEntryPoint);
        return getFinalResult(currentMatches, result);
    }

    /**
     * Search for a match in rhs of an expression. RHS of an expression can be the end
     * of the expression or the rhs of a binary expression.
     *
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param isEntryPoint
     * @return Recovery result
     */
    private Result seekMatchInExpressionRhs(int lookahead, int currentDepth, int currentMatches, boolean isEntryPoint) {
        ParserRuleContext parentCtx = getParentContext();
        ParserRuleContext[] next;
        switch (parentCtx) {
            case ARG_LIST:
                next = new ParserRuleContext[] { ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR, ParserRuleContext.COMMA,
                        ParserRuleContext.ARG_LIST_START, ParserRuleContext.ARG_LIST_END };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case MAPPING_CONSTRUCTOR:
            case MULTI_WAIT_FIELDS:
                next = new ParserRuleContext[] { ParserRuleContext.CLOSE_BRACE, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                        ParserRuleContext.COMMA, ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case COMPUTED_FIELD_NAME:
                // Here we give high priority to the comma. Therefore order of the below array matters.
                next = new ParserRuleContext[] { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.OPEN_BRACKET,
                        ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case LISTENERS_LIST:
                next = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                        ParserRuleContext.OPEN_BRACE, ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case LIST_CONSTRUCTOR:
            case MEMBER_ACCESS_KEY_EXPR:
            case BRACKETED_LIST:
            case STMT_START_BRACKETED_LIST:
                next = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                        ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case LET_EXPR_LET_VAR_DECL:
                next = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                        ParserRuleContext.IN_KEYWORD, ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case LET_CLAUSE_LET_VAR_DECL:
                next = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                        ParserRuleContext.LET_CLAUSE_END, ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case QUERY_EXPRESSION:
                next = new ParserRuleContext[] { ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR, ParserRuleContext.QUERY_PIPELINE_RHS,
                        ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            default:
                if (isParameter(parentCtx)) {
                    next = new ParserRuleContext[] { ParserRuleContext.CLOSE_PARENTHESIS,
                            ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                            ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                            ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                            ParserRuleContext.MEMBER_ACCESS_KEY_EXPR, ParserRuleContext.COMMA,
                            ParserRuleContext.ARG_LIST_START };
                    return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
                }
                break;
        }

        ParserRuleContext nextContext;
        if (parentCtx == ParserRuleContext.IF_BLOCK || parentCtx == ParserRuleContext.WHILE_BLOCK ||
                parentCtx == ParserRuleContext.FOREACH_STMT) {
            nextContext = ParserRuleContext.BLOCK_STMT;
        } else if (isStatement(parentCtx) || parentCtx == ParserRuleContext.RECORD_FIELD ||
                parentCtx == ParserRuleContext.OBJECT_MEMBER || parentCtx == ParserRuleContext.LISTENER_DECL ||
                parentCtx == ParserRuleContext.CONSTANT_DECL) {
            nextContext = ParserRuleContext.SEMICOLON;
        } else if (parentCtx == ParserRuleContext.ANNOTATIONS) {
            nextContext = ParserRuleContext.ANNOTATION_END;
        } else if (parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
            nextContext = ParserRuleContext.CLOSE_BRACKET;
        } else if (parentCtx == ParserRuleContext.INTERPOLATION) {
            nextContext = ParserRuleContext.CLOSE_BRACE;
        } else if (parentCtx == ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAMS) {
            nextContext = ParserRuleContext.CLOSE_PARENTHESIS;
        } else if (parentCtx == ParserRuleContext.FUNC_DEF) {
            // expression bodied func in module level
            nextContext = ParserRuleContext.SEMICOLON;
        } else if (parentCtx == ParserRuleContext.ALTERNATE_WAIT_EXPRS) {
            nextContext = ParserRuleContext.ALTERNATE_WAIT_EXPR_LIST_END;
        } else if (parentCtx == ParserRuleContext.CONDITIONAL_EXPRESSION) {
            nextContext = ParserRuleContext.COLON;
        } else if (parentCtx == ParserRuleContext.ENUM_MEMBER_LIST) {
            nextContext = ParserRuleContext.ENUM_MEMBER_END;
        } else {
            throw new IllegalStateException(parentCtx.toString());
        }

        ParserRuleContext[] alternatives =
                { ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.IS_KEYWORD, ParserRuleContext.DOT,
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR, ParserRuleContext.RIGHT_ARROW,
                        ParserRuleContext.SYNC_SEND_TOKEN, nextContext, ParserRuleContext.ARG_LIST_START };

        return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, alternatives, isEntryPoint);
    }

    /**
     * Get the next parser rule/context given the current parser context.
     *
     * @param currentCtx Current parser context
     * @param nextLookahead Position of the next token to consider, relative to the position of the original error
     * @return Next parser context
     */
    @Override
    protected ParserRuleContext getNextRule(ParserRuleContext currentCtx, int nextLookahead) {
        // If this is a production, then push the context to the stack.
        // We can do this within the same switch-case that follows after this one.
        // But doing it separately for the sake of readability/maintainability.
        startContextIfRequired(currentCtx);

        ParserRuleContext parentCtx;
        STToken nextToken;
        switch (currentCtx) {
            case EOF:
                return ParserRuleContext.EOF;
            case COMP_UNIT:
                return ParserRuleContext.TOP_LEVEL_NODE;
            case PUBLIC_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.OBJECT_TYPE_DESCRIPTOR ||
                        parentCtx == ParserRuleContext.OBJECT_MEMBER) {
                    return ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY;
                } else if (isParameter(parentCtx)) {
                    return ParserRuleContext.TYPE_DESC_IN_PARAM;
                }
                return ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_MODIFIER;
            case PRIVATE_KEYWORD:
                return ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY;
            case FUNC_DEF:
            case FUNC_DEF_OR_FUNC_TYPE:
            case FUNC_TYPE_DESC:
            case ANON_FUNC_EXPRESSION:
                return ParserRuleContext.FUNCTION_KEYWORD;
            case EXTERNAL_FUNC_BODY:
                return ParserRuleContext.ASSIGN_OP;
            case FUNC_BODY_BLOCK:
                return ParserRuleContext.OPEN_BRACE;
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
                // We reach here only if an end of a block is reached.
                endContext(); // end statement
                return ParserRuleContext.CLOSE_BRACE;
            case ASSIGN_OP:
                return getNextRuleForEqualOp();
            case COMPOUND_BINARY_OPERATOR:
                return ParserRuleContext.ASSIGN_OP;
            case CLOSE_BRACE:
                return getNextRuleForCloseBrace(nextLookahead);
            case CLOSE_PARENTHESIS:
                return getNextRuleForCloseParenthsis();
            case EXPRESSION:
            case BASIC_LITERAL:
            case TERMINAL_EXPRESSION:
                return getNextRuleForExpr();
            case EXTERNAL_KEYWORD:
                return ParserRuleContext.SEMICOLON;
            case FUNCTION_KEYWORD:
                return ParserRuleContext.FUNCTION_KEYWORD_RHS;
            case FUNC_NAME:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case OPEN_BRACE:
                return getNextRuleForOpenBrace(nextLookahead);
            case OPEN_PARENTHESIS:
                return getNextRuleForOpenParenthesis();
            case RETURNS_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_RETURN_TYPE_DESC;
            case SEMICOLON:
                return getNextRuleForSemicolon(nextLookahead);
            case SIMPLE_TYPE_DESCRIPTOR:
                return ParserRuleContext.TYPEDESC_RHS;
            case VARIABLE_NAME:
            case PARAMETER_NAME_RHS:
                return getNextRuleForVarName();
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
                return ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE;
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                return ParserRuleContext.TYPE_DESC_IN_PARAM;
            case ASSIGNMENT_STMT:
                return ParserRuleContext.VARIABLE_NAME;
            case COMPOUND_ASSIGNMENT_STMT:
                return ParserRuleContext.VARIABLE_NAME;
            case VAR_DECL_STMT:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case EXPRESSION_RHS:
                return ParserRuleContext.BINARY_OPERATOR;
            case BINARY_OPERATOR:
                return ParserRuleContext.EXPRESSION;
            case COMMA:
                return getNextRuleForComma();
            case AFTER_PARAMETER_TYPE:
                return getNextRuleForParamType();
            case MODULE_TYPE_DEFINITION:
                return ParserRuleContext.TYPE_KEYWORD;
            case CLOSED_RECORD_BODY_END:
                endContext();
                nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                    return ParserRuleContext.EOF;
                }
                return ParserRuleContext.TYPEDESC_RHS;
            case CLOSED_RECORD_BODY_START:
                return ParserRuleContext.RECORD_FIELD_OR_RECORD_END;
            case ELLIPSIS:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.MAPPING_CONSTRUCTOR || parentCtx == ParserRuleContext.ARG_LIST) {
                    return ParserRuleContext.EXPRESSION;
                }

                if (parentCtx == ParserRuleContext.TYPE_DESC_IN_TUPLE ||
                        parentCtx == ParserRuleContext.STMT_START_BRACKETED_LIST ||
                        parentCtx == ParserRuleContext.BRACKETED_LIST) {
                    return ParserRuleContext.CLOSE_BRACKET;
                }
                return ParserRuleContext.VARIABLE_NAME;
            case QUESTION_MARK:
                return getNextRuleForQuestionMark();
            case RECORD_KEYWORD:
                return ParserRuleContext.RECORD_BODY_START;
            case TYPE_KEYWORD:
                return ParserRuleContext.TYPE_NAME;
            case RECORD_TYPE_DESCRIPTOR:
                return ParserRuleContext.RECORD_KEYWORD;
            case ASTERISK:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
                    return ParserRuleContext.CLOSE_BRACKET;
                } else if (parentCtx == ParserRuleContext.XML_ATOMIC_NAME_PATTERN) {
                    endContext();
                    return ParserRuleContext.XML_NAME_PATTERN_RHS;
                }
                return ParserRuleContext.TYPE_REFERENCE;
            case TYPE_NAME:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_DEF;
            case OBJECT_KEYWORD:
                return ParserRuleContext.OPEN_BRACE;
            case REMOTE_KEYWORD:
                return ParserRuleContext.FUNCTION_KEYWORD;
            case OBJECT_TYPE_DESCRIPTOR:
                return ParserRuleContext.OBJECT_TYPE_DESCRIPTOR_START;
            case OBJECT_TYPE_FIRST_QUALIFIER:
            case OBJECT_TYPE_SECOND_QUALIFIER:
                return ParserRuleContext.OBJECT_KEYWORD;
            case ABSTRACT_KEYWORD:
            case CLIENT_KEYWORD:
                return ParserRuleContext.OBJECT_KEYWORD;
            case OPEN_BRACKET:
                return getNextRuleForOpenBracket();
            case CLOSE_BRACKET:
                return getNextRuleForCloseBracket();
            case DOT:
                return getNextRuleForDot();
            case IF_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case ELSE_KEYWORD:
                return ParserRuleContext.ELSE_BODY;
            case BLOCK_STMT:
                return ParserRuleContext.OPEN_BRACE;
            case IF_BLOCK:
                return ParserRuleContext.IF_KEYWORD;
            case WHILE_BLOCK:
                return ParserRuleContext.WHILE_KEYWORD;
            case WHILE_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case CHECKING_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case CALL_STMT:
                return ParserRuleContext.CALL_STMT_START;
            case PANIC_STMT:
                return ParserRuleContext.PANIC_KEYWORD;
            case PANIC_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case FUNC_CALL:
                // TODO: check this again
                return ParserRuleContext.IMPORT_PREFIX;
            case IMPORT_KEYWORD:
                return ParserRuleContext.IMPORT_ORG_OR_MODULE_NAME;
            case IMPORT_PREFIX:
            case NAMESPACE_PREFIX:
                return ParserRuleContext.SEMICOLON;
            case VERSION_NUMBER:
            case VERSION_KEYWORD:
                return ParserRuleContext.MAJOR_VERSION;
            case SLASH:
                return ParserRuleContext.IMPORT_MODULE_NAME;
            case IMPORT_ORG_OR_MODULE_NAME:
                return ParserRuleContext.IMPORT_DECL_RHS;
            case IMPORT_MODULE_NAME:
                return ParserRuleContext.AFTER_IMPORT_MODULE_NAME;
            case AS_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.IMPORT_DECL) {
                    return ParserRuleContext.IMPORT_PREFIX;
                } else if (parentCtx == ParserRuleContext.XML_NAMESPACE_DECLARATION) {
                    return ParserRuleContext.NAMESPACE_PREFIX;
                }
                throw new IllegalStateException();
            case MAJOR_VERSION:
            case MINOR_VERSION:
            case IMPORT_SUB_VERSION:
                return ParserRuleContext.MAJOR_MINOR_VERSION_END;
            case PATCH_VERSION:
                return ParserRuleContext.IMPORT_PREFIX_DECL;
            case IMPORT_DECL:
                return ParserRuleContext.IMPORT_KEYWORD;
            case CONTINUE_STATEMENT:
                return ParserRuleContext.CONTINUE_KEYWORD;
            case BREAK_STATEMENT:
                return ParserRuleContext.BREAK_KEYWORD;
            case CONTINUE_KEYWORD:
            case BREAK_KEYWORD:
                return ParserRuleContext.SEMICOLON;
            case RETURN_STMT:
                return ParserRuleContext.RETURN_KEYWORD;
            case RETURN_KEYWORD:
                return ParserRuleContext.RETURN_STMT_RHS;
            case ACCESS_EXPRESSION:
                return ParserRuleContext.VARIABLE_REF;
            case MAPPING_FIELD_NAME:
                return ParserRuleContext.SPECIFIC_FIELD_RHS;
            case COLON:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.MAPPING_CONSTRUCTOR) {
                    return ParserRuleContext.EXPRESSION;
                } else if (parentCtx == ParserRuleContext.MULTI_RECEIVE_WORKERS) {
                    return ParserRuleContext.PEER_WORKER_NAME;
                } else if (parentCtx == ParserRuleContext.MULTI_WAIT_FIELDS) {
                    return ParserRuleContext.EXPRESSION;
                } else if (parentCtx == ParserRuleContext.CONDITIONAL_EXPRESSION) {
                    endContext(); // end conditional-expr
                    return ParserRuleContext.EXPRESSION;
                } else if (parentCtx == ParserRuleContext.MAPPING_BINDING_PATTERN) {
                    return ParserRuleContext.VARIABLE_NAME;
                } else if (parentCtx == ParserRuleContext.FIELD_BINDING_PATTERN) {
                    endContext();
                    return ParserRuleContext.VARIABLE_NAME;
                } else if (parentCtx == ParserRuleContext.XML_ATOMIC_NAME_PATTERN) {
                    return ParserRuleContext.XML_ATOMIC_NAME_IDENTIFIER_RHS;
                }

                return ParserRuleContext.IDENTIFIER;
            case STRING_LITERAL:
                // We assume string literal is specifically used only in the mapping constructor key.
                return ParserRuleContext.COLON;
            case COMPUTED_FIELD_NAME:
                return ParserRuleContext.OPEN_BRACKET;
            case LISTENERS_LIST:
                return ParserRuleContext.EXPRESSION;
            case ON_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ANNOTATION_DECL) {
                    return ParserRuleContext.ANNOT_ATTACH_POINTS_LIST;
                }
                return ParserRuleContext.LISTENERS_LIST;
            case RESOURCE_KEYWORD:
                return ParserRuleContext.FUNC_DEF;
            case SERVICE_DECL:
                return ParserRuleContext.SERVICE_KEYWORD;
            case SERVICE_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.SERVICE_CONSTRUCTOR_EXPRESSION) {
                    return ParserRuleContext.LISTENERS_LIST;
                }
                return ParserRuleContext.OPTIONAL_SERVICE_NAME;
            case SERVICE_NAME:
                return ParserRuleContext.ON_KEYWORD;
            case LISTENER_KEYWORD:
                return ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER;
            case LISTENER_DECL:
                return ParserRuleContext.LISTENER_KEYWORD;
            case FINAL_KEYWORD:
                // Assume the final keyword is only used in var-decl.
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case CONSTANT_DECL:
                return ParserRuleContext.CONST_KEYWORD;
            case CONST_KEYWORD:
                return ParserRuleContext.CONST_DECL_TYPE;
            case CONST_DECL_TYPE:
                return ParserRuleContext.CONST_DECL_RHS;
            case NIL_TYPE_DESCRIPTOR:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case TYPEOF_EXPRESSION:
                return ParserRuleContext.TYPEOF_KEYWORD;
            case TYPEOF_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case OPTIONAL_TYPE_DESCRIPTOR:
                return ParserRuleContext.QUESTION_MARK;
            case UNARY_EXPRESSION:
                return ParserRuleContext.UNARY_OPERATOR;
            case UNARY_OPERATOR:
                return ParserRuleContext.EXPRESSION;
            case ARRAY_TYPE_DESCRIPTOR:
                return ParserRuleContext.OPEN_BRACKET;
            case ARRAY_LENGTH:
                return ParserRuleContext.CLOSE_BRACKET;
            case AT:
                return ParserRuleContext.ANNOT_REFERENCE;
            case DOC_STRING:
                return ParserRuleContext.ANNOTATIONS;
            case ANNOTATIONS:
                return ParserRuleContext.AT;
            case MAPPING_CONSTRUCTOR:
                return ParserRuleContext.OPEN_BRACE;
            case VARIABLE_REF:
            case TYPE_REFERENCE:
            case ANNOT_REFERENCE:
            case FIELD_ACCESS_IDENTIFIER:
                return ParserRuleContext.QUALIFIED_IDENTIFIER;
            case QUALIFIED_IDENTIFIER:
            case XML_ATOMIC_NAME_IDENTIFIER:
                nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.COLON_TOKEN) {
                    return ParserRuleContext.COLON;
                }
                // Else this is a simple identifier. Hence fall through.
            case IDENTIFIER:
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case VARIABLE_REF:
                        endContext();
                        return getNextRuleForExpr();
                    case TYPE_REFERENCE:
                        endContext();
                        if (isInTypeDescContext()) {
                            return ParserRuleContext.TYPEDESC_RHS;
                        }
                        return ParserRuleContext.SEMICOLON;
                    case ANNOT_REFERENCE:
                        endContext();
                        return ParserRuleContext.ANNOTATION_REF_RHS;
                    case ANNOTATION_DECL:
                        return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
                    case FIELD_ACCESS_IDENTIFIER:
                        endContext();
                        return ParserRuleContext.EXPRESSION_RHS;
                    case XML_ATOMIC_NAME_PATTERN:
                        endContext();
                        return ParserRuleContext.XML_NAME_PATTERN_RHS;
                    default:
                        throw new IllegalStateException(parentCtx.toString());
                }
            case IS_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_EXPRESSION;
            case NULL_KEYWORD:
                return ParserRuleContext.EXPRESSION_RHS;
            case NIL_LITERAL:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case LOCAL_TYPE_DEFINITION_STMT:
                return ParserRuleContext.TYPE_KEYWORD;
            case RIGHT_ARROW:
                return ParserRuleContext.EXPRESSION;
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case TYPE_NAME_OR_VAR_NAME:
                return getNextRuleForDecimalIntegerLiteral();
            case EXPRESSION_STATEMENT:
                return ParserRuleContext.EXPRESSION_STATEMENT_START;
            case MAP_KEYWORD:
            case FUTURE_KEYWORD:
            case LOCK_STMT:
                return ParserRuleContext.LOCK_KEYWORD;
            case LOCK_KEYWORD:
                return ParserRuleContext.BLOCK_STMT;
            case RECORD_FIELD:
                return ParserRuleContext.RECORD_FIELD_START;
            case ANNOTATION_TAG:
                return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
            case ANNOTATION_KEYWORD:
                return ParserRuleContext.ANNOT_DECL_OPTIONAL_TYPE;
            case ANNOT_ATTACH_POINTS_LIST:
                return ParserRuleContext.ATTACH_POINT;
            case FIELD_IDENT:
            case FUNCTION_IDENT:
            case IDENT_AFTER_OBJECT_IDENT:
            case SINGLE_KEYWORD_ATTACH_POINT_IDENT:
            case ATTACH_POINT:
                return ParserRuleContext.ATTACH_POINT_END;
            case RECORD_FIELD_OR_RECORD_END:
                return ParserRuleContext.RECORD_BODY_END;
            case SOURCE_KEYWORD:
                return ParserRuleContext.ATTACH_POINT_IDENT;
            case OBJECT_IDENT:
                return ParserRuleContext.IDENT_AFTER_OBJECT_IDENT;
            case RECORD_IDENT:
                return ParserRuleContext.FIELD_IDENT;
            case RESOURCE_IDENT:
                return ParserRuleContext.FUNCTION_IDENT;
            case ANNOTATION_DECL:
                return ParserRuleContext.ANNOTATION_KEYWORD;
            case XML_NAMESPACE_DECLARATION:
                return ParserRuleContext.XMLNS_KEYWORD;
            case XMLNS_KEYWORD:
                return ParserRuleContext.CONSTANT_EXPRESSION;
            case CONSTANT_EXPRESSION:
                return ParserRuleContext.CONSTANT_EXPRESSION_START;
            case XML_NAMESPACE_PREFIX_DECL:
                return ParserRuleContext.SEMICOLON;
            case NAMED_WORKER_DECL:
                return ParserRuleContext.WORKER_KEYWORD;
            case WORKER_KEYWORD:
                return ParserRuleContext.WORKER_NAME;
            case WORKER_NAME:
                return ParserRuleContext.WORKER_NAME_RHS;
            case FORK_STMT:
                return ParserRuleContext.FORK_KEYWORD;
            case SERVICE_CONSTRUCTOR_EXPRESSION:
                return ParserRuleContext.SERVICE_KEYWORD;
            case XML_FILTER_EXPR:
                return ParserRuleContext.DOT_LT_TOKEN;
            case DOT_LT_TOKEN:
                return ParserRuleContext.XML_NAME_PATTERN;
            case XML_NAME_PATTERN:
                return ParserRuleContext.XML_ATOMIC_NAME_PATTERN;
            case XML_ATOMIC_NAME_PATTERN:
                return ParserRuleContext.XML_ATOMIC_NAME_PATTERN_START;
            case XML_STEP_EXPR:
                return ParserRuleContext.XML_STEP_START;
            case SLASH_ASTERISK_TOKEN:
                return ParserRuleContext.EXPRESSION_RHS;
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
            case SLASH_LT_TOKEN:
                return ParserRuleContext.XML_NAME_PATTERN;
            default:
                return getNextRuleInternal(currentCtx, nextLookahead);

        }
    }

    private ParserRuleContext getNextRuleInternal(ParserRuleContext currentCtx, int nextLookahead) {
        ParserRuleContext parentCtx;
        switch (currentCtx) {
            case FORK_KEYWORD:
                return ParserRuleContext.OPEN_BRACE;
            case TRAP_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case LIST_CONSTRUCTOR:
                return ParserRuleContext.OPEN_BRACKET;
            case FOREACH_STMT:
                return ParserRuleContext.FOREACH_KEYWORD;
            case FOREACH_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case IN_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_EXPR_LET_VAR_DECL) {
                    endContext(); // end let-expr-let-var-decl
                }
                return ParserRuleContext.EXPRESSION;
            case TYPE_CAST:
                return ParserRuleContext.LT;
            case PIPE:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ALTERNATE_WAIT_EXPRS) {
                    return ParserRuleContext.EXPRESSION;
                } else if (parentCtx == ParserRuleContext.XML_NAME_PATTERN) {
                    return ParserRuleContext.XML_ATOMIC_NAME_PATTERN;
                }
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case TABLE_CONSTRUCTOR:
                return ParserRuleContext.OPEN_BRACKET;
            case TABLE_KEYWORD:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.ROW_TYPE_PARAM;
                }
                return ParserRuleContext.TABLE_KEYWORD_RHS;
            case KEY_SPECIFIER:
                return ParserRuleContext.KEY_KEYWORD;
            case KEY_KEYWORD:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.KEY_CONSTRAINTS_RHS;
                }
                return ParserRuleContext.OPEN_PARENTHESIS;
            case ERROR_KEYWORD:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.ERROR_TYPE_PARAM_START;
                }
                return ParserRuleContext.ARG_LIST_START;
            case ERROR_TYPE_PARAM_START:
                return ParserRuleContext.ERROR_TYPE_PARAMS;
            case LET_EXPRESSION:
                return ParserRuleContext.LET_KEYWORD;
            case LET_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.QUERY_EXPRESSION) {
                    return ParserRuleContext.LET_CLAUSE_LET_VAR_DECL;
                } else if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext(); // end let-clause-let-var-decl
                    return ParserRuleContext.LET_CLAUSE_LET_VAR_DECL;
                }
                return ParserRuleContext.LET_EXPR_LET_VAR_DECL;
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
                return ParserRuleContext.LET_VAR_DECL_START;
            case STREAM_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION) {
                    return ParserRuleContext.QUERY_EXPRESSION;
                }
                return ParserRuleContext.LT;
            case END_OF_TYPE_DESC:
                return getNextRuleForTypeDescriptor();
            case TYPED_BINDING_PATTERN:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case CAPTURE_BINDING_PATTERN:
                return ParserRuleContext.VARIABLE_NAME;
            case REST_BINDING_PATTERN:
                return ParserRuleContext.ELLIPSIS;
            case LIST_BINDING_PATTERN:
                return ParserRuleContext.OPEN_BRACKET;
            case MAPPING_BINDING_PATTERN:
                return ParserRuleContext.OPEN_BRACE;
            case FIELD_BINDING_PATTERN:
                return ParserRuleContext.FIELD_BINDING_PATTERN_NAME;
            case FIELD_BINDING_PATTERN_NAME:
                return ParserRuleContext.FIELD_BINDING_PATTERN_END;
            case PARAMETERIZED_TYPE:
                return ParserRuleContext.LT;
            case NEW_KEYWORD:
                return ParserRuleContext.NEW_KEYWORD_RHS;
            case LT:
                return getNextRuleForLt();
            case GT:
                return getNextRuleForGt(nextLookahead);
            case TEMPLATE_END:
                return ParserRuleContext.EXPRESSION_RHS;
            case TEMPLATE_START:
                return ParserRuleContext.TEMPLATE_BODY;
            case TEMPLATE_BODY:
                return ParserRuleContext.TEMPLATE_MEMBER;
            case TEMPLATE_STRING:
                return ParserRuleContext.TEMPLATE_STRING_RHS;
            case INTERPOLATION_START_TOKEN:
                return ParserRuleContext.EXPRESSION;
            case XML_KEYWORD:
            case STRING_KEYWORD:
            case BASE16_KEYWORD:
            case BASE64_KEYWORD:
                return ParserRuleContext.TEMPLATE_START;
            case ARG_LIST_START:
                return ParserRuleContext.ARG_LIST;
            case ARG_LIST_END:
            case QUERY_EXPRESSION_END:
                endContext();
                return ParserRuleContext.EXPRESSION_RHS;
            case ARG_LIST:
                return ParserRuleContext.ARG_START_OR_ARG_LIST_END;
            case TYPE_DESC_IN_ANNOTATION_DECL:
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
            case TYPE_DESC_IN_PARAM:
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
            case TYPE_DESC_IN_TYPE_DEF:
            case TYPE_DESC_IN_ANGLE_BRACKETS:
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
            case TYPE_DESC_IN_EXPRESSION:
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
            case TYPE_DESC_IN_PARENTHESIS:
            case TYPE_DESC_IN_NEW_EXPR:
            case TYPE_DESC_IN_TUPLE:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case VAR_DECL_STARTED_WITH_DENTIFIER:
                // We come here trying to recover statement started with identifier,
                // and trying to match it against a var-decl. Since this wasn't a var-decl
                // originally, a context for type hasn't started yet. Therefore start a
                // a context manually here.
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                return ParserRuleContext.TYPEDESC_RHS;
            case INFERRED_TYPE_DESC:
                return ParserRuleContext.GT;
            case ROW_TYPE_PARAM:
                return ParserRuleContext.LT;
            case PARENTHESISED_TYPE_DESC_START:
                return ParserRuleContext.TYPE_DESC_IN_PARENTHESIS;
            case SELECT_CLAUSE:
                return ParserRuleContext.SELECT_KEYWORD;
            case SELECT_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.QUERY_EXPRESSION) {
                    endContext(); // end query-expression
                }
                if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext(); // end let-clause-let-var-decl
                    endContext(); // end query-expression
                }
                return ParserRuleContext.EXPRESSION;
            case WHERE_CLAUSE:
                return ParserRuleContext.WHERE_KEYWORD;
            case WHERE_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext(); // end let-clause-let-var-decl
                }
                return ParserRuleContext.EXPRESSION;
            case FROM_CLAUSE:
                return ParserRuleContext.FROM_KEYWORD;
            case FROM_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext(); // end let-clause-let-var-decl
                }
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case LET_CLAUSE:
                return ParserRuleContext.LET_KEYWORD;
            case QUERY_EXPRESSION:
                return ParserRuleContext.FROM_CLAUSE;
            case TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION:
                return ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_START;
            case BITWISE_AND_OPERATOR:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case EXPR_FUNC_BODY_START:
                return ParserRuleContext.EXPRESSION;
            case MODULE_LEVEL_AMBIGUOUS_FUNC_TYPE_DESC_RHS:
                endContext();
                // We come here trying to recover module-var-decl/object-member started with function,
                // keyword and trying to match it against a var-decl. Since this wasn't a var-decl
                // originally, a context for type hasn't started yet. Therefore start a
                // a context manually here.
                startContext(ParserRuleContext.VAR_DECL_STMT);
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                return ParserRuleContext.TYPEDESC_RHS;
            case STMT_LEVEL_AMBIGUOUS_FUNC_TYPE_DESC_RHS:
                endContext();
                // We come here trying to recover statement started with function-keyword,
                // and trying to match it against a var-decl. Since this wasn't a var-decl
                // originally, a context for type hasn't started yet. Therefore switch to
                // var-decl context
                switchContext(ParserRuleContext.VAR_DECL_STMT);
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                return ParserRuleContext.TYPEDESC_RHS;
            case FUNC_TYPE_DESC_END:
                endContext();
                return ParserRuleContext.TYPEDESC_RHS;
            case IMPLICIT_ANON_FUNC_PARAM:
                return ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS;
            case EXPLICIT_ANON_FUNC_EXPR_BODY_START:
                endContext(); // end explicit anon-func
                return ParserRuleContext.EXPR_FUNC_BODY_START;
            case OBJECT_MEMBER:
                return ParserRuleContext.OBJECT_MEMBER_START;
            case ANNOTATION_END:
                return getNextRuleForAnnotationEnd(nextLookahead);
            case START_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case FLUSH_KEYWORD:
                return ParserRuleContext.OPTIONAL_PEER_WORKER;
            case PEER_WORKER_NAME:
            case DEFAULT_KEYWORD:
                if (getParentContext() == ParserRuleContext.MULTI_RECEIVE_WORKERS) {
                    return ParserRuleContext.RECEIVE_FIELD_END;
                }
                return ParserRuleContext.EXPRESSION_RHS;
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return ParserRuleContext.SIGNED_INT_OR_FLOAT_RHS;
            case SIGNED_INT_OR_FLOAT_RHS:
                return getNextRuleForExpr();
            case TUPLE_TYPE_DESC_START:
                return ParserRuleContext.TYPE_DESC_IN_TUPLE;
            case TYPE_DESC_IN_TUPLE_RHS:
                return ParserRuleContext.OPEN_BRACKET;
            case WORKER_NAME_OR_METHOD_NAME:
                return ParserRuleContext.WORKER_NAME_OR_METHOD_NAME;
            case DEFAULT_WORKER_NAME_IN_ASYNC_SEND:
                return ParserRuleContext.SEMICOLON;
            case SYNC_SEND_TOKEN:
                return ParserRuleContext.PEER_WORKER_NAME;
            case LEFT_ARROW_TOKEN:
                return ParserRuleContext.RECEIVE_WORKERS;
            case MULTI_RECEIVE_WORKERS:
                return ParserRuleContext.OPEN_BRACE;
            case RECEIVE_FIELD_NAME:
                return ParserRuleContext.COLON;
            case WAIT_KEYWORD:
                return ParserRuleContext.WAIT_KEYWORD_RHS;
            case WAIT_FIELD_NAME:
                return ParserRuleContext.WAIT_FIELD_NAME_RHS;
            case ALTERNATE_WAIT_EXPR_LIST_END:
                return getNextRuleForWaitExprListEnd();
            case MULTI_WAIT_FIELDS:
                return ParserRuleContext.OPEN_BRACE;
            case ALTERNATE_WAIT_EXPRS:
                return ParserRuleContext.EXPRESSION;
            case ANNOT_CHAINING_TOKEN:
                return ParserRuleContext.FIELD_ACCESS_IDENTIFIER;
            case DO_CLAUSE:
                return ParserRuleContext.DO_KEYWORD;
            case DO_KEYWORD:
                return ParserRuleContext.OPEN_BRACE;
            case LET_CLAUSE_END:
                endContext();
                return ParserRuleContext.QUERY_PIPELINE_RHS;
            case MEMBER_ACCESS_KEY_EXPR:
                return ParserRuleContext.OPEN_BRACKET;
            case OPTIONAL_CHAINING_TOKEN:
                return ParserRuleContext.FIELD_ACCESS_IDENTIFIER;
            case CONDITIONAL_EXPRESSION:
                return ParserRuleContext.QUESTION_MARK;
            case TRANSACTION_STMT:
                return ParserRuleContext.TRANSACTION_KEYWORD;
            case RETRY_STMT:
                return ParserRuleContext.RETRY_KEYWORD;
            case ROLLBACK_STMT:
                return ParserRuleContext.ROLLBACK_KEYWORD;
            case TRANSACTION_KEYWORD:
                return ParserRuleContext.BLOCK_STMT;
            case COMMIT_KEYWORD:
                return ParserRuleContext.EXPRESSION_RHS;
            case ROLLBACK_KEYWORD:
                return ParserRuleContext.ROLLBACK_RHS;
            case RETRY_KEYWORD:
                return ParserRuleContext.RETRY_KEYWORD_RHS;
            case TRANSACTIONAL_KEYWORD:
                return ParserRuleContext.EXPRESSION_RHS;
            case MODULE_ENUM_DECLARATION:
                return ParserRuleContext.ENUM_KEYWORD;
            case ENUM_KEYWORD:
                return ParserRuleContext.MODULE_ENUM_NAME;
            case MODULE_ENUM_NAME:
                return ParserRuleContext.OPEN_BRACE;
            case ENUM_MEMBER_LIST:
                return ParserRuleContext.ENUM_MEMBER_START;
            case ENUM_MEMBER_NAME:
                return ParserRuleContext.ENUM_MEMBER_RHS;
            case TYPED_BINDING_PATTERN_TYPE_RHS:
                return ParserRuleContext.BINDING_PATTERN;
            case UNION_OR_INTERSECTION_TOKEN:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case READONLY_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.MAPPING_CONSTRUCTOR ||
                        parentCtx == ParserRuleContext.MAPPING_FIELD) {
                    return ParserRuleContext.SPECIFIC_FIELD;
                }
                // fall through
            default:
                throw new IllegalStateException("cannot find the next rule for: " + currentCtx);
        }
    }

    private void startContextIfRequired(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case COMP_UNIT:
            case FUNC_DEF_OR_FUNC_TYPE:
            case ANON_FUNC_EXPRESSION:
            case FUNC_DEF:
            case FUNC_TYPE_DESC:
            case EXTERNAL_FUNC_BODY:
            case FUNC_BODY_BLOCK:
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case VAR_DECL_STMT:
            case ASSIGNMENT_STMT:
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
            case MODULE_TYPE_DEFINITION:
            case RECORD_FIELD:
            case RECORD_TYPE_DESCRIPTOR:
            case OBJECT_TYPE_DESCRIPTOR:
            case ARG_LIST:
            case OBJECT_FUNC_OR_FIELD:
            case IF_BLOCK:
            case BLOCK_STMT:
            case WHILE_BLOCK:
            case PANIC_STMT:
            case CALL_STMT:
            case IMPORT_DECL:
            case CONTINUE_STATEMENT:
            case BREAK_STATEMENT:
            case RETURN_STMT:
            case COMPUTED_FIELD_NAME:
            case LISTENERS_LIST:
            case SERVICE_DECL:
            case LISTENER_DECL:
            case CONSTANT_DECL:
            case NIL_TYPE_DESCRIPTOR:
            case COMPOUND_ASSIGNMENT_STMT:
            case OPTIONAL_TYPE_DESCRIPTOR:
            case ARRAY_TYPE_DESCRIPTOR:
            case ANNOTATIONS:
            case VARIABLE_REF:
            case TYPE_REFERENCE:
            case ANNOT_REFERENCE:
            case FIELD_ACCESS_IDENTIFIER:
            case MAPPING_CONSTRUCTOR:
            case LOCAL_TYPE_DEFINITION_STMT:
            case EXPRESSION_STATEMENT:
            case NIL_LITERAL:
            case LOCK_STMT:
            case ANNOTATION_DECL:
            case ANNOT_ATTACH_POINTS_LIST:
            case XML_NAMESPACE_DECLARATION:
            case CONSTANT_EXPRESSION:
            case NAMED_WORKER_DECL:
            case FORK_STMT:
            case FOREACH_STMT:
            case LIST_CONSTRUCTOR:
            case TYPE_CAST:
            case KEY_SPECIFIER:
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
            case ROW_TYPE_PARAM:
            case TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION:
            case OBJECT_MEMBER:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case TYPED_BINDING_PATTERN:
            case CAPTURE_BINDING_PATTERN:
            case MULTI_RECEIVE_WORKERS:
            case MULTI_WAIT_FIELDS:
            case ALTERNATE_WAIT_EXPRS:
            case DO_CLAUSE:
            case MEMBER_ACCESS_KEY_EXPR:
            case CONDITIONAL_EXPRESSION:
            case TRANSACTION_STMT:
            case RETRY_STMT:
            case ROLLBACK_STMT:
            case MODULE_ENUM_DECLARATION:
            case ENUM_MEMBER_LIST:
            case SERVICE_CONSTRUCTOR_EXPRESSION:
            case XML_NAME_PATTERN:
            case XML_ATOMIC_NAME_PATTERN:

                // Contexts that expect a type
            case TYPE_DESC_IN_ANNOTATION_DECL:
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
            case TYPE_DESC_IN_PARAM:
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
            case TYPE_DESC_IN_TYPE_DEF:
            case TYPE_DESC_IN_ANGLE_BRACKETS:
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
            case TYPE_DESC_IN_EXPRESSION:
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
            case TYPE_DESC_IN_PARENTHESIS:
            case TYPE_DESC_IN_NEW_EXPR:
            case TYPE_DESC_IN_TUPLE:
                startContext(currentCtx);
                break;
            default:
                break;
        }

        switch (currentCtx) {
            case TABLE_CONSTRUCTOR:
            case QUERY_EXPRESSION:
                switchContext(currentCtx);
                break;
            default:
                break;
        }
    }

    private ParserRuleContext getNextRuleForCloseParenthsis() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.PARAM_LIST) {
            endContext(); // end parameters
            return ParserRuleContext.FUNC_OPTIONAL_RETURNS;
        } else if (isParameter(parentCtx)) {
            endContext(); // end parameters
            endContext(); // end parameter
            return ParserRuleContext.FUNC_OPTIONAL_RETURNS;
        } else if (parentCtx == ParserRuleContext.NIL_TYPE_DESCRIPTOR) {
            endContext();
            // After parsing nil type descriptor all the other parsing is same as next rule of simple type
            return ParserRuleContext.TYPEDESC_RHS;
        } else if (parentCtx == ParserRuleContext.NIL_LITERAL) {
            endContext();
            return getNextRuleForExpr();
        } else if (parentCtx == ParserRuleContext.KEY_SPECIFIER) {
            endContext(); // end key-specifier
            if (isInTypeDescContext()) {
                return ParserRuleContext.TYPEDESC_RHS;
            }
            return ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_RHS;
        } else if (isInTypeDescContext()) {
            return ParserRuleContext.TYPEDESC_RHS;
        } else if (parentCtx == ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAMS) {
            endContext(); // end infered-param/parenthesised-expr context
            return ParserRuleContext.INFER_PARAM_END_OR_PARENTHESIS_END;
        }
        return ParserRuleContext.EXPRESSION_RHS;
    }

    private ParserRuleContext getNextRuleForOpenParenthesis() {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.EXPRESSION_STATEMENT) {
            return ParserRuleContext.EXPRESSION_STATEMENT_START;
        } else if (isStatement(parentCtx) || isExpressionContext(parentCtx) ||
                parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
            return ParserRuleContext.EXPRESSION;
        } else if (parentCtx == ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE ||
                parentCtx == ParserRuleContext.FUNC_TYPE_DESC || parentCtx == ParserRuleContext.FUNC_DEF ||
                parentCtx == ParserRuleContext.ANON_FUNC_EXPRESSION ||
                parentCtx == ParserRuleContext.FUNC_TYPE_DESC_OR_ANON_FUNC) {
            // TODO: find a better way
            startContext(ParserRuleContext.PARAM_LIST);
            return ParserRuleContext.PARAM_LIST;
        } else if (parentCtx == ParserRuleContext.NIL_TYPE_DESCRIPTOR || parentCtx == ParserRuleContext.NIL_LITERAL) {
            return ParserRuleContext.CLOSE_PARENTHESIS;
        } else if (parentCtx == ParserRuleContext.KEY_SPECIFIER) {
            return ParserRuleContext.KEY_SPECIFIER_RHS;
        } else if (isInTypeDescContext()) {
            // if the parent context is table type desc then we are in key specifier context.hence start context
            startContext(ParserRuleContext.KEY_SPECIFIER);
            return ParserRuleContext.KEY_SPECIFIER_RHS;
        } else if (isParameter(parentCtx)) {
            return ParserRuleContext.EXPRESSION;
        }
        return ParserRuleContext.EXPRESSION;
    }

    private ParserRuleContext getNextRuleForOpenBrace(int nextLookahead) {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.LISTENERS_LIST) {
            endContext();
        }

        switch (parentCtx) {
            case OBJECT_TYPE_DESCRIPTOR:
                return ParserRuleContext.OBJECT_MEMBER;
            case RECORD_TYPE_DESCRIPTOR:
                return ParserRuleContext.RECORD_FIELD;
            case MAPPING_CONSTRUCTOR:
                return ParserRuleContext.FIRST_MAPPING_FIELD;
            case FORK_STMT:
                return ParserRuleContext.NAMED_WORKER_DECL;
            case MULTI_RECEIVE_WORKERS:
                return ParserRuleContext.RECEIVE_FIELD;
            case MULTI_WAIT_FIELDS:
                return ParserRuleContext.WAIT_FIELD_NAME;
            case MODULE_ENUM_DECLARATION:
                return ParserRuleContext.ENUM_MEMBER_LIST;
            case MAPPING_BINDING_PATTERN:
                return ParserRuleContext.MAPPING_BINDING_PATTERN_MEMBER;
            default:
                return ParserRuleContext.STATEMENT;
        }
    }

    private boolean isExpressionContext(ParserRuleContext ctx) {
        switch (ctx) {
            case LISTENERS_LIST:
            case MAPPING_CONSTRUCTOR:
            case COMPUTED_FIELD_NAME:
            case LIST_CONSTRUCTOR:
            case INTERPOLATION:
            case ARG_LIST:
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
            case TABLE_CONSTRUCTOR:
            case QUERY_EXPRESSION:
            case TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION:
            case SERVICE_CONSTRUCTOR_EXPRESSION:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#AFTER_PARAMETER_TYPE}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForParamType() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.REQUIRED_PARAM || parentCtx == ParserRuleContext.DEFAULTABLE_PARAM) {
            return ParserRuleContext.VARIABLE_NAME;
        } else if (parentCtx == ParserRuleContext.REST_PARAM) {
            return ParserRuleContext.ELLIPSIS;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#COMMA}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForComma() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case PARAM_LIST:
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                endContext();
                return parentCtx;
            case ARG_LIST:
                return ParserRuleContext.ARG_START;
            case MAPPING_CONSTRUCTOR:
                return ParserRuleContext.MAPPING_FIELD;
            case LISTENERS_LIST:
            case LIST_CONSTRUCTOR:
                return ParserRuleContext.EXPRESSION;
            case ANNOT_ATTACH_POINTS_LIST:
                return ParserRuleContext.ATTACH_POINT;
            case TABLE_CONSTRUCTOR:
                return ParserRuleContext.MAPPING_CONSTRUCTOR;
            case KEY_SPECIFIER:
                return ParserRuleContext.VARIABLE_NAME;
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
                return ParserRuleContext.LET_VAR_DECL_START;
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case BRACED_EXPR_OR_ANON_FUNC_PARAMS:
                return ParserRuleContext.IMPLICIT_ANON_FUNC_PARAM;
            case TYPE_DESC_IN_TUPLE:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case LIST_BINDING_PATTERN:
                return ParserRuleContext.LIST_BINDING_PATTERN_CONTENTS;
            case MAPPING_BINDING_PATTERN:
                return ParserRuleContext.MAPPING_BINDING_PATTERN_MEMBER;
            case MULTI_RECEIVE_WORKERS:
                return ParserRuleContext.RECEIVE_FIELD;
            case MULTI_WAIT_FIELDS:
                return ParserRuleContext.WAIT_FIELD_NAME;
            case ENUM_MEMBER_LIST:
                return ParserRuleContext.ENUM_MEMBER_START;
            case MEMBER_ACCESS_KEY_EXPR:
                return ParserRuleContext.MEMBER_ACCESS_KEY_EXPR_END;
            case STMT_START_BRACKETED_LIST:
                return ParserRuleContext.STMT_START_BRACKETED_LIST_MEMBER;
            case BRACKETED_LIST:
                return ParserRuleContext.BRACKETED_LIST_MEMBER;
            default:
                throw new IllegalStateException(parentCtx.toString());
        }
    }

    /**
     * Get the next parser context to visit after a type descriptor.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForTypeDescriptor() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            // Contexts that expect a type
            case TYPE_DESC_IN_ANNOTATION_DECL:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.ANNOTATION_TAG;
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.VARIABLE_NAME;
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                if (getParentContext() == ParserRuleContext.FOREACH_STMT) {
                    return ParserRuleContext.BINDING_PATTERN;
                }
                return ParserRuleContext.VARIABLE_NAME;
            case TYPE_DESC_IN_PARAM:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.AFTER_PARAMETER_TYPE;
            case TYPE_DESC_IN_TYPE_DEF:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.SEMICOLON;
            case TYPE_DESC_IN_ANGLE_BRACKETS:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.GT;
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }

                parentCtx = getParentContext();
                switch (parentCtx) {
                    case FUNC_TYPE_DESC:
                        endContext();
                        return ParserRuleContext.TYPEDESC_RHS;
                    case FUNC_DEF_OR_FUNC_TYPE:
                        return ParserRuleContext.FUNC_BODY_OR_TYPE_DESC_RHS;
                    case FUNC_TYPE_DESC_OR_ANON_FUNC:
                        return ParserRuleContext.FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY;
                    case FUNC_DEF:
                        return ParserRuleContext.FUNC_BODY;
                    case ANON_FUNC_EXPRESSION:
                        return ParserRuleContext.ANON_FUNC_BODY;
                    case NAMED_WORKER_DECL:
                        return ParserRuleContext.BLOCK_STMT;
                    default:
                        throw new IllegalStateException(parentCtx.toString());
                }
            case TYPE_DESC_IN_EXPRESSION:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.EXPRESSION_RHS;
            case COMP_UNIT:
                /*
                 * Fact 1:
                 * ------
                 * FUNC_DEF_OR_FUNC_TYPE is only possible for module level construct or object member
                 * that starts with 'function' keyword. However, until the end of func-signature,
                 * we don't know whether this is a func-def or a function type.
                 * Hence a var-decl-stmt context is not started until this point.
                 *
                 * Fact 2:
                 * ------
                 * We reach here for END_OF_TYPE_DESC context. That means we are going to end the
                 * func-type-desc.
                 */
                startContext(ParserRuleContext.VAR_DECL_STMT);
                return ParserRuleContext.VARIABLE_NAME; // TODO add typed-binding-patters
            case OBJECT_MEMBER:
                return ParserRuleContext.VARIABLE_NAME;
            case ANNOTATION_DECL:
                return ParserRuleContext.IDENTIFIER;
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
                return ParserRuleContext.STREAM_TYPE_FIRST_PARAM_RHS;
            case TYPE_DESC_IN_PARENTHESIS:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case TYPE_DESC_IN_NEW_EXPR:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.ARG_LIST_START;
            case TYPE_DESC_IN_TUPLE:
            case STMT_START_BRACKETED_LIST:
                return ParserRuleContext.TYPE_DESC_IN_TUPLE_RHS;
            default:
                // If none of the above that means we reach here via, anonymous-func-or-func-type context.
                // Then the rhs of this is definitely an expression-rhs
                return ParserRuleContext.EXPRESSION_RHS;
        }
    }

    private boolean isInTypeDescContext() {
        switch (getParentContext()) {
            case TYPE_DESC_IN_ANNOTATION_DECL:
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
            case TYPE_DESC_IN_PARAM:
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
            case TYPE_DESC_IN_TYPE_DEF:
            case TYPE_DESC_IN_ANGLE_BRACKETS:
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
            case TYPE_DESC_IN_EXPRESSION:
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
            case TYPE_DESC_IN_PARENTHESIS:
            case TYPE_DESC_IN_NEW_EXPR:
            case TYPE_DESC_IN_TUPLE:
            case STMT_START_BRACKETED_LIST:
            case BRACKETED_LIST:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#ASSIGN_OP}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForEqualOp() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case EXTERNAL_FUNC_BODY:
                return ParserRuleContext.EXTERNAL_KEYWORD;
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case RECORD_FIELD:
            case ARG_LIST:
            case OBJECT_MEMBER:
            case LISTENER_DECL:
            case CONSTANT_DECL:
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
            case ENUM_MEMBER_LIST:
                return ParserRuleContext.EXPRESSION;
            default:
                if (isStatement(parentCtx)) {
                    return ParserRuleContext.EXPRESSION;
                }
                throw new IllegalStateException("equal op cannot exist in a " + parentCtx);
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#CLOSE_BRACE}.
     *
     * @param nextLookahead Position of the next token to consider, relative to the position of the original error
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForCloseBrace(int nextLookahead) {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case FUNC_BODY_BLOCK:
                endContext(); // end body block
                STToken nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                    return ParserRuleContext.EOF;
                }

                parentCtx = getParentContext();
                switch (parentCtx) {
                    case SERVICE_DECL:
                        return ParserRuleContext.RESOURCE_DEF;
                    case OBJECT_MEMBER:
                        return ParserRuleContext.OBJECT_MEMBER_START;
                    case COMP_UNIT:
                        return ParserRuleContext.TOP_LEVEL_NODE;
                    case FUNC_DEF:
                    case FUNC_DEF_OR_FUNC_TYPE:
                        endContext(); // end func-def
                        return ParserRuleContext.TOP_LEVEL_NODE;
                    case ANON_FUNC_EXPRESSION:
                    default:
                        // Anonynous func
                        endContext(); // end anon-func
                        return ParserRuleContext.EXPRESSION_RHS;
                }
            case SERVICE_DECL:
                endContext();
                nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                    return ParserRuleContext.EOF;
                }
                return ParserRuleContext.TOP_LEVEL_NODE;
            case OBJECT_MEMBER:
                endContext(); // end object member
                // fall through
            case RECORD_TYPE_DESCRIPTOR:
            case OBJECT_TYPE_DESCRIPTOR:
                endContext(); // end record/object type def
                return ParserRuleContext.TYPEDESC_RHS;
            case BLOCK_STMT:
            case AMBIGUOUS_STMT:
                endContext(); // end block stmt
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case LOCK_STMT:
                    case FOREACH_STMT:
                    case WHILE_BLOCK:
                    case RETRY_STMT:
                        endContext();
                        return ParserRuleContext.STATEMENT;
                    case IF_BLOCK:
                        endContext(); // end parent stmt if/lock/while/ block
                        return ParserRuleContext.ELSE_BLOCK;
                    case TRANSACTION_STMT:
                        endContext(); // end transaction context
                        parentCtx = getParentContext();

                        // If this is a retry-transaction block, then end the enclosing retry
                        // context as well.
                        if (parentCtx == ParserRuleContext.RETRY_STMT) {
                            endContext();
                        }
                        return ParserRuleContext.STATEMENT;
                    case NAMED_WORKER_DECL:
                        endContext(); // end named-worker
                        parentCtx = getParentContext();
                        if (parentCtx == ParserRuleContext.FORK_STMT) {
                            nextToken = this.tokenReader.peek(nextLookahead);
                            switch (nextToken.kind) {
                                case CLOSE_BRACE_TOKEN:
                                    return ParserRuleContext.CLOSE_BRACE;
                                default:
                                    return ParserRuleContext.STATEMENT;
                            }
                        } else {
                            return ParserRuleContext.STATEMENT;
                        }
                    default:
                        return ParserRuleContext.STATEMENT;
                }
            case MAPPING_CONSTRUCTOR:
                endContext(); // end mapping constructor
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.TABLE_CONSTRUCTOR) {
                    return ParserRuleContext.TABLE_ROW_END;
                }

                if (parentCtx == ParserRuleContext.ANNOTATIONS) {
                    return ParserRuleContext.ANNOTATION_END;
                }

                return getNextRuleForExpr();
            case STMT_START_BRACKETED_LIST:
                return ParserRuleContext.BRACKETED_LIST_MEMBER_END;
            case MAPPING_BINDING_PATTERN:
                endContext();
                return getNextRuleForBindingPattern();
            case FORK_STMT:
                endContext(); // end fork-statement
                return ParserRuleContext.STATEMENT;
            case INTERPOLATION:
                endContext();
                return ParserRuleContext.TEMPLATE_MEMBER;
            case MULTI_RECEIVE_WORKERS:
            case MULTI_WAIT_FIELDS:
            case SERVICE_CONSTRUCTOR_EXPRESSION:
            case DO_CLAUSE:
                endContext();
                return ParserRuleContext.EXPRESSION_RHS;
            case ENUM_MEMBER_LIST:
                endContext(); // end ENUM_MEMBER_LIST context
                endContext(); // end MODULE_ENUM_DECLARATION ctx
                return ParserRuleContext.TOP_LEVEL_NODE;
            default:
                throw new IllegalStateException("found close-brace in: " + parentCtx);
        }
    }

    private ParserRuleContext getNextRuleForAnnotationEnd(int nextLookahead) {
        ParserRuleContext parentCtx;
        STToken nextToken;
        nextToken = this.tokenReader.peek(nextLookahead);
        if (nextToken.kind == SyntaxKind.AT_TOKEN) {
            return ParserRuleContext.AT;
        }

        endContext(); // end annotations
        parentCtx = getParentContext();
        switch (parentCtx) {
            case COMP_UNIT:
                return ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_METADATA;
            case FUNC_DEF:
            case FUNC_TYPE_DESC:
            case FUNC_DEF_OR_FUNC_TYPE:
            case ANON_FUNC_EXPRESSION:
            case FUNC_TYPE_DESC_OR_ANON_FUNC:
                return ParserRuleContext.TYPE_DESC_IN_RETURN_TYPE_DESC;
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case RECORD_FIELD:
                return ParserRuleContext.RECORD_FIELD_WITHOUT_METADATA;
            case OBJECT_MEMBER:
                return ParserRuleContext.OBJECT_MEMBER_WITHOUT_METADATA;
            case SERVICE_DECL:
                return ParserRuleContext.RESOURCE_DEF;
            case FUNC_BODY_BLOCK:
                return ParserRuleContext.STATEMENT_WITHOUT_ANNOTS;
            case EXTERNAL_FUNC_BODY:
                return ParserRuleContext.EXTERNAL_KEYWORD;
            case TYPE_CAST:
                return ParserRuleContext.TYPE_CAST_PARAM_RHS;
            case ENUM_MEMBER_LIST:
                return ParserRuleContext.ENUM_MEMBER_NAME;
            default:
                if (isParameter(parentCtx)) {
                    return ParserRuleContext.REQUIRED_PARAM;
                }

                // everything else, treat as an annotation in an expression
                return ParserRuleContext.EXPRESSION;
        }
    }

    /**
     * Get the next parser context to visit after a variable/parameter name.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForVarName() {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.REQUIRED_PARAM || parentCtx == ParserRuleContext.PARAM_LIST) {
            return ParserRuleContext.REQUIRED_PARAM_NAME_RHS;
        } else if (parentCtx == ParserRuleContext.DEFAULTABLE_PARAM) {
            return ParserRuleContext.ASSIGN_OP;
        } else if (parentCtx == ParserRuleContext.REST_PARAM) {
            return ParserRuleContext.PARAM_END;
        } else if (parentCtx == ParserRuleContext.FOREACH_STMT) {
            return ParserRuleContext.IN_KEYWORD;
        } else if (parentCtx == ParserRuleContext.TYPED_BINDING_PATTERN) {
            return getNextRuleForBindingPattern();
        } else if (parentCtx == ParserRuleContext.CAPTURE_BINDING_PATTERN) {
            return getNextRuleForBindingPattern();
        } else if (parentCtx == ParserRuleContext.LIST_BINDING_PATTERN ||
                parentCtx == ParserRuleContext.STMT_START_BRACKETED_LIST_MEMBER) {
            return getNextRuleForBindingPattern();
        } else if (parentCtx == ParserRuleContext.REST_BINDING_PATTERN) {
            return getNextRuleForBindingPattern();
        } else if (parentCtx == ParserRuleContext.FIELD_BINDING_PATTERN) {
            return getNextRuleForBindingPattern();
        } else if (parentCtx == ParserRuleContext.MAPPING_BINDING_PATTERN) {
            return getNextRuleForBindingPattern();
        } else if (isStatement(parentCtx) || parentCtx == ParserRuleContext.LISTENER_DECL ||
                parentCtx == ParserRuleContext.CONSTANT_DECL) {
            return ParserRuleContext.VAR_DECL_STMT_RHS;
        } else if (parentCtx == ParserRuleContext.RECORD_FIELD) {
            return ParserRuleContext.FIELD_DESCRIPTOR_RHS;
        } else if (parentCtx == ParserRuleContext.ARG_LIST) {
            return ParserRuleContext.NAMED_OR_POSITIONAL_ARG_RHS;
        } else if (parentCtx == ParserRuleContext.OBJECT_MEMBER) {
            return ParserRuleContext.OBJECT_FIELD_RHS;
        } else if (parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
            return ParserRuleContext.CLOSE_BRACKET;
        } else if (parentCtx == ParserRuleContext.KEY_SPECIFIER) {
            return ParserRuleContext.TABLE_KEY_RHS;
        } else if (parentCtx == ParserRuleContext.LET_EXPR_LET_VAR_DECL ||
                parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
            return ParserRuleContext.ASSIGN_OP;
        } else if (parentCtx == ParserRuleContext.ANNOTATION_DECL) {
            return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
        } else if (parentCtx == ParserRuleContext.QUERY_EXPRESSION) {
            return ParserRuleContext.IN_KEYWORD;
        } else {
            throw new IllegalStateException(parentCtx.toString());
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#SEMICOLON}.
     *
     * @param nextLookahead Position of the next token to consider, relative to the position of the original error
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForSemicolon(int nextLookahead) {
        STToken nextToken;
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.EXTERNAL_FUNC_BODY) {
            endContext(); // end external func-body
            endContext(); // end func-def
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.QUERY_EXPRESSION) {
            endContext(); // end expression
            return getNextRuleForSemicolon(nextLookahead);
        } else if (isExpressionContext(parentCtx)) {
            // A semicolon after an expression also means its an end of a statement/field, Hence pop the ctx.
            endContext(); // end statement
            return ParserRuleContext.STATEMENT;
        } else if (parentCtx == ParserRuleContext.VAR_DECL_STMT) {
            endContext(); // end var-decl
            parentCtx = getParentContext();
            if (parentCtx == ParserRuleContext.COMP_UNIT) {
                return ParserRuleContext.TOP_LEVEL_NODE;
            }
            return ParserRuleContext.STATEMENT;
        } else if (isStatement(parentCtx)) {
            endContext(); // end statement
            return ParserRuleContext.STATEMENT;
        } else if (parentCtx == ParserRuleContext.RECORD_FIELD) {
            endContext(); // end record field
            return ParserRuleContext.RECORD_FIELD_OR_RECORD_END;
        } else if (parentCtx == ParserRuleContext.MODULE_TYPE_DEFINITION ||
                parentCtx == ParserRuleContext.LISTENER_DECL || parentCtx == ParserRuleContext.CONSTANT_DECL ||
                parentCtx == ParserRuleContext.ANNOTATION_DECL ||
                parentCtx == ParserRuleContext.XML_NAMESPACE_DECLARATION) {
            endContext(); // end declaration
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.OBJECT_MEMBER) {
            if (isEndOfObjectTypeNode(nextLookahead)) {
                endContext(); // end object member
                return ParserRuleContext.CLOSE_BRACE;
            }
            return ParserRuleContext.OBJECT_MEMBER_START;
        } else if (parentCtx == ParserRuleContext.IMPORT_DECL) {
            endContext(); // end object member
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.ANNOT_ATTACH_POINTS_LIST) {
            endContext(); // end annot attach points list
            endContext(); // end annot declaration
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.FUNC_DEF || parentCtx == ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE) {
            endContext(); // end func-def
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else {
            throw new IllegalStateException(parentCtx.toString());
        }
    }

    private ParserRuleContext getNextRuleForDot() {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.IMPORT_DECL) {
            return ParserRuleContext.IMPORT_MODULE_NAME;
        }
        return ParserRuleContext.FIELD_ACCESS_IDENTIFIER;
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#QUESTION_MARK}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForQuestionMark() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case OPTIONAL_TYPE_DESCRIPTOR:
                endContext();
                return ParserRuleContext.TYPEDESC_RHS;
            case CONDITIONAL_EXPRESSION:
                return ParserRuleContext.EXPRESSION;
            default:
                return ParserRuleContext.SEMICOLON;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#OPEN_BRACKET}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForOpenBracket() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case ARRAY_TYPE_DESCRIPTOR:
                return ParserRuleContext.ARRAY_LENGTH;
            case LIST_CONSTRUCTOR:
                return ParserRuleContext.LIST_CONSTRUCTOR_FIRST_MEMBER;
            case TABLE_CONSTRUCTOR:
                return ParserRuleContext.ROW_LIST_RHS;
            case LIST_BINDING_PATTERN:
                return ParserRuleContext.LIST_BINDING_PATTERN_CONTENTS;
            default:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_IN_TUPLE;
                }
                return ParserRuleContext.EXPRESSION;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#CLOSE_BRACKET}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForCloseBracket() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case ARRAY_TYPE_DESCRIPTOR:
            case TYPE_DESC_IN_TUPLE:
                endContext(); // End array/tuple type descriptor context
                return ParserRuleContext.TYPEDESC_RHS;
            case COMPUTED_FIELD_NAME:
                endContext(); // end computed-field-name
                return ParserRuleContext.COLON;
            case LIST_BINDING_PATTERN:
                endContext(); // end list-binding-pattern context
                return getNextRuleForBindingPattern();
            case LIST_CONSTRUCTOR:
            case TABLE_CONSTRUCTOR:
            case MEMBER_ACCESS_KEY_EXPR:
                endContext();
                return getNextRuleForExpr();
            case STMT_START_BRACKETED_LIST:
                endContext();
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.STMT_START_BRACKETED_LIST) {
                    return ParserRuleContext.BRACKETED_LIST_MEMBER_END;
                }

                return ParserRuleContext.STMT_START_BRACKETED_LIST_RHS;
            case BRACKETED_LIST:
                endContext();
                return ParserRuleContext.BRACKETED_LIST_RHS;
            default:
                return getNextRuleForExpr();
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#DECIMAL_INTEGER_LITERAL}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForDecimalIntegerLiteral() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case CONSTANT_EXPRESSION:
                endContext();
                return getNextRuleForConstExpr();
            case ARRAY_TYPE_DESCRIPTOR:
            default:
                return ParserRuleContext.CLOSE_BRACKET;
        }
    }

    private ParserRuleContext getNextRuleForExpr() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.CONSTANT_EXPRESSION) {
            endContext();
            return getNextRuleForConstExpr();
        }
        return ParserRuleContext.EXPRESSION_RHS;
    }

    private ParserRuleContext getNextRuleForConstExpr() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case XML_NAMESPACE_DECLARATION:
                return ParserRuleContext.XML_NAMESPACE_PREFIX_DECL;
            default:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                throw new IllegalStateException(parentCtx.toString());
        }
    }

    private ParserRuleContext getNextRuleForLt() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case TYPE_CAST:
                return ParserRuleContext.TYPE_CAST_PARAM;
            default:
                return ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS;
        }
    }

    private ParserRuleContext getNextRuleForGt(int nextLookahead) {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC) {
            // Since type-desc in a stream-type can have alternate endings,
            // we haven't end the context. So if its '>', then end the ctx here.
            endContext();
            return ParserRuleContext.TYPEDESC_RHS;
        }

        if (isInTypeDescContext()) {
            return ParserRuleContext.TYPEDESC_RHS;
        }

        if (parentCtx == ParserRuleContext.ROW_TYPE_PARAM) {
            endContext(); // end row type param ctx
            return ParserRuleContext.TABLE_TYPE_DESC_RHS;
        } else if (parentCtx == ParserRuleContext.RETRY_STMT) {
            return ParserRuleContext.RETRY_TYPE_PARAM_RHS;
        }

        if (parentCtx == ParserRuleContext.XML_NAME_PATTERN) {
            endContext();
            return ParserRuleContext.EXPRESSION_RHS;
        }

        // Type cast expression:
        endContext();
        return ParserRuleContext.EXPRESSION;
    }

    /**
     * Get the next parser context to visit after a binding-pattern.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForBindingPattern() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case CAPTURE_BINDING_PATTERN:
            case TYPED_BINDING_PATTERN:
                endContext();
                return getNextRuleForBindingPattern();
            case FOREACH_STMT:
            case QUERY_EXPRESSION:
                return ParserRuleContext.IN_KEYWORD;
            case LIST_BINDING_PATTERN:
            case STMT_START_BRACKETED_LIST:
            case BRACKETED_LIST:
                return ParserRuleContext.LIST_BINDING_PATTERN_END_OR_CONTINUE;
            case MAPPING_BINDING_PATTERN:
                return ParserRuleContext.MAPPING_BINDING_PATTERN_END;
            case REST_BINDING_PATTERN:
                endContext();
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LIST_BINDING_PATTERN) {
                    return ParserRuleContext.CLOSE_BRACKET;
                }
                return ParserRuleContext.CLOSE_BRACE; // for mapping binding pattern
            case AMBIGUOUS_STMT:
                switchContext(ParserRuleContext.VAR_DECL_STMT);
                return ParserRuleContext.VAR_DECL_STMT_RHS;
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case VAR_DECL_STMT:
                return ParserRuleContext.VAR_DECL_STMT_RHS;
            case LET_CLAUSE_LET_VAR_DECL:
            case LET_EXPR_LET_VAR_DECL:
            case ASSIGNMENT_STMT:
                return ParserRuleContext.ASSIGN_OP;
            default:
                throw new IllegalStateException(parentCtx.toString());
        }
    }

    private ParserRuleContext getNextRuleForWaitExprListEnd() {
        // TODO: add other endings based on the locations where action is allowed.
        endContext();
        return ParserRuleContext.EXPRESSION_RHS;
    }

    /**
     * Check whether the given context is a statement.
     *
     * @param ctx Parser context to check
     * @return <code>true</code> if the given context is a statement. <code>false</code> otherwise
     */
    private boolean isStatement(ParserRuleContext parentCtx) {
        switch (parentCtx) {
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case VAR_DECL_STMT:
            case ASSIGNMENT_STMT:
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case IF_BLOCK:
            case BLOCK_STMT:
            case WHILE_BLOCK:
            case CALL_STMT:
            case PANIC_STMT:
            case CONTINUE_STATEMENT:
            case BREAK_STATEMENT:
            case RETURN_STMT:
            case COMPOUND_ASSIGNMENT_STMT:
            case LOCAL_TYPE_DEFINITION_STMT:
            case EXPRESSION_STATEMENT:
            case LOCK_STMT:
            case FORK_STMT:
            case FOREACH_STMT:
            case TRANSACTION_STMT:
            case RETRY_STMT:
            case ROLLBACK_STMT:
            case AMBIGUOUS_STMT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the given token refers to a binary operator.
     *
     * @param token Token to check
     * @return <code>true</code> if the given token refers to a binary operator. <code>false</code> otherwise
     */
    private boolean isBinaryOperator(STToken token) {
        switch (token.kind) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case SLASH_TOKEN:
            case ASTERISK_TOKEN:
            case GT_TOKEN:
            case LT_TOKEN:
            case DOUBLE_EQUAL_TOKEN:
            case TRIPPLE_EQUAL_TOKEN:
            case LT_EQUAL_TOKEN:
            case GT_EQUAL_TOKEN:
            case NOT_EQUAL_TOKEN:
            case NOT_DOUBLE_EQUAL_TOKEN:
            case BITWISE_AND_TOKEN:
            case BITWISE_XOR_TOKEN:
            case PIPE_TOKEN:
            case LOGICAL_AND_TOKEN:
            case LOGICAL_OR_TOKEN:
            case DOUBLE_LT_TOKEN:
            case DOUBLE_GT_TOKEN:
            case TRIPPLE_GT_TOKEN:
            case ELLIPSIS_TOKEN:
            case DOUBLE_DOT_LT_TOKEN:
            case ELVIS_TOKEN:
                return true;

            // Treat these also as binary operators.
            case RIGHT_ARROW_TOKEN:
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isParameter(ParserRuleContext ctx) {
        switch (ctx) {
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
            case PARAM_LIST:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the expected token kind at the given parser rule context. If the parser rule is a terminal,
     * then the corresponding terminal token kind is returned. If the parser rule is a production,
     * then {@link SyntaxKind#NONE} is returned.
     *
     * @param ctx Parser rule context
     * @return Token kind expected at the given parser rule
     */
    @Override
    protected SyntaxKind getExpectedTokenKind(ParserRuleContext ctx) {
        switch (ctx) {
            case ASSIGN_OP:
                return SyntaxKind.EQUAL_TOKEN;
            case BINARY_OPERATOR:
                return SyntaxKind.PLUS_TOKEN;
            case CLOSE_BRACE:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case CLOSE_PARENTHESIS:
            case ARG_LIST_END:
                return SyntaxKind.CLOSE_PAREN_TOKEN;
            case COMMA:
                return SyntaxKind.COMMA_TOKEN;
            case EXTERNAL_KEYWORD:
                return SyntaxKind.EXTERNAL_KEYWORD;
            case FUNCTION_KEYWORD:
                return SyntaxKind.FUNCTION_KEYWORD;
            case FUNC_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case OPEN_BRACE:
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case OPEN_PARENTHESIS:
            case ARG_LIST_START:
                return SyntaxKind.OPEN_PAREN_TOKEN;
            case RETURNS_KEYWORD:
                return SyntaxKind.RETURNS_KEYWORD;
            case SEMICOLON:
                return SyntaxKind.SEMICOLON_TOKEN;
            case VARIABLE_NAME:
            case TYPE_NAME_OR_VAR_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case PUBLIC_KEYWORD:
                return SyntaxKind.PUBLIC_KEYWORD;
            case ASSIGNMENT_STMT:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case EXPRESSION_RHS:
                return SyntaxKind.PLUS_TOKEN;
            case EXPRESSION:
            case TERMINAL_EXPRESSION:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case EXTERNAL_FUNC_BODY:
                return SyntaxKind.EQUAL_TOKEN;
            case FUNC_BODY_OR_TYPE_DESC_RHS:
            case FUNC_BODY_BLOCK:
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case FUNC_DEF:
            case FUNC_DEF_OR_FUNC_TYPE:
            case FUNC_TYPE_DESC:
            case FUNC_TYPE_DESC_OR_ANON_FUNC:
                return SyntaxKind.FUNCTION_KEYWORD;
            case VAR_DECL_STMT_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case SIMPLE_TYPE_DESCRIPTOR:
            case REQUIRED_PARAM:
            case VAR_DECL_STMT:
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                return SyntaxKind.TYPE_DESC;
            case ASTERISK:
            case INFERRED_TYPE_DESC:
                return SyntaxKind.ASTERISK_TOKEN;
            case CLOSED_RECORD_BODY_END:
                return SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
            case CLOSED_RECORD_BODY_START:
                return SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
            case ELLIPSIS:
                return SyntaxKind.ELLIPSIS_TOKEN;
            case QUESTION_MARK:
                return SyntaxKind.QUESTION_MARK_TOKEN;
            case RECORD_BODY_START:
                return SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
            case RECORD_FIELD:
            case RECORD_KEYWORD:
                return SyntaxKind.RECORD_KEYWORD;
            case TYPE_KEYWORD:
                return SyntaxKind.TYPE_KEYWORD;
            case TYPE_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case TYPE_REFERENCE:
            case FIELD_ACCESS_IDENTIFIER:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case RECORD_BODY_END:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case OBJECT_KEYWORD:
                return SyntaxKind.OBJECT_KEYWORD;
            case PRIVATE_KEYWORD:
                return SyntaxKind.PRIVATE_KEYWORD;
            case REMOTE_KEYWORD:
                return SyntaxKind.REMOTE_KEYWORD;
            case OBJECT_FIELD_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case ABSTRACT_KEYWORD:
                return SyntaxKind.ABSTRACT_KEYWORD;
            case CLIENT_KEYWORD:
                return SyntaxKind.CLIENT_KEYWORD;
            case OBJECT_TYPE_FIRST_QUALIFIER:
            case OBJECT_TYPE_SECOND_QUALIFIER:
                return SyntaxKind.OBJECT_KEYWORD;
            case CLOSE_BRACKET:
            case MEMBER_ACCESS_KEY_EXPR_END:
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case DOT:
                return SyntaxKind.DOT_TOKEN;
            case OPEN_BRACKET:
            case TUPLE_TYPE_DESC_START:
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case IF_KEYWORD:
                return SyntaxKind.IF_KEYWORD;
            case ELSE_KEYWORD:
                return SyntaxKind.ELSE_KEYWORD;
            case WHILE_KEYWORD:
                return SyntaxKind.WHILE_KEYWORD;
            case CHECKING_KEYWORD:
                return SyntaxKind.CHECK_KEYWORD;
            case AS_KEYWORD:
                return SyntaxKind.AS_KEYWORD;
            case BOOLEAN_LITERAL:
                return SyntaxKind.TRUE_KEYWORD;
            case IMPORT_KEYWORD:
                return SyntaxKind.IMPORT_KEYWORD;
            case IMPORT_MODULE_NAME:
            case IMPORT_ORG_OR_MODULE_NAME:
            case IMPORT_PREFIX:
            case VARIABLE_REF:
            case BASIC_LITERAL: // return var-ref for any kind of terminal expression
            case SERVICE_NAME:
            case IDENTIFIER:
            case QUALIFIED_IDENTIFIER:
            case NAMESPACE_PREFIX:
            case IMPLICIT_ANON_FUNC_PARAM:
            case WORKER_NAME_OR_METHOD_NAME:
            case PEER_WORKER_NAME:
            case RECEIVE_FIELD_NAME:
            case WAIT_FIELD_NAME:
            case FIELD_BINDING_PATTERN_NAME:
            case XML_ATOMIC_NAME_IDENTIFIER:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case VERSION_NUMBER:
            case MAJOR_VERSION:
            case MINOR_VERSION:
            case PATCH_VERSION:
                return SyntaxKind.DECIMAL_INTEGER_LITERAL;
            case SLASH:
                return SyntaxKind.SLASH_TOKEN;
            case VERSION_KEYWORD:
                return SyntaxKind.VERSION_KEYWORD;
            case IMPORT_DECL_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case IMPORT_SUB_VERSION:
                return SyntaxKind.SEMICOLON_TOKEN;
            case COLON:
                return SyntaxKind.COLON_TOKEN;
            case MAPPING_FIELD_NAME:
            case MAPPING_FIELD:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case PANIC_KEYWORD:
                return SyntaxKind.PANIC_KEYWORD;
            case STRING_LITERAL:
                return SyntaxKind.STRING_LITERAL;
            case ON_KEYWORD:
                return SyntaxKind.ON_KEYWORD;
            case RESOURCE_KEYWORD:
                return SyntaxKind.RESOURCE_KEYWORD;
            case RETURN_KEYWORD:
                return SyntaxKind.RETURN_KEYWORD;
            case SERVICE_KEYWORD:
                return SyntaxKind.SERVICE_KEYWORD;
            case BREAK_KEYWORD:
                return SyntaxKind.BREAK_KEYWORD;
            case LISTENER_KEYWORD:
                return SyntaxKind.CONST_KEYWORD;
            case CONTINUE_KEYWORD:
                return SyntaxKind.CONTINUE_KEYWORD;
            case CONST_KEYWORD:
                return SyntaxKind.CONST_KEYWORD;
            case FINAL_KEYWORD:
                return SyntaxKind.FINAL_KEYWORD;
            case CONST_DECL_TYPE:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case NIL_TYPE_DESCRIPTOR:
                return SyntaxKind.NIL_TYPE_DESC;
            case TYPEOF_KEYWORD:
                return SyntaxKind.TYPEOF_KEYWORD;
            case OPTIONAL_TYPE_DESCRIPTOR:
                return SyntaxKind.OPTIONAL_TYPE_DESC;
            case UNARY_OPERATOR:
                return SyntaxKind.PLUS_TOKEN;
            case ARRAY_TYPE_DESCRIPTOR:
                return SyntaxKind.ARRAY_TYPE_DESC;
            case AT:
                return SyntaxKind.AT_TOKEN;
            case FIELD_DESCRIPTOR_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case AFTER_PARAMETER_TYPE:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case CONST_DECL_RHS:
                return SyntaxKind.EQUAL_TOKEN;
            case IS_KEYWORD:
                return SyntaxKind.IS_KEYWORD;
            case OBJECT_MEMBER_WITHOUT_METADATA:
            case RECORD_FIELD_WITHOUT_METADATA:
            case PARAMETER_WITHOUT_ANNOTS:
            case TYPE_DESCRIPTOR:
                return SyntaxKind.TYPE_DESC;
            case TYPEOF_EXPRESSION:
                return SyntaxKind.TYPEOF_KEYWORD;
            case RIGHT_ARROW:
                return SyntaxKind.RIGHT_ARROW_TOKEN;
            case STMT_START_WITH_EXPR_RHS:
                return SyntaxKind.EQUAL_TOKEN;
            case COMPOUND_BINARY_OPERATOR:
                return SyntaxKind.PLUS_TOKEN;
            case UNARY_EXPRESSION:
                return SyntaxKind.PLUS_TOKEN;
            case MAP_KEYWORD:
                return SyntaxKind.MAP_KEYWORD;
            case FUTURE_KEYWORD:
                return SyntaxKind.FUTURE_KEYWORD;
            case TYPEDESC_KEYWORD:
                return SyntaxKind.TYPEDESC_KEYWORD;
            case GT:
                return SyntaxKind.GT_TOKEN;
            case LT:
                return SyntaxKind.LT_TOKEN;
            case NULL_KEYWORD:
                return SyntaxKind.NULL_KEYWORD;
            case LOCK_KEYWORD:
                return SyntaxKind.LOCK_KEYWORD;
            case ANNOTATION_KEYWORD:
                return SyntaxKind.ANNOTATION_KEYWORD;
            case ANNOT_DECL_OPTIONAL_TYPE:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case ANNOT_DECL_RHS:
                return SyntaxKind.ON_KEYWORD;
            case ARRAY_LENGTH:
                return SyntaxKind.DECIMAL_INTEGER_LITERAL;
            case ATTACH_POINT_IDENT:
            case IDENT_AFTER_OBJECT_IDENT:
            case SINGLE_KEYWORD_ATTACH_POINT_IDENT:
                return SyntaxKind.TYPE_KEYWORD;
            case FIELD_IDENT:
                return SyntaxKind.FIELD_KEYWORD;
            case FUNCTION_IDENT:
                return SyntaxKind.FUNCTION_KEYWORD;
            case HEX_INTEGER_LITERAL:
                return SyntaxKind.HEX_INTEGER_LITERAL;
            case RECORD_FIELD_OR_RECORD_END:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case SOURCE_KEYWORD:
                return SyntaxKind.SOURCE_KEYWORD;
            case ATTACH_POINT_END:
                return SyntaxKind.SEMICOLON_TOKEN;
            case CONSTANT_EXPRESSION:
                return SyntaxKind.STRING_LITERAL;
            case CONSTANT_EXPRESSION_START:
            case OBJECT_IDENT:
                return SyntaxKind.OBJECT_KEYWORD;
            case RECORD_IDENT:
                return SyntaxKind.RECORD_KEYWORD;
            case RESOURCE_IDENT:
                return SyntaxKind.RESOURCE_KEYWORD;
            case XMLNS_KEYWORD:
            case XML_NAMESPACE_DECLARATION:
                return SyntaxKind.XMLNS_KEYWORD;
            case XML_NAMESPACE_PREFIX_DECL:
                return SyntaxKind.SEMICOLON_TOKEN;
            case NAMED_WORKER_DECL:
            case WORKER_KEYWORD:
                return SyntaxKind.WORKER_KEYWORD;
            case WORKER_NAME:
            case NAMED_WORKERS:
            case ANNOTATION_TAG:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case NIL_LITERAL:
                return SyntaxKind.OPEN_PAREN_TOKEN;
            case FORK_KEYWORD:
                return SyntaxKind.FORK_KEYWORD;
            case DECIMAL_FLOATING_POINT_LITERAL:
                return SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL;
            case HEX_FLOATING_POINT_LITERAL:
                return SyntaxKind.HEX_FLOATING_POINT_LITERAL;
            case PARAMETERIZED_TYPE:
                return SyntaxKind.MAP_KEYWORD;
            case TRAP_KEYWORD:
                return SyntaxKind.TRAP_KEYWORD;
            case FOREACH_KEYWORD:
                return SyntaxKind.FOREACH_KEYWORD;
            case IN_KEYWORD:
                return SyntaxKind.IN_KEYWORD;
            case PIPE:
            case UNION_OR_INTERSECTION_TOKEN:
                return SyntaxKind.PIPE_TOKEN;
            case TABLE_KEYWORD:
                return SyntaxKind.TABLE_KEYWORD;
            case KEY_KEYWORD:
                return SyntaxKind.KEY_KEYWORD;
            case ERROR_KEYWORD:
                return SyntaxKind.ERROR_KEYWORD;
            case STREAM_KEYWORD:
                return SyntaxKind.STREAM_KEYWORD;
            case LET_KEYWORD:
                return SyntaxKind.LET_KEYWORD;
            case TEMPLATE_END:
            case TEMPLATE_START:
                return SyntaxKind.BACKTICK_TOKEN;
            case LT_TOKEN:
                return SyntaxKind.LT_TOKEN;
            case GT_TOKEN:
                return SyntaxKind.GT_TOKEN;
            case INTERPOLATION_START_TOKEN:
                return SyntaxKind.INTERPOLATION_START_TOKEN;
            case XML_KEYWORD:
                return SyntaxKind.XML_KEYWORD;
            case XML_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case STRING_KEYWORD:
                return SyntaxKind.STRING_KEYWORD;
            case BASE16_KEYWORD:
                return SyntaxKind.BASE16_KEYWORD;
            case BASE64_KEYWORD:
                return SyntaxKind.BASE64_KEYWORD;
            case SELECT_KEYWORD:
                return SyntaxKind.SELECT_KEYWORD;
            case WHERE_KEYWORD:
                return SyntaxKind.WHERE_KEYWORD;
            case FROM_KEYWORD:
                return SyntaxKind.FROM_KEYWORD;
            case EXPR_FUNC_BODY_START:
                return SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN;
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case START_KEYWORD:
                return SyntaxKind.START_KEYWORD;
            case FLUSH_KEYWORD:
                return SyntaxKind.FLUSH_KEYWORD;
            case DEFAULT_KEYWORD:
            case OPTIONAL_PEER_WORKER:
            case DEFAULT_WORKER_NAME_IN_ASYNC_SEND:
                return SyntaxKind.DEFAULT_KEYWORD;
            case DECIMAL_INTEGER_LITERAL:
            case SIGNED_INT_OR_FLOAT_RHS:
                return SyntaxKind.DECIMAL_INTEGER_LITERAL;
            case SYNC_SEND_TOKEN:
                return SyntaxKind.SYNC_SEND_TOKEN;
            case WAIT_KEYWORD:
                return SyntaxKind.WAIT_KEYWORD;
            case ANNOT_CHAINING_TOKEN:
                return SyntaxKind.ANNOT_CHAINING_TOKEN;
            case OPTIONAL_CHAINING_TOKEN:
                return SyntaxKind.OPTIONAL_CHAINING_TOKEN;
            case TRANSACTION_KEYWORD:
                return SyntaxKind.TRANSACTION_KEYWORD;
            case COMMIT_KEYWORD:
                return SyntaxKind.COMMIT_KEYWORD;
            case RETRY_KEYWORD:
                return SyntaxKind.RETRY_KEYWORD;
            case ROLLBACK_KEYWORD:
                return SyntaxKind.ROLLBACK_KEYWORD;
            case ENUM_KEYWORD:
                return SyntaxKind.ENUM_KEYWORD;
            case MODULE_ENUM_NAME:
            case ENUM_MEMBER_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case ENUM_MEMBER_RHS:
            case ENUM_MEMBER_END:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case TYPED_BINDING_PATTERN_TYPE_RHS:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case DOT_LT_TOKEN:
                return SyntaxKind.DOT_LT_TOKEN;
            case SLASH_LT_TOKEN:
                return SyntaxKind.SLASH_LT_TOKEN;
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
                return SyntaxKind.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN;
            case SLASH_ASTERISK_TOKEN:
                return SyntaxKind.SLASH_ASTERISK_TOKEN;
            default:
                break;
        }

        return SyntaxKind.NONE;
    }

    /**
     * Check whether a token kind is a basic literal.
     *
     * @param kind Token kind to check
     * @return <code>true</code> if the given token kind belongs to a basic literal.<code>false</code> otherwise
     */
    private boolean isBasicLiteral(SyntaxKind kind) {
        switch (kind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the given token refers to a unary operator.
     *
     * @param token Token to check
     * @return <code>true</code> if the given token refers to a unary operator. <code>false</code> otherwise
     */
    private boolean isUnaryOperator(STToken token) {
        switch (token.kind) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case NEGATION_TOKEN:
            case EXCLAMATION_MARK_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isSingleKeywordAttachPointIdent(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case ANNOTATION_KEYWORD:
            case EXTERNAL_KEYWORD:
            case VAR_KEYWORD:
            case CONST_KEYWORD:
            case LISTENER_KEYWORD:
            case WORKER_KEYWORD:
            case TYPE_KEYWORD:
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case FIELD_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the given token is a parameterized type keyword.
     *
     * @param tokenKind Token to check
     * @return <code>true</code> if the given token is a parameterized type keyword. <code>false</code> otherwise
     */
    public boolean isParameterizedTypeToken(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case MAP_KEYWORD:
            case FUTURE_KEYWORD:
            case TYPEDESC_KEYWORD:
                return true;
            default:
                return false;
        }
    }
}
