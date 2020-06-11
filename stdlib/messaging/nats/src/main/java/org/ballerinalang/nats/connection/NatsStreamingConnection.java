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
package org.ballerinalang.nats.connection;

import io.nats.client.Connection;
import io.nats.streaming.StreamingConnection;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;
import org.ballerinalang.nats.observability.NatsMetricsReporter;
import org.ballerinalang.nats.observability.NatsObservabilityConstants;
import org.ballerinalang.nats.observability.NatsTracingUtil;
import org.ballerinalang.nats.streaming.BallerinaNatsStreamingConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Remote function implementation for NATS Streaming Connection creation.
 */
public class NatsStreamingConnection {

    public static void createConnection(ObjectValue streamingClientObject, ObjectValue connectionObject,
                                        String clusterId, Object clientIdNillable, Object streamingConfig) {
        Connection natsConnection = (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);
        String clientId = clientIdNillable == null ? UUID.randomUUID().toString() :
                ((BString) clientIdNillable).getValue();
        BallerinaNatsStreamingConnectionFactory streamingConnectionFactory =
                new BallerinaNatsStreamingConnectionFactory(
                        natsConnection, clusterId, clientId, (MapValue<BString, Object>) streamingConfig);
        try {
            io.nats.streaming.StreamingConnection streamingConnection = streamingConnectionFactory.createConnection();
            streamingClientObject.addNativeData(Constants.NATS_STREAMING_CONNECTION, streamingConnection);
            ((AtomicInteger) connectionObject.getNativeData(Constants.CONNECTED_CLIENTS)).incrementAndGet();
        } catch (IOException e) {
            NatsMetricsReporter.reportError(NatsObservabilityConstants.CONTEXT_STREAMING_CONNNECTION,
                                            NatsObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw Utils.createNatsError(e.getMessage());
        } catch (InterruptedException e) {
            NatsMetricsReporter.reportError(NatsObservabilityConstants.CONTEXT_STREAMING_CONNNECTION,
                                            NatsObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw Utils.createNatsError("Internal error while creating streaming connection");
        }
    }

    public static Object closeConnection(ObjectValue streamingClientObject, ObjectValue natsConnection) {
        StreamingConnection streamingConnection = (StreamingConnection) streamingClientObject
                .getNativeData(Constants.NATS_STREAMING_CONNECTION);
        NatsTracingUtil.traceResourceInvocation(Scheduler.getStrand(),
                                                streamingConnection.getNatsConnection().getConnectedUrl());
        try {
            streamingConnection.close();
            ((AtomicInteger) natsConnection.getNativeData(Constants.CONNECTED_CLIENTS)).decrementAndGet();
            return null;
        } catch (IOException | TimeoutException e) {
            NatsMetricsReporter.reportStremingError(streamingConnection.getNatsConnection().getConnectedUrl(),
                                                    NatsObservabilityConstants.UNKNOWN,
                                                    NatsObservabilityConstants.CONTEXT_STREAMING_CONNNECTION,
                                                    NatsObservabilityConstants.ERROR_TYPE_CLOSE);
            return Utils.createNatsError(e.getMessage());
        } catch (InterruptedException e) {
            NatsMetricsReporter.reportStremingError(streamingConnection.getNatsConnection().getConnectedUrl(),
                                                    NatsObservabilityConstants.UNKNOWN,
                                                    NatsObservabilityConstants.CONTEXT_STREAMING_CONNNECTION,
                                                    NatsObservabilityConstants.ERROR_TYPE_CLOSE);
            return Utils.createNatsError("Internal error while closing producer");
        }
    }

}
