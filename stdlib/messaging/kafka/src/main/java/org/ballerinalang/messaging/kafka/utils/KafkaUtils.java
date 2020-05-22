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

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BError;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.ballerinalang.jvm.BallerinaValues.createRecord;
import static org.ballerinalang.messaging.kafka.utils.AvroUtils.handleAvroConsumer;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ADDITIONAL_PROPERTIES_MAP_FIELD;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_CONCURRENT_CONSUMERS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DECOUPLE_PROCESSING;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_OFFSET;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_POLLING_INTERVAL;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_POLLING_TIMEOUT;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_TOPIC;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_TOPICS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.AUTHENTICATION_CONFIGURATION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.AUTHENTICATION_MECHANISM;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.BALLERINA_STRAND;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_KEY_DESERIALIZER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_KEY_DESERIALIZER_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_VALUE_DESERIALIZER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_VALUE_DESERIALIZER_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KEYSTORE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PASSWORD;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_KEY_SERIALIZER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_KEY_SERIALIZER_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_VALUE_SERIALIZER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_VALUE_SERIALIZER_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PROTOCOL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SASL_PLAIN;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SECURE_SOCKET;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SECURITY_PROTOCOL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SERDES_AVRO;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SERDES_CUSTOM;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TRUSTSTORE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.USERNAME;

/**
 * Utility class for Kafka Connector Implementation.
 */
public class KafkaUtils {

    private KafkaUtils() {
    }

    public static Object[] getResourceParameters(ObjectValue service, ObjectValue listener,
                                                 ConsumerRecords records, String groupId) {

        BArray consumerRecordsArray = BValueCreator.createArrayValue(new BArrayType(getConsumerRecord().getType()));
        String keyType = listener.getStringValue(CONSUMER_KEY_DESERIALIZER_TYPE_CONFIG).getValue();
        String valueType = listener.getStringValue(KafkaConstants.CONSUMER_VALUE_DESERIALIZER_TYPE_CONFIG).getValue();

        if (service.getType().getAttachedFunctions()[0].getParameterType().length == 2) {
            for (Object record : records) {
                MapValue<BString, Object> consumerRecord = populateConsumerRecord(
                        (ConsumerRecord) record, keyType, valueType);
                consumerRecordsArray.append(consumerRecord);
            }
            return new Object[]{listener, true, consumerRecordsArray, true, null, false, null, false};
        } else {
            BArray partitionOffsetsArray =
                    BValueCreator.createArrayValue(new BArrayType(getPartitionOffsetRecord().getType()));
            for (Object record : records) {
                ConsumerRecord kafkaRecord = (ConsumerRecord) record;
                MapValue<BString, Object> consumerRecord = populateConsumerRecord(kafkaRecord, keyType, valueType);
                MapValue<BString, Object> topicPartition = populateTopicPartitionRecord(kafkaRecord.topic(),
                                                                                       kafkaRecord.partition());
                MapValue<BString, Object> partitionOffset = populatePartitionOffsetRecord(topicPartition,
                                                                                         kafkaRecord.offset());
                consumerRecordsArray.append(consumerRecord);
                partitionOffsetsArray.append(partitionOffset);
            }
            return new Object[]{listener, true, consumerRecordsArray, true, partitionOffsetsArray, true,
                    StringUtils.fromString(groupId), true};
        }
    }

    public static Properties processKafkaConsumerConfig(MapValue<BString, Object> configurations) {
        Properties properties = new Properties();

        addStringParamIfPresent(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_BOOTSTRAP_SERVERS_CONFIG);
        addStringParamIfPresent(ConsumerConfig.GROUP_ID_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_GROUP_ID_CONFIG);
        addStringParamIfPresent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_AUTO_OFFSET_RESET_CONFIG);
        addStringParamIfPresent(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_PARTITION_ASSIGNMENT_STRATEGY_CONFIG);
        addStringParamIfPresent(ConsumerConfig.METRICS_RECORDING_LEVEL_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_METRICS_RECORDING_LEVEL_CONFIG);
        addStringParamIfPresent(ConsumerConfig.METRIC_REPORTER_CLASSES_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_METRIC_REPORTER_CLASSES_CONFIG);
        addStringParamIfPresent(ConsumerConfig.CLIENT_ID_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_CLIENT_ID_CONFIG);
        addStringParamIfPresent(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_INTERCEPTOR_CLASSES_CONFIG);
        addStringParamIfPresent(ConsumerConfig.ISOLATION_LEVEL_CONFIG, configurations, properties,
                                KafkaConstants.CONSUMER_ISOLATION_LEVEL_CONFIG);

