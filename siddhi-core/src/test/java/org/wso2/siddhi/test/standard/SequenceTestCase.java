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
package org.wso2.siddhi.test.standard;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.sequence.Sequence;
import org.wso2.siddhi.query.api.utils.SiddhiConstants;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class SequenceTestCase {
    static final Logger log = Logger.getLogger(SequenceTestCase.class);
    int eventCount;

    @Before
    public void inti() {
        eventCount = 0;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testSequence1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
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
                Assert.assertEquals("WSO2", inEvents[0].getData(0));
                Assert.assertEquals("IBM", inEvents[0].getData(1));
                eventCount++;
            }
        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("testSequence2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
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
                Assert.assertEquals("GOOG", inEvents[0].getData(0));
                Assert.assertEquals("IBM", inEvents[0].getData(1));
                eventCount++;
            }


        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 57.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 65.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(1, eventCount);
    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("testSequence3  - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
                                QueryFactory.inputStream("e1", "Stream1").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))),
                                Sequence.zeroOrMany(QueryFactory.inputStream("e2", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.variable("e1", "price")))))));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol1", Expression.variable("e1", "symbol")).
                        select("symbol2", Expression.variable("e2", 0, "symbol")).
                        select("symbol3", Expression.variable("e2", 1, "symbol"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"WSO2", null, null}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{"IBM", null, null}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }

        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(2, eventCount);

    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("testSequence4  - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
                                Sequence.zeroOrMany(QueryFactory.inputStream("e1", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20)))),
                                QueryFactory.inputStream("e2", "Stream1").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.variable("e1", 0, "price")))
                        )));
        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", 0, "price")).
                        select("price2", Expression.variable("e1", 1, "price")).
                        select("price3", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{55.6f, 55.7f, 57.6f}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{55.7f, null, 57.6f}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }

        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events ", 2, eventCount);

    }

    @Test
    public void testQuery5() throws InterruptedException {
        log.info("testSequence5  - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
                                Sequence.zeroOrOne(QueryFactory.inputStream("e1", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20)))),
                                QueryFactory.inputStream("e2", "Stream1").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.variable("e1", 0, "price")))
                        )));
        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", 0, "price")).
                        select("price3", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{55.7f, 57.6f}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }

        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events ", 1, eventCount);

    }

    @Test
    public void testQuery6() throws InterruptedException {
        log.info("testSequence6  - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
                                QueryFactory.inputStream("e1", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))),
                                Sequence.or(
                                        QueryFactory.inputStream("e2", "Stream2").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.variable("e1", "price"))),
                                        QueryFactory.inputStream("e3", "Stream2").filter(
                                                Condition.compare(Expression.value("IBM"),
                                                                  Condition.Operator.EQUAL,
                                                                  Expression.variable("symbol")))))));
        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", "price")).
                        select("price2", Expression.variable("e2", "price")).
                        select("price3", Expression.variable("e3", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{55.6f, null, 55.7f}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{55.7f, 57.6f, null}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }


        });
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream2.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events ", 2, eventCount);

    }

    @Test
    public void testQuery7() throws InterruptedException {
        log.info("testSequence7  - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
                                QueryFactory.inputStream("e1", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))),
                                Sequence.or(
                                        QueryFactory.inputStream("e2", "Stream2").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.variable("e1", "price"))),
                                        QueryFactory.inputStream("e3", "Stream2").filter(
                                                Condition.compare(Expression.value("IBM"),
                                                                  Condition.Operator.EQUAL,
                                                                  Expression.variable("symbol")))))));
        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", "price")).
                        select("price2", Expression.variable("e2", "price")).
                        select("price3", Expression.variable("e3", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{55.6f, 57.6f, null}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{57.6f, null, 55.7f}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }


        });
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream2.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events ", 2, eventCount);

    }


    @Test
    public void testQuery8() throws InterruptedException {
        log.info("testSequence8  - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream2").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
                                Sequence.oneOrMany(QueryFactory.inputStream("e1", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20)))),
                                QueryFactory.inputStream("e2", "Stream1").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.variable("e1", 0, "price")))
                        )));
        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", 0, "price")).
                        select("price2", Expression.variable("e1", 1, "price")).
                        select("price3", Expression.variable("e2", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{55.6f, null, 57.6f}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }


        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events ", 1, eventCount);

    }

    @Test
    public void testQuery9() throws InterruptedException {
        log.info("testSequence9  - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("Stream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(
                                QueryFactory.inputStream("e1", "Stream1").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.value(20))),
                                Sequence.next(
                                        Sequence.oneOrMany(
                                                QueryFactory.inputStream("e2", "Stream1").filter(
                                                        Condition.compare(Expression.variable("price"),
                                                                          Condition.Operator.GREATER_THAN,
                                                                          Expression.variable("e2", SiddhiConstants.PREV, "price")))),
                                        QueryFactory.inputStream("e3", "Stream1").filter(
                                                Condition.compare(Expression.variable("price"),
                                                                  Condition.Operator.LESS_THAN_EQUAL,
                                                                  Expression.variable("e3", SiddhiConstants.PREV, "price")))
                                ))));
        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", "price")).
                        select("price2", Expression.variable("e2", 0, "price")).
                        select("price3", Expression.variable("e2", 1, "price")).
                        select("price4", Expression.variable("e3", "price"))

        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{29.6f, 35.6f, 57.6f, 47.6f}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }


        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"IBM", 47.6f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }

    @Test
    public void testQuery10() throws InterruptedException, SiddhiParserException {
        log.info("testSequence10  - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream twiterStream ( symbol string, count int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] , e2 = twiterStream [count > 10 ] " +
                                                       "select e1.price as price, e1.symbol as symbol, e2.count as count " +
                                                       "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{76.6f, "IBM", 20}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiManager.getInputHandler("twiterStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        cseStreamHandler.send(new Object[]{"GOOG", 51f, 101});
        cseStreamHandler.send(new Object[]{"IBM", 76.6f, 111});
        Thread.sleep(500);
        twitterStreamHandler.send(new Object[]{"IBM", 20});
        cseStreamHandler.send(new Object[]{"WSO2", 45.6f, 100});
        Thread.sleep(500);
        twitterStreamHandler.send(new Object[]{"GOOG", 20});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }


    @Test
    public void testQuery11() throws InterruptedException, SiddhiParserException {
        log.info("testSequence11  within test");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream twiterStream ( symbol string, count int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] , e2 = twiterStream [count > 10 ] " +
                                                       "within 1000 " +
                                                       "select e1.price as price, e1.symbol as symbol, e2.count as count " +
                                                       "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{76.6f, "WSO2", 24}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiManager.getInputHandler("twiterStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        twitterStreamHandler.send(new Object[]{"IBM", 20});
        cseStreamHandler.send(new Object[]{"GOOG", 51f, 101});
        cseStreamHandler.send(new Object[]{"WSO2", 76.6f, 111});
        Thread.sleep(500);
        twitterStreamHandler.send(new Object[]{"GOOG", 24});
        cseStreamHandler.send(new Object[]{"WSO2", 45.6f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }

    @Test
    public void testQuery12() throws InterruptedException, SiddhiParserException {
        log.info("testSequence12  OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] , e2 = cseEventStream [price <= 40 ] * , e3 = cseEventStream [volume <= 70 ] " +
                                                       "select e1.symbol as symbol1,e2[0].symbol as symbol2,e3.symbol as symbol3 " +
                                                       "insert into StockQuote;");
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
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }

    @Test
    public void testSimpleQueryForMemoryOptimization() throws InterruptedException, SiddhiParserException {
        log.info("testSimpleSequenceForMemoryOptimization  OUT 3");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream lseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] , e2 = lseEventStream [price <= 40 ] * , e3 = lseEventStream [volume <= 70 ] " +
                                                       "select e3.symbol as symbol1,e2[0].symbol as symbol2,e3.volume as symbol3 " +
                                                       "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{"WSO2", "DDD", 60}, inEvents[0].getData());
                } else if (eventCount == 2) {
                    Assert.assertArrayEquals(new Object[]{"DOX", null, 25}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler lseStreamHandler = siddhiManager.getInputHandler("lseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        lseStreamHandler.send(new Object[]{"GOOG", 21f, 81});
        lseStreamHandler.send(new Object[]{"WSO2", 176.6f, 65});
        cseStreamHandler.send(new Object[]{"BIRT", 21f, 81});
        cseStreamHandler.send(new Object[]{"AMBA", 126.6f, 165});
        lseStreamHandler.send(new Object[]{"DDD", 23f, 181});
        lseStreamHandler.send(new Object[]{"BIRT", 21f, 86});
        lseStreamHandler.send(new Object[]{"BIRT", 21f, 82});
        lseStreamHandler.send(new Object[]{"WSO2", 176.6f, 60});
        cseStreamHandler.send(new Object[]{"AMBA", 126.6f, 165});
        lseStreamHandler.send(new Object[]{"DOX", 16.2f, 25});

        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 3, eventCount);

    }

    @Test
    public void testAdvancedQueryForMemoryOptimization() throws InterruptedException, SiddhiParserException {
        log.info("testAdvancedSequenceForMemoryOptimization  OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream lseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] , e2 = lseEventStream [e1.symbol != 'AMBA' ] * , e3 = lseEventStream [volume <= 70 ] " +
                                                       "select e3.symbol as symbol1,e2[0].symbol as symbol2,e3.volume as symbol3 " +
                                                       "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{"DOX", null, 25}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler lseStreamHandler = siddhiManager.getInputHandler("lseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        lseStreamHandler.send(new Object[]{"GOOG", 21f, 81});
        lseStreamHandler.send(new Object[]{"WSO2", 176.6f, 65});
        cseStreamHandler.send(new Object[]{"BIRT", 21f, 81});
        cseStreamHandler.send(new Object[]{"AMBA", 126.6f, 165});
        lseStreamHandler.send(new Object[]{"DDD", 23f, 181});
        lseStreamHandler.send(new Object[]{"BIRT", 21f, 86});
        lseStreamHandler.send(new Object[]{"BIRT", 21f, 82});
        lseStreamHandler.send(new Object[]{"WSO2", 176.6f, 60});
        cseStreamHandler.send(new Object[]{"AMBA", 126.6f, 165});
        lseStreamHandler.send(new Object[]{"DOX", 16.2f, 25});

        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 2, eventCount);

    }

    @Test
    public void testAdvancedQueryForMemoryOptimization1() throws InterruptedException, SiddhiParserException {
        log.info("testAdvancedSequenceForMemoryOptimization1  OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream lseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream , e2 = lseEventStream [e1.symbol != 'AMBA' ] * , e3 = lseEventStream [volume <= 70 ] " +
                                                       "select e3.symbol as symbol1,e2[0].symbol as symbol2,e3.volume as symbol3 " +
                                                       "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{"DOX", null, 25}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler lseStreamHandler = siddhiManager.getInputHandler("lseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        lseStreamHandler.send(new Object[]{"GOOG", 21f, 81});
        lseStreamHandler.send(new Object[]{"WSO2", 176.6f, 65});
        cseStreamHandler.send(new Object[]{"BIRT", 21f, 81});
        cseStreamHandler.send(new Object[]{"AMBA", 126.6f, 165});
        lseStreamHandler.send(new Object[]{"DDD", 23f, 181});
        lseStreamHandler.send(new Object[]{"BIRT", 21f, 86});
        lseStreamHandler.send(new Object[]{"BIRT", 21f, 82});
        lseStreamHandler.send(new Object[]{"WSO2", 176.6f, 60});
        cseStreamHandler.send(new Object[]{"AMBA", 126.6f, 165});
        lseStreamHandler.send(new Object[]{"DOX", 16.2f, 25});

        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 2, eventCount);

    }

    @Test
    public void testTimeBatchAndSequence() throws Exception {
        log.info("testTimeBatchAndSequence  OUT 1");
        SiddhiManager siddhi = new SiddhiManager();
        InputHandler i1 = siddhi.defineStream("define stream received_reclamations (timestamp long, product_id string, defect_category string)");

        siddhi.addQuery("from received_reclamations#window.timeBatch(1 sec) " +
                "select product_id, defect_category, count(*) as num group by product_id, defect_category " +
                "insert into reclamation_averages");

        final CountDownLatch increaseEventReceived = new CountDownLatch(1);
        siddhi.addQuery("from a=reclamation_averages[num > 1], b=reclamation_averages[num > a.num and product_id == a.product_id and defect_category == a.defect_category] " +
                "select a.product_id, a.defect_category, a.num as oldNum, b.num as newNum " +
                "insert into increased_reclamations");
        siddhi.addCallback("increased_reclamations", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                increaseEventReceived.countDown();
            }
        });

        for (int i = 0; i < 5; i++) {
            i1.send(new Object[]{System.currentTimeMillis(), "abc", "123"});
            Thread.sleep(1);
        }

        Thread.sleep(1000);

        for (int i = 0; i < 8; i++) {
            i1.send(new Object[] { System.currentTimeMillis(), "abc", "123" });
            Thread.sleep(1);
        }

        assertTrue("Did not receive event in stream increased_reclamations", increaseEventReceived.await(1000, TimeUnit.MILLISECONDS));

        siddhi.shutdown();
    }

}
