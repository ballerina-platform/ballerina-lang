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

package org.wso2.siddhi.core.query.function;

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

public class InstanceOfFunctionTestCase {

    static final Logger log = Logger.getLogger(InstanceOfFunctionTestCase.class);
    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testInstanceOfLongFunctionExtensionTestCase() throws InterruptedException {
        log.info("testInstanceOfLongFunctionExtension TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfLong(timestamp) as valid, timestamp " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{19900813115534l, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{1990, false, 602, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testInstanceOfBooleanFunctionExtensionTestCase() throws InterruptedException {
        log.info("testInstanceOfBooleanFunctionExtension TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfBoolean(isPowerSaverEnabled) as valid, isPowerSaverEnabled " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{19900813115534l, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534l, "notAvailable", 602, "temperature", 90.34344, 20.44345, 2.3f,
                20.44345});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testInstanceOfIntegerFunctionExtensionTestCase() throws InterruptedException {
        log.info("testInstanceOfIntegerFunctionExtension TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfInteger(sensorId) as valid, sensorId " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{19900813115534l, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534l, true, 60232434.657, "temperature", 90.34344, 20.44345, 2.3f,
                20.44345});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testInstanceOfStringFunctionExtensionTestCase() throws InterruptedException {
        log.info("testInstanceOfStringFunctionExtension TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfString(sensorName) as valid " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{19900813115534l, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534l, true, 602, 90.34344, 90.34344, 20.44345, 2.3f, 20.44345});
        Thread.sleep(1000);
        junit.framework.Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testInstanceOfDoubleFunctionExtensionTestCase() throws InterruptedException {
        log.info("testInstanceOfDoubleFunctionExtension TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfDouble(longitude) as valid, longitude " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{19900813115534l, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534l, true, 602, "temperature", "90.3434", 20.44345, 2.3f, 20.44345});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testInstanceOfFloatFunctionExtensionTestCase() throws InterruptedException {
        log.info("testInstanceOfFloatFunctionExtension TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfFloat(humidity) as valid, longitude " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{19900813115534l, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534l, true, 602, "temperature", 90.34344, 20.44345, 2.3, 20.44345});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }
}
