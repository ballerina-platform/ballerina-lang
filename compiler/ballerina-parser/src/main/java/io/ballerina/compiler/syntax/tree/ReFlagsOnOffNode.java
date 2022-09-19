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
public class ReFlagsOnOffNode extends NonTerminalNode {

    public ReFlagsOnOffNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ReFlagsNode lhsReFlags() {
        return childInBucket(0);
    }

    public Optional<Token> minusToken() {
        return optionalChildInBucket(1);
    }

    public Optional<ReFlagsNode> rhsReFlags() {
        return optionalChildInBucket(2);
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
                "lhsReFlags",
                "minusToken",
                "rhsReFlags"};
    }

    public ReFlagsOnOffNode modify(
            ReFlagsNode lhsReFlags,
            Token minusToken,
            ReFlagsNode rhsReFlags) {
        if (checkForReferenceEquality(
                lhsReFlags,
                minusToken,
                rhsReFlags)) {
            return this;
        }

        return NodeFactory.createReFlagsOnOffNode(
                lhsReFlags,
                minusToken,
                rhsReFlags);
    }

    public ReFlagsOnOffNodeModifier modify() {
        return new ReFlagsOnOffNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReFlagsOnOffNodeModifier {
        private final ReFlagsOnOffNode oldNode;
        private ReFlagsNode lhsReFlags;
        private Token minusToken;
        private ReFlagsNode rhsReFlags;

        public ReFlagsOnOffNodeModifier(ReFlagsOnOffNode oldNode) {
            this.oldNode = oldNode;
            this.lhsReFlags = oldNode.lhsReFlags();
            this.minusToken = oldNode.minusToken().orElse(null);
            this.rhsReFlags = oldNode.rhsReFlags().orElse(null);
        }

        public ReFlagsOnOffNodeModifier withLhsReFlags(
                ReFlagsNode lhsReFlags) {
            Objects.requireNonNull(lhsReFlags, "lhsReFlags must not be null");
            this.lhsReFlags = lhsReFlags;
            return this;
        }

        public ReFlagsOnOffNodeModifier withMinusToken(
                Token minusToken) {
            this.minusToken = minusToken;
            return this;
        }

        public ReFlagsOnOffNodeModifier withRhsReFlags(
                ReFlagsNode rhsReFlags) {
            this.rhsReFlags = rhsReFlags;
            return this;
        }

        public ReFlagsOnOffNode apply() {
            return oldNode.modify(
                    lhsReFlags,
                    minusToken,
                    rhsReFlags);
        }
    }
}
