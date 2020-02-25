/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.kafka.clients.producer.ProducerRecord;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.messaging.kafka.nativeimpl.producer.Send.sendKafkaRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getLongValue;

/**
 * Native methods to send {@code any} keys and with different types of values to Kafka broker from ballerina kafka
 * producer.
 */
public class SendAnyKeys {
    // TODO: This class should be merged with "Send" when #20918 is fixed.
    private static final Logger logger = LoggerFactory.getLogger(SendAnyKeys.class);

    // String and ballerina any
    public static Object send(ObjectValue producer, String value, String topic, Object key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, String> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina any
    public static Object send(ObjectValue producer, long value, String topic, Object key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                        value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and ballerina any
    public static Object send(ObjectValue producer, double value, String topic, Object key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina byte[] and ballerina any
    public static Object send(ObjectValue producer, BArray value, String topic, Object key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, byte[]> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value.getBytes());
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina any and ballerina any
    // TODO: This method should be renamed to "send" when #20918 is fixed.
    public static Object sendAnyAny(ObjectValue producer, Object value, String topic, Object key, Object partition,
                              Object timestamp) {
        // Shall we pass this here, or access it in sendKafkaRecord() function ?
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, Object> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                          value);
        return sendKafkaRecord(kafkaRecord, producer);
    }
}
