/*
 *  Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.messaging.kafka.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getFilePath;

/**
 * Creates a local Kafka cluster for testing Ballerina Kafka module.
 */
public class KafkaCluster {

    // Default properties file paths
    private static final String propertiesPath = Paths.get("data-files", "properties").toString();
    private static final Path zookeeperPropFile = Paths.get(propertiesPath, "zookeeper.properties");
    private static final Path kafkaPropFile = Paths.get(propertiesPath, "server.properties");

    // Suffixes
    private static final String zookeeperSuffix = "zookeeper";
    private static final String kafkaSuffix = "kafka";

    // Broker configs
    private static final String zookeeperConnectConfig = "zookeeper.connect";
    private static final String logDirConfig = "log.dirs";
    private static final String listenersConfig = "listeners";

    // Zookeeper configs
    private static final String dataDirConfig = "dataDir";
    private static final String portConfig = "clientPort";

    private KafkaConsumer<?, ?> kafkaConsumer = null;
    private KafkaProducer<?, ?> kafkaProducer = null;
    private final String dataDir;

    private ZookeeperLocal zookeeper;
    private List<KafkaLocal> brokerList;

    private Properties defaultZookeeperProperties;
    private Properties defaultKafkaProperties;
    private int zookeeperPort = 2181;
    private int brokerPort = 9092;
    private String host = "localhost";
    private String bootstrapServer = host + ":" + brokerPort;

    public KafkaCluster(String dataDir, String host) throws IOException {
        this.dataDir = dataDir;
        if (host != null) {
            this.host = host;
        }
        initializeDefaultProperties();
        this.brokerList = new ArrayList<>();
    }

    public KafkaCluster withZookeeper(int port, Properties customProperties) {
        Properties properties = new Properties();
        this.zookeeperPort = port;
        properties.putAll(defaultZookeeperProperties);
        if (customProperties != null) {
            properties.putAll(customProperties);
        }
        String zookeeperDataDir = Paths.get(dataDir, zookeeperSuffix).toString();
        properties.setProperty(dataDirConfig, zookeeperDataDir);
        properties.setProperty(portConfig, Integer.toString(port));

        this.zookeeper = new ZookeeperLocal(properties);
        return this;
    }

    public KafkaCluster withBroker(String protocol, int port, Properties customProperties) {
        if (this.zookeeper == null) {
            throw new IllegalStateException("Zookeeper not initialized");
        }
        Properties properties = new Properties();
        properties.putAll(defaultKafkaProperties);
        if (customProperties != null) {
            properties.putAll(customProperties);
        }

        if (properties.getProperty(listenersConfig) == null) {
            String listeners = protocol + "://" + this.host + ":" + port;
            properties.setProperty(listenersConfig, listeners);
        }

        String kafkaDataDir = Paths.get(dataDir, kafkaSuffix).toString();
        String zookeeperConfig = this.host + ":" + this.zookeeperPort;
        properties.setProperty(zookeeperConnectConfig, zookeeperConfig);
        properties.setProperty(logDirConfig, kafkaDataDir);
        KafkaLocal kafkaServer = new KafkaLocal(properties);
        this.brokerList.add(kafkaServer);
        this.brokerPort = port;
        return this;
    }

    public KafkaCluster withConsumer(String keyDeserializer, String valueDeserializer, String groupId,
                                     List<String> topics) {
        this.bootstrapServer = this.host + ":" + this.brokerPort;
        int maximumMessagesPerPoll = 1;
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                               Integer.toString(maximumMessagesPerPoll));
        this.kafkaConsumer = new KafkaConsumer(properties);
        this.kafkaConsumer.subscribe(topics);
        return this;
    }

    public KafkaCluster withProducer(String keySerializer, String valueSerializer) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        // Stop batching the messages, w=since we need to send messages ASAP in the tests.
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(0));
        this.kafkaProducer = new KafkaProducer(properties);
        return this;
    }

    public KafkaCluster start() throws Throwable {
        if (this.zookeeper == null) {
            throw new IllegalStateException("Zookeeper is not started");
        } else if (this.brokerList.isEmpty()) {
            throw new IllegalStateException("No brokers added");
        }
        for (KafkaLocal kafkaServer : brokerList) {
            kafkaServer.start();
        }
        return this;
    }

    public void stop() {
        if (this.kafkaConsumer != null) {
            this.kafkaConsumer.close();
        }

        for (KafkaLocal kafkaServer : brokerList) {
            kafkaServer.stop();
        }
    }

    public void createTopic(String topic, int partitions, short replicationFactor) {
        Properties properties = new Properties();
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServer);
        AdminClient adminClient = AdminClient.create(properties);
        NewTopic newTopic = new NewTopic(topic, partitions, replicationFactor);
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singletonList(newTopic));
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> createTopicsResult.all().isDone());
    }

    public String consumeMessage(long timeout) {
        if (this.kafkaConsumer == null) {
            throw new IllegalStateException("Kafka cluster does not have a consumer");
        }
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<?, ?> records = this.kafkaConsumer.poll(duration);
        for (Object record : records) {
            ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) record;
            return consumerRecord.value().toString();
        }
        return null;
    }

    public void sendMessage(String topic, Object key, Object value) throws ExecutionException, InterruptedException {
        ProducerRecord producerRecord = new ProducerRecord<>(topic, key, value);
        // Since this is for tests, we block until producer sends the message
        this.kafkaProducer.send(producerRecord).get();
    }

    public void sendMessage(String topic, Object value) throws ExecutionException, InterruptedException {
        ProducerRecord producerRecord = new ProducerRecord<>(topic, value);
        // Since this is for tests, we block until producer sends the message
        this.kafkaProducer.send(producerRecord).get();
    }

    private void initializeDefaultProperties() throws IOException {
        defaultZookeeperProperties = new Properties();
        defaultKafkaProperties = new Properties();
        InputStream zookeeperPropertiesStream = new FileInputStream(getFilePath(zookeeperPropFile));
        defaultZookeeperProperties.load(zookeeperPropertiesStream);
        InputStream kafkaPropertiesStream = new FileInputStream(getFilePath(kafkaPropFile));
        defaultKafkaProperties.load(kafkaPropertiesStream);
    }
}
