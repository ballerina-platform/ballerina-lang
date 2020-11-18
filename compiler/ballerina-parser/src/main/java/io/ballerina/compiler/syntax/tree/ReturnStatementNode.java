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
public class ReturnStatementNode extends StatementNode {

    public ReturnStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token returnKeyword() {
        return childInBucket(0);
    }

    public Optional<ExpressionNode> expression() {
        return optionalChildInBucket(1);
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
                "returnKeyword",
                "expression",
                "semicolonToken"};
    }

    public ReturnStatementNode modify(
            Token returnKeyword,
            ExpressionNode expression,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                returnKeyword,
                expression,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createReturnStatementNode(
                returnKeyword,
                expression,
                semicolonToken);
    }

    public ReturnStatementNodeModifier modify() {
        return new ReturnStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReturnStatementNodeModifier {
        private final ReturnStatementNode oldNode;
        private Token returnKeyword;
        private ExpressionNode expression;
        private Token semicolonToken;

        public ReturnStatementNodeModifier(ReturnStatementNode oldNode) {
            this.oldNode = oldNode;
            this.returnKeyword = oldNode.returnKeyword();
            this.expression = oldNode.expression().orElse(null);
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ReturnStatementNodeModifier withReturnKeyword(
                Token returnKeyword) {
            Objects.requireNonNull(returnKeyword, "returnKeyword must not be null");
            this.returnKeyword = returnKeyword;
            return this;
        }

        public ReturnStatementNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public ReturnStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ReturnStatementNode apply() {
            return oldNode.modify(
                    returnKeyword,
                    expression,
                    semicolonToken);
        }
    }
}
