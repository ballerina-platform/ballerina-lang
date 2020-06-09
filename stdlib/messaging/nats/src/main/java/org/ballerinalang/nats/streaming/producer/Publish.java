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
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;
import org.ballerinalang.nats.observability.NatsMetricsReporter;
import org.ballerinalang.nats.observability.NatsObservabilityConstants;
import org.ballerinalang.nats.observability.NatsTracingUtil;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.ballerinalang.nats.Utils.convertDataIntoByteArray;

/**
 * Remote function implementation for publishing a message to a NATS streaming server.
 */
public class Publish {

    public static Object externStreamingPublish(ObjectValue publisher, BString subject, Object data,
                                                ObjectValue connectionObject) {
        StreamingConnection streamingConnection = (StreamingConnection) publisher
                .getNativeData(Constants.NATS_STREAMING_CONNECTION);
        NatsMetricsReporter natsMetricsReporter =
                (NatsMetricsReporter) connectionObject.getNativeData(Constants.NATS_METRIC_UTIL);
        NatsTracingUtil.traceResourceInvocation(Scheduler.getStrand(),
                                                streamingConnection.getNatsConnection().getConnectedUrl(),
                                                subject.getValue());
        byte[] byteData = convertDataIntoByteArray(data);
        try {
            NonBlockingCallback nonBlockingCallback = new NonBlockingCallback(Scheduler.getStrand());
            AckListener ackListener = new AckListener(nonBlockingCallback, subject.getValue(), natsMetricsReporter);
            natsMetricsReporter.reportPublish(subject.getValue(), byteData.length);
            return StringUtils.fromString(streamingConnection.publish(subject.getValue(), byteData, ackListener));
        } catch (InterruptedException e) {
            natsMetricsReporter.reportProducerError(subject.getValue(), NatsObservabilityConstants.ERROR_TYPE_PUBLISH);
            return Utils.createNatsError("Failed to publish due to an internal error");
        } catch (IOException | TimeoutException e) {
            natsMetricsReporter.reportProducerError(subject.getValue(), NatsObservabilityConstants.ERROR_TYPE_PUBLISH);
            return Utils.createNatsError(e.getMessage());
        }
    }
}
