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
import org.ballerinalang.jvm.values.ChannelDetails;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.ballerinalang.jvm.scheduling.SchedulerItem.POISON_PILL;

/**
 * Strand scheduler for JBallerina.
 *
 * @since 0.995.0
 */
public class Scheduler {


    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    /**
     * Scheduler does not get killed if the immortal value is true. Specific to services.
     */
    public boolean immortal;
    /**
     * Strands that are ready for execution.
     */
    private BlockingQueue<SchedulerItem> runnableList = new LinkedBlockingDeque<>();

    private static final boolean DEBUG = false;

    private static final BlockingQueue<String> DEBUG_LOG;

    static {
        if (DEBUG) {
            DEBUG_LOG = new LinkedBlockingDeque<>();
        } else {
            DEBUG_LOG = null;
        }
    }

    private AtomicInteger totalStrands = new AtomicInteger();
    private final int numThreads;
    private Semaphore mainBlockSem;

    public Scheduler(int numThreads, boolean immortal) {
        this.numThreads = numThreads;
        this.immortal = immortal;
    }

    public FutureValue scheduleFunction(Object[] params, FPValue<?, ?> fp, Strand parent) {
        return schedule(params, fp.getFunction(), parent, null, null);
    }

    public FutureValue scheduleConsumer(Object[] params, FPValue<?, ?> fp, Strand parent) {
        return schedule(params, fp.getConsumer(), parent);
    }

    /**
     * Add a task to the runnable list, which will eventually be executed by the Scheduler.
     *
     * @param params   - parameters to be passed to the function
     * @param function - function to be executed
     * @param parent   - parent strand that makes the request to schedule another
     * @param callback - to notify any listener when ever the execution of the given function is finished
     * @param properties - request properties which requires for co-relation
     * @return - Reference to the scheduled task
     */
    public FutureValue schedule(Object[] params, Function function, Strand parent, CallableUnitCallback callback,
                                Map<String, Object> properties) {
        FutureValue future = createFuture(parent, callback, properties);
        return schedule(params, function, parent, future);
    }

    private FutureValue schedule(Object[] params, Function function, Strand parent, FutureValue future) {
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(function, params, future);
        future.strand.schedulerItem = item;
        totalStrands.incrementAndGet();
        if (DEBUG) {
            debugLog(item + " scheduled");
        }
        runnableList.add(item);
        return future;
    }

    /**
     * Add a void returning task to the runnable list, which will eventually be executed by the Scheduler.
     *
     * @param params   - parameters to be passed to the function
     * @param consumer - consumer to be executed
     * @param parent   - parent strand that makes the request to schedule another
     * @return - Reference to the scheduled task
     */
    public FutureValue schedule(Object[] params, Consumer consumer, Strand parent) {
        FutureValue future = createFuture(parent, null, null);
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(consumer, params, future);
        future.strand.schedulerItem = item;
        totalStrands.incrementAndGet();
        if (DEBUG) {
            debugLog(item + " scheduled");
        }
        runnableList.add(item);
        return future;
    }

    public void start() {
        if (DEBUG) {
            PrintStream out = System.out;
            Thread debugLogger = new Thread(() -> {
                try {
                    String take;
                    while (true) {
                        take = DEBUG_LOG.take();
                        out.println(take);
                    }
                } catch (InterruptedException e) {
                    logger.error("Error in debug logger", e);
                }
            }, "debug-logger");
            debugLogger.setDaemon(true);
            debugLogger.start();
        }
        this.mainBlockSem = new Semaphore(-(numThreads - 1));
        for (int i = 0; i < numThreads - 1; i++) {
            new Thread(this::runSafely, "jbal-strand-exec-" + i).start();
        }
        this.runSafely();
        try {
            this.mainBlockSem.acquire();
        } catch (InterruptedException e) {
            logger.error("Error while waiting for poison to work", e);
        }
    }

    /**
     * Defensive programming to prevent unforeseen errors.
     */
    private void runSafely() {
        try {
            run();
        } catch (Throwable t) {
            logger.error("Error occurred in scheduler", t);
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
                if (DEBUG) {
                    debugLog(item + " executing");
                }
                result = item.execute();
            } catch (Throwable e) {
                panic = e;
                notifyChannels(item, panic);
                logger.error("Strand died", e);
            }

            switch (item.getState()) {
                case BLOCK_AND_YIELD:
                    if (DEBUG) {
                        debugLog(item + " blocked");
                    }
                    item.future.strand.lock();
                    // need to recheck due to concurrency, unblockStrand() may have changed state
                    if (item.getState().getStatus() == State.YIELD.getStatus()) {
                        reschedule(item);
                        item.future.strand.unlock();
                        break;
                    }
                    if (DEBUG) {
                        debugLog(item + " parked");
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
                    } else {
                        if (DEBUG) {
                            debugLog(item + " waiting");
                        }
                    }
                    waitContext.unLock();
                    break;
                case YIELD:
                    reschedule(item);
                    if (DEBUG) {
                        debugLog(item + " yielded");
                    }
                    break;
                case RUNNABLE:
                    item.future.result = result;
                    item.future.isDone = true;
                    item.future.panic = panic;
                    // TODO clean, better move it to future value itself
                    if (item.future.callback != null) {
                        if (item.future.panic != null) {
                            item.future.callback.notifyFailure(BallerinaErrors.createError(panic));
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

                        // server agent start code will be inserted in above line during tests.
                        // It depends on this line number 279.
                        // update the linenumber @BallerinaServerAgent#SCHEDULER_LINE_NUM if modified
                        if (DEBUG) {
                            debugLog("+++++++++ all work completed ++++++++");
                        }

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

    private synchronized void debugLog(String msg) {
        try {
            Thread.sleep(100);
            DEBUG_LOG.add(msg);
            Thread.sleep(100);
        } catch (InterruptedException ignored) { }
    }

    private void notifyChannels(SchedulerItem item, Throwable panic) {
        Set<ChannelDetails> channels = item.future.strand.channelDetails;
        if (DEBUG) {
            debugLog("notifying channels:" + channels.toString());
        }

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
                if (DEBUG) {
                    debugLog(item + " rescheduled");
                }
            } else {
               debugLog(item + " " + item.getState().toString() + " not rescheduled");
            }
    }

    private FutureValue createFuture(Strand parent, CallableUnitCallback callback, Map<String, Object> properties) {
        Strand newStrand = new Strand(this, parent, properties);
        FutureValue future = new FutureValue(newStrand, callback);
        future.strand.frames = new Object[100];
        return future;
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
    private boolean isVoid;
    private Object[] params;
    final FutureValue future;
    boolean parked;

    public static final SchedulerItem POISON_PILL = new SchedulerItem();

    public SchedulerItem(Function function, Object[] params, FutureValue future) {
        this.future = future;
        this.function = function;
        this.params = params;
        this.isVoid = false;
    }

    public SchedulerItem(Consumer consumer, Object[] params, FutureValue future) {
        this.future = future;
        this.consumer = consumer;
        this.params = params;
        this.isVoid = true;
    }

    private SchedulerItem() {
        future = null;
    }

    public Object execute() {
        if (this.isVoid) {
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
