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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.util.*;
import java.util.concurrent.*;

public class ConsumerKafkaGroup {
    private final String topics[];
    private final String partitions[];
    private final Properties props;
    private List<KafkaConsumerThread> kafkaConsumerThreadList = new ArrayList<>();
    private Map<String, Map<Integer, Long>> topicOffsetMap = new HashMap<>();
    private ScheduledExecutorService executorService;
    private String threadingOption;
    private static final Logger log = Logger.getLogger(ConsumerKafkaGroup.class);

    public ConsumerKafkaGroup(String topics[], String partitions[], Properties props, Map<String, Map<Integer, Long>>
            topicOffsetMap, String threadingOption, ScheduledExecutorService executorService) {
        this.threadingOption = threadingOption;
        this.topicOffsetMap = topicOffsetMap;
        this.topics = topics;
        this.partitions = partitions;
        this.props = props;
        this.executorService = executorService;
    }

    public void pause() {
        kafkaConsumerThreadList.forEach(KafkaConsumerThread::pause);
    }

    public void resume() {
        kafkaConsumerThreadList.forEach(KafkaConsumerThread::resume);
    }

    public void restore(final Map<String, Map<Integer, Long>> topic) {
        kafkaConsumerThreadList.forEach(kafkaConsumerThread -> kafkaConsumerThread.restore(topic));
    }

    public void shutdown() {
        kafkaConsumerThreadList.forEach(KafkaConsumerThread::shutdownConsumer);
    }

    public void run(SourceEventListener sourceEventListener) {
        try {
            if(KafkaInputTransport.SINGLE_THREADED.equals(threadingOption)) {
                KafkaConsumerThread kafkaConsumerThread =
                        new KafkaConsumerThread(sourceEventListener, topics, partitions, props, topicOffsetMap);
                kafkaConsumerThreadList.add(kafkaConsumerThread);
                log.info("Kafka Consumer thread starting to listen on topic/s: " + Arrays.toString(topics) +
                        " with partition/s: " + Arrays.toString(partitions));
                kafkaConsumerThread.run();
            } else if (KafkaInputTransport.TOPIC_WISE.equals(threadingOption)) {
                for (String topic : topics) {
                    KafkaConsumerThread kafkaConsumerThread =
                            new KafkaConsumerThread(sourceEventListener, new String[]{topic}, partitions, props,
                                                    topicOffsetMap);
                    kafkaConsumerThreadList.add(kafkaConsumerThread);
                    executorService.submit(kafkaConsumerThread);
                    log.info("Kafka Consumer thread starting to listen on topic: " + topic +
                            " with partition/s: " + Arrays.toString(partitions));
                }
            } else if (KafkaInputTransport.PARTITION_WISE.equals(threadingOption)) {
                for (String topic : topics) {
                    for (String partition : partitions) {
                        KafkaConsumerThread kafkaConsumerThread =
                                new KafkaConsumerThread(sourceEventListener, new String[]{topic},
                                                        new String[]{partition}, props, topicOffsetMap);
                        kafkaConsumerThreadList.add(kafkaConsumerThread);
                        executorService.submit(kafkaConsumerThread);
                        log.info("Kafka Consumer thread starting to listen on topic: " + topic +
                                " with partition: " + partition);
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Error while creating KafkaConsumerThread for topic/s: " + Arrays.toString(topics), t);
        }
    }

    public Map<String, Map<Integer, Long>> getTopicOffsetMap() {
        Map<String, Map<Integer, Long>> topicOffsetMap = new HashMap<>();
        for (KafkaConsumerThread kafkaConsumerThread : kafkaConsumerThreadList) {
            Map<String, Map<Integer, Long>> topicOffsetMapTemp = kafkaConsumerThread.getTopicOffsetMap();
            for (Map.Entry<String, Map<Integer, Long>> entry : topicOffsetMapTemp.entrySet()) {
                topicOffsetMap.put(entry.getKey(), entry.getValue());
            }
        }
        return topicOffsetMap;
    }
}