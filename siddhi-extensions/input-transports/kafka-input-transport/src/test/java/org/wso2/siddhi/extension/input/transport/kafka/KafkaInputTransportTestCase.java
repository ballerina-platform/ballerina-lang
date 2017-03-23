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

import junit.framework.Assert;
import kafka.admin.AdminUtils;
import kafka.common.TopicExistsException;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.input.mapper.text.TextInputMapper;
import org.wso2.siddhi.extension.output.mapper.text.TextOutputMapper;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Variable;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Properties;

public class KafkaInputTransportTestCase {
    private static final Logger log = Logger.getLogger(KafkaInputTransportTestCase.class);
    private static TestingServer zkTestServer;
    private static KafkaServerStartable kafkaServer;
    private volatile int count;
    private volatile boolean eventArrived;

    @BeforeClass
    public static void init() throws Exception {
        try {
            setupKafkaBroker();
            Thread.sleep(3000);
        }catch (Exception e) {
            throw new RemoteException("Exception caught when starting server", e);
        }
    }

    @Before
    public void init2() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testKafkaMultipleTopicPartitionPartitionWiseSubscription() throws InterruptedException {
        try{
            log.info("Creating test for multiple topics and partitions and thread partition wise");
            String topics[] = new String[]{"kafka_topic","kafka_topic2"};
            createTopic(topics, 2);
            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("inputmapper:text", TextInputMapper.class);
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(
                    "@Plan:name('TestExecutionPlan') " +
                    "define stream BarStream (symbol string, price float, volume long); " +
                    "@info(name = 'query1') " +
                        "@source(type='kafka', topic='kafka_topic,kafka_topic2', group.id='test', threading.option='partition.wise', " +
                                "bootstrap.servers='localhost:9092', partition.no.list='0,1', " +
                                "@map(type='text'))" +
                                    "Define stream FooStream (symbol string, price float, volume long);" +
                    "from FooStream select symbol, price, volume insert into BarStream;");
            executionPlanRuntime.addCallback("BarStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        System.out.println(event);
                        eventArrived = true;
                        count++;
                        switch (count) {
                            case 1:
                                Assert.assertEquals(0, event.getData(2));
                                break;
                            case 2:
                                Assert.assertEquals(0, event.getData(2));
                                break;
                            case 3:
                                Assert.assertEquals(1, event.getData(2));
                                break;
                            case 4:
                                Assert.assertEquals(1, event.getData(2));
                                break;
                            default:
                                org.junit.Assert.fail();
                        }
                    }

                }
            });
            executionPlanRuntime.start();
            Thread.sleep(2000);
            kafkaPublisher(topics, 2, 2);
            Thread.sleep(5000);
            Assert.assertEquals(4, count);
            Assert.assertTrue(eventArrived);
            executionPlanRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }

//    @Test
    public void testKafkaMultipleTopicPartitionTopicWiseSubscription() throws InterruptedException {
        try{
            log.info("Creating test for multiple topics and partitions and thread topic wise");
            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("inputmapper:text", TextInputMapper.class);
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(
                    "@Plan:name('TestExecutionPlan') " +
                    "define stream BarStream (symbol string, price float, volume long); " +
                    "@info(name = 'query1') " +
                        "@source(type='kafka', topic='kafka_topic,kafka_topic2', group.id='test', threading.option='topic.wise', " +
                            "bootstrap.servers='localhost:9092', partition.no.list='0,1', " +
                            "@map(type='text'))" +
                                "Define stream FooStream (symbol string, price float, volume long);" +
                    "from FooStream select symbol, price, volume insert into BarStream;");
            executionPlanRuntime.addCallback("BarStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        System.out.println(event);
                    }
                }
            });
            executionPlanRuntime.start();
            Thread.sleep(20000);
            executionPlanRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }

