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

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.jms.JmsConstants;

import java.util.concurrent.CountDownLatch;

/**
 * Close the message consumer object.
 */
public class StartNonDaemonThreadHandler {

    private StartNonDaemonThreadHandler() {
    }

    public static void handle(Context context) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        @SuppressWarnings(JmsConstants.UNCHECKED)
        BMap<String, BValue> listenerObj = (BMap<String, BValue>) context.getRefArgument(0);
        @SuppressWarnings(JmsConstants.UNCHECKED)
        BMap<String, BValue> sessionObj = (BMap<String, BValue>) listenerObj.get("session");
        @SuppressWarnings(JmsConstants.UNCHECKED)
        BMap<String, BValue> connectionObj = (BMap<String, BValue>) sessionObj.get("conn");

        // It is essential to keep a non-daemon thread running in order to avoid the java program or the
        // Ballerina service from exiting
        boolean nonDaemonThread = (boolean) connectionObj.getNativeData(JmsConstants.NON_DAEMON_THREAD_RUNNING);
        if (!nonDaemonThread) {
            listenerObj.addNativeData(JmsConstants.COUNTDOWN_LATCH, countDownLatch);
            new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

}
