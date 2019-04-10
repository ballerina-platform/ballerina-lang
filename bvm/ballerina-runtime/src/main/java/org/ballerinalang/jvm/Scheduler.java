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
import java.util.function.Function;

/**
 * Strand scheduler for JBallerina.
 *
 * @since 0.995.0
 */
public class Scheduler {

    private LinkedList<SchedulerItem> runnableList = new LinkedList<>();
    private Map<Strand, ArrayList<SchedulerItem>> blockedList = new HashMap<>();

    private static Scheduler scheduler = null;

    /**
     * Add a task to the runnable list, which will eventually be executed by the Scheduler.
     * @param params - parameters to be passed to the function
     * @param function - function to be executed
     * @return - Reference to the scheduled task
     */
    public FutureValue schedule(Object[] params, Function function) {
        FutureValue future = new FutureValue();
        params[0] = future.strand;
        SchedulerItem item = new SchedulerItem(function, params, future);
        runnableList.add(item);
        return future;
    }

    /**
     * Get an instance of the Scheduler.
     * @return - scheduler instance
     */
    public static Scheduler getInstance() {
        if (scheduler == null) {
            synchronized (Scheduler.class) {
                if (scheduler == null) {
                    scheduler = new Scheduler();
                }
            }
        }
        return scheduler;
    }

    private Scheduler(){}

    /**
     * Executes tasks that are submitted to the Scheduler.
     */
    public void execute() {
        while (!runnableList.isEmpty()) {
            SchedulerItem item = runnableList.poll();
            Object result = item.function.apply(item.params);

            //TODO: Need to improve for performance and support conditional waits
            if (item.future.strand.blocked) {
                blockedList.putIfAbsent(item.future.strand.blockedOn, new ArrayList<>());
                blockedList.get(item.future.strand.blockedOn).add(item);
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
                item.future.strand.blocked = false;
                blockedItems.forEach(strand -> runnableList.add(item));
                blockedList.remove(item.future.strand);
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
    Function function;
    Object[] params;
    FutureValue future;

    public SchedulerItem(Function function, Object[] params, FutureValue future) {
        this.future = future;
        this.function = function;
        this.params = params;
    }
}
