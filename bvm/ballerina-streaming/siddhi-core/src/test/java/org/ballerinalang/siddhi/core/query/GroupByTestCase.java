/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.siddhi.core.query;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GroupByTestCase {
    private static final Logger log = LoggerFactory.getLogger(GroupByTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testGroupByQuery1() throws InterruptedException {
        log.info("GroupBy test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('GroupByTest1') " +
                "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) " +
                "select symbol, sum(volume) as totalVolume, avg(price) as avgPrice " +
                "   group by symbol " +
                "insert into outputStream;" +
                "";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 50f, 200L});
        inputHandler.send(new Object[]{"WSO2", 50f, 200L});
        Thread.sleep(200);
        inputHandler.send(new Object[]{"WSO2", 50f, 200L});
        inputHandler.send(new Object[]{"IBM", 50f, 200L});
        Thread.sleep(4200);
        inputHandler.send(new Object[]{"WSO2", 50f, 200L});
        inputHandler.send(new Object[]{"WSO2", 50f, 200L});
        Thread.sleep(100);

        AssertJUnit.assertEquals(6, count);
        AssertJUnit.assertTrue(eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testGroupByQuery2() throws InterruptedException {
        log.info("GroupBy test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('GroupByTest1') " +
                "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.timeBatch(1 sec) " +
                "select symbol, sum(volume) as totalVolume, avg(price) as avgPrice " +
                "   group by symbol " +
                "insert into outputStream;" +
                "";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 50f, 200L});
        inputHandler.send(new Object[]{"WSO2", 50f, 200L});
        Thread.sleep(200);
        inputHandler.send(new Object[]{"WSO2", 50f, 200L});
        inputHandler.send(new Object[]{"IBM", 50f, 200L});
        Thread.sleep(3200);
        inputHandler.send(new Object[]{"WSO2", 50f, 200L});
        inputHandler.send(new Object[]{"WSO2", 50f, 200L});
        Thread.sleep(2000);

        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);

        siddhiAppRuntime.shutdown();
    }
}
