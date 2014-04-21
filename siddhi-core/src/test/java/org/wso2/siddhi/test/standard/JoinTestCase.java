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
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.MalformedAttributeException;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.api.query.output.stream.OutStream;

public class JoinTestCase {
    static final Logger log = Logger.getLogger(JoinTestCase.class);
    private int eventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        eventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testJoinQuery1() throws InterruptedException {
        log.info("Join test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("cseEventStream").
                                window("time", Expression.value(1000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("twitterStream").
                                window("time", Expression.value(1000)),
                        Condition.compare(Expression.variable("cseEventStream", "symbol"),
                                          Condition.Operator.EQUAL,
                                          Expression.variable("twitterStream", "symbol"))


                )
        );
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("cseEventStream", "symbol")).
                        select("tweet", Expression.variable("twitterStream", "tweet")).
                        select("price", Expression.variable("cseEventStream", "price"))
        );
        query.insertInto("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS);


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    eventCount++;
                } else {
                    Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                    eventCount--;
                }
                eventArrived = true;
            }

        });
        InputHandler cseEventStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiManager.getInputHandler("twitterStream");
        cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
//        Thread.sleep(70000);
        cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(2000);

        Assert.assertEquals("Number of success events", 0, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testJoinQuery2() throws InterruptedException {
        log.info("Join test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("a", "cseEventStream").
                                window("time", Expression.value(500)),
                        JoinStream.Type.JOIN,
                        QueryFactory.inputStream("b", "cseEventStream").
                                window("time", Expression.value(500)),
                        Condition.compare(Expression.variable("a", "symbol"),
                                          Condition.Operator.EQUAL,
                                          Expression.variable("b", "symbol"))


                )
        );
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("a", "symbol")).
                        select("priceA", Expression.variable("a", "price")).
                        select("priceB", Expression.variable("b", "price"))
        );
        query.insertInto("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS);


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    eventCount++;
                } else {
                    Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                    eventCount--;
                }
                eventArrived = true;
            }

        });
        InputHandler cseEventStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(2500);
        siddhiManager.shutdown();
        Assert.assertEquals("Number of success events", 0, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testJoinQuery3() throws InterruptedException {
        log.info("Join test3 with within");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("cseEventStream").
                                window("time",Expression.value( 2000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("twitterStream").
                                window("time", Expression.value(2000)),
                        Condition.compare(Expression.variable("cseEventStream", "symbol"),
                                          Condition.Operator.EQUAL,
                                          Expression.variable("twitterStream", "symbol"))
                        ,
                        Expression.value(1000)
                )
        );
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("cseEventStream", "symbol")).
                        select("tweet", Expression.variable("twitterStream", "tweet")).
                        select("price", Expression.variable("cseEventStream", "price"))
        );
        query.insertInto("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS);


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    eventCount += 2;
                } else {
                    Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                    eventCount--;
                }
                eventArrived = true;
            }

        });
        InputHandler cseEventStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiManager.getInputHandler("twitterStream");
        cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(1300);
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(4000);

        Assert.assertEquals("Number of success events", 1, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testJoinQuery4() throws InterruptedException {
        log.info("Join test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));


        String queryReference = siddhiManager.addQuery("from cseEventStream join twitterStream " +
                                                       "select cseEventStream.symbol,cseEventStream.volume as volume,user " +
                                                       "insert into joinOutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
            }

        });
        InputHandler cseEventStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiManager.getInputHandler("twitterStream");
        cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(1300);
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(4000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }


    @Test(expected = MalformedAttributeException.class)
    public void testJoinQuery5() throws InterruptedException {
        log.info("Join test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));


        String queryReference = siddhiManager.addQuery("from cseEventStream join twitterStream " +
                                                       "select symbol,cseEventStream.volume as volume ,user " +
                                                       "insert into joinOutputStream;");
        siddhiManager.shutdown();

    }

    @Test
    public void testJoinQuery6() throws InterruptedException {
        log.info("Join test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));


        String queryReference = siddhiManager.addQuery("from cseEventStream as eStream join twitterStream as twitter " +
                                                       "select eStream.symbol,eStream.volume as volume,user " +
                                                       "insert into joinOutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
            }

        });
        InputHandler cseEventStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiManager.getInputHandler("twitterStream");
        cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(1300);
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(4000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }
}
