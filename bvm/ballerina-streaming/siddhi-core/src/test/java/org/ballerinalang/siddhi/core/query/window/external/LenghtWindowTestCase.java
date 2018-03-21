/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.query.window.external;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LenghtWindowTestCase {
    private static final Logger log = LoggerFactory.getLogger(LenghtWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private int count;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testLengthWindow0() throws InterruptedException {
        log.info("Testing length window definition");
        SiddhiManager siddhiManager = new SiddhiManager();
        String window = "define window EventWindow1(symbol string, price int, volume float) length(5) output all " +
                "events; " +
                "define window EventWindow2(symbol string, price int, volume float) length(5);";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(window);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testLengthWindow1() throws InterruptedException {
        log.info("Testing length window with no of events smaller than window size");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseWindow (symbol string, price float, volume int) length(4) output all events; ";
        String query = "@info(name = 'query1') from cseEventStream select symbol,price,volume insert into cseWindow ;" +
                "@info(name = 'query2') from cseWindow insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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
    public void testLengthWindow2() throws InterruptedException {
        log.info("Testing length window with no of events greater than window size");

        final int length = 4;
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseWindow (symbol string, price float, volume int) length(" + length + ") output all " +
                "events; ";
        String query = "@info(name = 'query1') from cseEventStream select symbol,price,volume insert into cseWindow ;" +
                "@info(name = 'query2') from cseWindow insert all events into outputStream ;";

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
    public void testLengthWindow3() throws InterruptedException {
        log.info("Testing length window with no of events greater than window size");

        final int length = 4;
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseWindow (symbol string, price float, volume int) length(" + length + ") output all " +
                "events; ";
        String query = "@info(name = 'query1') from cseEventStream select symbol,price,volume insert into cseWindow ;" +
                "@info(name = 'query2') from cseWindow insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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
}