//    @Test
    public void testKafkaMultipleTopicPartitionSingleThreadSubscription() throws InterruptedException {
        try{
            log.info("Creating test for multiple topics and partitions on single thread");
            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("inputmapper:text", TextInputMapper.class);
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(
                    "@Plan:name('TestExecutionPlan') " +
                    "define stream BarStream (symbol string, price float, volume long); " +
                    "@info(name = 'query1') " +
                        "@source(type='kafka', topic='kafka_topic,kafka_topic2', group.id='test', threading.option='single.thread', " +
                            "bootstrap.servers='localhost:9092', partition.no.list='0,1', " +
                            "@map(type='text'))" +
                                "Define stream FooStream (symbol string, price float, volume long);" +
                    "from FooStream select symbol, price, volume insert into BarStream;");
            executionPlanRuntime.addCallback("BarStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        System.out.println(event);
                    }
                }
            });
            executionPlanRuntime.start();
            Thread.sleep(20000);
            executionPlanRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }

//    @Test
    public void testKafkaSingleTopicSubscriptionWithPartition() throws InterruptedException {
        try{
            log.info("Creating test for single topic with multiple partitions on single thread");
            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("inputmapper:text", TextInputMapper.class);
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(
                    "@Plan:name('TestExecutionPlan') " +
                    "define stream BarStream (symbol string, price float, volume long); " +
                    "@info(name = 'query1') " +
                    "@source(type='kafka', topic='kafka_topic', group.id='test', threading.option='single.thread', " +
                        "bootstrap.servers='localhost:9092', partition.no.list='0,1', " +
                        "@map(type='text'))" +
                            "Define stream FooStream (symbol string, price float, volume long);" +
                    "from FooStream select symbol, price, volume insert into BarStream;");
            executionPlanRuntime.addCallback("BarStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        System.out.println(event);
                    }
                }
            });
            executionPlanRuntime.start();
            Thread.sleep(20000);
            executionPlanRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }

//    @Test
    public void testCreatingKafkaSubscriptionWithoutPartition() throws InterruptedException {
        try{
            log.info("Creating test for multiple topic with no partitions on single thread");
            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("inputmapper:text", TextInputMapper.class);
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(
                    "@Plan:name('TestExecutionPlan') " +
                    "define stream BarStream (symbol string, price float, volume long); " +
                    "@info(name = 'query1') " +
                        "@source(type='kafka', topic='simple_topic,simple_topic2', group.id='test', threading.option='single.thread', " +
                            "bootstrap.servers='localhost:9092', " +
                                "@map(type='text'))" +
                                    "Define stream FooStream (symbol string, price float, volume long);" +
                    "from FooStream select symbol, price, volume insert into BarStream;");
            executionPlanRuntime.addCallback("BarStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                }
            });
            executionPlanRuntime.start();
            Thread.sleep(20000);
            executionPlanRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }

