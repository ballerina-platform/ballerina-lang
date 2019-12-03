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

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.impl.KafkaTransactionContext;

import java.util.Objects;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TRANSACTION_CONTEXT;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaProducer;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.processKafkaProducerConfig;
import static org.ballerinalang.messaging.kafka.utils.TransactionUtils.createKafkaTransactionContext;

/**
 * Native action initializes a producer instance for connector.
 */
public class Init {

    public static Object init(ObjectValue producerObject, MapValue<String, Object> configs) {
        Properties producerProperties = processKafkaProducerConfig(configs);

        try {
            if (Objects.nonNull(producerProperties.get(ProducerConfig.TRANSACTIONAL_ID_CONFIG))) {
                if (!((boolean) producerProperties.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG))) {
                    throw new IllegalStateException("configuration enableIdempotence must be set to true to enable " +
                            "transactional producer");
                }
                createKafkaProducer(producerProperties, producerObject);
                KafkaTransactionContext transactionContext = createKafkaTransactionContext(producerObject);
                producerObject.addNativeData(TRANSACTION_CONTEXT, transactionContext);
            } else {
                createKafkaProducer(producerProperties, producerObject);
            }
        } catch (IllegalStateException | KafkaException e) {
            return createKafkaError("Failed to initialize the producer: " + e.getMessage(), PRODUCER_ERROR);
        }
        return null;
    }
}
