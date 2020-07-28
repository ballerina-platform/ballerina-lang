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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class FailExpressionNode extends ExpressionNode {

    public FailExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token failKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
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
                "failKeyword",
                "expression"};
    }

    public FailExpressionNode modify(
            SyntaxKind kind,
            Token failKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                failKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createFailExpressionNode(
                kind,
                failKeyword,
                expression);
    }

    public FailExpressionNodeModifier modify() {
        return new FailExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FailExpressionNodeModifier {
        private final FailExpressionNode oldNode;
        private Token failKeyword;
        private ExpressionNode expression;

        public FailExpressionNodeModifier(FailExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.failKeyword = oldNode.failKeyword();
            this.expression = oldNode.expression();
        }

        public FailExpressionNodeModifier withFailKeyword(
                Token failKeyword) {
            Objects.requireNonNull(failKeyword, "failKeyword must not be null");
            this.failKeyword = failKeyword;
            return this;
        }

        public FailExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public FailExpressionNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    failKeyword,
                    expression);
        }
    }
}
