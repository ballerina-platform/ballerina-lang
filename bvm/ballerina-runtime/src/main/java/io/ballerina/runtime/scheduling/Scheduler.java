/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.scheduling;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.util.BLangConstants;
import io.ballerina.runtime.util.RuntimeUtils;
import io.ballerina.runtime.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.values.ChannelDetails;
import io.ballerina.runtime.values.FutureValue;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.ballerina.runtime.scheduling.ItemGroup.POISON_PILL;

/**
 * Strand scheduler for JBallerina.
 *
 * @since 0.995.0
 */
public class Scheduler {

    private PrintStream err = System.err;
    /**
     * Scheduler does not get killed if the immortal value is true. Specific to services.
     */
    public boolean immortal;
    /**
     * Strands that are ready for execution.
     */
    private BlockingQueue<ItemGroup> runnableList = new LinkedBlockingDeque<>();

    private static final ThreadLocal<StrandHolder> strandHolder = ThreadLocal.withInitial(StrandHolder::new);

    private AtomicInteger totalStrands = new AtomicInteger();

    private static String poolSizeConf = System.getenv(BLangConstants.BALLERINA_MAX_POOL_SIZE_ENV_VAR);

    /**
     * This can be changed by setting the BALLERINA_MAX_POOL_SIZE system variable.
     * Default is 100.
     */
    private final int numThreads;

    private static int poolSize = Runtime.getRuntime().availableProcessors() * 2;

    private Semaphore mainBlockSem;

    public Scheduler(boolean immortal) {
        try {
            if (poolSizeConf != null) {
                poolSize = Integer.parseInt(poolSizeConf);
            }
        } catch (Throwable t) {
            // Log and continue with default
            err.println("ballerina: error occurred in scheduler while reading system variable:" +
                                BLangConstants.BALLERINA_MAX_POOL_SIZE_ENV_VAR + ", " + t.getMessage());
        }
        this.numThreads = poolSize;
        this.immortal = immortal;
    }

    public Scheduler(int numThreads, boolean immortal) {
        this.numThreads = numThreads;
        this.immortal = immortal;
    }

    public static Strand getStrand() {
        Strand strand = strandHolder.get().strand;
        if (strand == null) {
            throw new IllegalStateException("strand is not accessible form non-strand-worker threads");
        }
        return strand;
    }

    /**
     * Schedules given function by creating a new strand group.
     *
     * @param params     parameters to underlying function.
     * @param fp         function ponter to be executed.
     * @param parent     parent of the new Strand that get created here.
     * @param returnType return type of the function.
     * @param strandName name for new strand
     * @param metadata   meta data of new strand
     * @return {@link FutureValue} reference to the given function pointer invocation.
     */
    public FutureValue scheduleFunction(Object[] params, BFunctionPointer<?, ?> fp, Strand parent, Type returnType,
                                        String strandName, StrandMetadata metadata) {
        return schedule(params, fp.getFunction(), parent, null, null, returnType, strandName, metadata);
    }

    /**
     * Schedules given function to the callers strand group.
     *
     * @param params     parameters to underlying function.
     * @param fp         function to be executed.
     * @param parent     parent of the new Strand that get created here.
     * @param returnType return type of the function.
     * @param strandName name for new strand
     * @param metadata   meta data of new strand
     * @return {@link FutureValue} reference to the given function invocation.
     */
    public FutureValue scheduleLocal(Object[] params, BFunctionPointer<?, ?> fp, Strand parent, Type returnType,
                                     String strandName, StrandMetadata metadata) {
        FutureValue future = createFuture(parent, null, null, returnType, strandName, metadata);
        return scheduleLocal(params, fp, parent, future);
    }

    public FutureValue scheduleLocal(Object[] params, BFunctionPointer<?, ?> fp, Strand parent, FutureValue future) {
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(fp.getFunction(), params, future);
        future.strand.schedulerItem = item;
        totalStrands.incrementAndGet();
        future.strand.strandGroup = parent.strandGroup;
        parent.strandGroup.add(item);
        if (parent.strandGroup.scheduled.compareAndSet(false, true)) {
            runnableList.add(future.strand.strandGroup);
        }
        return future;
    }

