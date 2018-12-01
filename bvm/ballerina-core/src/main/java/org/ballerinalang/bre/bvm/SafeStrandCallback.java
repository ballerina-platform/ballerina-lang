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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Safe BVM callback implementation.
 *
 * @since 0.985.0
 */
public class SafeStrandCallback extends StrandCallback {

    private volatile boolean done;

    private CallbackWaitHandler callbackWaitHandler;

    //TODO try to generalize below to normal data channels
    WDChannels parentChannels;

    WDChannels wdChannels;

    String[] sendIns;

    SafeStrandCallback(BType retType) {
        super(retType);
        this.callbackWaitHandler = new CallbackWaitHandler();
        this.done = false;
        this.wdChannels = new WDChannels();
    }

    @Override
    public void signal() {
        super.signal();
        try {
            this.callbackWaitHandler.dataLock.lock();
            this.done = true;
            if (this.getErrorVal() != null) {
                //mark all channels as errored
            }
            if (this.callbackWaitHandler.waitingStrand == null) {
                return;
            }
            if (this.callbackWaitHandler.waitingStrand.strandWaitHandler.waitCompleted) {
                return;
            }
            if (this.callbackWaitHandler.waitForAll) {
                Map<Integer, SafeStrandCallback> callbackHashMap = new HashMap();
                callbackHashMap.put(this.callbackWaitHandler.keyReg, this);

                Strand resultStrand = CallbackReturnHandler.handleReturn(this.callbackWaitHandler.waitingStrand,
                        this.callbackWaitHandler.retReg, callbackHashMap);
                if (resultStrand != null) {
                    BVMScheduler.stateChange(resultStrand, State.PAUSED, State.RUNNABLE);
                    BVMScheduler.schedule(resultStrand);
                }
                return;
            }
            Strand resultStrand = CallbackReturnHandler.handleReturn(this.callbackWaitHandler.waitingStrand,
                    this.callbackWaitHandler.expType, this.callbackWaitHandler.retReg, this);
            if (resultStrand != null) {
                BVMScheduler.stateChange(resultStrand, State.PAUSED, State.RUNNABLE);
                BVMScheduler.schedule(resultStrand);
            }
        } finally {
            this.callbackWaitHandler.dataLock.unlock();
        }
    }

    @Override
    public WDChannels getWorkerDataChannels() {

        return this.wdChannels;
    }

    void acquireDataLock() {
        this.callbackWaitHandler.dataLock.lock();
    }

    void releaseDataLock() {
        this.callbackWaitHandler.dataLock.unlock();
    }

    void configureWaitHandler(Strand waitingStrand, boolean waitForAll, BType expType, int retReg, int keyReg) {
        this.callbackWaitHandler.waitingStrand = waitingStrand;
        this.callbackWaitHandler.waitForAll = waitForAll;
        this.callbackWaitHandler.expType = expType;
        this.callbackWaitHandler.retReg = retReg;
        this.callbackWaitHandler.keyReg = keyReg;
    }

    public boolean isDone() {
        return this.done;
    }

    /**
     * This class holds relevant data for callback wait handling related to callback side.
     */
    public static class CallbackWaitHandler {
        private Lock dataLock;
        Strand waitingStrand;
        boolean waitForAll;
        BType expType;
        int retReg;

        //WaitForAll
        int keyReg;

        public CallbackWaitHandler() {
            dataLock = new ReentrantLock();
        }
    }
}
