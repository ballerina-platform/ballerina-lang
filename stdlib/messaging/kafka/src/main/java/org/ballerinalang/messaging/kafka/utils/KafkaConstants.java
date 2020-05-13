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

package org.ballerinalang.messaging.kafka.utils;

import org.ballerinalang.jvm.types.BPackage;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * Constants related to for Kafka API.
 */
public class KafkaConstants {

    private KafkaConstants() {
    }

    public static final int DURATION_UNDEFINED_VALUE = -1;

    public static final String BLOCK_SEPARATOR = ":";
    public static final String ARRAY_INDICATOR = "[]";
    public static final String KAFKA_PACKAGE_NAME = "kafka";
    private static final String VERSION = "2.0.0";

    public static final String FULL_PACKAGE_NAME = KAFKA_PACKAGE_NAME + BLOCK_SEPARATOR + VERSION;
    public static final String KAFKA_PROTOCOL_PACKAGE_FQN = BALLERINA_PACKAGE_PREFIX + FULL_PACKAGE_NAME;
    public static final BPackage KAFKA_PROTOCOL_PACKAGE_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                          KAFKA_PACKAGE_NAME, VERSION);

    // Kafka log messages
    public static final String SERVICE_STARTED = "[ballerina/kafka] started kafka listener ";
    public static final String SERVICE_STOPPED = "[ballerina/kafka] stopped kafka listener ";
    public static final String KAFKA_SERVERS = "[ballerina/kafka] kafka servers: ";
    public static final String SUBSCRIBED_TOPICS = "[ballerina/kafka] subscribed topics: ";

    public static final String NATIVE_CONSUMER = "KafkaConsumer";
    public static final String NATIVE_PRODUCER = "KafkaProducer";
    public static final String NATIVE_CONSUMER_CONFIG = "KafkaConsumerConfig";
    public static final String NATIVE_PRODUCER_CONFIG = "KafkaProducerConfig";
    public static final String CONNECTOR_ID = "connectorId";

    public static final String TRANSACTION_CONTEXT = "TransactionInitiated";

    public static final String TOPIC_PARTITION_STRUCT_NAME = "TopicPartition";
    public static final String AVRO_DATA_RECORD_NAME = "dataRecord";
    public static final String AVRO_SCHEMA_STRING_NAME = "schemaString";
    public static final String OFFSET_STRUCT_NAME = "PartitionOffset";

    public static final String CONSUMER_ERROR = "{ballerina/kafka}ConsumerError";
    public static final String PRODUCER_ERROR = "{ballerina/kafka}ProducerError";
    public static final String AVRO_ERROR = "{ballerina/kafka}AvroError";
    public static final String DETAIL_RECORD_NAME = "Detail";

    public static final String AVRO_GENERIC_RECORD_NAME = "AvroGenericRecord";
    public static final String CONSUMER_RECORD_STRUCT_NAME = "ConsumerRecord";
    public static final String CONSUMER_STRUCT_NAME = "Consumer";
    public static final String SERVER_CONNECTOR = "serverConnector";

    public static final String CONSUMER_CONFIG_FIELD_NAME = "consumerConfig";
    public static final String PRODUCER_CONFIG_FIELD_NAME = "producerConfig";

    public static final String PARAMETER_CONSUMER_NAME =
            KAFKA_PROTOCOL_PACKAGE_FQN + BLOCK_SEPARATOR + CONSUMER_STRUCT_NAME;
    public static final String PARAMETER_RECORD_ARRAY_NAME = KAFKA_PROTOCOL_PACKAGE_FQN + BLOCK_SEPARATOR
            + CONSUMER_RECORD_STRUCT_NAME + ARRAY_INDICATOR;
    public static final String PARAMETER_PARTITION_OFFSET_ARRAY_NAME = KAFKA_PROTOCOL_PACKAGE_FQN + BLOCK_SEPARATOR
            + OFFSET_STRUCT_NAME + ARRAY_INDICATOR;

    public static final String KAFKA_RESOURCE_ON_MESSAGE = "onMessage";

    public static final String ADDITIONAL_PROPERTIES_MAP_FIELD = "properties";

    public static final String ALIAS_CONCURRENT_CONSUMERS = "concurrentConsumers";
    public static final String ALIAS_TOPICS = "topics";
    public static final String ALIAS_POLLING_TIMEOUT = "pollingTimeoutInMillis";
    public static final String ALIAS_POLLING_INTERVAL = "pollingIntervalInMillis";
    public static final String ALIAS_DECOUPLE_PROCESSING = "decoupleProcessing";
    public static final String ALIAS_TOPIC = "topic";
    public static final String ALIAS_PARTITION = "partition";
    public static final String ALIAS_OFFSET = "offset";
    public static final String ALIAS_DURATION = "duration";
    public static final String ALIAS_VALUE = "value";

    // Consumer Configuration.
    public static final String CONSUMER_BOOTSTRAP_SERVERS_CONFIG = "bootstrapServers";
    public static final String CONSUMER_GROUP_ID_CONFIG = "groupId";
    public static final String CONSUMER_AUTO_OFFSET_RESET_CONFIG = "offsetReset";
    public static final String CONSUMER_PARTITION_ASSIGNMENT_STRATEGY_CONFIG = "partitionAssignmentStrategy";
    public static final String CONSUMER_METRICS_RECORDING_LEVEL_CONFIG = "metricsRecordingLevel";
    public static final String CONSUMER_METRIC_REPORTER_CLASSES_CONFIG = "metricsReporterClasses";
    public static final String CONSUMER_CLIENT_ID_CONFIG = "clientId";
    public static final String CONSUMER_INTERCEPTOR_CLASSES_CONFIG = "interceptorClasses";
    public static final String CONSUMER_ISOLATION_LEVEL_CONFIG = "isolationLevel";
    public static final String CONSUMER_KEY_DESERIALIZER_TYPE_CONFIG = "keyDeserializerType";
    public static final String CONSUMER_VALUE_DESERIALIZER_TYPE_CONFIG = "valueDeserializerType";
    public static final String CONSUMER_KEY_DESERIALIZER_CONFIG = "keyDeserializer";
    public static final String CONSUMER_VALUE_DESERIALIZER_CONFIG = "valueDeserializer";
    public static final String BALLERINA_STRAND = "ballerina.strand";
    public static final String CONSUMER_SCHEMA_REGISTRY_URL = "schemaRegistryUrl";

    public static final String CONSUMER_SESSION_TIMEOUT_MS_CONFIG = "sessionTimeoutInMillis";
    public static final String CONSUMER_HEARTBEAT_INTERVAL_MS_CONFIG = "heartBeatIntervalInMillis";
    public static final String CONSUMER_METADATA_MAX_AGE_CONFIG = "metadataMaxAgeInMillis";
    public static final String CONSUMER_AUTO_COMMIT_INTERVAL_MS_CONFIG = "autoCommitIntervalInMillis";
    public static final String CONSUMER_MAX_PARTITION_FETCH_BYTES_CONFIG = "maxPartitionFetchBytes";
    public static final String CONSUMER_SEND_BUFFER_CONFIG = "sendBuffer";
    public static final String CONSUMER_RECEIVE_BUFFER_CONFIG = "receiveBuffer";
    public static final String CONSUMER_FETCH_MIN_BYTES_CONFIG = "fetchMinBytes";
    public static final String CONSUMER_FETCH_MAX_BYTES_CONFIG = "fetchMaxBytes";
    public static final String CONSUMER_FETCH_MAX_WAIT_MS_CONFIG = "fetchMaxWaitTimeInMillis";
    public static final String CONSUMER_RECONNECT_BACKOFF_MS_CONFIG = "reconnectBackoffTimeInMillis";
    public static final String CONSUMER_RETRY_BACKOFF_MS_CONFIG = "retryBackoffInMillis";
    public static final String CONSUMER_METRICS_SAMPLE_WINDOW_MS_CONFIG = "metricsSampleWindowInMillis";
    public static final String CONSUMER_METRICS_NUM_SAMPLES_CONFIG = "metricsNumSamples";
    public static final String CONSUMER_REQUEST_TIMEOUT_MS_CONFIG = "requestTimeoutInMillis";
    public static final String CONSUMER_CONNECTIONS_MAX_IDLE_MS_CONFIG = "connectionMaxIdleTimeInMillis";
    public static final String CONSUMER_MAX_POLL_RECORDS_CONFIG = "maxPollRecords";
    public static final String CONSUMER_MAX_POLL_INTERVAL_MS_CONFIG = "maxPollInterval";
    public static final String CONSUMER_RECONNECT_BACKOFF_MAX_MS_CONFIG = "reconnectBackoffTimeMaxInMillis";
    public static final String CONSUMER_ENABLE_AUTO_COMMIT_CONFIG = "autoCommit";
    public static final String CONSUMER_CHECK_CRCS_CONFIG = "checkCRCS";
    public static final String CONSUMER_EXCLUDE_INTERNAL_TOPICS_CONFIG = "excludeInternalTopics";
    public static final String CONSUMER_DEFAULT_API_TIMEOUT_CONFIG = "defaultApiTimeoutInMillis";

    // Producer Configuration.
    public static final String PRODUCER_BOOTSTRAP_SERVERS_CONFIG = "bootstrapServers";
    public static final String PRODUCER_ACKS_CONFIG = "acks";
    public static final String PRODUCER_COMPRESSION_TYPE_CONFIG = "compressionType";
    public static final String PRODUCER_CLIENT_ID_CONFIG = "clientId";
    public static final String PRODUCER_METRICS_RECORDING_LEVEL_CONFIG = "metricsRecordingLevel";
    public static final String PRODUCER_METRIC_REPORTER_CLASSES_CONFIG = "metricReporterClasses";
    public static final String PRODUCER_PARTITIONER_CLASS_CONFIG = "partitionerClass";
    public static final String PRODUCER_INTERCEPTOR_CLASSES_CONFIG = "interceptorClasses";
    public static final String PRODUCER_TRANSACTIONAL_ID_CONFIG = "transactionalId";
    public static final String PRODUCER_KEY_SERIALIZER_TYPE_CONFIG = "keySerializerType";
    public static final String PRODUCER_VALUE_SERIALIZER_TYPE_CONFIG = "valueSerializerType";
    public static final String PRODUCER_KEY_SERIALIZER_CONFIG = "keySerializer";
    public static final String PRODUCER_VALUE_SERIALIZER_CONFIG = "valueSerializer";
    public static final String PRODUCER_SCHEMA_REGISTRY_URL = "schemaRegistryUrl";
    public static final String PRODUCER_BUFFER_MEMORY_CONFIG = "bufferMemory";
    public static final String PRODUCER_RETRIES_CONFIG = "retryCount";
    public static final String PRODUCER_BATCH_SIZE_CONFIG = "batchSize";
    public static final String PRODUCER_LINGER_MS_CONFIG = "linger";
    public static final String PRODUCER_SEND_BUFFER_CONFIG = "sendBuffer";
    public static final String PRODUCER_RECEIVE_BUFFER_CONFIG = "receiveBuffer";
    public static final String PRODUCER_MAX_REQUEST_SIZE_CONFIG = "maxRequestSize";
    public static final String PRODUCER_RECONNECT_BACKOFF_MS_CONFIG = "reconnectBackoffTimeInMillis";
    public static final String PRODUCER_RECONNECT_BACKOFF_MAX_MS_CONFIG = "reconnectBackoffMaxTimeInMillis";
    public static final String PRODUCER_RETRY_BACKOFF_MS_CONFIG = "retryBackoffTimeInMillis";
    public static final String PRODUCER_MAX_BLOCK_MS_CONFIG = "maxBlock";
    public static final String PRODUCER_REQUEST_TIMEOUT_MS_CONFIG = "requestTimeoutInMillis";
    public static final String PRODUCER_METADATA_MAX_AGE_CONFIG = "metadataMaxAgeInMillis";
    public static final String PRODUCER_METRICS_SAMPLE_WINDOW_MS_CONFIG = "metricsSampleWindowInMillis";
    public static final String PRODUCER_METRICS_NUM_SAMPLES_CONFIG = "metricsNumSamples";
    public static final String PRODUCER_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = "maxInFlightRequestsPerConnection";
    public static final String PRODUCER_CONNECTIONS_MAX_IDLE_MS_CONFIG = "connectionsMaxIdleTimeInMillis";
    public static final String PRODUCER_TRANSACTION_TIMEOUT_CONFIG = "transactionTimeoutInMillis";
    public static final String PRODUCER_ENABLE_IDEMPOTENCE_CONFIG = "enableIdempotence";

    // SSL Configuration parameters.
    public static final String SECURE_SOCKET = "secureSocket";
    public static final String KEYSTORE_CONFIG = "keyStore";
    public static final String TRUSTSTORE_CONFIG = "trustStore";
    public static final String PROTOCOL_CONFIG = "protocol";
    public static final String LOCATION_CONFIG = "location";
    public static final String PASSWORD_CONFIG = "password";
    public static final String KEYSTORE_TYPE_CONFIG = "keyStoreType";
    public static final String KEYMANAGER_ALGORITHM_CONFIG = "keyManagerAlgorithm";
    public static final String TRUSTSTORE_TYPE_CONFIG = "trustStoreType";
    public static final String TRUSTMANAGER_ALGORITHM_CONFIG = "trustManagerAlgorithm";
    public static final String ENABLED_PROTOCOLS_CONFIG = "sslProtocolVersions";
    public static final String SECURITY_PROTOCOL_CONFIG = "securityProtocol";
    public static final String SSL_PROTOCOL_CONFIG = "sslProtocol";
    public static final String SSL_PROVIDER_CONFIG = "sslProvider";
    public static final String SSL_KEY_PASSWORD_CONFIG = "sslKeyPassword";
    public static final String SSL_CIPHER_SUITES_CONFIG = "sslCipherSuites";
    public static final String SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG = "sslEndpointIdentificationAlgorithm";
    public static final String SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG = "sslSecureRandomImplementation";

    // SASL Configuration parameters
    public static final String AUTHENTICATION_CONFIGURATION = "authenticationConfiguration";
    public static final String AUTHENTICATION_MECHANISM = "mechanism";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    // Authentication Mechanisms
    public static final String SASL_PLAIN = "PLAIN";

    // Serializer - Deserializer names
    // Ballerina String Names
    public static final String SERDES_BYTE_ARRAY = "BYTE_ARRAY";
    public static final String SERDES_STRING = "STRING";
    public static final String SERDES_INT = "INT";
    public static final String SERDES_FLOAT = "FLOAT";
    public static final String SERDES_AVRO = "AVRO";
    public static final String SERDES_CUSTOM = "CUSTOM";

    // Default class names
    // Serializers
    public static final String BYTE_ARRAY_SERIALIZER = "org.apache.kafka.common.serialization.ByteArraySerializer";
    public static final String STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String INT_SERIALIZER = "org.apache.kafka.common.serialization.LongSerializer";
    public static final String FLOAT_SERIALIZER = "org.apache.kafka.common.serialization.DoubleSerializer";
    public static final String AVRO_SERIALIZER = "io.confluent.kafka.serializers.KafkaAvroSerializer";
    public static final String CUSTOM_SERIALIZER = "org.ballerinalang.messaging.kafka.serdes.BallerinaKafkaSerializer";

    // Deserializers
    public static final String BYTE_ARRAY_DESERIALIZER = "org.apache.kafka.common.serialization.ByteArrayDeserializer";
    public static final String STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String INT_DESERIALIZER = "org.apache.kafka.common.serialization.LongDeserializer";
    public static final String FLOAT_DESERIALIZER = "org.apache.kafka.common.serialization.DoubleDeserializer";
    public static final String AVRO_DESERIALIZER = "io.confluent.kafka.serializers.KafkaAvroDeserializer";
    public static final String CUSTOM_DESERIALIZER =
            "org.ballerinalang.messaging.kafka.serdes.BallerinaKafkaDeserializer";

    // Serializer / Deserializer function names
    public static final String FUNCTION_SERIALIZE = "serialize";
    public static final String FUNCTION_DESERIALIZE = "deserialize";
    public static final String FUNCTION_CLOSE = "close";

    // Warning suppression
    public static final String UNCHECKED = "unchecked";

    //Properties constants
    public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    public static final String CLIENT_ID = "client.id";
    public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    public static final String SPECIFIC_AVRO_READER = "specific.avro.reader";
}
