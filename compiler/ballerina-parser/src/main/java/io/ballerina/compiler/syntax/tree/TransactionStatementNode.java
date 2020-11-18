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

    public Optional<OnFailClauseNode> onFailClause() {
        return optionalChildInBucket(2);
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
                "blockStatement",
                "onFailClause"};
    }

    public TransactionStatementNode modify(
            Token transactionKeyword,
            BlockStatementNode blockStatement,
            OnFailClauseNode onFailClause) {
        if (checkForReferenceEquality(
                transactionKeyword,
                blockStatement,
                onFailClause)) {
            return this;
        }

        return NodeFactory.createTransactionStatementNode(
                transactionKeyword,
                blockStatement,
                onFailClause);
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
        private OnFailClauseNode onFailClause;

        public TransactionStatementNodeModifier(TransactionStatementNode oldNode) {
            this.oldNode = oldNode;
            this.transactionKeyword = oldNode.transactionKeyword();
            this.blockStatement = oldNode.blockStatement();
            this.onFailClause = oldNode.onFailClause().orElse(null);
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

        public TransactionStatementNodeModifier withOnFailClause(
                OnFailClauseNode onFailClause) {
            Objects.requireNonNull(onFailClause, "onFailClause must not be null");
            this.onFailClause = onFailClause;
            return this;
        }

        public TransactionStatementNode apply() {
            return oldNode.modify(
                    transactionKeyword,
                    blockStatement,
                    onFailClause);
        }
    }
}
