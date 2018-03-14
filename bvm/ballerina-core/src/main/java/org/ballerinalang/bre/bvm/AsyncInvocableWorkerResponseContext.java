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
    
    public AsyncInvocableWorkerResponseContext(BType[] responseTypes, int workerCount) {
        super(responseTypes, workerCount, false);
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
        if (this.targetContextInfos.size() == 0) {
            return null;
        }
        WorkerExecutionContext runInCallerCtx = this.doFulfillmentOnWorker(this.targetContextInfos.get(0), true);
        for (int i = 1; i < this.targetContextInfos.size(); i++) {
            this.doFulfillmentOnWorker(this.targetContextInfos.get(i), false);
        }
        return runInCallerCtx;
    }
    
    private WorkerExecutionContext doFulfillmentOnWorker(TargetContextInfo targetCtxInfo, boolean runInCaller) {
        WorkerExecutionContext runInCallerCtx = null;
        BLangVMUtils.mergeResultData(this.currentSignal.getResult(), targetCtxInfo.targetCtx.workerLocal, 
                this.responseTypes, targetCtxInfo.retRegIndexes);
        this.modifyDebugCommands(targetCtxInfo.targetCtx, this.currentSignal.getSourceContext());
        if (!targetCtxInfo.targetCtx.isRootContext()) {
            runInCallerCtx = BLangScheduler.resume(targetCtxInfo.targetCtx, runInCaller);
        }
        return runInCallerCtx;
    }
    
    @Override
    protected WorkerExecutionContext propagateErrorToTarget() {
        this.errored = true;
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
                BLangVMErrors.createCallFailedException(targetCtx, this.getWorkerErrors()));
        return BLangScheduler.resume(ctx, runInCaller);
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
