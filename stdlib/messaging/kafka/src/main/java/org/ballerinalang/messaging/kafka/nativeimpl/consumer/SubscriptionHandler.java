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

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.META_DATA_ON_PARTITION_ASSIGNED;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.META_DATA_ON_PARTITION_REVOKED;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getStringListFromStringBArray;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicNamesString;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateTopicPartitionRecord;

/**
 * Native methods to handle subscription of the ballerina kafka consumer.
 */
public class SubscriptionHandler {
    private static final PrintStream console = System.out;

    /**
     * Subscribe the ballerina kafka consumer to the given array of topics.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @param topics         Ballerina {@code string[]} of topics.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object subscribe(ObjectValue consumerObject, BArray topics) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        List<String> topicsList = getStringListFromStringBArray(topics);
        try {
            kafkaConsumer.subscribe(topicsList);
            Set<String> subscribedTopics = kafkaConsumer.subscription();
            KafkaMetricsUtil.reportBulkSubscription(consumerObject, subscribedTopics);
        } catch (IllegalArgumentException | IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_SUBSCRIBE);
            return createKafkaError("Failed to subscribe to the provided topics: " + e.getMessage(), CONSUMER_ERROR);
        }
        console.println(KafkaConstants.SUBSCRIBED_TOPICS + getTopicNamesString(topicsList));
        return null;
    }

    /**
     * Subscribes the ballerina kafka consumer to the topics matching the given regex.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @param topicRegex     Regex to match topics to subscribe.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object subscribeToPattern(ObjectValue consumerObject, String topicRegex) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        try {
            kafkaConsumer.subscribe(Pattern.compile(topicRegex));
            Set<String> topicsList = kafkaConsumer.subscription();
            KafkaMetricsUtil.reportBulkSubscription(consumerObject, topicsList);
        } catch (IllegalArgumentException | IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_SUBSCRIBE_PATTERN);
            return createKafkaError("Failed to unsubscribe from the topics: " + e.getMessage(), CONSUMER_ERROR);
        }
        return null;
    }

    /**
     * Subscribes the ballerina kafka consumer and re-balances the assignments.
     *
     * @param consumerObject       Kafka consumer object from ballerina.
     * @param topics               Ballerina {@code string[]} of topics.
     * @param onPartitionsRevoked  Function pointer to invoke if partitions are revoked.
     * @param onPartitionsAssigned Function pointer to invoke if partitions are assigned.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object subscribeWithPartitionRebalance(ObjectValue consumerObject, BArray topics,
                                                         FPValue onPartitionsRevoked, FPValue onPartitionsAssigned) {
        Strand strand = Scheduler.getStrand();
        KafkaTracingUtil.traceResourceInvocation(strand, consumerObject);
        NonBlockingCallback callback = new NonBlockingCallback(strand);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        List<String> topicsList = getStringListFromStringBArray(topics);
        ConsumerRebalanceListener consumer = new SubscriptionHandler.KafkaRebalanceListener(strand, strand.scheduler,
                                                                                            onPartitionsRevoked,
                                                                                            onPartitionsAssigned,
                                                                                            consumerObject);
        try {
            kafkaConsumer.subscribe(topicsList, consumer);
            Set<String> subscribedTopics = kafkaConsumer.subscription();
            KafkaMetricsUtil.reportBulkSubscription(consumerObject, subscribedTopics);
        } catch (IllegalArgumentException | IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_SUBSCRIBE_PARTITION_REBALANCE);
            callback.notifyFailure(createKafkaError("Failed to subscribe the consumer: " + e.getMessage(),
                                                    CONSUMER_ERROR));
        }
        callback.notifySuccess();
        return null;
    }

    /**
     * Unsubscribe the ballerina kafka consumer from all the topics.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object unsubscribe(ObjectValue consumerObject) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        try {
            Set<String> topics = kafkaConsumer.subscription();
            kafkaConsumer.unsubscribe();
            KafkaMetricsUtil.reportBulkUnsubscription(consumerObject, topics);
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_UNSUBSCRIBE);
            return createKafkaError("Failed to unsubscribe the consumer: " + e.getMessage(), CONSUMER_ERROR);
        }
        return null;
    }

    /**
     * Implementation for {@link ConsumerRebalanceListener} interface from connector side. We register this listener at
     * subscription.
     * <p>
     * {@inheritDoc}
     */
    static class KafkaRebalanceListener implements ConsumerRebalanceListener {

        private Strand strand;
        private Scheduler scheduler;
        private FPValue onPartitionsRevoked;
        private FPValue onPartitionsAssigned;
        private ObjectValue consumer;

        KafkaRebalanceListener(Strand strand, Scheduler scheduler, FPValue onPartitionsRevoked,
                               FPValue onPartitionsAssigned, ObjectValue consumer) {
            this.strand = strand;
            this.scheduler = scheduler;
            this.onPartitionsRevoked = onPartitionsRevoked;
            this.onPartitionsAssigned = onPartitionsAssigned;
            this.consumer = consumer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            Object[] inputArgs = {null, consumer, true, getPartitionsArray(partitions), true};
            this.scheduler.schedule(inputArgs, onPartitionsRevoked.getConsumer(), strand, null, null,
                                    META_DATA_ON_PARTITION_REVOKED);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            Object[] inputArgs = {null, consumer, true, getPartitionsArray(partitions), true};
            this.scheduler.schedule(inputArgs, onPartitionsAssigned.getConsumer(), strand, null, null,
                                    META_DATA_ON_PARTITION_ASSIGNED);
        }

        private BArray getPartitionsArray(Collection<TopicPartition> partitions) {
            BArray topicPartitionArray = BValueCreator.createArrayValue(
                    new BArrayType(getTopicPartitionRecord().getType()));
            for (TopicPartition partition : partitions) {
                MapValue<BString, Object> topicPartition = populateTopicPartitionRecord(partition.topic(),
                                                                                        partition.partition());
                topicPartitionArray.append(topicPartition);
            }
            return topicPartitionArray;
        }
    }
}
