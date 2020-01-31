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
import org.apache.kafka.common.config.SslConfigs;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
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
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_CONCURRENT_CONSUMERS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DECOUPLE_PROCESSING;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_OFFSET;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_POLLING_INTERVAL;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_POLLING_TIMEOUT;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_TOPIC;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_TOPICS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KEYSTORE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PROPERTIES_ARRAY;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PROTOCOL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SECURE_SOCKET;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TRUSTSTORE_CONFIG;

/**
 * Utility class for Kafka Connector Implementation.
 */
public class KafkaUtils {

    private KafkaUtils() {
    }

    public static Object[] getResourceParameters(ObjectValue service, ObjectValue listener,
                                                 ConsumerRecords records, String groupId) {

        BArray consumerRecordsArray = BValueCreator.createArrayValue(new BArrayType(getConsumerRecord().getType()));
        String keyType = listener.getStringValue(KafkaConstants.CONSUMER_KEY_DESERIALIZER_CONFIG);
        String valueType = listener.getStringValue(KafkaConstants.CONSUMER_VALUE_DESERIALIZER_CONFIG);

        if (service.getType().getAttachedFunctions()[0].getParameterType().length == 2) {
            for (Object record : records) {
                MapValue<String, Object> consumerRecord = populateConsumerRecord((ConsumerRecord) record, keyType,
                                                                                 valueType);
                consumerRecordsArray.append(consumerRecord);
            }
            return new Object[]{listener, true, consumerRecordsArray, true, null, false, null, false};
        } else {
            BArray partitionOffsetsArray =
                    BValueCreator.createArrayValue(new BArrayType(getPartitionOffsetRecord().getType()));
            for (Object record : records) {
                ConsumerRecord kafkaRecord = (ConsumerRecord) record;
                MapValue<String, Object> consumerRecord = populateConsumerRecord(kafkaRecord, keyType, valueType);
                MapValue<String, Object> topicPartition = populateTopicPartitionRecord(kafkaRecord.topic(),
                                                                                       kafkaRecord.partition());
                MapValue<String, Object> partitionOffset = populatePartitionOffsetRecord(topicPartition,
                                                                                         kafkaRecord.offset());
                consumerRecordsArray.append(consumerRecord);
                partitionOffsetsArray.append(partitionOffset);
            }
            return new Object[]{listener, true, consumerRecordsArray, true, partitionOffsetsArray, true, groupId, true};
        }
    }

    public static Properties processKafkaConsumerConfig(MapValue<String, Object> configurations) {
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
                               KafkaConstants.CONSUMER_KEY_DESERIALIZER_CONFIG);
        addDeserializerConfigs(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, configurations, properties,
                               KafkaConstants.CONSUMER_VALUE_DESERIALIZER_CONFIG);

        addStringArrayParamIfPresent(ALIAS_TOPICS, configurations, properties,
                                     ALIAS_TOPICS);
        addStringArrayParamIfPresent(PROPERTIES_ARRAY, configurations, properties, PROPERTIES_ARRAY);

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

        addIntParamIfPresent(ALIAS_POLLING_TIMEOUT, configurations, properties, ALIAS_POLLING_TIMEOUT);
        addIntParamIfPresent(ALIAS_POLLING_INTERVAL, configurations, properties, ALIAS_POLLING_INTERVAL);
        addIntParamIfPresent(ALIAS_CONCURRENT_CONSUMERS, configurations, properties, ALIAS_CONCURRENT_CONSUMERS);

