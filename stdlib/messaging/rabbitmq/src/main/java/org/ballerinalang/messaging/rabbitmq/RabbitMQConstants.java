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

package org.ballerinalang.messaging.rabbitmq;

import static org.ballerinalang.util.BLangConstants.ORG_NAME_SEPARATOR;

/**
 * RabbitMQ Connector Constants.
 *
 * @since 0.995.0
 */
public class RabbitMQConstants {

    // RabbitMQ package name constant fields
    public static final String ORG_NAME = "ballerina";
    public static final String RABBITMQ = "rabbitmq";
    public static final String PACKAGE_RABBITMQ = ORG_NAME + ORG_NAME_SEPARATOR + RABBITMQ;

    // Error constant fields
    static final String RABBITMQ_ERROR_RECORD = "RabbitMQError";
    static final String RABBITMQ_ERROR_CODE = "{ballerina/rabbitmq}RabbitMQError";
    public static final String RABBITMQ_CLIENT_ERROR = "RabbitMQ Client Error: ";

    // Connection errors
    public static final String CLOSE_CONNECTION_ERROR = "An error occurred while closing the connection: ";
    public static final String ABORT_CONNECTION_ERROR = "An error occurred while aborting the connection: ";
    public static final String CREATE_CONNECTION_ERROR = "An error occurred while connecting to the broker: ";

    // Channel errors
    public static final String CLOSE_CHANNEL_ERROR = "An error occurred while closing the channel: ";
    public static final String ABORT_CHANNEL_ERROR = "An error occurred while aborting the channel: ";
    public static final String CHANNEL_CLOSED_ERROR = "Channel already closed, messages will no longer be received: ";

    // Connection constant fields
    public static final String CONNECTION_OBJECT = "Connection";
    public static final String CONNECTION_NATIVE_OBJECT = "rabbitmq_connection_object";
    public static final String CHANNEL_REFERENCE = "amqpChannel";

    // Connection configuration constant fields
    public static final String RABBITMQ_CONNECTION_HOST = "host";
    public static final String RABBITMQ_CONNECTION_PORT = "port";
    public static final String RABBITMQ_CONNECTION_USER = "username";
    public static final String RABBITMQ_CONNECTION_PASS = "password";
    public static final String RABBITMQ_CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String RABBITMQ_CONNECTION_HANDSHAKE_TIMEOUT = "handshakeTimeout";
    public static final String RABBITMQ_CONNECTION_SHUTDOWN_TIMEOUT = "shutdownTimeout";
    public static final String RABBITMQ_CONNECTION_HEARTBEAT = "heartbeat";

    // Channel listener constant fields
    public static final String QUEUE_CONFIG = "queueConfig";
    public static final String ACK_MODE = "ackMode";
    public static final String PREFETCH_COUNT = "prefetchCount";
    public static final String PREFETCH_SIZE = "prefetchSize";
    public static final String PREFETCH_GLOBAL = "global";
    public static final String CHANNEL_LISTENER_OBJECT = "ChannelListener";
    public static final String SERVICE_CONFIG = "ServiceConfig";
    public static final String CONSUMER_SERVICES = "consumer_services";
    public static final String AUTO_ACKMODE = "auto";
    public static final String CLIENT_ACKMODE = "client";
    public static final int DEFAULT_PREFETCH = 10;
    public static final String MULTIPLE_ACK_ERROR = "Trying to acknowledge the same message multiple times";
    public static final String ACK_MODE_ERROR = "Trying to acknowledge messages in auto-ack mode";

    // Channel constant fields
    public static final String QOS_STATUS = "qos_status";
    public static final String CHANNEL_OBJECT = "Channel";
    public static final String CHANNEL_NATIVE_OBJECT = "rabbitmq_channel_object";

    // Message constant fields
    public static final String MESSAGE_OBJECT = "Message";
    public static final String MESSAGE_CONTENT = "message_content";
    public static final String DELIVERY_TAG = "delivery_tag";
    public static final String MESSAGE_ACK_STATUS = "message_ack_status";
    public static final String AUTO_ACK_STATUS = "message_ack_status";

    // Queue configuration constant fields
    public static final String ALIAS_QUEUE_NAME = "queueName";
    public static final String ALIAS_QUEUE_DURABLE = "durable";
    public static final String ALIAS_QUEUE_EXCLUSIVE = "exclusive";
    public static final String ALIAS_QUEUE_AUTODELETE = "autoDelete";

    // Exchange configuration constant fields
    public static final String ALIAS_EXCHANGE_NAME = "exchangeName";
    public static final String ALIAS_EXCHANGE_TYPE = "exchangeType";
    public static final String ALIAS_EXCHANGE_DURABLE = "durable";
    public static final String ALIAS_EXCHANGE_AUTODELETE = "autoDelete";

    // Warning suppression
    public static final String UNCHECKED = "unchecked";

    private RabbitMQConstants() {
    }
}
