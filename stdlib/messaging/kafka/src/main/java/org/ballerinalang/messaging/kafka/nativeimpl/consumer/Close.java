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
import org.ballerinalang.jvm.values.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DURATION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DURATION_UNDEFINED_VALUE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getDefaultApiTimeout;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntFromLong;

/**
 * Native function to close a given consumer.
 */
public class Close {

    private static final Logger logger = LoggerFactory.getLogger(Close.class);

    public static Object close(ObjectValue consumerObject, long duration) {
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        Properties consumerProperties = (Properties) consumerObject.getNativeData(NATIVE_CONSUMER_CONFIG);
        int defaultApiTimeout = getDefaultApiTimeout(consumerProperties);
        int apiTimeout = getIntFromLong(duration, logger, ALIAS_DURATION);
        try {
            if (apiTimeout > DURATION_UNDEFINED_VALUE) { // API timeout should given the priority over the default value
                closeWithDuration(kafkaConsumer, apiTimeout);
            } else if (defaultApiTimeout > DURATION_UNDEFINED_VALUE) {
                closeWithDuration(kafkaConsumer, defaultApiTimeout);
            } else {
                kafkaConsumer.close();
            }
        } catch (KafkaException e) {
            return createKafkaError("Failed to close the connection from Kafka server: " + e.getMessage(),
                    CONSUMER_ERROR);
        }
        return null;
    }

    private static void closeWithDuration(KafkaConsumer kafkaConsumer, long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        kafkaConsumer.close(duration);
    }
}
