/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.parser.antlr4;

/**
 * Contains region ids of possible whitespace regions within each language construct according to grammar.
 *
 * @see WhiteSpaceUtil
 * @since 0.9.0
 */
public class WhiteSpaceRegions {
    // whitespace regions related to BFile
    public static final int BFILE_START = 0;
    public static final int BFILE_PKG_KEYWORD_TO_PKG_NAME_START = 1;
    public static final int BFILE_PKG_NAME_END_TO_SEMICOLON = 2;
    public static final int BFILE_PKG_DEC_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a import declaration
    public static final int IMPORT_DEC_IMPORT_KEYWORD_TO_PKG_NAME_START = 0;
    public static final int IMPORT_DEC_PKG_NAME_END_TO_NEXT = 1;
    public static final int IMPORT_DEC_AS_KEYWORD_TO_IDENTIFIER = 2;
    public static final int IMPORT_DEC_IDENTIFIER_TO_IMPORT_DEC_END = 3;
    public static final int IMPORT_DEC_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a service definition
    public static final int SERVICE_DEF_SERVICE_KEYWORD_TO_LEFT_ANGLE_BRACKET = 0;
    public static final int SERVICE_DEF_LEFT_ANGLE_BRACKET_TO_IDENTIFIER = 1;
    public static final int SERVICE_DEF_IDENTIFIER_TO_RIGHT_ANGLE_BRACKET = 2;
    public static final int SERVICE_DEF_RIGHT_ANGLE_BRACKET_TO_IDENTIFIER = 3;
    public static final int SERVICE_DEF_IDENTIFIER_TO_BODY_START = 4;
    public static final int SERVICE_DEF_BODY_START_TO_FIRST_CHILD = 5;
    public static final int SERVICE_DEF_END_TO_NEXT_TOKEN = 6;

    // whitespace regions in a resource definition
    public static final int RESOURCE_DEF_RESOURCE_KEYWORD_TO_IDENTIFIER = 0;
    public static final int RESOURCE_DEF_IDENTIFIER_TO_PARAM_LIST_START = 1;
    public static final int RESOURCE_DEF_PARAM_LIST_START_TO_FIRST_PARAM = 2;
    public static final int RESOURCE_DEF_PARAM_LIST_END_TO_BODY_START = 3;
    public static final int RESOURCE_DEF_BODY_START_TO_FIRST_CHILD = 4;
    public static final int RESOURCE_DEF_END_TO_NEXT_TOKEN = 5;

    // whitespace regions in a annotation attachment
    public static final int ANNOTATION_ATCHMNT_AT_KEYWORD_TO_IDENTIFIER = 0;
    public static final int ANNOTATION_ATCHMNT_IDENTIFIER_TO_ATTRIB_LIST_START = 1;
    public static final int ANNOTATION_ATCHMNT_ATTRIB_LIST_START_TO_FIRST_ATTRIB = 2;
    public static final int ANNOTATION_ATCHMNT_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a annotation attribute
    public static final int ANNOTATION_ATTRIB_KEY_START_TO_LAST_TOKEN = 0;
    public static final int ANNOTATION_ATTRIB_KEY_TO_COLON = 1;
    public static final int ANNOTATION_ATTRIB_COLON_TO_VALUE_START = 2;
    public static final int ANNOTATION_ATTRIB_VALUE_START_TO_LAST_TOKEN = 3;
    public static final int ANNOTATION_ATTRIB_VALUE_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a function definition
    public static final int FUNCTION_DEF_NATIVE_KEYWORD_TO_FUNCTION_KEYWORD = 0;
    public static final int FUNCTION_DEF_FUNCTION_KEYWORD_TO_IDENTIFIER_START = 1;
    public static final int FUNCTION_DEF_IDENTIFIER_TO_PARAM_LIST_START = 2;
    public static final int FUNCTION_DEF_PARAM_LIST_START_TO_NEXT_TOKEN = 3;
    public static final int FUNCTION_DEF_PARAM_LIST_END_TO_RETURN_PARAM_START = 4;
    public static final int FUNCTION_DEF_RETURN_PARAM_START_TO_NEXT_TOKEN = 5;
    public static final int FUNCTION_DEF_BODY_START_TO_LAST_TOKEN = 6;
    public static final int FUNCTION_DEF_BODY_START_TO_NEXT_TOKEN = 7;
    public static final int FUNCTION_DEF_BODY_END_TO_NEXT_TOKEN = 8;

