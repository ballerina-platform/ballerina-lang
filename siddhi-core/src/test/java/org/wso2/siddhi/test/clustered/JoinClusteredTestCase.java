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
package org.wso2.siddhi.test.clustered;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
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
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.api.query.output.stream.OutStream;

public class JoinClusteredTestCase {
    static final Logger log = Logger.getLogger(JoinClusteredTestCase.class);
    private int eventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        eventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("JoinClustered test1");

        String siddhiPlanId = "siddhi1-cluster-" + System.currentTimeMillis();

        SiddhiManager siddhiManager1 = createInstanceQuery1(siddhiPlanId);
        InputHandler cseEventStreamHandler = siddhiManager1.getInputHandler("cseEventStream");
        Thread.sleep(1000);
        SiddhiManager siddhiManager2 = createInstanceQuery1(siddhiPlanId);
        InputHandler twitterStreamHandler = siddhiManager2.getInputHandler("twitterStream");
        try {


            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
            Thread.sleep(500);
            twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
            Thread.sleep(500);
            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
            Thread.sleep(500);
            cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(6000);

        } finally {
            siddhiManager1.shutdown();
            siddhiManager2.shutdown();
        }

        Assert.assertEquals("Number of success events", 0, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    private SiddhiManager createInstanceQuery1(String planId) {

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        configuration.setQueryPlanIdentifier(planId);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("cseEventStream").
                                window("time", Expression.value(5000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("twitterStream").
                                window("time", Expression.value(5000)),
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
        return siddhiManager;
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("JoinClustered test2");

        String siddhiPlanId = "siddhi2-cluster-" + System.currentTimeMillis();

        SiddhiManager siddhiManager1 = createInstanceQuery2(siddhiPlanId, true);
        InputHandler cseEventStreamHandler1 = siddhiManager1.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler1 = siddhiManager1.getInputHandler("twitterStream");

        SiddhiManager siddhiManager2 = createInstanceQuery2(siddhiPlanId, true);
        InputHandler cseEventStreamHandler2 = siddhiManager2.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler2 = siddhiManager2.getInputHandler("twitterStream");

        try {
            cseEventStreamHandler1.send(new Object[]{"WSO2", 55.6f, 100});
            Thread.sleep(500);
            twitterStreamHandler2.send(new Object[]{"User1", "Hello World", "WSO2"});
            Thread.sleep(500);
            cseEventStreamHandler1.send(new Object[]{"IBM", 75.6f, 100});
            Thread.sleep(500);
            cseEventStreamHandler2.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(6000);

        } finally {
            siddhiManager1.shutdown();
            siddhiManager2.shutdown();
        }


        Assert.assertEquals("Number of events", 0, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    private SiddhiManager createInstanceQuery2(String planId, boolean addCallback) {

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        configuration.setQueryPlanIdentifier(planId);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("cseEventStream").
                                window("time", Expression.value(5000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("twitterStream").
                                window("time", Expression.value(5000)),
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
        if (addCallback) {
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
        }
        return siddhiManager;
    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("JoinClustered test3");

        String siddhiPlanId = "siddhi3-cluster-" + System.currentTimeMillis();

        SiddhiManager siddhiManager1 = createInstanceQuery3(siddhiPlanId);
        InputHandler cseEventStreamHandler1 = siddhiManager1.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler1 = siddhiManager1.getInputHandler("twitterStream");

        SiddhiManager siddhiManager2 = createInstanceQuery3(siddhiPlanId);
        InputHandler cseEventStreamHandler2 = siddhiManager2.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler2 = siddhiManager2.getInputHandler("twitterStream");
        try {

            cseEventStreamHandler1.send(new Object[]{"WSO2", 55.6f, 100});
            Thread.sleep(500);
            twitterStreamHandler2.send(new Object[]{"User1", "Hello World", "WSO2"});
            Thread.sleep(500);
            cseEventStreamHandler1.send(new Object[]{"IBM", 75.6f, 100});
            Thread.sleep(500);
            cseEventStreamHandler2.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(6000);

        } finally {
            siddhiManager1.shutdown();
            siddhiManager2.shutdown();
        }


        Assert.assertEquals("Number of events", 4, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    private SiddhiManager createInstanceQuery3(String planId) {

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        configuration.setQueryPlanIdentifier(planId);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("cseEventStream").
                                window("time", Expression.value(5000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("twitterStream").
                                window("time", Expression.value(5000)),
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
        siddhiManager.addCallback("StockQuote", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                eventCount++;
                eventArrived = true;
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        return siddhiManager;
    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("JoinClustered test4");

        String siddhiPlanId = "siddhi4-cluster-" + System.currentTimeMillis();

        SiddhiManager siddhiManager1 = createInstanceQuery4("siddhi1", siddhiPlanId);
        InputHandler cseEventStreamHandler1 = siddhiManager1.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler1 = siddhiManager1.getInputHandler("twitterStream");

        SiddhiManager siddhiManager2 = createInstanceQuery4("siddhi2", siddhiPlanId);
        InputHandler cseEventStreamHandler2 = siddhiManager2.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler2 = siddhiManager2.getInputHandler("twitterStream");

        cseEventStreamHandler1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        twitterStreamHandler2.send(new Object[]{"User1", "Hello World", "WSO2"});
        Thread.sleep(500);
        cseEventStreamHandler1.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);

        siddhiManager1.shutdown();
        Thread.sleep(500);

        cseEventStreamHandler2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(6000);

        siddhiManager2.shutdown();

        Assert.assertEquals("Number of events", 4, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    private SiddhiManager createInstanceQuery4(String instanceId, String queryPlanIdentifier) {

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        configuration.setInstanceIdentifier(instanceId);
        configuration.setQueryPlanIdentifier(queryPlanIdentifier);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("cseEventStream").
                                window("time", Expression.value(5000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("twitterStream").
                                window("time", Expression.value(5000)),
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
        siddhiManager.addCallback("StockQuote", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                eventCount++;
                eventArrived = true;
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        return siddhiManager;
    }


    @Test
    public void testQuery5() throws InterruptedException {
        log.info("JoinClustered test5");

        String siddhiPlanId = "siddhi5-cluster-" + System.currentTimeMillis();

        SiddhiManager siddhiManager1 = createInstanceQuery5("siddhi1", siddhiPlanId);
        InputHandler cseEventStreamHandler1 = siddhiManager1.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler1 = siddhiManager1.getInputHandler("twitterStream");

        SiddhiManager siddhiManager2 = createInstanceQuery5("siddhi2", siddhiPlanId);
        InputHandler cseEventStreamHandler2 = siddhiManager2.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler2 = siddhiManager2.getInputHandler("twitterStream");


        cseEventStreamHandler1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(500);
        twitterStreamHandler2.send(new Object[]{"User1", "Hello World", "WSO2"});
        Thread.sleep(500);
        cseEventStreamHandler1.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);

        siddhiManager1.shutdown();
        Thread.sleep(500);

        cseEventStreamHandler2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(6000);

        siddhiManager2.shutdown();

        Assert.assertEquals("Number of events", 0, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
    }

    private SiddhiManager createInstanceQuery5(String instanceId, String queryPlanId) {

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        configuration.setInstanceIdentifier(instanceId);
        configuration.setQueryPlanIdentifier(queryPlanId);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("twitterStream").attribute("user", Attribute.Type.STRING).attribute("tweet", Attribute.Type.STRING).attribute("symbol", Attribute.Type.STRING));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("cseEventStream").
                                window("time", Expression.value(5000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("twitterStream").
                                window("time", Expression.value(5000)),
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
        return siddhiManager;
    }
}
