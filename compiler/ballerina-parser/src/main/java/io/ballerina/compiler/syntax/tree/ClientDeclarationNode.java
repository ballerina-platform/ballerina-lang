/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.3.0
 */
public class ClientDeclarationNode extends StatementNode {

    public ClientDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token clientKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode clientUri() {
        return childInBucket(1);
    }

    public Token asKeyword() {
        return childInBucket(2);
    }

    public IdentifierToken clientPrefix() {
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
                "clientKeyword",
                "clientUri",
                "asKeyword",
                "clientPrefix",
                "semicolonToken"};
    }

    public ClientDeclarationNode modify(
            Token clientKeyword,
            ExpressionNode clientUri,
            Token asKeyword,
            IdentifierToken clientPrefix,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                clientKeyword,
                clientUri,
                asKeyword,
                clientPrefix,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createClientDeclarationNode(
                clientKeyword,
                clientUri,
                asKeyword,
                clientPrefix,
                semicolonToken);
    }

    public ClientDeclarationNodeModifier modify() {
        return new ClientDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ClientDeclarationNodeModifier {
        private final ClientDeclarationNode oldNode;
        private Token clientKeyword;
        private ExpressionNode clientUri;
        private Token asKeyword;
        private IdentifierToken clientPrefix;
        private Token semicolonToken;

        public ClientDeclarationNodeModifier(ClientDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.clientKeyword = oldNode.clientKeyword();
            this.clientUri = oldNode.clientUri();
            this.asKeyword = oldNode.asKeyword();
            this.clientPrefix = oldNode.clientPrefix();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ClientDeclarationNodeModifier withClientKeyword(
                Token clientKeyword) {
            Objects.requireNonNull(clientKeyword, "clientKeyword must not be null");
            this.clientKeyword = clientKeyword;
            return this;
        }

        public ClientDeclarationNodeModifier withClientUri(
                ExpressionNode clientUri) {
            Objects.requireNonNull(clientUri, "clientUri must not be null");
            this.clientUri = clientUri;
            return this;
        }

        public ClientDeclarationNodeModifier withAsKeyword(
                Token asKeyword) {
            Objects.requireNonNull(asKeyword, "asKeyword must not be null");
            this.asKeyword = asKeyword;
            return this;
        }

        public ClientDeclarationNodeModifier withClientPrefix(
                IdentifierToken clientPrefix) {
            Objects.requireNonNull(clientPrefix, "clientPrefix must not be null");
            this.clientPrefix = clientPrefix;
            return this;
        }

        public ClientDeclarationNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ClientDeclarationNode apply() {
            return oldNode.modify(
                    clientKeyword,
                    clientUri,
                    asKeyword,
                    clientPrefix,
                    semicolonToken);
        }
    }
}
