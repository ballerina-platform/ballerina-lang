/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
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

public class ReplaceAllFunctionExtensionTestCase {
    static final Logger log = Logger.getLogger(ReplaceAllFunctionExtensionTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testReplaceAllFunctionExtension1() throws InterruptedException {
        log.info("ReplaceAllFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, price long, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:replace_all(symbol, 'hello', 'test') as replacedString " +
                        "insert into outputStream;"
        );

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 1) {
                    Assert.assertEquals("test hi test", inEvents[0].getData(1));
                    eventArrived = true;
                }
                if (count == 2) {
                    Assert.assertEquals("WSO2 hi test", inEvents[1].getData(1));
                    eventArrived = true;
                }
                if (count == 3) {
                    Assert.assertEquals("WSO2 cep", inEvents[2].getData(1));
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"hello hi hello", 700f, 100l});
        inputHandler.send(new Object[]{"WSO2 hi hello", 60.5f, 200l});
        inputHandler.send(new Object[]{"WSO2 cep", 60.5f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testReplaceAllFunctionExtension2() throws InterruptedException {
        log.info("ReplaceAllFunctionExtension TestCase, variable target and replacement strings scenario");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, target string, replacement string);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:replace_all(symbol, target, replacement) as replacedString " +
                        "insert into outputStream;"
        );

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 1) {
                    Assert.assertEquals("test hi test", inEvents[0].getData(1));
                    eventArrived = true;
                }
                if (count == 2) {
                    Assert.assertEquals("WSD3 hi test", inEvents[1].getData(1));
                    eventArrived = true;
                }
                if (count == 3) {
                    Assert.assertEquals("WSO2 bam", inEvents[2].getData(1));
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"hello hi hello", "hello", "test"});
        inputHandler.send(new Object[]{"WSO2 hi hello", "O2", "D3"});
        inputHandler.send(new Object[]{"WSO2 cep", "cep", "bam"});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
