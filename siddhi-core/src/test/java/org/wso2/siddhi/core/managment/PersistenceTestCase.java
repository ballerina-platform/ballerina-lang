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

package org.wso2.siddhi.core.managment;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.NoPersistenceStoreException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;

import java.util.concurrent.ExecutionException;

public class PersistenceTestCase {
    private static final Logger log = Logger.getLogger(PersistenceTestCase.class);
    private int count;
    private boolean eventArrived;
    private long firstValue;
    private Long lastValue;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
        firstValue = 0;
        lastValue = 0L;
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
                    Assert.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(new Long(200), lastValue);

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
        siddhiAppRuntime.restoreLastRevision();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(10);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        Assert.assertTrue(count <= 6);
        Assert.assertEquals(new Long(400), lastValue);
        Assert.assertEquals(true, eventArrived);

    }

    @Test
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
                    Assert.assertArrayEquals(new Object[]{25.6f, 47.6f, null, null, 45.7f}, inEvent.getData());
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

        Assert.assertEquals("Number of success events", 0, count);
        Assert.assertEquals("Event arrived", false, eventArrived);

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
        siddhiAppRuntime.restoreLastRevision();

        stream2.send(new Object[]{"IBM", 45.7f, 100});
        Thread.sleep(500);
        stream1.send(new Object[]{"GOOG", 47.8f, 100});
        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(500);

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        Assert.assertEquals("Number of success events", 1, count);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }

    @Test(expected = NoPersistenceStoreException.class)
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
                    Assert.assertArrayEquals(new Object[]{25.6f, 47.6f, null, null, 45.7f}, inEvent.getData());
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

        Assert.assertEquals("Number of success events", 0, count);
        Assert.assertEquals("Event arrived", false, eventArrived);

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

    @Test
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
                    Assert.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(new Long(200), lastValue);

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
        siddhiAppRuntime.restoreLastRevision();

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        Assert.assertEquals(new Long(400), lastValue);
        Assert.assertEquals(true, eventArrived);

    }

    @Test
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
                        Assert.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(firstValue, 200);

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
        siddhiAppRuntime.restoreLastRevision();

        //shutdown siddhi app
        Thread.sleep(15000);
        siddhiAppRuntime.shutdown();

        Assert.assertEquals(400, firstValue);
        Assert.assertEquals(null, lastValue);
        Assert.assertEquals(true, eventArrived);

    }

    @Test
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
                    Assert.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(2, count);

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
        siddhiAppRuntime.restoreLastRevision();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        Assert.assertEquals(count, 6);
        Assert.assertEquals(true, eventArrived);

    }


    @Test
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
                    Assert.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
                    if (count == 5) {
                        Assert.assertEquals(300L, inEvent.getData(2));
                    }
                    if (count == 6) {
                        Assert.assertEquals(100L, inEvent.getData(2));
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(3, count);

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
        siddhiAppRuntime.restoreLastRevision();

        inputHandler.send(new Object[]{"IBM", 75.4f, 100, currentTime + 4000});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"IBM", 75.5f, 100, currentTime + 5000});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100, currentTime + 6000});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        Assert.assertEquals(count, 6);
        Assert.assertEquals(true, eventArrived);

    }

    @Test
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
                    Assert.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(new Long(200), lastValue);

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
        siddhiAppRuntime.restore(snapshot);

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(10);
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //shutdown siddhi app
        Thread.sleep(500);
        siddhiAppRuntime.shutdown();

        Assert.assertTrue(count == 6);
        Assert.assertEquals(new Long(400), lastValue);
        Assert.assertEquals(true, eventArrived);

    }

}
