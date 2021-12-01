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
package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.internal.values.ChannelDetails;
import io.ballerina.runtime.internal.values.FutureValue;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.ballerina.runtime.internal.scheduling.ItemGroup.POISON_PILL;

/**
 * Strand scheduler for JBallerina.
 *
 * @since 0.995.0
 */
public class Scheduler {

    private static PrintStream err = System.err;

    /**
     * Scheduler does not get killed if the immortal value is true. Specific to services.
     */
    private volatile boolean immortal;
    private boolean listenerDeclarationFound;
    /**
     * Strands that are ready for execution.
     */
    private BlockingQueue<ItemGroup> runnableList = new LinkedBlockingDeque<>();

    private static final ThreadLocal<StrandHolder> strandHolder = ThreadLocal.withInitial(StrandHolder::new);
    private final Strand previousStrand;

    private AtomicInteger totalStrands = new AtomicInteger();

    private static String poolSizeConf = System.getenv(RuntimeConstants.BALLERINA_MAX_POOL_SIZE_ENV_VAR);

    /**
     * This can be changed by setting the BALLERINA_MAX_POOL_SIZE system variable.
     * Default is 100.
     */
    private final int numThreads;

    private static int poolSize = Runtime.getRuntime().availableProcessors() * 2;

    private Semaphore mainBlockSem;
    private ListenerRegistry listenerRegistry;
    private AtomicReference<ItemGroup> objectGroup = new AtomicReference<>();

    public Scheduler(boolean immortal) {
        this(getPoolSize(), immortal);
    }

    public Scheduler(int numThreads, boolean immortal) {
        this.numThreads = numThreads;
        this.immortal = immortal;
        this.listenerRegistry = new ListenerRegistry();
        this.previousStrand = numThreads == 1 ? strandHolder.get().strand : null;
        ItemGroup group = new ItemGroup();
        objectGroup.set(group);
    }

    public static Strand getStrand() {
        Strand strand = strandHolder.get().strand;
        if (strand == null) {
            throw new IllegalStateException("strand is not accessible from non-strand-worker threads");
        }
        return strand;
    }

    public static Strand getStrandNoException() {
        // issue #22871 is opened to fix this
        return strandHolder.get().strand;
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

    public FutureValue scheduleTransactionalFunction(Object[] params, BFunctionPointer<?, ?> fp, Strand parent,
                                                     Type returnType, String strandName, StrandMetadata metadata) {
        return scheduleTransactional(params, fp.getFunction(), parent, null, null, returnType, strandName, metadata);
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
        addToRunnableList(item, parent.strandGroup);
        return future;
    }

    public FutureValue scheduleToObjectGroup(Object[] params, Function function, Strand parent,
                                             Callback callback, Map<String, Object> properties, Type returnType,
                                             String strandName, StrandMetadata metadata) {
        FutureValue future = createFuture(parent, callback, properties, returnType, strandName, metadata);
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(function, params, future);
        future.strand.schedulerItem = item;
        totalStrands.incrementAndGet();
        ItemGroup group = objectGroup.get();
        future.strand.strandGroup = group;
        addToRunnableList(item, group);
        return future;
    }

    public FutureValue scheduleTransactionalLocal(Object[] params, BFunctionPointer<?, ?> fp, Strand parent,
                                                  Type returnType, String strandName, StrandMetadata metadata) {
        FutureValue future = createTransactionalFuture(parent, null, null, returnType, strandName, metadata);
        return scheduleLocal(params, fp, parent, future);
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

    public FutureValue scheduleTransactional(Object[] params, Function function, Strand parent, Callback callback,
                                Map<String, Object> properties, Type returnType, String strandName,
                                StrandMetadata metadata) {
        FutureValue future = createTransactionalFuture(parent, callback, properties, returnType, strandName, metadata);
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

            boolean isItemsEmpty = group.items.isEmpty();
            while (!isItemsEmpty) {
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
                    strandHolder.get().strand = previousStrand;
                }
                postProcess(item, result, panic);
                group.lock();
                if ((isItemsEmpty = group.items.empty())) {
                    group.scheduled.set(false);
                }
                group.unlock();
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
                        item.future.callback.notifySuccess(result);
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
                    assert runnableList.isEmpty();

                    if (!immortal) {
                        poison();
                    }
                }
                break;
            default:
                assert false : "illegal strand state during execute " + item.getState();
        }
    }

