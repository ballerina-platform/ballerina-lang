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

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ReCapturingGroupsNode extends NonTerminalNode {

    public ReCapturingGroupsNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openParenthesis() {
        return childInBucket(0);
    }

    public Optional<ReFlagExpressionNode> reFlagExpression() {
        return optionalChildInBucket(1);
    }

    public NodeList<Node> reSequences() {
        return new NodeList<>(childInBucket(2));
    }

    public Token closeParenthesis() {
        return childInBucket(3);
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
                "openParenthesis",
                "reFlagExpression",
                "reSequences",
                "closeParenthesis"};
    }

    public ReCapturingGroupsNode modify(
            Token openParenthesis,
            ReFlagExpressionNode reFlagExpression,
            NodeList<Node> reSequences,
            Token closeParenthesis) {
        if (checkForReferenceEquality(
                openParenthesis,
                reFlagExpression,
                reSequences.underlyingListNode(),
                closeParenthesis)) {
            return this;
        }

        return NodeFactory.createReCapturingGroupsNode(
                openParenthesis,
                reFlagExpression,
                reSequences,
                closeParenthesis);
    }

    public ReCapturingGroupsNodeModifier modify() {
        return new ReCapturingGroupsNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReCapturingGroupsNodeModifier {
        private final ReCapturingGroupsNode oldNode;
        private Token openParenthesis;
        private ReFlagExpressionNode reFlagExpression;
        private NodeList<Node> reSequences;
        private Token closeParenthesis;

        public ReCapturingGroupsNodeModifier(ReCapturingGroupsNode oldNode) {
            this.oldNode = oldNode;
            this.openParenthesis = oldNode.openParenthesis();
            this.reFlagExpression = oldNode.reFlagExpression().orElse(null);
            this.reSequences = oldNode.reSequences();
            this.closeParenthesis = oldNode.closeParenthesis();
        }

        public ReCapturingGroupsNodeModifier withOpenParenthesis(
                Token openParenthesis) {
            Objects.requireNonNull(openParenthesis, "openParenthesis must not be null");
            this.openParenthesis = openParenthesis;
            return this;
        }

        public ReCapturingGroupsNodeModifier withReFlagExpression(
                ReFlagExpressionNode reFlagExpression) {
            this.reFlagExpression = reFlagExpression;
            return this;
        }

        public ReCapturingGroupsNodeModifier withReSequences(
                NodeList<Node> reSequences) {
            Objects.requireNonNull(reSequences, "reSequences must not be null");
            this.reSequences = reSequences;
            return this;
        }

        public ReCapturingGroupsNodeModifier withCloseParenthesis(
                Token closeParenthesis) {
            Objects.requireNonNull(closeParenthesis, "closeParenthesis must not be null");
            this.closeParenthesis = closeParenthesis;
            return this;
        }

        public ReCapturingGroupsNode apply() {
            return oldNode.modify(
                    openParenthesis,
                    reFlagExpression,
                    reSequences,
                    closeParenthesis);
        }
    }
}
