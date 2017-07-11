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

package org.wso2.siddhi.core.query.function;

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
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

public class MaximumFunctionExtensionTestCase {
    private static final Logger log = Logger.getLogger(MaximumFunctionExtensionTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }


    @Test
    public void testMaxFunctionExtension1() throws InterruptedException {
        log.info("MaximumFunctionExecutor TestCase 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 double,price2 double, price3 double);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select maximum(price1, price2, price3) as max " +
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
                            Assert.assertEquals(36.75, event.getData(0));
                            break;
                        case 2:
                            Assert.assertEquals(38.12, event.getData(0));
                            break;
                        case 3:
                            Assert.assertEquals(39.25, event.getData(0));
                            break;
                        case 4:
                            Assert.assertEquals(37.75, event.getData(0));
                            break;
                        case 5:
                            Assert.assertEquals(38.12, event.getData(0));
                            break;
                        case 6:
                            Assert.assertEquals(40.0, event.getData(0));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{36, 36.75, 35.75});
        inputHandler.send(new Object[]{37.88, 38.12, 37.62});
        inputHandler.send(new Object[]{39.00, 39.25, 38.62});
        inputHandler.send(new Object[]{36.88, 37.75, 36.75});
        inputHandler.send(new Object[]{38.12, 38.12, 37.75});
        inputHandler.send(new Object[]{38.12, 40, 37.75});

        Thread.sleep(300);
        Assert.assertEquals(6, count);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test(expected = SiddhiAppValidationException.class)
    public void testMaxFunctionExtension2() throws InterruptedException {
        log.info("MaximumFunctionExecutor TestCase 2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 int,price2 double, price3 double);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select maximum(price1, price2, price3) as max " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);
    }

    @Test
    public void testMaxFunctionExtension3() throws InterruptedException {
        log.info("MaximumFunctionExecutor TestCase 3");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 int,price2 int, price3 int);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select maximum(price1, price2, price3) as max " +
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
                            Assert.assertEquals(74, event.getData(0));
                            break;
                        case 2:
                            Assert.assertEquals(78, event.getData(0));
                            break;
                        case 3:
                            Assert.assertEquals(39, event.getData(0));
                            break;
                        default:
                            org.junit.Assert.fail();
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
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testMaxFunctionExtension4() throws InterruptedException {
        log.info("MaximumFunctionExecutor TestCase 4");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 float, price2 float, price3 float);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select maximum(price1, price2, price3) as max " +
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
                            Assert.assertEquals(36.75f, event.getData(0));
                            break;
                        case 2:
                            Assert.assertEquals(38.12f, event.getData(0));
                            break;
                        case 3:
                            Assert.assertEquals(39.25f, event.getData(0));
                            break;
                        case 4:
                            Assert.assertEquals(37.75f, event.getData(0));
                            break;
                        case 5:
                            Assert.assertEquals(38.12f, event.getData(0));
                            break;
                        case 6:
                            Assert.assertEquals(40.0f, event.getData(0));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{36, 36.75, 35.75});
        inputHandler.send(new Object[]{37.88, 38.12, 37.62});
        inputHandler.send(new Object[]{39.00, 39.25, 38.62});
        inputHandler.send(new Object[]{36.88, 37.75, 36.75});
        inputHandler.send(new Object[]{38.12, 38.12, 37.75});
        inputHandler.send(new Object[]{38.12, 40, 37.75});

        Thread.sleep(300);
        Assert.assertEquals(6, count);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testMaxFunctionExtension5() throws InterruptedException {
        log.info("MaximumFunctionExecutor TestCase 5");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 long, price2 long, price3 long);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select maximum(price1, price2, price3) as max " +
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
                            Assert.assertEquals(74L, event.getData(0));
                            break;
                        case 2:
                            Assert.assertEquals(78L, event.getData(0));
                            break;
                        case 3:
                            Assert.assertEquals(39L, event.getData(0));
                            break;
                        default:
                            org.junit.Assert.fail();
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
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testMaxFunctionExtension6() throws InterruptedException {
        log.info("MaximumFunctionExecutor TestCase 6");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 double,price2 double, price3 double);";
        String query = ("@info(name = 'query1') from inputStream " +
                "select maximum(*) as max " +
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
                            Assert.assertEquals(36.75, event.getData(0));
                            break;
                        case 2:
                            Assert.assertEquals(38.12, event.getData(0));
                            break;
                        case 3:
                            Assert.assertEquals(39.25, event.getData(0));
                            break;
                        case 4:
                            Assert.assertEquals(37.75, event.getData(0));
                            break;
                        case 5:
                            Assert.assertEquals(38.12, event.getData(0));
                            break;
                        case 6:
                            Assert.assertEquals(40.0, event.getData(0));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{36, 36.75, 35.75});
        inputHandler.send(new Object[]{37.88, 38.12, 37.62});
        inputHandler.send(new Object[]{39.00, 39.25, 38.62});
        inputHandler.send(new Object[]{36.88, 37.75, 36.75});
        inputHandler.send(new Object[]{38.12, 38.12, 37.75});
        inputHandler.send(new Object[]{38.12, 40, 37.75});

        Thread.sleep(300);
        Assert.assertEquals(6, count);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }
}
