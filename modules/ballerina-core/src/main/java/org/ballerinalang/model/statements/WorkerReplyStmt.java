/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.statements;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.runtime.worker.WorkerDataChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code WorkerReplyStmt} Class to hold data related to worker reply statement.
 * <br>
 * result &lt;- sampleWorker;
 *
 *  @since 0.8.0
 */
public class WorkerReplyStmt extends AbstractStatement {
    private String workerName;
    protected List<Expression> expressionList = new ArrayList<>();
    private Worker worker;
    private WorkerDataChannel workerDataChannel;

    public WorkerReplyStmt(String workerName, List<Expression> expressionList, NodeLocation nodeLocation) {
        super(nodeLocation);
        this.workerName = workerName;
        this.expressionList = expressionList;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public Expression[] getExpressionList() {
        return expressionList.toArray(new Expression[expressionList.size()]);
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public WorkerDataChannel getWorkerDataChannel() {
        return workerDataChannel;
    }

    public void setWorkerDataChannel(WorkerDataChannel workerDataChannel) {
        this.workerDataChannel = workerDataChannel;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }
}
