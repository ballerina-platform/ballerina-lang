/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.composer.service.ballerina.parser.service;

/**
 * Constants used in definition the JSON model when parsing the bFile.
 */
public class BLangJSONModelConstants {

    public static final String ROOT = "root";

    public static final String CHILDREN = "children";

    public static final String BODY = "body";

    public static final String STATEMENTS = "statements";

    public static final String TOP_LEVEL_NODES = "topLevelNodes";

    public static final String ENDPOINT_NODES = "endpointNodes";

    public static final String DEFINITION_TYPE = "type";

    public static final String CONNECTOR_DEFINITION = "connector_definition";

    public static final String FUNCTION_DEFINITION = "function_definition";

    public static final String TYPE_MAPPER_DEFINITION = "type_mapper_definition";

    public static final String ACTION_DEFINITION = "action_definition";

    public static final String FUNCTIONS_NAME = "function_name";

    public static final String TYPE_MAPPER_NAME = "type_mapper_name";

    public static final String IS_PUBLIC_FUNCTION = "is_public_function";

    public static final String IS_LAMBDA_FUNCTION = "is_lambda_function";

    public static final String ANNOTATION_NAME = "annotation_name";

    public static final String ANNOTATION_ATTACHMENT_NAME = "annotation_attachment_name";

    public static final String ANNOTATION_ATTACHMENT_POINTS = "annotation_attachment_points";

    public static final String PACKAGE_DEFINITION = "package";

    public static final String PACKAGE_NAME = "package_name";

    public static final String PACKAGE_PATH = "package_path";

    public static final String IMPORT_DEFINITION = "import";

    public static final String IMPORT_PACKAGE_NAME = "import_package_name";

    public static final String IMPORT_PACKAGE_PATH = "import_package_path";

    public static final String IMPORT_AS_NAME = "import_as_name";

    public static final String CONSTANT_DEFINITION = "constant_definition";

    public static final String CONSTANT_DEFINITION_BTYPE = "constant_definition_btype";

    public static final String CONSTANT_DEFINITION_IDENTIFIER = "constant_definition_identifier";

    public static final String GLOBAL_VARIABLE_DEFINITION = "global_variable_definition";

    public static final String GLOBAL_VARIABLE_DEFINITION_BTYPE = "global_variable_definition_btype";

    public static final String GLOBAL_VARIABLE_DEFINITION_IDENTIFIER = "global_variable_definition_identifier";

    public static final String SERVICE_DEFINITION = "service_definition";

    public static final String ANNOTATION_DEFINITION = "annotation_definition";

    public static final String ANNOTATION_ATTRIBUTE_DEFINITION = "annotation_attribute_definition";

    public static final String ANNOTATION_ATTRIBUTE_NAME = "annotation_attribute_name";

    public static final String ANNOTATION_ATTRIBUTE_TYPE = "annotation_attribute_type";

    public static final String ANNOTATION_ATTRIBUTE_PACKAGE_PATH = "annotation_attribute_package_path";

    public static final String SERVICE_NAME = "service_name";

    public static final String SERVICE_PROTOCOL_PKG_NAME = "service_protocol_package_name";

    public static final String SERVICE_PROTOCOL_PKG_PATH = "service_protocol_package_path";

    public static final String RESOURCE_DEFINITION = "resource_definition";

    public static final String ANNOTATION_ATTACHMENT = "annotation_attachment";

    public static final String ANNOTATION_ATTRIBUTE = "annotation_attribute";

    public static final String ANNOTATION_ATTRIBUTE_PAIR_KEY = "annotation_attribute_pair_key";

    public static final String ANNOTATION_ATTRIBUTE_VALUE = "annotation_attribute_value";

    public static final String PARAMETER_DEFINITION = "parameter_definition";

    public static final String ANNOTATION_ATTACHMENT_PACKAGE_NAME = "annotation_attachment_package_name";

    public static final String ANNOTATION_ATTACHMENT_FULL_PACKAGE_NAME = "annotation_attachment_full_package_name";

    public static final String VARIABLE_DEFINITION = "variable_definition";

    public static final String VARIABLE_DEFINITION_STATEMENT = "variable_definition_statement";

    public static final String VARIABLE_REFERENCE_LIST = "variable_reference_list";

    public static final String RESOURCE_NAME = "resource_name";

    public static final String WORKER_DEFINITION = "worker";

    public static final String WORKER_INVOCATION_STATEMENT = "worker_invocation_statement";

    public static final String WORKER_REPLY_STATEMENT = "worker_reply_statement";

    public static final String WORKER_NAME = "worker_name";

    public static final String PARAMETER_NAME = "parameter_name";

    public static final String PARAMETER_TYPE = "parameter_type";

    public static final String VARIABLE_NAME = "variable_name";

    public static final String VARIABLE_TYPE = "variable_type";

    public static final String CONNECTOR_NAME = "connector_name";

    public static final String ACTION_NAME = "action_name";

    public static final String ACTION_PKG_NAME = "action_pkg_name";

