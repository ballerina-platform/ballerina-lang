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
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.ValidatorException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

public class FilterTestCase {
    static final Logger log = Logger.getLogger(FilterTestCase.class);
    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }


    // Test cases for GREATER_THAN operator
    @Test
    public void FilterTest1() throws InterruptedException, ValidatorException {
        log.info("filter test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[70 > price] select symbol,price insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream+query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("WSO2".equals(inEvents[0].getData(0)));
                count++;
                eventArrived = true;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 700f, 100l});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        Assert.assertTrue(eventArrived);
    }

    @Test
    public void FilterTest2() throws InterruptedException, ValidatorException {
        log.info("filter test2");
        SiddhiManager siddhiManager = new SiddhiManager();


        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[150 > volume] select symbol,price insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream+query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)));
                count++;
                eventArrived = true;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 700f, 100l});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        Assert.assertTrue(eventArrived);
    }

    @Test
    public void testFilterQuery3() throws InterruptedException, ValidatorException {
        log.info("Filter test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream[70 > price] select symbol,price insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream+query);


        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("WSO2".equals(inEvents[0].getData(0)));
                count++;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
    }


    @Test
    public void testFilterQuery4() throws InterruptedException, ValidatorException {
        log.info("Filter test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[volume > 50f] select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream+query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
    }

    @Test
    public void testFilterQuery5() throws InterruptedException, ValidatorException {
        log.info("Filter test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[volume > 50l] select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream+query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery6() throws InterruptedException, ValidatorException {
        log.info("Filter test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream[volume > 50l] select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream+query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60});
        inputHandler.send(new Object[]{"WSO2", 70f, 40});
        inputHandler.send(new Object[]{"WSO2", 44f, 200});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery7() throws InterruptedException, ValidatorException {
        log.info("Filter test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume double);";
//        String query = "@info(name = 'query1') from cseEventStream[volume > 50l] select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream);

//        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                count++;
//            }
//        });
//
//        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
//        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
//        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
//        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
//        Thread.sleep(100);
//        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery8() throws InterruptedException, ValidatorException {
        log.info("Filter test8");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.FLOAT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60f});
        inputHandler.send(new Object[]{"WSO2", 70f, 40f});
        inputHandler.send(new Object[]{"WSO2", 44f, 200f});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery9() throws InterruptedException, ValidatorException {
        log.info("Filter test9");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.FLOAT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60f});
        inputHandler.send(new Object[]{"WSO2", 70f, 40f});
        inputHandler.send(new Object[]{"WSO2", 44f, 200f});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery10() throws InterruptedException, ValidatorException {
        log.info("Filter test10");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery11() throws InterruptedException, ValidatorException {
        log.info("Filter test11");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery12() throws InterruptedException, ValidatorException {
        log.info("Filter test12");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(45))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery13() throws InterruptedException, ValidatorException {
        log.info("Filter test13");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.FLOAT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60f});
        inputHandler.send(new Object[]{"WSO2", 70f, 40f});
        inputHandler.send(new Object[]{"WSO2", 44f, 200f});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery14() throws InterruptedException, ValidatorException {
        log.info("Filter test14");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.FLOAT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(45))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60f});
        inputHandler.send(new Object[]{"WSO2", 70f, 40f});
        inputHandler.send(new Object[]{"WSO2", 44f, 200f});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery15() throws InterruptedException, ValidatorException {
        log.info("Filter test15");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.GREATER_THAN, Expression.value(4d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery16() throws InterruptedException, ValidatorException {
        log.info("Filter test16");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery17() throws InterruptedException, ValidatorException {
        log.info("Filter test17");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(45))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery18() throws InterruptedException, ValidatorException {
        log.info("Filter test18");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.value(70),
                        Compare.Operator.GREATER_THAN,
                        Expression.variable("volume"))
                )
        );
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price"))
        );
        query.insertInto("StockQuote");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        QueryCallback queryCallback = new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("WSO2".equals(inEvents[0].getData(0)));
                count++;
            }

        };
        executionPlanRuntime.addCallback("query1", queryCallback);
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 50});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 30});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
    }


    // Test case for CONTAINS operator
    @Test
    public void testFilterQuery19() throws InterruptedException, ValidatorException {
        log.info("Filter test19");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.variable("symbol"),
                        Compare.Operator.CONTAINS,
                        Expression.value("WS"))
                )
        );
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query.insertInto("StockQuote");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery20() throws InterruptedException, ValidatorException {
        log.info("Filter test20");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.variable("volume"),
                        Compare.Operator.LESS_THAN,
                        Expression.value(100))
                )
        );
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query.insertInto("StockQuote");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
                count++;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 103l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 10l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery21() throws InterruptedException, ValidatorException {
        log.info("Filter test21");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.variable("volume"),
                        Compare.Operator.NOT_EQUAL,
                        Expression.value(100))
                )
        );
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query.insertInto("StockQuote");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
                count++;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 10l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

