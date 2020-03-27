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
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;

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
}
