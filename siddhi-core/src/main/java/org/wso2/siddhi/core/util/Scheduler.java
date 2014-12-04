/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.util;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.query.processor.Processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created on 12/3/14.
 */
public class Scheduler {

    private static final Logger log = Logger.getLogger(Scheduler.class);
    private final BlockingQueue<Long> toNotifyQueue = new LinkedBlockingQueue<Long>();
    private ScheduledExecutorService scheduledExecutorService;
    private EventCaller eventCaller;
    private volatile boolean running = false;


    public Scheduler(ScheduledExecutorService scheduledExecutorService, Processor singleThreadEntryValve) {
        this.scheduledExecutorService = scheduledExecutorService;
        eventCaller = new EventCaller(singleThreadEntryValve);
    }

    public void notifyAt(long time) {
        try {
            toNotifyQueue.put(time);

            if (!running) {
                synchronized (toNotifyQueue) {
                    if (!running) {
                        running = true;
                        long timeDiff = time - System.currentTimeMillis();
                        if (timeDiff > 0) {
                            scheduledExecutorService.schedule(eventCaller, timeDiff, TimeUnit.MILLISECONDS);
                        } else {
                            scheduledExecutorService.schedule(eventCaller, 0, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            }

        } catch (InterruptedException e) {
            log.error("Error when adding time:" + time + " to TimeNotifier ", e);//todo improve
        }


    }

    private class EventCaller implements Runnable {
        private Processor singleThreadEntryValve;

        public EventCaller(Processor singleThreadEntryValve) {

            this.singleThreadEntryValve = singleThreadEntryValve;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            Long toNotifyTime = toNotifyQueue.peek();
            long currentTime = System.currentTimeMillis();
            while (toNotifyTime != null && currentTime - toNotifyTime <= 0) {
                toNotifyQueue.poll();
//                singleThreadEntryValve.process(new StreamEvent(0, 0, 0));   //todo fix

                toNotifyTime = toNotifyQueue.peek();
                currentTime = System.currentTimeMillis();

            }
            if (toNotifyTime != null) {
                scheduledExecutorService.schedule(eventCaller, currentTime - toNotifyTime, TimeUnit.MILLISECONDS);
            } else {
                synchronized (toNotifyQueue) {
                    running = false;
                    if( toNotifyQueue.peek()!=null){
                        running = true;
                        scheduledExecutorService.schedule(eventCaller, 0, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }

    }
}
