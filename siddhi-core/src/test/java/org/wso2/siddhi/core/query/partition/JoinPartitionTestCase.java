/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.query.partition;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class JoinPartitionTestCase {
    private static final Logger log = Logger.getLogger(JoinPartitionTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testJoinPartition1() throws InterruptedException {
        log.info("Join partition test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.volume " +
                "insert into outputStream ;" + "" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});

        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "Hellno World", "WSO2"});

        Thread.sleep(2000);
        Assert.assertEquals(2, inEventCount);
        Assert.assertEquals(2, removeEventCount);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testJoinPartition2() throws InterruptedException {
        log.info("Join partition test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, cseEventStream.volume " +
                "insert into outputStream ;" + "" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2"});

        cseEventStreamHandler.send(new Object[]{"IBM", "User2", 100});
        twitterStreamHandler.send(new Object[]{"User2", "Hello World", "IBM"});
        twitterStreamHandler.send(new Object[]{"User2", "World", "IBM"});

        Thread.sleep(2000);
        Assert.assertEquals(4, inEventCount);
        Assert.assertEquals(4, removeEventCount);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void testJoinPartition3() throws InterruptedException {
        log.info("Join partition test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, cseEventStream.volume " +
                "insert into #outputStream ;" +
                "@info(name = 'query2') from #outputStream select symbol,user insert into outStream;" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2"});

        cseEventStreamHandler.send(new Object[]{"IBM", "User2", 100});
        twitterStreamHandler.send(new Object[]{"User2", "Hello World", "IBM"});

        twitterStreamHandler.send(new Object[]{"User2", "World", "IBM"});

        Thread.sleep(2000);
        Assert.assertEquals(4, inEventCount);
        Assert.assertEquals(4, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testJoinPartition4() throws InterruptedException {
        log.info("Join partition test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, cseEventStream.volume " +
                "insert into #outputStream ;" +
                "@info(name = 'query2') from #outputStream select symbol,user insert into outputStream;" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        InputHandler outputStreamStreamHandler = executionPlanRuntime.getInputHandler("outputStream");

        executionPlanRuntime.start();

        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2"});

        cseEventStreamHandler.send(new Object[]{"IBM", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "IBM"});

        twitterStreamHandler.send(new Object[]{"User1", "World", "IBM"});
        outputStreamStreamHandler.send(new Object[]{"GOOG", "new_user_1"});
        outputStreamStreamHandler.send(new Object[]{"GOOG", "new_user_2"});

        Thread.sleep(2000);

        Assert.assertEquals(6, inEventCount);
        Assert.assertEquals(4, removeEventCount);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testJoinPartition5() throws InterruptedException {
        log.info("Join partition test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
                "@config(async = 'true') " +
                "define stream cseEventStream (symbol string, user string,volume int); " +
                "@config(async = 'true')" +
                "define stream twitterStream (user string, tweet string, company string);" +
                " " +
                "partition with (user of cseEventStream) begin " +
                "@info(name = 'query2') from cseEventStream select symbol, user, sum(volume) as volume insert into #cseInnerStream;" +
                "@info(name = 'query1') from #cseInnerStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on twitterStream.company== #cseInnerStream.symbol " +
                "select #cseInnerStream.user as user,twitterStream.tweet as tweet, twitterStream.company, #cseInnerStream.volume ,  #cseInnerStream.symbol " +
                "insert into outputStream ;" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                    }
                }
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");

        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 200});
        cseEventStreamHandler.send(new Object[]{"IBM", "User2", 500});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "IBM"});
        twitterStreamHandler.send(new Object[]{"User3", "Hello World", "GOOG"});

        Thread.sleep(2000);

        Assert.assertEquals(2, inEventCount);
        Assert.assertEquals(2, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testJoinPartition6() throws InterruptedException {
        log.info("Join partition test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream) begin " +
                "@info(name = 'query2') from cseEventStream select symbol, user, sum(volume) as volume insert into #cseEventStream;" +
                "@info(name = 'query1') from #cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on twitterStream.company== #cseEventStream.symbol " +
                "select #cseEventStream.user as user,twitterStream.tweet as tweet, twitterStream.company, #cseEventStream.volume ,  #cseEventStream.symbol " +
                "insert into outputStream ;" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                inEventCount = events.length + inEventCount;
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");

        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 200});
        cseEventStreamHandler.send(new Object[]{"IBM", "User2", 500});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "IBM"});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});


        Thread.sleep(2000);

        Assert.assertEquals(4, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testJoinPartition7() throws InterruptedException {
        log.info("Join partition test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string, volume int);"
                + "partition with (volume>=100 as 'large' or volume<100 as 'small' of cseEventStream, volume>=100 as 'large' or volume<100 as 'small' of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.user== twitterStream.user " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, cseEventStream.volume,twitterStream.company " +
                "insert into outputStream;" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                    }
                }
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");

        executionPlanRuntime.start();

        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 200});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2", 200});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2", 200});

        cseEventStreamHandler.send(new Object[]{"IBM", "User1", 10});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2", 10});
        twitterStreamHandler.send(new Object[]{"User1", "World", "IBM", 10});

        Thread.sleep(1000);

        Assert.assertEquals(4, inEventCount);
        Assert.assertEquals(4, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testJoinPartition8() throws InterruptedException {
        log.info("Join partition test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.volume " +
                "insert into outputStream ;" + "" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});

        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User2", "Hellno World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User3", "Hellno World", "WSO2"});

        Thread.sleep(2000);
        Assert.assertEquals(3, inEventCount);
        Assert.assertEquals(3, removeEventCount);
        executionPlanRuntime.shutdown();

    }
}
