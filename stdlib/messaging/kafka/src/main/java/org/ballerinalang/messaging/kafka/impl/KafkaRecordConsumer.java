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

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.WakeupException;
import org.ballerinalang.messaging.kafka.api.KafkaListener;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code KafkaRecordConsumer} This class represents Runnable flow which periodically poll the remote broker and fetch
 * Kafka records.
 */
public class KafkaRecordConsumer {

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private static final Logger logger = LoggerFactory.getLogger(KafkaRecordConsumer.class);

    private KafkaConsumer kafkaConsumer;
    private Duration pollingTimeout = Duration.ofMillis(1000);
    private int pollingInterval = 1000;
    private boolean decoupleProcessing = true;
    private String groupId;
    private KafkaListener kafkaListener;
    private String serviceId;
    private int consumerId;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture pollTaskFuture;

    public KafkaRecordConsumer(KafkaListener kafkaListener, Properties configParams, String serviceId, int consumerId,
                               KafkaConsumer kafkaConsumer) {
        this.serviceId = serviceId;
        this.consumerId = consumerId;
        // Initialize Kafka Consumer.
        if (Objects.isNull(kafkaConsumer)) {
            this.kafkaConsumer = new KafkaConsumer<>(configParams);
        } else {
            this.kafkaConsumer = kafkaConsumer;
        }
        List<String> topics = (ArrayList<String>) configParams.get(KafkaConstants.ALIAS_TOPICS.getValue());
        // Subscribe Kafka Consumer to given topics.
        this.kafkaConsumer.subscribe(topics);
        this.kafkaListener = kafkaListener;
        if (configParams.get(KafkaConstants.ALIAS_POLLING_TIMEOUT.getValue()) != null) {
            this.pollingTimeout = Duration.ofMillis((Integer) configParams.get(KafkaConstants.ALIAS_POLLING_TIMEOUT));
        }
        if (configParams.get(KafkaConstants.ALIAS_POLLING_INTERVAL.getValue()) != null) {
            this.pollingInterval = (Integer) configParams.get(KafkaConstants.ALIAS_POLLING_INTERVAL.getValue());
        }
        if (configParams.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG) != null) {
            this.decoupleProcessing = (Boolean) configParams.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
        }
        // This is to override default decouple processing setting if required.
        if (configParams.get(KafkaConstants.ALIAS_DECOUPLE_PROCESSING.getValue()) != null) {
            this.decoupleProcessing = (Boolean) configParams.get(KafkaConstants.ALIAS_DECOUPLE_PROCESSING);
        }
        this.groupId = (String) configParams.get(ConsumerConfig.GROUP_ID_CONFIG);
    }

    private void poll() {
        try {
            ConsumerRecords recordsRetrieved = null;
            try {
                // Make thread-safe as kafka does not support multiple thread access
                if (!closed.get()) {
                    recordsRetrieved = this.kafkaConsumer.poll(this.pollingTimeout);
                }
            } catch (WakeupException e) {
                // Ignore exception if connection is closing.
                if (!closed.get()) {
                    throw e;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Kafka service " + this.serviceId + " attached to consumer "
                                     + this.consumerId + " has received " + recordsRetrieved.count() + " records.");
            }
            processRetrievedRecords(recordsRetrieved);
        } catch (KafkaException | IllegalStateException | IllegalArgumentException e) {
            this.kafkaListener.onError(e);
            // When un-recoverable exception is thrown we stop scheduling task to the executor.
            // Later at stopConsume() on KafkaRecordConsumer we close the consumer.
            this.pollTaskFuture.cancel(false);
        }
    }

    private void processRetrievedRecords(ConsumerRecords consumerRecords) {
        if (Objects.nonNull(consumerRecords) && !consumerRecords.isEmpty()) {
            // When decoupleProcessing == 'true' Kafka records set will be dispatched and processed in
            // Parallel threads.
            // Otherwise dispatching and processing will have single threaded semantics.
            if (this.decoupleProcessing) {
                this.kafkaListener.onRecordsReceived(consumerRecords, kafkaConsumer, groupId);
            } else {
                Semaphore sem = new Semaphore(0);
                KafkaPollCycleFutureListener pollCycleListener = new KafkaPollCycleFutureListener(sem, serviceId);
                this.kafkaListener.onRecordsReceived(consumerRecords, kafkaConsumer, groupId, pollCycleListener);
                // We suspend execution of poll cycle here before moving to the next cycle.
                // Once we receive signal from BVM via KafkaPollCycleFutureListener this suspension is removed
                // We will move to the next polling cycle.
                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    this.kafkaListener.onError(e);
                    this.pollTaskFuture.cancel(false);
                }
            }
        }
    }

    /**
     * Starts Kafka consumer polling cycles, schedules thread pool for given polling cycle.
     */
    public void consume() {
        final Runnable pollingFunction = () -> poll();
        this.pollTaskFuture = this.executorService.scheduleAtFixedRate(pollingFunction, 0, this.pollingInterval,
                                                                       TimeUnit.MILLISECONDS);
    }

    /**
     * Returns current consumer id.
     *
     * @return consumer id integer.
     */
    public int getConsumerId() {
        return this.consumerId;
    }

    /**
     * Stops Kafka consumer polling cycles, shutdowns scheduled thread pool and closes the consumer instance.
     */
    public void stopConsume() {
        // Make closed true, therefore poll function stops polling, and make stop operation thread-safe
        closed.set(true);
        this.kafkaConsumer.wakeup();
        this.kafkaConsumer.close();
        this.executorService.shutdown();
    }
}
