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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.ballerinalang.jvm.transactions.BallerinaTransactionContext;

import javax.transaction.xa.XAResource;

/**
 * {@code KafkaTransactionContext} Transaction context for Kafka transactions.
 */
public class KafkaTransactionContext implements BallerinaTransactionContext {

    private KafkaProducer<byte[], byte[]> kafkaProducer;

    public KafkaTransactionContext(KafkaProducer<byte[], byte[]> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Commits transaction for the producer.
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        // Kafka exception should be handled at the place where commit is called, as we should return an error there.
        this.kafkaProducer.commitTransaction();
    }

    /**
     * Aborts transaction for the producer.
     * {@inheritDoc}
     */
    @Override
    public void rollback() {
        // Kafka exception should be handled at the place where rollback is called, as we should return an error there.
        this.kafkaProducer.abortTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // Not required for Kafka Transactions.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XAResource getXAResource() {
        return null;
    }

}
