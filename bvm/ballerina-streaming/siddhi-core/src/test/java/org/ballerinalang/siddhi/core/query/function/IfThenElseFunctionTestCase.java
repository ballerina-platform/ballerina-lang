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
 * Testcase for queries that contain If-then-else.
 */
public class IfThenElseFunctionTestCase {
    private static final Logger log = LoggerFactory.getLogger(IfThenElseFunctionTestCase.class);

    private int count;

    @BeforeMethod
    public void init() {
        count = 0;
    }

    @Test
    public void ifFunctionExtensionTestCase1() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,'High','Low') as status " +
                "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue(inEvent.getData(0) instanceof Double);
                    AssertJUnit.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        AssertJUnit.assertEquals(50.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(20.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(2, count);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void ifFunctionExtensionTestCase2() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,'High',5) as status " +
                "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue(inEvent.getData(0) instanceof Double);
                    AssertJUnit.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        AssertJUnit.assertEquals(50.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(20.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(2, count);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void ifFunctionExtensionTestCase3() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(35,'High','Low') as status " +
                "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue(inEvent.getData(0) instanceof Double);
                    AssertJUnit.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        AssertJUnit.assertEquals(50.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(20.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(2, count);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void ifFunctionExtensionTestCase4() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,'High') as status " +
                "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue(inEvent.getData(0) instanceof Double);
                    AssertJUnit.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        AssertJUnit.assertEquals(50.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(20.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(2, count);
    }

    @Test
    public void ifFunctionExtensionTestCase5() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue int,gainValue int);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,sensorValue*5,sensorValue*10) as gainValue " +
                "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue(inEvent.getData(0) instanceof Integer);
                    AssertJUnit.assertTrue(inEvent.getData(1) instanceof Integer);
                    if (count == 1) {
                        AssertJUnit.assertEquals(50, inEvent.getData(0));
                        AssertJUnit.assertEquals(250, inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(20, inEvent.getData(0));
                        AssertJUnit.assertEquals(200, inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{50});
        inputHandler.send(new Object[]{20});
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(2, count);
    }

    @Test
    public void ifFunctionExtensionTestCase6() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(ifThenElse(sensorValue>35,true,false),'High','Low') as status " +
                "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(sensorEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue(inEvent.getData(0) instanceof Double);
                    AssertJUnit.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        AssertJUnit.assertEquals(50.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        AssertJUnit.assertEquals(20.4, inEvent.getData(0));
                        AssertJUnit.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("sensorEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(2, count);
    }
}
