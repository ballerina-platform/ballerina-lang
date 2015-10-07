/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.wso2.siddhi.extension.eventtable.hazelcast;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class IndexedTableTestCase {
    private static final Logger log = Logger.getLogger(IndexedTableTestCase.class);
    private static final long EXEC_WAIT = 0;
    private static final long RESULT_WAIT = 500;
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }


    @Test
    public void indexedTableTest1() throws InterruptedException {
        log.info("indexedTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@from(eventtable = 'hazelcast')" +
                "@IndexBy('symbol') " +
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

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query3", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"IBM", 100l}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"WSO2", 100l}, event.getData());
                                break;
                            case 3:
                                Assert.assertArrayEquals(new Object[]{"IBM", 200l}, event.getData());
                                break;
                            case 4:
                                Assert.assertArrayEquals(new Object[]{"WSO2", 100l}, event.getData());
                                break;
                            default:
                                Assert.assertSame(4, inEventCount);
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

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");
        InputHandler updateStockStream = executionPlanRuntime.getInputHandler("UpdateStockStream");

        executionPlanRuntime.start();
        Thread.sleep(EXEC_WAIT);


        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        stockStream.send(new Object[]{"IBM", 55.6f, 100l});
        checkStockStream.send(new Object[]{"IBM", 100l});
        checkStockStream.send(new Object[]{"WSO2", 100l});
        updateStockStream.send(new Object[]{"IBM", 77.6f, 200l});
        Thread.sleep(2000);
        checkStockStream.send(new Object[]{"IBM", 100l});
        checkStockStream.send(new Object[]{"WSO2", 100l});


        Thread.sleep(RESULT_WAIT);

        Assert.assertEquals("Number of success events", 4, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void indexedTableTest2() throws InterruptedException {
        log.info("indexedTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "@from(eventtable = 'hazelcast')" +
                "@IndexBy('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query3", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"IBM", 300l}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
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

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

        executionPlanRuntime.start();
        Thread.sleep(EXEC_WAIT);

        stockStream.send(new Object[]{"IBM", 55.6f, 200l});
        stockStream.send(new Object[]{"IBM", 55.6f, 300l});
        checkStockStream.send(new Object[]{"IBM", 100l});

        Thread.sleep(RESULT_WAIT);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void indexedTableTest3() throws InterruptedException {
        log.info("indexedTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string); " +
                "@from(eventtable = 'hazelcast')" +
                "@IndexBy('symbol') " +
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
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query3", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"IBM", 300l}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
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

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");
        InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

        executionPlanRuntime.start();
        Thread.sleep(EXEC_WAIT);

        stockStream.send(new Object[]{"IBM", 55.6f, 200l});
        stockStream.send(new Object[]{"IBM", 55.6f, 300l});
        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        deleteStockStream.send(new Object[]{"WSO2"});
        checkStockStream.send(new Object[]{"IBM", 100l});

        Thread.sleep(RESULT_WAIT);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void indexedTableTest4() throws InterruptedException {
        log.info("indexedTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string); " +
                "@from(eventtable = 'hazelcast')" +
                "@IndexBy('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on symbol=='WSO2';" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query3", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");
        InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

        executionPlanRuntime.start();

        Thread.sleep(EXEC_WAIT);

        stockStream.send(new Object[]{"IBM", 55.6f, 200l});
        stockStream.send(new Object[]{"IBM", 55.6f, 300l});
        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        deleteStockStream.send(new Object[]{"WSO2"});
        checkStockStream.send(new Object[]{"IBM", 100l});

        Thread.sleep(RESULT_WAIT);

        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", false, eventArrived);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void indexedTableTest5() throws InterruptedException {
        log.info("indexedTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream DeleteStockStream (symbol string); " +
                "@from(eventtable = 'hazelcast')" +
                "@IndexBy('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.symbol=='WSO2';" +
                "" +
                "@info(name = 'query3') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query3", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"IBM", 300l}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
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

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");
        InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

        executionPlanRuntime.start();
        Thread.sleep(EXEC_WAIT);

        stockStream.send(new Object[]{"IBM", 55.6f, 200l});
        stockStream.send(new Object[]{"IBM", 55.6f, 300l});
        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        deleteStockStream.send(new Object[]{"IBM"});
        checkStockStream.send(new Object[]{"IBM", 100l});

        Thread.sleep(RESULT_WAIT);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();

    }

}