    /**
     * Add a task to the runnable list, which will eventually be executed by the Scheduler.
     *
     * @param params     parameters to be passed to the function
     * @param function   function to be executed
     * @param parent     parent strand that makes the request to schedule another
     * @param callback   to notify any listener when ever the execution of the given function is finished
     * @param properties request properties which requires for co-relation
     * @param returnType return type of the scheduled function
     * @param strandName name for new strand
     * @param metadata   meta data of new strand
     * @return Reference to the scheduled task
     */
    public FutureValue schedule(Object[] params, Function function, Strand parent, Callback callback,
                                Map<String, Object> properties, Type returnType, String strandName,
                                StrandMetadata metadata) {
        FutureValue future = createFuture(parent, callback, properties, returnType, strandName, metadata);
        return schedule(params, function, future);
    }

    /**
     * Add a task to the runnable list, which will eventually be executed by the Scheduler.
     *
     * @param params     parameters to be passed to the function
     * @param function   function to be executed
     * @param parent     parent strand that makes the request to schedule another
     * @param callback   to notify any listener when ever the execution of the given function is finished
     * @param strandName name for new strand
     * @param metadata   meta data of new strand
     * @return Reference to the scheduled task
     */
    public FutureValue schedule(Object[] params, Function function, Strand parent, Callback callback,
                                String strandName, StrandMetadata metadata) {
        FutureValue future = createFuture(parent, callback, null, PredefinedTypes.TYPE_NULL, strandName, metadata);
        return schedule(params, function, future);
    }

    private FutureValue schedule(Object[] params, Function function, FutureValue future) {
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(function, params, future);
        future.strand.schedulerItem = item;
        totalStrands.incrementAndGet();
        ItemGroup group = new ItemGroup(item);
        future.strand.strandGroup = group;
        group.scheduled.set(true);
        runnableList.add(group);
        return future;
    }

    /**
     * Add a void returning task to the runnable list, which will eventually be executed by the Scheduler.
     *
     * @param params     parameters to be passed to the function
     * @param consumer   consumer to be executed
     * @param parent     parent strand that makes the request to schedule another
     * @param callback   to notify any listener when ever the execution of the given function is finished
     * @param strandName name for new strand
     * @param metadata   meta data of new strand
     * @return Reference to the scheduled task
     */
    @Deprecated
    public FutureValue schedule(Object[] params, Consumer consumer, Strand parent, Callback callback,
                                String strandName, StrandMetadata metadata) {
        FutureValue future = createFuture(parent, callback, null, PredefinedTypes.TYPE_NULL, strandName, metadata);
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(consumer, params, future);
        future.strand.schedulerItem = item;
        totalStrands.incrementAndGet();
        ItemGroup group = new ItemGroup(item);
        future.strand.strandGroup = group;
        group.scheduled.set(true);
        runnableList.add(group);
        return future;
    }

    public void start() {
        this.mainBlockSem = new Semaphore(-(numThreads - 1));
        for (int i = 0; i < numThreads - 1; i++) {
            new Thread(this::runSafely, "jbal-strand-exec-" + i).start();
        }
        this.runSafely();
        try {
            this.mainBlockSem.acquire();
        } catch (InterruptedException e) {
            RuntimeUtils.printCrashLog(e);
        }
    }

    /**
     * Defensive programming to prevent unforeseen errors.
     */
    private void runSafely() {
        try {
            run();
        } catch (Throwable t) {
            RuntimeUtils.printCrashLog(t);
        }
    }

