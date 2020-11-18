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

import java.util.function.Function;

/**
 * Produces a new tree by doing a depth-first traversal of the tree.
 *
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class TreeModifier extends NodeTransformer<Node> {

    @Override
    public DocumentNode transform(
            DocumentNode documentNode) {
        NodeList<DocumentMemberDeclarationNode> members =
                modifyNodeList(documentNode.members());
        Token eofToken =
                modifyToken(documentNode.eofToken());
        return documentNode.modify(
                members,
                eofToken);
    }

    @Override
    public TableNode transform(
            TableNode tableNode) {
        Token openBracket =
                modifyToken(tableNode.openBracket());
        SeparatedNodeList<ValueNode> identifier =
                modifySeparatedNodeList(tableNode.identifier());
        Token closeBracket =
                modifyToken(tableNode.closeBracket());
        NodeList<KeyValueNode> fields =
                modifyNodeList(tableNode.fields());
        return tableNode.modify(
                openBracket,
                identifier,
                closeBracket,
                fields);
    }

    @Override
    public TableArrayNode transform(
            TableArrayNode tableArrayNode) {
        Token firstOpenBracket =
                modifyToken(tableArrayNode.firstOpenBracket());
        Token secondOpenBracket =
                modifyToken(tableArrayNode.secondOpenBracket());
        SeparatedNodeList<ValueNode> identifier =
                modifySeparatedNodeList(tableArrayNode.identifier());
        Token firstCloseBracket =
                modifyToken(tableArrayNode.firstCloseBracket());
        Token secondCloseBracket =
                modifyToken(tableArrayNode.secondCloseBracket());
        NodeList<KeyValueNode> fields =
                modifyNodeList(tableArrayNode.fields());
        return tableArrayNode.modify(
                firstOpenBracket,
                secondOpenBracket,
                identifier,
                firstCloseBracket,
                secondCloseBracket,
                fields);
    }

    @Override
    public KeyValueNode transform(
            KeyValueNode keyValueNode) {
        SeparatedNodeList<ValueNode> identifier =
                modifySeparatedNodeList(keyValueNode.identifier());
        Token assign =
                modifyToken(keyValueNode.assign());
        ValueNode value =
                modifyNode(keyValueNode.value());
        return keyValueNode.modify(
                identifier,
                assign,
                value);
    }

    @Override
    public ArrayNode transform(
            ArrayNode arrayNode) {
        Token openBracket =
                modifyToken(arrayNode.openBracket());
        SeparatedNodeList<ValueNode> values =
                modifySeparatedNodeList(arrayNode.values());
        Token closeBracket =
                modifyToken(arrayNode.closeBracket());
        return arrayNode.modify(
                openBracket,
                values,
                closeBracket);
    }

    @Override
    public StringLiteralNode transform(
            StringLiteralNode stringLiteralNode) {
        Token startDoubleQuote =
                modifyToken(stringLiteralNode.startDoubleQuote());
        Token content =
                modifyToken(stringLiteralNode.content());
        Token endDoubleQuote =
                modifyToken(stringLiteralNode.endDoubleQuote());
        return stringLiteralNode.modify(
                startDoubleQuote,
                content,
                endDoubleQuote);
    }

    @Override
    public NumericLiteralNode transform(
            NumericLiteralNode numericLiteralNode) {
        Token value =
                modifyToken(numericLiteralNode.value());
        return numericLiteralNode.modify(
                numericLiteralNode.kind(),
                value);
    }

    @Override
    public BoolLiteralNode transform(
            BoolLiteralNode boolLiteralNode) {
        Token value =
                modifyToken(boolLiteralNode.value());
        return boolLiteralNode.modify(
                value);
    }

    @Override
    public IdentifierLiteralNode transform(
            IdentifierLiteralNode identifierLiteralNode) {
        IdentifierToken value =
                modifyNode(identifierLiteralNode.value());
        return identifierLiteralNode.modify(
                value);
    }

    // Tokens

    @Override
    public Token transform(Token token) {
        return token;
    }

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        return identifier;
    }

    @Override
    protected Node transformSyntaxNode(Node node) {
        return node;
    }

    protected <T extends Node> NodeList<T> modifyNodeList(NodeList<T> nodeList) {
        return modifyGenericNodeList(nodeList, NodeList::new);
    }

    protected <T extends Node> SeparatedNodeList<T> modifySeparatedNodeList(SeparatedNodeList<T> nodeList) {
        Function<NonTerminalNode, SeparatedNodeList> nodeListCreator = SeparatedNodeList::new;
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        STNode[] newSTNodes = new STNode[nodeList.internalListNode.size()];

        for (int index = 0; index < nodeList.size(); index++) {
            T oldNode = nodeList.get(index);
            T newNode = modifyNode(oldNode);
            if (oldNode != newNode) {
                nodeModified = true;
            }

            newSTNodes[2 * index] = newNode.internalNode();
            if (index == nodeList.size() - 1) {
                break;
            }

            Token oldSeperator = nodeList.getSeparator(index);
            Token newSeperator = modifyToken(oldSeperator);

            if (oldSeperator != newSeperator) {
                nodeModified = true;
            }

            newSTNodes[(2 * index) + 1] = newSeperator.internalNode();
        }

        if (!nodeModified) {
            return nodeList;
        }

        STNode stNodeList = STNodeFactory.createNodeList(java.util.Arrays.asList(newSTNodes));
        return nodeListCreator.apply(stNodeList.createUnlinkedFacade());
    }

    private <T extends Node, N extends NodeList<T>> N modifyGenericNodeList(
            N nodeList,
            Function<NonTerminalNode, N> nodeListCreator) {
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        STNode[] newSTNodes = new STNode[nodeList.size()];
        for (int index = 0; index < nodeList.size(); index++) {
            T oldNode = nodeList.get(index);
            T newNode = modifyNode(oldNode);
            if (oldNode != newNode) {
                nodeModified = true;
            }
            newSTNodes[index] = newNode.internalNode();
        }

        if (!nodeModified) {
            return nodeList;
        }

        STNode stNodeList = STNodeFactory.createNodeList(java.util.Arrays.asList(newSTNodes));
        return nodeListCreator.apply(stNodeList.createUnlinkedFacade());
    }

    protected <T extends Token> T modifyToken(T token) {
        if (token == null) {
            return null;
        }
        // TODO
        return (T) token.apply(this);
    }

    protected <T extends Node> T modifyNode(T node) {
        if (node == null) {
            return null;
        }
        // TODO
        return (T) node.apply(this);
    }
}

