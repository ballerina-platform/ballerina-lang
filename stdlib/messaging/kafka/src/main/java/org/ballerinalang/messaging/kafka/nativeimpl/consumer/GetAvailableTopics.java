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
import org.apache.kafka.common.PartitionInfo;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
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
 * Native function returns given partition assignment for consumer.
 */
public class GetAvailableTopics {

    private static final Logger logger = LoggerFactory.getLogger(GetAvailableTopics.class);

    public static Object getAvailableTopics(ObjectValue consumerObject, long duration) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        Properties consumerProperties = (Properties) consumerObject.getNativeData(NATIVE_CONSUMER_CONFIG);
        int defaultApiTimeout = getDefaultApiTimeout(consumerProperties);
        int apiTimeout = getIntFromLong(duration, logger, ALIAS_DURATION);
        Map<String, List<PartitionInfo>> topics;
        try {
            if (apiTimeout > DURATION_UNDEFINED_VALUE) {
                topics = getAvailableTopicWithDuration(kafkaConsumer, apiTimeout);
            } else if (defaultApiTimeout > DURATION_UNDEFINED_VALUE) {
                topics = getAvailableTopicWithDuration(kafkaConsumer, defaultApiTimeout);
            } else {
                topics = kafkaConsumer.listTopics();
            }
            return getBArrayFromMap(topics);
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_GET_TOPICS);
            return createKafkaError("Failed to retrieve available topics: " + e.getMessage(), CONSUMER_ERROR);
        }
    }

    private static Map<String, List<PartitionInfo>> getAvailableTopicWithDuration(
            KafkaConsumer kafkaConsumer, long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        return kafkaConsumer.listTopics(duration);
    }

    private static BArray getBArrayFromMap(Map<String, List<PartitionInfo>> map) {
        BArray bArray = BValueCreator.createArrayValue(new BArrayType(BTypes.typeString));
        if (!map.keySet().isEmpty()) {
            int i = 0;
            for (String topic : map.keySet()) {
                bArray.add(i++, topic);
            }
        }
        return bArray;
    }
}
