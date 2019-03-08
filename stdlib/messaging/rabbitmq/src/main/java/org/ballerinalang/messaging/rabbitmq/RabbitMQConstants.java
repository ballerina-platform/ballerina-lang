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
 */
public class RabbitMQConstants {

    // RabbitMQ package name constant fields
    public static final String ORG_NAME = "ballerina";
    public static final String PACKAGE_RABBITMQ = "rabbitmq";
    public static final String STRUCT_PACKAGE_RABBITMQ = ORG_NAME
            + ORG_NAME_SEPARATOR + PACKAGE_RABBITMQ;

    // Error constant fields
    public static final String RABBITMQ_ERROR_RECORD = "RabbitMQError";
    public static final String RABBITMQ_ERROR_CODE = "{ballerina/rabbitmq}RabbitMQError";

    // Connection constant fields
    public static final String CONNECTION_STRUCT = "Connection";
    public static final String CONNECTION_NATIVE_OBJECT = "rabbitmq_connection_object";

    // Connection configuration constant fields
    public static final String RABBITMQ_CONNECTION_HOST = "host";
    public static final String RABBITMQ_CONNECTION_PORT = "port";
    public static final String RABBITMQ_CONNECTION_USER = "username";
    public static final String RABBITMQ_CONNECTION_PASS = "password";

    // Channel constant fields
    public static final String CHANNEL_STRUCT = "Channel";
    public static final String CHANNEL_NATIVE_OBJECT = "rabbitmq_channel_object";

    // Queue configuration constant fields
    public static final String ALIAS_QUEUE_NAME = "queueName";
    public static final String ALIAS_QUEUE_DURABLE = "durable";
    public static final String ALIAS_QUEUE_EXCLUSIVE = "exclusive";
    public static final String ALIAS_QUEUE_AUTODELETE = "autoDelete";
    public static final String ALIAS_QUEUE_ARGUMENTS = "arguments";

    // Exchange configuration constant fields
    public static final String ALIAS_EXCHANGE_NAME = "exchangeName";
    public static final String ALIAS_EXCHANGE_TYPE = "exchangeType";
    public static final String ALIAS_EXCHANGE_DURABLE = "durable";

    // Message publishing constant fields
}
