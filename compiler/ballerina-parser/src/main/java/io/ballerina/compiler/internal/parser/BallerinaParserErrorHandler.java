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
package io.ballerina.compiler.internal.parser;

import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

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

    private static final ParserRuleContext[] METHOD_DECL_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] FUNC_BODY =
            { ParserRuleContext.FUNC_BODY_BLOCK, ParserRuleContext.EXTERNAL_FUNC_BODY };

    private static final ParserRuleContext[] EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS =
            { ParserRuleContext.ANNOTATIONS, ParserRuleContext.EXTERNAL_KEYWORD };

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
            ParserRuleContext.MATCH_STMT, ParserRuleContext.EXPRESSION_STATEMENT, ParserRuleContext.LOCK_STMT,
            ParserRuleContext.NAMED_WORKER_DECL, ParserRuleContext.FORK_STMT, ParserRuleContext.FOREACH_STMT,
            ParserRuleContext.XML_NAMESPACE_DECLARATION, ParserRuleContext.TRANSACTION_STMT,
            ParserRuleContext.RETRY_STMT, ParserRuleContext.ROLLBACK_STMT, ParserRuleContext.DO_BLOCK,
            ParserRuleContext.FAIL_STATEMENT, ParserRuleContext.BLOCK_STMT, ParserRuleContext.CLIENT_DECLARATION };

    private static final ParserRuleContext[] ASSIGNMENT_STMT_RHS =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.COMPOUND_BINARY_OPERATOR };

    private static final ParserRuleContext[] VAR_DECL_RHS =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] TOP_LEVEL_NODE = { ParserRuleContext.EOF,
            ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_METADATA, ParserRuleContext.DOC_STRING,
            ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] TOP_LEVEL_NODE_WITHOUT_METADATA = { ParserRuleContext.EOF,
            ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_MODIFIER, ParserRuleContext.PUBLIC_KEYWORD };

    private static final ParserRuleContext[] TOP_LEVEL_NODE_WITHOUT_MODIFIER =
            { ParserRuleContext.EOF, ParserRuleContext.FUNC_DEF, ParserRuleContext.MODULE_VAR_DECL,
                    ParserRuleContext.MODULE_CLASS_DEFINITION, ParserRuleContext.SERVICE_DECL,
                    ParserRuleContext.LISTENER_DECL, ParserRuleContext.MODULE_TYPE_DEFINITION,
                    ParserRuleContext.CONSTANT_DECL, ParserRuleContext.ANNOTATION_DECL,
                    ParserRuleContext.XML_NAMESPACE_DECLARATION, ParserRuleContext.MODULE_ENUM_DECLARATION,
                    ParserRuleContext.IMPORT_DECL };

    private static final ParserRuleContext[] FUNC_DEF_START =
            { ParserRuleContext.FUNCTION_KEYWORD, ParserRuleContext.FUNC_DEF_FIRST_QUALIFIER };

    private static final ParserRuleContext[] FUNC_DEF_WITHOUT_FIRST_QUALIFIER =
            { ParserRuleContext.FUNCTION_KEYWORD, ParserRuleContext.FUNC_DEF_SECOND_QUALIFIER };

    private static final ParserRuleContext[] TYPE_OR_VAR_NAME =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN };

    private static final ParserRuleContext[] FIELD_DESCRIPTOR_RHS =
            { ParserRuleContext.SEMICOLON, ParserRuleContext.QUESTION_MARK, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] FIELD_OR_REST_DESCIPTOR_RHS =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.ELLIPSIS };

    private static final ParserRuleContext[] RECORD_BODY_START =
            { ParserRuleContext.CLOSED_RECORD_BODY_START, ParserRuleContext.OPEN_BRACE };

    private static final ParserRuleContext[] RECORD_BODY_END =
            { ParserRuleContext.CLOSED_RECORD_BODY_END, ParserRuleContext.CLOSE_BRACE };

    // Give object the higher priority over records, since record body is a subset of object body.
    // Array, optional and union type descriptors are not added to the list since they are left recursive.
    private static final ParserRuleContext[] TYPE_DESCRIPTORS = {
            // SIMPLE_TYPE_DESC_IDENTIFIER is already included in TYPE_REFERENCE.
            // But added separately, in order to give "ERROR_MISSING_TYPE_DESC" diagnostic.
            ParserRuleContext.SIMPLE_TYPE_DESC_IDENTIFIER, ParserRuleContext.TYPE_REFERENCE,
            ParserRuleContext.SIMPLE_TYPE_DESCRIPTOR, ParserRuleContext.OBJECT_TYPE_DESCRIPTOR,
            ParserRuleContext.RECORD_TYPE_DESCRIPTOR, ParserRuleContext.MAP_TYPE_DESCRIPTOR,
            ParserRuleContext.PARAMETERIZED_TYPE, ParserRuleContext.TUPLE_TYPE_DESC_START,
            ParserRuleContext.STREAM_KEYWORD, ParserRuleContext.TABLE_KEYWORD, ParserRuleContext.FUNC_TYPE_DESC,
            ParserRuleContext.CONSTANT_EXPRESSION, ParserRuleContext.PARENTHESISED_TYPE_DESC_START };

    private static final ParserRuleContext[] TYPE_DESCRIPTOR_WITHOUT_ISOLATED =
            { ParserRuleContext.FUNC_TYPE_DESC, ParserRuleContext.OBJECT_TYPE_DESCRIPTOR };
    
    private static final ParserRuleContext[] CLASS_DESCRIPTOR =
            { ParserRuleContext.TYPE_REFERENCE, ParserRuleContext.STREAM_KEYWORD };

    private static final ParserRuleContext[] RECORD_FIELD_OR_RECORD_END =
            { ParserRuleContext.RECORD_BODY_END, ParserRuleContext.RECORD_FIELD };

    private static final ParserRuleContext[] RECORD_FIELD_START =
            { ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD, ParserRuleContext.ASTERISK, ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] RECORD_FIELD_WITHOUT_METADATA =
            { ParserRuleContext.ASTERISK, ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD };

    private static final ParserRuleContext[] ARG_START_OR_ARG_LIST_END =
            { ParserRuleContext.ARG_LIST_END, ParserRuleContext.ARG_START };

    private static final ParserRuleContext[] ARG_START =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.ELLIPSIS, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] ARG_END =
            { ParserRuleContext.ARG_LIST_END, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] NAMED_OR_POSITIONAL_ARG_RHS =
            { ParserRuleContext.ARG_END, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] OPTIONAL_FIELD_INITIALIZER =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] ON_FAIL_OPTIONAL_BINDING_PATTERN =
            { ParserRuleContext.BLOCK_STMT, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN };

    private static final ParserRuleContext[] CLASS_MEMBER_OR_OBJECT_MEMBER_START =
            { ParserRuleContext.ASTERISK, ParserRuleContext.OBJECT_FUNC_OR_FIELD, ParserRuleContext.CLOSE_BRACE,
                    ParserRuleContext.DOC_STRING, ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] OBJECT_CONSTRUCTOR_MEMBER_START =
            { ParserRuleContext.OBJECT_FUNC_OR_FIELD, ParserRuleContext.CLOSE_BRACE, ParserRuleContext.DOC_STRING,
                    ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] CLASS_MEMBER_OR_OBJECT_MEMBER_WITHOUT_META =
            { ParserRuleContext.OBJECT_FUNC_OR_FIELD, ParserRuleContext.ASTERISK, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] OBJECT_CONS_MEMBER_WITHOUT_META =
            { ParserRuleContext.OBJECT_FUNC_OR_FIELD, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] OBJECT_FUNC_OR_FIELD =
            { ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY,
                    ParserRuleContext.OBJECT_MEMBER_VISIBILITY_QUAL };

    private static final ParserRuleContext[] OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY =
            { ParserRuleContext.OBJECT_FIELD_START, ParserRuleContext.OBJECT_METHOD_START };

    private static final ParserRuleContext[] OBJECT_FIELD_QUALIFIER =
            { ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER, ParserRuleContext.FINAL_KEYWORD };

    private static final ParserRuleContext[] OBJECT_METHOD_START =
            { ParserRuleContext.FUNC_DEF, ParserRuleContext.OBJECT_METHOD_FIRST_QUALIFIER };

    private static final ParserRuleContext[] OBJECT_METHOD_WITHOUT_FIRST_QUALIFIER =
            { ParserRuleContext.FUNC_DEF, ParserRuleContext.OBJECT_METHOD_SECOND_QUALIFIER };

    private static final ParserRuleContext[] OBJECT_METHOD_WITHOUT_SECOND_QUALIFIER =
            { ParserRuleContext.FUNC_DEF, ParserRuleContext.OBJECT_METHOD_THIRD_QUALIFIER };

    private static final ParserRuleContext[] OBJECT_METHOD_WITHOUT_THIRD_QUALIFIER =
            { ParserRuleContext.FUNC_DEF, ParserRuleContext.OBJECT_METHOD_FOURTH_QUALIFIER };

    private static final ParserRuleContext[] OBJECT_TYPE_START =
            { ParserRuleContext.OBJECT_KEYWORD, ParserRuleContext.FIRST_OBJECT_TYPE_QUALIFIER };

    private static final ParserRuleContext[] OBJECT_TYPE_WITHOUT_FIRST_QUALIFIER =
            { ParserRuleContext.OBJECT_KEYWORD, ParserRuleContext.SECOND_OBJECT_TYPE_QUALIFIER };

    private static final ParserRuleContext[] OBJECT_CONSTRUCTOR_START =
            { ParserRuleContext.OBJECT_KEYWORD, ParserRuleContext.FIRST_OBJECT_CONS_QUALIFIER };

    private static final ParserRuleContext[] OBJECT_CONS_WITHOUT_FIRST_QUALIFIER =
            { ParserRuleContext.OBJECT_KEYWORD, ParserRuleContext.SECOND_OBJECT_CONS_QUALIFIER };

    private static final ParserRuleContext[] OBJECT_CONSTRUCTOR_RHS =
            { ParserRuleContext.OPEN_BRACE, ParserRuleContext.TYPE_REFERENCE };

    private static final ParserRuleContext[] ELSE_BODY = { ParserRuleContext.IF_BLOCK, ParserRuleContext.OPEN_BRACE };

    private static final ParserRuleContext[] ELSE_BLOCK =
            { ParserRuleContext.ELSE_KEYWORD, ParserRuleContext.STATEMENT };

    private static final ParserRuleContext[] CALL_STATEMENT =
            { ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.VARIABLE_REF, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] IMPORT_PREFIX_DECL =
            { ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] IMPORT_VERSION =
            { ParserRuleContext.VERSION_KEYWORD, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] IMPORT_DECL_ORG_OR_MODULE_NAME_RHS =
            { ParserRuleContext.SLASH, ParserRuleContext.AFTER_IMPORT_MODULE_NAME };

    private static final ParserRuleContext[] AFTER_IMPORT_MODULE_NAME = {
            ParserRuleContext.AS_KEYWORD, ParserRuleContext.DOT, ParserRuleContext.VERSION_KEYWORD,
            ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] MAJOR_MINOR_VERSION_END =
            { ParserRuleContext.DOT, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] RETURN_RHS = { ParserRuleContext.SEMICOLON, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] EXPRESSION_START = { ParserRuleContext.BASIC_LITERAL,
            ParserRuleContext.NIL_LITERAL, ParserRuleContext.VARIABLE_REF, ParserRuleContext.ACCESS_EXPRESSION,
            ParserRuleContext.TYPE_CAST, ParserRuleContext.BRACED_EXPRESSION,
            ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION, ParserRuleContext.LIST_CONSTRUCTOR,
            ParserRuleContext.LET_EXPRESSION, ParserRuleContext.TEMPLATE_START, ParserRuleContext.XML_KEYWORD,
            ParserRuleContext.STRING_KEYWORD, ParserRuleContext.BASE16_KEYWORD, ParserRuleContext.BASE64_KEYWORD,
            ParserRuleContext.ANON_FUNC_EXPRESSION, ParserRuleContext.ERROR_KEYWORD, ParserRuleContext.NEW_KEYWORD,
            ParserRuleContext.START_KEYWORD, ParserRuleContext.FLUSH_KEYWORD, ParserRuleContext.LEFT_ARROW_TOKEN,
            ParserRuleContext.WAIT_KEYWORD, ParserRuleContext.COMMIT_KEYWORD, ParserRuleContext.OBJECT_CONSTRUCTOR,
            ParserRuleContext.ERROR_CONSTRUCTOR, ParserRuleContext.TRANSACTIONAL_KEYWORD,
            ParserRuleContext.TYPEOF_EXPRESSION, ParserRuleContext.TRAP_KEYWORD, ParserRuleContext.UNARY_EXPRESSION,
            ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.MAPPING_CONSTRUCTOR, ParserRuleContext.RE_KEYWORD };

    private static final ParserRuleContext[] FIRST_MAPPING_FIELD_START =
            { ParserRuleContext.MAPPING_FIELD, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] MAPPING_FIELD_START = { ParserRuleContext.SPECIFIC_FIELD,
            ParserRuleContext.ELLIPSIS, ParserRuleContext.COMPUTED_FIELD_NAME, ParserRuleContext.READONLY_KEYWORD };

    private static final ParserRuleContext[] SPECIFIC_FIELD =
            { ParserRuleContext.MAPPING_FIELD_NAME, ParserRuleContext.STRING_LITERAL_TOKEN };

    private static final ParserRuleContext[] SPECIFIC_FIELD_RHS =
            { ParserRuleContext.COLON, ParserRuleContext.MAPPING_FIELD_END };

    private static final ParserRuleContext[] MAPPING_FIELD_END =
            { ParserRuleContext.CLOSE_BRACE, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] CONST_DECL_RHS =
            { ParserRuleContext.TYPE_NAME_OR_VAR_NAME, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] ARRAY_LENGTH =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.DECIMAL_INTEGER_LITERAL_TOKEN,
                    ParserRuleContext.HEX_INTEGER_LITERAL_TOKEN, ParserRuleContext.ASTERISK,
                    ParserRuleContext.VARIABLE_REF };

    private static final ParserRuleContext[] PARAM_LIST =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.REQUIRED_PARAM };

    private static final ParserRuleContext[] PARAMETER_START =
            { ParserRuleContext.PARAMETER_START_WITHOUT_ANNOTATION, ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] PARAMETER_START_WITHOUT_ANNOTATION =
            { ParserRuleContext.TYPE_DESC_IN_PARAM, ParserRuleContext.ASTERISK };

    private static final ParserRuleContext[] REQUIRED_PARAM_NAME_RHS =
            { ParserRuleContext.PARAM_END, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] PARAM_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] STMT_START_WITH_EXPR_RHS = { ParserRuleContext.SEMICOLON, 
            ParserRuleContext.ASSIGN_OP, ParserRuleContext.RIGHT_ARROW, ParserRuleContext.COMPOUND_BINARY_OPERATOR };

    private static final ParserRuleContext[] EXPR_STMT_RHS = { ParserRuleContext.SEMICOLON, ParserRuleContext.ASSIGN_OP,
            ParserRuleContext.RIGHT_ARROW, ParserRuleContext.COMPOUND_BINARY_OPERATOR };

    private static final ParserRuleContext[] EXPRESSION_STATEMENT_START =
            { ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.OPEN_PARENTHESIS,
                    ParserRuleContext.START_KEYWORD, ParserRuleContext.FLUSH_KEYWORD };

    private static final ParserRuleContext[] ANNOT_DECL_OPTIONAL_TYPE =
            { ParserRuleContext.ANNOTATION_TAG, ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER };

    private static final ParserRuleContext[] CONST_DECL_TYPE =
            { ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] ANNOT_DECL_RHS =
            { ParserRuleContext.ANNOTATION_TAG, ParserRuleContext.ON_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] ANNOT_OPTIONAL_ATTACH_POINTS =
            { ParserRuleContext.ON_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] ATTACH_POINT =
            { ParserRuleContext.SOURCE_KEYWORD, ParserRuleContext.ATTACH_POINT_IDENT };

    private static final ParserRuleContext[] ATTACH_POINT_IDENT = { ParserRuleContext.SINGLE_KEYWORD_ATTACH_POINT_IDENT,
            ParserRuleContext.OBJECT_IDENT, ParserRuleContext.SERVICE_IDENT, ParserRuleContext.RECORD_IDENT };

    private static final ParserRuleContext[] SERVICE_IDENT_RHS =
            { ParserRuleContext.REMOTE_IDENT, ParserRuleContext.ATTACH_POINT_END };

    private static final ParserRuleContext[] ATTACH_POINT_END =
            { ParserRuleContext.COMMA, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] XML_NAMESPACE_PREFIX_DECL =
            { ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] CONSTANT_EXPRESSION =
            { ParserRuleContext.BASIC_LITERAL, ParserRuleContext.VARIABLE_REF, ParserRuleContext.PLUS_TOKEN,
                    ParserRuleContext.MINUS_TOKEN, ParserRuleContext.NIL_LITERAL };

    private static final ParserRuleContext[] LIST_CONSTRUCTOR_FIRST_MEMBER =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.LIST_CONSTRUCTOR_MEMBER };

    private static final ParserRuleContext[] LIST_CONSTRUCTOR_MEMBER =
            { ParserRuleContext.EXPRESSION, ParserRuleContext.ELLIPSIS };

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
            { ParserRuleContext.FUNC_NAME, ParserRuleContext.FUNC_TYPE_FUNC_KEYWORD_RHS };

    private static final ParserRuleContext[] FUNC_TYPE_FUNC_KEYWORD_RHS_START =
            { ParserRuleContext.FUNC_TYPE_DESC_END, ParserRuleContext.OPEN_PARENTHESIS };

    private static final ParserRuleContext[] TYPE_DESC_RHS = { ParserRuleContext.END_OF_TYPE_DESC,
            ParserRuleContext.ARRAY_TYPE_DESCRIPTOR, ParserRuleContext.OPTIONAL_TYPE_DESCRIPTOR, ParserRuleContext.PIPE,
            ParserRuleContext.BITWISE_AND_OPERATOR };

    private static final ParserRuleContext[] TABLE_TYPE_DESC_RHS =
            { ParserRuleContext.KEY_KEYWORD, ParserRuleContext.TYPE_DESC_RHS };

    private static final ParserRuleContext[] NEW_KEYWORD_RHS =
            { ParserRuleContext.ARG_LIST_OPEN_PAREN, ParserRuleContext.CLASS_DESCRIPTOR_IN_NEW_EXPR,
                    ParserRuleContext.EXPRESSION_RHS };

    private static final ParserRuleContext[] TABLE_CONSTRUCTOR_OR_QUERY_START =
            { ParserRuleContext.TABLE_KEYWORD, ParserRuleContext.STREAM_KEYWORD, ParserRuleContext.QUERY_EXPRESSION,
                    ParserRuleContext.MAP_KEYWORD };

    private static final ParserRuleContext[] TABLE_CONSTRUCTOR_OR_QUERY_RHS =
            { ParserRuleContext.TABLE_CONSTRUCTOR, ParserRuleContext.QUERY_EXPRESSION };

    private static final ParserRuleContext[] QUERY_PIPELINE_RHS =
            { ParserRuleContext.QUERY_EXPRESSION_RHS, ParserRuleContext.INTERMEDIATE_CLAUSE,
                    ParserRuleContext.QUERY_ACTION_RHS };

    private static final ParserRuleContext[] INTERMEDIATE_CLAUSE_START =
            { ParserRuleContext.WHERE_CLAUSE, ParserRuleContext.FROM_CLAUSE, ParserRuleContext.LET_CLAUSE,
            ParserRuleContext.JOIN_CLAUSE, ParserRuleContext.ORDER_BY_CLAUSE, ParserRuleContext.LIMIT_CLAUSE };

    private static final ParserRuleContext[] BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] ANNOTATION_REF_RHS =
            { ParserRuleContext.ANNOTATION_END, ParserRuleContext.MAPPING_CONSTRUCTOR };

    private static final ParserRuleContext[] INFER_PARAM_END_OR_PARENTHESIS_END =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.EXPR_FUNC_BODY_START };

    private static final ParserRuleContext[] OPTIONAL_PEER_WORKER =
            { ParserRuleContext.PEER_WORKER_NAME, ParserRuleContext.EXPRESSION_RHS };

    private static final ParserRuleContext[] TYPE_DESC_IN_TUPLE_RHS =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.COMMA, ParserRuleContext.ELLIPSIS };

    private static final ParserRuleContext[] TUPLE_TYPE_MEMBER_RHS =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] LIST_CONSTRUCTOR_MEMBER_END =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] NIL_OR_PARENTHESISED_TYPE_DESC_RHS =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.TYPE_DESCRIPTOR };

    private static final ParserRuleContext[] BINDING_PATTERN =
            { ParserRuleContext.BINDING_PATTERN_STARTING_IDENTIFIER, ParserRuleContext.MAPPING_BINDING_PATTERN,
                    ParserRuleContext.LIST_BINDING_PATTERN, ParserRuleContext.ERROR_BINDING_PATTERN };

    private static final ParserRuleContext[] LIST_BINDING_PATTERNS_START =
            { ParserRuleContext.LIST_BINDING_PATTERN_MEMBER, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] LIST_BINDING_PATTERN_CONTENTS =
            { ParserRuleContext.BINDING_PATTERN, ParserRuleContext.REST_BINDING_PATTERN };

    private static final ParserRuleContext[] LIST_BINDING_PATTERN_MEMBER_END =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] MAPPING_BINDING_PATTERN_MEMBER =
            { ParserRuleContext.REST_BINDING_PATTERN, ParserRuleContext.FIELD_BINDING_PATTERN };

    private static final ParserRuleContext[] MAPPING_BINDING_PATTERN_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] FIELD_BINDING_PATTERN_END =
            { ParserRuleContext.COMMA, ParserRuleContext.COLON, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] ERROR_BINDING_PATTERN_ERROR_KEYWORD_RHS =
            { ParserRuleContext.OPEN_PARENTHESIS, ParserRuleContext.TYPE_REFERENCE };

    private static final ParserRuleContext[] ERROR_ARG_LIST_BINDING_PATTERN_START =
            { ParserRuleContext.SIMPLE_BINDING_PATTERN, ParserRuleContext.ERROR_FIELD_BINDING_PATTERN,
              ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] ERROR_MESSAGE_BINDING_PATTERN_END =
            { ParserRuleContext.ERROR_MESSAGE_BINDING_PATTERN_END_COMMA, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] ERROR_MESSAGE_BINDING_PATTERN_RHS =
            { ParserRuleContext.ERROR_CAUSE_SIMPLE_BINDING_PATTERN, ParserRuleContext.ERROR_BINDING_PATTERN,
              ParserRuleContext.ERROR_FIELD_BINDING_PATTERN };

    private static final ParserRuleContext[] ERROR_FIELD_BINDING_PATTERN =
            {  ParserRuleContext.NAMED_ARG_BINDING_PATTERN, ParserRuleContext.REST_BINDING_PATTERN };

    private static final ParserRuleContext[] ERROR_FIELD_BINDING_PATTERN_END =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] REMOTE_OR_RESOURCE_CALL_OR_ASYNC_SEND_RHS =
            { ParserRuleContext.DEFAULT_WORKER_NAME_IN_ASYNC_SEND, 
                    ParserRuleContext.RESOURCE_METHOD_CALL_SLASH_TOKEN, 
                    ParserRuleContext.PEER_WORKER_NAME, ParserRuleContext.METHOD_NAME };

    private static final ParserRuleContext[] REMOTE_CALL_OR_ASYNC_SEND_END =
            { ParserRuleContext.ARG_LIST_OPEN_PAREN, ParserRuleContext.SEMICOLON };

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
            { ParserRuleContext.ENUM_MEMBER_NAME, ParserRuleContext.DOC_STRING, ParserRuleContext.ANNOTATIONS };

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
            { ParserRuleContext.ARG_LIST_OPEN_PAREN, ParserRuleContext.RETRY_BODY };

    private static final ParserRuleContext[] RETRY_BODY =
            { ParserRuleContext.BLOCK_STMT, ParserRuleContext.TRANSACTION_STMT };

    private static final ParserRuleContext[] LIST_BP_OR_TUPLE_TYPE_MEMBER =
            { ParserRuleContext.TYPE_DESCRIPTOR, ParserRuleContext.LIST_BINDING_PATTERN_MEMBER };

    private static final ParserRuleContext[] LIST_BP_OR_TUPLE_TYPE_DESC_RHS =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] BRACKETED_LIST_MEMBER_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] BRACKETED_LIST_MEMBER =
            // array length is also an expression
            { ParserRuleContext.EXPRESSION, ParserRuleContext.BINDING_PATTERN };

    private static final ParserRuleContext[] LIST_BINDING_MEMBER_OR_ARRAY_LENGTH =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.BINDING_PATTERN, 
                    ParserRuleContext.ARRAY_LENGTH_START };

    private static final ParserRuleContext[] BRACKETED_LIST_RHS =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.TYPE_DESC_RHS_OR_BP_RHS,
                    ParserRuleContext.EXPRESSION_RHS };

    private static final ParserRuleContext[] BINDING_PATTERN_OR_VAR_REF_RHS =
            { ParserRuleContext.VARIABLE_REF_RHS, ParserRuleContext.ASSIGN_OP,
                    ParserRuleContext.TYPE_DESC_RHS_OR_BP_RHS };

    private static final ParserRuleContext[] TYPE_DESC_RHS_OR_BP_RHS =
            { ParserRuleContext.TYPE_DESC_RHS_IN_TYPED_BP, ParserRuleContext.LIST_BINDING_PATTERN_RHS };

    private static final ParserRuleContext[] XML_NAVIGATE_EXPR =
            { ParserRuleContext.XML_FILTER_EXPR, ParserRuleContext.XML_STEP_EXPR };

    private static final ParserRuleContext[] XML_NAME_PATTERN_RHS = { ParserRuleContext.GT, ParserRuleContext.PIPE };

    private static final ParserRuleContext[] XML_ATOMIC_NAME_PATTERN_START =
            { ParserRuleContext.ASTERISK, ParserRuleContext.XML_ATOMIC_NAME_IDENTIFIER };

    private static final ParserRuleContext[] XML_ATOMIC_NAME_IDENTIFIER_RHS =
            { ParserRuleContext.ASTERISK, ParserRuleContext.IDENTIFIER };

    private static final ParserRuleContext[] XML_STEP_START = { ParserRuleContext.SLASH_ASTERISK_TOKEN,
            ParserRuleContext.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN, ParserRuleContext.SLASH_LT_TOKEN };

    private static final ParserRuleContext[] MATCH_PATTERN_LIST_MEMBER_RHS =
            { ParserRuleContext.MATCH_PATTERN_END, ParserRuleContext.PIPE };

    private static final ParserRuleContext[] OPTIONAL_MATCH_GUARD =
            { ParserRuleContext.RIGHT_DOUBLE_ARROW, ParserRuleContext.IF_KEYWORD };

    private static final ParserRuleContext[] MATCH_PATTERN_START = { ParserRuleContext.CONSTANT_EXPRESSION,
            ParserRuleContext.VAR_KEYWORD, ParserRuleContext.MAPPING_MATCH_PATTERN,
            ParserRuleContext.LIST_MATCH_PATTERN, ParserRuleContext.ERROR_MATCH_PATTERN };

    private static final ParserRuleContext[] LIST_MATCH_PATTERNS_START =
            { ParserRuleContext.LIST_MATCH_PATTERN_MEMBER, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] LIST_MATCH_PATTERN_MEMBER =
            { ParserRuleContext.MATCH_PATTERN_START, ParserRuleContext.REST_MATCH_PATTERN };

    private static final ParserRuleContext[] LIST_MATCH_PATTERN_MEMBER_RHS =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] FIELD_MATCH_PATTERNS_START =
            { ParserRuleContext.FIELD_MATCH_PATTERN_MEMBER, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] FIELD_MATCH_PATTERN_MEMBER =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.REST_MATCH_PATTERN };

    private static final ParserRuleContext[] FIELD_MATCH_PATTERN_MEMBER_RHS =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] ERROR_MATCH_PATTERN_OR_CONST_PATTERN =
            { ParserRuleContext.OPEN_PARENTHESIS, ParserRuleContext.MATCH_PATTERN_RHS };

    private static final ParserRuleContext[] ERROR_MATCH_PATTERN_ERROR_KEYWORD_RHS =
            { ParserRuleContext.OPEN_PARENTHESIS, ParserRuleContext.TYPE_REFERENCE };

    private static final ParserRuleContext[] ERROR_ARG_LIST_MATCH_PATTERN_START =
            { ParserRuleContext.CONSTANT_EXPRESSION, ParserRuleContext.VAR_KEYWORD,
                    ParserRuleContext.ERROR_FIELD_MATCH_PATTERN, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] ERROR_MESSAGE_MATCH_PATTERN_END =
            { ParserRuleContext.ERROR_MESSAGE_MATCH_PATTERN_END_COMMA, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] ERROR_MESSAGE_MATCH_PATTERN_RHS =
            { ParserRuleContext.ERROR_CAUSE_MATCH_PATTERN, ParserRuleContext.ERROR_MATCH_PATTERN,
                    ParserRuleContext.ERROR_FIELD_MATCH_PATTERN };

    private static final ParserRuleContext[] ERROR_FIELD_MATCH_PATTERN =
            {  ParserRuleContext.NAMED_ARG_MATCH_PATTERN, ParserRuleContext.REST_MATCH_PATTERN };

    private static final ParserRuleContext[] ERROR_FIELD_MATCH_PATTERN_RHS =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] NAMED_ARG_MATCH_PATTERN_RHS =
            { ParserRuleContext.NAMED_ARG_MATCH_PATTERN, ParserRuleContext.REST_MATCH_PATTERN };

    private static final ParserRuleContext[] ORDER_KEY_LIST_END =
            { ParserRuleContext.ORDER_CLAUSE_END, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] LIST_BP_OR_LIST_CONSTRUCTOR_MEMBER =
            { ParserRuleContext.LIST_BINDING_PATTERN_MEMBER, ParserRuleContext.LIST_CONSTRUCTOR_FIRST_MEMBER };

    private static final ParserRuleContext[] TUPLE_TYPE_DESC_OR_LIST_CONST_MEMBER =
            { ParserRuleContext.TYPE_DESCRIPTOR, ParserRuleContext.LIST_CONSTRUCTOR_FIRST_MEMBER };

    private static final ParserRuleContext[] JOIN_CLAUSE_START =
            { ParserRuleContext.JOIN_KEYWORD, ParserRuleContext.OUTER_KEYWORD };

    private static final ParserRuleContext[] MAPPING_BP_OR_MAPPING_CONSTRUCTOR_MEMBER =
            { ParserRuleContext.MAPPING_BINDING_PATTERN_MEMBER, ParserRuleContext.MAPPING_FIELD };

    private static final ParserRuleContext[] LISTENERS_LIST_END =
            { ParserRuleContext.OBJECT_CONSTRUCTOR_BLOCK, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] FUNC_TYPE_DESC_START =
            { ParserRuleContext.FUNCTION_KEYWORD, ParserRuleContext.FUNC_TYPE_FIRST_QUALIFIER };

    private static final ParserRuleContext[] FUNC_TYPE_DESC_START_WITHOUT_FIRST_QUAL =
            { ParserRuleContext.FUNCTION_KEYWORD, ParserRuleContext.FUNC_TYPE_SECOND_QUALIFIER };

    private static final ParserRuleContext[] MODULE_CLASS_DEFINITION_START =
            { ParserRuleContext.CLASS_KEYWORD, ParserRuleContext.FIRST_CLASS_TYPE_QUALIFIER };

    private static final ParserRuleContext[] CLASS_DEF_WITHOUT_FIRST_QUALIFIER =
            { ParserRuleContext.CLASS_KEYWORD, ParserRuleContext.SECOND_CLASS_TYPE_QUALIFIER };

    private static final ParserRuleContext[] CLASS_DEF_WITHOUT_SECOND_QUALIFIER =
            { ParserRuleContext.CLASS_KEYWORD, ParserRuleContext.THIRD_CLASS_TYPE_QUALIFIER };

    private static final ParserRuleContext[] CLASS_DEF_WITHOUT_THIRD_QUALIFIER =
            { ParserRuleContext.CLASS_KEYWORD, ParserRuleContext.FOURTH_CLASS_TYPE_QUALIFIER };

    private static final ParserRuleContext[] REGULAR_COMPOUND_STMT_RHS =
            { ParserRuleContext.STATEMENT, ParserRuleContext.ON_FAIL_CLAUSE };

    private static final ParserRuleContext[] NAMED_WORKER_DECL_START =
            { ParserRuleContext.WORKER_KEYWORD, ParserRuleContext.TRANSACTIONAL_KEYWORD };

    private static final ParserRuleContext[] SERVICE_DECL_START =
            { ParserRuleContext.SERVICE_KEYWORD, ParserRuleContext.SERVICE_DECL_QUALIFIER };

    private static final ParserRuleContext[] OPTIONAL_SERVICE_DECL_TYPE =
            { ParserRuleContext.TYPE_DESC_IN_SERVICE, ParserRuleContext.OPTIONAL_ABSOLUTE_PATH };

    private static final ParserRuleContext[] OPTIONAL_ABSOLUTE_PATH =
            { ParserRuleContext.ABSOLUTE_RESOURCE_PATH, ParserRuleContext.STRING_LITERAL_TOKEN,
                    ParserRuleContext.ON_KEYWORD };

    private static final ParserRuleContext[] ABSOLUTE_RESOURCE_PATH_START =
            { ParserRuleContext.SLASH, ParserRuleContext.ABSOLUTE_PATH_SINGLE_SLASH };

    private static final ParserRuleContext[] ABSOLUTE_RESOURCE_PATH_END =
            { ParserRuleContext.SLASH, ParserRuleContext.SERVICE_DECL_RHS };

    // When service keyword is followed by an object type descriptor in top level,
    // we use this context go to recovery.
    private static final ParserRuleContext[] SERVICE_DECL_OR_VAR_DECL =
            { ParserRuleContext.OPTIONAL_ABSOLUTE_PATH, ParserRuleContext.SERVICE_VAR_DECL_RHS };

    private static final ParserRuleContext[] OPTIONAL_RELATIVE_PATH =
            { ParserRuleContext.OPEN_PARENTHESIS, ParserRuleContext.RELATIVE_RESOURCE_PATH };

    private static final ParserRuleContext[] FUNC_DEF_OR_TYPE_DESC_RHS =
            { ParserRuleContext.OPEN_PARENTHESIS, ParserRuleContext.RELATIVE_RESOURCE_PATH, ParserRuleContext.SEMICOLON,
                    ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] RELATIVE_RESOURCE_PATH_START =
            { ParserRuleContext.DOT, ParserRuleContext.RESOURCE_PATH_SEGMENT };

    private static final ParserRuleContext[] RESOURCE_PATH_SEGMENT =
            { ParserRuleContext.PATH_SEGMENT_IDENT, ParserRuleContext.RESOURCE_PATH_PARAM };

    private static final ParserRuleContext[] PATH_PARAM_OPTIONAL_ANNOTS =
            { ParserRuleContext.TYPE_DESC_IN_PATH_PARAM, ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] PATH_PARAM_ELLIPSIS =
            { ParserRuleContext.OPTIONAL_PATH_PARAM_NAME, ParserRuleContext.ELLIPSIS };
    
    private static final ParserRuleContext[] OPTIONAL_PATH_PARAM_NAME = 
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] RELATIVE_RESOURCE_PATH_END =
            { ParserRuleContext.RESOURCE_ACCESSOR_DEF_OR_DECL_RHS, ParserRuleContext.SLASH };

    private static final ParserRuleContext[] CONFIG_VAR_DECL_RHS =
            { ParserRuleContext.EXPRESSION, ParserRuleContext.QUESTION_MARK };

    private static final ParserRuleContext[] ERROR_CONSTRUCTOR_RHS =
            { ParserRuleContext.ARG_LIST_OPEN_PAREN, ParserRuleContext.TYPE_REFERENCE };

    private static final ParserRuleContext[] OPTIONAL_TYPE_PARAMETER =
            { ParserRuleContext.LT, ParserRuleContext.TYPE_DESC_RHS };

    private static final ParserRuleContext[] MAP_TYPE_OR_TYPE_REF =
            { ParserRuleContext.COLON, ParserRuleContext.LT };

    private static final ParserRuleContext[] OBJECT_TYPE_OR_TYPE_REF =
            { ParserRuleContext.COLON, ParserRuleContext.OBJECT_TYPE_OBJECT_KEYWORD_RHS };

    private static final ParserRuleContext[] STREAM_TYPE_OR_TYPE_REF =
            { ParserRuleContext.COLON, ParserRuleContext.LT };

    private static final ParserRuleContext[] TABLE_TYPE_OR_TYPE_REF =
            { ParserRuleContext.COLON, ParserRuleContext.ROW_TYPE_PARAM };

    private static final ParserRuleContext[] PARAMETERIZED_TYPE_OR_TYPE_REF =
            { ParserRuleContext.COLON, ParserRuleContext.OPTIONAL_TYPE_PARAMETER };

    private static final ParserRuleContext[] TYPE_DESC_RHS_OR_TYPE_REF =
            { ParserRuleContext.COLON, ParserRuleContext.TYPE_DESC_RHS };

    private static final ParserRuleContext[] TRANSACTION_STMT_RHS_OR_TYPE_REF =
            { ParserRuleContext.TYPE_REF_COLON, ParserRuleContext.TRANSACTION_STMT_TRANSACTION_KEYWORD_RHS };

    private static final ParserRuleContext[] TABLE_CONS_OR_QUERY_EXPR_OR_VAR_REF =
            { ParserRuleContext.VAR_REF_COLON, ParserRuleContext.EXPRESSION_START_TABLE_KEYWORD_RHS };

    private static final ParserRuleContext[] QUERY_EXPR_OR_VAR_REF =
            { ParserRuleContext.VAR_REF_COLON, ParserRuleContext.QUERY_CONSTRUCT_TYPE_RHS };

    private static final ParserRuleContext[] ERROR_CONS_EXPR_OR_VAR_REF =
            { ParserRuleContext.VAR_REF_COLON, ParserRuleContext.ERROR_CONS_ERROR_KEYWORD_RHS };

    private static final ParserRuleContext[] QUALIFIED_IDENTIFIER =
            { ParserRuleContext.QUALIFIED_IDENTIFIER_START_IDENTIFIER,
                    ParserRuleContext.QUALIFIED_IDENTIFIER_PREDECLARED_PREFIX };

    private static final ParserRuleContext[] MODULE_VAR_DECL_START =
            { ParserRuleContext.VAR_DECL_STMT, ParserRuleContext.MODULE_VAR_FIRST_QUAL };

    private static final ParserRuleContext[] MODULE_VAR_WITHOUT_FIRST_QUAL =
            { ParserRuleContext.VAR_DECL_STMT, ParserRuleContext.MODULE_VAR_SECOND_QUAL };

    private static final ParserRuleContext[] MODULE_VAR_WITHOUT_SECOND_QUAL =
            { ParserRuleContext.VAR_DECL_STMT, ParserRuleContext.MODULE_VAR_THIRD_QUAL };

    private static final ParserRuleContext[] EXPR_START_OR_INFERRED_TYPEDESC_DEFAULT_START =
            { ParserRuleContext.EXPRESSION, ParserRuleContext.INFERRED_TYPEDESC_DEFAULT_START_LT };

    private static final ParserRuleContext[] TYPE_CAST_PARAM_START_OR_INFERRED_TYPEDESC_DEFAULT_END =
            { ParserRuleContext.TYPE_CAST_PARAM_START, ParserRuleContext.INFERRED_TYPEDESC_DEFAULT_END_GT };

    private static final ParserRuleContext[] END_OF_PARAMS_OR_NEXT_PARAM_START =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] PARAM_START =
            { ParserRuleContext.TYPE_DESC_IN_PARAM, ParserRuleContext.ANNOTATIONS };
    
    private static final ParserRuleContext[] PARAM_RHS =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.REST_PARAM_RHS };

    private static final ParserRuleContext[] FUNC_TYPE_PARAM_RHS =
            { ParserRuleContext.PARAM_END, ParserRuleContext.PARAM_RHS };

    private static final ParserRuleContext[] ANNOTATION_DECL_START =
            { ParserRuleContext.ANNOTATION_KEYWORD, ParserRuleContext.CONST_KEYWORD };
    
    private static final ParserRuleContext[] OPTIONAL_RESOURCE_ACCESS_PATH =
            { ParserRuleContext.RESOURCE_ACCESS_PATH_SEGMENT, ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_METHOD };
    
    private static final ParserRuleContext[] RESOURCE_ACCESS_PATH_SEGMENT = 
            { ParserRuleContext.IDENTIFIER, ParserRuleContext.OPEN_BRACKET };
    
    private static final ParserRuleContext[] COMPUTED_SEGMENT_OR_REST_SEGMENT = 
            { ParserRuleContext.EXPRESSION, ParserRuleContext.ELLIPSIS };
    
    private static final ParserRuleContext[] RESOURCE_ACCESS_SEGMENT_RHS = 
            { ParserRuleContext.SLASH, ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_METHOD };
    
    private static final ParserRuleContext[] OPTIONAL_RESOURCE_ACCESS_METHOD = 
            { ParserRuleContext.DOT, ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST};
    
    private static final ParserRuleContext[] OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST = 
            { ParserRuleContext.ARG_LIST_OPEN_PAREN, ParserRuleContext.ACTION_END };

    private static final ParserRuleContext[] OPTIONAL_TOP_LEVEL_SEMICOLON =
            { ParserRuleContext.TOP_LEVEL_NODE, ParserRuleContext.SEMICOLON };

    public BallerinaParserErrorHandler(AbstractTokenReader tokenReader) {
        super(tokenReader);
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

        while (currentDepth < LOOKAHEAD_LIMIT) {
            skipRule = false;
            lookahead = getNextLookahead(lookahead);
            STToken nextToken = this.tokenReader.peek(lookahead);

            switch (currentCtx) {
                case EOF:
                    hasMatch = nextToken.kind == SyntaxKind.EOF_TOKEN;
                    break;
                case FUNC_NAME:
                case CLASS_NAME:
                case VARIABLE_NAME:
                case TYPE_NAME:
                case IMPORT_ORG_OR_MODULE_NAME:
                case IMPORT_MODULE_NAME:
                case MAPPING_FIELD_NAME:
                case QUALIFIED_IDENTIFIER_START_IDENTIFIER:
                case SIMPLE_TYPE_DESC_IDENTIFIER:
                case IDENTIFIER:
                case ANNOTATION_TAG:
                case NAMESPACE_PREFIX:
                case WORKER_NAME:
                case IMPLICIT_ANON_FUNC_PARAM:
                case METHOD_NAME:
                case RECEIVE_FIELD_NAME:
                case WAIT_FIELD_NAME:
                case FIELD_BINDING_PATTERN_NAME:
                case XML_ATOMIC_NAME_IDENTIFIER:
                case SIMPLE_BINDING_PATTERN:
                case ERROR_CAUSE_SIMPLE_BINDING_PATTERN:
                case PATH_SEGMENT_IDENT:
                case MODULE_ENUM_NAME:
                case ENUM_MEMBER_NAME:
                case NAMED_ARG_BINDING_PATTERN:
                    hasMatch = nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case IMPORT_PREFIX:
                    hasMatch = nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN ||
                            BallerinaParser.isPredeclaredPrefix(nextToken.kind);
                    break;
                case QUALIFIED_IDENTIFIER_PREDECLARED_PREFIX:
                    hasMatch = BallerinaParser.isPredeclaredPrefix(nextToken.kind);
                    break;
                case OPEN_PARENTHESIS:
                case PARENTHESISED_TYPE_DESC_START:
                case ARG_LIST_OPEN_PAREN:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN;
                    break;
                case CLOSE_PARENTHESIS:
                case ARG_LIST_CLOSE_PAREN:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN;
                    break;
                case SIMPLE_TYPE_DESCRIPTOR:
                    hasMatch = BallerinaParser.isSimpleType(nextToken.kind) ||
                            nextToken.kind == SyntaxKind.ERROR_KEYWORD ||
                            nextToken.kind == SyntaxKind.STREAM_KEYWORD ||
                            nextToken.kind == SyntaxKind.TYPEDESC_KEYWORD;
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
                case SEMICOLON:
                    hasMatch = nextToken.kind == SyntaxKind.SEMICOLON_TOKEN;
                    break;
                case BINARY_OPERATOR:
                    hasMatch = isBinaryOperator(nextToken);
                    break;
                case COMMA:
                case ERROR_MESSAGE_BINDING_PATTERN_END_COMMA:
                case ERROR_MESSAGE_MATCH_PATTERN_END_COMMA:
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
                case FIRST_OBJECT_CONS_QUALIFIER:
                case SECOND_OBJECT_CONS_QUALIFIER:
                case FIRST_OBJECT_TYPE_QUALIFIER:
                case SECOND_OBJECT_TYPE_QUALIFIER:
                    hasMatch = nextToken.kind == SyntaxKind.CLIENT_KEYWORD ||
                            nextToken.kind == SyntaxKind.ISOLATED_KEYWORD ||
                            nextToken.kind == SyntaxKind.SERVICE_KEYWORD;
                    break;
                case FIRST_CLASS_TYPE_QUALIFIER:
                case SECOND_CLASS_TYPE_QUALIFIER:
                case THIRD_CLASS_TYPE_QUALIFIER:
                case FOURTH_CLASS_TYPE_QUALIFIER:
                    hasMatch = nextToken.kind == SyntaxKind.DISTINCT_KEYWORD ||
                            nextToken.kind == SyntaxKind.CLIENT_KEYWORD ||
                            nextToken.kind == SyntaxKind.READONLY_KEYWORD ||
                            nextToken.kind == SyntaxKind.ISOLATED_KEYWORD ||
                            nextToken.kind == SyntaxKind.SERVICE_KEYWORD;
                    break;
                case OPEN_BRACKET:
                case TUPLE_TYPE_DESC_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN;
                    break;
                case CLOSE_BRACKET:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN;
                    break;
                case DOT:
                case METHOD_CALL_DOT:
                    hasMatch = nextToken.kind == SyntaxKind.DOT_TOKEN;
                    break;
                case BOOLEAN_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.TRUE_KEYWORD || nextToken.kind == SyntaxKind.FALSE_KEYWORD;
                    break;
                case DECIMAL_INTEGER_LITERAL_TOKEN:
                case MAJOR_VERSION:
                case MINOR_VERSION:
                case PATCH_VERSION:
                    hasMatch = nextToken.kind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN;
                    break;
                case SLASH:
                case ABSOLUTE_PATH_SINGLE_SLASH:
                case RESOURCE_METHOD_CALL_SLASH_TOKEN:    
                    hasMatch = nextToken.kind == SyntaxKind.SLASH_TOKEN;
                    break;
                case BASIC_LITERAL:
                    hasMatch = isBasicLiteral(nextToken.kind);
                    break;
                case COLON:
                case VAR_REF_COLON:
                case TYPE_REF_COLON:
                    hasMatch = nextToken.kind == SyntaxKind.COLON_TOKEN;
                    break;
                case STRING_LITERAL_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.STRING_LITERAL_TOKEN;
                    break;
                case UNARY_OPERATOR:
                    hasMatch = isUnaryOperator(nextToken);
                    break;
                case HEX_INTEGER_LITERAL_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN;
                    break;
                case AT:
                    hasMatch = nextToken.kind == SyntaxKind.AT_TOKEN;
                    break;
                case RIGHT_ARROW:
                    hasMatch = nextToken.kind == SyntaxKind.RIGHT_ARROW_TOKEN;
                    break;
                case PARAMETERIZED_TYPE:
                    hasMatch = BallerinaParser.isParameterizedTypeToken(nextToken.kind);
                    break;
                case LT:
                case STREAM_TYPE_PARAM_START_TOKEN:
                case INFERRED_TYPEDESC_DEFAULT_START_LT:
                    hasMatch = nextToken.kind == SyntaxKind.LT_TOKEN;
                    break;
                case GT:
                case INFERRED_TYPEDESC_DEFAULT_END_GT:
                    hasMatch = nextToken.kind == SyntaxKind.GT_TOKEN;
                    break;
                case FIELD_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.FIELD_KEYWORD;
                    break;
                case FUNCTION_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.FUNCTION_KEYWORD;
                    break;
                case IDENT_AFTER_OBJECT_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.FUNCTION_KEYWORD ||
                            nextToken.kind == SyntaxKind.FIELD_KEYWORD;
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
                case SERVICE_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.SERVICE_KEYWORD;
                    break;
                case REMOTE_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.REMOTE_KEYWORD;
                    break;
                case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN;
                    break;
                case HEX_FLOATING_POINT_LITERAL_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN;
                    break;
                case PIPE:
                    hasMatch = nextToken.kind == SyntaxKind.PIPE_TOKEN;
                    break;
                case TEMPLATE_START:
                case TEMPLATE_END:
                    hasMatch = nextToken.kind == SyntaxKind.BACKTICK_TOKEN;
                    break;
                case ASTERISK:
                    hasMatch = nextToken.kind == SyntaxKind.ASTERISK_TOKEN;
                    break;
                case BITWISE_AND_OPERATOR:
                    hasMatch = nextToken.kind == SyntaxKind.BITWISE_AND_TOKEN;
                    break;
                case EXPR_FUNC_BODY_START:
                case RIGHT_DOUBLE_ARROW:
                    hasMatch = nextToken.kind == SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN;
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
                    hasMatch = nextToken.kind == SyntaxKind.FUNCTION_KEYWORD ||
                            nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case LEFT_ARROW_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.LEFT_ARROW_TOKEN;
                    break;
                case ANNOT_CHAINING_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.ANNOT_CHAINING_TOKEN;
                    break;
                case OPTIONAL_CHAINING_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.OPTIONAL_CHAINING_TOKEN;
                    break;
                case TRANSACTIONAL_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TRANSACTIONAL_KEYWORD;
                    break;
                case SERVICE_DECL_QUALIFIER:
                    hasMatch = nextToken.kind == SyntaxKind.ISOLATED_KEYWORD;
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
                    hasMatch = nextToken.kind == SyntaxKind.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN;
                    break;
                case SLASH_ASTERISK_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.SLASH_ASTERISK_TOKEN;
                    break;
                case KEY_KEYWORD:
                    hasMatch = BallerinaParser.isKeyKeyword(nextToken);
                    break;
                case VAR_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.VAR_KEYWORD;
                    break;
                case ORDER_DIRECTION:
                    hasMatch = nextToken.kind == SyntaxKind.ASCENDING_KEYWORD ||
                            nextToken.kind == SyntaxKind.DESCENDING_KEYWORD;
                    break;
                case OBJECT_MEMBER_VISIBILITY_QUAL:
                    hasMatch = nextToken.kind == SyntaxKind.PRIVATE_KEYWORD ||
                            nextToken.kind == SyntaxKind.PUBLIC_KEYWORD;
                    break;
                case OBJECT_METHOD_FIRST_QUALIFIER:
                case OBJECT_METHOD_SECOND_QUALIFIER:
                case OBJECT_METHOD_THIRD_QUALIFIER:
                case OBJECT_METHOD_FOURTH_QUALIFIER:
                    hasMatch = nextToken.kind == SyntaxKind.ISOLATED_KEYWORD ||
                            nextToken.kind == SyntaxKind.TRANSACTIONAL_KEYWORD ||
                            nextToken.kind == SyntaxKind.REMOTE_KEYWORD ||
                            nextToken.kind == SyntaxKind.RESOURCE_KEYWORD;
                    break;
                case FUNC_DEF_FIRST_QUALIFIER:
                case FUNC_DEF_SECOND_QUALIFIER:
                case FUNC_TYPE_FIRST_QUALIFIER:
                case FUNC_TYPE_SECOND_QUALIFIER:
                    hasMatch = nextToken.kind == SyntaxKind.ISOLATED_KEYWORD ||
                            nextToken.kind == SyntaxKind.TRANSACTIONAL_KEYWORD;
                    break;
                case MODULE_VAR_FIRST_QUAL:
                case MODULE_VAR_SECOND_QUAL:
                case MODULE_VAR_THIRD_QUAL:
                    hasMatch = nextToken.kind == SyntaxKind.FINAL_KEYWORD ||
                            nextToken.kind == SyntaxKind.ISOLATED_KEYWORD ||
                            nextToken.kind == SyntaxKind.CONFIGURABLE_KEYWORD;
                    break;
                case COMPOUND_BINARY_OPERATOR:
                    hasMatch = BallerinaParser.isCompoundBinaryOperator(nextToken.kind);
                    break;
                case IS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IS_KEYWORD ||
                            nextToken.kind == SyntaxKind.NOT_IS_KEYWORD;
                    break;

                // start a context, so that we know where to fall back, and continue
                // having the qualified-identifier as the next rule.
                case VARIABLE_REF:
                case TYPE_REFERENCE_IN_TYPE_INCLUSION:
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
                case TYPE_DESC_IN_SERVICE:
                case TYPE_DESC_IN_PATH_PARAM:
                default:
                    if (isKeyword(currentCtx)) {
                        SyntaxKind expectedToken = getExpectedKeywordKind(currentCtx);
                        hasMatch = nextToken.kind == expectedToken;
                        break;
                    }

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

    /**
     * Get the next lookahead by skipping doc strings since error handler is not considering doc strings.
     *
     * @param lookahead current lookahead
     * @return next lookahead by skipping doc strings
     */
    private int getNextLookahead(int lookahead) {
        while (this.tokenReader.peek(lookahead).kind == SyntaxKind.DOCUMENTATION_STRING) {
            lookahead++;
        }
        return lookahead;
    }

    /**
     * @param currentCtx
     * @return
     */
    private boolean isKeyword(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case EOF:
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
            case FUNCTION_KEYWORD:
            case NEW_KEYWORD:
            case SELECT_KEYWORD:
            case WHERE_KEYWORD:
            case FROM_KEYWORD:
            case ORDER_KEYWORD:
            case BY_KEYWORD:
            case START_KEYWORD:
            case FLUSH_KEYWORD:
            case DEFAULT_WORKER_NAME_IN_ASYNC_SEND:
            case WAIT_KEYWORD:
            case CHECKING_KEYWORD:
            case FAIL_KEYWORD:
            case DO_KEYWORD:
            case TRANSACTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case COMMIT_KEYWORD:
            case RETRY_KEYWORD:
            case ROLLBACK_KEYWORD:
            case ENUM_KEYWORD:
            case MATCH_KEYWORD:
            case RETURNS_KEYWORD:
            case EXTERNAL_KEYWORD:
            case RECORD_KEYWORD:
            case TYPE_KEYWORD:
            case OBJECT_KEYWORD:
            case ABSTRACT_KEYWORD:
            case CLIENT_KEYWORD:
            case IF_KEYWORD:
            case ELSE_KEYWORD:
            case WHILE_KEYWORD:
            case PANIC_KEYWORD:
            case AS_KEYWORD:
            case LOCK_KEYWORD:
            case IMPORT_KEYWORD:
            case VERSION_KEYWORD:
            case CONTINUE_KEYWORD:
            case BREAK_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case ON_KEYWORD:
            case LISTENER_KEYWORD:
            case CONST_KEYWORD:
            case FINAL_KEYWORD:
            case TYPEOF_KEYWORD:
            case IS_KEYWORD:
            case NOT_IS_KEYWORD:
            case NULL_KEYWORD:
            case ANNOTATION_KEYWORD:
            case SOURCE_KEYWORD:
            case XMLNS_KEYWORD:
            case WORKER_KEYWORD:
            case FORK_KEYWORD:
            case TRAP_KEYWORD:
            case FOREACH_KEYWORD:
            case IN_KEYWORD:
            case TABLE_KEYWORD:
            case KEY_KEYWORD:
            case ERROR_KEYWORD:
            case LET_KEYWORD:
            case STREAM_KEYWORD:
            case XML_KEYWORD:
            case RE_KEYWORD:
            case STRING_KEYWORD:
            case BASE16_KEYWORD:
            case BASE64_KEYWORD:
            case DISTINCT_KEYWORD:
            case CONFLICT_KEYWORD:
            case LIMIT_KEYWORD:
            case EQUALS_KEYWORD:
            case JOIN_KEYWORD:
            case OUTER_KEYWORD:
            case CLASS_KEYWORD:
            case MAP_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected boolean hasAlternativePaths(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case TOP_LEVEL_NODE:
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
            case FUNC_OPTIONAL_RETURNS:
            case FUNC_BODY_OR_TYPE_DESC_RHS:
            case ANON_FUNC_BODY:
            case FUNC_BODY:
            case EXPRESSION:
            case TERMINAL_EXPRESSION:
            case VAR_DECL_STMT_RHS:
            case EXPRESSION_RHS:
            case VARIABLE_REF_RHS:
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case PARAM_LIST:
            case REQUIRED_PARAM_NAME_RHS:
            case TYPE_NAME_OR_VAR_NAME:
            case FIELD_DESCRIPTOR_RHS:
            case FIELD_OR_REST_DESCIPTOR_RHS:
            case RECORD_BODY_END:
            case RECORD_BODY_START:
            case TYPE_DESCRIPTOR:
            case TYPE_DESC_WITHOUT_ISOLATED:
            case RECORD_FIELD_OR_RECORD_END:
            case RECORD_FIELD_START:
            case RECORD_FIELD_WITHOUT_METADATA:
            case ARG_START:
            case ARG_START_OR_ARG_LIST_END:
            case NAMED_OR_POSITIONAL_ARG_RHS:
            case ARG_END:
            case CLASS_MEMBER_OR_OBJECT_MEMBER_START:
            case OBJECT_CONSTRUCTOR_MEMBER_START:
            case CLASS_MEMBER_OR_OBJECT_MEMBER_WITHOUT_META:
            case OBJECT_CONS_MEMBER_WITHOUT_META:
            case OPTIONAL_FIELD_INITIALIZER:
            case OBJECT_METHOD_START:
            case OBJECT_FUNC_OR_FIELD:
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
            case OBJECT_TYPE_START:
            case OBJECT_CONSTRUCTOR_START:
            case ELSE_BLOCK:
            case ELSE_BODY:
            case CALL_STMT_START:
            case IMPORT_PREFIX_DECL:
            case IMPORT_VERSION_DECL:
            case IMPORT_DECL_ORG_OR_MODULE_NAME_RHS:
            case AFTER_IMPORT_MODULE_NAME:
            case MAJOR_MINOR_VERSION_END:
            case RETURN_STMT_RHS:
            case ACCESS_EXPRESSION:
            case FIRST_MAPPING_FIELD:
            case MAPPING_FIELD:
            case SPECIFIC_FIELD:
            case SPECIFIC_FIELD_RHS:
            case MAPPING_FIELD_END:
            case OPTIONAL_ABSOLUTE_PATH:
            case CONST_DECL_TYPE:
            case CONST_DECL_RHS:
            case ARRAY_LENGTH:
            case PARAMETER_START:
            case PARAMETER_START_WITHOUT_ANNOTATION:
            case STMT_START_WITH_EXPR_RHS:
            case EXPR_STMT_RHS:
            case EXPRESSION_STATEMENT_START:
            case ANNOT_DECL_OPTIONAL_TYPE:
            case ANNOT_DECL_RHS:
            case ANNOT_OPTIONAL_ATTACH_POINTS:
            case ATTACH_POINT:
            case ATTACH_POINT_IDENT:
            case ATTACH_POINT_END:
            case XML_NAMESPACE_PREFIX_DECL:
            case CONSTANT_EXPRESSION_START:
            case TYPE_DESC_RHS:
            case LIST_CONSTRUCTOR_FIRST_MEMBER:
            case LIST_CONSTRUCTOR_MEMBER:
            case TYPE_CAST_PARAM:
            case TYPE_CAST_PARAM_RHS:
            case TABLE_KEYWORD_RHS:
            case ROW_LIST_RHS:
            case TABLE_ROW_END:
            case KEY_SPECIFIER_RHS:
            case TABLE_KEY_RHS:
            case LET_VAR_DECL_START:
            case ORDER_KEY_LIST_END:
            case STREAM_TYPE_FIRST_PARAM_RHS:
            case TEMPLATE_MEMBER:
            case TEMPLATE_STRING_RHS:
            case FUNCTION_KEYWORD_RHS:
            case FUNC_TYPE_FUNC_KEYWORD_RHS_START:
            case WORKER_NAME_RHS:
            case BINDING_PATTERN:
            case LIST_BINDING_PATTERNS_START:
            case LIST_BINDING_PATTERN_MEMBER_END:
            case FIELD_BINDING_PATTERN_END:
            case LIST_BINDING_PATTERN_MEMBER:
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
            case TUPLE_TYPE_MEMBER_RHS:
            case LIST_CONSTRUCTOR_MEMBER_END:
            case NIL_OR_PARENTHESISED_TYPE_DESC_RHS:
            case REMOTE_OR_RESOURCE_CALL_OR_ASYNC_SEND_RHS:
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
            case BINDING_PATTERN_OR_VAR_REF_RHS:
            case BRACKETED_LIST_RHS:
            case BRACKETED_LIST_MEMBER:
            case BRACKETED_LIST_MEMBER_END:
            case TYPE_DESC_RHS_OR_BP_RHS:
            case LIST_BINDING_MEMBER_OR_ARRAY_LENGTH:
            case XML_NAVIGATE_EXPR:
            case XML_NAME_PATTERN_RHS:
            case XML_ATOMIC_NAME_PATTERN_START:
            case XML_ATOMIC_NAME_IDENTIFIER_RHS:
            case XML_STEP_START:
            case FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY:
            case OPTIONAL_MATCH_GUARD:
            case MATCH_PATTERN_LIST_MEMBER_RHS:
            case MATCH_PATTERN_START:
            case LIST_MATCH_PATTERNS_START:
            case LIST_MATCH_PATTERN_MEMBER:
            case LIST_MATCH_PATTERN_MEMBER_RHS:
            case ERROR_BINDING_PATTERN_ERROR_KEYWORD_RHS:
            case ERROR_ARG_LIST_BINDING_PATTERN_START:
            case ERROR_MESSAGE_BINDING_PATTERN_END:
            case ERROR_MESSAGE_BINDING_PATTERN_RHS:
            case ERROR_FIELD_BINDING_PATTERN:
            case ERROR_FIELD_BINDING_PATTERN_END:
            case FIELD_MATCH_PATTERNS_START:
            case FIELD_MATCH_PATTERN_MEMBER:
            case FIELD_MATCH_PATTERN_MEMBER_RHS:
            case ERROR_MATCH_PATTERN_OR_CONST_PATTERN:
            case ERROR_MATCH_PATTERN_ERROR_KEYWORD_RHS:
            case ERROR_ARG_LIST_MATCH_PATTERN_START:
            case ERROR_MESSAGE_MATCH_PATTERN_END:
            case ERROR_MESSAGE_MATCH_PATTERN_RHS:
            case ERROR_FIELD_MATCH_PATTERN:
            case ERROR_FIELD_MATCH_PATTERN_RHS:
            case NAMED_ARG_MATCH_PATTERN_RHS:
            case EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS:
            case LIST_BP_OR_LIST_CONSTRUCTOR_MEMBER:
            case TUPLE_TYPE_DESC_OR_LIST_CONST_MEMBER:
            case OBJECT_METHOD_WITHOUT_FIRST_QUALIFIER:
            case OBJECT_METHOD_WITHOUT_SECOND_QUALIFIER:
            case OBJECT_METHOD_WITHOUT_THIRD_QUALIFIER:
            case JOIN_CLAUSE_START:
            case INTERMEDIATE_CLAUSE_START:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR_MEMBER:
            case TYPE_DESC_OR_EXPR_RHS:
            case LISTENERS_LIST_END:
            case REGULAR_COMPOUND_STMT_RHS:
            case NAMED_WORKER_DECL_START:
            case FUNC_TYPE_DESC_START:
            case ANON_FUNC_EXPRESSION_START:
            case MODULE_CLASS_DEFINITION_START:
            case OBJECT_CONSTRUCTOR_TYPE_REF:
            case OBJECT_FIELD_QUALIFIER:
            case OPTIONAL_SERVICE_DECL_TYPE:
            case SERVICE_IDENT_RHS:
            case ABSOLUTE_RESOURCE_PATH_START:
            case ABSOLUTE_RESOURCE_PATH_END:
            case SERVICE_DECL_OR_VAR_DECL:
            case OPTIONAL_RELATIVE_PATH:
            case RELATIVE_RESOURCE_PATH_START:
            case RELATIVE_RESOURCE_PATH_END:
            case RESOURCE_PATH_SEGMENT:
            case PATH_PARAM_OPTIONAL_ANNOTS:
            case PATH_PARAM_ELLIPSIS:
            case OPTIONAL_PATH_PARAM_NAME:    
            case OBJECT_CONS_WITHOUT_FIRST_QUALIFIER:
            case OBJECT_TYPE_WITHOUT_FIRST_QUALIFIER:
            case CONFIG_VAR_DECL_RHS:
            case SERVICE_DECL_START:
            case ERROR_CONSTRUCTOR_RHS:
            case OPTIONAL_TYPE_PARAMETER:
            case MAP_TYPE_OR_TYPE_REF:
            case OBJECT_TYPE_OR_TYPE_REF:
            case STREAM_TYPE_OR_TYPE_REF:
            case TABLE_TYPE_OR_TYPE_REF:
            case PARAMETERIZED_TYPE_OR_TYPE_REF:
            case TYPE_DESC_RHS_OR_TYPE_REF:
            case TRANSACTION_STMT_RHS_OR_TYPE_REF:
            case TABLE_CONS_OR_QUERY_EXPR_OR_VAR_REF:
            case QUERY_EXPR_OR_VAR_REF:
            case ERROR_CONS_EXPR_OR_VAR_REF:
            case QUALIFIED_IDENTIFIER:
            case CLASS_DEF_WITHOUT_FIRST_QUALIFIER:
            case CLASS_DEF_WITHOUT_SECOND_QUALIFIER:
            case CLASS_DEF_WITHOUT_THIRD_QUALIFIER:
            case FUNC_DEF_START:
            case FUNC_DEF_WITHOUT_FIRST_QUALIFIER:
            case FUNC_TYPE_DESC_START_WITHOUT_FIRST_QUAL:
            case MODULE_VAR_DECL_START:
            case MODULE_VAR_WITHOUT_FIRST_QUAL:
            case MODULE_VAR_WITHOUT_SECOND_QUAL:
            case FUNC_DEF_OR_TYPE_DESC_RHS:
            case CLASS_DESCRIPTOR:
            case EXPR_START_OR_INFERRED_TYPEDESC_DEFAULT_START:
            case TYPE_CAST_PARAM_START_OR_INFERRED_TYPEDESC_DEFAULT_END:
            case END_OF_PARAMS_OR_NEXT_PARAM_START:
            case ASSIGNMENT_STMT_RHS:
            case PARAM_START:
            case PARAM_RHS:
            case FUNC_TYPE_PARAM_RHS:
            case ANNOTATION_DECL_START:
            case ON_FAIL_OPTIONAL_BINDING_PATTERN:
            case OPTIONAL_RESOURCE_ACCESS_PATH:
            case RESOURCE_ACCESS_PATH_SEGMENT:
            case COMPUTED_SEGMENT_OR_REST_SEGMENT:    
            case RESOURCE_ACCESS_SEGMENT_RHS:    
            case OPTIONAL_RESOURCE_ACCESS_METHOD:
            case OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST:
            case OPTIONAL_TOP_LEVEL_SEMICOLON:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the shortest alternative path for a parser rule with alternatives.
     *
     * @param currentCtx Parser rule with alternatives
     * @return shortest alternative path
     */
    protected ParserRuleContext getShortestAlternative(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case TOP_LEVEL_NODE:
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
                return ParserRuleContext.EOF;
            case FUNC_OPTIONAL_RETURNS:
                return ParserRuleContext.RETURNS_KEYWORD;
            case FUNC_BODY_OR_TYPE_DESC_RHS:
                return ParserRuleContext.FUNC_BODY;
            case ANON_FUNC_BODY:
                return ParserRuleContext.EXPLICIT_ANON_FUNC_EXPR_BODY_START;
            case FUNC_BODY:
                return ParserRuleContext.FUNC_BODY_BLOCK;
            case EXPRESSION:
            case TERMINAL_EXPRESSION:
                return ParserRuleContext.VARIABLE_REF;
            case VAR_DECL_STMT_RHS:
                return ParserRuleContext.SEMICOLON;
            case EXPRESSION_RHS:
            case VARIABLE_REF_RHS:
                return ParserRuleContext.BINARY_OPERATOR;
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
                return ParserRuleContext.VAR_DECL_STMT;
            case PARAM_LIST:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case REQUIRED_PARAM_NAME_RHS:
                return ParserRuleContext.PARAM_END;
            case TYPE_NAME_OR_VAR_NAME:
                return ParserRuleContext.VARIABLE_NAME;
            case FIELD_DESCRIPTOR_RHS:
                return ParserRuleContext.SEMICOLON;
            case FIELD_OR_REST_DESCIPTOR_RHS:
                return ParserRuleContext.VARIABLE_NAME;
            case RECORD_BODY_END:
                return ParserRuleContext.CLOSE_BRACE;
            case RECORD_BODY_START:
                return ParserRuleContext.OPEN_BRACE;
            case TYPE_DESCRIPTOR:
                return ParserRuleContext.SIMPLE_TYPE_DESC_IDENTIFIER;
            case TYPE_DESC_WITHOUT_ISOLATED:
                return ParserRuleContext.FUNC_TYPE_DESC;
            case RECORD_FIELD_OR_RECORD_END:
                return ParserRuleContext.RECORD_BODY_END;
            case RECORD_FIELD_START:
            case RECORD_FIELD_WITHOUT_METADATA:
                return ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD;
            case ARG_START:
                return ParserRuleContext.EXPRESSION;
            case ARG_START_OR_ARG_LIST_END:
                return ParserRuleContext.ARG_LIST_END;
            case NAMED_OR_POSITIONAL_ARG_RHS:
                return ParserRuleContext.ARG_END;
            case ARG_END:
                return ParserRuleContext.ARG_LIST_END;
            case CLASS_MEMBER_OR_OBJECT_MEMBER_START:
            case OBJECT_CONSTRUCTOR_MEMBER_START:
            case CLASS_MEMBER_OR_OBJECT_MEMBER_WITHOUT_META:
            case OBJECT_CONS_MEMBER_WITHOUT_META:
                return ParserRuleContext.CLOSE_BRACE;
            case OPTIONAL_FIELD_INITIALIZER:
                return ParserRuleContext.SEMICOLON;
            case ON_FAIL_OPTIONAL_BINDING_PATTERN:
                return ParserRuleContext.BLOCK_STMT;
            case OBJECT_METHOD_START:
                return ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE;
            case OBJECT_FUNC_OR_FIELD:
                return ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY;
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
                return ParserRuleContext.OBJECT_FIELD_START;
            case OBJECT_TYPE_START:
            case OBJECT_CONSTRUCTOR_START:
                return ParserRuleContext.OBJECT_KEYWORD;
            case ELSE_BLOCK:
                return ParserRuleContext.STATEMENT;
            case ELSE_BODY:
                return ParserRuleContext.OPEN_BRACE;
            case CALL_STMT_START:
                return ParserRuleContext.VARIABLE_REF;
            case IMPORT_PREFIX_DECL:
            case IMPORT_VERSION_DECL:
                return ParserRuleContext.SEMICOLON;
            case IMPORT_DECL_ORG_OR_MODULE_NAME_RHS:
                return ParserRuleContext.AFTER_IMPORT_MODULE_NAME;
            case AFTER_IMPORT_MODULE_NAME:
            case MAJOR_MINOR_VERSION_END:
            case RETURN_STMT_RHS:
                return ParserRuleContext.SEMICOLON;
            case ACCESS_EXPRESSION:
                return ParserRuleContext.VARIABLE_REF;
            case FIRST_MAPPING_FIELD:
                return ParserRuleContext.CLOSE_BRACE;
            case MAPPING_FIELD:
                return ParserRuleContext.SPECIFIC_FIELD;
            case SPECIFIC_FIELD:
                return ParserRuleContext.MAPPING_FIELD_NAME;
            case SPECIFIC_FIELD_RHS:
                return ParserRuleContext.MAPPING_FIELD_END;
            case MAPPING_FIELD_END:
                return ParserRuleContext.CLOSE_BRACE;
            case OPTIONAL_ABSOLUTE_PATH:
                return ParserRuleContext.ON_KEYWORD;
            case CONST_DECL_TYPE:
                return ParserRuleContext.VARIABLE_NAME;
            case CONST_DECL_RHS:
                return ParserRuleContext.ASSIGN_OP;
            case ARRAY_LENGTH:
                return ParserRuleContext.CLOSE_BRACKET;
            case PARAMETER_START:
                return ParserRuleContext.PARAMETER_START_WITHOUT_ANNOTATION;
            case PARAMETER_START_WITHOUT_ANNOTATION:
                return ParserRuleContext.TYPE_DESC_IN_PARAM;
            case STMT_START_WITH_EXPR_RHS:
            case EXPR_STMT_RHS:
                return ParserRuleContext.SEMICOLON;
            case EXPRESSION_STATEMENT_START:
                return ParserRuleContext.VARIABLE_REF;
            case ANNOT_DECL_OPTIONAL_TYPE:
                return ParserRuleContext.ANNOTATION_TAG;
            case ANNOT_DECL_RHS:
            case ANNOT_OPTIONAL_ATTACH_POINTS:
                return ParserRuleContext.SEMICOLON;
            case ATTACH_POINT:
                return ParserRuleContext.ATTACH_POINT_IDENT;
            case ATTACH_POINT_IDENT:
                return ParserRuleContext.SINGLE_KEYWORD_ATTACH_POINT_IDENT;
            case ATTACH_POINT_END:
            case BINDING_PATTERN_OR_VAR_REF_RHS:
                return ParserRuleContext.SEMICOLON;
            case XML_NAMESPACE_PREFIX_DECL:
                return ParserRuleContext.SEMICOLON;
            case CONSTANT_EXPRESSION_START:
                return ParserRuleContext.VARIABLE_REF;
            case TYPE_DESC_RHS:
                return ParserRuleContext.END_OF_TYPE_DESC;
            case LIST_CONSTRUCTOR_FIRST_MEMBER:
                return ParserRuleContext.CLOSE_BRACKET;
            case LIST_CONSTRUCTOR_MEMBER:
                return ParserRuleContext.EXPRESSION;
            case TYPE_CAST_PARAM:
            case TYPE_CAST_PARAM_RHS:
                return ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS;
            case TABLE_KEYWORD_RHS:
                return ParserRuleContext.TABLE_CONSTRUCTOR;
            case ROW_LIST_RHS:
            case TABLE_ROW_END:
                return ParserRuleContext.CLOSE_BRACKET;
            case KEY_SPECIFIER_RHS:
            case TABLE_KEY_RHS:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case LET_VAR_DECL_START:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case ORDER_KEY_LIST_END:
                return ParserRuleContext.ORDER_CLAUSE_END;
            case STREAM_TYPE_FIRST_PARAM_RHS:
                return ParserRuleContext.GT;
            case TEMPLATE_MEMBER:
            case TEMPLATE_STRING_RHS:
                return ParserRuleContext.TEMPLATE_END;
            case FUNCTION_KEYWORD_RHS:
                return ParserRuleContext.FUNC_TYPE_FUNC_KEYWORD_RHS;
            case FUNC_TYPE_FUNC_KEYWORD_RHS_START:
                return ParserRuleContext.FUNC_TYPE_DESC_END;
            case WORKER_NAME_RHS:
                return ParserRuleContext.BLOCK_STMT;
            case BINDING_PATTERN:
                return ParserRuleContext.BINDING_PATTERN_STARTING_IDENTIFIER;
            case LIST_BINDING_PATTERNS_START:
            case LIST_BINDING_PATTERN_MEMBER_END:
                return ParserRuleContext.CLOSE_BRACKET;
            case FIELD_BINDING_PATTERN_END:
                return ParserRuleContext.CLOSE_BRACE;
            case LIST_BINDING_PATTERN_MEMBER:
                return ParserRuleContext.BINDING_PATTERN;
            case MAPPING_BINDING_PATTERN_END:
                return ParserRuleContext.CLOSE_BRACE;
            case MAPPING_BINDING_PATTERN_MEMBER:
                return ParserRuleContext.FIELD_BINDING_PATTERN;
            case KEY_CONSTRAINTS_RHS:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case TABLE_TYPE_DESC_RHS:
                return ParserRuleContext.TYPE_DESC_RHS;
            case NEW_KEYWORD_RHS:
                return ParserRuleContext.EXPRESSION_RHS;
            case TABLE_CONSTRUCTOR_OR_QUERY_START:
                return ParserRuleContext.TABLE_KEYWORD;
            case TABLE_CONSTRUCTOR_OR_QUERY_RHS:
                return ParserRuleContext.TABLE_CONSTRUCTOR;
            case QUERY_PIPELINE_RHS:
                return ParserRuleContext.QUERY_EXPRESSION_RHS;
            case BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS:
            case ANON_FUNC_PARAM_RHS:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case PARAM_END:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case ANNOTATION_REF_RHS:
                return ParserRuleContext.ANNOTATION_END;
            case INFER_PARAM_END_OR_PARENTHESIS_END:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case TYPE_DESC_IN_TUPLE_RHS:
                return ParserRuleContext.CLOSE_BRACKET;
            case TUPLE_TYPE_MEMBER_RHS:
                return ParserRuleContext.CLOSE_BRACKET;
            case LIST_CONSTRUCTOR_MEMBER_END:
                return ParserRuleContext.CLOSE_BRACKET;
            case NIL_OR_PARENTHESISED_TYPE_DESC_RHS:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case REMOTE_OR_RESOURCE_CALL_OR_ASYNC_SEND_RHS:
                return ParserRuleContext.PEER_WORKER_NAME;
            case REMOTE_CALL_OR_ASYNC_SEND_END:
                return ParserRuleContext.SEMICOLON;
            case RECEIVE_WORKERS:
            case RECEIVE_FIELD:
                return ParserRuleContext.PEER_WORKER_NAME;
            case RECEIVE_FIELD_END:
                return ParserRuleContext.CLOSE_BRACE;
            case WAIT_KEYWORD_RHS:
                return ParserRuleContext.MULTI_WAIT_FIELDS;
            case WAIT_FIELD_NAME_RHS:
                return ParserRuleContext.WAIT_FIELD_END;
            case WAIT_FIELD_END:
                return ParserRuleContext.CLOSE_BRACE;
            case WAIT_FUTURE_EXPR_END:
                return ParserRuleContext.ALTERNATE_WAIT_EXPR_LIST_END;
            case OPTIONAL_PEER_WORKER:
                return ParserRuleContext.EXPRESSION_RHS;
            case ENUM_MEMBER_START:
                return ParserRuleContext.ENUM_MEMBER_NAME;
            case ENUM_MEMBER_RHS:
                return ParserRuleContext.ENUM_MEMBER_END;
            case ENUM_MEMBER_END:
                return ParserRuleContext.CLOSE_BRACE;
            case MEMBER_ACCESS_KEY_EXPR_END:
                return ParserRuleContext.CLOSE_BRACKET;
            case ROLLBACK_RHS:
                return ParserRuleContext.SEMICOLON;
            case RETRY_KEYWORD_RHS:
                return ParserRuleContext.RETRY_TYPE_PARAM_RHS;
            case RETRY_TYPE_PARAM_RHS:
                return ParserRuleContext.RETRY_BODY;
            case RETRY_BODY:
                return ParserRuleContext.BLOCK_STMT;
            case STMT_START_BRACKETED_LIST_MEMBER:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case STMT_START_BRACKETED_LIST_RHS:
                return ParserRuleContext.VARIABLE_NAME;
            case BINDING_PATTERN_OR_EXPR_RHS:
            case BRACKETED_LIST_RHS:
                return ParserRuleContext.TYPE_DESC_RHS_OR_BP_RHS;
            case BRACKETED_LIST_MEMBER:
                return ParserRuleContext.EXPRESSION;
            case BRACKETED_LIST_MEMBER_END:
                return ParserRuleContext.CLOSE_BRACKET;
            case TYPE_DESC_RHS_OR_BP_RHS:
                return ParserRuleContext.TYPE_DESC_RHS_IN_TYPED_BP;
            case LIST_BINDING_MEMBER_OR_ARRAY_LENGTH:
                return ParserRuleContext.BINDING_PATTERN;
            case XML_NAVIGATE_EXPR:
                return ParserRuleContext.XML_FILTER_EXPR;
            case XML_NAME_PATTERN_RHS:
                return ParserRuleContext.GT;
            case XML_ATOMIC_NAME_PATTERN_START:
                return ParserRuleContext.XML_ATOMIC_NAME_IDENTIFIER;
            case XML_ATOMIC_NAME_IDENTIFIER_RHS:
                return ParserRuleContext.IDENTIFIER;
            case XML_STEP_START:
                return ParserRuleContext.SLASH_ASTERISK_TOKEN;
            case FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY:
                return ParserRuleContext.ANON_FUNC_BODY;
            case OPTIONAL_MATCH_GUARD:
                return ParserRuleContext.RIGHT_DOUBLE_ARROW;
            case MATCH_PATTERN_LIST_MEMBER_RHS:
                return ParserRuleContext.MATCH_PATTERN_END;
            case MATCH_PATTERN_START:
                return ParserRuleContext.CONSTANT_EXPRESSION;
            case LIST_MATCH_PATTERNS_START:
                return ParserRuleContext.CLOSE_BRACKET;
            case LIST_MATCH_PATTERN_MEMBER:
                return ParserRuleContext.MATCH_PATTERN_START;
            case LIST_MATCH_PATTERN_MEMBER_RHS:
                return ParserRuleContext.CLOSE_BRACKET;
            case ERROR_BINDING_PATTERN_ERROR_KEYWORD_RHS:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case ERROR_ARG_LIST_BINDING_PATTERN_START:
            case ERROR_MESSAGE_BINDING_PATTERN_END:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case ERROR_MESSAGE_BINDING_PATTERN_RHS:
                return ParserRuleContext.ERROR_CAUSE_SIMPLE_BINDING_PATTERN;
            case ERROR_FIELD_BINDING_PATTERN:
                return ParserRuleContext.NAMED_ARG_BINDING_PATTERN;
            case ERROR_FIELD_BINDING_PATTERN_END:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case FIELD_MATCH_PATTERNS_START:
                return ParserRuleContext.CLOSE_BRACE;
            case FIELD_MATCH_PATTERN_MEMBER:
                return ParserRuleContext.VARIABLE_NAME;
            case FIELD_MATCH_PATTERN_MEMBER_RHS:
                return ParserRuleContext.CLOSE_BRACE;
            case ERROR_MATCH_PATTERN_OR_CONST_PATTERN:
                return ParserRuleContext.MATCH_PATTERN_RHS;
            case ERROR_MATCH_PATTERN_ERROR_KEYWORD_RHS:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case ERROR_ARG_LIST_MATCH_PATTERN_START:
            case ERROR_MESSAGE_MATCH_PATTERN_END:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case ERROR_MESSAGE_MATCH_PATTERN_RHS:
                return ParserRuleContext.ERROR_CAUSE_MATCH_PATTERN;
            case ERROR_FIELD_MATCH_PATTERN:
                return ParserRuleContext.NAMED_ARG_MATCH_PATTERN;
            case ERROR_FIELD_MATCH_PATTERN_RHS:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case NAMED_ARG_MATCH_PATTERN_RHS:
                return ParserRuleContext.NAMED_ARG_MATCH_PATTERN;
            case EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS:
                return ParserRuleContext.EXTERNAL_KEYWORD;
            case LIST_BP_OR_LIST_CONSTRUCTOR_MEMBER:
                return ParserRuleContext.LIST_BINDING_PATTERN_MEMBER;
            case TUPLE_TYPE_DESC_OR_LIST_CONST_MEMBER:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case OBJECT_METHOD_WITHOUT_FIRST_QUALIFIER:
            case OBJECT_METHOD_WITHOUT_SECOND_QUALIFIER:
            case OBJECT_METHOD_WITHOUT_THIRD_QUALIFIER:
            case FUNC_DEF:
                return ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE;
            case JOIN_CLAUSE_START:
                return ParserRuleContext.JOIN_KEYWORD;
            case INTERMEDIATE_CLAUSE_START:
                return ParserRuleContext.WHERE_CLAUSE;
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR_MEMBER:
                return ParserRuleContext.MAPPING_BINDING_PATTERN_MEMBER;
            case TYPE_DESC_OR_EXPR_RHS:
                return ParserRuleContext.TYPE_DESC_RHS_OR_BP_RHS;
            case LISTENERS_LIST_END:
                return ParserRuleContext.OBJECT_CONSTRUCTOR_BLOCK;
            case REGULAR_COMPOUND_STMT_RHS:
                return ParserRuleContext.STATEMENT;
            case NAMED_WORKER_DECL_START:
                return ParserRuleContext.WORKER_KEYWORD;
            case FUNC_TYPE_DESC_START:
            case FUNC_DEF_START:
            case ANON_FUNC_EXPRESSION_START:
                return ParserRuleContext.FUNCTION_KEYWORD;
            case MODULE_CLASS_DEFINITION_START:
                return ParserRuleContext.CLASS_KEYWORD;
            case OBJECT_CONSTRUCTOR_TYPE_REF:
                return ParserRuleContext.OPEN_BRACE;
            case OBJECT_FIELD_QUALIFIER:
                return ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER;
            case OPTIONAL_SERVICE_DECL_TYPE:
                return ParserRuleContext.OPTIONAL_ABSOLUTE_PATH;
            case SERVICE_IDENT_RHS:
                return ParserRuleContext.ATTACH_POINT_END;
            case ABSOLUTE_RESOURCE_PATH_START:
                return ParserRuleContext.ABSOLUTE_PATH_SINGLE_SLASH;
            case ABSOLUTE_RESOURCE_PATH_END:
                return ParserRuleContext.SERVICE_DECL_RHS;
            case SERVICE_DECL_OR_VAR_DECL:
                return ParserRuleContext.SERVICE_VAR_DECL_RHS;
            case OPTIONAL_RELATIVE_PATH:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case RELATIVE_RESOURCE_PATH_START:
                return ParserRuleContext.DOT;
            case RELATIVE_RESOURCE_PATH_END:
                return ParserRuleContext.RESOURCE_ACCESSOR_DEF_OR_DECL_RHS;
            case RESOURCE_PATH_SEGMENT:
                return ParserRuleContext.PATH_SEGMENT_IDENT;
            case PATH_PARAM_OPTIONAL_ANNOTS:
                return ParserRuleContext.TYPE_DESC_IN_PATH_PARAM;
            case PATH_PARAM_ELLIPSIS:
            case OPTIONAL_PATH_PARAM_NAME:
                return ParserRuleContext.CLOSE_BRACKET;
            case OBJECT_CONS_WITHOUT_FIRST_QUALIFIER:
            case OBJECT_TYPE_WITHOUT_FIRST_QUALIFIER:
                return ParserRuleContext.OBJECT_KEYWORD;
            case CONFIG_VAR_DECL_RHS:
                return ParserRuleContext.EXPRESSION;
            case SERVICE_DECL_START:
                return ParserRuleContext.SERVICE_KEYWORD;
            case ERROR_CONSTRUCTOR_RHS:
                return ParserRuleContext.ARG_LIST_OPEN_PAREN;
            case OPTIONAL_TYPE_PARAMETER:
                return ParserRuleContext.TYPE_DESC_RHS;
            case MAP_TYPE_OR_TYPE_REF:
                return ParserRuleContext.LT;
            case OBJECT_TYPE_OR_TYPE_REF:
                return ParserRuleContext.OBJECT_TYPE_OBJECT_KEYWORD_RHS;
            case STREAM_TYPE_OR_TYPE_REF:
                return ParserRuleContext.LT;
            case TABLE_TYPE_OR_TYPE_REF:
                return ParserRuleContext.ROW_TYPE_PARAM;
            case PARAMETERIZED_TYPE_OR_TYPE_REF:
                return ParserRuleContext.OPTIONAL_TYPE_PARAMETER;
            case TYPE_DESC_RHS_OR_TYPE_REF:
                return ParserRuleContext.TYPE_DESC_RHS;
            case TRANSACTION_STMT_RHS_OR_TYPE_REF:
                return ParserRuleContext.TRANSACTION_STMT_TRANSACTION_KEYWORD_RHS;
            case TABLE_CONS_OR_QUERY_EXPR_OR_VAR_REF:
                return ParserRuleContext.EXPRESSION_START_TABLE_KEYWORD_RHS;
            case QUERY_EXPR_OR_VAR_REF:
                return ParserRuleContext.QUERY_CONSTRUCT_TYPE_RHS;
            case ERROR_CONS_EXPR_OR_VAR_REF:
                return ParserRuleContext.ERROR_CONS_ERROR_KEYWORD_RHS;
            case QUALIFIED_IDENTIFIER:
                return ParserRuleContext.QUALIFIED_IDENTIFIER_START_IDENTIFIER;
            case CLASS_DEF_WITHOUT_FIRST_QUALIFIER:
            case CLASS_DEF_WITHOUT_SECOND_QUALIFIER:
            case CLASS_DEF_WITHOUT_THIRD_QUALIFIER:
                return ParserRuleContext.CLASS_KEYWORD;
            case FUNC_DEF_WITHOUT_FIRST_QUALIFIER:
                return ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE;
            case FUNC_TYPE_DESC_START_WITHOUT_FIRST_QUAL:
                return ParserRuleContext.FUNCTION_KEYWORD;
            case MODULE_VAR_DECL_START:
            case MODULE_VAR_WITHOUT_FIRST_QUAL:
            case MODULE_VAR_WITHOUT_SECOND_QUAL:
                return ParserRuleContext.VAR_DECL_STMT;
            case FUNC_DEF_OR_TYPE_DESC_RHS:
                return ParserRuleContext.SEMICOLON;
            case CLASS_DESCRIPTOR:
                return ParserRuleContext.TYPE_REFERENCE;
            case EXPR_START_OR_INFERRED_TYPEDESC_DEFAULT_START:
                return ParserRuleContext.EXPRESSION;
            case TYPE_CAST_PARAM_START_OR_INFERRED_TYPEDESC_DEFAULT_END:
                return ParserRuleContext.INFERRED_TYPEDESC_DEFAULT_END_GT;
            case END_OF_PARAMS_OR_NEXT_PARAM_START:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case ASSIGNMENT_STMT_RHS:
                return ParserRuleContext.ASSIGN_OP;
            case PARAM_START:
                return ParserRuleContext.TYPE_DESC_IN_PARAM;
            case PARAM_RHS:
                return ParserRuleContext.VARIABLE_NAME;
            case FUNC_TYPE_PARAM_RHS:
                return ParserRuleContext.PARAM_END;
            case ANNOTATION_DECL_START:
                return ParserRuleContext.ANNOTATION_KEYWORD;
            case OPTIONAL_RESOURCE_ACCESS_PATH:
            case RESOURCE_ACCESS_SEGMENT_RHS:
                return ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_METHOD;
            case RESOURCE_ACCESS_PATH_SEGMENT:
                return ParserRuleContext.IDENTIFIER;
            case COMPUTED_SEGMENT_OR_REST_SEGMENT:
                return ParserRuleContext.EXPRESSION;
            case OPTIONAL_RESOURCE_ACCESS_METHOD:
                return ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST;
            case OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST:
                return ParserRuleContext.ACTION_END;
            case OPTIONAL_TOP_LEVEL_SEMICOLON:
                return ParserRuleContext.TOP_LEVEL_NODE;
            default:
                throw new IllegalStateException("Alternative path entry not found");
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
            case FUNC_DEF_START:
                alternativeRules = FUNC_DEF_START;
                break;
            case FUNC_DEF_WITHOUT_FIRST_QUALIFIER:
                alternativeRules = FUNC_DEF_WITHOUT_FIRST_QUALIFIER;
                break;
            case FUNC_TYPE_DESC_START_WITHOUT_FIRST_QUAL:
                alternativeRules = FUNC_TYPE_DESC_START_WITHOUT_FIRST_QUAL;
                break;
            case FUNC_OPTIONAL_RETURNS:
                ParserRuleContext parentCtx = getParentContext();
                ParserRuleContext[] alternatives;
                if (parentCtx == ParserRuleContext.FUNC_DEF) {
                    ParserRuleContext grandParentCtx = getGrandParentContext();
                    if (grandParentCtx == ParserRuleContext.OBJECT_TYPE_MEMBER) {
                        alternatives = METHOD_DECL_OPTIONAL_RETURNS;
                    } else {
                        alternatives = FUNC_DEF_OPTIONAL_RETURNS;
                    }
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
                alternativeRules = FUNC_BODY;
                break;
            case PARAM_LIST:
                alternativeRules = PARAM_LIST;
                break;
            case REQUIRED_PARAM_NAME_RHS:
                alternativeRules = REQUIRED_PARAM_NAME_RHS;
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
                assert isInTypeDescContext();
                alternativeRules = TYPE_DESCRIPTORS;
                break;
            case TYPE_DESC_WITHOUT_ISOLATED:
                alternativeRules = TYPE_DESCRIPTOR_WITHOUT_ISOLATED;
                break;
            case CLASS_DESCRIPTOR:
                alternativeRules = CLASS_DESCRIPTOR;
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
            case CLASS_MEMBER_OR_OBJECT_MEMBER_START:
                alternativeRules = CLASS_MEMBER_OR_OBJECT_MEMBER_START;
                break;
            case OBJECT_CONSTRUCTOR_MEMBER_START:
                alternativeRules = OBJECT_CONSTRUCTOR_MEMBER_START;
                break;
            case CLASS_MEMBER_OR_OBJECT_MEMBER_WITHOUT_META:
                alternativeRules = CLASS_MEMBER_OR_OBJECT_MEMBER_WITHOUT_META;
                break;
            case OBJECT_CONS_MEMBER_WITHOUT_META:
                alternativeRules = OBJECT_CONS_MEMBER_WITHOUT_META;
                break;
            case OPTIONAL_FIELD_INITIALIZER:
                alternativeRules = OPTIONAL_FIELD_INITIALIZER;
                break;
            case ON_FAIL_OPTIONAL_BINDING_PATTERN:
                alternativeRules = ON_FAIL_OPTIONAL_BINDING_PATTERN;
                break;
            case OBJECT_METHOD_START:
                alternativeRules = OBJECT_METHOD_START;
                break;
            case OBJECT_METHOD_WITHOUT_FIRST_QUALIFIER:
                alternativeRules = OBJECT_METHOD_WITHOUT_FIRST_QUALIFIER;
                break;
            case OBJECT_METHOD_WITHOUT_SECOND_QUALIFIER:
                alternativeRules = OBJECT_METHOD_WITHOUT_SECOND_QUALIFIER;
                break;
            case OBJECT_METHOD_WITHOUT_THIRD_QUALIFIER:
                alternativeRules = OBJECT_METHOD_WITHOUT_THIRD_QUALIFIER;
                break;
            case OBJECT_FUNC_OR_FIELD:
                alternativeRules = OBJECT_FUNC_OR_FIELD;
                break;
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
                alternativeRules = OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY;
                break;
            case OBJECT_TYPE_START:
                alternativeRules = OBJECT_TYPE_START;
                break;
            case OBJECT_CONSTRUCTOR_START:
                alternativeRules = OBJECT_CONSTRUCTOR_START;
                break;
            case IMPORT_PREFIX_DECL:
                alternativeRules = IMPORT_PREFIX_DECL;
                break;
            case IMPORT_VERSION_DECL:
                alternativeRules = IMPORT_VERSION;
                break;
            case IMPORT_DECL_ORG_OR_MODULE_NAME_RHS:
                alternativeRules = IMPORT_DECL_ORG_OR_MODULE_NAME_RHS;
                break;
            case AFTER_IMPORT_MODULE_NAME:
                alternativeRules = AFTER_IMPORT_MODULE_NAME;
                break;
            case MAJOR_MINOR_VERSION_END:
                alternativeRules = MAJOR_MINOR_VERSION_END;
                break;
            case OPTIONAL_ABSOLUTE_PATH:
                alternativeRules = OPTIONAL_ABSOLUTE_PATH;
                break;
            case CONST_DECL_TYPE:
                alternativeRules = CONST_DECL_TYPE;
                break;
            case CONST_DECL_RHS:
                alternativeRules = CONST_DECL_RHS;
                break;
            case PARAMETER_START:
                alternativeRules = PARAMETER_START;
                break;
            case PARAMETER_START_WITHOUT_ANNOTATION:
                alternativeRules = PARAMETER_START_WITHOUT_ANNOTATION;
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
            case ENUM_MEMBER_START:
                alternativeRules = ENUM_MEMBER_START;
                break;
            case ENUM_MEMBER_RHS:
                alternativeRules = ENUM_MEMBER_RHS;
                break;
            case ENUM_MEMBER_END:
                alternativeRules = ENUM_MEMBER_END;
                break;
            case EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS:
                alternativeRules = EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS;
                break;
            case LIST_BP_OR_LIST_CONSTRUCTOR_MEMBER:
                alternativeRules = LIST_BP_OR_LIST_CONSTRUCTOR_MEMBER;
                break;
            case TUPLE_TYPE_DESC_OR_LIST_CONST_MEMBER:
                alternativeRules = TUPLE_TYPE_DESC_OR_LIST_CONST_MEMBER;
                break;
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR_MEMBER:
                alternativeRules = MAPPING_BP_OR_MAPPING_CONSTRUCTOR_MEMBER;
                break;
            case FUNC_TYPE_DESC_START:
            case ANON_FUNC_EXPRESSION_START:
                alternativeRules = FUNC_TYPE_DESC_START;
                break;
            case MODULE_CLASS_DEFINITION_START:
                alternativeRules = MODULE_CLASS_DEFINITION_START;
                break;
            case CLASS_DEF_WITHOUT_FIRST_QUALIFIER:
                alternativeRules = CLASS_DEF_WITHOUT_FIRST_QUALIFIER;
                break;
            case CLASS_DEF_WITHOUT_SECOND_QUALIFIER:
                alternativeRules = CLASS_DEF_WITHOUT_SECOND_QUALIFIER;
                break;
            case CLASS_DEF_WITHOUT_THIRD_QUALIFIER:
                alternativeRules = CLASS_DEF_WITHOUT_THIRD_QUALIFIER;
                break;
            case OBJECT_CONSTRUCTOR_TYPE_REF:
                alternativeRules = OBJECT_CONSTRUCTOR_RHS;
                break;
            case OBJECT_FIELD_QUALIFIER:
                alternativeRules = OBJECT_FIELD_QUALIFIER;
                break;
            case CONFIG_VAR_DECL_RHS:
                alternativeRules = CONFIG_VAR_DECL_RHS;
                break;
            case OPTIONAL_SERVICE_DECL_TYPE:
                alternativeRules = OPTIONAL_SERVICE_DECL_TYPE;
                break;
            case SERVICE_IDENT_RHS:
                alternativeRules = SERVICE_IDENT_RHS;
                break;
            case ABSOLUTE_RESOURCE_PATH_START:
                alternativeRules = ABSOLUTE_RESOURCE_PATH_START;
                break;
            case ABSOLUTE_RESOURCE_PATH_END:
                alternativeRules = ABSOLUTE_RESOURCE_PATH_END;
                break;
            case SERVICE_DECL_OR_VAR_DECL:
                alternativeRules = SERVICE_DECL_OR_VAR_DECL;
                break;
            case OPTIONAL_RELATIVE_PATH:
                alternativeRules = OPTIONAL_RELATIVE_PATH;
                break;
            case RELATIVE_RESOURCE_PATH_START:
                alternativeRules = RELATIVE_RESOURCE_PATH_START;
                break;
            case RESOURCE_PATH_SEGMENT:
                alternativeRules = RESOURCE_PATH_SEGMENT;
                break;
            case PATH_PARAM_OPTIONAL_ANNOTS:
                alternativeRules = PATH_PARAM_OPTIONAL_ANNOTS;
                break;
            case PATH_PARAM_ELLIPSIS:
                alternativeRules = PATH_PARAM_ELLIPSIS;
                break;
            case OPTIONAL_PATH_PARAM_NAME:
                alternativeRules = OPTIONAL_PATH_PARAM_NAME;
                break;
            case RELATIVE_RESOURCE_PATH_END:
                alternativeRules = RELATIVE_RESOURCE_PATH_END;
                break;
            case SERVICE_DECL_START:
                alternativeRules = SERVICE_DECL_START;
                break;
            case OPTIONAL_TYPE_PARAMETER:
                alternativeRules = OPTIONAL_TYPE_PARAMETER;
                break;
            case MAP_TYPE_OR_TYPE_REF:
                alternativeRules = MAP_TYPE_OR_TYPE_REF;
                break;
            case OBJECT_TYPE_OR_TYPE_REF:
                alternativeRules = OBJECT_TYPE_OR_TYPE_REF;
                break;
            case STREAM_TYPE_OR_TYPE_REF:
                alternativeRules = STREAM_TYPE_OR_TYPE_REF;
                break;
            case TABLE_TYPE_OR_TYPE_REF:
                alternativeRules = TABLE_TYPE_OR_TYPE_REF;
                break;
            case PARAMETERIZED_TYPE_OR_TYPE_REF:
                alternativeRules = PARAMETERIZED_TYPE_OR_TYPE_REF;
                break;
            case TYPE_DESC_RHS_OR_TYPE_REF:
                alternativeRules = TYPE_DESC_RHS_OR_TYPE_REF;
                break;
            case TRANSACTION_STMT_RHS_OR_TYPE_REF:
                alternativeRules = TRANSACTION_STMT_RHS_OR_TYPE_REF;
                break;
            case TABLE_CONS_OR_QUERY_EXPR_OR_VAR_REF:
                alternativeRules = TABLE_CONS_OR_QUERY_EXPR_OR_VAR_REF;
                break;
            case QUERY_EXPR_OR_VAR_REF:
                alternativeRules = QUERY_EXPR_OR_VAR_REF;
                break;
            case ERROR_CONS_EXPR_OR_VAR_REF:
                alternativeRules = ERROR_CONS_EXPR_OR_VAR_REF;
                break;
            case QUALIFIED_IDENTIFIER:
                alternativeRules = QUALIFIED_IDENTIFIER;
                break;
            case MODULE_VAR_DECL_START:
                alternativeRules = MODULE_VAR_DECL_START;
                break;
            case MODULE_VAR_WITHOUT_FIRST_QUAL:
                alternativeRules = MODULE_VAR_WITHOUT_FIRST_QUAL;
                break;
            case MODULE_VAR_WITHOUT_SECOND_QUAL:
                alternativeRules = MODULE_VAR_WITHOUT_SECOND_QUAL;
                break;
            case OBJECT_TYPE_WITHOUT_FIRST_QUALIFIER:
                alternativeRules = OBJECT_TYPE_WITHOUT_FIRST_QUALIFIER;
                break;
            case FUNC_DEF_OR_TYPE_DESC_RHS:
                alternativeRules = FUNC_DEF_OR_TYPE_DESC_RHS;
                break;
            case EXPR_START_OR_INFERRED_TYPEDESC_DEFAULT_START:
                alternativeRules = EXPR_START_OR_INFERRED_TYPEDESC_DEFAULT_START;
                break;
            case TYPE_CAST_PARAM_START_OR_INFERRED_TYPEDESC_DEFAULT_END:
                alternativeRules = TYPE_CAST_PARAM_START_OR_INFERRED_TYPEDESC_DEFAULT_END;
                break;
            case END_OF_PARAMS_OR_NEXT_PARAM_START:
                alternativeRules = END_OF_PARAMS_OR_NEXT_PARAM_START;
                break;
            case PARAM_START:
                alternativeRules = PARAM_START;
                break;
            case PARAM_RHS:
                alternativeRules = PARAM_RHS;
                break;
            case FUNC_TYPE_PARAM_RHS:
                alternativeRules = FUNC_TYPE_PARAM_RHS;
                break;
            case ANNOTATION_DECL_START:
                alternativeRules = ANNOTATION_DECL_START;
                break;
            case OPTIONAL_TOP_LEVEL_SEMICOLON:
                alternativeRules = OPTIONAL_TOP_LEVEL_SEMICOLON;
                break;
            default:
                return seekMatchInStmtRelatedAlternativePaths(currentCtx, lookahead, currentDepth, matchingRulesCount,
                        isEntryPoint);
        }

        return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules, isEntryPoint);
    }

    private Result seekMatchInStmtRelatedAlternativePaths(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                                          int matchingRulesCount, boolean isEntryPoint) {
        ParserRuleContext[] alternativeRules;
        switch (currentCtx) {
            case VAR_DECL_STMT_RHS:
                alternativeRules = VAR_DECL_RHS;
                break;
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
                return seekInStatements(currentCtx, lookahead, currentDepth, matchingRulesCount, isEntryPoint);
            case TYPE_NAME_OR_VAR_NAME:
                alternativeRules = TYPE_OR_VAR_NAME;
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
            case RETURN_STMT_RHS:
                alternativeRules = RETURN_RHS;
                break;
            case ARRAY_LENGTH:
                alternativeRules = ARRAY_LENGTH;
                break;
            case STMT_START_WITH_EXPR_RHS:
                alternativeRules = STMT_START_WITH_EXPR_RHS;
                break;
            case EXPR_STMT_RHS:
                alternativeRules = EXPR_STMT_RHS;
                break;
            case EXPRESSION_STATEMENT_START:
                alternativeRules = EXPRESSION_STATEMENT_START;
                break;
            case TYPE_DESC_RHS:
                assert isInTypeDescContext();
                alternativeRules = TYPE_DESC_RHS;
                break;
            case STREAM_TYPE_FIRST_PARAM_RHS:
                alternativeRules = STREAM_TYPE_FIRST_PARAM_RHS;
                break;
            case FUNCTION_KEYWORD_RHS:
                alternativeRules = FUNCTION_KEYWORD_RHS;
                break;
            case FUNC_TYPE_FUNC_KEYWORD_RHS_START:
                alternativeRules = FUNC_TYPE_FUNC_KEYWORD_RHS_START;
                break;
            case WORKER_NAME_RHS:
                alternativeRules = WORKER_NAME_RHS;
                break;
            case BINDING_PATTERN:
                alternativeRules = BINDING_PATTERN;
                break;
            case LIST_BINDING_PATTERNS_START:
                alternativeRules = LIST_BINDING_PATTERNS_START;
                break;
            case LIST_BINDING_PATTERN_MEMBER_END:
                alternativeRules = LIST_BINDING_PATTERN_MEMBER_END;
                break;
            case LIST_BINDING_PATTERN_MEMBER:
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
            case ERROR_BINDING_PATTERN_ERROR_KEYWORD_RHS:
                alternativeRules = ERROR_BINDING_PATTERN_ERROR_KEYWORD_RHS;
                break;
            case ERROR_ARG_LIST_BINDING_PATTERN_START:
                alternativeRules = ERROR_ARG_LIST_BINDING_PATTERN_START;
                break;
            case ERROR_MESSAGE_BINDING_PATTERN_END:
                alternativeRules = ERROR_MESSAGE_BINDING_PATTERN_END;
                break;
            case ERROR_MESSAGE_BINDING_PATTERN_RHS:
                alternativeRules = ERROR_MESSAGE_BINDING_PATTERN_RHS;
                break;
            case ERROR_FIELD_BINDING_PATTERN:
                alternativeRules = ERROR_FIELD_BINDING_PATTERN;
                break;
            case ERROR_FIELD_BINDING_PATTERN_END:
                alternativeRules = ERROR_FIELD_BINDING_PATTERN_END;
                break;
            case KEY_CONSTRAINTS_RHS:
                alternativeRules = KEY_CONSTRAINTS_RHS;
                break;
            case TABLE_TYPE_DESC_RHS:
                alternativeRules = TABLE_TYPE_DESC_RHS;
                break;
            case TYPE_DESC_IN_TUPLE_RHS:
                alternativeRules = TYPE_DESC_IN_TUPLE_RHS;
                break;
            case TUPLE_TYPE_MEMBER_RHS:
                alternativeRules = TUPLE_TYPE_MEMBER_RHS;
                break;
            case LIST_CONSTRUCTOR_MEMBER_END:
                alternativeRules = LIST_CONSTRUCTOR_MEMBER_END;
                break;
            case NIL_OR_PARENTHESISED_TYPE_DESC_RHS:
                alternativeRules = NIL_OR_PARENTHESISED_TYPE_DESC_RHS;
                break;
            case REMOTE_OR_RESOURCE_CALL_OR_ASYNC_SEND_RHS:
                alternativeRules = REMOTE_OR_RESOURCE_CALL_OR_ASYNC_SEND_RHS;
                break;
            case REMOTE_CALL_OR_ASYNC_SEND_END:
                alternativeRules = REMOTE_CALL_OR_ASYNC_SEND_END;
                break;
            case OPTIONAL_RESOURCE_ACCESS_PATH:
                alternativeRules = OPTIONAL_RESOURCE_ACCESS_PATH;
                break;
            case RESOURCE_ACCESS_PATH_SEGMENT:
                alternativeRules = RESOURCE_ACCESS_PATH_SEGMENT;
                break;
            case COMPUTED_SEGMENT_OR_REST_SEGMENT:
                alternativeRules = COMPUTED_SEGMENT_OR_REST_SEGMENT;
                break;
            case RESOURCE_ACCESS_SEGMENT_RHS:
                alternativeRules = RESOURCE_ACCESS_SEGMENT_RHS;
                break;
            case OPTIONAL_RESOURCE_ACCESS_METHOD:
                alternativeRules = OPTIONAL_RESOURCE_ACCESS_METHOD;
                break;
            case OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST:
                alternativeRules = OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST;
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
            case TYPE_DESC_OR_EXPR_RHS:
                alternativeRules = BRACKETED_LIST_RHS;
                break;
            case BINDING_PATTERN_OR_VAR_REF_RHS:
                alternativeRules = BINDING_PATTERN_OR_VAR_REF_RHS;
                break;
            case TYPE_DESC_RHS_OR_BP_RHS:
                alternativeRules = TYPE_DESC_RHS_OR_BP_RHS;
                break;
            case LIST_BINDING_MEMBER_OR_ARRAY_LENGTH:
                alternativeRules = LIST_BINDING_MEMBER_OR_ARRAY_LENGTH;
                break;
            case MATCH_PATTERN_LIST_MEMBER_RHS:
                alternativeRules = MATCH_PATTERN_LIST_MEMBER_RHS;
                break;
            case MATCH_PATTERN_START:
                alternativeRules = MATCH_PATTERN_START;
                break;
            case LIST_MATCH_PATTERNS_START:
                alternativeRules = LIST_MATCH_PATTERNS_START;
                break;
            case LIST_MATCH_PATTERN_MEMBER:
                alternativeRules = LIST_MATCH_PATTERN_MEMBER;
                break;
            case LIST_MATCH_PATTERN_MEMBER_RHS:
                alternativeRules = LIST_MATCH_PATTERN_MEMBER_RHS;
                break;
            case FIELD_MATCH_PATTERNS_START:
                alternativeRules = FIELD_MATCH_PATTERNS_START;
                break;
            case FIELD_MATCH_PATTERN_MEMBER:
                alternativeRules = FIELD_MATCH_PATTERN_MEMBER;
                break;
            case FIELD_MATCH_PATTERN_MEMBER_RHS:
                alternativeRules = FIELD_MATCH_PATTERN_MEMBER_RHS;
                break;
            case ERROR_MATCH_PATTERN_OR_CONST_PATTERN:
                alternativeRules = ERROR_MATCH_PATTERN_OR_CONST_PATTERN;
                break;
            case ERROR_MATCH_PATTERN_ERROR_KEYWORD_RHS:
                alternativeRules = ERROR_MATCH_PATTERN_ERROR_KEYWORD_RHS;
                break;
            case ERROR_ARG_LIST_MATCH_PATTERN_START:
                alternativeRules = ERROR_ARG_LIST_MATCH_PATTERN_START;
                break;
            case ERROR_MESSAGE_MATCH_PATTERN_END:
                alternativeRules = ERROR_MESSAGE_MATCH_PATTERN_END;
                break;
            case ERROR_MESSAGE_MATCH_PATTERN_RHS:
                alternativeRules = ERROR_MESSAGE_MATCH_PATTERN_RHS;
                break;
            case ERROR_FIELD_MATCH_PATTERN:
                alternativeRules = ERROR_FIELD_MATCH_PATTERN;
                break;
            case ERROR_FIELD_MATCH_PATTERN_RHS:
                alternativeRules = ERROR_FIELD_MATCH_PATTERN_RHS;
                break;
            case NAMED_ARG_MATCH_PATTERN_RHS:
                alternativeRules = NAMED_ARG_MATCH_PATTERN_RHS;
                break;
            case JOIN_CLAUSE_START:
                alternativeRules = JOIN_CLAUSE_START;
                break;
            case INTERMEDIATE_CLAUSE_START:
                alternativeRules = INTERMEDIATE_CLAUSE_START;
                break;
            case REGULAR_COMPOUND_STMT_RHS:
                alternativeRules = REGULAR_COMPOUND_STMT_RHS;
                break;
            case NAMED_WORKER_DECL_START:
                alternativeRules = NAMED_WORKER_DECL_START;
                break;
            case ASSIGNMENT_STMT_RHS:
                alternativeRules = ASSIGNMENT_STMT_RHS;
                break;
            default:
                return seekMatchInExprRelatedAlternativePaths(currentCtx, lookahead, currentDepth, matchingRulesCount,
                        isEntryPoint);
        }

        return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules, isEntryPoint);
    }

    private Result seekMatchInExprRelatedAlternativePaths(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                                          int matchingRulesCount, boolean isEntryPoint) {
        ParserRuleContext[] alternativeRules;
        switch (currentCtx) {
            case EXPRESSION:
            case TERMINAL_EXPRESSION:
                alternativeRules = EXPRESSION_START;
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
            case LET_VAR_DECL_START:
                alternativeRules = LET_VAR_DECL_START;
                break;
            case ORDER_KEY_LIST_END:
                alternativeRules = ORDER_KEY_LIST_END;
                break;
            case TEMPLATE_MEMBER:
                alternativeRules = TEMPLATE_MEMBER;
                break;
            case TEMPLATE_STRING_RHS:
                alternativeRules = TEMPLATE_STRING_RHS;
                break;
            case CONSTANT_EXPRESSION_START:
                alternativeRules = CONSTANT_EXPRESSION;
                break;
            case LIST_CONSTRUCTOR_FIRST_MEMBER:
                alternativeRules = LIST_CONSTRUCTOR_FIRST_MEMBER;
                break;
            case LIST_CONSTRUCTOR_MEMBER:
                alternativeRules = LIST_CONSTRUCTOR_MEMBER;
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
                alternativeRules = QUERY_PIPELINE_RHS;
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
            case OPTIONAL_MATCH_GUARD:
                alternativeRules = OPTIONAL_MATCH_GUARD;
                break;
            case MEMBER_ACCESS_KEY_EXPR_END:
                alternativeRules = MEMBER_ACCESS_KEY_EXPR_END;
                break;
            case LISTENERS_LIST_END:
                alternativeRules = LISTENERS_LIST_END;
                break;
            case OBJECT_CONS_WITHOUT_FIRST_QUALIFIER:
                alternativeRules = OBJECT_CONS_WITHOUT_FIRST_QUALIFIER;
                break;
            case EXPRESSION_RHS:
                return seekMatchInExpressionRhs(lookahead, currentDepth, matchingRulesCount, isEntryPoint, false);
            case VARIABLE_REF_RHS:
                return seekMatchInExpressionRhs(lookahead, currentDepth, matchingRulesCount, isEntryPoint, true);
            case ERROR_CONSTRUCTOR_RHS:
                alternativeRules = ERROR_CONSTRUCTOR_RHS;
                break;
            default:
                throw new IllegalStateException("seekMatchInExprRelatedAlternativePaths found: " + currentCtx);
        }

        return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules, isEntryPoint);
    }

    /**
     * Search for matching token sequences within different kinds of statements and returns the most optimal solution.
     *
     * @param currentCtx     Current context
     * @param lookahead      Position of the next token to consider, relative to the position of the original error
     * @param currentDepth   Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param isEntryPoint   Flag indicating whether this is the entry point to the error recovery
     * @return Recovery result
     */
    private Result seekInStatements(ParserRuleContext currentCtx, int lookahead, int currentDepth, int currentMatches,
                                    boolean isEntryPoint) {
        STToken nextToken = this.tokenReader.peek(lookahead);
        if (nextToken.kind == SyntaxKind.SEMICOLON_TOKEN) {
            // Semicolon at the start of a statement is a special case. This is equivalent to an empty
            // statement. So assume the fix for this is a REMOVE operation and continue from the next token.
            Result result = seekMatchInSubTree(ParserRuleContext.STATEMENT, lookahead + 1, currentDepth + 1,
                    isEntryPoint);
            result.pushFix(new Solution(Action.REMOVE, currentCtx, nextToken.kind, nextToken.toString(), currentDepth));
            return getFinalResult(currentMatches, result);
        }

        return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, STATEMENTS, isEntryPoint);
    }

    /**
     * Search for matching token sequences within access expressions and returns the most optimal solution.
     * Access expression can be one of: method-call, field-access, member-access.
     *
     * @param currentCtx     Current context
     * @param lookahead      Position of the next token to consider, relative to the position of the original error
     * @param currentDepth   Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param isEntryPoint   Flag indicating whether this is the entry point to the error recovery
     * @return Recovery result
     */
    private Result seekInAccessExpression(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                          int currentMatches, boolean isEntryPoint) {
        // TODO: Remove this method
        STToken nextToken = this.tokenReader.peek(lookahead);
        currentDepth++;
        if (nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
            return fixAndContinue(currentCtx, lookahead, currentDepth, currentMatches, isEntryPoint);
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
     * @param lookahead      Position of the next token to consider, relative to the position of the original error
     * @param currentDepth   Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param isEntryPoint   Flag indicating whether this is the entry point to the error recovery
     * @param allowFuncCall  Whether function call is allowed or not
     * @return Recovery result
     */
    private Result seekMatchInExpressionRhs(int lookahead, int currentDepth, int currentMatches, boolean isEntryPoint,
                                            boolean allowFuncCall) {
        ParserRuleContext parentCtx = getParentContext();
        ParserRuleContext[] alternatives = null;
        switch (parentCtx) {
            case ARG_LIST:
                alternatives = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                        ParserRuleContext.ARG_LIST_END };
                break;
            case MAPPING_CONSTRUCTOR:
            case MULTI_WAIT_FIELDS:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                alternatives = new ParserRuleContext[] { ParserRuleContext.CLOSE_BRACE, ParserRuleContext.COMMA,
                        ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR };
                break;
            case COMPUTED_FIELD_NAME:
                // Here we give high priority to the comma. Therefore order of the below array matters.
                alternatives = new ParserRuleContext[] { ParserRuleContext.CLOSE_BRACKET,
                        ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR, ParserRuleContext.OPEN_BRACKET };
                break;
            case LISTENERS_LIST:
                alternatives = new ParserRuleContext[] { ParserRuleContext.LISTENERS_LIST_END,
                        ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR };
                break;
            case LIST_CONSTRUCTOR:
            case MEMBER_ACCESS_KEY_EXPR:
            case BRACKETED_LIST:
            case STMT_START_BRACKETED_LIST:
                alternatives = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                        ParserRuleContext.CLOSE_BRACKET };
                break;
            case LET_EXPR_LET_VAR_DECL:
                alternatives = new ParserRuleContext[] { ParserRuleContext.IN_KEYWORD, ParserRuleContext.COMMA, 
                        ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT, 
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN, 
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR, 
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR };
                break;
            case LET_CLAUSE_LET_VAR_DECL:
                alternatives = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                        ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                        ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                        ParserRuleContext.LET_CLAUSE_END };
                break;
            case ORDER_KEY_LIST:
                alternatives = new ParserRuleContext[] { ParserRuleContext.ORDER_DIRECTION,
                        ParserRuleContext.ORDER_KEY_LIST_END, ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR };
                break;
            case QUERY_EXPRESSION:
                alternatives = new ParserRuleContext[] { ParserRuleContext.QUERY_PIPELINE_RHS, 
                        ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                        ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                        ParserRuleContext.MEMBER_ACCESS_KEY_EXPR };
                break;
            default:
                if (isParameter(parentCtx)) {
                    alternatives = new ParserRuleContext[] { ParserRuleContext.CLOSE_PARENTHESIS,
                            ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                            ParserRuleContext.ANNOT_CHAINING_TOKEN, ParserRuleContext.OPTIONAL_CHAINING_TOKEN,
                            ParserRuleContext.CONDITIONAL_EXPRESSION, ParserRuleContext.XML_NAVIGATE_EXPR,
                            ParserRuleContext.MEMBER_ACCESS_KEY_EXPR, ParserRuleContext.COMMA };
                }
                break;
        }

        if (alternatives != null) {
            if (allowFuncCall) {
                alternatives = modifyAlternativesWithArgListStart(alternatives);
            }
            return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, alternatives, isEntryPoint);
        }

        ParserRuleContext nextContext;
        if (parentCtx == ParserRuleContext.IF_BLOCK || parentCtx == ParserRuleContext.WHILE_BLOCK ||
                parentCtx == ParserRuleContext.FOREACH_STMT) {
            nextContext = ParserRuleContext.BLOCK_STMT;
        } else if (parentCtx == ParserRuleContext.MATCH_STMT) {
            nextContext = ParserRuleContext.MATCH_BODY;
        } else if (parentCtx == ParserRuleContext.CALL_STMT) {
            nextContext = ParserRuleContext.METHOD_CALL_DOT;
        } else if (isStatement(parentCtx) ||
                parentCtx == ParserRuleContext.RECORD_FIELD ||
                parentCtx == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER ||
                parentCtx == ParserRuleContext.CLASS_MEMBER ||
                parentCtx == ParserRuleContext.OBJECT_TYPE_MEMBER ||
                parentCtx == ParserRuleContext.LISTENER_DECL || parentCtx == ParserRuleContext.CONSTANT_DECL) {
            nextContext = ParserRuleContext.SEMICOLON;
        } else if (parentCtx == ParserRuleContext.ANNOTATIONS) {
            nextContext = ParserRuleContext.ANNOTATION_END;
        } else if (parentCtx == ParserRuleContext.INTERPOLATION) {
            nextContext = ParserRuleContext.CLOSE_BRACE;
        } else if (parentCtx == ParserRuleContext.BRACED_EXPRESSION ||
                parentCtx == ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAMS) {
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
        } else if (parentCtx == ParserRuleContext.MATCH_BODY) {
            nextContext = ParserRuleContext.RIGHT_DOUBLE_ARROW;
        } else if (parentCtx == ParserRuleContext.SELECT_CLAUSE) {
            STToken nextToken = this.tokenReader.peek(lookahead);
            switch (nextToken.kind) {
                case ON_KEYWORD:
                case CONFLICT_KEYWORD:
                    nextContext = ParserRuleContext.ON_CONFLICT_CLAUSE;
                    break;
                default:
                    nextContext = ParserRuleContext.QUERY_EXPRESSION_END;
            }
        } else if (parentCtx == ParserRuleContext.JOIN_CLAUSE) {
            nextContext = ParserRuleContext.ON_CLAUSE;
        } else if (parentCtx == ParserRuleContext.ON_CLAUSE) {
            nextContext = ParserRuleContext.EQUALS_KEYWORD;
        } else if (parentCtx == ParserRuleContext.CLIENT_RESOURCE_ACCESS_ACTION) {
            nextContext = ParserRuleContext.CLOSE_BRACKET;
        } else {
            throw new IllegalStateException("seekMatchInExpressionRhs found: " + parentCtx);
        }

        alternatives = getExpressionRhsAlternatives(nextContext);

        if (allowFuncCall) {
            alternatives = modifyAlternativesWithArgListStart(alternatives);
        }
        return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, alternatives, isEntryPoint);
    }

    private ParserRuleContext[] getExpressionRhsAlternatives(ParserRuleContext nextContext) {
        if (nextContext == ParserRuleContext.SEMICOLON || nextContext == ParserRuleContext.QUERY_EXPRESSION_END ||
                nextContext == ParserRuleContext.MATCH_BODY) {
            return new ParserRuleContext[] { nextContext, ParserRuleContext.BINARY_OPERATOR,
                    ParserRuleContext.IS_KEYWORD, ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                    ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                    ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                    ParserRuleContext.RIGHT_ARROW, ParserRuleContext.SYNC_SEND_TOKEN };
        }

        return new ParserRuleContext[] { ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.IS_KEYWORD,
                ParserRuleContext.DOT, ParserRuleContext.ANNOT_CHAINING_TOKEN,
                ParserRuleContext.OPTIONAL_CHAINING_TOKEN, ParserRuleContext.CONDITIONAL_EXPRESSION,
                ParserRuleContext.XML_NAVIGATE_EXPR, ParserRuleContext.MEMBER_ACCESS_KEY_EXPR,
                ParserRuleContext.RIGHT_ARROW, ParserRuleContext.SYNC_SEND_TOKEN, nextContext };
    }

    private ParserRuleContext[] modifyAlternativesWithArgListStart(ParserRuleContext[] alternatives) {
        ParserRuleContext[] newAlternatives = new ParserRuleContext[alternatives.length + 1];
        System.arraycopy(alternatives, 0, newAlternatives, 0, alternatives.length);
        newAlternatives[alternatives.length] = ParserRuleContext.ARG_LIST_OPEN_PAREN;
        return newAlternatives;
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
            case FUNC_DEF:
                return ParserRuleContext.FUNC_DEF_START;
            case FUNC_DEF_FIRST_QUALIFIER:
                return ParserRuleContext.FUNC_DEF_WITHOUT_FIRST_QUALIFIER;
            case FUNC_TYPE_FIRST_QUALIFIER:
                return ParserRuleContext.FUNC_TYPE_DESC_START_WITHOUT_FIRST_QUAL;
            case FUNC_TYPE_SECOND_QUALIFIER:
            case FUNC_DEF_SECOND_QUALIFIER:
            case FUNC_DEF_OR_FUNC_TYPE:
                return ParserRuleContext.FUNCTION_KEYWORD;
            case ANON_FUNC_EXPRESSION:
                return ParserRuleContext.ANON_FUNC_EXPRESSION_START;
            case FUNC_TYPE_DESC:
                return ParserRuleContext.FUNC_TYPE_DESC_START;
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
                return getNextRuleForCloseParenthesis();
            case EXPRESSION:
            case BASIC_LITERAL:
                return getNextRuleForExpr();
            case FUNC_NAME:
                ParserRuleContext grandParentCtx = getGrandParentContext();
                if (grandParentCtx == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER ||
                        grandParentCtx == ParserRuleContext.CLASS_MEMBER) {
                    return ParserRuleContext.OPTIONAL_RELATIVE_PATH;
                }
                return ParserRuleContext.OPEN_PARENTHESIS;
            case OPEN_BRACE:
                return getNextRuleForOpenBrace();
            case OPEN_PARENTHESIS:
                return getNextRuleForOpenParenthesis();
            case SEMICOLON:
                return getNextRuleForSemicolon(nextLookahead);
            case SIMPLE_TYPE_DESCRIPTOR:
                return ParserRuleContext.TYPE_DESC_RHS;
            case VARIABLE_NAME:
            case PARAMETER_NAME_RHS:
                return getNextRuleForVarName();
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                return ParserRuleContext.PARAM_START;
            case REST_PARAM_RHS:
                // We reach here under required or defaultable param ctx
                switchContext(ParserRuleContext.REST_PARAM);
                return ParserRuleContext.ELLIPSIS;
            case ASSIGNMENT_STMT:
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
                return ParserRuleContext.TYPE_DESC_RHS;
            case CLOSED_RECORD_BODY_START:
                return ParserRuleContext.RECORD_FIELD_OR_RECORD_END;
            case ELLIPSIS:
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case MAPPING_CONSTRUCTOR:
                    case LIST_CONSTRUCTOR:
                    case ARG_LIST:
                        return ParserRuleContext.EXPRESSION;
                    case TYPE_DESC_IN_TUPLE:
                    case STMT_START_BRACKETED_LIST:
                    case BRACKETED_LIST:
                        return ParserRuleContext.CLOSE_BRACKET;
                    case REST_MATCH_PATTERN:
                        return ParserRuleContext.VAR_KEYWORD;
                    case RELATIVE_RESOURCE_PATH:
                        return ParserRuleContext.OPTIONAL_PATH_PARAM_NAME;
                    case CLIENT_RESOURCE_ACCESS_ACTION:
                        return ParserRuleContext.EXPRESSION;
                    default:
                        return ParserRuleContext.VARIABLE_NAME;
                }
            case QUESTION_MARK:
                return getNextRuleForQuestionMark();
            case RECORD_TYPE_DESCRIPTOR:
                return ParserRuleContext.RECORD_KEYWORD;
            case ASTERISK:
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case ARRAY_TYPE_DESCRIPTOR:
                        return ParserRuleContext.CLOSE_BRACKET;
                    case XML_ATOMIC_NAME_PATTERN:
                        endContext();
                        return ParserRuleContext.XML_NAME_PATTERN_RHS;
                    case REQUIRED_PARAM:
                    case DEFAULTABLE_PARAM:
                        return ParserRuleContext.TYPE_DESC_IN_PARAM;
                    default:
                        return ParserRuleContext.TYPE_REFERENCE_IN_TYPE_INCLUSION;
                }
            case TYPE_NAME:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_DEF;
            case OBJECT_TYPE_DESCRIPTOR:
                return ParserRuleContext.OBJECT_TYPE_START;
            case SECOND_OBJECT_CONS_QUALIFIER:
            case SECOND_OBJECT_TYPE_QUALIFIER:
                return ParserRuleContext.OBJECT_KEYWORD;
            case FIRST_OBJECT_CONS_QUALIFIER:
                return ParserRuleContext.OBJECT_CONS_WITHOUT_FIRST_QUALIFIER;
            case FIRST_OBJECT_TYPE_QUALIFIER:
                return ParserRuleContext.OBJECT_TYPE_WITHOUT_FIRST_QUALIFIER;
            case OPEN_BRACKET:
                return getNextRuleForOpenBracket();
            case CLOSE_BRACKET:
                return getNextRuleForCloseBracket();
            case DOT:
                return getNextRuleForDot();
            case METHOD_CALL_DOT:
                return ParserRuleContext.VARIABLE_NAME;
            case BLOCK_STMT:
                return ParserRuleContext.OPEN_BRACE;
            case IF_BLOCK:
                return ParserRuleContext.IF_KEYWORD;
            case WHILE_BLOCK:
                return ParserRuleContext.WHILE_KEYWORD;
            case DO_BLOCK:
                return ParserRuleContext.DO_KEYWORD;
            case CALL_STMT:
                return ParserRuleContext.CALL_STMT_START;
            case PANIC_STMT:
                return ParserRuleContext.PANIC_KEYWORD;
            case FUNC_CALL:
                // TODO: check this again
                return ParserRuleContext.IMPORT_PREFIX;
            case IMPORT_PREFIX:
            case NAMESPACE_PREFIX:
                return ParserRuleContext.SEMICOLON;
            case VERSION_NUMBER:
            case VERSION_KEYWORD:
                return ParserRuleContext.MAJOR_VERSION;
            case SLASH:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ABSOLUTE_RESOURCE_PATH) {
                    return ParserRuleContext.IDENTIFIER;
                } else if (parentCtx == ParserRuleContext.RELATIVE_RESOURCE_PATH) {
                    return ParserRuleContext.RESOURCE_PATH_SEGMENT;
                } else if (parentCtx == ParserRuleContext.CLIENT_RESOURCE_ACCESS_ACTION) {
                    return ParserRuleContext.RESOURCE_ACCESS_PATH_SEGMENT;
                }
                return ParserRuleContext.IMPORT_MODULE_NAME;
            case IMPORT_ORG_OR_MODULE_NAME:
                return ParserRuleContext.IMPORT_DECL_ORG_OR_MODULE_NAME_RHS;
            case IMPORT_MODULE_NAME:
                return ParserRuleContext.AFTER_IMPORT_MODULE_NAME;
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
            case RETURN_STMT:
                return ParserRuleContext.RETURN_KEYWORD;
            case FAIL_STATEMENT:
                return ParserRuleContext.FAIL_KEYWORD;
            case ACCESS_EXPRESSION:
                return ParserRuleContext.VARIABLE_REF;
            case MAPPING_FIELD_NAME:
                return ParserRuleContext.SPECIFIC_FIELD_RHS;
            case COLON:
                return getNextRuleForColon();
            case VAR_REF_COLON:
                startContext(ParserRuleContext.VARIABLE_REF);
                return ParserRuleContext.IDENTIFIER;
            case TYPE_REF_COLON:
                startContext(ParserRuleContext.VAR_DECL_STMT);
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                return ParserRuleContext.IDENTIFIER;
            case STRING_LITERAL_TOKEN:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.SERVICE_DECL) {
                    return ParserRuleContext.ON_KEYWORD;
                }
                return ParserRuleContext.COLON; // mapping constructor key
            case COMPUTED_FIELD_NAME:
                return ParserRuleContext.OPEN_BRACKET;
            case LISTENERS_LIST:
                return ParserRuleContext.EXPRESSION;
            case SERVICE_DECL:
                return ParserRuleContext.SERVICE_DECL_START;
            case SERVICE_DECL_QUALIFIER:
                return ParserRuleContext.SERVICE_KEYWORD;
            case LISTENER_DECL:
                return ParserRuleContext.LISTENER_KEYWORD;
            case CONSTANT_DECL:
                return ParserRuleContext.CONST_KEYWORD;
            case TYPEOF_EXPRESSION:
                return ParserRuleContext.TYPEOF_KEYWORD;
            case OPTIONAL_TYPE_DESCRIPTOR:
                return ParserRuleContext.QUESTION_MARK;
            case UNARY_EXPRESSION:
                return ParserRuleContext.UNARY_OPERATOR;
            case UNARY_OPERATOR:
                return ParserRuleContext.EXPRESSION;
            case ARRAY_TYPE_DESCRIPTOR:
                return ParserRuleContext.OPEN_BRACKET;
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
            case TYPE_REFERENCE_IN_TYPE_INCLUSION:
            case ANNOT_REFERENCE:
            case FIELD_ACCESS_IDENTIFIER:
                return ParserRuleContext.QUALIFIED_IDENTIFIER_START_IDENTIFIER;
            case QUALIFIED_IDENTIFIER_START_IDENTIFIER:
            case XML_ATOMIC_NAME_IDENTIFIER:
                nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.COLON_TOKEN) {
                    return ParserRuleContext.COLON;
                }
                // Else this is a simple identifier. Hence fall through.
            case IDENTIFIER:
            case SIMPLE_TYPE_DESC_IDENTIFIER:
                return getNextRuleForIdentifier();
            case QUALIFIED_IDENTIFIER_PREDECLARED_PREFIX:
                return ParserRuleContext.COLON;
            case PATH_SEGMENT_IDENT:
                return ParserRuleContext.RELATIVE_RESOURCE_PATH_END;
            case NIL_LITERAL:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case LOCAL_TYPE_DEFINITION_STMT:
                return ParserRuleContext.TYPE_KEYWORD;
            case RIGHT_ARROW:
                return ParserRuleContext.EXPRESSION;
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
                return getNextRuleForDecimalIntegerLiteral();
            case EXPRESSION_STATEMENT:
                return ParserRuleContext.EXPRESSION_STATEMENT_START;
            case LOCK_STMT:
                return ParserRuleContext.LOCK_KEYWORD;
            case LOCK_KEYWORD:
                return ParserRuleContext.BLOCK_STMT;
            case RECORD_FIELD:
                return ParserRuleContext.RECORD_FIELD_START;
            case ANNOTATION_TAG:
                return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
            case ANNOT_ATTACH_POINTS_LIST:
                return ParserRuleContext.ATTACH_POINT;
            case FIELD_IDENT:
            case FUNCTION_IDENT:
            case IDENT_AFTER_OBJECT_IDENT:
            case SINGLE_KEYWORD_ATTACH_POINT_IDENT:
                return ParserRuleContext.ATTACH_POINT_END;
            case OBJECT_IDENT:
                return ParserRuleContext.IDENT_AFTER_OBJECT_IDENT;
            case RECORD_IDENT:
                return ParserRuleContext.FIELD_IDENT;
            case SERVICE_IDENT:
                return ParserRuleContext.SERVICE_IDENT_RHS;
            case REMOTE_IDENT:
                return ParserRuleContext.FUNCTION_IDENT;
            case ANNOTATION_DECL:
                return ParserRuleContext.ANNOTATION_DECL_START;
            case XML_NAMESPACE_DECLARATION:
                return ParserRuleContext.XMLNS_KEYWORD;
            case CONSTANT_EXPRESSION:
                return ParserRuleContext.CONSTANT_EXPRESSION_START;
            case NAMED_WORKER_DECL:
                return ParserRuleContext.NAMED_WORKER_DECL_START;
            case WORKER_NAME:
                return ParserRuleContext.WORKER_NAME_RHS;
            case FORK_STMT:
                return ParserRuleContext.FORK_KEYWORD;
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
            case OBJECT_CONSTRUCTOR:
                return ParserRuleContext.OBJECT_CONSTRUCTOR_START;
            case ABSOLUTE_RESOURCE_PATH:
                return ParserRuleContext.ABSOLUTE_RESOURCE_PATH_START;
            case ABSOLUTE_PATH_SINGLE_SLASH:
                return ParserRuleContext.SERVICE_DECL_RHS;
            case SERVICE_DECL_RHS:
                endContext(); // end absolute-resource-path
                return ParserRuleContext.ON_KEYWORD;
            case OBJECT_CONSTRUCTOR_BLOCK:
                endContext(); // end listeners-list
                return ParserRuleContext.OPEN_BRACE;
            case SERVICE_VAR_DECL_RHS:
                switchContext(ParserRuleContext.VAR_DECL_STMT);
                return ParserRuleContext.TYPED_BINDING_PATTERN_TYPE_RHS;
            case RELATIVE_RESOURCE_PATH:
                return ParserRuleContext.RELATIVE_RESOURCE_PATH_START;
            case RESOURCE_PATH_PARAM:
                return ParserRuleContext.OPEN_BRACKET;
            case RESOURCE_ACCESSOR_DEF_OR_DECL_RHS:
                endContext(); // end relative-resource-path
                return ParserRuleContext.OPEN_PARENTHESIS;
            case ERROR_CONSTRUCTOR:
                return ParserRuleContext.ERROR_KEYWORD;
            case ERROR_CONS_ERROR_KEYWORD_RHS:
                // We reach here without starting the error constructor context, hence start the context.
                startContext(ParserRuleContext.ERROR_CONSTRUCTOR);
                return ParserRuleContext.ERROR_CONSTRUCTOR_RHS;
            case LIST_BINDING_PATTERN_RHS:
                return getNextRuleForBindingPattern();
            default:
                return getNextRuleInternal(currentCtx, nextLookahead);
        }
    }

    private ParserRuleContext getNextRuleInternal(ParserRuleContext currentCtx, int nextLookahead) {
        ParserRuleContext parentCtx;
        ParserRuleContext grandParentCtx;
        switch (currentCtx) {
            case LIST_CONSTRUCTOR:
                return ParserRuleContext.OPEN_BRACKET;
            case FOREACH_STMT:
                return ParserRuleContext.FOREACH_KEYWORD;
            case TYPE_CAST:
                return ParserRuleContext.LT;
            case PIPE:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ALTERNATE_WAIT_EXPRS) {
                    return ParserRuleContext.EXPRESSION;
                } else if (parentCtx == ParserRuleContext.XML_NAME_PATTERN) {
                    return ParserRuleContext.XML_ATOMIC_NAME_PATTERN;
                } else if (parentCtx == ParserRuleContext.MATCH_PATTERN) {
                    return ParserRuleContext.MATCH_PATTERN_START;
                }
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case TABLE_CONSTRUCTOR:
                return ParserRuleContext.OPEN_BRACKET;
            case KEY_SPECIFIER:
                return ParserRuleContext.KEY_KEYWORD;
            case LET_EXPRESSION:
                return ParserRuleContext.LET_KEYWORD;
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
                return ParserRuleContext.LET_VAR_DECL_START;
            case ORDER_KEY_LIST:
                return ParserRuleContext.EXPRESSION;
            case END_OF_TYPE_DESC:
                return getNextRuleForTypeDescriptor();
            case TYPED_BINDING_PATTERN:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case BINDING_PATTERN_STARTING_IDENTIFIER:
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
            case LT:
                return getNextRuleForLt();
            case INFERRED_TYPEDESC_DEFAULT_START_LT:
                return ParserRuleContext.INFERRED_TYPEDESC_DEFAULT_END_GT;
            case GT:
                return getNextRuleForGt();
            case STREAM_TYPE_PARAM_START_TOKEN:
                return ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC;
            case INFERRED_TYPEDESC_DEFAULT_END_GT:
                return ParserRuleContext.END_OF_PARAMS_OR_NEXT_PARAM_START;
            case TYPE_CAST_PARAM_START:
                startContext(ParserRuleContext.TYPE_CAST);
                return ParserRuleContext.TYPE_CAST_PARAM;
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
            case ARG_LIST_OPEN_PAREN:
                return ParserRuleContext.ARG_LIST;
            case ARG_LIST_END:
                endContext(); // end arg-list
                return ParserRuleContext.ARG_LIST_CLOSE_PAREN;
            case ARG_LIST_CLOSE_PAREN:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ERROR_CONSTRUCTOR) {
                    endContext();
                } else if (parentCtx == ParserRuleContext.CLIENT_RESOURCE_ACCESS_ACTION) {
                    return ParserRuleContext.ACTION_END;
                }
                return ParserRuleContext.EXPRESSION_RHS;
            case ARG_LIST:
                return ParserRuleContext.ARG_START_OR_ARG_LIST_END;
            case QUERY_EXPRESSION_END:
                endContext(); // end select, on-conflict, limit or do-clause ctx
                endContext(); // end query-expr ctx
                return ParserRuleContext.EXPRESSION_RHS;
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
            case TYPE_DESC_IN_TUPLE:
            case TYPE_DESC_IN_SERVICE:
            case TYPE_DESC_IN_PATH_PARAM:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case CLASS_DESCRIPTOR_IN_NEW_EXPR:
                return ParserRuleContext.CLASS_DESCRIPTOR;
            case VAR_DECL_STARTED_WITH_DENTIFIER:
            case TYPE_DESC_RHS_IN_TYPED_BP:
                // We come here trying to recover statement started with identifier,
                // and trying to match it against a var-decl. Or typed binding pattern starts
                // with array type descriptor.
                // originally, a context for type hasn't started yet. Therefore start a
                // a context manually here.
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                return ParserRuleContext.TYPE_DESC_RHS;
            case ROW_TYPE_PARAM:
                return ParserRuleContext.LT;
            case PARENTHESISED_TYPE_DESC_START:
                return ParserRuleContext.TYPE_DESC_IN_PARENTHESIS;
            case SELECT_CLAUSE:
                return ParserRuleContext.SELECT_KEYWORD;
            case WHERE_CLAUSE:
                return ParserRuleContext.WHERE_KEYWORD;
            case FROM_CLAUSE:
                return ParserRuleContext.FROM_KEYWORD;
            case LET_CLAUSE:
                return ParserRuleContext.LET_KEYWORD;
            case ORDER_BY_CLAUSE:
                return ParserRuleContext.ORDER_KEYWORD;
            case ON_CONFLICT_CLAUSE:
                return ParserRuleContext.ON_KEYWORD;
            case LIMIT_CLAUSE:
                return ParserRuleContext.LIMIT_KEYWORD;
            case JOIN_CLAUSE:
                return ParserRuleContext.JOIN_CLAUSE_START;
            case ON_CLAUSE:
                // We assume on-clause is only used in join-clause
                return ParserRuleContext.ON_KEYWORD;
            case QUERY_EXPRESSION:
                return ParserRuleContext.FROM_CLAUSE;
            case QUERY_CONSTRUCT_TYPE_RHS:
                startContext(ParserRuleContext.QUERY_EXPRESSION);
                return ParserRuleContext.FROM_CLAUSE;
            case EXPRESSION_START_TABLE_KEYWORD_RHS:
                startContext(ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION);
                return ParserRuleContext.TABLE_KEYWORD_RHS;
            case QUERY_EXPRESSION_RHS:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext();
                }
                return ParserRuleContext.SELECT_CLAUSE;
            case INTERMEDIATE_CLAUSE:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext();
                }
                return ParserRuleContext.INTERMEDIATE_CLAUSE_START;
            case QUERY_ACTION_RHS:
                return ParserRuleContext.DO_CLAUSE;
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
                return ParserRuleContext.TYPE_DESC_RHS;
            case STMT_LEVEL_AMBIGUOUS_FUNC_TYPE_DESC_RHS:
                endContext();
                // We come here trying to recover statement started with function-keyword,
                // and trying to match it against a var-decl. Or function keyword inside statement startambiguous
                // bracketed list.

                // If it is the first case, since this wasn't a var-decl
                // originally, a context for type hasn't started yet. Therefore switch to var-decl context.
                // If it is the second case just return TYPEDESC_RHS
                if (!isInTypeDescContext()) {
                    switchContext(ParserRuleContext.VAR_DECL_STMT);
                    startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                }
                return ParserRuleContext.TYPE_DESC_RHS;
            case FUNC_TYPE_DESC_END:
                endContext();
                return ParserRuleContext.TYPE_DESC_RHS;
            case BRACED_EXPR_OR_ANON_FUNC_PARAMS:
                return ParserRuleContext.IMPLICIT_ANON_FUNC_PARAM;
            case IMPLICIT_ANON_FUNC_PARAM:
                return ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS;
            case EXPLICIT_ANON_FUNC_EXPR_BODY_START:
                endContext(); // end explicit anon-func
                return ParserRuleContext.EXPR_FUNC_BODY_START;
            case OBJECT_CONSTRUCTOR_MEMBER:
                return ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER_START;
            case CLASS_MEMBER:
            case OBJECT_TYPE_MEMBER:
                return ParserRuleContext.CLASS_MEMBER_OR_OBJECT_MEMBER_START;
            case ANNOTATION_END:
                return getNextRuleForAnnotationEnd(nextLookahead);
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return ParserRuleContext.SIGNED_INT_OR_FLOAT_RHS;
            case SIGNED_INT_OR_FLOAT_RHS:
                return getNextRuleForExpr();
            case TUPLE_TYPE_DESC_START:
                return ParserRuleContext.TYPE_DESC_IN_TUPLE;
            case METHOD_NAME:
                return ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST;
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
            case LET_CLAUSE_END:
                parentCtx = getParentContext();
                // We can reach here without `LET_CLAUSE_LET_VAR_DECL` ctx for empty let var declarations
                if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext();
                    return ParserRuleContext.QUERY_PIPELINE_RHS;
                }
                return ParserRuleContext.QUERY_PIPELINE_RHS;
            case ORDER_CLAUSE_END:
            case JOIN_CLAUSE_END:
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
            case MODULE_ENUM_DECLARATION:
                return ParserRuleContext.ENUM_KEYWORD;
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
            case MATCH_STMT:
                return ParserRuleContext.MATCH_KEYWORD;
            case MATCH_BODY:
                return ParserRuleContext.OPEN_BRACE;
            case MATCH_PATTERN:
                return ParserRuleContext.MATCH_PATTERN_START;
            case MATCH_PATTERN_END:
                endContext(); // End match pattern context
                return getNextRuleForMatchPattern();
            case MATCH_PATTERN_RHS:
                return getNextRuleForMatchPattern();
            case RIGHT_DOUBLE_ARROW:
                // Assumption: RIGHT_DOUBLE_ARROW is only occurs in match clauses
                // in expr-func-body, it is used by a different alias.
                return ParserRuleContext.BLOCK_STMT;
            case LIST_MATCH_PATTERN:
                return ParserRuleContext.OPEN_BRACKET;
            case REST_MATCH_PATTERN:
                return ParserRuleContext.ELLIPSIS;
            case ERROR_BINDING_PATTERN:
                return ParserRuleContext.ERROR_KEYWORD;
            case SIMPLE_BINDING_PATTERN:
                return ParserRuleContext.ERROR_MESSAGE_BINDING_PATTERN_END;
            case ERROR_MESSAGE_BINDING_PATTERN_END_COMMA:
                return ParserRuleContext.ERROR_MESSAGE_BINDING_PATTERN_RHS;
            case ERROR_CAUSE_SIMPLE_BINDING_PATTERN:
                return ParserRuleContext.ERROR_FIELD_BINDING_PATTERN_END;
            case NAMED_ARG_BINDING_PATTERN:
                return ParserRuleContext.ASSIGN_OP;
            case MAPPING_MATCH_PATTERN:
                return ParserRuleContext.OPEN_BRACE;
            case ERROR_MATCH_PATTERN:
                return ParserRuleContext.ERROR_KEYWORD;
            case ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG:
                return ParserRuleContext.ERROR_ARG_LIST_MATCH_PATTERN_START;
            case ERROR_MESSAGE_MATCH_PATTERN_END_COMMA:
                return ParserRuleContext.ERROR_MESSAGE_MATCH_PATTERN_RHS;
            case ERROR_CAUSE_MATCH_PATTERN:
                return ParserRuleContext.ERROR_FIELD_MATCH_PATTERN_RHS;
            case NAMED_ARG_MATCH_PATTERN:
                return ParserRuleContext.IDENTIFIER;
            case MODULE_CLASS_DEFINITION:
                return ParserRuleContext.MODULE_CLASS_DEFINITION_START;
            case FIRST_CLASS_TYPE_QUALIFIER:
                return ParserRuleContext.CLASS_DEF_WITHOUT_FIRST_QUALIFIER;
            case SECOND_CLASS_TYPE_QUALIFIER:
                return ParserRuleContext.CLASS_DEF_WITHOUT_SECOND_QUALIFIER;
            case THIRD_CLASS_TYPE_QUALIFIER:
                return ParserRuleContext.CLASS_DEF_WITHOUT_THIRD_QUALIFIER;
            case FOURTH_CLASS_TYPE_QUALIFIER:
                return ParserRuleContext.CLASS_KEYWORD;
            case CLASS_KEYWORD:
                return ParserRuleContext.CLASS_NAME;
            case CLASS_NAME:
                return ParserRuleContext.OPEN_BRACE;
            case OBJECT_MEMBER_VISIBILITY_QUAL:
                return ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY;
            case OBJECT_FIELD_START:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.OBJECT_TYPE_MEMBER) {
                    return ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER;
                }
                return ParserRuleContext.OBJECT_FIELD_QUALIFIER;
            case ON_FAIL_CLAUSE:
                return ParserRuleContext.ON_KEYWORD;
            case OBJECT_FIELD_RHS:
                grandParentCtx = getGrandParentContext();
                if (grandParentCtx == ParserRuleContext.OBJECT_TYPE_DESCRIPTOR) {
                    return ParserRuleContext.SEMICOLON;
                } else {
                    return ParserRuleContext.OPTIONAL_FIELD_INITIALIZER;
                }
            case OBJECT_METHOD_FIRST_QUALIFIER:
                return ParserRuleContext.OBJECT_METHOD_WITHOUT_FIRST_QUALIFIER;
            case OBJECT_METHOD_SECOND_QUALIFIER:
                return ParserRuleContext.OBJECT_METHOD_WITHOUT_SECOND_QUALIFIER;
            case OBJECT_METHOD_THIRD_QUALIFIER:
                return ParserRuleContext.OBJECT_METHOD_WITHOUT_THIRD_QUALIFIER;
            case OBJECT_METHOD_FOURTH_QUALIFIER:
                return ParserRuleContext.FUNC_DEF;
            case MODULE_VAR_DECL:
                return ParserRuleContext.MODULE_VAR_DECL_START;
            case MODULE_VAR_FIRST_QUAL:
                return ParserRuleContext.MODULE_VAR_WITHOUT_FIRST_QUAL;
            case MODULE_VAR_SECOND_QUAL:
                return ParserRuleContext.MODULE_VAR_WITHOUT_SECOND_QUAL;
            case MODULE_VAR_THIRD_QUAL:
                return ParserRuleContext.VAR_DECL_STMT;
            case PARAMETERIZED_TYPE:
                return ParserRuleContext.OPTIONAL_TYPE_PARAMETER;
            case MAP_TYPE_DESCRIPTOR:
                return ParserRuleContext.MAP_KEYWORD;
            case FUNC_TYPE_FUNC_KEYWORD_RHS:
                return getNextRuleForFuncTypeFuncKeywordRhs();
            case TRANSACTION_STMT_TRANSACTION_KEYWORD_RHS:
                // We reach here without starting the `TRANSACTION_STMT` context. hence, start context
                startContext(ParserRuleContext.TRANSACTION_STMT);
                return ParserRuleContext.BLOCK_STMT;
            case BRACED_EXPRESSION:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case ARRAY_LENGTH_START:
                switchContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                startContext(ParserRuleContext.ARRAY_TYPE_DESCRIPTOR);
                return ParserRuleContext.ARRAY_LENGTH;
            case RESOURCE_METHOD_CALL_SLASH_TOKEN:
                return ParserRuleContext.CLIENT_RESOURCE_ACCESS_ACTION;
            case CLIENT_RESOURCE_ACCESS_ACTION:
                return ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_PATH;
            case ACTION_END:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.CLIENT_RESOURCE_ACCESS_ACTION) {
                    endContext();
                }
                return getNextRuleForAction();
            default:
                return getNextRuleForKeywords(currentCtx, nextLookahead);
        }
    }

    private ParserRuleContext getNextRuleForKeywords(ParserRuleContext currentCtx, int nextLookahead) {
        ParserRuleContext parentCtx;
        switch (currentCtx) {
            case PUBLIC_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.OBJECT_TYPE_DESCRIPTOR ||
                        parentCtx == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER ||
                        parentCtx == ParserRuleContext.CLASS_MEMBER ||
                        parentCtx == ParserRuleContext.OBJECT_TYPE_MEMBER) {
                    return ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY;
                } else if (isParameter(parentCtx)) {
                    return ParserRuleContext.TYPE_DESC_IN_PARAM;
                }
                return ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_MODIFIER;
            case PRIVATE_KEYWORD:
                return ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY;
            case ON_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ANNOTATION_DECL) {
                    return ParserRuleContext.ANNOT_ATTACH_POINTS_LIST;
                } else if (parentCtx == ParserRuleContext.ON_CONFLICT_CLAUSE) {
                    return ParserRuleContext.CONFLICT_KEYWORD;
                } else if (parentCtx == ParserRuleContext.ON_CLAUSE) {
                    return ParserRuleContext.EXPRESSION;
                } else if (parentCtx == ParserRuleContext.ON_FAIL_CLAUSE) {
                    return ParserRuleContext.FAIL_KEYWORD;
                }
                return ParserRuleContext.LISTENERS_LIST;
            case SERVICE_KEYWORD:
                return ParserRuleContext.OPTIONAL_SERVICE_DECL_TYPE;
            case LISTENER_KEYWORD:
                return ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER;
            case FINAL_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER ||
                        parentCtx == ParserRuleContext.CLASS_MEMBER) {
                    return ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER;
                }
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case CONST_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ANNOTATION_DECL) {
                    return ParserRuleContext.ANNOTATION_KEYWORD;
                }
                return ParserRuleContext.CONST_DECL_TYPE;
            case TYPEOF_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case IS_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_EXPRESSION;
            case NULL_KEYWORD:
                return ParserRuleContext.EXPRESSION_RHS;
            case ANNOTATION_KEYWORD:
                return ParserRuleContext.ANNOT_DECL_OPTIONAL_TYPE;
            case SOURCE_KEYWORD:
                return ParserRuleContext.ATTACH_POINT_IDENT;
            case XMLNS_KEYWORD:
                return ParserRuleContext.CONSTANT_EXPRESSION;
            case WORKER_KEYWORD:
                return ParserRuleContext.WORKER_NAME;
            case IF_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case ELSE_KEYWORD:
                return ParserRuleContext.ELSE_BODY;
            case WHILE_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case CHECKING_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case FAIL_KEYWORD:
                if (getParentContext() == ParserRuleContext.ON_FAIL_CLAUSE) {
                    return ParserRuleContext.ON_FAIL_OPTIONAL_BINDING_PATTERN;
                }
                return ParserRuleContext.EXPRESSION;
            case PANIC_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case IMPORT_KEYWORD:
                return ParserRuleContext.IMPORT_ORG_OR_MODULE_NAME;
            case AS_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.IMPORT_DECL) {
                    return ParserRuleContext.IMPORT_PREFIX;
                } else if (parentCtx == ParserRuleContext.XML_NAMESPACE_DECLARATION) {
                    return ParserRuleContext.NAMESPACE_PREFIX;
                }
                throw new IllegalStateException("next rule of as keyword found: " + parentCtx);
            case CONTINUE_KEYWORD:
            case BREAK_KEYWORD:
                return ParserRuleContext.SEMICOLON;
            case RETURN_KEYWORD:
                return ParserRuleContext.RETURN_STMT_RHS;
            case EXTERNAL_KEYWORD:
                return ParserRuleContext.SEMICOLON;
            case FUNCTION_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ANON_FUNC_EXPRESSION) {
                    return ParserRuleContext.OPEN_PARENTHESIS;
                } else if (parentCtx == ParserRuleContext.FUNC_TYPE_DESC) {
                    return ParserRuleContext.FUNC_TYPE_FUNC_KEYWORD_RHS_START;
                } else if (parentCtx == ParserRuleContext.FUNC_DEF) {
                    return ParserRuleContext.FUNC_NAME;
                }
                return ParserRuleContext.FUNCTION_KEYWORD_RHS;
            case RETURNS_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_RETURN_TYPE_DESC;
            case RECORD_KEYWORD:
                return ParserRuleContext.RECORD_BODY_START;
            case TYPE_KEYWORD:
                return ParserRuleContext.TYPE_NAME;
            case OBJECT_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.OBJECT_CONSTRUCTOR) {
                    return ParserRuleContext.OBJECT_CONSTRUCTOR_TYPE_REF;
                }
                return ParserRuleContext.OPEN_BRACE;
            case OBJECT_TYPE_OBJECT_KEYWORD_RHS:
                // If we reach here context for object type desc is not started yet, hence start context.
                startContext(ParserRuleContext.OBJECT_TYPE_DESCRIPTOR);
                return ParserRuleContext.OPEN_BRACE;
            case ABSTRACT_KEYWORD:
            case CLIENT_KEYWORD:
                return ParserRuleContext.OBJECT_KEYWORD;
            case FORK_KEYWORD:
                return ParserRuleContext.OPEN_BRACE;
            case TRAP_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case FOREACH_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case IN_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_EXPR_LET_VAR_DECL) {
                    endContext(); // end let-expr-let-var-decl
                }
                return ParserRuleContext.EXPRESSION;
            case KEY_KEYWORD:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.KEY_CONSTRAINTS_RHS;
                }
                return ParserRuleContext.OPEN_PARENTHESIS;
            case ERROR_KEYWORD:
                return getNextRuleForErrorKeyword();
            case LET_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.QUERY_EXPRESSION) {
                    STToken nextToken = this.tokenReader.peek(nextLookahead);
                    STToken nextNextToken = this.tokenReader.peek(nextLookahead + 1);
                    // This is a special case since parser quit parsing let clauses if 
                    // `isEndOfLetVarDeclarations()` is true
                    if (BallerinaParser.isEndOfLetVarDeclarations(nextToken.kind, nextNextToken)) {
                        return ParserRuleContext.LET_CLAUSE_END;
                    }
                    return ParserRuleContext.LET_CLAUSE_LET_VAR_DECL;
                } else if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext(); // end let-clause-let-var-decl
                    return ParserRuleContext.LET_CLAUSE_LET_VAR_DECL;
                }
                return ParserRuleContext.LET_EXPR_LET_VAR_DECL;
            case TABLE_KEYWORD:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.ROW_TYPE_PARAM;
                }
                return ParserRuleContext.TABLE_KEYWORD_RHS;
            case STREAM_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION) {
                    return ParserRuleContext.QUERY_EXPRESSION;
                }
                return ParserRuleContext.STREAM_TYPE_PARAM_START_TOKEN;
            case NEW_KEYWORD:
                return ParserRuleContext.NEW_KEYWORD_RHS;
            case XML_KEYWORD:
            case RE_KEYWORD:
            case STRING_KEYWORD:
            case BASE16_KEYWORD:
            case BASE64_KEYWORD:
                return ParserRuleContext.TEMPLATE_START;
            case SELECT_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case WHERE_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext(); // end let-clause-let-var-decl
                }
                return ParserRuleContext.EXPRESSION;
            case ORDER_KEYWORD:
                return ParserRuleContext.BY_KEYWORD;
            case BY_KEYWORD:
                return ParserRuleContext.ORDER_KEY_LIST;
            case ORDER_DIRECTION:
                return ParserRuleContext.ORDER_KEY_LIST_END;
            case FROM_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_CLAUSE_LET_VAR_DECL) {
                    endContext(); // end let-clause-let-var-decl
                }
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case JOIN_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case START_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case FLUSH_KEYWORD:
                return ParserRuleContext.OPTIONAL_PEER_WORKER;
            case PEER_WORKER_NAME:
                if (getParentContext() == ParserRuleContext.MULTI_RECEIVE_WORKERS) {
                    return ParserRuleContext.RECEIVE_FIELD_END;
                }
                return ParserRuleContext.EXPRESSION_RHS;
            case WAIT_KEYWORD:
                return ParserRuleContext.WAIT_KEYWORD_RHS;
            case DO_KEYWORD:
            case TRANSACTION_KEYWORD:
                return ParserRuleContext.BLOCK_STMT;
            case COMMIT_KEYWORD:
                return ParserRuleContext.EXPRESSION_RHS;
            case ROLLBACK_KEYWORD:
                return ParserRuleContext.ROLLBACK_RHS;
            case RETRY_KEYWORD:
                return ParserRuleContext.RETRY_KEYWORD_RHS;
            case TRANSACTIONAL_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.NAMED_WORKER_DECL) {
                    return ParserRuleContext.WORKER_KEYWORD;
                }
                // Assume we only reach here for transactional-expr
                return ParserRuleContext.EXPRESSION_RHS;
            case ENUM_KEYWORD:
                return ParserRuleContext.MODULE_ENUM_NAME;
            case MATCH_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case READONLY_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.MAPPING_CONSTRUCTOR ||
                        parentCtx == ParserRuleContext.MAPPING_BP_OR_MAPPING_CONSTRUCTOR ||
                        parentCtx == ParserRuleContext.MAPPING_FIELD) {
                    return ParserRuleContext.SPECIFIC_FIELD;
                }
                throw new IllegalStateException("next rule of readonly keyword found: " + currentCtx);
            case DISTINCT_KEYWORD:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case VAR_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.REST_MATCH_PATTERN ||
                        parentCtx == ParserRuleContext.ERROR_MATCH_PATTERN ||
                        parentCtx == ParserRuleContext.ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG) {
                    return ParserRuleContext.VARIABLE_NAME;
                }
                return ParserRuleContext.BINDING_PATTERN;
            case EQUALS_KEYWORD:
                assert getParentContext() == ParserRuleContext.ON_CLAUSE;
                endContext(); // end on-clause
                return ParserRuleContext.EXPRESSION;
            case CONFLICT_KEYWORD:
                endContext(); // end on-conflict-clause
                return ParserRuleContext.EXPRESSION;
            case LIMIT_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case OUTER_KEYWORD:
                return ParserRuleContext.JOIN_KEYWORD;
            case MAP_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION) {
                    return ParserRuleContext.QUERY_EXPRESSION;
                }
                return ParserRuleContext.LT;
            default:
                throw new IllegalStateException("getNextRuleForKeywords found: " + currentCtx);
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
            case FAIL_STATEMENT:
            case COMPUTED_FIELD_NAME:
            case LISTENERS_LIST:
            case SERVICE_DECL:
            case LISTENER_DECL:
            case CONSTANT_DECL:
            case OPTIONAL_TYPE_DESCRIPTOR:
            case ARRAY_TYPE_DESCRIPTOR:
            case ANNOTATIONS:
            case VARIABLE_REF:
            case TYPE_REFERENCE_IN_TYPE_INCLUSION:
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
            case ORDER_KEY_LIST:
            case ROW_TYPE_PARAM:
            case TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION:
            case OBJECT_CONSTRUCTOR_MEMBER:
            case CLASS_MEMBER:
            case OBJECT_TYPE_MEMBER:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case TYPED_BINDING_PATTERN:
            case BINDING_PATTERN_STARTING_IDENTIFIER:
            case MULTI_RECEIVE_WORKERS:
            case MULTI_WAIT_FIELDS:
            case ALTERNATE_WAIT_EXPRS:
            case DO_CLAUSE:
            case MEMBER_ACCESS_KEY_EXPR:
            case CONDITIONAL_EXPRESSION:
            case DO_BLOCK:
            case TRANSACTION_STMT:
            case RETRY_STMT:
            case ROLLBACK_STMT:
            case MODULE_ENUM_DECLARATION:
            case ENUM_MEMBER_LIST:
            case XML_NAME_PATTERN:
            case XML_ATOMIC_NAME_PATTERN:
            case MATCH_STMT:
            case MATCH_BODY:
            case MATCH_PATTERN:
            case LIST_MATCH_PATTERN:
            case REST_MATCH_PATTERN:
            case ERROR_BINDING_PATTERN:
            case MAPPING_MATCH_PATTERN:
            case ERROR_MATCH_PATTERN:
            case ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG:
            case NAMED_ARG_MATCH_PATTERN:
            case SELECT_CLAUSE:
            case JOIN_CLAUSE:
            case ON_FAIL_CLAUSE:
            case BRACED_EXPR_OR_ANON_FUNC_PARAMS:
            case MODULE_CLASS_DEFINITION:
            case OBJECT_CONSTRUCTOR:
            case ABSOLUTE_RESOURCE_PATH:
            case RELATIVE_RESOURCE_PATH:
            case ERROR_CONSTRUCTOR:
            case CLASS_DESCRIPTOR_IN_NEW_EXPR:
            case BRACED_EXPRESSION:
            case CLIENT_RESOURCE_ACCESS_ACTION:    

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
            case TYPE_DESC_IN_TUPLE:
            case TYPE_DESC_IN_SERVICE:
            case TYPE_DESC_IN_PATH_PARAM:
                startContext(currentCtx);
                break;
            default:
                break;
        }

        switch (currentCtx) {
            case TABLE_CONSTRUCTOR:
            case QUERY_EXPRESSION:
            case ON_CONFLICT_CLAUSE:
            case ON_CLAUSE:
                switchContext(currentCtx);
                break;
            default:
                break;
        }
    }

    private ParserRuleContext getNextRuleForCloseParenthesis() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.PARAM_LIST) {
            endContext(); // end parameters
            return ParserRuleContext.FUNC_OPTIONAL_RETURNS;
        } else if (isParameter(parentCtx)) {
            endContext(); // end parameters
            endContext(); // end parameter
            return ParserRuleContext.FUNC_OPTIONAL_RETURNS;
        } else if (parentCtx == ParserRuleContext.NIL_LITERAL) {
            endContext();
            return getNextRuleForExpr();
        } else if (parentCtx == ParserRuleContext.KEY_SPECIFIER) {
            endContext(); // end key-specifier
            if (isInTypeDescContext()) {
                return ParserRuleContext.TYPE_DESC_RHS;
            }
            return ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_RHS;
        } else if (parentCtx == ParserRuleContext.TYPE_DESC_IN_PARENTHESIS) {
            endContext();
            return ParserRuleContext.TYPE_DESC_RHS;
        } else if (isInTypeDescContext()) {
            return ParserRuleContext.TYPE_DESC_RHS;
        } else if (parentCtx == ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAMS) {
            endContext(); // end infered-param/parenthesised-expr context
            return ParserRuleContext.INFER_PARAM_END_OR_PARENTHESIS_END;
        } else if (parentCtx == ParserRuleContext.BRACED_EXPRESSION) {
            endContext();
            return ParserRuleContext.EXPRESSION_RHS;
        } else if (parentCtx == ParserRuleContext.ERROR_MATCH_PATTERN) {
            endContext();
            return getNextRuleForMatchPattern();
        } else if (parentCtx == ParserRuleContext.NAMED_ARG_MATCH_PATTERN) {
            endContext(); // end named arg math pattern context
            endContext(); // end error match pattern context
            return getNextRuleForMatchPattern();
        } else if (parentCtx == ParserRuleContext.ERROR_BINDING_PATTERN) {
            endContext(); // end error-binding-pattern
            return getNextRuleForBindingPattern();
        } else if (parentCtx == ParserRuleContext.ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG) {
            endContext();
            endContext(); // end error-binding-pattern
            return getNextRuleForBindingPattern();
        }
        return ParserRuleContext.EXPRESSION_RHS;
    }

    private ParserRuleContext getNextRuleForOpenParenthesis() {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.EXPRESSION_STATEMENT) {
            return ParserRuleContext.EXPRESSION_STATEMENT_START;
        } else if (isStatement(parentCtx) || isExpressionContext(parentCtx) ||
                parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR ||
                parentCtx == ParserRuleContext.BRACED_EXPRESSION) {
            return ParserRuleContext.EXPRESSION;
        } else if (parentCtx == ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE ||
                parentCtx == ParserRuleContext.FUNC_TYPE_DESC || parentCtx == ParserRuleContext.FUNC_DEF ||
                parentCtx == ParserRuleContext.ANON_FUNC_EXPRESSION ||
                parentCtx == ParserRuleContext.FUNC_TYPE_DESC_OR_ANON_FUNC) {
            // TODO: find a better way
            startContext(ParserRuleContext.PARAM_LIST);
            return ParserRuleContext.PARAM_LIST;
        } else if (parentCtx == ParserRuleContext.NIL_LITERAL) {
            return ParserRuleContext.CLOSE_PARENTHESIS;
        } else if (parentCtx == ParserRuleContext.KEY_SPECIFIER) {
            return ParserRuleContext.KEY_SPECIFIER_RHS;
        } else if (isInTypeDescContext()) {
            // if the parent context is table type desc then we are in key specifier context.hence start context
            startContext(ParserRuleContext.KEY_SPECIFIER);
            return ParserRuleContext.KEY_SPECIFIER_RHS;
        } else if (isParameter(parentCtx)) {
            return ParserRuleContext.EXPRESSION;
        } else if (parentCtx == ParserRuleContext.ERROR_MATCH_PATTERN) {
            return ParserRuleContext.ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG;
        } else if (isInMatchPatternCtx(parentCtx)) {
            // This is a special case which occurs because of ERROR_MATCH_PATTERN_OR_CONST_PATTERN context,
            // If this is the case we are in a error match pattern but the context is not started, hence
            // start the context.
            startContext(ParserRuleContext.ERROR_MATCH_PATTERN);
            return ParserRuleContext.ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG;
        } else if (parentCtx == ParserRuleContext.ERROR_BINDING_PATTERN) {
            return ParserRuleContext.ERROR_ARG_LIST_BINDING_PATTERN_START;
        }
        return ParserRuleContext.EXPRESSION;
    }

    private boolean isInMatchPatternCtx(ParserRuleContext context) {
        switch (context) {
            case MATCH_PATTERN:
            case LIST_MATCH_PATTERN:
            case MAPPING_MATCH_PATTERN:
            case ERROR_MATCH_PATTERN:
            case NAMED_ARG_MATCH_PATTERN:
                return true;
            default:
                return false;
        }
    }

    private ParserRuleContext getNextRuleForOpenBrace() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case OBJECT_TYPE_DESCRIPTOR:
                return ParserRuleContext.OBJECT_TYPE_MEMBER;
            case MODULE_CLASS_DEFINITION:
                return ParserRuleContext.CLASS_MEMBER;
            case OBJECT_CONSTRUCTOR:
            case SERVICE_DECL:
                return ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER;
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
            case MAPPING_MATCH_PATTERN:
                return ParserRuleContext.FIELD_MATCH_PATTERNS_START;
            case MATCH_BODY:
                return ParserRuleContext.MATCH_PATTERN;
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
            case ORDER_KEY_LIST:
            case SELECT_CLAUSE:
            case JOIN_CLAUSE:
            case ON_CONFLICT_CLAUSE:
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
            // Here we add ELLIPSIS as an alternative path via PARAM_RHS.
            // This is because we allow parsing rest param under REQUIRED_PARAM and DEFAULTABLE_PARAM ctx in the parser.
            if (hasAncestorContext(ParserRuleContext.FUNC_TYPE_DESC)) {
                return ParserRuleContext.FUNC_TYPE_PARAM_RHS;
            }
            return ParserRuleContext.PARAM_RHS;
        } else if (parentCtx == ParserRuleContext.REST_PARAM) {
            return ParserRuleContext.ELLIPSIS;
        } else {
            throw new IllegalStateException("getNextRuleForParamType found: " + parentCtx);
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
            case LIST_CONSTRUCTOR:
                return ParserRuleContext.LIST_CONSTRUCTOR_MEMBER;
            case LISTENERS_LIST:
            case ORDER_KEY_LIST:
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
                return ParserRuleContext.LIST_BINDING_PATTERN_MEMBER;
            case MAPPING_BINDING_PATTERN:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
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
            case LIST_MATCH_PATTERN:
                return ParserRuleContext.LIST_MATCH_PATTERN_MEMBER;
            case ERROR_BINDING_PATTERN:
                return ParserRuleContext.ERROR_FIELD_BINDING_PATTERN;
            case MAPPING_MATCH_PATTERN:
                return ParserRuleContext.FIELD_MATCH_PATTERN_MEMBER;
            case ERROR_MATCH_PATTERN:
                return ParserRuleContext.ERROR_FIELD_MATCH_PATTERN;
            case NAMED_ARG_MATCH_PATTERN:
                endContext();
                return ParserRuleContext.NAMED_ARG_MATCH_PATTERN_RHS;
            default:
                throw new IllegalStateException("getNextRuleForComma found: " + parentCtx);
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
                    return ParserRuleContext.TYPE_DESC_RHS;
                }
                return ParserRuleContext.ANNOTATION_TAG;
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }
                return ParserRuleContext.VARIABLE_NAME;
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }
                return ParserRuleContext.BINDING_PATTERN;
            case TYPE_DESC_IN_PARAM:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }
                return ParserRuleContext.AFTER_PARAMETER_TYPE;
            case TYPE_DESC_IN_TYPE_DEF:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }
                return ParserRuleContext.SEMICOLON;
            case TYPE_DESC_IN_ANGLE_BRACKETS:
                endContext();
                return ParserRuleContext.GT;
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }

                parentCtx = getParentContext();
                switch (parentCtx) {
                    case FUNC_TYPE_DESC:
                        endContext();
                        return ParserRuleContext.TYPE_DESC_RHS;
                    case FUNC_DEF_OR_FUNC_TYPE:
                        return ParserRuleContext.FUNC_BODY_OR_TYPE_DESC_RHS;
                    case FUNC_TYPE_DESC_OR_ANON_FUNC:
                        return ParserRuleContext.FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY;
                    case FUNC_DEF:
                        ParserRuleContext grandParentCtx = getGrandParentContext();
                        if (grandParentCtx == ParserRuleContext.OBJECT_TYPE_MEMBER) {
                            return ParserRuleContext.SEMICOLON;
                        } else {
                            return ParserRuleContext.FUNC_BODY;
                        }
                    case ANON_FUNC_EXPRESSION:
                        return ParserRuleContext.ANON_FUNC_BODY;
                    case NAMED_WORKER_DECL:
                        return ParserRuleContext.BLOCK_STMT;
                    default:
                        throw new IllegalStateException("next rule of type-desc-in-return-type found: " + parentCtx);
                }
            case TYPE_DESC_IN_EXPRESSION:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
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
            case OBJECT_CONSTRUCTOR_MEMBER:
            case CLASS_MEMBER:
            case OBJECT_TYPE_MEMBER:
                return ParserRuleContext.VARIABLE_NAME;
            case ANNOTATION_DECL:
                return ParserRuleContext.IDENTIFIER;
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
                return ParserRuleContext.STREAM_TYPE_FIRST_PARAM_RHS;
            case TYPE_DESC_IN_PARENTHESIS:
                return ParserRuleContext.CLOSE_PARENTHESIS;
            case TYPE_DESC_IN_TUPLE:
            case STMT_START_BRACKETED_LIST:
                return ParserRuleContext.TYPE_DESC_IN_TUPLE_RHS;
            case TYPE_REFERENCE_IN_TYPE_INCLUSION:
                endContext();
                return ParserRuleContext.SEMICOLON;
            case TYPE_DESC_IN_SERVICE:
                endContext();
                return ParserRuleContext.OPTIONAL_ABSOLUTE_PATH;
            case TYPE_DESC_IN_PATH_PARAM:
                endContext();
                return ParserRuleContext.PATH_PARAM_ELLIPSIS;
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
            case TYPE_DESC_IN_TUPLE:
            case TYPE_DESC_IN_SERVICE:
            case TYPE_DESC_IN_PATH_PARAM:
            case STMT_START_BRACKETED_LIST:
            case BRACKETED_LIST:
            case TYPE_REFERENCE_IN_TYPE_INCLUSION:
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
                return ParserRuleContext.EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS;
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
                return ParserRuleContext.EXPR_START_OR_INFERRED_TYPEDESC_DEFAULT_START;
            case RECORD_FIELD:
            case ARG_LIST:
            case OBJECT_CONSTRUCTOR_MEMBER:
            case CLASS_MEMBER:
            case OBJECT_TYPE_MEMBER:
            case LISTENER_DECL:
            case CONSTANT_DECL:
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
            case ENUM_MEMBER_LIST:
                return ParserRuleContext.EXPRESSION;
            case FUNC_DEF_OR_FUNC_TYPE:
                switchContext(ParserRuleContext.VAR_DECL_STMT);
                return ParserRuleContext.EXPRESSION;
            case NAMED_ARG_MATCH_PATTERN:
                return ParserRuleContext.MATCH_PATTERN;
            case ERROR_BINDING_PATTERN:
                return ParserRuleContext.BINDING_PATTERN;
            default:
                if (isStatement(parentCtx)) {
                    return ParserRuleContext.EXPRESSION;
                }
                throw new IllegalStateException("getNextRuleForEqualOp found: " + parentCtx);
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
        STToken nextToken;
        switch (parentCtx) {
            case FUNC_BODY_BLOCK:
                endContext(); // end body block
                return getNextRuleForCloseBraceInFuncBody();
            case CLASS_MEMBER:
                endContext();
                // fall through
            case SERVICE_DECL:
            case MODULE_CLASS_DEFINITION:
                endContext();
                return ParserRuleContext.OPTIONAL_TOP_LEVEL_SEMICOLON;
            case OBJECT_CONSTRUCTOR_MEMBER:
                endContext();
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.SERVICE_DECL) {
                    endContext();
                    return ParserRuleContext.TOP_LEVEL_NODE;
                }
                endContext(); // end object-constructor
                return ParserRuleContext.EXPRESSION_RHS;
            case OBJECT_TYPE_MEMBER:
                endContext();
                // fall through
            case RECORD_TYPE_DESCRIPTOR:
            case OBJECT_TYPE_DESCRIPTOR:
                endContext(); // end record/object-type-desc
                return ParserRuleContext.TYPE_DESC_RHS;
            case BLOCK_STMT:
            case AMBIGUOUS_STMT:
                endContext(); // end block stmt
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case LOCK_STMT:
                    case FOREACH_STMT:
                    case WHILE_BLOCK:
                    case DO_BLOCK:
                    case RETRY_STMT:
                        endContext();
                        return ParserRuleContext.REGULAR_COMPOUND_STMT_RHS;
                    case ON_FAIL_CLAUSE:
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
                        return ParserRuleContext.REGULAR_COMPOUND_STMT_RHS;
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
                    case MATCH_BODY:
                        return ParserRuleContext.MATCH_PATTERN;
                    case DO_CLAUSE:
                        return ParserRuleContext.QUERY_EXPRESSION_END;
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
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                endContext();
                return getNextRuleForBindingPattern();
            case FORK_STMT:
                endContext(); // end fork-statement
                return ParserRuleContext.STATEMENT;
            case INTERPOLATION:
                endContext();
                return ParserRuleContext.TEMPLATE_MEMBER;
            case OBJECT_CONSTRUCTOR:
            case MULTI_RECEIVE_WORKERS:
            case MULTI_WAIT_FIELDS:
                endContext();
                return ParserRuleContext.EXPRESSION_RHS;
            case ENUM_MEMBER_LIST:
                endContext(); // end ENUM_MEMBER_LIST context
                endContext(); // end MODULE_ENUM_DECLARATION ctx
                return ParserRuleContext.OPTIONAL_TOP_LEVEL_SEMICOLON;
            case MATCH_BODY:
                endContext(); // end match body
                endContext(); // end match stmt
                return ParserRuleContext.REGULAR_COMPOUND_STMT_RHS;
            case MAPPING_MATCH_PATTERN:
                endContext();
                return getNextRuleForMatchPattern();
            case MATCH_STMT:
                endContext();
                return ParserRuleContext.REGULAR_COMPOUND_STMT_RHS;
            default:
                throw new IllegalStateException("getNextRuleForCloseBrace found: " + parentCtx);
        }
    }

    private ParserRuleContext getNextRuleForCloseBraceInFuncBody() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        switch (parentCtx) {
            case OBJECT_CONSTRUCTOR_MEMBER:
                return ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER_START;
            case CLASS_MEMBER:
            case OBJECT_TYPE_MEMBER:
                return ParserRuleContext.CLASS_MEMBER_OR_OBJECT_MEMBER_START;
            case COMP_UNIT:
                return ParserRuleContext.OPTIONAL_TOP_LEVEL_SEMICOLON;
            case FUNC_DEF:
            case FUNC_DEF_OR_FUNC_TYPE:
                endContext(); // end func-def
                return getNextRuleForCloseBraceInFuncBody();
            case ANON_FUNC_EXPRESSION:
            default:
                // Anonynous func
                endContext(); // end anon-func
                return ParserRuleContext.EXPRESSION_RHS;
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
            case OBJECT_CONSTRUCTOR_MEMBER:
                return ParserRuleContext.OBJECT_CONS_MEMBER_WITHOUT_META;
            case CLASS_MEMBER:
            case OBJECT_TYPE_MEMBER:
                return ParserRuleContext.CLASS_MEMBER_OR_OBJECT_MEMBER_WITHOUT_META;
            case FUNC_BODY_BLOCK:
                return ParserRuleContext.STATEMENT_WITHOUT_ANNOTS;
            case EXTERNAL_FUNC_BODY:
                return ParserRuleContext.EXTERNAL_KEYWORD;
            case TYPE_CAST:
                return ParserRuleContext.TYPE_CAST_PARAM_RHS;
            case ENUM_MEMBER_LIST:
                return ParserRuleContext.ENUM_MEMBER_NAME;
            case RELATIVE_RESOURCE_PATH:
                return ParserRuleContext.TYPE_DESC_IN_PATH_PARAM;
            default:
                if (isParameter(parentCtx)) {
                    return ParserRuleContext.TYPE_DESC_IN_PARAM;
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
        switch (parentCtx) {
            case ASSIGNMENT_STMT:
                return ParserRuleContext.ASSIGNMENT_STMT_RHS;
            case CALL_STMT:
                return ParserRuleContext.ARG_LIST;
            case REQUIRED_PARAM:
            case PARAM_LIST:
                return ParserRuleContext.REQUIRED_PARAM_NAME_RHS;
            case DEFAULTABLE_PARAM:
                return ParserRuleContext.ASSIGN_OP;
            case REST_PARAM:
                return ParserRuleContext.PARAM_END;
            case FOREACH_STMT:
                return ParserRuleContext.IN_KEYWORD;
            case BINDING_PATTERN_STARTING_IDENTIFIER:
                return getNextRuleForBindingPattern(true);
            case TYPED_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case STMT_START_BRACKETED_LIST_MEMBER:
            case REST_BINDING_PATTERN:
            case FIELD_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            case ERROR_BINDING_PATTERN:
                return getNextRuleForBindingPattern();
            case LISTENER_DECL:
            case CONSTANT_DECL:
                return ParserRuleContext.VAR_DECL_STMT_RHS;
            case RECORD_FIELD:
                return ParserRuleContext.FIELD_DESCRIPTOR_RHS;
            case ARG_LIST:
                return ParserRuleContext.NAMED_OR_POSITIONAL_ARG_RHS;
            case OBJECT_CONSTRUCTOR_MEMBER:
            case CLASS_MEMBER:
            case OBJECT_TYPE_MEMBER:
                return ParserRuleContext.OBJECT_FIELD_RHS;
            case ARRAY_TYPE_DESCRIPTOR:
                return ParserRuleContext.CLOSE_BRACKET;
            case KEY_SPECIFIER:
                return ParserRuleContext.TABLE_KEY_RHS;
            case LET_EXPR_LET_VAR_DECL:
            case LET_CLAUSE_LET_VAR_DECL:
                return ParserRuleContext.ASSIGN_OP;
            case ANNOTATION_DECL:
                return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
            case QUERY_EXPRESSION:
            case JOIN_CLAUSE:
                return ParserRuleContext.IN_KEYWORD;
            case REST_MATCH_PATTERN:
                endContext(); // end rest match pattern context
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.MAPPING_MATCH_PATTERN) {
                    return ParserRuleContext.CLOSE_BRACE;
                }
                if (parentCtx == ParserRuleContext.ERROR_MATCH_PATTERN ||
                        parentCtx == ParserRuleContext.ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG) {
                    return ParserRuleContext.CLOSE_PARENTHESIS;
                }
                return ParserRuleContext.CLOSE_BRACKET;
            case MAPPING_MATCH_PATTERN:
                return ParserRuleContext.COLON;
            case ON_FAIL_CLAUSE:
                return ParserRuleContext.BLOCK_STMT;
            case ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG:
                endContext();
                return ParserRuleContext.ERROR_MESSAGE_MATCH_PATTERN_END;
            case ERROR_MATCH_PATTERN:
                return ParserRuleContext.ERROR_FIELD_MATCH_PATTERN_RHS;
            case RELATIVE_RESOURCE_PATH:
                return ParserRuleContext.CLOSE_BRACKET;
            default:
                if (isStatement(parentCtx)) {
                    return ParserRuleContext.VAR_DECL_STMT_RHS;
                }
                throw new IllegalStateException("getNextRuleForVarName found: " + parentCtx);
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
            return getNextRuleForSemicolon(nextLookahead);
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
        } else if (parentCtx == ParserRuleContext.XML_NAMESPACE_DECLARATION) {
            endContext();
            parentCtx = getParentContext();
            if (parentCtx == ParserRuleContext.COMP_UNIT) {
                return ParserRuleContext.TOP_LEVEL_NODE;
            }
            return ParserRuleContext.STATEMENT;
        } else if (parentCtx == ParserRuleContext.MODULE_TYPE_DEFINITION ||
                parentCtx == ParserRuleContext.LISTENER_DECL || parentCtx == ParserRuleContext.ANNOTATION_DECL) {
            endContext();
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.CONSTANT_DECL) {
            endContext();
            parentCtx = getParentContext();
            if (parentCtx == ParserRuleContext.FUNC_BODY_BLOCK) {
                // This is because we allow parsing constant-decl in func body and invalidate.
                return ParserRuleContext.STATEMENT;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER ||
                parentCtx == ParserRuleContext.CLASS_MEMBER ||
                parentCtx == ParserRuleContext.OBJECT_TYPE_MEMBER) {
            if (isEndOfObjectTypeNode(nextLookahead)) {
                endContext(); // end object member
                return ParserRuleContext.CLOSE_BRACE;
            }
            if (parentCtx == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER) {
                return ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER_START;
            } else {
                return ParserRuleContext.CLASS_MEMBER_OR_OBJECT_MEMBER_START;
            }
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
            return getNextRuleForSemicolon(nextLookahead);
        } else if (parentCtx == ParserRuleContext.MODULE_CLASS_DEFINITION) {
            return ParserRuleContext.CLASS_MEMBER;
        } else if (parentCtx == ParserRuleContext.OBJECT_CONSTRUCTOR) {
            return ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER;
        } else if (parentCtx == ParserRuleContext.COMP_UNIT) {
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else {
            throw new IllegalStateException("getNextRuleForSemicolon found: " + parentCtx);
        }
    }

    private ParserRuleContext getNextRuleForDot() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case IMPORT_DECL:
                return ParserRuleContext.IMPORT_MODULE_NAME;
            case RELATIVE_RESOURCE_PATH:
                return ParserRuleContext.RESOURCE_ACCESSOR_DEF_OR_DECL_RHS;
            case CLIENT_RESOURCE_ACCESS_ACTION:
                return ParserRuleContext.METHOD_NAME;
            default:
                return ParserRuleContext.FIELD_ACCESS_IDENTIFIER;
        }
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
                return ParserRuleContext.TYPE_DESC_RHS;
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
                return ParserRuleContext.LIST_BINDING_PATTERNS_START;
            case LIST_MATCH_PATTERN:
                return ParserRuleContext.LIST_MATCH_PATTERNS_START;
            case RELATIVE_RESOURCE_PATH:
                return ParserRuleContext.PATH_PARAM_OPTIONAL_ANNOTS;
            case CLIENT_RESOURCE_ACCESS_ACTION:
                return ParserRuleContext.COMPUTED_SEGMENT_OR_REST_SEGMENT;
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
                return ParserRuleContext.TYPE_DESC_RHS;
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
            case LIST_MATCH_PATTERN:
                endContext();
                return getNextRuleForMatchPattern();
            case RELATIVE_RESOURCE_PATH:
                return ParserRuleContext.RELATIVE_RESOURCE_PATH_END;
            case CLIENT_RESOURCE_ACCESS_ACTION:
                return ParserRuleContext.RESOURCE_ACCESS_SEGMENT_RHS;
            default:
                return getNextRuleForExpr();
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#DECIMAL_INTEGER_LITERAL_TOKEN}.
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

    private ParserRuleContext getNextRuleForExprStartsWithVarRef() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.CONSTANT_EXPRESSION) {
            endContext();
            return getNextRuleForConstExpr();
        } else if (parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
            return ParserRuleContext.CLOSE_BRACKET;
        } else if (parentCtx == ParserRuleContext.CALL_STMT) {
            return ParserRuleContext.ARG_LIST_OPEN_PAREN;
        }
        return ParserRuleContext.VARIABLE_REF_RHS;
    }

    private ParserRuleContext getNextRuleForConstExpr() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case XML_NAMESPACE_DECLARATION:
                return ParserRuleContext.XML_NAMESPACE_PREFIX_DECL;
            default:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }
                return getNextRuleForMatchPattern();
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

    private ParserRuleContext getNextRuleForGt() {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC) {
            // Since type-desc in a stream-type can have alternate endings,
            // we haven't end the context. So if its '>', then end the ctx here.
            endContext();
            parentCtx = getParentContext();
            if (parentCtx == ParserRuleContext.CLASS_DESCRIPTOR_IN_NEW_EXPR) {
                // stream-type-desc in class-descriptor cannot be a complex type
                endContext();
                return ParserRuleContext.ARG_LIST_OPEN_PAREN;
            }
            return ParserRuleContext.TYPE_DESC_RHS;
        }

        if (isInTypeDescContext()) {
            return ParserRuleContext.TYPE_DESC_RHS;
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
        return getNextRuleForBindingPattern(false);
    }

    private ParserRuleContext getNextRuleForBindingPattern(boolean isCaptureBP) {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case BINDING_PATTERN_STARTING_IDENTIFIER:
            case TYPED_BINDING_PATTERN:
                endContext();
                return getNextRuleForBindingPattern(isCaptureBP);
            case FOREACH_STMT:
            case QUERY_EXPRESSION:
            case JOIN_CLAUSE:
                return ParserRuleContext.IN_KEYWORD;
            case LIST_BINDING_PATTERN:
            case STMT_START_BRACKETED_LIST:
            case BRACKETED_LIST:
                return ParserRuleContext.LIST_BINDING_PATTERN_MEMBER_END;
            case MAPPING_BINDING_PATTERN:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                return ParserRuleContext.MAPPING_BINDING_PATTERN_END;
            case REST_BINDING_PATTERN:
                endContext();
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LIST_BINDING_PATTERN) {
                    return ParserRuleContext.CLOSE_BRACKET;
                } else if (parentCtx == ParserRuleContext.ERROR_BINDING_PATTERN) {
                    return ParserRuleContext.CLOSE_PARENTHESIS;
                }
                return ParserRuleContext.CLOSE_BRACE; // for mapping binding pattern
            case AMBIGUOUS_STMT:
                switchContext(ParserRuleContext.VAR_DECL_STMT);
                return isCaptureBP ? ParserRuleContext.VAR_DECL_STMT_RHS : ParserRuleContext.ASSIGN_OP;
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case VAR_DECL_STMT:
                return isCaptureBP ? ParserRuleContext.VAR_DECL_STMT_RHS : ParserRuleContext.ASSIGN_OP;
            case LET_CLAUSE_LET_VAR_DECL:
            case LET_EXPR_LET_VAR_DECL:
            case ASSIGNMENT_STMT:
                return ParserRuleContext.ASSIGN_OP;
            case MATCH_PATTERN:
                return ParserRuleContext.MATCH_PATTERN_LIST_MEMBER_RHS;
            case LIST_MATCH_PATTERN:
                return ParserRuleContext.LIST_MATCH_PATTERN_MEMBER_RHS;
            case ERROR_BINDING_PATTERN:
                return ParserRuleContext.ERROR_FIELD_BINDING_PATTERN_END;
            case ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG:
                endContext();
                return ParserRuleContext.ERROR_FIELD_MATCH_PATTERN_RHS;
            default:
                return getNextRuleForMatchPattern();
        }
    }

    private ParserRuleContext getNextRuleForWaitExprListEnd() {
        // TODO: add other endings based on the locations where action is allowed.
        endContext();
        return ParserRuleContext.EXPRESSION_RHS;
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#IDENTIFIER}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForIdentifier() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        switch (parentCtx) {
            case VARIABLE_REF:
                endContext();
                return getNextRuleForExprStartsWithVarRef();
            case TYPE_REFERENCE:
                endContext();
                return getNextRuleForTypeReference();
            case TYPE_REFERENCE_IN_TYPE_INCLUSION:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }
                return ParserRuleContext.SEMICOLON;
            case ANNOT_REFERENCE:
                endContext();
                return ParserRuleContext.ANNOTATION_REF_RHS;
            case ANNOTATION_DECL:
                return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
            case FIELD_ACCESS_IDENTIFIER:
                endContext();
                return ParserRuleContext.VARIABLE_REF_RHS;
            case XML_ATOMIC_NAME_PATTERN:
                endContext();
                return ParserRuleContext.XML_NAME_PATTERN_RHS;
            case NAMED_ARG_MATCH_PATTERN:
                return ParserRuleContext.ASSIGN_OP;
            case MODULE_CLASS_DEFINITION:
                return ParserRuleContext.OPEN_BRACE;
            case COMP_UNIT:
                return ParserRuleContext.TOP_LEVEL_NODE;
            case OBJECT_CONSTRUCTOR_MEMBER:
            case CLASS_MEMBER:
            case OBJECT_TYPE_MEMBER:
                return ParserRuleContext.SEMICOLON;
            case ABSOLUTE_RESOURCE_PATH:
                return ParserRuleContext.ABSOLUTE_RESOURCE_PATH_END;
            case CLIENT_RESOURCE_ACCESS_ACTION:
                return ParserRuleContext.RESOURCE_ACCESS_SEGMENT_RHS;
            default:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }
                throw new IllegalStateException("getNextRuleForIdentifier found: " + parentCtx);
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#COLON}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForColon() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        switch (parentCtx) {
            case MAPPING_CONSTRUCTOR:
                return ParserRuleContext.EXPRESSION;
            case MULTI_RECEIVE_WORKERS:
                return ParserRuleContext.PEER_WORKER_NAME;
            case MULTI_WAIT_FIELDS:
                return ParserRuleContext.EXPRESSION;
            case CONDITIONAL_EXPRESSION:
                endContext(); // end conditional-expr
                return ParserRuleContext.EXPRESSION;
            case MAPPING_BINDING_PATTERN:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                return ParserRuleContext.VARIABLE_NAME;
            case FIELD_BINDING_PATTERN:
                endContext();
                return ParserRuleContext.VARIABLE_NAME;
            case XML_ATOMIC_NAME_PATTERN:
                return ParserRuleContext.XML_ATOMIC_NAME_IDENTIFIER_RHS;
            case MAPPING_MATCH_PATTERN:
                return ParserRuleContext.MATCH_PATTERN;
            default:
                return ParserRuleContext.IDENTIFIER;
        }
    }

    private ParserRuleContext getNextRuleForMatchPattern() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case LIST_MATCH_PATTERN:
                return ParserRuleContext.LIST_MATCH_PATTERN_MEMBER_RHS;
            case MAPPING_MATCH_PATTERN:
                return ParserRuleContext.FIELD_MATCH_PATTERN_MEMBER_RHS;
            case MATCH_PATTERN:
                return ParserRuleContext.MATCH_PATTERN_LIST_MEMBER_RHS;
            case ERROR_MATCH_PATTERN:
            case NAMED_ARG_MATCH_PATTERN:
                return ParserRuleContext.ERROR_FIELD_MATCH_PATTERN_RHS;
            case ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG:
                endContext();
                return ParserRuleContext.ERROR_MESSAGE_MATCH_PATTERN_END;
            default:
                return ParserRuleContext.OPTIONAL_MATCH_GUARD;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#TYPE_REFERENCE}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForTypeReference() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case ERROR_CONSTRUCTOR:
                return ParserRuleContext.ARG_LIST_OPEN_PAREN;
            case OBJECT_CONSTRUCTOR:
                return ParserRuleContext.OPEN_BRACE;
            case ERROR_MATCH_PATTERN:
            case ERROR_BINDING_PATTERN:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case CLASS_DESCRIPTOR_IN_NEW_EXPR:
                endContext();
                return ParserRuleContext.ARG_LIST_OPEN_PAREN;
            default:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPE_DESC_RHS;
                }

                throw new IllegalStateException("getNextRuleForTypeReference found: " + parentCtx);
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#ERROR_KEYWORD}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForErrorKeyword() {
        if (isInTypeDescContext()) {
            return ParserRuleContext.LT;
        }

        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case ERROR_MATCH_PATTERN:
                return ParserRuleContext.ERROR_MATCH_PATTERN_ERROR_KEYWORD_RHS;
            case ERROR_BINDING_PATTERN:
                return ParserRuleContext.ERROR_BINDING_PATTERN_ERROR_KEYWORD_RHS;
            case ERROR_CONSTRUCTOR:
                return ParserRuleContext.ERROR_CONSTRUCTOR_RHS;
        }

        return ParserRuleContext.ARG_LIST_OPEN_PAREN;
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#FUNC_TYPE_FUNC_KEYWORD_RHS}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForFuncTypeFuncKeywordRhs() {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE) {
            endContext();
            parentCtx = getParentContext();
            switch (parentCtx) {
                case OBJECT_TYPE_MEMBER:
                case CLASS_MEMBER:
                case OBJECT_CONSTRUCTOR_MEMBER:
                    startContext(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
                    break;
                case COMP_UNIT:
                default:
                    startContext(ParserRuleContext.VAR_DECL_STMT);
                    startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                    break;
            }
        } else if (getGrandParentContext() == ParserRuleContext.OBJECT_TYPE_MEMBER) {
            switchContext(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        }

        assert isInTypeDescContext();
        startContext(ParserRuleContext.FUNC_TYPE_DESC);
        return ParserRuleContext.FUNC_TYPE_FUNC_KEYWORD_RHS_START;
    }
    
    private ParserRuleContext getNextRuleForAction() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case MATCH_STMT:
                return ParserRuleContext.MATCH_BODY;
            case FOREACH_STMT:
                return ParserRuleContext.BLOCK_STMT;
            default:
                return ParserRuleContext.SEMICOLON;
        }
    }

    /**
     * Check whether the given context is a statement.
     *
     * @param parentCtx Parser context to check
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
            case DO_BLOCK:
            case CALL_STMT:
            case PANIC_STMT:
            case CONTINUE_STATEMENT:
            case BREAK_STATEMENT:
            case RETURN_STMT:
            case FAIL_STATEMENT:
            case LOCAL_TYPE_DEFINITION_STMT:
            case EXPRESSION_STATEMENT:
            case LOCK_STMT:
            case FORK_STMT:
            case FOREACH_STMT:
            case TRANSACTION_STMT:
            case RETRY_STMT:
            case ROLLBACK_STMT:
            case AMBIGUOUS_STMT:
            case MATCH_STMT:
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
     * Get the shortest insert solution at the given parser rule context. If the parser rule is a terminal,
     * then return the corresponding terminal token kind as the fix. If the parser rule is a production,
     * then navigate the shortest path and get next available terminal token kind as the fix.
     *
     * @param ctx Parser rule context
     * @return Shortest insert solution at the given parser rule
     */
    @Override
    protected Solution getInsertSolution(ParserRuleContext ctx) {
        SyntaxKind kind = getExpectedTokenKind(ctx);
        if (kind != SyntaxKind.NONE) {
            return new Solution(Action.INSERT, ctx, kind, ctx.toString());
        }
        
        if (hasAlternativePaths(ctx)) {
            ctx = getShortestAlternative(ctx);
            return getInsertSolution(ctx);
        }
        
        ctx = getNextRule(ctx, 1);
        return getInsertSolution(ctx);
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
            case EXTERNAL_FUNC_BODY:
                return SyntaxKind.EQUAL_TOKEN;
            case FUNC_BODY_BLOCK:
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case FUNC_DEF:
            case FUNC_DEF_OR_FUNC_TYPE:
            case FUNC_TYPE_DESC:
            case FUNC_TYPE_DESC_OR_ANON_FUNC:
                return SyntaxKind.FUNCTION_KEYWORD;
            case SIMPLE_TYPE_DESCRIPTOR:
                return SyntaxKind.ANY_KEYWORD;
            case REQUIRED_PARAM:
            case VAR_DECL_STMT:
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
            case TYPE_NAME:
            case TYPE_REFERENCE_IN_TYPE_INCLUSION:
            case TYPE_REFERENCE:
            case SIMPLE_TYPE_DESC_IDENTIFIER:
            case FIELD_ACCESS_IDENTIFIER:
            case FUNC_NAME:
            case CLASS_NAME:
            case VARIABLE_NAME:
            case IMPORT_MODULE_NAME:
            case IMPORT_ORG_OR_MODULE_NAME:
            case IMPORT_PREFIX:
            case VARIABLE_REF:
            case BASIC_LITERAL: // return var-ref for any kind of terminal expression
            case IDENTIFIER:
            case QUALIFIED_IDENTIFIER_START_IDENTIFIER:
            case NAMESPACE_PREFIX:
            case IMPLICIT_ANON_FUNC_PARAM:
            case METHOD_NAME:
            case PEER_WORKER_NAME:
            case RECEIVE_FIELD_NAME:
            case WAIT_FIELD_NAME:
            case FIELD_BINDING_PATTERN_NAME:
            case XML_ATOMIC_NAME_IDENTIFIER:
            case MAPPING_FIELD_NAME:
            case WORKER_NAME:
            case NAMED_WORKERS:
            case ANNOTATION_TAG:
            case AFTER_PARAMETER_TYPE:
            case MODULE_ENUM_NAME:
            case ENUM_MEMBER_NAME:
            case TYPED_BINDING_PATTERN_TYPE_RHS:
            case ASSIGNMENT_STMT:
            case EXPRESSION:
            case TERMINAL_EXPRESSION:
            case XML_NAME:
            case ACCESS_EXPRESSION:
            case BINDING_PATTERN_STARTING_IDENTIFIER:
            case COMPUTED_FIELD_NAME:
            case SIMPLE_BINDING_PATTERN:
            case ERROR_FIELD_BINDING_PATTERN:
            case ERROR_CAUSE_SIMPLE_BINDING_PATTERN:
            case PATH_SEGMENT_IDENT:
            case TYPE_DESCRIPTOR:
            case NAMED_ARG_BINDING_PATTERN:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case VERSION_NUMBER:
            case MAJOR_VERSION:
            case MINOR_VERSION:
            case PATCH_VERSION:
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case SIGNED_INT_OR_FLOAT_RHS:
                return SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN;
            case STRING_LITERAL_TOKEN:
                return SyntaxKind.STRING_LITERAL_TOKEN;
            case OPTIONAL_TYPE_DESCRIPTOR:
                return SyntaxKind.OPTIONAL_TYPE_DESC;
            case ARRAY_TYPE_DESCRIPTOR:
                return SyntaxKind.ARRAY_TYPE_DESC;
            case HEX_INTEGER_LITERAL_TOKEN:
                return SyntaxKind.HEX_INTEGER_LITERAL_TOKEN;
            case IMPORT_SUB_VERSION:
            case OBJECT_FIELD_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
                return SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN;
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                return SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN;
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case ERROR_MATCH_PATTERN:
            case NIL_LITERAL:
                return SyntaxKind.OPEN_PAREN_TOKEN;
            default:
                return getExpectedSeperatorTokenKind(ctx);
        }
    }

    protected SyntaxKind getExpectedSeperatorTokenKind(ParserRuleContext ctx) {
        switch (ctx) {
            case BITWISE_AND_OPERATOR:
                return SyntaxKind.BITWISE_AND_TOKEN;
            case EQUAL_OR_RIGHT_ARROW:
                return SyntaxKind.EQUAL_TOKEN;
            case EOF:
                return SyntaxKind.EOF_TOKEN;
            case ASSIGN_OP:
                return SyntaxKind.EQUAL_TOKEN;
            case BINARY_OPERATOR:
                return SyntaxKind.PLUS_TOKEN;
            case CLOSE_BRACE:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case CLOSE_PARENTHESIS:
            case ARG_LIST_CLOSE_PAREN:
                return SyntaxKind.CLOSE_PAREN_TOKEN;
            case COMMA:
            case ERROR_MESSAGE_BINDING_PATTERN_END_COMMA:
            case ERROR_MESSAGE_MATCH_PATTERN_END_COMMA:
                return SyntaxKind.COMMA_TOKEN;
            case OPEN_BRACE:
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case OPEN_PARENTHESIS:
            case ARG_LIST_OPEN_PAREN:
            case PARENTHESISED_TYPE_DESC_START:
                return SyntaxKind.OPEN_PAREN_TOKEN;
            case SEMICOLON:
                return SyntaxKind.SEMICOLON_TOKEN;
            case ASTERISK:
                return SyntaxKind.ASTERISK_TOKEN;
            case CLOSED_RECORD_BODY_END:
                return SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
            case CLOSED_RECORD_BODY_START:
                return SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
            case ELLIPSIS:
                return SyntaxKind.ELLIPSIS_TOKEN;
            case QUESTION_MARK:
                return SyntaxKind.QUESTION_MARK_TOKEN;
            case CLOSE_BRACKET:
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case DOT:
            case METHOD_CALL_DOT:
                return SyntaxKind.DOT_TOKEN;
            case OPEN_BRACKET:
            case TUPLE_TYPE_DESC_START:
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case SLASH:
            case ABSOLUTE_PATH_SINGLE_SLASH:
            case RESOURCE_METHOD_CALL_SLASH_TOKEN:    
                return SyntaxKind.SLASH_TOKEN;
            case COLON:
            case TYPE_REF_COLON:
            case VAR_REF_COLON:
                return SyntaxKind.COLON_TOKEN;
            case UNARY_OPERATOR:
            case COMPOUND_BINARY_OPERATOR:
            case UNARY_EXPRESSION:
            case EXPRESSION_RHS:
                return SyntaxKind.PLUS_TOKEN;
            case AT:
                return SyntaxKind.AT_TOKEN;
            case RIGHT_ARROW:
                return SyntaxKind.RIGHT_ARROW_TOKEN;
            case GT:
            case INFERRED_TYPEDESC_DEFAULT_END_GT:
                return SyntaxKind.GT_TOKEN;
            case LT:
            case STREAM_TYPE_PARAM_START_TOKEN:
            case INFERRED_TYPEDESC_DEFAULT_START_LT:
                return SyntaxKind.LT_TOKEN;
            case SYNC_SEND_TOKEN:
                return SyntaxKind.SYNC_SEND_TOKEN;
            case ANNOT_CHAINING_TOKEN:
                return SyntaxKind.ANNOT_CHAINING_TOKEN;
            case OPTIONAL_CHAINING_TOKEN:
                return SyntaxKind.OPTIONAL_CHAINING_TOKEN;
            case DOT_LT_TOKEN:
                return SyntaxKind.DOT_LT_TOKEN;
            case SLASH_LT_TOKEN:
                return SyntaxKind.SLASH_LT_TOKEN;
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
                return SyntaxKind.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN;
            case SLASH_ASTERISK_TOKEN:
                return SyntaxKind.SLASH_ASTERISK_TOKEN;
            case PLUS_TOKEN:
                return SyntaxKind.PLUS_TOKEN;
            case MINUS_TOKEN:
                return SyntaxKind.MINUS_TOKEN;
            case LEFT_ARROW_TOKEN:
                return SyntaxKind.LEFT_ARROW_TOKEN;
            case TEMPLATE_END:
            case TEMPLATE_START:
                return SyntaxKind.BACKTICK_TOKEN;
            case LT_TOKEN:
                return SyntaxKind.LT_TOKEN;
            case GT_TOKEN:
                return SyntaxKind.GT_TOKEN;
            case INTERPOLATION_START_TOKEN:
                return SyntaxKind.INTERPOLATION_START_TOKEN;
            case EXPR_FUNC_BODY_START:
            case RIGHT_DOUBLE_ARROW:
                return SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN;
            default:
                return getExpectedKeywordKind(ctx);
        }
    }

    protected SyntaxKind getExpectedKeywordKind(ParserRuleContext ctx) {
        switch (ctx) {
            case EXTERNAL_KEYWORD:
                return SyntaxKind.EXTERNAL_KEYWORD;
            case FUNCTION_KEYWORD:
                return SyntaxKind.FUNCTION_KEYWORD;
            case RETURNS_KEYWORD:
                return SyntaxKind.RETURNS_KEYWORD;
            case PUBLIC_KEYWORD:
                return SyntaxKind.PUBLIC_KEYWORD;
            case RECORD_FIELD:
            case RECORD_KEYWORD:
                return SyntaxKind.RECORD_KEYWORD;
            case TYPE_KEYWORD:
                return SyntaxKind.TYPE_KEYWORD;
            case OBJECT_KEYWORD:
            case OBJECT_IDENT:
            case OBJECT_TYPE_DESCRIPTOR:
                return SyntaxKind.OBJECT_KEYWORD;
            case PRIVATE_KEYWORD:
                return SyntaxKind.PRIVATE_KEYWORD;
            case REMOTE_IDENT:
                return SyntaxKind.REMOTE_KEYWORD;
            case ABSTRACT_KEYWORD:
                return SyntaxKind.ABSTRACT_KEYWORD;
            case CLIENT_KEYWORD:
                return SyntaxKind.CLIENT_KEYWORD;
            case IF_KEYWORD:
                return SyntaxKind.IF_KEYWORD;
            case ELSE_KEYWORD:
                return SyntaxKind.ELSE_KEYWORD;
            case WHILE_KEYWORD:
                return SyntaxKind.WHILE_KEYWORD;
            case CHECKING_KEYWORD:
                return SyntaxKind.CHECK_KEYWORD;
            case FAIL_KEYWORD:
                return SyntaxKind.FAIL_KEYWORD;
            case AS_KEYWORD:
                return SyntaxKind.AS_KEYWORD;
            case BOOLEAN_LITERAL:
                return SyntaxKind.TRUE_KEYWORD;
            case IMPORT_KEYWORD:
                return SyntaxKind.IMPORT_KEYWORD;
            case ON_KEYWORD:
                return SyntaxKind.ON_KEYWORD;
            case PANIC_KEYWORD:
                return SyntaxKind.PANIC_KEYWORD;
            case RETURN_KEYWORD:
                return SyntaxKind.RETURN_KEYWORD;
            case SERVICE_KEYWORD:
            case SERVICE_IDENT:
                return SyntaxKind.SERVICE_KEYWORD;
            case BREAK_KEYWORD:
                return SyntaxKind.BREAK_KEYWORD;
            case LISTENER_KEYWORD:
                return SyntaxKind.LISTENER_KEYWORD;
            case CONTINUE_KEYWORD:
                return SyntaxKind.CONTINUE_KEYWORD;
            case CONST_KEYWORD:
                return SyntaxKind.CONST_KEYWORD;
            case FINAL_KEYWORD:
                return SyntaxKind.FINAL_KEYWORD;
            case IS_KEYWORD:
                return SyntaxKind.IS_KEYWORD;
            case TYPEOF_KEYWORD:
                return SyntaxKind.TYPEOF_KEYWORD;
            case MAP_KEYWORD:
            case MAP_TYPE_DESCRIPTOR:
                return SyntaxKind.MAP_KEYWORD;
            case PARAMETERIZED_TYPE:
                return SyntaxKind.ERROR_KEYWORD;
            case NULL_KEYWORD:
                return SyntaxKind.NULL_KEYWORD;
            case LOCK_KEYWORD:
                return SyntaxKind.LOCK_KEYWORD;
            case ANNOTATION_KEYWORD:
                return SyntaxKind.ANNOTATION_KEYWORD;
            case VERSION_KEYWORD:
                return SyntaxKind.VERSION_KEYWORD;
            case SINGLE_KEYWORD_ATTACH_POINT_IDENT:
                return SyntaxKind.TYPE_KEYWORD;
            case IDENT_AFTER_OBJECT_IDENT:
                return SyntaxKind.FUNCTION_KEYWORD;
            case FIELD_IDENT:
                return SyntaxKind.FIELD_KEYWORD;
            case FUNCTION_IDENT:
                return SyntaxKind.FUNCTION_KEYWORD;
            case RECORD_IDENT:
                return SyntaxKind.RECORD_KEYWORD;
            case XMLNS_KEYWORD:
            case XML_NAMESPACE_DECLARATION:
                return SyntaxKind.XMLNS_KEYWORD;
            case SOURCE_KEYWORD:
                return SyntaxKind.SOURCE_KEYWORD;
            case START_KEYWORD:
                return SyntaxKind.START_KEYWORD;
            case FLUSH_KEYWORD:
                return SyntaxKind.FLUSH_KEYWORD;
            case OPTIONAL_PEER_WORKER:
            case DEFAULT_WORKER_NAME_IN_ASYNC_SEND:
                return SyntaxKind.FUNCTION_KEYWORD;
            case WAIT_KEYWORD:
                return SyntaxKind.WAIT_KEYWORD;
            case TRANSACTION_KEYWORD:
                return SyntaxKind.TRANSACTION_KEYWORD;
            case TRANSACTIONAL_KEYWORD:
                return SyntaxKind.TRANSACTIONAL_KEYWORD;
            case COMMIT_KEYWORD:
                return SyntaxKind.COMMIT_KEYWORD;
            case RETRY_KEYWORD:
                return SyntaxKind.RETRY_KEYWORD;
            case ROLLBACK_KEYWORD:
                return SyntaxKind.ROLLBACK_KEYWORD;
            case ENUM_KEYWORD:
                return SyntaxKind.ENUM_KEYWORD;
            case MATCH_KEYWORD:
                return SyntaxKind.MATCH_KEYWORD;
            case NEW_KEYWORD:
                return SyntaxKind.NEW_KEYWORD;
            case FORK_KEYWORD:
                return SyntaxKind.FORK_KEYWORD;
            case NAMED_WORKER_DECL:
            case WORKER_KEYWORD:
                return SyntaxKind.WORKER_KEYWORD;
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
            case ERROR_BINDING_PATTERN:
                return SyntaxKind.ERROR_KEYWORD;
            case STREAM_KEYWORD:
                return SyntaxKind.STREAM_KEYWORD;
            case LET_KEYWORD:
                return SyntaxKind.LET_KEYWORD;
            case XML_KEYWORD:
                return SyntaxKind.XML_KEYWORD;
            case RE_KEYWORD:
                return SyntaxKind.RE_KEYWORD;
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
            case ORDER_KEYWORD:
                return SyntaxKind.ORDER_KEYWORD;
            case BY_KEYWORD:
                return SyntaxKind.BY_KEYWORD;
            case ORDER_DIRECTION:
                return SyntaxKind.ASCENDING_KEYWORD;
            case DO_KEYWORD:
                return SyntaxKind.DO_KEYWORD;
            case DISTINCT_KEYWORD:
                return SyntaxKind.DISTINCT_KEYWORD;
            case VAR_KEYWORD:
                return SyntaxKind.VAR_KEYWORD;
            case CONFLICT_KEYWORD:
                return SyntaxKind.CONFLICT_KEYWORD;
            case LIMIT_KEYWORD:
                return SyntaxKind.LIMIT_KEYWORD;
            case EQUALS_KEYWORD:
                return SyntaxKind.EQUALS_KEYWORD;
            case JOIN_KEYWORD:
                return SyntaxKind.JOIN_KEYWORD;
            case OUTER_KEYWORD:
                return SyntaxKind.OUTER_KEYWORD;
            case CLASS_KEYWORD:
                return SyntaxKind.CLASS_KEYWORD;
            default:
                return getExpectedQualifierKind(ctx);
        }
    }

    protected SyntaxKind getExpectedQualifierKind(ParserRuleContext ctx) {
        // Ideally, optimal solution should not be an INSERT action on qualifiers.
        // Therefore, qualifier ctxs are pointed to the end of qualifier parsing token to exit early.
        switch (ctx) {
            case FIRST_OBJECT_CONS_QUALIFIER:
            case SECOND_OBJECT_CONS_QUALIFIER:
            case FIRST_OBJECT_TYPE_QUALIFIER:
            case SECOND_OBJECT_TYPE_QUALIFIER:
                return SyntaxKind.OBJECT_KEYWORD;
            case FIRST_CLASS_TYPE_QUALIFIER:
            case SECOND_CLASS_TYPE_QUALIFIER:
            case THIRD_CLASS_TYPE_QUALIFIER:
            case FOURTH_CLASS_TYPE_QUALIFIER:
                return SyntaxKind.CLASS_KEYWORD;
            case FUNC_DEF_FIRST_QUALIFIER:
            case FUNC_DEF_SECOND_QUALIFIER:
            case FUNC_TYPE_FIRST_QUALIFIER:
            case FUNC_TYPE_SECOND_QUALIFIER:
            case OBJECT_METHOD_FIRST_QUALIFIER:
            case OBJECT_METHOD_SECOND_QUALIFIER:
            case OBJECT_METHOD_THIRD_QUALIFIER:
            case OBJECT_METHOD_FOURTH_QUALIFIER:
                return SyntaxKind.FUNCTION_KEYWORD;
            case MODULE_VAR_FIRST_QUAL:
            case MODULE_VAR_SECOND_QUAL:
            case MODULE_VAR_THIRD_QUAL:
            case OBJECT_MEMBER_VISIBILITY_QUAL:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case SERVICE_DECL_QUALIFIER:
                return SyntaxKind.SERVICE_KEYWORD;
            default:
                return SyntaxKind.NONE;
        }
    }

    /**
     * Check whether a token kind is a basic literal.
     *
     * @param kind Token kind to check
     * @return <code>true</code> if the given token kind belongs to a basic literal.<code>false</code> otherwise
     */
    private boolean isBasicLiteral(SyntaxKind kind) {
        switch (kind) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
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
            case FIELD_KEYWORD:
            case CLASS_KEYWORD:
                return true;
            default:
                return false;
        }
    }
}
