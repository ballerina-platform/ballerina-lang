/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.siddhi.core.query;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.query.api.SiddhiApp;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testcase for filter queries.
 */
public class FilterTestCase2 {
    private static final Logger log = LoggerFactory.getLogger(FilterTestCase2.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testFilterQuery83() throws InterruptedException {
        log.info("Filter test83");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.LESS_THAN,
                Expression.value(60L))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery84() throws InterruptedException {
        log.info("Filter test84");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"),
                Compare.Operator.LESS_THAN,
                Expression.value(60L))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery85() throws InterruptedException {
        log.info("Filter test85");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"),
                Compare.Operator.LESS_THAN,
                Expression.value(4L))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery86() throws InterruptedException {
        log.info("Filter test86");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute
                ("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.LESS_THAN,
                Expression.value(40L))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 500f, 50L, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 20L, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300L, 4});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery87() throws InterruptedException {
        log.info("Filter test87");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"),
                Compare.Operator.LESS_THAN, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testFilterQuery88() throws InterruptedException {
        log.info("Filter test88");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"),
                Compare.Operator.LESS_THAN, Expression.value(55f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery89() throws InterruptedException {
        log.info("Filter test89");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"),
                Compare.Operator.LESS_THAN, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery90() throws InterruptedException {
        log.info("Filter test90");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"),
                Compare.Operator.LESS_THAN, Expression.value(10f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery91() throws InterruptedException {
        log.info("Filter test91");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"),
                Compare.Operator.LESS_THAN, Expression.value(15))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery92() throws InterruptedException {
        log.info("Filter test92");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute
                ("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.LESS_THAN, Expression.value(100d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60L, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40L, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200L, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery93() throws InterruptedException {
        log.info("Filter test93");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute
                ("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.LESS_THAN, Expression.value(100f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60L, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40L, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200L, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }


    //*****************************************************************************************************************
    // Test cases for Greater_than_equal operator

    @Test
    public void testFilterQuery94() throws InterruptedException {
        log.info("Filter test94");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery95() throws InterruptedException {
        log.info("Filter test95");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(70f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery96() throws InterruptedException {
        log.info("Filter test96");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(50))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50d, 60d});
        inputHandler.send(new Object[]{"WSO2", 70d, 40d});
        inputHandler.send(new Object[]{"WSO2", 44d, 200d});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testFilterQuery97() throws InterruptedException {
        log.info("Filter test97");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.GREATER_THAN_EQUAL,
                Expression.value(60L))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery98() throws InterruptedException {
        log.info("Filter test98");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"),
                Compare.Operator.GREATER_THAN_EQUAL,
                Expression.value(60L))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery99() throws InterruptedException {
        log.info("Filter test99");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"),
                Compare.Operator.GREATER_THAN_EQUAL,
                Expression.value(4L))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery100() throws InterruptedException {
        log.info("Filter test100");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute
                ("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.GREATER_THAN_EQUAL,
                Expression.value(40L))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 500f, 50L, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 20L, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300L, 4});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testFilterQuery101() throws InterruptedException {
        log.info("Filter test101");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery102() throws InterruptedException {
        log.info("Filter test102");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("price"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(55f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery103() throws InterruptedException {
        log.info("Filter test103");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(50d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testFilterQuery104() throws InterruptedException {
        log.info("Filter test104");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(10f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(2, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery105() throws InterruptedException {
        log.info("Filter test105");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("quantity"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(15))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testFilterQuery106() throws InterruptedException {
        log.info("Filter test106");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute
                ("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(100d))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60L, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40L, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200L, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery107() throws InterruptedException {
        log.info("Filter test107");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute
                ("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.compare(Expression.variable("volume"),
                Compare.Operator.GREATER_THAN_EQUAL, Expression.value(100f))));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price", Expression
                .variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60L, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40L, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200L, 56});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testFilterQuery108() throws InterruptedException {
        log.info("Filter test108");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").
                filter(Expression.compare(Expression.variable("price"),
                        Compare.Operator.EQUAL,
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

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

    }

    //*****************************************************************************************************************
    //Expression-Add
    @Test
    public void testFilterQuery109() throws InterruptedException {
        log.info("Filter test109");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT).attribute("awards", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().
                select("symbol", Expression.variable("symbol")).
                select("increasedPrice", Expression.add(Expression.value(100), Expression.variable("price"))).
                select("increasedVolume", Expression.add(Expression.value(50), Expression.variable("volume"))).
                select("increasedQuantity", Expression.add(Expression.value(4), Expression.variable("quantity"))).
                select("increasedAwards", Expression.add(Expression.value(10), Expression.variable("awards")))
        );
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                AssertJUnit.assertTrue("155.5".equals(inEvents[0].getData()[1].toString()));
                AssertJUnit.assertTrue("150.0".equals(inEvents[0].getData()[2].toString()));
                AssertJUnit.assertTrue("9".equals(inEvents[0].getData()[3].toString()));
                AssertJUnit.assertTrue("20".equals(inEvents[0].getData()[4].toString()));
                count = count + inEvents.length;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.5f, 100d, 5, 10L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    //*****************************************************************************************************************
    //Expression-subtract
    @Test
    public void testFilterQuery110() throws InterruptedException {
        log.info("Filter test110");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT).attribute("awards", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("decreasedPrice", Expression.subtract(Expression.variable("price"), Expression.value
                                (20))).
                        select("decreasedVolume", Expression.subtract(Expression.variable("volume"), Expression.value
                                (50))).
                        select("decreasedQuantity", Expression.subtract(Expression.variable("quantity"), Expression
                                .value(4))).
                        select("decreasedAwards", Expression.subtract(Expression.variable("awards"), Expression.value
                                (10)))

        );
        query.insertInto("OutputStream");


        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                AssertJUnit.assertTrue("35.5".equals(inEvents[0].getData()[1].toString()));
                AssertJUnit.assertTrue("50.0".equals(inEvents[0].getData()[2].toString()));
                AssertJUnit.assertTrue("1".equals(inEvents[0].getData()[3].toString()));
                AssertJUnit.assertTrue("0".equals(inEvents[0].getData()[4].toString()));
                count = count + inEvents.length;
            }

        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.5f, 100d, 5, 10L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();

    }


    //***************************************************************************************************************
    //Expression Divide
    @Test
    public void testFilterQuery111() throws InterruptedException {
        log.info("Filter test111");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT).attribute("awards", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("dividedPrice", Expression.divide(Expression.variable("price"), Expression.value(2))).
                        select("dividedVolume", Expression.divide(Expression.variable("volume"), Expression.value(2))).
                        select("dividedQuantity", Expression.divide(Expression.variable("quantity"), Expression.value
                                (5))).
                        select("dividedAwards", Expression.divide(Expression.variable("awards"), Expression.value(10)))

        );
        query.insertInto("OutputStream");


        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                AssertJUnit.assertTrue("30.0".equals(inEvents[0].getData()[1].toString()));
                AssertJUnit.assertTrue("50.0".equals(inEvents[0].getData()[2].toString()));
                AssertJUnit.assertTrue("20".equals(inEvents[0].getData()[3].toString()));
                AssertJUnit.assertTrue("7".equals(inEvents[0].getData()[4].toString()));
                count = count + inEvents.length;

            }

        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 60f, 100d, 100, 70L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();

    }

    //****************************************************************************************************************
    //Expression Multiply
    @Test
    public void testFilterQuery112() throws InterruptedException {
        log.info("Filter test112");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT).attribute("awards", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("multipliedQuantity", Expression.multiply(Expression.variable("quantity"), Expression
                                .value(4))).
                        select("multipliedPrice", Expression.multiply(Expression.variable("price"), Expression.value
                                (2))).
                        select("multipliedVolume", Expression.multiply(Expression.variable("volume"), Expression
                                .value(3))).
                        select("multipliedAwards", Expression.multiply(Expression.variable("awards"), Expression
                                .value(5)))

        );
        query.insertInto("OutputStream");


        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                AssertJUnit.assertTrue("20".equals(inEvents[0].getData()[1].toString()));
                AssertJUnit.assertTrue("111.0".equals(inEvents[0].getData()[2].toString()));
                AssertJUnit.assertTrue("300.0".equals(inEvents[0].getData()[3].toString()));
                AssertJUnit.assertTrue("15".equals(inEvents[0].getData()[4].toString()));
                count = count + inEvents.length;
            }

        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.5f, 100d, 5, 3L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();

    }

    //******************************************************************************************************************
    //Expression Mod

    @Test
    public void testFilterQuery113() throws InterruptedException {
        log.info("Filter test113");

        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE)
                .attribute("quantity", Attribute.Type.INT).attribute("awards", Attribute.Type.LONG);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("modPrice", Expression.mod(Expression.variable("price"), Expression.value(2))).
                        select("modVolume", Expression.mod(Expression.variable("volume"), Expression.value(2))).
                        select("modQuantity", Expression.mod(Expression.variable("quantity"), Expression.value(2))).
                        select("modAwards", Expression.mod(Expression.variable("awards"), Expression.value(2)))

        );
        query.insertInto("OutputStream");


        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                AssertJUnit.assertTrue("1.5".equals(inEvents[0].getData()[1].toString()));
                AssertJUnit.assertTrue("1.0".equals(inEvents[0].getData()[2].toString()));
                AssertJUnit.assertTrue("1".equals(inEvents[0].getData()[3].toString()));
                AssertJUnit.assertTrue("1".equals(inEvents[0].getData()[4].toString()));
                count = count + inEvents.length;
            }

        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.5f, 101d, 5, 7L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }


    //true check

    @Test
    public void testFilterQuery114() throws InterruptedException {
        log.info("Filter test114");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.value(true)));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price1", Expression
                .variable("price1")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 1) {
                    AssertJUnit.assertEquals(50.0f, inEvents[0].getData()[1]);
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60L, 6});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testFilterQuery115() throws InterruptedException {
        log.info("Filter test115");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream").filter(Expression.value(false)));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(Selector.selector().select("symbol", Expression.variable("symbol")).select("price1", Expression
                .variable("price1")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                AssertJUnit.fail("No events should occur");

            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60L, 6});
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void filterTest116() throws InterruptedException {
        log.info("filter test116");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream select symbol,price+5 as price insert into " +
                "outputStream ;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void filterTest117() throws InterruptedException {
        log.info("filter test116");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream#window.timeBatch(500) select symbol,sum(price)+5 " +
                "as price insert into outputStream ;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
                if (count == 1) {
                    AssertJUnit.assertEquals(1465.5, inEvents[0].getData()[1]);
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        Thread.sleep(1000);
        AssertJUnit.assertEquals(1, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void filterTest118() throws InterruptedException {
        log.info("filter test118");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream#window.timeBatch(500)  select volume, sum(price) " +
                "as price group by volume insert into outputStream ;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        Thread.sleep(1000);
        AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void filterTest119() throws InterruptedException {
        log.info("filter test119");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream select symbol,sum(price)+10 as price group by " +
                "symbol having price > 880 insert into outputStream ;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void filterTest120() throws InterruptedException {
        log.info("filter test120");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream select symbol,sum(price) as sumprice group by " +
                "symbol having sumprice > 880 insert into outputStream ;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

//    @Test
//    public void filterTest1023() throws InterruptedException {
//        log.info("filter test2");
//        SiddhiManager siddhiManager = new SiddhiManager();
////        siddhiManager.setExtension("str:concat", ConcatFunctionExtension.class);
//
//
//        String cseEventStream = " define stream RequestStream (messageID string, app_key string, api_key string,
// app_tier string, api_tier string, user_id string, properties string, timeNow long); " +
//                " define stream EligibilityStream (rule string, messageID string, isEligible bool,
// isLocallyThrottled bool, throttle_key string , timeNow long); ";
//
//        String query = "" +
//                "@info(name = 'query1') " +
//                "FROM RequestStream " +
//                "SELECT 'sub_gold' AS rule, messageID, ( api_tier == 'Gold') AS isEligible,false as
// isLocallyThrottled,  'sub_gold_TEST1TEST1Test1_key' AS throttle_key , timeNow \n" +
//                "INSERT INTO EligibilityStream; " +
//                "@info(name = 'query2') FROM EligibilityStream[isEligible==false]\n" +
//                "\t\tSELECT rule, messageID, false AS isThrottled , timeNow\n" +
//                "\t\tINSERT INTO ThrottleStream;\n" +
//                "\n" +
//                " @info(name = 'query3') FROM EligibilityStream[isEligible==true AND isLocallyThrottled==true]\n" +
//                "\t\tSELECT rule, messageID, true AS isThrottled , timeNow \n" +
//                "\t\tINSERT INTO ThrottleStream; \n" +
//                "\n" +
//                "@info(name = 'query4') FROM EligibilityStream[isEligible==true AND isLocallyThrottled==false]\n" +
//                "\t\tSELECT rule, messageID, false AS isThrottled, timeNow \n" +
//                "\t\tINSERT INTO ThrottleStream;  ";
//
//
//        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
//        siddhiAppRuntime.addCallback("ThrottleStream", new StreamCallback() {
//                    public int eventCount = 0;
//                    public int timeSpent = 0;
//                    long startTime = System.currentTimeMillis();
//
//                    @Override
//                    public void receive(Event[] events) {
//                        for (Event event : events) {
//                            eventCount++;
//                            timeSpent += (System.currentTimeMillis() - (Long) event.getData(3));
//                            if (eventCount % 1000000 == 0) {
//                                log.info("Throughput : " + (eventCount * 1000) / ((System
// .currentTimeMillis()) - startTime));
//                                log.info("Time spend :  " + (timeSpent * 1.0 / eventCount));
//                                startTime = System.currentTimeMillis();
//                                eventCount = 0;
//                                timeSpent = 0;
//                            }
//                        }
//                    }
//                }
//
//        );
//
//        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("RequestStream");
//        siddhiAppRuntime.start();
//
//
//        for (int i = 0; i <= 100; i++) {
//            EventPublisher eventPublisher = new EventPublisher(inputHandler);
//            eventPublisher.run();
//        }
//
//        //siddhiAppRuntime.shutdown();
//
//    }
//
//
//    class EventPublisher implements Runnable {
//
//        InputHandler inputHandler;
//
//        EventPublisher(InputHandler inputHandler) {
//            this.inputHandler = inputHandler;
//        }
//
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    inputHandler.send(new Object[]{"IBM", "TEST1", "TEST1", "TEST1", "Gold", "Test1", null, System
// .currentTimeMillis()});
//                    inputHandler.send(new Object[]{"IBM", "TEST1", "TEST1", "TEST1", "Gold", "Test1", null, System
// .currentTimeMillis()});
//                    inputHandler.send(new Object[]{"IBM", "TEST1", "TEST1", "TEST1", "Gold", "Test1", null, System
// .currentTimeMillis()});
//                    inputHandler.send(new Object[]{"IBM", "TEST1", "TEST1", "TEST1", "Gold", "Test1", null, System
// .currentTimeMillis()});
//                } catch (InterruptedException e) {
//                    log.error(e);
//                }
//            }
//
//
//        }
//    }


    @Test
    public void filterTest121() throws InterruptedException {
        log.info("filter test121");
        SiddhiManager siddhiManager = new SiddhiManager();


        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') " +
                "from cseEventStream[150 > volume] " +
                "select symbol,price , symbol as sym1 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                AssertJUnit.assertTrue("IBM".equals(inEvents[0].getData(2)));
                count = count + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

}
