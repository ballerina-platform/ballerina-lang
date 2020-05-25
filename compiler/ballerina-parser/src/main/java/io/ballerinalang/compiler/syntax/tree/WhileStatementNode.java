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
public class WhileStatementNode extends StatementNode {

    public WhileStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token whileKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode condition() {
        return childInBucket(1);
    }

    public BlockStatementNode whileBody() {
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
                "whileKeyword",
                "condition",
                "whileBody"};
    }

    public WhileStatementNode modify(
            Token whileKeyword,
            ExpressionNode condition,
            BlockStatementNode whileBody) {
        if (checkForReferenceEquality(
                whileKeyword,
                condition,
                whileBody)) {
            return this;
        }

        return NodeFactory.createWhileStatementNode(
                whileKeyword,
                condition,
                whileBody);
    }

    public WhileStatementNodeModifier modify() {
        return new WhileStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class WhileStatementNodeModifier {
        private final WhileStatementNode oldNode;
        private Token whileKeyword;
        private ExpressionNode condition;
        private BlockStatementNode whileBody;

        public WhileStatementNodeModifier(WhileStatementNode oldNode) {
            this.oldNode = oldNode;
            this.whileKeyword = oldNode.whileKeyword();
            this.condition = oldNode.condition();
            this.whileBody = oldNode.whileBody();
        }

        public WhileStatementNodeModifier withWhileKeyword(
                Token whileKeyword) {
            Objects.requireNonNull(whileKeyword, "whileKeyword must not be null");
            this.whileKeyword = whileKeyword;
            return this;
        }

        public WhileStatementNodeModifier withCondition(
                ExpressionNode condition) {
            Objects.requireNonNull(condition, "condition must not be null");
            this.condition = condition;
            return this;
        }

        public WhileStatementNodeModifier withWhileBody(
                BlockStatementNode whileBody) {
            Objects.requireNonNull(whileBody, "whileBody must not be null");
            this.whileBody = whileBody;
            return this;
        }

        public WhileStatementNode apply() {
            return oldNode.modify(
                    whileKeyword,
                    condition,
                    whileBody);
        }
    }
}
