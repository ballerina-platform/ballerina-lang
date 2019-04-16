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

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A debouncer to be used to debounce calls to lang-server upon fast editing.
 * This is to minimize resource usage of lang-server while pushing updates to client.
 * Based on https://stackoverflow.com/a/20978973
 */
public class Debouncer {
    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<Path, Future<?>> delayedMap = new ConcurrentHashMap<>();
    private final int interval;

    public Debouncer(int interval) {
        this.interval = interval;
    }

    public void call(Path path, Runnable runnable) {
        final Future<?> prev = delayedMap.put(path, sched.schedule(() -> {
            try {
                runnable.run();
            } finally {
                delayedMap.remove(path);
            }
        }, interval, TimeUnit.MILLISECONDS));
        if (prev != null) {
            prev.cancel(true);
        }
    }

    public void terminate() {
        sched.shutdownNow();
    }
}
