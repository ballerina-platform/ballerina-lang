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

import org.ballerinalang.model.values.BError;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.program.BLangVMUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a {@link WorkerResponseContext} implementation which supports calling
 * the asynchronous functions.
 */
public class AsyncInvocableWorkerResponseContext extends SyncCallableWorkerResponseContext {

    private List<TargetContextInfo> targetContextInfos = new ArrayList<>();
    
    private boolean errored;
    
    private List<WorkerExecutionContext> workerExecutionContexts;
    
    private CallableUnitInfo callableUnitInfo;
    
    private boolean cancelled;
    
    public AsyncInvocableWorkerResponseContext(CallableUnitInfo callableUnitInfo, int workerCount) {
        super(callableUnitInfo.getRetParamTypes(), workerCount);
        this.callableUnitInfo = callableUnitInfo;
    }
    
    public AsyncInvocableWorkerResponseContext(CallableUnitInfo callableUnitInfo) {
        super(callableUnitInfo.getRetParamTypes(), 1);
        this.callableUnitInfo = callableUnitInfo;
    }
    
    public void setWorkerExecutionContexts(List<WorkerExecutionContext> workerExecutionContexts) {
        this.workerExecutionContexts = workerExecutionContexts;
    }
    
    @Override
    public synchronized WorkerExecutionContext signal(WorkerSignal signal) {
        if (this.cancelled) {
            return null;
        } else {
            return super.signal(signal);
        }
    }
    
    @Override
    public synchronized WorkerExecutionContext joinTargetContextInfo(WorkerExecutionContext targetCtx, 
            int[] retRegIndexes) {
        if (this.isFulfilled()) {
            this.onFulfillment(targetCtx, retRegIndexes, true);
            return targetCtx;
        } else if (this.errored) { 
            return this.propagateErrorToTarget(targetCtx, true);
        } else {
            this.targetContextInfos.add(new TargetContextInfo(targetCtx, retRegIndexes));
            return null;
        }
    }
    
    private WorkerExecutionContext onFulfillment(WorkerExecutionContext targetCtx, int[] retRegIndexes, 
            boolean runInCaller) {
        WorkerExecutionContext runInCallerCtx = null;
        BLangVMUtils.mergeResultData(this.currentSignal.getResult(), targetCtx.workerLocal, 
                this.responseTypes, retRegIndexes);
        this.modifyDebugCommands(targetCtx, this.currentSignal.getSourceContext());
        if (!targetCtx.isRootContext()) {
            runInCallerCtx = BLangScheduler.resume(targetCtx, runInCaller);
        }
        return runInCallerCtx;
    }

    @Override
    public WorkerExecutionContext onFulfillment(boolean runInCaller) {
        this.doSuccessCallbackNotify();
        if (this.targetContextInfos.size() == 0) {
            return null;
        }
        TargetContextInfo info = this.targetContextInfos.get(0);
        WorkerExecutionContext runInCallerCtx = this.onFulfillment(info.targetCtx, info.retRegIndexes, runInCaller);
        for (int i = 1; i < this.targetContextInfos.size(); i++) {
            info = this.targetContextInfos.get(i);
            this.onFulfillment(info.targetCtx, info.retRegIndexes, false);
        }
        return runInCallerCtx;
    }
    
    @Override
    protected WorkerExecutionContext propagateErrorToTarget() {
        this.errored = true;
        this.doFailCallbackNotify(null);
        if (this.targetContextInfos.size() == 0) {
            return null;
        }
        WorkerExecutionContext runInCallerCtx = this.propagateErrorToTarget(
                this.targetContextInfos.get(0).targetCtx, true);
        for (int i = 1; i < this.targetContextInfos.size(); i++) {
            this.propagateErrorToTarget(this.targetContextInfos.get(i).targetCtx, false);
        }
        return runInCallerCtx;
    }
    
    private WorkerExecutionContext propagateErrorToTarget(WorkerExecutionContext targetCtx, boolean runInCaller) {
        WorkerExecutionContext ctx = this.onFinalizedError(targetCtx,
                BLangVMErrors.handleError(targetCtx, workerErrors));
        return BLangScheduler.resume(ctx, runInCaller);
    }
    
    public boolean isDone() {
        return this.isFulfilled() || this.errored || this.isCancelled();
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public synchronized boolean cancel() {
        if (this.isDone()) {
            return false;
        }
        /* only non-native workers can be cancelled */
        if (this.workerExecutionContexts != null) {
            for (WorkerExecutionContext ctx: this.workerExecutionContexts) {
                BLangScheduler.stopWorker(ctx);
            }
            this.sendAsyncCancelErrorSignal();
            this.cancelled = true;
        }
        return this.cancelled;
    }
    
    private void sendAsyncCancelErrorSignal() {
        WorkerData result = BLangVMUtils.createWorkerData(this.callableUnitInfo.retWorkerIndex);
        BError error = BLangVMErrors.createCallCancelledException(this.workerExecutionContexts.get(0));
        WorkerExecutionContext ctx = this.signal(new WorkerSignal(
                new WorkerExecutionContext(error), SignalType.ERROR, result));
        BLangScheduler.resume(ctx);
    }
    
    /**
     * Represents a target context information entry.
     */
    private static class TargetContextInfo {
        
        public WorkerExecutionContext targetCtx;
        
        public int[] retRegIndexes;
        
        public TargetContextInfo(WorkerExecutionContext targetCtx, int[] retRegIndexes) {
            this.targetCtx = targetCtx;
            this.retRegIndexes = retRegIndexes;
        }
        
    }
    
}
