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

package org.ballerinalang.messaging.kafka.service;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.api.KafkaListener;
import org.ballerinalang.messaging.kafka.api.KafkaServerConnector;
import org.ballerinalang.messaging.kafka.exceptions.KafkaConnectorException;
import org.ballerinalang.messaging.kafka.impl.KafkaListenerImpl;
import org.ballerinalang.messaging.kafka.impl.KafkaServerConnectorImpl;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;

import java.util.Objects;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SERVER_CONNECTOR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;

/**
 * This is used to register a listener to the kafka service.
 */
public class Register {

    @SuppressWarnings(UNCHECKED)
    public static Object register(ObjectValue listener, ObjectValue service, Object name) {
        Strand strand = Scheduler.getStrand();
        MapValue<String, Object> listenerConfigurations = listener.getMapValue(CONSUMER_CONFIG_FIELD_NAME);
        Properties configs = KafkaUtils.processKafkaConsumerConfig(listenerConfigurations);

        try {
            KafkaConsumer kafkaConsumer = null;
            if (Objects.nonNull(listener.getNativeData(NATIVE_CONSUMER))) {
                kafkaConsumer = (KafkaConsumer) listener.getNativeData(NATIVE_CONSUMER);
            }
            KafkaListener kafkaListener = new KafkaListenerImpl(strand, listener, service);
            String serviceId = service.getType().getQualifiedName();
            KafkaServerConnector serverConnector = new KafkaServerConnectorImpl(serviceId, configs, kafkaListener,
                    kafkaConsumer);
            listener.addNativeData(SERVER_CONNECTOR, serverConnector);
        } catch (KafkaConnectorException e) {
            return KafkaUtils.createKafkaError(e.getMessage(), CONSUMER_ERROR);
        }
        return null;
    }
}
