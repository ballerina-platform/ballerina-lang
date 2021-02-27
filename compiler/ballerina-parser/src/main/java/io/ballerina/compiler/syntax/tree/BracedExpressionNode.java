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
public class BracedExpressionNode extends ExpressionNode {

    public BracedExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openParen() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
        return childInBucket(1);
    }

    public Token closeParen() {
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
                "openParen",
                "expression",
                "closeParen"};
    }

    public BracedExpressionNode modify(
            SyntaxKind kind,
            Token openParen,
            ExpressionNode expression,
            Token closeParen) {
        if (checkForReferenceEquality(
                openParen,
                expression,
                closeParen)) {
            return this;
        }

        return NodeFactory.createBracedExpressionNode(
                kind,
                openParen,
                expression,
                closeParen);
    }

    public BracedExpressionNodeModifier modify() {
        return new BracedExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class BracedExpressionNodeModifier {
        private final BracedExpressionNode oldNode;
        private Token openParen;
        private ExpressionNode expression;
        private Token closeParen;

        public BracedExpressionNodeModifier(BracedExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.openParen = oldNode.openParen();
            this.expression = oldNode.expression();
            this.closeParen = oldNode.closeParen();
        }

        public BracedExpressionNodeModifier withOpenParen(
                Token openParen) {
            Objects.requireNonNull(openParen, "openParen must not be null");
            this.openParen = openParen;
            return this;
        }

        public BracedExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public BracedExpressionNodeModifier withCloseParen(
                Token closeParen) {
            Objects.requireNonNull(closeParen, "closeParen must not be null");
            this.closeParen = closeParen;
            return this;
        }

        public BracedExpressionNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    openParen,
                    expression,
                    closeParen);
        }
    }
}
