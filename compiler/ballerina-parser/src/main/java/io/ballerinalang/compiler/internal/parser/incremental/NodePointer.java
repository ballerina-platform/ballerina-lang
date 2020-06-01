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
package io.ballerinalang.compiler.internal.parser.incremental;

import io.ballerinalang.compiler.internal.syntax.SyntaxUtils;
import io.ballerinalang.compiler.syntax.tree.ChildNodeList;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;

/**
 * Represents a pointer to a {@code Node} in the syntax tree.
 *
 * @since 1.3.0
 */
class NodePointer {

    private Node current;
    /**
     * Current node's bucket index in it's parent.
     */
    private int childBucketIndex;

    private NodePointer(Node node, int childBucketIndex) {
        this.current = node;
        this.childBucketIndex = childBucketIndex;
    }

    NodePointer(ModulePartNode node) {
        this(node, 0);
    }

    NodePointer clonePointer() {
        return new NodePointer(current, childBucketIndex);
    }

    Token currentToken() {
        return (Token) current;
    }

    Node currentNode() {
        return current;
    }

    boolean isAtEOF() {
        return isEOFToken(current);
    }

    NodePointer nextChild() {
        NonTerminalNode nonTerminalNode = (NonTerminalNode) current;
        int childIndex = 0;
        for (Node child : nonTerminalNode.children()) {
            if (!isZeroWidthNode(child) || isEOFToken(child)) {
                current = child;
                childBucketIndex = childIndex;
                return this;
            }
            childIndex++;
        }
        return new NodePointer(null);
    }

    NodePointer nextSibling() {
        if (current.parent() == null) {
            return new NodePointer(null);
        }

        NonTerminalNode parent = current.parent();
        ChildNodeList childNodeList = parent.children();
        for (int childIndex = childBucketIndex + 1; childIndex < childNodeList.size(); childIndex++) {
            Node sibling = childNodeList.get(childIndex);
            if (!isZeroWidthNode(sibling) || isEOFToken(sibling)) {
                current = sibling;
                childBucketIndex = childIndex;
                return this;
            }
        }
        return moveToParent().nextSibling();
    }

    /**
     * Returns a {@code NodePointer} to the next token. If the current
     * {@code NodePointer} is at token, then return the current {@code NodePointer}.
     * If the current {@code NodePointer} is at a {@code NonTerminalNode}, then move
     * down until you reach a token.
     *
     * @return NodePointer pointer to the next token
     */
    NodePointer nextToken() {
        NodePointer nodePointer = this;
        if (isAtEOF()) {
            return this;
        }

        Node node = nodePointer.current;
        while (!SyntaxUtils.isToken(node)) {
            nodePointer = nodePointer.nextChild();
            node = nodePointer.current;
        }

        return nodePointer;
    }

    private NodePointer moveToParent() {
        NonTerminalNode parent = current.parent();
        if (parent.parent() == null) {
            childBucketIndex = 0;
            current = parent;
            return this;
        }

        NonTerminalNode ancestor = parent.parent();
        int childIndex = 0;
        for (Node sibling : ancestor.children()) {
            if (parent == sibling) {
                current = parent;
                childBucketIndex = childIndex;
                return this;
            }
            childIndex++;
        }

        // This line cannot be reachable;
        return new NodePointer(null);
    }

    private boolean isEOFToken(Node node) {
        return node.kind() == SyntaxKind.EOF_TOKEN;
    }

    private boolean isZeroWidthNode(Node node) {
        return node.textRangeWithMinutiae().length() == 0;
    }
}
