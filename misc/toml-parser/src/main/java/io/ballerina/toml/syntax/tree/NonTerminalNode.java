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
package io.ballerina.toml.syntax.tree;

import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.syntax.SyntaxUtils;
import io.ballerina.toml.internal.syntax.TreeModifiers;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.ballerina.toml.internal.syntax.SyntaxUtils.isSTNodePresent;

/**
 * Represents a node with children in the syntax tree.
 *
 * @since 1.3.0
 */
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

    /**
     * Returns a collection of name and node pairs of the children of this node.
     *
     * @return a collection of {@code ChildNodeEntry}
     */
    public Collection<ChildNodeEntry> childEntries() {
        String[] childNames = childNames();
        return Collections.unmodifiableCollection(
                IntStream.range(0, bucketCount())
                        .filter(bucket -> childInBucket(bucket) != null)
                        .mapToObj(bucket -> new ChildNodeEntry(childNames[bucket], childInBucket(bucket)))
                        .collect(Collectors.toList()));
    }

    public MinutiaeList leadingMinutiae() {
        throw new UnsupportedOperationException("This method is not yet implemented. Please check again later.");
    }

    public MinutiaeList trailingMinutiae() {
        throw new UnsupportedOperationException("This method is not yet implemented. Please check again later.");
    }

    // TODO Find an efficient implementation which uses the previous children positions
    // TODO Can we optimize this algo?
    public Token findToken(int position) {
        if (!textRangeWithMinutiae().contains(position)) {
            // TODO Fix with a proper error message
            throw new IllegalArgumentException();
        }

        Node foundNode = this;
        while (!SyntaxUtils.isToken(foundNode)) {
            foundNode = ((NonTerminalNode) foundNode).findChildNode(position);
        }

        return (Token) foundNode;
    }

    // Node modification operations

    /**
     * Replaces the given target node with the replacement.
     *
     * @param target      the node to be replaced
     * @param replacement the replacement node
     * @param <T>         the type of the root node
     * @return return the new root node after replacing the target with the replacement
     */
    public <T extends NonTerminalNode> T replace(Node target, Node replacement) {
        return TreeModifiers.replace((T) this, target, replacement);
    }

    public Iterable<Diagnostic> diagnostics() {
        if (!internalNode.hasDiagnostics()) {
            return Collections::emptyIterator;
        }

        return () -> collectDiagnostics().iterator();
    }

    protected abstract String[] childNames();

    protected int bucketCount() {
        return internalNode.bucketCount();
    }

    protected <T extends Node> T childInBucket(int bucket) {
        T child = (T) childBuckets[bucket];
        if (child != null) {
            return child;
        }

        STNode internalChild = internalNode.childInBucket(bucket);
        if (isSTNodePresent(internalChild)) {
            child = (T) internalChild.createFacade(getChildPosition(bucket), this);
            childBuckets[bucket] = child;
        }
        return child;
    }

    protected <T extends Node> Optional<T> optionalChildInBucket(int bucket) {
        return Optional.ofNullable(childInBucket(bucket));
    }

    protected int getChildPosition(int bucket) {
        int childPos = this.position;
        for (int i = 0; i < bucket; i++) {
            STNode childNode = internalNode.childInBucket(i);
            if (childNode != null) {
                childPos += childNode.widthWithMinutiae();
            }
        }

        return childPos;
    }

    protected boolean checkForReferenceEquality(Node... children) {
        for (int bucket = 0; bucket < children.length; bucket++) {
            if (!checkForReferenceEquality(bucket, children[bucket])) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkForReferenceEquality(int index, Node child) {
        // Here we are using the childBuckets arrays instead of the childInBucket() method.
        // If the particular child is not loaded, then childBuckets[bucket] will be null.
        // That means the given child is not equal to what is stored in the childBuckets array.
        return childBuckets[index] == child;
    }

    private Node findChildNode(int position) {
        int offset = textRangeWithMinutiae().startOffset();
        for (int bucket = 0; bucket < internalNode.bucketCount(); bucket++) {
            STNode internalChildNode = internalNode.childInBucket(bucket);
            if (!isSTNodePresent(internalChildNode)) {
                continue;
            }
            if (position < offset + internalChildNode.widthWithMinutiae()) {
                // Populate the external node.
                return this.childInBucket(bucket);
            }
            offset += internalChildNode.widthWithMinutiae();
        }

        // TODO It is impossible to reach this line
        // TODO Can we rewrite this logic
        throw new IllegalStateException();
    }

    private List<Diagnostic> collectDiagnostics() {
        List<Diagnostic> diagnosticList = new ArrayList<>();
        collectDiagnostics(this, diagnosticList);
        return diagnosticList;
    }

    // TODO this is a very inefficient implementation. We need to fix this ASAP.
    // TODO this implementation builds the complete public tree
    // TODO find a way to traverse the internal tree to build the diagnostics information
    private void collectDiagnostics(NonTerminalNode node, List<Diagnostic> diagnosticList) {
        for (Node child : node.children()) {
            child.diagnostics().forEach(diagnosticList::add);
        }

        internalNode.diagnostics().stream()
                .map(this::createSyntaxDiagnostic)
                .forEach(diagnosticList::add);
    }
}
