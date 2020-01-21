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
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DURATION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_TOPIC;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DURATION_UNDEFINED_VALUE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getDefaultApiTimeout;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntFromLong;

/**
 * Native function returns current position for give topic partition.
 */
public class GetPositionOffset {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPositionOffset.class);

    public static Object getPositionOffset(ObjectValue consumerObject,
                                           MapValue<String, Object> topicPartition,
                                           long duration) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        Properties consumerProperties = (Properties) consumerObject.getNativeData(NATIVE_CONSUMER_CONFIG);

        int defaultApiTimeout = getDefaultApiTimeout(consumerProperties);
        int apiTimeout = getIntFromLong(duration, LOGGER, ALIAS_DURATION);
        String topic = topicPartition.getStringValue(ALIAS_TOPIC);
        Long partition = topicPartition.getIntValue(ALIAS_PARTITION);
        TopicPartition tp = new TopicPartition(topic, getIntFromLong(partition, LOGGER, ALIAS_PARTITION));

        try {
            long position;
            if (apiTimeout > DURATION_UNDEFINED_VALUE) {
                position = getPositionWithDuration(kafkaConsumer, tp, apiTimeout);
            } else if (defaultApiTimeout > DURATION_UNDEFINED_VALUE) {
                position = getPositionWithDuration(kafkaConsumer, tp, defaultApiTimeout);
            } else {
                position = kafkaConsumer.position(tp);
            }
            return position;
        } catch (IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_GET_POSITION_OFFSET);
            return createKafkaError("Failed to retrieve position offset: " + e.getMessage(), CONSUMER_ERROR);
        }
    }

    private static long getPositionWithDuration(KafkaConsumer kafkaConsumer,
                                                TopicPartition topicPartition,
                                                long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        return kafkaConsumer.position(topicPartition, duration);
    }
}
