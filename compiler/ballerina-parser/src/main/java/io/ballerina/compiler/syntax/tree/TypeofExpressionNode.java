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
public class TypeofExpressionNode extends ExpressionNode {

    public TypeofExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token typeofKeyword() {
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
                "typeofKeyword",
                "expression"};
    }

    public TypeofExpressionNode modify(
            Token typeofKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                typeofKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createTypeofExpressionNode(
                typeofKeyword,
                expression);
    }

    public TypeofExpressionNodeModifier modify() {
        return new TypeofExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TypeofExpressionNodeModifier {
        private final TypeofExpressionNode oldNode;
        private Token typeofKeyword;
        private ExpressionNode expression;

        public TypeofExpressionNodeModifier(TypeofExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.typeofKeyword = oldNode.typeofKeyword();
            this.expression = oldNode.expression();
        }

        public TypeofExpressionNodeModifier withTypeofKeyword(
                Token typeofKeyword) {
            Objects.requireNonNull(typeofKeyword, "typeofKeyword must not be null");
            this.typeofKeyword = typeofKeyword;
            return this;
        }

        public TypeofExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public TypeofExpressionNode apply() {
            return oldNode.modify(
                    typeofKeyword,
                    expression);
        }
    }
}
