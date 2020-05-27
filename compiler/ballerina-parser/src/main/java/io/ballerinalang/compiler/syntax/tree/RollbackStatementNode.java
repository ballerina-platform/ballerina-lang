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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class RollbackStatementNode extends StatementNode {

    public RollbackStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token rollbackKeyword() {
        return childInBucket(0);
    }

    public Optional<ExpressionNode> expression() {
        return optionalChildInBucket(1);
    }

    public Token semicolon() {
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
                "rollbackKeyword",
                "expression",
                "semicolon"};
    }

    public RollbackStatementNode modify(
            Token rollbackKeyword,
            ExpressionNode expression,
            Token semicolon) {
        if (checkForReferenceEquality(
                rollbackKeyword,
                expression,
                semicolon)) {
            return this;
        }

        return NodeFactory.createRollbackStatementNode(
                rollbackKeyword,
                expression,
                semicolon);
    }

    public RollbackStatementNodeModifier modify() {
        return new RollbackStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RollbackStatementNodeModifier {
        private final RollbackStatementNode oldNode;
        private Token rollbackKeyword;
        private ExpressionNode expression;
        private Token semicolon;

        public RollbackStatementNodeModifier(RollbackStatementNode oldNode) {
            this.oldNode = oldNode;
            this.rollbackKeyword = oldNode.rollbackKeyword();
            this.expression = oldNode.expression().orElse(null);
            this.semicolon = oldNode.semicolon();
        }

        public RollbackStatementNodeModifier withRollbackKeyword(
                Token rollbackKeyword) {
            Objects.requireNonNull(rollbackKeyword, "rollbackKeyword must not be null");
            this.rollbackKeyword = rollbackKeyword;
            return this;
        }

        public RollbackStatementNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public RollbackStatementNodeModifier withSemicolon(
                Token semicolon) {
            Objects.requireNonNull(semicolon, "semicolon must not be null");
            this.semicolon = semicolon;
            return this;
        }

        public RollbackStatementNode apply() {
            return oldNode.modify(
                    rollbackKeyword,
                    expression,
                    semicolon);
        }
    }
}
