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

public class WindowPartitionTestCase {
    private static final Logger log = LoggerFactory.getLogger(WindowPartitionTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;
    private boolean firstEvent;

    @BeforeMethod
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
        firstEvent = true;
    }


    @Test
    public void testWindowPartitionQuery1() throws InterruptedException {
        log.info("Window Partition test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream) begin @info(name = 'query1') from cseEventStream#window" +
                ".length(2)  select symbol,sum(price) as price,volume insert expired events into OutStockStream ;  " +
                "end ";


        SiddhiAppRuntime executionRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    removeEventCount++;
                    if (removeEventCount == 1) {
                        AssertJUnit.assertEquals(100.0, event.getData()[1]);
                    } else if (removeEventCount == 2) {
                        AssertJUnit.assertEquals(1000.0, event.getData()[1]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        executionRuntime.start();
        inputHandler.send(new Object[]{"IBM", 70f, 100});
        inputHandler.send(new Object[]{"WSO2", 700f, 100});
        inputHandler.send(new Object[]{"IBM", 100f, 100});
        inputHandler.send(new Object[]{"IBM", 200f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 1000f, 100});
        inputHandler.send(new Object[]{"WSO2", 500f, 100});

        Thread.sleep(1000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(2, removeEventCount);
        executionRuntime.shutdown();

    }

    @Test
    public void testWindowPartitionQuery2() throws InterruptedException {
        log.info("Window Partition test2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream) begin @info(name = 'query1') from cseEventStream#window" +
                ".lengthBatch(2)  select symbol,sum(price) as price,volume insert all events into OutStockStream ;  " +
                "end ";


        SiddhiAppRuntime executionRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    inEventCount++;
                    eventArrived = true;
                    if (inEventCount == 1) {
                        AssertJUnit.assertEquals(170.0, event.getData()[1]);
                    } else if (inEventCount == 2) {
                        AssertJUnit.assertEquals(1700.0, event.getData()[1]);
                    }
                }

            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        executionRuntime.start();
        inputHandler.send(new Object[]{"IBM", 70f, 100});
        inputHandler.send(new Object[]{"WSO2", 700f, 100});
        inputHandler.send(new Object[]{"IBM", 100f, 100});
        inputHandler.send(new Object[]{"IBM", 200f, 100});
        inputHandler.send(new Object[]{"WSO2", 1000f, 100});

        Thread.sleep(2000);
        AssertJUnit.assertEquals(2, inEventCount);
        executionRuntime.shutdown();

    }

    @Test
    public void testWindowPartitionQuery3() throws InterruptedException {
        log.info("Window Partition test3");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "define stream cseEventStream (symbol string, price float,volume int);" +
                "" +
                "partition with (symbol of cseEventStream) " +
                "begin " +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec)  " +
                "select symbol, default( sum(price), 0.0) as price, volume " +
                "insert all events into OutStockStream ;  " +
                "end ";


        SiddhiAppRuntime executionRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    if (event.getData()[0].equals("WSO2")) {
                        inEventCount++;
                        if (inEventCount == 1) {
                            AssertJUnit.assertEquals(700.0, event.getData()[1]);
                        } else if (inEventCount == 2) {
                            AssertJUnit.assertEquals(0.0, event.getData()[1]);
                        } else if (inEventCount == 3) {
                            AssertJUnit.assertEquals(1000.0, event.getData()[1]);
                        } else if (inEventCount == 4) {
                            AssertJUnit.assertEquals(0.0, event.getData()[1]);
                        }
                    } else {
                        removeEventCount++;
                        if (removeEventCount == 1) {
                            AssertJUnit.assertEquals(70.0, event.getData()[1]);
                        } else if (removeEventCount == 2) {
                            AssertJUnit.assertEquals(170.0, event.getData()[1]);
                        } else if (removeEventCount == 3) {
                            AssertJUnit.assertEquals(100.0, event.getData()[1]);
                        } else if (removeEventCount == 4) {
                            AssertJUnit.assertEquals(0.0, event.getData()[1]);
                        } else if (removeEventCount == 5) {
                            AssertJUnit.assertEquals(200.0, event.getData()[1]);
                        } else if (removeEventCount == 6) {
                            AssertJUnit.assertEquals(0.0, event.getData()[1]);
                        }
                    }
                }
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        executionRuntime.start();
        inputHandler.send(new Object[]{"IBM", 70f, 100});
        inputHandler.send(new Object[]{"WSO2", 700f, 100});
        inputHandler.send(new Object[]{"IBM", 100f, 200});

        Thread.sleep(3000);
        inputHandler.send(new Object[]{"IBM", 200f, 300});
        inputHandler.send(new Object[]{"WSO2", 1000f, 100});

        Thread.sleep(2000);
        executionRuntime.shutdown();
        AssertJUnit.assertTrue(inEventCount == 4);
        AssertJUnit.assertTrue(removeEventCount == 6);
        AssertJUnit.assertTrue(eventArrived);


    }


    @Test
    public void testWindowPartitionQuery4() throws InterruptedException {
        log.info("Window Partition test4");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "define stream cseEventStream (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream) begin @info(name = 'query1') from cseEventStream#window" +
                ".length(2)  select symbol,sum(price) as price,volume insert into OutStockStream ;  end ";


        SiddhiAppRuntime executionRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        if (inEventCount == 1) {
                            AssertJUnit.assertEquals(70.0, event.getData()[1]);
                        } else if (inEventCount == 2) {
                            AssertJUnit.assertEquals(700.0, event.getData()[1]);
                        } else if (inEventCount == 3) {
                            AssertJUnit.assertEquals(170.0, event.getData()[1]);
                        } else if (inEventCount == 4) {
                            AssertJUnit.assertEquals(300.0, event.getData()[1]);
                        } else if (inEventCount == 5) {
                            AssertJUnit.assertEquals(75.5999984741211, event.getData()[1]);
                        } else if (inEventCount == 6) {
                            AssertJUnit.assertEquals(1700.0, event.getData()[1]);
                        } else if (inEventCount == 7) {
                            AssertJUnit.assertEquals(1500.0, event.getData()[1]);
                        }
                    }


                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        executionRuntime.start();
        inputHandler.send(new Object[]{"IBM", 70f, 100});
        inputHandler.send(new Object[]{"WSO2", 700f, 100});
        inputHandler.send(new Object[]{"IBM", 100f, 100});
        inputHandler.send(new Object[]{"IBM", 200f, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 1000f, 100});
        inputHandler.send(new Object[]{"WSO2", 500f, 100});

        Thread.sleep(1000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertTrue(7 >= inEventCount);
        AssertJUnit.assertEquals(0, removeEventCount);
        executionRuntime.shutdown();

    }


    @Test
    public void testWindowPartitionQuery5() throws InterruptedException {
        log.info("Window Partition test5");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "define stream cseEventStream (symbol string, price double,volume int);"
                + "" +
                "partition with (symbol of cseEventStream) " +
                "begin " +
                "   @info(name = 'query1') " +
                "   from cseEventStream#window.timeBatch(5 sec)  " +
                "   select symbol, sum(price) as price, volume " +
                "   insert into OutStockStream ;  " +
                "end ";


        SiddhiAppRuntime executionRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);


        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                        if ("IBM".equals(event.getData()[0])) {
                            AssertJUnit.assertEquals(370.0, event.getData()[1]);
                        } else if ("WSO2".equals(event.getData()[0])) {
                            AssertJUnit.assertEquals(2200.0, event.getData()[1]);
                        } else if ("ORACLE".equals(event.getData()[0])) {
                            AssertJUnit.assertEquals(75.6, event.getData()[1]);
                        }
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        executionRuntime.start();
        inputHandler.send(new Object[]{"IBM", 70.0, 100});
        inputHandler.send(new Object[]{"WSO2", 700.0, 100});
        inputHandler.send(new Object[]{"IBM", 100.0, 100});
        inputHandler.send(new Object[]{"IBM", 200.0, 100});
        inputHandler.send(new Object[]{"ORACLE", 75.6, 100});
        inputHandler.send(new Object[]{"WSO2", 1000.0, 100});
        inputHandler.send(new Object[]{"WSO2", 500.0, 100});

        Thread.sleep(7000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertTrue(7 >= inEventCount);
        AssertJUnit.assertEquals(0, removeEventCount);
        executionRuntime.shutdown();

    }


}
