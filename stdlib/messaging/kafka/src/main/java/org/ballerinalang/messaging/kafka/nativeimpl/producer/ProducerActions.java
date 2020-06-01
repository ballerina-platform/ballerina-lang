/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.messaging.kafka.nativeimpl.producer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.messaging.kafka.impl.KafkaTransactionContext;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_GROUP_ID_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TRANSACTION_CONTEXT;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaProducer;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getPartitionToMetadataMap;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.processKafkaProducerConfig;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.createKafkaTransactionContext;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.handleTransactions;

/**
 * Native methods to handle ballerina kafka producer.
 */
public class ProducerActions {

    /**
     * Initializes the ballerina kafka producer.
     *
     * @param producerObject Kafka producer object from ballerina.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object init(ObjectValue producerObject) {
        MapValue<BString, Object> configs = producerObject.getMapValue(PRODUCER_CONFIG_FIELD_NAME);
        Properties producerProperties = processKafkaProducerConfig(configs);
        try {
            if (Objects.nonNull(
                    producerProperties.get(ProducerConfig.TRANSACTIONAL_ID_CONFIG))) {
                if (!((boolean) producerProperties.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG))) {
                    throw new IllegalStateException("configuration enableIdempotence must be set to true to enable " +
                                                            "transactional producer");
                }
                createKafkaProducer(producerProperties, producerObject);
                KafkaTransactionContext transactionContext = createKafkaTransactionContext(producerObject);
                producerObject.addNativeData(TRANSACTION_CONTEXT, transactionContext);
            } else {
                createKafkaProducer(producerProperties, producerObject);
            }
        } catch (IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_CONNECTION);
            return createKafkaError("Failed to initialize the producer: " + e.getMessage(), PRODUCER_ERROR);
        }
        return null;
    }

    /**
     * Closes the connection between ballerina kafka producer and the kafka broker.
     *
     * @param producerObject Kafka producer object from ballerina.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object close(ObjectValue producerObject) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), producerObject);
        KafkaProducer kafkaProducer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        try {
            kafkaProducer.close();
            KafkaMetricsUtil.reportProducerClose(producerObject);
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject, KafkaObservabilityConstants.ERROR_TYPE_CLOSE);
            return createKafkaError("Failed to close the Kafka producer: " + e.getMessage(), PRODUCER_ERROR);
        }
        return null;
    }

    /**
     * Commits all the messages consumed by the provided ballerina kafka consumer object.
     *
     * @param producerObject Kafka producer object from ballerina.
     * @param consumer       Kafka consumer object from ballerina.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object commitConsumer(ObjectValue producerObject, ObjectValue consumer) {
        Strand strand = Scheduler.getStrand();
        KafkaTracingUtil.traceResourceInvocation(strand, producerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumer.getNativeData(NATIVE_CONSUMER);
        KafkaProducer kafkaProducer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = new HashMap<>();
        Set<TopicPartition> topicPartitions = kafkaConsumer.assignment();

        for (TopicPartition topicPartition : topicPartitions) {
            long position = kafkaConsumer.position(topicPartition);
            partitionToMetadataMap.put(new TopicPartition(topicPartition.topic(), topicPartition.partition()),
                                       new OffsetAndMetadata(position));
        }
        MapValue<String, Object> consumerConfig = consumer.getMapValue(CONSUMER_CONFIG_FIELD_NAME);
        String groupId = consumerConfig.getStringValue(CONSUMER_GROUP_ID_CONFIG).getValue();
        try {
            if (strand.isInTransaction()) {
                handleTransactions(strand, producerObject);
            }
            kafkaProducer.sendOffsetsToTransaction(partitionToMetadataMap, groupId);
        } catch (IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject, KafkaObservabilityConstants.ERROR_TYPE_COMMIT);
            return createKafkaError("Failed to commit consumer: " + e.getMessage(), PRODUCER_ERROR);
        }
        return null;
    }

    /**
     * Commits the given partition offsets.
     * @param producerObject Kafka producer object from ballerina.
     * @param offsets Ballerina {@code PartitionOffset[]} to commit.
     * @param groupId Group ID of the consumers to commit the messages.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object commitConsumerOffsets(ObjectValue producerObject, BArray offsets, BString groupId) {
        Strand strand = Scheduler.getStrand();
        KafkaTracingUtil.traceResourceInvocation(strand, producerObject);
        KafkaProducer kafkaProducer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = getPartitionToMetadataMap(offsets);
        try {
            if (strand.isInTransaction()) {
                handleTransactions(strand, producerObject);
            }
            kafkaProducer.sendOffsetsToTransaction(partitionToMetadataMap, groupId.getValue());
        } catch (IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject, KafkaObservabilityConstants.ERROR_TYPE_COMMIT);
            return createKafkaError("Failed to commit consumer offsets: " + e.getMessage(), PRODUCER_ERROR);
        }
        return null;
    }

    /**
     * Makes all the records buffered are immediately available.
     *
     * @param producerObject Kafka producer object from ballerina.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object flushRecords(ObjectValue producerObject) {
        Strand strand = Scheduler.getStrand();
        KafkaTracingUtil.traceResourceInvocation(strand, producerObject);
        KafkaProducer kafkaProducer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        try {
            if (strand.isInTransaction()) {
                handleTransactions(strand, producerObject);
            }
            kafkaProducer.flush();
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject, KafkaObservabilityConstants.ERROR_TYPE_FLUSH);
            return createKafkaError("Failed to flush Kafka records: " + e.getMessage(), PRODUCER_ERROR);
        }
        return null;
    }

    /**
     * Get information about a given topic.
     *
     * @param producerObject Kafka producer object from ballerina.
     * @param topic Topic about which the information is needed.
     * @return Ballerina {@code TopicPartition[]} for the given topic.
     */
    public static Object getTopicPartitions(ObjectValue producerObject, BString topic) {
        Strand strand = Scheduler.getStrand();
        KafkaTracingUtil.traceResourceInvocation(strand, producerObject, topic.getValue());
        KafkaProducer kafkaProducer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        try {
            if (strand.isInTransaction()) {
                handleTransactions(strand, producerObject);
            }
            List<PartitionInfo> partitionInfoList = kafkaProducer.partitionsFor(topic.getValue());
            BArray topicPartitionArray =
                    BValueCreator.createArrayValue(new BArrayType(getTopicPartitionRecord().getType()));
            for (PartitionInfo info : partitionInfoList) {
                MapValue<BString, Object> partition = populateTopicPartitionRecord(info.topic(), info.partition());
                topicPartitionArray.append(partition);
            }
            return topicPartitionArray;
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_TOPIC_PARTITIONS);
            return createKafkaError("Failed to fetch partitions from the producer " + e.getMessage(), PRODUCER_ERROR);
        }
    }
}
