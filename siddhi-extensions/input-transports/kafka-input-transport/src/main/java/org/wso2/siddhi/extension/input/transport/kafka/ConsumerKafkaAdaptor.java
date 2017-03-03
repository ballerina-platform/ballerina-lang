/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.input.transport.kafka;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.stream.input.source.SourceCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerKafkaAdaptor {
    private final ConsumerConnector consumer;
    private final String topic;
    private ExecutorService executor;
    private static final Logger log = Logger.getLogger(ConsumerKafkaAdaptor.class);
    private int tenantId;

    public ConsumerKafkaAdaptor(String inTopic, ConsumerConfig conf) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(conf);
        this.topic = inTopic;
    }

    public void shutdown() {
        if (consumer != null) {
            consumer.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void run(int numThreads, SourceCallback sourceCallback) {
        try {
            Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
            topicCountMap.put(topic, numThreads);
            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
            List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
            // now launch all the threads
            executor = Executors.newFixedThreadPool(numThreads);
            // now create an object to consume the messages
            for (final KafkaStream stream : streams) {
                executor.submit(new KafkaConsumer(stream, sourceCallback));
            }
            log.info("Kafka Consumer started listening on topic: " + topic);
        } catch (Throwable t) {
            log.error("Error while creating KafkaConsumer for topic: " + topic, t);
        }
    }
}
