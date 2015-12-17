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
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class IfThenElseFunctionTestCase {
    static final Logger log = Logger.getLogger(IfThenElseFunctionTestCase.class);

    private int count;

    @Before
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

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    Assert.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        Assert.assertEquals(50.4, inEvent.getData(0));
                        Assert.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(20.4, inEvent.getData(0));
                        Assert.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(2, count);
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void ifFunctionExtensionTestCase2() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,'High',5) as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    Assert.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        Assert.assertEquals(50.4, inEvent.getData(0));
                        Assert.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(20.4, inEvent.getData(0));
                        Assert.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(2, count);
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void ifFunctionExtensionTestCase3() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(35,'High','Low') as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    Assert.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        Assert.assertEquals(50.4, inEvent.getData(0));
                        Assert.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(20.4, inEvent.getData(0));
                        Assert.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(2, count);
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void ifFunctionExtensionTestCase4() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,'High') as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    Assert.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        Assert.assertEquals(50.4, inEvent.getData(0));
                        Assert.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(20.4, inEvent.getData(0));
                        Assert.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(2, count);
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

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    Assert.assertTrue(inEvent.getData(0) instanceof Integer);
                    Assert.assertTrue(inEvent.getData(1) instanceof Integer);
                    if (count == 1) {
                        Assert.assertEquals(50, inEvent.getData(0));
                        Assert.assertEquals(250, inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(20, inEvent.getData(0));
                        Assert.assertEquals(200, inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50});
        inputHandler.send(new Object[]{20});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(2, count);
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

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    Assert.assertTrue(inEvent.getData(1) instanceof String);
                    if (count == 1) {
                        Assert.assertEquals(50.4, inEvent.getData(0));
                        Assert.assertEquals("High", inEvent.getData(1));
                    }
                    if (count == 2) {
                        Assert.assertEquals(20.4, inEvent.getData(0));
                        Assert.assertEquals("Low", inEvent.getData(1));
                    }

                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(2, count);
    }


}