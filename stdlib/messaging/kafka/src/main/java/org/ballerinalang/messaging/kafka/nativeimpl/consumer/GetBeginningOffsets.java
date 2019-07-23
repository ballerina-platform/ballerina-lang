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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DURATION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DURATION_UNDEFINED_VALUE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ORG_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getDefaultApiTimeout;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntFromLong;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getPartitionOffsetArrayFromOffsetMap;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionList;

/**
 * Native function returns beginning offsets for given partition array.
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "getBeginningOffsets",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = CONSUMER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class GetBeginningOffsets {

    private static final Logger logger = LoggerFactory.getLogger(GetBeginningOffsets.class);

    public static Object getBeginningOffsets(Strand strand, ObjectValue consumerObject, ArrayValue topicPartitions,
                                             long duration) {

        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        Properties consumerProperties = (Properties) consumerObject.getNativeData(NATIVE_CONSUMER_CONFIG);

        int defaultApiTimeout = getDefaultApiTimeout(consumerProperties);
        int apiTimeout = getIntFromLong(duration, logger, ALIAS_DURATION);
        ArrayList<TopicPartition> partitionList = getTopicPartitionList(topicPartitions);
        Map<TopicPartition, Long> offsetMap;
        try {
            if (apiTimeout > DURATION_UNDEFINED_VALUE) {
                offsetMap = getBeginningOffsetsWithDuration(kafkaConsumer, partitionList, apiTimeout);
            } else if (defaultApiTimeout > DURATION_UNDEFINED_VALUE) {
                offsetMap = getBeginningOffsetsWithDuration(kafkaConsumer, partitionList, defaultApiTimeout);
            } else {
                offsetMap = kafkaConsumer.beginningOffsets(partitionList);
            }
            return getPartitionOffsetArrayFromOffsetMap(offsetMap);
        } catch (KafkaException e) {
            return createError("Failed to retrieve offsets for the topic partitions: " + e.getMessage(),
                    CONSUMER_ERROR);
        }
    }

    private static Map<TopicPartition, Long> getBeginningOffsetsWithDuration(KafkaConsumer<byte[], byte[]> consumer,
                                                                             ArrayList<TopicPartition> partitions,
                                                                             long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        return consumer.beginningOffsets(partitions, duration);
    }
}
