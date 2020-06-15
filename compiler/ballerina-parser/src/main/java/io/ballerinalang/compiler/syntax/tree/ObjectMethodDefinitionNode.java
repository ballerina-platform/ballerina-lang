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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ObjectMethodDefinitionNode extends NonTerminalNode {

    public ObjectMethodDefinitionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public MetadataNode metadata() {
        return childInBucket(0);
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(1);
    }

    public Optional<Token> remoteKeyword() {
        return optionalChildInBucket(2);
    }

    public Optional<Token> transactionalKeyword() {
        return optionalChildInBucket(3);
    }

    public Token functionKeyword() {
        return childInBucket(4);
    }

    public IdentifierToken methodName() {
        return childInBucket(5);
    }

    public FunctionSignatureNode methodSignature() {
        return childInBucket(6);
    }

    public FunctionBodyNode functionBody() {
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
                "remoteKeyword",
                "transactionalKeyword",
                "functionKeyword",
                "methodName",
                "methodSignature",
                "functionBody"};
    }

    public ObjectMethodDefinitionNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token remoteKeyword,
            Token transactionalKeyword,
            Token functionKeyword,
            IdentifierToken methodName,
            FunctionSignatureNode methodSignature,
            FunctionBodyNode functionBody) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                remoteKeyword,
                transactionalKeyword,
                functionKeyword,
                methodName,
                methodSignature,
                functionBody)) {
            return this;
        }

        return NodeFactory.createObjectMethodDefinitionNode(
                metadata,
                visibilityQualifier,
                remoteKeyword,
                transactionalKeyword,
                functionKeyword,
                methodName,
                methodSignature,
                functionBody);
    }

    public ObjectMethodDefinitionNodeModifier modify() {
        return new ObjectMethodDefinitionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ObjectMethodDefinitionNodeModifier {
        private final ObjectMethodDefinitionNode oldNode;
        private MetadataNode metadata;
        private Token visibilityQualifier;
        private Token remoteKeyword;
        private Token transactionalKeyword;
        private Token functionKeyword;
        private IdentifierToken methodName;
        private FunctionSignatureNode methodSignature;
        private FunctionBodyNode functionBody;

        public ObjectMethodDefinitionNodeModifier(ObjectMethodDefinitionNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata();
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.remoteKeyword = oldNode.remoteKeyword().orElse(null);
            this.transactionalKeyword = oldNode.transactionalKeyword().orElse(null);
            this.functionKeyword = oldNode.functionKeyword();
            this.methodName = oldNode.methodName();
            this.methodSignature = oldNode.methodSignature();
            this.functionBody = oldNode.functionBody();
        }

        public ObjectMethodDefinitionNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public ObjectMethodDefinitionNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public ObjectMethodDefinitionNodeModifier withRemoteKeyword(
                Token remoteKeyword) {
            Objects.requireNonNull(remoteKeyword, "remoteKeyword must not be null");
            this.remoteKeyword = remoteKeyword;
            return this;
        }

        public ObjectMethodDefinitionNodeModifier withTransactionalKeyword(
                Token transactionalKeyword) {
            Objects.requireNonNull(transactionalKeyword, "transactionalKeyword must not be null");
            this.transactionalKeyword = transactionalKeyword;
            return this;
        }

        public ObjectMethodDefinitionNodeModifier withFunctionKeyword(
                Token functionKeyword) {
            Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
            this.functionKeyword = functionKeyword;
            return this;
        }

        public ObjectMethodDefinitionNodeModifier withMethodName(
                IdentifierToken methodName) {
            Objects.requireNonNull(methodName, "methodName must not be null");
            this.methodName = methodName;
            return this;
        }

        public ObjectMethodDefinitionNodeModifier withMethodSignature(
                FunctionSignatureNode methodSignature) {
            Objects.requireNonNull(methodSignature, "methodSignature must not be null");
            this.methodSignature = methodSignature;
            return this;
        }

        public ObjectMethodDefinitionNodeModifier withFunctionBody(
                FunctionBodyNode functionBody) {
            Objects.requireNonNull(functionBody, "functionBody must not be null");
            this.functionBody = functionBody;
            return this;
        }

        public ObjectMethodDefinitionNode apply() {
            return oldNode.modify(
                    metadata,
                    visibilityQualifier,
                    remoteKeyword,
                    transactionalKeyword,
                    functionKeyword,
                    methodName,
                    methodSignature,
                    functionBody);
        }
    }
}
