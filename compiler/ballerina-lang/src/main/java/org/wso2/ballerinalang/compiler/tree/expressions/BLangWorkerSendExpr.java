/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.expressions.WorkerSendExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

/**
 * Represents commons in worker async-send and sync-send.
 *
 * @since 2201.9.0
 */
public abstract class BLangWorkerSendExpr extends BLangWorkerSendReceiveExpr implements WorkerSendExpressionNode {

    // BLangNodes
    public BLangExpression expr;

    // Semantic Data
    public BLangWorkerReceive receive;
    public BType sendType;

    // For alternate receive type inference
    public boolean noMessagePossible;
    public BType sendTypeWithNoMsgIgnored;

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public BLangIdentifier getWorkerName() {
        return workerIdentifier;
    }

    @Override
    public void setWorkerName(IdentifierNode identifierNode) {
        this.workerIdentifier = (BLangIdentifier) identifierNode;
    }
}
