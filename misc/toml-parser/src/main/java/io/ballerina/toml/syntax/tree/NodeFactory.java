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
package io.ballerina.toml.syntax.tree;

import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeFactory;

import java.util.Objects;

/**
 * A factory for creating nodes in the syntax tree.
 *
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class NodeFactory extends AbstractNodeFactory {

    private NodeFactory() {
    }

    public static DocumentNode createDocumentNode(
            NodeList<DocumentMemberDeclarationNode> members,
            Token eofToken) {
        Objects.requireNonNull(members, "members must not be null");
        Objects.requireNonNull(eofToken, "eofToken must not be null");

        STNode stDocumentNode = STNodeFactory.createDocumentNode(
                members.underlyingListNode().internalNode(),
                eofToken.internalNode());
        return stDocumentNode.createUnlinkedFacade();
    }

    public static TableNode createTableNode(
            Token openBracket,
            SeparatedNodeList<ValueNode> identifier,
            Token closeBracket,
            NodeList<KeyValueNode> fields) {
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");
        Objects.requireNonNull(fields, "fields must not be null");

        STNode stTableNode = STNodeFactory.createTableNode(
                openBracket.internalNode(),
                identifier.underlyingListNode().internalNode(),
                closeBracket.internalNode(),
                fields.underlyingListNode().internalNode());
        return stTableNode.createUnlinkedFacade();
    }

    public static TableArrayNode createTableArrayNode(
            Token firstOpenBracket,
            Token secondOpenBracket,
            SeparatedNodeList<ValueNode> identifier,
            Token firstCloseBracket,
            Token secondCloseBracket,
            NodeList<KeyValueNode> fields) {
        Objects.requireNonNull(firstOpenBracket, "firstOpenBracket must not be null");
        Objects.requireNonNull(secondOpenBracket, "secondOpenBracket must not be null");
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(firstCloseBracket, "firstCloseBracket must not be null");
        Objects.requireNonNull(secondCloseBracket, "secondCloseBracket must not be null");
        Objects.requireNonNull(fields, "fields must not be null");

        STNode stTableArrayNode = STNodeFactory.createTableArrayNode(
                firstOpenBracket.internalNode(),
                secondOpenBracket.internalNode(),
                identifier.underlyingListNode().internalNode(),
                firstCloseBracket.internalNode(),
                secondCloseBracket.internalNode(),
                fields.underlyingListNode().internalNode());
        return stTableArrayNode.createUnlinkedFacade();
    }

    public static KeyValueNode createKeyValueNode(
            SeparatedNodeList<ValueNode> identifier,
            Token assign,
            ValueNode value) {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(assign, "assign must not be null");
        Objects.requireNonNull(value, "value must not be null");

        STNode stKeyValueNode = STNodeFactory.createKeyValueNode(
                identifier.underlyingListNode().internalNode(),
                assign.internalNode(),
                value.internalNode());
        return stKeyValueNode.createUnlinkedFacade();
    }

    public static ArrayNode createArrayNode(
            Token openBracket,
            SeparatedNodeList<ValueNode> values,
            Token closeBracket) {
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");

        STNode stArrayNode = STNodeFactory.createArrayNode(
                openBracket.internalNode(),
                values.underlyingListNode().internalNode(),
                closeBracket.internalNode());
        return stArrayNode.createUnlinkedFacade();
    }

    public static StringLiteralNode createStringLiteralNode(
            Token startDoubleQuote,
            Token content,
            Token endDoubleQuote) {
        Objects.requireNonNull(startDoubleQuote, "startDoubleQuote must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(endDoubleQuote, "endDoubleQuote must not be null");

        STNode stStringLiteralNode = STNodeFactory.createStringLiteralNode(
                startDoubleQuote.internalNode(),
                content.internalNode(),
                endDoubleQuote.internalNode());
        return stStringLiteralNode.createUnlinkedFacade();
    }

    public static NumericLiteralNode createNumericLiteralNode(
            SyntaxKind kind,
            Token value) {
        Objects.requireNonNull(value, "value must not be null");

        STNode stNumericLiteralNode = STNodeFactory.createNumericLiteralNode(
                kind,
                value.internalNode());
        return stNumericLiteralNode.createUnlinkedFacade();
    }

    public static BoolLiteralNode createBoolLiteralNode(
            Token value) {
        Objects.requireNonNull(value, "value must not be null");

        STNode stBoolLiteralNode = STNodeFactory.createBoolLiteralNode(
                value.internalNode());
        return stBoolLiteralNode.createUnlinkedFacade();
    }

    public static IdentifierLiteralNode createIdentifierLiteralNode(
            IdentifierToken value) {
        Objects.requireNonNull(value, "value must not be null");

        STNode stIdentifierLiteralNode = STNodeFactory.createIdentifierLiteralNode(
                value.internalNode());
        return stIdentifierLiteralNode.createUnlinkedFacade();
    }
}

