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
package generated.facade;

import generated.internal.SyntaxNode;

/**
 * Represents a node with children in the syntax tree.
 */
// TODO This class can be used by multiple threads. Since the tree is lazily constructed,
//  we need to ensure only one tree is created.
// TODO find a better name for this class.
public abstract class BLNonTerminalNode extends BLNode {

    // The following two fields allow us to navigate the tree without the knowledge of the particular tree nodes
    protected final BLNode[] childBuckets;
    protected final int bucketCount;

    public BLNonTerminalNode(SyntaxNode node, int position, BLNonTerminalNode parent) {
        super(node, position, parent);

        this.bucketCount = node.bucketCount();
        this.childBuckets = new BLNode[this.bucketCount];
    }

    public int bucketCount() {
        return bucketCount;
    }

    public BLNode childInBucket(int bucket) {
        if (bucket < 0 || bucket >= bucketCount) {
            return null;
        }

        return childBuckets[bucket];
    }

    protected BLNode getFacadeOfChild(int bucket) {
        SyntaxNode childSyntaxNode = node.childInBucket(bucket);
        if (childSyntaxNode == null) {
            return null;
        }

        BLNode childNode = childSyntaxNode.createFacade(getChildPosition(bucket), this);
        this.childBuckets[bucket] = childNode;
        return childNode;
    }

    // TODO Find an efficient implementation which uses the previous children positions
    protected int getChildPosition(int bucket) {
        int childPos = this.position;
        for (int i = 0; i < bucket; i++) {
            SyntaxNode childNode = this.node.childInBucket(i);
            if (childNode != null) {
                childPos += childNode.width();
            }
        }

        return childPos;
    }

    // Create a BLNodeList for the child in the given bucket
    protected <T extends BLNode> BLNodeList<T> createListNode(int bucket) {
        BLNodeList<T> nodeList = new BLNodeList<T>(this.node.childInBucket(bucket),
                this.getChildPosition(bucket), this);
        this.childBuckets[bucket] = nodeList;
        return nodeList;
    }

    // Create a BLToken for the TextChange token in the given bucket
    protected BLSyntaxToken createToken(int bucket) {
        SyntaxNode internaltoken = this.node.childInBucket(bucket);
        if (internaltoken == null) {
            return null;
        }

        BLSyntaxToken token = new BLSyntaxToken(internaltoken, this.getChildPosition(bucket), this);
        this.childBuckets[bucket] = token;
        return token;
    }
}
