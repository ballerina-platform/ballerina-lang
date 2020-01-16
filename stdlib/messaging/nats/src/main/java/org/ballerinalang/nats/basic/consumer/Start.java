/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nats.basic.consumer;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.observability.NatsTracingUtil;

import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.nats.Constants.COUNTDOWN_LATCH;

/**
 * Extern function to start the NATS subscriber.
 *
 * @since 0.995
 */
public class Start {
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void basicStart(ObjectValue listenerObject) {
        NatsTracingUtil.traceResourceInvocation(Scheduler.getStrand(), listenerObject);
        listenerObject.addNativeData(COUNTDOWN_LATCH, countDownLatch);
        // It is essential to keep a non-daemon thread running in order to avoid the java program or the
        // Ballerina service from exiting
        new Thread(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
