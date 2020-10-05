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
public class OnConflictClauseNode extends ClauseNode {

    public OnConflictClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token onKeyword() {
        return childInBucket(0);
    }

    public Token conflictKeyword() {
        return childInBucket(1);
    }

    public ExpressionNode expression() {
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
                "onKeyword",
                "conflictKeyword",
                "expression"};
    }

    public OnConflictClauseNode modify(
            Token onKeyword,
            Token conflictKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                onKeyword,
                conflictKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createOnConflictClauseNode(
                onKeyword,
                conflictKeyword,
                expression);
    }

    public OnConflictClauseNodeModifier modify() {
        return new OnConflictClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OnConflictClauseNodeModifier {
        private final OnConflictClauseNode oldNode;
        private Token onKeyword;
        private Token conflictKeyword;
        private ExpressionNode expression;

        public OnConflictClauseNodeModifier(OnConflictClauseNode oldNode) {
            this.oldNode = oldNode;
            this.onKeyword = oldNode.onKeyword();
            this.conflictKeyword = oldNode.conflictKeyword();
            this.expression = oldNode.expression();
        }

        public OnConflictClauseNodeModifier withOnKeyword(
                Token onKeyword) {
            Objects.requireNonNull(onKeyword, "onKeyword must not be null");
            this.onKeyword = onKeyword;
            return this;
        }

        public OnConflictClauseNodeModifier withConflictKeyword(
                Token conflictKeyword) {
            Objects.requireNonNull(conflictKeyword, "conflictKeyword must not be null");
            this.conflictKeyword = conflictKeyword;
            return this;
        }

        public OnConflictClauseNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public OnConflictClauseNode apply() {
            return oldNode.modify(
                    onKeyword,
                    conflictKeyword,
                    expression);
        }
    }
}
