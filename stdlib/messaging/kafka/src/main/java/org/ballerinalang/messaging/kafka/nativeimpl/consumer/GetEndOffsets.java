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

package org.ballerinalang.messaging.kafka.nativeimpl.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DURATION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DURATION_UNDEFINED_VALUE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getDefaultApiTimeout;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntFromLong;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getPartitionOffsetArrayFromOffsetMap;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionList;

/**
 * Native function returns end offsets for given partition array.
 */
public class GetEndOffsets {

    private static final Logger logger = LoggerFactory.getLogger(GetEndOffsets.class);

    public static Object getEndOffsets(ObjectValue consumerObject, ArrayValue topicPartitions,
                                       long duration) {
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        Properties consumerProperties = (Properties) consumerObject.getNativeData(NATIVE_CONSUMER_CONFIG);

        int defaultApiTimeout = getDefaultApiTimeout(consumerProperties);
        int apiTimeout = getIntFromLong(duration, logger, ALIAS_DURATION);
        ArrayList<TopicPartition> partitionList = getTopicPartitionList(topicPartitions, logger);
        Map<TopicPartition, Long> offsetMap;

        try {
            if (apiTimeout > DURATION_UNDEFINED_VALUE) {
                offsetMap = getEndOffsetsWithDuration(kafkaConsumer, partitionList, apiTimeout);
            } else if (defaultApiTimeout > DURATION_UNDEFINED_VALUE) {
                offsetMap = getEndOffsetsWithDuration(kafkaConsumer, partitionList, defaultApiTimeout);
            } else {
                offsetMap = kafkaConsumer.endOffsets(partitionList);
            }
        } catch (KafkaException e) {
            return createKafkaError("Failed to retrieve end offsets for the consumer: " + e.getMessage(),
                    CONSUMER_ERROR);
        }

        return getPartitionOffsetArrayFromOffsetMap(offsetMap);
    }

    private static Map<TopicPartition, Long> getEndOffsetsWithDuration(KafkaConsumer<byte[], byte[]> consumer,
                                                                       ArrayList<TopicPartition> partitions,
                                                                       long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        return consumer.endOffsets(partitions, duration);
    }
}
