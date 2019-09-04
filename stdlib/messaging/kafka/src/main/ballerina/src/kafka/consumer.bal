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

import ballerina/lang.'object as lang;

# Configuration related to consumer endpoint.
#
# + bootstrapServers - List of remote server endpoints of kafka brokers.
# + groupId - Unique string that identifies the consumer.
# + offsetReset - Offset reset strategy if no initial offset.
# + partitionAssignmentStrategy - Strategy class for handling the partition assignment among consumers.
# + metricsRecordingLevel - Metrics recording level.
# + metricsReporterClasses - Metrics reporter classes.
# + clientId - Identifier to be used for server side logging.
# + interceptorClasses - Interceptor classes to be used before sending records.
# + isolationLevel - Transactional message reading method. Use "read_committed" to read committed messages only in transactional mode when poll() is called. Use "read_uncommitted" to read all the messages, even the aborted ones.
# + topics - Topics to be subscribed by the consumer.
# + properties - Additional properties if required.
# + sessionTimeoutInMillis - Timeout used to detect consumer failures when heartbeat threshold is reached.
# + heartBeatIntervalInMillis - Expected time between heartbeats.
# + metadataMaxAgeInMillis - Maximum time to force a refresh of metadata.
# + autoCommitIntervalInMillis - Auto committing interval for commit offset, when auto-commit is enabled.
# + maxPartitionFetchBytes - The maximum amount of data per-partition the server returns.
# + sendBuffer - Size of the TCP send buffer (SO_SNDBUF).
# + receiveBuffer - Size of the TCP receive buffer (SO_RCVBUF).
# + fetchMinBytes - Minimum amount of data the server should return for a fetch request.
# + fetchMaxBytes - Maximum amount of data the server should return for a fetch request.
# + fetchMaxWaitTimeInMillis - Maximum amount of time the server will block before answering the fetch request.
# + reconnectBackoffTimeMaxInMillis - Maximum amount of time in milliseconds to wait when reconnecting.
# + retryBackoffInMillis - Time to wait before attempting to retry a failed request.
# + metricsSampleWindowInMillis - Window of time a metrics sample is computed over.
# + metricsNumSamples - Number of samples maintained to compute metrics.
# + requestTimeoutInMillis - Wait time for response of a request.
# + connectionMaxIdleTimeInMillis - Close idle connections after the number of milliseconds.
# + maxPollRecords - Maximum number of records returned in a single call to poll.
# + maxPollInterval - Maximum delay between invocations of poll.
# + reconnectBackoffTimeInMillis - Time to wait before attempting to reconnect.
# + pollingTimeoutInMillis - Timeout interval for polling.
# + pollingIntervalInMillis - Polling interval for the consumer.
# + concurrentConsumers - Number of concurrent consumers.
# + defaultApiTimeoutInMillis - Default API timeout value for APIs with duration.
# + autoCommit - Enables auto committing offsets.
# + checkCRCS - Check the CRC32 of the records consumed.
# + excludeInternalTopics - Whether records from internal topics should be exposed to the consumer.
# + decoupleProcessing - Decouples processing
# + secureSocket - SSL/TLS related options
public type ConsumerConfig record {|
    string? bootstrapServers = ();
    string? groupId = ();
    string? offsetReset = ();
    string? partitionAssignmentStrategy = ();
    string? metricsRecordingLevel = ();
    string? metricsReporterClasses = ();
    string? clientId = ();
    string? interceptorClasses = ();
    string? isolationLevel = ();

    string[]? topics = ();
    string[]? properties = ();

    int sessionTimeoutInMillis = -1;
    int heartBeatIntervalInMillis = -1;
    int metadataMaxAgeInMillis = -1;
    int autoCommitIntervalInMillis = -1;
    int maxPartitionFetchBytes = -1;
    int sendBuffer = -1;
    int receiveBuffer = -1;
    int fetchMinBytes = -1;
    int fetchMaxBytes = -1;
    int fetchMaxWaitTimeInMillis = -1;
    int reconnectBackoffTimeMaxInMillis = -1;
    int retryBackoffInMillis = -1;
    int metricsSampleWindowInMillis = -1;
    int metricsNumSamples = -1;
    int requestTimeoutInMillis = -1;
    int connectionMaxIdleTimeInMillis = -1;
    int maxPollRecords = -1;
    int maxPollInterval = -1;
    int reconnectBackoffTimeInMillis = -1;
    int pollingTimeoutInMillis = -1;
    int pollingIntervalInMillis = -1;
    int concurrentConsumers = -1;
    int defaultApiTimeoutInMillis = 30000;

    boolean autoCommit = true;
    boolean checkCRCS = true;
    boolean excludeInternalTopics = true;
    boolean decoupleProcessing = false;

    SecureSocket secureSocket?;
|};

