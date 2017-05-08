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
    static final Logger log = Logger.getLogger(JsonSourceMapperTestCase.class);
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
                            junit.framework.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            junit.framework.Assert.assertEquals(55.678f, event.getData(1));
                            break;
                        case 3:
                            junit.framework.Assert.assertEquals(55f, event.getData(1));
                            break;
                        case 4:
                            junit.framework.Assert.assertEquals("WSO2@#$%^*", event.getData(0));
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
                "@map(type='json', fail.on.unknown.attribute='true'))\n" +
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
                "         \"price\":55,\n" +
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
        Assert.assertEquals("Number of events", 1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest3() throws InterruptedException {
        log.info("test JsonSourceMapper 3");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.unknown.attribute='false'))\n" +
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
                            junit.framework.Assert.assertEquals(null, event.getData(1));
                            break;
                        case 3:
                            junit.framework.Assert.assertEquals(56.0f, event.getData(1));
                            break;
                        case 4:
                            junit.framework.Assert.assertEquals(57.6f, event.getData(1));
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
                "@map(type='json', fail.on.unknown.attribute='true'))\n" +
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
                            junit.framework.Assert.assertEquals(1.0f, event.getData(1));
                            break;
                        case 3:
                            junit.framework.Assert.assertEquals(56.0f, event.getData(1));
                            break;
                        case 4:
                            junit.framework.Assert.assertEquals(57.6f, event.getData(1));
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
                "         \"symbol\":10.234\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\":a\n" +
                "      }\n" +
                " }");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void jsonSourceMapperTest5() throws InterruptedException {
        log.info("test JsonSourceMapper 5");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', " +
                "@map(type='json', fail.on.unknown.attribute='true'))\n" +
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
                            junit.framework.Assert.assertEquals(1.0f, event.getData(1));
                            break;
                        case 3:
                            junit.framework.Assert.assertEquals(56.0f, event.getData(1));
                            break;
                        case 4:
                            junit.framework.Assert.assertEquals(57.6f, event.getData(1));
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
                "         \"symbol\":10.234\",\n" +
                "         \"price\":55.6,\n" +
                "         \"volume\":a\n" +
                "      }\n" +
                " }");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
    }


}
