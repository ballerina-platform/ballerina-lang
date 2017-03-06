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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class KafkaConsumerThread implements Runnable {

    private final KafkaConsumer<byte[], byte[]> consumer;
    private String evento;
    private SourceEventListener sourceEventListener;
    private static final Logger log = Logger.getLogger(KafkaConsumerThread.class);


    public KafkaConsumerThread(SourceEventListener sourceEventListener, String topic, String partitionList, Properties props) {
        this.consumer = new KafkaConsumer<>(props);
        this.sourceEventListener = sourceEventListener;
        String partitions[] = partitionList.split(",");
        List<TopicPartition> partitionsList = new ArrayList<>();
        for (String partition1 : partitions) {
            TopicPartition partition = new TopicPartition(topic, Integer.parseInt(partition1));
            partitionsList.add(partition);
        }
        consumer.assign(partitionsList);
    }

    @Override
    public void run() {
        log.info("Kafka listening thread started.");
        while (true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(200);
            for (ConsumerRecord record : records) {
                evento = record.value().toString();
                if (log.isDebugEnabled()) {
                    log.debug("Event received in Kafka Event Adaptor: " + evento + ", offSet: " + record.offset() + ", key: " + record.key() + ", partition: " + record.partition());
                }
                sourceEventListener.onEvent(evento);
            }
        }

    }
}
