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

package org.wso2.siddhi.core.query.ratelimit;


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

public class TimeOutputRateLimitTestCase {
    static final Logger log = Logger.getLogger(TimeOutputRateLimitTestCase.class);
    private volatile int count;
    private volatile int inEventCount;
    private volatile int removeEventCount;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
        inEventCount = 0;
        removeEventCount = 0;
    }

    @Test
    public void testTimeOutputRateLimitQuery1() throws InterruptedException {
        log.info("TimeOutputRateLimit test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest1') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output every 1 sec " +
                "insert into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.40"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 6, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery2() throws InterruptedException {
        log.info("TimeOutputRateLimit test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest2') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output all every 1 sec " +
                "insert into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1500);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.40"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 6, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery3() throws InterruptedException {
        log.info("TimeOutputRateLimit test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest3') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output every 1 sec " +
                "insert into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 8, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery4() throws InterruptedException {
        log.info("TimeOutputRateLimit test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest4') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output first every 1 sec " +
                "insert into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData(0)) || "192.10.1.9".equals(inEvents[0].getData(0)) || "192.10.1.30".equals(inEvents[0].getData(0)));
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 3, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery5() throws InterruptedException {
        log.info("TimeOutputRateLimit test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest5') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output last every 1 sec " +
                "insert into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData(0)) || "192.10.1.3".equals(inEvents[0].getData(0)) || "192.10.1.4".equals(inEvents[0].getData(0)) || "192.10.1.30".equals(inEvents[0].getData(0)));
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertTrue("Number of output event value", 3 <= count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery6() throws InterruptedException {
        log.info("TimeOutputRateLimit test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest6') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "group by ip " +
                "output first every 1 sec " +
                "insert into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 6, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery7() throws InterruptedException {
        log.info("TimeOutputRateLimit test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest7') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "group by ip " +
                "output last every 1 sec " +
                "insert into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 6, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery8() throws InterruptedException {
        log.info("TimeOutputRateLimit test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest8') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(2) " +
                "select ip " +
                "group by ip " +
                "output last every 1 sec " +
                "insert into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 5, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery9() throws InterruptedException {
        log.info("TimeOutputRateLimit test9");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest7') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(2) " +
                "select ip " +
                "group by ip " +
                "output last every 1 sec " +
                "insert expired events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    count += removeEvents.length;
                } else {
                    Assert.fail("inEvents emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1100);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery10() throws InterruptedException {
        log.info("TimeOutputRateLimit test10");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest10') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(2) " +
                "select ip, count(*) as total " +
                "group by ip " +
                "output first every 1 sec " +
                "insert expired events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    count += removeEvents.length;
                } else {
                    Assert.fail("inEvents emitted");
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1100);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery11() throws InterruptedException {
        log.info("TimeOutputRateLimit test11");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest11') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(2) " +
                "select ip, count(*) as total " +
                "group by ip " +
                "output first every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1100);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output in event value", 5, inEventCount);
        Assert.assertEquals("Number of output remove event value", 2, removeEventCount);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery12() throws InterruptedException {
        log.info("TimeOutputRateLimit test12");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest11') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(2) " +
                "select ip, count(*) as total " +
                "group by ip " +
                "output last every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1100);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output in event value", 4, inEventCount);
        Assert.assertEquals("Number of output remove event value", 3, removeEventCount);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeOutputRateLimitQuery13() throws InterruptedException {
        log.info("TimeOutputRateLimit test13");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest11') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(2) " +
                "select ip, count(*) as total " +
                "group by ip " +
                "output all every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1100);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output in event value", 6, inEventCount);
        Assert.assertEquals("Number of output remove event value", 3, removeEventCount);

        executionPlanRuntime.shutdown();

    }

}
