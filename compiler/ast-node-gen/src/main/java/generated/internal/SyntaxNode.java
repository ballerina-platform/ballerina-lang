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
package generated.internal;


import generated.facade.BLNode;
import generated.facade.BLNonTerminalNode;

public abstract class SyntaxNode {
    protected final static SyntaxNode[] EMPTY_BUCKET = new SyntaxNode[0];
    public final SyntaxKind kind;
    protected int width;
    // The following two fields allow us to navigate the tree without the knowledge of the particular tree nodes
    protected SyntaxNode[] childBuckets = EMPTY_BUCKET;
    protected int bucketCount;

    public SyntaxNode(SyntaxKind kind) {
        this.kind = kind;
    }

    public SyntaxNode(SyntaxKind kind, int width) {
        this.kind = kind;
        this.width = width;
    }

    protected void addChildNode(SyntaxNode node, int bucket) {
        if (node == null) {
            return;
        }
        this.width += node.width;
        this.childBuckets[bucket] = node;
    }

    protected void addTrivia(SyntaxNode trivia) {
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

    public SyntaxNode childInBucket(int bucket) {
        if (bucket < 0 || bucket >= bucketCount) {
            return null;
        }

        return childBuckets[bucket];
    }

    public BLNode createFacade() {
        return createFacade(0, null);
    }

    public abstract BLNode createFacade(int position, BLNonTerminalNode parent);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SyntaxNode child : this.childBuckets) {
            sb.append(child != null ? child.toString() : "");
        }
        return sb.toString();
    }
}
