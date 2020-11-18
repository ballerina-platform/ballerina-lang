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
package io.ballerina.runtime.scheduling;

import io.ballerina.runtime.values.ErrorValue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.ballerina.runtime.scheduling.State.BLOCK_AND_YIELD;

/**
 * This represents a worker data channel that is created for each worker to
 * worker interaction for each function call.
 *
 * @since 0.995.0
 */
public class WorkerDataChannel {

    private Strand receiver;
    private WaitingSender waitingSender;
    private WaitingSender flushSender;
    private ErrorValue error;
    private Throwable panic;
    private int senderCounter;
    private int receiverCounter;
    private boolean reschedule;

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
        try {
            acquireChannelLock();
            this.channel.add(new WorkerResult(data));
            this.senderCounter++;
            if (this.receiver != null) {
                this.receiver.scheduler.unblockStrand(this.receiver);
                this.receiver = null;
            }
        } finally {
            releaseChannelLock();
        }
    }

    /**
     * Put data for sync send.
     * @param data - data to be sent over the channel
     * @param strand - sending strand, that will be paused
     * @return error if receiver already in error state, else null
     * @throws Throwable panic
     */
    public Object syncSendData(Object data, Strand strand) throws Throwable {
        try {
            acquireChannelLock();
            if (!reschedule) {
                // this is a new message, not a reschedule
                this.channel.add(new WorkerResult(data, true));
                this.senderCounter++;
                this.waitingSender = new WaitingSender(strand, -1);

                if (this.receiver != null) {
                    // multiple checks are added to make sure this is
                    this.receiver.scheduler.unblockStrand(this.receiver);
                    this.receiver = null;
                } else if (this.panic != null) {
                    Throwable panic = this.panic;
                    this.panic = null;
                    throw panic;
                } else if (this.error != null) {
                    ErrorValue ret = this.error;
                    return ret;
                }

                reschedule = true;
                strand.setState(BLOCK_AND_YIELD);
                return null;
            }

            reschedule = false;
            if (this.panic != null && this.channel.peek() != null) {
                Throwable e = this.panic;
                throw e;
            } else if (this.error != null && this.channel.peek() != null) {
                ErrorValue ret = this.error;
                return ret;
            }

            // sync send done
            return null;
        } finally {
            releaseChannelLock();
        }
    }

    @SuppressWarnings("rawtypes")
    public Object tryTakeData(Strand strand) throws Throwable {
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
                } else if (this.flushSender != null && this.flushSender.flushCount == this.receiverCounter) {
                    this.flushSender.waitingStrand.flushDetail.flushLock.lock();
                    this.flushSender.waitingStrand.flushDetail.flushedCount++;
                    if (this.flushSender.waitingStrand.flushDetail.flushedCount
                            == this.flushSender.waitingStrand.flushDetail.flushChannels.length &&
                            this.flushSender.waitingStrand.isBlocked()) {
                            //will continue if this is a sync wait, will try to flush again if blocked on flush
                            this.flushSender.waitingStrand.scheduler.unblockStrand(this.flushSender.waitingStrand);

                    }
                    this.flushSender.waitingStrand.flushDetail.flushLock.unlock();
                    this.flushSender = null;
                }
                return result.value;
            } else if (this.panic != null && this.senderCounter == this.receiverCounter + 1) {
                this.receiverCounter++;
                throw this.panic;
            } else if (this.error != null && this.senderCounter == this.receiverCounter + 1) {
                this.receiverCounter++;
                return error;
            } else {
                this.receiver = strand;
                strand.setState(BLOCK_AND_YIELD);
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
     * Method to set receiver errors.
     *
     * @param error to be set
     */
    public void setReceiveError(ErrorValue error) {
        acquireChannelLock();
        this.error = error;
        this.receiverCounter++;
        if (this.flushSender != null) {
            this.flushSender.waitingStrand.flushDetail.flushLock.lock();
            Strand flushStrand = this.flushSender.waitingStrand;
            if (flushStrand.isBlocked()) {
                flushStrand.flushDetail.result = error;
                flushStrand.scheduler.unblockStrand(flushStrand);
            }
            this.flushSender.waitingStrand.flushDetail.flushLock.unlock();
            this.flushSender = null;
        } else if (this.waitingSender != null) {
            Strand waiting = this.waitingSender.waitingStrand;
            waiting.scheduler.unblockStrand(waiting);
            this.waitingSender = null;
        }
        releaseChannelLock();
    }

    /**
     * Method to flush channel.
     *
     * @param strand waiting for flush
     * @return error or null
     * @throws Throwable panic
     */
    public ErrorValue flushChannel(Strand strand) throws Throwable {
        acquireChannelLock();
        try {
            if (this.panic != null) {
                throw this.panic;
            } else if (this.error != null) {
                return this.error;
            } else if (this.receiverCounter == this.senderCounter) {
                strand.flushDetail.flushLock.lock();
                strand.flushDetail.flushedCount++;
                strand.flushDetail.flushLock.unlock();
                return null;
            }
            this.flushSender = new WaitingSender(strand, this.senderCounter);
            return null;
        } finally {
            releaseChannelLock();
        }
    }

    public void removeFlushWait() {
        acquireChannelLock();
        this.flushSender = null;
        releaseChannelLock();
    }

    /**
     * Method to set sender panics.
     *
     * @param panic to be set
     */
    public void setSendPanic(Throwable panic) {
        try {
            acquireChannelLock();
            this.panic  = panic;
            this.senderCounter++;
            if (this.receiver != null) {
                this.receiver.scheduler.unblockStrand(this.receiver);
                this.receiver = null;
            }
        } finally {
            releaseChannelLock();
        }
    }

    /**
     * Method to set receiver panics.
     *
     * @param panic to be set
     */
    public void setReceiverPanic(Throwable panic) {
        acquireChannelLock();
        this.panic  = panic;
        this.receiverCounter++;
        if (this.flushSender != null) {
            this.flushSender.waitingStrand.flushDetail.flushLock.lock();
            Strand flushStrand = this.flushSender.waitingStrand;
            this.flushSender.waitingStrand.flushDetail.panic = panic;
            if (flushStrand.isBlocked()) {
                flushStrand.scheduler.unblockStrand(flushStrand);
            }
            this.flushSender.waitingStrand.flushDetail.flushLock.unlock();
            this.flushSender = null;
        } else if (this.waitingSender != null) {
            Strand waiting = this.waitingSender.waitingStrand;
            waiting.scheduler.unblockStrand(waiting);
            this.waitingSender = null;
        }
        releaseChannelLock();
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
