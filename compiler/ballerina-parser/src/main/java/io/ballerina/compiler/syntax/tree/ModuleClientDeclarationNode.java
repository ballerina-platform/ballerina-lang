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
public class ModuleClientDeclarationNode extends ModuleMemberDeclarationNode {

    public ModuleClientDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token clientKeyword() {
        return childInBucket(0);
    }

    public BasicLiteralNode clientUri() {
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

    public ModuleClientDeclarationNode modify(
            Token clientKeyword,
            BasicLiteralNode clientUri,
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

        return NodeFactory.createModuleClientDeclarationNode(
                clientKeyword,
                clientUri,
                asKeyword,
                clientPrefix,
                semicolonToken);
    }

    public ModuleClientDeclarationNodeModifier modify() {
        return new ModuleClientDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ModuleClientDeclarationNodeModifier {
        private final ModuleClientDeclarationNode oldNode;
        private Token clientKeyword;
        private BasicLiteralNode clientUri;
        private Token asKeyword;
        private IdentifierToken clientPrefix;
        private Token semicolonToken;

        public ModuleClientDeclarationNodeModifier(ModuleClientDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.clientKeyword = oldNode.clientKeyword();
            this.clientUri = oldNode.clientUri();
            this.asKeyword = oldNode.asKeyword();
            this.clientPrefix = oldNode.clientPrefix();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ModuleClientDeclarationNodeModifier withClientKeyword(
                Token clientKeyword) {
            Objects.requireNonNull(clientKeyword, "clientKeyword must not be null");
            this.clientKeyword = clientKeyword;
            return this;
        }

        public ModuleClientDeclarationNodeModifier withClientUri(
                BasicLiteralNode clientUri) {
            Objects.requireNonNull(clientUri, "clientUri must not be null");
            this.clientUri = clientUri;
            return this;
        }

        public ModuleClientDeclarationNodeModifier withAsKeyword(
                Token asKeyword) {
            Objects.requireNonNull(asKeyword, "asKeyword must not be null");
            this.asKeyword = asKeyword;
            return this;
        }

        public ModuleClientDeclarationNodeModifier withClientPrefix(
                IdentifierToken clientPrefix) {
            Objects.requireNonNull(clientPrefix, "clientPrefix must not be null");
            this.clientPrefix = clientPrefix;
            return this;
        }

        public ModuleClientDeclarationNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ModuleClientDeclarationNode apply() {
            return oldNode.modify(
                    clientKeyword,
                    clientUri,
                    asKeyword,
                    clientPrefix,
                    semicolonToken);
        }
    }
}
