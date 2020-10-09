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
public class SelectClauseNode extends ClauseNode {

    public SelectClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token selectKeyword() {
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
                "selectKeyword",
                "expression"};
    }

    public SelectClauseNode modify(
            Token selectKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                selectKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createSelectClauseNode(
                selectKeyword,
                expression);
    }

    public SelectClauseNodeModifier modify() {
        return new SelectClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class SelectClauseNodeModifier {
        private final SelectClauseNode oldNode;
        private Token selectKeyword;
        private ExpressionNode expression;

        public SelectClauseNodeModifier(SelectClauseNode oldNode) {
            this.oldNode = oldNode;
            this.selectKeyword = oldNode.selectKeyword();
            this.expression = oldNode.expression();
        }

        public SelectClauseNodeModifier withSelectKeyword(
                Token selectKeyword) {
            Objects.requireNonNull(selectKeyword, "selectKeyword must not be null");
            this.selectKeyword = selectKeyword;
            return this;
        }

        public SelectClauseNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public SelectClauseNode apply() {
            return oldNode.modify(
                    selectKeyword,
                    expression);
        }
    }
}
