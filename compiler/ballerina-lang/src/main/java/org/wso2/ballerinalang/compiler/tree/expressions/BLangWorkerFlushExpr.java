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
import org.ballerinalang.model.tree.expressions.WorkerFlushExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link BLangWorkerFlushExpr}.
 *
 * @since 0.985.0
 */
public class BLangWorkerFlushExpr extends BLangExpression implements WorkerFlushExpressionNode {

    public BLangIdentifier workerIdentifier;
    public BSymbol workerSymbol;
    public List<BLangIdentifier> workerIdentifierList = new ArrayList<>();
    public List<BLangWorkerSend> cachedWorkerSendStmts = new ArrayList<>();

    @Override
    public NodeKind getKind() {
        return NodeKind.WORKER_FLUSH;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (workerIdentifier != null) {
            return "flush " + String.valueOf(workerIdentifier);
        }
        return "flush";
    }

    @Override
    public IdentifierNode getWorkerName() {
        return workerIdentifier;
    }

}
