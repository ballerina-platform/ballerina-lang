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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testcase for sequence queries within partition.
 */
public class SequencePartitionTestCase {

    private static final Logger log = LoggerFactory.getLogger(SequencePartitionTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testSequencePartitionQuery1() throws InterruptedException {
        log.info("Pattern -testSequence1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20],e2=Stream2[price>e1.price] " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"BIRT", "GOOG"}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount);
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
        stream1.send(new Object[]{"BIRT", 55.6f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 55.7f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery2() throws InterruptedException {
        log.info("Pattern -testSequence2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], e2=Stream2[price>e1.price] " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        AssertJUnit.assertArrayEquals(new Object[]{"GOOG", "IBM"}, event.getData());
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
        stream1.send(new Object[]{"GOOG", 57.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 65.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 57.6f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 65.7f, 300});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testSequencePartitionQuery3() throws InterruptedException {
        log.info("Pattern -testSequence3 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], e2=Stream2[price>e1.price]* " +
                "select e1.symbol as symbol1, e2[0].symbol as symbol2, e2[1].symbol as symbol3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", null, null}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", null, null}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{"BIRT", null, null}, event.getData());
                                break;
                            case 4:
                                AssertJUnit.assertArrayEquals(new Object[]{"GOOG", null, null}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(4, inEventCount);
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
        stream1.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"BIRT", 55.6f, 200});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 55.7f, 200});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 4, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery4() throws InterruptedException {
        log.info("Pattern -testSequence4 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20]*, e2=Stream1[price>e1[0].price] " +
                "select e1[0].price as price1, e1[1].price as price2, e2.price as price3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 55.7f, 57.6f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{65.6f, 75.7f, 87.6f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });


        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"BIRT", 69.6f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"BIRT", 65.6f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 75.7f, 200});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"BIRT", 87.6f, 200});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery5() throws InterruptedException {
        log.info("Pattern -testSequence5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20]*, e2=Stream1[price>e1[0].price] " +
                "select e1[0].price as price1, e1[1].price as price2, e2.price as price3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 55.0f, 57.6f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{85.6f, 85.0f, 87.6f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 85.6f, 1000});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 85.0f, 1000});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 87.6f, 1000});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery6() throws InterruptedException {
        log.info("Pattern -testSequence6 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20]?, e2=Stream1[price>e1[0].price] " +
                "select e1[0].price as price1, e2.price as price3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;

                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 200});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 0, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", false, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery7() throws InterruptedException {
        log.info("Pattern -testSequence7 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20], e2=Stream2[price>e1.price] or e3=Stream2[symbol=='IBM'] " +
                "select e1.price as price1, e2.price as price2, e3.price as price3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 55.7f, null}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{55.7f, 57.6f, null}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 155.7f, null}, event.getData());
                                break;
                            case 4:
                                AssertJUnit.assertArrayEquals(new Object[]{155.7f, 457.6f, null}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(4, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream2.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 599.6f, 4100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 4100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 155.7f, 4100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 457.6f, 4100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 4, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery8() throws InterruptedException {
        log.info("Pattern -testSequence8 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20], e2=Stream2[price>e1.price] or e3=Stream2[symbol=='IBM'] " +
                "select e1.price as price1, e2.price as price2, e3.price as price3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, null, 55.0f}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{155.6f, null, 95.0f}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{55.0f, 57.6f, null}, event.getData());
                                break;
                            case 4:
                                AssertJUnit.assertArrayEquals(new Object[]{95.0f, 207.6f, null}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(4, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream2.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 259.6f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 155.6f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 95.0f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 207.6f, 200});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 4, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery9() throws InterruptedException {
        log.info("Pattern -testSequence9 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20], e2=Stream2[price>e1.price] or e3=Stream2[symbol=='IBM'] " +
                "select e1.price as price1, e2.price as price2, e3.price as price3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, 57.6f, null}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{57.6f, null, 55.7f}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{155.6f, 207.6f, null}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(3, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream2.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 155.6f, 200});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 207.6f, 200});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 3, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery10() throws InterruptedException {
        log.info("Pattern -testSequence10 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20]+, e2=Stream1[price>e1[0].price] " +
                "select e1[0].price as price1, e1[1].price as price2, e2.price as price3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{55.6f, null, 57.6f}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 120});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 150});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery11() throws InterruptedException {
        log.info("Pattern -testSequence11 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String partitionStart = "partition with (volume of Stream1 , volume of Stream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[((e2[last].price is null) and price>=e1.price) or ((not (e2[last].price is null)) and " +
                "price>=e2[last].price)]+, " +
                "   e3=Stream1[price<e2[last].price] " +
                "select e1.price as price1, e2[0].price as price2, e2[1].price as price3, e3.price as price4 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{29.6f, 35.6f, 57.6f, 47.6f},
                                        event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{129.6f, 135.6f, 157.6f, 147.6f},
                                        event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 47.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 129.6f, 10});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 135.6f, 10});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 157.6f, 10});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 147.6f, 10});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery12() throws InterruptedException {
        log.info("Pattern -testSequence12 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume int,name String); " +
                "define stream TwitterStream (symbol string, count int,user String); ";
        String partitionStart = "partition with (name of StockStream , user of TwitterStream) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream[ price >= 50 and volume > 100 ], e2=TwitterStream[count > 10] " +
                "select e1.price as price, e1.symbol as symbol, e2.count as count " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{76.6f, "IBM", 20}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
        InputHandler twitterStream = siddhiAppRuntime.getInputHandler("TwitterStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"IBM", 75.6f, 105, "user"});
        stockStream.send(new Object[]{"GOOG", 51f, 101, "user"});
        stockStream.send(new Object[]{"IBM", 76.6f, 111, "user"});
        stockStream.send(new Object[]{"IBM", 76.6f, 111, "user2"});
        Thread.sleep(100);
        twitterStream.send(new Object[]{"IBM", 20, "user"});
        stockStream.send(new Object[]{"WSO2", 45.6f, 100, "user"});
        Thread.sleep(100);
        twitterStream.send(new Object[]{"GOOG", 20, "user"});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testSequencePartitionQuery13() throws InterruptedException {
        log.info("Pattern -testSequence13 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume int, name String); " +
                "define stream TwitterStream (symbol string, count int, user String); ";
        String partitionStart = "partition with (name of StockStream , user of TwitterStream) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream[ price >= 50 and volume > 100 ], e2=StockStream[price <= 40]*, " +
                "e3=StockStream[volume <= 70] " +
                "select e1.symbol as symbol1, e2[0].symbol as symbol2, e3.symbol as symbol3 " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", "GOOG", "WSO2"}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"GOOG", "BIRT", "DDD"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });


        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
        InputHandler twitterStream = siddhiAppRuntime.getInputHandler("TwitterStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"IBM", 75.6f, 105, "user"});
        Thread.sleep(100);
        stockStream.send(new Object[]{"GOOG", 21f, 81, "user"});
        stockStream.send(new Object[]{"WSO2", 176.6f, 65, "user"});
        Thread.sleep(100);
        stockStream.send(new Object[]{"GOOG", 75.6f, 105, "user2"});
        Thread.sleep(100);
        stockStream.send(new Object[]{"BIRT", 21f, 81, "user2"});
        stockStream.send(new Object[]{"DDD", 176.6f, 65, "user2"});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery14() throws InterruptedException {
        log.info("Pattern -testSequence14 - OUT 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream1 (symbol string, price float, volume int, quantity int); " +
                "define stream StockStream2 (symbol string, price float, volume int, quantity int); ";
        String partitionStart = "partition with (quantity of StockStream1 , quantity of StockStream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream1[ price >= 50 and volume > 100 ], e2=StockStream2[price <= 40]*, " +
                "e3=StockStream2[volume <= 70] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "DDD", 60}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{"DOX", null, 25}, event.getData());
                                break;
                            case 4:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 5:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "DDD", 60}, event.getData());
                                break;
                            case 6:
                                AssertJUnit.assertArrayEquals(new Object[]{"DOX", null, 25}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(6, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stockStream1 = siddhiAppRuntime.getInputHandler("StockStream1");
        InputHandler stockStream2 = siddhiAppRuntime.getInputHandler("StockStream2");

        siddhiAppRuntime.start();

        stockStream1.send(new Object[]{"IBM", 75.6f, 105, 2});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81, 2});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65, 2});
        stockStream1.send(new Object[]{"BIRT", 21f, 81, 2});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 2});
        stockStream2.send(new Object[]{"DDD", 23f, 181, 2});
        stockStream2.send(new Object[]{"BIRT", 21f, 86, 2});
        stockStream2.send(new Object[]{"BIRT", 21f, 82, 2});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60, 2});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 2});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25, 2});
        Thread.sleep(100);

        stockStream1.send(new Object[]{"IBM", 75.6f, 105, 22});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81, 22});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65, 22});
        stockStream1.send(new Object[]{"BIRT", 21f, 81, 22});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 22});
        stockStream2.send(new Object[]{"DDD", 23f, 181, 22});
        stockStream2.send(new Object[]{"BIRT", 21f, 86, 22});
        stockStream2.send(new Object[]{"BIRT", 21f, 82, 22});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60, 22});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 22});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25, 22});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 6, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery15() throws InterruptedException {
        log.info("Pattern -testSequence15 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream1 (symbol string, price float, volume int,quantity int); " +
                "define stream StockStream2 (symbol string, price float, volume int,quantity int); ";
        String partitionStart = "partition with (quantity of StockStream1 , quantity of StockStream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream1[ price >= 50 and volume > 100 ], e2=StockStream2[e1.symbol != 'AMBA']*, " +
                "e3=StockStream2[volume <= 70] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"DOX", null, 25}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 4:
                                AssertJUnit.assertArrayEquals(new Object[]{"DOX", null, 25}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(4, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler stockStream1 = siddhiAppRuntime.getInputHandler("StockStream1");
        InputHandler stockStream2 = siddhiAppRuntime.getInputHandler("StockStream2");

        siddhiAppRuntime.start();

        stockStream1.send(new Object[]{"IBM", 75.6f, 105, 10});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81, 10});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65, 10});
        stockStream1.send(new Object[]{"BIRT", 21f, 81, 10});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 10});
        stockStream2.send(new Object[]{"DDD", 23f, 181, 10});
        stockStream2.send(new Object[]{"BIRT", 21f, 86, 10});
        stockStream2.send(new Object[]{"BIRT", 21f, 82, 10});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60, 10});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 10});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25, 10});
        Thread.sleep(100);

        stockStream1.send(new Object[]{"IBM", 75.6f, 105, 100});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81, 100});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65, 100});
        stockStream1.send(new Object[]{"BIRT", 21f, 81, 100});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 100});
        stockStream2.send(new Object[]{"DDD", 23f, 181, 100});
        stockStream2.send(new Object[]{"BIRT", 21f, 86, 100});
        stockStream2.send(new Object[]{"BIRT", 21f, 82, 100});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60, 100});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 100});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 4, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testSequencePartitionQuery16() throws InterruptedException {
        log.info("Pattern -testSequence16 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream1 (symbol string, price float, volume int, quantity int); " +
                "define stream StockStream2 (symbol string, price float, volume int, quantity int); ";
        String partitionStart = "partition with (quantity of StockStream1 , quantity of StockStream2) begin ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream1, e2=StockStream2[e1.symbol != 'AMBA']*, e3=StockStream2[volume <= 70] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume, e1.quantity as quantity " +
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
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65, 5}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65, 155}, event.getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{"DOX", null, 25, 5}, event.getData());
                                break;
                            case 4:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65, 55}, event.getData());
                                break;
                            case 5:
                                AssertJUnit.assertArrayEquals(new Object[]{"DOX", null, 25, 55}, event.getData());
                                break;
                            case 6:
                                AssertJUnit.assertArrayEquals(new Object[]{"DOX", null, 25, 155}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(6, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
            }
        });


        InputHandler stockStream1 = siddhiAppRuntime.getInputHandler("StockStream1");
        InputHandler stockStream2 = siddhiAppRuntime.getInputHandler("StockStream2");

        siddhiAppRuntime.start();

        stockStream1.send(new Object[]{"IBM", 75.6f, 105, 5});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81, 5});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65, 5});
        stockStream1.send(new Object[]{"BIRT", 21f, 81, 5});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 5});

        stockStream1.send(new Object[]{"IBM", 75.6f, 105, 155});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81, 155});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65, 155});
        stockStream1.send(new Object[]{"BIRT", 21f, 81, 155});

        stockStream2.send(new Object[]{"DDD", 23f, 181, 5});
        stockStream2.send(new Object[]{"BIRT", 21f, 86, 5});
        stockStream2.send(new Object[]{"BIRT", 21f, 82, 5});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60, 5});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 5});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25, 5});
        Thread.sleep(100);

        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 155});
        stockStream2.send(new Object[]{"DDD", 23f, 181, 155});
        stockStream2.send(new Object[]{"BIRT", 21f, 86, 155});
        stockStream2.send(new Object[]{"BIRT", 21f, 82, 155});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60, 155});

        stockStream1.send(new Object[]{"IBM", 75.6f, 105, 55});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81, 55});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65, 55});
        stockStream1.send(new Object[]{"BIRT", 21f, 81, 55});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 55});
        stockStream2.send(new Object[]{"DDD", 23f, 181, 55});
        stockStream2.send(new Object[]{"BIRT", 21f, 86, 55});
        stockStream2.send(new Object[]{"BIRT", 21f, 82, 55});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60, 55});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 55});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25, 55});
        Thread.sleep(100);

        stockStream1.send(new Object[]{"AMBA", 126.6f, 165, 155});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25, 155});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 6, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }
}

