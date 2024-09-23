/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.jvm.tests;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import org.testng.Assert;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used for Java interoperability tests.
 * <p>
 * Returns a number after a delay.
 *
 * @since 1.0.0
 */
public final class AsyncInterop {

    private AsyncInterop() {
    }

    public static int countSlowly() {
        CompletableFuture<Object> future = AsyncUtils.markAsync();

        new Thread(() -> {
            sleep();
            future.complete(42);
        }).start();

        return -1;
    }

    public static void completeFutureMoreThanOnce(Environment env) {
        Future future = env.markAsync();
        final AtomicInteger count = new AtomicInteger();
        new Thread(() -> {
            while (count.incrementAndGet() < 100) {

                future.complete(null);
            }
        }).start();
    }

    private static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}
