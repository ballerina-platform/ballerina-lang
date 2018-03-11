/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.transport;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.ConnectionUnavailableException;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.config.InMemoryConfigManager;
import org.ballerinalang.siddhi.core.util.transport.InMemoryBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTransportTestCase {
    private static final Logger log = LoggerFactory.getLogger(InMemoryTransportTestCase.class);
    private AtomicInteger wso2Count = new AtomicInteger(0);
    private AtomicInteger ibmCount = new AtomicInteger(0);

    @BeforeMethod
    public void init() {
        wso2Count.set(0);
        ibmCount.set(0);
    }

    @Test
    public void inMemorySinkAndEventMappingWithSiddhiQLDynamicParams() throws InterruptedException {
        log.info("Test inMemorySink And EventMapping With SiddhiQL Dynamic Params");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 2, wso2Count.get());
        AssertJUnit.assertEquals("Number of IBM events", 1, ibmCount.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);
    }

    @Test
    public void inMemorySinkAndEventMappingWithSiddhiQL() throws InterruptedException {
        log.info("Test inMemorySink And EventMapping With SiddhiQL");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='IBM', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 0, wso2Count.get());
        AssertJUnit.assertEquals("Number of IBM events", 3, ibmCount.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);
    }

    @Test
    public void inMemorySourceAndEventMappingWithSiddhiQL() throws InterruptedException {
        log.info("Test inMemorySource And EventMapping With SiddhiQL");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='inMemory', topic='WSO2', @map(type='passThrough')) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("BarStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    switch (wso2Count.incrementAndGet()) {
                        case 1:
                            org.testng.AssertJUnit.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            org.testng.AssertJUnit.assertEquals(57.6f, event.getData(1));
                            break;
                        default:
                            org.testng.AssertJUnit.fail();
                    }
                }
            }
        });

        siddhiAppRuntime.start();

        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 55.6f, 100L}));
        InMemoryBroker.publish("IBM", new Event(System.currentTimeMillis(), new Object[]{"IBM", 75.6f, 100L}));
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 57.6f, 100L}));
        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 2, wso2Count.get());
        AssertJUnit.assertEquals("Number of IBM events", 0, ibmCount.get());
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void inMemorySourceSinkAndEventMappingWithSiddhiQL() throws InterruptedException {
        log.info("Test inMemory Source Sink And EventMapping With SiddhiQL");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='inMemory', topic='Foo', @map(type='passThrough')) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.start();
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 55.6f, 100L}));
        InMemoryBroker.publish("IBM", new Event(System.currentTimeMillis(), new Object[]{"IBM", 75.6f, 100L}));
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 57.6f, 100L}));
        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 2, wso2Count.get());
        AssertJUnit.assertEquals("Number of IBM events", 1, ibmCount.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);
    }

    @Test
    public void inMemorySourceSinkAndEventMappingWithSiddhiQL2() throws InterruptedException {
        log.info("Test inMemory Source Sink And EventMapping With SiddhiQL2");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='inMemory', topic='Foo', @map(type='passThrough')) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into FooBarStream; " +
                "" +
                "from FooBarStream " +
                "select * " +
                "insert into BarStream; " +
                "";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.start();
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 55.6f, 100L}));
        InMemoryBroker.publish("IBM", new Event(System.currentTimeMillis(), new Object[]{"IBM", 75.6f, 100L}));
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 57.6f, 100L}));
        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 2, wso2Count.get());
        AssertJUnit.assertEquals("Number of IBM events", 1, ibmCount.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void inMemoryTestCase3() throws InterruptedException {
        log.info("Test inMemory 3");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='inMemory', topic='Foo', @map(type='passThrough', @attributes(symbol='symbol'))) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void inMemoryTestCase4() throws InterruptedException {
        log.info("Test inMemory 4");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='inMemory', topic='Foo', @map(type='passThrough', @attributes('symbol','price'))) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

    }

    @Test
    public void inMemoryTestCase5() throws InterruptedException {
        log.info("Test inMemory 5");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='inMemory', topic='Foo', @map(type='passThrough', @attributes(symbol='symbol'," +
                "volume='volume',price='price'))) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

    }

    @Test
    public void inMemoryTestCase6() throws InterruptedException {
        log.info("Test inMemory 6");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='testTrpInMemory', topic='Foo', prop1='hi', prop2='test', " +
                "   @map(type='passThrough', @attributes(symbol='trp:symbol'," +
                "        volume='volume',price='trp:price'))) " +
                "define stream FooStream (symbol string, price string, volume long); " +
                "define stream BarStream (symbol string, price string, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("BarStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                wso2Count.incrementAndGet();
                for (Event event : events) {
                    AssertJUnit.assertArrayEquals(event.getData(), new Object[]{"hi", "test", 100L});
                }
            }
        });
        siddhiAppRuntime.start();
        InMemoryBroker.publish("Foo", new Event(System.currentTimeMillis(), new Object[]{"WSO2", "in", 100L}));
        InMemoryBroker.publish("Foo", new Event(System.currentTimeMillis(), new Object[]{"IBM", "in", 100L}));
        InMemoryBroker.publish("Foo", new Event(System.currentTimeMillis(), new Object[]{"WSO2", "in", 100L}));
        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of events", 3, wso2Count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void inMemoryTestCase7() throws InterruptedException {
        log.info("Test inMemory 7");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='testTrpInMemory', topic='Foo', prop1='hi', prop2='test', fail='true', " +
                "   @map(type='passThrough', @attributes(symbol='trp:symbol'," +
                "        volume='volume',price='trp:price'))) " +
                "define stream FooStream (symbol string, price string, volume long); " +
                "define stream BarStream (symbol string, price string, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("BarStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                wso2Count.incrementAndGet();
                for (Event event : events) {
                    AssertJUnit.assertArrayEquals(event.getData(), new Object[]{"hi", "test", 100L});
                }
            }
        });
        siddhiAppRuntime.start();
        InMemoryBroker.publish("Foo", new Event(System.currentTimeMillis(), new Object[]{"WSO2", "in", 100L}));
        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of events", 0, wso2Count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void inMemoryWithFailingSink() throws InterruptedException {
        log.info("Test failing inMemorySink");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='testFailingInMemory', topic='{{symbol}}', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler stockStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        TestFailingInMemorySink.fail = true;
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        TestFailingInMemorySink.fail = false;
        Thread.sleep(5500);
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 1, wso2Count.get());
        AssertJUnit.assertEquals("Number of IBM events", 2, ibmCount.get());
        AssertJUnit.assertEquals("Number of errors", 2, TestFailingInMemorySink.numberOfErrorOccurred);
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);
    }

    @Test
    public void inMemoryWithFailingSource() throws InterruptedException {
        log.info("Test failing inMemorySource");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='testFailingInMemory', topic='WSO2', @map(type='passThrough')) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("BarStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    wso2Count.incrementAndGet();
                }
            }
        });

        siddhiAppRuntime.start();

        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 55.6f, 100L}));
        InMemoryBroker.publish("IBM", new Event(System.currentTimeMillis(), new Object[]{"IBM", 75.6f, 100L}));
        TestFailingInMemorySource.fail = true;
        TestFailingInMemorySource.connectionCallback.onError(new ConnectionUnavailableException("Connection Lost"));
        Thread.sleep(500);
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 57.6f, 100L}));
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 57.6f, 100L}));
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 57.6f, 100L}));
        TestFailingInMemorySource.fail = false;
        Thread.sleep(5500);
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 55.6f, 100L}));
        InMemoryBroker.publish("IBM", new Event(System.currentTimeMillis(), new Object[]{"IBM", 75.6f, 100L}));

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 2, wso2Count.get());
        AssertJUnit.assertEquals("Number of errors", 1, TestFailingInMemorySource.numberOfErrorOccurred);
        siddhiAppRuntime.shutdown();

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void inMemoryTestCase8() throws InterruptedException {
        log.info("Test inMemory 8");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(type='testEventInMemory', topic='Foo', prop1='hi', prop2='test', fail='true', " +
                "   @map(type='testString', @attributes(symbol='trp:symbol'," +
                "        volume='volume',price='trp:price'))) " +
                "define stream FooStream (symbol string, price string, volume long); " +
                "define stream BarStream (symbol string, price string, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void inMemoryTestCase9() throws InterruptedException {
        log.info("Test inMemory 9");

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='testStringInMemory', topic='{{symbol}}', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

    }

    @Test
    public void inMemorySourceSinkAndEventMappingWithSiddhiQLAndRef() throws InterruptedException {
        log.info("Test inMemory Source Sink And EventMapping With SiddhiQL and Ref");

        InMemoryBroker.Subscriber subscriptionWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriptionIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriptionWSO2);
        InMemoryBroker.subscribe(subscriptionIBM);

        String streams = "" +
                "@app:name('TestSiddhiApp')" +
                "@source(ref='test1', @map(type='passThrough')) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(ref='test2', @map(type='passThrough')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        Map<String, String> systemConfigs = new HashMap<>();
        systemConfigs.put("test1.topic", "Foo");
        systemConfigs.put("test1.type", "inMemory");
        systemConfigs.put("test2.type", "inMemory");
        systemConfigs.put("test2.topic", "{{symbol}}");
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(null, systemConfigs);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setConfigManager(inMemoryConfigManager);

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.start();
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 55.6f, 100L}));
        InMemoryBroker.publish("IBM", new Event(System.currentTimeMillis(), new Object[]{"IBM", 75.6f, 100L}));
        InMemoryBroker.publish("WSO2", new Event(System.currentTimeMillis(), new Object[]{"WSO2", 57.6f, 100L}));
        Thread.sleep(100);

        //assert event count
        AssertJUnit.assertEquals("Number of WSO2 events", 2, wso2Count.get());
        AssertJUnit.assertEquals("Number of IBM events", 1, ibmCount.get());
        siddhiAppRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriptionWSO2);
        InMemoryBroker.unsubscribe(subscriptionIBM);
    }


}
