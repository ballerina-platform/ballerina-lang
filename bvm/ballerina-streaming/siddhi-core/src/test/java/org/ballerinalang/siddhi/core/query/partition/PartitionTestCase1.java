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
import org.ballerinalang.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.ballerinalang.siddhi.query.api.SiddhiApp;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.execution.partition.Partition;
import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testcase for partition queries.
 */
public class PartitionTestCase1 {
    private static final Logger log = LoggerFactory.getLogger(PartitionTestCase1.class);
    private AtomicInteger count = new AtomicInteger(0);
    private int stockStreamEventCount;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
        stockStreamEventCount = 0;
    }

    @Test
    public void testPartitionQuery() throws InterruptedException {
        log.info("Partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest') " +
                "define stream streamA (symbol string, price int);" +
                "partition with (symbol of streamA) " +
                "begin " +
                "@info(name = 'query1') " +
                "from streamA select symbol,price insert into StockQuote ;  " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700});
        inputHandler.send(new Object[]{"WSO2", 60});
        inputHandler.send(new Object[]{"WSO2", 60});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery1() throws InterruptedException {
        log.info("Partition test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest1') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStreamOne (symbol string, price float,volume int);"
                + "@info(name = 'query')from cseEventStreamOne select symbol,price,volume insert into cseEventStream;"
                + "partition with (symbol of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>price] select symbol,sum(price) as price,volume insert into OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    eventArrived = true;
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(75.5999984741211, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(151.1999969482422, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(75.5999984741211, event.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStreamOne");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 70005.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testPartitionQuery2() throws InterruptedException {
        log.info("Partition test2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest2') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream StockStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream , symbol of StockStream1) begin @info(name = 'query1') " +
                "from cseEventStream[700>price] select symbol,sum(price) as price,volume insert into OutStockStream ;" +
                "  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery3() throws InterruptedException {
        log.info("Partition test3");


        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiApp siddhiApp = new SiddhiApp("plan3");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        siddhiApp.defineStream(streamDefinition);

        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));

        Query query = Query.query();
        query.from(InputStream.stream("cseEventStream"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query.insertIntoInner("StockStream");


        Query query1 = Query.query();
        query1.from(InputStream.innerStream("StockStream"));
        query1.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query1.insertInto("OutStockStream");

        partition.addQuery(query);
        partition.addQuery(query1);

        siddhiApp.addPartition(partition);


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(4, count.get());

    }


    @Test
    public void testPartitionQuery4() throws InterruptedException {
        log.info("Partition test4");
        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiApp siddhiApp = new SiddhiApp("plan4");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        StreamDefinition streamDefinition1 = StreamDefinition.id("cseEventStream1").attribute("symbol", Attribute
                .Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        siddhiApp.defineStream(streamDefinition);
        siddhiApp.defineStream(streamDefinition1);


        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));

        Query query = Query.query();
        query.from(InputStream.stream("cseEventStream"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query.insertIntoInner("StockStream");


        Query query1 = Query.query();
        query1.from(InputStream.innerStream("StockStream"));
        query1.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query1.insertInto("OutStockStream");


        ////////////////////////////////////partition-2/////////////////


        Partition partition1 = Partition.partition().
                with("cseEventStream1", Expression.variable("symbol"));

        Query query2 = Query.query();
        query2.from(InputStream.stream("cseEventStream1"));
        query2.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query2.insertIntoInner("StockStream");


        Query query3 = Query.query();
        query3.from(InputStream.innerStream("StockStream"));
        query3.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query3.insertInto("OutStockStream");


        partition.addQuery(query);
        partition.addQuery(query1);

        partition1.addQuery(query2);
        partition1.addQuery(query3);

        siddhiApp.addPartition(partition);

        siddhiApp.addPartition(partition1);

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler inputHandler2 = siddhiAppRuntime.getInputHandler("cseEventStream1");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});

        inputHandler2.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler2.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler2.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler2.send(new Object[]{"ORACLE", 75.6f, 100});

        SiddhiTestHelper.waitForEvents(100, 8, count, 60000);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(8, count.get());

    }

    @Test
    public void testPartitionQuery5() throws InterruptedException {
        log.info("Partition test5");
        final SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiApp siddhiApp = new SiddhiApp("plan5");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        StreamDefinition streamDefinition1 = StreamDefinition.id("cseEventStream1").attribute("symbol", Attribute
                .Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        StreamDefinition streamDefinition2 = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);


        siddhiApp.defineStream(streamDefinition);
        siddhiApp.defineStream(streamDefinition1);
        siddhiApp.defineStream(streamDefinition2);


        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));

        Query query = Query.query();
        query.from(InputStream.stream("cseEventStream")).annotation(Annotation.annotation("info").element("name",
                "query"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query.insertIntoInner("StockStream");

        Query query1 = Query.query();
        query1.from(InputStream.innerStream("StockStream")).annotation(Annotation.annotation("info").element("name",
                "query1"));
        query1.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query1.insertInto("OutStockStream");


        ////////////////////////////////////partition-2/////////////////


        Partition partition1 = Partition.partition().
                with("cseEventStream1", Expression.variable("symbol"));

        Query query2 = Query.query();
        query2.from(InputStream.stream("cseEventStream1")).annotation(Annotation.annotation("info").element("name",
                "query2"));
        query2.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query2.insertIntoInner("StockStream");


        Query query3 = Query.query();
        query3.from(InputStream.innerStream("StockStream")).annotation(Annotation.annotation("info").element("name",
                "query3"));
        query3.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query3.insertInto("OutStockStream");


        Query query4 = Query.query();
        query4.from(InputStream.stream("StockStream")).annotation(Annotation.annotation("info").element("name",
                "query4"));
        query4.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query4.insertInto("OutStockStream");


        Query query5 = Query.query();
        query5.from(InputStream.innerStream("StockStream")).annotation(Annotation.annotation("info").element("name",
                "query5"));
        query5.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query5.insertInto("StockStream");

        partition.addQuery(query);
        partition.addQuery(query1);
        partition.addQuery(query5);

        partition1.addQuery(query2);
        partition1.addQuery(query3);

        siddhiApp.addPartition(partition);

        siddhiApp.addPartition(partition1);

        siddhiApp.addQuery(query4);

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("StockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                stockStreamEventCount = stockStreamEventCount + events.length;

            }
        });


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                synchronized (siddhiManager) {
                    count.addAndGet(events.length);
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler inputHandler2 = siddhiAppRuntime.getInputHandler("cseEventStream1");
        InputHandler inputHandler3 = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});

        inputHandler2.send(new Object[]{"IBM1", 75.6f, 100});
        inputHandler2.send(new Object[]{"WSO21", 75.6f, 100});
        inputHandler2.send(new Object[]{"IBM1", 75.6f, 100});
        inputHandler2.send(new Object[]{"ORACLE1", 75.6f, 100});

        inputHandler3.send(new Object[]{"ABC", 75.6f, 100});
        inputHandler3.send(new Object[]{"DEF", 75.6f, 100});
        inputHandler3.send(new Object[]{"KLM", 75.6f, 100});
        inputHandler3.send(new Object[]{"ABC", 75.6f, 100});

        SiddhiTestHelper.waitForEvents(100, 16, count, 60000);
        AssertJUnit.assertEquals(16, count.get());
        Thread.sleep(100);
        AssertJUnit.assertEquals(8, stockStreamEventCount);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testPartitionQuery6() throws InterruptedException {
        log.info("Partition test6");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest6') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream , symbol of cseEventStream1) begin @info(name = 'query') " +
                "from cseEventStream select symbol,price,volume insert into #StockStream ;"
                + "@info(name = 'query1') from #StockStream select symbol,price,volume insert into OutStockStream ;"
                + "@info(name = 'query2') from cseEventStream1 select symbol,price,volume insert into  #StockStream1 ;"
                + "@info(name = 'query3') from #StockStream1 select symbol,price,volume insert into OutStockStream ; " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler inputHandler2 = siddhiAppRuntime.getInputHandler("cseEventStream1");

        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});

        inputHandler2.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler2.send(new Object[]{"WSO21", 75.6f, 100});
        inputHandler2.send(new Object[]{"IBM1", 75.6f, 100});
        inputHandler2.send(new Object[]{"ORACLE1", 75.6f, 100});

        SiddhiTestHelper.waitForEvents(100, 8, count, 60000);
        AssertJUnit.assertEquals(8, count.get());

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery7() throws InterruptedException {
        log.info("Partition test7");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest7') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream)"
                + "begin"
                + "@info(name = 'query') from cseEventStream select symbol,sum(price) as price,volume insert into " +
                "OutStockStream ;"
                + "end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(75.0, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(705.0, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(110.0, event.getData()[1]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertEquals(50.0, event.getData()[1]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery8() throws InterruptedException {
        log.info("Partition test8");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest8') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream)"
                + "begin"
                + "@info(name = 'query') from cseEventStream select symbol,max(price) as max_price,volume insert into" +
                " OutStockStream ;"
                + "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(75.0f, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(705.0f, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(75.0f, event.getData()[1]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertEquals(50.0f, event.getData()[1]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery9() throws InterruptedException {
        log.info("Partition test9");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest9') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream)"
                + "begin"
                + "@info(name = 'query') from cseEventStream select symbol,min(price) as min_price,volume insert into" +
                " OutStockStream ;"
                + "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(75.0f, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(705.0f, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(35.0f, event.getData()[1]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertEquals(50.0f, event.getData()[1]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery10() throws InterruptedException {
        log.info("Partition test10");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest10') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream)"
                + "begin"
                + "@info(name = 'query') from cseEventStream select symbol,avg(price) as avgPrice,volume insert into " +
                "OutStockStream ;"
                + "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(75.0, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(705.0, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(55.0, event.getData()[1]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertEquals(50.0, event.getData()[1]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        SiddhiTestHelper.waitForEvents(200, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery11() throws InterruptedException {
        log.info("Partition test11");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest11') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream)"
                + "begin"
                + "@info(name = 'query') from cseEventStream select count(symbol) as entries insert into " +
                "OutStockStream ;"
                + "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(1L, event.getData()[0]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(1L, event.getData()[0]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(2L, event.getData()[0]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertEquals(1L, event.getData()[0]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        SiddhiTestHelper.waitForEvents(200, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testPartitionQuery15() throws InterruptedException {
        log.info("Partition test15");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest15') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "define stream StockStream (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream) begin @info(name = 'query') from cseEventStream select " +
                "symbol,price as price,volume insert into #StockStream ;"
                + "@info(name = 'query1') from #StockStream select symbol,price,volume insert into OutStockStream ;"
                + "@info(name = 'query2') from #StockStream select symbol,price,volume insert into StockStream ; end ;"
                + "partition with (symbol of cseEventStream1) begin @info(name = 'query3') from cseEventStream1 " +
                "select symbol,price+5 as price,volume insert into #StockStream ;"
                + "@info(name = 'query4') from #StockStream select symbol,price,volume insert into OutStockStream ; " +
                "end ;"
                + "@info(name = 'query5') from StockStream select symbol,price+15  as price,volume group by symbol " +
                "insert into OutStockStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("StockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                stockStreamEventCount = stockStreamEventCount + events.length;

            }
        });

        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        InputHandler inputHandler2 = siddhiAppRuntime.getInputHandler("cseEventStream1");
        InputHandler inputHandler3 = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});

        inputHandler2.send(new Object[]{"IBM1", 75.6f, 100});
        inputHandler2.send(new Object[]{"WSO21", 75.6f, 100});
        inputHandler2.send(new Object[]{"IBM1", 75.6f, 100});
        inputHandler2.send(new Object[]{"ORACLE1", 75.6f, 100});

        inputHandler3.send(new Object[]{"ABC", 75.6d, 100});
        inputHandler3.send(new Object[]{"DEF", 75.6d, 100});
        inputHandler3.send(new Object[]{"KLM", 75.6d, 100});
        inputHandler3.send(new Object[]{"ABC", 75.6d, 100});

        SiddhiTestHelper.waitForEvents(100, 16, count, 60000);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(16, count.get());
        AssertJUnit.assertEquals(8, stockStreamEventCount);
    }

    @Test
    public void testPartitionQuery16() throws InterruptedException {
        log.info("partition test16");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest16') " +
                "define stream streamA (symbol string, price int);"
                + "partition with (symbol of streamA) begin @info(name = 'query1') from streamA select symbol,price " +
                "insert into StockQuote ;"
                + "@info(name = 'query2') from streamA select symbol,price insert into StockQuote ; end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700});
        inputHandler.send(new Object[]{"WSO2", 60});
        inputHandler.send(new Object[]{"WSO2", 60});
        SiddhiTestHelper.waitForEvents(100, 6, count, 60000);
        AssertJUnit.assertEquals(6, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testPartitionQuery17() throws InterruptedException {
        log.info("Partition test17");


        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiApp siddhiApp = new SiddhiApp("plan17");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        siddhiApp.defineStream(streamDefinition);

        Partition partition = Partition.partition().
                with("cseEventStream",
                        Partition.range("LessValue",
                                Expression.compare(
                                        Expression.value(200),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.variable("volume"))
                        ),
                        Partition.range("HighValue",
                                Expression.compare(
                                        Expression.value(200),
                                        Compare.Operator.LESS_THAN_EQUAL,
                                        Expression.variable("volume"))
                        ));

        Query query = Query.query();
        query.from(InputStream.stream("cseEventStream"));

        query.select(
                Selector.selector().
                        select("sumvolume", Expression.function("sum", Expression.variable("volume")))

        );
        query.insertInto("StockStream");


        partition.addQuery(query);


        siddhiApp.addPartition(partition);


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("StockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(100L, event.getData()[0]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(600L, event.getData()[0]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(200L, event.getData()[0]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertEquals(250L, event.getData()[0]);
                    }
                    eventArrived = true;
                }
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 600});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 50});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();

    }


    @Test
    public void testPartitionQuery18() throws InterruptedException {
        log.info("Partition test18");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest18') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStreamOne (symbol string, price float,volume int);"
                + "@info(name = 'query')from cseEventStreamOne select symbol,price,volume insert into cseEventStream;"
                + "partition with (price>=100 as 'large' or price<100 as 'small' of cseEventStream) begin @info(name " +
                "= 'query1') from cseEventStream#window.length(4) select symbol,sum(price) as price insert into " +
                "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    eventArrived = true;
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(25.0, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(7005.60009765625, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertTrue(event.getData()[1].equals(75.0) || event.getData()[1].equals(100.0));
                    } else if (count.get() == 4) {
                        AssertJUnit.assertEquals(100.0, event.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStreamOne");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 25f, 100});
        inputHandler.send(new Object[]{"WSO2", 7005.6f, 100});
        inputHandler.send(new Object[]{"IBM", 50f, 100});
        inputHandler.send(new Object[]{"ORACLE", 25f, 100});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertTrue(count.get() <= 4);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testPartitionQuery19() throws InterruptedException {
        log.info("Partition test19");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest19') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStreamOne (symbol string, price float,volume int);"
                + "@info(name = 'query')from cseEventStreamOne select symbol,price,volume insert into cseEventStream;"
                + "partition with (price>=100 as 'large' or price<100 as 'medium' or price<50 as 'small' of " +
                "cseEventStream) begin @info(name = 'query1') from cseEventStream select symbol,sum(price) as price " +
                "insert into OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    eventArrived = true;
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(25.0, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(25.0, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(7005.60009765625, event.getData()[1]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertTrue(event.getData()[1].equals(75.0) || event.getData()[1].equals(100.0));
                    } else if (count.get() == 5) {
                        AssertJUnit.assertTrue(event.getData()[1].equals(50.0) || event.getData()[1].equals(100.0));
                    } else if (count.get() == 6) {
                        AssertJUnit.assertEquals(50.0, event.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStreamOne");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 25f, 100});
        inputHandler.send(new Object[]{"WSO2", 7005.6f, 100});
        inputHandler.send(new Object[]{"IBM", 50f, 100});
        inputHandler.send(new Object[]{"ORACLE", 25f, 100});
        SiddhiTestHelper.waitForEvents(100, 5, count, 60000);
        AssertJUnit.assertTrue(6 >= count.get());
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testPartitionQuery20() throws InterruptedException {
        log.info("Partition test20");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('PartitionTest20') " +
                "" +
                "" +
                "define stream cseEventStream (symbol string, price float,volume int); " +
                "" +
                "define stream cseEventStreamOne (symbol string, price float,volume int);" +
                "" +
                "@info(name = 'query')" +
                "from cseEventStreamOne " +
                "select symbol, price, volume " +
                "insert into cseEventStream;" +
                " " +
                "partition with (price>=100 as 'large' or price<100 as 'medium' or price<50 as 'small' of " +
                "cseEventStream) " +
                "   begin" +
                "   @info(name = 'query1') " +
                "   from cseEventStream " +
                "   select symbol, sum(price) as price " +
                "   insert into #OutStockStream1 ; " +
                " " +
                "   @info(name = 'query2') " +
                "   from #OutStockStream1 " +
                "   insert into #OutStockStream2 ;" +
                " " +
                "   @info(name = 'query3') " +
                "   from #OutStockStream2 " +
                "   insert into OutStockStream ;" +
                " " +
                "   end ; ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    eventArrived = true;
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(25.0, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(25.0, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(7005.60009765625, event.getData()[1]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertTrue(event.getData()[1].equals(75.0) || event.getData()[1].equals(100.0));
                    } else if (count.get() == 5) {
                        AssertJUnit.assertTrue(event.getData()[1].equals(50.0) || event.getData()[1].equals(100.0));
                    } else if (count.get() == 6) {
                        AssertJUnit.assertEquals(50.0, event.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStreamOne");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 25f, 100});
        inputHandler.send(new Object[]{"WSO2", 7005.6f, 100});
        inputHandler.send(new Object[]{"IBM", 50f, 100});
        inputHandler.send(new Object[]{"ORACLE", 25f, 100});
        SiddhiTestHelper.waitForEvents(100, 5, count, 60000);
        AssertJUnit.assertTrue(6 >= count.get());
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testPartitionQuery21() throws InterruptedException {
        log.info("Partition test21");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('PartitionTest20') " +
                "" +
                "" +
                "define stream cseEventStream (symbol string, price float,volume int); " +
                "" +
                "define stream cseEventStreamOne (symbol string, price float,volume int);" +
                "" +
                "@info(name = 'query')" +
                "from cseEventStreamOne " +
                "select symbol, price, volume " +
                "insert into cseEventStream;" +
                " " +
                "partition with (price>=100 as 'large' or price<100 as 'medium' or price<50 as 'small' of " +
                "cseEventStream) " +
                "   begin" +
                "   @info(name = 'query1') " +
                "   from cseEventStream " +
                "   select symbol, sum(price) as price " +
                "   insert into #OutStockStream1 ; " +
                " " +
                "   @info(name = 'query2') " +
                "   from #OutStockStream1 " +
                "   insert into #OutStockStream2 ;" +
                " " +
                "   @info(name = 'query3') " +
                "   from #OutStockStream2 " +
                "   insert into OutStockStream ;" +
                " " +
                "   end ; ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    eventArrived = true;
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(25.0, event.getData()[1]);
                    } else if (count.get() == 2) {
                        AssertJUnit.assertEquals(25.0, event.getData()[1]);
                    } else if (count.get() == 3) {
                        AssertJUnit.assertEquals(7005.60009765625, event.getData()[1]);
                    } else if (count.get() == 4) {
                        AssertJUnit.assertTrue(event.getData()[1].equals(50.0) || event.getData()[1].equals(100.0));
                    } else if (count.get() == 5) {
                        AssertJUnit.assertTrue(event.getData()[1].equals(50.0) || event.getData()[1].equals(100.0));
                    } else if (count.get() == 6) {
                        AssertJUnit.assertEquals(50.0, event.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStreamOne");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 25f, 100});
        inputHandler.send(new Object[]{"WSO2", 7005.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 25f, 100});
        SiddhiTestHelper.waitForEvents(100, 5, count, 60000);
        AssertJUnit.assertTrue(5 == count.get());
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testPartitionQuery22() throws InterruptedException {
        log.info("Partition test22");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest10') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream)"
                + "begin"
                + "@info(name = 'query') from cseEventStream#window.time(1 sec) " +
                "select symbol, avg(price) as avgPrice, volume " +
                "having avgPrice > 10" +
                "insert expired events into OutStockStream ;"
                + "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(75.0, event.getData()[1]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        SiddhiTestHelper.waitForEvents(200, 1, count, 60000);
        AssertJUnit.assertTrue(1 <= count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery23() throws InterruptedException {
        log.info("Partition test23");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest10') " +
                "define stream cseEventStream (symbol string, price float,volume int);"
                + "define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream)"
                + "begin"
                + "@info(name = 'query') from cseEventStream#window.time(1 sec) " +
                "select symbol, avg(price) as avgPrice, volume " +
                "having avgPrice >= 0 or avgPrice is null " +
                "insert expired events into OutStockStream ;"
                + "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(null, event.getData()[1]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        SiddhiTestHelper.waitForEvents(200, 1, count, 60000);
        AssertJUnit.assertEquals(1, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery24() throws InterruptedException {
        log.info("Partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest') " +
                "define stream streamA (symbol string,  price int);" +
                "partition with (symbol of streamA) " +
                "begin " +
                "@info(name = 'query1') " +
                "from streamA  " +
                "select symbol, price insert into StockQuote ;  " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        Event[] events = {
                new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}),
                new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}),
                new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60})
        };
        inputHandler.send(events);
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery25() throws InterruptedException {
        log.info("Partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest') " +
                "define stream streamA (symbol string,  price int);" +
                "" +
                "from streamA#window.lengthBatch(3) " +
                "insert into streamB;" +
                "" +
                "partition with (symbol of streamB) " +
                "begin " +
                "@info(name = 'query1') " +
                "from streamB  " +
                "select symbol, price insert into StockQuote ;  " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery26() throws InterruptedException {
        log.info("Partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest') " +
                "define stream streamA (symbol string,  price int);" +
                "partition with (symbol of streamA) " +
                "begin " +
                "@info(name = 'query1') " +
                "from streamA  " +
                "select symbol, price insert into StockQuote ;  " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery27() throws InterruptedException {
        log.info("Partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('PartitionTest')" +
                "@async(buffer.size='2') " +
                "define stream streamA (symbol string,  price int); " +
                "@async(buffer.size='2') " +
                "define stream streamB (symbol string,  price int); " +
                "partition with (symbol of streamA,  symbol of streamB) " +
                "begin " +
                "@info(name = 'query1') " +
                "from streamA  " +
                "select symbol, price insert into StockQuote ;  " +
                "@info(name = 'query2') " +
                "from streamB  " +
                "select symbol, price insert into StockQuote ;  " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandlerA = siddhiAppRuntime.getInputHandler("streamA");
        InputHandler inputHandlerB = siddhiAppRuntime.getInputHandler("streamB");

        siddhiAppRuntime.start();
        Thread.sleep(500);
        inputHandlerA.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 701}));
        inputHandlerA.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 61}));
        inputHandlerA.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 62}));
        inputHandlerB.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 702}));
        inputHandlerB.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 63}));
        inputHandlerB.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 64}));
        SiddhiTestHelper.waitForEvents(500, 6, count, 60000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(6, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery28() throws InterruptedException {
        log.info("Partition test24");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest24') " +
                "define stream cseEventStream (symbol string,  price float, volume int, threshold double);"
                + "partition with (symbol of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>price AND threshold != volume] select symbol, sum(price) as price, " +
                "volume , threshold" + " insert into " + "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 100.0});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, 52.0});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 50.0});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100, 200.0});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery29() throws InterruptedException {
        log.info("Partition test25");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest25') " +
                "define stream cseEventStream (symbol string,  price float, volume int, threshold double, " +
                "maxPrice long);"
                + "partition with (symbol of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>price AND threshold != volume AND price != threshold AND maxPrice " +
                "!= threshold" +
                "] select " +
                "symbol, sum(price) as price, " +
                "volume , threshold" + " insert into " + "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 100.0, 500L});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, 52.0, 120L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 50.0, 800L});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100, 200.0, 400L});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery30() throws InterruptedException {
        log.info("Partition test26");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest26') " +
                "define stream cseEventStream (atr1 string,  atr2 float, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);"
                + "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>atr5 AND atr5 != atr6 AND atr2 != atr3 AND atr6 " +
                "!= atr3 AND atr2 != atr5 AND atr4 != atr6 AND atr3 != atr4 AND atr4 != atr7 AND atr8 " +
                "!= atr2 AND atr4 != atr8 AND atr6 != atr8 AND atr3 != atr2 AND atr3 != atr5 AND atr9 " +
                "!= atr10 AND atr3 != atr11] select " +
                "atr1 as symbol, sum(atr2) as price" +
                " insert into " + "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, true, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery31() throws InterruptedException {
        log.info("Partition test30");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest30') " +
                "define stream cseEventStream (atr1 string,  atr2 float, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);"
                + "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>atr5 OR atr4 == atr7 OR atr6 == atr8 OR atr3 " +
                "== atr7 OR atr5 == atr6 OR atr7 == atr3 OR atr8 == atr7 OR atr11 == atr6 OR atr8" +
                "== atr6 OR atr7 == atr5 OR atr6 == atr7 OR atr3 == atr2 OR atr2 == atr8 OR atr4 " +
                "== atr8 OR atr6 == atr3 OR atr2 == atr3 OR atr3 == atr11 OR atr10 == atr9] select" +
                " atr1 as symbol, sum(atr2) as price" + " insert into " + "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, true, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery32() throws InterruptedException {
        log.info("Partition test31");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest31') " +
                "define stream cseEventStream (atr1 string,  atr2 float, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);"
                + "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>atr5 OR atr8< atr3 OR atr6 < atr2 OR atr4 " +
                "< atr6 OR atr2 < atr7 OR atr3 < atr4 OR atr4 < atr3 OR atr6 < atr3 OR atr3" +
                "< atr2 OR atr8 < atr5 OR atr6 < atr4 OR atr3 < atr6 OR atr7 < atr8 OR atr7 " +
                "< atr4 OR atr6 < atr5 OR atr11 < atr3 OR atr8 < atr2] select" +
                " atr1 as symbol, sum(atr2) as price" + " insert into " + "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, true, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery33() throws InterruptedException {
        log.info("Partition test32");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest32') " +
                "define stream cseEventStream (atr1 string,  atr2 float, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);"
                + "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>atr5 OR atr8 <= atr3 OR atr6 <= atr2 OR atr4 " +
                "<= atr6 OR atr2 <= atr7 OR atr3 <= atr4 OR atr4 <= atr3 OR atr6 <= atr3 OR atr3" +
                "<= atr2 OR atr8 <= atr5 OR atr6 <= atr4 OR atr3 <= atr6 OR atr7 <= atr8 OR atr7 " +
                "<= atr4 OR atr6 <= atr5 OR atr11 <= atr3 OR atr8 <= atr2] select" +
                " atr1 as symbol, sum(atr2) as price" + " insert into " + "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, true, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery34() throws InterruptedException {
        log.info("Partition test33");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest33') " +
                "define stream cseEventStream (atr1 string,  atr2 float, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);"
                + "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>atr5 OR atr8 >= atr3 OR atr6 >= atr2 OR atr4 " +
                ">= atr6 OR atr2 >= atr7 OR atr3 >= atr4 OR atr4 >= atr3 OR atr6 >= atr3 OR atr3" +
                ">= atr2 OR atr8 >= atr5 OR atr6 >= atr4 OR atr3 >= atr6 OR atr7 >= atr8 OR atr7 " +
                ">= atr4 OR atr6 >= atr5 OR atr11 >= atr3 OR atr8 >= atr2] select" +
                " atr1 as symbol, sum(atr2) as price" + " insert into " + "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, true, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery35() throws InterruptedException {
        log.info("Partition test34");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest34') " +
                "define stream cseEventStream (atr1 string,  atr2 float, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);"
                + "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[700>atr5 OR atr8 > atr3 OR atr6 > atr2 OR atr4 " +
                "> atr6 OR atr2 > atr7 OR atr3 > atr4 OR atr4 > atr3 OR atr6 > atr3 OR atr3" +
                "> atr2 OR atr8 > atr5 OR atr6 > atr4 OR atr3 > atr6 OR atr7 > atr8 OR atr7 " +
                "> atr4 OR atr6 > atr5 OR atr11 > atr3 OR atr8 > atr2] select" +
                " atr1 as symbol, sum(atr2) as price" + " insert into " + "OutStockStream ;  end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, true, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery36() throws InterruptedException {
        log.info("Partition test35");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest35') " +
                "define stream cseEventStream (atr1 string,  atr2 object, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream select atr5 as threshold,  atr1 as symbol, cast(atr2,  'double') as " +
                "priceInDouble,  sum(atr7) as summedValue insert into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", 75.6, 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", 75.6, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", 75.6, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, true, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery37() throws InterruptedException {
        log.info("Partition test36");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest36') " +
                "define stream cseEventStream (atr1 string,  atr2 object, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 AND atr10] select atr5 as threshold,  atr1 as symbol, cast" +
                "(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert into " +
                "OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 75.6, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", 75.6, 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", 75.6, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", 75.6, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery38() throws InterruptedException {
        log.info("Partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('PartitionTest') " +
                "define stream streamA (symbol string,  price int); " +
                "define stream streamB (symbol string,  price int); " +
                "partition with (symbol of streamA,  symbol of streamB) " +
                "begin " +
                "@info(name = 'query1') " +
                "from streamA " +
                "select symbol, price " +
                "insert into StockQuote ;  " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                //AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        SiddhiAppRuntime siddhiAppRuntime2 = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback2 = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime2.addCallback("StockQuote", streamCallback2);

        InputHandler inputHandlerA = siddhiAppRuntime.getInputHandler("streamA");
        InputHandler inputHandlerB = siddhiAppRuntime2.getInputHandler("streamA");

        siddhiAppRuntime.start();
        inputHandlerA.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        inputHandlerA.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        inputHandlerA.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        byte[] snapshot = siddhiAppRuntime.snapshot();
        siddhiAppRuntime.shutdown();
        Thread.sleep(1000);
        try {
            siddhiAppRuntime2.restore(snapshot);
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }
        siddhiAppRuntime2.start();
        inputHandlerB.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        inputHandlerB.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        inputHandlerB.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        AssertJUnit.assertEquals(6, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery39() throws InterruptedException {
        log.info("Partition test");
        SiddhiApp siddhiApp = SiddhiApp.siddhiApp("Test")
                .defineStream(
                        StreamDefinition.id("streamA")
                                .attribute("symbol", Attribute.Type.STRING)
                                .attribute("price", Attribute.Type.INT)
                );
        Query query = Query.query();
        query.from(
                InputStream.stream("streamA")
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")));
        query.insertInto("StockQuote");
        Partition partition = Partition.partition()
                .annotation(Annotation.annotation("info").element("name", "partitionA"))
                .with("streamA", Expression.variable("symbol")).addQuery(query);
        siddhiApp.addPartition(partition);

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        Thread.sleep(1000);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery40() throws InterruptedException {
        log.info("Partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('PartitionTest') " +
                "@app:statistics('true') " +
                "define stream streamA (symbol string,  price int);" +
                "@info(name = 'partitionB')" +
                "partition with (symbol of streamA) " +
                "begin " +
                "@info(name = 'query1') " +
                "from streamA  " +
                "select symbol, price insert into StockQuote ;  " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPartitionQuery41() throws InterruptedException {
        log.info("Partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('PartitionTest') " +
                "define stream streamA (symbol string,  price int);" +
                "" +
                "from streamA#window.lengthBatch(3) " +
                "insert into streamB;" +
                "" +
                "partition with (symbol of streamB) " +
                "begin " +
                "@info(name = 'query1') " +
                "from streamB  " +
                "select symbol, price insert into StockQuote ;  " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testPartitionQuery42() throws InterruptedException {
        log.info("Partition test");
        SiddhiApp siddhiApp = SiddhiApp.siddhiApp("Test")
                .defineStream(
                        StreamDefinition.id("streamA")
                                .attribute("symbol", Attribute.Type.STRING)
                                .attribute("price", Attribute.Type.INT)
                );
        Query query = Query.query();
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.from(
                InputStream.stream("streamA")
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")));
        query.insertInto("StockQuote");
        Partition partition = Partition.partition();
        partition.addQuery(query);
        siddhiApp.addPartition(partition);

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                AssertJUnit.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count.addAndGet(events.length);
                eventArrived = true;
            }
        };
        siddhiAppRuntime.addCallback("StockQuote", streamCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("streamA");
        siddhiAppRuntime.start();
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"IBM", 700}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        inputHandler.send(new Event(System.currentTimeMillis(), new Object[]{"WSO2", 60}));
        Thread.sleep(1000);
        AssertJUnit.assertEquals(0, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testIsNullStreamConditionCase() throws InterruptedException {
        log.info("Partition testIsNullStreamConditionCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testIsNullStreamConditionCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 AND atr2 is null ] select atr5 as threshold,  atr1 as " +
                "symbol, " +
                "cast" +
                "(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert into " +
                "OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 200L, 102.0, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 201L, 103.0, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 104.0, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 202L, 104.0, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testDivideExpressionExecutorDoubleCase() throws InterruptedException {
        log.info("Partition testDivideExpressionExecutorDoubleCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testDivideExpressionExecutorDoubleCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr4/atr7 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(8.836395450568679, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(6.640368178829717, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(2.255140393544108, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(1.156400274788184, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 200L, 11.43, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 201L, 15.21, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 202L, 87.34, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testDivideExpressionExecutorFloatCase() throws InterruptedException {
        log.info("Partition testDivideExpressionExecutorFloatCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testDivideExpressionExecutorFloatCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 float, " +
                "atr5 long,  atr6 long,  atr7 float,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr4/atr7 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(8.836395f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(6.640368f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(2.2551403f, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(1.1564003f, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0f, 500L, 200L, 11.43f, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0f, 501L, 201L, 15.21f, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0f, 502L, 202L, 45.23f, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0f, 502L, 202L, 87.34f, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testDivideExpressionExecutorLongCase() throws InterruptedException {
        log.info("Partition testDivideExpressionExecutorLongCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testDivideExpressionExecutorLongCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr5/atr6 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(25L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(2L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(2L, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(2L, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 20L, 11.43, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 206L, 15.21, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 209L, 87.34, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testDivideExpressionExecutorIntCase() throws InterruptedException {
        log.info("Partition testDivideExpressionExecutorIntCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testDivideExpressionExecutorIntCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr3/atr11 as dividedVal, atr5 as threshold,  atr1 " +
                "as symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert" +
                " into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(4, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(1, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(8, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(2, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 20L, 11.43, 75.7f, false, true, 23});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 206L, 15.21, 76.7f, false, true, 65});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 12});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 209L, 87.34, 77.7f, false, false, 34});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testModExpressionExecutorDoubleCase() throws InterruptedException {
        log.info("Partition testModExpressionExecutorDoubleCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@app:name('testModExpressionExecutorDoubleCase') " +
                "define stream cseEventStream (atr1 string,  atr2 string, atr3 int, atr4 double, " +
                "atr5 long,  atr6 long,  atr7 double,  atr8 float , atr9 bool, atr10 bool,  atr11 int);" +
                "partition with (atr1 of cseEventStream) begin @info(name = 'query1') from " +
                "cseEventStream[atr5 < 700 ] select atr4%atr7 as dividedVal, atr5 as threshold,  atr1 as" +
                " symbol, " + "cast(atr2,  'double') as priceInDouble,  sum(atr7) as summedValue insert " +
                "into OutStockStream ; end ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        siddhiAppRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(9.560000000000002, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(9.739999999999995, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(11.540000000000006, event.getData(0));
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        AssertJUnit.assertEquals(13.659999999999997, event.getData(0));
                        eventArrived = true;
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", null, 100, 101.0, 500L, 200L, 11.43, 75.7f, false, true, 105});
        inputHandler.send(new Object[]{"WSO2", "aa", 100, 101.0, 501L, 201L, 15.21, 76.7f, false, true, 106});
        inputHandler.send(new Object[]{"IBM", null, 100, 102.0, 502L, 202L, 45.23, 77.7f, false, true, 107});
        inputHandler.send(new Object[]{"ORACLE", null, 100, 101.0, 502L, 202L, 87.34, 77.7f, false, false, 108});
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        siddhiAppRuntime.shutdown();
    }
}
