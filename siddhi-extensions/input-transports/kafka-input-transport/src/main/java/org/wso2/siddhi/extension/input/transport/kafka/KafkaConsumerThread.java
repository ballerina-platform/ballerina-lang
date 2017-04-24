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

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class KafkaConsumerThread implements Runnable {

    private final KafkaConsumer<byte[], byte[]> consumer;
    private SourceEventListener sourceEventListener;
    private static final Logger log = Logger.getLogger(KafkaConsumerThread.class);
    private String topics[];
    private Map<String, Map<Integer, Long>> topicOffsetMap = new HashMap<>();
    private volatile boolean paused;
    private volatile boolean inactive;
    // KafkaConsumer is not thread safe, hence we need a lock
    private final Lock consumerLock = new ReentrantLock();
    private List<TopicPartition> partitionsList = new ArrayList<>();

    public KafkaConsumerThread(SourceEventListener sourceEventListener, String topics[], String partitions[],
                               Properties props, Map<String, Map<Integer, Long>> topicOffsetMap) {
        this.consumer = new KafkaConsumer<>(props);
        this.sourceEventListener = sourceEventListener;
        this.topicOffsetMap = topicOffsetMap;
        this.topics = topics;
        if(null != partitions) {
            for (String topic : topics) {
                if(null == topicOffsetMap.get(topic)) {
                    this.topicOffsetMap.put(topic, new HashMap<>());
                }
                for (String partition1 : partitions) {
                    TopicPartition partition = new TopicPartition(topic, Integer.parseInt(partition1));
                    partitionsList.add(partition);
                }
                log.info("Adding partitions " + Arrays.toString(partitions) + " for topic: " + topic);
                consumer.assign(partitionsList);
            }
            restore(topicOffsetMap);
        } else {
            for (String topic : topics) {
                if (null == topicOffsetMap.get(topic)) {
                    this.topicOffsetMap.put(topic, new HashMap<>());
                }
            }
            consumer.subscribe(Arrays.asList(topics));
        }
        log.info("Subscribed for topics: " + Arrays.toString(topics));
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        restore(topicOffsetMap);
        paused = false;
    }

    public void restore(Map<String, Map<Integer, Long>> topicOffsetMap) {
        final Lock consumerLock = this.consumerLock;
        if (null != topicOffsetMap) {
            for (String topic : topics) {
                Map<Integer, Long> offsetMap = topicOffsetMap.get(topic);
                if (null != offsetMap) {
                    for (Map.Entry<Integer, Long> entry : offsetMap.entrySet()) {
                        TopicPartition partition = new TopicPartition(topic, entry.getKey());
                        if (partitionsList.contains(partition)) {
                            log.info("Seeking partition: " + partition + " for topic: " + topic);
                            try {
                                consumerLock.lock();
                                consumer.seek(partition, entry.getValue() + 1);
                            } finally {
                                consumerLock.unlock();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        final Lock consumerLock = this.consumerLock;
        while (!inactive) {
            while (!paused) {
                // The time, in milliseconds, spent waiting in poll if data is not available. If 0, returns
                // immediately with any records that are available now. Must not be negative
                ConsumerRecords<byte[], byte[]> records;
                try {
                    consumerLock.lock();
                    records = consumer.poll(100);
                } finally {
                    consumerLock.unlock();
                }
                for (ConsumerRecord record : records) {
                    String event = record.value().toString();
                    if (log.isDebugEnabled()) {
                        log.debug("Event received in Kafka Event Adaptor: " + event + ", offSet: " + record.offset()
                                + ", key: " + record.key() + ", topic: " + record.topic() + ", partition: " + record
                                .partition());
                    }
                    topicOffsetMap.get(record.topic()).put(record.partition(), record.offset());
                    sourceEventListener.onEvent(event);
                }
                try {
                    consumerLock.lock();
                    if (!records.isEmpty()) {
                        consumer.commitAsync();
                    }
                } catch (CommitFailedException e) {
                    log.error("Kafka commit failed for topic kafka_result_topic", e);
                } finally {
                    consumerLock.unlock();
                }
            }
        }
        consumerLock.lock();
        consumer.close();
        consumerLock.unlock();
    }

    public void shutdownConsumer() {
        inactive = true;
    }

    public Map<String, Map<Integer, Long>> getTopicOffsetMap() {
        return topicOffsetMap;
    }
}
