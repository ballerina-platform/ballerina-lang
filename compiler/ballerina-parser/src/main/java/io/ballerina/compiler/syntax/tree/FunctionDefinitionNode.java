/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class FunctionDefinitionNode extends ModuleMemberDeclarationNode {

    public FunctionDefinitionNode(STNode internalNode, int position, NonTerminalNode parent) {
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

    public IdentifierToken functionName() {
        return childInBucket(3);
    }

    public NodeList<Node> relativeResourcePath() {
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
                "functionName",
                "relativeResourcePath",
                "functionSignature",
                "functionBody"};
    }

    public FunctionDefinitionNode modify(
            SyntaxKind kind,
            MetadataNode metadata,
            NodeList<Token> qualifierList,
            Token functionKeyword,
            IdentifierToken functionName,
            NodeList<Node> relativeResourcePath,
            FunctionSignatureNode functionSignature,
            FunctionBodyNode functionBody) {
        if (checkForReferenceEquality(
                metadata,
                qualifierList.underlyingListNode(),
                functionKeyword,
                functionName,
                relativeResourcePath.underlyingListNode(),
                functionSignature,
                functionBody)) {
            return this;
        }

        return NodeFactory.createFunctionDefinitionNode(
                kind,
                metadata,
                qualifierList,
                functionKeyword,
                functionName,
                relativeResourcePath,
                functionSignature,
                functionBody);
    }

    public FunctionDefinitionNodeModifier modify() {
        return new FunctionDefinitionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FunctionDefinitionNodeModifier {
        private final FunctionDefinitionNode oldNode;
        @Nullable
        private MetadataNode metadata;
        private NodeList<Token> qualifierList;
        private Token functionKeyword;
        private IdentifierToken functionName;
        private NodeList<Node> relativeResourcePath;
        private FunctionSignatureNode functionSignature;
        private FunctionBodyNode functionBody;

        public FunctionDefinitionNodeModifier(FunctionDefinitionNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.qualifierList = oldNode.qualifierList();
            this.functionKeyword = oldNode.functionKeyword();
            this.functionName = oldNode.functionName();
            this.relativeResourcePath = oldNode.relativeResourcePath();
            this.functionSignature = oldNode.functionSignature();
            this.functionBody = oldNode.functionBody();
        }

        public FunctionDefinitionNodeModifier withMetadata(
                @Nullable MetadataNode metadata) {
            this.metadata = metadata;
            return this;
        }

        public FunctionDefinitionNodeModifier withQualifierList(
                NodeList<Token> qualifierList) {
            Objects.requireNonNull(qualifierList, "qualifierList must not be null");
            this.qualifierList = qualifierList;
            return this;
        }

        public FunctionDefinitionNodeModifier withFunctionKeyword(
                Token functionKeyword) {
            Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
            this.functionKeyword = functionKeyword;
            return this;
        }

        public FunctionDefinitionNodeModifier withFunctionName(
                IdentifierToken functionName) {
            Objects.requireNonNull(functionName, "functionName must not be null");
            this.functionName = functionName;
            return this;
        }

        public FunctionDefinitionNodeModifier withRelativeResourcePath(
                NodeList<Node> relativeResourcePath) {
            Objects.requireNonNull(relativeResourcePath, "relativeResourcePath must not be null");
            this.relativeResourcePath = relativeResourcePath;
            return this;
        }

        public FunctionDefinitionNodeModifier withFunctionSignature(
                FunctionSignatureNode functionSignature) {
            Objects.requireNonNull(functionSignature, "functionSignature must not be null");
            this.functionSignature = functionSignature;
            return this;
        }

        public FunctionDefinitionNodeModifier withFunctionBody(
                FunctionBodyNode functionBody) {
            Objects.requireNonNull(functionBody, "functionBody must not be null");
            this.functionBody = functionBody;
            return this;
        }

        public FunctionDefinitionNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    metadata,
                    qualifierList,
                    functionKeyword,
                    functionName,
                    relativeResourcePath,
                    functionSignature,
                    functionBody);
        }
    }
}
