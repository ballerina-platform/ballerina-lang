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

package org.wso2.siddhi.core.query.table;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PrimaryKeyTableTestCase {
    private static final Logger log = Logger.getLogger(PrimaryKeyTableTestCase.class);
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
    public void primaryKeyTableTest1() throws InterruptedException {
        log.info("primaryKeyTableTest1");

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
            stockStream.send(new Object[]{"IBM", 56.6f, 200L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest2() throws InterruptedException {
        log.info("primaryKeyTableTest2");

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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest3() throws InterruptedException {
        log.info("primaryKeyTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(2, 3), expected2));
            Assert.assertEquals("Number of success events", 3, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest4() throws InterruptedException {
        log.info("primaryKeyTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest5() throws InterruptedException {
        log.info("primaryKeyTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest6() throws InterruptedException {
        log.info("primaryKeyTableTest6");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest7() throws InterruptedException {
        log.info("primaryKeyTableTest7");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest8() throws InterruptedException {
        log.info("primaryKeyTableTest8");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "update or insert into StockTable " +
                "   on volume == StockTable.volume ;" +
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
                    new Object[]{"IBM", "WSO2", 200L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }


    //Update Test
    @Test
    public void primaryKeyTableTest9() throws InterruptedException {
        log.info("primaryKeyTableTest9");

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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 4, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest10() throws InterruptedException {
        log.info("primaryKeyTableTest10");

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

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"WSO2", 100L},
                    new Object[]{"IBM", 100L},
                    new Object[]{"IBM", 100L}
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

    //Todo improve update to support non updatable condition parameters
    @Test
    public void primaryKeyTableTest11() throws InterruptedException {
        log.info("primaryKeyTableTest11");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
                    new Object[]{55.6f, 200L},
                    new Object[]{55.6f, 100L}
            );

            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            Assert.assertEquals("In first events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            Assert.assertEquals("In second events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(2, 4), expected2));
            Assert.assertEquals("Number of success events", 4, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest12() throws InterruptedException {
        log.info("primaryKeyTableTest12");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
                    new Object[]{55.6f, 200L},
                    new Object[]{55.6f, 100L}
            );

            SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);
            Assert.assertEquals("In first events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            Assert.assertEquals("In second events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(2, 4), expected2));
            Assert.assertEquals("Number of success events", 4, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest13() throws InterruptedException {
        log.info("primaryKeyTableTest13");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In first events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest14() throws InterruptedException {
        log.info("primaryKeyTableTest14");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In first events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    //Delete Test
    @Test
    public void primaryKeyTableTest15() throws InterruptedException {
        log.info("primaryKeyTableTest15");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3),
                    expected2));
            Assert.assertEquals("Number of success events", 3, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest16() throws InterruptedException {
        log.info("primaryKeyTableTest16");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3),
                    expected2));
            Assert.assertEquals("Number of success events", 3, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest17() throws InterruptedException {
        log.info("primaryKeyTableTest17");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3),
                    expected2));
            Assert.assertEquals("Number of success events", 3, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest18() throws InterruptedException {
        log.info("primaryKeyTableTest18");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3),
                    expected2));
            Assert.assertEquals("Number of success events", 3, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }


    @Test
    public void primaryKeyTableTest19() throws InterruptedException {
        log.info("primaryKeyTableTest19");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 2), expected1));
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList.subList(2, 3),
                    expected2));
            Assert.assertEquals("Number of success events", 3, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest20() throws InterruptedException {
        log.info("primaryKeyTableTest20");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList
                    .subList(0, 3), expected1));
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList.subList(3, 4),
                    expected2));
            Assert.assertEquals("Number of success events", 4, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest21() throws InterruptedException {
        log.info("primaryKeyTableTest21");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@PrimaryKey('symbol') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected1));
            Assert.assertEquals("Number of success events", 1, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest22() throws InterruptedException {
        log.info("primaryKeyTableTest22");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@PrimaryKey('symbol') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest23() throws InterruptedException {
        log.info("primaryKeyTableTest23");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }


    @Test
    public void primaryKeyTableTest24() throws InterruptedException {
        log.info("primaryKeyTableTest24");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 1, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest25() throws InterruptedException {
        log.info("primaryKeyTableTest25");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest26() throws InterruptedException {
        log.info("primaryKeyTableTest26");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@PrimaryKey('volume') " +
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
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList,
                    expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest27() throws InterruptedException {
        log.info("primaryKeyTableTest27");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long, price float); " +
                "define stream UpdateStockStream (comp string, vol long); " +
                "@PrimaryKey('symbol') " +
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
                                    Assert.assertArrayEquals(new Object[]{"IBM", 200L, 0f}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 300L, 55.6f}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(2, inEventCount.get());
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
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(expected = AttributeNotExistException.class)
    public void primaryKeyTableTest28() throws InterruptedException {
        log.info("primaryKeyTableTest28");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@PrimaryKey('symbol1') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = null;
        try {
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        } finally {
            if (siddhiAppRuntime != null) {
                siddhiAppRuntime.shutdown();
            }
        }
    }

    @Test(expected = SiddhiParserException.class)
    public void primaryKeyTableTest29() throws InterruptedException {
        log.info("primaryKeyTableTest29");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@PrimaryKey() " +
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

    @Test(expected = DuplicateAnnotationException.class)
    public void primaryKeyTableTest31() throws InterruptedException {
        log.info("primaryKeyTableTest31");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@PrimaryKey('symbol') " +
                "@PrimaryKey('price') " +
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

    @Test(expected = SiddhiParserException.class)
    public void primaryKeyTableTest32() throws InterruptedException {
        log.info("primaryKeyTableTest32");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@PrimaryKey'symbol' " +
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

    @Test(expected = AttributeNotExistException.class)
    public void primaryKeyTableTest33() throws InterruptedException {
        log.info("primaryKeyTableTest33");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@PrimaryKey ('Symbol') " +
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

    @Test
    public void primaryKeyTableTest34() throws InterruptedException {
        log.info("primaryKeyTableTest34");

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
            stockStream.send(new Object[]{1, 55.6f, 100L});
            checkStockStream.send(new Object[]{1, 100L});

            List<Object[]> expected = new ArrayList<>();
            expected.add(new Object[]{1, 100L});

            SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 1, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest35() throws InterruptedException {
        log.info("primaryKeyTableTest35");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "define table StockTable (symbol string, price float, volume long); " +

                "define stream StockStream1 (symbol string, price float, volume long); " +
                "define stream CheckStockStream1 (symbol string, volume long); " +
                "define stream UpdateStockStream1 (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable1 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;" +
                "" +
                "@info(name = 'query11') " +
                "from StockStream1 " +
                "insert into StockTable1 ;" +
                "" +
                "@info(name = 'query21') " +
                "from CheckStockStream1 join StockTable1 " +
                " on CheckStockStream1.symbol==StockTable1.symbol " +
                "select CheckStockStream1.symbol, StockTable1.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    eventArrived = true;
                }
            });

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

            InputHandler stockStream1 = siddhiAppRuntime.getInputHandler("StockStream1");
            InputHandler checkStockStream1 = siddhiAppRuntime.getInputHandler("CheckStockStream1");

            siddhiAppRuntime.start();

            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                stockStream.send(new Object[]{"WSO2" + i, 55.6f, 100L});
            }
            for (int i = 0; i < 1000000; i++) {
                checkStockStream.send(new Object[]{"WSO20", 100L});
                checkStockStream.send(new Object[]{"WSO21", 100L});
            }
            long timeDiff = System.currentTimeMillis() - startTime;

            long startTime1 = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                stockStream1.send(new Object[]{"WSO2" + i, 55.6f, 100L});
            }
            for (int i = 0; i < 1000000; i++) {
                checkStockStream1.send(new Object[]{"WSO20", 100L});
                checkStockStream1.send(new Object[]{"WSO21", 100L});
            }
            long timeDiff1 = System.currentTimeMillis() - startTime1;

            Assert.assertEquals("Indexing is faster", true, timeDiff1 < timeDiff);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest36() throws InterruptedException {
        log.info("primaryKeyTableTest36");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol','volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and CheckStockStream.volume==StockTable.volume " +
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
            stockStream.send(new Object[]{"IBM", 56.6f, 200L});
            checkStockStream.send(new Object[]{"IBM", 200L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 200L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest37() throws InterruptedException {
        log.info("primaryKeyTableTest37");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol','volume') " +
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
            stockStream.send(new Object[]{"IBM", 56.6f, 200L});
            checkStockStream.send(new Object[]{"IBM", 200L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100L},
                    new Object[]{"IBM", 200L},
                    new Object[]{"WSO2", 100L}
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
    public void primaryKeyTableTest38() throws InterruptedException {
        log.info("primaryKeyTableTest38");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol','volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on (CheckStockStream.symbol==StockTable.symbol and CheckStockStream.volume==StockTable.volume) and " +
                " 55.6f == StockTable.price " +
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
            stockStream.send(new Object[]{"IBM", 55.6f, 101L});
            stockStream.send(new Object[]{"IBM", 55.6f, 102L});
            stockStream.send(new Object[]{"IBM", 55.6f, 200L});
            checkStockStream.send(new Object[]{"IBM", 200L});
            checkStockStream.send(new Object[]{"WSO2", 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 200L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void primaryKeyTableTest39() throws InterruptedException {
        log.info("primaryKeyTableTest39");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol','volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and CheckStockStream.volume==StockTable.volume and " +
                " CheckStockStream.price == StockTable.price " +
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
            stockStream.send(new Object[]{"IBM", 55.6f, 101L});
            stockStream.send(new Object[]{"IBM", 55.6f, 102L});
            stockStream.send(new Object[]{"IBM", 55.6f, 200L});
            checkStockStream.send(new Object[]{"IBM", 55.6f, 200L});
            checkStockStream.send(new Object[]{"WSO2", 55.6f, 100L});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 200L},
                    new Object[]{"WSO2", 100L}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

//
//    @Test
//    public void primaryKeyTableTest33() throws InterruptedException {
//        log.info("primaryKeyTableTest33");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//        String streams = "" +
//                "define stream StockStream (symbol string, price float, volume long); " +
//                "define stream CheckStockStream (symbol string, volume long); " +
//                "define stream UpdateStockStream (symbol string, price float, volume long);" +
//                "@PrimaryKey('symbol') " +
//                "define table StockTable (symbol string, price float, volume long); ";
//        String query = "" +
//                "@info(name = 'query1') " +
//                "from StockStream " +
//                "select symbol, price, volume " +
////                "insert into StockTable " +
//                "update or insert into StockTable " +
//                "   on symbol == StockTable.symbol ;" +
//                "";
//
//        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
//        try {
////            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
////                @Override
////                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
////                    EventPrinter.print(timeStamp, inEvents, removeEvents);
////                    if (inEvents != null) {
////                        for (Event event : inEvents) {
////                            inEventsList.add(event.getData());
////                            inEventCount.incrementAndGet();
////                        }
////                        eventArrived = true;
////                    }
////                    if (removeEvents != null) {
////                        removeEventCount = removeEventCount + removeEvents.length;
////                    }
////                    eventArrived = true;
////                }
////            });
//
//            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
//            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");
//
//            siddhiAppRuntime.start();
//            long i = 0;
//            while (i < 100000) {
//                stockStream.send(new Object[]{"FOO", 50.6f, 200L});
//                stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
//                stockStream.send(new Object[]{"GOOG", 50.6f, 50L});
//                stockStream.send(new Object[]{"ABC", 5.6f, 70L});
//                i++;
//            }
//            int k = 0;
//            while (k < 100) {
//                k++;
//                long startTime = System.currentTimeMillis();
//                i = 0;
//                while (i < 10000000) {
//                    stockStream.send(new Object[]{"FOO", 50.6f, 200L});
//                    stockStream.send(new Object[]{"WSO2", 55.6f, 200L});
//                    stockStream.send(new Object[]{"GOOG", 50.6f, 50L});
//                    stockStream.send(new Object[]{"ABC", 5.6f, 70L});
//                    i++;
//                }
//                log.info(System.currentTimeMillis() - startTime);
//            }
//        } finally {
//            siddhiAppRuntime.shutdown();
//        }
//    }

}
