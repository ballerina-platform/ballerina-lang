/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.map;

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
import org.wso2.siddhi.extension.map.test.util.SiddhiTestHelper;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateFunctionExtensionTestCase {
    private static final Logger log = Logger.getLogger(CreateFunctionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testCreateFunctionExtension() throws InterruptedException {
        log.info("CreateFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();


        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream select symbol,price, "
                + "map:create(symbol,price) as hashMap insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        HashMap map = (HashMap) event.getData(2);
                        Assert.assertEquals(100, map.get("IBM"));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        HashMap map = (HashMap) event.getData(2);
                        Assert.assertEquals(200, map.get("WSO2"));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        HashMap map = (HashMap) event.getData(2);
                        Assert.assertEquals(300, map.get("XYZ"));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 100, 100l});
        inputHandler.send(new Object[]{"WSO2", 200, 200l});
        inputHandler.send(new Object[]{"XYZ", 300, 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testCreateFunctionExtension2() throws InterruptedException {
        log.info("CreateFunctionExtension TestCase2");
        SiddhiManager siddhiManager = new SiddhiManager();


        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream select symbol,price, "
                + "map:create() as hashMap insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        Assert.assertEquals("IBM", event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals("WSO2", event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals("XYZ", event.getData(0));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 100, 100l});
        inputHandler.send(new Object[]{"WSO2", 200, 200l});
        inputHandler.send(new Object[]{"XYZ", 300, 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testCreateFunctionExtension3() throws InterruptedException {
        log.info("CreateFunctionExtension TestCase2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream "
                + "select map:create(\"key1\",\"value1\",\"key2\",\"value2\",symbol,price) as hashMap "
                + "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] inEvents) {
                EventPrinter.print(inEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    HashMap hashMap = (HashMap) event.getData(0);
                    Assert.assertEquals("value1", hashMap.get("key1"));
                    Assert.assertEquals("value2", hashMap.get("key2"));
                    if (count.get() == 1) {
                        Assert.assertEquals(100, hashMap.get("IBM"));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals(200, hashMap.get("WSO2"));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals(300, hashMap.get("XYZ"));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 100, 100l});
        inputHandler.send(new Object[]{"WSO2", 200, 200l});
        inputHandler.send(new Object[]{"XYZ", 300, 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
