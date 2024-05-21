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

package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.values.ChannelDetails;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.transactions.TransactionLocalContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
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

import static io.ballerina.runtime.api.constants.RuntimeConstants.CURRENT_TRANSACTION_CONTEXT_PROPERTY;
import static io.ballerina.runtime.internal.scheduling.State.BLOCK_AND_YIELD;
import static io.ballerina.runtime.internal.scheduling.State.BLOCK_ON_AND_YIELD;
import static io.ballerina.runtime.internal.scheduling.State.DONE;
import static io.ballerina.runtime.internal.scheduling.State.RUNNABLE;
import static io.ballerina.runtime.internal.scheduling.State.YIELD;

/**
 * Strand base class used with jvm code generation for functions.
 *
 * @since 0.955.0
 */
public class Strand {

    private static final AtomicInteger nextStrandId = new AtomicInteger(0);

    private final int id;
    private final String name;
    private final StrandMetadata metadata;

    public Stack<FunctionFrame> frames;
    public int resumeIndex;
    public int functionInvocation;
    public Object returnValue;
    public BError panic;
    public Scheduler scheduler;
    public Strand parent;
    public WDChannels wdChannels;
    public FlushDetail flushDetail;
    public boolean blockedOnExtern;
    public Set<ChannelDetails> channelDetails;
    public Set<SchedulerItem> dependants;
    public boolean cancel;
    public int acquiredLockCount;

    SchedulerItem schedulerItem;
    List<WaitContext> waitingContexts;
    WaitContext waitContext;
    ItemGroup strandGroup;

    private Map<String, Object> globalProps;
    public TransactionLocalContext currentTrxContext;
    public Stack<TransactionLocalContext> trxContexts;
    private State state;
    private final ReentrantLock strandLock;
    public BMap<BString, Object> workerReceiveMap = null;
    public int channelCount = 0;

    public Strand(String name, StrandMetadata metadata, Scheduler scheduler, Strand parent,
                  Map<String, Object> properties) {
        this.id = nextStrandId.incrementAndGet();
        this.scheduler = scheduler;
        this.wdChannels = new WDChannels();
        this.channelDetails = new HashSet<>();
        this.state = RUNNABLE;
        this.dependants = new HashSet<>();
        this.strandLock = new ReentrantLock();
        this.waitingContexts = new ArrayList<>();
        this.name = name;
        this.metadata = metadata;
        this.trxContexts = new Stack<>();
        this.parent = parent;

        //TODO: improve by using a copy on write map #26710
        if (properties != null) {
            this.globalProps = properties;
        } else if (parent != null) {
            this.globalProps = new HashMap<>(parent.globalProps);
        } else {
            this.globalProps = new HashMap<>();
        }
    }
    public Strand(String name, StrandMetadata metadata, Scheduler scheduler, Strand parent,
                  Map<String, Object> properties, TransactionLocalContext currentTrxContext) {
        this(name, metadata, scheduler, parent, properties);
        if (currentTrxContext != null) {
            this.trxContexts = parent.trxContexts;
            this.trxContexts.push(currentTrxContext);
            this.currentTrxContext = currentTrxContext;
        } else {
            Object currentContext = globalProps.get(CURRENT_TRANSACTION_CONTEXT_PROPERTY);
            if (currentContext != null) {
                TransactionLocalContext branchedContext =
                        createTrxContextBranch((TransactionLocalContext) currentContext, this.id);
                setCurrentTransactionContext(branchedContext);
            }
        }
    }

    public static int getCreatedStrandCount() {
        return nextStrandId.get();
    }