//    @Test
//    public void testFilterQuery23() throws InterruptedException {
//        log.info("Filter test23");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long)");
//        String queryReference = siddhiManager.addQuery("from cseEventStream[symbol != 'WSO2'] " +
//                "select symbol,price,volume " +
//                "insert into OutputStream;");
//
//        siddhiManager.addCallback(queryReference, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                count++;
//            }
//        });
//
//        inputHandler.send(new Object[]{"WSO2", 45f, 100l});
//        inputHandler.send(new Object[]{"IBM", 35f, 50l});
//
//        Thread.sleep(100);
//        Assert.assertEquals(1, count);
//        siddhiManager.shutdown();
//
//    }
//
//    @Test
//    public void testFilterQuery24() throws InterruptedException {
//        log.info("Filter test24");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));
//        Query query = QueryFactory.createQuery();
//        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(50f))));
//        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
//        query.insertInto("outputStream");
//
//        String queryReference = siddhiManager.addQuery(query);
//
//        siddhiManager.addCallback(queryReference, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                count++;
//            }
//        });
//
//        inputHandler.send(new Object[]{"WSO2", 45f, 100l});
//        inputHandler.send(new Object[]{"IBM", 35f, 50l});
//
//        Thread.sleep(100);
//        Assert.assertEquals(1, count);
//        siddhiManager.shutdown();
//
//    }
//
//    @Test
//    public void testFilterQuery25() throws InterruptedException {
//        log.info("Filter test25");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));
//        Query query = QueryFactory.createQuery();
//        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(50l))));
//        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
//        query.insertInto("outputStream");
//
//        String queryReference = siddhiManager.addQuery(query);
//
//        siddhiManager.addCallback(queryReference, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                count++;
//            }
//        });
//
//        inputHandler.send(new Object[]{"WSO2", 45f, 100l});
//        inputHandler.send(new Object[]{"IBM", 35f, 50l});
//
//        Thread.sleep(100);
//        Assert.assertEquals(1, count);
//        siddhiManager.shutdown();
//
//    }
//
//
//    @Test
//    public void testFilterQuery22() throws InterruptedException {
//        log.info("Filter test22");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
//        String queryReference = siddhiManager.addQuery("from cseEventStream[volume != 100l ] " +
//                "select symbol, price, volume " +
//                "insert into OutputStream;");
//
//        siddhiManager.addCallback(queryReference, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                //Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
//                count++;
//            }
//
//        });
//        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
//        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
//        inputHandler.send(new Object[]{"IBM", 57.6f, 10l});
//        Thread.sleep(100);
//        Assert.assertEquals(1, count);
//        siddhiManager.shutdown();
//
//    }
//
//
//    @Test
//    public void testFilterQuery26() throws InterruptedException {
//        log.info("Filter test26");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
//
//        String queryReference = siddhiManager.addQuery("from cseEventStream[price != 55.6f] " +
//                "select symbol, price, volume " +
//                "insert into outputStream;");
//
//        siddhiManager.addCallback(queryReference, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                //Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
//                count++;
//            }
//
//        });
//        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
//        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
//        inputHandler.send(new Object[]{"IBM", 57.6f, 10l});
//        Thread.sleep(100);
//        Assert.assertEquals(1, count);
//        siddhiManager.shutdown();
//
//    }
//
//    @Test
//    public void testFilterQuery27() throws InterruptedException {
//        log.info("Filter test27");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string,price float,volume long)");
//
//        String queryReference = siddhiManager.addQuery("from cseEventStream[volume != 50d ] select symbol,price,volume insert into outputStream ;");
//
//        siddhiManager.addCallback(queryReference, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                count++;
//            }
//        });
//
//        inputHandler.send(new Object[]{"WSO2", 55.5f, 40l});
//        inputHandler.send(new Object[]{"WSO2", 53.5f, 50l});
//        inputHandler.send(new Object[]{"WSO2", 50.5f, 400l});
//
//        Thread.sleep(100);
//        Assert.assertEquals(2, count);
//        siddhiManager.shutdown();
//
//    }
//
//    @Test
//    public void testFilterQuery28() throws InterruptedException {
//        log.info("Filter test28");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int)");
//        Query query = QueryFactory.createQuery();
//        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(100L))));
//        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
//        query.insertInto("outputStream");
//
//        String queryReference = siddhiManager.addQuery(query);
//
//        siddhiManager.addCallback(queryReference, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                count++;
//            }
//        });
//
//        inputHandler.send(new Object[]{"WSO2", 40f, 70});
//        inputHandler.send(new Object[]{"WSO2", 60f, 50});
//        inputHandler.send(new Object[]{"WSO2", 100f, 100});
//
//        Thread.sleep(100);
//        Assert.assertEquals(2, count);
//        siddhiManager.shutdown();
//
//
//    }
//
//    @Test
//    public void testFilterQuery29() throws InterruptedException {
//        log.info("Filter test29");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
//
//        Query query = QueryFactory.createQuery();
//        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(50))));
//        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
//        query.insertInto("outputStream");
//
//        String queryRefernce = siddhiManager.addQuery(query);
//
//        siddhiManager.addCallback(queryRefernce, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                count++;
//            }
//        });
//
//        inputHandler.send(new Object[]{"WSO2", 40f, 50});
//        inputHandler.send(new Object[]{"WSO2", 20f, 100});
//        Thread.sleep(100);
//        Assert.assertEquals(1, count);
//        siddhiManager.shutdown();
//
//
//    }
//
//    @Test
//    public void testFilterQuery30() throws InterruptedException {
//        log.info("Filter test30");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
//
//        Query query = QueryFactory.createQuery();
//        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(50f))));
//        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
//        query.insertInto("outputStream");
//
//        String queryRefernce = siddhiManager.addQuery(query);
//
//        siddhiManager.addCallback(queryRefernce, new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                count++;
//            }
//        });
//
//        inputHandler.send(new Object[]{"WSO2", 40f, 50});
//        inputHandler.send(new Object[]{"WSO2", 20f, 100});
//        Thread.sleep(100);
//        Assert.assertEquals(1, count);
//        siddhiManager.shutdown();
//    }
    //...........................................

    @Test
    public void testFilterQuery51() throws InterruptedException, ValidatorException {
        log.info("Filter test51");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.EQUAL, Expression.value(60f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery52() throws InterruptedException, ValidatorException {
        log.info("Filter test52");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.EQUAL, Expression.value(60))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");


        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery53() throws InterruptedException, ValidatorException {
        log.info("Filter test53");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.EQUAL, Expression.value(60l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");


        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery54() throws InterruptedException, ValidatorException {
        log.info("Filter test54");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery55() throws InterruptedException, ValidatorException {
        log.info("Filter test55");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.EQUAL, Expression.value(50f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery56() throws InterruptedException, ValidatorException {
        log.info("Filter test56");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.EQUAL, Expression.value(70))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery57() throws InterruptedException, ValidatorException {
        log.info("Filter test57");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.EQUAL, Expression.value(60l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery58() throws InterruptedException, ValidatorException {
        log.info("Filter test58");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.EQUAL, Expression.value(5d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery59() throws InterruptedException, ValidatorException {
        log.info("Filter test59");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.EQUAL, Expression.value(5f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery60() throws InterruptedException, ValidatorException {
        log.info("Filter test60");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.EQUAL, Expression.value(2))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery61() throws InterruptedException, ValidatorException {
        log.info("Filter test61");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.EQUAL, Expression.value(4l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery62() throws InterruptedException, ValidatorException {
        log.info("Filter test62");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.EQUAL, Expression.value(200l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60l, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200l, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery63() throws InterruptedException, ValidatorException {
        log.info("Filter test63");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.EQUAL, Expression.value(40d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery64() throws InterruptedException, ValidatorException {
        log.info("Filter test64");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.EQUAL, Expression.value(40f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery65() throws InterruptedException, ValidatorException {
        log.info("Filter test65");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.EQUAL, Expression.value(40))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    //**************************************************************************************************************************

    @Test
    public void testFilterQuery66() throws InterruptedException, ValidatorException {
        log.info("Filter test66 : NOT Operator");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.not(Expression.compare(Expression.variable("volume"), Compare.Operator.EQUAL, Expression.value(40)))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    //**************************************************************************************************************************
    //Test cases for less than or equal
    @Test
    public void testFilterQuery67() throws InterruptedException, ValidatorException {
        log.info("Filter test67");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(60d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50d, 60l});
        inputHandler.send(new Object[]{"WSO2", 70d, 40l});
        inputHandler.send(new Object[]{"WSO2", 44d, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery68() throws InterruptedException, ValidatorException {
        log.info("Filter test68");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(100f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50d, 60l});
        inputHandler.send(new Object[]{"WSO2", 70d, 40l});
        inputHandler.send(new Object[]{"WSO2", 44d, 200l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);

    }

    @Test
    public void testFilterQuery69() throws InterruptedException, ValidatorException {
        log.info("Filter test69");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(50))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50d, 60l});
        inputHandler.send(new Object[]{"WSO2", 70d, 40l});
        inputHandler.send(new Object[]{"WSO2", 44d, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery70() throws InterruptedException, ValidatorException {
        log.info("Filter test70");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(200l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery71() throws InterruptedException, ValidatorException {
        log.info("Filter test71");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);


    }

    @Test
    public void testFilterQuery72() throws InterruptedException, ValidatorException {
        log.info("Filter test72");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(200l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery73() throws InterruptedException, ValidatorException {
        log.info("Filter test73");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(5d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery74() throws InterruptedException, ValidatorException {
        log.info("Filter test74");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(5f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);


    }

    @Test
    public void testFilterQuery75() throws InterruptedException, ValidatorException {
        log.info("Filter test75");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(3l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery76() throws InterruptedException, ValidatorException {
        log.info("Filter test76");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery77() throws InterruptedException, ValidatorException {
        log.info("Filter test77");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(50f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery78() throws InterruptedException, ValidatorException {
        log.info("Filter test78");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(50))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery79() throws InterruptedException, ValidatorException {
        log.info("Filter test79");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN_EQUAL, Expression.value(60l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60l, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300l, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }


    //*************************************************************************************************************************
    //Test cases for less-than operator

    @Test
    public void testFilterQuery80() throws InterruptedException, ValidatorException {
        log.info("Filter test80");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery81() throws InterruptedException, ValidatorException {
        log.info("Filter test81");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN, Expression.value(70f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery82() throws InterruptedException, ValidatorException {
        log.info("Filter test82");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN, Expression.value(50))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50d, 60d});
        inputHandler.send(new Object[]{"WSO2", 70d, 40d});
        inputHandler.send(new Object[]{"WSO2", 44d, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }


    @Test
    public void testFilterQuery83() throws InterruptedException, ValidatorException {
        log.info("Filter test83");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN, Expression.value(60l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery84() throws InterruptedException, ValidatorException {
        log.info("Filter test84");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN, Expression.value(60l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery85() throws InterruptedException, ValidatorException {
        log.info("Filter test85");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.LESS_THAN, Expression.value(4l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery86() throws InterruptedException, ValidatorException {
        log.info("Filter test86");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN, Expression.value(40l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 50l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 20l, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300l, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery87() throws InterruptedException, ValidatorException {
        log.info("Filter test87");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery88() throws InterruptedException, ValidatorException {
        log.info("Filter test88");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.LESS_THAN, Expression.value(55f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery89() throws InterruptedException, ValidatorException {
        log.info("Filter test89");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.LESS_THAN, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery90() throws InterruptedException, ValidatorException {
        log.info("Filter test90");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.LESS_THAN, Expression.value(10f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery91() throws InterruptedException, ValidatorException {
        log.info("Filter test91");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.LESS_THAN, Expression.value(15))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery92() throws InterruptedException, ValidatorException {
        log.info("Filter test92");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN, Expression.value(100d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery93() throws InterruptedException, ValidatorException {
        log.info("Filter test93");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.LESS_THAN, Expression.value(100f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }


    //*********************************************************************************************************************
    // Test cases for Greater_than_equal operator

    @Test
    public void testFilterQuery94() throws InterruptedException, ValidatorException {
        log.info("Filter test94");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery95() throws InterruptedException, ValidatorException {
        log.info("Filter test95");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(70f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery96() throws InterruptedException, ValidatorException {
        log.info("Filter test96");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(50))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50d, 60d});
        inputHandler.send(new Object[]{"WSO2", 70d, 40d});
        inputHandler.send(new Object[]{"WSO2", 44d, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }


    @Test
    public void testFilterQuery97() throws InterruptedException, ValidatorException {
        log.info("Filter test97");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(60l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery98() throws InterruptedException, ValidatorException {
        log.info("Filter test98");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(60l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery99() throws InterruptedException, ValidatorException {
        log.info("Filter test99");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(4l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery100() throws InterruptedException, ValidatorException {
        log.info("Filter test100");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(40l))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 500f, 50l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 20l, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300l, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery101() throws InterruptedException, ValidatorException {
        log.info("Filter test101");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery102() throws InterruptedException, ValidatorException {
        log.info("Filter test102");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(55f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery103() throws InterruptedException, ValidatorException {
        log.info("Filter test103");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery104() throws InterruptedException, ValidatorException {
        log.info("Filter test104");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(10f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

    }

    @Test
    public void testFilterQuery105() throws InterruptedException, ValidatorException {
        log.info("Filter test105");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(15))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery106() throws InterruptedException, ValidatorException {
        log.info("Filter test106");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(100d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test
    public void testFilterQuery107() throws InterruptedException, ValidatorException {
        log.info("Filter test107");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN_EQUAL, Expression.value(100f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);

    }

    @Test(expected = OperationNotSupportedException.class)
    public void testFilterQuery108() throws InterruptedException, ValidatorException {
        log.info("Filter test108");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.variable("price"),
                        Compare.Operator.CONTAINS,
                        Expression.value("WS"))
                )
        );
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query.insertInto("StockQuote");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

    }

    //***********************************************************************************************************************
    //Expression-Add
    @Test
    public void testFilterQuery109() throws InterruptedException, ValidatorException {
        log.info("Filter test109");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("increasedPrice", Expression.add(Expression.value(100), Expression.variable("price"))).
                        select("increasedVolume", Expression.add(Expression.value(50), Expression.variable("volume"))).
                        select("increasedQuantity", Expression.add(Expression.value(4), Expression.variable("quantity")))
        );
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 4, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("155.5".equals(inEvents[0].getData()[1].toString()));
                Assert.assertTrue("150.0".equals(inEvents[0].getData()[2].toString()));
                Assert.assertTrue("9".equals(inEvents[0].getData()[3].toString()));
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.5f, 100d, 5});

        Thread.sleep(100);

    }

    //true check

    @Test
    public void testFilterQuery117() throws InterruptedException, ValidatorException {
        log.info("Filter test117");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.value(true)));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price1", Expression.variable("price1")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 1) {
                    Assert.assertEquals(50.0f, inEvents[0].getData()[1]);
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        Thread.sleep(100);
        Assert.assertEquals(1, count);


    }

    @Test
    public void testFilterQuery118() throws InterruptedException, ValidatorException {
        log.info("Filter test118");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.value(false)));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price1", Expression.variable("price1")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                Assert.fail("No events should occur");

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        Thread.sleep(100);
        Assert.assertEquals(0, count);


    }


}
