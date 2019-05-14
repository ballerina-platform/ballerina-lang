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

import org.ballerinalang.jvm.values.FutureValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Strand scheduler for JBallerina.
 *
 * @since 0.995.0
 */
public class Scheduler {

    /**
     * Strands that are ready for execution.
     */
    private LinkedList<SchedulerItem> runnableList = new LinkedList<>();

    /**
     * Scheduler items that are blocked on given strand.
     * List key is the blocker.
     */
    private Map<Strand, ArrayList<SchedulerItem>> blockedList = new HashMap<>();

    /**
     * Scheduler items that are blocked but the blocker is not known.
     * List key is the own strand.
     */
    private Map<Strand, SchedulerItem> blockedOnUnknownList = new HashMap<>();

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
        runnableList.add(item);
        return future;
    }

    /**
     * Executes tasks that are submitted to the Scheduler.
     */
    public void execute() {
        while (!runnableList.isEmpty()) {
            SchedulerItem item = runnableList.poll();
            Object result = null;

            if (item.isVoid) {
                item.consumer.accept(item.params);
            } else {
                result = item.function.apply(item.params);
            }

            //TODO: Need to improve for performance and support conditional waits
            if (item.future.strand.blocked) {
                if (item.future.strand.blockedOn != null) {
                    blockedList.putIfAbsent(item.future.strand.blockedOn, new ArrayList<>());
                    blockedList.get(item.future.strand.blockedOn).add(item);
                } else {
                    blockedOnUnknownList.put(item.future.strand, item);
                }
                continue;
            }

            if (item.future.strand.yield) {
                item.future.strand.yield = false;
                runnableList.add(item);
                continue;
            }

            //strand has completed execution
            item.future.result = result;
            item.future.isDone = true;
            ArrayList<SchedulerItem> blockedItems = blockedList.get(item.future.strand);
            if (blockedItems != null) {
                blockedItems.forEach(blockedItem -> {
                    blockedItem.future.strand.blocked = false;
                    blockedItem.future.strand.yield = false;
                    blockedItem.future.strand.blockedOn = null;
                    runnableList.add(blockedItem);
                });
                blockedList.remove(item.future.strand);
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
        SchedulerItem  item = blockedOnUnknownList.get(strand);
        item.future.strand.blocked = false;
        item.future.strand.yield = false;
        blockedOnUnknownList.remove(strand);
        runnableList.add(item);
    }
}

/**
 * Represent an executable item in Scheduler.
 *
 * @since 0.995.0
 */
class SchedulerItem {
    Function function;
    Consumer consumer;
    boolean isVoid;
    Object[] params;
    FutureValue future;

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
}
