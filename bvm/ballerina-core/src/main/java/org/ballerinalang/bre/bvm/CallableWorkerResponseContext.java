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
import org.ballerinalang.util.program.BLangVMUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This represents a invocation worker result context for supporting
 * handling a single worker, i.e. non-synchronized.
 * 
 * @since 0.965.0
 */
public class CallableWorkerResponseContext extends BaseWorkerResponseContext {
    
    protected BType[] responseTypes;

    protected boolean fulfilled;

    protected Map<String, BStruct> workerErrors;
        
    protected int haltCount;

    public CallableWorkerResponseContext(BType[] responseTypes, int workerCount) {
        super(workerCount);
        this.responseTypes = responseTypes;
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
    protected void onMessage(WorkerSignal signal) { 
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
        BStruct error = BLangVMErrors.createCallFailedException(this.targetCtx, this.getWorkerErrors());
        WorkerExecutionContext ctx = this.onFinalizedError(this.targetCtx, error);
        this.doFailCallbackNotify(error);
        return ctx;
    }

    @Override
    protected WorkerExecutionContext onHalt(WorkerSignal signal) {
        WorkerExecutionContext runInCallerCtx = null;
        this.haltCount++;
        if (this.isReturnable()) {
            if (!this.isFulfilled() && this.isWorkersDone()) {
                this.setCurrentSignal(signal);
                this.propagateErrorToTarget();
            }
        } else {
            if (!this.isFulfilled() && this.isWorkersDone()) {
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
            this.workerErrors = new LinkedHashMap<>();
        }
    }
    
    protected void printError(BStruct error) {
        BLangVMUtils.log(error.stringValue());
    }
    
    protected boolean isWorkersDone() {
        /* return count is not mentioned here, because, when a return happens, it will be immediately fulfilled,
         * and there is no need to call this function to find out if all workers are done */
        return ((this.workerErrors == null ? 0 : this.workerErrors.size()) + this.haltCount) >= this.workerCount;
    }
    
    protected void storeError(WorkerExecutionContext sourceCtx, BStruct error) {
        this.workerErrors.put(sourceCtx.workerInfo.getWorkerName(), error);
    }
    
    protected Map<String, BStruct> getWorkerErrors() {
        return workerErrors;
    }
    
    @Override
    protected WorkerExecutionContext onError(WorkerSignal signal) {
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
    
    protected WorkerExecutionContext onFinalizedError(WorkerExecutionContext targetCtx, BStruct error) {
        this.modifyDebugCommands(targetCtx, this.currentSignal.getSourceContext());
        WorkerExecutionContext runInCallerCtx = BLangScheduler.errorThrown(targetCtx, error);
        return runInCallerCtx;
    }
    
    protected void printStoredErrors() {
        if (this.workerErrors != null) {
            BLangVMUtils.log("worker errors: " + this.workerErrors);
        }
    }

    @Override
    protected WorkerExecutionContext onReturn(WorkerSignal signal) {
        WorkerExecutionContext runInCallerCtx = null;
        if (this.isFulfilled() && this.isReturnable()) {
            this.handleAlreadyFulfilled(signal);
        } else {
            this.setAsFulfilled();
            this.setCurrentSignal(signal);
            this.printStoredErrors();
            runInCallerCtx = this.onFulfillment(true);
        }
        return runInCallerCtx;
    }

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
        this.doSuccessCallbackNotify();
        return runInCallerCtx;
    }
    
}
