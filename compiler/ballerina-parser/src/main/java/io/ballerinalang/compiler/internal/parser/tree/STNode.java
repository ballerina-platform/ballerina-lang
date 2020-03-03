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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

public abstract class STNode {
    public final SyntaxKind kind;
    protected int width;

    protected final static STNode[] EMPTY_BUCKET = new STNode[0];

    // The following two fields allow us to navigate the tree without the knowledge of the particular tree nodes
    protected STNode[] childBuckets = EMPTY_BUCKET;
    protected int bucketCount;

    public STNode(SyntaxKind kind) {
        this.kind = kind;
    }

    public STNode(SyntaxKind kind, int width) {
        this.kind = kind;
        this.width = width;
    }

    protected void addChildNode(STNode node, int bucket) {
        if (node == null) {
            return;
        }
        this.width += node.width;
        this.childBuckets[bucket] = node;
    }

    protected void addTrivia(STNode trivia) {
        if (trivia == null) {
            return;
        }

        this.width += trivia.width;
    }

    public int width() {
        return width;
    }

    public int bucketCount() {
        return bucketCount;
    }

    public STNode childInBucket(int bucket) {
        if (bucket < 0 || bucket >= bucketCount) {
            return null;
        }

        return childBuckets[bucket];
    }

    public Node createFacade() {
        return createFacade(0, null);
    }

    public abstract Node createFacade(int position, NonTerminalNode parent);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (STNode child : this.childBuckets) {
            sb.append(child != null ? child.toString() : "");
        }
        return sb.toString();
    }
}
