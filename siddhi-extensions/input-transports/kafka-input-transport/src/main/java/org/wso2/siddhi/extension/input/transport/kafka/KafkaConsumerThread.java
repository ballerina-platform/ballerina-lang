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


public class KafkaConsumerThread implements Runnable {

    private final KafkaConsumer<byte[], byte[]> consumer;
    private SourceEventListener sourceEventListener;
    private static final Logger log = Logger.getLogger(KafkaConsumerThread.class);
    private static volatile boolean active = true;
    private String topics[];
    private HashMap <String, HashMap<Integer, Long>> topicOffsetMap = new HashMap<>();

    public KafkaConsumerThread(SourceEventListener sourceEventListener, String topics[], String partitions[],
                               Properties props, HashMap<String, HashMap<Integer, Long>> topicOffsetMap) {
        this.consumer = new KafkaConsumer<>(props);
        this.sourceEventListener = sourceEventListener;
        this.topicOffsetMap = topicOffsetMap;
        this.topics = topics;
        if(null != partitions) {
            List<TopicPartition> partitionsList = new ArrayList<>();
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
            if(null != topicOffsetMap) {
                for (String topic : topics) {
                    HashMap<Integer, Long> offsetMap = topicOffsetMap.get(topic);
                    if(null != offsetMap) {
                        for (Map.Entry<Integer, Long> entry : offsetMap.entrySet()) {
                            TopicPartition partition = new TopicPartition(topic, entry.getKey());
                            log.info("Seeking partition: " + partition + " for topic: " + topic);
                            consumer.seek(partition, entry.getValue());
                        }
                    }
                }
            }
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

    @Override
    public void run() {
        while (active) {
            //The time, in milliseconds, spent waiting in poll if data is not available. If 0, returns immediately with
            //any records that are available now. Must not be negative
            ConsumerRecords<byte[], byte[]> records = consumer.poll(200);
            for (ConsumerRecord record : records) {
                String evento = record.value().toString();
                if (log.isDebugEnabled()) {
                    log.debug("Event received in Kafka Event Adaptor: " + evento + ", offSet: " + record.offset() +
                            ", key: " + record.key() + ", topic: " + record.topic() + ", partition: " + record.partition());
                }
                topicOffsetMap.get(record.topic()).put(record.partition(), record.offset());
                sourceEventListener.onEvent(evento);
            }
            try {
                consumer.commitAsync();
            } catch (CommitFailedException e) {
                log.error("Kafka commit failed for topic/s" + Arrays.toString(topics), e);
            }
        }
        consumer.close();
    }

    public void shutdownConsumer() {
        active = false;
    }

    public HashMap <String, HashMap<Integer, Long>> getTopicOffsetMap() {
        return topicOffsetMap;
    }
}
