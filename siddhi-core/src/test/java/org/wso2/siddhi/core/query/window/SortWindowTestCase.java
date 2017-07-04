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

package org.wso2.siddhi.core.query.window;


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

public class SortWindowTestCase {
    private static final Logger log = Logger.getLogger(SortWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void sortWindowTest1() throws InterruptedException {
        log.info("sortWindow test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.sort(2,volume, 'asc') " +
                "select volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 300L});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 200L});
        inputHandler.send(new Object[]{"WSO2", 55.6f, 20L});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 40L});
        Thread.sleep(1000);
        Assert.assertEquals(5, inEventCount);
        Assert.assertEquals(3, removeEventCount);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void sortWindowTest2() throws InterruptedException {
        log.info("sortWindow test2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String planName = "@app:name('sortWindow2') ";
        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price int, volume long);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.sort(2,volume, 'asc', price, 'desc') " +
                "select price, volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(planName +
                cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50, 100L});
        inputHandler.send(new Object[]{"IBM", 20, 100L});
        inputHandler.send(new Object[]{"WSO2", 40, 50L});
        inputHandler.send(new Object[]{"WSO2", 100, 20L});
        inputHandler.send(new Object[]{"WSO2", 50, 50L});
        Thread.sleep(1000);
        Assert.assertEquals(5, inEventCount);
        Assert.assertEquals(3, removeEventCount);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }
}
