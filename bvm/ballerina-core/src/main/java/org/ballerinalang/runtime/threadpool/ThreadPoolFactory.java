/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.runtime.threadpool;

import org.ballerinalang.config.ConfigRegistry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code ThreadPoolFactory} holds the thread pools in Ballerina engine.
 *
 * @since 0.8.0
 */
public class ThreadPoolFactory {

    private static final int DEFAULT_THREAD_POOL_SIZE = 100;

    private static final String WORKER_THREAD_POOL_SIZE_PROP = "worker.thread.pool.size";

    private static ThreadPoolFactory instance = new ThreadPoolFactory();

    private ExecutorService workerExecutor;

    private ThreadPoolFactory() {
        int poolSize = DEFAULT_THREAD_POOL_SIZE;
        String workerThreadPoolSizeProp = ConfigRegistry.getInstance().getAsString(WORKER_THREAD_POOL_SIZE_PROP);
        if (workerThreadPoolSizeProp != null) {
            try {
                poolSize = Integer.parseInt(workerThreadPoolSizeProp);
            } catch (NumberFormatException ignore) { /* ignore */ }
        }
        this.workerExecutor = Executors.newFixedThreadPool(poolSize,
                new BLangThreadFactory(new ThreadGroup("worker"), "worker-thread-pool"));
    };

    public static ThreadPoolFactory getInstance() {
        return instance;
    }

    public ExecutorService getWorkerExecutor() {
        return workerExecutor;
    }

}
