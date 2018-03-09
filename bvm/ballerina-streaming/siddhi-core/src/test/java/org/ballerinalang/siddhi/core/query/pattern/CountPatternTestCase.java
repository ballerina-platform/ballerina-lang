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

package org.ballerinalang.siddhi.core.query.pattern;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CountPatternTestCase {

    private static final Logger log = LoggerFactory.getLogger(CountPatternTestCase.class);
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
    public void testQuery1() throws InterruptedException {
        log.info("testPatternCount1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, 47.8f, null, 45.7f},
                            inEvents[0].getData());
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
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
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("testPatternCount2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, null, null, 45.7f}, inEvents[0].getData());
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
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
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("testPatternCount3 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.8f, null, null, 55.7f}, inEvents[0].getData());
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
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
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("testPatternCount4 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                AssertJUnit.fail();
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 0, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", false, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery5() throws InterruptedException {
        log.info("testPatternCount5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, 23.7f, 24.7f, 45.7f},
                            inEvents[0].getData());
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
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
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery6() throws InterruptedException {
        log.info("testPatternCount6 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>e1[1].price] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, 55.7f}, inEvents[0].getData());
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
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
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery7() throws InterruptedException {
        log.info("testPatternCount7 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <0:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertArrayEquals(new Object[]{null, null, 45.7f}, inEvents[0].getData());
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery8() throws InterruptedException {
        log.info("testPatternCount8 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <0:5> -> e2=Stream2[price>e1[0].price] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e2.price as price2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, null, 45.7f}, inEvents[0].getData());
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
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
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery9() throws InterruptedException {
        log.info("testPatternCount9 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = EventStream [price >= 50 and volume > 100] -> e2 = EventStream [price <= 40] <0:5> " +
                "   -> e3 = EventStream [volume <= 70] " +
                "select e1.symbol as symbol1, e2[0].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", "GOOG", "WSO2"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        eventStream.send(new Object[]{"GOOG", 21f, 81});
        eventStream.send(new Object[]{"WSO2", 176.6f, 65});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery10() throws InterruptedException {
        log.info("testPatternCount10 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = EventStream [price >= 50 and volume > 100] -> e2 = EventStream [price <= 40] <:5> " +
                "   -> e3 = EventStream [volume <= 70] " +
                "select e1.symbol as symbol1, e2[0].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", null, "GOOG"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        eventStream.send(new Object[]{"GOOG", 21f, 61});
        eventStream.send(new Object[]{"WSO2", 21f, 61});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery11() throws InterruptedException {
        log.info("testPatternCount11 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = EventStream [price >= 50 and volume > 100] -> e2 = EventStream [price <= 40] <:5> " +
                "   -> e3 = EventStream [volume <= 70] " +
                "select e1.symbol as symbol1, e2[last].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", null, "GOOG"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        eventStream.send(new Object[]{"GOOG", 21f, 61});
        eventStream.send(new Object[]{"WSO2", 21f, 61});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery12() throws InterruptedException {
        log.info("testPatternCount12 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = EventStream [price >= 50 and volume > 100] -> e2 = EventStream [price <= 40] <:5> " +
                "   -> e3 = EventStream [volume <= 70] " +
                "select e1.symbol as symbol1, e2[last].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"IBM", "FB", "WSO2"}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        eventStream.send(new Object[]{"GOOG", 21f, 91});
        eventStream.send(new Object[]{"FB", 21f, 81});
        eventStream.send(new Object[]{"WSO2", 21f, 61});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery13() throws InterruptedException {
        log.info("testPatternCount13 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream EventStream (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1 = EventStream -> " +
                "     e2 = EventStream [e1.symbol==e2.symbol]<4:6> " +
                "select e1.volume as volume1, e2[0].volume as volume2, e2[1].volume as volume3, e2[2].volume as " +
                "volume4, e2[3].volume as volume5, e2[4].volume as volume6, e2[5].volume as volume7 " +
                "insert into StockQuote;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{100, 200, 300, 400, 500, null, null}, event
                                        .getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{200, 300, 400, 500, 600, null, null}, event
                                        .getData());
                                break;
                            case 3:
                                AssertJUnit.assertArrayEquals(new Object[]{300, 400, 500, 600, 700, null, null}, event
                                        .getData());
                                break;
                            case 4:
                                AssertJUnit.assertArrayEquals(new Object[]{400, 500, 600, 700, 800, null, null}, event
                                        .getData());
                                break;
                            case 5:
                                AssertJUnit.assertArrayEquals(new Object[]{500, 600, 700, 800, 900, null, null}, event
                                        .getData());
                                break;
                            default:
                                AssertJUnit.assertSame(5, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler eventStream = siddhiAppRuntime.getInputHandler("EventStream");

        siddhiAppRuntime.start();

        eventStream.send(new Object[]{"IBM", 75.6f, 100});
        eventStream.send(new Object[]{"IBM", 75.6f, 200});
        eventStream.send(new Object[]{"IBM", 75.6f, 300});
        eventStream.send(new Object[]{"GOOG", 21f, 91});
        eventStream.send(new Object[]{"IBM", 75.6f, 400});
        eventStream.send(new Object[]{"IBM", 75.6f, 500});

        eventStream.send(new Object[]{"GOOG", 21f, 91});

        eventStream.send(new Object[]{"IBM", 75.6f, 600});
        eventStream.send(new Object[]{"IBM", 75.6f, 700});
        eventStream.send(new Object[]{"IBM", 75.6f, 800});
        eventStream.send(new Object[]{"GOOG", 21f, 91});
        eventStream.send(new Object[]{"IBM", 75.6f, 900});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 5, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testQuery14() throws InterruptedException {
        log.info("testPatternCount14 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <0:5> -> e2=Stream2[price>e1[0].price] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, e2.price as price2" +
                " " +
                "having instanceOfFloat(e1[1].price) and not instanceOfFloat(e1[2].price) and instanceOfFloat" +
                "(price1_1) and not instanceOfFloat(price1_2) " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, 23.6f, null, 45.7f}, inEvents[0].getData());
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 23.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 7.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }
}
