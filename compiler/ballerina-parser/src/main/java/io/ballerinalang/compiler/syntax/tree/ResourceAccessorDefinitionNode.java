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
public class ResourceAccessorDefinitionNode extends NonTerminalNode {

    public ResourceAccessorDefinitionNode(STNode internalNode, int position, NonTerminalNode parent) {
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

    public IdentifierToken accessorName() {
        return childInBucket(3);
    }

    public NodeList<Token> relativeResourcePath() {
        return new NodeList<>(childInBucket(4));
    }

    public FunctionSignatureNode functionSignature() {
        return childInBucket(5);
    }

    public FunctionBodyNode functionBody() {
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
                "metadata",
                "qualifierList",
                "functionKeyword",
                "accessorName",
                "relativeResourcePath",
                "functionSignature",
                "functionBody"};
    }

    public ResourceAccessorDefinitionNode modify(
            MetadataNode metadata,
            NodeList<Token> qualifierList,
            Token functionKeyword,
            IdentifierToken accessorName,
            NodeList<Token> relativeResourcePath,
            FunctionSignatureNode functionSignature,
            FunctionBodyNode functionBody) {
        if (checkForReferenceEquality(
                metadata,
                qualifierList.underlyingListNode(),
                functionKeyword,
                accessorName,
                relativeResourcePath.underlyingListNode(),
                functionSignature,
                functionBody)) {
            return this;
        }

        return NodeFactory.createResourceAccessorDefinitionNode(
                metadata,
                qualifierList,
                functionKeyword,
                accessorName,
                relativeResourcePath,
                functionSignature,
                functionBody);
    }

    public ResourceAccessorDefinitionNodeModifier modify() {
        return new ResourceAccessorDefinitionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ResourceAccessorDefinitionNodeModifier {
        private final ResourceAccessorDefinitionNode oldNode;
        private MetadataNode metadata;
        private NodeList<Token> qualifierList;
        private Token functionKeyword;
        private IdentifierToken accessorName;
        private NodeList<Token> relativeResourcePath;
        private FunctionSignatureNode functionSignature;
        private FunctionBodyNode functionBody;

        public ResourceAccessorDefinitionNodeModifier(ResourceAccessorDefinitionNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.qualifierList = oldNode.qualifierList();
            this.functionKeyword = oldNode.functionKeyword();
            this.accessorName = oldNode.accessorName();
            this.relativeResourcePath = oldNode.relativeResourcePath();
            this.functionSignature = oldNode.functionSignature();
            this.functionBody = oldNode.functionBody();
        }

        public ResourceAccessorDefinitionNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public ResourceAccessorDefinitionNodeModifier withQualifierList(
                NodeList<Token> qualifierList) {
            Objects.requireNonNull(qualifierList, "qualifierList must not be null");
            this.qualifierList = qualifierList;
            return this;
        }

        public ResourceAccessorDefinitionNodeModifier withFunctionKeyword(
                Token functionKeyword) {
            Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
            this.functionKeyword = functionKeyword;
            return this;
        }

        public ResourceAccessorDefinitionNodeModifier withAccessorName(
                IdentifierToken accessorName) {
            Objects.requireNonNull(accessorName, "accessorName must not be null");
            this.accessorName = accessorName;
            return this;
        }

        public ResourceAccessorDefinitionNodeModifier withRelativeResourcePath(
                NodeList<Token> relativeResourcePath) {
            Objects.requireNonNull(relativeResourcePath, "relativeResourcePath must not be null");
            this.relativeResourcePath = relativeResourcePath;
            return this;
        }

        public ResourceAccessorDefinitionNodeModifier withFunctionSignature(
                FunctionSignatureNode functionSignature) {
            Objects.requireNonNull(functionSignature, "functionSignature must not be null");
            this.functionSignature = functionSignature;
            return this;
        }

        public ResourceAccessorDefinitionNodeModifier withFunctionBody(
                FunctionBodyNode functionBody) {
            Objects.requireNonNull(functionBody, "functionBody must not be null");
            this.functionBody = functionBody;
            return this;
        }

        public ResourceAccessorDefinitionNode apply() {
            return oldNode.modify(
                    metadata,
                    qualifierList,
                    functionKeyword,
                    accessorName,
                    relativeResourcePath,
                    functionSignature,
                    functionBody);
        }
    }
}
