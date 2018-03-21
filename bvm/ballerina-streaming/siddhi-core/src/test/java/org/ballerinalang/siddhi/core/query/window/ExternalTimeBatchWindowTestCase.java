/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since Dec 23, 2015
 */
public class ExternalTimeBatchWindowTestCase {


    private static final Logger log = LoggerFactory.getLogger(TimeWindowTestCase.class);
    private static SiddhiManager siddhiManager;
    private int inEventCount;
    private int removeEventCount;
    private long sum;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void test02NoMsg() throws Exception {
        siddhiManager = new SiddhiManager();

        SiddhiAppRuntime runtime = simpleQueryRuntime();

        final AtomicBoolean recieved = new AtomicBoolean();
        runtime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                recieved.set(true);
                EventPrinter.print(timestamp, inEvents, removeEvents);
            }
        });

        InputHandler input = runtime.getInputHandler("jmxMetric");

        runtime.start();
        // external events' time stamp less than the window, should not have event recieved in call back.
        long now = System.currentTimeMillis();
        int length = 5;
        for (int i = 0; i < length; i++) {
            input.send(new Object[]{15, now + i * 1000});
        }

        Thread.sleep(1000);
        AssertJUnit.assertFalse("Event happens inner external time batch window, should not have event recieved in " +
                "callback!", recieved.get());

        runtime.shutdown();
    }

    private SiddhiAppRuntime simpleQueryRuntime() {
        String query = "define stream jmxMetric(cpu int, timestamp long); "
                + "@info(name='query')"
                + "from jmxMetric#window.externalTimeBatch(timestamp, 10 sec) "
                + "select avg(cpu) as avgCpu, count(1) as count insert into tmp;";

        return siddhiManager.createSiddhiAppRuntime(query);
    }

    // for test findable
    @Test
    public void test04ExternalJoin() {
        // TODO
    }

    @Test
    public void test05EdgeCase() throws Exception {
        siddhiManager = new SiddhiManager();

        // every 10 sec
        SiddhiAppRuntime runtime = simpleQueryRuntime();

        final AtomicInteger recCount = new AtomicInteger(0);
        runtime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                AssertJUnit.assertEquals(1, inEvents.length);
                recCount.incrementAndGet();
                double avgCpu = (Double) inEvents[0].getData()[0];
                if (recCount.get() == 1) {
                    AssertJUnit.assertEquals(15, avgCpu, 0);
                } else if (recCount.get() == 2) {
                    AssertJUnit.assertEquals(85, avgCpu, 0);
                }
                long count = (Long) inEvents[0].getData()[1];
                AssertJUnit.assertEquals(3, count);
            }
        });

        InputHandler input = runtime.getInputHandler("jmxMetric");
        runtime.start();
        // external events' time stamp less than the window, should not have event recieved in call back.
        long now = 0;
        int length = 3;
        for (int i = 0; i < length; i++) {
            input.send(new Object[]{15, now + i * 10});
        }

        // second round
        // if the trigger event mix with the last window, we should see the avgValue is not expected
        for (int i = 0; i < length; i++) {
            input.send(new Object[]{85, now + 10000 + i * 10}); // the first entity of the second round
        }
        // to trigger second round
        input.send(new Object[]{10000, now + 10 * 10000});

