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

import java.util.concurrent.CountDownLatch;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PercentileFunctionExtensionTestCase {
    private static Logger logger = Logger.getLogger(PercentileFunctionExtensionTestCase.class);
    protected static SiddhiManager siddhiManager;
    private CountDownLatch countDownLatch;
    private volatile int count;
    private volatile boolean eventArrived;
    private static final String INPUT_STREAM_DOUBLE= "define stream inputStream (sensorId int, temperature double);";
    private static final String INPUT_STREAM_FLOAT= "define stream inputStream (sensorId int, temperature float);";
    private static final String INPUT_STREAM_INT= "define stream inputStream (sensorId int, temperature int);";
    private static final String INPUT_STREAM_LONG= "define stream inputStream (sensorId int, temperature long);";

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testPercentileFunctionExtensionDouble1() throws Exception {
        logger.info("PercentileFunctionExtension no window test case.");

        final int EXPECTED_NO_OF_EVENTS = 10;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_DOUBLE + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
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

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionDouble2() throws Exception {
        logger.info("PercentileFunctionExtension length window test case.");

        final int EXPECTED_NO_OF_EVENTS = 10;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream#window.length(5) "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_DOUBLE + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(10.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(30.0, event.getData(0));
                        break;
                    case 3:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 4:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 7:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 8:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 9:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 10:
                        Assert.assertEquals(90.0, event.getData(0));
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
        inputHandler.send(new Object[] { 3, 100d });
        inputHandler.send(new Object[] { 4, 40d });
        inputHandler.send(new Object[] { 5, 80d });
        inputHandler.send(new Object[] { 6, 60d });
        inputHandler.send(new Object[] { 7, 20d });
        inputHandler.send(new Object[] { 8, 90d });
        inputHandler.send(new Object[] { 9, 70d });
        inputHandler.send(new Object[] { 10, 50d });

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionDouble3() throws Exception {
        logger.info("PercentileFunctionExtension lengthBatch window test case.");

        final int EXPECTED_NO_OF_EVENTS = 2;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream#window.lengthBatch(5) "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_DOUBLE + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(90.0, event.getData(0));
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
        inputHandler.send(new Object[] { 3, 100d });
        inputHandler.send(new Object[] { 4, 40d });
        inputHandler.send(new Object[] { 5, 80d });
        inputHandler.send(new Object[] { 6, 60d });
        inputHandler.send(new Object[] { 7, 20d });
        inputHandler.send(new Object[] { 8, 90d });
        inputHandler.send(new Object[] { 9, 70d });
        inputHandler.send(new Object[] { 10, 50d });

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionFloat1() throws Exception {
        logger.info("PercentileFunctionExtension no window test case.");

        final int EXPECTED_NO_OF_EVENTS = 10;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_FLOAT + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
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

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionFloat2() throws Exception {
        logger.info("PercentileFunctionExtension length window test case.");

        final int EXPECTED_NO_OF_EVENTS = 10;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream#window.length(5) "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_FLOAT + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(10.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(30.0, event.getData(0));
                        break;
                    case 3:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 4:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 7:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 8:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 9:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 10:
                        Assert.assertEquals(90.0, event.getData(0));
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
        inputHandler.send(new Object[] { 3, 100f });
        inputHandler.send(new Object[] { 4, 40f });
        inputHandler.send(new Object[] { 5, 80f });
        inputHandler.send(new Object[] { 6, 60f });
        inputHandler.send(new Object[] { 7, 20f });
        inputHandler.send(new Object[] { 8, 90f });
        inputHandler.send(new Object[] { 9, 70f });
        inputHandler.send(new Object[] { 10, 50f });

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionFloat3() throws Exception {
        logger.info("PercentileFunctionExtension lengthBatch window test case.");

        final int EXPECTED_NO_OF_EVENTS = 2;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream#window.lengthBatch(5) "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_FLOAT + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(90.0, event.getData(0));
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
        inputHandler.send(new Object[] { 3, 100f });
        inputHandler.send(new Object[] { 4, 40f });
        inputHandler.send(new Object[] { 5, 80f });
        inputHandler.send(new Object[] { 6, 60f });
        inputHandler.send(new Object[] { 7, 20f });
        inputHandler.send(new Object[] { 8, 90f });
        inputHandler.send(new Object[] { 9, 70f });
        inputHandler.send(new Object[] { 10, 50f });

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionInt1() throws Exception {
        logger.info("PercentileFunctionExtension no window test case.");

        final int EXPECTED_NO_OF_EVENTS = 10;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_INT + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
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

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionInt2() throws Exception {
        logger.info("PercentileFunctionExtension length window test case.");

        final int EXPECTED_NO_OF_EVENTS = 10;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream#window.length(5) "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_INT + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(10.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(30.0, event.getData(0));
                        break;
                    case 3:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 4:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 7:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 8:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 9:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 10:
                        Assert.assertEquals(90.0, event.getData(0));
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
        inputHandler.send(new Object[] { 3, 100 });
        inputHandler.send(new Object[] { 4, 40 });
        inputHandler.send(new Object[] { 5, 80 });
        inputHandler.send(new Object[] { 6, 60 });
        inputHandler.send(new Object[] { 7, 20 });
        inputHandler.send(new Object[] { 8, 90 });
        inputHandler.send(new Object[] { 9, 70 });
        inputHandler.send(new Object[] { 10, 50 });

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionInt3() throws Exception {
        logger.info("PercentileFunctionExtension lengthBatch window test case.");

        final int EXPECTED_NO_OF_EVENTS = 2;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream#window.lengthBatch(5) "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_INT + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(90.0, event.getData(0));
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
        inputHandler.send(new Object[] { 3, 100 });
        inputHandler.send(new Object[] { 4, 40 });
        inputHandler.send(new Object[] { 5, 80 });
        inputHandler.send(new Object[] { 6, 60 });
        inputHandler.send(new Object[] { 7, 20 });
        inputHandler.send(new Object[] { 8, 90 });
        inputHandler.send(new Object[] { 9, 70 });
        inputHandler.send(new Object[] { 10, 50 });

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionLong1() throws Exception {
        logger.info("PercentileFunctionExtension no window test case.");

        final int EXPECTED_NO_OF_EVENTS = 10;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_LONG + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
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

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionLong2() throws Exception {
        logger.info("PercentileFunctionExtension length window test case.");

        final int EXPECTED_NO_OF_EVENTS = 10;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream#window.length(5) "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_LONG + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(10.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(30.0, event.getData(0));
                        break;
                    case 3:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 4:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 7:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 8:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 9:
                        Assert.assertEquals(90.0, event.getData(0));
                        break;
                    case 10:
                        Assert.assertEquals(90.0, event.getData(0));
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
        inputHandler.send(new Object[] { 3, 100l });
        inputHandler.send(new Object[] { 4, 40l });
        inputHandler.send(new Object[] { 5, 80l });
        inputHandler.send(new Object[] { 6, 60l });
        inputHandler.send(new Object[] { 7, 20l });
        inputHandler.send(new Object[] { 8, 90l });
        inputHandler.send(new Object[] { 9, 70l });
        inputHandler.send(new Object[] { 10, 50l });

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(10, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testPercentileFunctionExtensionLong3() throws Exception {
        logger.info("PercentileFunctionExtension lengthBatch window test case.");

        final int EXPECTED_NO_OF_EVENTS = 2;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();

        String executionPlan = ("@info(name = 'query1') from inputStream#window.lengthBatch(5) "
                + "select math:percentile(temperature, 97.0) as percentile "
                + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(INPUT_STREAM_LONG + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                    case 1:
                        Assert.assertEquals(100.0, event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals(90.0, event.getData(0));
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
        inputHandler.send(new Object[] { 3, 100l });
        inputHandler.send(new Object[] { 4, 40l });
        inputHandler.send(new Object[] { 5, 80l });
        inputHandler.send(new Object[] { 6, 60l });
        inputHandler.send(new Object[] { 7, 20l });
        inputHandler.send(new Object[] { 8, 90l });
        inputHandler.send(new Object[] { 9, 70l });
        inputHandler.send(new Object[] { 10, 50l });

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
