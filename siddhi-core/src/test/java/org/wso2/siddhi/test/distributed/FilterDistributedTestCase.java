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

public class FilterDistributedTestCase {
    static final Logger log = Logger.getLogger(FilterDistributedTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testFilterDistributedQuery1() throws InterruptedException {
        log.info("FilterDistributed test1");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

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

            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    count+=inEvents.length;
                }
            });
//        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
            inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
            Thread.sleep(500);
            Assert.assertEquals(2, count);
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testFilterDistributedQuery2() throws InterruptedException {
        log.info("FilterDistributed test2");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(QueryFactory.inputStream("cseEventStream"));
            query.insertInto("StockQuote");

            String queryReference = siddhiManager.addQuery(query);

            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    count+=inEvents.length;
                }

            });
            InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
            inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
            Thread.sleep(500);
            Assert.assertEquals(2, count);
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testFilterDistributedQuery3() throws InterruptedException {
        log.info("FilterDistributed test3");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(QueryFactory.inputStream("cseEventStream"));
            query.select(
                    QueryFactory.outputSelector().
                            select("symbol", Expression.variable("symbol"))
            );
            query.insertInto("StockQuote");

            String queryReference = siddhiManager.addQuery(query);

            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    count+=inEvents.length;
                }

            });
            InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
            inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
            Thread.sleep(500);
            Assert.assertEquals(2, count);
        } finally {
            siddhiManager.shutdown();
        }

    }

    @Test
    public void testFilterDistributedQuery4() throws InterruptedException {
        log.info("FilterDistributed test4");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(QueryFactory.inputStream("cseEventStream").
                    filter(Condition.compare(Expression.value(70),
                                              Condition.Operator.GREATER_THAN,
                                              Expression.variable("price"))
                    )
            );
            query.select(
                    QueryFactory.outputSelector().
                            select("symbol", Expression.variable("symbol")).
                            select("price", Expression.variable("price"))
            );
            query.insertInto("StockQuote");

            String queryReference = siddhiManager.addQuery(query);

            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    Assert.assertTrue("WSO2".equals(inEvents[0].getData(0)));
                    count+=inEvents.length;
                }

            });
            InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
            inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
            inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(500);
            Assert.assertEquals(2, count);
        } finally {
            siddhiManager.shutdown();
        }

    }


    @Test
    public void testFilterDistributedQuery5() throws InterruptedException {
        log.info("FilterDistributed test5");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        try {

            siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

            Query query = QueryFactory.createQuery();
            query.from(QueryFactory.inputStream("cseEventStream"));
            query.select(
                    QueryFactory.outputSelector().
                            select("symbol", Expression.variable("symbol")).
                            select("price", Expression.variable("price")).
                            select("volume", Expression.variable("volume")).groupBy("symbol")
            );
            query.insertInto("StockQuote");

            String queryReference = siddhiManager.addQuery(query);

            siddhiManager.addCallback(queryReference, new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                    count+=inEvents.length;
                    eventArrived = true;
                }
            });
            InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
            inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
            Thread.sleep(500);
            Assert.assertEquals(2, count);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiManager.shutdown();
        }

    }

}
