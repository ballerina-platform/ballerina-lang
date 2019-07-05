/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ChannelDetails;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.MapValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Strand base class used with jvm code generation for functions.
 *
 * @since 0.955.0
 */

public class Strand {
    public boolean yield;
    public Object[] frames;
    public int resumeIndex;
    public Object returnValue;
    public boolean blocked;
    public List<Strand> blockedOn;
    public Scheduler scheduler;
    public Strand parent = null;
    public WDChannels wdChannels;
    public FlushDetail flushDetail;
    public boolean blockedOnExtern;
    public Set<ChannelDetails> channelDetails;
    private Map<String, Object> globalProps;
    public boolean cancel;
    public ObserverContext observerContext;

    public Strand(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.wdChannels = new WDChannels();
        this.blockedOn = new CopyOnWriteArrayList();
        this.channelDetails = new HashSet<>();
        this.globalProps = new HashMap<>();
    }

    public Strand(Scheduler scheduler, Strand parent, Map<String, Object> properties) {
        this.scheduler = scheduler;
        this.parent = parent;
        this.wdChannels = new WDChannels();
        this.blockedOn = new CopyOnWriteArrayList();
        this.channelDetails = new HashSet<>();
        this.globalProps = properties != null ? properties : new HashMap<>();
    }

    public void handleChannelError(ChannelDetails[] channels, ErrorValue error) {
        for (int i = 0; i < channels.length; i++) {
            ChannelDetails channelDetails = channels[i];
            WorkerDataChannel channel = getWorkerDataChannel(channelDetails);

            if (channels[i].send) {
                channel.setSendError(error);
            } else {
                channel.setReceiveError(error);
            }
        }
    }

    public void setReturnValues(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Object getProperty(String key) {
        return this.globalProps.get(key);
    }

    public void setProperty(String key, Object value) {
        this.globalProps.put(key, value);
    }

    public ErrorValue handleFlush(ChannelDetails[] channels) throws Throwable {
        try {
            if (flushDetail == null) {
                this.flushDetail = new FlushDetail(channels);
            }
            this.flushDetail.flushLock.lock();
            if (flushDetail.inProgress) {
                // this is a reschedule when flush is completed
                if (this.flushDetail.panic != null) {
                    throw this.flushDetail.panic;
                }
                ErrorValue result = this.flushDetail.result;
                cleanUpFlush(channels);
                return result;
            } else {
                //this can be another flush in the same worker
                this.flushDetail.panic = null;
                this.flushDetail.result = null;
                this.flushDetail.flushChannels = channels;
            }

            for (int i = 0; i < channels.length; i++) {
                ErrorValue error = getWorkerDataChannel(channels[i]).flushChannel(this);
                if (error != null) {
                    cleanUpFlush(channels);
                    return error;
                } else if (this.flushDetail.flushedCount == this.flushDetail.flushChannels.length) {
                    // flush completed
                    cleanUpFlush(channels);
                    return null;
                }
            }
            flushDetail.inProgress = true;
            this.yield = true;
            this.blocked = true;
            return null;
        } finally {
            this.flushDetail.flushLock.unlock();
        }
    }

    private void cleanUpFlush(ChannelDetails[] channels) {
        this.flushDetail.inProgress = false;
        this.flushDetail.flushedCount = 0;
        this.flushDetail.result = null;
        for (int i = 0; i < channels.length; i++) {
            getWorkerDataChannel(channels[i]).removeFlushWait();
        }
    }

    public void handleWaitMultiple(Map<String, FutureValue> keyValues, MapValue target) throws Throwable {
        this.blockedOn.clear();
        for (Map.Entry<String, FutureValue> entry : keyValues.entrySet()) {
            synchronized (entry.getValue()) {
                if (entry.getValue().isDone) {
                    if (entry.getValue().panic != null) {
                        this.blockedOn.clear();
                        throw entry.getValue().panic;
                    }
                    target.put(entry.getKey(), entry.getValue().result);
                } else {
                    this.yield = true;
                    this.blocked = true;
                    this.blockedOn.add(entry.getValue().strand);
                }
            }
        }
    }

    public WaitResult handleWaitAny(List<FutureValue> futures) throws Throwable {
        WaitResult waitResult = new WaitResult(false, null);
        int completed = 0;
        Object error = null;
        for (FutureValue future : futures) {
            synchronized (future) {
                if (future.isDone) {
                    completed++;
                    if (future.panic != null) {
                        throw future.panic;
                    }

                    if (TypeChecker.checkIsType(future.result, BTypes.typeError)) {
                        // if error, should wait for other futures as well
                        error = future.result;
                        continue;
                    }
                    waitResult = new WaitResult(true, future.result);
                    break;
                } else {
                    this.blockedOn.add(future.strand);
                }
            }
        }

        if (waitResult.done) {
            this.blockedOn.clear();
        } else if (completed == futures.size()) {
            // all futures have error result
            this.blockedOn.clear();
            waitResult = new WaitResult(true, error);
        } else {
            this.yield = true;
            this.blocked = true;
        }

        return waitResult;
    }

    public void updateChannelDetails(ChannelDetails[] channels) {
        for (ChannelDetails details : channels) {
            this.channelDetails.add(details);
        }
    }

    private WorkerDataChannel getWorkerDataChannel(ChannelDetails channel) {
        WorkerDataChannel dataChannel;
        if (channel.channelInSameStrand) {
            dataChannel = this.wdChannels.getWorkerDataChannel(channel.name);
        } else {
            dataChannel = this.parent.wdChannels.getWorkerDataChannel(channel.name);
        }
        return dataChannel;
    }

    /**
     * Class to hold flush action related details.
     *
     * 0.995.0
     */
    public static class FlushDetail {
        public ChannelDetails[] flushChannels;
        public int flushedCount;
        public Lock flushLock;
        public ErrorValue result;
        public boolean inProgress;
        public Throwable panic;

        public FlushDetail(ChannelDetails[] flushChannels) {
            this.flushChannels = flushChannels;
            this.flushedCount = 0;
            this.flushLock = new ReentrantLock();
            this.result = null;
            this.inProgress = false;
        }
    }

    /**
     * Holds both waiting state and result.
     *
     * 0.995.0
     */
    public static class WaitResult {
        public boolean done;
        public Object result;

        public WaitResult(boolean done, Object result) {
            this.done = done;
            this.result = result;
        }
    }
}
