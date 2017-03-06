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

import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.curator.test.TestingServer;
import org.apache.log4j.Logger;
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
import java.util.Properties;

public class KafkaInputTransportTestCase {
    private static final Logger log = Logger.getLogger(KafkaInputTransportTestCase.class);
    private TestingServer zkTestServer;
    private KafkaServerStartable kafkaServer;
    @Test
    public void testCreatingKafkaSubscription() throws InterruptedException {
        try{

            StreamDefinition inputDefinition = StreamDefinition.id("FooStream")
                    .attribute("symbol", Attribute.Type.STRING)
                    .attribute("price", Attribute.Type.FLOAT)
                    .attribute("volume", Attribute.Type.INT)
                    .annotation(Annotation.annotation("source")
                            .element("type", "kafka")
                            .element("topic", "kafka_topic")
                            .element("threads", "1")
                            .element("partition.no.list", "0,1")
                            .element("group.id", "test")
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

            StreamDefinition outputDefinition = StreamDefinition.id("BarStream")
                    .attribute("symbol", Attribute.Type.STRING)
                    .attribute("price", Attribute.Type.FLOAT)
                    .attribute("volume", Attribute.Type.INT);

            SiddhiManager siddhiManager = new SiddhiManager();
            siddhiManager.setExtension("inputmapper:text", TextInputMapper.class);

            ExecutionPlan executionPlan = new ExecutionPlan("ep1");
            executionPlan.defineStream(inputDefinition);
            executionPlan.defineStream(outputDefinition);
            executionPlan.addQuery(query);
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);


            executionPlanRuntime.addCallback("BarStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                }
            });
            executionPlanRuntime.start();
            Thread.sleep(30000);
            executionPlanRuntime.shutdown();
        } catch (ZkTimeoutException ex) {
            log.warn("No zookeeper may not be available.", ex);
        }
    }

    @Test
    public void testCreatingFullKafkaEventFlow() throws InterruptedException {
//        setupKafkaBroker();
//        Thread.sleep(10000);

        Runnable kafkaReceiver = new KafkaFlow();
        Thread t1 = new Thread(kafkaReceiver);
        t1.start();
        Thread.sleep(35000);

//        stopKafkaBroker();
    }

    //---- private methods --------
    private void setupKafkaBroker() {
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

            // create "page_visits" topic
//            ZkClient zkClient = new ZkClient(zkTestServer.getConnectString(), 10000, 10000, ZKStringSerializer$.MODULE$);
//            AdminUtils.createTopic(zkClient, "page_visits", 1, 1, new Properties());
//            zkClient.close();
        } catch (Exception e) {
            log.error("Error running local Kafka broker / Zookeeper", e);
        }
    }

    private void stopKafkaBroker() {
        try {
            if (kafkaServer != null) {
                kafkaServer.shutdown();
            }
            Thread.sleep(10000);
            if (zkTestServer != null) {
                zkTestServer.stop();
            }
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error shutting down Kafka broker / Zookeeper", e);
        }
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

