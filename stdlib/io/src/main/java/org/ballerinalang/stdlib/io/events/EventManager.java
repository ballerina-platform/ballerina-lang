/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.events;

import org.ballerinalang.runtime.threadpool.BLangThreadFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Manages the I/O Connections.
 */
public class EventManager {

    private static EventManager instance = new EventManager();

    /**
     * Performs I/O related operations.
     */
    private final ExecutorService workers;

    public static EventManager getInstance() {
        return instance;
    }

    private EventManager() {
        final int numberOfCPU = Runtime.getRuntime().availableProcessors();
        final float scalingFactor = 0.25f;
        final int threadLimit = 0;
        final int minimumNumberOfThreads = 1;
        final int keepAliveTime = 1;
        int numberOfThreads = (int) (numberOfCPU * scalingFactor);
        numberOfThreads = numberOfThreads > threadLimit ? numberOfThreads : minimumNumberOfThreads;
        ThreadFactory factory = new BLangThreadFactory("BLangIO");
        workers = new ThreadPoolExecutor(
                numberOfThreads,
                numberOfThreads,
                keepAliveTime,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(),
                factory);
    }

    /**
     * Shut down the executor.
     */
    public void shutdown() {
        this.workers.shutdown();
    }

    /**
     * Publishes an event to the event queue.
     *
     * @param event incoming event which will be processed by the I/O threads.
     * @return future which will be notified on the response.
     */
    public CompletableFuture<EventResult> publish(Event event) {
        return CompletableFuture.supplyAsync(event, workers);
    }
}
