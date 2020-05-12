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
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;

/**
 * Creates a local Kafka cluster for testing Ballerina Kafka module.
 */
public class KafkaCluster {

    private static PrintStream console = System.out;

    // Default properties file paths
    private static final String zookeeperProp = "zookeeper.properties";
    private static final String kafkaProp = "server.properties";

    // Suffixes
    private static final String zookeeperSuffix = "zookeeper";
    private static final String kafkaSuffix = "kafka";

    // Broker configs
    private static final String zookeeperConnectConfig = "zookeeper.connect";
    private static final String logDirConfig = "log.dirs";
    private static final String listenersConfig = "listeners";
    private static final String brokerIdConfig = "broker.id";

    // Zookeeper configs
    private static final String dataDirConfig = "dataDir";
    private static final String portConfig = "clientPort";

    private KafkaConsumer<?, ?> kafkaConsumer = null;
    private KafkaProducer<?, ?> kafkaProducer = null;
    private AdminClient kafkaAdminClient = null;
    private final String dataDir;

    private ZookeeperLocal zookeeper;
    private List<KafkaLocal> brokerList;

    private Properties defaultZookeeperProperties;
    private Properties defaultKafkaProperties;
    private int zookeeperPort = 2181;
    private int brokerPort = 9092;
    private String host = "localhost";
    private String bootstrapServer = host + ":" + brokerPort;
    private String propertiesPath = Paths.get("data-files", "properties").toString();

    public KafkaCluster(String dataDir, String host, String resourceDirectory) throws IOException {
        if (dataDir == null) {
            throw new IllegalArgumentException("Data directory cannot be null");
        }
        this.dataDir = dataDir;
        if (host != null) {
            this.host = host;
        }
        if (resourceDirectory != null) {
            this.propertiesPath = resourceDirectory;
        }
        initializeDefaultProperties(resourceDirectory);
        this.brokerList = new ArrayList<>();
    }

    public KafkaCluster(String dataDir, String host) throws IOException {
        if (dataDir == null) {
            throw new IllegalArgumentException("Data directory cannot be null");
        }
        this.dataDir = dataDir;
        if (host != null) {
            this.host = host;
        }
        initializeDefaultProperties();
        this.brokerList = new ArrayList<>();
    }

