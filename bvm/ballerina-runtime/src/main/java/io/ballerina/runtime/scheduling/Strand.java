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
package io.ballerina.runtime.scheduling;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.transactions.TransactionLocalContext;
import io.ballerina.runtime.values.ChannelDetails;
import io.ballerina.runtime.values.ErrorValue;
import io.ballerina.runtime.values.FutureValue;
import io.ballerina.runtime.values.MapValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.ballerina.runtime.scheduling.State.BLOCK_AND_YIELD;
import static io.ballerina.runtime.scheduling.State.BLOCK_ON_AND_YIELD;
import static io.ballerina.runtime.scheduling.State.RUNNABLE;
import static io.ballerina.runtime.scheduling.State.YIELD;

/**
 * Strand base class used with jvm code generation for functions.
 *
 * @since 0.955.0
 */

public class Strand {

    private static AtomicInteger nextStrandId = new AtomicInteger(0);

    private int id;
    private String name;
    private StrandMetadata metadata;

    public Object[] frames;
    public int resumeIndex;
    public Object returnValue;
    public BError panic;
    public Scheduler scheduler;
    public Strand parent;
    public WDChannels wdChannels;
    public FlushDetail flushDetail;
    public boolean blockedOnExtern;
    public Set<ChannelDetails> channelDetails;
    public Set<SchedulerItem> dependants;
    public ObserverContext observerContext;
    public boolean cancel;

    SchedulerItem schedulerItem;
    List<WaitContext> waitingContexts;
    WaitContext waitContext;
    ItemGroup strandGroup;

    private Map<String, Object> globalProps;
    public TransactionLocalContext currentTrxContext;
    public Stack<TransactionLocalContext> trxContexts;
    private State state;
    private final ReentrantLock strandLock;

    public Strand(String name, StrandMetadata metadata, Scheduler scheduler, Strand parent,
                  Map<String, Object> properties) {
        this.id = nextStrandId.incrementAndGet();
        this.scheduler = scheduler;
        this.wdChannels = new WDChannels();
        this.channelDetails = new HashSet<>();
        this.globalProps = new HashMap<>();
        this.state = RUNNABLE;
        this.dependants = new HashSet<>();
        this.strandLock = new ReentrantLock();
        this.waitingContexts = new ArrayList<>();
        this.name = name;
        this.metadata = metadata;
        this.trxContexts = new Stack<>();
        this.parent = parent;
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

    public Object getProperty(String key) {
        return this.globalProps.get(key);
    }

    public void setProperty(String key, Object value) {
        this.globalProps.put(key, value);
    }

    public boolean isInTransaction() {
        return this.currentTrxContext != null && this.currentTrxContext.isTransactional();
    }

    @Deprecated
    public void removeLocalTransactionContext() {
        this.currentTrxContext = null;
    }

    public void removeCurrentTrxContext() {
        if (!this.trxContexts.isEmpty()) {
            this.currentTrxContext = this.trxContexts.pop();
            return;
        }
        this.currentTrxContext = null;
    }

    public void setCurrentTransactionContext(TransactionLocalContext ctx) {
        if (this.currentTrxContext != null) {
            this.trxContexts.push(this.currentTrxContext);
        }
        this.currentTrxContext = ctx;
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
            this.setState(BLOCK_AND_YIELD);
            return null;
        } finally {
            this.flushDetail.flushLock.unlock();
        }
    }

    private void cleanUpFlush(ChannelDetails[] channels) {
        this.flushDetail.inProgress = false;
        this.flushDetail.flushedCount = 0;
        this.flushDetail.result = null;
        for (ChannelDetails channel : channels) {
            getWorkerDataChannel(channel).removeFlushWait();
        }
    }

