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

package org.ballerinalang.nats.observability;

/**
 * Constants for NATS Observability.
 *
 * @since 1.1.0
 */
public class NatsObservabilityConstants {

    static final String CONNECTOR_NAME = "nats";

    static final String[] METRIC_CONNECTIONS = {"connections", "Number of currently active connections"};
    static final String[] METRIC_PUBLISHERS = {"publishers", "Number of currently active publishers"};
    static final String[] METRIC_PUBLISHED = {"published", "Number of messages published"};
    static final String[] METRIC_PUBLISHED_SIZE = {"published_size", "Total size in bytes of messages published"};
    static final String[] METRIC_ERRORS = {"errors", "Number of errors"};
    static final String[] METRIC_REQUEST = {"requests", "Number of requests sent"};
    static final String[] METRIC_RESPONSE = {"responses", "Number of responses_received"};
    static final String[] METRIC_SUBSCRIPTION = {"subscriptions", "Number of subscriptions"};
    static final String[] METRIC_CONSUMED = {"consumed", "Number of messages consumed"};
    static final String[] METRIC_DELIVERED = {"delivered", "Number of messages successfully received by consumer"};
    static final String[] METRIC_CONSUMED_SIZE = {"consumed_size", "Total size in bytes of messages consumed"};
    static final String[] METRIC_ACK = {"acknowledgements", "Total number of acknowledgements received"};

    static final String TAG_URL = "url";
    static final String TAG_SUBJECT = "subject";
    static final String TAG_ERROR_TYPE = "error_type";
    static final String TAG_CONTEXT = "context";
    static final String TAG_REQUEST_OR_RESPONSE = "request_or_response";

    public static final String ERROR_TYPE_CONNECTION = "connection";
    public static final String ERROR_TYPE_PUBLISH = "publish";
    public static final String ERROR_TYPE_REQUEST = "request";
    public static final String ERROR_TYPE_CLOSE = "close";
    public static final String ERROR_TYPE_SUBSCRIPTION = "subscription";
    public static final String ERROR_TYPE_MSG_RECEIVED = "message_received";
    public static final String ERROR_TYPE_ACKNOWLEDGEMENT = "acknowledgement";
    public static final String ERROR_TYPE_EXCEPTION = "exception";
    public static final String ERROR_TYPE_SLOW_CONSUMER = "slow_consumer";
    public static final String ERROR_TYPE_ON_ERROR = "on_error";

    public static final String CONTEXT_CONNECTION = "connection";
    public static final String CONTEXT_PRODUCER = "producer";
    public static final String CONTEXT_CONSUMER = "consumer";
    public static final String CONTEXT_STREAMING_CONNNECTION = "streaming_connection";

    public static final String UNKNOWN = "unknown";

    private NatsObservabilityConstants() {
    }

}
