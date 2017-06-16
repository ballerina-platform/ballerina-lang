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
 * A {@code WorkerInvocationStmt} Class to hold data related to worker invocation statement.
 * <br>
 * message -&gt; sampleWorker;
 *
 *  @since 0.8.0
 */
public class WorkerInvocationStmt extends AbstractStatement implements CallableUnitInvocationExpr<Worker> {

    private String workerName;
    private Worker calleeWorker;
    private BType[] types = new BType[0];
    protected List<Expression> expressionList = new ArrayList<>();
    private WorkerDataChannel workerDataChannel;
    private String enclosingCallableUnitName;
    private String packagePath;

    private int[] offsets;

    public WorkerInvocationStmt(String workerName, List<Expression> expressionList, NodeLocation nodeLocation,
                                WhiteSpaceDescriptor whiteSpaceDescriptor) {
        super(nodeLocation);
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        this.workerName = workerName;
        this.expressionList = expressionList;
    }

    public String getCallableUnitName() {
        return workerName;
    }


    public String getEnclosingCallableUnitName() {
        return enclosingCallableUnitName;
    }

    public void setEnclosingCallableUnitName(String enclosingCallableUnitName) {
        this.enclosingCallableUnitName = enclosingCallableUnitName;
    }

    public WorkerDataChannel getWorkerDataChannel() {
        return workerDataChannel;
    }

    public void setWorkerDataChannel(WorkerDataChannel workerDataChannel) {
        this.workerDataChannel = workerDataChannel;
    }

    @Override
    public String getName() {
        return workerName;
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

    @Override
    public Worker getCallableUnit() {
        return calleeWorker;
    }

    @Override
    public void setCallableUnit(Worker callableUnit) {
        this.calleeWorker = callableUnit;
    }

    @Override
    public BType[] getTypes() {
        return this.types;
    }

    @Override
    public void setTypes(BType[] types) {
        this.types = types;
    }

    public int[] getOffsets() {
        return offsets;
    }

    public void setOffsets(int[] offsets) {
        this.offsets = offsets;
    }

    public Expression[] getExpressionList() {
        return expressionList.toArray(new Expression[expressionList.size()]);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}


