/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.test.distributed.pattern;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.pattern.Pattern;

public class PatternEveryDistributedTestCase {
    static final Logger log = Logger.getLogger(PatternEveryDistributedTestCase.class);
    private int eventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        eventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testPatternEveryDistributed1 - OUT 1");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.patternStream(
                            Pattern.followedBy(
                                    QueryFactory.inputStream("e1", "Stream1").filter(
                                            Condition.compare(Expression.variable("price"),
                                                              Condition.Operator.GREATER_THAN,
                                                              Expression.value(20))),
                                    QueryFactory.inputStream("e2", "Stream2").filter(
                                            Condition.compare(Expression.variable("price"),
                                                              Condition.Operator.GREATER_THAN,
                                                              Expression.variable("e1", "price"))))));

            query.select(
                    QueryFactory.outputSelector().
                            select("symbol1", Expression.variable("e1", "symbol")).
                            select("symbol2", Expression.variable("e2", "symbol"))

            );
            query.insertInto("OutStream");


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    Assert.assertArrayEquals(new Object[]{"WSO2", "IBM"}, inEvents[0].getData());
                    eventCount++;
                    eventArrived = true;
                }
            });
            InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
            InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
            stream1.send(new Object[]{"WSO2", 55.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"GOOG", 55.6f, 100});
            Thread.sleep(50000);
            stream2.send(new Object[]{"IBM", 55.7f, 100});
            Thread.sleep(500);

        } finally {
            siddhiManager.shutdown();
        }

        Assert.assertEquals("Number of success events", 1, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("testPatternEveryDistributed2 - OUT 2");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.patternStream(
                            Pattern.followedBy(
                                    Pattern.every(
                                            QueryFactory.inputStream("e1", "Stream1").filter(
                                                    Condition.compare(Expression.variable("price"),
                                                                      Condition.Operator.GREATER_THAN,
                                                                      Expression.value(20)))),
                                    QueryFactory.inputStream("e2", "Stream2").filter(
                                            Condition.compare(Expression.variable("price"),
                                                              Condition.Operator.GREATER_THAN,
                                                              Expression.variable("e1", "price"))))));

            query.select(
                    QueryFactory.outputSelector().
                            select("symbol1", Expression.variable("e1", "symbol")).
                            select("symbol2", Expression.variable("e2", "symbol"))

            );
            query.insertInto("OutStream");


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (eventCount == 0) {
                        Assert.assertArrayEquals(new Object[]{"WSO2", "IBM"}, inEvents[0].getData());
                    } else if (eventCount == 1) {
                        Assert.assertArrayEquals(new Object[]{"GOOG", "IBM"}, inEvents[0].getData());
                    } else {
                        Assert.fail();
                    }
                    eventCount++;
                    eventArrived = true;
                }
            });
            InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
            InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
            stream1.send(new Object[]{"WSO2", 55.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"GOOG", 54f, 100});
            Thread.sleep(500);
            stream2.send(new Object[]{"IBM", 55.7f, 100});
            Thread.sleep(500);

        } finally {
            siddhiManager.shutdown();
        }

        Assert.assertEquals("Number of success events", 2, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("testPatternEveryDistributed3 - OUT 1");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.patternStream(
                            Pattern.followedBy(
                                    Pattern.every(
                                            Pattern.followedBy(QueryFactory.inputStream("e1", "Stream1").filter(
                                                    Condition.compare(Expression.variable("price"),
                                                                      Condition.Operator.GREATER_THAN,
                                                                      Expression.value(20))), QueryFactory.inputStream("e3", "Stream1").filter(
                                                    Condition.compare(Expression.variable("price"),
                                                                      Condition.Operator.GREATER_THAN,
                                                                      Expression.value(20))))),
                                    QueryFactory.inputStream("e2", "Stream2").filter(
                                            Condition.compare(Expression.variable("price"),
                                                              Condition.Operator.GREATER_THAN,
                                                              Expression.variable("e1", "price"))))));

            query.select(
                    QueryFactory.outputSelector().
                            select("price1", Expression.variable("e1", "price")).
                            select("price3", Expression.variable("e3", "price")).
                            select("price2", Expression.variable("e2", "price"))

            );
            query.insertInto("OutStream");


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    Assert.assertArrayEquals(new Object[]{55.6f, 54f, 57.7f}, inEvents[0].getData());
                    eventCount++;
                    eventArrived = true;
                }
            });
            InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
            InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
            stream1.send(new Object[]{"WSO2", 55.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"GOOG", 54f, 100});
            Thread.sleep(500);
            stream2.send(new Object[]{"IBM", 57.7f, 100});
            Thread.sleep(500);

        } finally {
            siddhiManager.shutdown();
        }

        Assert.assertEquals("Number of success events", 1, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("testPatternEveryDistributed4 - OUT 2");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.patternStream(
                            Pattern.followedBy(
                                    Pattern.every(
                                            Pattern.followedBy(
                                                    QueryFactory.inputStream("e1", "Stream1").filter(
                                                            Condition.compare(Expression.variable("price"),
                                                                              Condition.Operator.GREATER_THAN,
                                                                              Expression.value(20))),
                                                    QueryFactory.inputStream("e3", "Stream1").filter(
                                                            Condition.compare(Expression.variable("price"),
                                                                              Condition.Operator.GREATER_THAN,
                                                                              Expression.value(20))))),
                                    QueryFactory.inputStream("e2", "Stream2").filter(
                                            Condition.compare(Expression.variable("price"),
                                                              Condition.Operator.GREATER_THAN,
                                                              Expression.variable("e1", "price"))))));

            query.select(
                    QueryFactory.outputSelector().
                            select("price1", Expression.variable("e1", "price")).
                            select("price3", Expression.variable("e3", "price")).
                            select("price2", Expression.variable("e2", "price"))

            );
            query.insertInto("OutStream");


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (eventCount == 0) {
                        Assert.assertArrayEquals(new Object[]{55.6f, 54f, 57.7f}, inEvents[0].getData());
                    } else if (eventCount == 1) {
                        Assert.assertArrayEquals(new Object[]{53.6f, 53f, 57.7f}, inEvents[0].getData());
                    } else {
                        Assert.fail();
                    }
                    eventCount++;
                    eventArrived = true;
                }
            });
            InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
            InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
            stream1.send(new Object[]{"WSO2", 55.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"GOOG", 54f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"WSO2", 53.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"GOOG", 53f, 100});
            Thread.sleep(500);
            stream2.send(new Object[]{"IBM", 57.7f, 100});
            Thread.sleep(500);

        } finally {
            siddhiManager.shutdown();
        }

        Assert.assertEquals("Number of success events", 2, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    @Test
    public void testQuery5() throws InterruptedException {
        log.info("testPatternEveryDistributed5  - OUT 2");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.patternStream(
                            Pattern.followedBy(QueryFactory.inputStream("e4", "Stream1").filter(
                                    Condition.compare(Expression.variable("symbol"),
                                                      Condition.Operator.EQUAL,
                                                      Expression.value("MSFT"))),
                                               Pattern.followedBy(
                                                       Pattern.every(
                                                               Pattern.followedBy(QueryFactory.inputStream("e1", "Stream1").filter(
                                                                       Condition.compare(Expression.variable("price"),
                                                                                         Condition.Operator.GREATER_THAN,
                                                                                         Expression.value(20))), QueryFactory.inputStream("e3", "Stream1").filter(
                                                                       Condition.compare(Expression.variable("price"),
                                                                                         Condition.Operator.GREATER_THAN,
                                                                                         Expression.value(20))))),
                                                       QueryFactory.inputStream("e2", "Stream2").filter(
                                                               Condition.compare(Expression.variable("price"),
                                                                                 Condition.Operator.GREATER_THAN,
                                                                                 Expression.variable("e1", "price")))))));

            query.select(
                    QueryFactory.outputSelector().
                            select("price1", Expression.variable("e1", "price")).
                            select("price3", Expression.variable("e3", "price")).
                            select("price2", Expression.variable("e2", "price"))

            );
            query.insertInto("OutStream");


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (eventCount == 0) {
                        Assert.assertArrayEquals(new Object[]{55.6f, 54f, 57.7f}, inEvents[0].getData());
                    } else if (eventCount == 1) {
                        Assert.assertArrayEquals(new Object[]{53.6f, 53f, 57.7f}, inEvents[0].getData());
                    } else {
                        Assert.fail();
                    }
                    eventCount++;
                    eventArrived = true;
                }
            });
            InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
            InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
            stream1.send(new Object[]{"MSFT", 55.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"WSO2", 55.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"GOOG", 54f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"WSO2", 53.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"GOOG", 53f, 100});
            Thread.sleep(500);
            stream2.send(new Object[]{"IBM", 57.7f, 100});
            Thread.sleep(500);

        } finally {
            siddhiManager.shutdown();
        }

        Assert.assertEquals("Number of success events", 2, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    @Test
    public void testQuery6() throws InterruptedException {
        log.info("testPatternEveryDistributed6  - OUT 2");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.patternStream(
                            Pattern.every(
                                    Pattern.followedBy(
                                            QueryFactory.inputStream("e1", "Stream1").filter(
                                                    Condition.compare(Expression.variable("price"),
                                                                      Condition.Operator.GREATER_THAN,
                                                                      Expression.value(20))),
                                            QueryFactory.inputStream("e3", "Stream1").filter(
                                                    Condition.compare(Expression.variable("price"),
                                                                      Condition.Operator.GREATER_THAN,
                                                                      Expression.value(20)))))));

            query.select(
                    QueryFactory.outputSelector().
                            select("price1", Expression.variable("e1", "price")).
                            select("price3", Expression.variable("e3", "price"))

            );
            query.insertInto("OutStream");


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (eventCount == 0) {
                        Assert.assertArrayEquals(new Object[]{55.6f, 57.6f}, inEvents[0].getData());
                    } else if (eventCount == 1) {
                        Assert.assertArrayEquals(new Object[]{54f, 53.6f}, inEvents[0].getData());
                    } else {
                        Assert.fail();
                    }
                    eventCount++;
                    eventArrived = true;
                }
            });
            InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
            stream1.send(new Object[]{"MSFT", 55.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"GOOG", 54f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"WSO2", 53.6f, 100});
            Thread.sleep(500);

        } finally {
            siddhiManager.shutdown();
        }

        Assert.assertEquals("Number of success events", 2, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    @Test
    public void testQuery7() throws InterruptedException {
        log.info("testPatternEveryDistributed7  - OUT 2");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.patternStream(
                            Pattern.every(
                                    QueryFactory.inputStream("e1", "Stream1").filter(
                                            Condition.compare(Expression.variable("price"),
                                                              Condition.Operator.GREATER_THAN,
                                                              Expression.value(20f)))
                            )));

            query.select(
                    QueryFactory.outputSelector().
                            select("price1", Expression.variable("e1", "price"))

            );
            query.insertInto("OutStream");


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (eventCount == 0) {
                        Assert.assertArrayEquals(new Object[]{55.6f}, inEvents[0].getData());
                    } else if (eventCount == 1) {
                        Assert.assertArrayEquals(new Object[]{57.6f}, inEvents[0].getData());
                    } else {
                        Assert.fail();
                    }
                    eventCount++;
                    eventArrived = true;
                }
            });
            InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
            stream1.send(new Object[]{"MSFT", 55.6f, 100});
            Thread.sleep(500);
            stream1.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(500);

        } finally {
            siddhiManager.shutdown();
        }

        Assert.assertEquals("Number of success events", 2, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

}
