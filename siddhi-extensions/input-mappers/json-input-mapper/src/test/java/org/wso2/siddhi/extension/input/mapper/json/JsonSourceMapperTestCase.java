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

public class JsonSourceMapperTestCase {
    private static final Logger log = Logger.getLogger(JsonSourceMapperTestCase.class);
    private AtomicInteger count = new AtomicInteger();

    @Test
    public void jsonSourceMapperTest1() throws InterruptedException {
        log.info("test JsonSourceMapper 1");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='json')) " +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            Assert.assertEquals(55.678f, event.getData(1));
                            break;
                        case 3:
                            Assert.assertEquals(55f, event.getData(1));
                            break;
                        case 4:
                            Assert.assertEquals("WSO2@#$%^*", event.getData(0));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.678,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2@#$%^*\",\n" +
                "         \"price\":55,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 4, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest2() throws InterruptedException {
        log.info("test JsonSourceMapper 2");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.missing.attribute='true'))\n" +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            Assert.assertEquals(null, event.getData(1));
                            break;
                        case 3:
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\":null\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":null,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":null,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 3, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest3() throws InterruptedException {
        log.info("test JsonSourceMapper 3");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.missing.attribute='false'))\n" +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            Assert.assertEquals(null, event.getData(1));
                            break;
                        case 3:
                            Assert.assertEquals(56.0f, event.getData(1));
                            break;
                        case 4:
                            Assert.assertEquals(57.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\":null\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":null,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":null,\n" +
                "         \"price\":56,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":57.6,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 4, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest4() throws InterruptedException {
        log.info("test JsonSourceMapper 4");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.missing.attribute='true'))\n" +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            Assert.assertEquals(1.0f, event.getData(1));
                            break;
                        case 3:
                            Assert.assertEquals(56.0f, event.getData(1));
                            break;
                        case 4:
                            Assert.assertEquals(57.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\": \"\"\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\": \"\",\n" +
                "         \"volume\": 100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\" \",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\": 100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\": 123,\n" +
                "         \"price\":1,\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":10.234,\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\":a\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":USD55.6,\n" +
                "         \"volume\": 100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\": 100.20\n" +
                "      }\n" +
                " }");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest5() throws InterruptedException {
        log.info("test JsonSourceMapper 5");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.missing.attribute='true'))\n" +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "  {\n" +
                "   \"event\":{\n" +
                "       <symbol>WSO2</symbol>\n" +
                "       <price>55.6</price>\n" +
                "       <volume>100</volume>\n" +
                "   }\n" +
                "}");
        InMemoryBroker.publish("stock", " {\n" +
                "   \"event\":{\n" +
                "       \"TESTevent\": {\n" +
                "       \"symbol\": \"WSO2\",\n" +
                "       \"price\": \"55.6\",\n" +
                "       \"volume\": \"100\"\n" +
                "   }\n" +
                "}");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\": 100\n" +
                "      }\n" +
                " }");

        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest6() throws InterruptedException {
        log.info("test JsonSourceMapper 6");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.missing.attribute='true'))\n" +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", " {\n" +
                "   \"event\": {\n" +
                "       \"symbol\": \"WSO2\",\n" +
                "       \"price\": 55.6,\n" +
                "       \"volume\": [\n" +
                "           {\"v\": 100},\n" +
                "           {\"v\": 200}\n" +
                "       ]\n" +
                "   }\n" +
                "}");
        InMemoryBroker.publish("stock", " {\n" +
                "   \"event\":{\n" +
                "       \"symbol\":\"WSO2\",\n" +
                "       \"volume\":100\n" +
                "   }\n" +
                "}");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":10.12,\n" +
                "          \"discount\":\"3%\",\n" +
                "         \"volume\":100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\": 100\n" +
                "      }\n" +
                " }");

        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest8() throws InterruptedException {
        log.info("test JsonSourceMapper 8");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.missing.attribute='true'))\n" +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", " {\n" +
                "      \"testEvent\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\": 100\n" +
                "      }\n" +
                " }");
        InMemoryBroker.publish("stock", " {\n" +
                "      \"event\":{\n" +
                "         \"symbol\":\"WSO2\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\": 100\n" +
                "      }\n" +
                " }");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest9() throws InterruptedException {
        log.info("test JsonSourceMapper 9");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.missing.attribute='true'))\n" +
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
                            Assert.assertEquals(52.6f, event.getData(1));
                            break;
                        case 2:
                            Assert.assertEquals(53.6f, event.getData(1));
                            break;
                        case 3:
                            Assert.assertEquals(54.6f, event.getData(1));
                            break;
                        case 4:
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 5:
                            Assert.assertEquals(57.6f, event.getData(1));
                            break;
                        case 6:
                            Assert.assertEquals(58.6f, event.getData(1));
                            break;
                        case 7:
                            Assert.assertEquals(60.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", " \n" +
                "[\n" +
                "{\"event\":{\"symbol\":\"WSO2\",\"price\":52.6,\"volume\":100}},\n" +
                "{\"event\":{\"symbol\":\"WSO2\",\"price\":53.6,\"volume\":99}},\n" +
                "{\"event\":{\"symbol\":\"WSO2\",\"price\":54.6,\"volume\":80}}\n" +
                "]\n");

        InMemoryBroker.publish("stock", " \n" +
                "[\n" +
                "{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":100}},\n" +
                "{\"testEvent\":{\"symbol\":\"WSO2\",\"price\":56.6,\"volume\":99}},\n" +
                "{\"event\":{\"symbol\":\"WSO2\",\"price\":57.6,\"volume\":80}}\n" +
                "]\n");
        InMemoryBroker.publish("stock", "\n" +
                "[\n" +
                "{\"event\":{\"symbol\":\"WSO2\",\"price\":58.6,\"volume\":100}},\n" +
                "{\"Event\":{\"symbol\":\"WSO2\",\"price\":59.6,\"volume\":99}},\n" +
                "{\"event\":{\"symbol\":\"WSO2\",\"price\":60.6,\"volume\":80}}\n" +
                "]\n");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 7, count.get());
        executionPlanRuntime.shutdown();
    }

    /*
    * Test cases for custom input mapping
    */

    @Test
    public void jsonSourceMapperTest10() throws InterruptedException {
        log.info("test JsonSourceMapper 10");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', enclosing.element=\"portfolio\", " +
                "@attributes(symbol = \"stock.company.symbol\", price = \"stock.price\", " +
                "volume = \"stock.volume\"))) " +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            Assert.assertEquals(56.6f, event.getData(1));
                            break;
                        case 3:
                            Assert.assertEquals(57.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "\n" +
                "{\"portfolio\":\n" +
                "   {\"stock\":{\"volume\":100,\"company\":{\"symbol\":\"wso2\"},\"price\":55.6}}" +
                "}");
        InMemoryBroker.publish("stock", "\n" +
                "{\"portfolio\":\n" +
                "   [" +
                "       {\"stock\":{\"volume\":100,\"company\":{\"symbol\":\"wso2\"},\"price\":56.6}}," +
                "       {\"stock\":{\"volume\":200,\"company\":{\"symbol\":\"wso2\"},\"price\":57.6}}" +
                "   ]\n" +
                "}\n");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 3, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest11() throws InterruptedException {
        log.info("test JsonSourceMapper 11");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', enclosing.element=\"portfolio\", " +
                "fail.on.missing.attribute=\"true\", " +
                "@attributes(symbol = \"stock.company.symbol\", price = \"stock.price\", " +
                "volume = \"stock.volume\"))) " +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            Assert.assertEquals(56.6f, event.getData(1));
                            break;
                        case 3:
                            Assert.assertEquals(null, event.getData(1));
                            break;
                        case 4:
                            Assert.assertEquals(76.6f, event.getData(1));
                            break;
                        case 5:
                            Assert.assertEquals(77.6f, event.getData(1));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "\n" +
                "{\"portfolio\":\n" +
                "   [" +
                "       {\"stock\":{\"volume\":100,\"company\":{\"symbol\":\"wso2\"},\"price\":55.6}}," +
                "       {\"stock\":{\"volume\":null,\"company\":{\"symbol\":\"wso2\"},\"price\":56.6}}" +
                "   ]\n" +
                "}\n");
        InMemoryBroker.publish("stock", "\n" +
                "{\"portfolio\":\n" +
                "   [" +
                "       {\"stock\":{\"volume\":100,\"company\":{\"symbol\":\"wso2\"}}}," +
                "       {\"stock\":{\"volume\":200,\"company\":{\"symbol\":\"wso2\"},\"price\":null}}" +
                "   ]\n" +
                "}\n");
        InMemoryBroker.publish("stock", "\n" +
                "{\"portfolio\":\n" +
                "   [" +
                "       {\"stock\":{\"volume\":100,\"company\":{\"symbol\":\"wso2\"},\"price\":76.6}}," +
                "       {\"stock\":{\"volume\":200,\"company\":{\"symbol\":null},\"price\":77.6}}" +
                "   ]\n" +
                "}\n");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 5, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest12() throws InterruptedException {
        log.info("test JsonSourceMapper 12");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', enclosing.element=\"portfolio\", " +
                "fail.on.missing.attribute=\"false\", " +
                "@attributes(symbol = \"stock.company.symbol\", price = \"stock.price\", " +
                "volume = \"stock.volume\"))) " +
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
                            Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            Assert.assertEquals(56.6f, event.getData(1));
                            break;
                        case 3:
                            Assert.assertEquals(100L, event.getData(2));
                            break;
                        case 4:
                            Assert.assertEquals(200L, event.getData(2));
                            break;
                        case 5:
                            Assert.assertEquals("wso2", event.getData(0));
                            break;
                        case 6:
                            Assert.assertEquals(null, event.getData(0));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "\n" +
                "{\"portfolio\":\n" +
                "   [" +
                "       {\"stock\":{\"volume\":100,\"company\":{\"symbol\":\"wso2\"},\"price\":55.6}}," +
                "       {\"stock\":{\"volume\":null,\"company\":{\"symbol\":\"IBM\"},\"price\":56.6}}" +
                "   ]\n" +
                "}\n");
        InMemoryBroker.publish("stock", "\n" +
                "{\"portfolio\":\n" +
                "   [" +
                "       {\"stock\":{\"volume\":100,\"company\":{\"symbol\":\"wso2\"},\"price\":66.6}}," +
                "       {\"stock\":{\"volume\":200,\"company\":{\"symbol\":\"IBM\"},\"price\":null}}" +
                "   ]\n" +
                "}\n");
        InMemoryBroker.publish("stock", "\n" +
                "{\"portfolio\":\n" +
                "   [" +
                "       {\"stock\":{\"volume\":100,\"company\":{\"symbol\":\"wso2\"},\"price\":76.6}}," +
                "       {\"stock\":{\"volume\":200,\"company\":{\"symbol\":null},\"price\":77.6}}" +
                "   ]\n" +
                "}\n");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 6, count.get());
        executionPlanRuntime.shutdown();
    }

}
