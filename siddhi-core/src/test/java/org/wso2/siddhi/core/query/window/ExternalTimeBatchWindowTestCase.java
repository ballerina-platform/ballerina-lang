/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.query.window;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

/**
 * @since Dec 23, 2015
 *
 */
public class ExternalTimeBatchWindowTestCase {

    private static SiddhiManager siddhiManager;
    
    @BeforeClass
    public static void beforeClass() {
        siddhiManager = new SiddhiManager();
    }
    
    @AfterClass
    public static void afterClass() {
        siddhiManager.shutdown();
    }

    @Test
    public void test02NoMsg() throws Exception {
        ExecutionPlanRuntime runtime = simpleQueryRuntime();

        final AtomicBoolean recieved = new AtomicBoolean();
        runtime.addCallback("query", new QueryCallback() {

            @Override
            public void receive(long arg0, Event[] arg1, Event[] arg2) {
                recieved.set(true);
                System.out.println(arg1);
            }
        });

        InputHandler input = runtime.getInputHandler("jmxMetric");

        runtime.start();
        // external events' time stamp less than the window, should not have event recieved in call back.
        long now = System.currentTimeMillis();
        int length = 5;
        for (int i = 0; i < length; i++) {
            input.send(new Object[] { 15, now + i * 1000 });
        }
        
        Thread.sleep(1000);
        Assert.assertFalse("Event happens inner external time batch window, should not have event recieved in callback!", recieved.get());

        runtime.shutdown();
    }

    private ExecutionPlanRuntime simpleQueryRuntime() {
        String query = "define stream jmxMetric(cpu int, timestamp long); " 
                + "@info(name='query')"
                + "from jmxMetric#window.externalTimeBatch(timestamp, 10 sec) " 
                + "select avg(cpu) as avgCpu, count(1) as count insert into tmp;";

        return siddhiManager.createExecutionPlanRuntime(query);
    }

    /**
     * This case try to capture the case that the window get a chunk of event that exceed the time batch.
     * In this case, two next processor should be triggered.
     */
    @Test
    public void test03BunchChunkExceedBatch() {
        // TODO 
    }
    // for test findable
    @Test
    public void test04ExternalJoin() {
        // TODOs
    }
    
    @Test
    public void test05EdgeCase() throws Exception {
        // every 10 sec
        ExecutionPlanRuntime runtime = simpleQueryRuntime();

        final AtomicInteger recCount = new AtomicInteger(0);
//        final CountDownLatch latch = new CountDownLatch(2);// for debug
        runtime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long arg0, Event[] arg1, Event[] arg2) {
//                latch.countDown();
                Assert.assertEquals(1, arg1.length);
                recCount.incrementAndGet();
                int avgCpu = (Integer)arg1[0].getData()[0];
                if (recCount.get() == 1) {
                    Assert.assertEquals(15, avgCpu);
                } else if (recCount.get() == 2) {
                    Assert.assertEquals(85, avgCpu);
                }
                int count = (Integer) arg1[0].getData()[1];
                Assert.assertEquals(3, count);
            }
        });

        InputHandler input = runtime.getInputHandler("jmxMetric");
        runtime.start();
        // external events' time stamp less than the window, should not have event recieved in call back.
        long now = 0;
        int length = 3;
        for (int i = 0; i < length; i++) {
            input.send(new Object[] { 15, now + i * 10 });
        }
        
        // second round
        // if the trigger event mix with the last window, we should see the avgValue is not expected
        for (int i = 0; i < length; i++) {
            input.send(new Object[] { 85, now + 10000 + i * 10 }); // the first entity of the second round
        }
        // to trigger second round
        input.send(new Object[] { 10000, now + 10 * 10000 });
        
//        latch.await();// for debug

        Thread.sleep(1000);
        
        Assert.assertEquals(2, recCount.get());
    }

    @Test
    public void test06Pull76() throws Exception {
        String defaultStream = "define stream LoginEvents (myTime long, ip string, phone string,price int);";

        String query = " @info(name='pull76') "
                + " from LoginEvents#window.externalTimeBatch(myTime, 5 sec)  "
                + " select myTime, phone, ip, price, count(ip) as cntip , "
                + " min(myTime) as mintime, max(myTime) as maxtime "
                + " insert into events ;";
        
        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(defaultStream + query);

        InputHandler inputHandler = runtime.getInputHandler("LoginEvents");

        runtime.addCallback("pull76", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    System.out.println("======================== START ===============================");
                    int i = 0;
                    System.out.println(" Events Size:" + inEvents.length);
                    for (i = 0; i < inEvents.length; i++) {
                        Event e = inEvents[i];
                        System.out.println("----------------------------");
                        System.out.println(new Date((Long) e.getData(0)));
                        System.out.println("IP:" + e.getData(2));
                        System.out.println("price :" + e.getData(3));
                        System.out.println("count :" + e.getData(4));
                        System.out.println("mintime :" + new Date((Long) e.getData(5)) );
                        System.out.println("maxtime :" + new Date((Long) e.getData(6)) );
                        System.out.println("----------------------------");
                    }
                    System.out.println("======================== END  ===============================");

                }
            }
        });
        
        
        runtime.start();
        
        long start = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);
        c.add(Calendar.SECOND, 1);
        int i = 0;
        for (i = 0; i <= 10000; i++) {
            c.add(Calendar.SECOND, 1);
            inputHandler.send(c.getTime().getTime(),
                    new Object[] { c.getTime().getTime(), new String("192.10.1.1"), "1", new Random().nextInt(1000) });
        }
        long end = System.currentTimeMillis();
        System.out.printf("End : %d ", end - start);

        Thread.sleep(1000);
        runtime.shutdown();
    }
    
    @Test
    public void test01DownSampling() throws Exception {
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
        ExecutionPlanRuntime plan = sm.createExecutionPlanRuntime(stream + query);

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
                public void receive(long arg0, Event[] inevents, Event[] removeevents) {
                    int currentCount = queryWideCounter.addAndGet(inevents.length);
                    System.out.println(MessageFormat.format("Round {0} ====", currentCount));
                    System.out.println(" events count " + inevents.length);

                    for (Event e : inevents) {
                        Object[] tranformedData = e.getData();
                        for (Object o : tranformedData) {
                            System.out.print(o);
                            System.out.print(' ');
                        }
                        System.out.println(" events endendend");
                    }
                }

            });
        }

        plan.start();

        int round = 4;
        int eventsPerRound= 0;
        long externalTs = System.currentTimeMillis();
        for (int i = 0; i < round; i++) {
            eventsPerRound = sendEvent(input, i, externalTs);
            Thread.sleep(3000);
        }
        // trigger next round
        sendEvent(input, round, externalTs);

        plan.shutdown();
        Thread.sleep(1000);
        Assert.assertEquals(round * eventsPerRound + eventsPerRound, counter.get());
        Assert.assertEquals(round, queryWideCounter.get());
    }

    // one round of sending events
    private int sendEvent(InputHandler input, int ite, long externalTs) throws Exception {
        int len = 3;
        Event[] events = new Event[len];
        for (int i = 0; i < len; i++) {
            // cpu int, memory int, bytesIn long, bytesOut long, timestamp long
            events[i] = new Event(externalTs,
                    new Object[] { 15 + 10 * i * ite, 1500 + 10 * i * ite, 1000L, 2000L, externalTs + ite * 10000 + i * 50 });
        }

        input.send(events);
        return len;
    }

}
