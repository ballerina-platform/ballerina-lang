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

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxUtils;

/**
 * Represents a node with children in the syntax tree.
 */
// TODO This class can be used by multiple threads. Since the tree is lazily constructed,
//  we need to ensure only one tree is created.
// TODO find a better name for this class.
public abstract class NonTerminalNode extends Node {

    // The following two fields allow us to navigate the tree without the knowledge of the particular tree nodes
    protected final Node[] childBuckets;
    protected final int bucketCount;

    public NonTerminalNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);

        this.bucketCount = node.bucketCount();
        this.childBuckets = new Node[this.bucketCount];
    }

    public int bucketCount() {
        return bucketCount;
    }

    public Node childInBucket(int bucket) {
        if (bucket < 0 || bucket >= bucketCount) {
            return null;
        }

        return childBuckets[bucket];
    }

    protected Node getFacadeOfChild(int bucket) {
        STNode childSyntaxNode = node.childInBucket(bucket);
        if (childSyntaxNode == null) {
            return null;
        }

        Node childNode = childSyntaxNode.createFacade(getChildPosition(bucket), this);
        this.childBuckets[bucket] = childNode;
        return childNode;
    }

    // TODO Find an efficient implementation which uses the previous children positions
    protected int getChildPosition(int bucket) {
        int childPos = this.position;
        for (int i = 0; i < bucket; i++) {
            STNode childNode = this.node.childInBucket(i);
            if (childNode != null) {
                childPos += childNode.width();
            }
        }

        return childPos;
    }

    // Create a NodeList for the child in the given bucket
    protected <T extends Node> NodeList<T> createListNode(int bucket) {
        NodeList<T> nodeList = new NodeList<>(this.node.childInBucket(bucket),
                this.getChildPosition(bucket), this);
        this.childBuckets[bucket] = nodeList;
        return nodeList;
    }

    // Create a Token for the TextChange token in the given bucket
    protected Token createToken(int bucket) {
        STNode internaltoken = this.node.childInBucket(bucket);
        if (internaltoken == null) {
            return null;
        }

        Token token = new Token(internaltoken, this.getChildPosition(bucket), this);
        this.childBuckets[bucket] = token;
        return token;
    }

    // TODO Can we optimize this algo?
    public Token findToken(int position) {
        if (!spanWithMinutiae.contains(position)) {
            // TODO Fix with a proper error message
            throw new IllegalArgumentException();
        }

        Node foundNode = this;
        while (!SyntaxUtils.isToken(foundNode)) {
            foundNode = ((NonTerminalNode) foundNode).findChildNode(position);
        }

        return (Token) foundNode;
    }

    private Node findChildNode(int position) {
        int offset = this.spanWithMinutiae.startOffset();
        STNode internalNode = this.node;
        for (int bucket = 0; bucket < internalNode.bucketCount(); bucket++) {
            STNode internalChildNode = internalNode.childInBucket(bucket);
            if (position < offset + internalChildNode.width()) {
                // Populate the BL node.
                return this.childInBucket(bucket);
            }
            offset += internalChildNode.width();
        }

        // TODO It is impossible to reach this line
        // TODO Can we rewrite this logic
        throw new IllegalStateException();
    }
}
