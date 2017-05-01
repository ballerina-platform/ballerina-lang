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

package org.wso2.siddhi.extension.input.mapper.xml;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.concurrent.atomic.AtomicInteger;

public class XmlInputMapperTestCase {
    static final Logger log = Logger.getLogger(XmlInputMapperTestCase.class);
    private AtomicInteger count = new AtomicInteger();

    @Before
    public void init() {
        count.set(0);
    }

    /**
     * Expected input format:
     * <events>
     *     <event>
     *         <symbol>WSO2</symbol>
     *         <price>55.6</price>
     *         <volume>100</volume>
     *     </event>
     * </events>
     */
    @Test
    public void testXmlInputMappingDefault() throws InterruptedException {
        log.info("Test case for xml input mapping with default mapping");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml')) " +
                "define stream FooStream (symbol string, price float, volume int); " +
                "define stream BarStream (symbol string, price float, volume int); ";

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
                            org.junit.Assert.assertEquals(55.689f, event.getData(1));
                            org.junit.Assert.assertEquals("", event.getData(0));
                            break;
                        case 2:
                            org.junit.Assert.assertEquals(75.0f, event.getData(1));
                            org.junit.Assert.assertEquals("IBM@#$%^*", event.getData(0));
                            break;
                        case 3:
                            org.junit.Assert.assertEquals(" ", event.getData(0));
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<events><event><symbol></symbol><price>55.689</price>" +
                "<volume>100</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol>IBM@#$%^*</symbol><price>75</price>" +
                "<volume>10</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price>75</price>" +
                "<volume></volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price></price>" +
                "<volume>10</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol> </symbol><price>56</price>" +
                "<volume>10</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price>56</price>" +
                "<volume>aa</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price>bb</price>" +
                "<volume>10</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price>bb</price>" +
                "<volume>10.6</volume></event></events>");

        //assert event count
        Assert.assertEquals("Number of events", 3, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingDefaultMultipleEvents() throws InterruptedException {
        log.info("Test case for xml input mapping with default mapping for multiple events");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml')) " +
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
                            org.junit.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            org.junit.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price>55.6</price>" +
                "<volume>100</volume></event><event><symbol>IBM</symbol><price>75.6</price>" +
                "<volume>10</volume></event><event111><symbol>IBM</symbol><price>75.6</price>" +
                "<volume>10</volume></event111></events>");

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingDefaultNegative() throws InterruptedException {
        log.info("Test case for xml input mapping with default mapping for multiple events");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml')) " +
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
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<test><event><symbol>WSO2</symbol><price>55.6</price>" +
                "<volume>100</volume></event><event><symbol>IBM</symbol><price>75.6</price>" +
                "<volume>10</volume></event></test>");
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price>55.6</price>" +
                "<volume>100</volume><event><symbol>IBM</symbol><price>75.6</price>" +
                "<volume>10</volume></event></events>");
        InMemoryBroker.publish("stock", "<test><event><symbol>WSO2</symbol><price>55.6</price>" +
                "<volume>100</volume></event><event><symbol>IBM</symbol><price><v1>33</v1></price>" +
                "<volume>10</volume></event></test>");


