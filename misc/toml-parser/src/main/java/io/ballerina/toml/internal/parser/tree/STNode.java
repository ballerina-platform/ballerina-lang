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
package io.ballerina.toml.internal.parser.tree;

import io.ballerina.toml.internal.syntax.NodeListUtils;
import io.ballerina.toml.internal.syntax.SyntaxUtils;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * {@code STNode} is the base class for all tree nodes in the internal syntax tree.
 *
 * @since 1.3.0
 */
public abstract class STNode {
    public final SyntaxKind kind;
    protected final Collection<STNodeDiagnostic> diagnostics;
    protected int width;
    protected int widthWithLeadingMinutiae;
    protected int widthWithTrailingMinutiae;
    protected int widthWithMinutiae;

    protected EnumSet<STNodeFlags> flags = EnumSet.noneOf(STNodeFlags.class);

    protected static final STNode[] EMPTY_BUCKET = new STNode[0];
    // The following fields allow us to navigate the tree without the knowledge of the particular tree nodes
    protected int bucketCount;
    protected STNode[] childBuckets = EMPTY_BUCKET;

    STNode(SyntaxKind kind) {
        this.kind = kind;
        this.diagnostics = Collections.emptyList();
    }

    STNode(SyntaxKind kind, Collection<STNodeDiagnostic> diagnostics) {
        this.kind = kind;
        this.diagnostics = diagnostics;
        if (diagnostics.size() > 0) {
            flags.add(STNodeFlags.HAS_DIAGNOSTICS);
        }
    }

    public STNode childInBucket(int bucket) {
        return childBuckets[bucket];
    }

    public int widthWithMinutiae() {
        return widthWithMinutiae;
    }

    public int width() {
        return width;
    }

    public int widthWithLeadingMinutiae() {
        return widthWithLeadingMinutiae;
    }

    public int widthWithTrailingMinutiae() {
        return widthWithTrailingMinutiae;
    }

    public STNode leadingMinutiae() {
        throw new UnsupportedOperationException("" +
                "The leadingMinutiae() method is only supported for STToken instances");
    }

    public STNode trailingMinutiae() {
        throw new UnsupportedOperationException("" +
                "The trailingMinutiae() method is only supported for STToken instances");
    }

    public boolean hasDiagnostics() {
        return flags.contains(STNodeFlags.HAS_DIAGNOSTICS);
    }

    public Collection<STNodeDiagnostic> diagnostics() {
        return Collections.unmodifiableCollection(this.diagnostics);
    }

    public int bucketCount() {
        return bucketCount;
    }

    public boolean isMissing() {
        return this instanceof STMissingToken;
    }

    public List<STToken> tokens() {
        List<STToken> tokens = new ArrayList<>();
        tokensInternal(tokens);
        return Collections.unmodifiableList(tokens);
    }

    protected void tokensInternal(List<STToken> tokens) {
        for (STNode child : childBuckets) {
            if (SyntaxUtils.isSTNodePresent(child)) {
                child.tokensInternal(tokens);
            }
        }
    }

    public STToken firstToken() {
        return (STToken) firstTokenInternal();
    }

    protected STNode firstTokenInternal() {
        for (STNode child : childBuckets) {
            if (SyntaxUtils.isToken(child)) {
                return child;
            }

            if (!SyntaxUtils.isSTNodePresent(child) ||
                    NodeListUtils.isSTNodeList(child) && child.bucketCount == 0) {
                continue;
            }

            // Some nodes have non-empty child nodes that contain empty STNodeList child. e.g. STMetadata
            STNode firstToken = child.firstTokenInternal();
            if (SyntaxUtils.isSTNodePresent(firstToken)) {
                return firstToken;
            }
        }
        return null;
    }

    public STToken lastToken() {
        return (STToken) lastTokenInternal();
    }

    protected STNode lastTokenInternal() {
        for (int bucket = childBuckets.length - 1; bucket >= 0; bucket--) {
            STNode child = childInBucket(bucket);
            if (SyntaxUtils.isToken(child)) {
                return child;
            }

            if (!SyntaxUtils.isSTNodePresent(child) ||
                    NodeListUtils.isSTNodeList(child) && child.bucketCount == 0) {
                continue;
            }

            // Some nodes have non-empty child nodes that contain empty STNodeList child. e.g. STMetadata
            STNode lastToken = child.lastTokenInternal();
            if (SyntaxUtils.isSTNodePresent(lastToken)) {
                return lastToken;
            }
        }
        return null;
    }

    // Modification methods

    public abstract STNode modifyWith(Collection<STNodeDiagnostic> diagnostics);

