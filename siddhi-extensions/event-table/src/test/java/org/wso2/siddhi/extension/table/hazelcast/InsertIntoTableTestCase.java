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

public class InsertIntoTableTestCase {
    private static final Logger log = Logger.getLogger(InsertIntoTableTestCase.class);
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
    public void insertIntoTableTest1() throws InterruptedException {
        log.info("InsertIntoTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('InsertIntoTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT011 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT011 ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void insertIntoTableTest2() throws InterruptedException {
        log.info("InsertIntoTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('InsertIntoTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT021 (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT022 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT021 ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockStream " +
                "insert into StockTableT022 ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void insertIntoTableTest3() throws InterruptedException {
        log.info("InsertIntoTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('InsertIntoTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream StockStream2 (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT031 (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT032 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT031 ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockStream2 " +
                "insert into StockTableT032 ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 75.6f, 100l});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
            Thread.sleep(RESULT_WAIT);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void insertIntoTableTest4() throws InterruptedException {
        log.info("InsertIntoTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('InsertIntoTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream StockCheckStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT041 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT041 ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockCheckStream[symbol==StockTableT041.symbol in StockTableT041] " +
                "insert into OutStream ;";

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
            InputHandler stockCheckStream = siddhiAppRuntime.getInputHandler("StockCheckStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockCheckStream.send(new Object[]{"IBM"});
            stockCheckStream.send(new Object[]{"WSO2"});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"WSO2"});
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
    public void insertIntoTableTest5() throws InterruptedException {
        log.info("InsertIntoTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('InsertIntoTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream StockCheckStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT051 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT051 ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockCheckStream[StockTableT051.symbol==symbol in StockTableT051] " +
                "insert into OutStream ;";

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
            InputHandler stockCheckStream = siddhiAppRuntime.getInputHandler("StockCheckStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 100l});
            stockCheckStream.send(new Object[]{"IBM"});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{"IBM"});
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
    public void insertIntoTableTest6() throws InterruptedException {
        log.info("InsertIntoTableTest6");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('InsertIntoTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream StockCheckStream (symbol string); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT061 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT061 ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockCheckStream[StockTableT061.symbol==StockCheckStream.symbol in StockTableT061] " +
                "insert into OutStream ;";

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
            InputHandler stockCheckStream = siddhiAppRuntime.getInputHandler("StockCheckStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 100l});
            stockCheckStream.send(new Object[]{"IBM"});
            stockCheckStream.send(new Object[]{"WSO2"});

            List<Object[]> expected = Arrays.asList(new Object[]{"IBM"}, new Object[]{"WSO2"});
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
    public void insertIntoTableTest7() throws InterruptedException {
        log.info("InsertIntoTableTest7");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('InsertIntoTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream StockCheckStream (price float); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT071 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT071 ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockCheckStream[price >= StockTableT071.price in StockTableT071] " +
                "insert into OutStream ;";

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
            InputHandler stockCheckStream = siddhiAppRuntime.getInputHandler("StockCheckStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 100l});
            stockStream.send(new Object[]{"GOOG", 255.6f, 100l});
            stockCheckStream.send(new Object[]{200f});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{200f});
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
    public void insertIntoTableTest8() throws InterruptedException {
        log.info("InsertIntoTableTest8");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "@app:name('InsertIntoTableSiddhiApp')" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream StockCheckStream (price float); " +
                "@store(type = 'hazelcast')" +
                "define table StockTableT081 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTableT081 ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockCheckStream[StockCheckStream.price >= StockTableT081.price in StockTableT081] " +
                "insert into OutStream ;";

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
            InputHandler stockCheckStream = siddhiAppRuntime.getInputHandler("StockCheckStream");

            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 100l});
            stockStream.send(new Object[]{"GOOG", 255.6f, 100l});
            stockCheckStream.send(new Object[]{100f});

            List<Object[]> expected = new ArrayList<Object[]>();
            expected.add(new Object[]{100f});
            SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 1, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
