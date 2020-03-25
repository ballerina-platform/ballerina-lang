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

import org.apache.kafka.clients.producer.ProducerRecord;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getLongValue;

/**
 * Native methods to send {@code float} values and with different types of keys to Kafka broker from ballerina kafka
 * producer.
 */
public class SendFloatValues extends Send {
    /* ************************************************************************ *
     *              Send records with value of type float                       *
     *       The value is considered first since key can be null                *
     ************************************************************************** */

    private static final Logger logger = LoggerFactory.getLogger(SendFloatValues.class);

    // ballerina float and ()
    public static Object sendFloatValuesNilKeys(ObjectValue producer, double value, String topic, Object partition,
                                                Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<?, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, null,
                                                                     value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and String
    public static Object sendFloatValuesStringKeys(ObjectValue producer, double value, String topic, String key,
                                                   Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                          value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and ballerina int
    public static Object sendFloatValuesIntKeys(ObjectValue producer, double value, String topic, long key,
                                                Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                        value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and ballerina float
    public static Object sendFloatValuesFloatKeys(ObjectValue producer, double value, String topic, double key,
                                                  Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue, key,
                                                                          value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and ballerina byte[]
    public static Object sendFloatValuesByteArrayKeys(ObjectValue producer, double value, String topic, BArray key,
                                                      Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key.getBytes(), value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and ballerina anydata
    public static Object sendFloatValuesCustomKeys(ObjectValue producer, double value, String topic, Object key,
                                                   Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                          key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }
}
