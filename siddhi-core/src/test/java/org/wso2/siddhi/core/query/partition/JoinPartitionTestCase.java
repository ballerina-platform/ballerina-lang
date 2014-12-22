/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.partition;

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
                + "partition with (symbol of cseEventStream, company of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.user== twitterStream.user " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, cseEventStream.volume " +
                "insert into outputStream ;" + "" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if(event.isExpired()){
                        removeEventCount++;
                    } else{
                        inEventCount++;
                    }
                    eventArrived = true;
                };
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});

        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "Hellno World", "WSO2"});

        Thread.sleep(2000);
        Assert.assertEquals(3, inEventCount);
        Assert.assertEquals(3, removeEventCount);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testJoinPartition2() throws InterruptedException {
        log.info("Join partition test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (symbol of cseEventStream, company of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.user== twitterStream.user " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, cseEventStream.volume " +
                "insert into outputStream ;" + "" +
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if(event.isExpired()){
                        removeEventCount++;
                    } else{
                        inEventCount++;
                    }
                    eventArrived = true;
                };
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2"});

        cseEventStreamHandler.send(new Object[]{"IBM", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "IBM"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "IBM"});

        Thread.sleep(2000);
        Assert.assertEquals(6, inEventCount);
        Assert.assertEquals(6, removeEventCount);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void testJoinPartition3() throws InterruptedException {
        log.info("Join partition test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (symbol of cseEventStream, company of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.user== twitterStream.user " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, cseEventStream.volume " +
                "insert into #outputStream ;" +
                "@info(name = 'query2') from #outputStream select symbol,user insert into outStream;"+
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                inEventCount = inEventCount+events.length;
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2"});

        cseEventStreamHandler.send(new Object[]{"IBM", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "IBM"});

        twitterStreamHandler.send(new Object[]{"User1", "World", "IBM"});

        Thread.sleep(2000);
        Assert.assertEquals(12, inEventCount);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testJoinPartition4() throws InterruptedException {
        log.info("Join partition test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, user string,volume int);  @config(async = 'true')define stream twitterStream (user string, tweet string, company string);"
                + "partition with (symbol of cseEventStream, company of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.user== twitterStream.user " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, cseEventStream.volume " +
                "insert into #outputStream ;" +
                "@info(name = 'query2') from #outputStream select symbol,user insert into outputStream;"+
                "end ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                inEventCount = inEventCount+events.length;
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

        Assert.assertEquals(14, inEventCount);
        executionPlanRuntime.shutdown();

    }


}
