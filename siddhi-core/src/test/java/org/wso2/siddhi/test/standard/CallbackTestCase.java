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

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.exception.QueryNotExistException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class CallbackTestCase {
    static final Logger log = Logger.getLogger(CallbackTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {

        log.info("StreamCallback test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query.insertInto("StockQuote");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count++;
            }
        });
//        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        Thread.sleep(500);
        siddhiManager.shutdown();
        Assert.assertEquals(2, count);

    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("QueryStreamCallback test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

        String queryReference = siddhiManager.addQuery("from cseStream [price>10]#window.length(3) " +
                                                       "select symbol, avg(price) as avgPrice, volume " +
                                                       "insert into outStream for all-events");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    count++;
                }
                eventArrived = true;
            }

        });

        siddhiManager.addCallback("outStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count++;
            }
        });

        InputHandler inputHandler = siddhiManager.getInputHandler("cseStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 27.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 127.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 150.6f, 100});
        Thread.sleep(1000);
        siddhiManager.shutdown();
        Thread.sleep(1000);

        Assert.assertEquals("Expected events 5 current and 2+2 expired", 9, count);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }

    @Test
    public void testQuery3() throws InterruptedException, SiddhiParserException {

        log.info("QueryCallback test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

        String queryReference = siddhiManager.addQuery("from cseStream[price>10] " +
                                                       "select symbol, price, volume " +
                                                       "insert into outStream ;");


        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
            }
        });

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        siddhiManager.removeQuery(queryReference);

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        queryReference = siddhiManager.addQuery("from cseStream[price>10] " +
                                                "select symbol, price, volume " +
                                                "insert into outStream ;");

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        siddhiManager.shutdown();

        Assert.assertEquals(2, count);

    }


    @Test(expected = QueryNotExistException.class)
    public void testQuery4() throws InterruptedException, SiddhiParserException {

        log.info("QueryNotExistException for Callback assignment test");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");


        siddhiManager.addCallback("test", new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
            }
        });

        String queryReference = siddhiManager.addQuery("from cseStream[price>10] " +
                                                       "select symbol, price, volume " +
                                                       "insert into outStream;");
        siddhiManager.shutdown();
    }

    @Test
    public void testQuery5() throws InterruptedException {

        log.info("QueryCallback test5");
        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query.returnStream();

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
            }
        });

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        Thread.sleep(500);
        siddhiManager.shutdown();
        Assert.assertEquals(2, count);

    }

    @Test
    public void testQuery6() throws InterruptedException {
        log.info("QueryCallback test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

        String queryReference = siddhiManager.addQuery("from cseStream [price>10]#window.length(3) " +
                                                        "select symbol, avg(price) as avgPrice, volume " +
                                                        "return");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    count+=removeEvents.length;
                }else {
                    count+=inEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiManager.getInputHandler("cseStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 27.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 127.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 150.6f, 100});
        Thread.sleep(1000);
        siddhiManager.shutdown();
        Thread.sleep(1000);

        Assert.assertEquals("Expected events 5 current and 2 expired", 7, count);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }


    @Test
    public void testQuery7() throws InterruptedException {
        log.info("QueryCallback test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

        String queryReference = siddhiManager.addQuery("from cseStream [price>10]#window.length(3) " +
                "select symbol, avg(price) as avgPrice, volume " +
                "return ");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    count+=removeEvents.length;
                }else {
                    count+=inEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiManager.getInputHandler("cseStream");
        inputHandler.send(System.currentTimeMillis(),new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(System.currentTimeMillis(),new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(System.currentTimeMillis(),new Object[]{"WSO2", 27.6f, 100});
        inputHandler.send(System.currentTimeMillis(),new Object[]{"WSO2", 127.6f, 100});
        inputHandler.send(System.currentTimeMillis(),new Object[]{"WSO2", 150.6f, 100});
        Thread.sleep(1000);
        siddhiManager.shutdown();
        Thread.sleep(1000);

        Assert.assertEquals("Expected events 5 current and 2 expired", 7, count);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }

    @Test
    public void testQuery8() throws InterruptedException {
        log.info("QueryCallback test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

        String queryReference = siddhiManager.addQuery("from cseStream [price>10]#window.length(2) " +
                "select symbol, avg(price) as avgPrice, volume " +
                "return ");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    count+=removeEvents.length;
                }else {
                    count+=inEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiManager.getInputHandler("cseStream");
        inputHandler.send(new InEvent(inputHandler.getStreamId(),System.currentTimeMillis(),new Object[]{"WSO2", 55.6f, 100}));
        inputHandler.send(new InEvent(inputHandler.getStreamId(),System.currentTimeMillis(),new Object[]{"IBM", 75.6f, 100}));
        inputHandler.send(new InEvent(inputHandler.getStreamId(),System.currentTimeMillis(),new Object[]{"WSO2", 27.6f, 100}));

        Thread.sleep(1000);
        siddhiManager.shutdown();
        Thread.sleep(1000);

        Assert.assertEquals("Expected events 3 current and 1 expired", 4, count);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }
}
