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
import ballerinax/java;

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
# + keySerializer - Serializer used for the Kafka record key. This can be either `kafka:SerializerType` or an
#       user-defined serializer.
# + valueSerializer - Serializer used for the Kafka record value. This can be either `kafka:SerializerType` or an
#       user-defined serializer.
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
    SerializerType valueSerializer = SER_BYTE_ARRAY;
    SerializerType keySerializer = SER_BYTE_ARRAY;

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

# Producer acknowledgement type 'all'. This will gurantee that the record will not be lost as long as at least one
# in-sync replica is alive.
public const ACKS_ALL = "all";

# Producer acknowledgement type '0'. If the acknowledgement type set to this, the producer will not wait for any
# acknowledgement from the server.
public const ACKS_NONE = "0";

# Producer acknowledgement type '1'. If the acknowledgement type set to this, the leader will write the record to its
# local log but will respond without awaiting full acknowledgement from all followers.
public const ACKS_SINGLE = "1";

# Kafka producer acknowledgement type.
public type Producer_Acks ACKS_ALL|ACKS_NONE|ACKS_SINGLE;

# In-built Kafka Byte Array serializer.
public const SER_BYTE_ARRAY = "BYTE_ARRAY";

# In-built Kafka string serializer.
public const SER_STRING = "STRING";

# In-built Kafka int serializer.
public const SER_INT = "INT";

# In-built Kafka float serializer.
public const SER_FLOAT = "FLOAT";

# Kafka in-built serializer type.
public type SerializerType SER_BYTE_ARRAY|SER_STRING|SER_INT|SER_FLOAT;