    public KafkaCluster(String dataDir) throws IOException {
        if (dataDir == null) {
            throw new IllegalArgumentException("Data directory cannot be null");
        }
        this.dataDir = dataDir;
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

    public KafkaCluster withZookeeper(int port) {
        return withZookeeper(port, null);
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
        // Assign next broker index as the broker ID
        properties.setProperty(brokerIdConfig, Integer.toString(brokerList.size()));
        KafkaLocal kafkaServer = new KafkaLocal(properties);
        this.brokerList.add(kafkaServer);
        this.brokerPort = port;
        this.bootstrapServer = this.host + ":" + this.brokerPort;
        return this;
    }

    public KafkaCluster withBroker(String protocol, int port) {
        return withBroker(protocol, port, null);
    }

    public KafkaCluster withConsumer(String keyDeserializer, String valueDeserializer, String groupId,
                                     List<String> topics, Properties additionalProperties) {
        String maximumMessagesPerPoll = Integer.toString(1);
        String offsetCommit = "earliest";
        Properties properties = new Properties();
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // Consumer one message at a time. Call this again to consume more.
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maximumMessagesPerPoll);
        // We don't want to miss any messages, which were sent before the consumer started.
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetCommit);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        this.kafkaConsumer = new KafkaConsumer(properties);
        this.kafkaConsumer.subscribe(topics);
        return this;
    }

    public KafkaCluster withConsumer(String keyDeserializer, String valueDeserializer, String groupId,
                                     List<String> topics) {
        return withConsumer(keyDeserializer, valueDeserializer, groupId, topics, null);
    }

    public KafkaCluster withProducer(String keySerializer, String valueSerializer, Properties additionalProperties) {
        Properties properties = new Properties();
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        // Stop batching the messages since we need to send messages ASAP in the tests.
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(0));
        this.kafkaProducer = new KafkaProducer(properties);
        return this;
    }

    public KafkaCluster withProducer(String keySerializer, String valueSerializer) {
        return withProducer(keySerializer, valueSerializer, null);
    }

    public KafkaCluster withAdminClient(Properties additionalProperties) {
        Properties properties = new Properties();
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServer);
        this.kafkaAdminClient = KafkaAdminClient.create(properties);
        return this;
    }

    public KafkaCluster withAdminClient() {
        return withAdminClient(null);
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
        console.println("Started ZooKeeper at: " + this.host + ":" + this.zookeeperPort);
        console.println("Started Kafka Server at: " + this.host + ":" + this.brokerPort);
        return this;
    }

    public void stop() {
        if (this.kafkaConsumer != null) {
            this.kafkaConsumer.close();
        }
        if (this.kafkaProducer != null) {
            this.kafkaProducer.close();
        }
        if (this.kafkaAdminClient != null) {
            this.kafkaAdminClient.close();
        }
        for (KafkaLocal kafkaServer : brokerList) {
            kafkaServer.stop();
        }

        this.zookeeper.stop();
        deleteDirectory(new File(dataDir));

        console.println("Stopped ZooKeeper at: " + this.host + ":" + this.zookeeperPort);
        console.println("Stopped Kafka Server at: " + this.host + ":" + this.brokerPort);
    }

    public void createTopic(String topic, int partitions, int replicationFactor) {
        if (this.kafkaAdminClient == null) {
            throw new IllegalStateException("Kafka cluster does not have an admin client");
        }
        NewTopic newTopic = new NewTopic(topic, partitions, (short) replicationFactor);
        CreateTopicsResult createTopicsResult = this.kafkaAdminClient.createTopics(Collections.singletonList(newTopic));
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> createTopicsResult.all().isDone());
    }

    public String consumeMessage(long timeout) {
        if (this.kafkaConsumer == null) {
            throw new IllegalStateException("Kafka cluster does not have a consumer");
        }
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<?, ?> records = this.kafkaConsumer.poll(duration);
        if (records != null && !records.isEmpty()) {
            for (ConsumerRecord<?, ?> record : records) {
                ConsumerRecord<?, ?> consumerRecord = record;
                TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset());
                Map<TopicPartition, OffsetAndMetadata> consumedMap = new HashMap<>();
                consumedMap.put(topicPartition, offsetAndMetadata);
                kafkaConsumer.commitSync(consumedMap, Duration.ofMillis(1000));
                return consumerRecord.value().toString();
            }
        }
        return null;
    }

    public void sendMessage(String topic, Object key, Object value) throws ExecutionException, InterruptedException {
        if (this.kafkaProducer == null) {
            throw new IllegalStateException("Kafka cluster does not have a producer");
        }
        ProducerRecord producerRecord = new ProducerRecord<>(topic, key, value);
        // Since this is for tests, block until producer sends the message.
        this.kafkaProducer.send(producerRecord).get();
    }

    public void sendMessage(String topic, Object value) throws ExecutionException, InterruptedException {
        if (this.kafkaProducer == null) {
            throw new IllegalStateException("Kafka cluster does not have a producer");
        }
        ProducerRecord producerRecord = new ProducerRecord<>(topic, value);
        // Since this is for tests, block until producer sends the message
        this.kafkaProducer.send(producerRecord).get();
    }

    private void deleteDirectory(File file) {
        String[] entries = file.list();
        if (entries != null) {
            for (String s : entries) {
                File currentFile = new File(file.getPath(), s);
                boolean deleted = currentFile.delete();
                if (!deleted) {
                    currentFile.deleteOnExit();
                }
            }
        }
    }

    private void initializeDefaultProperties() throws IOException {
        Path zookeeperPropFile = Paths.get(propertiesPath, zookeeperProp);
        Path kafkaPropFile = Paths.get(propertiesPath, kafkaProp);
        defaultZookeeperProperties = new Properties();
        defaultKafkaProperties = new Properties();
        InputStream zookeeperPropertiesStream = new FileInputStream(getResourcePath(zookeeperPropFile));
        defaultZookeeperProperties.load(zookeeperPropertiesStream);
        InputStream kafkaPropertiesStream = new FileInputStream(getResourcePath(kafkaPropFile));
        defaultKafkaProperties.load(kafkaPropertiesStream);
    }

    private void initializeDefaultProperties(String resourceDirectory) throws IOException {
        defaultZookeeperProperties = new Properties();
        defaultKafkaProperties = new Properties();
        InputStream zookeeperPropertiesStream = new FileInputStream(
                getResourcePath(Paths.get(resourceDirectory, "zookeeper.properties")));
        defaultZookeeperProperties.load(zookeeperPropertiesStream);
        InputStream kafkaPropertiesStream = new FileInputStream(getResourcePath(Paths.get(resourceDirectory, "server" +
                ".properties")));
        defaultKafkaProperties.load(kafkaPropertiesStream);
    }
}
