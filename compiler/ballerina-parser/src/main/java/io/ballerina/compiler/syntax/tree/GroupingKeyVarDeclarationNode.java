/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.4.0
 */
public class GroupingKeyVarDeclarationNode extends NonTerminalNode {

    public GroupingKeyVarDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public TypeDescriptorNode typeDescriptor() {
        return childInBucket(0);
    }

    public BindingPatternNode simpleBindingPattern() {
        return childInBucket(1);
    }

    public Token equalsToken() {
        return childInBucket(2);
    }

    public ExpressionNode expression() {
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
                "typeDescriptor",
                "simpleBindingPattern",
                "equalsToken",
                "expression"};
    }

    public GroupingKeyVarDeclarationNode modify(
            TypeDescriptorNode typeDescriptor,
            BindingPatternNode simpleBindingPattern,
            Token equalsToken,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                typeDescriptor,
                simpleBindingPattern,
                equalsToken,
                expression)) {
            return this;
        }

        return NodeFactory.createGroupingKeyVarDeclarationNode(
                typeDescriptor,
                simpleBindingPattern,
                equalsToken,
                expression);
    }

    public GroupingKeyVarDeclarationNodeModifier modify() {
        return new GroupingKeyVarDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class GroupingKeyVarDeclarationNodeModifier {
        private final GroupingKeyVarDeclarationNode oldNode;
        private TypeDescriptorNode typeDescriptor;
        private BindingPatternNode simpleBindingPattern;
        private Token equalsToken;
        private ExpressionNode expression;

        public GroupingKeyVarDeclarationNodeModifier(GroupingKeyVarDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.typeDescriptor = oldNode.typeDescriptor();
            this.simpleBindingPattern = oldNode.simpleBindingPattern();
            this.equalsToken = oldNode.equalsToken();
            this.expression = oldNode.expression();
        }

        public GroupingKeyVarDeclarationNodeModifier withTypeDescriptor(
                TypeDescriptorNode typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public GroupingKeyVarDeclarationNodeModifier withSimpleBindingPattern(
                BindingPatternNode simpleBindingPattern) {
            Objects.requireNonNull(simpleBindingPattern, "simpleBindingPattern must not be null");
            this.simpleBindingPattern = simpleBindingPattern;
            return this;
        }

        public GroupingKeyVarDeclarationNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public GroupingKeyVarDeclarationNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public GroupingKeyVarDeclarationNode apply() {
            return oldNode.modify(
                    typeDescriptor,
                    simpleBindingPattern,
                    equalsToken,
                    expression);
        }
    }
}
