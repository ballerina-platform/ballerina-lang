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

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_BOOTSTRAP_SERVERS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_SERVERS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ORG_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.processKafkaConsumerConfig;

/**
 * Native function connects consumer to remote cluster.
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "connect",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = KafkaConstants.CONSUMER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class Connect {
    private static final PrintStream console = System.out;

    public static Object connect(Strand strand, ObjectValue consumerObject) {
        // Check whether already native consumer is attached to the struct.
        // This can be happen either from Kafka service or via programmatically.
        if (Objects.nonNull(consumerObject.getNativeData(NATIVE_CONSUMER))) {
            return createKafkaError("Kafka consumer is already connected to external broker. " +
                    "Please close it before re-connecting the external broker again.", CONSUMER_ERROR);
        }
        MapValue<String, Object> configs = (MapValue<String, Object>) consumerObject.get(CONSUMER_CONFIG_FIELD_NAME);
        Properties consumerProperties = processKafkaConsumerConfig(configs);
        try {
            KafkaConsumer<byte[], byte[]> kafkaConsumer = new KafkaConsumer<>(consumerProperties);
            consumerObject.addNativeData(NATIVE_CONSUMER, kafkaConsumer);
            consumerObject.addNativeData(NATIVE_CONSUMER_CONFIG, consumerProperties);
        } catch (KafkaException e) {
            return createKafkaError("Cannot connect to the kafka server: " + e.getMessage(), CONSUMER_ERROR);
        }
        console.println(KAFKA_SERVERS + configs.get(CONSUMER_BOOTSTRAP_SERVERS_CONFIG));
        return null;
    }
}
