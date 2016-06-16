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

package org.wso2.siddhi.extension.math;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class PercentileFunctionExtensionTestCase {
    private static Logger logger = Logger.getLogger(PercentileFunctionExtensionTestCase.class);
    protected static SiddhiManager siddhiManager;
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void PercentileFunctionExtensionDouble() throws Exception {
        logger.info("PercentileFunctionExtensionTestCase TestCase 1");

        siddhiManager = new SiddhiManager();
        String inStreamDefinition = "define stream inputStream (sensorId int, temperature double);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from inputStream "
                + "select math:percentile(temperature, 97.0) as percentile " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + eventFuseExecutionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(10.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(30.0, event.getData(0));
                        break;
                    case 3:
                        Assert.assertEquals(50.0, event.getData(0));
                        break;
                    case 4:
                        Assert.assertEquals(50.0, event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 7:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 8:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 9:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 10:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    default:
                        Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[] { 1, 10d });
        inputHandler.send(new Object[] { 2, 30d });
        inputHandler.send(new Object[] { 3, 50d });
        inputHandler.send(new Object[] { 4, 40d });
        inputHandler.send(new Object[] { 5, 80d });
        inputHandler.send(new Object[] { 6, 60d });
        inputHandler.send(new Object[] { 7, 20d });
        inputHandler.send(new Object[] { 8, 90d });
        inputHandler.send(new Object[] { 9, 70d });
        inputHandler.send(new Object[] { 10, 100d });

        Thread.sleep(100);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void PercentileFunctionExtensionFloat() throws Exception {
        logger.info("PercentileFunctionExtensionTestCase TestCase 2");

        siddhiManager = new SiddhiManager();
        String inStreamDefinition = "define stream inputStream (sensorId int, temperature float);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from inputStream "
                + "select math:percentile(temperature, 97.0) as percentile " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + eventFuseExecutionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(10.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(30.0, event.getData(0));
                        break;
                    case 3:
                        Assert.assertEquals(50.0, event.getData(0));
                        break;
                    case 4:
                        Assert.assertEquals(50.0, event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 7:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 8:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 9:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 10:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    default:
                        Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[] { 1, 10f });
        inputHandler.send(new Object[] { 2, 30f });
        inputHandler.send(new Object[] { 3, 50f });
        inputHandler.send(new Object[] { 4, 40f });
        inputHandler.send(new Object[] { 5, 80f });
        inputHandler.send(new Object[] { 6, 60f });
        inputHandler.send(new Object[] { 7, 20f });
        inputHandler.send(new Object[] { 8, 90f });
        inputHandler.send(new Object[] { 9, 70f });
        inputHandler.send(new Object[] { 10, 100f });

        Thread.sleep(100);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void PercentileFunctionExtensionInt() throws Exception {
        logger.info("PercentileFunctionExtensionTestCase TestCase 3");

        siddhiManager = new SiddhiManager();
        String inStreamDefinition = "define stream inputStream (sensorId int, temperature int);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from inputStream "
                + "select math:percentile(temperature, 97.0) as percentile " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + eventFuseExecutionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(10.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(30.0, event.getData(0));
                        break;
                    case 3:
                        Assert.assertEquals(50.0, event.getData(0));
                        break;
                    case 4:
                        Assert.assertEquals(50.0, event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 7:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 8:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 9:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 10:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    default:
                        Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[] { 1, 10 });
        inputHandler.send(new Object[] { 2, 30 });
        inputHandler.send(new Object[] { 3, 50 });
        inputHandler.send(new Object[] { 4, 40 });
        inputHandler.send(new Object[] { 5, 80 });
        inputHandler.send(new Object[] { 6, 60 });
        inputHandler.send(new Object[] { 7, 20 });
        inputHandler.send(new Object[] { 8, 90 });
        inputHandler.send(new Object[] { 9, 70 });
        inputHandler.send(new Object[] { 10, 100 });

        Thread.sleep(100);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void PercentileFunctionExtensionLong() throws Exception {
        logger.info("PercentileFunctionExtensionTestCase TestCase 4");

        siddhiManager = new SiddhiManager();
        String inStreamDefinition = "define stream inputStream (sensorId int, temperature long);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from inputStream "
                + "select math:percentile(temperature, 97.0) as percentile " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + eventFuseExecutionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(10.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(30.0, event.getData(0));
                        break;
                    case 3:
                        Assert.assertEquals(50.0, event.getData(0));
                        break;
                    case 4:
                        Assert.assertEquals(50.0, event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 7:
                        Assert.assertEquals(80.0, event.getData(0));
                        break;
                    case 8:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 9:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 10:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    default:
                        Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[] { 1, 10l });
        inputHandler.send(new Object[] { 2, 30l });
        inputHandler.send(new Object[] { 3, 50l });
        inputHandler.send(new Object[] { 4, 40l });
        inputHandler.send(new Object[] { 5, 80l });
        inputHandler.send(new Object[] { 6, 60l });
        inputHandler.send(new Object[] { 7, 20l });
        inputHandler.send(new Object[] { 8, 90l });
        inputHandler.send(new Object[] { 9, 70l });
        inputHandler.send(new Object[] { 10, 100l });

        Thread.sleep(100);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
