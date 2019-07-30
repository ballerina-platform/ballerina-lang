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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.time.Duration;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getConsumerRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateConsumerRecord;

/**
 * Native function polls the broker to retrieve messages within given timeout.
 */
@BallerinaFunction(
        orgName = KafkaConstants.ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "poll",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = KafkaConstants.CONSUMER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class Poll {

    public static Object poll(Strand strand, ObjectValue consumerObject, long timeout) {
        NonBlockingCallback callback = new NonBlockingCallback(strand);
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        Duration duration = Duration.ofMillis(timeout);
        ArrayValue consumerRecordsArray = new ArrayValue(new BArrayType(getConsumerRecord().getType()));
        try {
            ConsumerRecords<byte[], byte[]> recordsRetrieved = kafkaConsumer.poll(duration);
//            if (!recordsRetrieved.isEmpty()) {
//                recordsRetrieved.forEach(record -> {
//                    MapValue<String, Object> recordValue = populateConsumerRecord(record);
//                    consumerRecordsArray.append(recordValue);
//                });
//            }
            // TODO: Use the above commented code instead of the for loop once #17075 fixed.
            int i = 0;
            for (ConsumerRecord<byte[], byte[]> record : recordsRetrieved) {
                MapValue<String, Object> partition = populateConsumerRecord(record);
                consumerRecordsArray.add(i++, partition);
            }
            callback.setReturnValues(consumerRecordsArray);
        } catch (IllegalStateException | IllegalArgumentException | KafkaException e) {
            callback.setReturnValues(createKafkaError("Failed to poll from the Kafka server: " + e.getMessage(),
                    CONSUMER_ERROR));
        }
        callback.notifySuccess();
        return null;
    }
}


