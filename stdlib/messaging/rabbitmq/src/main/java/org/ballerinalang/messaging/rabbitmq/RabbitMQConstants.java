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

import org.ballerinalang.jvm.types.BPackage;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;

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
    public static final BPackage PACKAGE_ID_RABBITMQ = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, "rabbitmq");

    // Error constant fields
    public static final String RABBITMQ_ERROR_CODE = "{ballerina/rabbitmq}Error";
    static final String RABBITMQ_ERROR_DETAILS = "Detail";
    static final String RABBITMQ_ERROR_MESSAGE = "message";
    static final String RABBITMQ_ERROR_CAUSE = "cause";
    public static final String RABBITMQ_CLIENT_ERROR = "RabbitMQ Client Error: ";

    // Connection errors
    public static final String CLOSE_CONNECTION_ERROR = "An error occurred while closing the connection: ";
    public static final String CREATE_CONNECTION_ERROR = "An error occurred while connecting to the broker: ";
    public static final String CREATE_SECURE_CONNECTION_ERROR = "An error occurred while configuring " +
            "the SSL connection: ";

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
    public static final String RABBITMQ_CONNECTION_TIMEOUT = "connectionTimeoutInMillis";
    public static final String RABBITMQ_CONNECTION_HANDSHAKE_TIMEOUT = "handshakeTimeoutMillis";
    public static final String RABBITMQ_CONNECTION_SHUTDOWN_TIMEOUT = "shutdownTimeoutInMillis";
    public static final String RABBITMQ_CONNECTION_HEARTBEAT = "heartbeatInSeconds";
    public static final String RABBITMQ_CONNECTION_SECURE_SOCKET = "secureSocket";
    public static final String RABBITMQ_CONNECTION_KEYSTORE = "keyStore";
    public static final String RABBITMQ_CONNECTION_TRUSTORE = "trustStore";
    public static final String RABBITMQ_CONNECTION_VERIFY_HOST = "verifyHostname";
    public static final String RABBITMQ_CONNECTION_TLS_VERSION = "tlsVersion";
    public static final String KEY_STORE_TYPE = "PKCS12";
    public static final String KEY_STORE_PASS = "password";
    public static final String KEY_STORE_PATH = "path";

    // Channel listener constant fields
    public static final String LISTENER_OBJECT = "Listener";
    public static final String SERVICE_CONFIG = "ServiceConfig";
    public static final String ALIAS_QUEUE_CONFIG = "queueConfig";
    public static final String ALIAS_ACK_MODE = "ackMode";
    public static final String ALIAS_PREFETCH_COUNT = "prefetchCount";
    public static final String ALIAS_PREFETCH_SIZE = "prefetchSize";
    public static final String CONSUMER_SERVICES = "consumer_services";
    public static final String STARTED_SERVICES = "started_services";
    public static final String AUTO_ACKMODE = "auto";
    public static final String CLIENT_ACKMODE = "client";
    public static final int DEFAULT_PREFETCH = 10;
    public static final String MULTIPLE_ACK_ERROR = "Trying to acknowledge the same message multiple times";
    public static final String ACK_MODE_ERROR = "Trying to acknowledge messages in auto-ack mode";
    static final String THREAD_INTERRUPTED = "Error occurred in RabbitMQ service. " +
            "The current thread got interrupted";
    public static final String ACK_ERROR = "Error occurred while positively acknowledging the message: ";
    public static final String NACK_ERROR = "Error occurred while negatively acknowledging the message: ";
    static final String FUNC_ON_MESSAGE = "onMessage";
    static final String FUNC_ON_ERROR = "onError";

    // Channel constant fields
    public static final String QOS_STATUS = "qos_status";
    public static final String CHANNEL_OBJECT = "Channel";
    public static final String CHANNEL_NATIVE_OBJECT = "rabbitmq_channel_object";

    // Message constant fields
    public static final String MESSAGE_OBJECT = "Message";
    static final String MESSAGE_OBJ_FULL_NAME = PACKAGE_RABBITMQ + ":" + MESSAGE_OBJECT;
    public static final String MESSAGE_CONTENT = "message_content";
    public static final String DELIVERY_TAG = "delivery_tag";
    public static final String MESSAGE_ACK_STATUS = "message_ack_status";
    public static final String AUTO_ACK_STATUS = "message_ack_status";
    public static final String BASIC_PROPERTIES = "basic_properties";
    public static final String XML_CONTENT_ERROR = "Error while retrieving the xml content of the message. ";
    public static final String JSON_CONTENT_ERROR = "Error while retrieving the json content of the message. ";
    public static final String TEXT_CONTENT_ERROR = "Error while retrieving the string content of the message. ";
    public static final String INT_CONTENT_ERROR = "Error while retrieving the int content of the message. ";
    public static final String FLOAT_CONTENT_ERROR = "Error while retrieving the float content of the message. ";
    static final String DISPATCH_ERROR = "Error occurred while dispatching the message. ";

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

    // Transaction constant fields
    public static final String RABBITMQ_TRANSACTION_CONTEXT = "rabbitmq_transactional_context";
    static final String COMMIT_FAILED = "Transaction commit failed: ";
    static final String ROLLBACK_FAILED = "Transaction rollback failed: ";

    // Basic Properties constant fields
    public static final String RECORD_BASIC_PROPERTIES = "BasicProperties";
    public static final String ALIAS_REPLY_TO = "replyTo";
    public static final String ALIAS_CONTENT_TYPE = "contentType";
    public static final String ALIAS_CONTENT_ENCODING = "contentEncoding";
    public static final String ALIAS_CORRELATION_ID = "correlationId";

    private RabbitMQConstants() {
    }
}
