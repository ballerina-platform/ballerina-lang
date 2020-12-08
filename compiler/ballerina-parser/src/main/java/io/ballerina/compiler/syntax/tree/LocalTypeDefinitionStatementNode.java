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
public class LocalTypeDefinitionStatementNode extends StatementNode {

    public LocalTypeDefinitionStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public Token typeKeyword() {
        return childInBucket(1);
    }

    public Node typeName() {
        return childInBucket(2);
    }

    public Node typeDescriptor() {
        return childInBucket(3);
    }

    public Token semicolonToken() {
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
                "annotations",
                "typeKeyword",
                "typeName",
                "typeDescriptor",
                "semicolonToken"};
    }

    public LocalTypeDefinitionStatementNode modify(
            NodeList<AnnotationNode> annotations,
            Token typeKeyword,
            Node typeName,
            Node typeDescriptor,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createLocalTypeDefinitionStatementNode(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    public LocalTypeDefinitionStatementNodeModifier modify() {
        return new LocalTypeDefinitionStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class LocalTypeDefinitionStatementNodeModifier {
        private final LocalTypeDefinitionStatementNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private Token typeKeyword;
        private Node typeName;
        private Node typeDescriptor;
        private Token semicolonToken;

        public LocalTypeDefinitionStatementNodeModifier(LocalTypeDefinitionStatementNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.typeKeyword = oldNode.typeKeyword();
            this.typeName = oldNode.typeName();
            this.typeDescriptor = oldNode.typeDescriptor();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public LocalTypeDefinitionStatementNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public LocalTypeDefinitionStatementNodeModifier withTypeKeyword(
                Token typeKeyword) {
            Objects.requireNonNull(typeKeyword, "typeKeyword must not be null");
            this.typeKeyword = typeKeyword;
            return this;
        }

        public LocalTypeDefinitionStatementNodeModifier withTypeName(
                Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public LocalTypeDefinitionStatementNodeModifier withTypeDescriptor(
                Node typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public LocalTypeDefinitionStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public LocalTypeDefinitionStatementNode apply() {
            return oldNode.modify(
                    annotations,
                    typeKeyword,
                    typeName,
                    typeDescriptor,
                    semicolonToken);
        }
    }
}
