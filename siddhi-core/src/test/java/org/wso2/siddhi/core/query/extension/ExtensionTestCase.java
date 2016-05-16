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
package org.wso2.siddhi.core.query.extension;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.extension.util.CustomFunctionExtension;
import org.wso2.siddhi.core.query.extension.util.StringConcatAggregatorString;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class ExtensionTestCase {
    static final Logger log = Logger.getLogger(ExtensionTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void extensionTest1() throws InterruptedException {
        log.info("extension test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = ("@info(name = 'query1') from cseEventStream select price , custom:getAll(symbol) as toConcat " +
                "group by volume insert into mailOutput;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 3) {
                    Assert.assertEquals("WSO2ABC", inEvents[inEvents.length - 1].getData(1));
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 100l});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200l});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"ABC", 60.5f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void extensionTest2() throws InterruptedException, ClassNotFoundException {
        log.info("extension test2");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("custom:plus", CustomFunctionExtension.class);
        siddhiManager.setExtension("email:getAll", StringConcatAggregatorString.class);

        String cseEventStream = "define stream cseEventStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from cseEventStream select symbol , custom:plus(price,volume) as totalCount " +
                "insert into mailOutput;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(800l, inEvent.getData(1));
                    } else if (count == 2) {
                        Assert.assertEquals(805l, inEvent.getData(1));
                    } else if (count == 3) {
                        Assert.assertEquals(260l, inEvent.getData(1));
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700l, 100l});
        inputHandler.send(new Object[]{"WSO2", 605l, 200l});
        inputHandler.send(new Object[]{"ABC", 60l, 200l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void extensionTest3() throws InterruptedException {
        log.info("extension test3");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("custom:plus", CustomFunctionExtension.class);
        siddhiManager.setExtension("email:getAllNew", StringConcatAggregatorString.class);

        String cseEventStream = "" +
                "" +
                "define stream cseEventStream (symbol string, price float, volume long);";
        String query = ("" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select price , email:getAllNew(symbol,'') as toConcat " +
                "group by volume " +
                "insert into mailOutput;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 3) {
                    Assert.assertEquals("WSO2ABC", inEvents[inEvents.length - 1].getData(1));
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 100l});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200l});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"ABC", 60.5f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void extensionTest4() throws InterruptedException, ClassNotFoundException {
        log.info("extension test4");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("custom:plus", CustomFunctionExtension.class);
        siddhiManager.setExtension("email:getAll", StringConcatAggregatorString.class);


        String cseEventStream = "define stream cseEventStream (price long, volume long);";
        String query = ("@info(name = 'query1') from cseEventStream select  custom:plus(*) as totalCount " +
                "insert into mailOutput;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(800l, inEvent.getData(0));
                    } else if (count == 2) {
                        Assert.assertEquals(805l, inEvent.getData(0));
                    } else if (count == 3) {
                        Assert.assertEquals(260l, inEvent.getData(0));
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{700l, 100l});
        inputHandler.send(new Object[]{605l, 200l});
        inputHandler.send(new Object[]{60l, 200l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
