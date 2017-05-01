/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SystemTimeBasedScheduler extends Scheduler {
    private static final Logger log = Logger.getLogger(SystemTimeBasedScheduler.class);
    private EventCaller eventCaller;
    private volatile boolean running = false;
    private ScheduledExecutorService scheduledExecutorService;

    public SystemTimeBasedScheduler(ScheduledExecutorService scheduledExecutorService, Schedulable
            singleThreadEntryValve, ExecutionPlanContext executionPlanContext) {
        super(singleThreadEntryValve, executionPlanContext);
        this.scheduledExecutorService = scheduledExecutorService;
        this.eventCaller = new EventCaller();
    }

    @Override
    public void schedule(long time) {
        if (!running && toNotifyQueue.size() == 1) {
            synchronized (toNotifyQueue) {
                if (!running) {
                    running = true;
                    long timeDiff = time - executionPlanContext.getTimestampGenerator().currentTime();
                    if (timeDiff > 0) {
                        scheduledExecutorService.schedule(eventCaller, timeDiff, TimeUnit.MILLISECONDS);
                    } else {
                        scheduledExecutorService.schedule(eventCaller, 0, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }

    }

    @Override
    public Scheduler clone(String key, EntryValveProcessor entryValveProcessor) {
        Scheduler scheduler = new SystemTimeBasedScheduler(scheduledExecutorService, entryValveProcessor,
                executionPlanContext);
        scheduler.elementId = elementId + "-" + key;
        return scheduler;
    }

    private class EventCaller implements Runnable {
        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            try {
                sendTimerEvents();

                Long toNotifyTime = toNotifyQueue.peek();
                long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
                if (!executionPlanContext.isPlayback()) {
                    if (toNotifyTime != null) {
                        scheduledExecutorService.schedule(eventCaller, toNotifyTime - currentTime, TimeUnit
                                .MILLISECONDS);
                    } else {
                        synchronized (toNotifyQueue) {
                            running = false;
                            if (toNotifyQueue.peek() != null) {
                                running = true;
                                scheduledExecutorService.schedule(eventCaller, 0, TimeUnit.MILLISECONDS);
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                log.error(t);
            }
        }

    }
}
