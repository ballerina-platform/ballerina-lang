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

package org.wso2.siddhi.core.aggregation;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.exception.StoreQueryCreationException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AggregationTestCase {

    private static final Logger LOG = Logger.getLogger(AggregationTestCase.class);
    private AtomicInteger inEventCount;
    private AtomicInteger removeEventCount;
    private boolean eventArrived;
    private List<Object[]> inEventsList;
    private List<Object[]> removeEventsList;
    @BeforeMethod
    public void init() {
        inEventCount = new AtomicInteger(0);
        removeEventCount = new AtomicInteger(0);
        eventArrived = false;
        inEventsList = new ArrayList<>();
        removeEventsList = new ArrayList<>();
    }

    @Test
    public void incrementalStreamProcessorTest1() {
        LOG.info("incrementalStreamProcessorTest1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query1') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " aggregate by arrival every sec ... min";

        siddhiManager.createSiddhiAppRuntime(stockStream + query);
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest1"})
    public void incrementalStreamProcessorTest2() {
        LOG.info("incrementalStreamProcessorTest2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query2') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " aggregate every sec ... min";

        siddhiManager.createSiddhiAppRuntime(stockStream + query);
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest2"})
    public void incrementalStreamProcessorTest3() {
        LOG.info("incrementalStreamProcessorTest3");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query3') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " group by price " +
                " aggregate every sec, min, hour, day";

        siddhiManager.createSiddhiAppRuntime(stockStream + query);
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest3"})
    public void incrementalStreamProcessorTest4() {
        LOG.info("incrementalStreamProcessorTest4");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query3') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " group by price, volume " +
                " aggregate every sec, min, hour, day";

        siddhiManager.createSiddhiAppRuntime(stockStream + query);
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest4"})
    public void incrementalStreamProcessorTest5() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest5");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query = " define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...hour ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
        siddhiAppRuntime.start();

        stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});
        Thread.sleep(100);

        Event[] events = siddhiAppRuntime.query("from stockAggregation " +
                "within \"2017-06-** **:**:**\" " +
                "per \"seconds\"");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(3, events.length);
        for (int i = 0; i < events.length; i++) {
            switch (i) {
                case 0:
                    AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 60.0, 120.0, 700f}, events[i].getData());
                    break;
                case 1:
                    AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 80.0, 160.0, 1600f}, events[i].getData());
                    break;
                case 3:
                    AssertJUnit.assertArrayEquals(new Object[]{"IBM", 100.0, 200.0, 9600f}, events[i].getData());
                    break;
                default:
                    AssertJUnit.assertEquals(3, events.length);
            }
        }

        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest5"})
    public void incrementalStreamProcessorTest6() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest6");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query = "" +
                "@BufferSize('3') " +
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...year  ; " +

                "define stream inputStream (symbol string, value int, startTime string, endTime string, " +
                "perValue string); " +

                "@info(name = 'query1') " +
                "from inputStream as i join stockAggregation as s " +
                "within i.startTime, i.endTime " +
                "per i.perValue " +
                "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  " +
                "insert all events into outputStream; ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    if (inEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
            InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
            siddhiAppRuntime.start();

            // Thursday, June 1, 2017 4:05:50 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:51 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289951000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289951000L});

            // Thursday, June 1, 2017 4:05:52 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289952000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289952000L});

            // Thursday, June 1, 2017 4:05:53 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289953000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289953000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496289953000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 140f, null, 200L, 11, 1496289953000L});

            // Thursday, June 1, 2017 4:05:54 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496289954000L});

            // Thursday, June 1, 2017 4:06:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 1000f, null, 200L, 9, 1496290016000L});

            Thread.sleep(100);
            inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 04:05:50",
                    "2017-06-01 04:06:57", "seconds"});
            Thread.sleep(100);

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"WSO2", 60.0, 240.0, 700f},
                    new Object[]{"IBM", 600.0, 600.0, 3600f},
                    new Object[]{"IBM", 100.0, 200.0, 9600f},
                    new Object[]{"IBM", 700.0, 1400.0, 3500f},
                    new Object[]{"IBM", 400.0, 400.0, 3600f},
                    new Object[]{"WSO2", 100.0, 300.0, 1540f},
                    new Object[]{"IBM", 1000.0, 1000.0, 9000f}
            );
            SiddhiTestHelper.waitForEvents(100, 7, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Remove events matched", true, SiddhiTestHelper.isEventsMatch(
                    removeEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 7, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 7, removeEventCount.get());
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        }   finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest6"})
    public void incrementalStreamProcessorTest7() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest7");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp string);";
        String query = "" +
                "@BufferSize('3') " +
                "@IgnoreEventsOlderThanBuffer('true')" +
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...year; " +

                "define stream inputStream (symbol string, value int, startTime string, " +
                "endTime string, perValue string); " +

                "@info(name = 'query1') " +
                "from inputStream as i join stockAggregation as s " +
                "on i.symbol == s.symbol " +
                "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" " +
                "per i.perValue " +
                "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  " +
                "insert all events into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    if (inEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
            InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
            siddhiAppRuntime.start();

            // Thursday, June 1, 2017 4:05:50 AM (add 5.30 to get corresponding IST time)
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, "2017-06-01 04:05:50"});

            // Thursday, June 1, 2017 4:05:51 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, "2017-06-01 04:05:51"});

            // Thursday, June 1, 2017 4:05:52 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, "2017-06-01 04:05:52"});
            stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, "2017-06-01 04:05:52"});

            // Thursday, June 1, 2017 4:05:50 AM (out of order. must be processed with 1st event for 50th second)
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, "2017-06-01 04:05:50"});

            // Thursday, June 1, 2017 4:05:54 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, "2017-06-01 04:05:54"});
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, "2017-06-01 04:05:54"});

            // Thursday, June 1, 2017 4:05:50 AM (out of order. should not be processed since events for 50th second is
            // no longer in the buffer and @IgnoreEventsOlderThanBuffer is true)
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, "2017-06-01 04:05:50"});

            // Thursday, June 1, 2017 4:05:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, "2017-06-01 04:05:56"});
            stockStreamInputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, "2017-06-01 04:05:56"});

            // Thursday, June 1, 2017 4:06:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, "2017-06-01 04:06:56"});

            // Thursday, June 1, 2017 4:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, "2017-06-01 04:07:56"});

            // Thursday, June 1, 2017 5:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 700f, null, 200L, 20, "2017-06-01 05:07:56"});

            Thread.sleep(100);
            inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "minutes"});
            Thread.sleep(100);

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 330.0, 1650.0, 3500f},
                    new Object[]{"IBM", 400.0, 400.0, 3600f},
                    new Object[]{"IBM", 700.0, 700.0, 14000f},
                    new Object[]{"IBM", 600.0, 600.0, 3600f}
            );
            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Remove events matched", true, SiddhiTestHelper.isEventsMatch(
                    removeEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 4, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 4, removeEventCount.get());
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest7"})
    public void incrementalStreamProcessorTest8() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest8");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp string);";
        String query = "" +
                "@BufferSize('3') " +
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...year; " +

                "define stream inputStream (symbol string, value int, startTime string, " +
                "endTime string, perValue string); " +

                "@info(name = 'query1') " +
                "from inputStream as i join stockAggregation as s " +
                "on i.symbol == s.symbol " +
                "within \"2017-06-01 09:35:00 +05:30\", \"2017-06-01 10:37:57 +05:30\" " +
                "per \"minutes\" " +
                "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  " +
                "insert all events into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    if (inEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
            InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
            siddhiAppRuntime.start();

            // Thursday, June 1, 2017 4:05:50 AM (add 5.30 to get corresponding IST time)
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, "2017-06-01 04:05:50"});

            // Thursday, June 1, 2017 4:05:51 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, "2017-06-01 04:05:51"});

            // Thursday, June 1, 2017 4:05:52 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, "2017-06-01 04:05:52"});
            stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, "2017-06-01 04:05:52"});

            // Thursday, June 1, 2017 4:05:50 AM (out of order. must be processed with 1st event for 50th second)
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, "2017-06-01 04:05:50"});

            // Thursday, June 1, 2017 4:05:54 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, "2017-06-01 04:05:54"});
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, "2017-06-01 04:05:54"});

            // Thursday, June 1, 2017 4:05:50 AM (out of order and older than buffer. should be processed with the
            // current minimum event [which is 51st second] in the buffer since @IgnoreEventsOlderThanBuffer is not set.
            // IgnoreEventsOlderThanBuffer is false by default)
            stockStreamInputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, "2017-06-01 04:05:50"});

            // Thursday, June 1, 2017 4:05:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, "2017-06-01 04:05:56"});
            stockStreamInputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, "2017-06-01 04:05:56"});

            // Thursday, June 1, 2017 4:06:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, "2017-06-01 04:06:56"});

            // Thursday, June 1, 2017 4:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, "2017-06-01 04:07:56"});

            // Thursday, June 1, 2017 5:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 700f, null, 200L, 20, "2017-06-01 05:07:56"});

            Thread.sleep(100);
            inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "minutes"});
            Thread.sleep(100);

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 283.3333333333333, 1700.0, 3500f},
                    new Object[]{"IBM", 400.0, 400.0, 3600f},
                    new Object[]{"IBM", 700.0, 700.0, 14000f},
                    new Object[]{"IBM", 600.0, 600.0, 3600f}
            );
            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Remove events matched", true, SiddhiTestHelper.isEventsMatch(
                    removeEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 4, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 4, removeEventCount.get());
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest8"})
    public void incrementalStreamProcessorTest9() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest9");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query =
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) as lastTradeValue, " +
                "count() as count " +
                "aggregate by timestamp every sec...year ;" +

                "define stream inputStream (symbol string, value int, startTime string, " +
                "endTime string, perValue string); " +

                "@info(name = 'query1') " +
                "from inputStream as i join stockAggregation as s " +
                "within 1496200000000L, 1596434876000L " +
                "per \"days\" " +
                "select s.avgPrice, totalPrice, lastTradeValue, count  " +
                "insert all events into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    if (inEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }
            });
            InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
            InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
            siddhiAppRuntime.start();

            // Thursday, June 1, 2017 4:05:50 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:52 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

            // Thursday, June 1, 2017 4:05:54 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});

            // Thursday, June 1, 2017 4:05:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289956000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289956000L});

            // Thursday, June 1, 2017 4:06:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496290016000L});

            // Thursday, June 1, 2017 4:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496290076000L});

            // Thursday, June 1, 2017 5:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 700f, null, 200L, 20, 1496293676000L});

            // Thursday, June 1, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496297276000L});

            // Friday, June 2, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 800f, null, 100L, 10, 1496383676000L});

            // Saturday, June 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 900f, null, 100L, 15, 1496470076000L});

            // Monday, July 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1499062076000L});

            // Thursday, August 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1501740476000L});

            // Friday, August 3, 2018 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 6, 1533276476000L});

            // Saturday, August 3, 2019 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 260f, 44f, 200L, 16, 1564812476000L});

            // Monday, August 3, 2020 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1596434876000L});

            Thread.sleep(100);

            inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(100);

            List<Object[]> expected = Arrays.asList(
                    new Object[]{303.3333333333333, 3640.0, 3360f, 12L},
                    new Object[]{800.0, 800.0, 8000f, 1L},
                    new Object[]{900.0, 900.0, 13500f, 1L},
                    new Object[]{100.0, 100.0, 9600f, 1L},
                    new Object[]{60.0, 60.0, 360f, 1L},
                    new Object[]{400.0, 400.0, 3600f, 1L},
                    new Object[]{260.0, 260.0, 4160f, 1L},
                    new Object[]{260.0, 260.0, 4160f, 1L}
            );
            SiddhiTestHelper.waitForEvents(100, 8, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Remove events matched", true, SiddhiTestHelper.isEventsMatch(
                    removeEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 8, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 8, removeEventCount.get());
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        }  finally {
            siddhiAppRuntime.shutdown();
        }
    }


    @Test(dependsOnMethods = {"incrementalStreamProcessorTest9"})
    public void incrementalStreamProcessorTest10() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest10");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query =
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, " +
                        "(price * quantity) as lastTradeValue " +
                "group by symbol " +
                "aggregate by timestamp every sec...year; " +

                "define stream inputStream (symbol string, value int, startTime string, " +
                "endTime string, perValue string); " +

                "@info(name = 'query1') " +
                "from inputStream as i join stockAggregation as s " +
                "within \"2017-06-** **:**:**\" " +
                "per \"seconds\" " +
                "select s.symbol, lastTradeValue, totalPrice " +
                "insert all events into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    if (inEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }
            });
            InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
            InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
            siddhiAppRuntime.start();

            // Thursday, June 1, 2017 4:05:50 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:52 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

            // Thursday, June 1, 2017 4:05:50 AM (out of order. since there's no buffer, this must be processed with
            // 52nd second's data. since IgnoreEventsOlderThanBuffer is false by default, the event must not be dropped)
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:54 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});

            // Thursday, June 1, 2017 4:05:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289956000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289956000L});

            // Thursday, June 1, 2017 4:06:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496290016000L});

            // Thursday, June 1, 2017 4:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496290076000L});

            // Thursday, June 1, 2017 5:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 700f, null, 200L, 20, 1496293676000L});

            // Thursday, June 1, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496297276000L});

            // Friday, June 2, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 800f, null, 100L, 10, 1496383676000L});

            // Saturday, June 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 900f, null, 100L, 15, 1496470076000L});

            // Monday, July 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1499062076000L});

            // Thursday, August 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1501740476000L});

            // Friday, August 3, 2018 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 6, 1533276476000L});

            // Saturday, August 3, 2019 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 260f, 44f, 200L, 16, 1564812476000L});

            // Monday, August 3, 2020 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1596434876000L});

            // Monday, December 3, 2020 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1606975676000L});

            Thread.sleep(100);
            inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(100);

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"WSO2", 700f, 120.0},
                    new Object[]{"WSO2", 700f, 280.0},
                    new Object[]{"IBM", 9600f, 200.0},
                    new Object[]{"IBM", 3500f, 1400.0},
                    new Object[]{"IBM", 3600f, 400.0},
                    new Object[]{"IBM", 3600f, 600.0},
                    new Object[]{"CISCO", 14000f, 700.0},
                    new Object[]{"WSO2", 3360f, 60.0},
                    new Object[]{"CISCO", 8000f, 800.0},
                    new Object[]{"CISCO", 13500f, 900.0}
            );
            SiddhiTestHelper.waitForEvents(100, 10, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Remove events matched", true, SiddhiTestHelper.isEventsMatch(
                    removeEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 10, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 10, removeEventCount.get());
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest10"})
    public void incrementalStreamProcessorTest11() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest11");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query =
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, " +
                        "(price * quantity) as lastTradeValue  " +
                "group by symbol " +
                "aggregate every sec...hour ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
        siddhiAppRuntime.start();

        stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});
        Thread.sleep(2000);

        stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});
        Thread.sleep(2000);

        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});
        Thread.sleep(2000);

        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        Integer month = currentDate.getMonth().getValue();
        if (month.toString().length() == 1) {
            month = Integer.parseInt("0".concat(month.toString()));
        }

        Event[] events = siddhiAppRuntime.query("from stockAggregation " +
                "on symbol == \"IBM\" " +
                "within \"" + year + "-" + month + "-** **:**:** +05:30\" " +
                "per \"seconds\"; ");

        EventPrinter.print(events);

        AssertJUnit.assertEquals(1, events.length);
        for (int i = 0; i < events.length; i++) {
            switch (i) {
                case 0:
                    AssertJUnit.assertArrayEquals(new Object[]{"IBM", 100.0, 200.0, 9600f}, events[i].getData());
                    break;
                default:
                    AssertJUnit.assertEquals(1, events.length);
            }
        }

        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest11"})
    public void incrementalStreamProcessorTest12() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest12");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query = "" +
                "@BufferSize('3') " +
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, " +
                    "(price * quantity) as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...year  ; " +

                "define stream inputStream (symbol string, value int, startTime string, " +
                "endTime string, perValue string); " +

                "@info(name = 'query1') " +
                "from inputStream as i join stockAggregation as s " +
                "within \"2017-06-01 04:05:50\", \"2017-06-01 04:06:57\" " +
                "per \"seconds\" " +
                "select totalPrice, avgPrice, lastTradeValue, s.symbol " +
                "insert all events into outputStream; ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    if (inEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
            InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
            siddhiAppRuntime.start();

            // Thursday, June 1, 2017 4:05:50 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:53 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289953000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289953000L});

            // Thursday, June 1, 2017 4:05:52 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289952000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289952000L});

            // Thursday, June 1, 2017 4:05:51 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289951000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289951000L});

            // Thursday, June 1, 2017 4:05:53 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496289953000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 140f, null, 200L, 11, 1496289953000L});

            // Thursday, June 1, 2017 4:05:50 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:54 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496289954000L});

            // Thursday, June 1, 2017 4:06:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 1000f, null, 200L, 9, 1496290016000L});

            Thread.sleep(100);
            inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(100);

            List<Object[]> expected = Arrays.asList(
                    new Object[]{240.0, 60.0, 700f, "WSO2"},
                    new Object[]{600.0, 600.0, 3600f, "IBM"},
                    new Object[]{200.0, 100.0, 9600f, "IBM"},
                    new Object[]{1400.0, 700.0, 3500f, "IBM"},
                    new Object[]{400.0, 400.0, 3600f, "IBM"},
                    new Object[]{300.0, 100.0, 1540f, "WSO2"},
                    new Object[]{1000.0, 1000.0, 9000f, "IBM"}
            );
            SiddhiTestHelper.waitForEvents(100, 7, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Remove events matched", true, SiddhiTestHelper.isEventsMatch(
                    removeEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 7, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 7, removeEventCount.get());
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        }   finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest12"}, expectedExceptions =
            SiddhiAppCreationException.class)
    public void incrementalStreamProcessorTest13() {
        LOG.info("incrementalStreamProcessorTest13");
        SiddhiManager siddhiManager = new SiddhiManager();

        String query = "" +
                " @info(name = 'query1') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " aggregate by arrival every sec ... min";

        siddhiManager.createSiddhiAppRuntime(query);
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest13"}, expectedExceptions = SiddhiParserException.class)
    public void incrementalStreamProcessorTest14() {
        LOG.info("incrementalStreamProcessorTest14");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query =
                " @info(name = 'query1') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " aggregate by arrival every week";

        siddhiManager.createSiddhiAppRuntime(stockStream + query);
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest14"},
            expectedExceptions = SiddhiAppCreationException.class)
    public void incrementalStreamProcessorTest15() {
        LOG.info("incrementalStreamProcessorTest15");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query3') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " group by price " +
                " aggregate every sec, hour, day";

        siddhiManager.createSiddhiAppRuntime(stockStream + query);
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest15"}, expectedExceptions =
            SiddhiAppRuntimeException.class)
    public void incrementalStreamProcessorTest16() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest16");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp string);";
        String query = "" +
                "@BufferSize('3') " +
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, " +
                "(price * quantity) as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...year  ; ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
        siddhiAppRuntime.start();

        // Thursday, June 1, 2017 4:05:50 AM
        stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, "June 1, 2017 4:05:50 AM"});
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest16"})
    public void incrementalStreamProcessorTest17() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest17");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query =
                "define aggregation stockAggregation " +
                        "from stockStream " +
                        "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, " +
                        "(price * quantity) as lastTradeValue " +
                        "group by symbol " +
                        "aggregate by timestamp every sec...year; " +

                        "define stream inputStream (symbol string, value int, startTime string, " +
                        "endTime string, perValue string); " +

                        "@info(name = 'query1') " +
                        "from inputStream as i join stockAggregation as s " +
                        "within \"2017-01-01 00:00:00\", \"2021-01-01 00:00:00\" " +
                        "per \"months\" " +
                        "select s.symbol, avgPrice, totalPrice " +
                        "insert all events into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    if (inEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }
            });
            InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
            InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
            siddhiAppRuntime.start();

            // Thursday, June 1, 2017 4:05:50 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:52 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

            // Thursday, June 1, 2017 4:05:50 AM (out of order. since there's no buffer, this must be processed with
            // 52nd second's data. since IgnoreEventsOlderThanBuffer is false by default, the event must not be dropped)
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:54 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});

            // Thursday, June 1, 2017 4:05:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289956000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289956000L});

            // Thursday, June 1, 2017 4:06:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496290016000L});

            // Thursday, June 1, 2017 4:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496290076000L});

            // Thursday, June 1, 2017 5:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 700f, null, 200L, 20, 1496293676000L});

            // Thursday, June 1, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496297276000L});

            // Friday, June 2, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 800f, null, 100L, 10, 1496383676000L});

            // Saturday, June 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 900f, null, 100L, 15, 1496470076000L});

            // Monday, July 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1499062076000L});

            // Thursday, August 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1501740476000L});

            // Friday, August 3, 2018 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 6, 1533276476000L});

            // Saturday, August 3, 2019 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 260f, 44f, 200L, 16, 1564812476000L});

            // Monday, August 3, 2020 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1596434876000L});

            // Monday, December 3, 2020 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1606975676000L});

            Thread.sleep(100);
            inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(100);

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"CISCO", 800.0, 2400.0},
                    new Object[]{"IBM", 433.3333333333333, 2600.0},
                    new Object[]{"WSO2", 65.71428571428571, 460.0},
                    new Object[]{"IBM", 100.0, 100.0},
                    new Object[]{"CISCO", 260.0, 260.0},
                    new Object[]{"WSO2", 260.0, 260.0},
                    new Object[]{"IBM", 400.0, 400.0},
                    new Object[]{"WSO2", 60.0, 60.0},
                    new Object[]{"CISCO", 260.0, 260.0}
            );
            SiddhiTestHelper.waitForEvents(100, 9, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Remove events matched", true, SiddhiTestHelper.isEventsMatch(
                    removeEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 9, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 9, removeEventCount.get());
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest17"})
    public void incrementalStreamProcessorTest18() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest18");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query =
                "define aggregation stockAggregation " +
                        "from stockStream " +
                        "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, " +
                        "(price * quantity) as lastTradeValue " +
                        "group by symbol " +
                        "aggregate by timestamp every sec...year; " +

                        "define stream inputStream (symbol string, value int, startTime string, " +
                        "endTime string, perValue string); " +

                        "@info(name = 'query1') " +
                        "from inputStream as i join stockAggregation as s " +
                        "within \"2017-01-01 00:00:00\", \"2021-01-01 00:00:00\" " +
                        "per \"years\" " +
                        "select s.symbol, avgPrice, totalPrice " +
                        "insert all events into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    if (inEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        EventPrinter.print(timestamp, inEvents, removeEvents);
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }
            });
            InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
            InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
            siddhiAppRuntime.start();

            // Thursday, June 1, 2017 4:05:50 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:52 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

            // Thursday, June 1, 2017 4:05:50 AM (out of order. since there's no buffer, this must be processed with
            // 52nd second's data. since IgnoreEventsOlderThanBuffer is false by default, the event must not be dropped)
            stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
            stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

            // Thursday, June 1, 2017 4:05:54 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});

            // Thursday, June 1, 2017 4:05:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289956000L});
            stockStreamInputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289956000L});

            // Thursday, June 1, 2017 4:06:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496290016000L});

            // Thursday, June 1, 2017 4:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496290076000L});

            // Thursday, June 1, 2017 5:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 700f, null, 200L, 20, 1496293676000L});

            // Thursday, June 1, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496297276000L});

            // Friday, June 2, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 800f, null, 100L, 10, 1496383676000L});

            // Saturday, June 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 900f, null, 100L, 15, 1496470076000L});

            // Monday, July 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1499062076000L});

            // Thursday, August 3, 2017 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1501740476000L});

            // Friday, August 3, 2018 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 6, 1533276476000L});

            // Saturday, August 3, 2019 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"WSO2", 260f, 44f, 200L, 16, 1564812476000L});

            // Monday, August 3, 2020 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1596434876000L});

            // Monday, December 3, 2020 6:07:56 AM
            stockStreamInputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1606975676000L});

            Thread.sleep(100);
            inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                    "2017-06-01 09:35:52 +05:30", "seconds"});
            Thread.sleep(100);

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"WSO2", 60.0, 60.0},
                    new Object[]{"CISCO", 260.0, 520.0},
                    new Object[]{"CISCO", 800.0, 2400.0},
                    new Object[]{"IBM", 387.5, 3100.0},
                    new Object[]{"WSO2", 65.71428571428571, 460.0},
                    new Object[]{"WSO2", 260.0, 260.0}
            );
            SiddhiTestHelper.waitForEvents(100, 6, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Remove events matched", true, SiddhiTestHelper.isEventsMatch(
                    removeEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 6, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 6, removeEventCount.get());
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest18"}, expectedExceptions = SiddhiParserException.class)
    public void incrementalStreamProcessorTest19() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest19");
        SiddhiManager siddhiManager = new SiddhiManager();

        String query =  "define stream inputStream (symbol string, value int, startTime string, " +
                        "endTime string, perValue string); " +

                        "@info(name = 'query1') " +
                        "from inputStream as i join stockAggregation as s " +
                        "within \"2017-01-01 00:00:00\", \"2021-01-01 00:00:00\" " +
                        "per \"months\" " +
                        "select s.symbol, avgPrice, totalPrice " +
                        "insert all events into outputStream; ";

        siddhiManager.createSiddhiAppRuntime(query);
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest19"}, expectedExceptions =
            StoreQueryCreationException.class)
    public void incrementalStreamProcessorTest20() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest20");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream);
        siddhiAppRuntime.start();

        siddhiAppRuntime.query("from stockAggregation " +
                "on symbol == \"IBM\" " +
                "within \"2017-**-** **:**:** +05:30\" " +
                "per \"seconds\"; ");

        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest20"}, expectedExceptions =
            StoreQueryCreationException.class)
    public void incrementalStreamProcessorTest21() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest21");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query = " define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...hour ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        siddhiAppRuntime.start();
        Thread.sleep(100);

        Event[] events = siddhiAppRuntime.query("from stockAggregation " +
                "within \"2017-06-** **:**:**\" " +
                "per \"days\"");
        EventPrinter.print(events);

        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest21"})
    public void incrementalStreamProcessorTest22() throws InterruptedException {
        // Error gets logged at AbstractStreamProcessor level and event gets dropped. Hence no expectedExceptions for
        // this test case
        LOG.info("incrementalStreamProcessorTest22");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query =
                "define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...hour  ; " +

                "define stream inputStream (symbol string, value int, startTime string, endTime string, " +
                "perValue string); " +

                "@info(name = 'query1') " +
                "from inputStream as i join stockAggregation as s " +
                "within \"2017-06-** **:**:**\" " +
                "per \"days\" " +
                "select s.symbol, avgPrice, totalPrice as sumPrice, lastTradeValue  " +
                "insert all events into outputStream; ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        InputHandler inputStreamInputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputStreamInputHandler.send(new Object[]{"IBM", 1, "2017-06-01 09:35:51 +05:30",
                "2017-06-01 09:35:52 +05:30", "seconds"});

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest22"})
    public void incrementalStreamProcessorTest23() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest23");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query = " define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...hour ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
        siddhiAppRuntime.start();

        stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});
        Thread.sleep(100);

        Event[] events = siddhiAppRuntime.query("from stockAggregation " +
                "on symbol==\"IBM\" " +
                "within \"2017-06-** **:**:**\" " +
                "per \"seconds\" " +
                "select symbol, avgPrice;");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(1, events.length);
        for (int i = 0; i < events.length; i++) {
            switch (i) {
                case 0:
                    AssertJUnit.assertArrayEquals(new Object[]{"IBM", 100.0}, events[i].getData());
                    break;
                default:
                    AssertJUnit.assertEquals(1, events.length);
            }
        }

        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest23"})
    public void incrementalStreamProcessorTest24() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest24");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query = " define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...hour ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
        siddhiAppRuntime.start();

        stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});
        Thread.sleep(100);

        Event[] events = siddhiAppRuntime.query("from stockAggregation " +
                "within \"2017-06-** **:**:**\" " +
                "per \"seconds\"");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(3, events.length);
        for (int i = 0; i < events.length; i++) {
            switch (i) {
                case 0:
                    AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 60.0, 120.0, 700f}, events[i].getData());
                    break;
                case 1:
                    AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 80.0, 160.0, 1600f}, events[i].getData());
                    break;
                case 3:
                    AssertJUnit.assertArrayEquals(new Object[]{"IBM", 100.0, 200.0, 9600f}, events[i].getData());
                    break;
                default:
                    AssertJUnit.assertEquals(3, events.length);
            }
        }

        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"incrementalStreamProcessorTest24"})
    public void incrementalStreamProcessorTest25() throws InterruptedException {
        LOG.info("incrementalStreamProcessorTest25");
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query = " define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...hour ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream + query);

        InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
        siddhiAppRuntime.start();

        stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});
        Thread.sleep(100);

        Event[] events = siddhiAppRuntime.query("from stockAggregation " +
                "within \"2017-06-** **:**:**\" " +
                "per \"seconds\" " +
                "select *; ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(3, events.length);
        for (int i = 0; i < events.length; i++) {
            switch (i) {
                case 0:
                    AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 60.0, 120.0, 700f}, events[i].getData());
                    break;
                case 1:
                    AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 80.0, 160.0, 1600f}, events[i].getData());
                    break;
                case 3:
                    AssertJUnit.assertArrayEquals(new Object[]{"IBM", 100.0, 200.0, 9600f}, events[i].getData());
                    break;
                default:
                    AssertJUnit.assertEquals(3, events.length);
            }
        }

        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
    }
}
