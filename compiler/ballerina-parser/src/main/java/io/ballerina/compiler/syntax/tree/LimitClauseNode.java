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
public class LimitClauseNode extends IntermediateClauseNode {

    public LimitClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token limitKeyword() {
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
                "limitKeyword",
                "expression"};
    }

    public LimitClauseNode modify(
            Token limitKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                limitKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createLimitClauseNode(
                limitKeyword,
                expression);
    }

    public LimitClauseNodeModifier modify() {
        return new LimitClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class LimitClauseNodeModifier {
        private final LimitClauseNode oldNode;
        private Token limitKeyword;
        private ExpressionNode expression;

        public LimitClauseNodeModifier(LimitClauseNode oldNode) {
            this.oldNode = oldNode;
            this.limitKeyword = oldNode.limitKeyword();
            this.expression = oldNode.expression();
        }

        public LimitClauseNodeModifier withLimitKeyword(
                Token limitKeyword) {
            Objects.requireNonNull(limitKeyword, "limitKeyword must not be null");
            this.limitKeyword = limitKeyword;
            return this;
        }

        public LimitClauseNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public LimitClauseNode apply() {
            return oldNode.modify(
                    limitKeyword,
                    expression);
        }
    }
}