    // whitespace regions in a connector definition
    public static final int CONNECTOR_DEF_CONNECTOR_KEYWORD_TO_IDENTIFIER = 0;
    public static final int CONNECTOR_DEF_IDENTIFIER_TO_PARAM_LIST_START = 1;
    public static final int CONNECTOR_DEF_PARAM_LIST_END_TO_BODY_START = 2;
    public static final int CONNECTOR_DEF_PARAM_BODY_START_TO_NEXT_TOKEN = 3;
    public static final int CONNECTOR_DEF_BODY_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a action definition
    public static final int ACTION_DEF_NATIVE_KEYWORD_TO_ACTION_KEYWORD = 0;
    public static final int ACTION_DEF_ACTION_KEYWORD_TO_IDENTIFIER_START = 1;
    public static final int ACTION_DEF_IDENTIFIER_TO_PARAM_LIST_START = 2;
    public static final int ACTION_DEF_PARAM_LIST_END_TO_RETURN_PARAM_START = 3;
    public static final int ACTION_DEF_BODY_START_TO_LAST_TOKEN = 4;
    public static final int ACTION_DEF_BODY_START_TO_NEXT_TOKEN = 5;
    public static final int ACTION_DEF_BODY_END_TO_NEXT_TOKEN = 6;

    // whitespace regions in a struct definition
    public static final int STRUCT_DEF_STRUCT_KEYWORD_TO_IDENTIFIER = 0;
    public static final int STRUCT_DEF_IDENTIFIER_TO_BODY_START = 1;
    public static final int STRUCT_DEF_BODY_START_TO_FIRST_TOKEN = 2;
    public static final int STRUCT_DEF_BODY_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a type mapper definition
    public static final int TYPE_MAP_DEF_NATIVE_KEYWORD_TO_SIGNATURE_START = 0;
    public static final int TYPE_MAP_DEF_TYPE_MAPPER_KEYWORD_TO_IDENTIFIER = 1;
    public static final int TYPE_MAP_DEF_IDENTIFIER_PARAM_WRAPPER_START = 2;
    public static final int TYPE_MAP_DEF_PARAM_WRAPPER_END_TO_RETURN_TYPE_WRAPPER_START = 3;
    public static final int TYPE_MAP_DEF_RETURN_TYPE_WRAPPER_TO_BODY_START = 4;
    public static final int TYPE_MAP_DEF_BODY_END_TO_NEXT_TOKEN = 5;

    // whitespace regions in a constant definition
    public static final int CONST_DEF_CONST_KEYWORD_TO_VAL_TYPE = 0;
    public static final int CONST_DEF_VAL_TYPE_TO_IDENTIFIER = 1;
    public static final int CONST_DEF_IDENTIFIER_TO_EQUAL_OPERATOR = 2;
    public static final int CONST_DEF_EQUAL_OPERATOR_TO_LITERAL_START = 3;
    public static final int CONST_DEF_LITERAL_END_TO_NEXT_TOKEN = 4;
    public static final int CONST_DEF_END_TO_NEXT_TOKEN = 5;

    // whitespace regions in a annotation definition
    public static final int ANNOTATION_DEF_ANNOTATION_KEYWORD_TO_IDENTIFIER = 0;
    public static final int ANNOTATION_DEF_IDENTIFIER_TO_ATTACH_KEYWORD = 1;
    public static final int ANNOTATION_DEF_BODY_START_TO_LAST_TOKEN = 2;
    public static final int ANNOTATION_DEF_BODY_START_TO_NEXT_TOKEN = 3;
    public static final int ANNOTATION_DEF_BODY_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in an annotation attachment point
    public static final int ANNOTATION_ATTACHMENT_POINT_PRECEDING_WS = 0;
    public static final int ANNOTATION_ATTACHMENT_POINT_FOLLOWING_WS = 1;

