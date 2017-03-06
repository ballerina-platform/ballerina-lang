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
import org.wso2.siddhi.core.stream.input.source.SourceCallback;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerKafkaGroup {
    private final String topic;
    private final String partitionList;
    private final Properties props;
    private ExecutorService executor;
    private static final Logger log = Logger.getLogger(ConsumerKafkaGroup.class);

    public ConsumerKafkaGroup(String topic, String partitionList, Properties props) {
        this.topic = topic;
        this.partitionList = partitionList;
        this.props = props;
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void run(int numThreads, SourceCallback sourceCallback) {
        try {
            // now launch all the threads
            executor = Executors.newFixedThreadPool(numThreads);
            // now create consumers to consume the messages
            for (int i = 0; i < numThreads; i++) {
                executor.submit(new KafkaConsumerThread(sourceCallback, topic, partitionList, props));
            }
            log.info("Kafka Consumer started listening on topic: " + topic);
        } catch (Throwable t) {
            log.error("Error while creating KafkaConsumerThread for topic: " + topic, t);
        }
    }
}
