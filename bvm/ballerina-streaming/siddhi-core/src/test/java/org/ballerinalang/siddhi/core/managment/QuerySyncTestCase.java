/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.siddhi.core.managment;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testcase for synchronized streaming queries.
 */
public class QuerySyncTestCase {
    private static final Logger log = LoggerFactory.getLogger(QuerySyncTestCase.class);
    private AtomicInteger inEventCount;
    private AtomicInteger removeEventCount;
    private boolean eventArrived;
    private AtomicInteger count;

    @BeforeMethod
    public void init() {
        count = new AtomicInteger(0);
        inEventCount = new AtomicInteger(0);
        removeEventCount = new AtomicInteger(0);
        eventArrived = false;
    }

    @Test
    public void querySyncTest1() throws InterruptedException {
        log.info("querySync test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "@synchronized('true') " +
                "from cseEventStream#window.time(2 sec) " +
                "select symbol,price,volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount.addAndGet(inEvents.length);
                }
                if (removeEvents != null) {
                    AssertJUnit.assertTrue("InEvents arrived before RemoveEvents", inEventCount.get() > removeEventCount
                            .get());
                    removeEventCount.addAndGet(removeEvents.length);
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(4000);
        AssertJUnit.assertEquals(2, inEventCount.get());
        AssertJUnit.assertEquals(2, removeEventCount.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void querySyncTest2() throws InterruptedException {
        log.info("querySync test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest3') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "@synchronized('true') " +
                "from LoginEvents " +
                "select ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        count.incrementAndGet();
                        AssertJUnit.assertTrue("192.10.1.3".equals(event.getData(0)) ||
                                "192.10.1.4".equals(event.getData(0)));
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);

        siddhiAppRuntime.shutdown();

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertTrue("Number of output event value", 3 == count.get());

    }

    @Test
    public void querySyncTest3() throws InterruptedException {
        log.info("querySync test3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "" +
                "@info(name = 'query1') " +
                "@synchronized('true') " +
                "from cseEventStream#window.time(1 sec) as a join twitterStream#window.time(1 sec) as b " +
                "on a.symbol== b.company " +
                "select a.symbol as symbol, b.tweet, a.price " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timestamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        inEventCount.addAndGet(inEvents.length);
                    }
                    if (removeEvents != null) {
                        removeEventCount.addAndGet(removeEvents.length);
                    }
                    eventArrived = true;
                }
            });

            InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
            InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
            siddhiAppRuntime.start();
            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
            twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
            Thread.sleep(500);
            cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});

            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            SiddhiTestHelper.waitForEvents(100, 2, removeEventCount, 60000);
            AssertJUnit.assertEquals(2, inEventCount.get());
            AssertJUnit.assertEquals(2, removeEventCount.get());
            AssertJUnit.assertTrue(eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void querySyncTest4() throws InterruptedException {
        log.info("querySync test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "@synchronized('true') " +
                "from every ( e1=Stream1[price>20] -> e3=Stream1[price>20]) -> e2=Stream2[price>e1.price] " +
                "select e1.price as price1, e3.price as price3, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                org.testng.AssertJUnit.assertArrayEquals(new Object[]{55.6f, 54f, 57.7f},
                                        event.getData());
                                break;
                            case 2:
                                org.testng.AssertJUnit.assertArrayEquals(new Object[]{53.6f, 53f, 57.7f},
                                        event.getData());
                                break;
                            default:
                                org.testng.AssertJUnit.assertSame(2, inEventCount);
                        }

                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount.addAndGet(removeEvents.length);
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 53.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 53f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 57.7f, 100});
        Thread.sleep(100);

        org.testng.AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        org.testng.AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }
}
