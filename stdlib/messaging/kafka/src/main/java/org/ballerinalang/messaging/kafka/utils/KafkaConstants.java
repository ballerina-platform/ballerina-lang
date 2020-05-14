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

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.api.BString;

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
    private static final String VERSION = "1.1.0";

    public static final String FULL_PACKAGE_NAME = KAFKA_PACKAGE_NAME + BLOCK_SEPARATOR + VERSION;
    public static final String KAFKA_PROTOCOL_PACKAGE = BALLERINA_PACKAGE_PREFIX + KAFKA_PACKAGE_NAME;
    public static final BPackage KAFKA_PROTOCOL_PACKAGE_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                          KAFKA_PACKAGE_NAME);

    // Kafka log messages
    public static final String SERVICE_STARTED = "[ballerina/kafka] started kafka listener ";
    public static final String SERVICE_STOPPED = "[ballerina/kafka] stopped kafka listener ";
    public static final String KAFKA_SERVERS = "[ballerina/kafka] kafka servers: ";
    public static final String SUBSCRIBED_TOPICS = "[ballerina/kafka] subscribed topics: ";

    public static final String NATIVE_CONSUMER = "KafkaConsumer";
    public static final String NATIVE_PRODUCER = "KafkaProducer";
    public static final String NATIVE_CONSUMER_CONFIG = "KafkaConsumerConfig";
    public static final String NATIVE_PRODUCER_CONFIG = "KafkaProducerConfig";
    public static final BString CONNECTOR_ID = StringUtils.fromString("connectorId");

    public static final String TRANSACTION_CONTEXT = "TransactionInitiated";

    public static final String TOPIC_PARTITION_STRUCT_NAME = "TopicPartition";
    public static final BString AVRO_DATA_RECORD_NAME = StringUtils.fromString("dataRecord");
    public static final BString AVRO_SCHEMA_STRING_NAME = StringUtils.fromString("schemaString");
    public static final String OFFSET_STRUCT_NAME = "PartitionOffset";

    public static final String CONSUMER_ERROR = "{ballerina/kafka}ConsumerError";
    public static final String PRODUCER_ERROR = "{ballerina/kafka}ProducerError";
    public static final String AVRO_ERROR = "{ballerina/kafka}AvroError";
    public static final String DETAIL_RECORD_NAME = "Detail";

    public static final String AVRO_GENERIC_RECORD_NAME = "AvroGenericRecord";
    public static final String CONSUMER_RECORD_STRUCT_NAME = "ConsumerRecord";
    public static final String CONSUMER_STRUCT_NAME = "Consumer";
    public static final String SERVER_CONNECTOR = "serverConnector";

    public static final BString CONSUMER_CONFIG_FIELD_NAME = StringUtils.fromString("consumerConfig");
    public static final BString PRODUCER_CONFIG_FIELD_NAME = StringUtils.fromString("producerConfig");

    public static final String PARAMETER_CONSUMER_NAME = KAFKA_PROTOCOL_PACKAGE + BLOCK_SEPARATOR
            + CONSUMER_STRUCT_NAME;
    public static final String PARAMETER_RECORD_ARRAY_NAME = KAFKA_PROTOCOL_PACKAGE + BLOCK_SEPARATOR
            + CONSUMER_RECORD_STRUCT_NAME + ARRAY_INDICATOR;
    public static final String PARAMETER_PARTITION_OFFSET_ARRAY_NAME = KAFKA_PROTOCOL_PACKAGE + BLOCK_SEPARATOR
            + OFFSET_STRUCT_NAME + ARRAY_INDICATOR;

    public static final String KAFKA_RESOURCE_ON_MESSAGE = "onMessage";

    public static final String ADDITIONAL_PROPERTIES_MAP_FIELD = "properties";

    public static final BString ALIAS_CONCURRENT_CONSUMERS = StringUtils.fromString("concurrentConsumers");
    public static final BString ALIAS_TOPICS = StringUtils.fromString("topics");
    public static final BString ALIAS_POLLING_TIMEOUT = StringUtils.fromString("pollingTimeoutInMillis");
    public static final BString ALIAS_POLLING_INTERVAL = StringUtils.fromString("pollingIntervalInMillis");
    public static final BString ALIAS_DECOUPLE_PROCESSING = StringUtils.fromString("decoupleProcessing");
    public static final BString ALIAS_TOPIC = StringUtils.fromString("topic");
    public static final BString ALIAS_PARTITION = StringUtils.fromString("partition");
    public static final BString ALIAS_OFFSET = StringUtils.fromString("offset");
    public static final String ALIAS_DURATION = "duration";
    public static final String ALIAS_VALUE = "value";

    // Consumer Configuration.
    public static final BString CONSUMER_BOOTSTRAP_SERVERS_CONFIG = StringUtils.fromString("bootstrapServers");
    public static final BString CONSUMER_GROUP_ID_CONFIG = StringUtils.fromString("groupId");
    public static final BString CONSUMER_AUTO_OFFSET_RESET_CONFIG = StringUtils.fromString("offsetReset");
    public static final BString CONSUMER_PARTITION_ASSIGNMENT_STRATEGY_CONFIG = StringUtils.fromString(
            "partitionAssignmentStrategy");
    public static final BString CONSUMER_METRICS_RECORDING_LEVEL_CONFIG = StringUtils.fromString(
            "metricsRecordingLevel");
    public static final BString CONSUMER_METRIC_REPORTER_CLASSES_CONFIG = StringUtils.fromString(
            "metricsReporterClasses");
    public static final BString CONSUMER_CLIENT_ID_CONFIG = StringUtils.fromString("clientId");
    public static final BString CONSUMER_INTERCEPTOR_CLASSES_CONFIG = StringUtils.fromString("interceptorClasses");
    public static final BString CONSUMER_ISOLATION_LEVEL_CONFIG = StringUtils.fromString("isolationLevel");
    public static final BString CONSUMER_KEY_DESERIALIZER_TYPE_CONFIG = StringUtils.fromString("keyDeserializerType");
    public static final BString CONSUMER_VALUE_DESERIALIZER_TYPE_CONFIG = StringUtils.fromString(
            "valueDeserializerType");
    public static final BString CONSUMER_KEY_DESERIALIZER_CONFIG = StringUtils.fromString("keyDeserializer");
    public static final BString CONSUMER_VALUE_DESERIALIZER_CONFIG = StringUtils.fromString("valueDeserializer");
    public static final String BALLERINA_STRAND = "ballerina.strand";
    public static final BString CONSUMER_SCHEMA_REGISTRY_URL = StringUtils.fromString("schemaRegistryUrl");

    public static final BString CONSUMER_SESSION_TIMEOUT_MS_CONFIG = StringUtils.fromString("sessionTimeoutInMillis");
    public static final BString CONSUMER_HEARTBEAT_INTERVAL_MS_CONFIG = StringUtils.fromString(
            "heartBeatIntervalInMillis");
    public static final BString CONSUMER_METADATA_MAX_AGE_CONFIG = StringUtils.fromString("metadataMaxAgeInMillis");
    public static final BString CONSUMER_AUTO_COMMIT_INTERVAL_MS_CONFIG = StringUtils.fromString(
            "autoCommitIntervalInMillis");
    public static final BString CONSUMER_MAX_PARTITION_FETCH_BYTES_CONFIG = StringUtils.fromString(
            "maxPartitionFetchBytes");
    public static final BString CONSUMER_SEND_BUFFER_CONFIG = StringUtils.fromString("sendBuffer");
    public static final BString CONSUMER_RECEIVE_BUFFER_CONFIG = StringUtils.fromString("receiveBuffer");
    public static final BString CONSUMER_FETCH_MIN_BYTES_CONFIG = StringUtils.fromString("fetchMinBytes");
    public static final BString CONSUMER_FETCH_MAX_BYTES_CONFIG = StringUtils.fromString("fetchMaxBytes");
    public static final BString CONSUMER_FETCH_MAX_WAIT_MS_CONFIG = StringUtils.fromString("fetchMaxWaitTimeInMillis");
    public static final BString CONSUMER_RECONNECT_BACKOFF_MS_CONFIG = StringUtils.fromString(
            "reconnectBackoffTimeInMillis");
    public static final BString CONSUMER_RETRY_BACKOFF_MS_CONFIG = StringUtils.fromString("retryBackoffInMillis");
    public static final BString CONSUMER_METRICS_SAMPLE_WINDOW_MS_CONFIG = StringUtils.fromString(
            "metricsSampleWindowInMillis");
    public static final BString CONSUMER_METRICS_NUM_SAMPLES_CONFIG = StringUtils.fromString("metricsNumSamples");
    public static final BString CONSUMER_REQUEST_TIMEOUT_MS_CONFIG = StringUtils.fromString("requestTimeoutInMillis");
    public static final BString CONSUMER_CONNECTIONS_MAX_IDLE_MS_CONFIG = StringUtils.fromString(
            "connectionMaxIdleTimeInMillis");
    public static final BString CONSUMER_MAX_POLL_RECORDS_CONFIG = StringUtils.fromString("maxPollRecords");
    public static final BString CONSUMER_MAX_POLL_INTERVAL_MS_CONFIG = StringUtils.fromString("maxPollInterval");
    public static final BString CONSUMER_RECONNECT_BACKOFF_MAX_MS_CONFIG = StringUtils.fromString(
            "reconnectBackoffTimeMaxInMillis");
    public static final BString CONSUMER_ENABLE_AUTO_COMMIT_CONFIG = StringUtils.fromString("autoCommit");
    public static final BString CONSUMER_CHECK_CRCS_CONFIG = StringUtils.fromString("checkCRCS");
    public static final BString CONSUMER_EXCLUDE_INTERNAL_TOPICS_CONFIG = StringUtils.fromString(
            "excludeInternalTopics");
    public static final BString CONSUMER_DEFAULT_API_TIMEOUT_CONFIG = StringUtils.fromString(
            "defaultApiTimeoutInMillis");

    // Producer Configuration.
    public static final BString PRODUCER_BOOTSTRAP_SERVERS_CONFIG = StringUtils.fromString("bootstrapServers");
    public static final BString PRODUCER_ACKS_CONFIG = StringUtils.fromString("acks");
    public static final BString PRODUCER_COMPRESSION_TYPE_CONFIG = StringUtils.fromString("compressionType");
    public static final BString PRODUCER_CLIENT_ID_CONFIG = StringUtils.fromString("clientId");
    public static final BString PRODUCER_METRICS_RECORDING_LEVEL_CONFIG = StringUtils.fromString(
            "metricsRecordingLevel");
    public static final BString PRODUCER_METRIC_REPORTER_CLASSES_CONFIG = StringUtils.fromString(
            "metricReporterClasses");
    public static final BString PRODUCER_PARTITIONER_CLASS_CONFIG = StringUtils.fromString("partitionerClass");
    public static final BString PRODUCER_INTERCEPTOR_CLASSES_CONFIG = StringUtils.fromString("interceptorClasses");
    public static final BString PRODUCER_TRANSACTIONAL_ID_CONFIG = StringUtils.fromString("transactionalId");
    public static final BString PRODUCER_KEY_SERIALIZER_TYPE_CONFIG = StringUtils.fromString("keySerializerType");
    public static final BString PRODUCER_VALUE_SERIALIZER_TYPE_CONFIG = StringUtils.fromString("valueSerializerType");
    public static final BString PRODUCER_KEY_SERIALIZER_CONFIG = StringUtils.fromString("keySerializer");
    public static final BString PRODUCER_VALUE_SERIALIZER_CONFIG = StringUtils.fromString("valueSerializer");
    public static final BString PRODUCER_SCHEMA_REGISTRY_URL = StringUtils.fromString("schemaRegistryUrl");
    public static final BString PRODUCER_BUFFER_MEMORY_CONFIG = StringUtils.fromString("bufferMemory");
    public static final BString PRODUCER_RETRIES_CONFIG = StringUtils.fromString("retryCount");
    public static final BString PRODUCER_BATCH_SIZE_CONFIG = StringUtils.fromString("batchSize");
    public static final BString PRODUCER_LINGER_MS_CONFIG = StringUtils.fromString("linger");
    public static final BString PRODUCER_SEND_BUFFER_CONFIG = StringUtils.fromString("sendBuffer");
    public static final BString PRODUCER_RECEIVE_BUFFER_CONFIG = StringUtils.fromString("receiveBuffer");
    public static final BString PRODUCER_MAX_REQUEST_SIZE_CONFIG = StringUtils.fromString("maxRequestSize");
    public static final BString PRODUCER_RECONNECT_BACKOFF_MS_CONFIG = StringUtils.fromString(
            "reconnectBackoffTimeInMillis");
    public static final BString PRODUCER_RECONNECT_BACKOFF_MAX_MS_CONFIG = StringUtils.fromString(
            "reconnectBackoffMaxTimeInMillis");
    public static final BString PRODUCER_RETRY_BACKOFF_MS_CONFIG = StringUtils.fromString("retryBackoffTimeInMillis");
    public static final BString PRODUCER_MAX_BLOCK_MS_CONFIG = StringUtils.fromString("maxBlock");
    public static final BString PRODUCER_REQUEST_TIMEOUT_MS_CONFIG = StringUtils.fromString("requestTimeoutInMillis");
    public static final BString PRODUCER_METADATA_MAX_AGE_CONFIG = StringUtils.fromString("metadataMaxAgeInMillis");
    public static final BString PRODUCER_METRICS_SAMPLE_WINDOW_MS_CONFIG = StringUtils.fromString(
            "metricsSampleWindowInMillis");
    public static final BString PRODUCER_METRICS_NUM_SAMPLES_CONFIG = StringUtils.fromString("metricsNumSamples");
    public static final BString PRODUCER_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = StringUtils.fromString(
            "maxInFlightRequestsPerConnection");
    public static final BString PRODUCER_CONNECTIONS_MAX_IDLE_MS_CONFIG = StringUtils.fromString(
            "connectionsMaxIdleTimeInMillis");
    public static final BString PRODUCER_TRANSACTION_TIMEOUT_CONFIG = StringUtils.fromString(
            "transactionTimeoutInMillis");
    public static final BString PRODUCER_ENABLE_IDEMPOTENCE_CONFIG = StringUtils.fromString("enableIdempotence");

    // SSL Configuration parameters.
    public static final BString SECURE_SOCKET = StringUtils.fromString("secureSocket");
    public static final BString KEYSTORE_CONFIG = StringUtils.fromString("keyStore");
    public static final BString TRUSTSTORE_CONFIG = StringUtils.fromString("trustStore");
    public static final BString PROTOCOL_CONFIG = StringUtils.fromString("protocol");
    public static final BString LOCATION_CONFIG = StringUtils.fromString("location");
    public static final BString PASSWORD_CONFIG = StringUtils.fromString("password");
    public static final BString KEYSTORE_TYPE_CONFIG = StringUtils.fromString("keyStoreType");
    public static final BString KEYMANAGER_ALGORITHM_CONFIG = StringUtils.fromString("keyManagerAlgorithm");
    public static final BString TRUSTSTORE_TYPE_CONFIG = StringUtils.fromString("trustStoreType");
    public static final BString TRUSTMANAGER_ALGORITHM_CONFIG = StringUtils.fromString("trustManagerAlgorithm");
    public static final BString ENABLED_PROTOCOLS_CONFIG = StringUtils.fromString("sslProtocolVersions");
    public static final BString SECURITY_PROTOCOL_CONFIG = StringUtils.fromString("securityProtocol");
    public static final BString SSL_PROTOCOL_CONFIG = StringUtils.fromString("sslProtocol");
    public static final BString SSL_PROVIDER_CONFIG = StringUtils.fromString("sslProvider");
    public static final BString SSL_KEY_PASSWORD_CONFIG = StringUtils.fromString("sslKeyPassword");
    public static final BString SSL_CIPHER_SUITES_CONFIG = StringUtils.fromString("sslCipherSuites");
    public static final BString SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG = StringUtils.fromString(
            "sslEndpointIdentificationAlgorithm");
    public static final BString SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG = StringUtils.fromString(
            "sslSecureRandomImplementation");

    // SASL Configuration parameters
    public static final BString AUTHENTICATION_CONFIGURATION = StringUtils.fromString("authenticationConfiguration");
    public static final BString AUTHENTICATION_MECHANISM = StringUtils.fromString("mechanism");
    public static final BString USERNAME = StringUtils.fromString("username");
    public static final BString PASSWORD = StringUtils.fromString("password");

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
