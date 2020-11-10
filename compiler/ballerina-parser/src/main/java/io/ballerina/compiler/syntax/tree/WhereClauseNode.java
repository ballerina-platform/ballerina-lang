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
public class WhereClauseNode extends IntermediateClauseNode {

    public WhereClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token whereKeyword() {
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
                "whereKeyword",
                "expression"};
    }

    public WhereClauseNode modify(
            Token whereKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                whereKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createWhereClauseNode(
                whereKeyword,
                expression);
    }

    public WhereClauseNodeModifier modify() {
        return new WhereClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class WhereClauseNodeModifier {
        private final WhereClauseNode oldNode;
        private Token whereKeyword;
        private ExpressionNode expression;

        public WhereClauseNodeModifier(WhereClauseNode oldNode) {
            this.oldNode = oldNode;
            this.whereKeyword = oldNode.whereKeyword();
            this.expression = oldNode.expression();
        }

        public WhereClauseNodeModifier withWhereKeyword(
                Token whereKeyword) {
            Objects.requireNonNull(whereKeyword, "whereKeyword must not be null");
            this.whereKeyword = whereKeyword;
            return this;
        }

        public WhereClauseNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public WhereClauseNode apply() {
            return oldNode.modify(
                    whereKeyword,
                    expression);
        }
    }
}
