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

package org.wso2.siddhi.extension.input.mapper.json;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;

import java.util.concurrent.atomic.AtomicInteger;

public class JsonInputMapperTestCase {
    static final Logger log = Logger.getLogger(JsonInputMapperTestCase.class);

    private AtomicInteger count = new AtomicInteger();

    /*
    * Default Input mapping
    */

    /*
    * Default Json mapping for single event
    */
    @Test
    public void jsonInputMapperTest1() throws InterruptedException {
        log.info("test TextInputMapper1");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='json')) " +
                "define stream FooStream (symbol string, price float, volume long, country string); " +
                "define stream BarStream (symbol string, price float, volume long, country string); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("BarStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    switch (count.incrementAndGet()) {
                        case 1:
                            junit.framework.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            junit.framework.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":100,\"country\":\"Sri Lanka\"}}");
        InMemoryBroker.publish("stock", "{\"event\":{\"symbol\":\"IBM\",\"price\":75.6,\"volume\":10,\"country\":\"USA\"}}");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
    }


    /*
    * Default Json mapping with Fail.on.unknown.attribute for single event
    */
    @Test
    public void jsonInputMapperTest2() throws InterruptedException {
        log.info("test TextInputMapper1");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='json', fail.on.unknown.attribute='false', validate.json='true')) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("BarStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    switch (count.incrementAndGet()) {
                        case 1:
                            junit.framework.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            junit.framework.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{\"event\":{\"price\":55.6,\"volume\":100}}");
        InMemoryBroker.publish("stock", "{\"event\":{\"symbol\":\"WSO2\",\"price\":75.6,\"volume\":10}}");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
    }

    /*
    * Default Json mapping with Fail.on.unknown.attribute for event array
    */
    @Test
    public void jsonInputMapperTest3() throws InterruptedException {
        log.info("test TextInputMapper2");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='json', fail.on.unknown.attribute='true', validate.json='true')) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("BarStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    switch (count.incrementAndGet()) {
                        case 1:
                            junit.framework.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            junit.framework.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "[" +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":55.6,\"volume\":100}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":56.6,\"volume\":101}}," +
                "{\"event\":{\"price\":57.6,\"volume\":102}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":58.6,\"volume\":103}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":59.6,\"volume\":104}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":60.6,\"volume\":105}}" +
                "]");
        //InMemoryBroker.publish("stock", "\"events\":{\"event\":{\"symbol\":\"WSO2\",\"price\":75.6,\"volume\":10}}");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
    }

    /*
    Default mapping : event array
    */
    @Test
    public void jsonInputMapperTest4() throws InterruptedException {
        log.info("test TextInputMapper2");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='json', grouping.element='$.events', fail.on.unknown.attribute='true', " +
                "@attributes(symbol=\"symbol\",price=\"price\",volume=\"volume\")))  " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("BarStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.set(0);
                for (Event event : events) {
                    switch (count.incrementAndGet()) {
                        case 1:
                            junit.framework.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            junit.framework.Assert.assertEquals(56.6f, event.getData(1));
                            break;
                        case 3:
                            junit.framework.Assert.assertEquals(57.6f, event.getData(1));
                            break;
                        case 4:
                            junit.framework.Assert.assertEquals(58.6f, event.getData(1));
                            break;
                        case 5:
                            junit.framework.Assert.assertEquals(59.6f, event.getData(1));
                            break;
                        case 6:
                            junit.framework.Assert.assertEquals(60.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{\"events\":[" +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":55.6,\"volume\":100}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":56.6,\"volume\":101}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":57.6,\"volume\":102}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":58.6,\"volume\":103}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":59.6,\"volume\":104}}," +
                "{\"event\":{\"symbol\":\"wso2\",\"price\":60.6,\"volume\":105}}" +
                "]}");
        InMemoryBroker.publish("stock", "{\"events\":[" +
                "{\"event\":{\"symbol\":\"IBM\",\"price\":55.6,\"volume\":100}}," +
                "{\"event\":{\"symbol\":\"IBM\",\"price\":56.6,\"volume\":101}}," +
                "{\"event\":{\"symbol\":\"IBM\",\"price\":57.6,\"volume\":102}}," +
                "{\"event\":{\"symbol\":\"IBM\",\"price\":58.6,\"volume\":103}}," +
                "{\"event\":{\"symbol\":\"IBM\",\"price\":59.6,\"volume\":104}}," +
                "{\"event\":{\"symbol\":\"IBM\",\"price\":60.6,\"volume\":105}}" +
                "]}");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 6, count.get());
        executionPlanRuntime.shutdown();
    }

    /*@Test(expected = ExecutionPlanValidationException.class)
    public void subscriptionTest2() throws InterruptedException {
        log.info("Subscription Test 2: Test an in memory transport with named and positional json mapping - expect " +
                "exception");

        Subscription subscription = SiddhiCompiler.parseSubscription(
                "subscribe inMemory options (topic 'stock') " +
                        "map json '$.country', '$.price', '$.volume' as 'volume' " +
                        "insert into FooStream;");
        // First two parameters are based on position and the last one is named parameter

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.defineStream(StreamDefinition.id("FooStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT));
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("inputtransport:inMemory", InMemoryInputTransport.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock","{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }

    *//**
     * Expected input format:
     * {'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}
     *//*
    @Test
    public void subscriptionTest3() throws InterruptedException {
        log.info("Subscription Test 3: Test an in memory transport with custom positional json mapping");

        Subscription subscription = SiddhiCompiler.parseSubscription(
                "subscribe inMemory options(topic 'stock') " +
                        "map json '$.symbol', '$.price', '$.volume' " +
                        "insert into FooStream;");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.defineStream(StreamDefinition.id("FooStream")
                .attribute("output_symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT));
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("inputtransport:inMemory", InMemoryInputTransport.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock","{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }

    *//**
     * Expected input format:
     * {'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}
     *//*
    @Test
    public void subscriptionTest4() throws InterruptedException {
        log.info("Subscription Test 4: Test an in memory transport with custom named json mapping");

        Subscription subscription = SiddhiCompiler.parseSubscription(
                "subscribe inMemory options(topic 'stock') " +
                        "map json '$.volume' as 'volume', '$.symbol' as 'symbol', '$.price' as 'price' " +
                        "insert into FooStream;");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.defineStream(StreamDefinition.id("FooStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT));
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("inputtransport:inMemory", InMemoryInputTransport.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock","{'country': 'Sri Lanka', 'symbol': 'WSO2', 'price': 56.75, 'volume': 5}");

        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void subscriptionTest5() throws InterruptedException {
        log.info("Subscription Test 5: Test infer output stream using json mapping - expect exception");

        Subscription subscription = Subscription.Subscribe(Transport.transport("inMemory").option("topic","stock"));
        subscription.map(Mapping.format("json").map("volume", "$.volume").map("symbol", "$.symbol").map("price", "$" +
                ".price"));
        subscription.insertInto("FooStream");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("inputtransport:inMemory", InMemoryInputTransport.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock","{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void subscriptionTest6() throws InterruptedException {
        log.info("Subscription Test 6: Test error in infer output stream using json mapping without mapping name" +
                " - expect exception");

        Subscription subscription = Subscription.Subscribe(Transport.transport("inMemory").option("topic","stock"));
        subscription.map(Mapping.format("json").map("$.volume").map("$.symbol").map("$.price"));
        subscription.insertInto("FooStream");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("inputtransport:inMemory", InMemoryInputTransport.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock","{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }*/
}
