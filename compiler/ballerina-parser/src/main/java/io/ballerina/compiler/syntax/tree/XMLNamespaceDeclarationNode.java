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
public class XMLNamespaceDeclarationNode extends StatementNode {

    public XMLNamespaceDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token xmlnsKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode namespaceuri() {
        return childInBucket(1);
    }

    public Optional<Token> asKeyword() {
        return optionalChildInBucket(2);
    }

    public Optional<IdentifierToken> namespacePrefix() {
        return optionalChildInBucket(3);
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
                "xmlnsKeyword",
                "namespaceuri",
                "asKeyword",
                "namespacePrefix",
                "semicolonToken"};
    }

    public XMLNamespaceDeclarationNode modify(
            Token xmlnsKeyword,
            ExpressionNode namespaceuri,
            Token asKeyword,
            IdentifierToken namespacePrefix,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createXMLNamespaceDeclarationNode(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken);
    }

    public XMLNamespaceDeclarationNodeModifier modify() {
        return new XMLNamespaceDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLNamespaceDeclarationNodeModifier {
        private final XMLNamespaceDeclarationNode oldNode;
        private Token xmlnsKeyword;
        private ExpressionNode namespaceuri;
        private Token asKeyword;
        private IdentifierToken namespacePrefix;
        private Token semicolonToken;

        public XMLNamespaceDeclarationNodeModifier(XMLNamespaceDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.xmlnsKeyword = oldNode.xmlnsKeyword();
            this.namespaceuri = oldNode.namespaceuri();
            this.asKeyword = oldNode.asKeyword().orElse(null);
            this.namespacePrefix = oldNode.namespacePrefix().orElse(null);
            this.semicolonToken = oldNode.semicolonToken();
        }

        public XMLNamespaceDeclarationNodeModifier withXmlnsKeyword(
                Token xmlnsKeyword) {
            Objects.requireNonNull(xmlnsKeyword, "xmlnsKeyword must not be null");
            this.xmlnsKeyword = xmlnsKeyword;
            return this;
        }

        public XMLNamespaceDeclarationNodeModifier withNamespaceuri(
                ExpressionNode namespaceuri) {
            Objects.requireNonNull(namespaceuri, "namespaceuri must not be null");
            this.namespaceuri = namespaceuri;
            return this;
        }

        public XMLNamespaceDeclarationNodeModifier withAsKeyword(
                Token asKeyword) {
            Objects.requireNonNull(asKeyword, "asKeyword must not be null");
            this.asKeyword = asKeyword;
            return this;
        }

        public XMLNamespaceDeclarationNodeModifier withNamespacePrefix(
                IdentifierToken namespacePrefix) {
            Objects.requireNonNull(namespacePrefix, "namespacePrefix must not be null");
            this.namespacePrefix = namespacePrefix;
            return this;
        }

        public XMLNamespaceDeclarationNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public XMLNamespaceDeclarationNode apply() {
            return oldNode.modify(
                    xmlnsKeyword,
                    namespaceuri,
                    asKeyword,
                    namespacePrefix,
                    semicolonToken);
        }
    }
}
