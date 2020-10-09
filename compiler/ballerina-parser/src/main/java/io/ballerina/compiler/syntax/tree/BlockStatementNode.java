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
public class BlockStatementNode extends StatementNode {

    public BlockStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBraceToken() {
        return childInBucket(0);
    }

    public NodeList<StatementNode> statements() {
        return new NodeList<>(childInBucket(1));
    }

    public Token closeBraceToken() {
        return childInBucket(2);
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
                "openBraceToken",
                "statements",
                "closeBraceToken"};
    }

    public BlockStatementNode modify(
            Token openBraceToken,
            NodeList<StatementNode> statements,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                openBraceToken,
                statements.underlyingListNode(),
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createBlockStatementNode(
                openBraceToken,
                statements,
                closeBraceToken);
    }

    public BlockStatementNodeModifier modify() {
        return new BlockStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class BlockStatementNodeModifier {
        private final BlockStatementNode oldNode;
        private Token openBraceToken;
        private NodeList<StatementNode> statements;
        private Token closeBraceToken;

        public BlockStatementNodeModifier(BlockStatementNode oldNode) {
            this.oldNode = oldNode;
            this.openBraceToken = oldNode.openBraceToken();
            this.statements = oldNode.statements();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public BlockStatementNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public BlockStatementNodeModifier withStatements(
                NodeList<StatementNode> statements) {
            Objects.requireNonNull(statements, "statements must not be null");
            this.statements = statements;
            return this;
        }

        public BlockStatementNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public BlockStatementNode apply() {
            return oldNode.modify(
                    openBraceToken,
                    statements,
                    closeBraceToken);
        }
    }
}
