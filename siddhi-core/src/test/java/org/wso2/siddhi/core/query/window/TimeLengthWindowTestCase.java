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
package org.wso2.siddhi.core.query.window;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class TimeLengthWindowTestCase {
    private static final Logger log = Logger.getLogger(TimeLengthWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;
    private int count = 0;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    /*
        Time Period < Window time
        Number of Events < Window length
    */
    @Test
    public void timeLengthWindowTest1() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.timeLength(4 sec,10) select symbol,price," +
                "volume insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    Assert.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        Thread.sleep(5000);


        Assert.assertEquals(4, inEventCount);
        Assert.assertEquals(4, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    /*
       Time Period > Window time
       Number of Events < Window length
    */
    @Test
    public void timeLengthWindowTest2() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.timeLength(2 sec,10) " +
                "select symbol,price,volume " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    Assert.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"Google", 80.5f, 2});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"Yahoo", 90.5f, 3});
        Thread.sleep(4000);
        Assert.assertEquals(4, inEventCount);
        Assert.assertEquals(4, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    /*
        Time Period < Window time
        Number of Events > Window length
    */
    @Test
    public void timeLengthWindowTest3() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorStream = "define stream sensorStream (id string, sensorValue float);";
        String query = "@info(name = 'query1') from sensorStream#window.timeLength(10 sec,4)" +
                " select id,sensorValue" +
                " insert all events into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    Assert.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"id1", 10d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id2", 20d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id3", 30d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id4", 40d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id5", 50d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id6", 60d});

        Thread.sleep(15000);

        Assert.assertEquals(6, inEventCount);
        Assert.assertEquals(6, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    /*
           Time Period > Window time
           Number of Events > Window length
    */
    @Test
    public void timeLengthWindowTest4() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorStream = "define stream sensorStream (id string, sensorValue float);";
        String query = "@info(name = 'query1') from sensorStream#window.timeLength(3 sec,4)" +
                " select id,sensorValue" +
                " insert all events into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    Assert.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"id1", 10d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id2", 20d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id3", 30d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id4", 40d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id5", 50d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id6", 60d});

        Thread.sleep(4000);

        Assert.assertEquals(6, inEventCount);
        Assert.assertEquals(6, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    /*
        Time period > Window time
        Number of events > length
    */
    @Test
    public void timeLengthWindowTest6() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorStream = "define stream sensorStream (id string, sensorValue int);";
        String query = "@info(name = 'query1') from sensorStream#window.timeLength(5 sec,5)" +
                " select id,sum(sensorValue) as sum" +
                " insert all events into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorStream + query);
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                System.out.println(count);
                if (count < 5) {
                    if(inEvents != null) {
//                        Assert.assertEquals((long) count + 1, (inEvents[0].getData(1)));
                    }
                } else {
                    if (inEvents != null) {
//                        Assert.assertEquals(5l, (inEvents[0].getData(1)));
                    }
                }
                count++;
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"id1", 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id2", 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id3", 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id4", 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id5", 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id6", 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id7", 1});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id8", 1});

        Thread.sleep(15000);

        Assert.assertEquals(8, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }


    /*
        Time period < Window time
        Number of events < length
    */
    @Test
    public void timeLengthWindowTest7() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorStream = "define stream sensorStream (id string, sensorValue int);";
        String query = "@info(name = 'query1') from sensorStream#window.timeLength(5 sec,5)" +
                " select id,sum(sensorValue) as sum" +
                " insert into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorStream + query);
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals((long) count + 1, (inEvents[0].getData(1)));

                count++;
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"id1", 1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"id2", 1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"id3", 1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"id4", 1});

        Thread.sleep(4000);

        Assert.assertEquals(4, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    /*
        Time period > Window time
        Number of events < Window length
    */
    @Test
    public void timeLengthWindowTest8() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorStream = "define stream sensorStream (id string, sensorValue int);";
        String query = "@info(name = 'query1') from sensorStream#window.timeLength(5 sec,5)" +
                " select id,sum(sensorValue) as sum" +
                " insert all events into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorStream + query);
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (count < 3) {
                    Assert.assertEquals((long) count + 1, (inEvents[0].getData(1)));
                } else {
                    if (inEvents != null) {
                        Assert.assertEquals(3l, (inEvents[0].getData(1)));
                    }
                }

                count++;
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"id1", 1});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"id2", 1});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"id3", 1});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"id4", 1});

        Thread.sleep(3000);

        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    /*
       Time Period < Window time
       Number of Events > Window length
    */
    @Test
    public void timeLengthWindowTest9() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorStream = "define stream sensorStream (id string, sensorValue double);";
        String query = "@info(name = 'query1') from sensorStream#window.timeLength(10 sec,5)" +
                " select id,sum(sensorValue) as sum" +
                " insert all events into outputStream ;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    Assert.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }

                if (count < 5) {
                    if (inEvents != null) {
                        Assert.assertEquals((double) count + 1, (inEvents[0].getData(1)));
                    }
                } else {
                    if (inEvents != null) {
                        Assert.assertEquals(6d, (inEvents[0].getData(1)));
                    }
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"id1", 1d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id2", 1d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id3", 1d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id4", 1d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id5", 1d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id6", 1d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id7", 1d});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"id8", 1d});

        Thread.sleep(15000);

        Assert.assertEquals(8, inEventCount);
        Assert.assertEquals(8, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }


}
