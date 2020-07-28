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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.clauses.OnFailClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;

/**
 * @since 1.3.0
 */
public class BLangTransaction extends BLangStatement implements TransactionNode {

    public BLangBlockStmt transactionBody;
    public boolean statementBlockReturns;
    public BLangOnFailClause onFailClause;

    public BLangTransaction() {
    }

    public BLangTransaction(BLangBlockStmt transactionBody) {
        this.transactionBody = transactionBody;
    }

    @Override
    public BLangBlockStmt getTransactionBody() {
        return transactionBody;
    }

    @Override
    public void setTransactionBody(BlockStatementNode body) {
        this.transactionBody = (BLangBlockStmt) body;
    }

    @Override
    public OnFailClauseNode getOnFailClause() {
        return this.onFailClause;
    }

    @Override
    public void setOnFailClause(OnFailClauseNode onFailClause) {
        this.onFailClause = (BLangOnFailClause) onFailClause;
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
        return "Transaction: {" + transactionBody + "} ";
    }
}
