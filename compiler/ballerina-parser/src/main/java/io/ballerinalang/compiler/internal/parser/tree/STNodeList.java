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

import io.ballerinalang.compiler.internal.syntax.ExternalTreeNodeList;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Arrays;
import java.util.Collection;

import static io.ballerinalang.compiler.internal.syntax.NodeListUtils.rangeCheck;
import static io.ballerinalang.compiler.internal.syntax.NodeListUtils.rangeCheckForAdd;

/**
 * Represents a list of {@code STNode} instances.
 *
 * @since 2.0.0
 */
public final class STNodeList extends STNode {

    STNodeList(STNode... nodes) {
        super(SyntaxKind.LIST);
        this.addChildren(nodes);
    }

    STNodeList(Collection<STNode> nodes) {
        this(nodes.toArray(new STNode[0]));
    }

    // Positional access methods
    public STNode get(int index) {
        rangeCheck(index, bucketCount);
        return childBuckets[index];
    }

    // Modification Operations

    public STNodeList add(STNode node) {
        int newLength = bucketCount + 1;
        STNode[] newNodesArray = Arrays.copyOf(childBuckets, newLength);
        newNodesArray[bucketCount] = node;
        return new STNodeList(newNodesArray);
    }

    public STNodeList add(int index, STNode node) {
        rangeCheckForAdd(index, bucketCount);
        STNode[] newNodesArray = Arrays.copyOf(childBuckets, bucketCount + 1);
        System.arraycopy(newNodesArray, index, newNodesArray, index + 1, bucketCount - index);
        newNodesArray[index] = node;
        return new STNodeList(newNodesArray);
    }

    public STNodeList addAll(Collection<STNode> c) {
        int newLength = bucketCount + c.size();
        STNode[] newNodesArray = Arrays.copyOf(childBuckets, newLength);
        int index = bucketCount;
        for (STNode stNode : c) {
            newNodesArray[index++] = stNode;
        }
        return new STNodeList(newNodesArray);
    }

    public STNodeList set(int index, STNode node) {
        rangeCheck(index, bucketCount);
        STNode[] newNodesArray = Arrays.copyOf(childBuckets, bucketCount);
        newNodesArray[index] = node;
        return new STNodeList(newNodesArray);
    }

    public STNodeList remove(int index) {
        rangeCheck(index, bucketCount);
        boolean lastElement = (index + 1) == bucketCount;
        STNode[] newNodesArray = Arrays.copyOf(childBuckets, bucketCount - 1);
        if (lastElement) {
            return new STNodeList(newNodesArray);
        }

        System.arraycopy(childBuckets, index + 1, newNodesArray, index, bucketCount - index - 1);
        return new STNodeList(newNodesArray);
    }

    public STNodeList remove(STNode node) {
        if (node == null) {
            return removeFirstNullValue();
        }

        for (int bucket = 0; bucket < bucketCount; bucket++) {
            if (node.equals(childBuckets[bucket])) {
                return remove(bucket);
            }
        }
        return this;
    }

    public STNodeList removeAll(Collection<STNode> c) {
        int index = 0;
        STNode[] newNodesArray = Arrays.copyOf(childBuckets, bucketCount);
        for (int bucket = 0; bucket < bucketCount; bucket++) {
            STNode currentNode = childBuckets[bucket];
            if (!c.contains(currentNode)) {
                newNodesArray[index++] = currentNode;
            }
        }

        return new STNodeList(Arrays.copyOf(newNodesArray, index));
    }

    //query methods

    public int size() {
        return bucketCount;
    }

    public boolean isEmpty() {
        return bucketCount == 0;
    }

    @Override
    public NonTerminalNode createFacade(int position, NonTerminalNode parent) {
        return new ExternalTreeNodeList(this, position, parent);
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }

    private STNodeList removeFirstNullValue() {
        for (int bucket = 0; bucket < bucketCount; bucket++) {
            if (childBuckets[bucket] == null) {
                return remove(bucket);
            }
        }
        return this;
    }
}
