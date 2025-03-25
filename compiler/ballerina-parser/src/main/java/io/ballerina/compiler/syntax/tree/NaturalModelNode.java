/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.13.0
 */
public class NaturalModelNode extends NonTerminalNode {

    public NaturalModelNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token modelKeyword() {
        return childInBucket(0);
    }

    public Token openParenthesis() {
        return childInBucket(1);
    }

    public ExpressionNode expression() {
        return childInBucket(2);
    }

    public Token closeParenthesis() {
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
                "modelKeyword",
                "openParenthesis",
                "expression",
                "closeParenthesis"};
    }

    public NaturalModelNode modify(
            Token modelKeyword,
            Token openParenthesis,
            ExpressionNode expression,
            Token closeParenthesis) {
        if (checkForReferenceEquality(
                modelKeyword,
                openParenthesis,
                expression,
                closeParenthesis)) {
            return this;
        }

        return NodeFactory.createNaturalModelNode(
                modelKeyword,
                openParenthesis,
                expression,
                closeParenthesis);
    }

    public NaturalModelNodeModifier modify() {
        return new NaturalModelNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.13.0
     */
    public static class NaturalModelNodeModifier {
        private final NaturalModelNode oldNode;
        private Token modelKeyword;
        private Token openParenthesis;
        private ExpressionNode expression;
        private Token closeParenthesis;

        public NaturalModelNodeModifier(NaturalModelNode oldNode) {
            this.oldNode = oldNode;
            this.modelKeyword = oldNode.modelKeyword();
            this.openParenthesis = oldNode.openParenthesis();
            this.expression = oldNode.expression();
            this.closeParenthesis = oldNode.closeParenthesis();
        }

        public NaturalModelNodeModifier withModelKeyword(
                Token modelKeyword) {
            Objects.requireNonNull(modelKeyword, "modelKeyword must not be null");
            this.modelKeyword = modelKeyword;
            return this;
        }

        public NaturalModelNodeModifier withOpenParenthesis(
                Token openParenthesis) {
            Objects.requireNonNull(openParenthesis, "openParenthesis must not be null");
            this.openParenthesis = openParenthesis;
            return this;
        }

        public NaturalModelNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public NaturalModelNodeModifier withCloseParenthesis(
                Token closeParenthesis) {
            Objects.requireNonNull(closeParenthesis, "closeParenthesis must not be null");
            this.closeParenthesis = closeParenthesis;
            return this;
        }

        public NaturalModelNode apply() {
            return oldNode.modify(
                    modelKeyword,
                    openParenthesis,
                    expression,
                    closeParenthesis);
        }
    }
}
