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
public class MethodDeclarationNode extends NonTerminalNode {

    public MethodDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public NodeList<Token> qualifierList() {
        return new NodeList<>(childInBucket(1));
    }

    public Token functionKeyword() {
        return childInBucket(2);
    }

    public IdentifierToken methodName() {
        return childInBucket(3);
    }

    public FunctionSignatureNode methodSignature() {
        return childInBucket(4);
    }

    public Token semicolon() {
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
                "qualifierList",
                "functionKeyword",
                "methodName",
                "methodSignature",
                "semicolon"};
    }

    public MethodDeclarationNode modify(
            MetadataNode metadata,
            NodeList<Token> qualifierList,
            Token functionKeyword,
            IdentifierToken methodName,
            FunctionSignatureNode methodSignature,
            Token semicolon) {
        if (checkForReferenceEquality(
                metadata,
                qualifierList.underlyingListNode(),
                functionKeyword,
                methodName,
                methodSignature,
                semicolon)) {
            return this;
        }

        return NodeFactory.createMethodDeclarationNode(
                metadata,
                qualifierList,
                functionKeyword,
                methodName,
                methodSignature,
                semicolon);
    }

    public MethodDeclarationNodeModifier modify() {
        return new MethodDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MethodDeclarationNodeModifier {
        private final MethodDeclarationNode oldNode;
        private MetadataNode metadata;
        private NodeList<Token> qualifierList;
        private Token functionKeyword;
        private IdentifierToken methodName;
        private FunctionSignatureNode methodSignature;
        private Token semicolon;

        public MethodDeclarationNodeModifier(MethodDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.qualifierList = oldNode.qualifierList();
            this.functionKeyword = oldNode.functionKeyword();
            this.methodName = oldNode.methodName();
            this.methodSignature = oldNode.methodSignature();
            this.semicolon = oldNode.semicolon();
        }

        public MethodDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public MethodDeclarationNodeModifier withQualifierList(
                NodeList<Token> qualifierList) {
            Objects.requireNonNull(qualifierList, "qualifierList must not be null");
            this.qualifierList = qualifierList;
            return this;
        }

        public MethodDeclarationNodeModifier withFunctionKeyword(
                Token functionKeyword) {
            Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
            this.functionKeyword = functionKeyword;
            return this;
        }

        public MethodDeclarationNodeModifier withMethodName(
                IdentifierToken methodName) {
            Objects.requireNonNull(methodName, "methodName must not be null");
            this.methodName = methodName;
            return this;
        }

        public MethodDeclarationNodeModifier withMethodSignature(
                FunctionSignatureNode methodSignature) {
            Objects.requireNonNull(methodSignature, "methodSignature must not be null");
            this.methodSignature = methodSignature;
            return this;
        }

        public MethodDeclarationNodeModifier withSemicolon(
                Token semicolon) {
            Objects.requireNonNull(semicolon, "semicolon must not be null");
            this.semicolon = semicolon;
            return this;
        }

        public MethodDeclarationNode apply() {
            return oldNode.modify(
                    metadata,
                    qualifierList,
                    functionKeyword,
                    methodName,
                    methodSignature,
                    semicolon);
        }
    }
}
