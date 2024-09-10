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
 * Represent both nodes and attached comments to each node.
 *
 * @param <T> The type of NonTerminalNode
 * @param <K> The type of Node
 */
public class NodeAndCommentList<T extends NonTerminalNode, K extends Node> implements Iterable<K> {
    protected final STNodeList internalListNode;
    protected final NonTerminalNode nonTerminalNode;
    protected final int size;
    protected final Node[] nodes;

    NodeAndCommentList(NonTerminalNode nonTerminalNode, Token semicolon) {
        this(nonTerminalNode, semicolon, nonTerminalNode.bucketCount() * 2 + 1);
    }

    protected NodeAndCommentList(NonTerminalNode nonTerminalNode, Token semicolon, int size) {
        if (!NodeListUtils.isSTNodeList(nonTerminalNode.internalNode())) {
            throw new IllegalArgumentException("An STNodeList instance is expected");
        }

        this.internalListNode = (STNodeList) nonTerminalNode.internalNode();
        this.nonTerminalNode = nonTerminalNode;
        this.nodes = new Node[size]; // TODO: Init with max size
        int nodeIndex = 0;
        for (int i = 0; i < nonTerminalNode.bucketCount(); i++) {
            Node node = nonTerminalNode.childInBucket(i);
            CommentNode commentNode = getCommentNode(node);
            if (commentNode != null) {
                this.nodes[nodeIndex++] = commentNode;
            }
            this.nodes[nodeIndex++] = node;
        }

        CommentNode commentNodeBeforeEnd = getCommentNode(semicolon);
        if (commentNodeBeforeEnd != null) {
            this.nodes[nodeIndex++] = commentNodeBeforeEnd;
        }
        this.size = nodeIndex;
    }

    private CommentNode getCommentNode(Node node) {
        List<String> commentLines = new ArrayList<>();
        Minutiae lastMinutiae = null;
        for (Minutiae minutiae : node.leadingMinutiae()) {
            String[] splits = minutiae.text().split("// ");
            if (splits.length >= 2) {
                commentLines.add(splits[1]);
                lastMinutiae = minutiae;
            } else if (splits.length == 1 && splits[0].contains("//")) {
                commentLines.add("");
                lastMinutiae = minutiae;
            }
        }
        if (commentLines.isEmpty()) {
            return null;
        }
        CommentNode commentNode = new CommentNode(node.internalNode(), 0, null);
        commentNode.setCommentAttachedNode(node);
        commentNode.setLastMinutiae(lastMinutiae);
        commentNode.setCommentLines(commentLines);
        return commentNode;
    }

    // Positional access methods

    public K get(int index) {
        rangeCheck(index, size);
        return (K) this.nodes[index];
    }

    // Modification methods

    public NodeAndCommentList<T, K> add(T node) {
        Objects.requireNonNull(node, "node should not be null");
        return new NodeAndCommentList<>(internalListNode.add(node.internalNode()).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T, K> add(int index, T node) {
        Objects.requireNonNull(node, "node should not be null");
        rangeCheckForAdd(index, size);
        return new NodeAndCommentList<>(internalListNode.add(index, node.internalNode()).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T, K> addAll(Collection<T> c) {
        if (c.isEmpty()) {
            return this;
        }

        List<STNode> stNodesToBeAdded = c.stream()
                .map(node -> Objects.requireNonNull(node, "node should not be null"))
                .map(Node::internalNode)
                .collect(Collectors.toList());
        return new NodeAndCommentList<>(internalListNode.addAll(stNodesToBeAdded).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T, K> set(int index, T node) {
        Objects.requireNonNull(node, "node should not be null");
        rangeCheck(index, size);
        if (nonTerminalNode.checkForReferenceEquality(index, node)) {
            return this;
        }

        return new NodeAndCommentList<>(internalListNode.set(index, node.internalNode()).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T, K> remove(int index) {
        rangeCheck(index, size);
        return new NodeAndCommentList<>(internalListNode.remove(index).createUnlinkedFacade(), null);
    }

    public NodeAndCommentList<T, K> remove(T node) {
        Objects.requireNonNull(node, "node should not be null");
        for (int bucket = 0; bucket < nonTerminalNode.bucketCount(); bucket++) {
            if (nonTerminalNode.checkForReferenceEquality(bucket, node)) {
                return remove(bucket);
            }
        }
        return this;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public NodeAndCommentList<T, K> removeAll(Collection<T> c) {
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
    public Iterator<K> iterator() {
        return new NodeAndCommentListIterator();
    }

    public Stream<K> stream() {
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
    protected class NodeAndCommentListIterator implements Iterator<K> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return this.currentIndex < size;
        }

        @Override
        public K next() {
            return get(currentIndex++);
        }
    }
}
