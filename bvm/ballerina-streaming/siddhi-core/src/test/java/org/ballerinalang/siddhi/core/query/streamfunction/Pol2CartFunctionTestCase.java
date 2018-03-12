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
package org.ballerinalang.siddhi.core.query.streamfunction;

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

public class Pol2CartFunctionTestCase {
    private static final Logger log = LoggerFactory.getLogger(Pol2CartFunctionTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void pol2CartFunctionTest() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String polarStream = "define stream PolarStream (theta double, rho double);";
        String query = "@info(name = 'query1') " +
                "from PolarStream#pol2Cart(theta, rho) " +
                "select x, y " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(polarStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertEquals(12, Math.round((Double) inEvents[0].getData(0)));
                    AssertJUnit.assertEquals(5, Math.round((Double) inEvents[0].getData(1)));

                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("PolarStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{22.6, 13.0});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void pol2CartFunctionTest2() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String polarStream = "define stream PolarStream (theta double, rho double, elevation double);";
        String query = "@info(name = 'query1') " +
                "from PolarStream#pol2Cart(theta, rho, elevation) " +
                "select x, z " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(polarStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertEquals(12, Math.round((Double) inEvents[0].getData(0)));
                    AssertJUnit.assertEquals(7, Math.round((Double) inEvents[0].getData(1)));

                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("PolarStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{22.6, 13.0, 7.0});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void pol2CartFunctionTest3() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String polarStream = "define stream PolarStream (theta double, rho double);";
        String query = "@info(name = 'query1') " +
                "from PolarStream#pol2Cart(*) " +
                "select x, y " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(polarStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertEquals(12, Math.round((Double) inEvents[0].getData(0)));
                    AssertJUnit.assertEquals(5, Math.round((Double) inEvents[0].getData(1)));

                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("PolarStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{22.6, 13.0});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }
}
