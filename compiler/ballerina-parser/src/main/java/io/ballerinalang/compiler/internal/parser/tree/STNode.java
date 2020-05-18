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

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * {@code STNode} is the base class for all tree nodes in the internal syntax tree.
 *
 * @since 1.3.0
 */
public abstract class STNode {
    public final SyntaxKind kind;
    protected int width;
    protected int widthWithLeadingMinutiae;
    protected int widthWithTrailingMinutiae;
    protected int widthWithMinutiae;

    protected static final STNode[] EMPTY_BUCKET = new STNode[0];
    // The following fields allow us to navigate the tree without the knowledge of the particular tree nodes
    protected int bucketCount;
    protected STNode[] childBuckets = EMPTY_BUCKET;

    STNode(SyntaxKind kind) {
        this.kind = kind;
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

    public int bucketCount() {
        return bucketCount;
    }

    public <T extends Node> T createUnlinkedFacade() {
        return (T) createFacade(0, null);
    }

    public abstract Node createFacade(int position, NonTerminalNode parent);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (STNode child : this.childBuckets) {
            sb.append(child != null ? child.toString() : "");
        }
        return sb.toString();
    }

    protected void addChildren(STNode... children) {
        this.bucketCount = children.length;
        this.childBuckets = children;
        if (bucketCount == 0) {
            return;
        }
        updateWidth(children);
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
}
