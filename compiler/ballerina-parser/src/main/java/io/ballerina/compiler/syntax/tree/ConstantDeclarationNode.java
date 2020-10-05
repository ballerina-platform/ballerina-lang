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
public class ConstantDeclarationNode extends ModuleMemberDeclarationNode {

    public ConstantDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(1);
    }

    public Token constKeyword() {
        return childInBucket(2);
    }

    public Optional<TypeDescriptorNode> typeDescriptor() {
        return optionalChildInBucket(3);
    }

    public Token variableName() {
        return childInBucket(4);
    }

    public Token equalsToken() {
        return childInBucket(5);
    }

    public Node initializer() {
        return childInBucket(6);
    }

    public Token semicolonToken() {
        return childInBucket(7);
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
                "constKeyword",
                "typeDescriptor",
                "variableName",
                "equalsToken",
                "initializer",
                "semicolonToken"};
    }

    public ConstantDeclarationNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token constKeyword,
            TypeDescriptorNode typeDescriptor,
            Token variableName,
            Token equalsToken,
            Node initializer,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createConstantDeclarationNode(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public ConstantDeclarationNodeModifier modify() {
        return new ConstantDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ConstantDeclarationNodeModifier {
        private final ConstantDeclarationNode oldNode;
        private MetadataNode metadata;
        private Token visibilityQualifier;
        private Token constKeyword;
        private TypeDescriptorNode typeDescriptor;
        private Token variableName;
        private Token equalsToken;
        private Node initializer;
        private Token semicolonToken;

        public ConstantDeclarationNodeModifier(ConstantDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.constKeyword = oldNode.constKeyword();
            this.typeDescriptor = oldNode.typeDescriptor().orElse(null);
            this.variableName = oldNode.variableName();
            this.equalsToken = oldNode.equalsToken();
            this.initializer = oldNode.initializer();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ConstantDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public ConstantDeclarationNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public ConstantDeclarationNodeModifier withConstKeyword(
                Token constKeyword) {
            Objects.requireNonNull(constKeyword, "constKeyword must not be null");
            this.constKeyword = constKeyword;
            return this;
        }

        public ConstantDeclarationNodeModifier withTypeDescriptor(
                TypeDescriptorNode typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public ConstantDeclarationNodeModifier withVariableName(
                Token variableName) {
            Objects.requireNonNull(variableName, "variableName must not be null");
            this.variableName = variableName;
            return this;
        }

        public ConstantDeclarationNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public ConstantDeclarationNodeModifier withInitializer(
                Node initializer) {
            Objects.requireNonNull(initializer, "initializer must not be null");
            this.initializer = initializer;
            return this;
        }

        public ConstantDeclarationNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ConstantDeclarationNode apply() {
            return oldNode.modify(
                    metadata,
                    visibilityQualifier,
                    constKeyword,
                    typeDescriptor,
                    variableName,
                    equalsToken,
                    initializer,
                    semicolonToken);
        }
    }
}