    // whitespace regions in a global variable def node
    public static final int GLOBAL_VAR_DEF_TYPE_NAME_TO_IDENTIFIER = 0;
    public static final int GLOBAL_VAR_DEF_IDENTIFIER_TO_EQUAL_OPERATOR = 1;
    public static final int GLOBAL_VAR_DEF_EQUAL_OPERATOR_TO_EXPRESSION_START = 2;
    public static final int GLOBAL_VAR_DEF_END_TO_LAST_TOKEN = 3;
    public static final int GLOBAL_VAR_DEF_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a variable def node
    public static final int VAR_DEF_TYPE_NAME_TO_IDENTIFIER = 0;
    public static final int VAR_DEF_IDENTIFIER_TO_EQUAL_OPERATOR = 1;
    public static final int VAR_DEF_EQUAL_OPERATOR_TO_EXPRESSION_START = 2;
    public static final int VAR_DEF_END_TO_LAST_TOKEN = 3;
    public static final int VAR_DEF_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a param def node
    public static final int PARAM_DEF_TYPENAME_START_TO_LAST_TOKEN = 0;
    public static final int PARAM_DEF_TYPENAME_TO_IDENTIFIER = 1;
    public static final int PARAM_DEF_END_TO_NEXT_TOKEN = 2;

    // whitespace regions in a worker declaration node
    public static final int WORKER_DEC_PRECEDING_WHITESPACE = 0;
    public static final int WORKER_DEC_WORKER_KEYWORD_TO_IDENTIFIER = 1;
    public static final int WORKER_DEC_IDENTIFIER_TO_BODY_START = 2;
    public static final int WORKER_DEC_BODY_START_TO_NEXT_TOKEN = 3;
    public static final int WORKER_DEC_END_TO_NEXT_TOKEN = 4;

    // whitespace regions of a type name
    public static final int TYPE_NAME_PRECEDING_WHITESPACE = 0;
    public static final int TYPE_NAME_FOLLOWING_WHITESPACE = 1;

    // whitespace regions in a name ref
    public static final int NAME_REF_START_TO_LAST_TOKEN = 0;
    public static final int NAME_REF_PACKAGE_NAME_TO_COLON = 1;
    public static final int NAME_REF_COLON_TO_REF_NAME = 2;
    public static final int NAME_REF_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in assign statement
    public static final int ASSIGN_STMT_PRECEDING_WHITESPACE = 0;
    public static final int ASSIGN_STMT_VAR_KEYWORD_TO_VAR_REF_LIST = 1;
    public static final int ASSIGN_STMT_VAR_REF_LIST_TO_EQUAL_OPERATOR = 2;
    public static final int ASSIGN_STMT_EQUAL_OPERATOR_TO_EXPRESSION_START = 3;
    public static final int ASSIGN_STMT_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a if clause
    public static final int IF_CLAUSE_PRECEDING_WHITESPACE = 0;
    public static final int IF_CLAUSE_IF_KEYWORD_TO_CONDITION_WRAPPER_START = 1;
    public static final int IF_CLAUSE_CONDITION_WRAPPER_START_TO_CONDITION = 2;
    public static final int IF_CLAUSE_CONDITION_WRAPPER_END_TO_BODY_START = 3;
    public static final int IF_CLAUSE_BODY_START_TO_NEXT_TOKEN = 4;
    public static final int IF_CLAUSE_BODY_END_TO_NEXT_TOKEN = 5;

    // whitespace regions in a else if clause
    public static final int ELSE_IF_CLAUSE_PRECEDING_WHITESPACE = 0;
    public static final int ELSE_IF_CLAUSE_ELSE_KEYWORD_TO_IF_KEYWORD = 1;
    public static final int ELSE_IF_CLAUSE_IF_KEYWORD_TO_CONDITION_WRAPPER_START = 2;
    public static final int ELSE_IF_CLAUSE_CONDITION_WRAPPER_START_TO_CONDITION = 3;
    public static final int ELSE_IF_CLAUSE_CONDITION_WRAPPER_END_TO_BODY_START = 4;
    public static final int ELSE_IF_CLAUSE_BODY_START_TO_NEXT_TOKEN = 5;
    public static final int ELSE_IF_CLAUSE_BODY_END_TO_NEXT_TOKEN = 6;