    /**
     * Replaces the given target node with the replacement.
     *
     * @param target      the node to be replaced
     * @param replacement the replacement node
     * @param <T>         the type of the root node
     * @return return the new root node after replacing the target with the replacement
     */
    public <T extends STNode> T replace(STNode target, STNode replacement) {
        return STTreeModifiers.replace((T) this, target, replacement);
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> T createUnlinkedFacade() {
        return (T) createFacade(0, null);
    }

    public abstract Node createFacade(int position, NonTerminalNode parent);

    /**
     * Accepts an instance of the {@code STNodeVisitor}, which can be used to
     * traverse the syntax tree.
     *
     * @param visitor an instance of the {@code STNodeVisitor}
     */
    public abstract void accept(STNodeVisitor visitor);

    /**
     * Applies the given {@code STNodeTransformer} to this node and returns
     * the transformed object.
     *
     * @param transformer an instance of the {@code STNodeTransformer}
     * @param <T>         the type of transformed object
     * @return the transformed object
     */
    public abstract <T> T apply(STNodeTransformer<T> transformer);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (STNode child : this.childBuckets) {
            sb.append(child != null ? child.toString() : "");
        }
        return sb.toString();
    }

    public void writeTo(StringBuilder builder) {
        for (STNode child : this.childBuckets) {
            if (SyntaxUtils.isSTNodePresent(child)) {
                child.writeTo(builder);
            }
        }
    }

    public String toSourceCode() {
        StringBuilder builder = new StringBuilder();
        writeTo(builder);
        return builder.toString();
    }

    protected void addChildren(STNode... children) {
        this.bucketCount = children.length;
        this.childBuckets = children;
        if (bucketCount == 0) {
            return;
        }
        updateDiagnostics(children);
        updateWidth(children);
    }

    protected boolean checkForReferenceEquality(STNode... children) {
        for (int index = 0; index < children.length; index++) {
            if (childBuckets[index] != children[index]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Update various field properties during the internal node creation time.
     * <p>
     * Here we update following four width related fields.
     * width                       - width of the node without minutiae
     * widthWithLeadingMinutiae    - width of the node with only the leading minutiae
     * widthWithTrailingMinutiae   - width of the node with only the trailing minutiae
     * widthWithMinutiae           - width of the node with both leading and trailing minutiae
     * <p>
     * All the above fields are required to calculate the absolute location of the node in a text document
     * during the external tree node creation time. Since our plan is to reuse many
     * internal tree nodes, I think it should be fine to update the width fields now.
     *
     * @param children child nodes of this node
     */
    private void updateWidth(STNode[] children) {
        int firstChildIndex = getFirstChildIndex(children);
        if (firstChildIndex == -1) {
            // All child elements are null;
            return;
        }

        int lastChildIndex = getLastChildIndex(children);
        STNode firstChild = children[firstChildIndex];
        STNode lastChild = children[lastChildIndex];
        if (firstChildIndex == lastChildIndex) {
            this.width = firstChild.width;
            this.widthWithLeadingMinutiae = firstChild.widthWithLeadingMinutiae;
            this.widthWithTrailingMinutiae = firstChild.widthWithTrailingMinutiae;
            this.widthWithMinutiae = firstChild.widthWithMinutiae;
            return;
        }

        this.width = firstChild.widthWithTrailingMinutiae + lastChild.widthWithLeadingMinutiae;
        this.widthWithLeadingMinutiae = firstChild.widthWithMinutiae + lastChild.widthWithLeadingMinutiae;
        this.widthWithTrailingMinutiae = firstChild.widthWithTrailingMinutiae + lastChild.widthWithMinutiae;
        this.widthWithMinutiae = firstChild.widthWithMinutiae + lastChild.widthWithMinutiae;
        updateWidth(children, firstChildIndex, lastChildIndex);
    }

    private void updateWidth(STNode[] children, int firstChildIndex, int lastChildIndex) {
        for (int index = firstChildIndex + 1; index < lastChildIndex; index++) {
            STNode child = children[index];
            if (!SyntaxUtils.isSTNodePresent(children[index])) {
                continue;
            }
            this.width += child.widthWithMinutiae;
            this.widthWithLeadingMinutiae += child.widthWithMinutiae;
            this.widthWithTrailingMinutiae += child.widthWithMinutiae;
            this.widthWithMinutiae += child.widthWithMinutiae;
        }
    }

    private int getFirstChildIndex(STNode... children) {
        for (int index = 0; index < children.length; index++) {
            STNode child = children[index];
            if (SyntaxUtils.isSTNodePresent(child) && child.widthWithMinutiae != 0) {
                return index;
            }
        }
        return -1;
    }

    private int getLastChildIndex(STNode... children) {
        for (int index = children.length - 1; index >= 0; index--) {
            STNode child = children[index];
            if (SyntaxUtils.isSTNodePresent(child) && child.widthWithMinutiae != 0) {
                return index;
            }
        }
        return -1;
    }

    private void updateDiagnostics(STNode[] children) {
        // Return from the function if at lest one child has diagnostics.
        for (STNode child : children) {
            if (!SyntaxUtils.isSTNodePresent(child)) {
                continue;
            }
            if (child.flags.contains(STNodeFlags.HAS_DIAGNOSTICS)) {
                this.flags.add(STNodeFlags.HAS_DIAGNOSTICS);
                return;
            }
        }
    }
}
