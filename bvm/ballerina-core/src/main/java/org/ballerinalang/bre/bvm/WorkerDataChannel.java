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
     */
    public synchronized void putData(BRefType data, Strand waitingCtx, int retReg) {
        this.channel.add(new WorkerResult(data, true));
        this.waitingSender = new WaitingSender(waitingCtx, retReg);
        BVMScheduler.stateChange(waitingCtx, State.RUNNABLE, State.PAUSED);
        if (this.pendingCtx != null) {
            BVMScheduler.stateChange(this.pendingCtx, State.PAUSED, State.RUNNABLE);
            BVMScheduler.schedule(this.pendingCtx);
            this.pendingCtx = null;
        }
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
                BVMScheduler.stateChange(this.pendingCtx, State.PAUSED, State.RUNNABLE);
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

    public synchronized boolean tryFlush(Strand ctx) {
        if (!channel.isEmpty()) {
            waitingSender = new WaitingSender(ctx);
            //flush should wait and need to recheck upon fetching data
            ctx.currentFrame.ip--;
            BVMScheduler.stateChange(ctx, State.RUNNABLE, State.PAUSED);
            return false;
        }
        return true;
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

        public WaitingSender(Strand strand) {

            this.waitingCtx = strand;
        }
    }
}
