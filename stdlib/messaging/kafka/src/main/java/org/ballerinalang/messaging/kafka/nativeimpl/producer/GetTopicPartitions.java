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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;

import java.util.List;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.handleTransactions;

/**
 * Native action retrieves partitions for given Topic via remote call.
 */
public class GetTopicPartitions {

    public static Object getTopicPartitions(ObjectValue producerObject, String topic) {
        Strand strand = Scheduler.getStrand();
        KafkaTracingUtil.traceResourceInvocation(strand, producerObject, topic);
        KafkaProducer kafkaProducer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        try {
            if (strand.isInTransaction()) {
                handleTransactions(strand, producerObject);
            }
            List<PartitionInfo> partitionInfoList = kafkaProducer.partitionsFor(topic);
            ArrayValue topicPartitionArray = new ArrayValueImpl(new BArrayType(getTopicPartitionRecord().getType()));
//            if (!partitionInfoList.isEmpty()) {
//                partitionInfoList.forEach(info -> {
//                    MapValue<String, Object> partition = populateTopicPartitionRecord(info.topic(), info.partition());
//                    topicPartitionArray.append(partition);
//                });
//            }

            // TODO: Use the above commented code instead of the for loop once #17075 fixed.
            int i = 0;
            for (PartitionInfo info : partitionInfoList) {
                MapValue<String, Object> partition = populateTopicPartitionRecord(info.topic(), info.partition());
                topicPartitionArray.add(i++, partition);
            }

            return topicPartitionArray;
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_TOPIC_PARTITIONS);
            return createKafkaError("Failed to fetch partitions from the producer " + e.getMessage(), PRODUCER_ERROR);
        }
    }
}
