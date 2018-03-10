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

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.debugger.DebugCommand;
import org.ballerinalang.util.program.BLangVMUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * This class represents the base implementation for a {@link WorkerResponseContext}.
 */
public abstract class BaseWorkerResponseContext implements WorkerResponseContext {

    protected int[] retRegIndexes;

    protected BType[] responseTypes;

    protected boolean fulfilled;

    protected WorkerSignal currentSignal;

    protected Semaphore responseChecker;

    protected WorkerExecutionContext targetCtx;

    protected Map<String, WorkerDataChannel> workerDataChannels;

    protected Map<String, BStruct> workerErrors;
    
    protected int haltCount;

    protected int workerCount;

    public BaseWorkerResponseContext(BType[] responseTypes, int workerCount, boolean checkResponse) {
        this.responseTypes = responseTypes;
        this.workerCount = workerCount;
        if (checkResponse) {
            this.responseChecker = new Semaphore(0);
        }
    }
    
    protected boolean isFulfilled() {
        return fulfilled;
    }
    
    protected void setAsFulfilled() {
        this.fulfilled = true;
    }
    
    protected void setCurrentSignal(WorkerSignal signal) {
        this.currentSignal = signal;
    }
    
    protected WorkerSignal getCurrentSignal() {
        return currentSignal;
    }

    @Override
    public WorkerExecutionContext signal(WorkerSignal signal) {
        switch (signal.getType()) {
        case ERROR:
            return this.onError(signal);
        case MESSAGE:
            this.onMessage(signal);
            break;
        case HALT:
            return this.onHalt(signal);
        case RETURN:
            return this.onReturn(signal);
        case TIMEOUT:
            return this.onTimeout(signal);
        default:
            break;
        }
        return null;
    }
    
    protected synchronized WorkerExecutionContext onTimeout(WorkerSignal signal) {
        return null;
    }
        
    protected synchronized void onMessage(WorkerSignal signal) { 
        if (this.isFulfilled() && this.isReturnable()) {
            this.handleAlreadyFulfilled(signal);
        } else {
            this.setAsFulfilled();
            this.setCurrentSignal(signal);
            this.printStoredErrors();
            this.onFulfillment(false);
        }
    }

    protected void handleAlreadyFulfilled(WorkerSignal signal) {
        WorkerExecutionContext sourceCtx = signal.getSourceContext();
        BLangVMUtils.log("error: worker '" + sourceCtx.workerInfo.getWorkerName() + 
                "' trying to return on already returned callable '" + 
                signal.getSourceContext().callableUnitInfo.getName() + "'.");
    }
    
    protected WorkerExecutionContext propagateErrorToTarget() {
        this.notifyResponseChecker();
        return this.onFinalizedError(BLangVMErrors.createCallFailedException(
                this.targetCtx, this.getWorkerErrors()));
    }

    protected synchronized WorkerExecutionContext onHalt(WorkerSignal signal) {
        WorkerExecutionContext runInCallerCtx = null;
        WorkerExecutionContext sourceCtx = signal.getSourceContext();
        BLangScheduler.workerDone(sourceCtx);
        this.haltCount++;
        if (this.isReturnable()) {
            if (!this.isFulfilled() && this.isWorkersDone()) {
                this.setCurrentSignal(signal);
                this.propagateErrorToTarget();
            }
        } else {
            if (!this.isFulfilled()) {
                this.setAsFulfilled();
                this.setCurrentSignal(signal);
                this.printStoredErrors();
                runInCallerCtx = this.onFulfillment(true);
            }
        }
        return runInCallerCtx;
    }
    
    
    protected boolean isReturnable() {
        return this.responseTypes.length > 0;
    }
    
    protected void initWorkerErrors() {
        if (this.workerErrors == null) {
            this.workerErrors = new HashMap<>();
        }
    }
    
    protected void printError(BStruct error) {
        BLangVMUtils.log(error.stringValue());
    }
    
    protected boolean isWorkersDone() {
        return (this.workerErrors == null ? 0 : this.workerErrors.size() + this.haltCount) >= this.workerCount;
    }
    
