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

package org.ballerinalang.messaging.kafka.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.impl.KafkaTransactionContext;

import java.util.Objects;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONNECTOR_ID;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TRANSACTION_CONTEXT;

/**
 * Utility functions for ballerina kafka transactions.
 */
public class TransactionUtils {

    private TransactionUtils() {
    }

    public static void handleTransactions(Strand strand, ObjectValue producer) {
        if (Objects.nonNull(producer.getNativeData(TRANSACTION_CONTEXT))) {
            KafkaTransactionContext transactionContext = (KafkaTransactionContext) producer
                    .getNativeData(TRANSACTION_CONTEXT);
            transactionContext.beginTransaction();
            registerKafkaTransactionContext(strand, producer, transactionContext);
        }
        // Do nothing if this is non-transactional producer.
    }

    public static KafkaTransactionContext createKafkaTransactionContext(ObjectValue producer) {
        KafkaProducer kafkaProducer = (KafkaProducer) producer.getNativeData(NATIVE_PRODUCER);
        return new KafkaTransactionContext(kafkaProducer);
    }

    public static void registerKafkaTransactionContext(Strand strand,
                                                       ObjectValue producer,
                                                       KafkaTransactionContext transactionContext) {
        String connectorId = producer.getStringValue(CONNECTOR_ID).getValue();
        if (Objects.isNull(strand.currentTrxContext.getTransactionContext(connectorId))) {
            TransactionLocalContext transactionLocalContext = strand.currentTrxContext;
            transactionLocalContext.registerTransactionContext(connectorId, transactionContext);
        }
    }
}