        //assert event count
        Assert.assertEquals("Number of events", 0, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom1() throws InterruptedException {
        log.info("Test case for xml input mapping with custom mapping. Here multiple events are sent in one message.");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes,at=urn:schemas-microsoft-com:datatypes\", " +
                "enclosing.element=\"//portfolio\", @attributes(symbol = \"symbol\"" +
                "                                           , price = \"price\"" +
                "                                           , volume = \"volume\"))) " +
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
                            org.junit.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            org.junit.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <price dt:dt=\"number\">55.6</price>" +
                "  </stock>" +
                "  <stock1 exchange=\"nyse\">" +
                "    <volume>200</volume>" +
                "    <symbol>IBM</symbol>" +
                "    <price dt:dt=\"number\">75.6</price>" +
                "  </stock1>" +
                "  <stock1 exchange=\"nyse\">" +
                "    <volume1>200</volume1>" +
                "    <symbol>IBM</symbol>" +
                "    <price dt:dt=\"number\">75.6</price>" +
                "  </stock1>" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>null</volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <price dt:dt=\"number\">55.6</price>" +
                "  </stock>" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <symbol>null</symbol>" +
                "    <price dt:dt=\"number\">55.6</price>" +
                "  </stock>" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <price dt:dt=\"number\">null</price>" +
                "  </stock>" +
                "</portfolio>");
        //assert event count
        Assert.assertEquals("Number of events", 3, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom2() throws InterruptedException {
        log.info("Test case for xml input mapping with custom mapping. Here, only one event is sent in a message.");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes\", " +
                "enclosing.element=\"//portfolio\", @attributes(symbol = \"symbol\"" +
                "                                           , price = \"price\"" +
                "                                           , volume = \"volume\"))) " +
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
                            org.junit.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            org.junit.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <price dt:dt=\"number\">55.6</price>" +
                "  </stock>" +
                "</portfolio>");
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nyse\">" +
                "    <volume>200</volume>" +
                "    <symbol>IBM</symbol>" +
                "    <price dt:dt=\"number\">75.6</price>" +
                "  </stock>" +
                "</portfolio>");
        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom3() throws InterruptedException {
        log.info("Test case for xml input mapping with custom mapping with complex xpath to extract one attribute. " +
                "Here multiple events are sent in one message.");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes\", " +
                "enclosing.element=\"//portfolio\", @attributes(symbol = \"company/symbol\"" +
                "                                           , price = \"price\"" +
                "                                           , volume = \"volume\"))) " +
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
                            org.junit.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            org.junit.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <company>" +
                "       <symbol>WSO2</symbol>" +
                "    </company>" +
                "    <price dt:dt=\"number\">55.6</price>" +
                "  </stock>" +
                "  <stock exchange=\"nyse\">" +
                "    <volume>200</volume>" +
                "    <company>" +
                "       <symbol>IBM</symbol>" +
                "    </company>" +
                "    <price dt:dt=\"number\">75.6</price>" +
                "  </stock>" +
                "  <stock exchange=\"nyse\">" +
                "    <volume></volume>" +
                "    <company>" +
                "       <symbol>IBM</symbol>" +
                "    </company>" +
                "    <price dt:dt=\"number\">75.6</price>" +
                "  </stock>" +
                "</portfolio>");
        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom4() throws InterruptedException {
        log.info("Test case for xml input mapping with custom mapping where @attribute is not present");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes\", " +
                "enclosing.element=\"//portfolio\")) " +
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
                            org.junit.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            org.junit.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price>55.6</price>" +
                "<volume>100</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol>IBM</symbol><price>75.6</price>" +
                "<volume>10</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol1>IBM</symbol1><price>75.6</price>" +
                "<volume>10</volume></event></events>");

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom5() throws InterruptedException {
        log.info("Verify xml message correctly mapped without grouping element with correct xpath from root");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes\", " +
                "@attributes(symbol = \"//stock[1]/symbol\"" +
                "                                           , price = \"//stock[1]/price\"" +
                "                                           , volume = \"//stock[1]/volume\"))) " +
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
                            org.junit.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <price dt=\"number\">55.6</price>" +
                "  </stock>" +
                "  <stock exchange=\"nyse\">" +
                "    <volume>200</volume>" +
                "    <symbol>IBM</symbol>" +
                "    <price dt=\"number\">75.6</price>" +
                "  </stock>" +
                "</portfolio>");
        //assert event count
        Assert.assertEquals("Number of events", 1, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom6() throws InterruptedException {
        log.info("Verify xml message being dropped due to incorrect namespace in mapping");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:data\", " +
                "enclosing.element=\"//portfolio\", @attributes(symbol = \"symbol\"" +
                "                                           , price = \"dt:price\"" +
                "                                           , volume = \"volume\"))) " +
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
                            org.junit.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            org.junit.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <dt:price dt:at=\"number\">55.6</dt:price>" +
                "  </stock>" +
                "  <stock exchange=\"nyse\">" +
                "    <volume>200</volume>" +
                "    <symbol>IBM</symbol>" +
                "    <dt:price dt:at=\"number\">75.6</dt:price>" +
                "  </stock>" +
                "</portfolio>");
        //assert event count
        Assert.assertEquals("Number of events", 0, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom7() throws InterruptedException {
        log.info("Verify xml message being dropped without grouping element when incorrect xpath is used from root");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes\", " +
                "@attributes(symbol = \"symbol\"" +
                "                                           , price = \"price\"" +
                "                                           , volume = \"volume\"))) " +
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
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <price dt=\"number\">55.6</price>" +
                "  </stock>" +
                "  <stock exchange=\"nyse\">" +
                "    <volume>200</volume>" +
                "    <symbol>IBM</symbol>" +
                "    <price dt=\"number\">75.6</price>" +
                "  </stock>" +
                "</portfolio>");
        //assert event count
        Assert.assertEquals("Number of events", 0, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom8() throws InterruptedException {
        log.info("Verify xml message being dropped due to incorrect grouping element configuration");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes\", " +
                "enclosing.element=\"//portfolio11\", @attributes(symbol = \"symbol\"" +
                "                                           , price = \"price\"" +
                "                                           , volume = \"volume\"))) " +
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
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume>100</volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <price dt:dt=\"number\">55.6</price>" +
                "  </stock>" +
                "  <stock exchange=\"nyse\">" +
                "    <volume>200</volume>" +
                "    <symbol>IBM</symbol>" +
                "    <price dt:dt=\"number\">75.6</price>" +
                "  </stock>" +
                "</portfolio>");
        //assert event count
        Assert.assertEquals("Number of events", 0, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void testXmlInputMappingCustom9() throws InterruptedException {
        log.info("Verify xml message being dropped due to incorrect grouping element configuration");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes\", " +
                "enclosing.element=\"//portfolio\", @attributes(symbol1 = \"symbol\"" +
                "                                           , price = \"price\"" +
                "                                           , volume = \"volume\"))) " +
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
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom10() throws InterruptedException {
        log.info("Verify xml mapping when elements defined are non existent and fail.on.unknown.attribute is false");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = " +
                "\"dt=urn:schemas-microsoft-com:datatypes\", fail.on.unknown.attribute=\"false\"," +
                "enclosing.element=\"//portfolio\", @attributes(symbol = \"symbol\"" +
                "                                           , price = \"price\"" +
                "                                           , volume = \"volume\"))) " +
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
                            org.junit.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            org.junit.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });
        executionPlanRuntime.start();
        InMemoryBroker.publish("stock", "<?xml version=\"1.0\"?>" +
                "<portfolio xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                "  <stock exchange=\"nasdaq\">" +
                "    <volume></volume>" +
                "    <symbol>WSO2</symbol>" +
                "    <price dt:dt=\"number\">55.6</price>" +
                "  </stock>" +
                "  <stock exchange=\"nyse\">" +
                "    <volume>200</volume>" +
                "    <symbol>IBM</symbol>" +
                "    <price dt:dt=\"number\">75.6</price>" +
                "  </stock>" +
                "</portfolio>");
        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
        siddhiManager.shutdown();
    }
}
