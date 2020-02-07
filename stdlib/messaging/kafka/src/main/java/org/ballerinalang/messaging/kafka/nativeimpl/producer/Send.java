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
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getLongValue;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.handleTransactions;

/**
 * Native method to send different types of keys and values to kafka broker from ballerina kafka producer.
 */
public class Send {

    private static final Logger logger = LoggerFactory.getLogger(Send.class);

    /* *********************************************************************** *
     *              Send records with value of type String                      *
     *       The value is considered first since key can be null                *
     ************************************************************************** */

    // String and ()
    public static Object send(ObjectValue producer, String value, String topic, Object partition, Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, null,
                                                                          value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // String and String
    public static Object send(ObjectValue producer, String value, String topic, String key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                          value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // String and ballerina int
    public static Object send(ObjectValue producer, String value, String topic, long key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, String> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                        value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // String and ballerina float
    public static Object send(ObjectValue producer, String value, String topic, double key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, String> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // String and ballerina byte[]
    public static Object send(ObjectValue producer, String value, String topic, BArray key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], String> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key.getBytes(), value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    /* *********************************************************************** *
     *              Send records with value of type int                      *
     *       The value is considered first since key can be null                *
     ************************************************************************** */
    // ballerina int and ()
    public static Object send(ObjectValue producer, long value, String topic, Object partition, Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                      null, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and String
    public static Object send(ObjectValue producer, long value, String topic, String key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                        key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina int
    public static Object send(ObjectValue producer, long value, String topic, long key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                      key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina float
    public static Object send(ObjectValue producer, long value, String topic, double key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                        key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina byte[]
    public static Object send(ObjectValue producer, long value, String topic, BArray key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                        key.getBytes(), value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    /* *********************************************************************** *
     *              Send records with value of type float                      *
     *       The value is considered first since key can be null                *
     ************************************************************************** */
    // ballerina float and ()
    public static Object send(ObjectValue producer, double value, String topic, Object partition, Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          null, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and String
    public static Object send(ObjectValue producer, double value, String topic, String key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and ballerina int
    public static Object send(ObjectValue producer, double value, String topic, long key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                        key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and ballerina float
    public static Object send(ObjectValue producer, double value, String topic, double key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and ballerina byte[]
    public static Object send(ObjectValue producer, double value, String topic, BArray key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key.getBytes(), value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    /* *********************************************************************** *
     *              Send records with value of type byte[]                      *
     *       The value is considered first since key can be null                *
     ************************************************************************** */
    // ballerina byte[]
    public static Object send(ObjectValue producer, BArray value, String topic, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], byte[]> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, null,
                                                                          value.getBytes());
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina byte[] and String
    public static Object send(ObjectValue producer, BArray value, String topic, String key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, byte[]> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value.getBytes());
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina byte[] and ballerina int
    public static Object send(ObjectValue producer, BArray value, String topic, long key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, byte[]> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                        key, value.getBytes());
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina byte[] and ballerina float
    public static Object send(ObjectValue producer, BArray value, String topic, double key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, byte[]> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value.getBytes());
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina byte[] and ballerina byte[]
    public static Object send(ObjectValue producer, BArray value, String topic, BArray key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], byte[]> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key.getBytes(), value.getBytes());
        return sendKafkaRecord(kafkaRecord, producer);
    }

    @SuppressWarnings(UNCHECKED)
    private static Object sendKafkaRecord(ProducerRecord record, ObjectValue producerObject) {
        Strand strand = Scheduler.getStrand();
        KafkaTracingUtil.traceResourceInvocation(strand, producerObject, record.topic());
        KafkaTracingUtil.traceResourceInvocation(strand, producerObject, record.topic());
        final NonBlockingCallback callback = new NonBlockingCallback(strand);
        KafkaProducer producer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        try {
            if (strand.isInTransaction()) {
                handleTransactions(strand, producerObject);
            }
            producer.send(record, (metadata, e) -> {
                if (Objects.nonNull(e)) {
                    KafkaMetricsUtil.reportProducerError(producerObject,
                                                         KafkaObservabilityConstants.ERROR_TYPE_PUBLISH);
                    callback.notifyFailure(createKafkaError("Failed to send data to Kafka server: " + e.getMessage(),
                                                            PRODUCER_ERROR));
                } else {
                    KafkaMetricsUtil.reportPublish(producerObject, record.topic(), record.value());
                }
                callback.notifySuccess();
            });
        } catch (IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject, KafkaObservabilityConstants.ERROR_TYPE_PUBLISH);
            callback.notifyFailure(createKafkaError("Failed to send data to Kafka server: " + e.getMessage(),
                                                    PRODUCER_ERROR));
        }
        return null;
    }
}
