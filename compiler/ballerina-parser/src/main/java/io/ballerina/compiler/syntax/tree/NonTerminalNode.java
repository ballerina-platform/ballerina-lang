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
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.syntax.SyntaxUtils;
import io.ballerina.compiler.internal.syntax.TreeModifiers;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.ballerina.compiler.internal.syntax.SyntaxUtils.isSTNodePresent;

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
        return this.internalNode.firstToken().createUnlinkedFacade().leadingMinutiae();
    }

    public MinutiaeList trailingMinutiae() {
        return this.internalNode.lastToken().createUnlinkedFacade().trailingMinutiae();
    }

    // TODO Find an efficient implementation which uses the previous children positions
    // TODO Can we optimize this algo?
    public Token findToken(int position) {
        TextRange textRangeWithMinutiae = textRangeWithMinutiae();
        // Check whether this position is the same as the end text position of the text.
        // If that is the case, return the eof token.
        // Fixes 24905
        if (textRangeWithMinutiae.endOffset() == position &&
                this instanceof ModulePartNode) {
            ModulePartNode modulePartNode = (ModulePartNode) this;
            return modulePartNode.eofToken();
        }

        if (!textRangeWithMinutiae.contains(position)) {
            throw new IndexOutOfBoundsException("Index: '" + position +
                    "', Size: '" + textRangeWithMinutiae.endOffset() + "'");
        }

        Node foundNode = this;
        while (!SyntaxUtils.isToken(foundNode)) {
            foundNode = ((NonTerminalNode) foundNode).findChildNode(position);
        }

        return (Token) foundNode;
    }

    public Token findToken(int position, boolean insideMinutiae) {
        Token token = findToken(position);
        if (!insideMinutiae) {
            return token;
        }

        Optional<Token> tokenInsideMinutiae = Optional.empty();
        if (positionWithinLeadingMinutiae(position, token)) {
            tokenInsideMinutiae = getInvalidNodeMinutiae(token.leadingMinutiae(), position);
        } else if (positionWithinTrailingMinutiae(position, token)) {
            tokenInsideMinutiae = getInvalidNodeMinutiae(token.trailingMinutiae(), position);
        }

        return tokenInsideMinutiae.orElse(token);
    }

    /**
     * Find the inner most node encapsulating the a text range.
     * Note: When evaluating the position of a node to check the range this will not include the start offset while
     * excluding the end offset
     *
     * @param textRange to evaluate and find the node
     * @return {@link NonTerminalNode} which is the inner most non terminal node, encapsulating the given position
     */
    public NonTerminalNode findNode(TextRange textRange) { 
        return findNode(textRange, false);
    }

    /**
     * Find the inner most node encapsulating the a text range.
     *
     * @param textRange          text range to evaluate
     * @param includeStartOffset whether to include start offset when checking text range
     * @return Innermost {@link NonTerminalNode} encapsulation given text range
     */
    public NonTerminalNode findNode(TextRange textRange, boolean includeStartOffset) {
        TextRange textRangeWithMinutiae = textRangeWithMinutiae();
        if (!(this instanceof ModulePartNode)
                && (!textRangeWithMinutiae.contains(textRange.startOffset())
                || !textRangeWithMinutiae.contains(textRange.endOffset()))) {
            throw new IllegalStateException("Invalid Text Range for: " + textRange.toString());
        }

        NonTerminalNode foundNode = null;
        Optional<Node> temp = Optional.of(this);
        while (temp.isPresent() && SyntaxUtils.isNonTerminalNode(temp.get())) {
            foundNode = (NonTerminalNode) temp.get();
            temp = ((NonTerminalNode) temp.get()).findChildNode(textRange, includeStartOffset);
        }

        return foundNode;
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

    /**
     * Find a child node enclosing the given text range.
     * If there is no child node which can wrap the given range, this method will return empty
     *
     * @param textRange          text range to evaluate
     * @param includeStartOffset whether to include start offset when checking textRange
     * @return {@link Optional} node found, which is enclosing the given range
     */
    private Optional<Node> findChildNode(TextRange textRange, boolean includeStartOffset) {
        int offset = textRangeWithMinutiae().startOffset();
        for (int bucket = 0; bucket < internalNode.bucketCount(); bucket++) {
            STNode internalChildNode = internalNode.childInBucket(bucket);
            if (!isSTNodePresent(internalChildNode)) {
                continue;
            }
            int offsetWithMinutiae = offset + internalChildNode.widthWithMinutiae();

            if (includeStartOffset) {
                if (textRange.startOffset() >= offset && textRange.endOffset() <= offsetWithMinutiae) {
                    // Populate the external node.
                    return Optional.ofNullable(this.childInBucket(bucket));
                }
            } else {
                if (textRange.startOffset() > offset && textRange.endOffset() <= offsetWithMinutiae) {
                    // Populate the external node.
                    return Optional.ofNullable(this.childInBucket(bucket));
                }
            }

            offset += internalChildNode.widthWithMinutiae();
        }

        return Optional.empty();
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

        for (STNodeDiagnostic stNodeDiagnostic : internalNode.diagnostics()) {
            Diagnostic syntaxDiagnostic = createSyntaxDiagnostic(stNodeDiagnostic);
            diagnosticList.add(syntaxDiagnostic);
        }
    }

    private boolean positionWithinLeadingMinutiae(int position, Token token) {
        return token.containsLeadingMinutiae() && position < token.textRange().startOffset();
    }

    private boolean positionWithinTrailingMinutiae(int position, Token token) {
        return token.containsTrailingMinutiae() && token.textRange().endOffset() <= position;
    }

    private Optional<Token> getInvalidNodeMinutiae(MinutiaeList minutiaeList, int position) {
        for (Minutiae minutiae : minutiaeList) {
            if (minutiae.textRange().contains(position) && minutiae.isInvalidNodeMinutiae()) {
                return Optional.of(minutiae.invalidTokenMinutiaeNode().get().invalidToken());
            }
        }
        return Optional.empty();
    }
}