    public static final String ACTION_CONNECTOR_NAME = "action_connector_name";

    public static final String STATEMENT_TYPE = "type";

    public static final String EXPRESSION_TYPE = "type";

    public static final String COMMENT_STATEMENT = "comment_statement";

    public static final String COMMENT_STRING = "comment_string";

    public static final String ASSIGNMENT_STATEMENT = "assignment_statement";

    public static final String IS_DECLARED_WITH_VAR = "is_declared_with_var";

    public static final String WHILE_STATEMENT = "while_statement";

    public static final String FUNCTION_INVOCATION_STATEMENT = "function_invocation_statement";

    public static final String RETURN_TYPE = "return_type";

    public static final String REPLY_STATEMENT = "reply_statement";

    public static final String RETURN_STATEMENT = "return_statement";

    public static final String BREAK_STATEMENT = "break_statement";

    public static final String CONTINUE_STATEMENT = "continue_statement";

    public static final String ABORT_STATEMENT = "abort_statement";

    public static final String FUNCTION_INVOCATION_EXPRESSION = "function_invocation_expression";

    public static final String ACTION_INVOCATION_EXPRESSION = "action_invocation_expression";

    public static final String ACTION_INVOCATION_STATEMENT = "action_invocation_statement";

    public static final String BASIC_LITERAL_EXPRESSION = "basic_literal_expression";

    public static final String BASIC_LITERAL_VALUE = "basic_literal_value";

    public static final String NULL_LITERAL_EXPRESSION = "null_literal_expression";

    public static final String BASIC_LITERAL_TYPE = "basic_literal_type";

    public static final String UNARY_EXPRESSION = "unary_expression";

    public static final String ADD_EXPRESSION = "add_expression";

    public static final String SUBTRACT_EXPRESSION = "subtract_expression";

    public static final String MULTIPLY_EXPRESSION = "multiplication_expression";

    public static final String DIVISION_EXPRESSION = "division_expression";

    public static final String MOD_EXPRESSION = "mod_expression";

    public static final String AND_EXPRESSION = "and_expression";

    public static final String OR_EXPRESSION = "or_expression";

    public static final String EQUAL_EXPRESSION = "equal_expression";

    public static final String NOT_EQUAL_EXPRESSION = "not_equal_expression";

    public static final String GREATER_EQUAL_EXPRESSION = "greater_equal_expression";

    public static final String GREATER_THAN_EXPRESSION = "greater_than_expression";

    public static final String LESS_EQUAL_EXPRESSION = "less_equal_expression";

    public static final String LESS_THAN_EXPRESSION = "less_than_expression";

    public static final String SIMPLE_VARIABLE_REFERENCE_EXPRESSION = "simple_variable_reference_expression";

    public static final String VARIABLE_REFERENCE_NAME = "variable_reference_name";

    public static final String ARRAY_INIT_EXPRESSION = "array_init_expression";

    public static final String INSTANCE_CREATION_EXPRESSION = "instance_creation_expression";

    public static final String INSTANCE_CREATION_EXPRESSION_INSTANCE_TYPE = "instance_type";

    public static final String IF_ELSE_STATEMENT = "if_else_statement";

    public static final String IF_STATEMENT = "if_statement";

    public static final String ELSE_STATEMENT = "else_statement";

    public static final String ELSE_IF_STATEMENT = "else_if_statement";

    public static final String ELSE_IF_STATEMENTS = "else_if_statements";

    public static final String CONDITION = "condition";

    public static final String EXPRESSION = "expression";

    public static final String STRUCT_DEFINITION = "struct_definition";

    public static final String INDEX_BASED_VAR_REF_EXPRESSION = "index_based_variable_reference_expression";

    public static final String FIELD_BASED_VAR_REF_EXPRESSION = "field_based_variable_reference_expression";

    public static final String FIELD_NAME = "field_name";

    public static final String STRUCT_NAME = "struct_name";

    public static final String KEY_VALUE_EXPRESSION = "key_value_expression";

    public static final String TYPE_CAST_EXPRESSION = "type_cast_expression";

    public static final String TYPE_CONVERSION_EXPRESSION = "type_conversion_expression";

    public static final String TARGET_TYPE = "target_type";

    public static final String TYPE_NAME = "type_name";

    public static final String CONNECTOR_INIT_EXPR = "connector_init_expr";

    public static final String PARENT_CONNECTOR_INIT_EXPR = "parent_connector_init_expr";

    public static final String ARGUMENTS = "arguments";

    public static final String LINE_NUMBER = "line_number";

    public static final String POSITION_INFO = "position_info";

    public static final String START_LINE = "start_line";

    public static final String START_OFFSET = "start_offset";

    public static final String STOP_LINE = "stop_line";

    public static final String STOP_OFFSET = "stop_offset";

    public static final String REFERENCE_TYPE_INIT_EXPR = "reference_type_init_expression";

    public static final String EXPRESSION_OPERATOR = "operator";

