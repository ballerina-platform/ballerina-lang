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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STMinutiae;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.syntax.NodeListUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A factory for creating nodes in the syntax tree.
 *
 * @since 1.3.0
 */
public abstract class AbstractNodeFactory {
    private static final MinutiaeList EMPTY_MINUTIAE_LIST = new MinutiaeList(null,
            STNodeFactory.createEmptyNodeList(), 0);
    @SuppressWarnings("rawtypes")
    private static final NodeList EMPTY_NODE_LIST = new NodeList(
            STNodeFactory.createEmptyNodeList().createUnlinkedFacade());


    public static IdentifierToken createIdentifierToken(String text) {
        STToken token = STNodeFactory.createIdentifierToken(text, STNodeFactory.createNodeList(),
                STNodeFactory.createNodeList());
        return token.createUnlinkedFacade();
    }

    public static IdentifierToken createIdentifierToken(String text,
                                                        MinutiaeList leadingMinutiae,
                                                        MinutiaeList trailingMinutiae) {
        STNode leadingMinutiaeSTNode = leadingMinutiae.internalNode();
        if (!NodeListUtils.isSTNodeList(leadingMinutiaeSTNode)) {
            leadingMinutiaeSTNode = STNodeFactory.createNodeList(leadingMinutiaeSTNode);
        }

        STNode trailingMinutiaeSTNode = trailingMinutiae.internalNode();
        if (!NodeListUtils.isSTNodeList(trailingMinutiaeSTNode)) {
            trailingMinutiaeSTNode = STNodeFactory.createNodeList(trailingMinutiaeSTNode);
        }

        STToken token = STNodeFactory.createIdentifierToken(text, leadingMinutiaeSTNode,
                trailingMinutiaeSTNode);
        return token.createUnlinkedFacade();
    }

    public static Token createToken(SyntaxKind kind) {
        return createToken(kind, createEmptyMinutiaeList(), createEmptyMinutiaeList());
    }

    public static Token createToken(SyntaxKind kind,
                                    MinutiaeList leadingMinutiae,
                                    MinutiaeList trailingMinutiae) {
        STNode leadingMinutiaeSTNode = leadingMinutiae.internalNode();
        if (!NodeListUtils.isSTNodeList(leadingMinutiaeSTNode)) {
            leadingMinutiaeSTNode = STNodeFactory.createNodeList(leadingMinutiaeSTNode);
        }

        STNode trailingMinutiaeSTNode = trailingMinutiae.internalNode();
        if (!NodeListUtils.isSTNodeList(trailingMinutiaeSTNode)) {
            trailingMinutiaeSTNode = STNodeFactory.createNodeList(trailingMinutiaeSTNode);
        }

        STToken token = STNodeFactory.createToken(kind, leadingMinutiaeSTNode,
                trailingMinutiaeSTNode);
        return token.createUnlinkedFacade();
    }

    public static MinutiaeList createEmptyMinutiaeList() {
        return EMPTY_MINUTIAE_LIST;
    }

    public static MinutiaeList createMinutiaeList(Minutiae... minutiaeNodes) {
        STNode[] internalNodes = new STNode[minutiaeNodes.length];
        for (int index = 0; index < minutiaeNodes.length; index++) {
            Minutiae minutiae = minutiaeNodes[index];
            Objects.requireNonNull(minutiae, "minutiae should not be null");
            internalNodes[index] = minutiae.internalNode();
        }
        return new MinutiaeList(null, STNodeFactory.createNodeList(internalNodes), 0);
    }

    public static MinutiaeList createMinutiaeList(Collection<Minutiae> minutiaeNodes) {
        return new MinutiaeList(null, STNodeFactory.createNodeList(
                minutiaeNodes.stream()
                        .map(minutiae -> Objects.requireNonNull(minutiae, "minutiae should not be null"))
                        .map(Minutiae::internalNode)
                        .collect(Collectors.toList())), 0);
    }

    public static Minutiae createCommentMinutiae(String text) {
        // TODO Validate the given text for comment characters
        // TODO Can we invoke the lexer here to get the minutiae
        STMinutiae internalNode = (STMinutiae) STNodeFactory.createMinutiae(
                SyntaxKind.COMMENT_MINUTIAE, text);
        return Minutiae.createUnlinked(internalNode);
    }

    public static Minutiae createWhitespaceMinutiae(String text) {
        // TODO Validate the given text for whitespace characters
        STMinutiae internalNode = (STMinutiae) STNodeFactory.createMinutiae(
                SyntaxKind.WHITESPACE_MINUTIAE, text);
        return Minutiae.createUnlinked((STMinutiae) internalNode);
    }

    public static Minutiae createEndOfLineMinutiae(String text) {
        // TODO Validate the given text for end of line characters
        STMinutiae internalNode = (STMinutiae) STNodeFactory.createMinutiae(
                SyntaxKind.END_OF_LINE_MINUTIAE, text);
        return Minutiae.createUnlinked(internalNode);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> NodeList<T> createEmptyNodeList() {
        return (NodeList<T>) EMPTY_NODE_LIST;
    }

    public static <T extends Node> NodeList<T> createNodeList(T... nodes) {
        STNode[] internalNodes = new STNode[nodes.length];
        for (int index = 0; index < nodes.length; index++) {
            T node = nodes[index];
            Objects.requireNonNull(node, "node should not be null");
            internalNodes[index] = node.internalNode();
        }
        return new NodeList<>(STNodeFactory.createNodeList(internalNodes).createUnlinkedFacade());
    }

    public static <T extends Node> NodeList<T> createNodeList(Collection<T> nodes) {
        return new NodeList<>(STNodeFactory.createNodeList(
                nodes.stream()
                        .map(node -> Objects.requireNonNull(node, "node should not be null"))
                        .map(Node::internalNode)
                        .collect(Collectors.toList())).createUnlinkedFacade());
    }

    protected static STNode getOptionalSTNode(Node node) {
        return node != null ? node.internalNode() : null;
    }
}
