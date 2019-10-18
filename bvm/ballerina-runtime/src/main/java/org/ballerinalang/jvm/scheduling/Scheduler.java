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
package org.ballerinalang.jvm.scheduling;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.jvm.util.RuntimeUtils;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.values.ChannelDetails;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.ballerinalang.jvm.runtime.RuntimeConstants.GLOBAL_TRANSACTION_ID;
import static org.ballerinalang.jvm.runtime.RuntimeConstants.TRANSACTION_URL;
import static org.ballerinalang.jvm.scheduling.SchedulerItem.POISON_PILL;

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
    private BlockingQueue<SchedulerItem> runnableList = new LinkedBlockingDeque<>();

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

    public FutureValue scheduleFunction(Object[] params, FPValue<?, ?> fp, Strand parent, BType returnType) {
        return schedule(params, fp.getFunction(), parent, null, null, returnType);
    }

    public FutureValue scheduleConsumer(Object[] params, FPValue<?, ?> fp, Strand parent) {
        return schedule(params, fp.getConsumer(), parent, null);
    }

    /**
     * Add a task to the runnable list, which will eventually be executed by the Scheduler.
     *
     * @param params   - parameters to be passed to the function
     * @param function - function to be executed
     * @param parent   - parent strand that makes the request to schedule another
     * @param callback - to notify any listener when ever the execution of the given function is finished
     * @param properties - request properties which requires for co-relation
     * @param returnType - return type of the scheduled function
     * @return - Reference to the scheduled task
     */
    public FutureValue schedule(Object[] params, Function function, Strand parent, CallableUnitCallback callback,
                                Map<String, Object> properties, BType returnType) {
        FutureValue future = createFuture(parent, callback, properties, returnType);
        return schedule(params, function, parent, future);
    }

    private FutureValue schedule(Object[] params, Function function, Strand parent, FutureValue future) {
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(function, params, future);
        future.strand.schedulerItem = item;
        totalStrands.incrementAndGet();
        runnableList.add(item);
        return future;
    }

    /**
     * Add a void returning task to the runnable list, which will eventually be executed by the Scheduler.
     *
     * @param params   - parameters to be passed to the function
     * @param consumer - consumer to be executed
     * @param parent   - parent strand that makes the request to schedule another
     * @param callback - to notify any listener when ever the execution of the given function is finished
     * @return - Reference to the scheduled task
     */
    public FutureValue schedule(Object[] params, Consumer consumer, Strand parent, CallableUnitCallback callback) {
        FutureValue future = createFuture(parent, callback, null, BTypes.typeNull);
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(consumer, params, future);
        future.strand.schedulerItem = item;
        totalStrands.incrementAndGet();
        runnableList.add(item);
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
            try {
                item = runnableList.take();
            } catch (InterruptedException ignored) {
                continue;
            }

            if (item == POISON_PILL) {
                this.mainBlockSem.release();
                break;
            }

            Object result = null;
            Throwable panic = null;
            try {
                strandHolder.get().strand = item.future.strand;
                result = item.execute();
            } catch (Throwable e) {
                panic = createError(e);
                notifyChannels(item, panic);
              
                if (!(panic instanceof ErrorValue)) {
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
                            item.future.callback.notifyFailure(BallerinaErrors.createError(panic));
                            if (item.future.transactionLocalContext != null) {
                                item.future.transactionLocalContext.notifyLocalRemoteParticipantFailure();
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
    }

    private Throwable createError(Throwable t) {
        if (t instanceof StackOverflowError) {
            ErrorValue error = BallerinaErrors.createError(BallerinaErrorReasons.STACK_OVERFLOW_ERROR);
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
                // release if the same strand is waiting for others as well (wait multiple)
                item.setState(State.RUNNABLE);
                runnableList.add(item);
            }
    }

    private FutureValue createFuture(Strand parent, CallableUnitCallback callback, Map<String, Object> properties,
                                     BType constraint) {
        Strand newStrand = new Strand(this, parent, properties);
        if (parent != null) {
            newStrand.observerContext = parent.observerContext;
        }
        FutureValue future = new FutureValue(newStrand, callback, constraint);
        infectResourceFunction(newStrand, future);
        future.strand.frames = new Object[100];
        return future;
    }

    public void poison() {
        for (int i = 0; i < numThreads; i++) {
            runnableList.add(POISON_PILL);
        }
    }

    private void infectResourceFunction(Strand strand, FutureValue futureValue) {
        String gTransactionId = (String) strand.getProperty(GLOBAL_TRANSACTION_ID);
        if (gTransactionId != null) {
            String url = strand.getProperty(TRANSACTION_URL).toString();
            TransactionLocalContext transactionLocalContext = TransactionLocalContext.create(gTransactionId,
                                                                                             url, "2pc");
            strand.setLocalTransactionContext(transactionLocalContext);
            futureValue.transactionLocalContext = transactionLocalContext;
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
    private Consumer consumer;
    private Object[] params;
    final FutureValue future;
    boolean parked;

    public static final SchedulerItem POISON_PILL = new SchedulerItem();

    public SchedulerItem(Function function, Object[] params, FutureValue future) {
        this.future = future;
        this.function = function;
        this.params = params;
    }

    public SchedulerItem(Consumer consumer, Object[] params, FutureValue future) {
        this.future = future;
        this.consumer = consumer;
        this.params = params;
    }

    private SchedulerItem() {
        future = null;
    }

    public Object execute() {
        if (this.consumer != null) {
            this.consumer.accept(this.params);
            return null;
        } else {
            return this.function.apply(this.params);
        }
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
