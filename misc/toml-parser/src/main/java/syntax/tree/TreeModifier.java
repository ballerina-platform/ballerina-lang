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
package syntax.tree;

import internal.parser.tree.STNode;
import internal.parser.tree.STNodeFactory;

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
    public ModulePartNode transform(
            ModulePartNode modulePartNode) {
        NodeList<ModuleMemberDeclarationNode> members =
                modifyNodeList(modulePartNode.members());
        Token eofToken =
                modifyToken(modulePartNode.eofToken());
        return modulePartNode.modify(
                members,
                eofToken);
    }

    @Override
    public TableNode transform(
            TableNode tableNode) {
        Token openBracket =
                modifyToken(tableNode.openBracket());
        IdentifierToken identifier =
                modifyNode(tableNode.identifier());
        Token closeBracket =
                modifyToken(tableNode.closeBracket());
        NodeList<Node> fields =
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
        Token openBracket =
                modifyToken(tableArrayNode.openBracket());
        IdentifierToken identifier =
                modifyNode(tableArrayNode.identifier());
        Token closeBracket =
                modifyToken(tableArrayNode.closeBracket());
        return tableArrayNode.modify(
                openBracket,
                identifier,
                closeBracket);
    }

    @Override
    public KeyValue transform(
            KeyValue keyValue) {
        Token identifier =
                modifyToken(keyValue.identifier());
        Token assign =
                modifyToken(keyValue.assign());
        Token value =
                modifyToken(keyValue.value());
        return keyValue.modify(
                identifier,
                assign,
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