    // whitespace regions in a else clause
    public static final int ELSE_CLAUSE_PRECEDING_WHITESPACE = 0;
    public static final int ELSE_CLAUSE_ELSE_KEYWORD_TO_BODY_START = 1;
    public static final int ELSE_CLAUSE_BODY_START_TO_NEXT_TOKEN = 2;
    public static final int ELSE_CLAUSE_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a while statement
    public static final int WHILE_STMT_PRECEDING_WHITESPACE = 0;
    public static final int WHILE_STMT_WHILE_KEYWORD_TO_CONDITION_WRAPPER = 1;
    public static final int WHILE_STMT_CONDITION_WRAPPER_CONDITION_START = 2;
    public static final int WHILE_STMT_CONDITION_WRAPPER_TO_BODY_START = 3;
    public static final int WHILE_STMT_BODY_START_TO_FIRST_TOKEN = 4;
    public static final int WHILE_STMT_END_TO_NEXT_TOKEN = 5;

    // whitespace regions in break statement
    public static final int BREAK_STMT_PRECEDING_WHITESPACE = 0;
    public static final int BREAK_STMT_BREAK_KEYWORD_TO_END = 1;
    public static final int BREAK_STMT_END_TO_NEXT_TOKEN = 2;

    // whitespace regions in continue statement
    public static final int CONTINUE_STMT_PRECEDING_WHITESPACE = 0;
    public static final int CONTINUE_STMT_CONTINUE_KEYWORD_TO_END = 1;
    public static final int CONTINUE_STMT_END_TO_NEXT_TOKEN = 2;

    // whitespace regions in try clause
    public static final int TRY_CLAUSE_PRECEDING_WHITESPACE = 0;
    public static final int TRY_CLAUSE_TRY_KEYWORD_TO_BODY_START = 1;
    public static final int TRY_CLAUSE_BODY_START_TO_NEXT_TOKEN = 2;
    public static final int TRY_CLAUSE_END_NEXT_TOKEN = 3;

    // whitespace regions in finally clause
    public static final int FINALLY_CLAUSE_PRECEDING_WHITESPACE = 0;
    public static final int FINALLY_CLAUSE_FINALLY_KEYWORD_TO_BODY_START = 1;
    public static final int FINALLY_CLAUSE_BODY_START_TO_NEXT_TOKEN = 2;
    public static final int FINALLY_CLAUSE_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in catch clause
    public static final int CATCH_CLAUSE_PRECEDING_WHITESPACE = 0;
    public static final int CATCH_CLAUSE_CATCH_KEYWORD_TO_EXCEPTION_WRAPPER = 1;
    public static final int CATCH_CLAUSE_EXCEPTION_WRAPPER_START_TO_EXCEPTION_TYPE = 2;
    public static final int CATCH_CLAUSE_EXCEPTION_TYPE_TO_EXCEPTION_IDENTIFIER = 3;
    public static final int CATCH_CLAUSE_EXCEPTION_IDENTIFIER_TO_EXCEPTION_WRAPPER_END = 4;
    public static final int CATCH_CLAUSE_EXCEPTION_WRAPPER_END_TO_BODY_START = 5;
    public static final int CATCH_CLAUSE_BODY_START_TO_NEXT_TOKEN = 6;
    public static final int CATCH_CLAUSE_END_TO_NEXT_TOKEN = 7;

    // whitespace regions in throw statement
    public static final int THROW_STMT_PRECEDING_WHITESPACE = 0;
    public static final int THROW_STMT_THROW_KEYWORD_TO_EXPRESSION = 1;
    public static final int THROW_STMT_END_TO_NEXT_TOKEN = 2;

    // whitespace regions in return statement
    public static final int RETURN_STMT_PRECEDING_WHITESPACE = 0;
    public static final int RETURN_STMT_RETURN_KEYWORD_TO_EXPRESSION_LIST = 1;
    public static final int RETURN_STMT_END_PRECEDING_WHITESPACE = 2;
    public static final int RETURN_STMT_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in reply statement
    public static final int REPLY_STMT_PRECEDING_WHITESPACE = 0;
    public static final int REPLY_STMT_REPLY_KEYWORD_TO_EXPRESSION = 1;
    public static final int REPLY_STMT_END_TO_NEXT_TOKEN = 2;

