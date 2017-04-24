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
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

@Extension(
        name = "kafka",
        namespace = "inputtransport",
        description = ""
)
public class KafkaInputTransport extends InputTransport{

    private SourceEventListener sourceEventListener;
    private ScheduledExecutorService executorService;
    private OptionHolder optionHolder;
    private ConsumerKafkaGroup consumerKafkaGroup;
    private static final Logger log = Logger.getLogger(KafkaInputTransport.class);
    private Map<String, Map<Integer, Long>> topicOffsetMap = new HashMap<>();

    private final static String ADAPTOR_SUBSCRIBER_TOPIC = "topic";
    private final static String ADAPTOR_SUBSCRIBER_GROUP_ID = "group.id";
    private final static String ADAPTOR_SUBSCRIBER_ZOOKEEPER_CONNECT_SERVERS = "bootstrap.servers";
    private final static String ADAPTOR_SUBSCRIBER_PARTITION_NO_LIST = "partition.no.list";
    private final static String ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES = "optional.configuration";
    private final static String TOPIC_OFFSET_MAP = "topic.offset.map";
    private final static String THREADING_OPTION = "threading.option";
    final static String SINGLE_THREADED = "single.thread";
    final static String TOPIC_WISE = "topic.wise";
    final static String PARTITION_WISE = "partition.wise";
    private static final String HEADER_SEPARATOR = ",";
    private static final String ENTRY_SEPARATOR = ":";

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     ExecutionPlanContext executionPlanContext) {
        this.sourceEventListener = sourceEventListener;
        this.optionHolder = optionHolder;
        this.executorService = executionPlanContext.getScheduledExecutorService();
        executionPlanContext.getSnapshotService().addSnapshotable("kafka-sink", this);
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        String zkServerList = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_ZOOKEEPER_CONNECT_SERVERS);
        String groupID = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_GROUP_ID, null);
        String threadingOption = optionHolder.validateAndGetStaticValue(THREADING_OPTION);
        String partitionList;
        String partitions[];
        partitionList = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_PARTITION_NO_LIST, null);
        partitions = (partitionList != null) ? partitionList.split(HEADER_SEPARATOR) : null;
        String topicList = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_TOPIC);
        String topics[] = topicList.split(HEADER_SEPARATOR);
        String optionalConfigs = optionHolder.validateAndGetStaticValue(ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES, null);
        consumerKafkaGroup = new ConsumerKafkaGroup(topics, partitions,
                                                    KafkaInputTransport.createConsumerConfig(zkServerList, groupID,
                                                                                            optionalConfigs),
                                                    topicOffsetMap, threadingOption, this.executorService);
        consumerKafkaGroup.run(sourceEventListener);
    }

    @Override
    public void disconnect() {
        if (consumerKafkaGroup != null) {
            consumerKafkaGroup.shutdown();
            log.debug("Kafka Adapter disconnected for topic/s" +
                        optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_TOPIC));
        }
    }

    @Override
    public void destroy() {
        consumerKafkaGroup = null;
    }

    @Override
    public void pause() {
        if (consumerKafkaGroup != null) {
            consumerKafkaGroup.pause();
            if (log.isDebugEnabled()) {
                log.debug("Kafka Adapter paused for topic/s" + optionHolder.validateAndGetStaticValue
                        (ADAPTOR_SUBSCRIBER_TOPIC));
            }
        }
    }

    @Override
    public void resume() {
        if (consumerKafkaGroup != null) {
            consumerKafkaGroup.resume();
            if (log.isDebugEnabled()) {
                log.debug("Kafka Adapter resumed for topic/s" + optionHolder.validateAndGetStaticValue
                        (ADAPTOR_SUBSCRIBER_TOPIC));
            }
        }
    }

    private static Properties createConsumerConfig(String zkServerList, String groupId, String optionalConfigs) {
        Properties props = new Properties();
        props.put(ADAPTOR_SUBSCRIBER_ZOOKEEPER_CONNECT_SERVERS, zkServerList);
        if (null != groupId) {
            props.put(ADAPTOR_SUBSCRIBER_GROUP_ID, groupId);
        }
        //If it stops heart-beating for a period of time longer than session.timeout.ms then it will be considered dead
        // and its partitions will be assigned to another process
        props.put("session.timeout.ms", "30000");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");

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
        return props;
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> currentState = new HashMap<>();
        currentState.put(TOPIC_OFFSET_MAP, this.topicOffsetMap);
        return currentState;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        this.topicOffsetMap = (Map<String, Map<Integer, Long>>) state.get(TOPIC_OFFSET_MAP);
        consumerKafkaGroup.restore(topicOffsetMap);
    }
}