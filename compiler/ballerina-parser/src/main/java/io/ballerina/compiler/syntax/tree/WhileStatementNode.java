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

    public Optional<OnFailClauseNode> onFailClause() {
        return optionalChildInBucket(3);
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
                "whileBody",
                "onFailClause"};
    }

    public WhileStatementNode modify(
            Token whileKeyword,
            ExpressionNode condition,
            BlockStatementNode whileBody,
            OnFailClauseNode onFailClause) {
        if (checkForReferenceEquality(
                whileKeyword,
                condition,
                whileBody,
                onFailClause)) {
            return this;
        }

        return NodeFactory.createWhileStatementNode(
                whileKeyword,
                condition,
                whileBody,
                onFailClause);
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
        private OnFailClauseNode onFailClause;

        public WhileStatementNodeModifier(WhileStatementNode oldNode) {
            this.oldNode = oldNode;
            this.whileKeyword = oldNode.whileKeyword();
            this.condition = oldNode.condition();
            this.whileBody = oldNode.whileBody();
            this.onFailClause = oldNode.onFailClause().orElse(null);
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

        public WhileStatementNodeModifier withOnFailClause(
                OnFailClauseNode onFailClause) {
            Objects.requireNonNull(onFailClause, "onFailClause must not be null");
            this.onFailClause = onFailClause;
            return this;
        }

        public WhileStatementNode apply() {
            return oldNode.modify(
                    whileKeyword,
                    condition,
                    whileBody,
                    onFailClause);
        }
    }
}
