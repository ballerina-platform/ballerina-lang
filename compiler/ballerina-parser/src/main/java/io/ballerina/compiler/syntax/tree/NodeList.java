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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.ballerina.compiler.internal.syntax.NodeListUtils.rangeCheck;
import static io.ballerina.compiler.internal.syntax.NodeListUtils.rangeCheckForAdd;

/**
 * Represents a list of {@code Node}s.
 *
 * @param <T> the type of the constituent node instance
 */
public class NodeList<T extends Node> implements Iterable<T> {
    protected final STNodeList internalListNode;
    protected final NonTerminalNode nonTerminalNode;
    protected final int size;

    NodeList(NonTerminalNode nonTerminalNode) {
        this(nonTerminalNode, nonTerminalNode.bucketCount());
    }

    protected NodeList(NonTerminalNode nonTerminalNode, int size) {
        if (!NodeListUtils.isSTNodeList(nonTerminalNode.internalNode())) {
            throw new IllegalArgumentException("An STNodeList instance is expected");
        }

        this.internalListNode = (STNodeList) nonTerminalNode.internalNode();
        this.nonTerminalNode = nonTerminalNode;
        this.size = size;
    }

    // Positional access methods

    public T get(int index) {
        rangeCheck(index, size);
        return this.nonTerminalNode.childInBucket(index);
    }

    // Modification methods

    public NodeList<T> add(T node) {
        Objects.requireNonNull(node, "node should not be null");
        return new NodeList<>(internalListNode.add(node.internalNode()).createUnlinkedFacade());
    }

    public NodeList<T> add(int index, T node) {
        Objects.requireNonNull(node, "node should not be null");
        rangeCheckForAdd(index, size);
        return new NodeList<>(internalListNode.add(index, node.internalNode()).createUnlinkedFacade());
    }

    public NodeList<T> addAll(Collection<T> c) {
        if (c.isEmpty()) {
            return this;
        }

        List<STNode> stNodesToBeAdded = c.stream()
                .map(node -> Objects.requireNonNull(node, "node should not be null"))
                .map(Node::internalNode)
                .collect(Collectors.toList());
        return new NodeList<>(internalListNode.addAll(stNodesToBeAdded).createUnlinkedFacade());
    }

    public NodeList<T> set(int index, T node) {
        Objects.requireNonNull(node, "node should not be null");
        rangeCheck(index, size);
        if (nonTerminalNode.checkForReferenceEquality(index, node)) {
            return this;
        }

        return new NodeList<>(internalListNode.set(index, node.internalNode()).createUnlinkedFacade());
    }

    public NodeList<T> remove(int index) {
        rangeCheck(index, size);
        return new NodeList<>(internalListNode.remove(index).createUnlinkedFacade());
    }

    public NodeList<T> remove(T node) {
        Objects.requireNonNull(node, "node should not be null");
        for (int bucket = 0; bucket < nonTerminalNode.bucketCount(); bucket++) {
            if (nonTerminalNode.checkForReferenceEquality(bucket, node)) {
                return remove(bucket);
            }
        }
        return this;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public NodeList<T> removeAll(Collection<T> c) {
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

        return new NodeList<>(internalListNode.removeAll(toBeDeletedList).createUnlinkedFacade());
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
        return new NodeListIterator();
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
     * @since 1.3.0
     */
    protected class NodeListIterator implements Iterator<T> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return this.currentIndex < size;
        }

        @Override
        public T next() {
            return get(currentIndex++);
        }
    }
}
