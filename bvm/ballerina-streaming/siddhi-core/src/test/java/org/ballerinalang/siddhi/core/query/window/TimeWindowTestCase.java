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
package org.ballerinalang.siddhi.core.query.window;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TimeWindowTestCase {
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
    public void timeWindowTest1() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.time(2 sec) " +
                "select symbol,price,volume " +
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
                    AssertJUnit.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(4000);
        AssertJUnit.assertEquals(2, inEventCount);
        AssertJUnit.assertEquals(2, removeEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }


    /**
     * Commenting out intermittent failing test case until fix this properly.
     *
     * @throws InterruptedException throw exception if interrupted the input handler sender.
     */
    @Test
    public void timeWindowTest2() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.time(1 sec) select symbol,price," +
                "volume insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    AssertJUnit.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(4000);
        AssertJUnit.assertEquals(6, inEventCount);
        AssertJUnit.assertEquals(6, removeEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void timeWindowTest3() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();


        String queries = "define stream fireAlarmEventStream (deviceID string, sonar double);\n" +
                "@info(name = 'query1')\n" +
                "from fireAlarmEventStream#window.time(30 milliseconds)\n" +
                "select deviceID\n" +
                "insert expired events into analyzeStream;\n" +
                "" +
                "@info(name = 'query2')\n" +
                "from analyzeStream\n" +
                "select deviceID\n" +
                "insert into bulbOnStream;\n";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(queries);

        siddhiAppRuntime.addCallback("analyzeStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("fireAlarmEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"id1", 20d});
        inputHandler.send(new Object[]{"id2", 20d});
        Thread.sleep(2000);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void timeWindowTest4() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.time(2 sec, 5) " +
                "select symbol,price,volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void timeWindowTest5() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, time long, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.time(time) " +
                "select symbol,price,volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void timeWindowTest6() throws InterruptedException, SiddhiAppCreationException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, time long, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.time(4.7) " +
                "select symbol,price,volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

}
