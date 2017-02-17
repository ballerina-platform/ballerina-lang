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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code ThreadPoolFactory} holds the thread pools in Ballerina engine.
 *
 * @since 0.8.0
 */
public class ThreadPoolFactory {

    private static ThreadPoolFactory instance = new ThreadPoolFactory();

    //TODO: Make the thread count configurable.
    // Ideally number of threads need to be calculated and spawned intelligently
    // based on the environment and runtime status (CPU Usage, memory, etc).
    // A configuration parameter which is user configurable is also required.
    // Issue#1929
    private ExecutorService executorService =  Executors.newFixedThreadPool(500);

    private ThreadPoolFactory(){};

    public static ThreadPoolFactory getInstance() {
        return instance;
    }

    public ExecutorService getExecutor() {
        return executorService;
    }

}
