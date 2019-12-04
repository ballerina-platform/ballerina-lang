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

import io.nats.client.Connection;
import io.nats.streaming.StreamingConnection;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;
import org.ballerinalang.nats.streaming.BallerinaNatsStreamingConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Remote function implementation for NATS Streaming Connection creation.
 */
public class CreateStreamingConnection {

    public static void createStreamingConnection(Object streamingClient, Object conn,
            String clusterId, Object clientIdNillable, Object streamingConfig) {
        ObjectValue connectionObject = (ObjectValue) conn;
        Connection natsConnection = (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);
        String clientId = clientIdNillable == null ? UUID.randomUUID().toString() : (String) clientIdNillable;
        BallerinaNatsStreamingConnectionFactory streamingConnectionFactory =
                new BallerinaNatsStreamingConnectionFactory(
                natsConnection, clusterId, clientId, (MapValue<String, Object>) streamingConfig);
        try {
            StreamingConnection streamingConnection = streamingConnectionFactory.createConnection();
            ((ObjectValue) streamingClient).addNativeData(Constants.NATS_STREAMING_CONNECTION, streamingConnection);
            ((AtomicInteger) connectionObject.getNativeData(Constants.CONNECTED_CLIENTS)).incrementAndGet();
        } catch (IOException e) {
            throw Utils.createNatsError(e.getMessage());
        } catch (InterruptedException e) {
            throw Utils.createNatsError("Internal error while creating streaming connection");
        }
    }
}
