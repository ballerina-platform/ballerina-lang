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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class InterpolationNode extends XMLItemNode {

    public InterpolationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token interpolationStartToken() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
        return childInBucket(1);
    }

    public Token interpolationEndToken() {
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
                "interpolationStartToken",
                "expression",
                "interpolationEndToken"};
    }

    public InterpolationNode modify(
            Token interpolationStartToken,
            ExpressionNode expression,
            Token interpolationEndToken) {
        if (checkForReferenceEquality(
                interpolationStartToken,
                expression,
                interpolationEndToken)) {
            return this;
        }

        return NodeFactory.createInterpolationNode(
                interpolationStartToken,
                expression,
                interpolationEndToken);
    }

    public InterpolationNodeModifier modify() {
        return new InterpolationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class InterpolationNodeModifier {
        private final InterpolationNode oldNode;
        private Token interpolationStartToken;
        private ExpressionNode expression;
        private Token interpolationEndToken;

        public InterpolationNodeModifier(InterpolationNode oldNode) {
            this.oldNode = oldNode;
            this.interpolationStartToken = oldNode.interpolationStartToken();
            this.expression = oldNode.expression();
            this.interpolationEndToken = oldNode.interpolationEndToken();
        }

        public InterpolationNodeModifier withInterpolationStartToken(
                Token interpolationStartToken) {
            Objects.requireNonNull(interpolationStartToken, "interpolationStartToken must not be null");
            this.interpolationStartToken = interpolationStartToken;
            return this;
        }

        public InterpolationNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public InterpolationNodeModifier withInterpolationEndToken(
                Token interpolationEndToken) {
            Objects.requireNonNull(interpolationEndToken, "interpolationEndToken must not be null");
            this.interpolationEndToken = interpolationEndToken;
            return this;
        }

        public InterpolationNode apply() {
            return oldNode.modify(
                    interpolationStartToken,
                    expression,
                    interpolationEndToken);
        }
    }
}
