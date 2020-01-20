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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ORG_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getStringListFromStringArrayValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateTopicPartitionRecord;

/**
 * Native function subscribes to given topic array with given function pointers to on revoked / on assigned events.
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "subscribeWithPartitionRebalance",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = CONSUMER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class SubscribeWithPartitionRebalance {

    public static Object subscribeWithPartitionRebalance(Strand strand, ObjectValue consumerObject, ArrayValue topics,
                                                         FPValue onPartitionsRevoked, FPValue onPartitionsAssigned) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        NonBlockingCallback callback = new NonBlockingCallback(strand);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        List<String> topicsList = getStringListFromStringArrayValue(topics);
        ConsumerRebalanceListener consumer = new KafkaRebalanceListener(strand, strand.scheduler, onPartitionsRevoked,
                                                                        onPartitionsAssigned, consumerObject);
        try {
            kafkaConsumer.subscribe(topicsList, consumer);
            Set<String> subscribedTopics = kafkaConsumer.subscription();
            KafkaMetricsUtil.reportBulkSubscription(consumerObject, subscribedTopics);
        } catch (IllegalArgumentException | IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject,
                                                 KafkaObservabilityConstants.ERROR_TYPE_SUBSCRIBE_PARTITION_REBALANCE);
            callback.setReturnValues(
                    createKafkaError("Failed to subscribe the consumer: " + e.getMessage(), CONSUMER_ERROR));
        }
        callback.notifySuccess();
        return null;
    }

    /**
     * Implementation for {@link ConsumerRebalanceListener} interface from connector side.
     * We register this listener at subscription.
     * <p>
     * {@inheritDoc}
     */
    static class KafkaRebalanceListener implements ConsumerRebalanceListener {

        private Strand strand;
        private Scheduler scheduler;
        private FPValue onPartitionsRevoked;
        private FPValue onPartitionsAssigned;
        private ObjectValue consumer;

        KafkaRebalanceListener(Strand strand,
                               Scheduler scheduler,
                               FPValue onPartitionsRevoked,
                               FPValue onPartitionsAssigned,
                               ObjectValue consumer) {
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
            this.scheduler.schedule(inputArgs, onPartitionsRevoked.getConsumer(), strand, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            Object[] inputArgs = {null, consumer, true, getPartitionsArray(partitions), true};
            this.scheduler.schedule(inputArgs, onPartitionsAssigned.getConsumer(), strand, null);
        }

        private BArray getPartitionsArray(Collection<TopicPartition> partitions) {
            BArray topicPartitionArray = BValueCreator.createArrayValue(
                    new BArrayType(getTopicPartitionRecord().getType()));
            partitions.forEach(partition -> {
                MapValue<String, Object> topicPartition = populateTopicPartitionRecord(partition.topic(),
                        partition.partition());
                topicPartitionArray.append(topicPartition);
            });
                                                                                       partition.partition());
                topicPartitionArray.add(i++, topicPartition);
            }
            return topicPartitionArray;
        }
    }
}
