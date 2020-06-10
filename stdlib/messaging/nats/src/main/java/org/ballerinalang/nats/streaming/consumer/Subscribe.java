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
package org.ballerinalang.nats.streaming.consumer;

import io.nats.streaming.Subscription;
import io.nats.streaming.SubscriptionOptions;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;
import org.ballerinalang.nats.connection.NatsStreamingConnection;
import org.ballerinalang.nats.observability.NatsMetricsReporter;
import org.ballerinalang.nats.observability.NatsObservabilityConstants;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import static org.ballerinalang.nats.Constants.NATS_CLIENT_SUBSCRIBED;
import static org.ballerinalang.nats.Constants.STREAMING_DISPATCHER_LIST;
import static org.ballerinalang.nats.Constants.STREAMING_SUBSCRIPTION_LIST;

/**
 * Remote function implementation for subscribing to a NATS subject.
 */
public class Subscribe {
    private static final PrintStream console;
    private static final String STREAMING_SUBSCRIPTION_CONFIG = "StreamingSubscriptionConfig";
    private static final BString SUBJECT_ANNOTATION_FIELD = StringUtils.fromString("subject");
    private static final BString QUEUE_NAME_ANNOTATION_FIELD = StringUtils.fromString("queueName");
    private static final BString DURABLE_NAME_ANNOTATION_FIELD = StringUtils.fromString("durableName");
    private static final BString MAX_IN_FLIGHT_ANNOTATION_FIELD = StringUtils.fromString("maxInFlight");
    private static final BString ACK_WAIT_ANNOTATION_FIELD = StringUtils.fromString("ackWaitInSeconds");
    private static final BString SUBSCRIPTION_TIMEOUT_ANNOTATION_FIELD = StringUtils.fromString(
            "subscriptionTimeoutInSeconds");
    private static final BString MANUAL_ACK_ANNOTATION_FIELD = StringUtils.fromString("manualAck");
    private static final BString START_POSITION_ANNOTATION_FIELD = StringUtils.fromString("startPosition");

    public static void streamingSubscribe(ObjectValue streamingListener, ObjectValue connectionObject,
                                          BString clusterId, Object clientIdNillable, Object streamingConfig) {
        NatsStreamingConnection.createConnection(streamingListener, connectionObject, clusterId.getValue(),
                                                 clientIdNillable, streamingConfig);
        NatsMetricsReporter natsMetricsReporter =
                (NatsMetricsReporter) connectionObject.getNativeData(Constants.NATS_METRIC_UTIL);
        io.nats.streaming.StreamingConnection streamingConnection =
                (io.nats.streaming.StreamingConnection) streamingListener
                        .getNativeData(Constants.NATS_STREAMING_CONNECTION);
        ConcurrentHashMap<ObjectValue, StreamingListener> serviceListenerMap =
                (ConcurrentHashMap<ObjectValue, StreamingListener>) streamingListener
                        .getNativeData(STREAMING_DISPATCHER_LIST);
        ConcurrentHashMap<ObjectValue, Subscription> subscriptionsMap =
                (ConcurrentHashMap<ObjectValue, Subscription>) streamingListener
                        .getNativeData(STREAMING_SUBSCRIPTION_LIST);
        Iterator serviceListeners = serviceListenerMap.entrySet().iterator();
        while (serviceListeners.hasNext()) {
            Map.Entry pair = (Map.Entry) serviceListeners.next();
            Subscription sub =
                    createSubscription((ObjectValue) pair.getKey(),
                                       (StreamingListener) pair.getValue(), streamingConnection, natsMetricsReporter);
            subscriptionsMap.put((ObjectValue) pair.getKey(), sub);
            serviceListeners.remove(); // avoids a ConcurrentModificationException
        }
    }

