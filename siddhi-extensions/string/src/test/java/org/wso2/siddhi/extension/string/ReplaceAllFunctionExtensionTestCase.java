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

package org.wso2.siddhi.extension.string;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.string.test.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class ReplaceAllFunctionExtensionTestCase {
    static final Logger log = Logger.getLogger(ReplaceAllFunctionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testReplaceAllFunctionExtension1() throws InterruptedException {
        log.info("ReplaceAllFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, price long, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:replaceAll(symbol, 'hello', 'test') as replacedString " +
                        "insert into outputStream;"
        );

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        Assert.assertEquals("test hi test", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals("WSO2 hi test", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals("WSO2 cep", event.getData(1));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"hello hi hello", 700f, 100l});
        inputHandler.send(new Object[]{"WSO2 hi hello", 60.5f, 200l});
        inputHandler.send(new Object[]{"WSO2 cep", 60.5f, 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testReplaceAllFunctionExtension2() throws InterruptedException {
        log.info("ReplaceAllFunctionExtension TestCase, variable regex and replacement strings scenario");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, regex string, replacement string);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:replaceAll(symbol, regex, replacement) as replacedString " +
                        "insert into outputStream;"
        );

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        Assert.assertEquals("test hi test", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals("WSD3 hi hello", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals("WSO2 bam", event.getData(1));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"hello hi hello", "hello", "test"});
        inputHandler.send(new Object[]{"WSO2 hi hello", "O2", "D3"});
        inputHandler.send(new Object[]{"WSO2 cep", "cep", "bam"});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
