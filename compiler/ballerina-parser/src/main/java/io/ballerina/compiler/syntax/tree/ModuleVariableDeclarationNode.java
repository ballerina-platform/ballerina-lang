/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ModuleVariableDeclarationNode extends ModuleMemberDeclarationNode {

    public ModuleVariableDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(1);
    }

    public NodeList<Token> qualifiers() {
        return new NodeList<>(childInBucket(2));
    }

    public TypedBindingPatternNode typedBindingPattern() {
        return childInBucket(3);
    }

    public Optional<Token> equalsToken() {
        return optionalChildInBucket(4);
    }

    public Optional<ExpressionNode> initializer() {
        return optionalChildInBucket(5);
    }

    public Token semicolonToken() {
        return childInBucket(6);
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
                "metadata",
                "visibilityQualifier",
                "qualifiers",
                "typedBindingPattern",
                "equalsToken",
                "initializer",
                "semicolonToken"};
    }

    public ModuleVariableDeclarationNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            NodeList<Token> qualifiers,
            TypedBindingPatternNode typedBindingPattern,
            Token equalsToken,
            ExpressionNode initializer,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                qualifiers.underlyingListNode(),
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createModuleVariableDeclarationNode(
                metadata,
                visibilityQualifier,
                qualifiers,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public ModuleVariableDeclarationNodeModifier modify() {
        return new ModuleVariableDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ModuleVariableDeclarationNodeModifier {
        private final ModuleVariableDeclarationNode oldNode;
        private MetadataNode metadata;
        private Token visibilityQualifier;
        private NodeList<Token> qualifiers;
        private TypedBindingPatternNode typedBindingPattern;
        private Token equalsToken;
        private ExpressionNode initializer;
        private Token semicolonToken;

        public ModuleVariableDeclarationNodeModifier(ModuleVariableDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.qualifiers = oldNode.qualifiers();
            this.typedBindingPattern = oldNode.typedBindingPattern();
            this.equalsToken = oldNode.equalsToken().orElse(null);
            this.initializer = oldNode.initializer().orElse(null);
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ModuleVariableDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            this.metadata = metadata;
            return this;
        }

        public ModuleVariableDeclarationNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public ModuleVariableDeclarationNodeModifier withQualifiers(
                NodeList<Token> qualifiers) {
            Objects.requireNonNull(qualifiers, "qualifiers must not be null");
            this.qualifiers = qualifiers;
            return this;
        }

        public ModuleVariableDeclarationNodeModifier withTypedBindingPattern(
                TypedBindingPatternNode typedBindingPattern) {
            Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
            this.typedBindingPattern = typedBindingPattern;
            return this;
        }

        public ModuleVariableDeclarationNodeModifier withEqualsToken(
                Token equalsToken) {
            this.equalsToken = equalsToken;
            return this;
        }

        public ModuleVariableDeclarationNodeModifier withInitializer(
                ExpressionNode initializer) {
            this.initializer = initializer;
            return this;
        }

        public ModuleVariableDeclarationNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ModuleVariableDeclarationNode apply() {
            return oldNode.modify(
                    metadata,
                    visibilityQualifier,
                    qualifiers,
                    typedBindingPattern,
                    equalsToken,
                    initializer,
                    semicolonToken);
        }
    }
}
