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

import ballerina/lang.'object;
import ballerina/java;

# Configurations related to consumer endpoint.
#
# + bootstrapServers - List of remote server endpoints of kafka brokers
# + groupId - Unique string that identifies the consumer
# + topics - Topics to be subscribed by the consumer
# + offsetReset - Offset reset strategy if no initial offset
# + partitionAssignmentStrategy - Strategy class for handling the partition assignment among consumers
# + metricsRecordingLevel - Metrics recording level
# + metricsReporterClasses - Metrics reporter classes
# + clientId - Identifier to be used for server side logging
# + interceptorClasses - Interceptor classes to be used before sending records
# + isolationLevel - Transactional message reading method
# + keyDeserializerType - Deserializer used for the Kafka record key. This should be a `kafka:DeserializerType`
# + valueDeserializerType - Deserializer used for the Kafka record value. This should be a `kafka:DeserializerType`
# + keyDeserializer - Custom deserializer object to deserialize kafka keys. This should be implement the
#                     `kafka:Deserializer` object
# + valueDeserializer - Custom deserializer object to deserialize kafka values. This should implement the
#                       `kafka:Deserializer` object
# + schemaRegistryUrl - Avro schema registry url. Use this field to specify schema registry url, if Avro serializer
#                       is used
# + properties - Additional properties for the property fields not provided by Ballerina Kafka module. Use this with
#                caution since this can override any of the fields. It is not recomendded to use this field except
#                in an extreme situation
# + sessionTimeoutInMillis - Timeout used to detect consumer failures when heartbeat threshold is reached
# + heartBeatIntervalInMillis - Expected time between heartbeats
# + metadataMaxAgeInMillis - Maximum time to force a refresh of metadata
# + autoCommitIntervalInMillis - Auto committing interval for commit offset, when auto-commit is enabled
# + maxPartitionFetchBytes - The maximum amount of data per-partition the server returns
# + sendBuffer - Size of the TCP send buffer (SO_SNDBUF)
# + receiveBuffer - Size of the TCP receive buffer (SO_RCVBUF)
# + fetchMinBytes - Minimum amount of data the server should return for a fetch request
# + fetchMaxBytes - Maximum amount of data the server should return for a fetch request
# + fetchMaxWaitTimeInMillis - Maximum amount of time the server will block before answering the fetch request
# + reconnectBackoffTimeMaxInMillis - Maximum amount of time in milliseconds to wait when reconnecting
# + retryBackoffInMillis - Time to wait before attempting to retry a failed request
# + metricsSampleWindowInMillis - Window of time a metrics sample is computed over
# + metricsNumSamples - Number of samples maintained to compute metrics
# + requestTimeoutInMillis - Wait time for response of a request
# + connectionMaxIdleTimeInMillis - Close idle connections after the number of milliseconds
# + maxPollRecords - Maximum number of records returned in a single call to poll
# + maxPollInterval - Maximum delay between invocations of poll
# + reconnectBackoffTimeInMillis - Time to wait before attempting to reconnect
# + pollingTimeoutInMillis - Timeout interval for polling
# + pollingIntervalInMillis - Polling interval for the consumer
# + concurrentConsumers - Number of concurrent consumers
# + defaultApiTimeoutInMillis - Default API timeout value for APIs with duration
# + autoCommit - Enables auto committing offsets
# + checkCRCS - Check the CRC32 of the records consumed. This ensures that no on-the-wire or on-disk corruption to
#               the messages occurred. This may add some overhead, and might needed set to `false` if extreme
#               performance is required
# + excludeInternalTopics - Whether records from internal topics should be exposed to the consumer
# + decoupleProcessing - Decouples processing
# + secureSocket - Configurations related to SSL/TLS encryption
# + authenticationConfiguration - Authentication-related configurations for the Kafka consumer
public type ConsumerConfiguration record {|
    string bootstrapServers;
    string groupId?;
    string[] topics?;
    string offsetReset?; // TODO: Introduce a type instead of `string`.
    string partitionAssignmentStrategy?;
    string metricsRecordingLevel?;
    string metricsReporterClasses?;
    string clientId?;
    string interceptorClasses?;
    IsolationLevel isolationLevel?;

    DeserializerType keyDeserializerType = DES_BYTE_ARRAY;
    DeserializerType valueDeserializerType = DES_BYTE_ARRAY;
    Deserializer keyDeserializer?;
    Deserializer valueDeserializer?;
    string schemaRegistryUrl?;

    map<string> properties?; // TODO: This should be renamed to additionalProperties in future releases.

    int sessionTimeoutInMillis?;
    int heartBeatIntervalInMillis?;
    int metadataMaxAgeInMillis?;
    int autoCommitIntervalInMillis?;
    int maxPartitionFetchBytes?;
    int sendBuffer?;
    int receiveBuffer?;
    int fetchMinBytes?;
    int fetchMaxBytes?;
    int fetchMaxWaitTimeInMillis?;
    int reconnectBackoffTimeMaxInMillis?;
    int retryBackoffInMillis?;
    int metricsSampleWindowInMillis?;
    int metricsNumSamples?;
    int requestTimeoutInMillis?;
    int connectionMaxIdleTimeInMillis?;
    int maxPollRecords?;
    int maxPollInterval?;
    int reconnectBackoffTimeInMillis?;
    int pollingTimeoutInMillis?;
    int pollingIntervalInMillis?;
    int concurrentConsumers?;
    int defaultApiTimeoutInMillis?;

    boolean autoCommit = true;
    boolean checkCRCS = true;
    boolean excludeInternalTopics = true;
    boolean decoupleProcessing = false;

    SecureSocket secureSocket?;
    AuthenticationConfiguration authenticationConfiguration?;
|};

