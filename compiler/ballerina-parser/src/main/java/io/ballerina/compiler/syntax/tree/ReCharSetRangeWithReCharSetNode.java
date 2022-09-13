/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.3.0
 */
public class ReCharSetRangeWithReCharSetNode extends NonTerminalNode {

    public ReCharSetRangeWithReCharSetNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ReCharSetRangeNode reCharSetRange() {
        return childInBucket(0);
    }

    public Optional<Node> reCharSet() {
        return optionalChildInBucket(1);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "reCharSetRange",
                "reCharSet"};
    }

    public ReCharSetRangeWithReCharSetNode modify(
            ReCharSetRangeNode reCharSetRange,
            Node reCharSet) {
        if (checkForReferenceEquality(
                reCharSetRange,
                reCharSet)) {
            return this;
        }

        return NodeFactory.createReCharSetRangeWithReCharSetNode(
                reCharSetRange,
                reCharSet);
    }

    public ReCharSetRangeWithReCharSetNodeModifier modify() {
        return new ReCharSetRangeWithReCharSetNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReCharSetRangeWithReCharSetNodeModifier {
        private final ReCharSetRangeWithReCharSetNode oldNode;
        private ReCharSetRangeNode reCharSetRange;
        private Node reCharSet;

        public ReCharSetRangeWithReCharSetNodeModifier(ReCharSetRangeWithReCharSetNode oldNode) {
            this.oldNode = oldNode;
            this.reCharSetRange = oldNode.reCharSetRange();
            this.reCharSet = oldNode.reCharSet().orElse(null);
        }

        public ReCharSetRangeWithReCharSetNodeModifier withReCharSetRange(
                ReCharSetRangeNode reCharSetRange) {
            Objects.requireNonNull(reCharSetRange, "reCharSetRange must not be null");
            this.reCharSetRange = reCharSetRange;
            return this;
        }

        public ReCharSetRangeWithReCharSetNodeModifier withReCharSet(
                Node reCharSet) {
            this.reCharSet = reCharSet;
            return this;
        }

        public ReCharSetRangeWithReCharSetNode apply() {
            return oldNode.modify(
                    reCharSetRange,
                    reCharSet);
        }
    }
}