        addDeserializerConfigs(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, configurations, properties,
                               CONSUMER_KEY_DESERIALIZER_TYPE_CONFIG);
        addDeserializerConfigs(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, configurations, properties,
                               KafkaConstants.CONSUMER_VALUE_DESERIALIZER_TYPE_CONFIG);
        addCustomDeserializer(CONSUMER_KEY_DESERIALIZER_CONFIG, CONSUMER_KEY_DESERIALIZER_TYPE_CONFIG, properties,
                              configurations);
        addCustomDeserializer(CONSUMER_VALUE_DESERIALIZER_CONFIG, CONSUMER_VALUE_DESERIALIZER_TYPE_CONFIG, properties,
                              configurations);
        addStringParamIfPresent(KafkaConstants.SCHEMA_REGISTRY_URL, configurations, properties,
                                KafkaConstants.CONSUMER_SCHEMA_REGISTRY_URL);

        addStringArrayParamIfPresent(ALIAS_TOPICS.getValue(), configurations, properties,
                                     ALIAS_TOPICS);

        addIntParamIfPresent(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_SESSION_TIMEOUT_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_HEARTBEAT_INTERVAL_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.METADATA_MAX_AGE_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_METADATA_MAX_AGE_CONFIG);
        addIntParamIfPresent(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_AUTO_COMMIT_INTERVAL_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_MAX_PARTITION_FETCH_BYTES_CONFIG);
        addIntParamIfPresent(ConsumerConfig.SEND_BUFFER_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_SEND_BUFFER_CONFIG);
        addIntParamIfPresent(ConsumerConfig.RECEIVE_BUFFER_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_RECEIVE_BUFFER_CONFIG);
        addIntParamIfPresent(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_FETCH_MIN_BYTES_CONFIG);
        addIntParamIfPresent(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_FETCH_MAX_BYTES_CONFIG);
        addIntParamIfPresent(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_FETCH_MAX_WAIT_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_RECONNECT_BACKOFF_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_RETRY_BACKOFF_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_METRICS_SAMPLE_WINDOW_MS_CONFIG);

        addIntParamIfPresent(ConsumerConfig.METRICS_NUM_SAMPLES_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_METRICS_NUM_SAMPLES_CONFIG);
        addIntParamIfPresent(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_REQUEST_TIMEOUT_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_CONNECTIONS_MAX_IDLE_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_MAX_POLL_RECORDS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_MAX_POLL_INTERVAL_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_RECONNECT_BACKOFF_MAX_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, configurations, properties,
                             KafkaConstants.CONSUMER_DEFAULT_API_TIMEOUT_CONFIG);

        addIntParamIfPresent(ALIAS_POLLING_TIMEOUT.getValue(), configurations, properties, ALIAS_POLLING_TIMEOUT);
        addIntParamIfPresent(ALIAS_POLLING_INTERVAL.getValue(), configurations, properties, ALIAS_POLLING_INTERVAL);
        addIntParamIfPresent(ALIAS_CONCURRENT_CONSUMERS.getValue(), configurations, properties,
                             ALIAS_CONCURRENT_CONSUMERS);

