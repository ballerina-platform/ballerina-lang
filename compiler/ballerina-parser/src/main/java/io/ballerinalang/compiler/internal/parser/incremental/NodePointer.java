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

import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxUtils;
import io.ballerinalang.compiler.syntax.tree.ModulePart;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.Token;

/**
 * Represents a pointer to a {@code Node} in the syntax tree.
 *
 * @since 1.3.0
 */
class NodePointer {

    private Node current;
    /**
     * Current node's bucket index in it's parent
     */
    private int childBucketIndex;

    private NodePointer(Node node, int childBucketIndex) {
        this.current = node;
        this.childBucketIndex = childBucketIndex;
    }

    NodePointer(ModulePart node) {
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
        for (int bucket = 0; bucket < nonTerminalNode.bucketCount(); bucket++) {
            Node child = nonTerminalNode.childInBucket(bucket);
            if (!isZeroWidthNode(child) || isEOFToken(child)) {
                current = child;
                childBucketIndex = bucket;
                return this;
            }
        }
        return new NodePointer(null);
    }

    NodePointer nextSibling() {
        if (current.getParent() == null) {
            return new NodePointer(null);
        }

        NonTerminalNode parent = current.getParent();
        for (int bucket = childBucketIndex + 1; bucket < parent.bucketCount(); bucket++) {
            Node sibling = parent.childInBucket(bucket);
            if (!isZeroWidthNode(sibling) || isEOFToken(sibling)) {
                current = sibling;
                childBucketIndex = bucket;
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
        NonTerminalNode parent = current.getParent();
        if (parent.getParent() == null) {
            childBucketIndex = 0;
            current = parent;
            return this;
        }

        NonTerminalNode ancestor = parent.getParent();
        for (int i = 0; i < ancestor.bucketCount(); i++) {
            if (parent == ancestor.childInBucket(i)) {
                current = parent;
                childBucketIndex = i;
                return this;
            }
        }

        // This line cannot be reachable;
        return new NodePointer(null);
    }

    private boolean isEOFToken(Node node) {
        return node.getKind() == SyntaxKind.EOF_TOKEN;
    }

    private boolean isZeroWidthNode(Node node) {
        return node.getSpanWithMinutiae().width() == 0;
    }
}
