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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.handleTransactions;

/**
 * Native method to send different types of keys and values to kafka broker from ballerina kafka producer.
 */
public class Send {

    private static final Logger logger = LoggerFactory.getLogger(Send.class);

    @SuppressWarnings(UNCHECKED)
    protected static Object sendKafkaRecord(ProducerRecord record, ObjectValue producerObject) {
        Strand strand = Scheduler.getStrand();
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
                    callback.setReturnValues(createKafkaError("Failed to send data to Kafka server: " + e.getMessage(),
                                                              PRODUCER_ERROR));
                } else {
                    KafkaMetricsUtil.reportPublish(producerObject, record.topic(), record.value());
                    callback.setReturnValues(null);
                }

                callback.notifySuccess();
            });
        } catch (IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportProducerError(producerObject, KafkaObservabilityConstants.ERROR_TYPE_PUBLISH);
            callback.setReturnValues(createKafkaError("Failed to send data to Kafka server: " + e.getMessage(),
                                                      PRODUCER_ERROR));
            callback.notifySuccess();

        }
        return null;
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
