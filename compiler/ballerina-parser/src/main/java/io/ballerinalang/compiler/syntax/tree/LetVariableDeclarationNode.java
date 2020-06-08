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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class LetVariableDeclarationNode extends NonTerminalNode {

    public LetVariableDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public TypedBindingPatternNode typedBindingPattern() {
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
                "annotations",
                "typedBindingPattern",
                "equalsToken",
                "expression"};
    }

    public LetVariableDeclarationNode modify(
            NodeList<AnnotationNode> annotations,
            TypedBindingPatternNode typedBindingPattern,
            Token equalsToken,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                typedBindingPattern,
                equalsToken,
                expression)) {
            return this;
        }

        return NodeFactory.createLetVariableDeclarationNode(
                annotations,
                typedBindingPattern,
                equalsToken,
                expression);
    }

    public LetVariableDeclarationNodeModifier modify() {
        return new LetVariableDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class LetVariableDeclarationNodeModifier {
        private final LetVariableDeclarationNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private TypedBindingPatternNode typedBindingPattern;
        private Token equalsToken;
        private ExpressionNode expression;

        public LetVariableDeclarationNodeModifier(LetVariableDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.typedBindingPattern = oldNode.typedBindingPattern();
            this.equalsToken = oldNode.equalsToken();
            this.expression = oldNode.expression();
        }

        public LetVariableDeclarationNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public LetVariableDeclarationNodeModifier withTypedBindingPattern(
                TypedBindingPatternNode typedBindingPattern) {
            Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
            this.typedBindingPattern = typedBindingPattern;
            return this;
        }

        public LetVariableDeclarationNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public LetVariableDeclarationNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public LetVariableDeclarationNode apply() {
            return oldNode.modify(
                    annotations,
                    typedBindingPattern,
                    equalsToken,
                    expression);
        }
    }
}
