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
public class ConditionalExpressionNode extends ExpressionNode {

    public ConditionalExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode lhsExpression() {
        return childInBucket(0);
    }

    public Token questionMarkToken() {
        return childInBucket(1);
    }

    public ExpressionNode middleExpression() {
        return childInBucket(2);
    }

    public Token colonToken() {
        return childInBucket(3);
    }

    public ExpressionNode endExpression() {
        return childInBucket(4);
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
                "lhsExpression",
                "questionMarkToken",
                "middleExpression",
                "colonToken",
                "endExpression"};
    }

    public ConditionalExpressionNode modify(
            ExpressionNode lhsExpression,
            Token questionMarkToken,
            ExpressionNode middleExpression,
            Token colonToken,
            ExpressionNode endExpression) {
        if (checkForReferenceEquality(
                lhsExpression,
                questionMarkToken,
                middleExpression,
                colonToken,
                endExpression)) {
            return this;
        }

        return NodeFactory.createConditionalExpressionNode(
                lhsExpression,
                questionMarkToken,
                middleExpression,
                colonToken,
                endExpression);
    }

    public ConditionalExpressionNodeModifier modify() {
        return new ConditionalExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ConditionalExpressionNodeModifier {
        private final ConditionalExpressionNode oldNode;
        private ExpressionNode lhsExpression;
        private Token questionMarkToken;
        private ExpressionNode middleExpression;
        private Token colonToken;
        private ExpressionNode endExpression;

        public ConditionalExpressionNodeModifier(ConditionalExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.lhsExpression = oldNode.lhsExpression();
            this.questionMarkToken = oldNode.questionMarkToken();
            this.middleExpression = oldNode.middleExpression();
            this.colonToken = oldNode.colonToken();
            this.endExpression = oldNode.endExpression();
        }

        public ConditionalExpressionNodeModifier withLhsExpression(
                ExpressionNode lhsExpression) {
            Objects.requireNonNull(lhsExpression, "lhsExpression must not be null");
            this.lhsExpression = lhsExpression;
            return this;
        }

        public ConditionalExpressionNodeModifier withQuestionMarkToken(
                Token questionMarkToken) {
            Objects.requireNonNull(questionMarkToken, "questionMarkToken must not be null");
            this.questionMarkToken = questionMarkToken;
            return this;
        }

        public ConditionalExpressionNodeModifier withMiddleExpression(
                ExpressionNode middleExpression) {
            Objects.requireNonNull(middleExpression, "middleExpression must not be null");
            this.middleExpression = middleExpression;
            return this;
        }

        public ConditionalExpressionNodeModifier withColonToken(
                Token colonToken) {
            Objects.requireNonNull(colonToken, "colonToken must not be null");
            this.colonToken = colonToken;
            return this;
        }

        public ConditionalExpressionNodeModifier withEndExpression(
                ExpressionNode endExpression) {
            Objects.requireNonNull(endExpression, "endExpression must not be null");
            this.endExpression = endExpression;
            return this;
        }

        public ConditionalExpressionNode apply() {
            return oldNode.modify(
                    lhsExpression,
                    questionMarkToken,
                    middleExpression,
                    colonToken,
                    endExpression);
        }
    }
}
