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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.values.ChannelDetails;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.ballerinalang.jvm.SchedulerItem.BLOCKED;
import static org.ballerinalang.jvm.SchedulerItem.POISON_PILL;
import static org.ballerinalang.jvm.SchedulerItem.RUNNABLE;
import static org.ballerinalang.jvm.SchedulerItem.YIELD;

/**
 * Strand scheduler for JBallerina.
 *
 * @since 0.995.0
 */
public class Scheduler {


    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    /**
     * Strands that are ready for execution.
     */
    private BlockingQueue<SchedulerItem> runnableList = new LinkedBlockingDeque<>();

    /**
     * Scheduler items that are blocked on given strand.
     * List key is the blocker.
     */
    private Map<Strand, Set<SchedulerItem>> blockedList = new ConcurrentHashMap<>();

    /**
     * Scheduler items that are blocked but the blocker is not known.
     * List key is the own strand.
     */
    private Map<Strand, SchedulerItem> blockedOnUnknownList = new HashMap<>();

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
    private static final Set<SchedulerItem> COMPLETED = Collections.emptySet();

    public Scheduler(int numThreads) {
        this.numThreads = numThreads;
    }

    public FutureValue scheduleFunction(Object[] params, FPValue<?, ?> fp, Strand parent) {
        return schedule(params, fp.getFunction(), parent);
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
     * @return - Reference to the scheduled task
     */
    public FutureValue schedule(Object[] params, Function function, Strand parent) {
        FutureValue future = createFuture(parent);
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(function, params, future);
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
        FutureValue future = createFuture(parent);
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(consumer, params, future);
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
        for (int i = 0; i < numThreads - 1; i++) {
            new Thread(this::runSafely, "jbal-strand-exec-" + i).start();
        }
        this.runSafely();
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
                case BLOCKED:
                    if (item.blockedOn().isEmpty()) {
                        if (DEBUG) {
                            debugLog(item + " blocked");
                        }
                        blockedOnUnknownList.put(item.future.strand, item);
                    } else {
                        item.blockedOn().forEach(blockedOn -> {
                            synchronized (blockedOn) {
                                blockedList.compute(blockedOn, (sameAsBlockedOn, blocked) -> {
                                    if (blocked == COMPLETED) {
                                        if (DEBUG) {
                                            debugLog(item + " blocked and freed on " + blockedOn.hashCode());
                                        }
                                        reschedule(item);
                                    } else {
                                        if (blocked == null) {
                                            blocked = new HashSet<>();
                                        }
                                        if (DEBUG) {
                                            debugLog(item + " blocked on wait for " + blockedOn.hashCode());
                                        }
                                        blocked.add(item);
                                    }
                                    return blocked;
                                });
                            }
                        });
                    }
                    break;
                case YIELD:
                    reschedule(item);
                    if (DEBUG) {
                        debugLog(item + " yielded");
                    }
                    break;
                case RUNNABLE:
                    synchronized (item.future) {
                        item.future.result = result;
                        item.future.isDone = true;
                        item.future.panic = panic;
                    }

                    Strand justCompleted = item.future.strand;
                    Set<SchedulerItem> blockedOnJustCompleted;
                    if (DEBUG) {
                        debugLog(item + " complected");
                    }
                    synchronized (justCompleted) {
                        blockedOnJustCompleted = blockedList.put(justCompleted, COMPLETED);
                    }

                    assert blockedOnJustCompleted != COMPLETED : "Can't be completed twice";

                    if (blockedOnJustCompleted != null) {
                        for (SchedulerItem blockedItem : blockedOnJustCompleted) {
                            if (DEBUG) {
                                debugLog(blockedItem + " freed by completion of " + item);
                            }
                            reschedule(blockedItem);
                        }
                    }

                    int strandsLeft = totalStrands.decrementAndGet();
                    if (strandsLeft == 0) {
                        // (number of started stands - finished stands) = 0, all the work is done
                        assert runnableList.size() == 0;

                        if (DEBUG) {
                            debugLog("+++++++++ all work completed ++++++++");
                        }

                        for (int i = 0; i < numThreads; i++) {
                            runnableList.add(POISON_PILL);
                        }
                    }
                    break;
                default:
                    assert false : "illegal strand state during execute " + item.getState();
            }
        }
    }

    private synchronized void debugLog(String msg) {
        try {
            Thread.sleep(100);
            DEBUG_LOG.add(msg);
            Thread.sleep(100);
        } catch (InterruptedException ignored) { }
    }

    private void notifyChannels(SchedulerItem item, Throwable panic) {
        ChannelDetails[] channels = item.future.strand.channelDetails;

        for (int i = 0; i < channels.length; i++) {
            ChannelDetails details = channels[i];
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
        synchronized (item) {
            if (item.getState() != RUNNABLE) {
                // release if the same strand is waiting for others (wait multiple)
                release(item.future.strand.blockedOn, item.future.strand);
                item.setState(RUNNABLE);
                runnableList.add(item);
            }
        }
    }

    private FutureValue createFuture(Strand parent) {
        Strand newStrand = new Strand(this, parent);
        FutureValue future = new FutureValue(newStrand);
        future.strand.frames = new Object[100];
        return future;
    }

    public void unblockStrand(Strand strand) {
        SchedulerItem item;
        int i = 0;
        // TODO: remove this busy waiting, use locking
        while ((item = blockedOnUnknownList.remove(strand)) == null) {
            i++;
            if (i == 1000000) {
                logger.warn("Possible infinite wait for receiver worker by :" +
                        Thread.currentThread().getStackTrace()[2]);
                if (DEBUG) {
                    debugLog("possible infinite wait for receiver " + strand.hashCode() + " to block, by " +
                            Thread.currentThread().getStackTrace()[2]);
                }
            }
        }
        if (DEBUG) {
            debugLog(item + " got unblocked due to send");
        }
        reschedule(item);
    }

    public void release(List<Strand> blockedOnList, Strand strand) {
        blockedOnList.forEach(blockedOn ->
            blockedList.computeIfPresent(blockedOn, (sameAsBlockedOn, blocked) -> {
                blocked.removeIf(item -> item.future.strand == strand);
                return blocked;
            })
        );

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

    public static final SchedulerItem POISON_PILL = new SchedulerItem();
    public static final int RUNNABLE = 0;
    public static final int YIELD = 1;
    public static final int BLOCKED = 2;

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
        return this.future.strand.yield;
    }

    public int getState() {
        if (this.future.strand.blocked) {
            return BLOCKED;
        } else if (this.future.strand.yield) {
            return YIELD;
        } else {
            return RUNNABLE;
        }
    }

    public List<Strand> blockedOn() {
        return this.future.strand.blockedOn;
    }

    public void setState(int state) {
        if (state == RUNNABLE) {
            this.future.strand.yield = false;
            this.future.strand.blockedOn.clear();
            this.future.strand.blocked = false;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(future.strand.hashCode());
    }
}
