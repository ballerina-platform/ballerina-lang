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
public class ObjectConstructorExpressionNode extends ExpressionNode {

    public ObjectConstructorExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public NodeList<Token> objectTypeQualifiers() {
        return new NodeList<>(childInBucket(1));
    }

    public Token objectKeyword() {
        return childInBucket(2);
    }

    public Optional<TypeDescriptorNode> typeReference() {
        return optionalChildInBucket(3);
    }

    public Token openBraceToken() {
        return childInBucket(4);
    }

    public NodeList<Node> members() {
        return new NodeList<>(childInBucket(5));
    }

    public Token closeBraceToken() {
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
                "annotations",
                "objectTypeQualifiers",
                "objectKeyword",
                "typeReference",
                "openBraceToken",
                "members",
                "closeBraceToken"};
    }

    public ObjectConstructorExpressionNode modify(
            NodeList<AnnotationNode> annotations,
            NodeList<Token> objectTypeQualifiers,
            Token objectKeyword,
            TypeDescriptorNode typeReference,
            Token openBraceToken,
            NodeList<Node> members,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                objectTypeQualifiers.underlyingListNode(),
                objectKeyword,
                typeReference,
                openBraceToken,
                members.underlyingListNode(),
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createObjectConstructorExpressionNode(
                annotations,
                objectTypeQualifiers,
                objectKeyword,
                typeReference,
                openBraceToken,
                members,
                closeBraceToken);
    }

    public ObjectConstructorExpressionNodeModifier modify() {
        return new ObjectConstructorExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ObjectConstructorExpressionNodeModifier {
        private final ObjectConstructorExpressionNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private NodeList<Token> objectTypeQualifiers;
        private Token objectKeyword;
        private TypeDescriptorNode typeReference;
        private Token openBraceToken;
        private NodeList<Node> members;
        private Token closeBraceToken;

        public ObjectConstructorExpressionNodeModifier(ObjectConstructorExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.objectTypeQualifiers = oldNode.objectTypeQualifiers();
            this.objectKeyword = oldNode.objectKeyword();
            this.typeReference = oldNode.typeReference().orElse(null);
            this.openBraceToken = oldNode.openBraceToken();
            this.members = oldNode.members();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public ObjectConstructorExpressionNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public ObjectConstructorExpressionNodeModifier withObjectTypeQualifiers(
                NodeList<Token> objectTypeQualifiers) {
            Objects.requireNonNull(objectTypeQualifiers, "objectTypeQualifiers must not be null");
            this.objectTypeQualifiers = objectTypeQualifiers;
            return this;
        }

        public ObjectConstructorExpressionNodeModifier withObjectKeyword(
                Token objectKeyword) {
            Objects.requireNonNull(objectKeyword, "objectKeyword must not be null");
            this.objectKeyword = objectKeyword;
            return this;
        }

        public ObjectConstructorExpressionNodeModifier withTypeReference(
                TypeDescriptorNode typeReference) {
            Objects.requireNonNull(typeReference, "typeReference must not be null");
            this.typeReference = typeReference;
            return this;
        }

        public ObjectConstructorExpressionNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public ObjectConstructorExpressionNodeModifier withMembers(
                NodeList<Node> members) {
            Objects.requireNonNull(members, "members must not be null");
            this.members = members;
            return this;
        }

        public ObjectConstructorExpressionNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public ObjectConstructorExpressionNode apply() {
            return oldNode.modify(
                    annotations,
                    objectTypeQualifiers,
                    objectKeyword,
                    typeReference,
                    openBraceToken,
                    members,
                    closeBraceToken);
        }
    }
}
