/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.runtime.nativeimpl;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Extern function ballerina/runtime:sleep.
 *
 * @since 0.94.1
 */
public class Sleep {

    private static final int CORE_THREAD_POOL_SIZE = 1;

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(CORE_THREAD_POOL_SIZE);

    public static void sleep(long delayMillis) {
        schedule(new NonBlockingCallback(Scheduler.getStrand())::notifySuccess, delayMillis);
    }

    /**
     * This can be used to register a callback to be triggered after the given delay. The callback
     * must not block the execution in any way, and should return as soon as possible. The duration to
     * execute the callback will affect other awaiting callbacks.
     * @param callback the callback to be invoked after the given delay
     * @param delayMillis the trigger delay in milliseconds
     */
    public static void schedule(TimerCallback callback, long delayMillis) {
        executor.schedule(callback::execute, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Represents the timer callback.
     */
    public interface TimerCallback {

        /**
         * This is executed when the timer is triggered.
         */
        void execute();

    }
}
