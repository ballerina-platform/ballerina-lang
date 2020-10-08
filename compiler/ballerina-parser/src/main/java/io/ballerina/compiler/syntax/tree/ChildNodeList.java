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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.syntax.SyntaxUtils;

import java.util.Iterator;

/**
 * A list of child nodes of a non-terminal node in the syntax tree.
 *
 * @since 1.3.0
 */
public class ChildNodeList implements Iterable<Node> {

    private final NonTerminalNode parent;
    private final int size;
    private final Node[] childNodes;

    ChildNodeList(NonTerminalNode parent) {
        this.parent = parent;
        this.size = getChildCount(parent.internalNode());
        this.childNodes = new Node[this.size];
    }

    public Node get(int childIndex) {
        rangeCheck(childIndex);
        Node child = childNodes[childIndex];
        if (child != null) {
            return child;
        }
        return loadNode(childIndex);
    }

    public int size() {
        return this.size;
    }

    @Override
    public Iterator<Node> iterator() {
        return new ChildNodeIterator(this.size);
    }

    private int getChildCount(STNode parent) {
        int count = 0;
        for (int bucket = 0; bucket < parent.bucketCount(); bucket++) {
            STNode child = parent.childInBucket(bucket);
            if (!SyntaxUtils.isSTNodePresent(child)) {
                continue;
            }
            if (child.kind == SyntaxKind.LIST) {
                count += child.bucketCount();
            } else {
                count++;
            }
        }
        return count;
    }

    private Node loadNode(int childIndex) {
        int index = 0;
        Node child = null;
        for (int bucket = 0; bucket < parent.bucketCount(); bucket++) {
            STNode internalChild = parent.internalNode.childInBucket(bucket);
            if (!SyntaxUtils.isSTNodePresent(internalChild)) {
                continue;
            }
            if (internalChild.kind == SyntaxKind.LIST) {
                if (childIndex < index + internalChild.bucketCount()) {
                    int listChildIndex = childIndex - index;
                    NonTerminalNode listChild = parent.childInBucket(bucket);
                    child = listChild.childInBucket(listChildIndex);
                    childNodes[childIndex] = child;
                    break;
                }
                index += internalChild.bucketCount();
            } else {
                if (index == childIndex) {
                    child = parent.childInBucket(bucket);
                    childNodes[childIndex] = child;
                    break;
                }
                index++;
            }
        }
        return child;
    }

    private void rangeCheck(int childIndex) {
        if (childIndex >= size || childIndex < 0) {
            throw new IndexOutOfBoundsException("Index: '" + childIndex + "', Size: '" + size + "'");
        }
    }

    /**
     * A child node iterator implementation.
     *
     * @since 1.3.0
     */
    private class ChildNodeIterator implements Iterator<Node> {
        private final int childCount;
        private int currentChildIndex = 0;

        ChildNodeIterator(int childCount) {
            this.childCount = childCount;
        }

        @Override
        public boolean hasNext() {
            return currentChildIndex < childCount;
        }

        @Override
        public Node next() {
            return get(currentChildIndex++);
        }
    }
}
