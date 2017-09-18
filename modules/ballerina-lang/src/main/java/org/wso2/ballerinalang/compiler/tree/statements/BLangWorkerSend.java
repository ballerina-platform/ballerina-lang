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
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.WorkerSendNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link WorkerSendNode}.
 *
 * @since 0.94
 */
public class BLangWorkerSend extends BLangStatement implements WorkerSendNode {

    public List<ExpressionNode> expressions;
    public IdentifierNode workerIdentifier;
    public boolean isForkJoinSend;
    public SymbolEnv env;

    public BLangWorkerSend() {
        this.expressions = new ArrayList<>();
    }

    @Override
    public List<ExpressionNode> getExpressions() {
        return expressions;
    }

    @Override
    public IdentifierNode getWorkerName() {
        return workerIdentifier;
    }

    @Override
    public void setWorkerName(IdentifierNode identifierNode) {
        this.workerIdentifier = identifierNode;
    }

    @Override
    public boolean isForkJoinedSend() {
        return isForkJoinSend;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.WORKER_SEND;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "BLangWorkerSend: " + Arrays.toString(this.expressions.toArray()) + " -> " + workerIdentifier;
    }
}
