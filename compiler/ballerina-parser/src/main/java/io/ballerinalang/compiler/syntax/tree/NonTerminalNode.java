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
 *
 * @since 1.3.0
 */
// TODO This class can be used by multiple threads. Since the tree is lazily constructed,
//  we need to ensure only one tree is created.
// TODO find a better name for this class.
public abstract class NonTerminalNode extends Node {

    // The following two fields allow us to navigate the tree without the knowledge of the particular tree nodes
    protected final Node[] childBuckets;
    private ChildNodeList childNodeList;

    public NonTerminalNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
        this.childBuckets = new Node[internalNode.bucketCount()];
    }

    // TODO Can we do ChildNodeList<T>
    // We can simply give a List here, but that would be not useful.
    public ChildNodeList children() {
        if (childNodeList != null) {
            return childNodeList;
        }
        childNodeList = new ChildNodeList(this);
        return childNodeList;
    }

    protected int bucketCount() {
        return internalNode.bucketCount();
    }

    protected <T extends Node> T childInBucket(int bucket) {
        T child = (T) childBuckets[bucket];
        if (child != null) {
            return child;
        }

        STNode internalChild = internalNode.childInBucket(bucket);
        child = (T) internalChild.createFacade(getChildPosition(bucket), this);
        childBuckets[bucket] = child;
        return child;
    }

    // TODO Find an efficient implementation which uses the previous children positions
    protected int getChildPosition(int bucket) {
        int childPos = this.position;
        for (int i = 0; i < bucket; i++) {
            STNode childNode = internalNode.childInBucket(i);
            if (childNode != null) {
                childPos += childNode.width();
            }
        }

        return childPos;
    }

    protected boolean checkForReferenceEquality(Node... children) {
        for (int bucket = 0; bucket < children.length; bucket++) {
            // Here we are using the childBuckets arrays instead of the childInBucket() method.
            // If the particular child is not loaded, then childBuckets[bucket] will be null.
            // That means the given child is not equal to what is stored in the childBuckets array.
            if (children[bucket] != childBuckets[bucket]) {
                return false;
            }
        }
        return true;
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
        for (int bucket = 0; bucket < internalNode.bucketCount(); bucket++) {
            STNode internalChildNode = internalNode.childInBucket(bucket);
            if (position < offset + internalChildNode.width()) {
                // Populate the external node.
                return this.childInBucket(bucket);
            }
            offset += internalChildNode.width();
        }

        // TODO It is impossible to reach this line
        // TODO Can we rewrite this logic
        throw new IllegalStateException();
    }
}
