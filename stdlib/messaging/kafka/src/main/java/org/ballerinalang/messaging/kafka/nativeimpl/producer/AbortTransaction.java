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
import org.apache.kafka.common.KafkaException;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.impl.KafkaTransactionContext;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONNECTOR_ID;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ORG_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createError;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.isTransactionInitiated;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.isTransactional;

/**
 * Native action aborts an ongoing transaction for the provided producer.
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "abortTransaction",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = CONSUMER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class AbortTransaction {

    public static Object abortTransaction(Strand strand, ObjectValue producerObject) {
        String connectorKey = producerObject.getStringValue(CONNECTOR_ID);
        TransactionLocalContext localContext = strand.getLocalTransactionContext();
        KafkaProducer<byte[], byte[]> producer = (KafkaProducer) producerObject.getNativeData(NATIVE_PRODUCER);
        Properties producerProperties = (Properties) producerObject.getNativeData(NATIVE_PRODUCER_CONFIG);
        try {
            if (isTransactional(strand, producerProperties)) {
                if (isTransactionInitiated(localContext, connectorKey)) {
                    KafkaTransactionContext transactionContext = new KafkaTransactionContext(producer);
                    transactionContext.rollback();
                }
            }
            return null;
        } catch (KafkaException e) {
            return createError("Failed to abort the transaction: " + e.getMessage(), PRODUCER_ERROR);
        }
    }
}
