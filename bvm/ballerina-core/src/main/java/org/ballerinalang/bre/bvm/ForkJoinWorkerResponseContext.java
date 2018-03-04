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
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This represents forkjoin worker response context.
 *
 * @since 0.965.0
 */
public class ForkJoinWorkerResponseContext extends InvocableWorkerResponseContext {

    private int reqJoinCount;

    private int joinTargetIp;

    private int joinVarReg;

    private int timeoutTargetIp;

    private int timeoutVarReg;

    private Set<String> joinWorkerNames;

    //Key - workerName, Value - data channel name;
    private Map<String, String> channelNames;

    private final Map<String, BStruct> workerErrors;

    private AtomicInteger haltCount;

    private AtomicInteger errorCount;

    public ForkJoinWorkerResponseContext(WorkerExecutionContext targetCtx, int joinTargetIp, int joinVarReg,
            int timeoutTargetIp, int timeoutVarReg, int workerCount, int reqJoinCount, Set<String> joinWorkerNames,
            Map<String, String> channelNames) {
        super(null, workerCount, false);
        this.targetCtx = targetCtx;
        this.joinTargetIp = joinTargetIp;
        this.joinVarReg = joinVarReg;
        this.timeoutTargetIp = timeoutTargetIp;
        this.timeoutVarReg = timeoutVarReg;
        this.reqJoinCount = reqJoinCount;
        this.joinWorkerNames = joinWorkerNames;
        this.channelNames = channelNames;
        this.haltCount = new AtomicInteger(0);
        this.errorCount = new AtomicInteger(0);
        this.workerErrors = new HashMap<>();
    }

    @Override
    public WorkerExecutionContext signal(WorkerSignal signal) {
        switch (signal.getType()) {
            case BREAK:
                break;
            case ERROR:
                //TODO can we run the erro flow in same thread?
                this.doError(signal);
                break;
            case MESSAGE:
                break;
            case HALT:
                return this.doHalt(signal);
            case RETURN:
                //nothing to do
                break;
            case TIMEOUT:
                return this.doTimeout();
            default:
                break;
        }
        return null;
    }

    @Override
    protected WorkerExecutionContext doHalt(WorkerSignal signal) {
        BLangScheduler.workerDone(signal.getSourceContext());
        if (!joinWorkerNames.contains(signal.getSourceContext().workerInfo.getWorkerName())) {
            return null;
        }
        if (this.haltCount.incrementAndGet() != reqJoinCount) {
            return null;
        }
        if (this.fulfilled.getAndSet(true)) {
            return null;
        }
        synchronized (workerErrors) {
            workerErrors.forEach(this::printError);
        }
        return this.onHaltFinalized();
    }

    protected void doError(WorkerSignal signal) {
        BLangScheduler.workerExcepted(signal.getSourceContext());
        BStruct error = signal.getSourceContext().getError();
        if (this.fulfilled.get()) {
            printError(signal.getSourceContext().workerInfo.getWorkerName(), error);
            return;
        }

        if ((workerCount - errorCount.incrementAndGet()) >= reqJoinCount) {
            addOrPrintError(signal.getSourceContext().workerInfo.getWorkerName(), error);
            return;
        }

        if (this.fulfilled.getAndSet(true)) {
            printError(signal.getSourceContext().workerInfo.getWorkerName(), error);
            return;
        }

        //This location means, no one will add errors to the workerErrors
        this.workerErrors.put(signal.getSourceContext().workerInfo.getWorkerName(), error);
        BLangScheduler.errorThrown(this.targetCtx, this.createCallFailedError(signal.getSourceContext(),
                this.workerErrors));
    }

    private WorkerExecutionContext doTimeout() {
        if (this.fulfilled.getAndSet(true)) {
            return null;
        }
        BMap<String, BRefValueArray> mbMap = new BMap<>();
        channelNames.forEach((k,v) -> {
            BRefValueArray workerRes = getWorkerResult(v);
            if (workerRes != null) {
                mbMap.put(k, workerRes);
            }
        });
        this.targetCtx.workerLocal.refRegs[timeoutVarReg] = (BRefType) mbMap;
        //Running the timeout call in a new thread.
        return BLangScheduler.resume(this.targetCtx, timeoutTargetIp, false);
    }

    private void addOrPrintError(String workerName, BStruct error) {
        synchronized (workerErrors) {
            if (this.fulfilled.get()) {
                printError(workerName, error);
                return;
            }
            workerErrors.put(workerName, error);
        }
    }

    protected WorkerExecutionContext onHaltFinalized() {
        BMap<String, BRefValueArray> mbMap = new BMap<>();
        channelNames.forEach((k, v) -> {
            BRefValueArray workerRes = getWorkerResult(v);
            if (workerRes != null) {
                mbMap.put(k, workerRes);
            }
        });
        this.targetCtx.workerLocal.refRegs[joinVarReg] = (BRefType) mbMap;
        return BLangScheduler.resume(this.targetCtx, joinTargetIp, true);
    }

    private BRefValueArray getWorkerResult(String channelName) {
        if (channelName == null) {
            return null;
        }
        BRefValueArray bRefValueArray = new BRefValueArray(new BArrayType(BTypes.typeAny));
        WorkerDataChannel dataChannel = getWorkerDataChannel(channelName);
        BRefType[] results;
        boolean dataNotAvailable = true;
        while ((results = dataChannel.tryTakeData()) != null) {
            dataNotAvailable = false;
            for (int i = 0; i < results.length; i++) {
                bRefValueArray.add(i, results[i]);
            }
        }
        if (dataNotAvailable) {
            return null;
        }
        return bRefValueArray;
    }

    private void printError(String workerName, BStruct error) {
        PrintStream out = System.out;
        out.println("error in worker - " + workerName + System.lineSeparator()
                + BLangVMErrors.getPrintableStackTrace(error));
    }


    private BStruct createCallFailedError(WorkerExecutionContext context, Map<String, BStruct> errors) {
        return BLangVMErrors.createCallFailedException(context, errors);
    }

}
