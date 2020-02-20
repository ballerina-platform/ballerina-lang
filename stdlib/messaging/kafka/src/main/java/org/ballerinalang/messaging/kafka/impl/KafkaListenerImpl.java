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

package org.ballerinalang.messaging.kafka.impl;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.messaging.kafka.api.KafkaListener;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaObserverContext;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_RESOURCE_ON_MESSAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getResourceParameters;

/**
 * Kafka Connector Consumer for Ballerina.
 */
public class KafkaListenerImpl implements KafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListenerImpl.class);

    private Scheduler scheduler;
    private ObjectValue service;
    private ObjectValue listener;
    private ResponseCallback callback;

    public KafkaListenerImpl(Strand strand, ObjectValue listener, ObjectValue service) {
        this.scheduler = strand.scheduler;
        this.listener = listener;
        this.service = service;
        callback = new ResponseCallback();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRecordsReceived(ConsumerRecords records, KafkaConsumer kafkaConsumer, String groupId) {
        listener.addNativeData(NATIVE_CONSUMER, kafkaConsumer);
        executeResource(listener, records, groupId);
        KafkaMetricsUtil.reportConsume(listener, records);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRecordsReceived(ConsumerRecords records, KafkaConsumer kafkaConsumer, String groupId,
                                  KafkaPollCycleFutureListener consumer) {
        listener.addNativeData(NATIVE_CONSUMER, kafkaConsumer);
        executeResource(listener, consumer, records, groupId);
        KafkaMetricsUtil.reportConsume(listener, records);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Throwable throwable) {
        logger.error("Kafka Ballerina server connector retrieved exception: " + throwable.getMessage(), throwable);
        KafkaMetricsUtil.reportConsumerError(listener, KafkaObservabilityConstants.ERROR_TYPE_MSG_RECEIVED);
    }

    private void executeResource(ObjectValue listener, ConsumerRecords records, String groupId) {
        if (ObserveUtils.isTracingEnabled()) {
            Map<String, Object> properties = getNewObserverContextInProperties(listener);
            Executor.submit(this.scheduler, service, KAFKA_RESOURCE_ON_MESSAGE, callback, properties,
                            getResourceParameters(service, this.listener, records, groupId));
        } else {
            Executor.submit(this.scheduler, service, KAFKA_RESOURCE_ON_MESSAGE, callback, null,
                            getResourceParameters(service, this.listener, records, groupId));
        }
    }

    private void executeResource(ObjectValue listener, KafkaPollCycleFutureListener consumer, ConsumerRecords records,
                                 String groupId) {
        if (ObserveUtils.isTracingEnabled()) {
            Map<String, Object> properties = getNewObserverContextInProperties(listener);
            Executor.submit(this.scheduler, service, KAFKA_RESOURCE_ON_MESSAGE, consumer, properties,
                            getResourceParameters(service, this.listener, records, groupId));
        } else {
            Executor.submit(this.scheduler, service, KAFKA_RESOURCE_ON_MESSAGE, consumer, null,
                            getResourceParameters(service, this.listener, records, groupId));
        }
    }

    private Map<String, Object> getNewObserverContextInProperties(ObjectValue listener) {
        Map<String, Object> properties = new HashMap<>();
        KafkaObserverContext observerContext = new KafkaObserverContext(KafkaObservabilityConstants.CONTEXT_CONSUMER,
                                                                        KafkaUtils.getClientId(listener),
                                                                        KafkaUtils.getBootstrapServers(listener));
        properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
        return properties;
    }

    private static class ResponseCallback implements CallableUnitCallback {

        @Override
        public void notifySuccess() {
            // do nothing
        }

        @Override
        public void notifyFailure(ErrorValue error) {
            // do nothing
        }
    }
}
