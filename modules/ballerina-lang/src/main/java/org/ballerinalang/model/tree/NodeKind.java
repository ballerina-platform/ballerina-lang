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

    ABORT,
    ACTION,
    ANNOTATION,
    ANNOTATION_ATTRIBUTE,
    ANNOTATION_ATTACHMENT,
    ANNOTATION_ATTACHMENT_ATTRIBUTE,
    ANNOTATION_ATTACHMENT_ATTRIBUTE_VALUE,
    ASSIGNMENT,
    BREAK,
    BLOCK,
    CATCH,
    COMPILATION_UNIT,
    COMMENT,
    CONNECTOR,
    CONTINUE,
    ENUM,
    EXPRESSION_STATEMENT,
    FORKJOIN,
    FUNCTION,
    IDENTIFIER,
    IF,
    IMPORT,
    LITERAL,
    PACKAGE,
    PACKAGE_DECLARATION,
    SERVICE,
    REPLY,
    RESOURCE,
    RETURN,
    STRUCT,
    THROW,
    TRANSACTION,
    TRANSFORM,
    TRY,
    TYPE,
    VARIABLE,
    VARIABLE_REF,
    WHILE,
    WORKER,
    WORKER_SEND,
    WORKER_RECEIVE,
    XMLNS,
    /* Expressions */
    INVOCATION,
    ARRAY_LITERAL_EXPR,
    RECODE_LITERAL_EXPR,
    BINARY_EXPR,
    UNARY_EXPR,
    CONNECTOR_INIT_EXPR,
    LAMBDA,
    XML_QNAME,
    XML_ELEMENT_LITERAL,
    XML_TEXT_LITERAL,
    XML_COMMENT_LITERAL,
    XML_PI_LITERAL,
    XML_QUOTED_STRING,
    TYPE_CAST_EXPR,
    TYPE_CONVERSION_EXPR,
    TERNARY_EXPR
}
