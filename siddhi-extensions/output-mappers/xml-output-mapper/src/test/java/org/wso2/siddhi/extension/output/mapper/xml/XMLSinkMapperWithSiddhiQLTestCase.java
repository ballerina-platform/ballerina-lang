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
package org.wso2.siddhi.extension.output.mapper.xml;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.NoSuchAttributeException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.sink.InMemorySink;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class XMLSinkMapperWithSiddhiQLTestCase {
    private static final Logger log = Logger.getLogger(XMLSinkMapperWithSiddhiQLTestCase.class);
    private AtomicInteger wso2Count = new AtomicInteger(0);
    private AtomicInteger ibmCount = new AtomicInteger(0);

    @Before
    public void init() {
        wso2Count.set(0);
        ibmCount.set(0);
    }

    //    from FooStream
    //    select symbol,price,volume
    //    publish inMemory options ("topic", "{{symbol}}")
    //    map xml
    @Test
    public void testXMLSinkmapperDefaultMappingWithSiddhiQL() throws InterruptedException {
        log.info("Test default xml mapping with SiddhiQL");
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='xml')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.645f, 100L});
        stockStream.send(new Object[]{"IBM", 75f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"IBM", 57.6f, null});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 2, ibmCount.get());
        //assert default mapping
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol>WSO2</symbol>" +
                "<price>55.645</price><volume>100</volume></event></events>", onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol>IBM</symbol>" +
                "<price>75.0</price><volume>100</volume></event></events>", onMessageList.get(1).toString());
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol>WSO2</symbol>" +
                "<price>57.6</price><volume>100</volume></event></events>", onMessageList.get(2).toString());
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol>IBM</symbol>" +
                "<price>57.6</price><volume xsi:nil=\"true\"/></event></events>", onMessageList.get(3).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    @Test
    public void testXMLSinkmapperDefaultMappingWithSiddhiQL2() throws InterruptedException {
        log.info("Test default xml mapping with SiddhiQL for multiple events");
        AtomicInteger companyCount = new AtomicInteger(0);
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberCompany = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                companyCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "company";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberCompany);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='company', @map(type='xml')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        Event wso2Event = new Event(System.currentTimeMillis(), new Object[]{"WSO2#@$", 55.6f, 100L});
        Event ibmEvent = new Event(System.currentTimeMillis(), new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Event[]{wso2Event, ibmEvent});
        stockStream.send(new Object[]{null, null, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, companyCount.get());
        //assert default mapping
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol>WSO2#@$</symbol>" +
                "<price>55.6</price><volume>100</volume></event><event><symbol>IBM</symbol><price>75.6</price>" +
                "<volume>100</volume></event></events>", onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol xsi:nil=\"true\"/>" +
                "<price xsi:nil=\"true\"/><volume>100</volume></event></events>", onMessageList.get(1).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberCompany);
        siddhiManager.shutdown();
    }

    @Test
    public void testXMLSinkmapperDefaultMappingWithNullElementSiddhiQL() throws InterruptedException {
        log.info("Test default xml mapping with null elements");
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='xml')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, null});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert default mapping
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol>WSO2</symbol>" +
                "<price>55.6</price><volume xsi:nil=\"true\"/></event></events>", onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol>IBM</symbol>" +
                "<price>75.6</price><volume>100</volume></event></events>", onMessageList.get(1).toString());
        Assert.assertEquals("Incorrect mapping!", "<events><event><symbol>WSO2</symbol>" +
                "<price>57.6</price><volume>100</volume></event></events>", onMessageList.get(2).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    //    from FooStream
    //    select symbol,price
    //    publish inMemory options ("topic", "{{symbol}}")
    //    map xml custom
    @Test
    public void testXMLOutputCustomMappingWithoutXMLEnclosingElement() throws InterruptedException {
        log.info("Test custom xml mapping with SiddhiQL");
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='xml', @payload(" +
                "\"<StockData><Symbol>{{symbol}}</Symbol><Price>{{price}}</Price></StockData>\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";
        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert custom xml
        Assert.assertEquals("Incorrect mapping!", "<StockData><Symbol>WSO2</Symbol>" +
                "<Price>55.6</Price></StockData>", onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<StockData><Symbol>IBM</Symbol>" +
                "<Price>75.6</Price></StockData>", onMessageList.get(1).toString());
        Assert.assertEquals("Incorrect mapping!", "<StockData><Symbol>WSO2</Symbol>" +
                "<Price>57.6</Price></StockData>", onMessageList.get(2).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    @Test
    public void testXMLOutputCustomMappingWithXMLEnclosingElement() throws InterruptedException {
        log.info("Test custom xml mapping with SiddhiQL. Here, XML enclosing element is being provided for mapping.");
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', " +
                "@map(type='xml', enclosing.element=\"<portfolio xmlns:dt='urn:schemas-microsoft-com:datatypes'>\", " +
                "@payload(\"<dt:StockData><Symbol>{{symbol}}</Symbol><Price>{{price}}</Price></dt:StockData>\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert custom xml
        Assert.assertEquals("Incorrect mapping!", "<portfolio xmlns:dt='urn:schemas-microsoft-com:datatypes'>" +
                        "<dt:StockData><Symbol>WSO2</Symbol><Price>55.6</Price></dt:StockData></portfolio>",
                onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<portfolio xmlns:dt='urn:schemas-microsoft-com:datatypes'>" +
                        "<dt:StockData><Symbol>IBM</Symbol><Price>75.6</Price></dt:StockData></portfolio>",
                onMessageList.get(1).toString());
        Assert.assertEquals("Incorrect mapping!", "<portfolio xmlns:dt='urn:schemas-microsoft-com:datatypes'>" +
                        "<dt:StockData><Symbol>WSO2</Symbol><Price>57.6</Price></dt:StockData></portfolio>",
                onMessageList.get(2).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    @Test
    public void testXMLOutputCustomMappingWithCustomAttributes() throws InterruptedException {
        log.info("Test custom xml mapping with SiddhiQL for XML attributes");
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (id int, symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='xml', @payload(" +
                "\"<StockData><Symbol id=''{{id}}'' value=''{{price}}''>{{symbol}}</Symbol>"
                + "<Price company=''{{symbol}}''>{{price}}</Price></StockData>\"))) " +
                "define stream BarStream (id int, symbol string, price float, volume long); ";
        String query = "" +
                "from FooStream " +
                "select id, symbol, price, volume " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{1, "WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{2, "IBM", 75.6f, 100L});
        stockStream.send(new Object[]{3, "WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert custom xml
        Assert.assertEquals("Incorrect mapping!", "<StockData><Symbol id='1' value='55.6'>WSO2</Symbol>" +
                "<Price company='WSO2'>55.6</Price></StockData>", onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<StockData><Symbol id='2' value='75.6'>IBM</Symbol>" +
                "<Price company='IBM'>75.6</Price></StockData>", onMessageList.get(1).toString());
        Assert.assertEquals("Incorrect mapping!", "<StockData><Symbol id='3' value='57.6'>WSO2</Symbol>" +
                "<Price company='WSO2'>57.6</Price></StockData>", onMessageList.get(2).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    @Test
    public void testXMLOutputCustomMappingWithoutPayloadElement() throws InterruptedException {
        log.info("Test custom xml mapping with SiddhiQL. Here, payload element is missing");
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', " +
                "@map(type='xml', enclosing.element='<portfolio>')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 1, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert custom xml
        Assert.assertEquals("Incorrect mapping!", "<portfolio><event><symbol>WSO2</symbol><price>55.6" +
                "</price><volume>100</volume></event></portfolio>", onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<portfolio><event><symbol>IBM</symbol><price>75.6" +
                "</price><volume>100</volume></event></portfolio>", onMessageList.get(1).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    @Test
    public void testXMLSinkmapperCustomMappingWithNullAttributes() throws InterruptedException {
        log.info("Test default xml mapping with SiddhiQL for multiple events");
        AtomicInteger companyCount = new AtomicInteger(0);
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberCompany = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                companyCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "company";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberCompany);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='company', @map(type='xml', enclosing.element='<portfolio>', " +
                "@payload(\"<StockData><symbol>{{symbol}}</symbol><price>{{price}}</price><volume>{{volume}}</volume>" +
                "</StockData>\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{null, 56, 100L});
        stockStream.send(new Object[]{"WSO2", null, 100L});
        stockStream.send(new Object[]{"WSO2", 56, null});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 3, companyCount.get());
        //assert default mapping
        Assert.assertEquals("Incorrect mapping!", "<portfolio><StockData><symbol>null</symbol>" +
                "<price>56</price><volume>100</volume></StockData></portfolio>", onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<portfolio><StockData><symbol>WSO2</symbol>" +
                "<price>null</price><volume>100</volume></StockData></portfolio>", onMessageList.get(1).toString());
        Assert.assertEquals("Incorrect mapping!", "<portfolio><StockData><symbol>WSO2</symbol>" +
                "<price>56</price><volume>null</volume></StockData></portfolio>", onMessageList.get(2).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberCompany);
        siddhiManager.shutdown();
    }

    @Test
    public void testXMLOutputCustomMappingWithXMLValidation() throws InterruptedException {
        log.info("Test custom xml mapping where XML validation is enabled.");
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='xml', enclosing.element='<portfolio>', " +
                " validate.xml='true', @payload(" +
                "\"<StockData><Symbol>{{symbol}}</Symbol><Price>{{price}}</Price></StockData>\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert custom xml
        Assert.assertEquals("Incorrect mapping!", "<portfolio><StockData><Symbol>WSO2</Symbol>" +
                "<Price>55.6</Price></StockData></portfolio>", onMessageList.get(0).toString());
        Assert.assertEquals("Incorrect mapping!", "<portfolio><StockData><Symbol>IBM</Symbol>" +
                "<Price>75.6</Price></StockData></portfolio>", onMessageList.get(1).toString());
        Assert.assertEquals("Incorrect mapping!", "<portfolio><StockData><Symbol>WSO2</Symbol>" +
                "<Price>57.6</Price></StockData></portfolio>", onMessageList.get(2).toString());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    @Test
    public void negativeTestXMLOutputCustomMappingWithXMLValidation() throws InterruptedException {
        log.info("Negative test case for testing whether events are dropped when XML validation is enabled" +
                " and a malformed XML event is generated as a result of output mapping.");
        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='xml', enclosing.element='<portfolio>', " +
                "validate.xml='true', @payload(" +
                "\"<StockData data><Symbol>{{symbol}}</Symbol><Price>{{price}}</Price></StockData data>\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 0, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 0, ibmCount.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    @Test
    public void testXMLOutputCustomMappingWithoutXMLValidation() throws InterruptedException {
        log.info("Test case for testing whether malformed events are delivered when xml validation is off");
        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='xml', enclosing.element='<portfolio>', " +
                "validate.xml='false', @payload(" +
                "\"<StockData data><Symbol>{{symbol}}</Symbol><Price>{{price}}</Price></StockData data>\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
        siddhiManager.shutdown();
    }

    //    from FooStream
    //    select symbol,price
    //    publish inMemory options ("topic", "{{symbol}}")
    //    map xml custom
    @Test(expected = NoSuchAttributeException.class)
    public void testNoSuchAttributeExceptionForXMLOutputMapping() throws InterruptedException {
        log.info("Test for non existing attribute in xml mapping with SiddhiQL - expects NoSuchAttributeException");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='xml', @payload(" +
                "\"<StockData><Symbol>{{non-exist}}</Symbol><Price>{{price}}</Price></StockData>\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink:inMemory", InMemorySink.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        siddhiManager.shutdown();
    }
}