    // whitespace regions in worker invocation statement
    public static final int WORKER_INVOKE_STMT_PRECEDING_WHITESPACE = 0;
    public static final int WORKER_INVOKE_STMT_EXP_TO_ARROW_OPERATOR = 1;
    public static final int WORKER_INVOKE_STMT_ARROW_OPERATOR_TO_WORKER_ID = 2;
    public static final int WORKER_INVOKE_STMT_WORKER_ID_TO_END = 3;
    public static final int WORKER_INVOKE_STMT_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in worker reply statement
    public static final int WORKER_REPLY_STMT_PRECEDING_WHITESPACE = 0;
    public static final int WORKER_REPLY_STMT_EXP_TO_ARROW_OPERATOR = 1;
    public static final int WORKER_REPLY_STMT_ARROW_OPERATOR_TO_WORKER_ID = 2;
    public static final int WORKER_REPLY_STMT_WORKER_ID_TO_END = 3;
    public static final int WORKER_REPLY_STMT_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a comment statement
    public static final int COMMENT_STMT_PRECEDING_WHITESPACE = 0;
    public static final int COMMENT_STMT_FOLLOWING_WHITESPACE = 1;

    // whitespace regions in an action invocation
    public static final int ACTION_INVOCATION_PRECEDING_WHITESPACE = 0;
    public static final int ACTION_INVOCATION_NAME_REF_TO_DOT_OPERATOR = 1;
    public static final int ACTION_INVOCATION_DOT_OPERATOR_TO_IDENTIFIER = 2;
    public static final int ACTION_INVOCATION_IDENTIFIER_TO_EXP_LIST_WRAPPER = 3;
    public static final int ACTION_INVOCATION_EXP_LIST_START_TO_NEXT_TOKEN = 4;
    public static final int ACTION_INVOCATION_END_TO_NEXT_TOKEN = 5;

    // whitespace regions in an action invocation statement
    public static final int ACTION_INVOCATION_STMT_PRECEDING_WHITESPACE = 0;
    public static final int ACTION_INVOCATION_STMT_END_TO_NEXT_TOKEN = 1;

    // whitespace regions in a function invocation expr
    public static final int FUNCTION_INVOCATION_EXPR_PRECEDING_WHITESPACE = 0;
    public static final int FUNCTION_INVOCATION_EXPR_NAME_REF_TO_ARG_LIST_START = 1;
    public static final int FUNCTION_INVOCATION_EXPR_ARG_LIST_START_TO_NEXT_TOKEN = 2;
    public static final int FUNCTION_INVOCATION_EXPR_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a function invocation stmt :Note other regions are coming from expr
    public static final int FUNCTION_INVOCATION_STMT_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a simple literal
    public static final int SIMPLE_LITERAL_PRECEDING_WHITESPACE = 0;
    public static final int SIMPLE_LITERAL_FOLLOWING_WHITESPACE = 1;

    // whitespace regions in an array init expression
    public static final int ARRAY_INIT_EXP_PRECEDING_WHITESPACE = 0;
    public static final int ARRAY_INIT_EXP_OPENING_SQUARE_BRACE_TO_EXP_LIST_START = 1;
    public static final int ARRAY_INIT_EXP_EXP_LIST_END_TO_CLOSING_SQUARE_BRACE = 2;
    public static final int ARRAY_INIT_EXP_FOLLOWING_WHITESPACE = 3;

    // whitespace regions in an map struct key value expr
    public static final int MAP_STRUCT_KEY_VAL_EXP_PRECEDING_WHITESPACE = 0;
    public static final int MAP_STRUCT_KEY_VAL_EXP_KEY_EXP_TO_COLON = 1;
    public static final int MAP_STRUCT_KEY_VAL_EXP_COLON_TO_VAL_EXP = 2;
    public static final int MAP_STRUCT_KEY_VAL_EXP_FOLLOWING_WHITESPACE = 3;

    // whitespace regions in a binary expression
    public static final int BINARY_EXP_PRECEDING_WHITESPACE = 0;
    public static final int BINARY_EXP_LEFT_EXP_TO_OPERATOR = 1;
    public static final int BINARY_EXP_OPERATOR_TO_RIGHT_EXP = 2;
    public static final int BINARY_EXP_FOLLOWING_WHITESPACE = 3;

