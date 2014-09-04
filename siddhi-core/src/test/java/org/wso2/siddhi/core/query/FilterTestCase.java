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

import java.util.concurrent.locks.Condition;

public class FilterTestCase {
    static final Logger log = Logger.getLogger(FilterTestCase.class);
    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }


    //greater than condition
    @Test
    public void FilterTest1() throws InterruptedException, ValidatorException {
        log.info("filter test1");
        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition streamA = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"). filter(Expression.compare(Expression.value(70),
                Compare.Operator.GREATER_THAN,
                Expression.variable("price"))
        ));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price"))
        );
        query.insertInto("StockQuote");


        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(streamA);
        executionPlan.addQuery(query);

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
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
        inputHandler.send(new Object[]{"WSO2", 60.5f,200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        Assert.assertTrue(eventArrived);
    }

    @Test
    public void FilterTest2() throws InterruptedException, ValidatorException {
        log.info("filter test2");
        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"). filter(Expression.compare(Expression.value(150),
                Compare.Operator.GREATER_THAN,
                Expression.variable("volume"))
        ));
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


        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)));
                count++;
                eventArrived = true;
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 700f,100l});
        inputHandler.send(new Object[]{"WSO2", 60.5f,200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        Assert.assertTrue(eventArrived);
    }

    @Test
    public void testFilterQuery3() throws InterruptedException, ValidatorException {
        log.info("Filter test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition streamA = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.value(70),
                        Compare.Operator.GREATER_THAN,
                        Expression.variable("price"))
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
        executionPlan.defineStream(streamA);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(executionPlan);


        executionPlanRuntime.addCallback("query1", new QueryCallback(query, "query1", 2, siddhiManager.getSiddhiContext()){
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

        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50f))));
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
    public void testFilterQuery5() throws InterruptedException, ValidatorException {
        log.info("Filter test5");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50l))));
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
    public void testFilterQuery6() throws InterruptedException, ValidatorException {
        log.info("Filter test6");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50l))));
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
        StreamDefinition cseEventStream = new StreamDefinition("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"), Compare.Operator.GREATER_THAN, Expression.value(50l))));
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
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);

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


}
