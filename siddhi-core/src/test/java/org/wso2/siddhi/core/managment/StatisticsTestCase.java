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
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiConstants;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StatisticsTestCase {
    private static final Logger log = Logger.getLogger(StatisticsTestCase.class);
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
                "@app:statistics(reporter = 'console', interval = '5' )" +
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

        Thread.sleep(5010);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(3, count);

        System.out.flush();
        System.setOut(old);
        String output = baos.toString();

        AssertJUnit.assertTrue(output.contains("Gauges"));
        AssertJUnit.assertTrue(output.contains("org.wso2.siddhi." + SiddhiConstants.METRIC_INFIX_EXECUTION_PLANS));
        AssertJUnit.assertTrue(output.contains("query1.memory"));
        AssertJUnit.assertTrue(output.contains("Meters"));
        AssertJUnit.assertTrue(output.contains(SiddhiConstants.METRIC_INFIX_SIDDHI + SiddhiConstants.METRIC_DELIMITER +
                SiddhiConstants.METRIC_INFIX_STREAMS + SiddhiConstants.METRIC_DELIMITER + "cseEventStream"));
        AssertJUnit.assertTrue(output.contains("Timers"));
        AssertJUnit.assertTrue(output.contains("query1.latency"));

        log.info(output);
    }
}
