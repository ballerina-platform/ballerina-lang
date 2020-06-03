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
public class PositionalArgumentNode extends FunctionArgumentNode {

    public PositionalArgumentNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> leadingComma() {
        return optionalChildInBucket(0);
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
                "leadingComma",
                "expression"};
    }

    public PositionalArgumentNode modify(
            Token leadingComma,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                leadingComma,
                expression)) {
            return this;
        }

        return NodeFactory.createPositionalArgumentNode(
                leadingComma,
                expression);
    }

    public PositionalArgumentNodeModifier modify() {
        return new PositionalArgumentNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class PositionalArgumentNodeModifier {
        private final PositionalArgumentNode oldNode;
        private Token leadingComma;
        private ExpressionNode expression;

        public PositionalArgumentNodeModifier(PositionalArgumentNode oldNode) {
            this.oldNode = oldNode;
            this.leadingComma = oldNode.leadingComma().orElse(null);
            this.expression = oldNode.expression();
        }

        public PositionalArgumentNodeModifier withLeadingComma(
                Token leadingComma) {
            Objects.requireNonNull(leadingComma, "leadingComma must not be null");
            this.leadingComma = leadingComma;
            return this;
        }

        public PositionalArgumentNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public PositionalArgumentNode apply() {
            return oldNode.modify(
                    leadingComma,
                    expression);
        }
    }
}
