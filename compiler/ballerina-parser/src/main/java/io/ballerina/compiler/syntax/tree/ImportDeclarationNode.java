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

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ImportDeclarationNode extends NonTerminalNode {

    public ImportDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token importKeyword() {
        return childInBucket(0);
    }

    public Optional<ImportOrgNameNode> orgName() {
        return optionalChildInBucket(1);
    }

    public SeparatedNodeList<IdentifierToken> moduleName() {
        return new SeparatedNodeList<>(childInBucket(2));
    }

    public Optional<ImportPrefixNode> prefix() {
        return optionalChildInBucket(3);
    }

    public Token semicolon() {
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
                "importKeyword",
                "orgName",
                "moduleName",
                "prefix",
                "semicolon"};
    }

    public ImportDeclarationNode modify(
            Token importKeyword,
            ImportOrgNameNode orgName,
            SeparatedNodeList<IdentifierToken> moduleName,
            ImportPrefixNode prefix,
            Token semicolon) {
        if (checkForReferenceEquality(
                importKeyword,
                orgName,
                moduleName.underlyingListNode(),
                prefix,
                semicolon)) {
            return this;
        }

        return NodeFactory.createImportDeclarationNode(
                importKeyword,
                orgName,
                moduleName,
                prefix,
                semicolon);
    }

    public ImportDeclarationNodeModifier modify() {
        return new ImportDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ImportDeclarationNodeModifier {
        private final ImportDeclarationNode oldNode;
        private Token importKeyword;
        private ImportOrgNameNode orgName;
        private SeparatedNodeList<IdentifierToken> moduleName;
        private ImportPrefixNode prefix;
        private Token semicolon;

        public ImportDeclarationNodeModifier(ImportDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.importKeyword = oldNode.importKeyword();
            this.orgName = oldNode.orgName().orElse(null);
            this.moduleName = oldNode.moduleName();
            this.prefix = oldNode.prefix().orElse(null);
            this.semicolon = oldNode.semicolon();
        }

        public ImportDeclarationNodeModifier withImportKeyword(
                Token importKeyword) {
            Objects.requireNonNull(importKeyword, "importKeyword must not be null");
            this.importKeyword = importKeyword;
            return this;
        }

        public ImportDeclarationNodeModifier withOrgName(
                ImportOrgNameNode orgName) {
            this.orgName = orgName;
            return this;
        }

        public ImportDeclarationNodeModifier withModuleName(
                SeparatedNodeList<IdentifierToken> moduleName) {
            Objects.requireNonNull(moduleName, "moduleName must not be null");
            this.moduleName = moduleName;
            return this;
        }

        public ImportDeclarationNodeModifier withPrefix(
                ImportPrefixNode prefix) {
            this.prefix = prefix;
            return this;
        }

        public ImportDeclarationNodeModifier withSemicolon(
                Token semicolon) {
            Objects.requireNonNull(semicolon, "semicolon must not be null");
            this.semicolon = semicolon;
            return this;
        }

        public ImportDeclarationNode apply() {
            return oldNode.modify(
                    importKeyword,
                    orgName,
                    moduleName,
                    prefix,
                    semicolon);
        }
    }
}
