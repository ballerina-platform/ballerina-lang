/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.query.window.external;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;

public class CustomJoinWindowTestCase {
    private static final Logger log = LoggerFactory.getLogger(CustomJoinWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private int count;
    private boolean eventArrived;
    private long value;

    @BeforeMethod
    public void init() {
        count = 0;
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
        value = 0;
    }

    @Test
    public void testJoinWindowWithTable() throws InterruptedException {
        log.info("Test join window with table");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "define window CheckStockWindow(symbol string) length(1) output all events; " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query1') " +
                "from CheckStockStream " +
                "insert into CheckStockWindow ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockWindow join StockTable " +
                " on CheckStockWindow.symbol==StockTable.symbol " +
                "select CheckStockWindow.symbol as checkSymbol, StockTable.symbol as symbol, StockTable.volume as " +
                "volume  " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                break;
                            default:
                                org.testng.AssertJUnit.assertSame(1, inEventCount);
                        }
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
        stockStream.send(new Object[]{"IBM", 75.6f, 10L});
        checkStockStream.send(new Object[]{"WSO2"});

        Thread.sleep(500);

        assertEquals("Number of success events", 1, inEventCount);
        assertEquals("Number of remove events", 0, removeEventCount);
        assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testJoinWindowWithWindow() throws InterruptedException {
        log.info("Test join window with another window");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream TempStream(deviceID long, roomNo int, temp double); " +
                "define stream RegulatorStream(deviceID long, roomNo int, isOn bool); " +
                "define window TempWindow(deviceID long, roomNo int, temp double) time(1 min); " +
                "define window RegulatorWindow(deviceID long, roomNo int, isOn bool) length(1); ";

        String query = "" +
                "@info(name = 'query1') " +
                "from TempStream[temp > 30.0] " +
                "insert into TempWindow; " +
                "" +
                "@info(name = 'query2') " +
                "from RegulatorStream[isOn == false] " +
                "insert into RegulatorWindow; " +
                "" +
                "@info(name = 'query3') " +
                "from TempWindow " +
                "join RegulatorWindow " +
                "on TempWindow.roomNo == RegulatorWindow.roomNo " +
                "select TempWindow.roomNo, RegulatorWindow.deviceID, 'start' as action " +
                "insert into RegulatorActionStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("RegulatorActionStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                inEventCount++;
            }
        });

        InputHandler tempStream = siddhiAppRuntime.getInputHandler("TempStream");
        InputHandler regulatorStream = siddhiAppRuntime.getInputHandler("RegulatorStream");

        siddhiAppRuntime.start();

        tempStream.send(new Object[]{100L, 1, 20.0});
        tempStream.send(new Object[]{100L, 2, 25.0});
        tempStream.send(new Object[]{100L, 3, 30.0});
        tempStream.send(new Object[]{100L, 4, 35.0});
        tempStream.send(new Object[]{100L, 5, 40.0});

        regulatorStream.send(new Object[]{100L, 1, false});
        regulatorStream.send(new Object[]{100L, 2, false});
        regulatorStream.send(new Object[]{100L, 3, false});
        regulatorStream.send(new Object[]{100L, 4, false});
        regulatorStream.send(new Object[]{100L, 5, false});

        Thread.sleep(500);
        assertEquals("Number of success events", 2, inEventCount);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testJoinWindowWithStream() throws InterruptedException {
        log.info("Test join window with a stream");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream TempStream(deviceID long, roomNo int, temp double); " +
                "define stream RegulatorStream(deviceID long, roomNo int, isOn bool); " +
                "define window TempWindow(deviceID long, roomNo int, temp double) time(1 min); ";

        String query = "" +
                "@info(name = 'query1') " +
                "from TempStream[temp > 30.0] " +
                "insert into TempWindow;" +
                "" +
                "@info(name = 'query2') " +
                "from TempWindow " +
                "join RegulatorStream[isOn == false]#window.length(1) as R " +
                "on TempWindow.roomNo == R.roomNo " +
                "select TempWindow.roomNo, R.deviceID, 'start' as action " +
                "insert into RegulatorActionStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("RegulatorActionStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                inEventCount++;
            }
        });

        InputHandler tempStream = siddhiAppRuntime.getInputHandler("TempStream");
        InputHandler regulatorStream = siddhiAppRuntime.getInputHandler("RegulatorStream");

        siddhiAppRuntime.start();

        tempStream.send(new Object[]{100L, 1, 20.0});
        tempStream.send(new Object[]{100L, 2, 25.0});
        tempStream.send(new Object[]{100L, 3, 30.0});
        tempStream.send(new Object[]{100L, 4, 35.0});
        tempStream.send(new Object[]{100L, 5, 40.0});

        regulatorStream.send(new Object[]{100L, 1, false});
        regulatorStream.send(new Object[]{100L, 2, false});
        regulatorStream.send(new Object[]{100L, 3, false});
        regulatorStream.send(new Object[]{100L, 4, false});
        regulatorStream.send(new Object[]{100L, 5, false});

        Thread.sleep(500);
        assertEquals("Number of success events", 2, inEventCount);
        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testMultipleStreamsToWindow() throws InterruptedException {
        log.info("Test sending events from multiple streams into a single window");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume long); " +
                "define stream Stream2 (symbol string, price float, volume long); " +
                "define stream Stream3 (symbol string, price float, volume long); " +
                "define stream Stream4 (symbol string, price float, volume long); " +
                "define stream Stream5 (symbol string, price float, volume long); " +
                "define stream Stream6 (symbol string, price float, volume long); " +
                "define window StockWindow (symbol string, price float, volume long) lengthBatch(5); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from Stream1 " +
                "insert into StockWindow; " +
                "from Stream2 " +
                "insert into StockWindow; " +
                "from Stream3 " +
                "insert into StockWindow; " +
                "from Stream4 " +
                "insert into StockWindow; " +
                "from Stream5 " +
                "insert into StockWindow; " +
                "from Stream6 " +
                "insert into StockWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from StockWindow " +
                "select symbol, sum(price) as totalPrice, sum(volume) as volumes " +
                "insert into OutputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                assertEquals("Invalid number of output events", 1, events.length);
                assertArrayEquals(new Object[]{"WSO2", 150.0, 5L}, events[0].getData());
            }
        });

        siddhiAppRuntime.start();

        for (int i = 1; i <= 6; i++) {
            siddhiAppRuntime.getInputHandler("Stream" + i).send(new Object[]{"WSO2", (i * 10.0f), 1L});
        }
        Thread.sleep(500);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testMultipleStreamsFromWindow() throws InterruptedException {
        log.info("Test joining a single window by multiple streams");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockInputStream (symbol string, price float, volume long); " +
                "define stream SummaryOfCompanyTriggerStream (symbol string); " +
                "define stream VolumeGreaterThanTriggerStream (volume long); " +
                "define window StockWindow (symbol string, price float, volume long) lengthBatch(10); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from StockInputStream " +
                "insert into StockWindow; " +

                "@info(name = 'query1') " +
                "from StockWindow join SummaryOfCompanyTriggerStream#window.length(1) " +
                "on StockWindow.symbol == SummaryOfCompanyTriggerStream.symbol " +
                "select StockWindow.symbol, sum(StockWindow.price) as totalPrice, sum(StockWindow.volume) as volumes " +
                "insert into SummaryOfCompanyOutputStream; " +
                "" +
                "@info(name = 'query2') " +
                "from StockWindow join VolumeGreaterThanTriggerStream#window.length(1) " +
                "on StockWindow.volume > VolumeGreaterThanTriggerStream.volume " +
                "select StockWindow.symbol, StockWindow.price, StockWindow.volume as volume " +
                "insert into VolumeGreaterThanOutputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("SummaryOfCompanyOutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Object[] data = events[0].getData();
                if ("WSO2".equals(data[0])) {
                    assertArrayEquals(new Object[]{"WSO2", 430.0, 57L}, data);
                } else if ("IBM".equals(data[0])) {
                    assertArrayEquals(new Object[]{"IBM", 222.0, 16L}, data);
                }
            }
        });

        siddhiAppRuntime.addCallback("VolumeGreaterThanOutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                assertEquals("Error in number of events with volume > 6", 6, events.length);
            }
        });

        siddhiAppRuntime.start();

        InputHandler stockInputStreamInputHandler = siddhiAppRuntime.getInputHandler("StockInputStream");
        InputHandler summaryOfCompanyTriggerStreamInputHandler = siddhiAppRuntime.getInputHandler
                ("SummaryOfCompanyTriggerStream");
        InputHandler volumeGreaterThanTriggerStreamInputHandler = siddhiAppRuntime.getInputHandler
                ("VolumeGreaterThanTriggerStream");

        // 10 inputs
        stockInputStreamInputHandler.send(new Object[]{"WSO2", 84.0f, 20L});
        stockInputStreamInputHandler.send(new Object[]{"IBM", 90.0f, 1L});
        stockInputStreamInputHandler.send(new Object[]{"WSO2", 55.0f, 5L});
        stockInputStreamInputHandler.send(new Object[]{"WSO2", 77.0f, 10L});
        stockInputStreamInputHandler.send(new Object[]{"IBM", 46.0f, 5L});
        stockInputStreamInputHandler.send(new Object[]{"WSO2", 36.0f, 2L});
        stockInputStreamInputHandler.send(new Object[]{"WSO2", 56.0f, 6L});
        stockInputStreamInputHandler.send(new Object[]{"IBM", 86.0f, 10L});
        stockInputStreamInputHandler.send(new Object[]{"WSO2", 26.0f, 6L});
        stockInputStreamInputHandler.send(new Object[]{"WSO2", 96.0f, 8L});

        // out of 10
        stockInputStreamInputHandler.send(new Object[]{"WSO2", 56.0f, 10L});
        stockInputStreamInputHandler.send(new Object[]{"IBM", 36.0f, 15L});

        Thread.sleep(100);

        summaryOfCompanyTriggerStreamInputHandler.send(new Object[]{"WSO2"});
        summaryOfCompanyTriggerStreamInputHandler.send(new Object[]{"IBM"});

        volumeGreaterThanTriggerStreamInputHandler.send(new Object[]{5L});
        Thread.sleep(500);

        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testWindowAfterWindow() throws InterruptedException {
        log.info("Test traditional window for a stream out of window");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define window StockWindow (symbol string, price float, volume long) timeBatch(1 sec) output current " +
                "events; ";
        String query = "" +
                "@info(name = 'query0') " +
                "from StockStream " +
                "insert into StockWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from StockWindow#window.lengthBatch(2) " +
                "insert into OutputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(1100);
        stockStream.send(new Object[]{"IBM", 65.0f, 100L});
        stockStream.send(new Object[]{"WSO2", 50.0f, 100L});
        Thread.sleep(1500);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testUnidirectionalJoin() throws InterruptedException {
        log.info("Test unidirectional join with two windows");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); " +
                "define window cseEventWindow (symbol string, price float, volume int) lengthBatch(2); " +
                "define window twitterWindow (user string, tweet string, company string) lengthBatch(2); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from cseEventStream " +
                "insert into cseEventWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from twitterStream " +
                "insert into twitterWindow; " +
                "" +
                "@info(name = 'query2') " +
                "from cseEventWindow unidirectional join twitterWindow " +
                "on cseEventWindow.symbol == twitterWindow.company " +
                "select cseEventWindow.symbol as symbol, twitterWindow.tweet, cseEventWindow.price " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("cseEventWindow", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    System.out.print("cseEventWindow: ");
                    EventPrinter.print(events);
                }
            });
            siddhiAppRuntime.addCallback("twitterWindow", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    System.out.print("twitterWindow: ");
                    EventPrinter.print(events);
                }
            });
            siddhiAppRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timestamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        inEventCount += (inEvents.length);
                    }
                    if (removeEvents != null) {
                        removeEventCount += (removeEvents.length);
                    }
                    eventArrived = true;
                }
            });
            InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
            InputHandler twitterStreamHandler = siddhiAppRuntime.getInputHandler("twitterStream");
            siddhiAppRuntime.start();
            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
            cseEventStreamHandler.send(new Object[]{"IBM", 59.6f, 100});
            twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
            twitterStreamHandler.send(new Object[]{"User2", "Hello World2", "WSO2"});
            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
            Thread.sleep(500);
            cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(1000);
            AssertJUnit.assertEquals(2, inEventCount);
            AssertJUnit.assertEquals(0, removeEventCount);
            AssertJUnit.assertTrue(eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testGroupByUseCase() throws InterruptedException {
        log.info("Test joining a single window by multiple streams");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream SensorStream (name string, value float, roomNo int, deviceID string); " +
                "define window SensorWindow (name string, value float, roomNo int, deviceID string) timeBatch(1 " +
                "second); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from SensorStream " +
                "insert into SensorWindow; " +

                "@info(name = 'query1') " +
                "from SensorWindow  " +
                "select name, max(value) as maxValue, roomNo " +
                "group by name, roomNo " +
                "insert into MaxSensorReadingPerRoomStream; " +

                "@info(name = 'query2') " +
                "from SensorWindow  " +
                "select name, max(value) as maxValue " +
                "group by name " +
                "insert into OverallMaxSensorReadingStream; " +

                "@info(name = 'query3') " +
                "from SensorWindow  " +
                "select name, avg(value) as avgValue " +
                "group by name " +
                "insert into OverallAverageSensorReadingStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("MaxSensorReadingPerRoomStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                System.out.print("MaxSensorReadingPerRoomStream: ");
                EventPrinter.print(events);
            }
        });
        siddhiAppRuntime.addCallback("OverallMaxSensorReadingStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                System.out.print("OverallMaxSensorReadingStream: ");
                EventPrinter.print(events);
            }
        });
        siddhiAppRuntime.addCallback("OverallAverageSensorReadingStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                System.out.print("OverallAverageSensorReadingStream: ");
                EventPrinter.print(events);
            }
        });
        siddhiAppRuntime.start();

        InputHandler sensorStreamInputHandler = siddhiAppRuntime.getInputHandler("SensorStream");

        sensorStreamInputHandler.send(new Object[]{"Temperature", 23.0f, 1, "T001A"});
        sensorStreamInputHandler.send(new Object[]{"Pressure", 20.0f, 1, "P001"});
        sensorStreamInputHandler.send(new Object[]{"Temperature", 25.0f, 2, "T002"});
        sensorStreamInputHandler.send(new Object[]{"Temperature", 24.0f, 3, "T003"});
        sensorStreamInputHandler.send(new Object[]{"Pressure", 45.0f, 3, "P003"});
        sensorStreamInputHandler.send(new Object[]{"Temperature", 23.5f, 1, "T001B"});
        sensorStreamInputHandler.send(new Object[]{"Humidity", 15.0f, 2, "H002"});
        sensorStreamInputHandler.send(new Object[]{"Humidity", 10.0f, 3, "H003"});

        Thread.sleep(1000);

        Thread.sleep(500);

        siddhiAppRuntime.shutdown();
    }

    /**
     * Window shares locks event outside of queries. This test is to ensure that there is no
     * deadlock according to the implementation.
     *
     * @throws InterruptedException throw exception if interrupted the input handler sender.
     */
    @Test
    public void testJoinWindowWithWindowForDeadLock() throws InterruptedException {
        log.info("Test join window with another window for deadlock");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream TempStream(deviceID long, roomNo int, temp double); " +
                "define stream RegulatorStream(deviceID long, roomNo int, isOn bool); " +
                "define window TempWindow(deviceID long, roomNo int, temp double) time(1 min); " +
                "define window RegulatorWindow(deviceID long, roomNo int, isOn bool) length(1); " +
                "define window RegulatorActionWindow(deviceID long, roomNo int, action string) timeBatch(1 sec); ";

        String query = "" +
                "@info(name = 'query1') " +
                "from TempStream[temp > 30.0] " +
                "insert into TempWindow; " +
                "" +
                "@info(name = 'query2') " +
                "from RegulatorStream[isOn == false] " +
                "insert into RegulatorWindow; " +
                "" +
                "@info(name = 'query3') " +
                "from TempWindow " +
                "join RegulatorWindow " +
                "on TempWindow.roomNo == RegulatorWindow.roomNo " +
                "select RegulatorWindow.deviceID as deviceID, TempWindow.roomNo as roomNo, 'start' as action " +
                "insert into RegulatorActionWindow; " +
                "" +
                "@info(name = 'query5') " +
                "from RegulatorActionWindow join TempWindow " +
                "on TempWindow.roomNo == RegulatorActionWindow.roomNo " +
                "select RegulatorActionWindow.deviceID as deviceID, TempWindow.roomNo as roomNo, true as isOn " +
                "insert into OutputStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                inEventCount++;
            }
        });

        InputHandler tempStream = siddhiAppRuntime.getInputHandler("TempStream");
        InputHandler regulatorStream = siddhiAppRuntime.getInputHandler("RegulatorStream");

        siddhiAppRuntime.start();

        tempStream.send(new Object[]{100L, 1, 20.0});
        tempStream.send(new Object[]{100L, 2, 25.0});
        tempStream.send(new Object[]{100L, 3, 30.0});
        tempStream.send(new Object[]{100L, 4, 35.0});
        tempStream.send(new Object[]{100L, 5, 40.0});

        regulatorStream.send(new Object[]{100L, 1, false});
        regulatorStream.send(new Object[]{100L, 2, false});
        regulatorStream.send(new Object[]{100L, 3, false});
        regulatorStream.send(new Object[]{100L, 4, false});
        regulatorStream.send(new Object[]{100L, 5, false});

        Thread.sleep(1500);
        assertEquals("Number of success events", 2, inEventCount);
        siddhiAppRuntime.shutdown();
    }

    /**
     * Behaviour of traditional Window and Window are different in join  with themselves.
     * Traditional Windows joins always maintain two windows and the event is passed to them one after
     * the other. Therefore, when left window receives a current event right window will bbe empty and
     * if an event is expired from left window it can join with right window since it will not expire at the same
     * time. Since Window maintains only one instance, when an event arrives to the window it will
     * be compared to the internal event chunk, where the current event will match with itself.
     * When an event expires, it will be immediately removed from the internal event chunk so that,
     * when joined with the window, the expired event will not match with itself.
     *
     * @throws InterruptedException throw exception if interrupted the input handler sender.
     */
    @Test
    public void testJoinWindowWithSameWindow() throws InterruptedException {
        log.info("Test join window with the same window");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseEventWindow (symbol string, price float, volume int) length(2); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from cseEventStream " +
                "insert into " +
                "cseEventWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from cseEventWindow as a " +
                "join cseEventWindow as b " +
                "on a.symbol== b.symbol " +
                "select a.symbol as symbol, a.price as priceA, b.price as priceB " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timestamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        inEventCount += inEvents.length;
                    }
                    if (removeEvents != null) {
                        removeEventCount += removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler cseEventStreamHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
            siddhiAppRuntime.start();
            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});    // Match with itself
            cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});   // Match with itself
            // When the next event enters, the expired {"IBM", 75.6f, 100} will come out and joined
            // with the window which contains [{"WSO2", 57.6f, 100}, {"IBM", 59.6f, 100}] and latter the
            // current event {"IBM", 59.6f, 100} will match with the window.
            cseEventStreamHandler.send(new Object[]{"IBM", 59.6f, 100});
            Thread.sleep(1000);
            AssertJUnit.assertEquals(3, inEventCount);
            AssertJUnit.assertEquals(1, removeEventCount);
            AssertJUnit.assertTrue(eventArrived);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

}
