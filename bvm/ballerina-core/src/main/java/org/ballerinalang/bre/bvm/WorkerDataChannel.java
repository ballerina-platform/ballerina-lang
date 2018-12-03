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
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BRefType;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This represents a worker data channel that is created for each worker to
 * worker interaction for each function call.
 */
public class WorkerDataChannel {

    private Strand pendingCtx;
    private WaitingSender waitingSender;
    private BRefType error;
    private BError panic;

    @SuppressWarnings("rawtypes")
    private Queue<WorkerResult> channel = new LinkedList<>();

    @SuppressWarnings("rawtypes")
    public synchronized void putData(BRefType data) {
        this.channel.add(new WorkerResult(data));
        if (this.pendingCtx != null) {
            BVMScheduler.stateChange(this.pendingCtx, State.PAUSED, State.RUNNABLE);
            BVMScheduler.schedule(this.pendingCtx);
            this.pendingCtx = null;
        }
    }

    /**
     * Put data for async send.
     * @param data - data to be sent over the channel
     * @param waitingCtx - sending context, that will be paused
     * @param retReg - Reg index to assign result of the send
     * @return true if execution can continue
     */
    public synchronized boolean putData(BRefType data, Strand waitingCtx, int retReg) {
        if (this.error != null) {
            waitingCtx.currentFrame.refRegs[retReg] = this.error;
            this.error = null;
            return true;
        }
        if (this.panic != null) {
            waitingCtx.setError(this.panic);
            this.panic = null;
            return true;
        }
        this.channel.add(new WorkerResult(data, true));
        this.waitingSender = new WaitingSender(waitingCtx, retReg);
        BVMScheduler.stateChange(waitingCtx, State.RUNNABLE, State.PAUSED);
        if (this.pendingCtx != null) {
            BVMScheduler.stateChange(this.pendingCtx, State.PAUSED, State.RUNNABLE);
            BVMScheduler.schedule(this.pendingCtx);
            this.pendingCtx = null;
        }
        return false;
    }
    
    @SuppressWarnings("rawtypes")
    public synchronized WorkerResult tryTakeData(Strand ctx) {
        WorkerResult result = this.channel.peek();
        if (result != null) {
            this.channel.remove();
            if (waitingSender != null) {
                if (result.isSync) {
                    waitingSender.waitingCtx.currentFrame.refRegs[waitingSender.returnReg] = null;
                }
                //will continue if this is a sync wait, will try to flush again if blocked on flush
                BVMScheduler.stateChange(this.waitingSender.waitingCtx, State.PAUSED, State.RUNNABLE);
                BVMScheduler.schedule(waitingSender.waitingCtx);
            }
            return result;
        } else {
            this.pendingCtx = ctx;
            ctx.currentFrame.ip--; // we are going to execute the same worker receive operation later
            BVMScheduler.stateChange(ctx, State.RUNNABLE, State.PAUSED);
            return null;
        }
    }

    /**
     * Check whether target strand already in a failed state.
     * @param ctx source strand
     * @param retReg return registry index of the flush
     * @return true if target failed
     */
    public synchronized boolean isFailed(Strand ctx, int retReg) {
        if (this.error != null) {
            ctx.currentFrame.refRegs[retReg] = this.error;
            this.error = null;
            return true;
        }

        return false;
    }

    /**
     * Check whether target strand already in a panic state.
     * @param ctx source strand
     * @param retReg return registry index of the flush
     * @return true if target failed
     */
    public synchronized boolean isPanicked(Strand ctx, int retReg) {
        if (this.panic != null) {
            ctx.setError(this.panic);
            this.panic = null;
            return true;
        }

        return false;
    }

    /**
     * Check if all the messages sent.
     * @param ctx source strand
     * @param retReg return registry index of the flush
     * @return true if messages are sent
     */
    public synchronized boolean isDataSent(Strand ctx, int retReg) {
        if (channel.isEmpty()) {
            return true;
        }
        waitingSender = new WaitingSender(ctx, retReg);
        ctx.currentFrame.ip--;
        BVMScheduler.stateChange(ctx, State.RUNNABLE, State.PAUSED);
        return false;
    }

    /**
     * Set the state as error if the receiving worker is in error state.
     * @param error the BError of the receiving worker
     */
    public synchronized void setError(BRefType error) {
        this.error = error;
        if (this.waitingSender != null) {
            this.waitingSender.waitingCtx.currentFrame.refRegs[waitingSender.returnReg] = error;
            BVMScheduler.stateChange(this.waitingSender.waitingCtx, State.PAUSED, State.RUNNABLE);
            BVMScheduler.schedule(waitingSender.waitingCtx);
        }
    }

    public synchronized void setPanic(BError error) {
        this.panic  = error;
        if (this.waitingSender != null) {
            this.waitingSender.waitingCtx.setError(error);
            BVMScheduler.stateChange(this.waitingSender.waitingCtx, State.PAUSED, State.RUNNABLE);
            BVM.handleError(this.waitingSender.waitingCtx);
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized WorkerResult tryTakeData() {
        return this.channel.poll();
    }

    /**
     * This represents a worker result value. This is done as a value to be used in the
     * queues used for worker communication. In this way, the queue can distinguish the
     * case of a value not there when the peek, by returning null, and then, if they
     * return a WorkerResult, the value inside it can be either null or not to mention
     * Ballerina null value and non-null value.
     */
    public static class WorkerResult {

        public BRefType value;
        public boolean isSync;


        public WorkerResult(BRefType value) {
            this.value = value;
        }

        public WorkerResult(BRefType value, boolean sync) {
            this.value = value;
            this.isSync = sync;
        }

    }

    /**
     * This represents the sender of the channel. If the sender is available, then we assume it is waiting for the
     * data retrieval. Upon fetching data, it will be resumed if a sync send or will try to flush.
     */
    public static class WaitingSender {

        public Strand waitingCtx;
        public int returnReg;

        public WaitingSender(Strand strand, int reg) {

            this.waitingCtx = strand;
            this.returnReg = reg;
        }
    }
}
