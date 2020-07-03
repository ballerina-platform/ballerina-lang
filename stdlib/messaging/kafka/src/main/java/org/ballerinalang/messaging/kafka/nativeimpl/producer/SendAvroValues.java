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
import org.ballerinalang.jvm.values.api.BString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.messaging.kafka.nativeimpl.producer.SendAvroKeys.createGenericRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getLongValue;

/**
 * Native methods to send `kafka:AvroRecord` values and with different types of keys to Kafka broker from ballerina
 * kafka producer.
 */
public class SendAvroValues extends Send {
    /* ************************************************************************ *
     *              Send records with value of type AvroRecord                  *
     *       The value is considered first since key can be null                *
     ************************************************************************** */

    private static final Logger logger = LoggerFactory.getLogger(SendAvroValues.class);

    // ballerina AvroRecord
    @SuppressWarnings(UNCHECKED)
    public static Object sendAvroValuesNilKeys(ObjectValue producer, MapValue<BString, Object> value, BString topic,
                                               Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<?, Object> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue, timestampValue,
                                                                     null, genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and String
    public static Object sendAvroValuesStringKeys(ObjectValue producer, MapValue<BString, Object> value, BString topic,
                                                  BString key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, GenericRecord> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                                 timestampValue, key.getValue(),
                                                                                 genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and ballerina int
    public static Object sendAvroValuesIntKeys(ObjectValue producer, MapValue<BString, Object> value, BString topic,
                                               long key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, GenericRecord> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                               timestampValue, key, genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and ballerina float
    public static Object sendAvroValuesFloatKeys(ObjectValue producer, MapValue<BString, Object> value, BString topic,
                                                 double key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, GenericRecord> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                                 timestampValue, key, genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and ballerina byte[]
    public static Object sendAvroValuesByteArrayKeys(ObjectValue producer, MapValue<BString, Object> value,
                                                     BString topic, BArray key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], GenericRecord> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                                 timestampValue, key.getBytes(),
                                                                                 genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and ballerina anydata
    public static Object sendAvroValuesCustomKeys(ObjectValue producer, MapValue<BString, Object> value, BString topic,
                                                  Object key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(value);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, GenericRecord> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                                 timestampValue, key, genericRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }
}
