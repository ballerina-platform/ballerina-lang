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
package org.ballerinalang.siddhi.core.query.partition;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class JoinPartitionTestCase {
    private static final Logger log = LoggerFactory.getLogger(JoinPartitionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testJoinPartition1() throws InterruptedException {
        log.info("Join partition test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, user string,volume int);  define stream " +
                "twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.volume " +
                "insert all events into outputStream ;" + "" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
        siddhiAppRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});

        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "Hellno World", "WSO2"});

        SiddhiTestHelper.waitForEvents(100, 4, count, 6000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testJoinPartition2() throws InterruptedException {
        log.info("Join partition test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, user string,volume int);  define stream " +
                "twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, " +
                "cseEventStream.volume " +
                "insert all events into outputStream ;" + "" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
        siddhiAppRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2"});

        cseEventStreamHandler.send(new Object[]{"IBM", "User2", 100});
        twitterStreamHandler.send(new Object[]{"User2", "Hello World", "IBM"});
        twitterStreamHandler.send(new Object[]{"User2", "World", "IBM"});

        SiddhiTestHelper.waitForEvents(1000, 8, count, 12000);
        AssertJUnit.assertEquals(8, count.get());
        siddhiAppRuntime.shutdown();

    }


    @Test
    public void testJoinPartition3() throws InterruptedException {
        log.info("Join partition test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, user string,volume int);  define stream " +
                "twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, " +
                "cseEventStream.volume " +
                "insert all events into #outputStream ;" +
                "@info(name = 'query2') from #outputStream select symbol,user insert all events into outStream;" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
        siddhiAppRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2"});

        cseEventStreamHandler.send(new Object[]{"IBM", "User2", 100});
        twitterStreamHandler.send(new Object[]{"User2", "Hello World", "IBM"});

        twitterStreamHandler.send(new Object[]{"User2", "World", "IBM"});

        SiddhiTestHelper.waitForEvents(100, 8, count, 6000);
        AssertJUnit.assertEquals(8, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testJoinPartition4() throws InterruptedException {
        log.info("Join partition test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, user string,volume int);  define stream " +
                "twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, " +
                "cseEventStream.volume " +
                "insert all events into #outputStream ;" +
                "@info(name = 'query2') from #outputStream select symbol,user insert all events into outputStream;" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
        InputHandler outputStreamStreamHandler = siddhiAppRuntime.getInputHandler("outputStream");

        siddhiAppRuntime.start();

        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2"});

        cseEventStreamHandler.send(new Object[]{"IBM", "User1", 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "IBM"});

        twitterStreamHandler.send(new Object[]{"User1", "World", "IBM"});
        outputStreamStreamHandler.send(new Object[]{"GOOG", "new_user_1"});
        outputStreamStreamHandler.send(new Object[]{"GOOG", "new_user_2"});

        SiddhiTestHelper.waitForEvents(100, 10, count, 6000);
        AssertJUnit.assertEquals(10, count.get());
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testJoinPartition5() throws InterruptedException {
        log.info("Join partition test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "" +
                "define stream cseEventStream (symbol string, user string,volume int); " +
                "" +
                "define stream twitterStream (user string, tweet string, company string);" +
                " " +
                "partition with (user of cseEventStream) begin " +
                "@info(name = 'query2') from cseEventStream select symbol, user, sum(volume) as volume insert all " +
                "events into #cseInnerStream;" +
                "@info(name = 'query1') from #cseInnerStream#window.time(1 sec) join twitterStream#window.time(1 sec)" +
                " " +
                "on twitterStream.company== #cseInnerStream.symbol " +
                "select #cseInnerStream.user as user,twitterStream.tweet as tweet, twitterStream.company, " +
                "#cseInnerStream.volume ,  #cseInnerStream.symbol " +
                "insert all events into outputStream ;" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");

        siddhiAppRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 200});
        cseEventStreamHandler.send(new Object[]{"IBM", "User2", 500});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "IBM"});
        twitterStreamHandler.send(new Object[]{"User3", "Hello World", "GOOG"});

        SiddhiTestHelper.waitForEvents(100, 4, count, 6000);
        AssertJUnit.assertEquals(4, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testJoinPartition6() throws InterruptedException {
        log.info("Join partition test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, user string,volume int);  define stream " +
                "twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream) begin " +
                "@info(name = 'query2') from cseEventStream select symbol, user, sum(volume) as volume insert all " +
                "events into #cseEventStream;" +
                "@info(name = 'query1') from #cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec)" +
                " " +
                "on twitterStream.company== #cseEventStream.symbol " +
                "select #cseEventStream.user as user,twitterStream.tweet as tweet, twitterStream.company, " +
                "#cseEventStream.volume ,  #cseEventStream.symbol " +
                "insert all events into outputStream ;" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");

        siddhiAppRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 200});
        cseEventStreamHandler.send(new Object[]{"IBM", "User2", 500});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "IBM"});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});

        SiddhiTestHelper.waitForEvents(100, 4, count, 6000);
        AssertJUnit.assertEquals(4, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testJoinPartition7() throws InterruptedException {
        log.info("Join partition test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, user string,volume int);  define stream " +
                "twitterStream (user string, tweet string, company string, volume int);"
                + "partition with (volume>=100 as 'large' or volume<100 as 'small' of cseEventStream, volume>=100 as " +
                "'large' or volume<100 as 'small' of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.user== twitterStream.user " +
                "select cseEventStream.symbol as symbol, cseEventStream.user as user,twitterStream.tweet, " +
                "cseEventStream.volume,twitterStream.company " +
                "insert all events into outputStream;" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");

        siddhiAppRuntime.start();

        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 200});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2", 200});
        twitterStreamHandler.send(new Object[]{"User1", "World", "WSO2", 200});

        cseEventStreamHandler.send(new Object[]{"IBM", "User1", 10});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2", 10});
        twitterStreamHandler.send(new Object[]{"User1", "World", "IBM", 10});

        SiddhiTestHelper.waitForEvents(100, 8, count, 6000);
        AssertJUnit.assertEquals(8, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testJoinPartition8() throws InterruptedException {
        log.info("Join partition test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, user string,volume int);  define stream " +
                "twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.volume " +
                "insert all events into outputStream ;" + "" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
        siddhiAppRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});

        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User2", "Hellno World", "WSO2"});
        twitterStreamHandler.send(new Object[]{"User3", "Hellno World", "WSO2"});

        SiddhiTestHelper.waitForEvents(100, 6, count, 6000);
        AssertJUnit.assertEquals(6, count.get());
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testJoinPartition9() throws InterruptedException {
        log.info("Join partition test9");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, user string,volume int);  define stream " +
                "twitterStream (user string, tweet string, company string);"
                + "partition with (user of cseEventStream, user of twitterStream) begin @info(name = 'query1') " +
                "from cseEventStream#window.length(1) unidirectional join twitterStream#window.length(1) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.user, cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream" +
                ".volume " +
                "insert all events into outputStream ;" + "" +
                "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
        siddhiAppRuntime.start();

        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});

        cseEventStreamHandler.send(new Object[]{"WSO2", "User2", 100});
        twitterStreamHandler.send(new Object[]{"User2", "Hello World", "WSO2"});

        twitterStreamHandler.send(new Object[]{"User3", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"WSO2", "User3", 100});

        SiddhiTestHelper.waitForEvents(100, 2, count, 60000);
        AssertJUnit.assertEquals(2, count.get());
        siddhiAppRuntime.shutdown();


    }

    @Test
    public void testJoinPartition10() throws InterruptedException {
        log.info("Join partition test10");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "" +
                "define stream cseEventStream (symbol string, user string,volume int);  " +
                "define stream twitterStream (user string, tweet string, company string); " +
                "" +
                "partition with (user of cseEventStream, user of twitterStream) " +
                "begin " +
                "   @info(name = 'query1') " +
                "   from cseEventStream#window.length(1) unidirectional join twitterStream#window.length(1) " +
                "   select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.volume, " +
                "cseEventStream.user" +
                "   insert all events into outputStream1 ;" + "" +
                "end;" +
                "" +
                "partition with (user of outputStream1) " +
                "begin " +
                "   @info(name = 'query2') " +
                "   from outputStream1#window.length(1) join twitterStream#window.length(1) " +
                "   select outputStream1.symbol as symbol, twitterStream.tweet, outputStream1.volume " +
                "   insert all events into outputStream ;" + "" +
                "end;" +
                " ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
        siddhiAppRuntime.start();

        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"WSO2", "User1", 100});

        cseEventStreamHandler.send(new Object[]{"WSO2", "User2", 100});
        twitterStreamHandler.send(new Object[]{"User2", "Hello World", "WSO2"});

        twitterStreamHandler.send(new Object[]{"User3", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"WSO2", "User3", 100});

        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();


    }
}
