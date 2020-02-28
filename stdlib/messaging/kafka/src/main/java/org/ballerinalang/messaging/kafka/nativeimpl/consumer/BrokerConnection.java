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
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.messaging.kafka.observability.KafkaMetricsUtil;
import org.ballerinalang.messaging.kafka.observability.KafkaObservabilityConstants;
import org.ballerinalang.messaging.kafka.observability.KafkaTracingUtil;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DURATION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.BOOTSTRAP_SERVERS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_BOOTSTRAP_SERVERS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DURATION_UNDEFINED_VALUE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_SERVERS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getClientIdFromProperties;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getDefaultApiTimeout;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntFromLong;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionList;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.processKafkaConsumerConfig;

/**
 * Native methods to handle the connection between Ballerina Kafka Consumer and the Kafka Broker.
 */
public class BrokerConnection {

    private static final Logger logger = LoggerFactory.getLogger(BrokerConnection.class);
    private static final PrintStream console = System.out;

    /**
     * Closes the connection between ballerina kafka consumer and the kafka broker.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @param duration       Duration in milliseconds to try the operation.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object close(ObjectValue consumerObject, long duration) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
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
            KafkaMetricsUtil.reportConsumerClose(consumerObject);
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_CLOSE);
            return createKafkaError("Failed to close the connection from Kafka server: " + e.getMessage(),
                                    CONSUMER_ERROR);
        }
        return null;
    }

    /**
     * Connects ballerina kafka consumer to a kafka broker.
     *
     * @param consumerObject Kafka consumer object from ballerina.
     * @return {@code ErrorValue}, if there's an error, null otherwise.
     */
    public static Object connect(ObjectValue consumerObject) {
        // Check whether already native consumer is attached to the struct.
        // This can be happen either from Kafka service or via programmatically.
        if (Objects.nonNull(consumerObject.getNativeData(NATIVE_CONSUMER))) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_CONNECTION);
            return createKafkaError(
                    "Kafka consumer is already connected to external broker. Please close it before re-connecting " +
                            "the external broker again.", CONSUMER_ERROR);
        }
        MapValue<String, Object> configs = (MapValue<String, Object>) consumerObject.get(CONSUMER_CONFIG_FIELD_NAME);
        Properties consumerProperties = processKafkaConsumerConfig(configs);
        try {
            KafkaConsumer kafkaConsumer = new KafkaConsumer<>(consumerProperties);
            consumerObject.addNativeData(NATIVE_CONSUMER, kafkaConsumer);
            consumerObject.addNativeData(NATIVE_CONSUMER_CONFIG, consumerProperties);
            consumerObject.addNativeData(BOOTSTRAP_SERVERS, consumerProperties.getProperty(BOOTSTRAP_SERVERS));
            consumerObject.addNativeData(KafkaConstants.CLIENT_ID, getClientIdFromProperties(consumerProperties));
            KafkaMetricsUtil.reportNewConsumer(consumerObject);
        } catch (KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_CONNECTION);
            return createKafkaError("Cannot connect to the kafka server: " + e.getMessage(), CONSUMER_ERROR);
        }
        console.println(KAFKA_SERVERS + configs.get(CONSUMER_BOOTSTRAP_SERVERS_CONFIG));
        return null;
    }

    /**
     * Pauses ballerina kafka consumer connection with the kafka broker.
     *
     * @param consumerObject  Kafka consumer object from ballerina.
     * @param topicPartitions Topic Partitions which needed to be paused.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object pause(ObjectValue consumerObject, BArray topicPartitions) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        ArrayList<TopicPartition> partitionList = getTopicPartitionList(topicPartitions, logger);

        try {
            kafkaConsumer.pause(partitionList);
        } catch (IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_PAUSE);
            return createKafkaError("Failed to pause topic partitions for the consumer: " + e.getMessage(),
                                    CONSUMER_ERROR);
        }
        return null;
    }

    /**
     * Resumes a paused connection between ballerina kafka consumer and kafka broker.
     *
     * @param consumerObject  Kafka consumer object from ballerina.
     * @param topicPartitions Topic Partitions which are currently paused and needed to be resumed.
     * @return {@code ErrorValue}, if there's any error, null otherwise.
     */
    public static Object resume(ObjectValue consumerObject, BArray topicPartitions) {
        KafkaTracingUtil.traceResourceInvocation(Scheduler.getStrand(), consumerObject);
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        ArrayList<TopicPartition> partitionList = getTopicPartitionList(topicPartitions, logger);

        try {
            kafkaConsumer.resume(partitionList);
        } catch (IllegalStateException | KafkaException e) {
            KafkaMetricsUtil.reportConsumerError(consumerObject, KafkaObservabilityConstants.ERROR_TYPE_RESUME);
            return createKafkaError("Failed to resume topic partitions for the consumer: " + e.getMessage(),
                                    CONSUMER_ERROR);
        }
        return null;
    }

    private static void closeWithDuration(KafkaConsumer kafkaConsumer, long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        kafkaConsumer.close(duration);
    }
}
