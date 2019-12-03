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

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates a subscription with the NATS server.
 *
 * @since 0.995
 */
public class Register {

    private static final PrintStream console;

    public static Object basicRegister(ObjectValue listenerObject, ObjectValue service, Object annotationData) {
        String errorMessage = "Error while registering the subscriber. ";
        Connection natsConnection =
                (Connection) ((ObjectValue) listenerObject.get(Constants.CONNECTION_OBJ))
                        .getNativeData(Constants.NATS_CONNECTION);
        @SuppressWarnings("unchecked")
        List<ObjectValue> serviceList =
                (List<ObjectValue>) ((ObjectValue) listenerObject.get(Constants.CONNECTION_OBJ))
                        .getNativeData(Constants.SERVICE_LIST);
        MapValue<String, Object> subscriptionConfig = Utils.getSubscriptionConfig(service.getType()
                .getAnnotation(Constants.NATS_PACKAGE, Constants.SUBSCRIPTION_CONFIG));
        if (subscriptionConfig == null) {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE,
                    errorMessage + " Cannot find subscription configuration.");
        }
        String queueName = subscriptionConfig.getStringValue(Constants.QUEUE_NAME);
        String subject = subscriptionConfig.getStringValue(Constants.SUBJECT);
        BRuntime runtime = BRuntime.getCurrentRuntime();
        Dispatcher dispatcher = natsConnection.createDispatcher(new DefaultMessageHandler(service, runtime));
        // Add dispatcher. This is needed when closing the connection.
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Dispatcher> dispatcherList = (ConcurrentHashMap<String, Dispatcher>)
                listenerObject.getNativeData(Constants.DISPATCHER_LIST);
        dispatcherList.put(service.getType().getName(), dispatcher);
        if (subscriptionConfig.getMapValue(Constants.PENDING_LIMITS) != null) {
            setPendingLimits(dispatcher, subscriptionConfig.getMapValue(Constants.PENDING_LIMITS));
        }
        try {
            if (queueName != null) {
                dispatcher.subscribe(subject, queueName);
            } else {
                dispatcher.subscribe(subject);
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE,
                    errorMessage + ex.getMessage());
        }
        serviceList.add(service);
        String consoleOutput = "subject " + subject + (queueName != null ? " & queue " + queueName : "");
        console.println(Constants.NATS_CLIENT_SUBSCRIBED + consoleOutput);
        return null;
    }

    // Set limits on the maximum number of messages, or maximum size of messages this consumer will
    // hold before it starts to drop new messages waiting for the resource functions to drain the queue.
    private static void setPendingLimits(Dispatcher dispatcher, MapValue pendingLimits) {
        long maxMessages = pendingLimits.getIntValue(Constants.MAX_MESSAGES);
        long maxBytes = pendingLimits.getIntValue(Constants.MAX_BYTES);
        dispatcher.setPendingLimits(maxMessages, maxBytes);
    }

    static {
        console = System.out;
    }
}