    protected void storeError(WorkerExecutionContext sourceCtx, BStruct error) {
        this.workerErrors.put(sourceCtx.workerInfo.getWorkerName(), error);
    }
    
    protected Map<String, BStruct> getWorkerErrors() {
        return workerErrors;
    }
    
    protected synchronized WorkerExecutionContext onError(WorkerSignal signal) {
        this.initWorkerErrors();
        WorkerExecutionContext sourceCtx = signal.getSourceContext();
        if (this.isFulfilled()) {
            printError(sourceCtx.getError());
        } else {
            this.storeError(sourceCtx, sourceCtx.getError());
            this.setCurrentSignal(signal);
            if (this.isWorkersDone()) {
                return this.propagateErrorToTarget();
            }
        }
        return null;
    }
    
    protected WorkerExecutionContext onFinalizedError(BStruct error) {
        this.modifyDebugCommands(this.targetCtx, this.currentSignal.getSourceContext());
        WorkerExecutionContext runInCallerCtx = BLangScheduler.errorThrown(this.targetCtx, error);
        return runInCallerCtx;
    }
    
    protected void notifyResponseChecker() {
        if (this.responseChecker != null) {
            this.responseChecker.release();
        }
    }
    
    protected void printStoredErrors() {
        if (this.workerErrors != null) {
            BLangVMUtils.log("worker errors: " + this.workerErrors);
        }
    }

    protected synchronized WorkerExecutionContext onReturn(WorkerSignal signal) {
        WorkerExecutionContext runInCallerCtx = null;
        if (this.isFulfilled() && this.isReturnable()) {
            this.handleAlreadyFulfilled(signal);
        } else {
            this.setAsFulfilled();
            this.setCurrentSignal(signal);
            this.printStoredErrors();
            runInCallerCtx = this.onFulfillment(true);
        }
        BLangScheduler.workerDone(signal.getSourceContext());
        return runInCallerCtx;
    }

    @Override
    public WorkerExecutionContext onFulfillment(boolean runInCaller) {
        WorkerExecutionContext runInCallerCtx = null;
        if (this.targetCtx != null) {
            BLangVMUtils.mergeResultData(this.currentSignal.getResult(), this.targetCtx.workerLocal, 
                    this.responseTypes, this.retRegIndexes);
            this.modifyDebugCommands(this.targetCtx, this.currentSignal.getSourceContext());
            if (!this.targetCtx.isRootContext()) {
                runInCallerCtx = BLangScheduler.resume(this.targetCtx, runInCaller);
            }
        }
        this.notifyResponseChecker();
        return runInCallerCtx;
    }

    protected void modifyDebugCommands(WorkerExecutionContext parent, WorkerExecutionContext child) {
        if (child.programFile == null || !child.programFile.getDebugger().isDebugEnabled()
                || parent == null || parent.getDebugContext() == null) {
            return;
        }
        DebugCommand crntCommand = child.getDebugContext().getCurrentCommand();
        if (!child.getDebugContext().isCmdChanged()) {
            return;
        }
        if (crntCommand == DebugCommand.STEP_OUT) {
            parent.getDebugContext().setCurrentCommand(DebugCommand.STEP_OVER);
        } else {
            parent.getDebugContext().setCurrentCommand(crntCommand);
        }
    }

    @Override
    public void updateTargetContextInfo(WorkerExecutionContext targetCtx, int[] retRegIndexes) {
        this.targetCtx = targetCtx;
        this.retRegIndexes = retRegIndexes;
    }

    public void waitForResponse() {
        if (this.responseChecker == null) {
            return;
        }
        try {
            this.responseChecker.acquire();
        } catch (InterruptedException ignore) { /* ignore */ }
    }

    @Override
    public synchronized WorkerDataChannel getWorkerDataChannel(String name) {
        if (this.workerDataChannels == null) {
            this.workerDataChannels = new HashMap<>();
        }
        WorkerDataChannel channel = this.workerDataChannels.get(name);
        if (channel == null) {
            channel = new WorkerDataChannel();
            this.workerDataChannels.put(name, channel);
        }
        return channel;
    }

}
