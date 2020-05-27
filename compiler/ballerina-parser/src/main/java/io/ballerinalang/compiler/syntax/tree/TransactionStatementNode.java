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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class TransactionStatementNode extends StatementNode {

    public TransactionStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token transactionKeyword() {
        return childInBucket(0);
    }

    public BlockStatementNode blockStatement() {
        return childInBucket(1);
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
                "transactionKeyword",
                "blockStatement"};
    }

    public TransactionStatementNode modify(
            Token transactionKeyword,
            BlockStatementNode blockStatement) {
        if (checkForReferenceEquality(
                transactionKeyword,
                blockStatement)) {
            return this;
        }

        return NodeFactory.createTransactionStatementNode(
                transactionKeyword,
                blockStatement);
    }

    public TransactionStatementNodeModifier modify() {
        return new TransactionStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TransactionStatementNodeModifier {
        private final TransactionStatementNode oldNode;
        private Token transactionKeyword;
        private BlockStatementNode blockStatement;

        public TransactionStatementNodeModifier(TransactionStatementNode oldNode) {
            this.oldNode = oldNode;
            this.transactionKeyword = oldNode.transactionKeyword();
            this.blockStatement = oldNode.blockStatement();
        }

        public TransactionStatementNodeModifier withTransactionKeyword(
                Token transactionKeyword) {
            Objects.requireNonNull(transactionKeyword, "transactionKeyword must not be null");
            this.transactionKeyword = transactionKeyword;
            return this;
        }

        public TransactionStatementNodeModifier withBlockStatement(
                BlockStatementNode blockStatement) {
            Objects.requireNonNull(blockStatement, "blockStatement must not be null");
            this.blockStatement = blockStatement;
            return this;
        }

        public TransactionStatementNode apply() {
            return oldNode.modify(
                    transactionKeyword,
                    blockStatement);
        }
    }
}
