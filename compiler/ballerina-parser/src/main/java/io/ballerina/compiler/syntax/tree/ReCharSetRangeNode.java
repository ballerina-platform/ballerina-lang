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

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.3.0
 */
public class ReCharSetRangeNode extends NonTerminalNode {

    public ReCharSetRangeNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node lhsReCharSetAtom() {
        return childInBucket(0);
    }

    public Token minusToken() {
        return childInBucket(1);
    }

    public Node rhsReCharSetAtom() {
        return childInBucket(2);
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
                "lhsReCharSetAtom",
                "minusToken",
                "rhsReCharSetAtom"};
    }

    public ReCharSetRangeNode modify(
            Node lhsReCharSetAtom,
            Token minusToken,
            Node rhsReCharSetAtom) {
        if (checkForReferenceEquality(
                lhsReCharSetAtom,
                minusToken,
                rhsReCharSetAtom)) {
            return this;
        }

        return NodeFactory.createReCharSetRangeNode(
                lhsReCharSetAtom,
                minusToken,
                rhsReCharSetAtom);
    }

    public ReCharSetRangeNodeModifier modify() {
        return new ReCharSetRangeNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReCharSetRangeNodeModifier {
        private final ReCharSetRangeNode oldNode;
        private Node lhsReCharSetAtom;
        private Token minusToken;
        private Node rhsReCharSetAtom;

        public ReCharSetRangeNodeModifier(ReCharSetRangeNode oldNode) {
            this.oldNode = oldNode;
            this.lhsReCharSetAtom = oldNode.lhsReCharSetAtom();
            this.minusToken = oldNode.minusToken();
            this.rhsReCharSetAtom = oldNode.rhsReCharSetAtom();
        }

        public ReCharSetRangeNodeModifier withLhsReCharSetAtom(
                Node lhsReCharSetAtom) {
            Objects.requireNonNull(lhsReCharSetAtom, "lhsReCharSetAtom must not be null");
            this.lhsReCharSetAtom = lhsReCharSetAtom;
            return this;
        }

        public ReCharSetRangeNodeModifier withMinusToken(
                Token minusToken) {
            Objects.requireNonNull(minusToken, "minusToken must not be null");
            this.minusToken = minusToken;
            return this;
        }

        public ReCharSetRangeNodeModifier withRhsReCharSetAtom(
                Node rhsReCharSetAtom) {
            Objects.requireNonNull(rhsReCharSetAtom, "rhsReCharSetAtom must not be null");
            this.rhsReCharSetAtom = rhsReCharSetAtom;
            return this;
        }

        public ReCharSetRangeNode apply() {
            return oldNode.modify(
                    lhsReCharSetAtom,
                    minusToken,
                    rhsReCharSetAtom);
        }
    }
}
