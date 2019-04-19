/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// This is a generated file. Not intended for manual editing.
package io.ballerina.plugins.idea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import io.ballerina.plugins.idea.psi.impl.*;

public interface BallerinaTypes {

  IElementType ABORTED_CLAUSE = new BallerinaCompositeElementType("ABORTED_CLAUSE");
  IElementType ABORT_STATEMENT = new BallerinaCompositeElementType("ABORT_STATEMENT");
  IElementType ACTION_INVOCATION = new BallerinaCompositeElementType("ACTION_INVOCATION");
  IElementType ACTION_INVOCATION_EXPRESSION = new BallerinaCompositeElementType("ACTION_INVOCATION_EXPRESSION");
  IElementType ALIAS = new BallerinaCompositeElementType("ALIAS");
  IElementType ANNOTATION_ATTACHMENT = new BallerinaCompositeElementType("ANNOTATION_ATTACHMENT");
  IElementType ANNOTATION_DEFINITION = new BallerinaCompositeElementType("ANNOTATION_DEFINITION");
  IElementType ANY_DATA_TYPE_NAME = new BallerinaCompositeElementType("ANY_DATA_TYPE_NAME");
  IElementType ANY_IDENTIFIER_NAME = new BallerinaCompositeElementType("ANY_IDENTIFIER_NAME");
  IElementType ANY_TYPE_NAME = new BallerinaCompositeElementType("ANY_TYPE_NAME");
  IElementType ARRAY_LITERAL = new BallerinaCompositeElementType("ARRAY_LITERAL");
  IElementType ARRAY_LITERAL_EXPRESSION = new BallerinaCompositeElementType("ARRAY_LITERAL_EXPRESSION");
  IElementType ARRAY_TYPE_NAME = new BallerinaCompositeElementType("ARRAY_TYPE_NAME");
  IElementType ARROW_FUNCTION = new BallerinaCompositeElementType("ARROW_FUNCTION");
  IElementType ARROW_FUNCTION_EXPRESSION = new BallerinaCompositeElementType("ARROW_FUNCTION_EXPRESSION");
  IElementType ARROW_PARAM = new BallerinaCompositeElementType("ARROW_PARAM");
  IElementType ASSIGNMENT_STATEMENT = new BallerinaCompositeElementType("ASSIGNMENT_STATEMENT");
  IElementType ATTACHED_OBJECT = new BallerinaCompositeElementType("ATTACHED_OBJECT");
  IElementType ATTACHMENT_POINT = new BallerinaCompositeElementType("ATTACHMENT_POINT");
  IElementType ATTRIBUTE = new BallerinaCompositeElementType("ATTRIBUTE");
  IElementType BACKTICKED_BLOCK = new BallerinaCompositeElementType("BACKTICKED_BLOCK");
  IElementType BINARY_ADD_SUB_EXPRESSION = new BallerinaCompositeElementType("BINARY_ADD_SUB_EXPRESSION");
  IElementType BINARY_AND_EXPRESSION = new BallerinaCompositeElementType("BINARY_AND_EXPRESSION");
  IElementType BINARY_COMPARE_EXPRESSION = new BallerinaCompositeElementType("BINARY_COMPARE_EXPRESSION");
  IElementType BINARY_DIV_MUL_MOD_EXPRESSION = new BallerinaCompositeElementType("BINARY_DIV_MUL_MOD_EXPRESSION");
  IElementType BINARY_EQUAL_EXPRESSION = new BallerinaCompositeElementType("BINARY_EQUAL_EXPRESSION");
  IElementType BINARY_OR_EXPRESSION = new BallerinaCompositeElementType("BINARY_OR_EXPRESSION");
  IElementType BINARY_REF_EQUAL_EXPRESSION = new BallerinaCompositeElementType("BINARY_REF_EQUAL_EXPRESSION");
  IElementType BINDING_PATTERN = new BallerinaCompositeElementType("BINDING_PATTERN");
  IElementType BINDING_REF_PATTERN = new BallerinaCompositeElementType("BINDING_REF_PATTERN");
  IElementType BITWISE_EXPRESSION = new BallerinaCompositeElementType("BITWISE_EXPRESSION");
  IElementType BITWISE_SHIFT_EXPRESSION = new BallerinaCompositeElementType("BITWISE_SHIFT_EXPRESSION");
  IElementType BLOB_LITERAL = new BallerinaCompositeElementType("BLOB_LITERAL");
  IElementType BLOCK = new BallerinaCompositeElementType("BLOCK");
  IElementType BRACED_OR_TUPLE_EXPRESSION = new BallerinaCompositeElementType("BRACED_OR_TUPLE_EXPRESSION");
  IElementType BREAK_STATEMENT = new BallerinaCompositeElementType("BREAK_STATEMENT");
  IElementType BUILT_IN_REFERENCE_TYPE_NAME = new BallerinaCompositeElementType("BUILT_IN_REFERENCE_TYPE_NAME");
  IElementType CALLABLE_UNIT_BODY = new BallerinaCompositeElementType("CALLABLE_UNIT_BODY");
  IElementType CALLABLE_UNIT_SIGNATURE = new BallerinaCompositeElementType("CALLABLE_UNIT_SIGNATURE");
  IElementType CATCH_CLAUSE = new BallerinaCompositeElementType("CATCH_CLAUSE");
  IElementType CATCH_CLAUSES = new BallerinaCompositeElementType("CATCH_CLAUSES");
  IElementType CHANNEL_TYPE = new BallerinaCompositeElementType("CHANNEL_TYPE");
  IElementType CHECKED_EXPRESSION = new BallerinaCompositeElementType("CHECKED_EXPRESSION");
  IElementType CLOSE_TAG = new BallerinaCompositeElementType("CLOSE_TAG");
  IElementType COMMENT = new BallerinaCompositeElementType("COMMENT");
  IElementType COMMITTED_ABORTED_CLAUSES = new BallerinaCompositeElementType("COMMITTED_ABORTED_CLAUSES");
  IElementType COMMITTED_CLAUSE = new BallerinaCompositeElementType("COMMITTED_CLAUSE");
  IElementType COMPLETE_PACKAGE_NAME = new BallerinaCompositeElementType("COMPLETE_PACKAGE_NAME");
  IElementType COMPOUND_ASSIGNMENT_STATEMENT = new BallerinaCompositeElementType("COMPOUND_ASSIGNMENT_STATEMENT");
  IElementType COMPOUND_OPERATOR = new BallerinaCompositeElementType("COMPOUND_OPERATOR");
  IElementType CONSTANT_DEFINITION = new BallerinaCompositeElementType("CONSTANT_DEFINITION");
  IElementType CONTENT = new BallerinaCompositeElementType("CONTENT");
  IElementType CONTINUE_STATEMENT = new BallerinaCompositeElementType("CONTINUE_STATEMENT");
  IElementType DEFAULTABLE_PARAMETER = new BallerinaCompositeElementType("DEFAULTABLE_PARAMETER");
  IElementType DEFINITION = new BallerinaCompositeElementType("DEFINITION");
  IElementType DEFINITION_REFERENCE_TYPE = new BallerinaCompositeElementType("DEFINITION_REFERENCE_TYPE");
  IElementType DEPRECATED_ATTACHMENT = new BallerinaCompositeElementType("DEPRECATED_ATTACHMENT");
  IElementType DEPRECATED_TEMPLATE_INLINE_CODE = new BallerinaCompositeElementType("DEPRECATED_TEMPLATE_INLINE_CODE");
  IElementType DEPRECATED_TEXT = new BallerinaCompositeElementType("DEPRECATED_TEXT");
  IElementType DOCUMENTATION_CONTENT = new BallerinaCompositeElementType("DOCUMENTATION_CONTENT");
  IElementType DOCUMENTATION_DEFINITION_REFERENCE = new BallerinaCompositeElementType("DOCUMENTATION_DEFINITION_REFERENCE");
  IElementType DOCUMENTATION_LINE = new BallerinaCompositeElementType("DOCUMENTATION_LINE");
  IElementType DOCUMENTATION_REFERENCE = new BallerinaCompositeElementType("DOCUMENTATION_REFERENCE");
  IElementType DOCUMENTATION_STRING = new BallerinaCompositeElementType("DOCUMENTATION_STRING");
  IElementType DOCUMENTATION_TEXT = new BallerinaCompositeElementType("DOCUMENTATION_TEXT");
  IElementType DOC_PARAMETER_DESCRIPTION = new BallerinaCompositeElementType("DOC_PARAMETER_DESCRIPTION");
  IElementType DOUBLE_BACKTICKED_BLOCK = new BallerinaCompositeElementType("DOUBLE_BACKTICKED_BLOCK");
  IElementType DOUBLE_BACK_TICK_DEPRECATED_INLINE_CODE = new BallerinaCompositeElementType("DOUBLE_BACK_TICK_DEPRECATED_INLINE_CODE");
  IElementType ELEMENT = new BallerinaCompositeElementType("ELEMENT");
  IElementType ELSE_CLAUSE = new BallerinaCompositeElementType("ELSE_CLAUSE");
  IElementType ELSE_IF_CLAUSE = new BallerinaCompositeElementType("ELSE_IF_CLAUSE");
  IElementType ELVIS_EXPRESSION = new BallerinaCompositeElementType("ELVIS_EXPRESSION");
  IElementType EMPTY_TAG = new BallerinaCompositeElementType("EMPTY_TAG");
  IElementType EMPTY_TUPLE_LITERAL = new BallerinaCompositeElementType("EMPTY_TUPLE_LITERAL");
  IElementType ENTRY_BINDING_PATTERN = new BallerinaCompositeElementType("ENTRY_BINDING_PATTERN");
  IElementType ENTRY_REF_BINDING_PATTERN = new BallerinaCompositeElementType("ENTRY_REF_BINDING_PATTERN");
  IElementType ERROR_BINDING_PATTERN = new BallerinaCompositeElementType("ERROR_BINDING_PATTERN");
  IElementType ERROR_CONSTRUCTOR_EXPRESSION = new BallerinaCompositeElementType("ERROR_CONSTRUCTOR_EXPRESSION");
  IElementType ERROR_DESTRUCTURING_STATEMENT = new BallerinaCompositeElementType("ERROR_DESTRUCTURING_STATEMENT");
  IElementType ERROR_REF_BINDING_PATTERN = new BallerinaCompositeElementType("ERROR_REF_BINDING_PATTERN");
  IElementType ERROR_TYPE_NAME = new BallerinaCompositeElementType("ERROR_TYPE_NAME");
  IElementType EXPRESSION = new BallerinaCompositeElementType("EXPRESSION");
  IElementType EXPRESSION_LIST = new BallerinaCompositeElementType("EXPRESSION_LIST");
  IElementType EXPRESSION_STMT = new BallerinaCompositeElementType("EXPRESSION_STMT");
  IElementType FIELD = new BallerinaCompositeElementType("FIELD");
  IElementType FIELD_BINDING_PATTERN = new BallerinaCompositeElementType("FIELD_BINDING_PATTERN");
  IElementType FIELD_DEFINITION = new BallerinaCompositeElementType("FIELD_DEFINITION");
  IElementType FIELD_REF_BINDING_PATTERN = new BallerinaCompositeElementType("FIELD_REF_BINDING_PATTERN");
  IElementType FIELD_VARIABLE_REFERENCE = new BallerinaCompositeElementType("FIELD_VARIABLE_REFERENCE");
  IElementType FINALLY_CLAUSE = new BallerinaCompositeElementType("FINALLY_CLAUSE");
  IElementType FINITE_TYPE = new BallerinaCompositeElementType("FINITE_TYPE");
  IElementType FINITE_TYPE_UNIT = new BallerinaCompositeElementType("FINITE_TYPE_UNIT");
  IElementType FLOATING_POINT_LITERAL = new BallerinaCompositeElementType("FLOATING_POINT_LITERAL");
  IElementType FLUSH_WORKER = new BallerinaCompositeElementType("FLUSH_WORKER");
  IElementType FLUSH_WORKER_EXPRESSION = new BallerinaCompositeElementType("FLUSH_WORKER_EXPRESSION");
  IElementType FOREACH_STATEMENT = new BallerinaCompositeElementType("FOREACH_STATEMENT");
  IElementType FOREVER_STATEMENT = new BallerinaCompositeElementType("FOREVER_STATEMENT");
  IElementType FOREVER_STATEMENT_BODY = new BallerinaCompositeElementType("FOREVER_STATEMENT_BODY");
  IElementType FORK_JOIN_STATEMENT = new BallerinaCompositeElementType("FORK_JOIN_STATEMENT");
  IElementType FORMAL_PARAMETER_LIST = new BallerinaCompositeElementType("FORMAL_PARAMETER_LIST");
  IElementType FUNCTION_DEFINITION = new BallerinaCompositeElementType("FUNCTION_DEFINITION");
  IElementType FUNCTION_INVOCATION = new BallerinaCompositeElementType("FUNCTION_INVOCATION");
  IElementType FUNCTION_INVOCATION_REFERENCE = new BallerinaCompositeElementType("FUNCTION_INVOCATION_REFERENCE");
  IElementType FUNCTION_NAME_REFERENCE = new BallerinaCompositeElementType("FUNCTION_NAME_REFERENCE");
  IElementType FUNCTION_TYPE_NAME = new BallerinaCompositeElementType("FUNCTION_TYPE_NAME");
  IElementType FUTURE_TYPE_NAME = new BallerinaCompositeElementType("FUTURE_TYPE_NAME");
  IElementType GLOBAL_VARIABLE_DEFINITION = new BallerinaCompositeElementType("GLOBAL_VARIABLE_DEFINITION");
  IElementType GROUP_BY_CLAUSE = new BallerinaCompositeElementType("GROUP_BY_CLAUSE");
  IElementType GROUP_TYPE_NAME = new BallerinaCompositeElementType("GROUP_TYPE_NAME");
  IElementType HAVING_CLAUSE = new BallerinaCompositeElementType("HAVING_CLAUSE");
  IElementType IF_CLAUSE = new BallerinaCompositeElementType("IF_CLAUSE");
  IElementType IF_ELSE_STATEMENT = new BallerinaCompositeElementType("IF_ELSE_STATEMENT");
  IElementType IMPORT_DECLARATION = new BallerinaCompositeElementType("IMPORT_DECLARATION");
  IElementType INDEX = new BallerinaCompositeElementType("INDEX");
  IElementType INIT_WITHOUT_TYPE = new BallerinaCompositeElementType("INIT_WITHOUT_TYPE");
  IElementType INIT_WITH_TYPE = new BallerinaCompositeElementType("INIT_WITH_TYPE");
  IElementType INTEGER_LITERAL = new BallerinaCompositeElementType("INTEGER_LITERAL");
  IElementType INTEGER_RANGE_EXPRESSION = new BallerinaCompositeElementType("INTEGER_RANGE_EXPRESSION");
  IElementType INT_RANGE_EXPRESSION = new BallerinaCompositeElementType("INT_RANGE_EXPRESSION");
  IElementType INVOCATION = new BallerinaCompositeElementType("INVOCATION");
  IElementType INVOCATION_ARG = new BallerinaCompositeElementType("INVOCATION_ARG");
  IElementType INVOCATION_ARG_LIST = new BallerinaCompositeElementType("INVOCATION_ARG_LIST");
  IElementType INVOCATION_REFERENCE = new BallerinaCompositeElementType("INVOCATION_REFERENCE");
  IElementType JOIN_STREAMING_INPUT = new BallerinaCompositeElementType("JOIN_STREAMING_INPUT");
  IElementType JOIN_TYPE = new BallerinaCompositeElementType("JOIN_TYPE");
  IElementType JSON_TYPE_NAME = new BallerinaCompositeElementType("JSON_TYPE_NAME");
  IElementType LAMBDA_FUNCTION = new BallerinaCompositeElementType("LAMBDA_FUNCTION");
  IElementType LAMBDA_FUNCTION_EXPRESSION = new BallerinaCompositeElementType("LAMBDA_FUNCTION_EXPRESSION");
  IElementType LAMBDA_RETURN_PARAMETER = new BallerinaCompositeElementType("LAMBDA_RETURN_PARAMETER");
  IElementType LIMIT_CLAUSE = new BallerinaCompositeElementType("LIMIT_CLAUSE");
  IElementType LOCK_STATEMENT = new BallerinaCompositeElementType("LOCK_STATEMENT");
  IElementType MAP_ARRAY_VARIABLE_REFERENCE = new BallerinaCompositeElementType("MAP_ARRAY_VARIABLE_REFERENCE");
  IElementType MAP_TYPE_NAME = new BallerinaCompositeElementType("MAP_TYPE_NAME");
  IElementType MATCH_PATTERN_CLAUSE = new BallerinaCompositeElementType("MATCH_PATTERN_CLAUSE");
  IElementType MATCH_STATEMENT = new BallerinaCompositeElementType("MATCH_STATEMENT");
  IElementType MATCH_STATEMENT_BODY = new BallerinaCompositeElementType("MATCH_STATEMENT_BODY");
  IElementType NAMED_ARGS = new BallerinaCompositeElementType("NAMED_ARGS");
  IElementType NAMESPACE_DECLARATION = new BallerinaCompositeElementType("NAMESPACE_DECLARATION");
  IElementType NAMESPACE_DECLARATION_STATEMENT = new BallerinaCompositeElementType("NAMESPACE_DECLARATION_STATEMENT");
  IElementType NAME_REFERENCE = new BallerinaCompositeElementType("NAME_REFERENCE");
  IElementType NULLABLE_TYPE_NAME = new BallerinaCompositeElementType("NULLABLE_TYPE_NAME");
  IElementType OBJECT_BODY = new BallerinaCompositeElementType("OBJECT_BODY");
  IElementType OBJECT_FIELD_DEFINITION = new BallerinaCompositeElementType("OBJECT_FIELD_DEFINITION");
  IElementType OBJECT_FUNCTION_DEFINITION = new BallerinaCompositeElementType("OBJECT_FUNCTION_DEFINITION");
  IElementType OBJECT_TYPE_NAME = new BallerinaCompositeElementType("OBJECT_TYPE_NAME");
  IElementType ON_RETRY_CLAUSE = new BallerinaCompositeElementType("ON_RETRY_CLAUSE");
  IElementType ORDER_BY_CLAUSE = new BallerinaCompositeElementType("ORDER_BY_CLAUSE");
  IElementType ORDER_BY_TYPE = new BallerinaCompositeElementType("ORDER_BY_TYPE");
  IElementType ORDER_BY_VARIABLE = new BallerinaCompositeElementType("ORDER_BY_VARIABLE");
  IElementType ORG_NAME = new BallerinaCompositeElementType("ORG_NAME");
  IElementType OUTPUT_RATE_LIMIT = new BallerinaCompositeElementType("OUTPUT_RATE_LIMIT");
  IElementType PACKAGE_NAME = new BallerinaCompositeElementType("PACKAGE_NAME");
  IElementType PACKAGE_REFERENCE = new BallerinaCompositeElementType("PACKAGE_REFERENCE");
  IElementType PACKAGE_VERSION = new BallerinaCompositeElementType("PACKAGE_VERSION");
  IElementType PANIC_STATEMENT = new BallerinaCompositeElementType("PANIC_STATEMENT");
  IElementType PARAMETER = new BallerinaCompositeElementType("PARAMETER");
  IElementType PARAMETER_DESCRIPTION = new BallerinaCompositeElementType("PARAMETER_DESCRIPTION");
  IElementType PARAMETER_DOCUMENTATION = new BallerinaCompositeElementType("PARAMETER_DOCUMENTATION");
  IElementType PARAMETER_DOCUMENTATION_LINE = new BallerinaCompositeElementType("PARAMETER_DOCUMENTATION_LINE");
  IElementType PARAMETER_LIST = new BallerinaCompositeElementType("PARAMETER_LIST");
  IElementType PARAMETER_TYPE_NAME = new BallerinaCompositeElementType("PARAMETER_TYPE_NAME");
  IElementType PARAMETER_TYPE_NAME_LIST = new BallerinaCompositeElementType("PARAMETER_TYPE_NAME_LIST");
  IElementType PARAMETER_WITH_TYPE = new BallerinaCompositeElementType("PARAMETER_WITH_TYPE");
  IElementType PATTERN_CLAUSE = new BallerinaCompositeElementType("PATTERN_CLAUSE");
  IElementType PATTERN_STREAMING_EDGE_INPUT = new BallerinaCompositeElementType("PATTERN_STREAMING_EDGE_INPUT");
  IElementType PATTERN_STREAMING_INPUT = new BallerinaCompositeElementType("PATTERN_STREAMING_INPUT");
  IElementType PROC_INS = new BallerinaCompositeElementType("PROC_INS");
  IElementType RECORD_BINDING_PATTERN = new BallerinaCompositeElementType("RECORD_BINDING_PATTERN");
  IElementType RECORD_DESTRUCTURING_STATEMENT = new BallerinaCompositeElementType("RECORD_DESTRUCTURING_STATEMENT");
  IElementType RECORD_FIELD_DEFINITION_LIST = new BallerinaCompositeElementType("RECORD_FIELD_DEFINITION_LIST");
  IElementType RECORD_KEY = new BallerinaCompositeElementType("RECORD_KEY");
  IElementType RECORD_KEY_VALUE = new BallerinaCompositeElementType("RECORD_KEY_VALUE");
  IElementType RECORD_LITERAL = new BallerinaCompositeElementType("RECORD_LITERAL");
  IElementType RECORD_LITERAL_BODY = new BallerinaCompositeElementType("RECORD_LITERAL_BODY");
  IElementType RECORD_LITERAL_EXPRESSION = new BallerinaCompositeElementType("RECORD_LITERAL_EXPRESSION");
  IElementType RECORD_REF_BINDING_PATTERN = new BallerinaCompositeElementType("RECORD_REF_BINDING_PATTERN");
  IElementType RECORD_REST_FIELD_DEFINITION = new BallerinaCompositeElementType("RECORD_REST_FIELD_DEFINITION");
  IElementType RECORD_TYPE_NAME = new BallerinaCompositeElementType("RECORD_TYPE_NAME");
  IElementType REFERENCE_TYPE_NAME = new BallerinaCompositeElementType("REFERENCE_TYPE_NAME");
  IElementType RESERVED_WORD = new BallerinaCompositeElementType("RESERVED_WORD");
  IElementType REST_ARGS = new BallerinaCompositeElementType("REST_ARGS");
  IElementType REST_BINDING_PATTERN = new BallerinaCompositeElementType("REST_BINDING_PATTERN");
  IElementType REST_PARAMETER = new BallerinaCompositeElementType("REST_PARAMETER");
  IElementType REST_REF_BINDING_PATTERN = new BallerinaCompositeElementType("REST_REF_BINDING_PATTERN");
  IElementType RETRIES_STATEMENT = new BallerinaCompositeElementType("RETRIES_STATEMENT");
  IElementType RETRY_STATEMENT = new BallerinaCompositeElementType("RETRY_STATEMENT");
  IElementType RETURN_PARAMETER = new BallerinaCompositeElementType("RETURN_PARAMETER");
  IElementType RETURN_PARAMETER_DESCRIPTION = new BallerinaCompositeElementType("RETURN_PARAMETER_DESCRIPTION");
  IElementType RETURN_PARAMETER_DOCUMENTATION = new BallerinaCompositeElementType("RETURN_PARAMETER_DOCUMENTATION");
  IElementType RETURN_PARAMETER_DOCUMENTATION_LINE = new BallerinaCompositeElementType("RETURN_PARAMETER_DOCUMENTATION_LINE");
  IElementType RETURN_STATEMENT = new BallerinaCompositeElementType("RETURN_STATEMENT");
  IElementType RETURN_TYPE = new BallerinaCompositeElementType("RETURN_TYPE");
  IElementType SEALED_LITERAL = new BallerinaCompositeElementType("SEALED_LITERAL");
  IElementType SELECT_CLAUSE = new BallerinaCompositeElementType("SELECT_CLAUSE");
  IElementType SELECT_EXPRESSION = new BallerinaCompositeElementType("SELECT_EXPRESSION");
  IElementType SELECT_EXPRESSION_LIST = new BallerinaCompositeElementType("SELECT_EXPRESSION_LIST");
  IElementType SERVICE_BODY = new BallerinaCompositeElementType("SERVICE_BODY");
  IElementType SERVICE_BODY_MEMBER = new BallerinaCompositeElementType("SERVICE_BODY_MEMBER");
  IElementType SERVICE_CONSTRUCTOR_EXPRESSION = new BallerinaCompositeElementType("SERVICE_CONSTRUCTOR_EXPRESSION");
  IElementType SERVICE_DEFINITION = new BallerinaCompositeElementType("SERVICE_DEFINITION");
  IElementType SERVICE_TYPE_NAME = new BallerinaCompositeElementType("SERVICE_TYPE_NAME");
  IElementType SHIFT_EXPRESSION = new BallerinaCompositeElementType("SHIFT_EXPRESSION");
  IElementType SIMPLE_LITERAL = new BallerinaCompositeElementType("SIMPLE_LITERAL");
  IElementType SIMPLE_LITERAL_EXPRESSION = new BallerinaCompositeElementType("SIMPLE_LITERAL_EXPRESSION");
  IElementType SIMPLE_TYPE_NAME = new BallerinaCompositeElementType("SIMPLE_TYPE_NAME");
  IElementType SIMPLE_VARIABLE_REFERENCE = new BallerinaCompositeElementType("SIMPLE_VARIABLE_REFERENCE");
  IElementType SINGLE_BACKTICKED_BLOCK = new BallerinaCompositeElementType("SINGLE_BACKTICKED_BLOCK");
  IElementType SINGLE_BACK_TICK_DEPRECATED_INLINE_CODE = new BallerinaCompositeElementType("SINGLE_BACK_TICK_DEPRECATED_INLINE_CODE");
  IElementType START_TAG = new BallerinaCompositeElementType("START_TAG");
  IElementType STATEMENT = new BallerinaCompositeElementType("STATEMENT");
  IElementType STATIC_MATCH_IDENTIFIER_LITERAL = new BallerinaCompositeElementType("STATIC_MATCH_IDENTIFIER_LITERAL");
  IElementType STATIC_MATCH_LITERALS = new BallerinaCompositeElementType("STATIC_MATCH_LITERALS");
  IElementType STATIC_MATCH_OR_EXPRESSION = new BallerinaCompositeElementType("STATIC_MATCH_OR_EXPRESSION");
  IElementType STATIC_MATCH_PATTERN = new BallerinaCompositeElementType("STATIC_MATCH_PATTERN");
  IElementType STATIC_MATCH_RECORD_LITERAL = new BallerinaCompositeElementType("STATIC_MATCH_RECORD_LITERAL");
  IElementType STATIC_MATCH_SIMPLE_LITERAL = new BallerinaCompositeElementType("STATIC_MATCH_SIMPLE_LITERAL");
  IElementType STATIC_MATCH_TUPLE_LITERAL = new BallerinaCompositeElementType("STATIC_MATCH_TUPLE_LITERAL");
  IElementType STREAMING_ACTION = new BallerinaCompositeElementType("STREAMING_ACTION");
  IElementType STREAMING_INPUT = new BallerinaCompositeElementType("STREAMING_INPUT");
  IElementType STREAMING_QUERY_STATEMENT = new BallerinaCompositeElementType("STREAMING_QUERY_STATEMENT");
  IElementType STREAM_TYPE_NAME = new BallerinaCompositeElementType("STREAM_TYPE_NAME");
  IElementType STRING_FUNCTION_INVOCATION_REFERENCE = new BallerinaCompositeElementType("STRING_FUNCTION_INVOCATION_REFERENCE");
  IElementType STRING_TEMPLATE_CONTENT = new BallerinaCompositeElementType("STRING_TEMPLATE_CONTENT");
  IElementType STRING_TEMPLATE_LITERAL = new BallerinaCompositeElementType("STRING_TEMPLATE_LITERAL");
  IElementType STRING_TEMPLATE_LITERAL_EXPRESSION = new BallerinaCompositeElementType("STRING_TEMPLATE_LITERAL_EXPRESSION");
  IElementType STRUCTURED_BINDING_PATTERN = new BallerinaCompositeElementType("STRUCTURED_BINDING_PATTERN");
  IElementType STRUCTURED_REF_BINDING_PATTERN = new BallerinaCompositeElementType("STRUCTURED_REF_BINDING_PATTERN");
  IElementType TABLE_COLUMN = new BallerinaCompositeElementType("TABLE_COLUMN");
  IElementType TABLE_COLUMN_DEFINITION = new BallerinaCompositeElementType("TABLE_COLUMN_DEFINITION");
  IElementType TABLE_DATA = new BallerinaCompositeElementType("TABLE_DATA");
  IElementType TABLE_DATA_ARRAY = new BallerinaCompositeElementType("TABLE_DATA_ARRAY");
  IElementType TABLE_DATA_LIST = new BallerinaCompositeElementType("TABLE_DATA_LIST");
  IElementType TABLE_LITERAL = new BallerinaCompositeElementType("TABLE_LITERAL");
  IElementType TABLE_LITERAL_EXPRESSION = new BallerinaCompositeElementType("TABLE_LITERAL_EXPRESSION");
  IElementType TABLE_QUERY = new BallerinaCompositeElementType("TABLE_QUERY");
  IElementType TABLE_QUERY_EXPRESSION = new BallerinaCompositeElementType("TABLE_QUERY_EXPRESSION");
  IElementType TABLE_TYPE_NAME = new BallerinaCompositeElementType("TABLE_TYPE_NAME");
  IElementType TERNARY_EXPRESSION = new BallerinaCompositeElementType("TERNARY_EXPRESSION");
  IElementType THROW_STATEMENT = new BallerinaCompositeElementType("THROW_STATEMENT");
  IElementType TIME_SCALE = new BallerinaCompositeElementType("TIME_SCALE");
  IElementType TRANSACTION_CLAUSE = new BallerinaCompositeElementType("TRANSACTION_CLAUSE");
  IElementType TRANSACTION_PROPERTY_INIT_STATEMENT = new BallerinaCompositeElementType("TRANSACTION_PROPERTY_INIT_STATEMENT");
  IElementType TRANSACTION_PROPERTY_INIT_STATEMENT_LIST = new BallerinaCompositeElementType("TRANSACTION_PROPERTY_INIT_STATEMENT_LIST");
  IElementType TRANSACTION_STATEMENT = new BallerinaCompositeElementType("TRANSACTION_STATEMENT");
  IElementType TRAP_EXPRESSION = new BallerinaCompositeElementType("TRAP_EXPRESSION");
  IElementType TRIPLE_BACKTICKED_BLOCK = new BallerinaCompositeElementType("TRIPLE_BACKTICKED_BLOCK");
  IElementType TRIPLE_BACK_TICK_DEPRECATED_INLINE_CODE = new BallerinaCompositeElementType("TRIPLE_BACK_TICK_DEPRECATED_INLINE_CODE");
  IElementType TRY_CATCH_STATEMENT = new BallerinaCompositeElementType("TRY_CATCH_STATEMENT");
  IElementType TUPLE_BINDING_PATTERN = new BallerinaCompositeElementType("TUPLE_BINDING_PATTERN");
  IElementType TUPLE_DESTRUCTURING_STATEMENT = new BallerinaCompositeElementType("TUPLE_DESTRUCTURING_STATEMENT");
  IElementType TUPLE_LITERAL = new BallerinaCompositeElementType("TUPLE_LITERAL");
  IElementType TUPLE_REF_BINDING_PATTERN = new BallerinaCompositeElementType("TUPLE_REF_BINDING_PATTERN");
  IElementType TUPLE_TYPE_NAME = new BallerinaCompositeElementType("TUPLE_TYPE_NAME");
  IElementType TYPE_ACCESS_EXPRESSION = new BallerinaCompositeElementType("TYPE_ACCESS_EXPRESSION");
  IElementType TYPE_ACCESS_EXPR_INVOCATION_REFERENCE = new BallerinaCompositeElementType("TYPE_ACCESS_EXPR_INVOCATION_REFERENCE");
  IElementType TYPE_CONVERSION_EXPRESSION = new BallerinaCompositeElementType("TYPE_CONVERSION_EXPRESSION");
  IElementType TYPE_DEFINITION = new BallerinaCompositeElementType("TYPE_DEFINITION");
  IElementType TYPE_DESC_TYPE_NAME = new BallerinaCompositeElementType("TYPE_DESC_TYPE_NAME");
  IElementType TYPE_INIT_EXPRESSION = new BallerinaCompositeElementType("TYPE_INIT_EXPRESSION");
  IElementType TYPE_NAME = new BallerinaCompositeElementType("TYPE_NAME");
  IElementType TYPE_REFERENCE = new BallerinaCompositeElementType("TYPE_REFERENCE");
  IElementType TYPE_TEST_EXPRESSION = new BallerinaCompositeElementType("TYPE_TEST_EXPRESSION");
  IElementType UNARY_EXPRESSION = new BallerinaCompositeElementType("UNARY_EXPRESSION");
  IElementType UNION_TYPE_NAME = new BallerinaCompositeElementType("UNION_TYPE_NAME");
  IElementType USER_DEFINE_TYPE_NAME = new BallerinaCompositeElementType("USER_DEFINE_TYPE_NAME");
  IElementType VALUE_TYPE_NAME = new BallerinaCompositeElementType("VALUE_TYPE_NAME");
  IElementType VARIABLE_DEFINITION_STATEMENT = new BallerinaCompositeElementType("VARIABLE_DEFINITION_STATEMENT");
  IElementType VARIABLE_DEFINITION_STATEMENT_WITHOUT_ASSIGNMENT = new BallerinaCompositeElementType("VARIABLE_DEFINITION_STATEMENT_WITHOUT_ASSIGNMENT");
  IElementType VARIABLE_DEFINITION_STATEMENT_WITH_ASSIGNMENT = new BallerinaCompositeElementType("VARIABLE_DEFINITION_STATEMENT_WITH_ASSIGNMENT");
  IElementType VARIABLE_REFERENCE = new BallerinaCompositeElementType("VARIABLE_REFERENCE");
  IElementType VARIABLE_REFERENCE_EXPRESSION = new BallerinaCompositeElementType("VARIABLE_REFERENCE_EXPRESSION");
  IElementType VARIABLE_REFERENCE_LIST = new BallerinaCompositeElementType("VARIABLE_REFERENCE_LIST");
  IElementType VAR_MATCH_PATTERN = new BallerinaCompositeElementType("VAR_MATCH_PATTERN");
  IElementType WAIT_EXPRESSION = new BallerinaCompositeElementType("WAIT_EXPRESSION");
  IElementType WAIT_FOR_COLLECTION = new BallerinaCompositeElementType("WAIT_FOR_COLLECTION");
  IElementType WAIT_KEY_VALUE = new BallerinaCompositeElementType("WAIT_KEY_VALUE");
  IElementType WHERE_CLAUSE = new BallerinaCompositeElementType("WHERE_CLAUSE");
  IElementType WHILE_STATEMENT = new BallerinaCompositeElementType("WHILE_STATEMENT");
  IElementType WHILE_STATEMENT_BODY = new BallerinaCompositeElementType("WHILE_STATEMENT_BODY");
  IElementType WINDOW_CLAUSE = new BallerinaCompositeElementType("WINDOW_CLAUSE");
  IElementType WITHIN_CLAUSE = new BallerinaCompositeElementType("WITHIN_CLAUSE");
  IElementType WORKER_BODY = new BallerinaCompositeElementType("WORKER_BODY");
  IElementType WORKER_DEFINITION = new BallerinaCompositeElementType("WORKER_DEFINITION");
  IElementType WORKER_RECEIVE_EXPRESSION = new BallerinaCompositeElementType("WORKER_RECEIVE_EXPRESSION");
  IElementType WORKER_SEND_ASYNC_EXPRESSION = new BallerinaCompositeElementType("WORKER_SEND_ASYNC_EXPRESSION");
  IElementType WORKER_SEND_ASYNC_STATEMENT = new BallerinaCompositeElementType("WORKER_SEND_ASYNC_STATEMENT");
  IElementType WORKER_WITH_STATEMENTS_BLOCK = new BallerinaCompositeElementType("WORKER_WITH_STATEMENTS_BLOCK");
  IElementType XML_ATTRIB = new BallerinaCompositeElementType("XML_ATTRIB");
  IElementType XML_ATTRIB_VARIABLE_REFERENCE = new BallerinaCompositeElementType("XML_ATTRIB_VARIABLE_REFERENCE");
  IElementType XML_DOUBLE_QUOTED_STRING = new BallerinaCompositeElementType("XML_DOUBLE_QUOTED_STRING");
  IElementType XML_ITEM = new BallerinaCompositeElementType("XML_ITEM");
  IElementType XML_LITERAL = new BallerinaCompositeElementType("XML_LITERAL");
  IElementType XML_LITERAL_EXPRESSION = new BallerinaCompositeElementType("XML_LITERAL_EXPRESSION");
  IElementType XML_LOCAL_NAME = new BallerinaCompositeElementType("XML_LOCAL_NAME");
  IElementType XML_NAMESPACE_NAME = new BallerinaCompositeElementType("XML_NAMESPACE_NAME");
  IElementType XML_QUALIFIED_NAME = new BallerinaCompositeElementType("XML_QUALIFIED_NAME");
  IElementType XML_QUOTED_STRING = new BallerinaCompositeElementType("XML_QUOTED_STRING");
  IElementType XML_SINGLE_QUOTED_STRING = new BallerinaCompositeElementType("XML_SINGLE_QUOTED_STRING");
  IElementType XML_TEXT = new BallerinaCompositeElementType("XML_TEXT");
  IElementType XML_TYPE_NAME = new BallerinaCompositeElementType("XML_TYPE_NAME");

