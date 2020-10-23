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
public class FailStatementNode extends StatementNode {

    public FailStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token failKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
        return childInBucket(1);
    }

    public Token semicolonToken() {
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
                "failKeyword",
                "expression",
                "semicolonToken"};
    }

    public FailStatementNode modify(
            Token failKeyword,
            ExpressionNode expression,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                failKeyword,
                expression,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createFailStatementNode(
                failKeyword,
                expression,
                semicolonToken);
    }

    public FailStatementNodeModifier modify() {
        return new FailStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FailStatementNodeModifier {
        private final FailStatementNode oldNode;
        private Token failKeyword;
        private ExpressionNode expression;
        private Token semicolonToken;

        public FailStatementNodeModifier(FailStatementNode oldNode) {
            this.oldNode = oldNode;
            this.failKeyword = oldNode.failKeyword();
            this.expression = oldNode.expression();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public FailStatementNodeModifier withFailKeyword(
                Token failKeyword) {
            Objects.requireNonNull(failKeyword, "failKeyword must not be null");
            this.failKeyword = failKeyword;
            return this;
        }

        public FailStatementNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public FailStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public FailStatementNode apply() {
            return oldNode.modify(
                    failKeyword,
                    expression,
                    semicolonToken);
        }
    }
}