    public void handleWaitMultiple(Map<String, FutureValue> keyValues, MapValue target) throws Throwable {
        WaitContext ctx = new WaitMultipleContext(this.schedulerItem);
        ctx.waitCount.set(keyValues.size());
        ctx.lock();
        for (Map.Entry<String, FutureValue> entry : keyValues.entrySet()) {
            FutureValue future = entry.getValue();
            // need to lock the future's strand since we cannot have a parallel state change
            future.strand.lock();
            if (future.isDone) {
                if (future.panic != null) {
                    ctx.completed = true;
                    ctx.waitCount.set(0);
                    this.setState(RUNNABLE);
                    future.strand.unlock();
                    ctx.unLock();
                    throw future.panic;
                }
                ctx.waitCount.decrementAndGet();
                target.put(StringUtils.fromString(entry.getKey()), future.result);
            } else {
                this.setState(BLOCK_ON_AND_YIELD);
                entry.getValue().strand.waitingContexts.add(ctx);
            }
            future.strand.unlock();
        }
        if (!this.isBlocked()) {
            ctx.waitCount.set(0);
            ctx.completed = true;
        } else {
            this.waitContext = ctx;
            ctx.intermediate = true;
        }
        ctx.unLock();
    }

    public WaitResult handleWaitAny(List<FutureValue> futures) throws Throwable {
        WaitResult waitResult = new WaitResult(false, null);
        WaitContext ctx = new WaitAnyContext(this.schedulerItem);
        ctx.lock();
        ctx.waitCount.set(futures.size());
        Object error = null;
        for (FutureValue future : futures) {
            // need to lock the future's strand since we cannot have a parallel state change
            try {
                future.strand.lock();
                if (future.isDone) {
                    if (future.panic != null) {
                        ctx.completed = true;
                        ctx.unLock();
                        throw future.panic;
                    }

                    if (TypeChecker.checkIsType(future.result, PredefinedTypes.TYPE_ERROR)) {
                        ctx.waitCount.decrementAndGet();
                        // if error, should wait for other futures as well
                        error = future.result;
                        continue;
                    }
                    waitResult = new WaitResult(true, future.result);
                    break;
                } else {
                    future.strand.waitingContexts.add(ctx);
                }
            } finally {
                future.strand.unlock();
            }
        }

        if (waitResult.done) {
            ctx.completed = true;
        } else if (ctx.waitCount.get() == 0) {
            ctx.completed = true;
            // all futures have error result
            waitResult = new WaitResult(true, error);
        } else {
            this.waitContext = ctx;
            this.setState(BLOCK_ON_AND_YIELD);
        }
        ctx.unLock();

        return waitResult;
    }

    public void updateChannelDetails(ChannelDetails[] channels) {
        for (ChannelDetails channel: channels) {
            this.channelDetails.add(channel);
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

    public void setState(State state) {
        this.lock();
        this.state = state;
        this.unlock();
    }

    public State getState() {
        return this.state;
    }

    public boolean isBlocked() {
        return (this.state.getStatus() & BLOCK_AND_YIELD.getStatus()) == BLOCK_AND_YIELD.getStatus();
    }

    public boolean isBlockedOn() {
        return (this.state.getStatus() & BLOCK_ON_AND_YIELD.getStatus()) == BLOCK_ON_AND_YIELD.getStatus();
    }

    public boolean isYielded() {
        return (this.state.getStatus() & YIELD.getStatus()) == YIELD.getStatus();
    }

    public boolean isBlockedOnExtern() {
        return blockedOnExtern;
    }

    public void lock() {
        this.strandLock.lock();
    }

    public void unlock() {
        this.strandLock.unlock();
    }

    public int getId() {
        return id;
    }

    /**
     * Gets the strand name. This will be optional. Strand name can be either name given in strand annotation or async
     * call or function pointer variable name.
     *
     * @return Optional strand name.
     */
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    /**
     * Gets @{@link StrandMetadata}.
     *
     * @return metadata of the strand.
     */
    public StrandMetadata getMetadata() {
        return metadata;
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
