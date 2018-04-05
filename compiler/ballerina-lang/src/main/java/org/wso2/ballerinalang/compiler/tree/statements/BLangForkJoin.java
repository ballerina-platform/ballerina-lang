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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link ForkJoinNode}.
 *
 * @since 0.94
 */
public class BLangForkJoin extends BLangStatement implements ForkJoinNode {

    public List<BLangWorker> workers;
    public List<BLangIdentifier> joinedWorkers;
    public JoinType joinType = JoinType.ALL;
    public int joinedWorkerCount;
    public BLangBlockStmt joinedBody;

    public BLangExpression timeoutExpression;
    public BLangVariable timeoutVariable;
    public BLangBlockStmt timeoutBody;
    
    public BLangVariable joinResultVar;

    public BLangForkJoin() {
        this.workers = new ArrayList<>();
        this.joinedWorkers = new ArrayList<>();
    }

    @Override
    public List<BLangWorker> getWorkers() {
        return workers;
    }

    @Override
    public List<BLangIdentifier> getJoinedWorkerIdentifiers() {
        return joinedWorkers;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public int getJoinCount() {
        return joinedWorkerCount;
    }

    @Override
    public BlockNode getJoinBody() {
        return joinedBody;
    }

    @Override
    public ExpressionNode getTimeOutExpression() {
        return timeoutExpression;
    }

    @Override
    public VariableNode getTimeOutVariable() {
        return timeoutVariable;
    }

    @Override
    public BlockNode getTimeoutBody() {
        return timeoutBody;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.FORK_JOIN;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "BLangForkJoin: " + Arrays.toString(workers.toArray()) +
                (joinedBody != null ? " Join: " + joinType + " " + joinedWorkerCount + " " +
                        (joinedWorkers.isEmpty() ? "" : Arrays.toString(joinedWorkers.toArray())) +
                        " (" + joinResultVar + ") {" + joinedBody + "}" : "") +
                (timeoutBody != null ? " Timeout: (" + timeoutExpression + ") (" + timeoutVariable + ")"
                        + " {" + timeoutBody + "}" : "");
    }

    @Override
    public VariableNode getJoinResultVar() {
        return (BLangVariable) joinResultVar;
    }

    @Override
    public void setJoinResultVar(VariableNode var) {
        this.joinResultVar = (BLangVariable) var;
    }
    
}
