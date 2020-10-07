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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ObjectTypeDescriptorNode extends TypeDescriptorNode {

    public ObjectTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<Token> objectTypeQualifiers() {
        return new NodeList<>(childInBucket(0));
    }

    public Token objectKeyword() {
        return childInBucket(1);
    }

    public Token openBrace() {
        return childInBucket(2);
    }

    public NodeList<Node> members() {
        return new NodeList<>(childInBucket(3));
    }

    public Token closeBrace() {
        return childInBucket(4);
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
                "objectTypeQualifiers",
                "objectKeyword",
                "openBrace",
                "members",
                "closeBrace"};
    }

    public ObjectTypeDescriptorNode modify(
            NodeList<Token> objectTypeQualifiers,
            Token objectKeyword,
            Token openBrace,
            NodeList<Node> members,
            Token closeBrace) {
        if (checkForReferenceEquality(
                objectTypeQualifiers.underlyingListNode(),
                objectKeyword,
                openBrace,
                members.underlyingListNode(),
                closeBrace)) {
            return this;
        }

        return NodeFactory.createObjectTypeDescriptorNode(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    public ObjectTypeDescriptorNodeModifier modify() {
        return new ObjectTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ObjectTypeDescriptorNodeModifier {
        private final ObjectTypeDescriptorNode oldNode;
        private NodeList<Token> objectTypeQualifiers;
        private Token objectKeyword;
        private Token openBrace;
        private NodeList<Node> members;
        private Token closeBrace;

        public ObjectTypeDescriptorNodeModifier(ObjectTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.objectTypeQualifiers = oldNode.objectTypeQualifiers();
            this.objectKeyword = oldNode.objectKeyword();
            this.openBrace = oldNode.openBrace();
            this.members = oldNode.members();
            this.closeBrace = oldNode.closeBrace();
        }

        public ObjectTypeDescriptorNodeModifier withObjectTypeQualifiers(
                NodeList<Token> objectTypeQualifiers) {
            Objects.requireNonNull(objectTypeQualifiers, "objectTypeQualifiers must not be null");
            this.objectTypeQualifiers = objectTypeQualifiers;
            return this;
        }

        public ObjectTypeDescriptorNodeModifier withObjectKeyword(
                Token objectKeyword) {
            Objects.requireNonNull(objectKeyword, "objectKeyword must not be null");
            this.objectKeyword = objectKeyword;
            return this;
        }

        public ObjectTypeDescriptorNodeModifier withOpenBrace(
                Token openBrace) {
            Objects.requireNonNull(openBrace, "openBrace must not be null");
            this.openBrace = openBrace;
            return this;
        }

        public ObjectTypeDescriptorNodeModifier withMembers(
                NodeList<Node> members) {
            Objects.requireNonNull(members, "members must not be null");
            this.members = members;
            return this;
        }

        public ObjectTypeDescriptorNodeModifier withCloseBrace(
                Token closeBrace) {
            Objects.requireNonNull(closeBrace, "closeBrace must not be null");
            this.closeBrace = closeBrace;
            return this;
        }

        public ObjectTypeDescriptorNode apply() {
            return oldNode.modify(
                    objectTypeQualifiers,
                    objectKeyword,
                    openBrace,
                    members,
                    closeBrace);
        }
    }
}
