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
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.table.test.util.SiddhiTestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexedTableTestCase {
    private static final Logger log = Logger.getLogger(IndexedTableTestCase.class);
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
    public void indexedTableTest1() throws InterruptedException {
        log.info("primaryKeyIndexTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('IndexedTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@store(type = 'hazelcast')" +
                "@IndexBy('symbol') " +
                "define table StockTableT011 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT011 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "update StockTableT011 " +
                "   on StockTableT011.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTableT011 " +
                " on CheckStockStream.symbol==StockTableT011.symbol " +
                "select CheckStockStream.symbol, StockTableT011.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 100l});
            checkStockStream.send(new Object[]{"IBM", 100l});
            checkStockStream.send(new Object[]{"WSO2", 100l});
            updateStockStream.send(new Object[]{"IBM", 77.6f, 200l});
            Thread.sleep(2000);
            checkStockStream.send(new Object[]{"IBM", 100l});
            checkStockStream.send(new Object[]{"WSO2", 100l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100l},
                    new Object[]{"WSO2", 100l},
                    new Object[]{"IBM", 200l},
                    new Object[]{"WSO2", 100l}
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
    public void indexedTableTest2() throws InterruptedException {
        log.info("indexedTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('IndexedTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@store(type = 'hazelcast')" +
                "@IndexBy('symbol') " +
                "define table StockTableT021 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT021 ;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTableT021 " +
                " on CheckStockStream.symbol==StockTableT021.symbol " +
                "select CheckStockStream.symbol, StockTableT021.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
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
            stockStream.send(new Object[]{"IBM", 55.6f, 200l});
            stockStream.send(new Object[]{"IBM", 55.6f, 300l});
            checkStockStream.send(new Object[]{"IBM", 100l});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"IBM", 300l});
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
    public void indexedTableTest3() throws InterruptedException {
        log.info("indexedTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('IndexedTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "@IndexBy('symbol') " +
                "define table StockTableT031 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT031 ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTableT031 " +
                "   on StockTableT031.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTableT031 " +
                " on CheckStockStream.symbol==StockTableT031.symbol " +
                "select CheckStockStream.symbol, StockTableT031.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
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
            InputHandler deleteStockStream = siddhiAppRuntime.getInputHandler("DeleteStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"IBM", 55.6f, 200l});
            stockStream.send(new Object[]{"IBM", 55.6f, 300l});
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            deleteStockStream.send(new Object[]{"WSO2"});
            checkStockStream.send(new Object[]{"IBM", 100l});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"IBM", 300l});
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
    public void indexedTableTest4() throws InterruptedException {
        log.info("indexedTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('IndexedTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "@IndexBy('symbol') " +
                "define table StockTableT041 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT041 ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTableT041 " +
                "   on symbol=='WSO2';" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTableT041 " +
                " on CheckStockStream.symbol==StockTableT041.symbol " +
                "select CheckStockStream.symbol, StockTableT041.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
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
            stockStream.send(new Object[]{"IBM", 55.6f, 200l});
            stockStream.send(new Object[]{"IBM", 55.6f, 300l});
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            deleteStockStream.send(new Object[]{"WSO2"});
            checkStockStream.send(new Object[]{"IBM", 100l});

            SiddhiTestHelper.waitForEvents(100, 0, inEventCount, 60000);
            Assert.assertEquals("Number of success events", 0, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", false, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexedTableTest5() throws InterruptedException {
        log.info("indexedTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('IndexedTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "@IndexBy('symbol') " +
                "define table StockTableT051 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT051 ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTableT051 " +
                "   on StockTableT051.symbol=='WSO2';" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTableT051 " +
                " on CheckStockStream.symbol==StockTableT051.symbol " +
                "select CheckStockStream.symbol, StockTableT051.volume " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
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
            InputHandler deleteStockStream = siddhiAppRuntime.getInputHandler("DeleteStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"IBM", 55.6f, 200l});
            stockStream.send(new Object[]{"IBM", 55.6f, 300l});
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            deleteStockStream.send(new Object[]{"IBM"});
            checkStockStream.send(new Object[]{"IBM", 100l});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"IBM", 300l});
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
    public void indexedTableTest6() throws InterruptedException {
        log.info("indexedTableTest6");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('IndexedTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long, price float); " +
                "define stream UpdateStockStream (comp string, vol long); " +
                "@store(type = 'hazelcast', instance.name = 'siddhi_instance') " +
                "@IndexBy('symbol') " +
                "define table StockTableT061 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT061 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream left outer join StockTableT061 " +
                "   on UpdateStockStream.comp == StockTableT061.symbol " +
                "select symbol, ifThenElse(price is null,0f,price) as price, vol as volume " +
                "update or insert into StockTableT061 " +
                "   on StockTableT061.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[(symbol==StockTableT061.symbol and volume==StockTableT061.volume" +
                " and price==StockTableT061.price) in StockTableT061] " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount.incrementAndGet();
                            switch (inEventCount.get()) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 100l, 155.6f}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 200l, 155.6f}, event.getData());
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 155.6f, 100l});
            checkStockStream.send(new Object[]{"IBM", 100l, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100l, 155.6f});
            updateStockStream.send(new Object[]{"IBM", 200l});
            checkStockStream.send(new Object[]{"IBM", 200l, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100l, 155.6f});

            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void indexedTableTest7() throws InterruptedException {
        log.info("indexedTableTest7");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('IndexedTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long, price float); " +
                "define stream UpdateStockStream (comp string, vol long); " +
                "@store(type = 'hazelcast', instance.name = 'siddhi_instance') " +
                "@IndexBy('symbol') " +
                "define table StockTableT071 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT071 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream left outer join StockTableT071 " +
                "   on UpdateStockStream.comp == StockTableT071.symbol " +
                "select comp as symbol, ifThenElse(price is null,0f,price) as price, vol as volume " +
                "update or insert into StockTableT071 " +
                "   on StockTableT071.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[(symbol==StockTableT071.symbol and volume==StockTableT071.volume" +
                " and price==StockTableT071.price) in StockTableT071] " +
                "insert into OutStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount.incrementAndGet();
                            switch (inEventCount.get()) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 200l, 0f}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 300l, 55.6f}, event.getData());
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
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            checkStockStream.send(new Object[]{"IBM", 100l, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100l, 155.6f});
            updateStockStream.send(new Object[]{"IBM", 200l});
            updateStockStream.send(new Object[]{"WSO2", 300l});
            checkStockStream.send(new Object[]{"IBM", 200l, 0f});
            checkStockStream.send(new Object[]{"WSO2", 300l, 55.6f});

            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
