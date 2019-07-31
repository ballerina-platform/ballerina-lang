// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/system;

# Struct which represents Kafka Producer configuration.
#
# + bootstrapServers - List of remote server endpoints of Kafka brokers.
# + acks - Number of acknowledgments.
# + compressionType - Compression type to be used for messages.
# + clientId - Identifier to be used for server side logging.
# + metricsRecordingLevel - Metrics recording level.
# + metricReporterClasses - Metrics reporter classes.
# + partitionerClass - Partitioner class to be used to select partition to which the message is sent.
# + interceptorClasses - Interceptor classes to be used before sending records.
# + transactionalId - Transactional ID to be used in transactional delivery.
# + bufferMemory - Total bytes of memory the producer can use to buffer records.
# + noRetries - Number of retries to resend a record.
# + batchSize - Number of records to be batched for a single request. Use 0 for no batching.
# + linger - Delay to allow other records to be batched.
# + sendBuffer - Size of the TCP send buffer (SO_SNDBUF).
# + receiveBuffer - Size of the TCP receive buffer (SO_RCVBUF).
# + maxRequestSize - The maximum size of a request in bytes.
# + reconnectBackoff - Time to wait before attempting to reconnect.
# + reconnectBackoffMax - Maximum amount of time in milliseconds to wait when reconnecting.
# + retryBackoff - Time to wait before attempting to retry a failed request.
# + maxBlock - Maximum block time which the send is blocked, when the buffer is full.
# + requestTimeout - Wait time for response of a request.
# + metadataMaxAge - Maximum time to force a refresh of metadata.
# + metricsSampleWindow - Time window for a metrics sample to computed over.
# + metricsNumSamples - Number of samples maintained to compute metrics.
# + maxInFlightRequestsPerConnection - Maximum number of unacknowledged requests on a single connection.
# + connectionsMaxIdle - Close idle connections after the number of milliseconds.
# + transactionTimeout - Timeout for transaction status update from the producer.
# + enableIdempotence - Exactly one copy of each message is written in the stream when enabled.
# + secureSocket - SSL/TLS related options
public type ProducerConfig record {|
    string? bootstrapServers = (); // BOOTSTRAP_SERVERS_CONFIG 0
    string? acks = (); // ACKS_CONFIG 1
    string? compressionType = (); // COMPRESSION_TYPE_CONFIG 2
    string? clientId = (); // CLIENT_ID_CONFIG 3
    string? metricsRecordingLevel = (); // METRICS_RECORDING_LEVEL_CONFIG 4
    string? metricReporterClasses = (); // METRIC_REPORTER_CLASSES_CONFIG 5
    string? partitionerClass = (); // PARTITIONER_CLASS_CONFIG 6
    string? interceptorClasses = (); // INTERCEPTOR_CLASSES_CONFIG 7
    string? transactionalId = (); // TRANSACTIONAL_ID_CONFIG 8

    int bufferMemory = -1; // BUFFER_MEMORY_CONFIG 0
    int noRetries = -1; // RETRIES_CONFIG 1
    int batchSize = -1; // BATCH_SIZE_CONFIG 2
    int linger = -1; // LINGER_MS_CONFIG 3
    int sendBuffer = -1; // SEND_BUFFER_CONFIG 4
    int receiveBuffer = -1; // RECEIVE_BUFFER_CONFIG 5
    int maxRequestSize = -1; // MAX_REQUEST_SIZE_CONFIG 6
    int reconnectBackoff = -1; // RECONNECT_BACKOFF_MS_CONFIG 7
    int reconnectBackoffMax = -1; // RECONNECT_BACKOFF_MAX_MS_CONFIG  8
    int retryBackoff = -1; // RETRY_BACKOFF_MS_CONFIG 9
    int maxBlock = -1; // MAX_BLOCK_MS_CONFIG 10
    int requestTimeout = -1; // REQUEST_TIMEOUT_MS_CONFIG  11
    int metadataMaxAge = -1; // METADATA_MAX_AGE_CONFIG 12
    int metricsSampleWindow = -1; // METRICS_SAMPLE_WINDOW_MS_CONFIG 13
    int metricsNumSamples = -1; // METRICS_NUM_SAMPLES_CONFIG  14
    int maxInFlightRequestsPerConnection = -1; // MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION 15
    int connectionsMaxIdle = -1; // CONNECTIONS_MAX_IDLE_MS_CONFIG 16
    int transactionTimeout = -1; // TRANSACTION_TIMEOUT_CONFIG 17

    boolean enableIdempotence = false; // ENABLE_IDEMPOTENCE_CONFIG 0

    SecureSocket secureSocket?;
|};

# Represent a Kafka producer endpoint.
#
# + connectorId - Unique ID for a particular connector.
# + producerConfig - Used to store configurations related to a Kafka connection.
public type Producer client object {

    public ProducerConfig? producerConfig = ();

    public function __init(ProducerConfig config) {
        self.producerConfig = config;
        var result = self.init(config);
        if (result is error) {
            panic result;
        }
    }

    # Initialize the producer endpoint. Panics if the initialization fails.
    #
    # + config - Configurations related to the endpoint.
    # + return - `kafka:ProducerError` if fails to initiate the `kafka:Producer`
    function init(ProducerConfig config) returns error? = external;

    public string connectorId = system:uuid();

    # Closes producer connection to the external Kafka broker.
    #
    # + return - `kafka:ProducerError` if closing the producer failed, none otherwise.
    public remote function close() returns ProducerError? = external;

    # Commits consumer action which commits consumer consumed offsets to offset topic.
    #
    # + consumer - Consumer which needs offsets to be committed.
    # + return - `kafka:ProducerError` if committing the consumer failed, none otherwise.
    public remote function commitConsumer(Consumer consumer) returns ProducerError? = external;

    # CommitConsumerOffsets action which commits consumer offsets in given transaction.
    #
    # + offsets - Consumer offsets to commit for given transaction.
    # + groupID - Consumer group id.
    # + return - `kafka:ProducerError` if committing consumer offsets failed, none otherwise.
    public remote function commitConsumerOffsets(PartitionOffset[] offsets, string groupID) returns ProducerError? = external;

    # Flush action which flush batch of records.
    #
    # + return - `kafka:ProducerError` if records couldn't be flushed, none otherwise.
    public remote function flushRecords() returns ProducerError? = external;

    # GetTopicPartitions action which returns given topic partition information.
    #
    # + topic - Topic which the partition information is given.
    # + return - Partitions for the given topic, returns `kafka:ProducerError` if operation fails.
    public remote function getTopicPartitions(string topic) returns TopicPartition[]|ProducerError = external;

    # Simple Send action which produce records to Kafka server.
    #
    # + value - Record contents.
    # + topic - Topic to which the record will be appended to.
    # + key - Key that will be included in the record.
    # + partition - Partition to which the record should be sent.
    # + timestamp - Timestamp of the record, in milliseconds since epoch.
    # + return - Returns `kafka:ProducerError` if send action fails to send data, none otherwise.
    public remote function send(
                                          byte[] value,
                                          string topic,
                                          public byte[]? key = (),
                                          public int? partition = (),
                                          public int? timestamp = ()
                                      ) returns ProducerError? = external;

};
