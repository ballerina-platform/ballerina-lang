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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.List;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ORG_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateTopicPartitionRecord;

/**
 * Native action retrieves partitions for given Topic via remote call.
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "getTopicPartitions",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = PRODUCER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class GetTopicPartitions {

    public static Object getTopicPartitions(Strand strand, ObjectValue producerObject, String topic) {
        KafkaProducer<byte[], byte[]> kafkaProducer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        try {
            List<PartitionInfo> partitionInfoList = kafkaProducer.partitionsFor(topic);
            ArrayValue topicPartitionArray = new ArrayValue(new BArrayType(getTopicPartitionRecord().getType()));
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
            return createKafkaError("Failed to fetch partitions from the producer " + e.getMessage(), PRODUCER_ERROR);
        }
    }
}
