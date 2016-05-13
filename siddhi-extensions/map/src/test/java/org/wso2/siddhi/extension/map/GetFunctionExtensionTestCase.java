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
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.map.test.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class GetFunctionExtensionTestCase {
    private static final Logger log = Logger.getLogger(GetFunctionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testGetFunctionExtension() throws InterruptedException {
        log.info("GetFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price double, volume long);";

        String query = ("@info(name = 'query1') from inputStream " +
                "select symbol,price,map:create() as tmpMap" +
                " insert into tmpStream;" +
                "@info(name = 'query2') " +
                "from tmpStream  " +
                "select symbol,price,tmpMap,map:put(tmpMap,symbol,price) as map1" +
                " insert into outputStream;" +
                "@info(name = 'query3') from outputStream  select map1, map:get(map1,symbol) as price" +
                " insert into outputStream2;"
        );

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("outputStream2", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();

                    if (count.get() == 1) {
                        Assert.assertEquals(100d, event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals(200d, event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals(300d, event.getData(1));
                        eventArrived = true;
                    }
                }
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 100d, 100l});
        inputHandler.send(new Object[]{"WSO2", 200d, 200l});
        inputHandler.send(new Object[]{"XYZ", 300d, 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }


    @Test
    public void testGetFunctionExtension2() throws InterruptedException {
        log.info("GetFunctionExtension TestCase 2");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream" +
                " select symbol,price,map:create() as tmpMap " +
                "insert into tmpStream;" +
                "@info(name = 'query2') from tmpStream  " +
                "select symbol,price,tmpMap, map:put(tmpMap,symbol,price) as map1" +
                " insert into outputStream;" +
                "@info(name = 'query3') from outputStream  select map1, convert(cast(map:get(map1,symbol), 'int'), 'string') as priceInString" +
                " insert into outputStream2;"
        );

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("outputStream2", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();

                    if (count.get() == 1) {
                        Assert.assertEquals("100", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals("200", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals("300", event.getData(1));
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
