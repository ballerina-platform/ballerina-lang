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
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.ballerinalang.siddhi.query.api.exception.AttributeNotExistException;
import org.ballerinalang.siddhi.query.api.exception.DuplicateAnnotationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexTableTestCase {
    private static final Logger log = LoggerFactory.getLogger(IndexTableTestCase.class);
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

    @Test
    public void indexTableTest1() throws InterruptedException {
        log.info("indexTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
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
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100L},
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
    public void indexTableTest2() throws InterruptedException {
        log.info("indexTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol!=StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.symbol as tableSymbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"GOOG", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"GOOG", "IBM", 100L},
                    new Object[]{"GOOG", "WSO2", 100L}
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
    public void indexTableTest3() throws InterruptedException {
        log.info("indexTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.volume > StockTable.volume " +
                "select CheckStockStream.symbol, StockTable.symbol as tableSymbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"GOOG", 50.6f, 50L});
            stockStream.send(new Object[]{"ABC", 5.6f, 70L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"FOO", 60L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{"IBM", "GOOG", 50L},
                    new Object[]{"IBM", "ABC", 70L}
            );
            List<Object[]> expected2 = new ArrayList<Object[]>();
            expected2.add(new Object[]{"FOO", "GOOG", 50L});

            SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(2, 3), expected2));
            AssertJUnit.assertEquals("Number of success events", 3, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest4() throws InterruptedException {
        log.info("indexTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on StockTable.volume < CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.symbol as tableSymbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"GOOG", 50.6f, 50L});
            stockStream.send(new Object[]{"ABC", 5.6f, 70L});
            checkStockStream.send(new Object[]{"IBM", 200L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", "ABC", 70L},
                    new Object[]{"IBM", "GOOG", 50L}
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
    public void indexTableTest5() throws InterruptedException {
        log.info("indexTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on StockTable.volume <= CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.symbol as tableSymbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"GOOG", 50.6f, 50L});
            stockStream.send(new Object[]{"ABC", 5.6f, 70L});
            checkStockStream.send(new Object[]{"IBM", 70L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", "ABC", 70L},
                    new Object[]{"IBM", "GOOG", 50L}
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
    public void indexTableTest6() throws InterruptedException {
        log.info("indexTableTest6");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on StockTable.volume > CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.symbol as tableSymbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"GOOG", 50.6f, 50L});
            stockStream.send(new Object[]{"ABC", 5.6f, 70L});
            checkStockStream.send(new Object[]{"IBM", 50L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", "WSO2", 200L},
                    new Object[]{"IBM", "ABC", 70L}
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
    public void indexTableTest7() throws InterruptedException {
        log.info("indexTableTest7");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on StockTable.volume >= CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.symbol as tableSymbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"GOOG", 50.6f, 50L});
            stockStream.send(new Object[]{"ABC", 5.6f, 70L});
            checkStockStream.send(new Object[]{"IBM", 70L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", "ABC", 70L},
                    new Object[]{"IBM", "WSO2", 200L}
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
    public void indexTableTest8() throws InterruptedException {
        log.info("indexTableTest8");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on StockTable.volume >= CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.symbol as tableSymbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"FOO", 50.6f, 200L});
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"GOOG", 50.6f, 50L});
            stockStream.send(new Object[]{"ABC", 5.6f, 70L});
            checkStockStream.send(new Object[]{"IBM", 70L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", "ABC", 70L},
                    new Object[]{"IBM", "WSO2", 200L},
                    new Object[]{"IBM", "FOO", 200L}
            );
            SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            AssertJUnit.assertEquals("Number of success events", 3, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }


    //Update Test
    @Test
    public void indexTableTest9() throws InterruptedException {
        log.info("indexTableTest9");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "update StockTable " +
                "   on StockTable.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.volume " +
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            updateStockStream.send(new Object[]{"IBM", 77.6f, 200L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"WSO2", 100L},
                    new Object[]{"IBM", 200L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            AssertJUnit.assertEquals("Number of success events", 4, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest10() throws InterruptedException {
        log.info("indexTableTest10");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "update StockTable " +
                "   on StockTable.symbol!=symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol!=StockTable.symbol " +
                "select StockTable.symbol, StockTable.volume " +
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            updateStockStream.send(new Object[]{"IBM", 77.6f, 200L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{"WSO2", 100L},
                    new Object[]{"IBM", 100L}
            );
            List<Object[]> expected2 = Arrays.asList(
                    new Object[]{"IBM", 200L},
                    new Object[]{"IBM", 100L}
            );

            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true,
                    SiddhiTestHelper.isEventsMatch(inEventsList.subList(0, 2), expected1));
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(2, 4), expected2));
            AssertJUnit.assertEquals("Number of success events", 4, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    //Todo improve update to support non updatable condition parameters
    @Test
    public void indexTableTest11() throws InterruptedException {
        log.info("indexTableTest11");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "select price, volume  " +
                "update StockTable " +
                "   on StockTable.volume <= volume;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.volume >= StockTable.volume " +
                "select StockTable.price, StockTable.volume " +
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 200L});
            updateStockStream.send(new Object[]{"FOO", 77.6f, 200L});
            checkStockStream.send(new Object[]{"BAR", 200L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{55.6f, 200L},
                    new Object[]{55.6f, 100L}
            );

            List<Object[]> expected2 = Arrays.asList(
                    new Object[]{77.6f, 200L},
                    new Object[]{77.6f, 200L}
            );

            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            AssertJUnit.assertEquals("In first events matched", true,
                    SiddhiTestHelper.isUnsortedEventsMatch(inEventsList.subList(0, 2), expected1));
            AssertJUnit.assertEquals("In second events matched", true,
                    SiddhiTestHelper.isUnsortedEventsMatch(inEventsList.subList(2, 4), expected2));
            AssertJUnit.assertEquals("Number of success events", 4, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest12() throws InterruptedException {
        log.info("indexTableTest12");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "select price, volume  " +
                "update StockTable " +
                "   on StockTable.volume < volume;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.volume >= StockTable.volume " +
                "select StockTable.price, StockTable.volume " +
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 200L});
            updateStockStream.send(new Object[]{"FOO", 77.6f, 200L});
            checkStockStream.send(new Object[]{"BAR", 200L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{55.6f, 200L},
                    new Object[]{55.6f, 100L}
            );

            List<Object[]> expected2 = Arrays.asList(
                    new Object[]{77.6f, 200L},
                    new Object[]{55.6f, 200L}
            );

            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            AssertJUnit.assertEquals("In first events matched", true,
                    SiddhiTestHelper.isUnsortedEventsMatch(inEventsList.subList(0, 2), expected1));
            AssertJUnit.assertEquals("In second events matched", true,
                    SiddhiTestHelper.isUnsortedEventsMatch(inEventsList.subList(2, 4), expected2));
            AssertJUnit.assertEquals("Number of success events", 4, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest13() throws InterruptedException {
        log.info("indexTableTest13");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "select price, volume  " +
                "update StockTable " +
                "   on StockTable.volume >= volume;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.volume <= StockTable.volume " +
                "select StockTable.price, StockTable.volume " +
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 200L});
            updateStockStream.send(new Object[]{"FOO", 77.6f, 200L});
            checkStockStream.send(new Object[]{"BAR", 200L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{55.6f, 200L},
                    new Object[]{77.6f, 200L}
            );

            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("In first events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList,
                    expected));
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest14() throws InterruptedException {
        log.info("indexTableTest14");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "select price, volume  " +
                "update StockTable " +
                "   on StockTable.volume > volume;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.volume <= StockTable.volume " +
                "select StockTable.price, StockTable.volume " +
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 150L});
            updateStockStream.send(new Object[]{"FOO", 77.6f, 150L});
            checkStockStream.send(new Object[]{"BAR", 150L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{55.6f, 200L},
                    new Object[]{77.6f, 150L}
            );

            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("In first events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList,
                    expected));
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    //Delete Test
    @Test
    public void indexTableTest15() throws InterruptedException {
        log.info("indexTableTest15");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                "select StockTable.symbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            deleteStockStream.send(new Object[]{"IBM", 77.6f, 200L});
            checkStockStream.send(new Object[]{"FOO", 100L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"WSO2", 100L}
            );

            List<Object[]> expected2 = new ArrayList<Object[]>();
            expected2.add(new Object[]{"WSO2", 100L});

            SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            AssertJUnit.assertEquals("In events matched", true,
                    SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3), expected2));
            AssertJUnit.assertEquals("Number of success events", 3, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest16() throws InterruptedException {
        log.info("indexTableTest16");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.symbol!=symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                "select StockTable.symbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            deleteStockStream.send(new Object[]{"IBM", 77.6f, 200L});
            checkStockStream.send(new Object[]{"FOO", 100L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"WSO2", 100L}
            );

            List<Object[]> expected2 = new ArrayList<Object[]>();
            expected2.add(new Object[]{"IBM", 100L});

            SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            AssertJUnit.assertEquals("In events matched", true,
                    SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3), expected2));
            AssertJUnit.assertEquals("Number of success events", 3, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest17() throws InterruptedException {
        log.info("indexTableTest17");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.volume>volume;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                "select StockTable.symbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            deleteStockStream.send(new Object[]{"IBM", 77.6f, 150L});
            checkStockStream.send(new Object[]{"FOO", 100L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"WSO2", 200L}
            );

            List<Object[]> expected2 = new ArrayList<Object[]>();
            expected2.add(new Object[]{"IBM", 100L});

            SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            AssertJUnit.assertEquals("In events matched", true,
                    SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3), expected2));
            AssertJUnit.assertEquals("Number of success events", 3, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest18() throws InterruptedException {
        log.info("indexTableTest18");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.volume>=volume;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                "select StockTable.symbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            deleteStockStream.send(new Object[]{"IBM", 77.6f, 200L});
            checkStockStream.send(new Object[]{"FOO", 100L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"WSO2", 200L}
            );

            List<Object[]> expected2 = new ArrayList<Object[]>();
            expected2.add(new Object[]{"IBM", 100L});

            SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            AssertJUnit.assertEquals("In events matched", true,
                    SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3), expected2));
            AssertJUnit.assertEquals("Number of success events", 3, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }


    @Test
    public void indexTableTest19() throws InterruptedException {
        log.info("indexTableTest19");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.volume < volume;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                "select StockTable.symbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            deleteStockStream.send(new Object[]{"IBM", 77.6f, 150L});
            checkStockStream.send(new Object[]{"FOO", 100L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"WSO2", 200L}
            );

            List<Object[]> expected2 = new ArrayList<Object[]>();
            expected2.add(new Object[]{"WSO2", 200L});

            SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            AssertJUnit.assertEquals("In events matched", true,
                    SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3), expected2));
            AssertJUnit.assertEquals("Number of success events", 3, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest20() throws InterruptedException {
        log.info("indexTableTest20");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.volume <= volume;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                "select StockTable.symbol, StockTable.volume " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"BAR", 55.6f, 150L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            deleteStockStream.send(new Object[]{"IBM", 77.6f, 150L});
            checkStockStream.send(new Object[]{"FOO", 100L});

            List<Object[]> expected1 = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"BAR", 150L},
                    new Object[]{"WSO2", 200L}
            );

            List<Object[]> expected2 = new ArrayList<Object[]>();
            expected2.add(new Object[]{"WSO2", 200L});

            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 3), expected1));
            AssertJUnit.assertEquals("In events matched", true,
                    SiddhiTestHelper.isEventsMatch(inEventsList.subList(3, 4), expected2));
            AssertJUnit.assertEquals("Number of success events", 4, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest21() throws InterruptedException {
        log.info("indexTableTest21");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream[(symbol==StockTable.symbol) in StockTable] " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"BAR", 55.6f, 150L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"FOO", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected1 = new ArrayList<Object[]>();
            expected1.add(new Object[]{"WSO2", 100L});

            SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected1));
            AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest22() throws InterruptedException {
        log.info("indexTableTest22");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream[(symbol!=StockTable.symbol) in StockTable] " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"BAR", 55.6f, 150L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"FOO", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"FOO", 100L},
                    new Object[]{"WSO2", 100L}
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
    public void indexTableTest23() throws InterruptedException {
        log.info("indexTableTest23");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream[(volume > StockTable.volume) in StockTable] " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"BAR", 55.6f, 150L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"FOO", 170L});
            checkStockStream.send(new Object[]{"FOO", 500L});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"FOO", 170L});
            expected.add(new Object[]{"FOO", 500L});

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
    public void indexTableTest24() throws InterruptedException {
        log.info("indexTableTest24");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream[(volume < StockTable.volume) in StockTable] " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"BAR", 55.6f, 150L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"FOO", 170L});
            checkStockStream.send(new Object[]{"FOO", 500L});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"FOO", 170L});

            SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);
            AssertJUnit.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest25() throws InterruptedException {
        log.info("indexTableTest25");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream[(volume <= StockTable.volume) in StockTable] " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"BAR", 55.6f, 150L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"FOO", 170L});
            checkStockStream.send(new Object[]{"FOO", 200L});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"FOO", 170L});
            expected.add(new Object[]{"FOO", 200L});

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
    public void indexTableTest26() throws InterruptedException {
        log.info("indexTableTest26");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream[(volume >= StockTable.volume) in StockTable] " +
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
            stockStream.send(new Object[]{"BAR", 55.6f, 150L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"FOO", 170L});
            checkStockStream.send(new Object[]{"FOO", 100L});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"FOO", 170L});
            expected.add(new Object[]{"FOO", 100L});

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
    public void indexTableTest27() throws InterruptedException {
        log.info("indexTableTest27");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long, price float); " +
                "define stream UpdateStockStream (comp string, vol long); " +
                "@Index('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream left outer join StockTable " +
                "   on UpdateStockStream.comp == StockTable.symbol " +
                "select comp as symbol, ifThenElse(price is null,0f,price) as price, vol as volume " +
                "update or insert into StockTable " +
                "   on StockTable.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[(symbol==StockTable.symbol and volume==StockTable.volume" +
                " and price==StockTable.price) in StockTable] " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timestamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount.incrementAndGet();
                            switch (inEventCount.get()) {
                                case 1:
                                    AssertJUnit.assertArrayEquals(new Object[]{"IBM", 200L, 0f}, event.getData());
                                    break;
                                case 2:
                                    AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 300L, 55.6f}, event.getData());
                                    break;
                                default:
                                    AssertJUnit.assertSame(2, inEventCount.get());
                            }
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount += removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100L, 155.6f});
            updateStockStream.send(new Object[]{"IBM", 200L});
            updateStockStream.send(new Object[]{"WSO2", 300L});
            checkStockStream.send(new Object[]{"IBM", 200L, 0f});
            checkStockStream.send(new Object[]{"WSO2", 300L, 55.6f});

            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
            AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
            AssertJUnit.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexTableTest28() throws InterruptedException {
        log.info("indexTableTest28");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('price','volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100L},
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
    public void indexTableTest29() throws InterruptedException {
        log.info("indexTableTest29");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@Index('symbol', 'volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
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

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 55.6f, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100L},
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

    @Test(expectedExceptions = AttributeNotExistException.class)
    public void indexTableTest30() throws InterruptedException {
        log.info("indexTableTest30");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@Index('') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "";

        SiddhiAppRuntime siddhiAppRuntime = null;
        try {
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        } finally {
            if (siddhiAppRuntime != null) {
                siddhiAppRuntime.shutdown();
            }
        }
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void indexTableTest31() throws InterruptedException {
        log.info("indexTableTest31");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@Index('symbol', 'symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "";

        SiddhiAppRuntime siddhiAppRuntime = null;
        try {
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        } finally {
            if (siddhiAppRuntime != null) {
                siddhiAppRuntime.shutdown();
            }
        }
    }

    @Test(expectedExceptions = DuplicateAnnotationException.class)
    public void indexTableTest32() throws InterruptedException {
        log.info("indexTableTest32");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@Index('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "";

        SiddhiAppRuntime siddhiAppRuntime = null;
        try {
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        } finally {
            if (siddhiAppRuntime != null) {
                siddhiAppRuntime.shutdown();
            }
        }
    }

    @Test(expectedExceptions = AttributeNotExistException.class)
    public void indexTableTest33() throws InterruptedException {
        log.info("indexTableTest33");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@Index('foo') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "";

        SiddhiAppRuntime siddhiAppRuntime = null;
        try {
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        } finally {
            if (siddhiAppRuntime != null) {
                siddhiAppRuntime.shutdown();
            }
        }
    }

}
