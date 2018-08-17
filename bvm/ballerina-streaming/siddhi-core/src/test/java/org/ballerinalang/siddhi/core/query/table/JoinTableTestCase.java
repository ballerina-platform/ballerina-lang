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
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testcase for table joins.
 */
public class JoinTableTestCase {
    private static final Logger log = LoggerFactory.getLogger(JoinTableTestCase.class);
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
    public void testTableJoinQuery1() throws InterruptedException {
        log.info("testTableJoinQuery1 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol2 string, price2 float, volume2 long); " +
                "define stream CheckStockStream (symbol1 string); " +
                "define table StockTable (symbol2 string, price2 float, volume2 long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTable " +
                "select symbol1, symbol2, volume2  " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM", 10L}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount);
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

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testTableJoinQuery2() throws InterruptedException {
        log.info("testTableJoinQuery2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, StockTable.volume as " +
                "volume  " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
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

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testTableJoinQuery3() throws InterruptedException {
        log.info("testTableJoinQuery3 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTable as t " +
                " on CheckStockStream.symbol!=t.symbol " +
                "select CheckStockStream.symbol as checkSymbol, t.symbol as symbol, t.volume as volume  " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM", 10L}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
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

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testTableJoinQuery4() throws InterruptedException {
        log.info("testTableJoinQuery4 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.time(1 sec) join StockTable " +
                " on CheckStockStream.symbol!=StockTable.symbol " +
                "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, StockTable.volume as " +
                "volume  " +
                "insert all events into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM", 200L}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    for (Event event : removeEvents) {
                        removeEventCount++;
                        switch (removeEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM", 200L}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, removeEventCount);
                        }
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 200L});
        checkStockStream.send(new Object[]{"WSO2"});

        Thread.sleep(1500);

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 1, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testTableJoinQuery5() throws InterruptedException {
        log.info("testTableJoinQuery5 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, StockTable.volume as " +
                "volume  " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                break;
                            case 2:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "IBM", 10L}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(2, inEventCount);
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

        AssertJUnit.assertEquals("Number of success events", 2, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testTableJoinQuery6() throws InterruptedException {
        log.info("testTableJoinQuery6 recursive join");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream RequestStream (start string, end string); " +
                "define stream TimeTableStream (start string, end string, elapsedTime int, startTime string); " +
                "define stream ResultStream (totalElapsedTime int); " +
                "define table TimeTable (start string, end string, elapsedTime int, startTime string);";

        String query = "" +
                "from TimeTableStream " +
                "select * " +
                "insert into TimeTable; " +
                "" +
                "from RequestStream join TimeTable " +
                "on TimeTable.start == RequestStream.start " +
                "select TimeTable.start as start, TimeTable.end as end, TimeTable.elapsedTime as elapsedTime, " +
                "RequestStream.end as destination " +
                "insert into intermediateResultStream;" +
                "" +
                "@info(name = 'query1') " +
                "from intermediateResultStream[end==destination] " +
                "select intermediateResultStream.elapsedTime as totalElapsedTime " +
                "insert into ResultStream;" +
                "" +
                "from intermediateResultStream[end!=destination] " +
                "insert into intermediateResultStream2; " +
                "" +
                "from intermediateResultStream2 join TimeTable " +
                "on TimeTable.start == intermediateResultStream2.end " +
                "select TimeTable.start as start, TimeTable.end as end, (intermediateResultStream2.elapsedTime + " +
                "TimeTable.elapsedTime) as elapsedTime, destination " +
                "insert into intermediateResultStream; ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
            }
        });

        InputHandler timeTableStream = siddhiAppRuntime.getInputHandler("TimeTableStream");
        InputHandler requestStream = siddhiAppRuntime.getInputHandler("RequestStream");

        siddhiAppRuntime.start();

        timeTableStream.send(new Object[]{"A", "B", 25, "1.27PM"});
        timeTableStream.send(new Object[]{"B", "C", 10, "1.52PM"});
        timeTableStream.send(new Object[]{"C", "D", 60, "2.52PM"});
        Thread.sleep(1000);

        requestStream.send(new Object[]{"A", "D"});

        Thread.sleep(1000);

        siddhiAppRuntime.shutdown();
        AssertJUnit.assertTrue("Events arrived", eventArrived);

    }

    @Test
    public void testTableJoinQuery7() throws InterruptedException {
        log.info("testTableJoinQuery7 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol2 string, price2 float, volume2 long); " +
                "define stream CheckStockStream (symbol1 string); " +
                "define table StockTable (symbol2 string, price2 float, volume2 long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTable " +
                "   on symbol1 == symbol2 " +
                "select symbol1, symbol2, volume2  " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                AssertJUnit.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                break;
                            default:
                                AssertJUnit.assertSame(1, inEventCount);
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

        AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();

    }

}
