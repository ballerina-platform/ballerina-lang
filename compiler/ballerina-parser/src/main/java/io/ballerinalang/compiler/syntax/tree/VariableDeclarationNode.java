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
public class VariableDeclarationNode extends StatementNode {

    public VariableDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public Optional<Token> finalKeyword() {
        return optionalChildInBucket(1);
    }

    public TypedBindingPatternNode typedBindingPattern() {
        return childInBucket(2);
    }

    public Optional<Token> equalsToken() {
        return optionalChildInBucket(3);
    }

    public Optional<ExpressionNode> initializer() {
        return optionalChildInBucket(4);
    }

    public Token semicolonToken() {
        return childInBucket(5);
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
                "finalKeyword",
                "typedBindingPattern",
                "equalsToken",
                "initializer",
                "semicolonToken"};
    }

    public VariableDeclarationNode modify(
            NodeList<AnnotationNode> annotations,
            Token finalKeyword,
            TypedBindingPatternNode typedBindingPattern,
            Token equalsToken,
            ExpressionNode initializer,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createVariableDeclarationNode(
                annotations,
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public VariableDeclarationNodeModifier modify() {
        return new VariableDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class VariableDeclarationNodeModifier {
        private final VariableDeclarationNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private Token finalKeyword;
        private TypedBindingPatternNode typedBindingPattern;
        private Token equalsToken;
        private ExpressionNode initializer;
        private Token semicolonToken;

        public VariableDeclarationNodeModifier(VariableDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.finalKeyword = oldNode.finalKeyword().orElse(null);
            this.typedBindingPattern = oldNode.typedBindingPattern();
            this.equalsToken = oldNode.equalsToken().orElse(null);
            this.initializer = oldNode.initializer().orElse(null);
            this.semicolonToken = oldNode.semicolonToken();
        }

        public VariableDeclarationNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public VariableDeclarationNodeModifier withFinalKeyword(
                Token finalKeyword) {
            Objects.requireNonNull(finalKeyword, "finalKeyword must not be null");
            this.finalKeyword = finalKeyword;
            return this;
        }

        public VariableDeclarationNodeModifier withTypedBindingPattern(
                TypedBindingPatternNode typedBindingPattern) {
            Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
            this.typedBindingPattern = typedBindingPattern;
            return this;
        }

        public VariableDeclarationNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public VariableDeclarationNodeModifier withInitializer(
                ExpressionNode initializer) {
            Objects.requireNonNull(initializer, "initializer must not be null");
            this.initializer = initializer;
            return this;
        }

        public VariableDeclarationNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public VariableDeclarationNode apply() {
            return oldNode.modify(
                    annotations,
                    finalKeyword,
                    typedBindingPattern,
                    equalsToken,
                    initializer,
                    semicolonToken);
        }
    }
}
