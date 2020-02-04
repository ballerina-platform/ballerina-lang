/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.messaging.rabbitmq.observability;

/**
 * Constants for RabbitMQ Observability.
 *
 * @since 1.2.0
 */
public class RabbitMQObservabilityConstants {

    static final String CONNECTOR_NAME = "rabbitmq";

    static final String[] METRIC_CONNECTIONS = {"connections", "Number of currently active connections"};
    static final String[] METRIC_CHANNELS = {"channels", "Number of currently active channels"};
    static final String[] METRIC_QUEUES = {"queues", "Number of currently active queues"};
    static final String[] METRIC_EXCHANGES = {"exchanges", "Number of currently active exchanges"};
    static final String[] METRIC_CONSUMERS = {"consumers", "Number of currently active consumers"};
    static final String[] METRIC_PUBLISHED = {"published", "Number of messages published"};
    static final String[] METRIC_PUBLISHED_SIZE = {"published_size", "Total size in bytes of messages published"};
    static final String[] METRIC_ERRORS = {"errors", "Number of errors"};
    static final String[] METRIC_SUBSCRIPTION = {"subscriptions", "Number of subscriptions"};
    static final String[] METRIC_CONSUMED = {"consumed", "Number of messages consumed"};
    static final String[] METRIC_CONSUMED_SIZE = {"consumed_size", "Total size in bytes of messages consumed"};
    static final String[] METRIC_ACK = {"acknowledgements", "Total number of acknowledgements received"};

    static final String TAG_URL = "url";
    static final String TAG_ERROR_TYPE = "error_type";
    public static final String TAG_QUEUE = "queue";
    static final String TAG_CHANNEL = "channel";
    static final String TAG_EXCHANGE = "exchange";
    static final String TAG_ROUTING_KEY = "routing_key";
    static final String TAG_ACK_TYPE = "ack_type";
    static final String TAG_CONSUME_TYPE = "consume_type";

    public static final String ERROR_TYPE_CONNECTION = "connection";
    public static final String ERROR_TYPE_CONNECTION_CLOSE = "connection_close";
    public static final String ERROR_TYPE_CONNECTION_ABORT = "connection_abort";
    public static final String ERROR_TYPE_PUBLISH = "publish";
    public static final String ERROR_TYPE_CONSUME = "consume";
    public static final String ERROR_TYPE_CHANNEL_CREATE = "channel_create";
    public static final String ERROR_TYPE_CHANNEL_CLOSE = "channel_close";
    public static final String ERROR_TYPE_QUEUE_DECLARE = "queue_declare";
    public static final String ERROR_TYPE_QUEUE_BIND = "queue_bind";
    public static final String ERROR_TYPE_QUEUE_DELETE = "queue_delete";
    public static final String ERROR_TYPE_QUEUE_PURGE = "queue_declare";
    public static final String ERROR_TYPE_EXCHANGE_DECLARE = "exchange_declare";
    public static final String ERROR_TYPE_EXCHANGE_DELETE = "exchange_delete";
    public static final String ERROR_TYPE_ABORT = "abort";
    public static final String ERROR_TYPE_GET_CONNECTION = "get_connection";
    public static final String ERROR_TYPE_BASIC_GET = "basic_get";
    public static final String ERROR_TYPE_REGISTER = "register";
    public static final String ERROR_TYPE_START = "start";
    public static final String ERROR_TYPE_STOP = "stop";
    public static final String ERROR_TYPE_DETACH = "detach";
    public static final String ERROR_TYPE_GET_CHANNEL = "get_channel";
    public static final String ERROR_TYPE_SET_QOS = "set_qos";
    public static final String ERROR_TYPE_ACK = "ack";
    public static final String ERROR_TYPE_NACK = "nack";
    public static final String ERROR_TYPE_GET_MSG_CONTENT = "get_message_content";
    public static final String ERROR_TYPE_DISPATCH = "dispatch";
    public static final String ERROR_TYPE_ERROR_DISPATCH = "error_dispatch";

    public static final String CONSUME_TYPE_SERVICE = "service";
    public static final String CONSUME_TYPE_CHANNEL = "channel";

    public static final String ACK = "ack";
    public static final String NACK = "nack";

    public static final String UNKNOWN = "unknown";
    public static final String DEFAULT = "default";

    private RabbitMQObservabilityConstants() {
    }
}
