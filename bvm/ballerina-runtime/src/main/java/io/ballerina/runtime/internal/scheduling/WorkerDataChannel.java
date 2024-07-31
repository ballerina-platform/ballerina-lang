/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.internal.values.ErrorValue;

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
    private WaitingSender waitingSender;
    private WaitingSender flushSender;
    private ErrorValue error;
    private Throwable panic;
    private int senderCounter;
    private int receiverCounter;
    private boolean reschedule;

    private final Lock channelLock;

    protected String chnlName;
    protected int callCount = 0;

    @SuppressWarnings("rawtypes")
    private final Queue<WorkerResult> channel = new LinkedList<>();
    private State state;

    public WorkerDataChannel() {
        this.channelLock = new ReentrantLock();
        this.senderCounter = 0;
        this.receiverCounter = 0;
        this.state = State.OPEN;
    }

    public WorkerDataChannel(String channelName) {
        this();
        this.chnlName = channelName;
    }

    public void acquireChannelLock() {
        this.channelLock.lock();
    }

    public void releaseChannelLock() {
        this.channelLock.unlock();
    }

    public boolean isClosed() {
        return this.state == State.CLOSED || this.state == State.AUTO_CLOSED;
    }

    private void close(State state) {
        try {
            acquireChannelLock();
            this.state = state;
            if (this.receiver != null) {
                this.receiver = null;
            }
        } finally {
            releaseChannelLock();
        }
    }

    public State getState() {
        return this.state;
    }

    public enum State {
        OPEN, AUTO_CLOSED, CLOSED
    }

    @SuppressWarnings("rawtypes")
    public void sendData(Object data, Strand sender) {
        if (isClosed()) {
            callCount++;
            return;
        }
        try {
            acquireChannelLock();
            this.channel.add(new WorkerResult(data));
            this.senderCounter++;
            if (this.receiver != null && receiver.scheduler != null) {
                this.receiver = null;
            }
            callCount++;
        } finally {
            releaseChannelLock();
        }
    }

    public void autoClose() {
        close(State.AUTO_CLOSED);
    }

    public void close() {
        close(State.CLOSED);
    }


    public Object syncSendData(Object data, Strand strand) throws Throwable {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Object tryTakeData(Strand strand) throws Throwable {
        return tryTakeData(strand, false);
    }

    public Object tryTakeData(Strand strand, boolean isMultiple) throws Throwable {
        return null;
    }


    public void setSendError(ErrorValue error) {

    }


    public void setReceiveError(ErrorValue error) {

    }

    public ErrorValue flushChannel(Strand strand) throws Throwable {
        return null;
    }

    public void removeFlushWait() {
        acquireChannelLock();
        this.flushSender = null;
        releaseChannelLock();
    }

    public void setSendPanic(Throwable panic) {

    }

    public void setReceiverPanic(Throwable panic) {

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
