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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ForkStatementNode extends StatementNode {

    public ForkStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token forkKeyword() {
        return childInBucket(0);
    }

    public Token openBraceToken() {
        return childInBucket(1);
    }

    public NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations() {
        return new NodeList<>(childInBucket(2));
    }

    public Token closeBraceToken() {
        return childInBucket(3);
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
                "forkKeyword",
                "openBraceToken",
                "namedWorkerDeclarations",
                "closeBraceToken"};
    }

    public ForkStatementNode modify(
            Token forkKeyword,
            Token openBraceToken,
            NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                forkKeyword,
                openBraceToken,
                namedWorkerDeclarations.underlyingListNode(),
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createForkStatementNode(
                forkKeyword,
                openBraceToken,
                namedWorkerDeclarations,
                closeBraceToken);
    }

    public ForkStatementNodeModifier modify() {
        return new ForkStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ForkStatementNodeModifier {
        private final ForkStatementNode oldNode;
        private Token forkKeyword;
        private Token openBraceToken;
        private NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations;
        private Token closeBraceToken;

        public ForkStatementNodeModifier(ForkStatementNode oldNode) {
            this.oldNode = oldNode;
            this.forkKeyword = oldNode.forkKeyword();
            this.openBraceToken = oldNode.openBraceToken();
            this.namedWorkerDeclarations = oldNode.namedWorkerDeclarations();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public ForkStatementNodeModifier withForkKeyword(
                Token forkKeyword) {
            Objects.requireNonNull(forkKeyword, "forkKeyword must not be null");
            this.forkKeyword = forkKeyword;
            return this;
        }

        public ForkStatementNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public ForkStatementNodeModifier withNamedWorkerDeclarations(
                NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations) {
            Objects.requireNonNull(namedWorkerDeclarations, "namedWorkerDeclarations must not be null");
            this.namedWorkerDeclarations = namedWorkerDeclarations;
            return this;
        }

        public ForkStatementNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public ForkStatementNode apply() {
            return oldNode.modify(
                    forkKeyword,
                    openBraceToken,
                    namedWorkerDeclarations,
                    closeBraceToken);
        }
    }
}
