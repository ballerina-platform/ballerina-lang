/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq.nativeimpl.listener;

import com.rabbitmq.client.Channel;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.MessageDispatcher;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQTransactionContext;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Starting the channel listener.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.LISTENER_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ)
)
public class Start {
    private static boolean started = false;

    public static Object start(Strand strand, ObjectValue listenerObjectValue) {
        boolean autoAck;
        ObjectValue channelObject = (ObjectValue) listenerObjectValue.get(RabbitMQConstants.CHANNEL_REFERENCE);
        Channel channel = (Channel) channelObject.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        RabbitMQTransactionContext rabbitMQTransactionContext = (RabbitMQTransactionContext) channelObject.
                getNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<ObjectValue> services =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.CONSUMER_SERVICES);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<ObjectValue> startedServices =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.STARTED_SERVICES);
        if (services != null && !services.isEmpty()) {
            for (ObjectValue service : services) {
                if (startedServices == null || !startedServices.contains(service)) {
                    MapValue serviceConfig =
                            (MapValue) service.getType().getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ,
                                    RabbitMQConstants.SERVICE_CONFIG);
                    @SuppressWarnings(RabbitMQConstants.UNCHECKED)
                    MapValue<String, Object> queueConfig =
                            (MapValue<String, Object>) serviceConfig.getMapValue(RabbitMQConstants.ALIAS_QUEUE_CONFIG);
                    autoAck = getAckMode(service);
                    boolean isQosSet = channelObject.getNativeData(RabbitMQConstants.QOS_STATUS) != null;
                    if (!isQosSet) {
                        try {
                            handleBasicQos(channel, queueConfig);
                        } catch (RabbitMQConnectorException exception) {
                            return RabbitMQUtils.returnErrorValue("Error occurred while setting the QoS settings."
                                    + exception.getDetail());
                        }
                    }
                    MessageDispatcher messageDispatcher = new MessageDispatcher(listenerObjectValue,
                            rabbitMQTransactionContext, service, channel, autoAck, strand.scheduler);
                    messageDispatcher.receiveMessages();
                }
            }
        }
        started = true;
        return null;
    }

    static void startReceivingMessages(ObjectValue service, RabbitMQTransactionContext rabbitMQTransactionContext,
                                       Channel channel, ObjectValue listenerObjectValue, Scheduler scheduler) {
        MessageDispatcher messageDispatcher = new MessageDispatcher(listenerObjectValue, rabbitMQTransactionContext,
                service, channel, getAckMode(service), scheduler);
        messageDispatcher.receiveMessages();

    }

    private static void handleBasicQos(Channel channel, MapValue<String, Object> serviceConfig) {
        long prefetchCount = RabbitMQConstants.DEFAULT_PREFETCH;

        if (serviceConfig.getIntValue(RabbitMQConstants.ALIAS_PREFETCH_COUNT) != null) {
            prefetchCount = serviceConfig.getIntValue(RabbitMQConstants.ALIAS_PREFETCH_COUNT);
        }
        boolean isValidPrefetchSize = serviceConfig.getIntValue(RabbitMQConstants.ALIAS_PREFETCH_SIZE) != null;
        try {
            if (isValidPrefetchSize) {
                channel.basicQos(Math.toIntExact(serviceConfig.getIntValue(RabbitMQConstants.ALIAS_PREFETCH_SIZE)),
                        Math.toIntExact(prefetchCount), false);
            } else {
                channel.basicQos(Math.toIntExact(prefetchCount));
            }
        } catch (IOException | ArithmeticException exception) {
            throw new RabbitMQConnectorException("An error occurred while setting the basic QoS settings; "
                    + exception.getMessage(), exception);
        }
    }

    static boolean isStarted() {
        return started;
    }

    private static boolean getAckMode(ObjectValue service) {
        boolean autoAck;
        MapValue serviceConfig = (MapValue) service.getType().getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ,
                RabbitMQConstants.SERVICE_CONFIG);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        String ackMode = serviceConfig.getStringValue(RabbitMQConstants.ALIAS_ACK_MODE);
        switch (ackMode) {
            case RabbitMQConstants.AUTO_ACKMODE:
                autoAck = true;
                break;
            case RabbitMQConstants.CLIENT_ACKMODE:
                autoAck = false;
                break;
            default:
                throw RabbitMQUtils.returnErrorValue("Unsupported acknowledgement mode");
        }
        return autoAck;
    }

    private Start() {
    }
}
