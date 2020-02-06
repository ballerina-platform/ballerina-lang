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

package org.ballerinalang.messaging.kafka.nativeimpl.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;

import java.time.Duration;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_KEY_DESERIALIZER_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_VALUE_DESERIALIZER_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getConsumerRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateConsumerRecord;

/**
 * Native function polls the broker to retrieve messages within given timeout.
 */
public class Poll {

    /**
     * Polls from kafka broker using the ballerina kafka consumer.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @param timeout        Duration in milliseconds to try the operation.
     * @return Ballerina {@code ConsumerRecords[]} after the polling.
     */
    public static Object poll(ObjectValue consumerObject, long timeout) {
        Strand strand = Scheduler.getStrand();
        KafkaTracingUtil.traceResourceInvocation(strand, consumerObject);
        NonBlockingCallback callback = new NonBlockingCallback(strand);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        String keyType = consumerObject.getStringValue(CONSUMER_KEY_DESERIALIZER_TYPE_CONFIG);
        String valueType = consumerObject.getStringValue(CONSUMER_VALUE_DESERIALIZER_TYPE_CONFIG);
        Duration duration = Duration.ofMillis(timeout);
        BArray consumerRecordsArray = BValueCreator.createArrayValue(new BArrayType(getConsumerRecord().getType()));
        try {
            ConsumerRecords recordsRetrieved = kafkaConsumer.poll(duration);
            if (!recordsRetrieved.isEmpty()) {
                for (Object record : recordsRetrieved) {
                    MapValue<String, Object> recordValue = populateConsumerRecord((ConsumerRecord) record, keyType,
                                                                                  valueType);
                    consumerRecordsArray.append(recordValue);
                    KafkaMetricsUtil.reportConsume(consumerObject, recordValue.getStringValue("topic"),
                                                   recordValue.getArrayValue("value").size());
                }
            }
            callback.setReturnValues(consumerRecordsArray);
        } catch (IllegalStateException | IllegalArgumentException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_POLL);
            callback.notifyFailure(createKafkaError("Failed to poll from the Kafka server: " + e.getMessage(),
                                                    CONSUMER_ERROR));
        }
        callback.notifySuccess();
        return null;
    }
}