    public void setImmortal(boolean immortal) {
        this.immortal = immortal;
    }

    private Throwable createError(Throwable t) {
        if (t instanceof StackOverflowError) {
            BError error = ErrorCreator.createError(BallerinaErrorReasons.STACK_OVERFLOW_ERROR);
            error.setStackTrace(t.getStackTrace());
            return error;
        } else if (t instanceof OutOfMemoryError) {
            BError error = ErrorCreator.createError(BallerinaErrorReasons.JAVA_OUT_OF_MEMORY_ERROR,
                    StringUtils.fromString(t.getMessage()));
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

        for (ChannelDetails details : channels) {
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
            addToRunnableList(item, group);
        }
    }

    private void addToRunnableList(SchedulerItem item, ItemGroup group) {
        group.lock();
        group.add(item);
        // Group maybe not picked by any thread at the moment because,
        //  1) All items are blocked.
        //  2) All others have finished
        // In this case we need to put it back in the runnable list.
        if (group.scheduled.compareAndSet(false, true)) {
            runnableList.add(group);
        }
        group.unlock();
    }

    public FutureValue createFuture(Strand parent, Callback callback, Map<String, Object> properties,
                                    Type constraint, String name, StrandMetadata metadata) {
        Strand newStrand = new Strand(name, metadata, this, parent, properties);
        return createFuture(parent, callback, constraint, newStrand);
    }

    public FutureValue createTransactionalFuture(Strand parent, Callback callback, Map<String, Object> properties,
                                    Type constraint, String name, StrandMetadata metadata) {
        Strand newStrand = new Strand(name, metadata, this, parent, properties, parent != null ?
                parent.currentTrxContext : null);
        return createFuture(parent, callback, constraint, newStrand);
    }

    private FutureValue createFuture(Strand parent, Callback callback, Type constraint, Strand newStrand) {
        FutureValue future = new FutureValue(newStrand, callback, constraint);
        future.strand.frames = new Object[100];
        return future;
    }

    public void poison() {
        for (int i = 0; i < numThreads; i++) {
            runnableList.add(POISON_PILL);
        }
    }

    public void setListenerDeclarationFound(boolean listenerDeclarationFound) {
        this.listenerDeclarationFound = listenerDeclarationFound;
        if (listenerDeclarationFound) {
            setImmortal(true);
        }
    }

    public boolean isListenerDeclarationFound() {
        return listenerDeclarationFound;
    }

    public ListenerRegistry getListenerRegistry() {
        return listenerRegistry;
    }

    private static int getPoolSize() {
        try {
            if (poolSizeConf != null) {
                poolSize = Integer.parseInt(poolSizeConf);
            }
        } catch (Throwable t) {
            // Log and continue with default
            err.println("ballerina: error occurred in scheduler while reading system variable:" +
                    RuntimeConstants.BALLERINA_MAX_POOL_SIZE_ENV_VAR + ", " + t.getMessage());
        }
        return poolSize;
    }

    /**
     * The registry for runtime dynamic listeners.
     */
    public class ListenerRegistry {
        private final Set<BObject> listenerSet = new HashSet<>();

        public synchronized void registerListener(BObject listener) {
            listenerSet.add(listener);
            setImmortal(true);
        }

        public synchronized void deregisterListener(BObject listener) {
            listenerSet.remove(listener);
            if (!isListenerDeclarationFound() && listenerSet.isEmpty()) {
                setImmortal(false);
            }
        }

        public synchronized void stopListeners(Strand strand) {
            for (BObject listener : listenerSet) {
                listener.call(strand, "gracefulStop");
            }
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
    Stack<SchedulerItem> items = new Stack<>();

    /**
     * Indicates this item is already in runnable list/executing or not.
     */
    AtomicBoolean scheduled = new AtomicBoolean(false);

    private final ReentrantLock groupLock = new ReentrantLock();

    public static final ItemGroup POISON_PILL = new ItemGroup();

    public ItemGroup(SchedulerItem item) {
        items.push(item);
    }

    public ItemGroup() {
    }

    public void add(SchedulerItem item) {
        items.push(item);
    }

    public SchedulerItem get() {
        return items.pop();
    }

    public void lock() {
        this.groupLock.lock();
    }

    public void unlock() {
        this.groupLock.unlock();
    }
}
