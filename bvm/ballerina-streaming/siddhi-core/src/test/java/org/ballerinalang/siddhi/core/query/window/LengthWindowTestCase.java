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
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testcase for length window queries.
 */
public class LengthWindowTestCase {
    private static final Logger log = LoggerFactory.getLogger(LengthWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private int count;
    private boolean eventArrived;
    private Event storedEvent;


    @BeforeMethod
    public void init() {
        count = 0;
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
        storedEvent = null;
    }

    @Test
    public void lengthWindowTest1() throws InterruptedException {
        log.info("Testing length window with no of events smaller than window size");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.length(4) select symbol,price,volume insert" +
                " all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                AssertJUnit.assertEquals("Message order inEventCount", inEventCount, inEvents[0].getData(2));
                AssertJUnit.assertEquals("Events cannot be expired", false, inEvents[0].isExpired());
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(500);
        AssertJUnit.assertEquals(2, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void lengthWindowTest2() throws InterruptedException {
        log.info("Testing length window with no of events greater than window size");

        final int length = 4;
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.length(" + length + ") select symbol,price," +
                "volume insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    if (count >= length && count % 2 == 0) {
                        removeEventCount++;
                        AssertJUnit.assertEquals("Remove event order", removeEventCount, event.getData(2));
                        AssertJUnit.assertEquals("Expired event triggering position", inEventCount + 1,
                                length + removeEventCount);
                    } else {
                        inEventCount++;
                        AssertJUnit.assertEquals("In event order", inEventCount, event.getData(2));
                    }
                    count++;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(500);
        AssertJUnit.assertEquals("In event count", 6, inEventCount);
        AssertJUnit.assertEquals("Remove event count", 2, removeEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void lengthWindowTest3() throws InterruptedException {
        log.info("Testing length window with no of events greater than window size");

        final int length = 4;
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.length(" + length + ") select symbol,price," +
                "volume insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        if (event.isExpired()) {
                            removeEventCount++;
                        } else {
                            inEventCount++;
                        }
                    }
                }
                if (removeEvents != null) {
                    for (Event event : removeEvents) {
                        if (event.isExpired()) {
                            removeEventCount++;
                        } else {
                            inEventCount++;
                        }
                    }
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(500);
        AssertJUnit.assertEquals("In event count", 6, inEventCount);
        AssertJUnit.assertEquals("Remove event count", 2, removeEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }


    @Test
    public void lengthWindowTest4() throws InterruptedException {
        log.info("Testing length window with no of events smaller than window size");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int, price2 double, volume2 long, " +
                "active bool);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.length(4) " +
                "select " +
                "max(price) as maxp, min(price) as minp, sum(price) as sump, avg(price) as avgp, " +
                "stdDev(price) as stdp, count(price) as cp, distinctCount(price) as dcp," +
                "max(volume) as maxvolumep, min(volume) as minvolumep, sum(volume) as sumvolumep," +
                " avg(volume) as avgvolumep, " +
                "stdDev(volume) as stdvolumep, count(volume) as cvolumep, distinctCount(volume) as dcvolumep," +
                "max(price2) as maxprice2p, min(price2) as minprice2p, sum(price2) as sumprice2p," +
                " avg(price2) as avgprice2p, " +
                "stdDev(price2) as stdprice2p, count(price2) as cpprice2, distinctCount(price2) as dcprice2p," +
                "max(volume2) as maxvolume2p, min(volume2) as minvolume2p, sum(volume2) as sumvolume2p," +
                " avg(volume2) as avgvolume2p, " +
                "stdDev(volume2) as stdvolume2p, count(volume2) as cvolume2p, distinctCount(volume2) as dcvolume2p" +
                " " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                inEventCount = inEventCount + inEvents.length;
                if (inEventCount == 2) {
                    storedEvent = inEvents[0];
                } else if (inEventCount == 3) {
                    Assert.assertEquals(inEvents[0].getData(1), storedEvent.getData(1),
                            "2nd and 3rd message should be same");
                    Assert.assertEquals(inEvents[0].getData(2), storedEvent.getData(2),
                            "2nd and 3rd message should be same");
                    Assert.assertEquals(inEvents[0].getData(3), storedEvent.getData(3),
                            "2nd and 3rd message should be same");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{null, null, null, null, null, null});
        inputHandler.send(new Object[]{"IBM", 700F, 0, 0.0D, 5L, true});
        inputHandler.send(new Object[]{null, null, null, null, null, null});
        inputHandler.send(new Object[]{"IBM", 700F, 0, 0.0D, 5L, true});
        inputHandler.send(new Object[]{"IBM", 700F, 0, 0.0D, 5L, true});
        inputHandler.send(new Object[]{"IBM", 700F, 0, 0.0D, 5L, true});
        inputHandler.send(new Object[]{"IBM", 700F, 0, 0.0D, 5L, true});
        inputHandler.send(new Object[]{"IBM", 700F, 0, 0.0D, 5L, true});
        Thread.sleep(500);
        AssertJUnit.assertEquals(8, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

}