    // whitespace regions in a map struct literal
    public static final int MAP_STRUCT_LITERAL_PRECEDING_WHITESPACE = 0;
    public static final int MAP_STRUCT_LITERAL_BODY_START_TO_NEXT_TOKEN = 1;
    public static final int MAP_STRUCT_LITERAL_FOLLOWING_WHITESPACE = 2;

    // whitespace regions in a connector init expr
    public static final int CONNECTOR_INIT_EXP_PRECEDING_WHITESPACE = 0;
    public static final int CONNECTOR_INIT_EXP_CREATE_KEYWORD_TO_NAME_REF = 1;
    public static final int CONNECTOR_INIT_EXP_NAME_REF_TO_EXP_LIST_WRAPPER = 2;
    public static final int CONNECTOR_INIT_EXP_FOLLOWING_WHITESPACE = 3;

    // whitespace regions in a struct field identifier
    public static final int STRUCT_FIELD_IDENTIFIER_PRECEDING_WHITESPACE = 0;
    public static final int STRUCT_FIELD_IDENTIFIER_FOLLOWING_WHITESPACE = 1;

    // whitespace regions in a map array var identifier
    public static final int MAP_ARR_VAR_ID_PRECEDING_WHITESPACE = 0;
    public static final int MAP_ARR_VAR_ID_EXP_OPENING_SQUARE_BRACE_PRECEDING = 1;
    public static final int MAP_ARR_VAR_ID_FOLLOWING_WHITESPACE = 2;

    // whitespace regions in a backtick literal
    public static final int BACK_TICK_LIT_PRECEDING_WHITESPACE = 0;
    public static final int BACK_TICK_LIT_FOLLOWING_WHITESPACE = 2;

    // white space regions in a typecast expr
    public static final int TYPE_CAST_EXP_PRECEDING_WHITESPACE = 0;
    public static final int TYPE_CAST_EXP_TYPE_CAST_START_TO_TYPE_NAME = 1;
    public static final int TYPE_CAST_EXP_TYPE_CAST_END_TO_EXP = 2;
    public static final int TYPE_CAST_EXP_FOLLOWING_WHITESPACE = 3;

    // white space regions in a type-conversion expr
    public static final int TYPE_CONVERSION_EXP_PRECEDING_WHITESPACE = 0;
    public static final int TYPE_CONVERSION_EXP_TYPE_CAST_START_TO_TYPE_NAME = 1;
    public static final int TYPE_CONVERSION_EXP_TYPE_CAST_END_TO_EXP = 2;
    public static final int TYPE_CONVERSION_EXP_FOLLOWING_WHITESPACE = 3;

    // white space regions in a unary expr
    public static final int UNARY_EXP_PRECEDING_WHITESPACE = 0;
    public static final int UNARY_EXP_OPERATOR_TO_EXP = 1;
    public static final int UNARY_EXP_FOLLOWING_WHITESPACE = 2;

    // white space regions in a field def
    public static final int FILED_DEF_TYPE_NAME_TO_ID = 0;
    public static final int FILED_DEF_ID_TO_NEXT_TOKEN = 1;
    public static final int FILED_DEF_EQUAL_OPERATOR_TO_LITERAL = 2;
    public static final int FILED_DEF_LITERAL_TO_NEXT_TOKEN = 3;
    public static final int FILED_DEF_FOLLOWING_WHITESPACE = 4;

    // whitespace regions in a transform statement
    public static final int TRANSFORM_STMT_PRECEDING_WHITESPACE = 0;
    public static final int TRANSFORM_STMT_TO_BODY_START = 1;
    public static final int TRANSFORM_STMT_BODY_START_TO_FIRST_CHILD = 2;
    public static final int TRANSFORM_STMT_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a transaction statement
    public static final int TRANSACTION_STMT_PRECEDING_WHITESPACE = 0;
    public static final int TRANSACTION_STMT_TRANSACTION_KEYWORD_TO_BODY_START = 1;
    public static final int TRANSACTION_STMT_BODY_START_TO_FIRST_TOKEN = 2;
    public static final int TRANSACTION_STMT_BODY_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a committed clause
    public static final int COMMITTED_STMT_PRECEDING_WHITESPACE = 0;
    public static final int COMMITTED_STMT_TRANSACTION_KEYWORD_TO_BODY_START = 1;
    public static final int COMMITTED_STMT_BODY_START_TO_FIRST_TOKEN = 2;
    public static final int COMMITTED_STMT_BODY_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a aborted clause
    public static final int ABORTED_STMT_PRECEDING_WHITESPACE = 0;
    public static final int ABORTED_STMT_TRANSACTION_KEYWORD_TO_BODY_START = 1;
    public static final int ABORTED_STMT_BODY_START_TO_FIRST_TOKEN = 2;
    public static final int ABORTED_STMT_BODY_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in abort statement
    public static final int ABORT_STMT_PRECEDING_WHITESPACE = 0;
    public static final int ABORT_STMT_ABORT_KEYWORD_TO_END = 1;
    public static final int ABORT_STMT_END_TO_NEXT_TOKEN = 2;

