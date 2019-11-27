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

package org.ballerinalang.messaging.kafka.nativeimpl.producer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_GROUP_ID_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.handleTransactions;

/**
 * Native action commits the consumer offsets in transaction.
 */
public class CommitConsumer {

    public static Object commitConsumer(ObjectValue producerObject, ObjectValue consumer) {
        Strand strand = Scheduler.getStrand();
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumer.getNativeData(NATIVE_CONSUMER);
        KafkaProducer kafkaProducer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = new HashMap<>();
        Set<TopicPartition> topicPartitions = kafkaConsumer.assignment();

        topicPartitions.forEach(topicPartition -> {
            long position = kafkaConsumer.position(topicPartition);
            partitionToMetadataMap.put(new TopicPartition(topicPartition.topic(), topicPartition.partition()),
                    new OffsetAndMetadata(position));
        });
        MapValue<String, Object> consumerConfig = consumer.getMapValue(CONSUMER_CONFIG_FIELD_NAME);
        String groupId = consumerConfig.getStringValue(CONSUMER_GROUP_ID_CONFIG);
        try {
            if (strand.isInTransaction()) {
                handleTransactions(strand, producerObject);
            }
            kafkaProducer.sendOffsetsToTransaction(partitionToMetadataMap, groupId);
        } catch (IllegalStateException | KafkaException e) {
            return createKafkaError("Failed to commit consumer: " + e.getMessage(), PRODUCER_ERROR);
        }
        return null;
    }
}
