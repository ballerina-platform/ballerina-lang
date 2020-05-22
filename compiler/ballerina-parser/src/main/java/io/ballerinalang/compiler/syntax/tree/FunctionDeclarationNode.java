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
public class FunctionDeclarationNode extends NonTerminalNode {

    public FunctionDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public MetadataNode metadata() {
        return childInBucket(0);
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(1);
    }

    public Token functionKeyword() {
        return childInBucket(2);
    }

    public IdentifierToken functionName() {
        return childInBucket(3);
    }

    public FunctionSignatureNode functionSignature() {
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
                "visibilityQualifier",
                "functionKeyword",
                "functionName",
                "functionSignature",
                "semicolon"};
    }

    public FunctionDeclarationNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token functionKeyword,
            IdentifierToken functionName,
            FunctionSignatureNode functionSignature,
            Token semicolon) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                functionKeyword,
                functionName,
                functionSignature,
                semicolon)) {
            return this;
        }

        return NodeFactory.createFunctionDeclarationNode(
                metadata,
                visibilityQualifier,
                functionKeyword,
                functionName,
                functionSignature,
                semicolon);
    }

    public FunctionDeclarationNodeModifier modify() {
        return new FunctionDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FunctionDeclarationNodeModifier {
        private final FunctionDeclarationNode oldNode;
        private MetadataNode metadata;
        private Token visibilityQualifier;
        private Token functionKeyword;
        private IdentifierToken functionName;
        private FunctionSignatureNode functionSignature;
        private Token semicolon;

        public FunctionDeclarationNodeModifier(FunctionDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata();
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.functionKeyword = oldNode.functionKeyword();
            this.functionName = oldNode.functionName();
            this.functionSignature = oldNode.functionSignature();
            this.semicolon = oldNode.semicolon();
        }

        public FunctionDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public FunctionDeclarationNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public FunctionDeclarationNodeModifier withFunctionKeyword(
                Token functionKeyword) {
            Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
            this.functionKeyword = functionKeyword;
            return this;
        }

        public FunctionDeclarationNodeModifier withFunctionName(
                IdentifierToken functionName) {
            Objects.requireNonNull(functionName, "functionName must not be null");
            this.functionName = functionName;
            return this;
        }

        public FunctionDeclarationNodeModifier withFunctionSignature(
                FunctionSignatureNode functionSignature) {
            Objects.requireNonNull(functionSignature, "functionSignature must not be null");
            this.functionSignature = functionSignature;
            return this;
        }

        public FunctionDeclarationNodeModifier withSemicolon(
                Token semicolon) {
            Objects.requireNonNull(semicolon, "semicolon must not be null");
            this.semicolon = semicolon;
            return this;
        }

        public FunctionDeclarationNode apply() {
            return oldNode.modify(
                    metadata,
                    visibilityQualifier,
                    functionKeyword,
                    functionName,
                    functionSignature,
                    semicolon);
        }
    }
}
