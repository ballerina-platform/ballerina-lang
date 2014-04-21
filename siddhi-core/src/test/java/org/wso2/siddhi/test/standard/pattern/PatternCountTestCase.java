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
package org.wso2.siddhi.test.standard.pattern;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
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
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class PatternCountTestCase {
    static final Logger log = Logger.getLogger(PatternCountTestCase.class);
    int eventCount;

    @Before
    public void inti() {
        eventCount = 0;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("test1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.value(20))), 2, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1.1", Expression.variable("e1", 0, "price")).
                        select("price1.2", Expression.variable("e1", 1, "price")).
                        select("price1.3", Expression.variable("e1", 2, "price")).
                        select("price1.4", Expression.variable("e1", 3, "price")).
                        select("price2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertArrayEquals(new Object[]{25.6f, 47.6f, 47.8f, null, 45.7f}, inEvents[0].getData());
                eventCount++;
            }
        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 13.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);

    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("test2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.value(20))), 2, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1.1", Expression.variable("e1", 0, "price")).
                        select("price1.2", Expression.variable("e1", 1, "price")).
                        select("price1.3", Expression.variable("e1", 2, "price")).
                        select("price1.4", Expression.variable("e1", 3, "price")).
                        select("price2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertArrayEquals(new Object[]{25.6f, 47.6f, null, null, 45.7f}, inEvents[0].getData());
                eventCount++;
            }

        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 13.7f, 100});
        Thread.sleep(500);

        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);
    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("test3 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.value(20))), 2, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1.1", Expression.variable("e1", 0, "price")).
                        select("price1.2", Expression.variable("e1", 1, "price")).
                        select("price1.3", Expression.variable("e1", 2, "price")).
                        select("price1.4", Expression.variable("e1", 3, "price")).
                        select("price2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertArrayEquals(new Object[]{25.6f, 47.8f, null, null, 55.7f}, inEvents[0].getData());
                eventCount++;
            }

        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);
    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("test4 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.value(20))), 2, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1.1", Expression.variable("e1", 0, "price")).
                        select("price1.2", Expression.variable("e1", 1, "price")).
                        select("price1.3", Expression.variable("e1", 2, "price")).
                        select("price1.4", Expression.variable("e1", 3, "price")).
                        select("price2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.fail();
                eventCount++;
            }

        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(0, eventCount);
    }

    @Test
    public void testQuery5() throws InterruptedException {
        log.info("test5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.value(20))), 2, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1.1", Expression.variable("e1", 0, "price")).
                        select("price1.2", Expression.variable("e1", 1, "price")).
                        select("price1.3", Expression.variable("e1", 2, "price")).
                        select("price1.4", Expression.variable("e1", 3, "price")).
                        select("price2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertArrayEquals(new Object[]{25.6f, 47.6f, 23.7f, 24.7f, 45.7f}, inEvents[0].getData());
                eventCount++;
            }
        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 23.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 24.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 25.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"WSO2", 27.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);
    }


    @Test
    public void testQuery6() throws InterruptedException {
        log.info("test6 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.value(20))), 2, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.variable("e1", 1, "price"))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", 0, "price")).
                        select("price2.1", Expression.variable("e1", 1, "price")).
                        select("price2.2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertArrayEquals(new Object[]{25.6f, 47.6f, 55.7f}, inEvents[0].getData());
                eventCount++;
            }
        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);

    }

    @Test
    public void testQuery7() throws InterruptedException {
        log.info("test7 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.value(20))), 0, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))))));
//                                                          Expression.variable("e1", 1, "price"))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", 0, "price")).
                        select("price2.1", Expression.variable("e1", 1, "price")).
                        select("price2.2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertArrayEquals(new Object[]{null, null, 45.7f}, inEvents[0].getData());
                eventCount++;
            }

        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);

    }

    @Test
    public void testQuery8() throws InterruptedException {
        log.info("test8 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.count(
                                        QueryFactory.inputStream("e1", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.value(20))), 0, 5),
                                QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
//                                                          Expression.value(20))))));
                                                          Expression.variable("e1", 0, "price"))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", 0, "price")).
                        select("price2.1", Expression.variable("e1", 1, "price")).
                        select("price2.2", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertArrayEquals(new Object[]{25.6f, null, 45.7f}, inEvents[0].getData());
                eventCount++;
            }
        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 7.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);

    }

    @Test
    public void testQuery9() throws InterruptedException, SiddhiParserException {
        log.info("test9  OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] -> e2 = cseEventStream [price <= 40 ] <:5> -> e3 = cseEventStream [volume <= 70 ] " +
                                                       "select e1.symbol as symbol1,e2[0].symbol as symbol2,e3.symbol as symbol3 " +
                                                       " insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"IBM", "GOOG", "WSO2"}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        cseStreamHandler.send(new Object[]{"GOOG", 21f, 81});
        cseStreamHandler.send(new Object[]{"WSO2", 176.6f, 65});
        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }

    @Test
    public void testQuery10() throws InterruptedException, SiddhiParserException {
        log.info("test10  OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] -> e2 = cseEventStream [price <= 40 ] <:5> -> e3 = cseEventStream [volume <= 70 ] " +
                                                       "select e1.symbol as symbol1,e2[0].symbol as symbol2,e3.symbol as symbol3 " +
                                                       "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"IBM", null, "GOOG"}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        cseStreamHandler.send(new Object[]{"GOOG", 21f, 61});
        cseStreamHandler.send(new Object[]{"WSO2", 21f, 61});
        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }


    @Test
    public void testQuery11() throws InterruptedException, SiddhiParserException {
        log.info("test11  OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] -> e2 = cseEventStream [price <= 40 ] <:5> -> e3 = cseEventStream [volume <= 70 ] " +
                                                       "select e1.symbol as symbol1,e2[last].symbol as symbol2,e3.symbol as symbol3 " +
                                                       "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"IBM", "FB", "WSO2"}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        cseStreamHandler.send(new Object[]{"GOOG", 21f, 91});
        cseStreamHandler.send(new Object[]{"FB", 21f, 81});
        cseStreamHandler.send(new Object[]{"WSO2", 21f, 61});
        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }

    @Test
    public void testQuery12() throws InterruptedException, SiddhiParserException {
        log.info("test12  OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] -> e2 = cseEventStream [price <= 40 ] <2:> -> e3 = cseEventStream [volume <= 70 ] " +
                "select e1.symbol as symbol1,e2[last].symbol as symbol2,e3.symbol as symbol3 " +
                "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"IBM", "FB", "WSO2"}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        cseStreamHandler.send(new Object[]{"GOOG", 21f, 91});
        cseStreamHandler.send(new Object[]{"FB", 21f, 81});
        cseStreamHandler.send(new Object[]{"WSO2", 21f, 61});
        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }

}
