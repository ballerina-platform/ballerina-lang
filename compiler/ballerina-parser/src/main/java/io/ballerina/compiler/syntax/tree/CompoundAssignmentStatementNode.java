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
public class CompoundAssignmentStatementNode extends StatementNode {

    public CompoundAssignmentStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode lhsExpression() {
        return childInBucket(0);
    }

    public Token binaryOperator() {
        return childInBucket(1);
    }

    public Token equalsToken() {
        return childInBucket(2);
    }

    public ExpressionNode rhsExpression() {
        return childInBucket(3);
    }

    public Token semicolonToken() {
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
                "binaryOperator",
                "equalsToken",
                "rhsExpression",
                "semicolonToken"};
    }

    public CompoundAssignmentStatementNode modify(
            ExpressionNode lhsExpression,
            Token binaryOperator,
            Token equalsToken,
            ExpressionNode rhsExpression,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createCompoundAssignmentStatementNode(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken);
    }

    public CompoundAssignmentStatementNodeModifier modify() {
        return new CompoundAssignmentStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class CompoundAssignmentStatementNodeModifier {
        private final CompoundAssignmentStatementNode oldNode;
        private ExpressionNode lhsExpression;
        private Token binaryOperator;
        private Token equalsToken;
        private ExpressionNode rhsExpression;
        private Token semicolonToken;

        public CompoundAssignmentStatementNodeModifier(CompoundAssignmentStatementNode oldNode) {
            this.oldNode = oldNode;
            this.lhsExpression = oldNode.lhsExpression();
            this.binaryOperator = oldNode.binaryOperator();
            this.equalsToken = oldNode.equalsToken();
            this.rhsExpression = oldNode.rhsExpression();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public CompoundAssignmentStatementNodeModifier withLhsExpression(
                ExpressionNode lhsExpression) {
            Objects.requireNonNull(lhsExpression, "lhsExpression must not be null");
            this.lhsExpression = lhsExpression;
            return this;
        }

        public CompoundAssignmentStatementNodeModifier withBinaryOperator(
                Token binaryOperator) {
            Objects.requireNonNull(binaryOperator, "binaryOperator must not be null");
            this.binaryOperator = binaryOperator;
            return this;
        }

        public CompoundAssignmentStatementNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public CompoundAssignmentStatementNodeModifier withRhsExpression(
                ExpressionNode rhsExpression) {
            Objects.requireNonNull(rhsExpression, "rhsExpression must not be null");
            this.rhsExpression = rhsExpression;
            return this;
        }

        public CompoundAssignmentStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public CompoundAssignmentStatementNode apply() {
            return oldNode.modify(
                    lhsExpression,
                    binaryOperator,
                    equalsToken,
                    rhsExpression,
                    semicolonToken);
        }
    }
}
