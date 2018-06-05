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

package org.ballerinalang.siddhi.core.query.aggregator;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testcase for MinForever aggregation.
 */
public class MinForeverAggregatorExtensionTestCase {
    private static final Logger log = LoggerFactory.getLogger(MinForeverAggregatorExtensionTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
    }


    @Test
    public void testMinForeverAggregatorExtension1() throws InterruptedException {
        log.info("minForeverAggregator TestCase 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 double,price2 double, price3 double);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select minForever(price1) as minForeverValue " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                        case 1:
                            AssertJUnit.assertEquals(36.0, event.getData(0));
                            break;
                        case 2:
                            AssertJUnit.assertEquals(36.0, event.getData(0));
                            break;
                        case 3:
                            AssertJUnit.assertEquals(36.0, event.getData(0));
                            break;
                        case 4:
                            AssertJUnit.assertEquals(36.0, event.getData(0));
                            break;
                        case 5:
                            AssertJUnit.assertEquals(36.0, event.getData(0));
                            break;
                        case 6:
                            AssertJUnit.assertEquals(36.0, event.getData(0));
                            break;
                        default:
                            org.testng.AssertJUnit.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{36d, 36.75, 35.75});
        inputHandler.send(new Object[]{37.88d, 38.12, 37.62});
        inputHandler.send(new Object[]{39.00d, 39.25, 38.62});
        inputHandler.send(new Object[]{36.88d, 37.75, 36.75});
        inputHandler.send(new Object[]{38.12d, 38.12, 37.75});
        inputHandler.send(new Object[]{38.12d, 40, 37.75});

        Thread.sleep(300);
        AssertJUnit.assertEquals(6, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testMinForeverAggregatorExtension2() throws InterruptedException {
        log.info("minForeverAggregator TestCase 2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 int,price2 int, price3 int);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select minForever(price1) as minForeverValue " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                        case 1:
                            AssertJUnit.assertEquals(36, event.getData(0));
                            break;
                        case 2:
                            AssertJUnit.assertEquals(36, event.getData(0));
                            break;
                        case 3:
                            AssertJUnit.assertEquals(9, event.getData(0));
                            break;
                        default:
                            org.testng.AssertJUnit.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{36, 38, 74});
        inputHandler.send(new Object[]{78, 38, 37});
        inputHandler.send(new Object[]{9, 39, 38});

        Thread.sleep(300);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }


    @Test
    public void testMinForeverAggregatorExtension3() throws InterruptedException {
        log.info("minForeverAggregator TestCase 3");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 float, price2 float, price3 float);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select minForever(price1) as minForeverValue " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                        case 1:
                            AssertJUnit.assertEquals(36f, event.getData(0));
                            break;
                        case 2:
                            AssertJUnit.assertEquals(36f, event.getData(0));
                            break;
                        case 3:
                            AssertJUnit.assertEquals(36f, event.getData(0));
                            break;
                        case 4:
                            AssertJUnit.assertEquals(36f, event.getData(0));
                            break;
                        case 5:
                            AssertJUnit.assertEquals(36f, event.getData(0));
                            break;
                        case 6:
                            AssertJUnit.assertEquals(36f, event.getData(0));
                            break;
                        default:
                            org.testng.AssertJUnit.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{36f, 36.75, 35.75});
        inputHandler.send(new Object[]{37.88f, 38.12, 37.62});
        inputHandler.send(new Object[]{39.00f, 39.25, 38.62});
        inputHandler.send(new Object[]{36.88f, 37.75, 36.75});
        inputHandler.send(new Object[]{38.12f, 38.12, 37.75});
        inputHandler.send(new Object[]{38.12f, 40, 37.75});

        Thread.sleep(300);
        AssertJUnit.assertEquals(6, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testMinForeverAggregatorExtension4() throws InterruptedException {
        log.info("minForeverAggregator TestCase 4");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 long, price2 long, price3 long);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select minForever(price1) as minForever " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                        case 1:
                            AssertJUnit.assertEquals(36L, event.getData(0));
                            break;
                        case 2:
                            AssertJUnit.assertEquals(36L, event.getData(0));
                            break;
                        case 3:
                            AssertJUnit.assertEquals(9L, event.getData(0));
                            break;
                        default:
                            org.testng.AssertJUnit.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{36L, 38, 74});
        inputHandler.send(new Object[]{78L, 38, 37});
        inputHandler.send(new Object[]{9L, 39, 38});

        Thread.sleep(300);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }


    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testMinForeverAggregatorExtension5() throws InterruptedException {
        log.info("minForeverAggregator TestCase 5");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 int,price2 double, price3 double);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select minForever(price1, price2, price3) as minForeverValue " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
    }

}
