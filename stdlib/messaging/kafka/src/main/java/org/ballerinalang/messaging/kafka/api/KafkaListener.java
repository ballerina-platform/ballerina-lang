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

package org.ballerinalang.messaging.kafka.api;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.ballerinalang.messaging.kafka.impl.KafkaPollCycleFutureListener;

/**
 * This interface defines listener which can be registered, to retrieve Kafka records returned from single poll cycle.
 */
public interface KafkaListener {

    /**
     * For each poll cycle, it will trigger invocation to this method dispatching polled kafka records.
     *
     * @param records       Kafka records
     * @param kafkaConsumer consumer on which poll is called upon
     * @param groupId       ID of the consumer group in which the consumer belongs
     */
    void onRecordsReceived(ConsumerRecords records, KafkaConsumer kafkaConsumer, String groupId);

    /**
     * For each poll cycle, it will trigger invocation to this method dispatching polled kafka records.
     *
     * @param records       Kafka records
     * @param kafkaConsumer consumer on which poll is called upon
     * @param listener      which control the flow of poll cycle
     * @param groupID       ID of the consumer group in which the consumer belongs
     */
    void onRecordsReceived(ConsumerRecords records,
                           KafkaConsumer kafkaConsumer,
                           String groupID,
                           KafkaPollCycleFutureListener listener);

    /**
     * If there are errors, Kafka connector will trigger this method.
     *
     * @param throwable contains the error details of the event.
     */
    void onError(Throwable throwable);

}
