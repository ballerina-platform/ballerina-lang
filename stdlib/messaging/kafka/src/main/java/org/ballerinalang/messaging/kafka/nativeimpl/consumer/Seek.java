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
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_OFFSET;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createTopicPartitionFromPartitionOffset;

/**
 * Native function seeks given consumer to given offset reside in partition.
 */
public class Seek {

    public static Object seek(ObjectValue consumerObject, MapValue<String, Object> partitionOffset) {
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        TopicPartition topicPartition = createTopicPartitionFromPartitionOffset(partitionOffset);
        Long offset = partitionOffset.getIntValue(ALIAS_OFFSET);

        try {
            kafkaConsumer.seek(topicPartition, offset);
        } catch (IllegalStateException | IllegalArgumentException | KafkaException e) {
            return createKafkaError("Failed to seek the consumer: " + e.getMessage(), CONSUMER_ERROR);
        }
        return null;
    }
}
