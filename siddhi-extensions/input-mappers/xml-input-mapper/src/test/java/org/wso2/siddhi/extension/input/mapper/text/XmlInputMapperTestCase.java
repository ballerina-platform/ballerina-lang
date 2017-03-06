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

package org.wso2.siddhi.extension.input.mapper.text;

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
        log.info("Test case for text input mapping with default mapping");

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
        InMemoryBroker.publish("stock", "<events><event><symbol>WSO2</symbol><price>55.6</price>" +
                "<volume>100</volume></event></events>");
        InMemoryBroker.publish("stock", "<events><event><symbol>IBM</symbol><price>75.6</price>" +
                "<volume>10</volume></event></events>");

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom1() throws InterruptedException {
        log.info("Test case for text input mapping with custom mapping. Here multiple events are sent in one message.");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = \"dt=urn:schemas-microsoft-com:datatypes\", " +
                "parentSelector=\"//portfolio\", @attributes(symbol = \"symbol\"" +
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
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testXmlInputMappingCustom2() throws InterruptedException {
        log.info("Test case for text input mapping with custom mapping. Here, only one event is sent in a message.");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='xml', namespaces = \"dt=urn:schemas-microsoft-com:datatypes\", " +
                "parentSelector=\"//portfolio\", @attributes(symbol = \"symbol\"" +
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
    }
}