    private TransactionLocalContext createTrxContextBranch(TransactionLocalContext currentTrxContext,
                                                           int strandName) {
        TransactionLocalContext trxCtx = TransactionLocalContext
                .createTransactionParticipantLocalCtx(currentTrxContext.getGlobalTransactionId(),
                        currentTrxContext.getURL(), currentTrxContext.getProtocol(),
                        currentTrxContext.getInfoRecord());
        String currentTrxBlockId = currentTrxContext.getCurrentTransactionBlockId();
        if (currentTrxBlockId.contains("_")) {
            // remove the parent strand id from the transaction block id
            currentTrxBlockId = currentTrxBlockId.split("_")[0];
        }
        trxCtx.addCurrentTransactionBlockId(currentTrxBlockId + "_" + strandName);
        trxCtx.setTransactionContextStore(currentTrxContext.getTransactionContextStore());
        return trxCtx;
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
            globalProps.put(CURRENT_TRANSACTION_CONTEXT_PROPERTY, this.currentTrxContext);
            return;
        }
        globalProps.remove(CURRENT_TRANSACTION_CONTEXT_PROPERTY);
        this.currentTrxContext = null;
    }

    public void setCurrentTransactionContext(TransactionLocalContext ctx) {
        if (this.currentTrxContext != null) {
            this.trxContexts.push(this.currentTrxContext);
        }
        this.currentTrxContext = ctx;
        globalProps.putIfAbsent(CURRENT_TRANSACTION_CONTEXT_PROPERTY, this.currentTrxContext);
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

            for (ChannelDetails channel : channels) {
                ErrorValue error = getWorkerDataChannel(channel).flushChannel(this);
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
        this.flushDetail.flushLock.unlock();
        for (ChannelDetails channel : channels) {
            getWorkerDataChannel(channel).removeFlushWait();
        }
        this.flushDetail.flushLock.lock();
    }

    public void handleWaitMultiple(Map<String, FutureValue> keyValues, MapValue<BString, Object> target)
            throws Throwable {
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
                if (future.hasWaited() && target.getNativeData(entry.getKey()) == null) {
                    target.put(StringUtils.fromString(entry.getKey()), createWaitOnSameFutureError());
                } else {
                    future.setWaited(true);
                    target.addNativeData(entry.getKey(), true);
                    target.put(StringUtils.fromString(entry.getKey()), future.result);
                }
                ctx.waitCount.decrementAndGet();
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
        int waitedCount = 0;
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
                    if (future.hasWaited()) {
                        waitedCount++;
                    }
                    boolean isErrorResult = TypeChecker.checkIsType(future.result, PredefinedTypes.TYPE_ERROR);
                    if (isErrorResult) {
                        ctx.waitCount.decrementAndGet();
                        // if error, should wait for other futures as well
                        error = future.result;
                    }
                    if (!(future.hasWaited() || isErrorResult)) {
                        waitResult = new WaitResult(true, future.result);
                        future.setWaited(true);
                        break;
                    }
                    future.setWaited(true);
                } else {
                    future.strand.waitingContexts.add(ctx);
                }
            } finally {
                future.strand.unlock();
            }
        }
        if (waitedCount == futures.size()) {
            waitResult = new WaitResult(true, createWaitOnSameFutureError());
        } else if (waitResult.done) {
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

    private BError createWaitOnSameFutureError() {
        return ErrorCreator.createError(StringUtils.fromString("multiple waits on the same future is not allowed"));
    }

    public void updateChannelDetails(ChannelDetails[] channels) {
        Collections.addAll(this.channelDetails, channels);
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

    public ItemGroup getStrandGroup() {
        return strandGroup;
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

    public String dumpState() {
        StringBuilder strandInfo = new StringBuilder("\tstrand " + this.id);
        if (this.name != null && this.getName().isPresent()) {
            strandInfo.append(" \"").append(this.getName().get()).append("\"");
        }

        strandInfo.append(" [");
        StrandMetadata strandMetadata = this.metadata;
        if (strandMetadata == null) {
            strandInfo.append("N/A");
        } else {
            strandInfo.append(strandMetadata.getModuleOrg()).append(".").append(strandMetadata.getModuleName())
                    .append(".").append(strandMetadata.getModuleVersion()).append(":")
                    .append(strandMetadata.getParentFunctionName());
        }
        if (this.parent != null) {
            strandInfo.append("][").append(this.parent.getId());
        }
        strandInfo.append("] [");

        String closingBracketWithNewLines = "]\n\n";
        if (this.isYielded()) {
            getInfoFromYieldedState(strandInfo, closingBracketWithNewLines);
        } else if (this.getState().equals(DONE)) {
            strandInfo.append(DONE).append(closingBracketWithNewLines);
        } else {
            strandInfo.append(RUNNABLE).append(closingBracketWithNewLines);
        }

        return strandInfo.toString();
    }

    private void getInfoFromYieldedState(StringBuilder strandInfo, String closingBracketWithNewLines) {
        Stack<FunctionFrame> strandFrames = this.frames;
        if ((strandFrames == null) || (strandFrames.isEmpty())) {
            // this means the strand frames is changed, hence the state is runnable
            strandInfo.append(RUNNABLE).append(closingBracketWithNewLines);
            return;
        }

        StringBuilder frameStackTrace = new StringBuilder();
        String stringPrefix = "\t\tat\t";
        String yieldStatus = "BLOCKED";
        boolean noPickedYieldStatus = true;
        try {
            for (FunctionFrame frame : strandFrames) {
                if (noPickedYieldStatus) {
                    yieldStatus = frame.yieldStatus;
                    noPickedYieldStatus = false;
                }
                String yieldLocation = frame.yieldLocation;
                frameStackTrace.append(stringPrefix).append(yieldLocation);
                frameStackTrace.append("\n");
                stringPrefix = "\t\t  \t";
            }
        } catch (ConcurrentModificationException ce) {
            // this exception can be thrown when frames get added or removed while it is being iterated
            // that means now the strand state is changed from yielded state to runnable state
            strandInfo.append(RUNNABLE).append(closingBracketWithNewLines);
            return;
        }
        if (!this.isYielded() || noPickedYieldStatus) {
            // if frames have got empty, noPickedYieldStatus is true, then the state has changed to runnable
            strandInfo.append(RUNNABLE).append(closingBracketWithNewLines);
            return;
        }
        strandInfo.append(yieldStatus).append("]:\n");
        strandInfo.append(frameStackTrace).append("\n");
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
