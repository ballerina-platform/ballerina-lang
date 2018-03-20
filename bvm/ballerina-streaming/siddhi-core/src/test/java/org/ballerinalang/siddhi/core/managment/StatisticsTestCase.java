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
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StatisticsTestCase {
    private static final Logger log = LoggerFactory.getLogger(StatisticsTestCase.class);
    private int count;
    private boolean eventArrived;
    private long firstValue;
    private long lastValue;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
        firstValue = 0;
        lastValue = 0;
    }

    @Test
    public void statisticsTest1() throws InterruptedException {
        log.info("statistics test 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "@app:statistics(reporter = 'console', interval = '2' )" +
                " " +
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
                    AssertJUnit.assertTrue("IBM".equals(event.getData(0)) || "WSO2".equals(event.getData(0)));
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(3010);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);

        System.out.flush();
        String output = baos.toString();

        AssertJUnit.assertTrue(output.contains("Gauges"));
        AssertJUnit.assertTrue(output.contains("org.ballerinalang.siddhi." + SiddhiConstants.METRIC_INFIX_SIDDHI_APPS));
        AssertJUnit.assertTrue(output.contains("query1.memory"));
        AssertJUnit.assertTrue(output.contains("Meters"));
        AssertJUnit.assertTrue(output.contains(SiddhiConstants.METRIC_INFIX_SIDDHI + SiddhiConstants.METRIC_DELIMITER +
                SiddhiConstants.METRIC_INFIX_STREAMS + SiddhiConstants.METRIC_DELIMITER + "cseEventStream"));
        AssertJUnit.assertTrue(output.contains("Timers"));
        AssertJUnit.assertTrue(output.contains("query1.latency"));

        log.info(output);
        System.setOut(old);

    }

    /**
     * To test stats disabling
     */
    @Test
    public void statisticsTest2() throws InterruptedException {
        log.info("statistics test 2");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "@app:statistics(reporter = 'console', interval = '2' )" +
                " " +
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
        siddhiAppRuntime.enableStats(false);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(event.getData(0)) || "WSO2".equals(event.getData(0)));
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(3010);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);

        //reset
        eventArrived = false;
        count = 0;

        Thread.sleep(1000);

        System.out.flush();
        String output = baos.toString();
        log.info(output);

        //assert
        AssertJUnit.assertFalse(output.contains("Gauges"));

        siddhiAppRuntime.shutdown();
        System.setOut(old);

    }

    /**
     * To test stats dynamic disabling
     */
    @Test
    public void statisticsTest3() throws InterruptedException {
        log.info("statistics test 3");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "@app:statistics(reporter = 'console', interval = '2' )" +
                " " +
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
                    AssertJUnit.assertTrue("IBM".equals(event.getData(0)) || "WSO2".equals(event.getData(0)));
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(3010);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);

        System.out.flush();
        String output = baos.toString();
        baos.reset();
        log.info(output);

        AssertJUnit.assertTrue(output.contains("Gauges"));

        //reset
        eventArrived = false;
        count = 0;

        Thread.sleep(100);

        siddhiAppRuntime.enableStats(false);

        Thread.sleep(100);
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(3010);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(1, count);

        System.out.flush();
        output = baos.toString();
        baos.reset();
        log.info(output);

        AssertJUnit.assertFalse(output.contains("Gauges"));

        siddhiAppRuntime.shutdown();
        System.setOut(old);

    }

    /**
     * To test stats dynamic enabling
     */
    @Test
    public void statisticsTest4() throws InterruptedException {
        log.info("statistics test 4");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "@app:statistics(reporter = 'console', interval = '2' )" +
                " " +
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
        siddhiAppRuntime.enableStats(false);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(event.getData(0)) || "WSO2".equals(event.getData(0)));
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        siddhiAppRuntime.start();
        siddhiAppRuntime.enableStats(false);
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(3010);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);

        System.out.flush();
        String output = baos.toString();
        baos.reset();
        log.info(output);

        AssertJUnit.assertFalse(output.contains("Gauges"));

        //reset
        eventArrived = false;
        count = 0;

        siddhiAppRuntime.enableStats(true);
        Thread.sleep(100);
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(3030);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(1, count);

        System.out.flush();
        output = baos.toString();
        baos.reset();
        log.info(output);

        AssertJUnit.assertTrue(output.contains("Gauges"));
        siddhiAppRuntime.shutdown();

        System.setOut(old);

    }

    /**
     * To not enable stats if no Stats manager enabled
     */
    @Test
    public void statisticsTest5() throws InterruptedException {
        log.info("statistics test 5");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
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
        siddhiAppRuntime.enableStats(true);
        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    count++;
                    AssertJUnit.assertTrue("IBM".equals(event.getData(0)) || "WSO2".equals(event.getData(0)));
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(3010);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);

        System.out.flush();
        String output = baos.toString();

        AssertJUnit.assertFalse(output.contains("Gauges"));

        log.info(output);
        System.setOut(old);

    }

    @Test
    public void statisticsTest6() throws InterruptedException {
        log.info("statistics test 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "@app:statistics(reporter = 'console', interval = '2', include='*query2.*,*cseEventStream2*' )" +
                " " +
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
                    AssertJUnit.assertTrue("IBM".equals(event.getData(0)) || "WSO2".equals(event.getData(0)));
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});

        Thread.sleep(3010);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);

        System.out.flush();
        String output = baos.toString();

        AssertJUnit.assertTrue(output.contains("Gauges"));
        AssertJUnit.assertTrue(output.contains("org.ballerinalang.siddhi." + SiddhiConstants.METRIC_INFIX_SIDDHI_APPS));
        AssertJUnit.assertFalse(output.contains("query1.memory"));
        AssertJUnit.assertTrue(output.contains("cseEventStream2.throughput"));
        AssertJUnit.assertTrue(output.contains("Meters"));
        AssertJUnit.assertTrue(output.contains(SiddhiConstants.METRIC_INFIX_SIDDHI + SiddhiConstants.METRIC_DELIMITER +
                SiddhiConstants.METRIC_INFIX_STREAMS + SiddhiConstants.METRIC_DELIMITER + "cseEventStream"));
        AssertJUnit.assertTrue(output.contains("Timers"));
        AssertJUnit.assertFalse(output.contains("query1.latency"));
        AssertJUnit.assertTrue(output.contains("query2.memory"));

        log.info(output);
        System.setOut(old);

    }

}