# Type related to consumer record.
#
# + key - Key that is included in the record
# + value - Record content
# + offset - Offset value
# + partition - Partition in which the record is stored
# + timestamp - Timestamp of the record, in milliseconds since epoch
# + topic - Topic to which the record belongs to
public type ConsumerRecord record {|
    anydata key;
    anydata value;
    int offset;
    int partition;
    int timestamp;
    string topic;
|};

# Represents a generic Avro record. This is the type of the value returned from an Avro deserializer consumer.
public type AvroGenericRecord record {
    // Left blank intentionally.
};

# Kafka in-built deserializer type.
public type DeserializerType DES_BYTE_ARRAY|DES_STRING|DES_INT|DES_FLOAT|DES_AVRO|DES_CUSTOM;

# Kafka consumer isolation level type.
public type IsolationLevel ISOLATION_COMMITTED|ISOLATION_UNCOMMITTED;

# Represents a Kafka consumer endpoint.
#
# + consumerConfig - Used to store configurations related to a Kafka connection
public type Consumer client object {
    *'object:Listener;

    public ConsumerConfiguration consumerConfig;
    private string keyDeserializerType;
    private string valueDeserializerType;
    private Deserializer? keyDeserializer = ();
    private Deserializer? valueDeserializer = ();

    # Creates a new Kafka `Consumer`.
    #
    # + config - Configurations related to consumer endpoint
    public function init (ConsumerConfiguration config) {
        self.consumerConfig = config;
        self.keyDeserializerType = config.keyDeserializerType;
        self.valueDeserializerType = config.valueDeserializerType;

        if (self.keyDeserializerType == DES_CUSTOM) {
            var keyDeserializerObject = config?.keyDeserializer;
            if (keyDeserializerObject is ()) {
                panic error(CONSUMER_ERROR, message = "Invalid keyDeserializer config: Please Provide a " +
                                        "valid custom deserializer for the keyDeserializer");
            } else {
                self.keyDeserializer = keyDeserializerObject;
            }
        }
        if (self.keyDeserializerType == DES_AVRO) {
            var schemaRegistryUrl = config?.schemaRegistryUrl;
            if (schemaRegistryUrl is ()) {
                panic error(PRODUCER_ERROR, message = "Missing schema registry URL for the Avro serializer. Please " +
                            "provide 'schemaRegistryUrl' configuration in 'kafka:ProducerConfiguration'.");
            }
        }

        if (self.valueDeserializerType == DES_CUSTOM) {
            var valueDeserializerObject = config?.valueDeserializer;
            if (valueDeserializerObject is ()) {
                panic error(CONSUMER_ERROR, message = "Invalid valueDeserializer config: Please Provide a" +
                                        " valid custom deserializer for the valueDeserializer");
            } else {
                self.valueDeserializer = valueDeserializerObject;
            }
        }
        if (self.valueDeserializerType == DES_AVRO) {
            var schemaRegistryUrl = config?.schemaRegistryUrl;
            if (schemaRegistryUrl is ()) {
                panic error(CONSUMER_ERROR, message = "Missing schema registry URL for the Avro deserializer. Please " +
                            "provide 'schemaRegistryUrl' configuration in 'kafka:ConsumerConfiguration'.");
            }
        }
        checkpanic self->connect();

        string[]? topics = config?.topics;
        if (topics is string[]){
            checkpanic self->subscribe(topics);
        }
    }

    # Starts the registered services.
    #
    # + return - An `kafka:ConsumerError` if an error is encountered while starting the server or else nil
    public function __start() returns error? {
        return start(self);
    }

    # Stops the kafka listener.
    #
    # + return - An `kafka:ConsumerError` if an error is encountered during the listener stopping process or else nil
    public function __gracefulStop() returns error? {
        return stop(self);
    }

    # Stops the kafka listener.
    #
    # + return - An `kafka:ConsumerError` if an error is encountered during the listener stopping process or else nil
    public function __immediateStop() returns error? {
        return stop(self);
    }

    # Gets called every time a service attaches itself to the listener.
    #
    # + s - The service to be attached
    # + name - Name of the service
    # + return - An `kafka:ConsumerError` if an error is encountered while attaching the service or else nil
    public function __attach(service s, string? name = ()) returns error? {
        return register(self, s, name);
    }

    # Detaches a consumer service from the listener.
    #
    # + s - The service to be detached
    # + return - An `kafka:ConsumerError` if an error is encountered while detaching a service or else nil
    public function __detach(service s) returns error? {
    }

    # Assigns consumer to a set of topic partitions.
    #
    # + partitions - Topic partitions to be assigned
    # + return - `kafka:ConsumerError` if an error is encountered or else nil
    public remote function assign(TopicPartition[] partitions) returns ConsumerError? {
        return consumerAssign(self, partitions);
    }

    # Closes the consumer connection of the external Kafka broker.
    # ```ballerina
    # kafka:ConsumerError? result = consumer->close();
    # ```
    #
    # + duration - Timeout duration for the close operation execution
    # + return - A `kafka:ConsumerError` if an error is encountered or else '()'
    public remote function close(public int duration = -1) returns ConsumerError? {
        return consumerClose(self, duration);
    }

    # Commits the current consumed offsets for the consumer.
    # ```ballerina
    # kafka:ConsumerError? result = consumer->commit();
    # ```
    #
    # + return - A `kafka:ConsumerError` if an error is encountered or else '()'
    public remote function commit() returns ConsumerError? {
        return consumerCommit(self);
    }

    # Commits given offsets and partitions for the given topics, for consumer.
    #
    # + duration - Timeout duration for the commit operation execution
    # + offsets - Offsets to be commited
    # + return - `kafka:ConsumerError` if an error is encountered or else nil
    public remote function commitOffset(PartitionOffset[] offsets, public int duration = -1) returns ConsumerError? {
        return consumerCommitOffset(self, offsets, duration);
    }

    # Connects the consumer to the provided host in the consumer configs.
    # ```ballerina
    # kafka:ConsumerError? result = consumer->connect();
    # ```
    #
    # + return - A `kafka:ConsumerError` if an error is encountered or else '()'
    public remote function connect() returns ConsumerError? {
        return consumerConnect(self);
    }

    # Retrieves the currently-assigned partitions for the consumer.
    # ```ballerina
    # kafka:TopicPartition[]|kafka:ConsumerError result = consumer->getAssignment();
    # ```
    #
    # + return - Array of assigned partitions for the consumer if executes successfully or else a `kafka:ConsumerError`
    public remote function getAssignment() returns TopicPartition[]|ConsumerError {
        return consumerGetAssignment(self);
    }

    # Retrieves the available list of topics for a particular consumer.
    # ```ballerina
    # string[]|kafka:ConsumerError result = consumer->getAvailableTopics();
    # ```
    #
    # + duration - Timeout duration for the execution of the `get available topics` operation
    # + return - Array of topics currently available (authorized) for the consumer to subscribe or else
    #           a `kafka:ConsumerError`
    public remote function getAvailableTopics(public int duration = -1) returns string[]|ConsumerError {
        return consumerGetAvailableTopics(self, duration);
    }

    # Retrieves the start offsets for given set of partitions.
    #
    # + partitions - Array of topic partitions to get the starting offsets
    # + duration - Timeout duration for the get beginning offsets execution
    # + return - Starting offsets for the given partitions if executes successfully or else `kafka:ConsumerError`
    public remote function getBeginningOffsets(TopicPartition[] partitions, public int duration = -1)
    returns PartitionOffset[]|ConsumerError {
        return consumerGetBeginningOffsets(self, partitions, duration);
    }

    # Retrieves the last committed offsets for the given topic partitions.
    #
    # + partition - The `TopicPartition` in which the committed offset is returned for consumer
    # + duration - Timeout duration for the get committed offset operation to execute
    # + return - Committed offset for the consumer for the given partition if executes successfully or else
    #            `kafka:ConsumerError`
    public remote function getCommittedOffset(TopicPartition partition, public int duration = -1)
    returns PartitionOffset|ConsumerError {
        return consumerGetCommittedOffset(self, partition, duration);
    }

    # Retrieves the last offsets for given set of partitions.
    #
    # + partitions - Set of partitions to get the last offsets
    # + duration - Timeout duration for the get end offsets operation to execute
    # + return - End offsets for the given partitions if executes successfully or else `kafka:ConsumerError`
    public remote function getEndOffsets(TopicPartition[] partitions, public int duration = -1)
    returns PartitionOffset[]|ConsumerError {
        return consumerGetEndOffsets(self, partitions, duration);
    }

    # Retrieves the partitions, which are currently paused.
    # ```ballerina
    # kafka:TopicPartition[]|kafka:ConsumerError result = consumer->getPausedPartitions();
    # ```
    #
    # + return - Set of partitions paused from message retrieval if executes successfully or else
    #            a `kafka:ConsumerError`
    public remote function getPausedPartitions() returns TopicPartition[]|ConsumerError {
        return consumerGetPausedPartitions(self);
    }

    # Retrieves the offset of the next record that will be fetched, if a records exists in that position.
    #
    # + partition - The `TopicPartition` in which the position is required
    # + duration - Timeout duration for the get position offset operation to execute
    # + return - Offset which will be fetched next (if a records exists in that offset) or else `kafka:ConsumerError` if
    #            the operation fails
    public remote function getPositionOffset(TopicPartition partition, public int duration = -1)
    returns int|ConsumerError {
        return consumerGetPositionOffset(self, partition, duration);
    }

    # Retrieves the set of topics, which are currently subscribed by the consumer.
    # ```ballerina
    # string[]|kafka:ConsumerError result = consumer->getSubscription();
    # ```
    #
    # + return - Array of subscribed topics for the consumer if executes successfully or else a `kafka:ConsumerError`
    public remote function getSubscription() returns string[]|ConsumerError {
        return consumerGetSubscription(self);
    }

    # Retrieves the set of partitions to which the topic belongs.
    # ```ballerina
    # kafka:TopicPartition[]|kafka:ConsumerError result = consumer->getTopicPartitions("kafka-topic");
    # ```
    #
    # + topic - The topic for which the partition information is needed
    # + duration - Timeout duration for the `get topic partitions` operation to execute
    # + return - Array of partitions for the given topic if executes successfully or else a `kafka:ConsumerError`
    public remote function getTopicPartitions(string topic, public int duration = -1)
    returns TopicPartition[]|ConsumerError {
        return consumerGetTopicPartitions(self, topic, duration);
    }

    # Pauses retrieving messages from a set of partitions.
    #
    # + partitions - Partitions to pause the retrieval of messages
    # + return - `kafka:ConsumerError` if an error is encountered or else nil
    public remote function pause(TopicPartition[] partitions) returns ConsumerError? {
        return consumerPause(self, partitions);
    }

    # Polls the consumer for the records of an external broker.
    # ```ballerina
    # kafka:ConsumerRecord[]|kafka:ConsumerError result = consumer->poll(1000);
    # ```
    #
    # + timeoutValue - Polling time in milliseconds
    # + return - Array of consumer records if executed successfully or else a `kafka:ConsumerError`
    public remote function poll(int timeoutValue) returns ConsumerRecord[]|ConsumerError {
        return consumerPoll(self, timeoutValue);
    }

    # Resumes consumer retrieving messages from set of partitions which were paused earlier.
    #
    # + partitions - Partitions to resume the retrieval of messages
    # + return - `kafka:ConsumerError` if an error is encountered or else ()
    public remote function resume(TopicPartition[] partitions) returns ConsumerError? {
        return consumerResume(self, partitions);
    }

    # Seeks for a given offset in a topic partition.
    #
    # + offset - The `PartitionOffset` to seek
    # + return - `kafka:ConsumerError` if an error is encountered or else ()
    public remote function seek(PartitionOffset offset) returns ConsumerError? {
        return consumerSeek(self, offset);
    }

    # Seeks the beginning of the offsets for the given set of topic partitions.
    #
    # + partitions - The set of topic partitions to seek
    # + return - `kafka:ConsumerError` if an error is encountered or else ()
    public remote function seekToBeginning(TopicPartition[] partitions) returns ConsumerError? {
        return consumerSeekToBeginning(self, partitions);
    }

    # Seeks end of the offsets for the given set of topic partitions.
    #
    # + partitions - The set of topic partitions to seek
    # + return - `kafka:ConsumerError` if an error is encountered or else ()
    public remote function seekToEnd(TopicPartition[] partitions) returns ConsumerError? {
        return consumerSeekToEnd(self, partitions);
    }

    # Subscribes the consumer to the provided set of topics.
    # ```ballerina
    # kafka:ConsumerError? result = consumer->subscribe(["kafka-topic-1", "kafka-topic-2"]);
    # ```
    #
    # + topics - Array of topics to be subscribed to
    # + return - A `kafka:ConsumerError` if an error is encountered or else '()'
    public remote function subscribe(string[] topics) returns ConsumerError? {
        if (self.consumerConfig?.groupId is string) {
            return consumerSubscribe(self, topics);
        } else {
            panic error(CONSUMER_ERROR, message = "The groupId of the consumer must be set to subscribe to the topics");
        }
    }

    # Subscribes the consumer to the topics, which match the provided pattern.
    # ```ballerina
    # kafka:ConsumerError? result = consumer->subscribeToPattern("kafka.*");
    # ```
    #
    # + regex - Pattern, which should be matched with the topics to be subscribed to
    # + return - A `kafka:ConsumerError` if an error is encountered or else '()'
    public remote function subscribeToPattern(string regex) returns ConsumerError? {
        return consumerSubscribeToPattern(self, regex);
    }

    # Subscribes to the provided set of topics with rebalance listening enabled.
    # This function can be used inside a service, to subscribe to a set of topics, while rebalancing the patition
    # assignment of the consumers.
    #
    # + topics - Array of topics to be subscribed to
    # + onPartitionsRevoked - Function which will be executed if partitions are revoked from this consumer
    # + onPartitionsAssigned - Function which will be executed if partitions are assigned this consumer
    # + return - `kafka:ConsumerError` if an error is encountered or else ()
    public remote function subscribeWithPartitionRebalance(string[] topics,
        function(Consumer consumer, TopicPartition[] partitions) onPartitionsRevoked,
        function(Consumer consumer, TopicPartition[] partitions) onPartitionsAssigned)
    returns ConsumerError? {
        return consumerSubscribeWithPartitionRebalance(self, topics, onPartitionsRevoked, onPartitionsAssigned);
    }

    # Unsubscribes from all the topic subscriptions.
    # ```ballerina
    # kafka:ConsumerError? result = consumer->unsubscribe();
    # ```
    #
    # + return - A `kafka:ConsumerError` if an error is encountered or else '()'
    public remote function unsubscribe() returns ConsumerError? {
        return consumerUnsubscribe(self);
    }
};

