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
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangVMUtils;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents the base implementation for a {@link WorkerResponseContext}.
 */
public abstract class BaseWorkerResponseContext implements WorkerResponseContext {

    protected int[] retRegIndexes;

    protected BType[] responseTypes;

    protected AtomicBoolean fulfilled;

    protected WorkerSignal currentSignal;

    protected Semaphore responseChecker;

    protected WorkerExecutionContext targetCtx;

    protected Map<String, WorkerDataChannel> workerDataChannels;

    protected Map<String, BStruct> workerErrors;
    
    protected int haltCount;

    protected int workerCount;

    public BaseWorkerResponseContext(BType[] responseTypes, int workerCount, boolean checkResponse) {
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
        case HALT:
            return this.doHalt();
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
        // TODO
    }

    protected synchronized WorkerExecutionContext doHalt() {
        this.haltCount++;
        if (this.isHaltFinalized()) {
            return this.onHaltFinalized();
        }
        return null;
    }
    
    protected WorkerExecutionContext onHaltFinalized() {
        if (this.responseChecker != null) {
            this.responseChecker.release();
        }
        if (this.targetCtx.code != null) {
            return BLangScheduler.resume(this.targetCtx, true);
        }
        return null;
    }
    
    protected boolean isHaltFinalized() {
        return this.responseTypes.length == 0 && this.haltCount > 0;
    }
    
    protected synchronized void doError(WorkerSignal signal) {
        if (this.workerErrors == null) {
            this.workerErrors = new HashMap<>();
        }
        WorkerExecutionContext sourceCtx = signal.getSourceContext();
        BLangScheduler.workerExcepted(sourceCtx);
        BStruct error = sourceCtx.getError();
        this.workerErrors.put(sourceCtx.workerInfo.getWorkerName(), error);
        if (this.isFinalizedError()) {
            this.onFinalizedError(this.createCallFailedError(sourceCtx.programFile, this.workerErrors));
        }
    }
    
    protected boolean isFinalizedError() {
        return (this.workerErrors.size() - this.haltCount) >= this.workerCount;
    }
    
    protected void onFinalizedError(BStruct error) {
        BLangScheduler.errorThrown(this.targetCtx, error);
        if (this.responseChecker != null) {
            this.responseChecker.release();
        }
    }

    private BStruct createCallFailedError(ProgramFile programFile, Map<String, BStruct> errors) {
        return BLangVMErrors.createCallFailedException(programFile, errors);
    }

    protected WorkerExecutionContext doReturn(WorkerSignal signal) {
        WorkerExecutionContext runInCallerCtx = null;
        if (this.fulfilled.getAndSet(true)) {
            this.handleAlreadyReturned();
        } else {
            this.currentSignal = signal;
            runInCallerCtx = this.onFinalizedReturn(true);
        }
        BLangScheduler.workerDone(signal.getSourceContext());
        return runInCallerCtx;
    }

    protected WorkerExecutionContext onFinalizedReturn(boolean runInCaller) {
        if (this.retRegIndexes != null) {
            BLangVMUtils.mergeResultData(this.currentSignal.getResult(), this.targetCtx.workerLocal, 
                    this.responseTypes, this.retRegIndexes);
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
            this.onFinalizedReturn(false);
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
