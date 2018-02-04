/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A debouncer to be used to debounce calls to lang-server upon fast editing.
 * This is to minimize resource usage of lang-server while pushing updates to client.
 * Based on https://stackoverflow.com/a/20978973
 */
public class Debouncer {
    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<Runnable, TimerTask> delayedMap = new ConcurrentHashMap<>();
    private final int interval;

    public Debouncer(int interval) {
        this.interval = interval;
    }

    public void call(Runnable runnable) {
        TimerTask task = new TimerTask(runnable);

        TimerTask prev;
        do {
            prev = delayedMap.putIfAbsent(runnable, task);
            if (prev == null) {
                sched.schedule(task, interval, TimeUnit.MILLISECONDS);
            }
        }
        while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing
        // task was extended successfully
    }

    public void terminate() {
        sched.shutdownNow();
    }

    // The task that wakes up when the wait time elapses
    private class TimerTask implements Runnable {
        private final Runnable runnable;
        private long dueTime;
        private final Object lock = new Object();

        public TimerTask(Runnable runnable) {
            this.runnable = runnable;
            extend();
        }

        public boolean extend() {
            synchronized (lock) {
                if (dueTime < 0) { // Task has been shutdown
                    return false;
                }
                dueTime = System.currentTimeMillis() + interval;
                return true;
            }
        }

        public void run() {
            synchronized (lock) {
                long remaining = dueTime - System.currentTimeMillis();
                if (remaining > 0) { // Re-schedule task
                    sched.schedule(this, remaining, TimeUnit.MILLISECONDS);
                } else { // Mark as terminated and run
                    dueTime = -1;
                    runnable.run();
                    delayedMap.remove(runnable);
                }
            }
        }
    }
}