//    @Test
    public void testCreatingFullKafkaEventFlow() throws InterruptedException {
        Runnable kafkaReceiver = new KafkaFlow();
        Thread t1 = new Thread(kafkaReceiver);
        t1.start();
        Thread.sleep(35000);
    }

    //---- private methods --------
    private static void setupKafkaBroker() {
        try {
            // mock zookeeper
            zkTestServer = new TestingServer(2181);
            // mock kafka
            Properties props = new Properties();
            props.put("broker.id", "0");
            props.put("host.name", "localhost");
            props.put("port", "9092");
            props.put("log.dir", "/tmp/tmp_kafka_dir");
            props.put("zookeeper.connect", zkTestServer.getConnectString());
            props.put("replica.socket.timeout.ms", "1500");
            KafkaConfig config = new KafkaConfig(props);
            kafkaServer = new KafkaServerStartable(config);
            kafkaServer.startup();
        } catch (Exception e) {
            log.error("Error running local Kafka broker / Zookeeper", e);
        }
    }

    private void createTopic(String topics[], int numOfPartitions) {
        ZkClient zkClient = new ZkClient(zkTestServer.getConnectString(), 10000, 10000, ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(zkTestServer.getConnectString());
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);
        for (String topic : topics) {
            try {
                AdminUtils.createTopic(zkUtils, topic, numOfPartitions, 1, new Properties());
            } catch (TopicExistsException e) {
                log.warn("topic exists for: " + topic);
            }
        }
        zkClient.close();
    }

    @AfterClass
    public static void stopKafkaBroker() {
        try {
            if (kafkaServer != null) {
                kafkaServer.shutdown();
            }
            Thread.sleep(5000);
            if (zkTestServer != null) {
                zkTestServer.stop();
            }
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error shutting down Kafka broker / Zookeeper", e);
        }
    }

    private void kafkaPublisher(String topics[], int numOfPartitions, int numberOfEvents) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        for(int i = 0; i < numberOfEvents; i++) {
            String msg = "wso2,12.5," + i;
            System.out.println(msg);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (String topic : topics) {
                if (numOfPartitions > 1) {
                    System.out.println("producing: " + msg + " into partition: " + (i % numOfPartitions));
                    producer.send(new ProducerRecord<>(topic, String.valueOf(i % numOfPartitions), msg));
                } else {
                    producer.send(new ProducerRecord<>(topic, msg));
                }
            }
        }
        producer.close();
    }

    private class KafkaFlow implements Runnable {
        @Override
        public void run() {
            try{
                StreamDefinition inputDefinition = StreamDefinition.id("FooStream")
                        .attribute("symbol", Attribute.Type.STRING)
                        .attribute("price", Attribute.Type.FLOAT)
                        .attribute("volume", Attribute.Type.INT)
                        .annotation(Annotation.annotation("source")
                                .element("type", "kafka")
                                .element("topic", "receiver_topic")
                                .element("threads", "1")
                                .element("partition.no.list", "0,1")
                                .element("group.id", "group1")
                                .element("bootstrap.servers", "localhost:9092")
                                .annotation(Annotation.annotation("map")
                                        .element("type", "text")));

                StreamDefinition outputDefinition = StreamDefinition.id("BarStream")
                        .attribute("symbol", Attribute.Type.STRING)
                        .attribute("price", Attribute.Type.FLOAT)
                        .attribute("volume", Attribute.Type.INT)
                        .annotation(Annotation.annotation("sink")
                                .element("type", "kafka")
                                .element("topic", "publisher_topic")
                                .element("partition.no", "0")
                                .element("bootstrap.servers", "localhost:9092")
                                .annotation(Annotation.annotation("map")
                                        .element("type", "text")));

                Query query = Query.query();
                query.from(
                        InputStream.stream("FooStream")
                );
                query.select(
                        Selector.selector().select(new Variable("symbol")).select(new Variable("price")).select(new Variable("volume"))
                );
                query.insertInto("BarStream");

                SiddhiManager siddhiManager = new SiddhiManager();
                siddhiManager.setExtension("inputmapper:text", TextInputMapper.class);
                siddhiManager.setExtension("outputmapper:text", TextOutputMapper.class);

                ExecutionPlan executionPlan = new ExecutionPlan("ep1");
                executionPlan.defineStream(inputDefinition);
                executionPlan.defineStream(outputDefinition);
                executionPlan.addQuery(query);
                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

                executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
                    @Override
                    public void receive(Event[] events) {
                        System.out.println("Printing received events !!");
                        EventPrinter.print(events);
                    }
                });
                executionPlanRuntime.addCallback("BarStream", new StreamCallback() {
                    @Override
                    public void receive(Event[] events) {
                        System.out.println("Printing publishing events !!");
                        EventPrinter.print(events);
                    }
                });
                executionPlanRuntime.start();
                Thread.sleep(30000);
                executionPlanRuntime.shutdown();
            } catch (ZkTimeoutException ex) {
                log.warn("No zookeeper may not be available.", ex);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