function consumerClose(Consumer consumer, int duration) returns ConsumerError? =
@java:Method {
    name: "close",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.BrokerConnection"
} external;

function consumerConnect(Consumer consumer) returns ConsumerError? =
@java:Method {
    name: "connect",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.BrokerConnection"
} external;

function consumerPause(Consumer consumer, TopicPartition[] partitions) returns ConsumerError? =
@java:Method {
    name: "pause",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.BrokerConnection"
} external;

function consumerResume(Consumer consumer, TopicPartition[] partitions) returns ConsumerError? =
@java:Method {
    name: "resume",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.BrokerConnection"
} external;

function consumerCommit(Consumer consumer) returns ConsumerError? =
@java:Method {
    name: "commit",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.Commit"
} external;

function consumerCommitOffset(Consumer consumer, PartitionOffset[] offsets, public int duration = -1)
returns ConsumerError? =
@java:Method {
    name: "commitOffset",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.Commit"
} external;

function consumerAssign(Consumer consumer, TopicPartition[] partitions) returns ConsumerError? =
@java:Method {
    name: "assign",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.ConsumerInformationHandler"
} external;

function consumerGetAssignment(Consumer consumer) returns TopicPartition[]|ConsumerError =
@java:Method {
    name: "getAssignment",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.ConsumerInformationHandler"
} external;

