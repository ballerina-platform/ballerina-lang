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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.MessageDispatcher;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Util class for RabbitMQ Listener handling.
 *
 * @since 1.0.4
 */
public class ListenerUtils {
    private static boolean started = false;
    private static final PrintStream console;
    private static List<ObjectValue> services = Collections.synchronizedList(new ArrayList<>());
    private static List<ObjectValue> startedServices = Collections.synchronizedList(new ArrayList<>());
    private static boolean qosStatus = false;
    private static BRuntime runtime;

    public static Object nativeSetQosSettings(Object prefetchCount, Object prefetchSize, ObjectValue channelObj) {
        runtime = BRuntime.getCurrentRuntime();
        Channel channel = (Channel) channelObj.get(RabbitMQConstants.JAVA_CLIENT_CHANNEL);
        boolean isValidCount = prefetchCount != null &&
                RabbitMQUtils.checkIfInt(prefetchCount);
        try {
            if (isValidCount) {
                boolean isValidSize = prefetchSize != null && RabbitMQUtils.checkIfInt(prefetchSize);
                if (isValidSize) {
                    channel.basicQos(Math.toIntExact(((Number) prefetchSize).longValue()),
                            Math.toIntExact(((Number) prefetchCount).longValue()), true);
                } else {
                    channel.basicQos(Math.toIntExact((Long) prefetchCount), true);
                }
                qosStatus = true;
            }
        } catch (IOException exception) {
            return RabbitMQUtils.returnErrorValue("An I/O error occurred while setting the global " +
                    "quality of service settings for the listener: " + exception.getMessage());
        }
        return null;
    }

    public static Object registerListener(ObjectValue channelObj, ObjectValue service) {
        Channel channel = (Channel) channelObj.get(RabbitMQConstants.JAVA_CLIENT_CHANNEL);
        if (service != null) {
            try {
                declareQueueIfNotExists(service, channel);
            } catch (IOException e) {
                return RabbitMQUtils.returnErrorValue("I/O Error occurred while declaring the queue.");
            }
            if (started) {
                startReceivingMessages(service, channel);
            }
            services.add(service);
        }
        return null;
    }

    private static void declareQueueIfNotExists(ObjectValue service, Channel channel) throws IOException {
        MapValue serviceConfig = (MapValue) service.getType().getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ,
                RabbitMQConstants.SERVICE_CONFIG);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        MapValue<Strand, Object> queueConfig =
                (MapValue) serviceConfig.getMapValue(RabbitMQConstants.ALIAS_QUEUE_CONFIG);
        Map<String, Object> argumentsMap = null;
        if (queueConfig.getMapValue(RabbitMQConstants.QUEUE_ARGUMENTS) != null) {
            argumentsMap =
                    (HashMap<String, Object>) queueConfig.getMapValue(RabbitMQConstants.QUEUE_ARGUMENTS);
        }
        String queueName = queueConfig.getStringValue(RabbitMQConstants.QUEUE_NAME);
        boolean durable = queueConfig.getBooleanValue(RabbitMQConstants.QUEUE_DURABLE);
        boolean exclusive = queueConfig.getBooleanValue(RabbitMQConstants.QUEUE_EXCLUSIVE);
        boolean autoDelete = queueConfig.getBooleanValue(RabbitMQConstants.QUEUE_AUTO_DELETE);
        channel.queueDeclare(queueName, durable, exclusive, autoDelete, argumentsMap);
    }

    public static Object start(ObjectValue channelObj) {
        Channel channel = (Channel) channelObj.get(RabbitMQConstants.JAVA_CLIENT_CHANNEL);
        boolean autoAck;
        if (services == null || services.isEmpty()) {
            return null;
        }
        for (ObjectValue service : services) {
            if (startedServices == null || !startedServices.contains(service)) {
                MapValue serviceConfig =
                        (MapValue) service.getType().getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ,
                                RabbitMQConstants.SERVICE_CONFIG);
                @SuppressWarnings(RabbitMQConstants.UNCHECKED)
                MapValue<String, Object> queueConfig =
                        (MapValue<String, Object>) serviceConfig.getMapValue(RabbitMQConstants.ALIAS_QUEUE_CONFIG);
                autoAck = getAckMode(service);
                if (!qosStatus) {
                    try {
                        handleBasicQos(channel, queueConfig);
                    } catch (RabbitMQConnectorException exception) {
                        return RabbitMQUtils.returnErrorValue("Error occurred while setting the QoS settings."
                                + exception.getDetail());
                    }
                }
                MessageDispatcher messageDispatcher = new MessageDispatcher(service, channel, autoAck, runtime);
                messageDispatcher.receiveMessages();
            }
        }
        started = true;
        return null;
    }

    public static Object abortConnection(ObjectValue channelObj) {
        Channel channel = (Channel) channelObj.get(RabbitMQConstants.JAVA_CLIENT_CHANNEL);
        if (channel == null) {
            return RabbitMQUtils.returnErrorValue("Listener is not properly initialised.");
        }
        try {
            Connection connection = channel.getConnection();
            channel.abort();
            connection.abort();
        } catch (IOException exception) {
            return RabbitMQUtils.returnErrorValue(RabbitMQConstants.CLOSE_CHANNEL_ERROR
                    + exception.getMessage());
        }
        return null;
    }

    public static Object detach(ObjectValue channelObj, ObjectValue service) {
        Channel channel = (Channel) channelObj.get(RabbitMQConstants.JAVA_CLIENT_CHANNEL);
        String serviceName = service.getType().getName();
        String queueName = (String) service.getNativeData(RabbitMQConstants.QUEUE_NAME);
        try {
            channel.basicCancel(serviceName);
            console.println("[ballerina/rabbitmq] Consumer service unsubscribed from the queue " + queueName);
        } catch (IOException e) {
            return RabbitMQUtils.returnErrorValue("Error occurred while detaching the service");
        }
        return null;
    }

    public static Object stop(ObjectValue channelObj) {
        Channel channel = (Channel) channelObj.get(RabbitMQConstants.JAVA_CLIENT_CHANNEL);
        if (channel == null) {
            return RabbitMQUtils.returnErrorValue("Listener is not properly initialised.");
        } else {
            try {
                Connection connection = channel.getConnection();
                channel.close();
                connection.close();
            } catch (IOException | TimeoutException exception) {
                return RabbitMQUtils.returnErrorValue(RabbitMQConstants.CLOSE_CHANNEL_ERROR
                        + exception.getMessage());
            }
        }
        return null;
    }

    private static void startReceivingMessages(ObjectValue service, Channel channel) {
        MessageDispatcher messageDispatcher = new MessageDispatcher(service, channel, getAckMode(service), runtime);
        messageDispatcher.receiveMessages();
    }

    private static boolean getAckMode(ObjectValue service) {
        boolean autoAck;
        MapValue serviceConfig = (MapValue) service.getType().getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ,
                RabbitMQConstants.SERVICE_CONFIG);
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
            throw RabbitMQUtils.returnErrorValue("An error occurred while setting the basic QoS settings; "
                    + exception.getMessage());
        }
    }

    public static void updateServiceList(ObjectValue service) {
        startedServices.add(service);
    }

    static {
        console = System.out;
    }

    private ListenerUtils() {
    }
}
