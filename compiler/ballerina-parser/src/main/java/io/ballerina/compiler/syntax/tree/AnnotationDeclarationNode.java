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
public class AnnotationDeclarationNode extends ModuleMemberDeclarationNode {

    public AnnotationDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(1);
    }

    public Optional<Token> constKeyword() {
        return optionalChildInBucket(2);
    }

    public Token annotationKeyword() {
        return childInBucket(3);
    }

    public Optional<Node> typeDescriptor() {
        return optionalChildInBucket(4);
    }

    public Token annotationTag() {
        return childInBucket(5);
    }

    public Optional<Token> onKeyword() {
        return optionalChildInBucket(6);
    }

    public SeparatedNodeList<Node> attachPoints() {
        return new SeparatedNodeList<>(childInBucket(7));
    }

    public Token semicolonToken() {
        return childInBucket(8);
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
                "annotationKeyword",
                "typeDescriptor",
                "annotationTag",
                "onKeyword",
                "attachPoints",
                "semicolonToken"};
    }

    public AnnotationDeclarationNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token constKeyword,
            Token annotationKeyword,
            Node typeDescriptor,
            Token annotationTag,
            Token onKeyword,
            SeparatedNodeList<Node> attachPoints,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                constKeyword,
                annotationKeyword,
                typeDescriptor,
                annotationTag,
                onKeyword,
                attachPoints.underlyingListNode(),
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createAnnotationDeclarationNode(
                metadata,
                visibilityQualifier,
                constKeyword,
                annotationKeyword,
                typeDescriptor,
                annotationTag,
                onKeyword,
                attachPoints,
                semicolonToken);
    }

    public AnnotationDeclarationNodeModifier modify() {
        return new AnnotationDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class AnnotationDeclarationNodeModifier {
        private final AnnotationDeclarationNode oldNode;
        private MetadataNode metadata;
        private Token visibilityQualifier;
        private Token constKeyword;
        private Token annotationKeyword;
        private Node typeDescriptor;
        private Token annotationTag;
        private Token onKeyword;
        private SeparatedNodeList<Node> attachPoints;
        private Token semicolonToken;

        public AnnotationDeclarationNodeModifier(AnnotationDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.constKeyword = oldNode.constKeyword().orElse(null);
            this.annotationKeyword = oldNode.annotationKeyword();
            this.typeDescriptor = oldNode.typeDescriptor().orElse(null);
            this.annotationTag = oldNode.annotationTag();
            this.onKeyword = oldNode.onKeyword().orElse(null);
            this.attachPoints = oldNode.attachPoints();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public AnnotationDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public AnnotationDeclarationNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public AnnotationDeclarationNodeModifier withConstKeyword(
                Token constKeyword) {
            Objects.requireNonNull(constKeyword, "constKeyword must not be null");
            this.constKeyword = constKeyword;
            return this;
        }

        public AnnotationDeclarationNodeModifier withAnnotationKeyword(
                Token annotationKeyword) {
            Objects.requireNonNull(annotationKeyword, "annotationKeyword must not be null");
            this.annotationKeyword = annotationKeyword;
            return this;
        }

        public AnnotationDeclarationNodeModifier withTypeDescriptor(
                Node typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public AnnotationDeclarationNodeModifier withAnnotationTag(
                Token annotationTag) {
            Objects.requireNonNull(annotationTag, "annotationTag must not be null");
            this.annotationTag = annotationTag;
            return this;
        }

        public AnnotationDeclarationNodeModifier withOnKeyword(
                Token onKeyword) {
            Objects.requireNonNull(onKeyword, "onKeyword must not be null");
            this.onKeyword = onKeyword;
            return this;
        }

        public AnnotationDeclarationNodeModifier withAttachPoints(
                SeparatedNodeList<Node> attachPoints) {
            Objects.requireNonNull(attachPoints, "attachPoints must not be null");
            this.attachPoints = attachPoints;
            return this;
        }

        public AnnotationDeclarationNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public AnnotationDeclarationNode apply() {
            return oldNode.modify(
                    metadata,
                    visibilityQualifier,
                    constKeyword,
                    annotationKeyword,
                    typeDescriptor,
                    annotationTag,
                    onKeyword,
                    attachPoints,
                    semicolonToken);
        }
    }
}
