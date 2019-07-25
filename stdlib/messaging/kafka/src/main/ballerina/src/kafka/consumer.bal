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

import ballerina/'lang\.object as lang;

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
# + sessionTimeout - Timeout used to detect consumer failures when heartbeat threshold is reached.
# + heartBeatInterval - Expected time between heartbeats.
# + metadataMaxAge - Maximum time to force a refresh of metadata.
# + autoCommitInterval - Auto committing interval for commit offset, when auto-commit is enabled.
# + maxPartitionFetchBytes - The maximum amount of data per-partition the server returns.
# + sendBuffer - Size of the TCP send buffer (SO_SNDBUF).
# + receiveBuffer - Size of the TCP receive buffer (SO_RCVBUF).
# + fetchMinBytes - Minimum amount of data the server should return for a fetch request.
# + fetchMaxBytes - Maximum amount of data the server should return for a fetch request.
# + fetchMaxWait - Maximum amount of time the server will block before answering the fetch request.
# + reconnectBackoffMax - Maximum amount of time in milliseconds to wait when reconnecting.
# + retryBackoff - Time to wait before attempting to retry a failed request.
# + metricsSampleWindow - Window of time a metrics sample is computed over.
# + metricsNumSamples - Number of samples maintained to compute metrics.
# + requestTimeout - Wait time for response of a request.
# + connectionMaxIdle - Close idle connections after the number of milliseconds.
# + maxPollRecords - Maximum number of records returned in a single call to poll.
# + maxPollInterval - Maximum delay between invocations of poll.
# + reconnectBackoff - Time to wait before attempting to reconnect.
# + pollingTimeout - Timeout interval for polling.
# + pollingInterval - Polling interval for the consumer.
# + concurrentConsumers - Number of concurrent consumers.
# + defaultApiTimeout - Default API timeout value for APIs with duration.
# + autoCommit - Enables auto committing offsets.
# + checkCRCS - Check the CRC32 of the records consumed.
# + excludeInternalTopics - Whether records from internal topics should be exposed to the consumer.
# + decoupleProcessing - Decouples processing
# + secureSocket - SSL/TLS related options
public type ConsumerConfig record {|
    string? bootstrapServers = (); // BOOTSTRAP_SERVERS_CONFIG 0
    string? groupId = (); // GROUP_ID_CONFIG 1
    string? offsetReset = (); // AUTO_OFFSET_RESET_CONFIG 2
    string? partitionAssignmentStrategy = (); // PARTITION_ASSIGNMENT_STRATEGY_CONFIG 3
    string? metricsRecordingLevel = (); // METRICS_RECORDING_LEVEL_CONFIG 4
    string? metricsReporterClasses = (); // METRIC_REPORTER_CLASSES_CONFIG 5
    string? clientId = (); // CLIENT_ID_CONFIG 6
    string? interceptorClasses = (); // INTERCEPTOR_CLASSES_CONFIG 7
    string? isolationLevel = (); // ISOLATION_LEVEL_CONFIG 8

    string[]? topics = (); // ALIAS_TOPICS 0
    string[]? properties = (); // PROPERTIES_ARRAY 1

    int sessionTimeout = -1; // SESSION_TIMEOUT_MS_CONFIG  0
    int heartBeatInterval = -1; // HEARTBEAT_INTERVAL_MS_CONFIG 1
    int metadataMaxAge = -1; // METADATA_MAX_AGE_CONFIG  2
    int autoCommitInterval = -1; // AUTO_COMMIT_INTERVAL_MS_CONFIG 3
    int maxPartitionFetchBytes = -1; // MAX_PARTITION_FETCH_BYTES_CONFIG 4
    int sendBuffer = -1; // SEND_BUFFER_CONFIG 5
    int receiveBuffer = -1; // RECEIVE_BUFFER_CONFIG 6
    int fetchMinBytes = -1; // FETCH_MIN_BYTES_CONFIG 7
    int fetchMaxBytes = -1; // FETCH_MAX_BYTES_CONFIG 8
    int fetchMaxWait = -1; // FETCH_MAX_WAIT_MS_CONFIG 9
    int reconnectBackoffMax = -1; // RECONNECT_BACKOFF_MAX_MS_CONFIG 10
    int retryBackoff = -1; // RETRY_BACKOFF_MS_CONFIG 11
    int metricsSampleWindow = -1; // METRICS_SAMPLE_WINDOW_MS_CONFIG 12
    int metricsNumSamples = -1; // METRICS_NUM_SAMPLES_CONFIG 13
    int requestTimeout = -1; // REQUEST_TIMEOUT_MS_CONFIG 14
    int connectionMaxIdle = -1; // CONNECTIONS_MAX_IDLE_MS_CONFIG 15
    int maxPollRecords = -1; // MAX_POLL_RECORDS_CONFIG 16
    int maxPollInterval = -1; // MAX_POLL_INTERVAL_MS_CONFIG 17
    int reconnectBackoff = -1; // RECONNECT_BACKOFF_MAX_MS_CONFIG 18
    int pollingTimeout = -1; // ALIAS_POLLING_TIMEOUT 19
    int pollingInterval = -1; // ALIAS_POLLING_INTERVAL 20
    int concurrentConsumers = -1; // ALIAS_CONCURRENT_CONSUMERS 21
    int defaultApiTimeout = 30000; // DEFAULT_API_TIMEOUT_MS_CONFIG 22

    boolean autoCommit = true; // ENABLE_AUTO_COMMIT_CONFIG 0
    boolean checkCRCS = true; // CHECK_CRCS_CONFIG 1
    boolean excludeInternalTopics = true; // EXCLUDE_INTERNAL_TOPICS_CONFIG 2
    boolean decoupleProcessing = false;                 // ALIAS_DECOUPLE_PROCESSING

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
    *lang:AbstractListener;

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

    public function __stop() returns error? {
        return self.stop();
    }

    public function __attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    function init(ConsumerConfig config) returns error? {
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

    function register(service serviceType, string? name) returns error? = external;

    function start() returns error? = external;

    function stop() returns error? = external;

    # Assigns consumer to a set of topic partitions.
    #
    # + partitions - Topic partitions to be assigned.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function assign(TopicPartition[] partitions) returns error? = external;

    # Closes consumer connection to the external Kafka broker.
    #
    # + duration - Timeout duration for the close operation execution.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function close(int duration = -1) returns error? = external;

    # Commits current consumed offsets for consumer.
    #
    # + return - Error if commit fails, none otherwise.
    public remote function commit() returns error? = external;

    # Commits given offsets and partitions for the given topics, for consumer.
    #
    # + duration - Timeout duration for the commit operation execution.
    # + offsets - Offsets to be commited.
    # + return - Error if committing offset is failed, none otherwise.
    public remote function commitOffset(PartitionOffset[] offsets, int duration = -1) returns error? = external;

    # Connects consumer to the provided host in the consumer configs.
    #
    # + return - Returns an error if encounters an error, returns nill otherwise.
    public remote function connect() returns error? = external;

    # Returns the currently assigned partitions for the consumer.
    #
    # + return - Array of assigned partitions for the consumer if executes successfully, error otherwise.
    public remote function getAssignment() returns TopicPartition[]|error = external;

    # Returns the available list of topics for a particular consumer.
    #
    # + duration - Timeout duration for the get available topics execution.
    # + return - Array of topics currently available (authorized) for the consumer to subscribe, returns error if the operation fails..
    public remote function getAvailableTopics(int duration = -1) returns string[]|error = external;

    # Returns start offsets for given set of partitions.
    #
    # + partitions - Array of topic partitions to get the starting offsets.
    # + duration - Timeout duration for the get beginning offsets execution.
    # + return - Starting offsets for the given partitions if executes successfully, error otherwise.
    public remote function getBeginningOffsets(TopicPartition[] partitions, int duration = -1)
                               returns PartitionOffset[]|error = external;

    # Returns last committed offsets for the given topic partitions.
    #
    # + partition - Topic partition in which the committed offset is returned for consumer.
    # + duration - Timeout duration for the get committed offset operation to execute.
    # + return - Committed offset for the consumer for the given partition if executes successfully, error otherwise.
    public remote function getCommittedOffset(TopicPartition partition, int duration = -1)
                               returns PartitionOffset|error = external;

    # Returns last offsets for given set of partitions.
    #
    # + partitions - Set of partitions to get the last offsets.
    # + duration - Timeout duration for the get end offsets operation to execute.
    # + return - End offsets for the given partitions if executes successfully, error otherwise.
    public remote function getEndOffsets(TopicPartition[] partitions, int duration = -1)
                               returns PartitionOffset[]|error = external;

    # Returns the partitions, which are currently paused.
    #
    # + return - Set of partitions paused from message retrieval if executes successfully, error otherwise.
    public remote function getPausedPartitions() returns TopicPartition[]|error = external;

    # Returns the offset of the next record that will be fetched, if a records exists in that position.
    #
    # + partition - Topic partition in which the position is required.
    # + duration - Timeout duration for the get position offset operation to execute.
    # + return - Offset which will be fetched next (if a records exists in that offset), returns error if the operation fails.
    public remote function getPositionOffset(TopicPartition partition, int duration = -1) returns int|error = external;

    # Returns set of topics wich are currently subscribed by the consumer.
    #
    # + return - Array of subscribed topics for the consumer if executes successfully, error otherwise.
    public remote function getSubscription() returns string[]|error = external;

    # Retrieve the set of partitions in which the topic belongs.
    #
    # + topic - Given topic for partition information is needed.
    # + duration - Timeout duration for the get topic partitions operation to execute.
    # + return - Array of partitions for the given topic if executes successfully, error otherwise.
    public remote function getTopicPartitions(string topic, int duration = -1) returns TopicPartition[]|error = external;

    # Pause consumer retrieving messages from set of partitions.
    #
    # + partitions - Set of partitions to pause the retrieval of messages.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function pause(TopicPartition[] partitions) returns error? = external;

    # Poll the consumer for external broker for records.
    #
    # + timeoutValue - Polling time in milliseconds.
    # + return - Array of consumer records if executes successfully, error otherwise.
    public remote function poll(int timeoutValue) returns ConsumerRecord[]|error = external;

    # Resume consumer retrieving messages from set of partitions which were paused earlier.
    #
    # + partitions - Set of partitions to resume the retrieval of messages.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function resume(TopicPartition[] partitions) returns error? = external;

    # Seek the consumer for a given offset in a topic partition.
    #
    # + offset - PartitionOffset to seek.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function seek(PartitionOffset offset) returns error? = external;

    # Seek consumer to the beginning of the offsets for the given set of topic partitions.
    #
    # + partitions - Set of topic partitions to seek.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function seekToBeginning(TopicPartition[] partitions) returns error? = external;

    # Seek consumer for end of the offsets for the given set of topic partitions.
    #
    # + partitions - Set of topic partitions to seek.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function seekToEnd(TopicPartition[] partitions) returns error? = external;

    # Subscribes the consumer to the provided set of topics.
    #
    # + topics - Array of topics to be subscribed.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function subscribe(string[] topics) returns error? = external;

    # Subscribes the consumer to the topics which matches to the provided pattern.
    #
    # + regex - Pattern which should be matched with the topics to be subscribed.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function subscribeToPattern(string regex) returns error? = external;

    # Subscribes to consumer to the provided set of topics with rebalance listening is enabled.
    #
    # + topics - Array of topics to be subscribed.
    # + onPartitionsRevoked - Function which will be executed if partitions are revoked from this consumer.
    # + onPartitionsAssigned - Function which will be executed if partitions are assigned this consumer.
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function subscribeWithPartitionRebalance(string[] topics,
                           function(Consumer consumer, TopicPartition[] partitions) onPartitionsRevoked,
                           function(Consumer consumer, TopicPartition[] partitions) onPartitionsAssigned)
                           returns error? = external;

    # Unsubscribe the consumer from all the topic subscriptions.
    #
    # + return - Returns an error if encounters an error, returns nil otherwise.
    public remote function unsubscribe() returns error? = external;
};
