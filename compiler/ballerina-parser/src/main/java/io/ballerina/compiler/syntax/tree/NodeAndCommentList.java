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
import io.ballerina.compiler.internal.parser.tree.STNodeList;
import io.ballerina.compiler.internal.syntax.NodeListUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.ballerina.compiler.internal.syntax.NodeListUtils.rangeCheck;
import static io.ballerina.compiler.internal.syntax.NodeListUtils.rangeCheckForAdd;

/**
 * Represents a list of {@code Node}s.
 *
 * @param <T> the type of the constituent node instance
 *
 * @since 2201.10.0
 */
public class NodeAndCommentList<T extends NonTerminalNode> implements Iterable<T> {
    protected final STNodeList internalListNode;
    protected final NonTerminalNode nonTerminalNode;
    protected final Token semicolon;
    protected final int size;

    NodeAndCommentList(NonTerminalNode nonTerminalNode, Token semicolon) {
        this(nonTerminalNode, semicolon, nonTerminalNode.bucketCount() + 1);
    }

    protected NodeAndCommentList(NonTerminalNode nonTerminalNode, Token semicolon, int size) {
        if (!NodeListUtils.isSTNodeList(nonTerminalNode.internalNode())) {
            throw new IllegalArgumentException("An STNodeList instance is expected");
        }

        this.internalListNode = (STNodeList) nonTerminalNode.internalNode();
        this.nonTerminalNode = nonTerminalNode;
        this.semicolon = semicolon;
        this.size = size;
    }

    // Positional access methods

    public T get(int index) {
        rangeCheck(index, size);
        return this.nonTerminalNode.childInBucket(index);
    }

    // Modification methods

    public NodeAndCommentList<T> add(T node) {
        Objects.requireNonNull(node, "node should not be null");
        return new NodeAndCommentList<>(internalListNode.add(node.internalNode()).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T> add(int index, T node) {
        Objects.requireNonNull(node, "node should not be null");
        rangeCheckForAdd(index, size);
        return new NodeAndCommentList<>(internalListNode.add(index, node.internalNode()).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T> addAll(Collection<T> c) {
        if (c.isEmpty()) {
            return this;
        }

        List<STNode> stNodesToBeAdded = c.stream()
                .map(node -> Objects.requireNonNull(node, "node should not be null"))
                .map(Node::internalNode)
                .collect(Collectors.toList());
        return new NodeAndCommentList<>(internalListNode.addAll(stNodesToBeAdded).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T> set(int index, T node) {
        Objects.requireNonNull(node, "node should not be null");
        rangeCheck(index, size);
        if (nonTerminalNode.checkForReferenceEquality(index, node)) {
            return this;
        }

        return new NodeAndCommentList<>(internalListNode.set(index, node.internalNode()).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T> remove(int index) {
        rangeCheck(index, size);
        return new NodeAndCommentList<>(internalListNode.remove(index).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T> remove(T node) {
        Objects.requireNonNull(node, "node should not be null");
        for (int bucket = 0; bucket < nonTerminalNode.bucketCount(); bucket++) {
            if (nonTerminalNode.checkForReferenceEquality(bucket, node)) {
                return remove(bucket);
            }
        }
        return this;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public NodeAndCommentList<T> removeAll(Collection<T> c) {
        if (c.isEmpty()) {
            return this;
        }
        c.forEach(node -> Objects.requireNonNull(node, "node should not be null"));

        List<STNode> toBeDeletedList = new ArrayList<>();
        for (int bucket = 0; bucket < nonTerminalNode.bucketCount(); bucket++) {
            Node childNode = nonTerminalNode.childBuckets[bucket];
            if (c.contains(childNode)) {
                toBeDeletedList.add(childNode.internalNode());
            }
        }

        return new NodeAndCommentList<>(internalListNode.removeAll(toBeDeletedList).createUnlinkedFacade(), null);
    }

    //query methods

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new NodeAndCommentListIterator();
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    NonTerminalNode underlyingListNode() {
        return this.nonTerminalNode;
    }

    /**
     * An iterator for this list of nodes.
     *
     * @since 2201.10.0
     */
    protected class NodeAndCommentListIterator implements Iterator<T> {
        private int currentIndex = 0;
        private T currentNode = null;

        @Override
        public boolean hasNext() {
            return this.currentIndex < size;
        }

        @Override
        public T next() {
            if (currentNode != null) {
                currentIndex++;
                T temp = currentNode;
                currentNode = null;
                return temp;
            }
            if (currentIndex == size - 1) {
                currentIndex++;
                return (T) new CommentNode(semicolon.internalNode(), 0, null);
            }
            currentNode = get(currentIndex);
            return (T) new CommentNode(currentNode.internalNode(), 0, currentNode);
        }
    }

}
