/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.query.window.external;

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

/**
 * Testcase for defined window with lossy frequency.
 */
public class LossyFrequentWindowTestCase {
    private static final Logger log = LoggerFactory.getLogger(LossyFrequentWindowTestCase.class);

    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @BeforeMethod
    public void initialize() {
        eventArrived = false;
        inEventCount = 0;
        removeEventCount = 0;
    }

    @Test
    public void testLossyFrequentUniqueWindow1() throws InterruptedException {
        log.info("LossyFrequentWindow test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream purchase (cardNo string, price float); " +
                "define window purchaseWindow (cardNo string, price float) lossyFrequent(0.1,0.01); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from purchase[price >= 30] " +
                "insert into purchaseWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from purchaseWindow " +
                "select cardNo, price " +
                "insert all events into PotentialFraud ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("purchase");
        siddhiAppRuntime.start();

        for (int i = 0; i < 25; i++) {
            inputHandler.send(new Object[]{"3234-3244-2432-4124", 73.36f});
            inputHandler.send(new Object[]{"1234-3244-2432-123", 46.36f});
            inputHandler.send(new Object[]{"5768-3244-2432-5646", 48.36f});
            inputHandler.send(new Object[]{"9853-3244-2432-4125", 78.36f});
        }
        inputHandler.send(new Object[]{"1124-3244-2432-4126", 78.36f});     // these events will not be picked
        inputHandler.send(new Object[]{"1124-3244-2432-4126", 78.36f});

        Thread.sleep(1000);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("In Event count", 100, inEventCount);
        AssertJUnit.assertEquals("Out Event count", 0, removeEventCount);

        siddhiAppRuntime.shutdown();

    }


    @Test
    public void frequentUniqueWindowTest2() throws InterruptedException {
        log.info("LossyFrequentWindow test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream purchase (cardNo string, price float); " +
                "define window purchaseWindow (cardNo string, price float) lossyFrequent(0.3,0.05); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from purchase[price >= 30] " +
                "insert into purchaseWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from purchaseWindow " +
                "select cardNo, price " +
                "insert all events into PotentialFraud ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("purchase");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"3224-3244-2432-4124", 73.36f});
        for (int i = 0; i < 25; i++) {
            inputHandler.send(new Object[]{"3234-3244-2432-4124", 73.36f});
            inputHandler.send(new Object[]{"3234-3244-2432-4124", 78.36f});
            inputHandler.send(new Object[]{"1234-3244-2432-123", 86.36f});
            inputHandler.send(new Object[]{"5768-3244-2432-5646", 48.36f}); //this event will not include in to the
            // window during first iteration because 1+0<5*0.25
        }
        Thread.sleep(1000);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Out Event count", 1, removeEventCount);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void frequentUniqueWindowTest3() throws InterruptedException {
        log.info("LossyFrequentWindow test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream purchase (cardNo string, price float); " +
                "define window purchaseWindow (cardNo string, price float) lossyFrequent(0.3,0.05,cardNo); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from purchase[price >= 30] " +
                "insert into purchaseWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from purchaseWindow " +
                "select cardNo, price " +
                "insert all events into PotentialFraud ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("purchase");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"3224-3244-2432-4124", 73.36f});
        for (int i = 0; i < 25; i++) {
            inputHandler.send(new Object[]{"3234-3244-2432-4124", 73.36f});
            inputHandler.send(new Object[]{"3234-3244-2432-4124", 78.36f});
            inputHandler.send(new Object[]{"1234-3244-2432-123", 86.36f});
            inputHandler.send(new Object[]{"3234-3244-2432-4124", 48.36f}); //this event will be included because we
            // only consider cardNo
        }
        Thread.sleep(1000);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("In Event count", 101, inEventCount);
        AssertJUnit.assertEquals("Out Event count", 1, removeEventCount);

        siddhiAppRuntime.shutdown();

    }

}