//        latch.await();// for debug

        Thread.sleep(1000);

        AssertJUnit.assertEquals(2, recCount.get());
    }

    @Test
    public void test01DownSampling() throws Exception {
        siddhiManager = new SiddhiManager();

        String stream = "define stream jmxMetric(cpu int, memory int, bytesIn long, bytesOut long, timestamp long);";
        String query = "@info(name = 'downSample') "
                + "from jmxMetric#window.externalTimeBatch(timestamp, 10 sec) "
                + "select "
                + "avg(cpu) as avgCpu, max(cpu) as maxCpu, min(cpu) as minCpu, "
                + " '|' as s, "
                + " avg(memory) as avgMem, max(memory) as maxMem, min(memory) as minMem, "
                + " '|' as s1, "
                + " avg(bytesIn) as avgBytesIn, max(bytesIn) as maxBytesIn, min(bytesIn) as minBytesIn, "
                + " '|' as s2, "
                + " avg(bytesOut) as avgBytesOut, max(bytesOut) as maxBytesOut, min(bytesOut) as minBytesOut, "
                + " '|' as s3, "
                + " timestamp as timeWindowEnds, "
                + " '|' as s4, "
                + " count(1) as metric_count "
                + " INSERT INTO tmp;";

        SiddhiManager sm = new SiddhiManager();
        SiddhiAppRuntime plan = sm.createSiddhiAppRuntime(stream + query);

        InputHandler input = plan.getInputHandler("jmxMetric");

        // stream call back doesn't follow the counter
        final AtomicInteger counter = new AtomicInteger();
        {
            // stream callback
            plan.addCallback("jmxMetric", new StreamCallback() {
                @Override
                public void receive(Event[] arg0) {
                    counter.addAndGet(arg0.length);
                }
            });
        }
        final AtomicInteger queryWideCounter = new AtomicInteger();
        {
            plan.addCallback("downSample", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inevents, Event[] removevents) {
                    int currentCount = queryWideCounter.addAndGet(inevents.length);
                    log.info(MessageFormat.format("Round {0} ====", currentCount));
                    log.info(" events count " + inevents.length);

                    EventPrinter.print(inevents);
                }

            });
        }

        plan.start();

        int round = 4;
        int eventsPerRound = 0;
        long externalTs = System.currentTimeMillis();
        for (int i = 0; i < round; i++) {
            eventsPerRound = sendEvent(input, i, externalTs);
            Thread.sleep(3000);
        }
        // trigger next round
        sendEvent(input, round, externalTs);

        plan.shutdown();
        Thread.sleep(1000);
        AssertJUnit.assertEquals(round * eventsPerRound + eventsPerRound, counter.get());
        AssertJUnit.assertEquals(round, queryWideCounter.get());
    }

    // one round of sending events
    private int sendEvent(InputHandler input, int ite, long externalTs) throws Exception {
        int len = 3;
        Event[] events = new Event[len];
        for (int i = 0; i < len; i++) {
            // cpu int, memory int, bytesIn long, bytesOut long, timestamp long
            events[i] = new Event(externalTs,
                    new Object[]{15 + 10 * i * ite, 1500 + 10 * i * ite, 1000L, 2000L, externalTs + ite * 10000 + i *
                            50});
        }

        input.send(events);
        return len;
    }

    @Test
    public void test1() throws InterruptedException {
        siddhiManager = new SiddhiManager();
        String inputStream = "define stream inputStream(currentTime long,value int); ";
        String query = " " +
                "@info(name='query') " +
                "from inputStream#window.externalTimeBatch(currentTime,5 sec) " +
                "select value " +
                "insert into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            int count = 0;

            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (count == 0) {
                    AssertJUnit.assertEquals(1, inEvents[0].getData(0));
                } else if (count == 1) {
                    AssertJUnit.assertEquals(6, inEvents[0].getData(0));
                } else if (count == 2) {
                    AssertJUnit.assertEquals(13, inEvents[0].getData(0));
                }
                count += 1;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{10000L, 1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{11000L, 2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{12000L, 3});
        Thread.sleep(100);
        inputHandler.send(new Object[]{13000L, 4});
        Thread.sleep(100);
        inputHandler.send(new Object[]{14000L, 5});
        Thread.sleep(100);
        inputHandler.send(new Object[]{15000L, 6});
        Thread.sleep(100);
        inputHandler.send(new Object[]{16500L, 7});
        Thread.sleep(100);
        inputHandler.send(new Object[]{17000L, 8});
        Thread.sleep(100);
        inputHandler.send(new Object[]{18000L, 9});
        Thread.sleep(100);
        inputHandler.send(new Object[]{19000L, 10});
        Thread.sleep(100);
        inputHandler.send(new Object[]{20000L, 11});
        Thread.sleep(100);
        inputHandler.send(new Object[]{20500L, 12});
        Thread.sleep(100);
        inputHandler.send(new Object[]{22000L, 13});
        Thread.sleep(100);
        inputHandler.send(new Object[]{23000L, 14});
        Thread.sleep(100);
    }

    @Test
    public void test2() throws InterruptedException {
        siddhiManager = new SiddhiManager();
        String inputStream = "define stream inputStream(currentTime long,value int); ";
        String query = " @info(name='query') " +
                "from inputStream#window.externalTimeBatch(currentTime,5 sec,1200) " +
                "select value " +
                "insert into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            int count = 0;

            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (count == 0) {
                    AssertJUnit.assertEquals(0L, inEvents[0].getData(0));
                    AssertJUnit.assertEquals(11L, inEvents[inEvents.length - 1].getData(0));
                }
                if (count == 1) {
                    AssertJUnit.assertEquals(12L, inEvents[0].getData(0));
                }
                count += 1;

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        for (long i = 0; i < 10000; i += 100) {
            inputHandler.send(new Object[]{i + 10000, i / 100});
            Thread.sleep(200);
        }

    }

    @Test
    public void schedulerLastBatchTriggerTest() throws InterruptedException {
        siddhiManager = new SiddhiManager();
        String inputStream = "define stream inputStream(currentTime long,value int); ";
        String query = " " +
                "@info(name='query') " +
                "from inputStream#window.externalTimeBatch(currentTime,5 sec, 0, 6 sec) " +
                "select value, currentTime " +
                "insert current events into outputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            int count = 0;

            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                if (count == 0) {
                    AssertJUnit.assertEquals(1, inEvents[0].getData(0));
                } else if (count == 1) {
                    AssertJUnit.assertEquals(6, inEvents[0].getData(0));
                } else if (count == 2) {
                    AssertJUnit.assertEquals(11, inEvents[0].getData(0));
                } else if (count == 3) {
                    AssertJUnit.assertEquals(14, inEvents[0].getData(0));
                } else if (count == 4) {
                    AssertJUnit.assertEquals(15, inEvents[0].getData(0));
                }
                count += 1;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{10000L, 1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{11000L, 2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{12000L, 3});
        Thread.sleep(100);
        inputHandler.send(new Object[]{13000L, 4});
        Thread.sleep(100);
        inputHandler.send(new Object[]{14000L, 5});
        Thread.sleep(100);
        inputHandler.send(new Object[]{15000L, 6});
        Thread.sleep(100);
        inputHandler.send(new Object[]{16500L, 7});
        Thread.sleep(100);
        inputHandler.send(new Object[]{17000L, 8});
        Thread.sleep(100);
        inputHandler.send(new Object[]{18000L, 9});
        Thread.sleep(100);
        inputHandler.send(new Object[]{19000L, 10});
        Thread.sleep(100);
        inputHandler.send(new Object[]{20100L, 11});
        Thread.sleep(100);
        inputHandler.send(new Object[]{20500L, 12});
        Thread.sleep(100);
        inputHandler.send(new Object[]{22000L, 13});
        Thread.sleep(100);
        inputHandler.send(new Object[]{25000L, 14});
        Thread.sleep(100);
        inputHandler.send(new Object[]{32000L, 15});
        Thread.sleep(100);
        inputHandler.send(new Object[]{33000L, 16});
        Thread.sleep(6000);

    }


    @Test
    public void externalTimeBatchWindowTest1() throws InterruptedException {
        log.info("externalTimeBatchWindow test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 6 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804342L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335814341L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335814345L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335824341L, "192.10.1.7"});

        Thread.sleep(1000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 2, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void externalTimeBatchWindowTest2() throws InterruptedException {
        log.info("externalTimeBatchWindow test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804342L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805340L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335814341L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335814345L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335824341L, "192.10.1.7"});

        Thread.sleep(1000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 2, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();


    }


    @Test
    public void externalTimeBatchWindowTest3() throws InterruptedException {
        log.info("externalTimeBatchWindow test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804342L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805341L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335814341L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335814345L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335824341L, "192.10.1.7"});

        Thread.sleep(1000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 3, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();


    }


    @Test
    public void externalTimeBatchWindowTest4() throws InterruptedException {
        log.info("externalTimeBatchWindow test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 6 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804999L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805000L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805999L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335806000L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335806001L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335824341L, "192.10.1.7"});

        Thread.sleep(1000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 3, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();


    }

    @Test
    public void externalTimeBatchWindowTest5() throws InterruptedException {
        log.info("externalTimeBatchWindow test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 3 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335804600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335804607L, "192.10.1.6"});

        Thread.sleep(5000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 1, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void externalTimeBatchWindowTest6() throws InterruptedException {
        log.info("externalTimeBatchWindow test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 3 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335804600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335804607L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335805599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335805607L, "192.10.1.6"});

        Thread.sleep(5000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 2, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();


    }

    @Test
    public void externalTimeBatchWindowTest7() throws InterruptedException {
        log.info("externalTimeBatchWindow test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 2 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335804600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335804607L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335805599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335805607L, "192.10.1.6"});
        Thread.sleep(3000);
        inputHandler.send(new Object[]{1366335805606L, "192.10.1.7"});
        inputHandler.send(new Object[]{1366335805605L, "192.10.1.8"});
        Thread.sleep(3000);
        inputHandler.send(new Object[]{1366335806606L, "192.10.1.9"});
        inputHandler.send(new Object[]{1366335806690L, "192.10.1.10"});
        Thread.sleep(3000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 4, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();


    }

    @Test
    public void externalTimeBatchWindowTest8() throws InterruptedException {
        log.info("externalTimeBatchWindow test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 2 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                for (Event event : inEvents) {
                    inEventCount++;
                    if (inEventCount == 1) {
                        AssertJUnit.assertEquals(4L, event.getData(2));
                    } else if (inEventCount == 2) {
                        AssertJUnit.assertEquals(3L, event.getData(2));
                    } else if (inEventCount == 3) {
                        AssertJUnit.assertEquals(5L, event.getData(2));
                    } else if (inEventCount == 4) {
                        AssertJUnit.assertEquals(7L, event.getData(2));
                    } else if (inEventCount == 5) {
                        AssertJUnit.assertEquals(2L, event.getData(2));
                    }
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335804600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335804607L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335805599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335805607L, "192.10.1.6"});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{1366335805606L, "192.10.1.7"});
        inputHandler.send(new Object[]{1366335805605L, "192.10.1.8"});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{1366335805606L, "192.10.1.91"});
        inputHandler.send(new Object[]{1366335805605L, "192.10.1.92"});
        inputHandler.send(new Object[]{1366335806606L, "192.10.1.9"});
        inputHandler.send(new Object[]{1366335806690L, "192.10.1.10"});
        Thread.sleep(3000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 5, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void externalTimeBatchWindowTest9() throws InterruptedException {
        log.info("externalTimeBatchWindow test9");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0,2 sec) " +
                "select timestamp, ip, count() as total  " +
                "group by ip " +
                "insert into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public synchronized void receive(Event[] events) {
                if (events != null) {
                    inEventCount = inEventCount + events.length;
                    for (Event event : events) {
                        sum = sum + (Long) event.getData(2);
                    }
                }
                eventArrived = true;
            }
        });

        final InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                int i = 0;
                long time = 1366335804341L;
                while (i < 10000) {

                    try {
                        inputHandler.send(new Object[]{time, "192.10.1." + Thread.currentThread().getId()});
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                    time += 1000;
                    i++;
                }
            }
        };
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();


        Thread.sleep(10000);
        AssertJUnit.assertEquals(10 * 10000, sum);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void externalTimeBatchWindowTest10() throws InterruptedException {
        log.info("externalTimeBatchWindow test10");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 2 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                for (Event event : inEvents) {
                    inEventCount++;
                    if (inEventCount == 1) {
                        AssertJUnit.assertEquals(4L, event.getData(2));
                    } else if (inEventCount == 2) {
                        AssertJUnit.assertEquals(3L, event.getData(2));
                    } else if (inEventCount == 3) {
                        AssertJUnit.assertEquals(5L, event.getData(2));
                    } else if (inEventCount == 4) {
                        AssertJUnit.assertEquals(7L, event.getData(2));
                    } else if (inEventCount == 5) {
                        AssertJUnit.assertEquals(2L, event.getData(2));
                    }
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335804600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335804607L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335805599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335805607L, "192.10.1.6"});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{1366335805606L, "192.10.1.7"});
        inputHandler.send(new Object[]{1366335805605L, "192.10.1.8"});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{1366335805606L, "192.10.1.91"});
        inputHandler.send(new Object[]{1366335805605L, "192.10.1.92"});
        inputHandler.send(new Object[]{1366335806606L, "192.10.1.9"});
        inputHandler.send(new Object[]{1366335806690L, "192.10.1.10"});
        Thread.sleep(3000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 5, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void externalTimeBatchWindowTest11() throws InterruptedException {
        log.info("externalTimeBatchWindow test11");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0) " +
                "select timestamp, ip, count() as total  " +
                "insert into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                for (Event event : inEvents) {
                    inEventCount++;
                    if (inEventCount == 1) {
                        AssertJUnit.assertEquals(4L, event.getData(2));
                    } else if (inEventCount == 2) {
                        AssertJUnit.assertEquals(7L, event.getData(2));
                    }
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335804600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335804607L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335805599L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805600L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335805607L, "192.10.1.6"});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{1366335805606L, "192.10.1.7"});
        inputHandler.send(new Object[]{1366335805605L, "192.10.1.8"});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{1366335805606L, "192.10.1.91"});
        inputHandler.send(new Object[]{1366335805605L, "192.10.1.92"});
        inputHandler.send(new Object[]{1366335806606L, "192.10.1.9"});
        inputHandler.send(new Object[]{1366335806690L, "192.10.1.10"});
        Thread.sleep(3000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 2, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void externalTimeBatchWindowTest12() throws InterruptedException {
        log.info("externalTimeBatchWindow Test12");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream cseEventStream (timestamp long, symbol string, price float, volume int); " +
                "define stream twitterStream (timestamp long, user string, tweet string, company string); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.externalTimeBatch(timestamp, 1 sec, 0) join twitterStream#window" +
                ".externalTimeBatch(timestamp, 1 sec, 0) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timestamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        inEventCount += (inEvents.length);
                    }
                    if (removeEvents != null) {
                        removeEventCount += (removeEvents.length);
                    }
                    eventArrived = true;
                }
            });
            InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
            InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
            siddhiAppRuntime.start();
            cseEventStreamHandler.send(new Object[]{1366335804341L, "WSO2", 55.6f, 100});
            twitterStreamHandler.send(new Object[]{1366335804341L, "User1", "Hello World", "WSO2"});
            twitterStreamHandler.send(new Object[]{1366335805301L, "User2", "Hello World2", "WSO2"});
            cseEventStreamHandler.send(new Object[]{1366335805341L, "WSO2", 75.6f, 100});
            cseEventStreamHandler.send(new Object[]{1366335806541L, "WSO2", 57.6f, 100});
            Thread.sleep(1000);
            org.testng.AssertJUnit.assertEquals(2, inEventCount);
            org.testng.AssertJUnit.assertEquals(0, removeEventCount);
            org.testng.AssertJUnit.assertTrue(eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void externalTimeBatchWindowTest13() throws InterruptedException {
        log.info("externalTimeBatchWindow Test13");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream cseEventStream (timestamp long, symbol string, price float, volume int); " +
                "define stream twitterStream (timestamp long, user string, tweet string, company string); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.externalTimeBatch(timestamp, 1 sec, 0) join twitterStream#window" +
                ".externalTimeBatch(timestamp, 1 sec, 0) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timestamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        inEventCount += (inEvents.length);
                    }
                    if (removeEvents != null) {
                        removeEventCount += (removeEvents.length);
                    }
                    eventArrived = true;
                }
            });
            InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
            InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
            siddhiAppRuntime.start();
            cseEventStreamHandler.send(new Object[]{1366335804341L, "WSO2", 55.6f, 100});
            twitterStreamHandler.send(new Object[]{1366335804341L, "User1", "Hello World", "WSO2"});
            twitterStreamHandler.send(new Object[]{1366335805301L, "User2", "Hello World2", "WSO2"});
            cseEventStreamHandler.send(new Object[]{1366335805341L, "WSO2", 75.6f, 100});
            cseEventStreamHandler.send(new Object[]{1366335806541L, "WSO2", 57.6f, 100});
            Thread.sleep(1000);
            org.testng.AssertJUnit.assertEquals(2, inEventCount);
            org.testng.AssertJUnit.assertEquals(1, removeEventCount);
            org.testng.AssertJUnit.assertTrue(eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void externalTimeBatchWindowTest14() throws InterruptedException {
        log.info("externalTimeBatchWindow test14");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, timestamp) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804342L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805340L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335814341L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335814345L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335824341L, "192.10.1.7"});

        Thread.sleep(1000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 2, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void externalTimeBatchWindowTest15() throws InterruptedException {
        log.info("externalTimeBatchWindow test15");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, timestamp, 100) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804342L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805341L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335814341L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335814345L, "192.10.1.7"});
        inputHandler.send(new Object[]{1366335824341L, "192.10.1.8"});
        inputHandler.send(new Object[]{1366335824351L, "192.10.1.9"});
        inputHandler.send(new Object[]{1366335824441L, "192.10.1.10"});

        Thread.sleep(1000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 4, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();


    }

    @Test
    public void externalTimeBatchWindowTest16() throws InterruptedException {
        log.info("externalTimeBatchWindow test16");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 100, true) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    for (Event event : inEvents) {
                        AssertJUnit.assertTrue(((Long) event.getData(0)) % 100 == 0);
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                    for (Event event : removeEvents) {
                        AssertJUnit.assertTrue(((Long) event.getData(0)) % 100 == 0);
                    }
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804342L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805341L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335814341L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335814345L, "192.10.1.7"});
        inputHandler.send(new Object[]{1366335824341L, "192.10.1.8"});
        inputHandler.send(new Object[]{1366335824351L, "192.10.1.9"});
        inputHandler.send(new Object[]{1366335824441L, "192.10.1.10"});

        Thread.sleep(1000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 4, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();


    }


    @Test
    public void externalTimeBatchWindowTest17() throws InterruptedException {
        log.info("externalTimeBatchWindow test17");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.externalTimeBatch(timestamp, 1 sec, 0, 100, false) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    for (Event event : inEvents) {
                        AssertJUnit.assertTrue(((Long) event.getData(0)) % 100 != 0);
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                    for (Event event : removeEvents) {
                        AssertJUnit.assertTrue(((Long) event.getData(0)) % 100 != 0);
                    }
                }

                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("LoginEvents");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1366335804341L, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804342L, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805341L, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335814341L, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335814345L, "192.10.1.7"});
        inputHandler.send(new Object[]{1366335824341L, "192.10.1.8"});
        inputHandler.send(new Object[]{1366335824351L, "192.10.1.9"});
        inputHandler.send(new Object[]{1366335824441L, "192.10.1.10"});

        Thread.sleep(1000);

        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        org.testng.AssertJUnit.assertEquals("In Events ", 4, inEventCount);
        org.testng.AssertJUnit.assertEquals("Remove Events ", 0, removeEventCount);
        siddhiAppRuntime.shutdown();


    }

}