    /**
     * Executes tasks that are submitted to the Scheduler.
     */
    private void run() {
        while (true) {
            SchedulerItem item;
            ItemGroup group;
            try {
                group = runnableList.take();
            } catch (InterruptedException ignored) {
                continue;
            }

            if (group == POISON_PILL) {
                this.mainBlockSem.release();
                break;
            }

            while (!group.items.empty()) {
                Object result = null;
                Throwable panic = null;

                item = group.get();

                try {
                    strandHolder.get().strand = item.future.strand;
                    result = item.execute();
                } catch (Throwable e) {
                    panic = createError(e);
                    notifyChannels(item, panic);

                    if (!(panic instanceof BError)) {
                        RuntimeUtils.printCrashLog(panic);
                    }
                    // Please refer #18763.
                    // This logs cases where errors have occurred while strand is blocked.
                    if (item.isYielded()) {
                        RuntimeUtils.printCrashLog(panic);
                    }
                } finally {
                    strandHolder.get().strand = null;
                }
                postProcess(item, result, panic);
                if (group.items.empty()) {
                    group.scheduled.set(false);
                }
            }
        }
    }

    /**
     * Processes the item after executing for notifying blocked items etc.
     */
    private void postProcess(SchedulerItem item, Object result, Throwable panic) {
        switch (item.getState()) {
            case BLOCK_AND_YIELD:
                item.future.strand.lock();
                // need to recheck due to concurrency, unblockStrand() may have changed state
                if (item.getState().getStatus() == State.YIELD.getStatus()) {
                    reschedule(item);
                    item.future.strand.unlock();
                    break;
                }
                item.parked = true;
                item.future.strand.unlock();
                break;
            case BLOCK_ON_AND_YIELD:
                WaitContext waitContext = item.future.strand.waitContext;
                waitContext.lock();
                waitContext.intermediate = false;
                if (waitContext.runnable) {
                    waitContext.completed = true;
                    reschedule(item);
                }
                waitContext.unLock();
                break;
            case YIELD:
                reschedule(item);
                break;
            case RUNNABLE:
                item.future.result = result;
                item.future.isDone = true;
                item.future.panic = panic;
                // TODO clean, better move it to future value itself
                if (item.future.callback != null) {
                    if (item.future.panic != null) {
                        item.future.callback.notifyFailure(ErrorCreator.createError(panic));
                        if (item.future.strand.currentTrxContext != null) {
                            item.future.strand.currentTrxContext.notifyLocalRemoteParticipantFailure();
                        }
                    } else {
                        item.future.callback.notifySuccess();
                    }
                }

                Strand justCompleted = item.future.strand;
                assert !justCompleted.getState().equals(State.DONE) : "Can't be completed twice";

                justCompleted.setState(State.DONE);


                for (WaitContext ctx : justCompleted.waitingContexts) {
                    ctx.lock();
                    if (!ctx.completed) {
                        if ((item.future.panic != null && ctx.handlePanic()) || ctx.waitCompleted(result)) {
                            if (ctx.intermediate) {
                                ctx.runnable = true;
                            } else {
                                ctx.completed = true;
                                reschedule(ctx.schedulerItem);
                            }
                        }
                    }
                    ctx.unLock();
                }

                cleanUp(justCompleted);

                int strandsLeft = totalStrands.decrementAndGet();
                if (strandsLeft == 0) {
                    // (number of started stands - finished stands) = 0, all the work is done
                    assert runnableList.size() == 0;

                    if (!immortal) {
                        for (int i = 0; i < numThreads; i++) {
                            runnableList.add(POISON_PILL);
                        }
                    }
                }
                break;
            default:
                assert false : "illegal strand state during execute " + item.getState();
        }
    }

    private Throwable createError(Throwable t) {
        if (t instanceof StackOverflowError) {
            BError error = ErrorCreator.createError(BallerinaErrorReasons.STACK_OVERFLOW_ERROR);
            error.setStackTrace(t.getStackTrace());
            return error;
        }
        return t;
    }

    public void unblockStrand(Strand strand) {
        strand.lock();
        if (strand.schedulerItem.parked) {
            strand.schedulerItem.parked = false;
            reschedule(strand.schedulerItem);
        } else {
            // item not returned to scheduler, yet.
            // scheduler will simply reschedule since this is already unlocked.
            strand.setState(State.YIELD);
        }
        strand.unlock();
    }

