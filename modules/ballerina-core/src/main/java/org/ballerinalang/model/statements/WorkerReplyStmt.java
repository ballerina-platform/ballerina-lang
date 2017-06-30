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

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.CallableUnitInvocationExpr;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.types.BType;
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
public class WorkerReplyStmt extends AbstractStatement implements CallableUnitInvocationExpr<Worker> {
    private String workerName;
    protected List<Expression> expressionList = new ArrayList<>();
    private Worker worker;
    private WorkerDataChannel workerDataChannel;
    private BType[] bTypes;
    private int[] offsets = new int[0];
    private String enclosingCallableUnitName;
    private String packagePath;

    public WorkerReplyStmt(String workerName, List<Expression> expressionList, NodeLocation nodeLocation,
                           WhiteSpaceDescriptor whiteSpaceDescriptor) {
        super(nodeLocation);
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        this.workerName = workerName;
        this.expressionList = expressionList;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }


    public String getEnclosingCallableUnitName() {
        return enclosingCallableUnitName;
    }

    public void setEnclosingCallableUnitName(String enclosingCallableUnitName) {
        this.enclosingCallableUnitName = enclosingCallableUnitName;
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
    public StatementKind getKind() {
        return StatementKind.WORKER_REPLY;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public String getPackagePath() {
        return this.packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }
    /**
     * Returns an arrays of arguments of this callable unit invocation expression.
     *
     * @return the arrays of arguments
     */
    @Override
    public Expression[] getArgExprs() {
        return new Expression[0];
    }

    /**
     * Returns the {@code CallableUnit} linked with this callable unit invocation expression.
     *
     * @return the linked {@code CallableUnit}
     */
    @Override
    public Worker getCallableUnit() {
        return null;
    }

    /**
     * Sets the {@code CallableUnit}.
     *
     * @param callableUnit type of the callable unit
     */
    @Override
    public void setCallableUnit(Worker callableUnit) {

    }

    /**
     * Returns an arrays of argument types of this expression.
     *
     * @return an arrays of argument types
     */
    @Override
    public BType[] getTypes() {
        return this.bTypes;
    }

    /**
     * Sets an arrays of argument types.
     *
     * @param types arrays of argument types
     */
    @Override
    public void setTypes(BType[] types) {
        this.bTypes = types;
    }

    @Override
    public int[] getOffsets() {
        return this.offsets;
    }

    @Override
    public void setOffsets(int[] offsets) {
        this.offsets = offsets;
    }
}
