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
public class TypeDefinitionNode extends ModuleMemberDeclarationNode {

    public TypeDefinitionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(1);
    }

    public Token typeKeyword() {
        return childInBucket(2);
    }

    public Token typeName() {
        return childInBucket(3);
    }

    public Node typeDescriptor() {
        return childInBucket(4);
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
                "visibilityQualifier",
                "typeKeyword",
                "typeName",
                "typeDescriptor",
                "semicolonToken"};
    }

    public TypeDefinitionNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token typeKeyword,
            Token typeName,
            Node typeDescriptor,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createTypeDefinitionNode(
                metadata,
                visibilityQualifier,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    public TypeDefinitionNodeModifier modify() {
        return new TypeDefinitionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TypeDefinitionNodeModifier {
        private final TypeDefinitionNode oldNode;
        private MetadataNode metadata;
        private Token visibilityQualifier;
        private Token typeKeyword;
        private Token typeName;
        private Node typeDescriptor;
        private Token semicolonToken;

        public TypeDefinitionNodeModifier(TypeDefinitionNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.typeKeyword = oldNode.typeKeyword();
            this.typeName = oldNode.typeName();
            this.typeDescriptor = oldNode.typeDescriptor();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public TypeDefinitionNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public TypeDefinitionNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public TypeDefinitionNodeModifier withTypeKeyword(
                Token typeKeyword) {
            Objects.requireNonNull(typeKeyword, "typeKeyword must not be null");
            this.typeKeyword = typeKeyword;
            return this;
        }

        public TypeDefinitionNodeModifier withTypeName(
                Token typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public TypeDefinitionNodeModifier withTypeDescriptor(
                Node typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public TypeDefinitionNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public TypeDefinitionNode apply() {
            return oldNode.modify(
                    metadata,
                    visibilityQualifier,
                    typeKeyword,
                    typeName,
                    typeDescriptor,
                    semicolonToken);
        }
    }
}
