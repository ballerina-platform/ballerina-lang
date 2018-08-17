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

package org.ballerinalang.siddhi.core.query.ratelimit;

import org.awaitility.Awaitility;
import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testcase for Snapshot output rate limiting.
 */
public class SnapshotOutputRateLimitTestCase {
    private static final Logger log = LoggerFactory.getLogger(SnapshotOutputRateLimitTestCase.class);
    private volatile AtomicInteger count;
    private long value;
    private volatile boolean eventArrived;
    private volatile int eventsSent;

    @BeforeMethod
    public void init() {
        count = new AtomicInteger(0);
        value = 0;
        eventArrived = false;
        eventsSent = 0;
    }

    @Test
    public void testSnapshotOutputRateLimitQuery1() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest1') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    eventArrived = true;
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        count.incrementAndGet();
                        AssertJUnit.assertTrue("192.10.1.3".equals(event.getData(0)));
                    }

                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        eventsSent++;
        Thread.sleep(10);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        eventsSent++;

        SiddhiTestHelper.waitForEvents(100, 1, count, 60000);
        siddhiAppRuntime.shutdown();

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 1);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertTrue("Number of output event value", 1 == count.get());
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery1"})
    public void testSnapshotOutputRateLimitQuery2() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest2') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventArrived = true;
                    EventPrinter.print(events);
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        count.incrementAndGet();
                        AssertJUnit.assertTrue("192.10.1.3".equals(event.getData(0)));
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        Thread.sleep(500);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertTrue("Number of output event value", 2 == count.get());
        Thread.sleep(2000);

    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery2"})
    public void testSnapshotOutputRateLimitQuery3() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest3') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        count.incrementAndGet();
                        AssertJUnit.assertTrue("192.10.1.3".equals(event.getData(0)) ||
                                "192.10.1.4".equals(event.getData(0)));
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);

        siddhiAppRuntime.shutdown();

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 3);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertTrue("Number of output event value", 3 == count.get());
        Thread.sleep(2000);

    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery3"})
    public void testSnapshotOutputRateLimitQuery4() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest4') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                count.incrementAndGet();
                for (Event event : events) {
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        value++;
                        AssertJUnit.assertTrue("192.10.1.5".equals(event.getData(0)) ||
                                "192.10.1.3".equals(event.getData(0)) || "192.10.1.4".equals(event.getData(0)));
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 3 && value == 7);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles ", 3, count.get());
        AssertJUnit.assertEquals("Number of output events  ", 7, value);

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery4"})
    public void testSnapshotOutputRateLimitQuery5() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest5') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents " +
                "select ip,  sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                count.incrementAndGet();
                if (count.get() == 3) {
                    AssertJUnit.assertTrue((Long) events[0].getData(1) == 5L && (Long) events[1].getData(1) == 16L);
                }
                for (Event event : events) {
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        value++;
                        AssertJUnit.assertTrue("192.10.1.5".equals(event.getData(0)) ||
                                "192.10.1.3".equals(event.getData(0)));

                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 3);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 3, count.get());
        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery5"})
    public void testSnapshotOutputRateLimitQuery6() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest6') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                count.incrementAndGet();
                value += events.length;
                if (count.get() == 1) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 3L || (Long) events[1].getData(0) == 6L);
                } else if (count.get() == 2) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 2L || (Long) events[1].getData(0) == 10L);
                }

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 2 && value == 4);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 2, count.get());
        AssertJUnit.assertTrue("Number of output events", 4 == value);
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery6"})
    public void testSnapshotOutputRateLimitQuery7() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest7') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                count.incrementAndGet();
                value += events.length;
                if (count.get() == 1 || count.get() == 2) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 3L || (Long) events[1].getData(0) == 6L);
                } else if (count.get() == 3) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 5L || (Long) events[1].getData(0) == 16L);
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 7 && value == 14);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 7, count.get());
        AssertJUnit.assertTrue("Number of output event value", 14 == value);

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery7"})
    public void testSnapshotOutputRateLimitQuery8() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest8') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                count.incrementAndGet();
                if (count.get() == 1) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 9L);
                } else if (count.get() == 2) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 12L);
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 2);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event value", 2, count.get());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery8"})
    public void testSnapshotOutputRateLimitQuery9() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test9");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest9') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                count.incrementAndGet();
                if (count.get() == 1) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 9L);
                } else if (count.get() == 2) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 9L);
                } else if (count.get() == 3) {
                    AssertJUnit.assertTrue((Long) events[0].getData(0) == 21L);
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 3);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertTrue("Number of output event with value", count.get() == 3);

        siddhiAppRuntime.shutdown();
        Thread.sleep(3000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery9"})
    public void testSnapshotOutputRateLimitQuery10() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test10");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest10') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(2 sec) " +
                "select  ip " +
                "output snapshot every 2 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        count.incrementAndGet();
                        AssertJUnit.assertTrue("192.10.1.5".equals(event.getData(0)) ||
                                "192.10.1.3".equals(event.getData(0)));
                    }
                }
            }
        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});

        Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 2);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event value", 2, count.get());

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery10"})
    public void testSnapshotOutputRateLimitQuery11() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test11");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest11') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        count.incrementAndGet();
                        AssertJUnit.assertTrue("192.10.1.5".equals(event.getData(0)) ||
                                "192.10.1.3".equals(event.getData(0)));
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 2);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event value", 2, count.get());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery11"})
    public void testSnapshotOutputRateLimitQuery12() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test12");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest12') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.incrementAndGet();
                eventArrived = true;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 1);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event value", 1, count.get());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery12"})
    public void testSnapshotOutputRateLimitQuery13() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test13");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest13') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        count.incrementAndGet();
                        AssertJUnit.assertTrue("192.10.1.3".equals(event.getData(0)) ||
                                "192.10.1.5".equals(event.getData(0)));
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 4);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event equal to 4 ", true, count.get() == 4);

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery13"})
    public void testSnapshotOutputRateLimitQuery14() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test14");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest14') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(2 sec) " +
                "select  ip " +
                "output snapshot every 2 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    if (event.isExpired()) {
                        AssertJUnit.fail("Remove events emitted");
                    } else {
                        count.incrementAndGet();
                        AssertJUnit.assertTrue("192.10.1.3".equals(event.getData(0)) ||
                                "192.10.1.5".equals(event.getData(0)));
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 2);

        AssertJUnit.assertTrue("Event arrived", (eventArrived && count.get() == 2));
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery14"})
    public void testSnapshotOutputRateLimitQuery15() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test15");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest15') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    value++;
                } else if (value == 1) {
                    AssertJUnit.assertNull(inEvents);
                }
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event value", 2, value);

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery15"})
    public void testSnapshotOutputRateLimitQuery16() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test16");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest16') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    count.incrementAndGet();
                    for (Event event : inEvents) {
                        AssertJUnit.assertTrue("192.10.1.5".equals(event.getData(0)) ||
                                "192.10.1.3".equals(event.getData(0)));
                    }
                    value += inEvents.length;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 2 && value == 4);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles with inEvents", 2, count.get());
        AssertJUnit.assertEquals("Number of output event", 4, value);

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery16"})
    public void testSnapshotOutputRateLimitQuery17() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test17");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest17') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select  ip " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count.incrementAndGet();
                    value += inEvents.length;
                    eventArrived = true;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 7 && value == 20);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles equal to 5 ", 7, count.get());
        AssertJUnit.assertEquals("Number of output event value", 20, value);

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery17"})
    public void testSnapshotOutputRateLimitQuery18() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test18");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest18') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip, sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 9L);
                    } else if (count.get() == 2) {
//                        AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 12L);
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 2);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event value", 2, count.get());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery18"})
    public void testSnapshotOutputRateLimitQuery19() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test19");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest19') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select  ip, sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    count.incrementAndGet();
                    if (count.get() == 1 || count.get() == 2) {
                        AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 9L);
                        AssertJUnit.assertTrue((Long) inEvents[1].getData(1) == 9L);
                    } else if (count.get() == 3 || count.get() == 4 || count.get() == 5) {
                        AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 21L);
                        AssertJUnit.assertTrue((Long) inEvents[1].getData(1) == 21L);
                        AssertJUnit.assertTrue((Long) inEvents[2].getData(1) == 21L);
                        AssertJUnit.assertTrue((Long) inEvents[3].getData(1) == 21L);
                    } else if (count.get() == 6 || count.get() == 7) {
                        AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 12L);
                        AssertJUnit.assertTrue((Long) inEvents[1].getData(1) == 12L);
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 7);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event value", 7, count.get());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery19"})
    public void testSnapshotOutputRateLimitQuery20() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test20");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest20') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(5 sec) " +
                "select  ip, sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    count.incrementAndGet();
                    if (count.get() == 1 || count.get() == 2) {
                        AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 3L &&
                                (Long) inEvents[1].getData(1) == 6L);
                    } else if (count.get() == 3 || count.get() == 4 || count.get() == 5) {
                        AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 5L &&
                                (Long) inEvents[1].getData(1) == 16L);
                        AssertJUnit.assertTrue((Long) inEvents[2].getData(1) == 5L &&
                                (Long) inEvents[3].getData(1) == 16L);
                    } else if (count.get() == 6 || count.get() == 7) {
                        AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 2L &&
                                (Long) inEvents[1].getData(1) == 10L);
                    }
                    value += inEvents.length;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 7 && value == 20);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 7, count.get());
        AssertJUnit.assertEquals("Number of output event value", 20, value);

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery20"})
    public void testSnapshotOutputRateLimitQuery21() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test21");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest21') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.time(1 sec) " +
                "select  ip, sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                count.incrementAndGet();
                if (count.get() == 2) {
                    AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 3L && (Long) inEvents[1].getData(1) == 6L);
                } else if (count.get() == 4) {
                    AssertJUnit.assertTrue((Long) inEvents[0].getData(1) == 2L && (Long) inEvents[1].getData(1) == 10L);
                }
                if (inEvents != null) {
                    value += inEvents.length;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 4 && value == 4);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 4, count.get());
        AssertJUnit.assertEquals("Number of output events", 4, value);

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);

    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery21"})
    public void testSnapshotOutputRateLimitQuery22() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test22");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest22') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(3) " +
                "select  ip, calls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                count.incrementAndGet();
                for (Event inEvent : inEvents) {
                    value++;
                    if (value == 1) {
                        AssertJUnit.assertEquals("192.10.1.5", (String) inEvent.getData(0));
                    } else if (value == 2) {
                        AssertJUnit.assertEquals("192.10.1.6", (String) inEvent.getData(0));
                    } else if (value == 3) {
                        AssertJUnit.assertEquals("192.10.1.7", (String) inEvent.getData(0));
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.6", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.7", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.8", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 1 && value == 3);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 1, count.get());
        AssertJUnit.assertEquals("Number of output events", 3, value);

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);

    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery22"})
    public void testSnapshotOutputRateLimitQuery23() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test23");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest23') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(3) " +
                "select  ip, sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                count.incrementAndGet();
                for (Event inEvent : inEvents) {
                    value++;
                    if (value == 1) {
                        AssertJUnit.assertEquals("192.10.1.5", (String) inEvent.getData(0));
                    } else if (value == 2) {
                        AssertJUnit.assertEquals("192.10.1.6", (String) inEvent.getData(0));
                    } else if (value == 3) {
                        AssertJUnit.assertEquals("192.10.1.7", (String) inEvent.getData(0));
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.6", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.7", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.8", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 1 && value == 3);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 1, count.get());
        AssertJUnit.assertEquals("Number of output events", 3, value);

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery23"})
    public void testSnapshotOutputRateLimitQuery24() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test24");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest23') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(3) " +
                "select  sum(calls) as totalCalls " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                count.incrementAndGet();
                for (Event inEvent : inEvents) {
                    value++;
                    if (value == 1) {
                        AssertJUnit.assertEquals((Long) 4L, inEvent.getData(0));
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.6", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.7", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.8", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 1 && value == 1);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 1, count.get());
        AssertJUnit.assertEquals("Number of output events", 1, value);

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery24"})
    public void testSnapshotOutputRateLimitQuery25() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test25");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest23') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(3) " +
                "select  sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                count.incrementAndGet();
                for (Event inEvent : inEvents) {
                    value++;
                    if (value == 1) {
                        AssertJUnit.assertEquals((Long) 1L, inEvent.getData(0));
                    } else if (value == 2) {
                        AssertJUnit.assertEquals((Long) 1L, inEvent.getData(0));
                    } else if (value == 3) {
                        AssertJUnit.assertEquals((Long) 2L, inEvent.getData(0));
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.6", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.7", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.8", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 1 && value == 3);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 1, count.get());
        AssertJUnit.assertEquals("Number of output events", 3, value);

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery25"})
    public void testSnapshotOutputRateLimitQuery26() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test26");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest23') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(3) " +
                "select ip, sum(calls) as totalCalls " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                count.incrementAndGet();
                for (Event inEvent : inEvents) {
                    value++;
                    if (value == 1) {
                        AssertJUnit.assertEquals((Long) 1L, inEvent.getData(1));
                    } else if (value == 2) {
                        AssertJUnit.assertEquals((Long) 1L, inEvent.getData(1));
                    } else if (value == 3) {
                        AssertJUnit.assertEquals((Long) 2L, inEvent.getData(1));
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.6", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.7", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.8", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 1 && value == 3);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 1, count.get());
        AssertJUnit.assertEquals("Number of output events", 3, value);

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery26"})
    public void testSnapshotOutputRateLimitQuery27() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test27");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest27') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(3) " +
                "select ip  " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                count.incrementAndGet();
                for (Event inEvent : inEvents) {
                    value++;
                    if (value == 1) {
                        AssertJUnit.assertEquals("192.10.1.5", (String) inEvent.getData(0));
                    } else if (value == 2) {
                        AssertJUnit.assertEquals("192.10.1.6", (String) inEvent.getData(0));
                    } else if (value == 3) {
                        AssertJUnit.assertEquals("192.10.1.7", (String) inEvent.getData(0));
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.6", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.7", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.8", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() == 1 && value == 3);

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertEquals("Number of output event bundles", 1, count.get());
        AssertJUnit.assertEquals("Number of output events", 3, value);

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testSnapshotOutputRateLimitQuery27"})
    public void testSnapshotOutputRateLimitQuery28() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test28");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('SnapshotOutputRateLimitTest28') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string, calls int);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(3) " +
                "select ip  " +
                "group by ip " +
                "output snapshot every 1 sec " +
                "insert all events into uniqueIps ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                count.incrementAndGet();
                if (inEvents != null) {
                    for (Event inEvent : inEvents) {
                        value++;
                        if (value == 1) {
                            AssertJUnit.assertEquals("192.10.1.5", (String) inEvent.getData(0));
                        } else if (value == 2) {
                            AssertJUnit.assertEquals("192.10.1.3", (String) inEvent.getData(0));
                        } else if (value == 3) {
                            AssertJUnit.assertEquals("192.10.1.4", (String) inEvent.getData(0));
                        } else if (value == 4) {
                            AssertJUnit.assertEquals("192.10.1.5", (String) inEvent.getData(0));
                        } else if (value == 5) {
                            AssertJUnit.assertEquals("192.10.1.6", (String) inEvent.getData(0));
                        } else if (value == 6) {
                            AssertJUnit.assertEquals("192.10.1.7", (String) inEvent.getData(0));
                        }
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");

        siddhiAppRuntime.start();

        Thread.sleep(2100);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 1});
        Thread.sleep(1200);
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.6", 1});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.7", 2});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.8", 10});

        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                eventArrived && count.get() > 2 && value == 6);

        siddhiAppRuntime.shutdown();

        AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        AssertJUnit.assertTrue("Number of output event bundles count > 2", count.get() > 2);
        AssertJUnit.assertEquals("Number of output events", 6, value);
    }
}
