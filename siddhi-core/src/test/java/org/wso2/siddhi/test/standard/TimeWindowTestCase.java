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
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Time;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.output.stream.OutStream;

public class TimeWindowTestCase {
    static final Logger log = Logger.getLogger(TimeWindowTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testTimeWindowQuery1() throws InterruptedException {
        log.info("Time Window test1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        window("time", Expression.value(Time.milliSec(5000).value()))
        );
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price"))
        );
        query.insertInto("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS);


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    count++;
                } else {
                    Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                    count--;
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(6000);
        Assert.assertEquals("In and Remove events has to be equal", 0, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testTimeWindowQuery2() throws InterruptedException {
        log.info("Time Window test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        window("time", Expression.value(Time.milliSec(5000).value()))//.
        );
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", "sum", Expression.variable("price")).
                        groupBy("symbol")
        );
        query.insertInto("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS);


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    count++;
                } else {
                    Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                    if ((new Double(0)).equals(removeEvents[0].getData(1))) {
                        count--;
                    }
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(7000);
        Assert.assertEquals("Remove events has two 0.0s out of three total Remove events", 3 - 2, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testTimeWindowQuery3() throws InterruptedException {
        log.info("Time Window test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        window("time", Expression.value(Time.sec(5).value()))
        );
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("totalPrice", "sum", Expression.variable("price")).
                        groupBy("symbol").
                        having(Condition.compare(Expression.variable("totalPrice"),
                                                 Condition.Operator.GREATER_THAN,
                                                 Expression.value(100.0)))
        );
        query.insertInto("StockQuote");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.assertTrue("WSO2".equals(inEvents[0].getData(0)));
                    count++;
                } else {
                    Assert.fail("No remove events expected");
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(7000);
        Assert.assertEquals("Expected events after having clause", 1, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testTimeWindowQuery4() throws InterruptedException {
        log.info("Time Window test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        window("time", Expression.value(Time.sec(10).milliSec(20).value()))
        );
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", "sum", Expression.variable("price")).
                        groupBy("symbol")
        );
        query.insertInto("StockQuote");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    count++;
                } else {
                    Assert.fail("no expired events are expected ");
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(7000);
        Assert.assertEquals("No expired events are expected only 3 current event", 3, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testTimeWindowQuery5() throws InterruptedException {
        log.info("Time Window test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream#window.time(1 sec) " +
                                                       "select symbol , sum(price) as sumPrice " +
                                                       "group by symbol " +
                                                       "insert  into StockQuote for expired-events;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.fail("no current  events are expected ");
                } else {
                    count--;
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(7000);
        Assert.assertEquals("No current events are expected only 3 expired event", -3, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testTimeWindowQuery6() throws InterruptedException {
        log.info("Time Window test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream#window.time(5 seconds) " +
                                                       "select  symbol , sum(volume) as sumVolume, avg(volume) as avgVolume " +
                                                       "group by symbol " +
                                                       "insert into StockQuote for all-events;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 75.6f, 300l});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 200l});
        Thread.sleep(7000);
        Assert.assertEquals("Expected total event", 6, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }

    @Test
    public void testTimeWindowQuery7() throws InterruptedException {
        log.info("Time Window test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream#window.time(292471208 years) " +
                                                       "select symbol , sum(price) as sumPrice " +
                                                       "group by symbol " +
                                                       "insert into StockQuote for expired-events;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    Assert.fail("no current  events are expected ");
                } else {
                    count--;
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(7000);
        Assert.assertEquals("No current events and expired event are expected ", 0, count);
        Assert.assertEquals("No Event arrived", false, eventArrived);
        siddhiManager.shutdown();
    }
}
