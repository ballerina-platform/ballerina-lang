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

public class UpdateFromTableTestCase {
    private static final Logger log = Logger.getLogger(UpdateFromTableTestCase.class);
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
    public void updateFromTableTest1() throws InterruptedException {
        log.info("updateFromTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('UpdateFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT011 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT011 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "update StockTableT011 " +
                "   on StockTableT011.symbol=='IBM' ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            updateStockStream.send(new Object[]{"GOOG", 10.6f, 100l});

            Thread.sleep(RESULT_WAIT);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void updateFromTableTest2() throws InterruptedException {
        log.info("updateFromTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('UpdateFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT021 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT021 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "update StockTableT021 " +
                "   on StockTableT021.symbol==symbol ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            updateStockStream.send(new Object[]{"WSO2", 10f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void updateFromTableTest3() throws InterruptedException {
        log.info("updateFromTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('UpdateFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT031 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT031 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "update StockTableT031 " +
                "   on StockTableT031.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[(symbol==StockTableT031.symbol" +
                "   and  volume==StockTableT031.volume) in StockTableT031] " +
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
            checkStockStream.send(new Object[]{"IBM", 100l});
            checkStockStream.send(new Object[]{"WSO2", 100l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100l},
                    new Object[]{"WSO2", 100l},
                    new Object[]{"WSO2", 100l}
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
    public void updateFromTableTest4() throws InterruptedException {
        log.info("updateFromTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('UpdateFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (comp string, vol long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT041 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT041 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "select comp as symbol, vol as volume " +
                "update StockTableT041 " +
                "   on StockTableT041.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[(symbol==StockTableT041.symbol" +
                "   and  volume==StockTableT041.volume) in StockTableT041] " +
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
            updateStockStream.send(new Object[]{"IBM", 200l});
            checkStockStream.send(new Object[]{"IBM", 100l});
            checkStockStream.send(new Object[]{"WSO2", 100l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100l},
                    new Object[]{"WSO2", 100l},
                    new Object[]{"WSO2", 100l}
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
    public void updateFromTableTest5() throws InterruptedException {
        log.info("updateFromTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('UpdateFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long, price float); " +
                "define stream UpdateStockStream (comp string, vol long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT051 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT051 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "select comp as symbol, vol as volume " +
                "update StockTableT051 " +
                "   on StockTableT051.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[(symbol==StockTableT051.symbol and volume==StockTableT051.volume " +
                "and price==StockTableT051.price) in StockTableT051] " +
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
            stockStream.send(new Object[]{"IBM", 155.6f, 100l});
            checkStockStream.send(new Object[]{"IBM", 100l, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100l, 155.6f});
            updateStockStream.send(new Object[]{"IBM", 200l});
            checkStockStream.send(new Object[]{"IBM", 200l, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100l, 155.6f});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100l, 155.6f},
                    new Object[]{"IBM", 200l, 155.6f}
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
    public void updateFromTableTest6() throws InterruptedException {
        log.info("updateFromTableTest6");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('UpdateFromTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long, price float); " +
                "define stream UpdateStockStream (comp string, vol long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT061 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT061 ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream join StockTableT061 " +
                "   on UpdateStockStream.comp == StockTableT061.symbol " +
                "select symbol, vol as volume " +
                "update StockTableT061 " +
                "   on StockTableT061.symbol==symbol;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream[(symbol==StockTableT061.symbol and volume==StockTableT061.volume " +
                "and price==StockTableT061.price) in StockTableT061] " +
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
            stockStream.send(new Object[]{"IBM", 155.6f, 100l});
            checkStockStream.send(new Object[]{"IBM", 100l, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100l, 155.6f});
            updateStockStream.send(new Object[]{"IBM", 200l});
            checkStockStream.send(new Object[]{"IBM", 200l, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100l, 155.6f});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100l, 155.6f},
                    new Object[]{"IBM", 200l, 155.6f}
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
}
