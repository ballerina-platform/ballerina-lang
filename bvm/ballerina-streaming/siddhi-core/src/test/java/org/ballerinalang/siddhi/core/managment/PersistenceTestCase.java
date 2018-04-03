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

package org.ballerinalang.siddhi.core.managment;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import org.ballerinalang.siddhi.core.exception.NoPersistenceStoreException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.ballerinalang.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.ballerinalang.siddhi.core.util.persistence.PersistenceStore;
import org.ballerinalang.siddhi.core.util.snapshot.PersistenceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistenceTestCase {
    private static final Logger log = LoggerFactory.getLogger(PersistenceTestCase.class);
    private int count;
    private boolean eventArrived;
    private long firstValue;
    private Long lastValue;
    private AtomicInteger atomicCount;
    private int lastValueRemoved;

    @BeforeMethod
    public void init() {
        atomicCount = new AtomicInteger(0);
        count = 0;
        eventArrived = false;
        firstValue = 0;
        lastValue = 0L;
        lastValueRemoved = 0;
    }

    @Test
    public void persistenceTest1() throws InterruptedException {
        log.info("persistence test 1 - window query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume int );" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream[price>10]#window.length(10) " +
                "select symbol, price, sum(volume) as totalVol " +
                "insert into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
                    lastValue = (Long) inEvent.getData(2);
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(new Long(200), lastValue);

        //persisting
        Thread.sleep(500);
        siddhiAppRuntime.persist();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(10);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertTrue(count <= 6);
        AssertJUnit.assertEquals(new Long(400), lastValue);
        AssertJUnit.assertEquals(true, eventArrived);

    }

    @Test(dependsOnMethods = "persistenceTest1")
    public void persistenceTest2() throws InterruptedException {
        log.info("persistence test 2 - pattern count query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";


        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, null, null, 45.7f}, inEvent.getData());
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2;
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 13.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 0, count);
        AssertJUnit.assertEquals("Event arrived", false, eventArrived);

        //persisting
        Thread.sleep(500);
        siddhiAppRuntime.persist();

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertEquals("Number of success events", 1, count);
        AssertJUnit.assertEquals("Event arrived", true, eventArrived);

    }

    @Test(expectedExceptions = NoPersistenceStoreException.class, dependsOnMethods = "persistenceTest2")
    public void persistenceTest3() throws Exception {
        log.info("persistence test 3 - no store defined");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] <2:5> -> e2=Stream2[price>20] " +
                "select e1[0].price as price1_0, e1[1].price as price1_1, e1[2].price as price1_2, " +
                "   e1[3].price as price1_3, e2.price as price2 " +
                "insert into OutputStream ;";


        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertArrayEquals(new Object[]{25.6f, 47.6f, null, null, 45.7f}, inEvent.getData());
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 47.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 13.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 0, count);
        AssertJUnit.assertEquals("Event arrived", false, eventArrived);

        //persisting
        Thread.sleep(500);
        PersistenceReference persistenceReference = siddhiAppRuntime.persist();
        try {
            persistenceReference.getFuture().get();
        } catch (ExecutionException e) {
            throw e.getCause() instanceof NoPersistenceStoreException ? new NoPersistenceStoreException() : e;
        }

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

    }

    @Test(dependsOnMethods = "persistenceTest3")
    public void persistenceTest4() throws InterruptedException {
        log.info("persistence test 4 - window restart");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume int );" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream[price>10]#window.time(10 sec) " +
                "select symbol, price, sum(volume) as totalVol " +
                "insert into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
                    lastValue = (Long) inEvent.getData(2);
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(new Long(200), lastValue);

        //persisting
        Thread.sleep(500);
        siddhiAppRuntime.persist();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertEquals(new Long(400), lastValue);
        AssertJUnit.assertEquals(true, eventArrived);

    }

    @Test(dependsOnMethods = "persistenceTest4")
    public void persistenceTest5() throws InterruptedException {
        log.info("persistence test 5 - window restart expired event ");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume int );" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream[price>10]#window.time(10 sec) " +
                "select symbol, price, sum(volume) as totalVol " +
                "insert all events into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    for (Event inEvent : inEvents) {
                        count++;
                        AssertJUnit.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
                        firstValue = (Long) inEvent.getData(2);
                    }
                }
                if (removeEvents != null) {
                    for (Event removeEvent : removeEvents) {
                        count++;
                        lastValue = (Long) removeEvent.getData(2);
                    }
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(firstValue, 200);

        //persisting
        Thread.sleep(500);
        siddhiAppRuntime.persist();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        //shutdown siddhi app
        Thread.sleep(15000);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertEquals(400, firstValue);
        AssertJUnit.assertEquals(null, lastValue);
        AssertJUnit.assertEquals(true, eventArrived);

    }

    @Test(dependsOnMethods = "persistenceTest5")
    public void persistenceTest6() throws InterruptedException {
        log.info("persistence test 6 - batch window query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume int );" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream[price>10]#window.timeBatch(10) " +
                "select symbol, price, sum(volume) as totalVol " +
                "insert into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        Thread.sleep(500);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(2, count);

        //persisting
        Thread.sleep(500);
        siddhiAppRuntime.persist();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertEquals(count, 6);
        AssertJUnit.assertEquals(true, eventArrived);

    }


    @Test(dependsOnMethods = "persistenceTest6")
    public void persistenceTest7() throws InterruptedException {
        log.info("persistence test 7 - external time window with group by query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream (symbol string, price float, volume int, timestamp long);" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream#window.externalTime(timestamp,3 sec) " +
                "select symbol, price, sum(volume) as totalVol, timestamp " +
                "group by symbol " +
                "insert into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
                    if (count == 5) {
                        AssertJUnit.assertEquals(300L, inEvent.getData(2));
                    }
                    if (count == 6) {
                        AssertJUnit.assertEquals(100L, inEvent.getData(2));
                    }
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();
        long currentTime = 0;

        inputHandler.send(new Object[]{"IBM", 75.1f, 100, currentTime + 1000});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.2f, 100, currentTime + 2000});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"IBM", 75.3f, 100, currentTime + 3000});

        Thread.sleep(500);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);

        //persisting
        Thread.sleep(500);
        siddhiAppRuntime.persist();

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        inputHandler.send(new Object[]{"IBM", 75.4f, 100, currentTime + 4000});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"IBM", 75.5f, 100, currentTime + 5000});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, currentTime + 6000});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertEquals(count, 6);
        AssertJUnit.assertEquals(true, eventArrived);

    }

    @Test(dependsOnMethods = "persistenceTest7")
    public void persistenceTest8() throws InterruptedException {
        log.info("persistence test 8 - window query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume int );" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream[price>10]#window.length(10) " +
                "select symbol, price, sum(volume) as totalVol " +
                "insert into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
                    lastValue = (Long) inEvent.getData(2);
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(new Long(200), lastValue);

        //persisting
        Thread.sleep(500);
        byte[] snapshot = siddhiAppRuntime.snapshot();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restore(snapshot);
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(10);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertTrue(count == 6);
        AssertJUnit.assertEquals(new Long(400), lastValue);
        AssertJUnit.assertEquals(true, eventArrived);

    }

    @Test(dependsOnMethods = "persistenceTest8")
    public void persistenceTest9() throws InterruptedException {
        log.info("persistence test 9 - batch window query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume long );" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream[price>10]#window.timeBatch(300) " +
                "select * " +
                "insert all events into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    for (Event inEvent : inEvents) {
                        atomicCount.incrementAndGet();
                        AssertJUnit.assertTrue("IBM".equals(inEvent.getData(0)) ||
                                "WSO2".equals(inEvent.getData(0)));
                        lastValue = (Long) inEvent.getData(2);
                    }
                }

            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100L});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 101L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 102L});
        Thread.sleep(400);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 103L});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 104L});
        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);

        //persisting
        siddhiAppRuntime.persist();

        inputHandler.send(new Object[]{"IBM", 75.6f, 105L});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 106L});
        Thread.sleep(50);
        //restarting execution plan
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        inputHandler.send(new Object[]{"IBM", 75.6f, 107L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 108L});
        Thread.sleep(10);

        SiddhiTestHelper.waitForEvents(100, 7, atomicCount, 10000);
        AssertJUnit.assertEquals(7, atomicCount.get());

        //shutdown siddhi app
        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = "persistenceTest9")
    public void persistenceTest10() throws InterruptedException {
        log.info("persistence test 10 - sort window query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume int );" +
                "" +
                "@info(name = 'query1') " +
                "from StockStream#window.sort(2,volume) " +
                "select volume " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                atomicCount.incrementAndGet();
                for (Event inEvent : inEvents) {
                    count++;
                }

                if (removeEvents != null) {
                    for (Event removeEvent : removeEvents) {
                        lastValueRemoved = (Integer) removeEvent.getData(0);
                    }
                }
            }
        };
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 300});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 200});

        Thread.sleep(1000);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        // persisting
        siddhiAppRuntime.persist();

        inputHandler.send(new Object[]{"WSO2", 55.6f, 20});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 40});

        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();
        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        inputHandler.send(new Object[]{"WSO2", 55.6f, 20});

        SiddhiTestHelper.waitForEvents(100, 6, atomicCount, 10000);
        AssertJUnit.assertEquals(true, eventArrived);
        AssertJUnit.assertEquals(200, lastValueRemoved);
        siddhiAppRuntime.shutdown();

    }

    @Test(dependsOnMethods = "persistenceTest10")
    public void persistenceTest11() throws InterruptedException {
        log.info("persistence test 11 - batch window query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "" +
                "@app:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume long );" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream[price>10]#window.lengthBatch(2) " +
                "select *" +
                "insert all events into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                atomicCount.incrementAndGet();
                for (Event inEvent : inEvents) {
                    AssertJUnit.assertTrue("IBM".equals(inEvent.getData(0)) ||
                            "WSO2".equals(inEvent.getData(0)));
                }

                if (removeEvents != null) {
                    for (Event removeEvent : removeEvents) {
                        lastValue = (Long) removeEvent.getData(2);
                    }
                }
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100L});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 101L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 102L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 103L});
        SiddhiTestHelper.waitForEvents(100, 2, atomicCount, 10000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(new Long(101), lastValue);

        //persisting
        siddhiAppRuntime.persist();
        Thread.sleep(500);

        inputHandler.send(new Object[]{"WSO2", 75.6f, 50L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 50L});
        inputHandler.send(new Object[]{"IBM", 75.6f, 50L});

        //restarting execution plan
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("query1", queryCallback);
        inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        inputHandler.send(new Object[]{"IBM", 75.6f, 100L});

        //shutdown siddhi app
        Thread.sleep(500);
        SiddhiTestHelper.waitForEvents(100, 3, atomicCount, 10000);
        AssertJUnit.assertEquals(new Long(103), lastValue);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void persistenceTest12() throws InterruptedException {
        log.info("persistence test 12 - partition query");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String siddhiApp = "@App:name('TestPlan1')\n" +
                "define stream TempStream(deviceID long);\n" +
                "\n" +
                "define stream DeviceTempStream (deviceID long, count long);\n" +
                "\n" +
                "from TempStream\n" +
                "select * insert into TempInternalStream;\n" +
                "\n" +
                "partition with ( deviceID of TempInternalStream )\n" +
                "begin\n" +
                "from TempInternalStream\n" +
                "select deviceID, count(deviceID) as count\n" +
                "insert into DeviceTempStream\n" +
                "end;";

        StreamCallback queryCallback = new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                count++;
                lastValue = (Long) events[0].getData(1);
            }
        };

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("DeviceTempStream", queryCallback);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("TempStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{2});

        Thread.sleep(100);

        //persisting
        Thread.sleep(500);
        siddhiAppRuntime.persist();

        inputHandler.send(new Object[]{2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{2});

        //restarting siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();
        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("DeviceTempStream", queryCallback);
        inputHandler = siddhiAppRuntime.getInputHandler("TempStream");
        siddhiAppRuntime.start();

        //loading
        try {
            siddhiAppRuntime.restoreLastRevision();
        } catch (CannotRestoreSiddhiAppStateException e) {
            Assert.fail("Restoring of Siddhi app " + siddhiAppRuntime.getName() + " failed");
        }

        inputHandler.send(new Object[]{1});
        Thread.sleep(10);
        inputHandler.send(new Object[]{1});
        Thread.sleep(10);
        inputHandler.send(new Object[]{2});
        Thread.sleep(10);
        inputHandler.send(new Object[]{2});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        AssertJUnit.assertTrue(count == 11);
        AssertJUnit.assertEquals(new Long(4), lastValue);
        AssertJUnit.assertEquals(true, eventArrived);
    }
}
