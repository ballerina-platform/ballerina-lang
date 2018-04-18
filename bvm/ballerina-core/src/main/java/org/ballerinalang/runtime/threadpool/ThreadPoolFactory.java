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
import org.ballerinalang.util.exceptions.BallerinaException;
import org.omg.PortableServer.ThreadPolicyOperations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code ThreadPoolFactory} holds the thread pools in Ballerina engine.
 *
 * @since 0.8.0
 */
public class ThreadPoolFactory {

    private static final int DEFAULT_THREAD_POOL_SIZE = 100;
    private static final int MAX_THREAD_POOL_SIZE = 1000;
    private static final int MIN_THREAD_POOL_SIZE = 1;

    private static final String WORKER_THREAD_POOL_SIZE_PROP = "b7a.runtime.scheduler.threadpoolsize";

    private static ThreadPoolFactory instance;

    private ExecutorService workerExecutor;

    private ThreadPoolFactory() {
        int poolSize = this.extractThreadPoolSize();
        this.workerExecutor = Executors.newFixedThreadPool(poolSize,
                new BLangThreadFactory(new ThreadGroup("worker"), "worker-thread-pool"));
    };
    
    private int extractThreadPoolSize() {
        int poolSize = DEFAULT_THREAD_POOL_SIZE;
        String workerThreadPoolSizeProp = ConfigRegistry.getInstance().getAsString(WORKER_THREAD_POOL_SIZE_PROP);
        if (workerThreadPoolSizeProp != null) {
            try {
                poolSize = Integer.parseInt(workerThreadPoolSizeProp);
                if (poolSize < MIN_THREAD_POOL_SIZE || poolSize > MAX_THREAD_POOL_SIZE) {
                    throw new BallerinaException(WORKER_THREAD_POOL_SIZE_PROP + " must be between "
                            + MIN_THREAD_POOL_SIZE + " and " + MAX_THREAD_POOL_SIZE + " (inclusive)");
                }
            } catch (NumberFormatException ignore) { 
                throw new BallerinaException("invalid value for '" + WORKER_THREAD_POOL_SIZE_PROP 
                        + "': " + workerThreadPoolSizeProp);
            }
        }
        return poolSize;
    }

    public static ThreadPoolFactory getInstance() {
        if (instance == null) {
            synchronized (ThreadPolicyOperations.class) {
                if (instance == null) {
                    instance = new ThreadPoolFactory();
                }
            }
        }
        return instance;
    }

    public ExecutorService getWorkerExecutor() {
        return workerExecutor;
    }

}
