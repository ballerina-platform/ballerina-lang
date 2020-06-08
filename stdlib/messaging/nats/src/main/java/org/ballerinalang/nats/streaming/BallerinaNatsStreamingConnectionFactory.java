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
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Wraps {@link StreamingConnectionFactory}.
 */
public class BallerinaNatsStreamingConnectionFactory {
    private final MapValue<BString, Object> streamingConfig;
    private final Connection natsConnection;
    private final String clusterId;
    private final String clientId;

    private static final BString ACK_TIMEOUT = StringUtils.fromString("ackTimeoutInSeconds");
    private static final BString CONNECTION_TIMEOUT = StringUtils.fromString("connectionTimeoutInSeconds");
    private static final BString MAX_PUB_ACKS_IN_FLIGHT = StringUtils.fromString("maxPubAcksInFlight");
    private static final BString DISCOVERY_PREFIX = StringUtils.fromString("discoverPrefix");

    public BallerinaNatsStreamingConnectionFactory(Connection natsConnection, String clusterId, String clientId,
                                                   MapValue<BString, Object> streamingConfig) {
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
            streamingConnectionFactory.setDiscoverPrefix(streamingConfig.getStringValue(DISCOVERY_PREFIX).getValue());
        }
        return streamingConnectionFactory.createConnection();
    }
}
