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

package org.wso2.siddhi.extension.regex;

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

public class RegExFunctionExtensionTestCase {
    static final Logger log = Logger.getLogger(RegExFunctionExtensionTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testFindFunctionExtensionTestCase() throws InterruptedException {
        log.info("FindFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, price long, regex string);";
        String query = ("@info(name = 'query1') from inputStream select symbol , regex:find(symbol, regex) as aboutWSO2 " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 1) {
                    Assert.assertEquals(true, inEvents[0].getData(1));
                    eventArrived = true;
                }
                if (count == 2) {
                    Assert.assertEquals(true, inEvents[1].getData(1));
                    eventArrived = true;
                }
                if (count == 3) {
                    Assert.assertEquals(true, inEvents[2].getData(1));
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"21 products are produced by WSO2 currently", 60.5f, "\\d\\d(.*)WSO2"});
        inputHandler.send(new Object[]{"WSO2 is situated in trace and its a middleware company", 60.5f, "WSO2(.*)middleware(.*)"});
        inputHandler.send(new Object[]{"WSO2 is situated in trace and its a middleware company", 60.5f, "WSO2(.*)middleware"});

        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testFindStartFromIndexFunctionExtensionTestCase() throws InterruptedException {
        log.info("FindStartFromIndexFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, price long, regex string, startingIndex int);";
        String query = ("@info(name = 'query1') from inputStream select symbol , regex:find(symbol, regex, startingIndex) as aboutWSO2 " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 1) {
                    Assert.assertEquals(true, inEvents[0].getData(1));
                    eventArrived = true;
                }
                if (count == 2) {
                    Assert.assertEquals(false, inEvents[1].getData(1));
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"21 products are produced within 10 years by WSO2 currently by WSO2 employees", 60.5f, "\\d\\d(.*)WSO2", 30});
        inputHandler.send(new Object[]{"21 products are produced within 10 years by WSO2 currently by WSO2 employees", 60.5f, "\\d\\d(.*)WSO2", 35});

        Thread.sleep(100);
        Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
    @Test
    public void testMatchesFunctionExtension() throws InterruptedException {
        log.info("MatchesFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, price long, regex string);";
        String query = ("@info(name = 'query1') from inputStream select symbol , regex:matches(symbol, regex) as aboutWSO2 " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 1) {
                    Assert.assertEquals(false, inEvents[0].getData(1));
                    eventArrived = true;
                }
                if (count == 2) {
                    Assert.assertEquals(true, inEvents[1].getData(1));
                    eventArrived = true;
                }
                if (count == 3) {
                    Assert.assertEquals(false, inEvents[2].getData(1));
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"21 products are produced by WSO2 currently", 60.5f, "\\d\\d(.*)WSO2"});
        inputHandler.send(new Object[]{"WSO2 is situated in trace and its a middleware company", 60.5f, "WSO2(.*)middleware(.*)"});
        inputHandler.send(new Object[]{"WSO2 is situated in trace and its a middleware company", 60.5f, "WSO2(.*)middleware"});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testLookingAtFunctionExtension() throws InterruptedException {
        log.info("LookingAtFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, price long, regex string);";
        String query = ("@info(name = 'query1') from inputStream select symbol , regex:lookingAt(symbol, regex) as aboutWSO2 " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 1) {
                    Assert.assertEquals(true, inEvents[0].getData(1));
                    eventArrived = true;
                }
                if (count == 2) {
                    Assert.assertEquals(false, inEvents[1].getData(1));
                    eventArrived = true;
                }
                if (count == 3) {
                    Assert.assertEquals(true, inEvents[2].getData(1));
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"21 products are produced by WSO2 currently in Sri Lanka", 60.5f, "\\d\\d(.*)WSO2"});
        inputHandler.send(new Object[]{"sample test string and WSO2 is situated in trace and its a middleware company", 60.5f, "WSO2(.*)middleware(.*)"});
        inputHandler.send(new Object[]{"WSO2 is situated in trace and its a middleware company", 60.5f, "WSO2(.*)middleware"});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testGroupFunctionExtension() throws InterruptedException {
        log.info("GroupFunctionExtensionTestCase TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "@config(async = 'true')define stream inputStream (symbol string, price long, regex string, group int);";
        String query = ("@info(name = 'query1') from inputStream select symbol , regex:group(symbol, regex, group) as aboutWSO2 " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 1) {
                    Assert.assertEquals("WSO2 employees", inEvents[0].getData(1));
                    eventArrived = true;
                }
                if (count == 2) {
                    Assert.assertEquals("21", inEvents[1].getData(1));
                    eventArrived = true;
                }
                if (count == 3) {
                    Assert.assertEquals(" is situated in trace and its a ", inEvents[2].getData(1));
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"21 products are produced within 10 years by WSO2 currently by WSO2 employees", 60.5f, "(\\d\\d)(.*)(WSO2.*)", 3});
        inputHandler.send(new Object[]{"21 products are produced within 10 years by WSO2 currently by WSO2 employees", 60.5f, "(\\d\\d)(.*)(WSO2.*)", 1});
        inputHandler.send(new Object[]{"WSO2 is situated in trace and its a middleware company", 60.5f, "WSO2(.*)middleware", 1});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