function consumerGetAvailableTopics(Consumer consumer, int duration) returns string[]|ConsumerError =
@java:Method {
    name: "getAvailableTopics",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.ConsumerInformationHandler"
} external;

function consumerGetPausedPartitions(Consumer consumer) returns TopicPartition[]|ConsumerError =
@java:Method {
    name: "getPausedPartitions",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.ConsumerInformationHandler"
} external;

function consumerGetTopicPartitions(Consumer consumer, string topic, public int duration = -1)
returns TopicPartition[]|ConsumerError =
@java:Method {
    name: "getTopicPartitions",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.ConsumerInformationHandler"
} external;

function consumerGetSubscription(Consumer consumer) returns string[]|ConsumerError =
@java:Method {
    name: "getSubscription",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.ConsumerInformationHandler"
} external;

function consumerGetBeginningOffsets(Consumer consumer, TopicPartition[] partitions, int duration)
returns PartitionOffset[]|ConsumerError =
@java:Method {
    name: "getBeginningOffsets",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.GetOffsets"
} external;

function consumerGetCommittedOffset(Consumer consumer, TopicPartition partition, int duration)
returns PartitionOffset|ConsumerError =
@java:Method {
    name: "getCommittedOffset",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.GetOffsets"
} external;

function consumerGetEndOffsets(Consumer consumer, TopicPartition[] partitions, int duration)
returns PartitionOffset[]|ConsumerError =
@java:Method {
    name: "getEndOffsets",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.GetOffsets"
} external;

