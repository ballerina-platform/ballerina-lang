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

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerDataChannelInfo;
import org.ballerinalang.util.program.BLangVMUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This represents forkjoin worker response context.
 *
 * @since 0.965.0
 */
public class ForkJoinWorkerResponseContext extends InvocableWorkerResponseContext {

    private int joinCount;

    private int targetIp;

    private int joinVarReg;

    private Set<String> joinWorkerNames;

    //Key - workerName, Value - data channel name;
    private Map<String, String> channelNames;

    private AtomicInteger haltCount;

    private List<String> joiningWorkers;

    public ForkJoinWorkerResponseContext(WorkerExecutionContext targetCtx, int targetIp, int joinVarReg,
            int workerCount, int joinCount, Set<String> joinWorkerNames, Map<String, String> channelNames) {
        super(null, workerCount, false);
        this.targetCtx = targetCtx;
        this.targetIp = targetIp;
        this.joinVarReg = joinVarReg;
        this.joinCount = joinCount;
        this.joinWorkerNames = joinWorkerNames;
        this.channelNames = channelNames;
        this.haltCount = new AtomicInteger(0);
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
                return this.doHalt(signal);
            case RETURN:
                //nothing to do
                break;
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

    private synchronized WorkerExecutionContext doHalt(WorkerSignal signal) {
        if (!joinWorkerNames.contains(signal.getSourceContext().workerInfo.getWorkerName())) {
            return null;
        }
        if (joiningWorkers == null) {
            joiningWorkers = new ArrayList<>();
        }
        joiningWorkers.add(signal.getSourceContext().workerInfo.getWorkerName());
        if (this.haltCount.incrementAndGet() == joinCount) {
            return this.onHaltFinalized();
        }
        return null;
    }

    protected WorkerExecutionContext onHaltFinalized() {
        BMap<String, BRefValueArray> mbMap = new BMap<>();
        channelNames.forEach((k,v) -> mbMap.put(k, getWorkerResult(v)));
        this.targetCtx.workerLocal.refRegs[joinVarReg] = (BRefType) mbMap;
        return BLangScheduler.resume(this.targetCtx, targetIp, true);
    }

    private BRefValueArray getWorkerResult(String channelName) {
        BRefValueArray bRefValueArray = new BRefValueArray(new BArrayType(BTypes.typeAny));
        if (channelName == null) {
            return bRefValueArray;
        }
        WorkerDataChannel dataChannel = getWorkerDataChannel(channelName);
        BRefType[] results;
        while ((results = dataChannel.tryTakeData()) != null) {
            for (int i = 0; i < results.length; i++) {
                bRefValueArray.add(i, results[i]);
            }
        }
        return bRefValueArray;
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

//    protected boolean isFinalizedError() {
//        return (this.workerErrors.size() - this.haltCount) >= this.workerCount;
//    }

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
        return this.workerDataChannels.computeIfAbsent(name, k -> new WorkerDataChannel());
    }

}
