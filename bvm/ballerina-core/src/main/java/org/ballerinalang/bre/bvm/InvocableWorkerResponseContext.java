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
import org.ballerinalang.model.types.TypeTags;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This represents a synchronized invocation worker result context.
 */
public class InvocableWorkerResponseContext implements WorkerResponseContext {
    
    private int[] retRegIndexes;
    
    private BType[] responseTypes;
    
    private AtomicBoolean fulfilled;
    
    private WorkerSignal currentSignal;
    
    private Semaphore responseChecker;
    
    public InvocableWorkerResponseContext() { }
    
    public InvocableWorkerResponseContext(BType[] responseTypes, boolean checkResponse) {
        this.fulfilled = new AtomicBoolean();
        this.responseTypes = responseTypes;
        if (checkResponse) {
            this.responseChecker = new Semaphore(0);
        }
    }
    
    @Override
    public WorkerExecutionContext signal(WorkerSignal signal) {
        switch (signal.getType()) {
        case BREAK:
            break;
        case ERROR:
            break;
        case MESSAGE:
            break;
        case RETURN:
            return this.doReturn(signal);
        default:
            break;
        }
        return null;
    }
    
    private void handleAlreadyReturned() {
        System.out.println("ALREADY RETURNED");
    }
    
    private WorkerExecutionContext doReturn(WorkerSignal signal) {
        if (this.fulfilled.getAndSet(true)) {
            this.handleAlreadyReturned();
            return null;
        }
        this.currentSignal = signal;
        WorkerExecutionContext runInCallerCtx = this.storeResponseInParentAndContinue();
        BLangScheduler.workerDone(signal.getSourceContext());
        return runInCallerCtx;
    }
    
    private void mergeResultData(WorkerData sourceData, WorkerData targetData) {
        int callersRetRegIndex;
        int longRegCount = 0;
        int doubleRegCount = 0;
        int stringRegCount = 0;
        int intRegCount = 0;
        int refRegCount = 0;
        int byteRegCount = 0;
        BType[] retTypes = this.responseTypes;
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i];
            callersRetRegIndex = this.retRegIndexes[i];
            switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                targetData.longRegs[callersRetRegIndex] = sourceData.longRegs[longRegCount++];
                break;
            case TypeTags.FLOAT_TAG:
                targetData.doubleRegs[callersRetRegIndex] = sourceData.doubleRegs[doubleRegCount++];
                break;
            case TypeTags.STRING_TAG:
                targetData.stringRegs[callersRetRegIndex] = sourceData.stringRegs[stringRegCount++];
                break;
            case TypeTags.BOOLEAN_TAG:
                targetData.intRegs[callersRetRegIndex] = sourceData.intRegs[intRegCount++];
                break;
            case TypeTags.BLOB_TAG:
                targetData.byteRegs[callersRetRegIndex] = sourceData.byteRegs[byteRegCount++];
                break;
            default:
                targetData.refRegs[callersRetRegIndex] = sourceData.refRegs[refRegCount++];
                break;
            }
        }
    }

    private WorkerExecutionContext storeResponseInParentAndContinue() {
        if (this.retRegIndexes != null) {
            this.mergeResultData(this.currentSignal.getResult(), 
                    this.currentSignal.getSourceContext().parent.workerLocal);
            if (this.responseChecker != null) {
                this.responseChecker.release();
            }
            WorkerExecutionContext ctx = this.currentSignal.getSourceContext();
            WorkerExecutionContext parentCtx = ctx.parent;
            if (parentCtx.code != null) {
                return BLangScheduler.resume(parentCtx, ctx.runInCaller);
            }
        }
        return null;
    }
    
    @Override
    public void updateParentWorkerResultLocation(int[] retRegIndexes) {
        this.retRegIndexes = retRegIndexes;
    }

    @Override
    public void checkAndRefreshFulfilledResponse() {
        if (this.fulfilled.get()) {
            this.storeResponseInParentAndContinue();
        }
    }
    
    public void waitForResponse() {
        if (this.responseChecker == null) {
            return;
        }
        try {
            this.responseChecker.acquire();
        } catch (InterruptedException ignore) { /* ignore */ }        
    }

}
