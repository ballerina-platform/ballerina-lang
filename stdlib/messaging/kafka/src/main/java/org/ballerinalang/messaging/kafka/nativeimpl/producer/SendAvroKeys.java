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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.messaging.kafka.nativeimpl.producer.Send.sendKafkaRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getLongValue;

/**
 * Sends Avro keys with different value types from Ballerina Kafka producers.
 */
public class SendAvroKeys {
    /* ************************************************************************ *
     *                 Send records with key of type AvroRecord                 *
     *                 This class is separated from the others                  *
     *         since we don't pack avro dependencies with the distribution      *
     ************************************************************************** */

    private static final Logger logger = LoggerFactory.getLogger(SendAvroKeys.class);

    // String and AvroRecord
    public static Object sendStringValuesAvroKeys(ObjectValue producer, String value, String topic,
                                                  MapValue<String, Object> key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(key);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<GenericRecord, String> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                                 genericRecord, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina int and AvroRecord
    public static Object sendIntValuesAvroKeys(ObjectValue producer, long value, String topic,
                                               MapValue<String, Object> key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(key);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<GenericRecord, Long> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                               genericRecord, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina float and AvroRecord
    public static Object sendFloatValuesAvroKeys(ObjectValue producer, double value, String topic,
                                                 MapValue<String, Object> key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(key);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<GenericRecord, Double> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                                 genericRecord, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina byte[] and AvroRecord
    public static Object sendByteArrayValuesAvroKeys(ObjectValue producer, BArray value, String topic,
                                                     MapValue<String, Object> key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(key);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<GenericRecord, byte[]> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                                 genericRecord, value.getBytes());
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina AvroRecord and AvroRecord
    public static Object sendAvroValuesAvroKeys(ObjectValue producer, MapValue<String, Object> value, String topic,
                                                MapValue<String, Object> key, Object partition, Object timestamp) {
        GenericRecord valueRecord = createGenericRecord(value);
        GenericRecord keyRecord = createGenericRecord(key);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<GenericRecord, GenericRecord> kafkaRecord = new ProducerRecord<>(topic, partitionValue,
                                                                                        timestampValue,
                                                                                        keyRecord, valueRecord);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    // ballerina anydata and AvroRecord
    public static Object sendCustomValuesAvroKeys(ObjectValue producer, Object value, String topic,
                                                  MapValue<String, Object> key, Object partition, Object timestamp) {
        GenericRecord genericRecord = createGenericRecord(key);
        Integer partitionValue = getIntValue(partition, ALIAS_PARTITION, logger);
        Long timestampValue = getLongValue(timestamp);
        ProducerRecord<GenericRecord, Object> kafkaRecord = new ProducerRecord<>(topic, partitionValue, timestampValue,
                                                                                 genericRecord, value);
        return sendKafkaRecord(kafkaRecord, producer);
    }

    protected static GenericRecord createGenericRecord(MapValue<String, Object> value) {
        GenericRecord genericRecord = createRecord(value);
        MapValue data = value.getMapValue(KafkaConstants.AVRO_DATA_RECORD_NAME);
        populateAvroRecord(genericRecord, data);
        return genericRecord;
    }

    protected static void populateAvroRecord(GenericRecord record, MapValue<String, Object> data) {
        String[] keys = data.getKeys();
        for (String key : keys) {
            Object value = data.get(key);
            if (value instanceof String || value instanceof Number || value == null) {
                record.put(key, value);
            } else if (value instanceof MapValue) {
                Schema childSchema = record.getSchema().getField(key).schema();
                GenericRecord subRecord = new GenericData.Record(childSchema);
                populateAvroRecord(subRecord, (MapValue<String, Object>) value);
                record.put(key, subRecord);
            } else if (value instanceof BArray) {
                Schema childSchema = record.getSchema().getField(key).schema().getElementType();
                GenericRecord subRecord = new GenericData.Record(childSchema);
                populateAvroRecordArray(subRecord, (BArray) value);
                record.put(key, subRecord);
            } else {
                throw KafkaUtils.createKafkaError("Invalid data type received for avro data",
                                                  KafkaConstants.AVRO_ERROR);
            }
        }
    }

    protected static void populateAvroRecordArray(GenericRecord record, BArray bArray) {
        for (int i = 0; i < bArray.size(); i++) {
            record.put(i, bArray.get(i));
        }
    }

    protected static GenericRecord createRecord(MapValue value) {
        String schemaString = value.getStringValue(KafkaConstants.AVRO_SCHEMA_STRING_NAME);
        Schema avroSchema = new Schema.Parser().parse(schemaString);
        return new GenericData.Record(avroSchema);
    }
}
