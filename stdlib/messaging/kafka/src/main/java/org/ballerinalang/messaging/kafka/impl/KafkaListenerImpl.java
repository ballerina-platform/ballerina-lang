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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.messaging.kafka.api.KafkaListener;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_RESOURCE_ON_MESSAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getResourceParameters;

/**
 * Kafka Connector Consumer for Ballerina.
 */
public class KafkaListenerImpl implements KafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListenerImpl.class);

    private Strand strand;
    private ObjectValue service;
    private ObjectValue listener;
    private ResponseCallback callback;

    public KafkaListenerImpl(Strand strand, ObjectValue listener, ObjectValue service) {
        this.strand = strand;
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
        Executor.submit(strand.scheduler, service, KAFKA_RESOURCE_ON_MESSAGE, callback,
                null, getResourceParameters(service, this.listener, records, groupId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRecordsReceived(ConsumerRecords records,
                                  KafkaConsumer kafkaConsumer,
                                  String groupID,
                                  KafkaPollCycleFutureListener consumer) {
        listener.addNativeData(NATIVE_CONSUMER, kafkaConsumer);
        Executor.submit(strand.scheduler, service, KAFKA_RESOURCE_ON_MESSAGE, consumer, null,
                getResourceParameters(service, this.listener, records, groupID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Throwable throwable) {
        logger.error("Kafka Ballerina server connector retrieved exception: " + throwable.getMessage(), throwable);
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