        addBooleanParamIfPresent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configurations, properties,
                                 KafkaConstants.CONSUMER_ENABLE_AUTO_COMMIT_CONFIG, true);
        addBooleanParamIfPresent(ConsumerConfig.CHECK_CRCS_CONFIG, configurations, properties,
                                 KafkaConstants.CONSUMER_CHECK_CRCS_CONFIG, true);
        addBooleanParamIfPresent(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG, configurations, properties,
                                 KafkaConstants.CONSUMER_EXCLUDE_INTERNAL_TOPICS_CONFIG, true);

        addBooleanParamIfPresent(ALIAS_DECOUPLE_PROCESSING.getValue(), configurations, properties,
                                 ALIAS_DECOUPLE_PROCESSING, false);
        if (Objects.nonNull(configurations.get(SECURE_SOCKET))) {
            processSslProperties(configurations, properties);
        }
        if (SERDES_AVRO.equals(configurations.get(CONSUMER_VALUE_DESERIALIZER_CONFIG)) ||
                SERDES_AVRO.equals(configurations.get(CONSUMER_VALUE_DESERIALIZER_CONFIG))) {
            properties.put(KafkaConstants.SPECIFIC_AVRO_READER, false);
        }
        if (Objects.nonNull(configurations.get(AUTHENTICATION_CONFIGURATION))) {
            processSaslProperties(configurations, properties);
        }
        if (Objects.nonNull(configurations.getMapValue(ADDITIONAL_PROPERTIES_MAP_FIELD))) {
            processAdditionalProperties(configurations.getMapValue(ADDITIONAL_PROPERTIES_MAP_FIELD), properties);
        }
        return properties;
    }

    public static Properties processKafkaProducerConfig(MapValue<BString, Object> configurations) {
        Properties properties = new Properties();
        addStringParamIfPresent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_BOOTSTRAP_SERVERS_CONFIG);
        addStringParamIfPresent(ProducerConfig.ACKS_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_ACKS_CONFIG);
        addStringParamIfPresent(ProducerConfig.COMPRESSION_TYPE_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_COMPRESSION_TYPE_CONFIG);
        addStringParamIfPresent(ProducerConfig.CLIENT_ID_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_CLIENT_ID_CONFIG);
        addStringParamIfPresent(ProducerConfig.METRICS_RECORDING_LEVEL_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_METRICS_RECORDING_LEVEL_CONFIG);
        addStringParamIfPresent(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_METRIC_REPORTER_CLASSES_CONFIG);
        addStringParamIfPresent(ProducerConfig.PARTITIONER_CLASS_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_PARTITIONER_CLASS_CONFIG);
        addStringParamIfPresent(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_INTERCEPTOR_CLASSES_CONFIG);
        addStringParamIfPresent(ProducerConfig.TRANSACTIONAL_ID_CONFIG, configurations,
                                properties, KafkaConstants.PRODUCER_TRANSACTIONAL_ID_CONFIG);
        addStringParamIfPresent(KafkaConstants.SCHEMA_REGISTRY_URL, configurations, properties,
                                KafkaConstants.PRODUCER_SCHEMA_REGISTRY_URL);

        addSerializerTypeConfigs(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, configurations,
                                 properties, PRODUCER_KEY_SERIALIZER_TYPE_CONFIG);
        addSerializerTypeConfigs(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, configurations,
                                 properties, PRODUCER_VALUE_SERIALIZER_TYPE_CONFIG);
        addCustomKeySerializer(properties, configurations);
        addCustomValueSerializer(properties, configurations);

        addIntParamIfPresent(ProducerConfig.BUFFER_MEMORY_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_BUFFER_MEMORY_CONFIG);
        addIntParamIfPresent(ProducerConfig.RETRIES_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_RETRIES_CONFIG);
        addIntParamIfPresent(ProducerConfig.BATCH_SIZE_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_BATCH_SIZE_CONFIG);
        addIntParamIfPresent(ProducerConfig.LINGER_MS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_LINGER_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.SEND_BUFFER_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_SEND_BUFFER_CONFIG);
        addIntParamIfPresent(ProducerConfig.RECEIVE_BUFFER_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_RECEIVE_BUFFER_CONFIG);
        addIntParamIfPresent(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_MAX_REQUEST_SIZE_CONFIG);
        addIntParamIfPresent(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_RECONNECT_BACKOFF_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_RECONNECT_BACKOFF_MAX_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_RETRY_BACKOFF_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.MAX_BLOCK_MS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_MAX_BLOCK_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_REQUEST_TIMEOUT_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.METADATA_MAX_AGE_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_METADATA_MAX_AGE_CONFIG);
        addIntParamIfPresent(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_METRICS_SAMPLE_WINDOW_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_METRICS_NUM_SAMPLES_CONFIG);
        addIntParamIfPresent(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, configurations,
                             properties, KafkaConstants.PRODUCER_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION);
        addIntParamIfPresent(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_CONNECTIONS_MAX_IDLE_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_TRANSACTION_TIMEOUT_CONFIG);

        addBooleanParamIfPresent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, configurations,
                                 properties, KafkaConstants.PRODUCER_ENABLE_IDEMPOTENCE_CONFIG);
        if (Objects.nonNull(configurations.get(SECURE_SOCKET))) {
            processSslProperties(configurations, properties);
        }
        if (Objects.nonNull(configurations.get(AUTHENTICATION_CONFIGURATION))) {
            processSaslProperties(configurations, properties);
        }
        if (Objects.nonNull(configurations.getMapValue(ADDITIONAL_PROPERTIES_MAP_FIELD))) {
            processAdditionalProperties(configurations.getMapValue(ADDITIONAL_PROPERTIES_MAP_FIELD), properties);
        }
        return properties;
    }

    @SuppressWarnings(KafkaConstants.UNCHECKED)
    private static void processSslProperties(MapValue<BString, Object> configurations, Properties configParams) {
        MapValue<BString, Object> secureSocket = (MapValue<BString, Object>) configurations.get(SECURE_SOCKET);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                                KafkaConstants.KEYSTORE_TYPE_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                                KafkaConstants.LOCATION_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                                KafkaConstants.PASSWORD_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                                KafkaConstants.KEYMANAGER_ALGORITHM_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                                KafkaConstants.TRUSTSTORE_TYPE_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                                KafkaConstants.LOCATION_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                                KafkaConstants.PASSWORD_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTMANAGER_ALGORITHM_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                                KafkaConstants.TRUSTMANAGER_ALGORITHM_CONFIG);
        addStringParamIfPresent(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
                                SECURITY_PROTOCOL_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_PROTOCOL_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
                                KafkaConstants.SSL_PROTOCOL_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG,
                                (MapValue<BString, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
                                KafkaConstants.ENABLED_PROTOCOLS_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_PROVIDER_CONFIG, configurations, configParams,
                                KafkaConstants.SSL_PROVIDER_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEY_PASSWORD_CONFIG, configurations, configParams,
                                KafkaConstants.SSL_KEY_PASSWORD_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_CIPHER_SUITES_CONFIG, configurations, configParams,
                                KafkaConstants.SSL_CIPHER_SUITES_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, configurations, configParams,
                                KafkaConstants.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG, configurations, configParams,
                                KafkaConstants.SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG);
    }

    @SuppressWarnings(UNCHECKED)
    private static void processSaslProperties(MapValue<BString, Object> configurations, Properties properties) {
        MapValue<BString, Object> authenticationConfig =
                (MapValue<BString, Object>) configurations.getMapValue(AUTHENTICATION_CONFIGURATION);
        String mechanism = authenticationConfig.getStringValue(AUTHENTICATION_MECHANISM).getValue();
        if (SASL_PLAIN.equals(mechanism)) {
            String username = authenticationConfig.getStringValue(USERNAME).getValue();
            String password = authenticationConfig.getStringValue(PASSWORD).getValue();
            String jaasConfigValue =
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + username +
                            "\" password=\"" + password + "\";";
            addStringParamIfPresent(SaslConfigs.SASL_MECHANISM, authenticationConfig, properties,
                                    AUTHENTICATION_MECHANISM);
            addStringParamIfPresent(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, authenticationConfig, properties,
                                    SECURITY_PROTOCOL_CONFIG);
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfigValue);
        }
    }

    private static void processAdditionalProperties(MapValue propertiesMap, Properties kafkaProperties) {
        for (Object key : propertiesMap.getKeys()) {
            kafkaProperties.setProperty(key.toString(), propertiesMap.getStringValue((BString) key).getValue());
        }
    }

    private static void addSerializerTypeConfigs(String paramName, MapValue<BString, Object> configs,
                                                 Properties configParams, BString key) {
        if (Objects.nonNull(configs.get(key))) {
            String value = getSerializerType(configs, key);
            configParams.put(paramName, value);
        }
    }

    private static void addDeserializerConfigs(String paramName, MapValue<BString, Object> configs,
                                               Properties configParams, BString key) {
        if (Objects.nonNull(configs.get(key))) {
            String value = getDeserializerValue(configs, key);
            configParams.put(paramName, value);
        }
    }

    private static void addCustomKeySerializer(Properties properties, MapValue<BString, Object> configurations) {
        Object serializer = configurations.get(PRODUCER_KEY_SERIALIZER_CONFIG);
        String serializerType = configurations.getStringValue(PRODUCER_KEY_SERIALIZER_TYPE_CONFIG).getValue();
        if (Objects.nonNull(serializer) && SERDES_CUSTOM.equals(serializerType)) {
            properties.put(PRODUCER_KEY_SERIALIZER_CONFIG.getValue(),
                           configurations.get(PRODUCER_KEY_SERIALIZER_CONFIG));
        }
    }

    private static void addCustomValueSerializer(Properties properties, MapValue<BString, Object> configurations) {
        Object serializer = configurations.get(PRODUCER_VALUE_SERIALIZER_CONFIG);
        String serializerType = configurations.getStringValue(PRODUCER_VALUE_SERIALIZER_TYPE_CONFIG).getValue();
        if (Objects.nonNull(serializer) && SERDES_CUSTOM.equals(serializerType)) {
            properties.put(PRODUCER_VALUE_SERIALIZER_CONFIG.getValue(),
                           configurations.get(PRODUCER_VALUE_SERIALIZER_CONFIG));
        }
    }

    private static void addCustomDeserializer(BString configParam, BString typeConfig, Properties properties,
                                              MapValue<BString, Object> configurations) {
        Object deserializer = configurations.get(configParam);
        String deserializerType = configurations.getStringValue(typeConfig).getValue();
        if (Objects.nonNull(deserializer) && SERDES_CUSTOM.equals(deserializerType)) {
            properties.put(configParam.getValue(), configurations.get(configParam));
            properties.put(BALLERINA_STRAND, BRuntime.getCurrentRuntime());
        }
    }

    private static String getSerializerType(MapValue<BString, Object> configs, BString key) {
        String value = configs.get(key).toString();
        switch (value) {
            case KafkaConstants.SERDES_BYTE_ARRAY:
                return KafkaConstants.BYTE_ARRAY_SERIALIZER;
            case KafkaConstants.SERDES_STRING:
                return KafkaConstants.STRING_SERIALIZER;
            case KafkaConstants.SERDES_INT:
                return KafkaConstants.INT_SERIALIZER;
            case KafkaConstants.SERDES_FLOAT:
                return KafkaConstants.FLOAT_SERIALIZER;
            case KafkaConstants.SERDES_AVRO:
                return KafkaConstants.AVRO_SERIALIZER;
            case SERDES_CUSTOM:
                return KafkaConstants.CUSTOM_SERIALIZER;
            default:
                return value;
        }
    }

    private static String getDeserializerValue(MapValue<BString, Object> configs, BString key) {
        String value = configs.get(key).toString();
        switch (value) {
            case KafkaConstants.SERDES_BYTE_ARRAY:
                return KafkaConstants.BYTE_ARRAY_DESERIALIZER;
            case KafkaConstants.SERDES_STRING:
                return KafkaConstants.STRING_DESERIALIZER;
            case KafkaConstants.SERDES_INT:
                return KafkaConstants.INT_DESERIALIZER;
            case KafkaConstants.SERDES_FLOAT:
                return KafkaConstants.FLOAT_DESERIALIZER;
            case SERDES_AVRO:
                return KafkaConstants.AVRO_DESERIALIZER;
            case SERDES_CUSTOM:
                return KafkaConstants.CUSTOM_DESERIALIZER;
            default:
                return value;
        }
    }

    private static void addStringParamIfPresent(String paramName,
                                                MapValue<BString, Object> configs,
                                                Properties configParams,
                                                BString key) {
        if (Objects.nonNull(configs.get(key))) {
            BString value = (BString) configs.get(key);
            if (!(value == null || value.getValue().equals(""))) {
                configParams.setProperty(paramName, value.getValue());
            }
        }
    }

    private static void addStringArrayParamIfPresent(String paramName,
                                                     MapValue<BString, Object> configs,
                                                     Properties configParams,
                                                     BString key) {
        BArray stringArray = (BArray) configs.get(key);
        List<String> values = getStringListFromStringBArray(stringArray);
        configParams.put(paramName, values);
    }

    private static void addIntParamIfPresent(String paramName,
                                             MapValue<BString, Object> configs,
                                             Properties configParams,
                                             BString key) {
        Long value = (Long) configs.get(key);
        if (Objects.nonNull(value)) {
            configParams.put(paramName, value.intValue());
        }
    }

    private static void addBooleanParamIfPresent(String paramName,
                                                 MapValue<BString, Object> configs,
                                                 Properties configParams,
                                                 BString key,
                                                 boolean defaultValue) {
        boolean value = (boolean) configs.get(key);
        if (value != defaultValue) {
            configParams.put(paramName, value);
        }
    }

    private static void addBooleanParamIfPresent(String paramName,
                                                 MapValue<BString, Object> configs,
                                                 Properties configParams,
                                                 BString key) {
        boolean value = (boolean) configs.get(key);
        configParams.put(paramName, value);
    }

    public static ArrayList<TopicPartition> getTopicPartitionList(BArray partitions, Logger logger) {
        ArrayList<TopicPartition> partitionList = new ArrayList<>();
        if (partitions != null) {
            for (int counter = 0; counter < partitions.size(); counter++) {
                MapValue<BString, Object> partition = (MapValue<BString, Object>) partitions.get(counter);
                String topic = partition.get(ALIAS_TOPIC).toString();
                int partitionValue = getIntFromLong((Long) partition.get(ALIAS_PARTITION), logger,
                                                    ALIAS_PARTITION.getValue());
                partitionList.add(new TopicPartition(topic, partitionValue));
            }
        }
        return partitionList;
    }

    public static List<String> getStringListFromStringBArray(BArray stringArray) {
        ArrayList<String> values = new ArrayList<>();
        if ((Objects.isNull(stringArray)) ||
                (!((BArrayType) stringArray.getType()).getElementType().equals(BTypes.typeString))) {
            return values;
        }
        if (stringArray.size() != 0) {
            for (int i = 0; i < stringArray.size(); i++) {
                values.add(stringArray.getString(i));
            }
        }
        return values;
    }

    /**
     * Populate the {@code TopicPartition} record type in Ballerina.
     *
     * @param topic     name of the topic
     * @param partition value of the partition offset
     * @return {@code MapValue} of the record
     */
    public static MapValue<BString, Object> populateTopicPartitionRecord(String topic, int partition) {
        return createRecord(getTopicPartitionRecord(), topic, partition);
    }

    public static MapValue<BString, Object> populatePartitionOffsetRecord(MapValue<BString, Object> topicPartition,
                                                                          long offset) {
        return createRecord(getPartitionOffsetRecord(), topicPartition, offset);
    }

    public static MapValue<BString, Object> populateConsumerRecord(ConsumerRecord record, String keyType,
                                                                   String valueType) {
        Object key = null;
        if (Objects.nonNull(record.key())) {
            key = getBValues(record.key(), keyType);
        }

        Object value = getBValues(record.value(), valueType);
        return createRecord(getConsumerRecord(), key, value, record.offset(), record.partition(), record.timestamp(),
                            record.topic());
    }

    private static Object getBValues(Object value, String type) {
        if (KafkaConstants.SERDES_BYTE_ARRAY.equals(type)) {
            if (value instanceof byte[]) {
                return BValueCreator.createArrayValue((byte[]) value);
            } else {
                throw createKafkaError(CONSUMER_ERROR, "Invalid type - expected: byte[]");
            }
        } else if (KafkaConstants.SERDES_STRING.equals(type)) {
            if (value instanceof String) {
                return StringUtils.fromString((String) value);
            } else {
                throw createKafkaError(CONSUMER_ERROR, "Invalid type - expected: string");
            }
        } else if (KafkaConstants.SERDES_INT.equals(type)) {
            if (value instanceof Long) {
                return value;
            } else {
                throw createKafkaError(CONSUMER_ERROR, "Invalid type - expected: int");
            }
        } else if (KafkaConstants.SERDES_FLOAT.equals(type)) {
            if (value instanceof Double) {
                return value;
            } else {
                throw createKafkaError(CONSUMER_ERROR, "Invalid type - expected: float");
            }
        } else if (SERDES_AVRO.equals(type)) {
            return handleAvroConsumer(value);
        } else if (SERDES_CUSTOM.equals(type)) {
            return value;
        }
        throw createKafkaError("Unexpected type found for consumer record", CONSUMER_ERROR);
    }

    public static MapValue<BString, Object> getConsumerRecord() {
        return createKafkaRecord(KafkaConstants.CONSUMER_RECORD_STRUCT_NAME);
    }

    public static MapValue<BString, Object> getAvroGenericRecord() {
        return createKafkaRecord(KafkaConstants.AVRO_GENERIC_RECORD_NAME);
    }

    public static MapValue<BString, Object> getPartitionOffsetRecord() {
        return createKafkaRecord(KafkaConstants.OFFSET_STRUCT_NAME);
    }

    public static MapValue<BString, Object> getTopicPartitionRecord() {
        return createKafkaRecord(KafkaConstants.TOPIC_PARTITION_STRUCT_NAME);
    }

    public static BError createKafkaError(String message, String reason) {
        MapValue<BString, Object> detail = createKafkaDetailRecord(message);
        return BValueCreator.createErrorValue(StringUtils.fromString(reason), detail);
    }

    public static BError createKafkaError(String message, String reason, BError cause) {
        MapValue<BString, Object> detail = createKafkaDetailRecord(message, cause);
        return BValueCreator.createErrorValue(StringUtils.fromString(reason), detail);
    }

    private static MapValue<BString, Object> createKafkaDetailRecord(String message) {
        return createKafkaDetailRecord(message, null);
    }

    private static MapValue<BString, Object> createKafkaDetailRecord(String message, BError cause) {
        MapValue<BString, Object> detail = createKafkaRecord(KafkaConstants.DETAIL_RECORD_NAME);
        return BallerinaValues.createRecord(detail, message, cause);
    }

    public static MapValue<BString, Object> createKafkaRecord(String recordName) {
        return BallerinaValues.createRecordValue(KafkaConstants.KAFKA_PROTOCOL_PACKAGE_ID, recordName);
    }

    public static BArray getPartitionOffsetArrayFromOffsetMap(Map<TopicPartition, Long> offsetMap) {
        BArray partitionOffsetArray = BValueCreator.createArrayValue(new BArrayType(
                getPartitionOffsetRecord().getType()));
        if (!offsetMap.entrySet().isEmpty()) {
            for (Map.Entry<TopicPartition, Long> entry : offsetMap.entrySet()) {
                TopicPartition tp = entry.getKey();
                Long offset = entry.getValue();
                MapValue<BString, Object> topicPartition = populateTopicPartitionRecord(tp.topic(), tp.partition());
                MapValue<BString, Object> partition = populatePartitionOffsetRecord(topicPartition, offset);
                partitionOffsetArray.append(partition);
            }
        }
        return partitionOffsetArray;
    }

    /**
     * Get {@code Map<TopicPartition, OffsetAndMetadata>} map used in committing consumers.
     *
     * @param offsets {@code BArray} of Ballerina {@code PartitionOffset} records
     * @return {@code Map<TopicPartition, OffsetAndMetadata>} created using Ballerina {@code PartitionOffset}
     */
    public static Map<TopicPartition, OffsetAndMetadata> getPartitionToMetadataMap(BArray offsets) {
        Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = new HashMap<>();
        for (int i = 0; i < offsets.size(); i++) {
            MapValue offset = (MapValue) offsets.get(i);
            int offsetValue = offset.getIntValue(ALIAS_OFFSET).intValue();
            TopicPartition topicPartition = createTopicPartitionFromPartitionOffset(offset);
            partitionToMetadataMap.put(topicPartition, new OffsetAndMetadata(offsetValue));
        }
        return partitionToMetadataMap;
    }

    /**
     * Get {@code TopicPartition} object from {@code MapValue} of Ballerina {@code PartitionOffset}.
     *
     * @param offset MapValue consists of Ballerina PartitionOffset record.
     * @return TopicPartition Object created
     */
    public static TopicPartition createTopicPartitionFromPartitionOffset(MapValue offset) {
        MapValue partition = (MapValue) offset.get(ALIAS_PARTITION);
        String topic = partition.getStringValue(ALIAS_TOPIC).getValue();
        int partitionValue = partition.getIntValue(ALIAS_PARTITION).intValue();

        return new TopicPartition(topic, partitionValue);
    }

    /**
     * Get the Integer value from an Object, if possible.
     *
     * @param value  the {@code Object} which needs to be converted to int
     * @param name   name of the parameter, for logging purposes
     * @param logger {@code Logger} instance to log if there's an issue
     * @return Integer value of the {@code Object}, {@code null} otherwise
     */
    public static Integer getIntValue(Object value, BString name, Logger logger) {
        Long longValue = getLongValue(value);
        if (Objects.isNull(longValue)) {
            return null;
        }
        return getIntFromLong(longValue, logger, name.getValue());
    }

    /**
     * Get the {@code int} value from a {@code long} value.
     *
     * @param longValue {@code long} value, which we want to convert
     * @param logger    {@code Logger} instance, to log the error if there's an error
     * @param name      parameter name, which will be converted. This is required for logging purposes
     * @return {@code int} value of the {@code long} value, if possible, {@code Integer.MAX_VALUE} is the number is too
     * large
     */
    public static int getIntFromLong(long longValue, Logger logger, String name) {
        try {
            return Math.toIntExact(longValue);
        } catch (ArithmeticException e) {
            logger.warn("The value set for {} needs to be less than {}. The {} value is set to {}", name,
                        Integer.MAX_VALUE, name, Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Get the {@code Long} value from an {@code Object}.
     *
     * @param value Object from which we want to get the Long value
     * @return Long value of the Object, if present. {@code null} otherwise
     */
    public static Long getLongValue(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return (Long) value;
    }

    /**
     * Get the default API timeout defined in the Kafka configurations.
     *
     * @param consumerProperties - Native consumer properties object
     * @return value of the default api timeout, if defined, -1 otherwise.
     */
    public static int getDefaultApiTimeout(Properties consumerProperties) {
        if (Objects.nonNull(consumerProperties.get(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG))) {
            return (int) consumerProperties.get(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG);
        }
        return KafkaConstants.DURATION_UNDEFINED_VALUE;
    }

    public static void createKafkaProducer(Properties producerProperties, ObjectValue producerObject) {
        KafkaProducer kafkaProducer = new KafkaProducer<>(producerProperties);
        producerObject.addNativeData(KafkaConstants.NATIVE_PRODUCER, kafkaProducer);
        producerObject.addNativeData(KafkaConstants.NATIVE_PRODUCER_CONFIG, producerProperties);
        producerObject.addNativeData(KafkaConstants.BOOTSTRAP_SERVERS,
                                     producerProperties.getProperty(KafkaConstants.BOOTSTRAP_SERVERS));
        producerObject.addNativeData(KafkaConstants.CLIENT_ID, getClientIdFromProperties(producerProperties));
        KafkaMetricsUtil.reportNewProducer(producerObject);
    }

    public static String getBrokerNames(ObjectValue listener) {
        MapValue<BString, Object> listenerConfigurations = listener.getMapValue(CONSUMER_CONFIG_FIELD_NAME);
        return listenerConfigurations.get(KafkaConstants.CONSUMER_BOOTSTRAP_SERVERS_CONFIG).toString();
    }

    public static String getTopicNamesString(List<String> topicsList) {
        return String.join(", ", topicsList);
    }

    public static String getClientIdFromProperties(Properties properties) {
        if (properties == null) {
            return KafkaObservabilityConstants.UNKNOWN;
        }
        String clientId = properties.getProperty(KafkaConstants.CLIENT_ID);
        if (clientId == null) {
            return KafkaObservabilityConstants.UNKNOWN;
        }
        return clientId;
    }

    public static String getBootstrapServers(ObjectValue object) {
        if (object == null) {
            return KafkaObservabilityConstants.UNKNOWN;
        }
        String bootstrapServers = (String) object.getNativeData(KafkaConstants.BOOTSTRAP_SERVERS);
        if (bootstrapServers == null) {
            return KafkaObservabilityConstants.UNKNOWN;
        }
        return bootstrapServers;
    }

    public static String getClientId(ObjectValue object) {
        if (object == null) {
            return KafkaObservabilityConstants.UNKNOWN;
        }
        String clientId = (String) object.getNativeData(KafkaConstants.CLIENT_ID);
        if (clientId == null) {
            return KafkaObservabilityConstants.UNKNOWN;
        }
        return clientId;
    }
}
