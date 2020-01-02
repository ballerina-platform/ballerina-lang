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
package org.ballerinalang.nats.streaming.producer;

import io.nats.streaming.StreamingConnection;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Closes a producer.
 *
 * @since 1.0.0
 */
public class CloseConnection {

    public static Object detachFromNatsConnection(Object streamingClient, Object natsConnection) {
        ObjectValue streamingClientObject = (ObjectValue) streamingClient;
        StreamingConnection streamingConnection = (StreamingConnection) streamingClientObject
                .getNativeData(Constants.NATS_STREAMING_CONNECTION);
        try {
            streamingConnection.close();
            ObjectValue basicNatsConnection = (ObjectValue) natsConnection;
            ((AtomicInteger) basicNatsConnection.getNativeData(Constants.CONNECTED_CLIENTS)).decrementAndGet();
            return null;
        } catch (IOException | TimeoutException e) {
            return Utils.createNatsError(e.getMessage());
        } catch (InterruptedException e) {
            return Utils.createNatsError("Internal error while closing producer");
        }
    }
}
