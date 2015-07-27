/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class SnapshotOutputRateLimitTestCase {
    static final Logger log = Logger.getLogger(SnapshotOutputRateLimitTestCase.class);
    private volatile int count;
    private long value;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        value = 0;
        eventArrived = false;
    }

    @Test
    public void testSnapshotOutputRateLimitQuery1() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest1') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        count++;
                        Assert.assertTrue("192.10.1.3".equals(event.getData(0)));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        Thread.sleep(10);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertTrue("Number of output event value", 1 <= count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery2() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest2') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    EventPrinter.print(events);
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        count++;
                        Assert.assertTrue("192.10.1.3".equals(event.getData(0)));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        Thread.sleep(500);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        executionPlanRuntime.shutdown();

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);

    }

    @Test
    public void testSnapshotOutputRateLimitQuery3() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest3') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        count++;
                        Assert.assertTrue("192.10.1.3".equals(event.getData(0)) || "192.10.1.4".equals(event.getData(0)));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 3, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery4() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest4') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
                for (Event event : events) {
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        value++;
                        Assert.assertTrue("192.10.1.5".equals(event.getData(0)) || "192.10.1.3".equals(event.getData(0)) || "192.10.1.4".equals(event.getData(0)));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles ", 3, count);
        Assert.assertEquals("Number of output events  ", 7, value);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery5() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest5') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip,  sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
                if (count == 3) {
                    Assert.assertTrue((Long) events[0].getData(1) == 5l && (Long) events[1].getData(1) == 16l);
                }
                for (Event event : events) {
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        value++;
                        Assert.assertTrue("192.10.1.5".equals(event.getData(0)) || "192.10.1.3".equals(event.getData(0)));

                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 3, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery6() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest6') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
                value += events.length;
                if (count == 1) {
                    Assert.assertTrue((Long) events[0].getData(0) == 6l);
                } else if (count == 2) {
                    Assert.assertTrue((Long) events[0].getData(0) == 10l);
                }
                eventArrived = true;

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 2, count);
        Assert.assertTrue("Number of output events", 4 >= value);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery7() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest7') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
                value += events.length;
                if (count == 1 || count == 2) {
                    Assert.assertTrue((Long) events[0].getData(0) == 6l);
                } else if (count == 3) {
                    Assert.assertTrue((Long) events[0].getData(0) == 16l);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 3, count);
        Assert.assertTrue("Number of output event value", 6 >= value);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery8() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest8') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
                if (count == 1) {
                    Assert.assertTrue((Long) events[0].getData(0) == 9l);
                } else if (count == 2) {
                    Assert.assertTrue((Long) events[0].getData(0) == 12l);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery9() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test9");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest9') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
                if (count == 1) {
                    Assert.assertTrue((Long) events[0].getData(0) == 9l);
                } else if (count == 2) {
                    Assert.assertTrue((Long) events[0].getData(0) == 9l);
                } else if (count == 3) {
                    Assert.assertTrue((Long) events[0].getData(0) == 21l);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event with value", 3, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery10() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test10");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest10') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        count++;
                        Assert.assertTrue("192.10.1.5".equals(event.getData(0)) || "192.10.1.3".equals(event.getData(0)));
                    }
                }
                eventArrived = true;
            }
        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery11() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test11");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest11') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        count++;
                        Assert.assertTrue("192.10.1.5".equals(event.getData(0)) || "192.10.1.3".equals(event.getData(0)));
                    }
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery12() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test12");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest12') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 1, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery13() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test13");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest13') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        count++;
                        Assert.assertTrue("192.10.1.3".equals(event.getData(0)) || "192.10.1.5".equals(event.getData(0)));
                    }
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event greater then equal to 4 ", true, count >= 4);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery14() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test14");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest14') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        Assert.fail("Remove events emitted");
                    } else {
                        count++;
                        Assert.assertTrue("192.10.1.3".equals(event.getData(0)) || "192.10.1.5".equals(event.getData(0)));
                    }
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery15() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test15");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest15') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    value++;
                } else if (value == 1) {
                    Assert.assertNull(inEvents);
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, value);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery16() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test16");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest16') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count++;
                    for (Event event : inEvents) {
                        Assert.assertTrue("192.10.1.5".equals(event.getData(0)) || "192.10.1.3".equals(event.getData(0)));
                    }
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles with inEvents", 2, count);
        Assert.assertEquals("Number of output event", 4, value);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery17() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test17");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest17') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select  ip " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (inEvents != null) {
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertTrue("Number of output event bundles greater than 3 ", count > 3);
        Assert.assertEquals("Number of output event value", 8, value);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery18() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test18");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest18') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip, sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count++;
                    if (count == 1) {
                        Assert.assertTrue((Long) inEvents[0].getData(1) == 9l);
                    } else if (count == 2) {
                        Assert.assertTrue((Long) inEvents[0].getData(1) == 12l);
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery19() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test19");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest19') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select  ip, sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 3) {
                    Assert.assertTrue((Long) inEvents[0].getData(1) == 9l);
                } else if (count == 4) {
                    Assert.assertTrue((Long) inEvents[0].getData(1) == 9l);
                } else if (count == 5) {
                    Assert.assertTrue((Long) inEvents[0].getData(1) == 21l);
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 5, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery20() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test20");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest20') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select  ip, sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 3 || count == 4) {
                    Assert.assertTrue((Long) inEvents[0].getData(1) == 3l || (Long) inEvents[1].getData(1) == 6l);
                } else if (count == 5) {
                    Assert.assertTrue((Long) inEvents[0].getData(1) == 5l || (Long) inEvents[1].getData(1) == 16l);
                }
                if (inEvents != null) {
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 5, count);
        Assert.assertEquals("Number of output event value", 8, value);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void testSnapshotOutputRateLimitQuery21() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test21");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@Plan:name('SnapshotOutputRateLimitTest21') " +
                "" +
                "define stream LoginEvents (timeStamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip, sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 3) {
                    Assert.assertTrue((Long) inEvents[0].getData(1) == 3l || (Long) inEvents[1].getData(1) == 6l);
                } else if (count == 5) {
                    Assert.assertTrue((Long) inEvents[0].getData(1) == 2l || (Long) inEvents[1].getData(1) == 10l);
                }
                if (inEvents != null) {
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");

        executionPlanRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 5, count);
        Assert.assertEquals("Number of output events", 4, value);

        executionPlanRuntime.shutdown();

    }
}
