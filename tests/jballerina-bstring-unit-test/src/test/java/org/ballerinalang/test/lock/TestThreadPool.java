/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.ballerinalang.test.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code TestThreadPool} holds the thread pools in Ballerina tests.
 *
 * @since 0.961.0
 */
public class TestThreadPool {

    private static TestThreadPool instance = new TestThreadPool();

    private ExecutorService executorService = Executors.newFixedThreadPool(500);

    private TestThreadPool() {
    }

    public static TestThreadPool getInstance() {
        return instance;
    }

    public ExecutorService getExecutor() {
        return executorService;
    }

}