# Represent a Kafka producer endpoint.
#
# + connectorId - Unique ID for a particular connector.
# + producerConfig - Used to store configurations related to a Kafka connection.
public type Producer client object {

    public ProducerConfig? producerConfig = ();
    private string keySerializer = "";
    private string valueSerializer = "";

    # Creates a new Kafka `Producer`.
    #
    # + config - Configurations related to initializing a Kafka `Producer`.
    public function __init(ProducerConfig config) {
        self.producerConfig = config;
        self.keySerializer = config.keySerializer;
        self.valueSerializer = config.valueSerializer;
        var result = self.init(config);
        if (result is error) {
            panic result;
        }
    }

    # Initialize the producer endpoint. Panics if the initialization fails.
    #
    # + config - Configurations related to the endpoint.
    # + return - `kafka:ProducerError` if fails to initiate the `kafka:Producer`, nil otherwise.
    function init(ProducerConfig config) returns error? {
        return producerInit(self, config);
    }

    public string connectorId = system:uuid();

    # Closes producer connection to the external Kafka broker.
    #
    # + return - `kafka:ProducerError` if closing the producer failed, nil otherwise.
    public remote function close() returns ProducerError? {
        return producerClose(self);
    }

    # Commits consumer action which commits consumer consumed offsets to offset topic.
    #
    # + consumer - Consumer which needs offsets to be committed.
    # + return - `kafka:ProducerError` if committing the consumer failed, nil otherwise.
    public remote function commitConsumer(Consumer consumer) returns ProducerError? {
        return producerCommitConsumer(self, consumer);
    }

    # CommitConsumerOffsets action which commits consumer offsets in given transaction.
    #
    # + offsets - Consumer offsets to commit for given transaction.
    # + groupID - Consumer group id.
    # + return - `kafka:ProducerError` if committing consumer offsets failed, nil otherwise.
    public remote function commitConsumerOffsets(PartitionOffset[] offsets, string groupID) returns ProducerError? {
        return producerCommitConsumerOffsets(self, offsets, java:fromString(groupID));
    }

    # Flush action which flush batch of records.
    #
    # + return - `kafka:ProducerError` if records couldn't be flushed, nil otherwise.
    public remote function flushRecords() returns ProducerError? {
        return producerFlushRecords(self);
    }

    # GetTopicPartitions action which returns given topic partition information.
    #
    # + topic - Topic which the partition information is given.
    # + return - `kafka:TopicPartition` array for the given topic, returns `kafka:ProducerError` if operation fails.
    public remote function getTopicPartitions(string topic) returns TopicPartition[]|ProducerError {
        return producerGetTopicPartitions(self, java:fromString(topic));
    }

    # Simple Send action which produce records to Kafka server.
    #
    # + value - Record contents.
    # + topic - Topic to which the record will be appended to.
    # + key - Key that will be included in the record.
    # + partition - Partition to which the record should be sent.
    # + timestamp - Timestamp of the record, in milliseconds since epoch.
    # + return - Returns `kafka:ProducerError` if send action fails to send data, nil otherwise.
    public remote function send(Data value, string topic, public Data? key = (), public int? partition = (),
                                public int? timestamp = ()) returns ProducerError? {

        handle topicHandle = java:fromString(topic);

        // Handle string values
        if (self.valueSerializer == SER_STRING) {
            if (value is string) {
                handle valueHandle = java:fromString(value);

                if (key is ()) {
                    return producerSendString(self, valueHandle, topicHandle, partition, timestamp);
                }

                if (self.keySerializer == SER_STRING) {
                    if (key is string) {
                        handle keyHandle = java:fromString(key);
                        return producerSendStringString(self, valueHandle, topicHandle, keyHandle, partition,
                                                        timestamp);
                    }
                    panic getKeyTypeMismatchError("string");
                }

                if (self.keySerializer == SER_INT) {
                    if (key is int) {
                        return producerSendStringInt(self, valueHandle, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("int");
                }

                if (self.keySerializer == SER_FLOAT) {
                    if (key is float) {
                        return producerSendStringFloat(self, valueHandle, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("float");
                }

                if (self.keySerializer == SER_BYTE_ARRAY) {
                    if (key is byte[]) {
                        return producerSendStringByteArray(self, valueHandle, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("byte[]");
                }
            }

            panic getValueTypeMismatchError("string");
        }

        // Handle int values
        if (self.valueSerializer == SER_INT) {
            if (value is int) {
                if (key is ()) {
                    return producerSendInt(self, value, topicHandle, partition, timestamp);
                }

                if (self.keySerializer == SER_STRING) {
                    if (key is string) {
                        handle keyHandle = java:fromString(key);
                        return producerSendIntString(self, value, topicHandle, keyHandle, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("string");
                }

                if (self.keySerializer == SER_INT) {
                    if (key is int) {
                        return producerSendIntInt(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("int");
                }

                if (self.keySerializer == SER_FLOAT) {
                    if (key is float) {
                        return producerSendIntFloat(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("float");
                }

                if (self.keySerializer == SER_BYTE_ARRAY) {
                    if (key is byte[]) {
                        return producerSendIntByteArray(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("byte[]");
                }
            }

            panic getValueTypeMismatchError("int");
        }

        // Handle float values
        if (self.valueSerializer == SER_FLOAT) {
            if (value is float) {
                if (key is ()) {
                    return producerSendFloat(self, value, topicHandle, partition, timestamp);
                }

                if (self.keySerializer == SER_STRING) {
                    if (key is string) {
                        handle keyHandle = java:fromString(key);
                        return producerSendFloatString(self, value, topicHandle, keyHandle, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("string");
                }

                if (self.keySerializer == SER_INT) {
                    if (key is int) {
                        return producerSendFloatInt(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("int");
                }

                if (self.keySerializer == SER_FLOAT) {
                    if (key is float) {
                        return producerSendFloatFloat(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("float");
                }

                if (self.keySerializer == SER_BYTE_ARRAY) {
                    if (key is byte[]) {
                        return producerSendFloatByteArray(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("byte[]");
                }
            }

            panic getValueTypeMismatchError("float");
        }

        // Handle byte[] values
        if (self.valueSerializer == SER_BYTE_ARRAY) {
            if (value is byte[]) {
                if (key is ()) {
                    return producerSendByteArray(self, value, topicHandle, partition, timestamp);
                }

                if (self.keySerializer == SER_STRING) {
                    if (key is string) {
                        handle keyHandle = java:fromString(key);
                        return producerSendByteArrayString(self, value, topicHandle, keyHandle, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("string");
                }

                if (self.keySerializer == SER_INT) {
                    if (key is int) {
                        return producerSendByteArrayInt(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("int");
                }

                if (self.keySerializer == SER_FLOAT) {
                    if (key is float) {
                        return producerSendByteArrayFloat(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("float");
                }

                if (self.keySerializer == SER_BYTE_ARRAY) {
                    if (key is byte[]) {
                        return producerSendByteArrayByteArray(self, value, topicHandle, key, partition, timestamp);
                    }
                    panic getKeyTypeMismatchError("byte[]");
                }
            }

            panic getValueTypeMismatchError("byte[]");
        }

        panic createProducerError("Invalid value serializer configuration");
    }
};

function producerInit(Producer producer, ProducerConfig config) returns error? =
@java:Method {
    name: "init",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Init"
} external;

function producerClose(Producer producer) returns ProducerError? =
@java:Method {
    name: "close",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Close"
} external;

function producerCommitConsumer(Producer producer, Consumer consumer) returns ProducerError? =
@java:Method {
    name: "commitConsumer",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.CommitConsumer"
} external;

function producerCommitConsumerOffsets(Producer producer, PartitionOffset[] offsets, handle groupID)
returns ProducerError? =
@java:Method {
    name: "commitConsumerOffsets",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.CommitConsumerOffsets"
} external;

function producerFlushRecords(Producer producer) returns ProducerError? =
@java:Method {
    name: "flushRecords",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.FlushRecords"
} external;

function producerGetTopicPartitions(Producer producer, handle topic) returns TopicPartition[]|ProducerError =
@java:Method {
    name: "getTopicPartitions",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.GetTopicPartitions"
} external;

//////////////////////////////////////////////////////////////////////////////////////
//              Different send functions to send different types of data            //
//                  Naming convention: producerSend<ValueType><KeyType>             //
//   Reason for this naming convention is that the key can be nil but value cannot  //
//////////////////////////////////////////////////////////////////////////////////////

// Send string values with different types of keys
function producerSendString(Producer producer, handle value, handle topic, int? partition = (), int? timestamp = ())
                                returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "java.lang.String", "java.lang.String", "java.lang.Object",
                "java.lang.Object"]
} external;

function producerSendStringString(Producer producer, handle value, handle topic, handle key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "java.lang.String", "java.lang.String", "java.lang.String",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendStringInt(Producer producer, handle value, handle topic, int key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "java.lang.String", "java.lang.String", "long",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendStringFloat(Producer producer, handle value, handle topic, float key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "java.lang.String", "java.lang.String", "double",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendStringByteArray(Producer producer, handle value, handle topic, byte[] key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "java.lang.String", "java.lang.String",
                "org.ballerinalang.jvm.values.ArrayValue", "java.lang.Object", "java.lang.Object"]
} external;


// Send int values with different types of keys
function producerSendInt(Producer producer, int value, handle topic, int? partition = (), int? timestamp = ())
                                returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "long", "java.lang.String", "java.lang.Object",
                "java.lang.Object"]
} external;

function producerSendIntString(Producer producer, int value, handle topic, handle key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "long", "java.lang.String", "java.lang.String",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendIntInt(Producer producer, int value, handle topic, int key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "long", "java.lang.String", "long",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendIntFloat(Producer producer, int value, handle topic, float key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "long", "java.lang.String", "double",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendIntByteArray(Producer producer, int value, handle topic, byte[] key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "long", "java.lang.String",
                "org.ballerinalang.jvm.values.ArrayValue", "java.lang.Object", "java.lang.Object"]
} external;


// Send float values with different types of keys
function producerSendFloat(Producer producer, float value, handle topic, int? partition = (), int? timestamp = ())
                                returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "double", "java.lang.String", "java.lang.Object",
                "java.lang.Object"]
} external;

function producerSendFloatString(Producer producer, float value, handle topic, handle key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "double", "java.lang.String", "java.lang.String",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendFloatInt(Producer producer, float value, handle topic, int key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "double", "java.lang.String", "long",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendFloatFloat(Producer producer, float value, handle topic, float key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "double", "java.lang.String", "double",
                "java.lang.Object", "java.lang.Object"]
} external;

function producerSendFloatByteArray(Producer producer, float value, handle topic, byte[] key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "double", "java.lang.String",
                "org.ballerinalang.jvm.values.ArrayValue", "java.lang.Object", "java.lang.Object"]
} external;



// Send byte[] values with different types of keys
function producerSendByteArray(Producer producer, byte[] value, handle topic, int? partition = (), int? timestamp = ())
                                returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "org.ballerinalang.jvm.values.ArrayValue",
                "java.lang.String", "java.lang.Object", "java.lang.Object"]
} external;

function producerSendByteArrayString(Producer producer, byte[] value, handle topic, handle key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "org.ballerinalang.jvm.values.ArrayValue",
                "java.lang.String", "java.lang.String", "java.lang.Object", "java.lang.Object"]
} external;

function producerSendByteArrayInt(Producer producer, byte[] value, handle topic, int key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "org.ballerinalang.jvm.values.ArrayValue",
                "java.lang.String", "long", "java.lang.Object", "java.lang.Object"]
} external;

function producerSendByteArrayFloat(Producer producer, byte[] value, handle topic, float key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "org.ballerinalang.jvm.values.ArrayValue",
                "java.lang.String", "double", "java.lang.Object", "java.lang.Object"]
} external;

function producerSendByteArrayByteArray(Producer producer, byte[] value, handle topic, byte[] key, int? partition = (),
                                int? timestamp = ()) returns ProducerError? =
@java:Method {
    name: "send",
    class: "org.ballerinalang.messaging.kafka.nativeimpl.producer.Send",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "org.ballerinalang.jvm.values.ArrayValue",
                "java.lang.String", "org.ballerinalang.jvm.values.ArrayValue", "java.lang.Object", "java.lang.Object"]
} external;
