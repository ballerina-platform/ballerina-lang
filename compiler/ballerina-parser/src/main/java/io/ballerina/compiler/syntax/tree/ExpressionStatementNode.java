/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
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
public class ExpressionStatementNode extends StatementNode {

    public ExpressionStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Token semicolonToken() {
        return childInBucket(1);
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
                "expression",
                "semicolonToken"};
    }

    public ExpressionStatementNode modify(
            SyntaxKind kind,
            ExpressionNode expression,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                expression,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createExpressionStatementNode(
                kind,
                expression,
                semicolonToken);
    }

    public ExpressionStatementNodeModifier modify() {
        return new ExpressionStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ExpressionStatementNodeModifier {
        private final ExpressionStatementNode oldNode;
        private ExpressionNode expression;
        private Token semicolonToken;

        public ExpressionStatementNodeModifier(ExpressionStatementNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ExpressionStatementNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public ExpressionStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ExpressionStatementNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    expression,
                    semicolonToken);
        }
    }
}
