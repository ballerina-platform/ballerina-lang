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
package org.ballerinalang.nats.streaming;

import io.nats.client.Connection;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import org.ballerinalang.jvm.values.MapValue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Wraps {@link StreamingConnectionFactory}.
 */
public class BallerinaNatsStreamingConnectionFactory {
    private final MapValue<String, Object> streamingConfig;
    private final Connection natsConnection;
    private final String clusterId;
    private final String clientId;

    private static final String ACK_TIMEOUT = "ackTimeoutInSeconds";
    private static final String CONNECTION_TIMEOUT = "connectionTimeoutInSeconds";
    private static final String MAX_PUB_ACKS_IN_FLIGHT = "maxPubAcksInFlight";
    private static final String DISCOVERY_PREFIX = "discoverPrefix";

    public BallerinaNatsStreamingConnectionFactory(Connection natsConnection, String clusterId, String clientId,
            MapValue<String, Object> streamingConfig) {
        this.streamingConfig = streamingConfig;
        this.natsConnection = natsConnection;
        this.clusterId = clusterId;
        this.clientId = clientId;
    }

    public StreamingConnection createConnection() throws IOException, InterruptedException {
        StreamingConnectionFactory streamingConnectionFactory = new StreamingConnectionFactory(clusterId, clientId);
        streamingConnectionFactory.setNatsConnection(natsConnection);
        if (streamingConfig != null) {
            streamingConnectionFactory.setAckTimeout(streamingConfig.getIntValue(ACK_TIMEOUT), TimeUnit.SECONDS);
            streamingConnectionFactory
                    .setConnectTimeout(streamingConfig.getIntValue(CONNECTION_TIMEOUT), TimeUnit.SECONDS);
            streamingConnectionFactory
                    .setMaxPubAcksInFlight(streamingConfig.getIntValue(MAX_PUB_ACKS_IN_FLIGHT).intValue());
            streamingConnectionFactory.setDiscoverPrefix(streamingConfig.getStringValue(DISCOVERY_PREFIX));
        }
        return streamingConnectionFactory.createConnection();
    }
}
