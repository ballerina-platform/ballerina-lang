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

package org.ballerinalang.siddhi.core.query.function;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testcase for InstanceOf function.
 */
public class InstanceOfFunctionTestCase {

    private static final Logger log = LoggerFactory.getLogger(InstanceOfFunctionTestCase.class);
    private int count;
    private boolean eventArrived;

    @BeforeMethod
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

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{19900813115534L, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{1990, false, 602, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        Thread.sleep(100);
        org.testng.AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

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

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{19900813115534L, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534L, "notAvailable", 602, "temperature", 90.34344, 20.44345, 2.3f,
                20.44345});
        Thread.sleep(100);
        org.testng.AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

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

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{19900813115534L, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534L, true, 60232434.657, "temperature", 90.34344, 20.44345, 2.3f,
                20.44345});
        Thread.sleep(100);
        org.testng.AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

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

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{19900813115534L, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534L, true, 602, 90.34344, 90.34344, 20.44345, 2.3f, 20.44345});
        Thread.sleep(1000);
        org.testng.AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

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

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{19900813115534L, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534L, true, 602, "temperature", "90.3434", 20.44345, 2.3f, 20.44345});
        Thread.sleep(100);
        org.testng.AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

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

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{19900813115534L, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{19900813115534L, true, 602, "temperature", 90.34344, 20.44345, 2.3, 20.44345});
        Thread.sleep(100);
        org.testng.AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testInstanceOfLongFunctionExtensionExceptionTestCase() throws InterruptedException {
        log.info("testInstanceOfLongFunctionExtensionException TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, " +
                "latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfLong(timestamp,sensorId) as valid, timestamp " +
                "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(true, inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(false, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{19900813115534L, false, 601, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        inputHandler.send(new Object[]{1990, false, 602, "temperature", 90.34344, 20.44345, 2.3f, 20.44345});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testInstanceOfBooleanFunctionExtensionExceptionTestCase() throws InterruptedException {
        log.info("testInstanceOfBooleanFunctionExtensionException TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, " +
                "latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfBoolean(isPowerSaverEnabled,sensorName) as valid, " +
                "isPowerSaverEnabled " +
                "insert into outputStream;");

        siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testInstanceOfIntegerFunctionExtensionExceptionTestCase() throws InterruptedException {
        log.info("testInstanceOfIntegerFunctionExtensionException TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, " +
                "latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfInteger(sensorId,sensorName) as valid, sensorId " +
                "insert into outputStream;");

        siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testInstanceOfStringFunctionExtensionExceptionTestCase() throws InterruptedException {
        log.info("testInstanceOfStringFunctionExtensionException TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, " +
                "latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfString(sensorName,sensorId) as valid " +
                "insert into outputStream;");

        siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testInstanceOfDoubleFunctionExtensionExceptionTestCase() throws InterruptedException {
        log.info("testInstanceOfDoubleFunctionExtensionException TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, " +
                "latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfDouble(longitude,sensorName) as valid, longitude " +
                "insert into outputStream;");

        siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testInstanceOfFloatFunctionExtensionExceptionTestCase() throws InterruptedException {
        log.info("testInstanceOfFloatFunctionExtensionException TestCase");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (timestamp long, " +
                "isPowerSaverEnabled bool, sensorId int , sensorName string, longitude double, " +
                "latitude double, " +
                "humidity float, sensorValue double);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorName ,instanceOfFloat(humidity,sensorName) as valid, longitude " +
                "insert into outputStream;");

        siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);
    }
}
