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

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.ballerinalang.messaging.kafka.api.KafkaListener;
import org.ballerinalang.messaging.kafka.api.KafkaServerConnector;
import org.ballerinalang.messaging.kafka.exceptions.KafkaConnectorException;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * {@code KafkaServerConnectorImpl} This is the implementation for the {@code KafkaServerConnector} API which provides
 * transport receiver implementation for Kafka.
 */
public class KafkaServerConnectorImpl implements KafkaServerConnector {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServerConnectorImpl.class);

    private String serviceId;
    private KafkaListener kafkaListener;
    private Properties configParams;
    private int numOfConcurrentConsumers = 1;
    private List<KafkaRecordConsumer> messageConsumers;
    private KafkaConsumer kafkaConsumer;

    public KafkaServerConnectorImpl(String serviceId, Properties configParams, KafkaListener kafkaListener,
                                    KafkaConsumer kafkaConsumer) throws KafkaConnectorException {
        this.kafkaListener = kafkaListener;
        this.serviceId = serviceId;
        if (configParams.get(KafkaConstants.ALIAS_CONCURRENT_CONSUMERS) != null) {
            this.numOfConcurrentConsumers = (Integer) configParams.get(KafkaConstants.ALIAS_CONCURRENT_CONSUMERS);
        }
        if (this.numOfConcurrentConsumers <= 0) {
            throw new KafkaConnectorException(
                    "Number of Concurrent consumers should be a positive integer value greater than zero.");
        }
        this.configParams = configParams;
        this.kafkaConsumer = kafkaConsumer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws KafkaConnectorException {
        try {
            this.messageConsumers = new ArrayList<>();
            for (int counter = 0; counter < numOfConcurrentConsumers; counter++) {
                KafkaRecordConsumer consumer = new KafkaRecordConsumer(this.kafkaListener, this.configParams,
                                                                       this.serviceId, counter, this.kafkaConsumer);
                this.messageConsumers.add(consumer);
                consumer.consume();
                if (logger.isDebugEnabled()) {
                    logger.debug("Started Kafka consumer " + counter + " on service : " + serviceId + ".");
                }
            }
        } catch (KafkaException e) {
            throw new KafkaConnectorException(
                    "Error creating Kafka consumer to connect with remote broker and subscribe to provided topics", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stop() throws KafkaConnectorException {
        KafkaConnectorException ex = null;
        for (KafkaRecordConsumer consumer : this.messageConsumers) {
            try {
                consumer.stopConsume();
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Stopped Kafka consumer " + consumer.getConsumerId() + " on service : " + serviceId + ".");
                }
            } catch (KafkaException e) {
                if (ex == null) {
                    ex = new KafkaConnectorException("Error closing the Kafka consumers for service " + serviceId, e);
                } else {
                    ex.addSuppressed(e);
                }
            }
        }
        this.messageConsumers = null;
        if (ex != null) {
            throw ex;
        }
        return true;
    }
}