# Type related to consumer record.
#
# + key - Key that is included in the record.
# + value - Record content.
# + offset - Offset value.
# + partition - Partition in which the record is stored.
# + timestamp - Timestamp of the record, in milliseconds since epoch.
# + topic - Topic to which the record belongs to.
public type ConsumerRecord record {|
    byte[] key;
    byte[] value;
    int offset;
    int partition;
    int timestamp;
    string topic;
|};

# Represent a Kafka consumer endpoint.
#
# + consumerConfig - Used to store configurations related to a Kafka connection.
public type Consumer client object {
    *lang:Listener;

    public ConsumerConfig? consumerConfig = ();

    public function __init (ConsumerConfig config) {
        self.consumerConfig = config;
        var initResult = self.init(config);
        if (initResult is error) {
            panic initResult;
        }
    }

    public function __start() returns error? {
        return self.start();
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return self.stop();
    }

    public function __attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function __detach(service s) returns error? {
    }

    function init(ConsumerConfig config) returns ConsumerError? {
        if (config.bootstrapServers is string) {
            var result = self->connect();
            if (result is error) {
                panic result;
            }
        }

        string[]? topics = config.topics;
        if (topics is string[]){
            var result = self->subscribe(topics);
            if (result is error) {
                panic result;
            }
        }
        return;
    }

    function register(service serviceType, string? name) returns ConsumerError? = external;

    function start() returns ConsumerError? = external;

    function stop() returns ConsumerError? = external;

    # Assigns consumer to a set of topic partitions.
    #
    # + partitions - Topic partitions to be assigned.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function assign(TopicPartition[] partitions) returns ConsumerError? = external;

    # Closes consumer connection to the external Kafka broker.
    #
    # + duration - Timeout duration for the close operation execution.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function close(public int duration = -1) returns ConsumerError? = external;

    # Commits current consumed offsets for consumer.
    #
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function commit() returns ConsumerError? = external;

    # Commits given offsets and partitions for the given topics, for consumer.
    #
    # + duration - Timeout duration for the commit operation execution.
    # + offsets - Offsets to be commited.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function commitOffset(PartitionOffset[] offsets, public int duration = -1)
                                                                        returns ConsumerError? = external;

    # Connects consumer to the provided host in the consumer configs.
    #
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function connect() returns ConsumerError? = external;

    # Returns the currently assigned partitions for the consumer.
    #
    # + return - Array of assigned partitions for the consumer if executes successfully, `kafka:ConsumerError` otherwise.
    public remote function getAssignment() returns TopicPartition[]|ConsumerError = external;

    # Returns the available list of topics for a particular consumer.
    #
    # + duration - Timeout duration for the get available topics execution.
    # + return - Array of topics currently available (authorized) for the consumer to subscribe, returns `kafka:ConsumerError` if the operation fails.
    public remote function getAvailableTopics(public int duration = -1) returns string[]|ConsumerError = external;

    # Returns start offsets for given set of partitions.
    #
    # + partitions - Array of topic partitions to get the starting offsets.
    # + duration - Timeout duration for the get beginning offsets execution.
    # + return - Starting offsets for the given partitions if executes successfully, returns `kafka:ConsumerError` if the operation fails.
    public remote function getBeginningOffsets(TopicPartition[] partitions, public int duration = -1)
                               returns PartitionOffset[]|ConsumerError = external;

    # Returns last committed offsets for the given topic partitions.
    #
    # + partition - Topic partition in which the committed offset is returned for consumer.
    # + duration - Timeout duration for the get committed offset operation to execute.
    # + return - Committed offset for the consumer for the given partition if executes successfully, returns `kafka:ConsumerError` if the operation fails.
    public remote function getCommittedOffset(TopicPartition partition, public int duration = -1)
                               returns PartitionOffset|ConsumerError = external;

    # Returns last offsets for given set of partitions.
    #
    # + partitions - Set of partitions to get the last offsets.
    # + duration - Timeout duration for the get end offsets operation to execute.
    # + return - End offsets for the given partitions if executes successfully, returns `kafka:ConsumerError` if the operation fails.
    public remote function getEndOffsets(TopicPartition[] partitions, public int duration = -1)
                               returns PartitionOffset[]|ConsumerError = external;

    # Returns the partitions, which are currently paused.
    #
    # + return - Set of partitions paused from message retrieval if executes successfully, returns `kafka:ConsumerError` if the operation fails.
    public remote function getPausedPartitions() returns TopicPartition[]|ConsumerError = external;

    # Returns the offset of the next record that will be fetched, if a records exists in that position.
    #
    # + partition - Topic partition in which the position is required.
    # + duration - Timeout duration for the get position offset operation to execute.
    # + return - Offset which will be fetched next (if a records exists in that offset), returns `kafka:ConsumerError` if the operation fails.
    public remote function getPositionOffset(TopicPartition partition, public int duration = -1) returns int|ConsumerError = external;

    # Returns set of topics wich are currently subscribed by the consumer.
    #
    # + return - Array of subscribed topics for the consumer if executes successfully, returns `kafka:ConsumerError` if the operation fails.
    public remote function getSubscription() returns string[]|ConsumerError = external;

    # Retrieve the set of partitions in which the topic belongs.
    #
    # + topic - Given topic for partition information is needed.
    # + duration - Timeout duration for the get topic partitions operation to execute.
    # + return - Array of partitions for the given topic if executes successfully, returns `kafka:ConsumerError` if the operation fails.
    public remote function getTopicPartitions(string topic, public int duration = -1) returns TopicPartition[]|ConsumerError = external;

    # Pause consumer retrieving messages from set of partitions.
    #
    # + partitions - Set of partitions to pause the retrieval of messages.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function pause(TopicPartition[] partitions) returns ConsumerError? = external;

    # Poll the consumer for external broker for records.
    #
    # + timeoutValue - Polling time in milliseconds.
    # + return - Array of consumer records if executes successfully, returns `kafka:ConsumerError` if the operation fails.
    public remote function poll(int timeoutValue) returns ConsumerRecord[]|ConsumerError = external;

    # Resume consumer retrieving messages from set of partitions which were paused earlier.
    #
    # + partitions - Set of partitions to resume the retrieval of messages.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function resume(TopicPartition[] partitions) returns ConsumerError? = external;

    # Seek the consumer for a given offset in a topic partition.
    #
    # + offset - PartitionOffset to seek.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function seek(PartitionOffset offset) returns ConsumerError? = external;

    # Seek consumer to the beginning of the offsets for the given set of topic partitions.
    #
    # + partitions - Set of topic partitions to seek.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function seekToBeginning(TopicPartition[] partitions) returns ConsumerError? = external;

    # Seek consumer for end of the offsets for the given set of topic partitions.
    #
    # + partitions - Set of topic partitions to seek.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function seekToEnd(TopicPartition[] partitions) returns ConsumerError? = external;

    # Subscribes the consumer to the provided set of topics.
    #
    # + topics - Array of topics to be subscribed.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function subscribe(string[] topics) returns ConsumerError? = external;

    # Subscribes the consumer to the topics which matches to the provided pattern.
    #
    # + regex - Pattern which should be matched with the topics to be subscribed.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function subscribeToPattern(string regex) returns ConsumerError? = external;

    # Subscribes to consumer to the provided set of topics with rebalance listening is enabled.
    # This function can be used inside a service, to subscribe to a set of topics, while rebalancing the patition
    # assignment of the consumers.
    #
    # + topics - Array of topics to be subscribed.
    # + onPartitionsRevoked - Function which will be executed if partitions are revoked from this consumer.
    # + onPartitionsAssigned - Function which will be executed if partitions are assigned this consumer.
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function subscribeWithPartitionRebalance(string[] topics,
                           function(Consumer consumer, TopicPartition[] partitions) onPartitionsRevoked,
                           function(Consumer consumer, TopicPartition[] partitions) onPartitionsAssigned)
                           returns ConsumerError? = external;

    # Unsubscribe the consumer from all the topic subscriptions.
    #
    # + return - `kafka:ConsumerError` if encounters an error, returns nil otherwise.
    public remote function unsubscribe() returns ConsumerError? = external;
};
