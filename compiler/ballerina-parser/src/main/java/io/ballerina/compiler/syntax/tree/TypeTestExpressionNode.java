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
public class TypeTestExpressionNode extends ExpressionNode {

    public TypeTestExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Token isKeyword() {
        return childInBucket(1);
    }

    public Node typeDescriptor() {
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
                "expression",
                "isKeyword",
                "typeDescriptor"};
    }

    public TypeTestExpressionNode modify(
            ExpressionNode expression,
            Token isKeyword,
            Node typeDescriptor) {
        if (checkForReferenceEquality(
                expression,
                isKeyword,
                typeDescriptor)) {
            return this;
        }

        return NodeFactory.createTypeTestExpressionNode(
                expression,
                isKeyword,
                typeDescriptor);
    }

    public TypeTestExpressionNodeModifier modify() {
        return new TypeTestExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TypeTestExpressionNodeModifier {
        private final TypeTestExpressionNode oldNode;
        private ExpressionNode expression;
        private Token isKeyword;
        private Node typeDescriptor;

        public TypeTestExpressionNodeModifier(TypeTestExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.isKeyword = oldNode.isKeyword();
            this.typeDescriptor = oldNode.typeDescriptor();
        }

        public TypeTestExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public TypeTestExpressionNodeModifier withIsKeyword(
                Token isKeyword) {
            Objects.requireNonNull(isKeyword, "isKeyword must not be null");
            this.isKeyword = isKeyword;
            return this;
        }

        public TypeTestExpressionNodeModifier withTypeDescriptor(
                Node typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public TypeTestExpressionNode apply() {
            return oldNode.modify(
                    expression,
                    isKeyword,
                    typeDescriptor);
        }
    }
}
