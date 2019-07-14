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

import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Remote function implementation for subscribing to a NATS subject.
 */
@BallerinaFunction(orgName = "ballerina",
                   packageName = "nats",
                   functionName = "subscribe",
                   receiver = @Receiver(type = TypeKind.OBJECT,
                                        structType = "StreamingListener",
                                        structPackage = "ballerina/nats"),
                   isPublic = true)
public class Subscribe implements NativeCallableUnit {
    private static final String STREAMING_SUBSCRIPTION_CONFIG = "StreamingSubscriptionConfig";
    private static final String SUBJECT_ANNOTATION_FIELD = "subject";
    private static final String QUEUE_NAME_ANNOTATION_FIELD = "queueName";
    private static final String DURABLE_NAME_ANNOTATION_FIELD = "durableName";
    private static final String MAX_IN_FLIGHT_ANNOTATION_FIELD = "maxInFlight";
    private static final String ACK_WAIT_ANNOTATION_FIELD = "ackWait";
    private static final String SUBSCRIPTION_TIMEOUT_ANNOTATION_FIELD = "subscriptionTimeout";
    private static final String MANUAL_ACK_ANNOTATION_FIELD = "manualAck";
    private static final String START_POSITION_ANNOTATION_FIELD = "startPosition";

    @Override
    public void execute(Context context, CallableUnitCallback callback) {

    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static void subscribe(Strand strand, ObjectValue streamingListener, ObjectValue service,
            ObjectValue connection) {
        StreamingConnection streamingConnection = (StreamingConnection) streamingListener
                .getNativeData(Constants.NATS_STREAMING_CONNECTION);
        MapValue<String, Object> annotation = (MapValue<String, Object>) service.getType()
                .getAnnotation(Constants.NATS_PACKAGE, STREAMING_SUBSCRIPTION_CONFIG);
        try {
            StreamingListener messageHandler = new StreamingListener(service, strand.scheduler);
            assertNull(annotation, "Streaming configuration annotation not present.");
            String subject = annotation.getStringValue(SUBJECT_ANNOTATION_FIELD);
            assertNull(subject, "`Subject` annotation field is mandatory");
            String queueName = annotation.getStringValue(QUEUE_NAME_ANNOTATION_FIELD);
            SubscriptionOptions subscriptionOptions = buildSubscriptionOptions(annotation);
            streamingConnection.subscribe(subject, queueName, messageHandler, subscriptionOptions);

            List<ObjectValue> serviceList = (List<ObjectValue>) connection.getNativeData(Constants.SERVICE_LIST);
            serviceList.add(service);
        } catch (IOException | TimeoutException e) {
            throw Utils.createNatsError(e.getMessage());
        } catch (InterruptedException e) {
            throw Utils.createNatsError("Error while creating the subscription");
        }
    }

    private static SubscriptionOptions buildSubscriptionOptions(MapValue<String, Object> annotation) {
        SubscriptionOptions.Builder builder = new SubscriptionOptions.Builder();
        String durableName = annotation.getStringValue(DURABLE_NAME_ANNOTATION_FIELD);
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

}
