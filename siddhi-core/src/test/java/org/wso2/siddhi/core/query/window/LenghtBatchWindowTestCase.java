/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.query.window;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class LenghtBatchWindowTestCase {
    private static final Logger log = Logger.getLogger(LenghtBatchWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void LengthBatchWindowTest1() throws InterruptedException {
        log.info("Testing length batch window with no of events smaller than window size");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.lengthBatch(4) select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.fail("No events should arrive");
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(500);
        Assert.assertEquals(0, inEventCount);
        Assert.assertFalse(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void LengthBatchWindowTest2() throws InterruptedException {
        log.info("Testing length batch window with no of events greater than window size");

        final int length = 4;
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.lengthBatch(" + length + ") select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                        Assert.assertEquals("Remove event order", removeEventCount, event.getData(2));
                        if (removeEventCount == 1) {
                            Assert.assertEquals("Expired event triggering position", length, inEventCount);
                        }
                    } else {
                        inEventCount++;
                        Assert.assertEquals("In event order", inEventCount, event.getData(2));
                    }
                }
                if (removeEventCount > 0) {
                    Assert.assertEquals("No of emitted events at window expiration", length, removeEventCount);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(500);
        Assert.assertEquals("In event count", 4, inEventCount);
        Assert.assertEquals("Remove event count", 0, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void LengthBatchWindowTest3() throws InterruptedException {
        log.info("Testing length batch window with no of events greater than window size");

        final int length = 2;
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.lengthBatch(" + length + ") select symbol,price,volume insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                        Assert.assertEquals("Remove event order", removeEventCount, event.getData(2));
                        if (removeEventCount == 1) {
                            Assert.assertEquals("Expired event triggering position", length, inEventCount);
                        }
                    } else {
                        inEventCount++;
                        Assert.assertEquals("In event order", inEventCount, event.getData(2));
                    }
                }
                Assert.assertEquals("No of emitted events at window expiration", inEventCount - length,
                        removeEventCount);
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(500);
        Assert.assertEquals("In event count", 6, inEventCount);
        Assert.assertEquals("Remove event count", 4, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void LengthBatchWindowTest4() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol,sum(price) as sumPrice,volume " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    Assert.assertEquals("Events cannot be expired", false, event.isExpired());
                    inEventCount++;
                    if(inEventCount==1){
                        Assert.assertEquals(100.0, event.getData(1));
                    }
                }

                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 10f, 0});
        inputHandler.send(new Object[]{"WSO2", 20f, 1});
        inputHandler.send(new Object[]{"IBM", 30f, 0});
        inputHandler.send(new Object[]{"WSO2", 40f, 1});
        inputHandler.send(new Object[]{"IBM", 50f, 0});
        inputHandler.send(new Object[]{"WSO2", 60f, 1});
        Thread.sleep(500);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(1, inEventCount);
        Assert.assertTrue(eventArrived);

    }

    @Test
    public void LengthBatchWindowTest5() throws InterruptedException {

        final int length = 2;
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.lengthBatch(" + length + ") select symbol,price,volume insert expired events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                        Assert.assertEquals("Remove event order", removeEventCount, event.getData(2));
                    } else {
                        inEventCount++;
                        Assert.fail("In event should not occur");
                    }
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(500);
        Assert.assertEquals("In event count", 0, inEventCount);
        Assert.assertEquals("Remove event count", 4, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

}
