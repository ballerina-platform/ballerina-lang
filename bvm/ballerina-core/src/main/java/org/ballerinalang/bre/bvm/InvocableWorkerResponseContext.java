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
import org.ballerinalang.model.values.BStruct;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
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
    
    private WorkerExecutionContext targetCtx;
    
    private Map<String, WorkerDataChannel> workerDataChannels;
    
    private Map<String, BStruct> workerErrors;
    
    private int workerCount;
    
    public InvocableWorkerResponseContext() { }
    
    public InvocableWorkerResponseContext(BType[] responseTypes, int workerCount, boolean checkResponse) {
        this.fulfilled = new AtomicBoolean();
        this.responseTypes = responseTypes;
        this.workerCount = workerCount;
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
            this.doError(signal);
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
        PrintStream out = System.out;
        out.println("Callable already returned");
        //TODO
    }
    
    private synchronized void doError(WorkerSignal signal) {
        if (this.workerErrors == null) {
            this.workerErrors = new HashMap<>();
        }
        WorkerExecutionContext sourceCtx = signal.getSourceContext();
        BLangScheduler.workerExcepted(sourceCtx);
        BStruct error = sourceCtx.getError();
        this.workerErrors.put(sourceCtx.workerInfo.getWorkerName(), error);
        if (this.workerErrors.size() >= this.workerCount) {
            BLangScheduler.errorThrown(this.targetCtx, this.createCallFailedError(this.workerErrors));
            if (this.responseChecker != null) {
                this.responseChecker.release();
            }
        }        
    }
    
    private BStruct createCallFailedError(Map<String, BStruct> errors) {
        //TODO
        return null;
    }
    
    private WorkerExecutionContext doReturn(WorkerSignal signal) {
        WorkerExecutionContext runInCallerCtx = null;
        if (this.fulfilled.getAndSet(true)) {
            this.handleAlreadyReturned();
        } else {
            this.currentSignal = signal;
            runInCallerCtx = this.storeResponseInParentAndContinue(true);
        }
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

    private WorkerExecutionContext storeResponseInParentAndContinue(boolean runInCaller) {
        if (this.retRegIndexes != null) {
            this.mergeResultData(this.currentSignal.getResult(), this.targetCtx.workerLocal);
            if (this.responseChecker != null) {
                this.responseChecker.release();
            }
            if (this.targetCtx.code != null) {
                return BLangScheduler.resume(this.targetCtx, runInCaller);
            }
        }
        return null;
    }
    
    @Override
    public void updateTargetContextInfo(WorkerExecutionContext targetCtx, int[] retRegIndexes) {
        this.targetCtx = targetCtx;
        this.retRegIndexes = retRegIndexes;
    }

    @Override
    public void checkAndRefreshFulfilledResponse() {
        if (this.fulfilled.get()) {
            this.storeResponseInParentAndContinue(false);
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
