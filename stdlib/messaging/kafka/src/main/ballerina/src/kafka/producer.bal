// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
# + acks - Number of acknowledgments. This can be either `kafka:ACKS_ALL`, `kafka:ACKS_SINGLE` or `kafka:ACKS_NONE`.
# + compressionType - Compression type to be used for messages.
# + clientId - Identifier to be used for server side logging.
# + metricsRecordingLevel - Metrics recording level.
# + metricReporterClasses - Metrics reporter classes.
# + partitionerClass - Partitioner class to be used to select partition to which the message is sent.
# + interceptorClasses - Interceptor classes to be used before sending records.
# + transactionalId - Transactional ID to be used in transactional delivery.
# + bufferMemory - Total bytes of memory the producer can use to buffer records.
# + retryCount - Number of retries to resend a record.
# + batchSize - Number of records to be batched for a single request. Use 0 for no batching.
# + linger - Delay to allow other records to be batched.
# + sendBuffer - Size of the TCP send buffer (SO_SNDBUF).
# + receiveBuffer - Size of the TCP receive buffer (SO_RCVBUF).
# + maxRequestSize - The maximum size of a request in bytes.
# + reconnectBackoffTimeInMillis - Time to wait before attempting to reconnect.
# + reconnectBackoffMaxTimeInMillis - Maximum amount of time in milliseconds to wait when reconnecting.
# + retryBackoffTimeInMillis - Time to wait before attempting to retry a failed request.
# + maxBlock - Maximum block time which the send is blocked, when the buffer is full.
# + requestTimeoutInMillis - Wait time for response of a request.
# + metadataMaxAgeInMillis - Maximum time to force a refresh of metadata.
# + metricsSampleWindowInMillis - Time window for a metrics sample to computed over.
# + metricsNumSamples - Number of samples maintained to compute metrics.
# + maxInFlightRequestsPerConnection - Maximum number of unacknowledged requests on a single connection.
# + connectionsMaxIdleTimeInMillis - Close idle connections after the number of milliseconds.
# + transactionTimeoutInMillis - Timeout for transaction status update from the producer.
# + enableIdempotence - Exactly one copy of each message is written in the stream when enabled.
# + secureSocket - Configurations related to SSL/TLS.
public type ProducerConfig record {|
    string? bootstrapServers = ();
    Producer_Acks acks = ACKS_SINGLE;
    string? compressionType = ();
    string? clientId = ();
    string? metricsRecordingLevel = ();
    string? metricReporterClasses = ();
    string? partitionerClass = ();
    string? interceptorClasses = ();
    string? transactionalId = ();

    int bufferMemory = -1;
    int retryCount = -1;
    int batchSize = -1;
    int linger = -1;
    int sendBuffer = -1;
    int receiveBuffer = -1;
    int maxRequestSize = -1;
    int reconnectBackoffTimeInMillis = -1;
    int reconnectBackoffMaxTimeInMillis = -1;
    int retryBackoffTimeInMillis = -1;
    int maxBlock = -1;
    int requestTimeoutInMillis = -1;
    int metadataMaxAgeInMillis = -1;
    int metricsSampleWindowInMillis = -1;
    int metricsNumSamples = -1;
    int maxInFlightRequestsPerConnection = -1;
    int connectionsMaxIdleTimeInMillis = -1;
    int transactionTimeoutInMillis = -1;

    boolean enableIdempotence = false;

    SecureSocket secureSocket?;
|};

# Producer acknowledgement type `all`. This will gurantee that the record will not be lost as long as at least one
# in-sync replica is alive.
public const ACKS_ALL = "all";

# Producer acknowledgement type `0`. If the acknowledgement type set to this, the producer will not wait for any
# acknowledgement from the server.
public const ACKS_NONE = "0";

# Producer acknowledgement type `1`. If the acknowledgement type set to this, the leader will write the record to its
# local log but will respond without awaiting full acknowledgement from all followers.
public const ACKS_SINGLE = "1";

# Kafka producer acknowledgement type.
public type Producer_Acks ACKS_ALL|ACKS_NONE|ACKS_SINGLE;

# Represent a Kafka producer endpoint.
#
# + connectorId - Unique ID for a particular connector.
# + producerConfig - Used to store configurations related to a Kafka connection.
public type Producer client object {

    public ProducerConfig? producerConfig = ();

    # Creates a new Kafka `Producer`.
    #
    # + config - Configurations related to initializing a Kafka `Producer`.
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
    # + return - `kafka:ProducerError` if fails to initiate the `kafka:Producer`, nil otherwise.
    function init(ProducerConfig config) returns error? = external;

    public string connectorId = system:uuid();

    # Closes producer connection to the external Kafka broker.
    #
    # + return - `kafka:ProducerError` if closing the producer failed, nil otherwise.
    public remote function close() returns ProducerError? = external;

    # Commits consumer action which commits consumer consumed offsets to offset topic.
    #
    # + consumer - Consumer which needs offsets to be committed.
    # + return - `kafka:ProducerError` if committing the consumer failed, nil otherwise.
    public remote function commitConsumer(Consumer consumer) returns ProducerError? = external;

    # CommitConsumerOffsets action which commits consumer offsets in given transaction.
    #
    # + offsets - Consumer offsets to commit for given transaction.
    # + groupID - Consumer group id.
    # + return - `kafka:ProducerError` if committing consumer offsets failed, nil otherwise.
    public remote function commitConsumerOffsets(PartitionOffset[] offsets, string groupID) returns ProducerError? = external;

    # Flush action which flush batch of records.
    #
    # + return - `kafka:ProducerError` if records couldn't be flushed, nil otherwise.
    public remote function flushRecords() returns ProducerError? = external;

    # GetTopicPartitions action which returns given topic partition information.
    #
    # + topic - Topic which the partition information is given.
    # + return - `kafka:TopicPartition` array for the given topic, returns `kafka:ProducerError` if operation fails.
    public remote function getTopicPartitions(string topic) returns TopicPartition[]|ProducerError = external;

    # Simple Send action which produce records to Kafka server.
    #
    # + value - Record contents.
    # + topic - Topic to which the record will be appended to.
    # + key - Key that will be included in the record.
    # + partition - Partition to which the record should be sent.
    # + timestamp - Timestamp of the record, in milliseconds since epoch.
    # + return - Returns `kafka:ProducerError` if send action fails to send data, nil otherwise.
    public remote function send(byte[] value, string topic, public byte[]? key = (), public int? partition = (),
                                public int? timestamp = ()) returns ProducerError? = external;

};
