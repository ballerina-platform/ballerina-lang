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
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ORG_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionList;

/**
 * Native function seeks given partitions to the beginning offset.
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "seekToBeginning",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = KafkaConstants.CONSUMER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class SeekToBeginning {

    private static final Logger logger = LoggerFactory.getLogger(SeekToBeginning.class);

    public static Object seekToBeginning(Strand strand, ObjectValue consumerObject, ArrayValue topicPartitions) {
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        ArrayList<TopicPartition> partitionList = getTopicPartitionList(topicPartitions, logger);
        try {
            kafkaConsumer.seekToBeginning(partitionList);
        } catch (IllegalStateException | IllegalArgumentException | KafkaException e) {
            return createKafkaError("Failed to seek the consumer to the beginning: " + e.getMessage(), CONSUMER_ERROR);
        }
        return null;
    }
}
