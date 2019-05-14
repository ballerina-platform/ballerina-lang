/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.values.ErrorValue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This represents a worker data channel that is created for each worker to
 * worker interaction for each function call.
 *
 * @since 0.995.0
 */
public class WorkerDataChannel {

    private Strand receiver;
    private Strand sender;
    private WaitingSender waitingSender;
    private WaitingSender flushSender;
    private ErrorValue error;
    private ErrorValue panic;
    private int senderCounter;
    private int receiverCounter;
    private boolean syncSent = false;

    private Lock channelLock;

    public String chnlName;

    @SuppressWarnings("rawtypes")
    private Queue<WorkerResult> channel = new LinkedList<>();

    public WorkerDataChannel() {
        this.channelLock = new ReentrantLock();
        this.senderCounter = 0;
        this.receiverCounter = 0;
    }
    public WorkerDataChannel(String channelName) {
        this.channelLock = new ReentrantLock();
        this.senderCounter = 0;
        this.receiverCounter = 0;
        this.chnlName = channelName;
    }

    public void acquireChannelLock() {
        this.channelLock.lock();
    }

    public void releaseChannelLock() {
        this.channelLock.unlock();
    }

    @SuppressWarnings("rawtypes")
    public void sendData(Object data, Strand sender) {
        this.sender = sender;
        acquireChannelLock();
        this.channel.add(new WorkerResult(data));
        this.senderCounter++;
        if (this.receiver != null) {
            this.receiver.scheduler.unblockStrand(this.receiver);
            this.receiver = null;
        }
        releaseChannelLock();
    }

    /**
     * Put data for sync send.
     * @param data - data to be sent over the channel
     * @param strand - sending strand, that will be paused
     * @return error if receiver already in error state, else null
     */
    public Object syncSendData(Object data, Strand strand) {
        try {
            acquireChannelLock();
            if (!syncSent) {
                // this is a new message, not a reschedule
                this.channel.add(new WorkerResult(data, true));
                this.senderCounter++;
                this.waitingSender = new WaitingSender(strand, -1);
            }

            if (this.receiver != null) {
                this.receiver.scheduler.unblockStrand(this.receiver);
                this.receiver = null;
                return null;
            }

            if (this.panic != null) {
                // TODO: Fix for receiver panics
            }

            if (this.error != null) {
                ErrorValue ret = this.error;
                this.error = null;
                return ret;
            }

            // could not send the message, should yield. Will pick the ret value from getErrorValue()
            if (!syncSent) {
                strand.blocked = true;
                strand.yield = true;
            }
            return null;
        } finally {
            this.syncSent = false;
            releaseChannelLock();
        }
    }
    
    @SuppressWarnings("rawtypes")
    public Object tryTakeData(Strand strand) {
        try {
            acquireChannelLock();
            WorkerResult result = this.channel.peek();
            if (result != null) {
                this.receiverCounter++;
                this.channel.remove();

                if (result.isSync) {
                    // sync sender will pick the this.error as result, which is null
                    Strand waiting  = this.waitingSender.waitingStrand;
                    waiting.scheduler.unblockStrand(waiting);
                    this.waitingSender = null;
                    this.syncSent = true;
                } else if (this.flushSender != null && this.flushSender.flushCount == this.receiverCounter) {
                    // TODO: Fix later for flush
                }
                return result.value;
            } else if (this.panic != null && this.senderCounter == this.receiverCounter + 1) {
                this.receiverCounter++;
                throw new RuntimeException(this.panic);
            } else if (this.error != null && this.senderCounter == this.receiverCounter + 1) {
                this.receiverCounter++;
                return error;
            } else {
                this.receiver = strand;
                strand.blocked = true;
                strand.yield = true;
                return null;
            }
        } finally {
            releaseChannelLock();
        }
    }

    /**
     * Set the state as error if the receiving worker is in error state.
     * @param error the BError of the receiving worker
     */
    public void setSendError(ErrorValue error) {
        acquireChannelLock();
        this.error = error;
        this.senderCounter++;
        if (this.receiver != null) {
            this.receiver.scheduler.unblockStrand(this.receiver);
            this.receiver = null;
        }
        releaseChannelLock();
    }

    /**
     * Method to set reciever errors.
     *
     * @param error to be set
     */
    public void setReceiveError(ErrorValue error) {
        acquireChannelLock();
        this.error = error;
        this.receiverCounter++;
        if (this.flushSender != null) {
            // TODO: FIX for Flush
            this.flushSender = null;
        } else if (this.waitingSender != null) {
            Strand waiting = this.waitingSender.waitingStrand;
            waiting.scheduler.unblockStrand(waiting);
            this.waitingSender = null;
        }
        releaseChannelLock();
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

        public Object value;
        public boolean isSync;


        public WorkerResult(Object value) {
            this.value = value;
        }

        public WorkerResult(Object value, boolean sync) {
            this.value = value;
            this.isSync = sync;
        }

    }

    /**
     * This represents the sender of the channel. If the sender is available, then we assume it is waiting for the
     * data retrieval. Upon fetching data, it will be resumed if a sync send or will try to flush.
     */
    public static class WaitingSender {

        public Strand waitingStrand;
        public int flushCount;

        public WaitingSender(Strand strand, int flushCount) {
            this.waitingStrand = strand;
            this.flushCount = flushCount;
        }
    }
}
