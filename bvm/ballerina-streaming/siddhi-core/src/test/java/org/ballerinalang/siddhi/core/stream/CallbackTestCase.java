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

package org.ballerinalang.siddhi.core.stream;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.DefinitionNotExistException;
import org.ballerinalang.siddhi.core.exception.QueryNotExistException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created on 1/24/15.
 */
public class CallbackTestCase {

    private static final Logger log = LoggerFactory.getLogger(CallbackTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void callbackTest1() throws InterruptedException {
        log.info("callback test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('callbackTest1') " +
                "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from StockStream[70 > price] " +
                "select symbol, price " +
                "insert into outputStream;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                count = count + events.length;
                eventArrived = true;
            }
        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = QueryNotExistException.class)
    public void callbackTest2() throws InterruptedException {
        log.info("callback test2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('callbackTest1') " +
                "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from StockStream[70 > price] " +
                "select symbol, price " +
                "insert into outputStream;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query3", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
            }

        });

        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = DefinitionNotExistException.class)
    public void callbackTest3() throws InterruptedException {
        log.info("callback test3");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('callbackTest1') " +
                "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from StockStream[70 > price] " +
                "select symbol, price " +
                "insert into outputStream;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream2", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        siddhiAppRuntime.shutdown();
    }
}
