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
                "metadata",
                "finalKeyword",
                "typedBindingPattern",
                "equalsToken",
                "initializer",
                "semicolonToken"};
    }

    public ModuleVariableDeclarationNode modify(
            MetadataNode metadata,
            Token finalKeyword,
            TypedBindingPatternNode typedBindingPattern,
            Token equalsToken,
            ExpressionNode initializer,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createModuleVariableDeclarationNode(
                metadata,
                finalKeyword,
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
        private Token finalKeyword;
        private TypedBindingPatternNode typedBindingPattern;
        private Token equalsToken;
        private ExpressionNode initializer;
        private Token semicolonToken;

        public ModuleVariableDeclarationNodeModifier(ModuleVariableDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.finalKeyword = oldNode.finalKeyword().orElse(null);
            this.typedBindingPattern = oldNode.typedBindingPattern();
            this.equalsToken = oldNode.equalsToken().orElse(null);
            this.initializer = oldNode.initializer().orElse(null);
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ModuleVariableDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public ModuleVariableDeclarationNodeModifier withFinalKeyword(
                Token finalKeyword) {
            Objects.requireNonNull(finalKeyword, "finalKeyword must not be null");
            this.finalKeyword = finalKeyword;
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
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public ModuleVariableDeclarationNodeModifier withInitializer(
                ExpressionNode initializer) {
            Objects.requireNonNull(initializer, "initializer must not be null");
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
                    finalKeyword,
                    typedBindingPattern,
                    equalsToken,
                    initializer,
                    semicolonToken);
        }
    }
}
