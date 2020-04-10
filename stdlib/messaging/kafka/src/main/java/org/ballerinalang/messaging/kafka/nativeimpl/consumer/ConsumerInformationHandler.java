/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DURATION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DURATION_UNDEFINED_VALUE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getDefaultApiTimeout;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntFromLong;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionList;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateTopicPartitionRecord;

/**
 * Native methods to handle ballerina kafka consumer subscriptions.
 */
public class ConsumerInformationHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerInformationHandler.class);
    private static final BArrayType stringArrayType = new BArrayType(BTypes.typeString);

    /**
     * Assign ballerina kafka consumer to the given topic array.
     *
     * @param consumerObject  Kafka consumer object from ballerina.
     * @param topicPartitions Topic partition array to which the consumer need to be subscribed.
     * @return {@code ErrorValue}, if there's an error, null otherwise.
     */
    public static Object assign(ObjectValue consumerObject, BArray topicPartitions) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        List<TopicPartition> partitions = getTopicPartitionList(topicPartitions, logger);
        try {
            kafkaConsumer.assign(partitions);
        } catch (IllegalArgumentException | IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_ASSIGN);
            return createKafkaError("Failed to assign topics for the consumer: " + e.getMessage(), CONSUMER_ERROR);
        }
        return null;
    }

    /**
     * Get the current assignment of the ballerina kafka consumer.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @return Topic partition ballerina array. If there's an error in the process {@code ErrorValue} is returned.
     */
    public static Object getAssignment(ObjectValue consumerObject) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        BArray topicPartitionArray =
                BValueCreator.createArrayValue(new BArrayType(getTopicPartitionRecord().getType()));
        try {
            Set<TopicPartition> topicPartitions = kafkaConsumer.assignment();
            for (TopicPartition partition : topicPartitions) {
                MapValue<String, Object> tp = populateTopicPartitionRecord(partition.topic(), partition.partition());
                topicPartitionArray.append(tp);
            }
            return topicPartitionArray;
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_GET_ASSIGNMENT);
            return createKafkaError("Failed to retrieve assignment for the consumer: " + e.getMessage(),
                                    CONSUMER_ERROR);
        }
    }

    /**
     * Get the available topics from the kafka broker for the ballerina kafka consumer.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @param duration       Duration in milliseconds to try the operation.
     * @return Array of ballerina strings, which consists of the available topics.
     */
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

    /**
     * Get the currently paused partitions for given consumer.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @return Array of ballerina strings, which consists of the paused topics.
     */
    public static Object getPausedPartitions(ObjectValue consumerObject) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        BArray topicPartitionArray =
                BValueCreator.createArrayValue(new BArrayType(getTopicPartitionRecord().getType()));
        try {
            Set<TopicPartition> pausedPartitions = kafkaConsumer.paused();
            for (TopicPartition partition : pausedPartitions) {
                MapValue<String, Object> tp = populateTopicPartitionRecord(partition.topic(), partition.partition());
                topicPartitionArray.append(tp);
            }
            return topicPartitionArray;
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_GET_PAUSED_PARTITIONS);
            return createKafkaError("Failed to retrieve paused partitions: " + e.getMessage(), CONSUMER_ERROR);
        }
    }

    /**
     * Get the topic partition data for the given topic.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @param topic          Topic, of which the data is needed.
     * @param duration       Duration in milliseconds to try the operation.
     * @return Topic partition array of the given topic.
     */
    public static Object getTopicPartitions(ObjectValue consumerObject, String topic, long duration) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        Properties consumerProperties = (Properties) consumerObject.getNativeData(NATIVE_CONSUMER_CONFIG);

        int defaultApiTimeout = getDefaultApiTimeout(consumerProperties);
        int apiTimeout = getIntFromLong(duration, logger, ALIAS_DURATION);

        try {
            List<PartitionInfo> partitionInfoList;
            if (apiTimeout > DURATION_UNDEFINED_VALUE) {
                partitionInfoList = getPartitionInfoList(kafkaConsumer, topic, apiTimeout);
            } else if (defaultApiTimeout > DURATION_UNDEFINED_VALUE) {
                partitionInfoList = getPartitionInfoList(kafkaConsumer, topic, defaultApiTimeout);
            } else {
                partitionInfoList = kafkaConsumer.partitionsFor(topic);
            }
            BArray topicPartitionArray =
                    BValueCreator.createArrayValue(new BArrayType(getTopicPartitionRecord().getType()));
            for (PartitionInfo info : partitionInfoList) {
                MapValue<String, Object> partition = populateTopicPartitionRecord(info.topic(), info.partition());
                topicPartitionArray.append(partition);
            }
            return topicPartitionArray;
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_GET_TOPIC_PARTITIONS);
            return KafkaUtils.createKafkaError("Failed to retrieve topic partitions for the consumer: "
                                                       + e.getMessage(), CONSUMER_ERROR);
        }
    }

    /**
     * Get the currently subscribed topics of the ballerina kafka consumer.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @return Array of ballerina strings, which consists of the subscribed topics.
     */
    public static Object getSubscription(ObjectValue consumerObject) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);

        try {
            Set<String> subscriptions = kafkaConsumer.subscription();
            BArray arrayValue = BValueCreator.createArrayValue(stringArrayType);
            if (!subscriptions.isEmpty()) {
                for (String subscription : subscriptions) {
                    arrayValue.append(subscription);
                }
            }
            return arrayValue;
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_GET_SUBSCRIPTION);
            return createKafkaError("Failed to retrieve subscribed topics: " + e.getMessage(), CONSUMER_ERROR);
        }
    }

    private static Map<String, List<PartitionInfo>> getAvailableTopicWithDuration(KafkaConsumer kafkaConsumer,
                                                                                  long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        return kafkaConsumer.listTopics(duration);
    }

    private static BArray getBArrayFromMap(Map<String, List<PartitionInfo>> map) {
        BArray bArray = BValueCreator.createArrayValue(new BArrayType(BTypes.typeString));
        if (!map.keySet().isEmpty()) {
            for (String topic : map.keySet()) {
                bArray.append(topic);
            }
        }
        return bArray;
    }

    @SuppressWarnings(UNCHECKED)
    private static List<PartitionInfo> getPartitionInfoList(KafkaConsumer kafkaConsumer, String topic, long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        return (List<PartitionInfo>) kafkaConsumer.partitionsFor(topic, duration);
    }
}
