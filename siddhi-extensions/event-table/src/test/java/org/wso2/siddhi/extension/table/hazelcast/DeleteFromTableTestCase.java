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

package org.wso2.siddhi.extension.table.hazelcast;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.table.test.util.SiddhiTestHelper;

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
                "@app:name('DeleteFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT011 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT011 ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTableT011 " +
                "   on symbol=='IBM' ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = siddhiAppRuntime.getInputHandler("DeleteStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            deleteStockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void deleteFromTableTest2() throws InterruptedException {
        log.info("deleteFromTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('DeleteFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT021 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT021 ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTableT021 " +
                "   on StockTableT021.symbol=='IBM' ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = siddhiAppRuntime.getInputHandler("DeleteStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            deleteStockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void deleteFromTableTest3() throws InterruptedException {
        log.info("deleteFromTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "@app:name('DeleteFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT031 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT031 ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTableT031 " +
                "   on symbol=='IBM' ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = siddhiAppRuntime.getInputHandler("DeleteStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void deleteFromTableTest4() throws InterruptedException {
        log.info("deleteFromTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('DeleteFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "define stream DeleteStockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT041 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT041 ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTableT041 " +
                "   on StockTableT041.symbol=='IBM';" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[symbol==StockTableT041.symbol in StockTableT041] " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timestamp, inEvents, removeEvents);
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

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");
            InputHandler deleteStockStream = siddhiAppRuntime.getInputHandler("DeleteStockStream");

            siddhiAppRuntime.start();
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
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void deleteFromTableTest5() throws InterruptedException {
        log.info("deleteFromTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('DeleteFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, vol long); " +
                "define stream DeleteStockStream (symbol string, price float, vol long); " +
                "define stream CountStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT051 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "select symbol, price, vol as volume " +
                "insert into StockTableT051 ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream[vol>=100] " +
                "delete StockTableT051 " +
                "   on StockTableT051.symbol==symbol ;" +
                "" +
                "@info(name = 'query3') " +
                "from CountStockStream#window.length(0) join StockTableT051" +
                " on CountStockStream.symbol==StockTableT051.symbol " +
                "select CountStockStream.symbol as symbol " +
                "insert into CountResultsStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = siddhiAppRuntime.getInputHandler("DeleteStockStream");
            InputHandler countStockStream = siddhiAppRuntime.getInputHandler("CountStockStream");

            siddhiAppRuntime.addCallback("CountResultsStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    EventPrinter.print(events);
                    inEventCount.addAndGet(events.length);
                }
            });
            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});
            countStockStream.send(new Object[]{"WSO2"});

            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals(2, inEventCount.get());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
