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
import org.wso2.siddhi.query.api.query.input.pattern.element.LogicalElement;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class PatternComplexTestCase {
    static final Logger log = Logger.getLogger(PatternComplexTestCase.class);
    private int eventCount;

    @Before
    public void inti() {
        eventCount = 0;
    }

    @Test
    public void testPatternComplexQuery1() throws InterruptedException {
        log.info("testPatternComplex1 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

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

                                                Pattern.logical(
                                                        QueryFactory.inputStream("e2", "Stream2").filter(
                                                                Condition.compare(Expression.variable("price"),
                                                                                  Condition.Operator.GREATER_THAN,
                                                                                  Expression.variable("e1", "price"))),
                                                        LogicalElement.Type.OR,
                                                        QueryFactory.inputStream("e3", "Stream2").filter(
                                                                Condition.compare(Expression.value("IBM"),
                                                                                  Condition.Operator.EQUAL,
                                                                                  Expression.variable("symbol")))))),
                                QueryFactory.inputStream("e4", "Stream2").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.variable("e1", "price"))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", "price")).
                        select("price2", Expression.variable("e2", "price")).
                        select("price3", Expression.variable("e3", "price")).
                        select("price4", Expression.variable("e4", "price"))

        );
        query.insertInto("OutStream");


        String queryReference= siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference,new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{55.6f, 55.7f, null, 57.7f},inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{54.0f, null, 57.7f, 59.7f}, inEvents[0].getData());
                } else {
                    Assert.fail("Count value exceeded!");
                }
                eventCount++;
            }

        });
        InputHandler stream1 = siddhiManager.getInputHandler("Stream1");
        InputHandler stream2 = siddhiManager.getInputHandler("Stream2");
        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"WSO2", 55.7f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"GOOG", 55f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 57.7f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 59.7f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals(2,eventCount);
    }

    @Test
    public void testPatternComplexQuery2() throws InterruptedException {
        log.info("testPatternComplex2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

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
                                                Pattern.count(
                                                        QueryFactory.inputStream("e2", "Stream1").filter(
                                                                Condition.compare(Expression.variable("price"),
                                                                                  Condition.Operator.GREATER_THAN,
                                                                                  Expression.value(20))), 1, 2))),
                                QueryFactory.inputStream("e3", "Stream1").filter(
                                        Condition.compare(Expression.variable("price"),
                                                          Condition.Operator.GREATER_THAN,
                                                          Expression.variable("e1", "price"))))));

        query.select(
                QueryFactory.outputSelector().
                        select("price1", Expression.variable("e1", "price")).
                        select("price2.0", Expression.variable("e2", 0, "price")).
                        select("price2.1", Expression.variable("e2", 1, "price")).
                        select("price3", Expression.variable("e3", "price"))

        );
        query.insertInto("OutStream");


        String queryReference= siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference,new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{55.6f, 54.0f, 53.6f, 57.0f},inEvents[0].getData());
                } else {
                    Assert.fail("Count value exceeded!");
                }
                eventCount++;
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
        stream1.send(new Object[]{"GOOG", 57f, 100});
        Thread.sleep(2000);

        siddhiManager.shutdown();

        Assert.assertEquals(1,eventCount);
    }

    @Test
    public void testPatternComplexQuery3() throws InterruptedException, SiddhiParserException {
        log.info("testPatternComplex3  OUT 3");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from every e1 = cseEventStream [ price >= 50 and volume > 100 ] -> e2 = cseEventStream [price <= 40 ] <2:> -> e3 = cseEventStream [volume <= 70 ] " +
                "select e1.symbol as symbol1,e2[last].symbol as symbol2,e3.symbol as symbol3 " +
                "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"IBM", "FB", "WSO2"}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{"ADP", "WSO2", "AMZN"}, inEvents[0].getData());
                } else if (eventCount == 2) {
                    Assert.assertArrayEquals(new Object[]{"WSO2", "QQQ", "CSCO"}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        cseStreamHandler.send(new Object[]{"GOOG", 39.8f, 91});
        cseStreamHandler.send(new Object[]{"FB", 35f, 81});
        cseStreamHandler.send(new Object[]{"WSO2", 21f, 61});
        cseStreamHandler.send(new Object[]{"ADP", 50f, 101});
        cseStreamHandler.send(new Object[]{"GOOG", 41.2f, 90});
        cseStreamHandler.send(new Object[]{"FB", 40f, 100});
        cseStreamHandler.send(new Object[]{"WSO2", 33.6f, 85});
        cseStreamHandler.send(new Object[]{"AMZN", 23.5f, 55});
        cseStreamHandler.send(new Object[]{"WSO2", 51.7f, 180});
        cseStreamHandler.send(new Object[]{"TXN", 34f, 61});
        cseStreamHandler.send(new Object[]{"QQQ", 24.6f, 45});
        cseStreamHandler.send(new Object[]{"CSCO", 181.6f, 40});
        cseStreamHandler.send(new Object[]{"WSO2", 53.7f, 200});
        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 3, eventCount);

    }

    @Test
    public void testSimpleQueryForMemoryOptimization() throws InterruptedException, SiddhiParserException {
        log.info("testSimpleSequenceForMemoryOptimization  OUT 3");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream lseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from every e1 = cseEventStream [ price >= 50 and volume > 100 ] -> e2 = lseEventStream [price <= 40 ] <1:> -> e3 = lseEventStream [volume <= 70 ] " +
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
    public void testAdvancedQueryForMemoryOptimization() throws InterruptedException, SiddhiParserException {
        log.info("testAdvancedSequenceForMemoryOptimization  OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream lseEventStream ( symbol string, price float, volume int )");
        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] -> e2 = lseEventStream [e1.symbol != 'AMBA' ] -> e3 = lseEventStream [volume <= 70 ] " +
                "select e3.symbol as symbol1,e2[0].symbol as symbol2,e3.volume as symbol3 " +
                "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, inEvents[0].getData());
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

        Assert.assertEquals("Number of success events", 1, eventCount);

    }


    @Test
    public void testAdvancedQueryForMemoryOptimization1() throws InterruptedException, SiddhiParserException {
        log.info("testAdvancedSequenceForMemoryOptimization1  OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream lseEventStream ( symbol string, price float, volume int )");
//        String queryReference = siddhiManager.addQuery("from every e1 = cseEventStream -> e2 = lseEventStream [e1.symbol != 'AMBA' ] <2:> -> e3 = lseEventStream [volume <= 70 ] " +
        String queryReference = siddhiManager.addQuery("from every e1 = cseEventStream -> e2 = lseEventStream [e1.symbol != 'AMBA' ] <2:> -> e3 = lseEventStream [volume <= 70 ] " +
                "select e3.symbol as symbol1,e2[0].symbol as symbol2,e3.volume as symbol3 " +
                "insert into StockQuote;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{"IBN", "DDD", 70}, inEvents[0].getData());
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
        lseStreamHandler.send(new Object[]{"GOOG", 21f, 51});
        lseStreamHandler.send(new Object[]{"FBX", 21f, 81});
        lseStreamHandler.send(new Object[]{"WSO2", 176.6f, 65});
        cseStreamHandler.send(new Object[]{"BIRT", 21f, 81});
        cseStreamHandler.send(new Object[]{"AMBA", 126.6f, 165});
        lseStreamHandler.send(new Object[]{"DDD", 23f, 181});
        lseStreamHandler.send(new Object[]{"BIRT", 21f, 86});
        lseStreamHandler.send(new Object[]{"IBN", 21f, 70});
        lseStreamHandler.send(new Object[]{"WSO2", 176.6f, 90});
        cseStreamHandler.send(new Object[]{"AMBA", 126.6f, 165});
        lseStreamHandler.send(new Object[]{"DOX", 16.2f, 25});

        Thread.sleep(1000);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 2, eventCount);

    }


}
