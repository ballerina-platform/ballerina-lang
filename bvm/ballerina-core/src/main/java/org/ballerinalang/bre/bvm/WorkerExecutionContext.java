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

import org.ballerinalang.bre.BallerinaTransactionManager;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.debugger.DebugCommand;
import org.ballerinalang.util.debugger.DebugContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This represents a Ballerina worker execution context.
 */
public class WorkerExecutionContext {

    public WorkerExecutionContext parent;
    
    public WorkerState state = WorkerState.CREATED;
    
    public Map<String, Object> globalProps;
    
    public Map<String, Object> localProps = new HashMap<>();
    
    public int ip;
    
    public int backupIP;
    
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

    private Lock executionLock;

    private BStruct error;

    private DebugContext debugContext;

    public WorkerExecutionContext(ProgramFile programFile) {
        this.programFile = programFile;
        this.globalProps = new HashMap<>();
        this.runInCaller = true;
    }
    
    public WorkerExecutionContext(WorkerExecutionContext parent, WorkerResponseContext respCtx, 
            CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, WorkerData workerLocal, 
            WorkerData workerResult, int[] retRegIndexes, Map<String, Object> globalProperties,
            boolean runInCaller) {
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
        this.globalProps = globalProperties;
        this.ip = this.workerInfo.getCodeAttributeInfo().getCodeAddrs();
        this.runInCaller = runInCaller;
        initDebugger();
        if (!this.runInCaller) {
            executionLock = new ReentrantLock();
        }
    }

    public WorkerExecutionContext(WorkerExecutionContext parent, WorkerResponseContext respCtx,
                                  CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, WorkerData workerLocal,
                                  Map<String, Object> globalProperties, boolean runInCaller) {
        this.parent = parent;
        this.respCtx = respCtx;
        this.callableUnitInfo = callableUnitInfo;
        this.workerInfo = workerInfo;
        this.programFile = callableUnitInfo.getPackageInfo().getProgramFile();
        this.constPool = callableUnitInfo.getPackageInfo().getConstPoolEntries();
        this.code = callableUnitInfo.getPackageInfo().getInstructions();
        this.workerLocal = workerLocal;
        this.globalProps = globalProperties;
        this.ip = this.workerInfo.getCodeAttributeInfo().getCodeAddrs();
        this.runInCaller = runInCaller;
        initDebugger();
        if (!this.runInCaller) {
            executionLock = new ReentrantLock();
        }
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
        if (parentCtx == null || parentCtx.getCurrentCommand() == DebugCommand.RESUME) {
            this.debugContext = new DebugContext();
        } else if (parentCtx.getCurrentCommand() == DebugCommand.STEP_IN) {
            this.debugContext = new DebugContext(DebugCommand.STEP_IN);
        } else if (parentCtx.getCurrentCommand() == DebugCommand.STEP_OVER) {
            this.debugContext = new DebugContext();
        }
        this.programFile.getDebugger().addWorkerContext(this);
    }
    
    public void backupIP() {
        this.backupIP = this.ip;
    }
    
    public void restoreIP() {
        this.ip = this.backupIP;
    }
    
    public void setError(BStruct error) {
        this.error = error;
    }
    
    public BStruct getError() {
        return error;
    }

    public boolean isInTransaction() {
        // TODO 
        return false;
    }

    public BallerinaTransactionManager getBallerinaTransactionManager() {
        return null;
    }

    public void setBallerinaTransactionManager(BallerinaTransactionManager ballerinaTransactionManager) {
        //TODO
    }
    
    public void lockExecution() {
        if (this.executionLock != null) {
            this.executionLock.lock();
        }
    }
    
    public void unlockExecution() {
        if (this.executionLock != null) {
            this.executionLock.unlock();
        }
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
        builder.append("Backup IP: " + this.backupIP + "} \n");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object rhs) {
        return this == rhs;
    }
    
}