    public static final int FORK_PRECEDING_WHITESPACE = 0;
    public static final int FORK_KEYWORD_TO_BODY_START = 1;
    public static final int FORK_START_TO_FIRST_CHILD = 2;
    public static final int FORK_BODY_END_TO_NEXT_TOKEN = 3;

    public static final int JOIN_PRECEDING_WHITESPACE = 0;
    public static final int JOIN_KEYWORD_TO_BODY_START = 1;
    public static final int JOIN_CONDITION_WRAPPER_END_TO_PARAM_WRAPPER = 2;
    public static final int JOIN_PARAM_TYPE_TO_PARAM_IDENTIFIER = 3;
    public static final int JOIN_PARAM_IDENTIFIER_TO_PARAM_WRAPPER_END = 4;
    public static final int JOIN_PARAM_WRAPPER_END_TO_JOIN_START = 5;
    public static final int JOIN_START_TO_FIRST_CHILD = 6;
    public static final int JOIN_BODY_END_TO_NEXT_TOKEN = 7;

    public static final int TIMEOUT_PRECEDING_WHITESPACE = 0;
    public static final int TIMEOUT_KEYWORD_TO_BODY_START = 1;
    public static final int TIMEOUT_CONDITION_WRAPPER_START_TO_CONDITION = 2;
    public static final int TIMEOUT_CONDITION_WRAPPER_END_TO_PARAM_WRAPPER = 3;
    public static final int TIMEOUT_PARAM_TYPE_TO_PARAM_IDENTIFIER = 4;
    public static final int TIMEOUT_PARAM_IDENTIFIER_TO_PARAM_WRAPPER_END = 5;
    public static final int TIMEOUT_PARAM_WRAPPER_END_TO_TIMEOUT_START = 6;
    public static final int TIMEOUT_START_TO_FIRST_CHILD = 7;
    public static final int TIMEOUT_BODY_END_TO_NEXT_TOKEN = 8;

    public static final int JOIN_CONDITION_WRAPPER_TO_JOIN_CONDITION = 0;
    public static final int JOIN_CONDITION_TYPE_TO_JOIN_CONDITION_COUNT = 1;
    public static final int JOIN_CONDITION_END_TO_CONDITION_WRAPPER_END = 2;

    public static final int JOIN_WORKER_PRECEDING_WHITESPACE = 0;
    public static final int JOIN_WORKER_END_TO_NEXT_TOKEN = 1;

    // whitespace regions in an index based var ref expression
    public static final int INDEX_VAR_REF_EXPR_PRECEDING_WHITESPACE = 0;
    public static final int INDEX_VAR_REF_EXPR_VAR_REF_TO_INDEX_EXPR_WRAPPER = 1;
    public static final int INDEX_VAR_REF_EXPR_INDEX_EXPR_WRAPPER_TO_INDEX_EXPR_START = 2;
    public static final int INDEX_VAR_REF_EXPR_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in an field based var ref expression
    public static final int FIELD_VAR_REF_EXPR_PRECEDING_WHITESPACE = 0;
    public static final int FIELD_VAR_REF_EXPR_VAR_REF_TO_DOT_OPERATOR = 1;
    public static final int FIELD_VAR_REF_EXPR_DOT_OPERATOR_TO_FIELD_NAME_START = 2;
    public static final int FIELD_VAR_REF_EXPR_END_TO_NEXT_TOKEN = 3;
}
