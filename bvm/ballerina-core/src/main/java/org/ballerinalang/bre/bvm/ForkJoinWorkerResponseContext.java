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

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.program.BLangVMUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This represents the fork/join worker response context.
 *
 * @since 0.965.0
 */
public class ForkJoinWorkerResponseContext extends SyncCallableWorkerResponseContext {

    private int reqJoinCount;

    private int joinTargetIp;

    private int joinVarReg;

    private int timeoutTargetIp;

    private int timeoutVarReg;

    private Set<String> joinWorkerNames;

    //Key - workerName, Value - data channel name;
    private Map<String, String> channelNames;

    private final Map<String, BStruct> workerErrors;

    private int haltCount;

    private int errorCount;

    public ForkJoinWorkerResponseContext(WorkerExecutionContext targetCtx, int joinTargetIp, int joinVarReg,
            int timeoutTargetIp, int timeoutVarReg, int workerCount, int reqJoinCount, Set<String> joinWorkerNames,
            Map<String, String> channelNames) {
        super(null, workerCount);
        this.targetCtx = targetCtx;
        this.joinTargetIp = joinTargetIp;
        this.joinVarReg = joinVarReg;
        this.timeoutTargetIp = timeoutTargetIp;
        this.timeoutVarReg = timeoutVarReg;
        this.reqJoinCount = reqJoinCount;
        this.joinWorkerNames = joinWorkerNames;
        this.channelNames = channelNames;
        this.workerErrors = new HashMap<>();
    }

    @Override
    protected WorkerExecutionContext onReturn(WorkerSignal signal) { 
        return null;
    }
    
    @Override
    protected void onMessage(WorkerSignal signal) { }

    @Override
    protected synchronized WorkerExecutionContext onHalt(WorkerSignal signal) {
        if (!joinWorkerNames.contains(signal.getSourceContext().workerInfo.getWorkerName())) {
            return null;
        }
        if (++this.haltCount != reqJoinCount) {
            return null;
        }
        if (this.isFulfilled()) {
            return null;
        }
        this.setAsFulfilled();
        workerErrors.forEach(this::printError);
        setCurrentSignal(signal);
        return this.onHaltFinalized();
    }

    @Override
    protected synchronized WorkerExecutionContext onError(WorkerSignal signal) {
        BStruct error = signal.getSourceContext().getError();
        if (this.isFulfilled()) {
            printError(signal.getSourceContext().workerInfo.getWorkerName(), error);
            return null;
        }

        if ((workerCount - (++errorCount)) >= reqJoinCount) {
            workerErrors.put(signal.getSourceContext().workerInfo.getWorkerName(), error);
            return null;
        }
        this.setAsFulfilled();

        //This location means, no one will add errors to the workerErrors
        this.workerErrors.put(signal.getSourceContext().workerInfo.getWorkerName(), error);
        this.modifyDebugCommands(this.targetCtx, signal.getSourceContext());
        return BLangScheduler.errorThrown(this.targetCtx, this.createCallFailedError(signal.getSourceContext(),
                this.workerErrors));
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected synchronized WorkerExecutionContext onTimeout(WorkerSignal signal) {
        if (this.isFulfilled()) {
            return null;
        }
        this.setAsFulfilled();
        BMap<String, BRefType> mbMap = new BMap<>();
        channelNames.forEach((k, v) -> {
            BRefType workerRes = getWorkerResult(v);
            if (workerRes != null) {
                mbMap.put(k, workerRes);
            }
        });
        this.targetCtx.workerLocal.refRegs[timeoutVarReg] = (BRefType) mbMap;
        //Running the timeout call in a new thread.
        return BLangScheduler.resume(this.targetCtx, timeoutTargetIp, false);
    }

    @SuppressWarnings("rawtypes")
    protected WorkerExecutionContext onHaltFinalized() {
        BMap<String, BRefType> mbMap = new BMap<>();
        channelNames.forEach((k, v) -> {
            BRefType workerRes = getWorkerResult(v);
            if (workerRes != null) {
                mbMap.put(k, workerRes);
            }
        });
        this.targetCtx.workerLocal.refRegs[joinVarReg] = (BRefType) mbMap;
        this.modifyDebugCommands(this.targetCtx, this.currentSignal.getSourceContext());
        return BLangScheduler.resume(this.targetCtx, joinTargetIp, true);
    }

    @SuppressWarnings("rawtypes")
    private BRefType getWorkerResult(String channelName) {
        if (channelName == null) {
            return null;
        }
        WorkerDataChannel dataChannel = getWorkerDataChannel(channelName);
        return dataChannel.tryTakeData();
    }

    private void printError(String workerName, BStruct error) {
        BLangVMUtils.log("error in worker - " + workerName + System.lineSeparator()
                + BLangVMErrors.getPrintableStackTrace(error));
    }


    private BStruct createCallFailedError(WorkerExecutionContext context, Map<String, BStruct> errors) {
        return BLangVMErrors.createCallFailedException(context, errors);
    }

}
