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

package org.ballerinalang.siddhi.core.query.window;


import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testcase for sort window queries.
 */
public class SortWindowTestCase {
    private static final Logger log = LoggerFactory.getLogger(SortWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;
    private AtomicInteger atomicCount;

    @BeforeMethod
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
        atomicCount = new AtomicInteger(0);
    }

    @Test
    public void sortWindowTest1() throws InterruptedException {
        log.info("sortWindow test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.sort(2,volume, 'asc') " +
                "select volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 300L});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 200L});
        inputHandler.send(new Object[]{"WSO2", 55.6f, 20L});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 40L});
        Thread.sleep(1000);
        AssertJUnit.assertEquals(5, inEventCount);
        AssertJUnit.assertEquals(3, removeEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void sortWindowTest2() throws InterruptedException {
        log.info("sortWindow test2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String planName = "@app:name('sortWindow2') ";
        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price int, volume long);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.sort(2,volume, 'asc', price, 'desc') " +
                "select price, volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(planName +
                cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50, 100L});
        inputHandler.send(new Object[]{"IBM", 20, 100L});
        inputHandler.send(new Object[]{"WSO2", 40, 50L});
        inputHandler.send(new Object[]{"WSO2", 100, 20L});
        inputHandler.send(new Object[]{"WSO2", 50, 50L});
        Thread.sleep(1000);
        AssertJUnit.assertEquals(5, inEventCount);
        AssertJUnit.assertEquals(3, removeEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void sortWindowTest3() throws InterruptedException {
        log.info("sortWindowTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" + "define stream cseEventStream (symbol string, price float, index int); "
                + "define stream twitterStream (id int, tweet string, company string); ";
        String query = "" + "@info(name = 'query1') "
                + "from cseEventStream#window.sort(2, index) join twitterStream#window.sort(2, id) "
                + "on cseEventStream.symbol== twitterStream.company "
                + "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price "
                + "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        atomicCount.addAndGet(inEvents.length);
                    }
                    if (removeEvents != null) {
                        removeEventCount += (removeEvents.length);
                    }
                    eventArrived = true;
                }
            });
            InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
            InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
            siddhiAppRuntime.start();
            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
            cseEventStreamHandler.send(new Object[]{"IBM", 59.6f, 101});
            twitterStreamHandler.send(new Object[]{10, "Hello World", "WSO2"});
            twitterStreamHandler.send(new Object[]{15, "Hello World2", "WSO2"});
            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 90});
            twitterStreamHandler.send(new Object[]{5, "Hello World2", "IBM"});
            SiddhiTestHelper.waitForEvents(100, 3, atomicCount, 10000);
            AssertJUnit.assertEquals(3, atomicCount.get());
            AssertJUnit.assertTrue(eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void sortWindowTest4()
            throws InterruptedException {
        log.info("sortWindowTest4");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" + "define stream cseEventStream (symbol string, price float, volume int);";
        String query =
                "" + "@info(name = 'query1') " + "from cseEventStream#window.sort(2.5) " + "select symbol,price,volume "
                        + "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void sortWindowTest5()
            throws InterruptedException {
        log.info("sortWindowTest5");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" + "define stream cseEventStream (symbol string, time long, volume int);";
        String query = "" + "@info(name = 'query1') " + "from cseEventStream#window.sort(2, 8) "
                + "select symbol,price,volume " + "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void sortWindowTest6()
            throws InterruptedException {
        log.info("sortWindowTest6");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" + "define stream cseEventStream (symbol string, time long, volume int);";
        String query = "" + "@info(name = 'query1') " + "from cseEventStream#window.sort(2, volume, 'ecs') "
                + "select symbol,price,volume " + "insert all events into outputStream ;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

}
