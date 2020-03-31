/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * @since 0.94
 */
public class BLangTransaction extends BLangStatement implements TransactionNode {
    public BLangBlockStmt transactionBody;
    public BLangBlockStmt onRetryBody;
    public BLangBlockStmt committedBody;
    public BLangBlockStmt abortedBody;
    public BLangExpression retryCount;

    public BLangTransaction() {
    }

    public BLangTransaction(BLangBlockStmt transactionBody,
                            BLangBlockStmt onRetryBody,
                            BLangExpression retryCount) {
        this.transactionBody = transactionBody;
        this.onRetryBody = onRetryBody;
        this.retryCount = retryCount;
    }

    @Override
    public BLangBlockStmt getTransactionBody() {
        return transactionBody;
    }

    @Override
    public BLangBlockStmt getOnRetryBody() {
        return onRetryBody;
    }

    @Override
    public ExpressionNode getRetryCount() {
        return retryCount;
    }

    @Override
    public BLangBlockStmt getAbortedBody() {
        return abortedBody;
    }

    @Override
    public BLangBlockStmt getCommittedBody() {
        return committedBody;
    }

    @Override
    public void setTransactionBody(BlockStatementNode body) {
        this.transactionBody = (BLangBlockStmt) body;
    }

    @Override
    public void setOnRetryBody(BlockStatementNode body) {
        this.onRetryBody = (BLangBlockStmt) body;
    }

    @Override
    public void setRetryCount(ExpressionNode retryCount) {
        this.retryCount = (BLangExpression) retryCount;
    }

    @Override
    public void setCommittedBody(BlockStatementNode committedBlock) {
        this.committedBody = (BLangBlockStmt) committedBlock;
    }

    @Override
    public void setAbortedBody(BlockStatementNode abortedBlock) {
        this.abortedBody = (BLangBlockStmt) abortedBlock;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TRANSACTION;
    }

    @Override
    public String toString() {
        return "Transaction: {" + transactionBody + "} "
                + (onRetryBody != null ? " failed {" + String.valueOf(onRetryBody) + "}" : "")
                + (retryCount != null ? " retry (" + retryCount + ")" : "");
    }
}