  IElementType ABORT = new BallerinaTokenType("abort");
  IElementType ABORTED = new BallerinaTokenType("aborted");
  IElementType ABSTRACT = new BallerinaTokenType("abstract");
  IElementType ADD = new BallerinaTokenType("+");
  IElementType ALL = new BallerinaTokenType("all");
  IElementType AND = new BallerinaTokenType("&&");
  IElementType ANNOTATION = new BallerinaTokenType("annotation");
  IElementType ANY = new BallerinaTokenType("any");
  IElementType ANYDATA = new BallerinaTokenType("anydata");
  IElementType AS = new BallerinaTokenType("as");
  IElementType ASCENDING = new BallerinaTokenType("ascending");
  IElementType ASSIGN = new BallerinaTokenType("=");
  IElementType AT = new BallerinaTokenType("@");
  IElementType BACKTICK = new BallerinaTokenType("`");
  IElementType BASE_16_BLOB_LITERAL = new BallerinaTokenType("BASE_16_BLOB_LITERAL");
  IElementType BASE_64_BLOB_LITERAL = new BallerinaTokenType("BASE_64_BLOB_LITERAL");
  IElementType BINARY_INTEGER_LITERAL = new BallerinaTokenType("BINARY_INTEGER_LITERAL");
  IElementType BITAND = new BallerinaTokenType("BITAND");
  IElementType BITXOR = new BallerinaTokenType("BITXOR");
  IElementType BIT_COMPLEMENT = new BallerinaTokenType("BIT_COMPLEMENT");
  IElementType BOOLEAN = new BallerinaTokenType("boolean");
  IElementType BOOLEAN_LITERAL = new BallerinaTokenType("BOOLEAN_LITERAL");
  IElementType BREAK = new BallerinaTokenType("break");
  IElementType BY = new BallerinaTokenType("by");
  IElementType BYTE = new BallerinaTokenType("byte");
  IElementType CATCH = new BallerinaTokenType("catch");
  IElementType CDATA = new BallerinaTokenType("cdata");
  IElementType CHANNEL = new BallerinaTokenType("channel");
  IElementType CHECK = new BallerinaTokenType("check");
  IElementType CLIENT = new BallerinaTokenType("client");
  IElementType COLON = new BallerinaTokenType(":");
  IElementType COMMA = new BallerinaTokenType(",");
  IElementType COMMITTED = new BallerinaTokenType("committed");
  IElementType COMPOUND_ADD = new BallerinaTokenType("+=");
  IElementType COMPOUND_BIT_AND = new BallerinaTokenType("COMPOUND_BIT_AND");
  IElementType COMPOUND_BIT_OR = new BallerinaTokenType("COMPOUND_BIT_OR");
  IElementType COMPOUND_BIT_XOR = new BallerinaTokenType("COMPOUND_BIT_XOR");
  IElementType COMPOUND_DIV = new BallerinaTokenType("/=");
  IElementType COMPOUND_LEFT_SHIFT = new BallerinaTokenType("COMPOUND_LEFT_SHIFT");
  IElementType COMPOUND_LOGICAL_SHIFT = new BallerinaTokenType("COMPOUND_LOGICAL_SHIFT");
  IElementType COMPOUND_MUL = new BallerinaTokenType("*=");
  IElementType COMPOUND_RIGHT_SHIFT = new BallerinaTokenType("COMPOUND_RIGHT_SHIFT");
  IElementType COMPOUND_SUB = new BallerinaTokenType("-=");
  IElementType CONST = new BallerinaTokenType("CONST");
  IElementType CONTINUE = new BallerinaTokenType("continue");
  IElementType DAY = new BallerinaTokenType("day");
  IElementType DAYS = new BallerinaTokenType("days");
  IElementType DB_DEPRECATED_INLINE_CODE_START = new BallerinaTokenType("DB_DEPRECATED_INLINE_CODE_START");
  IElementType DECIMAL = new BallerinaTokenType("decimal");
  IElementType DECIMAL_FLOATING_POINT_NUMBER = new BallerinaTokenType("DECIMAL_FLOATING_POINT_NUMBER");
  IElementType DECIMAL_INTEGER_LITERAL = new BallerinaTokenType("DECIMAL_INTEGER_LITERAL");
  IElementType DECREMENT = new BallerinaTokenType("--");
  IElementType DEFINITION_REFERENCE = new BallerinaTokenType("DEFINITION_REFERENCE");
  IElementType DEPRECATED = new BallerinaTokenType("deprecated");
  IElementType DEPRECATED_TEMPLATE_END = new BallerinaTokenType("DEPRECATED_TEMPLATE_END");
  IElementType DEPRECATED_TEMPLATE_START = new BallerinaTokenType("DEPRECATED_TEMPLATE_START");
  IElementType DEPRECATED_TEMPLATE_TEXT = new BallerinaTokenType("DEPRECATED_TEMPLATE_TEXT");
  IElementType DESCENDING = new BallerinaTokenType("descending");
  IElementType DESCRIPTION_SEPARATOR = new BallerinaTokenType("DESCRIPTION_SEPARATOR");
  IElementType DIV = new BallerinaTokenType("/");
  IElementType DOCUMENTATION_ESCAPED_CHARACTERS = new BallerinaTokenType("DOCUMENTATION_ESCAPED_CHARACTERS");
  IElementType DOT = new BallerinaTokenType(".");
  IElementType DOUBLE_BACKTICK_CONTENT = new BallerinaTokenType("DOUBLE_BACKTICK_CONTENT");
  IElementType DOUBLE_BACKTICK_MARKDOWN_END = new BallerinaTokenType("DOUBLE_BACKTICK_MARKDOWN_END");
  IElementType DOUBLE_BACKTICK_MARKDOWN_START = new BallerinaTokenType("DOUBLE_BACKTICK_MARKDOWN_START");
  IElementType DOUBLE_BACK_TICK_INLINE_CODE = new BallerinaTokenType("DOUBLE_BACK_TICK_INLINE_CODE");
  IElementType DOUBLE_BACK_TICK_INLINE_CODE_END = new BallerinaTokenType("DOUBLE_BACK_TICK_INLINE_CODE_END");
  IElementType DOUBLE_COLON = new BallerinaTokenType("::");
  IElementType DOUBLE_QUOTE = new BallerinaTokenType("DOUBLE_QUOTE");
  IElementType DOUBLE_QUOTE_END = new BallerinaTokenType("DOUBLE_QUOTE_END");
  IElementType ELLIPSIS = new BallerinaTokenType("...");
  IElementType ELSE = new BallerinaTokenType("else");
  IElementType ELVIS = new BallerinaTokenType("ELVIS");
  IElementType ENUM = new BallerinaTokenType("enum");
  IElementType EQUAL = new BallerinaTokenType("==");
  IElementType EQUALS = new BallerinaTokenType("EQUALS");
  IElementType EQUAL_GT = new BallerinaTokenType("=>");
  IElementType ERROR = new BallerinaTokenType("error");
  IElementType EVENTS = new BallerinaTokenType("events");
  IElementType EVERY = new BallerinaTokenType("every");
  IElementType EXPRESSION_END = new BallerinaTokenType("EXPRESSION_END");
  IElementType EXTERN = new BallerinaTokenType("extern");
  IElementType FAIL = new BallerinaTokenType("fail");
  IElementType FINAL = new BallerinaTokenType("final");
  IElementType FINALLY = new BallerinaTokenType("finally");
  IElementType FIRST = new BallerinaTokenType("first");
  IElementType FLOAT = new BallerinaTokenType("float");
  IElementType FLUSH = new BallerinaTokenType("flush");
  IElementType FOLLOWED = new BallerinaTokenType("followed");
  IElementType FOR = new BallerinaTokenType("for");
  IElementType FOREACH = new BallerinaTokenType("foreach");
  IElementType FOREVER = new BallerinaTokenType("forever");
  IElementType FORK = new BallerinaTokenType("fork");
  IElementType FROM = new BallerinaTokenType("from");
  IElementType FULL = new BallerinaTokenType("full");
  IElementType FUNCTION = new BallerinaTokenType("function");
  IElementType FUTURE = new BallerinaTokenType("future");
  IElementType GROUP = new BallerinaTokenType("group");
  IElementType GT = new BallerinaTokenType(">");
  IElementType GT_EQUAL = new BallerinaTokenType(">=");
  IElementType HALF_OPEN_RANGE = new BallerinaTokenType("HALF_OPEN_RANGE");
  IElementType HAVING = new BallerinaTokenType("having");
  IElementType HEXADECIMAL_FLOATING_POINT_LITERAL = new BallerinaTokenType("HEXADECIMAL_FLOATING_POINT_LITERAL");
  IElementType HEX_INTEGER_LITERAL = new BallerinaTokenType("HEX_INTEGER_LITERAL");
  IElementType HOUR = new BallerinaTokenType("hour");
  IElementType HOURS = new BallerinaTokenType("hours");
  IElementType IDENTIFIER = new BallerinaTokenType("identifier");
  IElementType IF = new BallerinaTokenType("if");
  IElementType IMPORT = new BallerinaTokenType("import");
  IElementType IN = new BallerinaTokenType("in");
  IElementType INCREMENT = new BallerinaTokenType("++");
  IElementType INNER = new BallerinaTokenType("inner");
  IElementType INT = new BallerinaTokenType("int");
  IElementType IS = new BallerinaTokenType("is");
  IElementType JOIN = new BallerinaTokenType("join");
  IElementType JSON = new BallerinaTokenType("json");
  IElementType LARROW = new BallerinaTokenType("<-");
  IElementType LAST = new BallerinaTokenType("last");
  IElementType LEFT = new BallerinaTokenType("left");
  IElementType LEFT_BRACE = new BallerinaTokenType("{");
  IElementType LEFT_BRACKET = new BallerinaTokenType("[");
  IElementType LEFT_PARENTHESIS = new BallerinaTokenType("(");
  IElementType LIMIT = new BallerinaTokenType("limit");
  IElementType LINE_COMMENT = new BallerinaTokenType("LINE_COMMENT");
  IElementType LISTENER = new BallerinaTokenType("listener");
  IElementType LOCK = new BallerinaTokenType("lock");
  IElementType LT = new BallerinaTokenType("<");
  IElementType LT_EQUAL = new BallerinaTokenType("<=");
  IElementType MAP = new BallerinaTokenType("map");
  IElementType MARKDOWN_DOCUMENTATION_LINE_START = new BallerinaTokenType("MARKDOWN_DOCUMENTATION_LINE_START");
  IElementType MARKDOWN_DOCUMENTATION_TEXT = new BallerinaTokenType("MARKDOWN_DOCUMENTATION_TEXT");
  IElementType MATCH = new BallerinaTokenType("match");
  IElementType MINUTE = new BallerinaTokenType("minute");
  IElementType MINUTES = new BallerinaTokenType("minutes");
  IElementType MOD = new BallerinaTokenType("%");
  IElementType MONTH = new BallerinaTokenType("month");
  IElementType MONTHS = new BallerinaTokenType("months");
  IElementType MUL = new BallerinaTokenType("*");
  IElementType NEW = new BallerinaTokenType("new");
  IElementType NOT = new BallerinaTokenType("!");
  IElementType NOT_EQUAL = new BallerinaTokenType("!=");
  IElementType NULL_LITERAL = new BallerinaTokenType("NULL_LITERAL");
  IElementType OBJECT = new BallerinaTokenType("object");
  IElementType OBJECT_INIT = new BallerinaTokenType("OBJECT_INIT");
  IElementType OCTAL_INTEGER_LITERAL = new BallerinaTokenType("OCTAL_INTEGER_LITERAL");
  IElementType ON = new BallerinaTokenType("on");
  IElementType ONRETRY = new BallerinaTokenType("onretry");
  IElementType OR = new BallerinaTokenType("||");
  IElementType ORDER = new BallerinaTokenType("order");
  IElementType OUTER = new BallerinaTokenType("outer");
  IElementType OUTPUT = new BallerinaTokenType("output");
  IElementType PANIC = new BallerinaTokenType("panic");
  IElementType PARAMETER_DOCUMENTATION_START = new BallerinaTokenType("PARAMETER_DOCUMENTATION_START");
  IElementType PARAMETER_NAME = new BallerinaTokenType("PARAMETER_NAME");
  IElementType PIPE = new BallerinaTokenType("|");
  IElementType POW = new BallerinaTokenType("^");
  IElementType PRIVATE = new BallerinaTokenType("private");
  IElementType PUBLIC = new BallerinaTokenType("PUBLIC");
  IElementType QNAME_SEPARATOR = new BallerinaTokenType("QNAME_SEPARATOR");
  IElementType QUESTION_MARK = new BallerinaTokenType("?");
  IElementType QUOTED_STRING_LITERAL = new BallerinaTokenType("QUOTED_STRING_LITERAL");
  IElementType RANGE = new BallerinaTokenType("..");
  IElementType RARROW = new BallerinaTokenType("->");
  IElementType RECORD = new BallerinaTokenType("record");
  IElementType REFERENCE_TYPE = new BallerinaTokenType("REFERENCE_TYPE");
  IElementType REF_EQUAL = new BallerinaTokenType("===");
  IElementType REF_NOT_EQUAL = new BallerinaTokenType("!==");
  IElementType REMOTE = new BallerinaTokenType("remote");
  IElementType RESOURCE = new BallerinaTokenType("resource");
  IElementType RETRIES = new BallerinaTokenType("retries");
  IElementType RETRY = new BallerinaTokenType("retry");
  IElementType RETURN = new BallerinaTokenType("return");
  IElementType RETURNS = new BallerinaTokenType("returns");
  IElementType RETURN_PARAMETER_DOCUMENTATION_START = new BallerinaTokenType("RETURN_PARAMETER_DOCUMENTATION_START");
  IElementType RIGHT = new BallerinaTokenType("right");
  IElementType RIGHT_BRACE = new BallerinaTokenType("}");
  IElementType RIGHT_BRACKET = new BallerinaTokenType("]");
  IElementType RIGHT_PARENTHESIS = new BallerinaTokenType(")");
  IElementType SAFE_ASSIGNMENT = new BallerinaTokenType("=?");
  IElementType SB_DEPRECATED_INLINE_CODE_START = new BallerinaTokenType("SB_DEPRECATED_INLINE_CODE_START");
  IElementType SECOND = new BallerinaTokenType("second");
  IElementType SECONDS = new BallerinaTokenType("seconds");
  IElementType SELECT = new BallerinaTokenType("select");
  IElementType SEMICOLON = new BallerinaTokenType(";");
  IElementType SERVICE = new BallerinaTokenType("service");
  IElementType SINGLE_BACKTICK_CONTENT = new BallerinaTokenType("SINGLE_BACKTICK_CONTENT");
  IElementType SINGLE_BACKTICK_MARKDOWN_END = new BallerinaTokenType("SINGLE_BACKTICK_MARKDOWN_END");
  IElementType SINGLE_BACKTICK_MARKDOWN_START = new BallerinaTokenType("SINGLE_BACKTICK_MARKDOWN_START");
  IElementType SINGLE_BACK_TICK_INLINE_CODE = new BallerinaTokenType("SINGLE_BACK_TICK_INLINE_CODE");
  IElementType SINGLE_BACK_TICK_INLINE_CODE_END = new BallerinaTokenType("SINGLE_BACK_TICK_INLINE_CODE_END");
  IElementType SINGLE_QUOTE = new BallerinaTokenType("SINGLE_QUOTE");
  IElementType SINGLE_QUOTE_END = new BallerinaTokenType("SINGLE_QUOTE_END");
  IElementType SNAPSHOT = new BallerinaTokenType("snapshot");
  IElementType START = new BallerinaTokenType("start");
  IElementType STREAM = new BallerinaTokenType("stream");
  IElementType STRING = new BallerinaTokenType("string");
  IElementType STRING_TEMPLATE_EXPRESSION_START = new BallerinaTokenType("STRING_TEMPLATE_EXPRESSION_START");
  IElementType STRING_TEMPLATE_LITERAL_END = new BallerinaTokenType("STRING_TEMPLATE_LITERAL_END");
  IElementType STRING_TEMPLATE_LITERAL_START = new BallerinaTokenType("STRING_TEMPLATE_LITERAL_START");
  IElementType STRING_TEMPLATE_TEXT = new BallerinaTokenType("STRING_TEMPLATE_TEXT");
  IElementType SUB = new BallerinaTokenType("-");
  IElementType SYMBOLIC_STRING_LITERAL = new BallerinaTokenType("SYMBOLIC_STRING_LITERAL");
  IElementType SYNCRARROW = new BallerinaTokenType("SYNCRARROW");
  IElementType TABLE = new BallerinaTokenType("table");
  IElementType TB_DEPRECATED_INLINE_CODE_START = new BallerinaTokenType("TB_DEPRECATED_INLINE_CODE_START");
  IElementType THROW = new BallerinaTokenType("throw");
  IElementType TRANSACTION = new BallerinaTokenType("transaction");
  IElementType TRAP = new BallerinaTokenType("trap");
  IElementType TRIPLE_BACKTICK_CONTENT = new BallerinaTokenType("TRIPLE_BACKTICK_CONTENT");
  IElementType TRIPLE_BACKTICK_MARKDOWN_END = new BallerinaTokenType("TRIPLE_BACKTICK_MARKDOWN_END");
  IElementType TRIPLE_BACKTICK_MARKDOWN_START = new BallerinaTokenType("TRIPLE_BACKTICK_MARKDOWN_START");
  IElementType TRIPLE_BACK_TICK_INLINE_CODE = new BallerinaTokenType("TRIPLE_BACK_TICK_INLINE_CODE");
  IElementType TRIPLE_BACK_TICK_INLINE_CODE_END = new BallerinaTokenType("TRIPLE_BACK_TICK_INLINE_CODE_END");
  IElementType TRY = new BallerinaTokenType("try");
  IElementType TYPE = new BallerinaTokenType("type");
  IElementType TYPEDESC = new BallerinaTokenType("typedesc");
  IElementType TYPE_PARAMETER = new BallerinaTokenType("TYPE_PARAMETER");
  IElementType UNIDIRECTIONAL = new BallerinaTokenType("unidirectional");
  IElementType UNTAINT = new BallerinaTokenType("untaint");
  IElementType VAR = new BallerinaTokenType("var");
  IElementType VERSION = new BallerinaTokenType("version");
  IElementType WAIT = new BallerinaTokenType("wait");
  IElementType WHERE = new BallerinaTokenType("where");
  IElementType WHILE = new BallerinaTokenType("while");
  IElementType WINDOW = new BallerinaTokenType("window");
  IElementType WITH = new BallerinaTokenType("with");
  IElementType WITHIN = new BallerinaTokenType("within");
  IElementType WORKER = new BallerinaTokenType("worker");
  IElementType XML = new BallerinaTokenType("xml");
  IElementType XMLNS = new BallerinaTokenType("xmlns");
  IElementType XML_COMMENT_START = new BallerinaTokenType("XML_COMMENT_START");
  IElementType XML_COMMENT_TEMPLATE_TEXT = new BallerinaTokenType("XML_COMMENT_TEMPLATE_TEXT");
  IElementType XML_COMMENT_TEXT = new BallerinaTokenType("XML_COMMENT_TEXT");
  IElementType XML_DOUBLE_QUOTED_STRING_SEQUENCE = new BallerinaTokenType("XML_DOUBLE_QUOTED_STRING_SEQUENCE");
  IElementType XML_DOUBLE_QUOTED_TEMPLATE_STRING = new BallerinaTokenType("XML_DOUBLE_QUOTED_TEMPLATE_STRING");
  IElementType XML_LITERAL_END = new BallerinaTokenType("XML_LITERAL_END");
  IElementType XML_LITERAL_START = new BallerinaTokenType("XML_LITERAL_START");
  IElementType XML_PI_TEMPLATE_TEXT = new BallerinaTokenType("XML_PI_TEMPLATE_TEXT");
  IElementType XML_PI_TEXT = new BallerinaTokenType("XML_PI_TEXT");
  IElementType XML_QNAME = new BallerinaTokenType("XML_QNAME");
  IElementType XML_SINGLE_QUOTED_STRING_SEQUENCE = new BallerinaTokenType("XML_SINGLE_QUOTED_STRING_SEQUENCE");
  IElementType XML_SINGLE_QUOTED_TEMPLATE_STRING = new BallerinaTokenType("XML_SINGLE_QUOTED_TEMPLATE_STRING");
  IElementType XML_TAG_CLOSE = new BallerinaTokenType("XML_TAG_CLOSE");
  IElementType XML_TAG_EXPRESSION_START = new BallerinaTokenType("XML_TAG_EXPRESSION_START");
  IElementType XML_TAG_OPEN = new BallerinaTokenType("XML_TAG_OPEN");
  IElementType XML_TAG_OPEN_SLASH = new BallerinaTokenType("XML_TAG_OPEN_SLASH");
  IElementType XML_TAG_SLASH_CLOSE = new BallerinaTokenType("XML_TAG_SLASH_CLOSE");
  IElementType XML_TAG_SPECIAL_OPEN = new BallerinaTokenType("XML_TAG_SPECIAL_OPEN");
  IElementType XML_TEMPLATE_TEXT = new BallerinaTokenType("XML_TEMPLATE_TEXT");
  IElementType XML_TEXT_SEQUENCE = new BallerinaTokenType("XML_TEXT_SEQUENCE");
  IElementType YEAR = new BallerinaTokenType("year");
  IElementType YEARS = new BallerinaTokenType("years");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ABORTED_CLAUSE) {
        return new BallerinaAbortedClauseImpl(node);
      }
      else if (type == ABORT_STATEMENT) {
        return new BallerinaAbortStatementImpl(node);
      }
      else if (type == ACTION_INVOCATION) {
        return new BallerinaActionInvocationImpl(node);
      }
      else if (type == ACTION_INVOCATION_EXPRESSION) {
        return new BallerinaActionInvocationExpressionImpl(node);
      }
      else if (type == ALIAS) {
        return new BallerinaAliasImpl(node);
      }
      else if (type == ANNOTATION_ATTACHMENT) {
        return new BallerinaAnnotationAttachmentImpl(node);
      }
      else if (type == ANNOTATION_DEFINITION) {
        return new BallerinaAnnotationDefinitionImpl(node);
      }
      else if (type == ANY_DATA_TYPE_NAME) {
        return new BallerinaAnyDataTypeNameImpl(node);
      }
      else if (type == ANY_IDENTIFIER_NAME) {
        return new BallerinaAnyIdentifierNameImpl(node);
      }
      else if (type == ANY_TYPE_NAME) {
        return new BallerinaAnyTypeNameImpl(node);
      }
      else if (type == ARRAY_LITERAL) {
        return new BallerinaArrayLiteralImpl(node);
      }
      else if (type == ARRAY_LITERAL_EXPRESSION) {
        return new BallerinaArrayLiteralExpressionImpl(node);
      }
      else if (type == ARRAY_TYPE_NAME) {
        return new BallerinaArrayTypeNameImpl(node);
      }
      else if (type == ARROW_FUNCTION) {
        return new BallerinaArrowFunctionImpl(node);
      }
      else if (type == ARROW_FUNCTION_EXPRESSION) {
        return new BallerinaArrowFunctionExpressionImpl(node);
      }
      else if (type == ARROW_PARAM) {
        return new BallerinaArrowParamImpl(node);
      }
      else if (type == ASSIGNMENT_STATEMENT) {
        return new BallerinaAssignmentStatementImpl(node);
      }
      else if (type == ATTACHED_OBJECT) {
        return new BallerinaAttachedObjectImpl(node);
      }
      else if (type == ATTACHMENT_POINT) {
        return new BallerinaAttachmentPointImpl(node);
      }
      else if (type == ATTRIBUTE) {
        return new BallerinaAttributeImpl(node);
      }
      else if (type == BACKTICKED_BLOCK) {
        return new BallerinaBacktickedBlockImpl(node);
      }
      else if (type == BINARY_ADD_SUB_EXPRESSION) {
        return new BallerinaBinaryAddSubExpressionImpl(node);
      }
      else if (type == BINARY_AND_EXPRESSION) {
        return new BallerinaBinaryAndExpressionImpl(node);
      }
      else if (type == BINARY_COMPARE_EXPRESSION) {
        return new BallerinaBinaryCompareExpressionImpl(node);
      }
      else if (type == BINARY_DIV_MUL_MOD_EXPRESSION) {
        return new BallerinaBinaryDivMulModExpressionImpl(node);
      }
      else if (type == BINARY_EQUAL_EXPRESSION) {
        return new BallerinaBinaryEqualExpressionImpl(node);
      }
      else if (type == BINARY_OR_EXPRESSION) {
        return new BallerinaBinaryOrExpressionImpl(node);
      }
      else if (type == BINARY_REF_EQUAL_EXPRESSION) {
        return new BallerinaBinaryRefEqualExpressionImpl(node);
      }
      else if (type == BINDING_PATTERN) {
        return new BallerinaBindingPatternImpl(node);
      }
      else if (type == BINDING_REF_PATTERN) {
        return new BallerinaBindingRefPatternImpl(node);
      }
      else if (type == BITWISE_EXPRESSION) {
        return new BallerinaBitwiseExpressionImpl(node);
      }
      else if (type == BITWISE_SHIFT_EXPRESSION) {
        return new BallerinaBitwiseShiftExpressionImpl(node);
      }
      else if (type == BLOB_LITERAL) {
        return new BallerinaBlobLiteralImpl(node);
      }
      else if (type == BLOCK) {
        return new BallerinaBlockImpl(node);
      }
      else if (type == BRACED_OR_TUPLE_EXPRESSION) {
        return new BallerinaBracedOrTupleExpressionImpl(node);
      }
      else if (type == BREAK_STATEMENT) {
        return new BallerinaBreakStatementImpl(node);
      }
      else if (type == BUILT_IN_REFERENCE_TYPE_NAME) {
        return new BallerinaBuiltInReferenceTypeNameImpl(node);
      }
      else if (type == CALLABLE_UNIT_BODY) {
        return new BallerinaCallableUnitBodyImpl(node);
      }
      else if (type == CALLABLE_UNIT_SIGNATURE) {
        return new BallerinaCallableUnitSignatureImpl(node);
      }
      else if (type == CATCH_CLAUSE) {
        return new BallerinaCatchClauseImpl(node);
      }
      else if (type == CATCH_CLAUSES) {
        return new BallerinaCatchClausesImpl(node);
      }
      else if (type == CHANNEL_TYPE) {
        return new BallerinaChannelTypeImpl(node);
      }
      else if (type == CHECKED_EXPRESSION) {
        return new BallerinaCheckedExpressionImpl(node);
      }
      else if (type == CLOSE_TAG) {
        return new BallerinaCloseTagImpl(node);
      }
      else if (type == COMMENT) {
        return new BallerinaCommentImpl(node);
      }
      else if (type == COMMITTED_ABORTED_CLAUSES) {
        return new BallerinaCommittedAbortedClausesImpl(node);
      }
      else if (type == COMMITTED_CLAUSE) {
        return new BallerinaCommittedClauseImpl(node);
      }
      else if (type == COMPLETE_PACKAGE_NAME) {
        return new BallerinaCompletePackageNameImpl(node);
      }
      else if (type == COMPOUND_ASSIGNMENT_STATEMENT) {
        return new BallerinaCompoundAssignmentStatementImpl(node);
      }
      else if (type == COMPOUND_OPERATOR) {
        return new BallerinaCompoundOperatorImpl(node);
      }
      else if (type == CONSTANT_DEFINITION) {
        return new BallerinaConstantDefinitionImpl(node);
      }
      else if (type == CONTENT) {
        return new BallerinaContentImpl(node);
      }
      else if (type == CONTINUE_STATEMENT) {
        return new BallerinaContinueStatementImpl(node);
      }
      else if (type == DEFAULTABLE_PARAMETER) {
        return new BallerinaDefaultableParameterImpl(node);
      }
      else if (type == DEFINITION) {
        return new BallerinaDefinitionImpl(node);
      }
      else if (type == DEFINITION_REFERENCE_TYPE) {
        return new BallerinaDefinitionReferenceTypeImpl(node);
      }
      else if (type == DEPRECATED_ATTACHMENT) {
        return new BallerinaDeprecatedAttachmentImpl(node);
      }
      else if (type == DEPRECATED_TEMPLATE_INLINE_CODE) {
        return new BallerinaDeprecatedTemplateInlineCodeImpl(node);
      }
      else if (type == DEPRECATED_TEXT) {
        return new BallerinaDeprecatedTextImpl(node);
      }
      else if (type == DOCUMENTATION_CONTENT) {
        return new BallerinaDocumentationContentImpl(node);
      }
      else if (type == DOCUMENTATION_DEFINITION_REFERENCE) {
        return new BallerinaDocumentationDefinitionReferenceImpl(node);
      }
      else if (type == DOCUMENTATION_LINE) {
        return new BallerinaDocumentationLineImpl(node);
      }
      else if (type == DOCUMENTATION_REFERENCE) {
        return new BallerinaDocumentationReferenceImpl(node);
      }
      else if (type == DOCUMENTATION_STRING) {
        return new BallerinaDocumentationStringImpl(node);
      }
      else if (type == DOCUMENTATION_TEXT) {
        return new BallerinaDocumentationTextImpl(node);
      }
      else if (type == DOC_PARAMETER_DESCRIPTION) {
        return new BallerinaDocParameterDescriptionImpl(node);
      }
      else if (type == DOUBLE_BACKTICKED_BLOCK) {
        return new BallerinaDoubleBacktickedBlockImpl(node);
      }
      else if (type == DOUBLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
        return new BallerinaDoubleBackTickDeprecatedInlineCodeImpl(node);
      }
      else if (type == ELEMENT) {
        return new BallerinaElementImpl(node);
      }
      else if (type == ELSE_CLAUSE) {
        return new BallerinaElseClauseImpl(node);
      }
      else if (type == ELSE_IF_CLAUSE) {
        return new BallerinaElseIfClauseImpl(node);
      }
      else if (type == ELVIS_EXPRESSION) {
        return new BallerinaElvisExpressionImpl(node);
      }
      else if (type == EMPTY_TAG) {
        return new BallerinaEmptyTagImpl(node);
      }
      else if (type == EMPTY_TUPLE_LITERAL) {
        return new BallerinaEmptyTupleLiteralImpl(node);
      }
      else if (type == ENTRY_BINDING_PATTERN) {
        return new BallerinaEntryBindingPatternImpl(node);
      }
      else if (type == ENTRY_REF_BINDING_PATTERN) {
        return new BallerinaEntryRefBindingPatternImpl(node);
      }
      else if (type == ERROR_BINDING_PATTERN) {
        return new BallerinaErrorBindingPatternImpl(node);
      }
      else if (type == ERROR_CONSTRUCTOR_EXPRESSION) {
        return new BallerinaErrorConstructorExpressionImpl(node);
      }
      else if (type == ERROR_DESTRUCTURING_STATEMENT) {
        return new BallerinaErrorDestructuringStatementImpl(node);
      }
      else if (type == ERROR_REF_BINDING_PATTERN) {
        return new BallerinaErrorRefBindingPatternImpl(node);
      }
      else if (type == ERROR_TYPE_NAME) {
        return new BallerinaErrorTypeNameImpl(node);
      }
      else if (type == EXPRESSION) {
        return new BallerinaExpressionImpl(node);
      }
      else if (type == EXPRESSION_LIST) {
        return new BallerinaExpressionListImpl(node);
      }
      else if (type == EXPRESSION_STMT) {
        return new BallerinaExpressionStmtImpl(node);
      }
      else if (type == FIELD) {
        return new BallerinaFieldImpl(node);
      }
      else if (type == FIELD_BINDING_PATTERN) {
        return new BallerinaFieldBindingPatternImpl(node);
      }
      else if (type == FIELD_DEFINITION) {
        return new BallerinaFieldDefinitionImpl(node);
      }
      else if (type == FIELD_REF_BINDING_PATTERN) {
        return new BallerinaFieldRefBindingPatternImpl(node);
      }
      else if (type == FIELD_VARIABLE_REFERENCE) {
        return new BallerinaFieldVariableReferenceImpl(node);
      }
      else if (type == FINALLY_CLAUSE) {
        return new BallerinaFinallyClauseImpl(node);
      }
      else if (type == FINITE_TYPE) {
        return new BallerinaFiniteTypeImpl(node);
      }
      else if (type == FINITE_TYPE_UNIT) {
        return new BallerinaFiniteTypeUnitImpl(node);
      }
      else if (type == FLOATING_POINT_LITERAL) {
        return new BallerinaFloatingPointLiteralImpl(node);
      }
      else if (type == FLUSH_WORKER) {
        return new BallerinaFlushWorkerImpl(node);
      }
      else if (type == FLUSH_WORKER_EXPRESSION) {
        return new BallerinaFlushWorkerExpressionImpl(node);
      }
      else if (type == FOREACH_STATEMENT) {
        return new BallerinaForeachStatementImpl(node);
      }
      else if (type == FOREVER_STATEMENT) {
        return new BallerinaForeverStatementImpl(node);
      }
      else if (type == FOREVER_STATEMENT_BODY) {
        return new BallerinaForeverStatementBodyImpl(node);
      }
      else if (type == FORK_JOIN_STATEMENT) {
        return new BallerinaForkJoinStatementImpl(node);
      }
      else if (type == FORMAL_PARAMETER_LIST) {
        return new BallerinaFormalParameterListImpl(node);
      }
      else if (type == FUNCTION_DEFINITION) {
        return new BallerinaFunctionDefinitionImpl(node);
      }
      else if (type == FUNCTION_INVOCATION) {
        return new BallerinaFunctionInvocationImpl(node);
      }
      else if (type == FUNCTION_INVOCATION_REFERENCE) {
        return new BallerinaFunctionInvocationReferenceImpl(node);
      }
      else if (type == FUNCTION_NAME_REFERENCE) {
        return new BallerinaFunctionNameReferenceImpl(node);
      }
      else if (type == FUNCTION_TYPE_NAME) {
        return new BallerinaFunctionTypeNameImpl(node);
      }
      else if (type == FUTURE_TYPE_NAME) {
        return new BallerinaFutureTypeNameImpl(node);
      }
      else if (type == GLOBAL_VARIABLE_DEFINITION) {
        return new BallerinaGlobalVariableDefinitionImpl(node);
      }
      else if (type == GROUP_BY_CLAUSE) {
        return new BallerinaGroupByClauseImpl(node);
      }
      else if (type == GROUP_TYPE_NAME) {
        return new BallerinaGroupTypeNameImpl(node);
      }
      else if (type == HAVING_CLAUSE) {
        return new BallerinaHavingClauseImpl(node);
      }
      else if (type == IF_CLAUSE) {
        return new BallerinaIfClauseImpl(node);
      }
      else if (type == IF_ELSE_STATEMENT) {
        return new BallerinaIfElseStatementImpl(node);
      }
      else if (type == IMPORT_DECLARATION) {
        return new BallerinaImportDeclarationImpl(node);
      }
      else if (type == INDEX) {
        return new BallerinaIndexImpl(node);
      }
      else if (type == INIT_WITHOUT_TYPE) {
        return new BallerinaInitWithoutTypeImpl(node);
      }
      else if (type == INIT_WITH_TYPE) {
        return new BallerinaInitWithTypeImpl(node);
      }
      else if (type == INTEGER_LITERAL) {
        return new BallerinaIntegerLiteralImpl(node);
      }
      else if (type == INTEGER_RANGE_EXPRESSION) {
        return new BallerinaIntegerRangeExpressionImpl(node);
      }
      else if (type == INT_RANGE_EXPRESSION) {
        return new BallerinaIntRangeExpressionImpl(node);
      }
      else if (type == INVOCATION) {
        return new BallerinaInvocationImpl(node);
      }
      else if (type == INVOCATION_ARG) {
        return new BallerinaInvocationArgImpl(node);
      }
      else if (type == INVOCATION_ARG_LIST) {
        return new BallerinaInvocationArgListImpl(node);
      }
      else if (type == INVOCATION_REFERENCE) {
        return new BallerinaInvocationReferenceImpl(node);
      }
      else if (type == JOIN_STREAMING_INPUT) {
        return new BallerinaJoinStreamingInputImpl(node);
      }
      else if (type == JOIN_TYPE) {
        return new BallerinaJoinTypeImpl(node);
      }
      else if (type == JSON_TYPE_NAME) {
        return new BallerinaJsonTypeNameImpl(node);
      }
      else if (type == LAMBDA_FUNCTION) {
        return new BallerinaLambdaFunctionImpl(node);
      }
      else if (type == LAMBDA_FUNCTION_EXPRESSION) {
        return new BallerinaLambdaFunctionExpressionImpl(node);
      }
      else if (type == LAMBDA_RETURN_PARAMETER) {
        return new BallerinaLambdaReturnParameterImpl(node);
      }
      else if (type == LIMIT_CLAUSE) {
        return new BallerinaLimitClauseImpl(node);
      }
      else if (type == LOCK_STATEMENT) {
        return new BallerinaLockStatementImpl(node);
      }
      else if (type == MAP_ARRAY_VARIABLE_REFERENCE) {
        return new BallerinaMapArrayVariableReferenceImpl(node);
      }
      else if (type == MAP_TYPE_NAME) {
        return new BallerinaMapTypeNameImpl(node);
      }
      else if (type == MATCH_PATTERN_CLAUSE) {
        return new BallerinaMatchPatternClauseImpl(node);
      }
      else if (type == MATCH_STATEMENT) {
        return new BallerinaMatchStatementImpl(node);
      }
      else if (type == MATCH_STATEMENT_BODY) {
        return new BallerinaMatchStatementBodyImpl(node);
      }
      else if (type == NAMED_ARGS) {
        return new BallerinaNamedArgsImpl(node);
      }
      else if (type == NAMESPACE_DECLARATION) {
        return new BallerinaNamespaceDeclarationImpl(node);
      }
      else if (type == NAMESPACE_DECLARATION_STATEMENT) {
        return new BallerinaNamespaceDeclarationStatementImpl(node);
      }
      else if (type == NAME_REFERENCE) {
        return new BallerinaNameReferenceImpl(node);
      }
      else if (type == NULLABLE_TYPE_NAME) {
        return new BallerinaNullableTypeNameImpl(node);
      }
      else if (type == OBJECT_BODY) {
        return new BallerinaObjectBodyImpl(node);
      }
      else if (type == OBJECT_FIELD_DEFINITION) {
        return new BallerinaObjectFieldDefinitionImpl(node);
      }
      else if (type == OBJECT_FUNCTION_DEFINITION) {
        return new BallerinaObjectFunctionDefinitionImpl(node);
      }
      else if (type == OBJECT_TYPE_NAME) {
        return new BallerinaObjectTypeNameImpl(node);
      }
      else if (type == ON_RETRY_CLAUSE) {
        return new BallerinaOnRetryClauseImpl(node);
      }
      else if (type == ORDER_BY_CLAUSE) {
        return new BallerinaOrderByClauseImpl(node);
      }
      else if (type == ORDER_BY_TYPE) {
        return new BallerinaOrderByTypeImpl(node);
      }
      else if (type == ORDER_BY_VARIABLE) {
        return new BallerinaOrderByVariableImpl(node);
      }
      else if (type == ORG_NAME) {
        return new BallerinaOrgNameImpl(node);
      }
      else if (type == OUTPUT_RATE_LIMIT) {
        return new BallerinaOutputRateLimitImpl(node);
      }
      else if (type == PACKAGE_NAME) {
        return new BallerinaPackageNameImpl(node);
      }
      else if (type == PACKAGE_REFERENCE) {
        return new BallerinaPackageReferenceImpl(node);
      }
      else if (type == PACKAGE_VERSION) {
        return new BallerinaPackageVersionImpl(node);
      }
      else if (type == PANIC_STATEMENT) {
        return new BallerinaPanicStatementImpl(node);
      }
      else if (type == PARAMETER) {
        return new BallerinaParameterImpl(node);
      }
      else if (type == PARAMETER_DESCRIPTION) {
        return new BallerinaParameterDescriptionImpl(node);
      }
      else if (type == PARAMETER_DOCUMENTATION) {
        return new BallerinaParameterDocumentationImpl(node);
      }
      else if (type == PARAMETER_DOCUMENTATION_LINE) {
        return new BallerinaParameterDocumentationLineImpl(node);
      }
      else if (type == PARAMETER_LIST) {
        return new BallerinaParameterListImpl(node);
      }
      else if (type == PARAMETER_TYPE_NAME) {
        return new BallerinaParameterTypeNameImpl(node);
      }
      else if (type == PARAMETER_TYPE_NAME_LIST) {
        return new BallerinaParameterTypeNameListImpl(node);
      }
      else if (type == PARAMETER_WITH_TYPE) {
        return new BallerinaParameterWithTypeImpl(node);
      }
      else if (type == PATTERN_CLAUSE) {
        return new BallerinaPatternClauseImpl(node);
      }
      else if (type == PATTERN_STREAMING_EDGE_INPUT) {
        return new BallerinaPatternStreamingEdgeInputImpl(node);
      }
      else if (type == PATTERN_STREAMING_INPUT) {
        return new BallerinaPatternStreamingInputImpl(node);
      }
      else if (type == PROC_INS) {
        return new BallerinaProcInsImpl(node);
      }
      else if (type == RECORD_BINDING_PATTERN) {
        return new BallerinaRecordBindingPatternImpl(node);
      }
      else if (type == RECORD_DESTRUCTURING_STATEMENT) {
        return new BallerinaRecordDestructuringStatementImpl(node);
      }
      else if (type == RECORD_FIELD_DEFINITION_LIST) {
        return new BallerinaRecordFieldDefinitionListImpl(node);
      }
      else if (type == RECORD_KEY) {
        return new BallerinaRecordKeyImpl(node);
      }
      else if (type == RECORD_KEY_VALUE) {
        return new BallerinaRecordKeyValueImpl(node);
      }
      else if (type == RECORD_LITERAL) {
        return new BallerinaRecordLiteralImpl(node);
      }
      else if (type == RECORD_LITERAL_BODY) {
        return new BallerinaRecordLiteralBodyImpl(node);
      }
      else if (type == RECORD_LITERAL_EXPRESSION) {
        return new BallerinaRecordLiteralExpressionImpl(node);
      }
      else if (type == RECORD_REF_BINDING_PATTERN) {
        return new BallerinaRecordRefBindingPatternImpl(node);
      }
      else if (type == RECORD_REST_FIELD_DEFINITION) {
        return new BallerinaRecordRestFieldDefinitionImpl(node);
      }
      else if (type == RECORD_TYPE_NAME) {
        return new BallerinaRecordTypeNameImpl(node);
      }
      else if (type == REFERENCE_TYPE_NAME) {
        return new BallerinaReferenceTypeNameImpl(node);
      }
      else if (type == RESERVED_WORD) {
        return new BallerinaReservedWordImpl(node);
      }
      else if (type == REST_ARGS) {
        return new BallerinaRestArgsImpl(node);
      }
      else if (type == REST_BINDING_PATTERN) {
        return new BallerinaRestBindingPatternImpl(node);
      }
      else if (type == REST_PARAMETER) {
        return new BallerinaRestParameterImpl(node);
      }
      else if (type == REST_REF_BINDING_PATTERN) {
        return new BallerinaRestRefBindingPatternImpl(node);
      }
      else if (type == RETRIES_STATEMENT) {
        return new BallerinaRetriesStatementImpl(node);
      }
      else if (type == RETRY_STATEMENT) {
        return new BallerinaRetryStatementImpl(node);
      }
      else if (type == RETURN_PARAMETER) {
        return new BallerinaReturnParameterImpl(node);
      }
      else if (type == RETURN_PARAMETER_DESCRIPTION) {
        return new BallerinaReturnParameterDescriptionImpl(node);
      }
      else if (type == RETURN_PARAMETER_DOCUMENTATION) {
        return new BallerinaReturnParameterDocumentationImpl(node);
      }
      else if (type == RETURN_PARAMETER_DOCUMENTATION_LINE) {
        return new BallerinaReturnParameterDocumentationLineImpl(node);
      }
      else if (type == RETURN_STATEMENT) {
        return new BallerinaReturnStatementImpl(node);
      }
      else if (type == RETURN_TYPE) {
        return new BallerinaReturnTypeImpl(node);
      }
      else if (type == SEALED_LITERAL) {
        return new BallerinaSealedLiteralImpl(node);
      }
      else if (type == SELECT_CLAUSE) {
        return new BallerinaSelectClauseImpl(node);
      }
      else if (type == SELECT_EXPRESSION) {
        return new BallerinaSelectExpressionImpl(node);
      }
      else if (type == SELECT_EXPRESSION_LIST) {
        return new BallerinaSelectExpressionListImpl(node);
      }
      else if (type == SERVICE_BODY) {
        return new BallerinaServiceBodyImpl(node);
      }
      else if (type == SERVICE_BODY_MEMBER) {
        return new BallerinaServiceBodyMemberImpl(node);
      }
      else if (type == SERVICE_CONSTRUCTOR_EXPRESSION) {
        return new BallerinaServiceConstructorExpressionImpl(node);
      }
      else if (type == SERVICE_DEFINITION) {
        return new BallerinaServiceDefinitionImpl(node);
      }
      else if (type == SERVICE_TYPE_NAME) {
        return new BallerinaServiceTypeNameImpl(node);
      }
      else if (type == SHIFT_EXPRESSION) {
        return new BallerinaShiftExpressionImpl(node);
      }
      else if (type == SIMPLE_LITERAL) {
        return new BallerinaSimpleLiteralImpl(node);
      }
      else if (type == SIMPLE_LITERAL_EXPRESSION) {
        return new BallerinaSimpleLiteralExpressionImpl(node);
      }
      else if (type == SIMPLE_TYPE_NAME) {
        return new BallerinaSimpleTypeNameImpl(node);
      }
      else if (type == SIMPLE_VARIABLE_REFERENCE) {
        return new BallerinaSimpleVariableReferenceImpl(node);
      }
      else if (type == SINGLE_BACKTICKED_BLOCK) {
        return new BallerinaSingleBacktickedBlockImpl(node);
      }
      else if (type == SINGLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
        return new BallerinaSingleBackTickDeprecatedInlineCodeImpl(node);
      }
      else if (type == START_TAG) {
        return new BallerinaStartTagImpl(node);
      }
      else if (type == STATEMENT) {
        return new BallerinaStatementImpl(node);
      }
      else if (type == STATIC_MATCH_IDENTIFIER_LITERAL) {
        return new BallerinaStaticMatchIdentifierLiteralImpl(node);
      }
      else if (type == STATIC_MATCH_OR_EXPRESSION) {
        return new BallerinaStaticMatchOrExpressionImpl(node);
      }
      else if (type == STATIC_MATCH_PATTERN) {
        return new BallerinaStaticMatchPatternImpl(node);
      }
      else if (type == STATIC_MATCH_RECORD_LITERAL) {
        return new BallerinaStaticMatchRecordLiteralImpl(node);
      }
      else if (type == STATIC_MATCH_SIMPLE_LITERAL) {
        return new BallerinaStaticMatchSimpleLiteralImpl(node);
      }
      else if (type == STATIC_MATCH_TUPLE_LITERAL) {
        return new BallerinaStaticMatchTupleLiteralImpl(node);
      }
      else if (type == STREAMING_ACTION) {
        return new BallerinaStreamingActionImpl(node);
      }
      else if (type == STREAMING_INPUT) {
        return new BallerinaStreamingInputImpl(node);
      }
      else if (type == STREAMING_QUERY_STATEMENT) {
        return new BallerinaStreamingQueryStatementImpl(node);
      }
      else if (type == STREAM_TYPE_NAME) {
        return new BallerinaStreamTypeNameImpl(node);
      }
      else if (type == STRING_FUNCTION_INVOCATION_REFERENCE) {
        return new BallerinaStringFunctionInvocationReferenceImpl(node);
      }
      else if (type == STRING_TEMPLATE_CONTENT) {
        return new BallerinaStringTemplateContentImpl(node);
      }
      else if (type == STRING_TEMPLATE_LITERAL) {
        return new BallerinaStringTemplateLiteralImpl(node);
      }
      else if (type == STRING_TEMPLATE_LITERAL_EXPRESSION) {
        return new BallerinaStringTemplateLiteralExpressionImpl(node);
      }
      else if (type == STRUCTURED_BINDING_PATTERN) {
        return new BallerinaStructuredBindingPatternImpl(node);
      }
      else if (type == STRUCTURED_REF_BINDING_PATTERN) {
        return new BallerinaStructuredRefBindingPatternImpl(node);
      }
      else if (type == TABLE_COLUMN) {
        return new BallerinaTableColumnImpl(node);
      }
      else if (type == TABLE_COLUMN_DEFINITION) {
        return new BallerinaTableColumnDefinitionImpl(node);
      }
      else if (type == TABLE_DATA) {
        return new BallerinaTableDataImpl(node);
      }
      else if (type == TABLE_DATA_ARRAY) {
        return new BallerinaTableDataArrayImpl(node);
      }
      else if (type == TABLE_DATA_LIST) {
        return new BallerinaTableDataListImpl(node);
      }
      else if (type == TABLE_LITERAL) {
        return new BallerinaTableLiteralImpl(node);
      }
      else if (type == TABLE_LITERAL_EXPRESSION) {
        return new BallerinaTableLiteralExpressionImpl(node);
      }
      else if (type == TABLE_QUERY) {
        return new BallerinaTableQueryImpl(node);
      }
      else if (type == TABLE_QUERY_EXPRESSION) {
        return new BallerinaTableQueryExpressionImpl(node);
      }
      else if (type == TABLE_TYPE_NAME) {
        return new BallerinaTableTypeNameImpl(node);
      }
      else if (type == TERNARY_EXPRESSION) {
        return new BallerinaTernaryExpressionImpl(node);
      }
      else if (type == THROW_STATEMENT) {
        return new BallerinaThrowStatementImpl(node);
      }
      else if (type == TIME_SCALE) {
        return new BallerinaTimeScaleImpl(node);
      }
      else if (type == TRANSACTION_CLAUSE) {
        return new BallerinaTransactionClauseImpl(node);
      }
      else if (type == TRANSACTION_PROPERTY_INIT_STATEMENT) {
        return new BallerinaTransactionPropertyInitStatementImpl(node);
      }
      else if (type == TRANSACTION_PROPERTY_INIT_STATEMENT_LIST) {
        return new BallerinaTransactionPropertyInitStatementListImpl(node);
      }
      else if (type == TRANSACTION_STATEMENT) {
        return new BallerinaTransactionStatementImpl(node);
      }
      else if (type == TRAP_EXPRESSION) {
        return new BallerinaTrapExpressionImpl(node);
      }
      else if (type == TRIPLE_BACKTICKED_BLOCK) {
        return new BallerinaTripleBacktickedBlockImpl(node);
      }
      else if (type == TRIPLE_BACK_TICK_DEPRECATED_INLINE_CODE) {
        return new BallerinaTripleBackTickDeprecatedInlineCodeImpl(node);
      }
      else if (type == TRY_CATCH_STATEMENT) {
        return new BallerinaTryCatchStatementImpl(node);
      }
      else if (type == TUPLE_BINDING_PATTERN) {
        return new BallerinaTupleBindingPatternImpl(node);
      }
      else if (type == TUPLE_DESTRUCTURING_STATEMENT) {
        return new BallerinaTupleDestructuringStatementImpl(node);
      }
      else if (type == TUPLE_LITERAL) {
        return new BallerinaTupleLiteralImpl(node);
      }
      else if (type == TUPLE_REF_BINDING_PATTERN) {
        return new BallerinaTupleRefBindingPatternImpl(node);
      }
      else if (type == TUPLE_TYPE_NAME) {
        return new BallerinaTupleTypeNameImpl(node);
      }
      else if (type == TYPE_ACCESS_EXPRESSION) {
        return new BallerinaTypeAccessExpressionImpl(node);
      }
      else if (type == TYPE_ACCESS_EXPR_INVOCATION_REFERENCE) {
        return new BallerinaTypeAccessExprInvocationReferenceImpl(node);
      }
      else if (type == TYPE_CONVERSION_EXPRESSION) {
        return new BallerinaTypeConversionExpressionImpl(node);
      }
      else if (type == TYPE_DEFINITION) {
        return new BallerinaTypeDefinitionImpl(node);
      }
      else if (type == TYPE_DESC_TYPE_NAME) {
        return new BallerinaTypeDescTypeNameImpl(node);
      }
      else if (type == TYPE_INIT_EXPRESSION) {
        return new BallerinaTypeInitExpressionImpl(node);
      }
      else if (type == TYPE_REFERENCE) {
        return new BallerinaTypeReferenceImpl(node);
      }
      else if (type == TYPE_TEST_EXPRESSION) {
        return new BallerinaTypeTestExpressionImpl(node);
      }
      else if (type == UNARY_EXPRESSION) {
        return new BallerinaUnaryExpressionImpl(node);
      }
      else if (type == UNION_TYPE_NAME) {
        return new BallerinaUnionTypeNameImpl(node);
      }
      else if (type == USER_DEFINE_TYPE_NAME) {
        return new BallerinaUserDefineTypeNameImpl(node);
      }
      else if (type == VALUE_TYPE_NAME) {
        return new BallerinaValueTypeNameImpl(node);
      }
      else if (type == VARIABLE_DEFINITION_STATEMENT) {
        return new BallerinaVariableDefinitionStatementImpl(node);
      }
      else if (type == VARIABLE_DEFINITION_STATEMENT_WITHOUT_ASSIGNMENT) {
        return new BallerinaVariableDefinitionStatementWithoutAssignmentImpl(node);
      }
      else if (type == VARIABLE_DEFINITION_STATEMENT_WITH_ASSIGNMENT) {
        return new BallerinaVariableDefinitionStatementWithAssignmentImpl(node);
      }
      else if (type == VARIABLE_REFERENCE_EXPRESSION) {
        return new BallerinaVariableReferenceExpressionImpl(node);
      }
      else if (type == VARIABLE_REFERENCE_LIST) {
        return new BallerinaVariableReferenceListImpl(node);
      }
      else if (type == VAR_MATCH_PATTERN) {
        return new BallerinaVarMatchPatternImpl(node);
      }
      else if (type == WAIT_EXPRESSION) {
        return new BallerinaWaitExpressionImpl(node);
      }
      else if (type == WAIT_FOR_COLLECTION) {
        return new BallerinaWaitForCollectionImpl(node);
      }
      else if (type == WAIT_KEY_VALUE) {
        return new BallerinaWaitKeyValueImpl(node);
      }
      else if (type == WHERE_CLAUSE) {
        return new BallerinaWhereClauseImpl(node);
      }
      else if (type == WHILE_STATEMENT) {
        return new BallerinaWhileStatementImpl(node);
      }
      else if (type == WHILE_STATEMENT_BODY) {
        return new BallerinaWhileStatementBodyImpl(node);
      }
      else if (type == WINDOW_CLAUSE) {
        return new BallerinaWindowClauseImpl(node);
      }
      else if (type == WITHIN_CLAUSE) {
        return new BallerinaWithinClauseImpl(node);
      }
      else if (type == WORKER_BODY) {
        return new BallerinaWorkerBodyImpl(node);
      }
      else if (type == WORKER_DEFINITION) {
        return new BallerinaWorkerDefinitionImpl(node);
      }
      else if (type == WORKER_RECEIVE_EXPRESSION) {
        return new BallerinaWorkerReceiveExpressionImpl(node);
      }
      else if (type == WORKER_SEND_ASYNC_EXPRESSION) {
        return new BallerinaWorkerSendAsyncExpressionImpl(node);
      }
      else if (type == WORKER_SEND_ASYNC_STATEMENT) {
        return new BallerinaWorkerSendAsyncStatementImpl(node);
      }
      else if (type == WORKER_WITH_STATEMENTS_BLOCK) {
        return new BallerinaWorkerWithStatementsBlockImpl(node);
      }
      else if (type == XML_ATTRIB) {
        return new BallerinaXmlAttribImpl(node);
      }
      else if (type == XML_ATTRIB_VARIABLE_REFERENCE) {
        return new BallerinaXmlAttribVariableReferenceImpl(node);
      }
      else if (type == XML_DOUBLE_QUOTED_STRING) {
        return new BallerinaXmlDoubleQuotedStringImpl(node);
      }
      else if (type == XML_ITEM) {
        return new BallerinaXmlItemImpl(node);
      }
      else if (type == XML_LITERAL) {
        return new BallerinaXmlLiteralImpl(node);
      }
      else if (type == XML_LITERAL_EXPRESSION) {
        return new BallerinaXmlLiteralExpressionImpl(node);
      }
      else if (type == XML_LOCAL_NAME) {
        return new BallerinaXmlLocalNameImpl(node);
      }
      else if (type == XML_NAMESPACE_NAME) {
        return new BallerinaXmlNamespaceNameImpl(node);
      }
      else if (type == XML_QUALIFIED_NAME) {
        return new BallerinaXmlQualifiedNameImpl(node);
      }
      else if (type == XML_QUOTED_STRING) {
        return new BallerinaXmlQuotedStringImpl(node);
      }
      else if (type == XML_SINGLE_QUOTED_STRING) {
        return new BallerinaXmlSingleQuotedStringImpl(node);
      }
      else if (type == XML_TEXT) {
        return new BallerinaXmlTextImpl(node);
      }
      else if (type == XML_TYPE_NAME) {
        return new BallerinaXmlTypeNameImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
