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
package org.ballerinalang.siddhi.core.query.partition;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testcase for pattern queries within partition.
 */
public class PatternPartitionTestCase {
    private static final Logger log = LoggerFactory.getLogger(PatternPartitionTestCase.class);
    private AtomicInteger inEventCount;
    private AtomicInteger removeEventCount;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        inEventCount = new AtomicInteger(0);
        removeEventCount = new AtomicInteger(0);
        eventArrived = false;
    }


    @Test
    public void testPatternPartitionQuery1() throws InterruptedException {
        log.info("Partition - testPatternEvery1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";

        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";

        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] -> e2=Stream2[price>e1.price] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);

                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM"}, event.getData());
                        eventArrived = true;
                    }
                    eventArrived = true;
                }

            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});

        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery2() throws InterruptedException {
        log.info("Partition - testPatternEvery2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price1 float, volume int); ";

        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";

        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] -> e2=Stream2[price1>e1.price] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);

                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM"}, event.getData());
                        eventArrived = true;
                    }
                    eventArrived = true;
                }

            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});

        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery3() throws InterruptedException {
        log.info("Partition - testPatternEvery3 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price1 float, volume int); ";

        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";

        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20] -> e2=Stream2[price1>e1.price] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);

                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM"}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"GOOG", "IBM"}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM"}, event.getData());
                                break;
                            case 4:
                                AssertJUnit.assertArrayEquals(new Object[]{"GOOG", "IBM"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(4, inEventCount.get());
                        }
                        eventArrived = true;
                    }
                    eventArrived = true;
                }

            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 55.6f, 150});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 55.6f, 150});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 150});

        SiddhiTestHelper.waitForEvents(100, 4, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 4, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery4() throws InterruptedException {
        log.info("Partition - testPatternEvery4 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";

        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";

        String query = "" +
                "@info(name = 'query1') " +
                "from every ( e1=Stream1[price>20] -> e3=Stream1[price>20]) -> e2=Stream2[price>e1.price] " +
                "select e1.price as price1, e3.price as price3, e2.price as price2 " +
                "insert into OutputStream ;";

        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);

                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{55.6f, 54f, 57.7f}, event.getData());
                    }
                    eventArrived = true;
                }

            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 57.7f, 100});

        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery5() throws InterruptedException {
        log.info("Partition - testPatternEvery5 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";

        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";

        String query = "" +
                "@info(name = 'query1') " +
                "from every ( e1=Stream1[price>20] -> e3=Stream1[price>20]) -> e2=Stream2[price>e1.price] " +
                "select e1.price as price1, e3.price as price3, e2.price as price2 " +
                "insert into OutputStream ;";

        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);

                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 54f, 57.7f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{53.6f, 53f, 57.7f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }

            }
        });


        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 53.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 53f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 57.7f, 100});

        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery6() throws InterruptedException {
        log.info("Partition - testPatternEvery6 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";

        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";

        String query = "" +
                "@info(name = 'query1') " +
                "from e4=Stream1[symbol=='MSFT'] -> every ( e1=Stream1[price>20] -> e3=Stream1[price>20]) -> " +
                "   e2=Stream2[price>e1.price] " +
                "select e1.price as price1, e3.price as price3, e2.price as price2 " +
                "insert into OutputStream ;";

        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);

                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.7f, 54f, 57.7f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{53.6f, 53f, 57.7f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }

            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"MSFT", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 55.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 53.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 53f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 57.7f, 100});

        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery7() throws InterruptedException {
        log.info("Partition - testPatternEvery7 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";

        String query = "" +
                "@info(name = 'query1') " +
                "from  every ( e1=Stream1[price>20] -> e3=Stream1[price>20]) " +
                "select e1.price as price1, e3.price as price3 " +
                "insert into OutputStream ;";

        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 57.6f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{54f, 53.6f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });


        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"MSFT", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 53.6f, 100});
        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery8() throws InterruptedException {
        log.info("Partition - testPatternEvery8 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20] " +
                "select e1.price as price1 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{57.6f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"MSFT", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery9() throws InterruptedException {
        log.info("testPatternCount1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, 47.8f, null, 45.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });


        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.6f, 10});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 13.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.6f, 10});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 13.7f, 10});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.8f, 10});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 10});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 10});
        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery10() throws InterruptedException {
        log.info("testPatternCount2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, null, null, 45.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 13.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);
        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery11() throws InterruptedException {
        log.info("testPatternCount3 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.8f, null, null, 55.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery12() throws InterruptedException {
        log.info("testPatternCount4 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 0, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", false, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery13() throws InterruptedException {
        log.info("testPatternCount5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, 23.7f, 24.7f, 45.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 23.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 24.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 25.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 27.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery14() throws InterruptedException {
        log.info("testPatternCount6 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>e1[1].price] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e2.price as price2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, 55.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery15() throws InterruptedException {
        log.info("testPatternCount7 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <0:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e2.price as price2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{null, null, 45.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 45.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery16() throws InterruptedException {
        log.info("testPatternCount8 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <0:5> -> e2=Stream2[price>e1[0].price] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e2.price as price2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{25.6f, null, 45.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 7.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery17() throws InterruptedException {
        log.info("testPatternCount9 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int , quantity int); ";
        String partitionStart = "partition with (quantity of EventStream) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = EventStream [price >= 50 and volume > 100] -> e2 = EventStream [price <= 40] <0:5> " +
                "   -> e3 = EventStream [volume <= 70] " +
                "select e1.symbol as symbol1, e2[0].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", "GOOG", "WSO2"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 105, 5});
        Thread.sleep(100);
        eventStream.send(new Object[]{"GOOG", 21f, 81, 5});
        eventStream.send(new Object[]{"WSO2", 176.6f, 65, 5});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery18() throws InterruptedException {
        log.info("testPatternCount10 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int,quantity int); ";
        String partitionStart = "partition with (quantity of EventStream) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = EventStream [price >= 50 and volume > 100] -> e2 = EventStream [price <= 40] <:5> " +
                "   -> e3 = EventStream [volume <= 70] " +
                "select e1.symbol as symbol1, e2[0].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", null, "GOOG"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 105, 2});
        Thread.sleep(100);
        eventStream.send(new Object[]{"GOOG", 21f, 61, 2});
        eventStream.send(new Object[]{"WSO2", 21f, 61, 2});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery19() throws InterruptedException {
        log.info("testPatternCount11 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int, quantity int); ";
        String partitionStart = "partition with (quantity of EventStream) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = EventStream [price >= 50 and volume > 100] -> e2 = EventStream [price <= 40] <:5> " +
                "   -> e3 = EventStream [volume <= 70] " +
                "select e1.symbol as symbol1, e2[last].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", null, "GOOG"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 105, 4});
        Thread.sleep(100);
        eventStream.send(new Object[]{"GOOG", 21f, 61, 4});
        eventStream.send(new Object[]{"WSO2", 21f, 61, 4});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery20() throws InterruptedException {
        log.info("testPatternCount12 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int,quantity int); ";
        String partitionStart = "partition with (quantity of EventStream) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = EventStream [price >= 50 and volume > 100] -> e2 = EventStream [price <= 40] <:5> " +
                "   -> e3 = EventStream [volume <= 70] " +
                "select e1.symbol as symbol1, e2[last].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", "FB", "WSO2"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 105, 1});
        Thread.sleep(100);
        eventStream.send(new Object[]{"GOOG", 21f, 91, 1});
        eventStream.send(new Object[]{"FB", 21f, 81, 1});
        eventStream.send(new Object[]{"WSO2", 21f, 61, 1});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery21() throws InterruptedException {
        log.info("testPatternLogical1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price > 20] -> e2=Stream2[price > e1.price] or e3=Stream2['IBM' == symbol] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG"}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 59.6f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 59.6f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery22() throws InterruptedException {
        log.info("testPatternLogical2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price > 20] -> e2=Stream2[price > e1.price] or e3=Stream2['IBM' == symbol] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{"WSO2", null}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 10.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery23() throws InterruptedException {
        log.info("testPatternLogical3 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price > 20] -> e2=Stream2[price > e1.price] or e3=Stream2['IBM' == symbol] " +
                "select e1.symbol as symbol1, e2.price as price2, e3.price as price3 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 72.7f, null}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 72.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 75.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery24() throws InterruptedException {
        log.info("testPatternLogical4 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price > 20] -> e2=Stream2[price > e1.price] and e3=Stream2['IBM' == symbol] " +
                "select e1.symbol as symbol1, e2.price as price2, e3.price as price3 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 72.7f, 4.7f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", 72.7f, 4.7f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });
        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 72.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 4.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 55.6f, 10});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 72.7f, 10});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 4.7f, 10});
        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery25() throws InterruptedException {
        log.info("testPatternLogical5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price > 20] -> e2=Stream2[price > e1.price] and e3=Stream2['IBM' == symbol] " +
                "select e1.symbol as symbol1, e2.price as price2, e3.price as price3 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 72.7f, 72.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 72.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 75.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery26() throws InterruptedException {
        log.info("testPatternLogical6 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price > 20] -> e2=Stream2[price > e1.price] and e3=Stream1['IBM' == symbol] " +
                "select e1.symbol as symbol1, e2.price as price2, e3.price as price3 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{"WSO2", 72.7f, 75.7f}, event.getData());
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 72.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 75.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery27() throws InterruptedException {
        log.info("testPatternComplex1 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every ( e1=Stream1[price > 20] -> e2=Stream2[price > e1.price] or e3=Stream2['IBM' == symbol]) " +
                "  -> e4=Stream2[price > e1.price] " +
                "select e1.price as price1, e2.price as price2, e3.price as price3, e4.price as price4 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 55.7f, null, 57.7f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{54.0f, 57.7f, null, 59.7f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });


        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 55f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 57.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 59.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery28() throws InterruptedException {
        log.info("testPatternComplex2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every ( e1=Stream1[price > 20] -> e2=Stream1[price > 20]<1:2>) -> e3=Stream1[price > e1.price] " +
                "select e1.price as price1, e2[0].price as price2_0, e2[1].price as price2_1, e3.price as price3 " +
                "insert into OutputStream ;";
        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 54.0f, 53.6f, 57.0f},
                                        event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 53.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 57f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testPatternPartitionQuery29() throws InterruptedException {
        log.info("testPatternComplex3 - OUT 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int,quantity int); ";
        String partitionStart = "partition with (quantity of Stream1) begin ";
        String partitionEnd = "end";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1 = Stream1 [ price >= 50 and volume > 100 ] -> e2 = Stream1 [price <= 40 ] <2:> -> e3 =" +
                " Stream1 [volume <= 70 ] " +
                "select e1.symbol as symbol1, e2[last].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);


        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", "FB", "WSO2"}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"ADP", "WSO2", "AMZN"}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "QQQ", "CSCO"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(3, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 75.6f, 105, 1});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 39.8f, 91, 1});
        stream1.send(new Object[]{"FB", 35f, 81, 1});
        stream1.send(new Object[]{"WSO2", 21f, 61, 1});
        stream1.send(new Object[]{"ADP", 50f, 101, 1});
        stream1.send(new Object[]{"GOOG", 41.2f, 90, 1});
        stream1.send(new Object[]{"FB", 40f, 100, 1});
        stream1.send(new Object[]{"WSO2", 33.6f, 85, 1});
        stream1.send(new Object[]{"AMZN", 23.5f, 55, 1});
        stream1.send(new Object[]{"WSO2", 51.7f, 180, 1});
        stream1.send(new Object[]{"TXN", 34f, 61, 1});
        stream1.send(new Object[]{"QQQ", 24.6f, 45, 1});
        stream1.send(new Object[]{"CSCO", 181.6f, 40, 1});
        stream1.send(new Object[]{"WSO2", 53.7f, 200, 1});
        SiddhiTestHelper.waitForEvents(100, 3, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 3, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery30() throws InterruptedException {
        log.info("testPatternComplex4 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int,quantity int); " +
                "define stream Stream2 (symbol string, price float, volume int,quantity int); ";
        String partitionStart = "partition with (quantity of Stream1) begin ";
        String partitionEnd = "end";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1 = Stream1 [ price >= 50 and volume > 100 ] " +
                "   -> e2 = Stream2 [price <= 40 ] <1:> -> e3 = Stream2 [volume <= 70 ] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as symbol3 " +
                "insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "DDD", 60}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 75.6f, 105, 1});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 21f, 81, 1});
        stream2.send(new Object[]{"WSO2", 176.6f, 65, 1});
        stream1.send(new Object[]{"AMBA", 126.6f, 165, 1});
        stream2.send(new Object[]{"DDD", 23f, 181, 1});
        stream2.send(new Object[]{"BIRT", 21f, 86, 1});
        stream2.send(new Object[]{"BIRT", 21f, 82, 1});
        stream2.send(new Object[]{"WSO2", 176.6f, 60, 1});
        stream1.send(new Object[]{"AMBA", 126.6f, 165, 1});
        stream2.send(new Object[]{"DOX", 16.2f, 25, 1});
        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery31() throws InterruptedException {
        log.info("testPatternComplex5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int,quantity int); " +
                "define stream Stream2 (symbol string, price float, volume int,quantity int); ";
        String partitionStart = "partition with (quantity of Stream1,quantity of Stream2) begin ";
        String partitionEnd = "end";

        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = Stream1 [ price >= 50 and volume > 100 ] -> e2 = Stream2 [e1.symbol != 'AMBA' ] " +
                "   -> e3 = Stream2 [volume <= 70 ] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume3 " +
                "insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });


        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 75.6f, 105, 1});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 21f, 81, 1});
        stream2.send(new Object[]{"WSO2", 176.6f, 65, 1});
        stream1.send(new Object[]{"BIRT", 21f, 81, 1});
        stream1.send(new Object[]{"AMBA", 126.6f, 165, 1});
        stream2.send(new Object[]{"DDD", 23f, 181, 1});
        stream2.send(new Object[]{"BIRT", 21f, 86, 1});
        stream2.send(new Object[]{"BIRT", 21f, 82, 1});
        stream2.send(new Object[]{"WSO2", 176.6f, 60, 1});
        stream1.send(new Object[]{"AMBA", 126.6f, 165, 1});
        stream2.send(new Object[]{"DOX", 16.2f, 25, 1});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery32() throws InterruptedException {
        log.info("testPatternComplex6 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int, quantity int); " +
                "define stream Stream2 (symbol string, price float, volume int, quantity int); ";
        String partitionStart = "partition with (quantity of Stream1,quantity of Stream2) begin ";
        String partitionEnd = "end";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1 = Stream1 -> e2 = Stream2 [e1.symbol != 'AMBA' ] <2:> -> e3 = Stream2 [volume <= 70 ] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume3 " +
                "insert into #StockQuote;         @info(name = 'query2') from #StockQuote select symbol1, symbol2, " +
                "volume3 insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);


        siddhiAppRuntime.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        switch (inEventCount.get()) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBN", "DDD", 70}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount.get());
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 75.6f, 105, 1});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 21f, 51, 1});
        stream2.send(new Object[]{"FBX", 21f, 81, 1});
        stream2.send(new Object[]{"WSO2", 176.6f, 65, 1});
        stream1.send(new Object[]{"BIRT", 21f, 81, 1});
        stream1.send(new Object[]{"AMBA", 126.6f, 165, 1});
        stream2.send(new Object[]{"DDD", 23f, 181, 1});
        stream2.send(new Object[]{"BIRT", 21f, 86, 1});
        stream2.send(new Object[]{"IBN", 21f, 70, 1});
        stream2.send(new Object[]{"WSO2", 176.6f, 90, 1});
        stream1.send(new Object[]{"AMBA", 126.6f, 165, 1});
        stream2.send(new Object[]{"DOX", 16.2f, 25, 1});
        SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testPatternPartitionQuery33() throws InterruptedException {
        log.info("Partition - testPatternEvery1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int);    " +
                "define stream Stream2 (symbol string, price float, volume int); ";

        String partitionStart = "partition with (volume of Stream1,volume of Stream2) begin ";

        String query = "" +
                "@info(name = 'query2') " +
                "from  Stream1 select symbol, price  " +
                "insert into #Stream1 ;" +

                "@info(name = 'query1') " +
                "from e1=#Stream1[price>20] -> e2=Stream2[price>e1.price] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        String partitionEnd = "end";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + partitionStart
                + query + partitionEnd);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);

                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount.incrementAndGet();
                    } else {
                        inEventCount.incrementAndGet();
                        AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM"}, event.getData());
                        eventArrived = true;
                    }
                    eventArrived = true;
                }

            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        SiddhiTestHelper.waitForEvents(100, 1, inEventCount, 60000);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount.get());
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount.get());
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

}
