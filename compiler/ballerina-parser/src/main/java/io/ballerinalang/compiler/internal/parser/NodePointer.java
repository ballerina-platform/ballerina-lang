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
        if (parent.bucketCount() == childBucketIndex + 1) {
            // `current` is the last child of it's parent. We need to move the next child of current.getParent().getParent();
            current = parent;
            parent();
        }

        childBucketIndex++;
        current = current.getParent().childInBucket(childBucketIndex);
    }

    private void parent() {
        // TODO we need to find bucketIndex of the current in parent.
        NonTerminalNode parent = current.getParent();
        if (parent == null) {
            throw new UnsupportedOperationException(" Handle this condition");
        }

        for (int i = 0; i < parent.bucketCount(); i++) {
            if (current == parent.childInBucket(i)) {
                childBucketIndex = i;
                break;
            }
        }

        if (parent.bucketCount() == childBucketIndex + 1) {
            current = parent;
            parent();
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
