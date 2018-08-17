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

package org.ballerinalang.siddhi.core.query.selector.attribute.aggregator;

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
 * Testcase for stddev aggregator.
 */
public class StdDevAttributeAggregatorTestCase {
    private static final Logger log = LoggerFactory.getLogger(StdDevAttributeAggregatorTestCase.class);
    private final double epsilon = 0.00001; // difference threshold for two doubles to be treated distinct
    private int inEventCount; // Only used in the Test #1 and #6

    @BeforeMethod
    public void init() {
        inEventCount = 0;
    }

    @Test
    public void stdDevAggregatorTest1() throws InterruptedException {
        log.info("stdDevAggregator Test #1: No events in the stream");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@app:name('stdDevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select stdDev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        SiddhiAppRuntime execPlanRunTime = siddhiManager.createSiddhiAppRuntime(execPlan);

        log.info("Running: " + execPlanRunTime.getName());

        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                inEventCount++;
            }
        });

        execPlanRunTime.start();
        execPlanRunTime.shutdown();
        AssertJUnit.assertEquals(0, inEventCount);
    }

    @Test
    public void stdDevAggregatorTest2() throws InterruptedException {
        log.info("stdDevAggregator Test #2: Single event in the stream");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@app:name('stdDevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(1) " +
                "select stdDev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        SiddhiAppRuntime execPlanRunTime = siddhiManager.createSiddhiAppRuntime(execPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                AssertJUnit.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 0) < epsilon);
            }
        });

        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        execPlanRunTime.start();
        inputHandler.send(new Object[]{"WSO2", 1.0});
        Thread.sleep(100);
        execPlanRunTime.shutdown();
    }

    @Test
    public void stdDevAggregatorTest3() throws InterruptedException {
        log.info("stdDevAggregator Test #3: All the events in the stream are equal");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@app:name('stdDevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(3) " +
                "select stdDev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        SiddhiAppRuntime execPlanRunTime = siddhiManager.createSiddhiAppRuntime(execPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                AssertJUnit.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 0) < epsilon);
            }
        });

        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        execPlanRunTime.start();
        inputHandler.send(new Object[]{"WSO2", 1.0});
        inputHandler.send(new Object[]{"WSO2", 1.0});
        inputHandler.send(new Object[]{"WSO2", 1.0});
        Thread.sleep(300);
        execPlanRunTime.shutdown();
    }

    @Test
    public void stdDevAggregatorTest4() throws InterruptedException {
        log.info("stdDevAggregator Test #4: Two symbols in the stream with same stdDev");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@app:name('stdDevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(6) " +
                "select stdDev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        SiddhiAppRuntime execPlanRunTime = siddhiManager.createSiddhiAppRuntime(execPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                AssertJUnit.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 0.40825) < epsilon);
                AssertJUnit.assertTrue(Math.abs((Double) inEvents[1].getData(0) - 0.40825) < epsilon);
            }
        });

        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        execPlanRunTime.start();
        inputHandler.send(new Object[]{"WSO2", 10000.0});
        inputHandler.send(new Object[]{"WSO2", 10000.5});
        inputHandler.send(new Object[]{"WSO2", 10001.0});
        inputHandler.send(new Object[]{"IBM", 1.0});
        inputHandler.send(new Object[]{"IBM", 1.5});
        inputHandler.send(new Object[]{"IBM", 2.0});
        Thread.sleep(600);
        execPlanRunTime.shutdown();
    }

    @Test
    public void stdDevAggregatorTest5() throws InterruptedException {
        log.info("stdDevAggregator Test #5: stdDev of large and small numbers");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@app:name('stdDevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(6) " +
                "select stdDev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        SiddhiAppRuntime execPlanRunTime = siddhiManager.createSiddhiAppRuntime(execPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                AssertJUnit.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 400.13025) < epsilon);
                AssertJUnit.assertTrue(Math.abs((Double) inEvents[1].getData(0) - 0.00103) < epsilon);
            }
        });

        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        execPlanRunTime.start();
        inputHandler.send(new Object[]{"WSO2", 23.0});
        inputHandler.send(new Object[]{"WSO2", 1003.0});
        inputHandler.send(new Object[]{"WSO2", 500.0});
        inputHandler.send(new Object[]{"IBM", 0.002});
        inputHandler.send(new Object[]{"IBM", 0.0034});
        inputHandler.send(new Object[]{"IBM", 0.00454});
        Thread.sleep(600);
        execPlanRunTime.shutdown();
    }

    @Test
    public void stdDevAggregatorTest6() throws InterruptedException {
        log.info("stdDevAggregator Test #6");

        final double[] results = new double[]{0.0, 489.95, 400.09052, 405.11802, 199.96026};

        SiddhiManager siddhiManager = new SiddhiManager();

        String windowExecPlan = "" +
                "@app:name('stdDevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.length(3) " +
                "select stdDev(price) as deviation " +
                "group by symbol " +
                "insert all events into outputStream;";

        SiddhiAppRuntime execPlanRunTime = siddhiManager.createSiddhiAppRuntime(windowExecPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    AssertJUnit.assertTrue(Math.abs(results[inEventCount] - (Double) event.getData(0)) < epsilon);
                    inEventCount++;
                }
            }
        });

        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        execPlanRunTime.start();
        inputHandler.send(new Object[]{"WSO2", 23.3});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 1003.2});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 500.1});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 10.9});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 234.5});
        Thread.sleep(100);
        execPlanRunTime.shutdown();
    }
}
