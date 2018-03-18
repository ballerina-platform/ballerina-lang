/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.siddhi.core.transport;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.config.InMemoryConfigManager;
import org.ballerinalang.siddhi.core.util.transport.InMemoryBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class SingleClientDistributedTransportTestCases {
    private static final Logger log = LoggerFactory.getLogger(SingleClientDistributedTransportTestCases.class);
    private AtomicInteger topic1Count = new AtomicInteger(0);
    private AtomicInteger topic2Count = new AtomicInteger(0);

    @BeforeMethod
    public void init() {
        topic1Count.set(0);
        topic2Count.set(0);
    }

    @Test
    public void singleClientRoundRobin() throws InterruptedException {
        log.info("Test inMemorySink And EventMapping With SiddhiQL Dynamic Params");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                topic1Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "topic1";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                topic2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "topic2";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', @map(type='passThrough'), " +
                "   @distribution(strategy='roundRobin', " +
                "       @destination(topic = 'topic1'), " +
                "       @destination(topic = 'topic2'))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});

        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 3, topic1Count.get());
        AssertJUnit.assertEquals("Number of IBM events", 2, topic2Count.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);

    }

    @Test
    public void singleClientPartitioned() throws InterruptedException {
        log.info("Test inMemorySink And EventMapping With SiddhiQL Dynamic Params");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                topic1Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "topic1";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                topic2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "topic2";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', @map(type='passThrough'), " +
                "   @distribution(strategy='partitioned', partitionKey='symbol'," +
                "       @destination(topic = 'topic1'), " +
                "       @destination(topic = 'topic2'))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"IBM", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});

        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of topic 1 events", 2, topic1Count.get());
        AssertJUnit.assertEquals("Number of topic 2 events", 4, topic2Count.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);

    }

    @Test
    public void singleClientBroadcast() throws InterruptedException {
        log.info("Test inMemorySink And EventMapping With SiddhiQL Dynamic Params");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                topic1Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "topic1";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                topic2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "topic2";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', @map(type='passThrough'), " +
                "   @distribution(strategy='broadcast'," +
                "       @destination(topic = 'topic1'), " +
                "       @destination(topic = 'topic2'))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"IBM", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});

        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of topic 1 events", 6, topic1Count.get());
        AssertJUnit.assertEquals("Number of topic 2 events", 6, topic2Count.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);

    }

    @Test
    public void singleClientBroadcastWithRef() throws InterruptedException {
        log.info("Test inMemorySink And EventMapping With SiddhiQL Dynamic Params with ref");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                topic1Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "topic1";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                topic2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "topic2";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(ref='test1', @map(type='passThrough'), " +
                "   @distribution(strategy='broadcast'," +
                "       @destination(ref = 'test2'), " +
                "       @destination(ref = 'test3'))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        Map<String, String> systemConfigs = new HashMap<>();
        systemConfigs.put("test1.type", "testInMemory");
        systemConfigs.put("test2.topic", "topic1");
        systemConfigs.put("test3.topic", "topic2");
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(null, systemConfigs);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setConfigManager(inMemoryConfigManager);

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"IBM", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});

        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of topic 1 events", 6, topic1Count.get());
        AssertJUnit.assertEquals("Number of topic 2 events", 6, topic2Count.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);

    }
}