    public static final String TRY_CATCH_STATEMENT = "try_catch_statement";

    public static final String TRY_BLOCK = "try_block";

    public static final String CATCH_BLOCK = "catch_block";

    public static final String FINALLY_BLOCK = "finally_block";

    public static final String CATCH_BLOCKS = "catch_blocks";

    public static final String THROW_STATEMENT = "throw_statement";

    public static final String ARGUMENT_PARAMETER_DEFINITIONS = "argument_parameter_definitions";

    public static final String RETURN_PARAMETER_DEFINITIONS = "return_parameter_definitions";

    public static final String WHITESPACE_DESCRIPTOR = "whitespace_descriptor";

    public static final String CHILD_DESCRIPTORS = "children";

    public static final String WHITESPACE_REGIONS = "regions";

    public static final String TRANSFORM_STATEMENT = "transform_statement";

    public static final String TRANSFORM_INPUT = "transform_input";

    public static final String TRANSFORM_OUTPUT = "transform_output";

    public static final String IS_ARRAY_TYPE = "is_array_type";

    public static final String DIMENSIONS = "dimensions";

    public static final String TYPE_CONSTRAINT = "type_constraint";

    public static final String EXPRESSION_LIST = "expression_list";

    public static final String WORKER_REPLY_EXPRESSION = "worker_reply_expression";

    public static final String WORKER_INVOKE_EXPRESSION = "worker_invoke_expression";

    public static final String JOIN_STATEMENT = "join_statement";

    public static final String FORK_JOIN_STATEMENT = "fork_join_statement";

    public static final String TRANSACTION_ABORTED_STATEMENT = "transaction_aborted_statement";

    public static final String TRANSACTION_STATEMENT = "transaction_statement";

    public static final String ABORTED_STATEMENT = "aborted_statement";

    public static final String ABORTED_CLAUSE = "AbortedClause";

    public static final String COMMITTED_CLAUSE = "CommittedClause";

    public static final String COMMITTED_STATEMENT = "committed_statement";

    public static final String FAILED_STATEMENT = "failed_statement";

    public static final String JOIN_TYPE = "join_type";

    public static final String IS_IDENTIFIER_LITERAL = "is_identifier_literal";

    public static final String TIMEOUT_STATEMENT = "timeout_statement";

    public static final String JOIN_PARAMETER = "join_parameter";

    public static final String TIMEOUT_PARAMETER = "timeout_parameter";

    public static final String IS_NATIVE = "is_native";

    public static final String SIMPLE_TYPE_NAME = "simple_type_name";

    public static final String JOIN_WORKERS = "join_workers";

    public static final String JOIN_COUNT = "join_count";

    public static final String BVALUE = "bvalue";

    public static final String BVALUE_TYPE = "bvalue_type";

    public static final String BVALUE_STRING_VALUE = "bvalue_string_value";

    public static final String NAMESPACE_DECLARATION_STATEMENT = "namespace_declaration_statement";

    public static final String NAMESPACE_URI = "namespace_uri";

    public static final String NAMESPACE_NAME = "namespace_name";

    public static final String NAMESPACE_IDENTIFIER = "namespace_identifier";

    public static final String NAMESPACE_PACKAGE_PATH = "namespace_packagePath";

    public static final String NAMESPACE_DECLARATION = "namespace_declaration";

    public static final String XML_QNAME_EXPRESSION = "xml_qname_expression";

    public static final String XML_QNAME_LOCALNAME = "local_name";

    public static final String XML_QNAME_PREFIX = "prefix";

    public static final String XML_QNAME_URI = "namespace_uri";

    public static final String XML_QNAME_IS_LHS = "is_lhs_expression";

    public static final String XML_QNAME_IS_USED_IN_XML = "is_used_in_xml";

    public static final String XML_ATTRIBUTE_REF_EXPR = "xml_attribute_ref_expression";

    public static final String XML_ATTRIBUTE_REF_IS_LHS_EXPR = "is_lhs_expression";

    public static final String RETRY_STATEMENT = "retry_statement";

    public static final String HAS_RETURNS_KEYWORD = "has_returns_keyword";

    public static final String XML_ELEMENT_LITERAL = "xml_element_literal";

    public static final String XML_TEXT_LITERAL = "xml_text_literal";

    public static final String XML_SEQUENCE_LITERAL = "xml_sequence_literal";

    public static final String XML_ATTRIBUTES = "attributes";

    public static final String CONCAT_EXPR = "concat_expr";

    public static final String XML_COMMENT_LITERAL = "xml_comment_literal";

    public static final String XML_PI_LITERAL = "xml_pi_literal";

    public static final String STRING_TEMPLATE_LITERAL = "string_template_literal";

    public static final String PARAMETERS = "parameters";

    public static final String RETURN_PARAMETERS = "returnParameters";
    public static final String ACTIONS = "actions";
    public static final String RESOURCES = "resources";

    public static final String WORKERS = "workers";
    public static final String FAILED_BODY = "failedBody";
}
