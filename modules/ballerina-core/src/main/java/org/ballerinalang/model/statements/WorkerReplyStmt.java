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
import org.ballerinalang.model.expressions.VariableRefExpr;

/**
 * A {@code WorkerReplyStmt} Class to hold data related to worker reply statement
 * <p>
 * result <- sampleWorker;
 *
 *  @since 0.8.0
 */
public class WorkerReplyStmt extends AbstractStatement {
    private String workerName;
    private VariableRefExpr receiveExpr;
    Worker worker;

    public WorkerReplyStmt(VariableRefExpr receiveExpr, String workerName, NodeLocation nodeLocation) {
        super(nodeLocation);
        this.receiveExpr = receiveExpr;
        this.workerName = workerName;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public VariableRefExpr getReceiveExpr() {
        return receiveExpr;
    }

    public void setReceiveExpr(VariableRefExpr receiveExpr) {
        this.receiveExpr = receiveExpr;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
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
