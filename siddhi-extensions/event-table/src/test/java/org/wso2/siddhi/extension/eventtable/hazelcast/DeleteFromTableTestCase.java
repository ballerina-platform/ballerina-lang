/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.eventtable.hazelcast;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.eventtable.test.util.SiddhiTestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DeleteFromTableTestCase {
    private static final Logger log = Logger.getLogger(DeleteFromTableTestCase.class);
    private static final long RESULT_WAIT = 500;
    private AtomicInteger inEventCount = new AtomicInteger(0);
    private int removeEventCount;
    private boolean eventArrived;
    private List<Object[]> inEventsList;

    @Before
    public void init() {
        inEventCount.set(0);
        removeEventCount = 0;
        eventArrived = false;
        inEventsList = new ArrayList<Object[]>();
    }

    @Test
    public void deleteFromTableTest1() throws InterruptedException {
        log.info("deleteFromTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long); " +
                "@from(eventtable = 'hazelcast', instance.name = 'siddhi_instance')" +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on symbol=='IBM' ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            deleteStockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void deleteFromTableTest2() throws InterruptedException {
        log.info("deleteFromTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long); " +
                "@from(eventtable = 'hazelcast', instance.name = 'siddhi_instance')" +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.symbol=='IBM' ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            deleteStockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void deleteFromTableTest3() throws InterruptedException {
        log.info("deleteFromTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long); " +
                "@from(eventtable = 'hazelcast', instance.name = 'siddhi_instance')" +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on symbol=='IBM' ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void deleteFromTableTest4() throws InterruptedException {
        log.info("deleteFromTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "define stream DeleteStockStream (symbol string, price float, volume long); " +
                "@from(eventtable = 'hazelcast', instance.name = 'siddhi_instance')" +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.symbol=='IBM';" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[symbol==StockTable.symbol in StockTable] " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount = removeEventCount + removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");
            InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 100l});
            checkStockStream.send(new Object[]{"IBM"});
            checkStockStream.send(new Object[]{"WSO2"});
            deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});
            checkStockStream.send(new Object[]{"IBM"});
            checkStockStream.send(new Object[]{"WSO2"});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM"},
                    new Object[]{"WSO2"},
                    new Object[]{"WSO2"}
            );
            SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 3, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void deleteFromTableTest5() throws InterruptedException {
        log.info("deleteFromTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, vol long); " +
                "define stream DeleteStockStream (symbol string, price float, vol long); " +
                "define stream CountStockStream (symbol string); " +
                "@from(eventtable = 'hazelcast', instance.name = 'siddhi_instance')" +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "select symbol, price, vol as volume " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream[vol>=100] " +
                "delete StockTable " +
                "   on StockTable.symbol==symbol ;" +
                "" +
                "@info(name = 'query3') " +
                "from CountStockStream#window.length(0) join StockTable" +
                " on CountStockStream.symbol==StockTable.symbol " +
                "select CountStockStream.symbol as symbol " +
                "insert into CountResultsStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");
            InputHandler countStockStream = executionPlanRuntime.getInputHandler("CountStockStream");

            executionPlanRuntime.addCallback("CountResultsStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    inEventCount.addAndGet(events.length);
                }
            });
            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});
            countStockStream.send(new Object[]{"WSO2"});

            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals(2, inEventCount.get());
        } finally {
            executionPlanRuntime.shutdown();
        }
    }
}
