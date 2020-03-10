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
package io.ballerinalang.compiler.internal.parser;

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
public class NodePointer {

    private Node current;
    /**
     * Current node's bucket index in it's parent
     */
    private int childBucketIndex;

    private NodePointer(Node node, int childBucketIndex) {
        this.current = node;
        this.childBucketIndex = childBucketIndex;
    }

    public NodePointer(ModulePart node) {
        this(node, 0);
    }

    public NodePointer nextToken() {

        if (SyntaxUtils.isToken(current)) {
            nextSibling();
        }

        if (!SyntaxUtils.isToken(current)) {
            firstChild();
            while (!SyntaxUtils.isToken(current)) {
                firstChild();
            }
        }

        // case 1: We are at a NonTerminalNode => nextChild()
        // case 2: We are at a Token already => nextSibling();
        return this;
    }

    public Token currentToken() {
        return (Token) current;
    }

    public boolean isAtEOF() {
        return current.getKind() == SyntaxKind.EOF_TOKEN;
    }

    private void nextSibling() {
        NonTerminalNode parent = current.getParent();

        int bucketCount = parent.bucketCount();
        for (int bucket = childBucketIndex + 1; bucket < bucketCount; bucket++) {
            Node sibling = parent.childInBucket(bucket);
            if (sibling.getSpanWithMinutiae().width() != 0) {
                current = sibling;
                childBucketIndex = bucket;
                return;
            }
        }

        // We need to move the next child of parent.getParent();
        parent();
        nextSibling();
    }

    private void parent() {
        NonTerminalNode parent = current.getParent();
        if (parent == null) {
            throw new UnsupportedOperationException(" Handle this condition");
        }

        NonTerminalNode ancestor = parent.getParent();
        for (int i = 0; i < ancestor.bucketCount(); i++) {
            if (parent == ancestor.childInBucket(i)) {
                current = parent;
                childBucketIndex = i;
                break;
            }
        }
    }

    // first child
    private void firstChild() {
        NonTerminalNode nonTerminalNode = (NonTerminalNode) current;
        current = nonTerminalNode.childInBucket(0);
        childBucketIndex = 0;
        if (current.getSpanWithMinutiae().width() == 0) {
            nextSibling();
        }
    }

}
