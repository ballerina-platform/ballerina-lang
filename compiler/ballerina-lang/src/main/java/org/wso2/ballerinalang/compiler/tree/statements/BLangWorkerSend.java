/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.WorkerSendNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * Models the async-send-action.
 *
 * @since 0.94
 */
public class BLangWorkerSend extends BLangStatement implements WorkerSendNode {

    public BLangExpression expr;
    public BLangIdentifier workerIdentifier;
    public SymbolEnv env;
    public BLangExpression keyExpr;
    public boolean isChannel = false;
    public BLangIdentifier currentWorker;
    public BSymbol workerSymbol;

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public BLangExpression getKeyExpression() {
        return keyExpr;
    }

    @Override
    public BLangIdentifier getWorkerName() {
        return workerIdentifier;
    }

    @Override
    public void setWorkerName(IdentifierNode identifierNode) {
        this.workerIdentifier = (BLangIdentifier) identifierNode;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.WORKER_SEND;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    public String toActionString() {
        if (keyExpr != null) {
            return this.expr + " -> " + this.workerIdentifier + "," + this.keyExpr;
        }
        return this.expr + " -> " + this.workerIdentifier;
    }

    @Override
    public String toString() {
        return "BLangWorkerSend: " + this.toActionString();
    }
}
