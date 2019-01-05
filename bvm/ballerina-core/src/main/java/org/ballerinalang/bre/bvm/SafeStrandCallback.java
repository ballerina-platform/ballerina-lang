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

import org.ballerinalang.bre.bvm.Strand.State;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.util.codegen.CallableUnitInfo.ChannelDetails;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Safe BVM callback implementation.
 *
 * @since 0.985.0
 */
public class SafeStrandCallback extends StrandCallback {
    private static PrintStream errStream = System.err;

    private CallbackWaitHandler callbackWaitHandler;

    SafeStrandCallback(BType retType, WDChannels parentChannels, ChannelDetails[] sendIns) {
        super(retType, sendIns, parentChannels);
        this.callbackWaitHandler = new CallbackWaitHandler();
    }

    @Override
    public void signal() {
        try {
            this.callbackWaitHandler.dataLock.lock();
            super.signal();

            if (this.callbackWaitHandler.waitingStrands.isEmpty()) {
                return;
            }

            for (WaitingStrandInfo strandInfo : this.callbackWaitHandler.waitingStrands) {
                if (strandInfo.waitingStrand.strandWaitHandler.waitCompleted) {
                    continue;
                }

                if (strandInfo.waitForAll) {
                    List<WaitMultipleCallback> callbackList = new ArrayList<>();
                    callbackList.add(new WaitMultipleCallback(strandInfo.keyReg, this));
                    if (WaitCallbackHandler.handleReturnInWaitMultiple(strandInfo.waitingStrand,
                            strandInfo.retReg, callbackList)) {
                        BVMScheduler.stateChange(strandInfo.waitingStrand, State.PAUSED, State.RUNNABLE);
                        BVMScheduler.schedule(strandInfo.waitingStrand);
                    }
                    continue;
                }

                if (WaitCallbackHandler.handleReturnInWait(strandInfo.waitingStrand,
                        strandInfo.expType, strandInfo.retReg, this)) {
                    BVMScheduler.stateChange(strandInfo.waitingStrand, State.PAUSED, State.RUNNABLE);
                    BVMScheduler.schedule(strandInfo.waitingStrand);
                }
            }
        } finally {
            this.callbackWaitHandler.dataLock.unlock();
        }
    }

    public void setError(BError error) {
        super.setError(error);
        //Printing current stack trace for strand callback
        //This will be printed regardless of the parent strand handling this error or not.
        //This may be improved to log the error only if parent strand doesn't handle it.
        errStream.println("error: " + BLangVMErrors.getPrintableStackTrace(error));
    }

    public void setErrorForCancelledFuture(BError error) {
        super.setError(error);
    }

    void acquireDataLock() {
        this.callbackWaitHandler.dataLock.lock();
    }

    void releaseDataLock() {
        this.callbackWaitHandler.dataLock.unlock();
    }

    void configureWaitHandler(Strand waitingStrand, boolean waitForAll, BType expType, int retReg, int keyReg) {
        WaitingStrandInfo strandInfo = new WaitingStrandInfo(waitingStrand, waitForAll, expType, retReg, keyReg);
        this.callbackWaitHandler.waitingStrands.add(strandInfo);
    }

    /**
     * This class holds relevant data for callback wait handling related to callback side.
     */
    public static class CallbackWaitHandler {
        private Lock dataLock;
        List<WaitingStrandInfo> waitingStrands;

        public CallbackWaitHandler() {
            dataLock = new ReentrantLock();
            waitingStrands = new LinkedList<>();
        }
    }

    /**
     * This class holds relevant data for wait for all callbacks.
     */
    public static class WaitMultipleCallback {
        private int keyRegIndex;
        private SafeStrandCallback callback;

        public WaitMultipleCallback(int keyRegIndex, SafeStrandCallback callback) {
            this.keyRegIndex = keyRegIndex;
            this.callback = callback;
        }

        public int getKeyRegIndex() {
            return keyRegIndex;
        }

        public SafeStrandCallback getCallback() {
            return callback;
        }
    }

    /**
     * This class holds info of a waiting strand for a particular future.
     */
    public static class WaitingStrandInfo {
        Strand waitingStrand;
        boolean waitForAll;
        BType expType;
        int retReg;

        //WaitForAll
        int keyReg;

        public WaitingStrandInfo(Strand waitingStrand, boolean waitForAll, BType expType, int retReg) {
            this.waitingStrand = waitingStrand;
            this.waitForAll = waitForAll;
            this.expType = expType;
            this.retReg = retReg;
        }

        public WaitingStrandInfo(Strand waitingStrand, boolean waitForAll, BType expType, int retReg, int keyReg) {
            this.waitingStrand = waitingStrand;
            this.waitForAll = waitForAll;
            this.expType = expType;
            this.retReg = retReg;
            this.keyReg = keyReg;
        }
    }

}
