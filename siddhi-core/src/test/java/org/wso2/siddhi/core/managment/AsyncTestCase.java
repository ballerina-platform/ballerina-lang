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
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class AsyncTestCase {
    private static final Logger log = Logger.getLogger(AsyncTestCase.class);
    private int count;
    private boolean eventArrived;
    private long firstValue;
    private long lastValue;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
        firstValue = 0;
        lastValue = 0;
    }

    @Test
    public void asyncTest1() throws InterruptedException {
        log.info("async test 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:async " +
//                "@config(async='true') " +
                "define stream cseEventStream (symbol string, price float, volume int);" +
                "define stream cseEventStream2 (symbol string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into outputStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from cseEventStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    count++;
                    Assert.assertTrue("IBM".equals(event.getData(0)) || "WSO2".equals(event.getData(0)));
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(3, count);

    }

    @Test
    public void asyncTest2() throws InterruptedException {
        log.info("async test 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:async " +
                " " +
                "define stream cseEventStream (symbol string, price float, volume int);" +
                "define stream cseEventStream2 (symbol string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into innerStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from innerStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    count++;
                    Assert.assertTrue("WSO2".equals(event.getData(0)));
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(1, count);

    }

    @Test
    public void asyncTest3() throws InterruptedException {
        log.info("async test 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:async" +
                " " +
                "define stream cseEventStream (symbol string, price float, volume int);" +
                "define stream cseEventStream2 (symbol string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into innerStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from innerStream[volume > 90]#window.time(30 milliseconds) " +
                "select symbol, count() as eventCount " +
                "insert all events into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    count++;
                    Assert.assertTrue("WSO2".equals(event.getData(0)));
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(2, count);

    }

    @Test
    public void asyncTest4() throws InterruptedException {
        log.info("async test 4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:async(buffer.size='2')" +
                " " +
                "define stream cseEventStream (symbol string, price float, volume int);" +
                "define stream cseEventStream2 (symbol string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into innerStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from innerStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                eventArrived = true;
                for (Event event : events) {
                    count++;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        long startTime = System.currentTimeMillis();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 9.6f, 100});
        inputHandler.send(new Object[]{"FB", 7.6f, 100});
        inputHandler.send(new Object[]{"GOOG", 5.6f, 100});
        inputHandler.send(new Object[]{"FB", 23.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 13.6f, 100});
        inputHandler.send(new Object[]{"IBM", 15.6f, 100});
        long timeDiff = System.currentTimeMillis() - startTime;
        Thread.sleep(9000);
        siddhiAppRuntime.shutdown();
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(7, count);
        Assert.assertTrue(timeDiff >= 1000);

    }


    @Test
    public void asyncTest5() throws InterruptedException {
        log.info("async test 5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                " " +
                "@async(buffer.size='2')" +
                "define stream cseEventStream (symbol string, price float, volume int);" +
                "" +
                "define stream cseEventStream2 (symbol string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into innerStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from innerStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                eventArrived = true;
                for (Event event : events) {
                    count++;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        long startTime = System.currentTimeMillis();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 9.6f, 100});
        inputHandler.send(new Object[]{"FB", 7.6f, 100});
        inputHandler.send(new Object[]{"GOOG", 5.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 15.6f, 100});
        long timeDiff = System.currentTimeMillis() - startTime;
        Thread.sleep(5000);
        siddhiAppRuntime.shutdown();
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(5, count);
        Assert.assertTrue(timeDiff >= 2000);

    }

}
