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
 *
 */

package org.ballerinalang.net.jms.nativeimpl.endpoint.common;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import java.util.concurrent.CountDownLatch;

/**
 * Close the message consumer object.
 */
public class StartNonDaemonThreadHandler {

    private StartNonDaemonThreadHandler() {
    }

    public static void handle(ObjectValue listenerObj) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        // It is essential to keep a non-daemon thread running in order to avoid the java program or the
        // Ballerina service from exiting
        listenerObj.addNativeData(JmsConstants.COUNTDOWN_LATCH, countDownLatch);
        new Thread(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                BallerinaAdapter.throwBallerinaException("The current thread got interrupted");
            }
        }).start();
    }

}