        addBooleanParamIfPresent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configurations, properties,
                                 KafkaConstants.CONSUMER_ENABLE_AUTO_COMMIT_CONFIG, true);
        addBooleanParamIfPresent(ConsumerConfig.CHECK_CRCS_CONFIG, configurations, properties,
                                 KafkaConstants.CONSUMER_CHECK_CRCS_CONFIG, true);
        addBooleanParamIfPresent(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG, configurations, properties,
                                 KafkaConstants.CONSUMER_EXCLUDE_INTERNAL_TOPICS_CONFIG, true);

        addBooleanParamIfPresent(ALIAS_DECOUPLE_PROCESSING, configurations, properties,
                                 ALIAS_DECOUPLE_PROCESSING, false);
        if (Objects.nonNull(configurations.get(SECURE_SOCKET))) {
            processSSLProperties(configurations, properties);
        }
        return properties;
    }

    public static Properties processKafkaProducerConfig(MapValue<String, Object> configurations) {
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

        addSerializerConfigs(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_KEY_SERIALIZER_TYPE_CONFIG);
        addSerializerConfigs(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, configurations,
                             properties, KafkaConstants.PRODUCER_VALUE_SERIALIZER_TYPE_CONFIG);

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
            processSSLProperties(configurations, properties);
        }
        return properties;
    }

    @SuppressWarnings(KafkaConstants.UNCHECKED)
    private static void processSSLProperties(MapValue<String, Object> configurations, Properties configParams) {
        MapValue<String, Object> secureSocket = (MapValue<String, Object>) configurations.get(SECURE_SOCKET);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                                KafkaConstants.KEYSTORE_TYPE_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                                KafkaConstants.LOCATION_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                                KafkaConstants.PASSWORD_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                                KafkaConstants.KEYMANAGER_ALGORITHM_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                                KafkaConstants.TRUSTSTORE_TYPE_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                                KafkaConstants.LOCATION_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                                KafkaConstants.PASSWORD_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTMANAGER_ALGORITHM_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                                KafkaConstants.TRUSTMANAGER_ALGORITHM_CONFIG);
        addStringParamIfPresent(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
                                KafkaConstants.SECURITY_PROTOCOL_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_PROTOCOL_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
                                KafkaConstants.SSL_PROTOCOL_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG,
                                (MapValue<String, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
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

    private static void addSerializerConfigs(String paramName,
                                             MapValue<String, Object> configs,
                                             Properties configParams,
                                             String key) {
        if (Objects.nonNull(configs.get(key))) {
            String value = getSerializerValue(configs, key);
            configParams.put(paramName, value);
        }
    }

    private static void addDeserializerConfigs(String paramName,
                                               MapValue<String, Object> configs,
                                               Properties configParams,
                                               String key) {
        if (Objects.nonNull(configs.get(key))) {
            String value = getDeserializerValue(configs, key);
            configParams.put(paramName, value);
        }
    }

    private static String getSerializerValue(MapValue<String, Object> configs, String key) {
        String value = (String) configs.get(key);
        switch (value) {
            case KafkaConstants.SERDES_BYTE_ARRAY:
                return KafkaConstants.BYTE_ARRAY_SERIALIZER;
            case KafkaConstants.SERDES_STRING:
                return KafkaConstants.STRING_SERIALIZER;
            case KafkaConstants.SERDES_INT:
                return KafkaConstants.INT_SERIALIZER;
            case KafkaConstants.SERDES_FLOAT:
                return KafkaConstants.FLOAT_SERIALIZER;
            default:
                return value;
        }
    }

    private static String getDeserializerValue(MapValue<String, Object> configs, String key) {
        String value = (String) configs.get(key);
        switch (value) {
            case KafkaConstants.SERDES_BYTE_ARRAY:
                return KafkaConstants.BYTE_ARRAY_DESERIALIZER;
            case KafkaConstants.SERDES_STRING:
                return KafkaConstants.STRING_DESERIALIZER;
            case KafkaConstants.SERDES_INT:
                return KafkaConstants.INT_DESERIALIZER;
            case KafkaConstants.SERDES_FLOAT:
                return KafkaConstants.FLOAT_DESERIALIZER;
            default:
                return value;
        }
    }

    private static void addStringParamIfPresent(String paramName,
                                                MapValue<String, Object> configs,
                                                Properties configParams,
                                                String key) {
        if (Objects.nonNull(configs.get(key))) {
            String value = (String) configs.get(key);
            if (!(value == null || value.equals(""))) {
                configParams.put(paramName, value);
            }
        }
    }

    private static void addStringArrayParamIfPresent(String paramName,
                                                     MapValue<String, Object> configs,
                                                     Properties configParams,
                                                     String key) {
        BArray stringArray = (BArray) configs.get(key);
        List<String> values = getStringListFromStringBArray(stringArray);
        configParams.put(paramName, values);
    }

    private static void addIntParamIfPresent(String paramName,
                                             MapValue<String, Object> configs,
                                             Properties configParams,
                                             String key) {
        long value = (long) configs.get(key);
        if (value != -1) {
            configParams.put(paramName, Long.valueOf(value).intValue());
        }
    }

    private static void addBooleanParamIfPresent(String paramName,
                                                 MapValue<String, Object> configs,
                                                 Properties configParams,
                                                 String key,
                                                 boolean defaultValue) {
        boolean value = (boolean) configs.get(key);
        if (value != defaultValue) {
            configParams.put(paramName, value);
        }
    }

    private static void addBooleanParamIfPresent(String paramName,
                                                 MapValue<String, Object> configs,
                                                 Properties configParams,
                                                 String key) {
        boolean value = (boolean) configs.get(key);
        configParams.put(paramName, value);
    }

    public static ArrayList<TopicPartition> getTopicPartitionList(BArray partitions, Logger logger) {
        ArrayList<TopicPartition> partitionList = new ArrayList<>();
        if (partitions != null) {
            for (int counter = 0; counter < partitions.size(); counter++) {
                MapValue<String, Object> partition = (MapValue<String, Object>) partitions.get(counter);
                String topic = (String) partition.get(ALIAS_TOPIC);
                int partitionValue = getIntFromLong((Long) partition.get(ALIAS_PARTITION), logger, ALIAS_PARTITION);
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
    public static MapValue<String, Object> populateTopicPartitionRecord(String topic, int partition) {
        return createRecord(getTopicPartitionRecord(), topic, partition);
    }

    public static MapValue<String, Object> populatePartitionOffsetRecord(MapValue<String, Object> topicPartition,
                                                                         long offset) {
        return createRecord(getPartitionOffsetRecord(), topicPartition, offset);
    }

    public static MapValue<String, Object> populateConsumerRecord(ConsumerRecord record, String keyType,
                                                                  String valueType) {
        if (Objects.isNull(record)) {
            return null;
        }

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
                throw new BLangRuntimeException("Invalid type - expected: byte[]");
            }
        } else if (KafkaConstants.SERDES_STRING.equals(type)) {
            if (value instanceof String) {
                return StringUtils.fromString((String) value);
            } else {
                throw new BLangRuntimeException("Invalid type - expected: string");
            }
        } else if (KafkaConstants.SERDES_INT.equals(type)) {
            if (value instanceof Long) {
                return value;
            } else {
                throw new BLangRuntimeException("Invalid type - expected: int");
            }
        } else if (KafkaConstants.SERDES_FLOAT.equals(type)) {
            if (value instanceof Double) {
                return value;
            } else {
                throw new BLangRuntimeException("Invalid type - expected: float");
            }
        }
        throw new BLangRuntimeException("Unexpected type");
    }

    public static MapValue<String, Object> getConsumerRecord() {
        return createKafkaRecord(KafkaConstants.CONSUMER_RECORD_STRUCT_NAME);
    }

    public static MapValue<String, Object> getPartitionOffsetRecord() {
        return createKafkaRecord(KafkaConstants.OFFSET_STRUCT_NAME);
    }

    public static MapValue<String, Object> getTopicPartitionRecord() {
        return createKafkaRecord(KafkaConstants.TOPIC_PARTITION_STRUCT_NAME);
    }

    public static ErrorValue createKafkaError(String message) {
        return createKafkaError(message, KafkaConstants.CONSUMER_ERROR);
    }

    public static ErrorValue createKafkaError(String message, String reason) {
        MapValue<String, Object> detail = createKafkaDetailRecord(message);
        return BallerinaErrors.createError(reason, detail);
    }

    private static MapValue<String, Object> createKafkaDetailRecord(String message) {
        return createKafkaDetailRecord(message, null);
    }

    private static MapValue<String, Object> createKafkaDetailRecord(String message, ErrorValue cause) {
        MapValue<String, Object> detail = createKafkaRecord(KafkaConstants.DETAIL_RECORD_NAME);
        return BallerinaValues.createRecord(detail, message, cause);
    }

    public static MapValue<String, Object> createKafkaRecord(String recordName) {
        return BallerinaValues.createRecordValue(KafkaConstants.KAFKA_PROTOCOL_PACKAGE_ID, recordName);
    }

    public static BArray getPartitionOffsetArrayFromOffsetMap(Map<TopicPartition, Long> offsetMap) {
        BArray partitionOffsetArray = BValueCreator.createArrayValue(new BArrayType(
                getPartitionOffsetRecord().getType()));
        if (!offsetMap.entrySet().isEmpty()) {
            for (Map.Entry<TopicPartition, Long> entry : offsetMap.entrySet()) {
                TopicPartition tp = entry.getKey();
                Long offset = entry.getValue();
                MapValue<String, Object> topicPartition = populateTopicPartitionRecord(tp.topic(), tp.partition());
                MapValue<String, Object> partition = populatePartitionOffsetRecord(topicPartition, offset);
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
        String topic = partition.getStringValue(ALIAS_TOPIC);
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
    public static Integer getIntValue(Object value, String name, Logger logger) {
        Long longValue = getLongValue(value);
        if (Objects.isNull(longValue)) {
            return null;
        }
        return getIntFromLong(longValue, logger, name);
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
        MapValue<String, Object> listenerConfigurations = listener.getMapValue(CONSUMER_CONFIG_FIELD_NAME);
        return (String) listenerConfigurations.get(KafkaConstants.CONSUMER_BOOTSTRAP_SERVERS_CONFIG);
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
