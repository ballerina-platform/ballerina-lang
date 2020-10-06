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
public class OnClauseNode extends ClauseNode {

    public OnClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token onKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode lhsExpression() {
        return childInBucket(1);
    }

    public Token equalsKeyword() {
        return childInBucket(2);
    }

    public ExpressionNode rhsExpression() {
        return childInBucket(3);
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
                "lhsExpression",
                "equalsKeyword",
                "rhsExpression"};
    }

    public OnClauseNode modify(
            Token onKeyword,
            ExpressionNode lhsExpression,
            Token equalsKeyword,
            ExpressionNode rhsExpression) {
        if (checkForReferenceEquality(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression)) {
            return this;
        }

        return NodeFactory.createOnClauseNode(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression);
    }

    public OnClauseNodeModifier modify() {
        return new OnClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OnClauseNodeModifier {
        private final OnClauseNode oldNode;
        private Token onKeyword;
        private ExpressionNode lhsExpression;
        private Token equalsKeyword;
        private ExpressionNode rhsExpression;

        public OnClauseNodeModifier(OnClauseNode oldNode) {
            this.oldNode = oldNode;
            this.onKeyword = oldNode.onKeyword();
            this.lhsExpression = oldNode.lhsExpression();
            this.equalsKeyword = oldNode.equalsKeyword();
            this.rhsExpression = oldNode.rhsExpression();
        }

        public OnClauseNodeModifier withOnKeyword(
                Token onKeyword) {
            Objects.requireNonNull(onKeyword, "onKeyword must not be null");
            this.onKeyword = onKeyword;
            return this;
        }

        public OnClauseNodeModifier withLhsExpression(
                ExpressionNode lhsExpression) {
            Objects.requireNonNull(lhsExpression, "lhsExpression must not be null");
            this.lhsExpression = lhsExpression;
            return this;
        }

        public OnClauseNodeModifier withEqualsKeyword(
                Token equalsKeyword) {
            Objects.requireNonNull(equalsKeyword, "equalsKeyword must not be null");
            this.equalsKeyword = equalsKeyword;
            return this;
        }

        public OnClauseNodeModifier withRhsExpression(
                ExpressionNode rhsExpression) {
            Objects.requireNonNull(rhsExpression, "rhsExpression must not be null");
            this.rhsExpression = rhsExpression;
            return this;
        }

        public OnClauseNode apply() {
            return oldNode.modify(
                    onKeyword,
                    lhsExpression,
                    equalsKeyword,
                    rhsExpression);
        }
    }
}
