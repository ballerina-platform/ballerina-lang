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
public class PanicStatementNode extends StatementNode {

    public PanicStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token panicKeyword() {
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
                "panicKeyword",
                "expression",
                "semicolonToken"};
    }

    public PanicStatementNode modify(
            Token panicKeyword,
            ExpressionNode expression,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                panicKeyword,
                expression,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createPanicStatementNode(
                panicKeyword,
                expression,
                semicolonToken);
    }

    public PanicStatementNodeModifier modify() {
        return new PanicStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class PanicStatementNodeModifier {
        private final PanicStatementNode oldNode;
        private Token panicKeyword;
        private ExpressionNode expression;
        private Token semicolonToken;

        public PanicStatementNodeModifier(PanicStatementNode oldNode) {
            this.oldNode = oldNode;
            this.panicKeyword = oldNode.panicKeyword();
            this.expression = oldNode.expression();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public PanicStatementNodeModifier withPanicKeyword(
                Token panicKeyword) {
            Objects.requireNonNull(panicKeyword, "panicKeyword must not be null");
            this.panicKeyword = panicKeyword;
            return this;
        }

        public PanicStatementNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public PanicStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public PanicStatementNode apply() {
            return oldNode.modify(
                    panicKeyword,
                    expression,
                    semicolonToken);
        }
    }
}
