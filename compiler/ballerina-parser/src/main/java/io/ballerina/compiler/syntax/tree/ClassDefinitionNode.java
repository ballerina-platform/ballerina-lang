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
public class ClassDefinitionNode extends ModuleMemberDeclarationNode {

    public ClassDefinitionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(1);
    }

    public NodeList<Token> classTypeQualifiers() {
        return new NodeList<>(childInBucket(2));
    }

    public Token classKeyword() {
        return childInBucket(3);
    }

    public Token className() {
        return childInBucket(4);
    }

    public Token openBrace() {
        return childInBucket(5);
    }

    public NodeList<Node> members() {
        return new NodeList<>(childInBucket(6));
    }

    public Token closeBrace() {
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
                "classTypeQualifiers",
                "classKeyword",
                "className",
                "openBrace",
                "members",
                "closeBrace"};
    }

    public ClassDefinitionNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            NodeList<Token> classTypeQualifiers,
            Token classKeyword,
            Token className,
            Token openBrace,
            NodeList<Node> members,
            Token closeBrace) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                classTypeQualifiers.underlyingListNode(),
                classKeyword,
                className,
                openBrace,
                members.underlyingListNode(),
                closeBrace)) {
            return this;
        }

        return NodeFactory.createClassDefinitionNode(
                metadata,
                visibilityQualifier,
                classTypeQualifiers,
                classKeyword,
                className,
                openBrace,
                members,
                closeBrace);
    }

    public ClassDefinitionNodeModifier modify() {
        return new ClassDefinitionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ClassDefinitionNodeModifier {
        private final ClassDefinitionNode oldNode;
        private MetadataNode metadata;
        private Token visibilityQualifier;
        private NodeList<Token> classTypeQualifiers;
        private Token classKeyword;
        private Token className;
        private Token openBrace;
        private NodeList<Node> members;
        private Token closeBrace;

        public ClassDefinitionNodeModifier(ClassDefinitionNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.classTypeQualifiers = oldNode.classTypeQualifiers();
            this.classKeyword = oldNode.classKeyword();
            this.className = oldNode.className();
            this.openBrace = oldNode.openBrace();
            this.members = oldNode.members();
            this.closeBrace = oldNode.closeBrace();
        }

        public ClassDefinitionNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public ClassDefinitionNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public ClassDefinitionNodeModifier withClassTypeQualifiers(
                NodeList<Token> classTypeQualifiers) {
            Objects.requireNonNull(classTypeQualifiers, "classTypeQualifiers must not be null");
            this.classTypeQualifiers = classTypeQualifiers;
            return this;
        }

        public ClassDefinitionNodeModifier withClassKeyword(
                Token classKeyword) {
            Objects.requireNonNull(classKeyword, "classKeyword must not be null");
            this.classKeyword = classKeyword;
            return this;
        }

        public ClassDefinitionNodeModifier withClassName(
                Token className) {
            Objects.requireNonNull(className, "className must not be null");
            this.className = className;
            return this;
        }

        public ClassDefinitionNodeModifier withOpenBrace(
                Token openBrace) {
            Objects.requireNonNull(openBrace, "openBrace must not be null");
            this.openBrace = openBrace;
            return this;
        }

        public ClassDefinitionNodeModifier withMembers(
                NodeList<Node> members) {
            Objects.requireNonNull(members, "members must not be null");
            this.members = members;
            return this;
        }

        public ClassDefinitionNodeModifier withCloseBrace(
                Token closeBrace) {
            Objects.requireNonNull(closeBrace, "closeBrace must not be null");
            this.closeBrace = closeBrace;
            return this;
        }

        public ClassDefinitionNode apply() {
            return oldNode.modify(
                    metadata,
                    visibilityQualifier,
                    classTypeQualifiers,
                    classKeyword,
                    className,
                    openBrace,
                    members,
                    closeBrace);
        }
    }
}
