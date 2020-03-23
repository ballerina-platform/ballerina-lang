/*
 *  Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.messaging.kafka.nativeimpl.producer;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getLongValue;

/**
 * Sends Avro values from Ballerina Kafka producers.
 */
public class SendAvroValues extends Send {
    private static final Logger logger = LoggerFactory.getLogger(SendAvroValues.class);

    // ballerina AvroRecord
    @SuppressWarnings(UNCHECKED)
    public static Object sendAvro(ObjectValue producer, MapValue<String, Object> value, String topic, Object partition,
                                  Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<?, Object> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, null,
                                                                     genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and String
    public static Object sendAvroString(ObjectValue producer, MapValue<String, Object> value, String topic, String key,
                                        Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, GenericRecord> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                                 key, genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and ballerina int
    public static Object sendAvroInt(ObjectValue producer, MapValue<String, Object> value, String topic, long key,
                                     Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, GenericRecord> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                               key, genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and ballerina float
    public static Object sendAvroFloat(ObjectValue producer, MapValue<String, Object> value, String topic, double key,
                                       Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, GenericRecord> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                                 key, genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and ballerina byte[]
    public static Object sendAvroByteArray(ObjectValue producer, MapValue<String, Object> value, String topic,
                                           BArray key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], GenericRecord> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                                 key.getBytes(), genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and AvroRecord
    public static Object sendAvroAvro(ObjectValue producer, MapValue<String, Object> value, String topic,
                                      MapValue<String, Object> key, Object partition, Object timestamp) {
        GenericRecord valueRecord = createGenericRecord(value);
        GenericRecord keyRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<GenericRecord, GenericRecord> kafkaRecord = new ProducerRecord<>(topic, partitionValue,
                                                                                        timestampValue,
                                                                                        keyRecord, valueRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and ballerina any
    public static Object sendAvroAny(ObjectValue producer, MapValue<String, Object> value, String topic, Object key,
                                     Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, GenericRecord> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                                 key, genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }
}