    private static Subscription createSubscription(ObjectValue service, StreamingListener messageHandler,
                                                   io.nats.streaming.StreamingConnection streamingConnection,
                                                   NatsMetricsReporter natsMetricsReporter) {
        MapValue<BString, Object> annotation = (MapValue<BString, Object>) service.getType()
                .getAnnotation(Constants.NATS_PACKAGE, STREAMING_SUBSCRIPTION_CONFIG);
        assertNull(annotation, "Streaming configuration annotation not present.");
        String subject = annotation.getStringValue(SUBJECT_ANNOTATION_FIELD).getValue();
        assertNull(subject, "`Subject` annotation field is mandatory");
        String queueName = annotation.getStringValue(QUEUE_NAME_ANNOTATION_FIELD).getValue();
        SubscriptionOptions subscriptionOptions = buildSubscriptionOptions(annotation);
        String consoleOutput = "subject " + subject + (queueName != null ? " & queue " + queueName : "");
        try {
            Subscription subscription =
                    streamingConnection.subscribe(subject, queueName, messageHandler, subscriptionOptions);
            console.println(NATS_CLIENT_SUBSCRIBED + consoleOutput);
            NatsMetricsReporter.reportSubscription(streamingConnection.getNatsConnection().getConnectedUrl(), subject);
            return subscription;
        } catch (IOException | InterruptedException e) {
            natsMetricsReporter.reportConsumerError(subject, NatsObservabilityConstants.ERROR_TYPE_SUBSCRIPTION);
            throw Utils.createNatsError(e.getMessage());
        } catch (TimeoutException e) {
            natsMetricsReporter.reportConsumerError(subject, NatsObservabilityConstants.ERROR_TYPE_SUBSCRIPTION);
            throw Utils.createNatsError("Error while creating the subscription");
        }
    }

    private static SubscriptionOptions buildSubscriptionOptions(MapValue<BString, Object> annotation) {
        SubscriptionOptions.Builder builder = new SubscriptionOptions.Builder();
        String durableName = annotation.getStringValue(DURABLE_NAME_ANNOTATION_FIELD).getValue();
        int maxInFlight = annotation.getIntValue(MAX_IN_FLIGHT_ANNOTATION_FIELD).intValue();
        int ackWait = annotation.getIntValue(ACK_WAIT_ANNOTATION_FIELD).intValue();
        int subscriptionTimeout = annotation.getIntValue(SUBSCRIPTION_TIMEOUT_ANNOTATION_FIELD).intValue();
        boolean manualAck = annotation.getBooleanValue(MANUAL_ACK_ANNOTATION_FIELD);
        Object startPosition = annotation.get(START_POSITION_ANNOTATION_FIELD);

        setStartPositionInBuilder(builder, startPosition);
        builder.durableName(durableName).maxInFlight(maxInFlight).ackWait(Duration.ofSeconds(ackWait))
                .subscriptionTimeout(Duration.ofSeconds(subscriptionTimeout));
        if (manualAck) {
            builder.manualAcks();
        }
        return builder.build();
    }

    private static void setStartPositionInBuilder(SubscriptionOptions.Builder builder, Object startPosition) {
        BType type = TypeChecker.getType(startPosition);
        int startPositionType = type.getTag();
        switch (startPositionType) {
            case TypeTags.STRING_TAG:
                BallerinaStartPosition startPositionValue = BallerinaStartPosition.valueOf((String) startPosition);
                if (startPositionValue.equals(BallerinaStartPosition.LAST_RECEIVED)) {
                    builder.startWithLastReceived();
                } else if (startPositionValue.equals(BallerinaStartPosition.FIRST)) {
                    builder.deliverAllAvailable();
                }
                // The else scenario is when the Start Position is "NEW_ONLY". There is no option to set this
                // to the builder since this is the default.
                break;
            case TypeTags.TUPLE_TAG:
                ArrayValue tupleValue = (ArrayValue) startPosition;
                String startPositionKind = (String) tupleValue.getRefValue(0);
                long timeOrSequenceNo = (Long) tupleValue.getRefValue(1);
                if (startPositionKind.equals(BallerinaStartPosition.TIME_DELTA_START.name())) {
                    builder.startAtTimeDelta(Duration.ofSeconds(timeOrSequenceNo));
                } else {
                    builder.startAtSequence(timeOrSequenceNo);
                }
                break;
            default:
                throw new AssertionError("Invalid type for start position value " + startPositionType);
        }
    }

    private static void assertNull(Object nullableObject, String errorMessage) {
        if (nullableObject == null) {
            throw Utils.createNatsError(errorMessage);
        }
    }

    /**
     * Enum representing the constant values of the Ballerina level StartPosition type.
     */
    private enum BallerinaStartPosition {
        NEW_ONLY, LAST_RECEIVED, FIRST, TIME_DELTA_START, SEQUENCE_NUMBER;
    }

    static {
        console = System.out;
    }
}
