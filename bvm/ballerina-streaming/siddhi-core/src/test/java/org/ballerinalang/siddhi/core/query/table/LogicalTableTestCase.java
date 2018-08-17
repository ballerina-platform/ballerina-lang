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

package org.ballerinalang.siddhi.core.query.table;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testcase for table joins with logical conditions.
 */

public class LogicalTableTestCase {
    private static final Logger log = LoggerFactory.getLogger(LogicalTableTestCase.class);
    private AtomicInteger inEventCount = new AtomicInteger(0);
    private int removeEventCount;
    private boolean eventArrived;
    private List<Object[]> inEventsList;

    @BeforeMethod
    public void init() {
        inEventCount.set(0);
        removeEventCount = 0;
        eventArrived = false;
        inEventsList = new ArrayList<Object[]>();
    }

    //Join Test

    @Test
    public void logicalTableTest1() throws InterruptedException {
        log.info("logicalTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and CheckStockStream.volume==200 " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 300L});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300L});
            checkStockStream.send(new Object[]{"IBM", 200L});
            checkStockStream.send(new Object[]{"WSO2", 200L});
            checkStockStream.send(new Object[]{"GOOG", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 300L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest2() throws InterruptedException {
        log.info("logicalTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and StockTable.volume==300 " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 300L});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300L});
            checkStockStream.send(new Object[]{"IBM", 200L});
            checkStockStream.send(new Object[]{"WSO2", 200L});
            checkStockStream.send(new Object[]{"GOOG", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 300L},
                    new Object[]{"GOOG", 300L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest3() throws InterruptedException {
        log.info("logicalTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and StockTable.volume==CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 300L});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300L});
            checkStockStream.send(new Object[]{"IBM", 300L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            checkStockStream.send(new Object[]{"GOOG", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 300L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest4() throws InterruptedException {
        log.info("logicalTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and StockTable.volume<=CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 50L});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300L});
            checkStockStream.send(new Object[]{"IBM", 300L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            checkStockStream.send(new Object[]{"GOOG", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 50L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest5() throws InterruptedException {
        log.info("logicalTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on 55.6f==StockTable.price and StockTable.volume<=CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 50L});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300L});
            checkStockStream.send(new Object[]{"IBM", 150L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"IBM", 50L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest6() throws InterruptedException {
        log.info("logicalTableTest6");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "   and StockTable.volume==CheckStockStream.volume " +
                "   and StockTable.price<=CheckStockStream.price " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 50L});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300L});
            checkStockStream.send(new Object[]{"IBM", 55.6f, 50L});
            checkStockStream.send(new Object[]{"WSO2", 55.6f, 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 50L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
