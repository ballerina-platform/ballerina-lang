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

package org.ballerinalang.messaging.kafka.observability;

/**
 * Constants for Kafka Observability.
 *
 * @since 1.2.0
 */
public class KafkaObservabilityConstants {

    static final String CONNECTOR_NAME = "kafka";

    static final String[] METRIC_CONNECTIONS = {"connections", "Number of currently active connections"};
    static final String[] METRIC_PUBLISHERS = {"publishers", "Number of currently active publishers"};
    static final String[] METRIC_CONSUMERS = {"consumers", "Number of currently active consumers"};
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
    static final String TAG_TOPIC = "topic";
    static final String TAG_ERROR_TYPE = "error_type";
    static final String TAG_CONTEXT = "context";
    static final String TAG_CLIENT_ID = "client_id";
    static final String TAG_PRODUCER_ID = "producer_id";
    static final String TAG_CONSUMER_ID = "consumer_id";

    public static final String ERROR_TYPE_CONNECTION = "connection";
    public static final String ERROR_TYPE_PUBLISH = "publish";
    public static final String ERROR_TYPE_CLOSE = "close";
    public static final String ERROR_TYPE_MSG_RECEIVED = "message_received";

    public static final String ERROR_TYPE_COMMIT = "commit";
    public static final String ERROR_TYPE_FLUSH = "flush";
    public static final String ERROR_TYPE_TOPIC_PARTITIONS = "topic_partitions";
    public static final String ERROR_TYPE_ASSIGN = "assign";
    public static final String ERROR_TYPE_GET_ASSIGNMENT = "get_assignment";
    public static final String ERROR_TYPE_GET_TOPICS = "get_topics";
    public static final String ERROR_TYPE_GET_BEG_OFFSETS = "get_beginning_offsets";
    public static final String ERROR_TYPE_GET_COMMIT_OFFSET = "get_committed_offset";
    public static final String ERROR_TYPE_GET_END_OFFSETS = "get_end_offsets";
    public static final String ERROR_TYPE_GET_PAUSED_PARTITIONS = "get_paused_partitions";
    public static final String ERROR_TYPE_GET_POSITION_OFFSET = "get_position_offset";
    public static final String ERROR_TYPE_GET_SUBSCRIPTION = "get_subscription";
    public static final String ERROR_TYPE_GET_TOPIC_PARTITIONS = "get_topic_partitions";
    public static final String ERROR_TYPE_PAUSE = "get_pause";
    public static final String ERROR_TYPE_POLL = "poll";
    public static final String ERROR_TYPE_RESUME = "resume";
    public static final String ERROR_TYPE_SEEK = "seek";
    public static final String ERROR_TYPE_SEEK_BEG = "seek_to_beginning";
    public static final String ERROR_TYPE_SEEK_END = "seek_to_end";
    public static final String ERROR_TYPE_SUBSCRIBE = "subscribe";
    public static final String ERROR_TYPE_SUBSCRIBE_PATTERN = "subscribe_to_pattern";
    public static final String ERROR_TYPE_SUBSCRIBE_PARTITION_REBALANCE = "subscribe_with_partition_rebalance";
    public static final String ERROR_TYPE_UNSUBSCRIBE = "unsubscribe";

    public static final String CONTEXT_PRODUCER = "producer";
    public static final String CONTEXT_CONSUMER = "consumer";

    public static final String UNKNOWN = "unknown";

    private KafkaObservabilityConstants() {
    }
}
