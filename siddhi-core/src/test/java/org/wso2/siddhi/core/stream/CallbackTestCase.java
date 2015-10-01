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

package org.wso2.siddhi.core.stream;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.exception.QueryNotExistException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

/**
 * Created on 1/24/15.
 */
public class CallbackTestCase {

    static final Logger log = Logger.getLogger(CallbackTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void callbackTest1() throws InterruptedException {
        log.info("callback test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('callbackTest1') " +
                "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from StockStream[70 > price] " +
                "select symbol, price " +
                "insert into outputStream;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                count = count + events.length;
                eventArrived = true;
            }
        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("StockStream");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100l});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test(expected = QueryNotExistException.class)
    public void callbackTest2() throws InterruptedException {
        log.info("callback test2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('callbackTest1') " +
                "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from StockStream[70 > price] " +
                "select symbol, price " +
                "insert into outputStream;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("query3", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }

        });

        executionPlanRuntime.shutdown();
    }

    @Test(expected = DefinitionNotExistException.class)
    public void callbackTest3() throws InterruptedException {
        log.info("callback test3");
        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('callbackTest1') " +
                "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from StockStream[70 > price] " +
                "select symbol, price " +
                "insert into outputStream;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream2", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        executionPlanRuntime.shutdown();
    }
}