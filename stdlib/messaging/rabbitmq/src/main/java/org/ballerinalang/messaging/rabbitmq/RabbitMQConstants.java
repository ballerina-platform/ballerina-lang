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
    public static final String PACKAGE_RABBITMQ = ORG_NAME
            + ORG_NAME_SEPARATOR + RABBITMQ;

    // Error constant fields
    public static final String RABBITMQ_ERROR_RECORD = "RabbitMQError";
    public static final String RABBITMQ_ERROR_CODE = "{ballerina/rabbitmq}RabbitMQError";

    // Connection errors
    public static final String CLOSE_CONNECTION_ERROR = "An error occurred while closing the connection ";
    public static final String CREATE_CONNECTION_ERROR = "An error occurred while connecting to the broker";

    // Channel errors
    public static final String CLOSE_CHANNEL_ERROR = "An error occurred while closing the channel";

    // Connection constant fields
    public static final String CONNECTION_OBJECT = "Connection";
    public static final String CONNECTION_NATIVE_OBJECT = "rabbitmq_connection_object";

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
    public static final String CHANNEL_LISTENER_OBJECT = "ChannelListener";
    public static final String SERVICE_CONFIG = "ServiceConfig";

    // Channel constant fields
    public static final String CHANNEL_OBJECT = "Channel";
    public static final String CHANNEL_NATIVE_OBJECT = "rabbitmq_channel_object";

    // Queue configuration constant fields
    public static final String ALIAS_QUEUE_NAME = "queueName";
    public static final String ALIAS_QUEUE_DURABLE = "durable";
    public static final String ALIAS_QUEUE_EXCLUSIVE = "exclusive";
    public static final String ALIAS_QUEUE_AUTODELETE = "autoDelete";

    // Exchange configuration constant fields
    public static final String ALIAS_EXCHANGE_NAME = "exchangeName";
    public static final String ALIAS_EXCHANGE_TYPE = "exchangeType";
    public static final String ALIAS_EXCHANGE_DURABLE = "durable";
}
