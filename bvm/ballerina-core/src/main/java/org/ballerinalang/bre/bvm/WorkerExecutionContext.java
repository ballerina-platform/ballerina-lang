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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.debugger.DebugCommand;
import org.ballerinalang.util.debugger.DebugContext;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.LocalTransactionInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a Ballerina worker execution context.
 */
public class WorkerExecutionContext {

    private static final String WORKER_NAME_NATIVE = "native";

    public WorkerExecutionContext parent;
    
    public WorkerState state = WorkerState.CREATED;
    
    public Map<String, Object> globalProps;
    
    public Map<String, Object> localProps;
    
    public int ip;
    
    public boolean stop;
        
    public ProgramFile programFile;
    
    public ConstantPoolEntry[] constPool;
    
    public Instruction[] code;
    
    public WorkerData workerLocal;
    
    public WorkerData workerResult;
    
    public int[] retRegIndexes;
        
    public CallableUnitInfo callableUnitInfo;
    
    public WorkerInfo workerInfo;
    
    public WorkerResponseContext respCtx;
    
    public boolean runInCaller;

    private BStruct error;

    private DebugContext debugContext;

    private static final String DISTRIBUTED_TRANSACTIONS = "b7a.distributed.transactions.enabled";

    private static final String FALSE = "false";

    public WorkerExecutionContext(ProgramFile programFile) {
        this.programFile = programFile;
        this.globalProps = new HashMap<>();
        this.runInCaller = true;
        setGlobalTransactionsEnabled();
    }
    
    public WorkerExecutionContext(BStruct error) {
        this.error = error;
        this.workerInfo = new WorkerInfo(0, WORKER_NAME_NATIVE);
    }
    
    public WorkerExecutionContext(WorkerExecutionContext parent, WorkerResponseContext respCtx, 
            CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, WorkerData workerLocal, 
            WorkerData workerResult, int[] retRegIndexes, boolean runInCaller) {
        this.parent = parent;
        this.respCtx = respCtx;
        this.callableUnitInfo = callableUnitInfo;
        this.workerInfo = workerInfo;
        this.programFile = callableUnitInfo.getPackageInfo().getProgramFile();
        this.constPool = callableUnitInfo.getPackageInfo().getConstPoolEntries();
        this.code = callableUnitInfo.getPackageInfo().getInstructions();
        this.workerLocal = workerLocal;
        this.workerResult = workerResult;
        this.retRegIndexes = retRegIndexes;
        this.globalProps = parent.globalProps;
        this.ip = this.workerInfo.getCodeAttributeInfo().getCodeAddrs();
        this.runInCaller = runInCaller;
        initDebugger();
    }

    public WorkerExecutionContext(WorkerExecutionContext parent, WorkerResponseContext respCtx,
                                  CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, WorkerData workerLocal, 
                                  boolean runInCaller) {
        this.parent = parent;
        this.respCtx = respCtx;
        this.callableUnitInfo = callableUnitInfo;
        this.workerInfo = workerInfo;
        this.programFile = callableUnitInfo.getPackageInfo().getProgramFile();
        this.constPool = callableUnitInfo.getPackageInfo().getConstPoolEntries();
        this.code = callableUnitInfo.getPackageInfo().getInstructions();
        this.workerLocal = workerLocal;
        this.globalProps = parent.globalProps;
        this.ip = this.workerInfo.getCodeAttributeInfo().getCodeAddrs();
        this.runInCaller = runInCaller;
        initDebugger();
    }

    private void initDebugger() {
        if (!programFile.getDebugger().isDebugEnabled()) {
            return;
        }
        if (parent == null) {
            this.debugContext = new DebugContext();
            this.programFile.getDebugger().addWorkerContext(this);
            return;
        }
        DebugContext parentCtx = parent.getDebugContext();
        if (parentCtx == null || parentCtx.getCurrentCommand() == DebugCommand.RESUME
                || parentCtx.getCurrentCommand() == DebugCommand.STEP_OVER
                || parentCtx.getCurrentCommand() == DebugCommand.STEP_OUT) {
            this.debugContext = new DebugContext();
        } else if (parentCtx.getCurrentCommand() == DebugCommand.STEP_IN) {
            this.debugContext = new DebugContext(DebugCommand.STEP_IN);
        }
        this.programFile.getDebugger().addWorkerContext(this);
    }
    
    public void setError(BStruct error) {
        this.error = error;
    }
    
    public BStruct getError() {
        return error;
    }

    public boolean isInTransaction() {
        return BLangVMUtils.getTransactionInfo(this) != null;
    }

    public void setLocalTransactionInfo(LocalTransactionInfo localTransactionInfo) {
        BLangVMUtils.setTransactionInfo(this, localTransactionInfo);
    }

    public LocalTransactionInfo getLocalTransactionInfo() {
        return BLangVMUtils.getTransactionInfo(this);
    }

    public boolean getGlobalTransactionEnabled() {
        return BLangVMUtils.getGlobalTransactionenabled(this);
    }

    public boolean isRootContext() {
        return this.code == null;
    }

    public DebugContext getDebugContext() {
        return debugContext;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n{ ID: " + this.hashCode() + "\n");
        builder.append("Parent: " + (this.parent != null ? this.parent.hashCode() : "N/A") + "\n");
        builder.append("Callable Unit: " + (this.callableUnitInfo != null ? 
                this.callableUnitInfo.getName() : "N/A") + "\n");
        builder.append("Worker ID: " + (this.workerInfo != null ? this.workerInfo.getWorkerName() : "N/A") + "\n");
        builder.append("STATE: " + this.state + "\n");
        builder.append("Run In Caller: " + this.runInCaller + "\n");
        builder.append("IP: " + this.ip + "\n");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object rhs) {
        return this == rhs;
    }

    private void setGlobalTransactionsEnabled() {
        String distributedTransactionsEnabledConfig = ConfigRegistry.getInstance()
                .getAsString(DISTRIBUTED_TRANSACTIONS);
        boolean distributedTransactionEnabled = true;
        if (distributedTransactionsEnabledConfig != null && distributedTransactionsEnabledConfig.equals(FALSE)) {
            distributedTransactionEnabled = false;
        }
        BLangVMUtils.setGlobalTransactionEnabledStatus(this, distributedTransactionEnabled);
    }
}