    private void cleanUp(Strand justCompleted) {
        justCompleted.scheduler = null;
        justCompleted.frames = null;
        justCompleted.waitingContexts = null;
        //TODO: more cleanup , eg channels
    }

    private void notifyChannels(SchedulerItem item, Throwable panic) {
        Set<ChannelDetails> channels = item.future.strand.channelDetails;

        for (ChannelDetails details: channels) {
            WorkerDataChannel wdChannel;

            if (details.channelInSameStrand) {
                wdChannel = item.future.strand.wdChannels.getWorkerDataChannel(details.name);
            } else {
                wdChannel = item.future.strand.parent.wdChannels.getWorkerDataChannel(details.name);
            }

            if (details.send) {
                wdChannel.setSendPanic(panic);
            } else {
                wdChannel.setReceiverPanic(panic);
            }
        }
    }

    private void reschedule(SchedulerItem item) {
        if (!item.getState().equals(State.RUNNABLE)) {
            ItemGroup group = item.future.strand.strandGroup;
            item.setState(State.RUNNABLE);
            group.add(item);

            // Group maybe not picked by any thread at the moment because,
            //  1) All items are blocked.
            //  2) All others have finished
            // In this case we need to put it back in the runnable list.
            if (group.scheduled.compareAndSet(false, true)) {
                runnableList.add(group);
            }
        }
    }

    public FutureValue createFuture(Strand parent, Callback callback, Map<String, Object> properties,
                                    Type constraint, String name, StrandMetadata metadata) {
        Strand newStrand = new Strand(name, metadata, this, parent, properties);
        return createFuture(parent, callback, constraint, newStrand);
    }

    private FutureValue createFuture(Strand parent, Callback callback, Type constraint, Strand newStrand) {
        if (parent != null) {
            newStrand.observerContext = parent.observerContext;
        }
        FutureValue future = new FutureValue(newStrand, callback, constraint);
        future.strand.frames = new Object[100];
        return future;
    }

    public void poison() {
        for (int i = 0; i < numThreads; i++) {
            runnableList.add(POISON_PILL);
        }
    }
}

/**
 * Represent an executable item in Scheduler.
 *
 * @since 0.995.0
 */
class SchedulerItem {
    private Function function;
    private Object[] params;
    final FutureValue future;
    boolean parked;

    public SchedulerItem(Function function, Object[] params, FutureValue future) {
        this.future = future;
        this.function = function;
        this.params = params;
    }

    @Deprecated
    public SchedulerItem(Consumer consumer, Object[] params, FutureValue future) {
        this.future = future;
        this.function = val -> {
            consumer.accept(val);
            return null;
        };
        this.params = params;
    }

    public Object execute() {
        return this.function.apply(this.params);
    }

    public boolean isYielded() {
        return this.future.strand.isYielded();
    }

    public State getState() {
        return this.future.strand.getState();
    }

    public void setState(State state) {
        this.future.strand.setState(state);
    }

    @Override
    public String toString() {
        return future == null ? "POISON_PILL" : String.valueOf(future.strand.hashCode());
    }
}

/**
 * Represents a group of {@link SchedulerItem} that should run on same thread.
 */
class ItemGroup {

    /**
     * Keep the list of items that should run on same thread.
     * Using a stack to get advantage of the locality.
     */
    Stack<SchedulerItem> items = new Stack();

    /**
     * Indicates this item is already in runnable list/executing or not.
     */
    AtomicBoolean scheduled = new AtomicBoolean(false);

    public static final ItemGroup POISON_PILL = new ItemGroup();

    public ItemGroup(SchedulerItem item) {
        items.push(item);
    }

    private ItemGroup() {
        items = null;
    }

    public void add(SchedulerItem item) {
        items.push(item);
    }

    public SchedulerItem get() {
        return items.pop();
    }
}
