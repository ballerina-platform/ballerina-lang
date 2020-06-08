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

package org.ballerinalang.messaging.rabbitmq.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.messaging.rabbitmq.MessageDispatcher;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQMetricsUtil;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQObservabilityConstants;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQTracingUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Util class for RabbitMQ Listener actions handling.
 *
 * @since 1.1.0
 */
public class ListenerUtils {
    private static final PrintStream console;
    private static boolean started = false;
    private static ArrayList<ObjectValue> services = new ArrayList<>();
    private static ArrayList<ObjectValue> startedServices = new ArrayList<>();
    private static BRuntime runtime;

    public static void init(ObjectValue listenerObjectValue, Channel channel) {
        listenerObjectValue.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, channel);
        listenerObjectValue.addNativeData(RabbitMQConstants.CONSUMER_SERVICES, services);
        listenerObjectValue.addNativeData(RabbitMQConstants.STARTED_SERVICES, startedServices);
        RabbitMQMetricsUtil.reportNewConsumer(channel);
    }

    public static Object registerListener(ObjectValue listenerObjectValue, ObjectValue service) {
        runtime = BRuntime.getCurrentRuntime();
        Channel channel = (Channel) listenerObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        if (service == null) {
            return null;
        }
        try {
            declareQueueIfNotExists(service, channel);
        } catch (IOException e) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_REGISTER);
            return RabbitMQUtils.returnErrorValue("I/O Error occurred while declaring the queue: " +
                    e.getMessage());
        }
        if (isStarted()) {
            services =
                    (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.CONSUMER_SERVICES);
            startReceivingMessages(service, channel, listenerObjectValue);
        }
        services.add(service);
        return null;
    }

    public static Object start(ObjectValue listenerObjectValue) {
        runtime = BRuntime.getCurrentRuntime();
        boolean autoAck;
        ObjectValue channelObject = (ObjectValue) listenerObjectValue.get(RabbitMQConstants.CHANNEL_REFERENCE);
        Channel channel = (Channel) listenerObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<ObjectValue> services =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.CONSUMER_SERVICES);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<ObjectValue> startedServices =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.STARTED_SERVICES);
        if (services == null || services.isEmpty()) {
            return null;
        }
        for (ObjectValue service : services) {
            if (startedServices == null || !startedServices.contains(service)) {
                MapValue serviceConfig = (MapValue) service.getType()
                        .getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ_FQN,
                                       RabbitMQConstants.SERVICE_CONFIG);
                @SuppressWarnings(RabbitMQConstants.UNCHECKED)
                MapValue<BString, Object> queueConfig =
                        (MapValue<BString, Object>) serviceConfig.getMapValue(RabbitMQConstants.ALIAS_QUEUE_CONFIG);
                autoAck = getAckMode(service);
                boolean isQosSet = channelObject.getNativeData(RabbitMQConstants.QOS_STATUS) != null;
                if (!isQosSet) {
                    try {
                        handleBasicQos(channel, queueConfig);
                    } catch (RabbitMQConnectorException exception) {
                        RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_START);
                        return RabbitMQUtils.returnErrorValue("Error occurred while setting the QoS settings."
                                + exception.getDetail());
                    }
                }
                MessageDispatcher messageDispatcher = new MessageDispatcher(service, channel, autoAck, runtime);
                messageDispatcher.receiveMessages(listenerObjectValue);
                RabbitMQMetricsUtil.reportSubscription(channel, service);
            }
        }
        started = true;
        return null;
    }

    public static Object detach(ObjectValue listenerObjectValue, ObjectValue service) {
        Channel channel = (Channel) listenerObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<ObjectValue> startedServices =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.STARTED_SERVICES);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<ObjectValue> services =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.CONSUMER_SERVICES);
        String serviceName = service.getType().getName();
        String queueName = (String) service.getNativeData(RabbitMQConstants.QUEUE_NAME.getValue());
        try {
            channel.basicCancel(serviceName);
            console.println("[ballerina/rabbitmq] Consumer service unsubscribed from the queue " + queueName);
        } catch (IOException e) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_DETACH);
            return RabbitMQUtils.returnErrorValue("Error occurred while detaching the service");
        }
        listenerObjectValue.addNativeData(RabbitMQConstants.CONSUMER_SERVICES,
                RabbitMQUtils.removeFromList(services, service));
        listenerObjectValue.addNativeData(RabbitMQConstants.STARTED_SERVICES,
                RabbitMQUtils.removeFromList(startedServices, service));
        RabbitMQMetricsUtil.reportUnsubscription(channel, service);
        RabbitMQTracingUtil.traceQueueResourceInvocation(channel, queueName);
        return null;
    }

    public static Object getChannel(ObjectValue listenerObjectValue) {
        ObjectValue channel = (ObjectValue) listenerObjectValue.get(RabbitMQConstants.CHANNEL_REFERENCE);
        if (channel != null) {
            return channel;
        } else {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_GET_CHANNEL);
            return RabbitMQUtils.returnErrorValue("Error occurred while retrieving the Channel," +
                    " Channel is not properly initialized");
        }
    }

    private static void declareQueueIfNotExists(ObjectValue service, Channel channel) throws IOException {
        MapValue serviceConfig = (MapValue) service.getType()
                .getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ_FQN, RabbitMQConstants.SERVICE_CONFIG);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        MapValue<Strand, Object> queueConfig =
                (MapValue) serviceConfig.getMapValue(RabbitMQConstants.ALIAS_QUEUE_CONFIG);
        String queueName = queueConfig.getStringValue(RabbitMQConstants.QUEUE_NAME).getValue();
        boolean durable = queueConfig.getBooleanValue(RabbitMQConstants.QUEUE_DURABLE);
        boolean exclusive = queueConfig.getBooleanValue(RabbitMQConstants.QUEUE_EXCLUSIVE);
        boolean autoDelete = queueConfig.getBooleanValue(RabbitMQConstants.QUEUE_AUTO_DELETE);
        channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
        RabbitMQMetricsUtil.reportNewQueue(channel, queueName);
    }

    public static Object setQosSettings(Object prefetchCount, Object prefetchSize,
                                              ObjectValue listenerObjectValue) {
        ObjectValue channelObject =
                (ObjectValue) listenerObjectValue.get(RabbitMQConstants.CHANNEL_REFERENCE);
        Channel channel = (Channel) listenerObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        boolean isValidCount = prefetchCount != null &&
                RabbitMQUtils.checkIfInt(prefetchCount);
        try {
            if (isValidCount) {
                boolean isValidSize = prefetchSize != null && RabbitMQUtils.checkIfInt(prefetchSize);
                if (isValidSize) {
                    channel.basicQos(Math.toIntExact(((Number) prefetchSize).longValue()),
                            Math.toIntExact(((Number) prefetchCount).longValue()),
                            true);
                    channelObject.addNativeData(RabbitMQConstants.QOS_STATUS, true);
                } else {
                    channel.basicQos(Math.toIntExact((Long) prefetchCount), true);
                    channelObject.addNativeData(RabbitMQConstants.QOS_STATUS, true);
                }
            }
        } catch (IOException exception) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_SET_QOS);
            return BallerinaErrors.createError("An I/O error occurred while setting the global " +
                    "quality of service settings for the listener");
        }
        return null;
    }

    private static void startReceivingMessages(ObjectValue service, Channel channel, ObjectValue listener) {
        MessageDispatcher messageDispatcher = new MessageDispatcher(service, channel, getAckMode(service), runtime);
        messageDispatcher.receiveMessages(listener);

    }

    private static void handleBasicQos(Channel channel, MapValue<BString, Object> serviceConfig) {
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

    private static boolean isStarted() {
        return started;
    }

    private static boolean getAckMode(ObjectValue service) {
        boolean autoAck;
        MapValue serviceConfig = (MapValue) service.getType()
                .getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ_FQN, RabbitMQConstants.SERVICE_CONFIG);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        String ackMode = serviceConfig.getStringValue(RabbitMQConstants.ALIAS_ACK_MODE).getValue();
        switch (ackMode) {
            case RabbitMQConstants.AUTO_ACKMODE:
                autoAck = true;
                break;
            case RabbitMQConstants.CLIENT_ACKMODE:
                autoAck = false;
                break;
            default:
                RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_START);
                throw RabbitMQUtils.returnErrorValue("Unsupported acknowledgement mode");
        }
        return autoAck;
    }

    public static Object stop(ObjectValue listenerObjectValue) {
        Channel channel = (Channel) listenerObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        if (channel == null) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_STOP);
            return RabbitMQUtils.returnErrorValue("ChannelListener is not properly initialised.");
        } else {
            try {
                Connection connection = channel.getConnection();
                RabbitMQMetricsUtil.reportBulkUnsubscription(channel, listenerObjectValue);
                RabbitMQMetricsUtil.reportConsumerClose(channel);
                RabbitMQMetricsUtil.reportChannelClose(channel);
                RabbitMQMetricsUtil.reportConnectionClose(connection);
                channel.close();
                connection.close();
            } catch (IOException | TimeoutException exception) {
                return RabbitMQUtils.returnErrorValue(RabbitMQConstants.CLOSE_CHANNEL_ERROR
                        + exception.getMessage());
            }
        }
        return null;
    }

    public static Object abortConnection(ObjectValue listenerObjectValue) {
        Channel channel = (Channel) listenerObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        if (channel == null) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION_ABORT);
            return RabbitMQUtils.returnErrorValue("ChannelListener is not properly initialised.");
        } else {
            Connection connection = channel.getConnection();
            RabbitMQMetricsUtil.reportBulkUnsubscription(channel, listenerObjectValue);
            RabbitMQMetricsUtil.reportConsumerClose(channel);
            RabbitMQMetricsUtil.reportChannelClose(channel);
            RabbitMQMetricsUtil.reportConnectionClose(connection);
            connection.abort();
        }
        return null;
    }

    static {
        console = System.out;
    }
}
