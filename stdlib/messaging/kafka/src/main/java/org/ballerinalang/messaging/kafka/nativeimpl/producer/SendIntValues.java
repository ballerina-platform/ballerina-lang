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
import org.ballerinalang.jvm.values.api.BString;
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
    /* ************************************************************************ *
     *              Send records with value of type int                         *
     *       The value is considered first since key can be null                *
     ************************************************************************** */

    private static final Logger logger = LoggerFactory.getLogger(SendIntValues.class);

    // ballerina int and ()
    public static Object sendIntValuesNilKeys(ObjectValue producer, long value, BString topic, Object partition,
                                              Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<?, Long> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue, timestampValue,
                                                                   null, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and String
    public static Object sendIntValuesStringKeys(ObjectValue producer, long value, BString topic, BString key,
                                                 Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<String, Long> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                        timestampValue, key.getValue(), value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina int
    public static Object sendIntValuesIntKeys(ObjectValue producer, long value, BString topic, long key,
                                              Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Long, Long> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue, timestampValue,
                                                                      key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina float
    public static Object sendIntValuesFloatKeys(ObjectValue producer, long value, BString topic, double key,
                                                Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Double, Long> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                        timestampValue, key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina byte[]
    public static Object sendIntValuesByteArrayKeys(ObjectValue producer, long value, BString topic, BArray key,
                                                    Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<byte[], Long> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                        timestampValue, key.getBytes(), value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and ballerina anydata
    public static Object sendIntValuesCustomKeys(ObjectValue producer, long value, BString topic, Object key,
                                                 Object partition, Object timestamp) {
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<Object, Long> kafkaRecord = new ProducerRecord<>(topic.getValue(), partitionValue,
                                                                        timestampValue, key, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }
}
