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
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getLongValue;

/**
 * Native methods to send {@code int} values and with different types of keys to Kafka broker from ballerina kafka
 * producer.
 */
public class SendIntValues extends Send {

    private static final Logger logger = LoggerFactory.getLogger(SendIntValues.class);

    /* *********************************************************************** *
     *              Send records with value of type int                      *
     *       The value is considered first since key can be null                *
     ************************************************************************** */
    // ballerina int and ()
    public static Object sendInt(ObjectValue producer, long value, String topic, Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<?, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, null, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and String
    public static Object sendIntString(ObjectValue producer, long value, String topic, String key, Object partition,
                                       Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                        value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina int
    public static Object sendIntInt(ObjectValue producer, long value, String topic, long key, Object partition,
                                    Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                      value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina float
    public static Object sendIntFloat(ObjectValue producer, long value, String topic, double key, Object partition,
                                      Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                        value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina byte[]
    public static Object sendIntByteArray(ObjectValue producer, long value, String topic, BArray key, Object partition,
                                          Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                        key.getBytes(), value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and AvroRecord
    public static Object sendIntAvro(ObjectValue producer, long value, String topic, MapValue<String, Object> key,
                                     Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(key);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<GenericRecord, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                               genericRecord, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina any
    public static Object sendIntAny(ObjectValue producer, long value, String topic, Object key, Object partition,
                                    Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                        value);
        return sendKafkaRecord(kafkaRecord, producer);
    }
}
