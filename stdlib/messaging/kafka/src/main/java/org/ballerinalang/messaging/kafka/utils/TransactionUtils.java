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

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.impl.KafkaTransactionContext;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONNECTOR_ID;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER_CONFIG;

/**
 * Utility functions for ballerina kafka transactions.
 */
public class TransactionUtils {

    private TransactionUtils() {
    }

    public static boolean isTransactional(Strand strand, Properties properties) {
        return (Objects.nonNull(properties.get(ProducerConfig.TRANSACTIONAL_ID_CONFIG)) && strand.isInTransaction());
    }

    public static boolean isTransactionInitiated(TransactionLocalContext localTransactionInfo, String connectorKey) {
        return Objects.nonNull(localTransactionInfo.getTransactionContext(connectorKey));
    }

    public static void commitKafkaConsumer(Strand strand, ObjectValue producerObject, Map<TopicPartition,
            OffsetAndMetadata> partitionToMetadataMap, String groupID) {

        Properties producerProperties = (Properties) producerObject.getNativeData(NATIVE_PRODUCER_CONFIG);
        KafkaProducer<byte[], byte[]> producer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        if (isTransactional(strand, producerProperties)) {
            initiateTransaction(strand, (String) producerObject.get(CONNECTOR_ID), producer);
        }
        producer.sendOffsetsToTransaction(partitionToMetadataMap, groupID);
    }
    public static void initiateTransaction(Strand strand, String connectorKey, KafkaProducer producer) {
        TransactionLocalContext localTransactionInfo = strand.getLocalTransactionContext();
        beginTransaction(localTransactionInfo, connectorKey, producer);
    }

    private static void beginTransaction(TransactionLocalContext localTransactionInfo, String connectorKey,
                                         KafkaProducer producer) {
        if (!isTransactionInitiated(localTransactionInfo, connectorKey)) {
            KafkaTransactionContext txContext = new KafkaTransactionContext(producer);
            localTransactionInfo.registerTransactionContext(connectorKey, txContext);
            producer.beginTransaction();
        }
    }
}
