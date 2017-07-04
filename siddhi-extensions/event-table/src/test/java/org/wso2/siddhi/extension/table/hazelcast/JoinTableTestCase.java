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

public class JoinTableTestCase {
    private static final Logger log = Logger.getLogger(JoinTableTestCase.class);
    private AtomicInteger inEventCount = new AtomicInteger(0);
    private AtomicInteger removeEventCount = new AtomicInteger(0);
    private boolean eventArrived;
    private List<Object[]> inEventsList;
    private List<Object[]> removeEventsList;

    @Before
    public void init() {
        inEventCount.set(0);
        removeEventCount.set(0);
        eventArrived = false;
        inEventsList = new ArrayList<Object[]>();
        removeEventsList = new ArrayList<Object[]>();
    }

    @Test
    public void testTableJoinQuery1() throws InterruptedException {
        log.info("testTableJoinQuery1 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('JoinTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT011 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT011 ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTableT011 " +
                "select CheckStockStream.symbol as checkSymbol, StockTableT011.symbol as symbol, " +
                "StockTableT011.volume as volume  " +
                "insert into OutputStream ;";

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
                        removeEventCount.addAndGet(removeEvents.length);
                    }
                    eventArrived = true;
                }

            });

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 10l});
            checkStockStream.send(new Object[]{"WSO2"});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"WSO2", "WSO2", 100l},
                    new Object[]{"WSO2", "IBM", 10l}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount.get());
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testTableJoinQuery2() throws InterruptedException {
        log.info("testTableJoinQuery2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('JoinTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT021 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT021 ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTableT021 " +
                " on CheckStockStream.symbol==StockTableT021.symbol " +
                "select CheckStockStream.symbol as checkSymbol, StockTableT021.symbol as symbol, " +
                "StockTableT021.volume as volume  " +
                "insert into OutputStream ;";

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
                        removeEventCount.addAndGet(removeEvents.length);
                    }
                    eventArrived = true;
                }

            });

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 10l});
            checkStockStream.send(new Object[]{"WSO2"});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"WSO2", "WSO2", 100l});
            SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 1, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount.get());
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testTableJoinQuery3() throws InterruptedException {
        log.info("testTableJoinQuery3 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('JoinTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT031 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT031 ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTableT031 as t " +
                " on CheckStockStream.symbol!=t.symbol " +
                "select CheckStockStream.symbol as checkSymbol, t.symbol as symbol, t.volume as volume  " +
                "insert into OutputStream ;";

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
                        removeEventCount.addAndGet(removeEvents.length);
                    }
                    eventArrived = true;
                }

            });

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 10l});
            checkStockStream.send(new Object[]{"WSO2"});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"WSO2", "IBM", 10l});
            SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 1, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount.get());
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testTableJoinQuery4() throws InterruptedException {
        log.info("testTableJoinQuery4 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('JoinTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT041 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT041 ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.time(1 sec) join StockTableT041 " +
                " on CheckStockStream.symbol!=StockTableT041.symbol " +
                "select CheckStockStream.symbol as checkSymbol, StockTableT041.symbol as symbol, " +
                "StockTableT041.volume as volume  " +
                "insert all events into OutputStream ;";

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
                    }
                    if (removeEvents != null) {
                        for (Event event : removeEvents) {
                            removeEventsList.add(event.getData());
                            removeEventCount.incrementAndGet();
                        }
                    }
                    eventArrived = true;
                }

            });

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 200l});
            checkStockStream.send(new Object[]{"WSO2"});

            List<Object[]> expectedIn = new ArrayList<Object[]>();
            expectedIn.add(new Object[]{"WSO2", "IBM", 200l});
            SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);
            Assert.assertEquals("Number of success events", 1, inEventCount.get());
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expectedIn));

            List<Object[]> expectedRemove = new ArrayList<Object[]>();
            expectedRemove.add(new Object[]{"WSO2", "IBM", 200l});
            SiddhiTestHelper.waitForEvents(100, 1, removeEventCount, 60000);
            Assert.assertEquals("Number of remove events", 1, removeEventCount.get());
            Assert.assertEquals("In events matched", true,
                    SiddhiTestHelper.isEventsMatch(removeEventsList, expectedRemove));
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testTableJoinQuery5() throws InterruptedException {
        log.info("testTableJoinQuery5 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('JoinTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT051 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT051 ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTableT051 " +
                "select CheckStockStream.symbol as checkSymbol, StockTableT051.symbol as symbol, " +
                "StockTableT051.volume as volume  " +
                "insert into OutputStream ;";

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
                        removeEventCount.addAndGet(removeEvents.length);
                    }
                    eventArrived = true;
                }

            });

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 10l});
            checkStockStream.send(new Object[]{"WSO2"});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"WSO2", "WSO2", 100l},
                    new Object[]{"WSO2", "IBM", 10l}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount.get());
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
