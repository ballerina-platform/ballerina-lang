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
public class EnumDeclarationNode extends ModuleMemberDeclarationNode {

    public EnumDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public Optional<Token> qualifier() {
        return optionalChildInBucket(1);
    }

    public Token enumKeywordToken() {
        return childInBucket(2);
    }

    public IdentifierToken identifier() {
        return childInBucket(3);
    }

    public Token openBraceToken() {
        return childInBucket(4);
    }

    public SeparatedNodeList<Node> enumMemberList() {
        return new SeparatedNodeList<>(childInBucket(5));
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
                "metadata",
                "qualifier",
                "enumKeywordToken",
                "identifier",
                "openBraceToken",
                "enumMemberList",
                "closeBraceToken"};
    }

    public EnumDeclarationNode modify(
            MetadataNode metadata,
            Token qualifier,
            Token enumKeywordToken,
            IdentifierToken identifier,
            Token openBraceToken,
            SeparatedNodeList<Node> enumMemberList,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList.underlyingListNode(),
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createEnumDeclarationNode(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList,
                closeBraceToken);
    }

    public EnumDeclarationNodeModifier modify() {
        return new EnumDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class EnumDeclarationNodeModifier {
        private final EnumDeclarationNode oldNode;
        private MetadataNode metadata;
        private Token qualifier;
        private Token enumKeywordToken;
        private IdentifierToken identifier;
        private Token openBraceToken;
        private SeparatedNodeList<Node> enumMemberList;
        private Token closeBraceToken;

        public EnumDeclarationNodeModifier(EnumDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.qualifier = oldNode.qualifier().orElse(null);
            this.enumKeywordToken = oldNode.enumKeywordToken();
            this.identifier = oldNode.identifier();
            this.openBraceToken = oldNode.openBraceToken();
            this.enumMemberList = oldNode.enumMemberList();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public EnumDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public EnumDeclarationNodeModifier withQualifier(
                Token qualifier) {
            Objects.requireNonNull(qualifier, "qualifier must not be null");
            this.qualifier = qualifier;
            return this;
        }

        public EnumDeclarationNodeModifier withEnumKeywordToken(
                Token enumKeywordToken) {
            Objects.requireNonNull(enumKeywordToken, "enumKeywordToken must not be null");
            this.enumKeywordToken = enumKeywordToken;
            return this;
        }

        public EnumDeclarationNodeModifier withIdentifier(
                IdentifierToken identifier) {
            Objects.requireNonNull(identifier, "identifier must not be null");
            this.identifier = identifier;
            return this;
        }

        public EnumDeclarationNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public EnumDeclarationNodeModifier withEnumMemberList(
                SeparatedNodeList<Node> enumMemberList) {
            Objects.requireNonNull(enumMemberList, "enumMemberList must not be null");
            this.enumMemberList = enumMemberList;
            return this;
        }

        public EnumDeclarationNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public EnumDeclarationNode apply() {
            return oldNode.modify(
                    metadata,
                    qualifier,
                    enumKeywordToken,
                    identifier,
                    openBraceToken,
                    enumMemberList,
                    closeBraceToken);
        }
    }
}
