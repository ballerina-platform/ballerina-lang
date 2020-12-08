/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.WorkerSendSyncExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of {@link BLangWorkerSyncSendExpr}.
 *
 * @since 0.985
 */
public class BLangWorkerSyncSendExpr extends BLangExpression implements WorkerSendSyncExpressionNode {

    public BLangIdentifier workerIdentifier;
    public BSymbol workerSymbol;
    public BLangExpression expr;
    public BLangWorkerReceive receive;
    public SymbolEnv env;
    public BType workerType;

    @Override
    public NodeKind getKind() {
        return NodeKind.WORKER_SYNC_SEND;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExpressionNode getExpression() {
        return expr;
    }

    @Override
    public IdentifierNode getWorkerName() {
        return workerIdentifier;
    }

    @Override
    public void setWorkerName(IdentifierNode identifierNode) {
        this.workerIdentifier = (BLangIdentifier) identifierNode;
    }

    public String toActionString() {
        return this.expr + " ->> " + this.workerIdentifier;
    }

    @Override
    public String toString() {
        return "BLangWorkerSyncSend: " + this.toActionString();
    }
}
