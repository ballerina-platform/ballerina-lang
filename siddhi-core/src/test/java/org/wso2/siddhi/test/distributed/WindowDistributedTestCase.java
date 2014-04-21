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
package org.wso2.siddhi.test.distributed;

import junit.framework.Assert;
import org.apache.log4j.Logger;
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
import org.wso2.siddhi.query.api.query.output.stream.OutStream;

public class WindowDistributedTestCase {
    static final Logger log = Logger.getLogger(WindowDistributedTestCase.class);

    private int count;
    private int count1;
    private boolean eventArrived;
    private Long lastValue;

    @Before
    public void init() {
        count = 0;
        count1 = 0;
        eventArrived = false;
        lastValue = -1l;
    }

    @Test
    public void testWindowDistributedQuery1() throws InterruptedException {
        log.info("WindowDistributed test1");


        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.inputStream("cseEventStream").
                            window("time", Expression.value(5000))//.
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
                        count += inEvents.length;
                    } else {
                        Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                        count -= removeEvents.length;
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
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testWindowDistributedQuery2() throws InterruptedException {
        log.info("WindowDistributed test2");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        try {
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.inputStream("cseEventStream").
                            window("time", Expression.value(3000))//.
            );
            query.select(
                    QueryFactory.outputSelector().
                            select("symbol", Expression.variable("symbol")).
                            select("price", "sum", Expression.variable("price")).
                            select("count", "count", Expression.variable("price"))

            );
            query.insertInto("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS);


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                        count += inEvents.length;
                        lastValue = (Long) inEvents[inEvents.length - 1].getData2();
                    } else {
                        Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                        count1 += removeEvents.length;
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
            Assert.assertTrue(count > 0);
            Assert.assertTrue(count1 > 0);
            Assert.assertEquals("total count in query", new Long(3), lastValue);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testWindowDistributedQuery3() throws InterruptedException {
        log.info("WindowDistributed test3");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.inputStream("cseEventStream").
                            window("timeBatch", Expression.value(3000))//.
            );
            query.select(
                    QueryFactory.outputSelector().
                            select("symbol", Expression.variable("symbol")).
                            select("price", "sum", Expression.variable("price")).
                            select("count", "count", Expression.variable("price"))
            );
            query.insertInto("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS);


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                        count += inEvents.length;
                    } else {
                        Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                        count -= removeEvents.length;
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
            Assert.assertEquals("In and Remove event diff", 0, count);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiManager.shutdown();
        }
    }

    @Test
    public void testWindowDistributedQuery4() throws InterruptedException {
        log.info("WindowDistributed test4");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.inputStream("cseEventStream").
                            window("time", Expression.value(5000))//.
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
                        count += inEvents.length;
                    } else {
                        Assert.assertTrue("IBM".equals(removeEvents[0].getData(0)) || "WSO2".equals(removeEvents[0].getData(0)));
                        if ((new Double(0)).equals(removeEvents[0].getData(1))) {
                            count -= removeEvents.length;
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
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testWindowDistributedQuery5() throws InterruptedException {
        log.info("WindowDistributed test5");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.inputStream("cseEventStream").
                            window("time", Expression.value(5000))//.
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
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testWindowDistributedQuery6() throws InterruptedException {
        log.info("WindowDistributed test6");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {
            siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

            String queryReference = siddhiManager.addQuery("from cseStream [price>10]#window.length(3) " +
                                                           "select symbol, avg(price) as avgPrice, price " +
                                                           "insert into outStream for all-events");
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (removeEvents != null) {
                        count += removeEvents.length;
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
            Thread.sleep(7000);
            Assert.assertEquals("Expected remove events ", 2, count);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testWindowDistributedQuery7() throws InterruptedException {
        log.info("WindowDistributed test7");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.inputStream("cseEventStream").
                            window("time", Expression.value(5000))//.
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
                        count += inEvents.length;
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
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testWindowDistributedQuery8() throws InterruptedException {
        log.info("WindowDistributed test8");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {
            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(
                    QueryFactory.inputStream("cseEventStream").
                            window("time", Expression.value(5000))//.
            );
            query.select(
                    QueryFactory.outputSelector().
                            select("symbol", Expression.variable("symbol")).
                            select("price", "sum", Expression.variable("price")).
                            groupBy("symbol")
            );
            query.insertInto("StockQuote", OutStream.OutputEventsFor.EXPIRED_EVENTS);


            String queryReference = siddhiManager.addQuery(query);
            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        Assert.fail("no current  events are expected ");
                    } else {
                        count -= removeEvents.length;
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
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testWindowDistributedQuery9() throws InterruptedException {
        log.info("WindowDistributed test9");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.length(3) " +
                                                       "select  ip, count(ip) as ipCount " +
                                                       "insert into LoginEventsByHourAndIP;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Event[] events;
                if (inEvents != null) {
                    events = inEvents;
                } else {
                    events = removeEvents;
                }
                for (Event event : events) {
                    if (lastValue < (Long) event.getData1()) {
                        lastValue = (Long) event.getData1();
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        for (int i = 0; i < 3; i++) {
            loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
            loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        }

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Event max value", new Long(3l), lastValue);
        siddhiManager.shutdown();
    }

    @Test
    public void testWindowDistributedQuery10() throws InterruptedException {
        log.info("WindowDistributed test10");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.length(5) " +
                                                       "select ip, count(ip) as ipCount " +
                                                       "insert into LoginEventsByHourAndIP;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Event[] events;
                if (inEvents != null) {
                    events = inEvents;
                } else {
                    events = removeEvents;
                }
                for (Event event : events) {
                    if (lastValue < (Long) event.getData1()) {
                        lastValue = (Long) event.getData1();
                    }
                }
                eventArrived = true;
            }
        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1000);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Event max value", new Long(5l), lastValue);
        siddhiManager.shutdown();
    }

    @Test
    public void testWindowDistributedQuery12() throws InterruptedException {
        log.info("WindowDistributed test12");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.unique(ip) " +
                                                       "select count(ip) as ipCount, ip " +
                                                       "insert into uniqueIps ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    lastValue = (Long) inEvents[inEvents.length - 1].getData0();
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1000);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Unique event value", Long.valueOf(3), lastValue);
        siddhiManager.shutdown();
    }

    @Test
    public void testWindowDistributedQuery13() throws InterruptedException {
        log.info("WindowDistributed test13");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.firstUnique(ip) " +
                                                       "select  ip " +
                                                       "insert into uniqueIps for all-events;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1000);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 3, count);
        siddhiManager.shutdown();
    }
}