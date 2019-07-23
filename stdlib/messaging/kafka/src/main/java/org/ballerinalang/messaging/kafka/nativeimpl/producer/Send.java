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
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONNECTOR_ID;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ORG_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getLongValue;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.initiateTransaction;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.isTransactional;

/**
 * Native action produces blob value to given string topic.
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "send",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = PRODUCER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class Send {

    private static final Logger logger = LoggerFactory.getLogger(Send.class);

    @SuppressWarnings(UNCHECKED)
    public static Object send(Strand strand, ObjectValue producerObject, ArrayValue value, String topic, Object key,
                              Object partition, Object timestamp) {

        final NonBlockingCallback callback = new NonBlockingCallback(strand);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        byte[] keyValue = Objects.nonNull(key) ? ((ArrayValue) key).getBytes() : null;
        ProducerRecord<byte[], byte[]> kafkaRecord = new ProducerRecord(topic, partitionValue, timestampValue,
                keyValue, value.getBytes());
        Properties producerProperties = (Properties) producerObject.getNativeData(NATIVE_PRODUCER_CONFIG);
        KafkaProducer<byte[], byte[]> producer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        try {
            if (isTransactional(strand, producerProperties)) {
                initiateTransaction(strand, (String) producerObject.get(CONNECTOR_ID), producer);
            }
            producer.send(kafkaRecord, (metadata, e) -> {
                if (Objects.nonNull(e)) {
                    ErrorValue error = createError("Failed to send data to Kafka server: " + e.getMessage(),
                            CONSUMER_ERROR);
                    callback.setReturnValues(error);
                }
                callback.notifySuccess();
            });
        } catch (IllegalStateException | KafkaException e) {
            return createError("Failed to send data to Kafka server: " + e.getMessage());
        }
        return null;
    }
}
