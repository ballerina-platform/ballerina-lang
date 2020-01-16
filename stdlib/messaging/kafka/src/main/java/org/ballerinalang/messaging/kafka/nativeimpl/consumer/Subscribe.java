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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;

import java.io.PrintStream;
import java.util.List;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SUBSCRIBED_TOPICS;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getStringListFromStringArrayValue;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicNamesString;

/**
 * Native function subscribes consumer to given set of topic array.
 */
public class Subscribe {
    private static final PrintStream console = System.out;

    public static Object subscribe(ObjectValue consumerObject, ArrayValue topics) {
        KafkaConsumer kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        List<String> topicsList = getStringListFromStringArrayValue(topics);
        try {
            kafkaConsumer.subscribe(topicsList);
        } catch (IllegalArgumentException | IllegalStateException | KafkaException e) {
            return createKafkaError("Failed to subscribe to the provided topics: " + e.getMessage(), CONSUMER_ERROR);
        }
        console.println(SUBSCRIBED_TOPICS + getTopicNamesString(topicsList));
        return null;
    }
}

