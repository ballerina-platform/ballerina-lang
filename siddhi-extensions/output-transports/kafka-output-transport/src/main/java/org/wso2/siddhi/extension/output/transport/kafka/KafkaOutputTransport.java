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

package org.wso2.siddhi.extension.output.transport.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.OutputTransport;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;

@Extension(
        name = "kafka",
        namespace = "outputtransport",
        description = ""
)
public class KafkaOutputTransport extends OutputTransport {

    private ScheduledExecutorService executorService;
    private Producer<String, String>  producer;
    private Option topicOption = null;
    private String kafkaConnect;
    private String optionalConfigs;
    private Option partitionOption;

    private static final String KAFKA_PUBLISH_TOPIC = "topic";
    private static final String KAFKA_BROKER_LIST = "bootstrap.servers";
    private static final String KAFKA_OPTIONAL_CONFIGURATION_PROPERTIES = "optional.configuration";
    private static final String HEADER_SEPARATOR = ",";
    private static final String ENTRY_SEPARATOR = ":";
    private static final String KAFKA_PARTITION_NO = "partition.no";

    private static final Logger log = Logger.getLogger(KafkaOutputTransport.class);

    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                        ExecutionPlanContext executionPlanContext) {
        kafkaConnect = optionHolder.validateAndGetStaticValue(KAFKA_BROKER_LIST);
        optionalConfigs = optionHolder.validateAndGetStaticValue(KAFKA_OPTIONAL_CONFIGURATION_PROPERTIES, null);
        topicOption = optionHolder.validateAndGetOption(KAFKA_PUBLISH_TOPIC);
        partitionOption = optionHolder.getOrCreateOption(KAFKA_PARTITION_NO, null);
        executorService = executionPlanContext.getScheduledExecutorService();
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaConnect);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        if (optionalConfigs != null) {
            String[] optionalProperties = optionalConfigs.split(HEADER_SEPARATOR);
            if (optionalProperties != null && optionalProperties.length > 0) {
                for (String header : optionalProperties) {
                    try {
                        String[] configPropertyWithValue = header.split(ENTRY_SEPARATOR, 2);
                        props.put(configPropertyWithValue[0], configPropertyWithValue[1]);
                    } catch (Exception e) {
                        log.warn("Optional property '" + header + "' is not defined in the correct format.", e);
                    }
                }
            }
        }
        producer = new KafkaProducer<>(props);
        log.info("Kafka producer created.");
    }

    @Override
    public void publish(Object payload, DynamicOptions transportOptions) throws ConnectionUnavailableException {
        String topic = topicOption.getValue(transportOptions);
        String partitionNo = partitionOption.getValue(transportOptions);
        try {
            executorService.submit(new KafkaSender(topic, partitionNo, payload));
        } catch (RejectedExecutionException e) {
            log.error("Job queue is full : " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        //close producer
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{KAFKA_PUBLISH_TOPIC, KAFKA_PARTITION_NO};
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {

    }

    private class KafkaSender implements Runnable {

        String topic;
        Object message;
        String partitionNo;

        KafkaSender(String topic, String partitionNo, Object message) {
            this.topic = topic;
            this.message = message;
            this.partitionNo = partitionNo;
        }

        @Override
        public void run() {
            try {
                if(null == partitionNo) {
                    producer.send(new ProducerRecord<>(topic, message.toString()));
                } else {
                    producer.send(new ProducerRecord<>(topic, partitionNo, message.toString()));
                }
            } catch (Throwable e) {
                log.error("Unexpected error when sending event via Kafka Output Adapter:" + e.getMessage(), e);
            }
        }
    }
}
