/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.siddhi.core.query.partition;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
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
 * Testcase for partition queries.
 */
public class PartitionTestCase2 {
    private static final Logger log = LoggerFactory.getLogger(PartitionTestCase2.class);
    private AtomicInteger count = new AtomicInteger(0);
    private int stockStreamEventCount;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
        stockStreamEventCount = 0;
    }

    @Test
    public void testModExpressionExecutorFloatCase() throws InterruptedException {
        log.info("Partition testModExpressionExecutorFloatCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testModExpressionExecutorFloatCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 float, " +
                "atr5 long,  atr6 long,  atr7 float,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr4%atr7 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(9.559998f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(9.74f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(11.540001f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(13.660004f, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0f, 500L, 200L, 11.43f, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0f, 501L, 201L, 15.21f, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0f, 502L, 202L, 45.23f, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0f, 502L, 202L, 87.34f, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testModExpressionExecutorLongCase() throws InterruptedException {
        log.info("Partition testModExpressionExecutorLongCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testModExpressionExecutorLongCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr5%atr6 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(0L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(89L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(98L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(84L, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 20L, 11.43, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 206L, 15.21, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 209L, 87.34, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testModExpressionExecutorIntCase() throws InterruptedException {
        log.info("Partition testModExpressionExecutorIntCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testModExpressionExecutorIntCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr3%atr11 as dividedVal, atr5 as threshold,  atr1 " +
                "as symbol," + "cast(atr2, 'double') as priceInDouble, sum(atr7) as summedValue insert" +
                " into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(8, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(35, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(4, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(32, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 20L, 11.43, 75.7f, false, true, 23});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 206L, 15.21, 76.7f, false, true, 65});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 12});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 209L, 87.34, 77.7f, false, false, 34});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSubtractExpressionExecutorDoubleCase() throws InterruptedException {
        log.info("Partition testSubtractExpressionExecutorDoubleCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testSubtractExpressionExecutorDoubleCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr4-atr7 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(89.57, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(85.78999999999999, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(56.77, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(13.659999999999997, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 200L, 11.43, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 201L, 15.21, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 202L, 87.34, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSubtractExpressionExecutorFloatCase() throws InterruptedException {
        log.info("Partition testSubtractExpressionExecutorFloatCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testSubtractExpressionExecutorFloatCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 float, " +
                "atr5 long,  atr6 long,  atr7 float,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr4-atr7 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(89.57f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(85.79f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(56.77f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(13.660004f, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0f, 500L, 200L, 11.43f, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0f, 501L, 201L, 15.21f, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0f, 502L, 202L, 45.23f, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0f, 502L, 202L, 87.34f, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSubtractExpressionExecutorLongCase() throws InterruptedException {
        log.info("Partition testSubtractExpressionExecutorLongCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testSubtractExpressionExecutorLongCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr5-atr6 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(480L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(295L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(300L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(293L, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 20L, 11.43, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 206L, 15.21, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 209L, 87.34, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSubtractExpressionExecutorIntCase() throws InterruptedException {
        log.info("Partition testSubtractExpressionExecutorIntCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testSubtractExpressionExecutorIntCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr3-atr11 as dividedVal, atr5 as threshold,  atr1 " +
                "as symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert" +
                " into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(77, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(35, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(88, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(66, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 20L, 11.43, 75.7f, false, true, 23});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 206L, 15.21, 76.7f, false, true, 65});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 12});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 209L, 87.34, 77.7f, false, false, 34});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testMultiplyExpressionExecutorDoubleCase() throws InterruptedException {
        log.info("Partition testMultiplyExpressionExecutorDoubleCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testMultiplyExpressionExecutorDoubleCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr4*atr7 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(1154.43, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(1536.21, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(4613.46, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(8821.34, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 200L, 11.43, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 201L, 15.21, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 202L, 87.34, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testMultiplyExpressionExecutorFloatCase() throws InterruptedException {
        log.info("Partition testMultiplyExpressionExecutorFloatCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testMultiplyExpressionExecutorFloatCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 float, " +
                "atr5 long,  atr6 long,  atr7 float,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr4*atr7 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(1154.43f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(1536.21f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(4613.46f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(8821.34f, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0f, 500L, 200L, 11.43f, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0f, 501L, 201L, 15.21f, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0f, 502L, 202L, 45.23f, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0f, 502L, 202L, 87.34f, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testMultiplyExpressionExecutorLongCase() throws InterruptedException {
        log.info("Partition testMultiplyExpressionExecutorLongCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testMultiplyExpressionExecutorLongCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr5*atr6 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(10000L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(103206L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(101404L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(104918L, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 20L, 11.43, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 206L, 15.21, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 209L, 87.34, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testMultiplyExpressionExecutorIntCase() throws InterruptedException {
        log.info("Partition testMultiplyExpressionExecutorIntCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testMultiplyExpressionExecutorIntCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr3*atr11 as dividedVal, atr5 as threshold,  atr1 " +
                "as symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert" +
                " into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(2300, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(6500, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(1200, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(3400, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 20L, 11.43, 75.7f, false, true, 23});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 206L, 15.21, 76.7f, false, true, 65});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 12});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 209L, 87.34, 77.7f, false, false, 34});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void partitionStreamValidationTest() throws InterruptedException {
        log.info("filter test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@App:name(\"ExtremaBottomKLeng\")\n" +
                "@App:Description('Demonstrates how to use the siddhi-execution-extrema with " +
                "bottomKLengthBatch function')\n" +
                "\n" +
                "define stream inputStream (item string, price long);\n" +
                "\n" +
                "@sink(type='log') \n" +
                "define stream outputStream(item string, price long);\n" +
                "\n" +
                "partition with (itemsss of inputStreamssss)\n" +
                "begin \n" +
                "    from inputStream select item\n" +
                "    insert into s\n" +
                "end;\n" +

                "from inputStream\n" +
                "insert all events into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        try {
            log.info("Running : " + siddhiAppRuntime.getName());

            siddhiAppRuntime.start();
        } finally {
            siddhiAppRuntime.shutdown();

        }
    }
}
