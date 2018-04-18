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
package org.ballerinalang.model.tree;

/**
 * @since 0.94
 */
public enum NodeKind {

    ACTION,
    ANNOTATION,
    ANNOTATION_ATTACHMENT,
    ANNOTATION_ATTRIBUTE,
    CATCH,
    COMPILATION_UNIT,
    CONNECTOR,
    DEPRECATED,
    DOCUMENTATION,
    ENDPOINT,
    ENUM,
    ENUMERATOR,
    FUNCTION,
    IDENTIFIER,
    IMPORT,
    PACKAGE,
    PACKAGE_DECLARATION,
    RECORD_LITERAL_KEY_VALUE,
    RESOURCE,
    SERVICE,
    STRUCT,
    TYPE_DEFINITION,
    RECORD,
    OBJECT,
    TABLE,
    VARIABLE,
    WORKER,
    XMLNS,
    TRANSFORMER,

    /* Expressions */
    DOCUMENTATION_ATTRIBUTE,
    ANNOTATION_ATTACHMENT_ATTRIBUTE,
    ANNOTATION_ATTACHMENT_ATTRIBUTE_VALUE,
    ARRAY_LITERAL_EXPR,
    BINARY_EXPR,
    ELVIS_EXPR,
    BRACED_TUPLE_EXPR,
    Type_INIT_EXPR,
    FIELD_BASED_ACCESS_EXPR,
    INDEX_BASED_ACCESS_EXPR,
    INT_RANGE_EXPR,
    INVOCATION,
    LAMBDA,
    LITERAL,
    RECORD_LITERAL_EXPR,
    SIMPLE_VARIABLE_REF,
    STRING_TEMPLATE_LITERAL,
    TERNARY_EXPR,
    AWAIT_EXPR,
    TYPEDESC_EXPRESSION,
    TYPE_CAST_EXPR,
    TYPE_CONVERSION_EXPR,
    IS_ASSIGNABLE_EXPR,
    UNARY_EXPR,
    REST_ARGS_EXPR,
    NAMED_ARGS_EXPR,
    XML_QNAME,
    XML_ATTRIBUTE,
    XML_ATTRIBUTE_ACCESS_EXPR,
    XML_QUOTED_STRING,
    XML_ELEMENT_LITERAL,
    XML_TEXT_LITERAL,
    XML_COMMENT_LITERAL,
    XML_PI_LITERAL,
    XML_SEQUENCE_LITERAL,
    STATEMENT_EXPRESSION,
    MATCH_EXPRESSION,
    MATCH_EXPRESSION_PATTERN_CLAUSE,
    CHECK_EXPR,

    /* streams/tables expressions */
    SELECT_EXPRESSION,
    TABLE_QUERY_EXPRESSION,

    /* Statements */
    ABORT,
    DONE,
    RETRY,
    ASSIGNMENT,
    COMPOUND_ASSIGNMENT,
    POST_INCREMENT,
    BIND,
    BLOCK,
    BREAK,
    NEXT,
    EXPRESSION_STATEMENT,
    FOREACH,
    FORK_JOIN,
    IF,
    MATCH,
    MATCH_PATTERN_CLAUSE,
    REPLY,
    RETURN,
    THROW,
    TRANSACTION,
    TRANSFORM,
    TRY,
    TUPLE_DESTRUCTURE,
    VARIABLE_DEF,
    WHILE,
    LOCK,
    WORKER_RECEIVE,
    WORKER_SEND,
    STREAM,

    /* Types */
    ARRAY_TYPE,
    UNION_TYPE_NODE,
    TUPLE_TYPE_NODE,
    BUILT_IN_REF_TYPE,
    CONSTRAINED_TYPE,
    FUNCTION_TYPE,
    USER_DEFINED_TYPE,
    ENDPOINT_TYPE,
    VALUE_TYPE,

    /* Clauses */
    ORDER_BY,
    GROUP_BY,
    HAVING,
    SELECT_CLAUSE,
    WHERE,
    FUNCTION_CLAUSE,
    WINDOW_CLAUSE,
    STREAMING_INPUT,
    JOIN_STREAMING_INPUT,
    TABLE_QUERY,
    SET_ASSIGNMENT_CLAUSE,
    SET,
    STREAM_ACTION,
    PATTERN_STREAMING_EDGE_INPUT,
    PATTERN_STREAMING_INPUT,
    STREAMING_QUERY,
    QUERY,
    STREAMING_QUERY_DECLARATION,
    WITHIN,
    PATTERN_CLAUSE,
    OUTPUT_RATE_LIMIT,
    FOREVER,
}
