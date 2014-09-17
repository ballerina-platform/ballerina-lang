/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
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

package org.wso2.siddhi.core.query;

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
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import sun.security.validator.ValidatorException;


public class PartitionTestCase {
    static final Logger log = Logger.getLogger(PartitionTestCase.class);
    private int count;
    private int stockStreamEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
        stockStreamEventCount = 0;
    }

    @Test
    public void PartitionTest() throws InterruptedException {
        log.info("partition test");
        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition streamA = new StreamDefinition("streamA").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("config").element("async", "true"));

        Partition partition = Partition.partition().
                with("streamA", Expression.variable("symbol"));

        Query query = new Query();
        query.from(InputStream.stream("streamA"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price"))
        );
        query.insertInto("StockQuote");

        partition.addQuery(query);

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(streamA);
        executionPlan.addPartition(partition);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count = count+events.length;
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("streamA");
        inputHandler.send(new Object[]{"IBM", 700});
        inputHandler.send(new Object[]{"WSO2", 60});
        inputHandler.send(new Object[]{"WSO2", 60});
        Thread.sleep(1000);
        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
    }

    @Test
    public void testPartitionQuery1() throws InterruptedException {
        log.info("Partition test1");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).
                annotation(Annotation.annotation("config").element("async", "true"));
        ;
        StreamDefinition streamDefinition1 = StreamDefinition.id("1cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).
                annotation(Annotation.annotation("config").element("async", "true"));
        ;
        executionPlan.defineStream(streamDefinition);
        executionPlan.defineStream(streamDefinition1);

        Query query1 = Query.query().annotation(Annotation.annotation("info").element("name", "query1"));
        query1.from(InputStream.stream("1cseEventStream")


        );
        query1.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price",Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query1.insertInto("cseEventStream");


        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));


        Query query = Query.query().annotation(Annotation.annotation("info").element("name", "query1"));
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.value(700),
                        Compare.Operator.GREATER_THAN,
                        Expression.variable("price"))
                )

        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.function("sum",Expression.variable("price"))).
                        select("volume", Expression.variable("volume"))
        );
        query.insertInto("OutStockStream");

        partition.addQuery(query);

        executionPlan.addQuery(query1);
        executionPlan.addPartition(partition);


        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count = count+events.length;
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("1cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 70005.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});
        Thread.sleep(1000);
        Assert.assertEquals(3, count);


    }

    @Test
    public void testPartitionQuery2() throws InterruptedException {
        log.info("Partition test2");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        executionPlan.defineStream(streamDefinition);
        StreamDefinition streamDefinition2 = StreamDefinition.id("StockStream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        executionPlan.defineStream(streamDefinition2);

        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol")).
                with("StockStream1", Expression.variable("symbol"));

        Query query = Query.query();
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.value(700),
                        Compare.Operator.GREATER_THAN,
                        Expression.variable("price"))
                )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query.insertInto("OutStockStream");

        partition.addQuery(query);


        executionPlan.addPartition(partition);

        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                count = count+events.length;
                eventArrived = true;
            }
        });
        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});
        Thread.sleep(1000);
        Assert.assertEquals(4, count);

    }

    @Test
    public void testPartitionQuery3() throws InterruptedException {
        log.info("Partition test3");


        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        executionPlan.defineStream(streamDefinition);

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

        executionPlan.addPartition(partition);


        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                count = count+events.length;
                eventArrived = true;
            }
        });
        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});
        Thread.sleep(1000);

        Assert.assertEquals(4, count);


    }


    @Test
    public void testPartitionQuery4() throws InterruptedException {
        log.info("Partition test4");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        StreamDefinition streamDefinition1 = StreamDefinition.id("cseEventStream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        executionPlan.defineStream(streamDefinition);
        executionPlan.defineStream(streamDefinition1);


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

        executionPlan.addPartition(partition);

        executionPlan.addPartition(partition1);

        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                count = count+events.length;
                eventArrived = true;
            }
        });


        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});

        InputHandler inputHandler2 = executionRuntime.getInputHandler("cseEventStream1");
        inputHandler2.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler2.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler2.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler2.send(new Object[]{"ORACLE", 75.6f, 100});

        Thread.sleep(2000);
        Assert.assertEquals(8, count);


    }

    @Test
    public void testPartitionQuery5() throws InterruptedException {
        log.info("Partition test5");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        StreamDefinition streamDefinition1 = StreamDefinition.id("cseEventStream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        StreamDefinition streamDefinition2 = StreamDefinition.id("StockStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);


        executionPlan.defineStream(streamDefinition);
        executionPlan.defineStream(streamDefinition1);

        executionPlan.defineStream(streamDefinition2);


        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));

        Query query = Query.query();
        query.from(InputStream.stream("cseEventStream")).annotation(Annotation.annotation("info").element("name", "query"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query.insertIntoInner("StockStream");

        Query query1 = Query.query();
        query1.from(InputStream.innerStream("StockStream")).annotation(Annotation.annotation("info").element("name", "query1"));
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
        query2.from(InputStream.stream("cseEventStream1")).annotation(Annotation.annotation("info").element("name", "query2"));
        query2.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query2.insertIntoInner("StockStream");


        Query query3 = Query.query();
        query3.from(InputStream.innerStream("StockStream")).annotation(Annotation.annotation("info").element("name", "query3"));
        query3.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query3.insertInto("OutStockStream");


        Query query4 = Query.query();
        query4.from(InputStream.stream("StockStream")).annotation(Annotation.annotation("info").element("name", "query4"));
        query4.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query4.insertInto("OutStockStream");


        Query query5 = Query.query();
        query5.from(InputStream.innerStream("StockStream")).annotation(Annotation.annotation("info").element("name", "query5"));
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

        executionPlan.addPartition(partition);

        executionPlan.addPartition(partition1);

        executionPlan.addQuery(query4);

        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("StockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                stockStreamEventCount = stockStreamEventCount+events.length;

            }
        });


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                count = count+events.length;
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});

        InputHandler inputHandler2 = executionRuntime.getInputHandler("cseEventStream1");
        inputHandler2.send(new Object[]{"IBM1", 75.6f, 100});
        inputHandler2.send(new Object[]{"WSO21", 75.6f, 100});
        inputHandler2.send(new Object[]{"IBM1", 75.6f, 100});
        inputHandler2.send(new Object[]{"ORACLE1", 75.6f, 100});


        InputHandler inputHandler3 = executionRuntime.getInputHandler("StockStream");
        inputHandler3.send(new Object[]{"ABC", 75.6f, 100});
        inputHandler3.send(new Object[]{"DEF", 75.6f, 100});
        inputHandler3.send(new Object[]{"KLM", 75.6f, 100});
        inputHandler3.send(new Object[]{"ABC", 75.6f, 100});

        Thread.sleep(8000);


        Assert.assertEquals(16, count);
        Thread.sleep(1000);
        Assert.assertEquals(8, stockStreamEventCount);

    }

    @Test
    public void testPartitionQuery6() throws InterruptedException {
        log.info("Partition test6");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);
        StreamDefinition streamDefinition1 = StreamDefinition.id("cseEventStream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);


        executionPlan.defineStream(streamDefinition);
        executionPlan.defineStream(streamDefinition1);


        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol")).with("cseEventStream1", Expression.variable("symbol"));

        Query query = Query.query();
        query.from(InputStream.stream("cseEventStream")).annotation(Annotation.annotation("info").element("name", "query"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query.insertIntoInner("StockStream");

        Query query1 = Query.query();
        query1.from(InputStream.innerStream("StockStream")).annotation(Annotation.annotation("info").element("name", "query1"));
        query1.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query1.insertInto("OutStockStream");

        Query query2 = Query.query();
        query2.from(InputStream.stream("cseEventStream1")).annotation(Annotation.annotation("info").element("name", "query2"));
        query2.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query2.insertIntoInner("StockStream1");


        Query query3 = Query.query();
        query3.from(InputStream.innerStream("StockStream1")).annotation(Annotation.annotation("info").element("name", "query3"));
        query3.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))

        );
        query3.insertInto("OutStockStream");

        partition.addQuery(query);
        partition.addQuery(query1);
        partition.addQuery(query2);
        partition.addQuery(query3);

        executionPlan.addPartition(partition);


        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                count = count+events.length;
                eventArrived = true;
            }
        });


        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});

        InputHandler inputHandler2 = executionRuntime.getInputHandler("cseEventStream1");
        inputHandler2.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler2.send(new Object[]{"WSO21", 75.6f, 100});
        inputHandler2.send(new Object[]{"IBM1", 75.6f, 100});
        inputHandler2.send(new Object[]{"ORACLE1", 75.6f, 100});


        Thread.sleep(1000);
        Assert.assertEquals(8, count);

    }

    @Test
    public void testPartitionQuery7() throws InterruptedException, ValidatorException {
        log.info("Partition test7");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).
                annotation(Annotation.annotation("config").element("async", "true"));

        executionPlan.defineStream(streamDefinition);

        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));


        Query query = Query.query().annotation(Annotation.annotation("info").element("name", "query1"));
        query.from(InputStream.stream("cseEventStream"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.function("sum",Expression.variable("price")))
        );
        query.insertInto("OutStockStream");

        partition.addQuery(query);


        executionPlan.addPartition(partition);

        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count = count+events.length;
                if (count == 1) {
                    Assert.assertEquals(75.0, events[0].getData()[1]);
                } else if (count == 2) {
                    Assert.assertEquals(705.0, events[0].getData()[1]);
                } else if (count == 3) {
                    Assert.assertEquals(110.0, events[0].getData()[1]);
                } else if (count == 4) {
                    Assert.assertEquals(50.0,events[0].getData()[1]);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        Thread.sleep(1000);
        Assert.assertEquals(4, count);


    }

    @Test
    public void testPartitionQuery8() throws InterruptedException, ValidatorException {
        log.info("Partition test8");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).
                annotation(Annotation.annotation("config").element("async", "true"));

        executionPlan.defineStream(streamDefinition);

        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));


        Query query = Query.query().annotation(Annotation.annotation("info").element("name", "query1"));
        query.from(InputStream.stream("cseEventStream"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.function("max",Expression.variable("price")))
        );
        query.insertInto("OutStockStream");

        partition.addQuery(query);


        executionPlan.addPartition(partition);

        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count = count+events.length;
                if (count == 1) {
                    Assert.assertEquals(75.0f, events[0].getData()[1]);
                } else if (count == 2) {
                    Assert.assertEquals(705.0f, events[0].getData()[1]);
                } else if (count == 3) {
                    Assert.assertEquals(75.0f, events[0].getData()[1]);
                } else if (count == 4) {
                    Assert.assertEquals(50.0f,events[0].getData()[1]);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        Thread.sleep(1000);
        Assert.assertEquals(4, count);


    }

    @Test
    public void testPartitionQuery9() throws InterruptedException, ValidatorException {
        log.info("Partition test9");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).
                annotation(Annotation.annotation("config").element("async", "true"));

        executionPlan.defineStream(streamDefinition);

        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));


        Query query = Query.query().annotation(Annotation.annotation("info").element("name", "query1"));
        query.from(InputStream.stream("cseEventStream"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.function("min",Expression.variable("price")))
        );
        query.insertInto("OutStockStream");

        partition.addQuery(query);


        executionPlan.addPartition(partition);

        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count = count+events.length;
                if (count == 1) {
                    Assert.assertEquals(75.0f, events[0].getData()[1]);
                } else if (count == 2) {
                    Assert.assertEquals(705.0f, events[0].getData()[1]);
                } else if (count == 3) {
                    Assert.assertEquals(35.0f, events[0].getData()[1]);
                } else if (count == 4) {
                    Assert.assertEquals(50.0f,events[0].getData()[1]);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        Thread.sleep(1000);
        Assert.assertEquals(4, count);


    }

    @Test
    public void testPartitionQuery10() throws InterruptedException, ValidatorException {
        log.info("Partition test10");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).
                annotation(Annotation.annotation("config").element("async", "true"));

        executionPlan.defineStream(streamDefinition);

        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));


        Query query = Query.query().annotation(Annotation.annotation("info").element("name", "query1"));
        query.from(InputStream.stream("cseEventStream"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.function("avg",Expression.variable("price")))
        );
        query.insertInto("OutStockStream");

        partition.addQuery(query);


        executionPlan.addPartition(partition);

        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count = count+events.length;
                if (count == 1) {
                    Assert.assertEquals(75.0, events[0].getData()[1]);
                } else if (count == 2) {
                    Assert.assertEquals(705.0, events[0].getData()[1]);
                } else if (count == 3) {
                    Assert.assertEquals(55.0, events[0].getData()[1]);
                } else if (count == 4) {
                    Assert.assertEquals(50.0,events[0].getData()[1]);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        Thread.sleep(1000);
        Assert.assertEquals(4, count);


    }

    @Test
    public void testPartitionQuery11() throws InterruptedException, ValidatorException {
        log.info("Partition test11");
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan executionPlan = new ExecutionPlan("plan1");

        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).
                annotation(Annotation.annotation("config").element("async", "true"));

        executionPlan.defineStream(streamDefinition);

        Partition partition = Partition.partition().
                with("cseEventStream", Expression.variable("symbol"));


        Query query = Query.query().annotation(Annotation.annotation("info").element("name", "query1"));
        query.from(InputStream.stream("cseEventStream"));
        query.select(
                Selector.selector().
                        select("entries", Expression.function("count",Expression.variable("symbol")))
        );
        query.insertInto("OutStockStream");

        partition.addQuery(query);


        executionPlan.addPartition(partition);

        ExecutionPlanRuntime executionRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count = count+events.length;
                if (count == 1) {
                    Assert.assertEquals(1l, events[0].getData()[0]);
                } else if (count == 2) {
                    Assert.assertEquals(1l, events[0].getData()[0]);
                } else if (count == 3) {
                    Assert.assertEquals(2l, events[0].getData()[0]);
                } else if (count == 4) {
                    Assert.assertEquals(1l,events[0].getData()[0]);
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        Thread.sleep(1000);
        Assert.assertEquals(4, count);


    }
}