function consumerGetPositionOffset(Consumer consumer, TopicPartition partition, public int duration = -1)
returns int|ConsumerError =
@java:Method {
    name: "getPositionOffset",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.GetOffsets"
} external;

function consumerPoll(Consumer consumer, int timeoutValue) returns ConsumerRecord[]|ConsumerError =
@java:Method {
    name: "poll",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.Poll"
} external;

function consumerSeek(Consumer consumer, PartitionOffset offset) returns ConsumerError? =
@java:Method {
    name: "seek",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.Seek"
} external;

function consumerSeekToBeginning(Consumer consumer, TopicPartition[] partitions) returns ConsumerError? =
@java:Method {
    name: "seekToBeginning",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.Seek"
} external;

function consumerSeekToEnd(Consumer consumer, TopicPartition[] partitions) returns ConsumerError? =
@java:Method {
    name: "seekToEnd",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.Seek"
} external;

function consumerSubscribe(Consumer consumer, string[] topics) returns ConsumerError? =
@java:Method {
    name: "subscribe",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.SubscriptionHandler"
} external;

function consumerSubscribeToPattern(Consumer consumer, string regex) returns ConsumerError? =
@java:Method {
    name: "subscribeToPattern",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.SubscriptionHandler"
} external;

function consumerSubscribeWithPartitionRebalance(Consumer consumer, string[] topics,
    function(Consumer consumer, TopicPartition[] partitions) onPartitionsRevoked,
    function(Consumer consumer, TopicPartition[] partitions) onPartitionsAssigned)
returns ConsumerError? =
@java:Method {
    name: "subscribeWithPartitionRebalance",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.SubscriptionHandler"
} external;

function consumerUnsubscribe(Consumer consumer) returns ConsumerError? =
@java:Method {
    name: "unsubscribe",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.consumer.SubscriptionHandler"
} external;

function register(Consumer consumer, service serviceType, string? name) returns ConsumerError? =
@java:Method {
    class: "org.ballerinalang.messaging.kafka.service.Register"
} external;

function start(Consumer consumer) returns ConsumerError? =
@java:Method {
    class: "org.ballerinalang.messaging.kafka.service.Start"
} external;

function stop(Consumer consumer) returns ConsumerError? =
@java:Method {
    class: "org.ballerinalang.messaging.kafka.service.Stop"
} external;
