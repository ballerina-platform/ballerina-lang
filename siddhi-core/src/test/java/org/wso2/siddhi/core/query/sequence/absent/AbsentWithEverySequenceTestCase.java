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

package org.wso2.siddhi.core.query.sequence.absent;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Test the patterns:
 * - every A, not B for 1 sec
 */
public class AbsentWithEverySequenceTestCase {

    private static final Logger log = Logger.getLogger(AbsentWithEverySequenceTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;
    private List<AssertionError> assertionErrors = new ArrayList<>();

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("Test the query every e1, not e2 for 1 sec with e1 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price1 float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], not Stream2[price1>e1.price] for 1sec " +
                "select e1.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        addCallback(siddhiAppRuntime, "query1", new Object[]{"GOOG"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 55.6f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("Test the query every e1, not e2 for 1 sec with e1 and e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price1 float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], not Stream2[price1>e1.price] for 1sec " +
                "select e1.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testQuery3() throws InterruptedException {
        log.info("Test the query every e1, not e2 for 1 sec, e3 with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], not Stream2[price>e1.price] for 1sec, " +
                "e3=Stream3[price>e1.price] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        addCallback(siddhiAppRuntime, "query1", new Object[]{"GOOG", "IBM"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 55.6f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }


    private void addCallback(SiddhiAppRuntime siddhiAppRuntime, String queryName, Object[]... expected) {
        final int noOfExpectedEvents = expected.length;
        siddhiAppRuntime.addCallback(queryName, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    eventArrived = true;

                    for (Event event : inEvents) {
                        inEventCount++;
                        if (noOfExpectedEvents > 0 && inEventCount <= noOfExpectedEvents) {
                            try {
                                Assert.assertArrayEquals(expected[inEventCount - 1], event.getData());
                            } catch (AssertionError e) {
                                assertionErrors.add(e);
                            }
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });
    }
}
