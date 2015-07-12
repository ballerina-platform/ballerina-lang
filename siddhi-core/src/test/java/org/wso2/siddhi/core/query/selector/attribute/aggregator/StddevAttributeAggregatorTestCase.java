/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.query.selector.attribute.aggregator;

import junit.framework.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import org.apache.log4j.Logger;

public class StddevAttributeAggregatorTestCase {
    static final Logger log = Logger.getLogger(StddevAttributeAggregatorTestCase.class);

    @Test
    public void StddevAggregatorTest1() throws InterruptedException {
        log.info("StddevAggregator Test #1: No events in the stream");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('StddevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select stddev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);

        log.info("Running: " + execPlanRunTime.getName());

        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 0) < 0.00001);
            }
        });

        execPlanRunTime.start();
        execPlanRunTime.shutdown();
    }

    @Test
    public void StddevAggregatorTest2() throws InterruptedException {
        log.info("StddevAggregator Test #2: Single event in the stream");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('StddevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select stddev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 0) < 0.00001);
            }
        });

        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        execPlanRunTime.start();
        inputHandler.send(new Object[]{"WSO2", 1.0});
        execPlanRunTime.shutdown();
    }

    @Test
    public void StddevAggregatorTest3() throws InterruptedException {
        log.info("StddevAggregator Test #3: All the events in the stream are equal");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('StddevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select stddev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 0) < 0.00001);
            }
        });

        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        execPlanRunTime.start();
        inputHandler.send(new Object[]{"WSO2", 1.0});
        inputHandler.send(new Object[]{"WSO2", 1.0});
        inputHandler.send(new Object[]{"WSO2", 1.0});
        execPlanRunTime.shutdown();
    }

    @Test
    public void StddevAggregatorTest4() throws InterruptedException {
        log.info("StddevAggregator Test #4: Two symbols in the stream with same stddev");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('StddevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select stddev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                    Assert.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 0.40825) < 0.00001);
                    Assert.assertTrue(Math.abs((Double) inEvents[1].getData(0) - 0.40825) < 0.00001);
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
        execPlanRunTime.shutdown();
    }

    @Test
    public void StddevAggregatorTest5() throws InterruptedException {
        log.info("StddevAggregator Test #5: Stddev of large and small numbers");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('StddevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select stddev(price) as deviation " +
                "group by symbol " +
                "insert into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 400.13025) < 0.00001);
                Assert.assertTrue(Math.abs((Double) inEvents[1].getData(0) - 0.0010387) < 0.0000001);
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
        execPlanRunTime.shutdown();
    }

    @Test
    public void StddevAggregatorTest6() throws InterruptedException {
        log.info("StddevAggregator Test #6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String windowExecPlan = "" +
                "@Plan:name('StddevAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price double);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.length(3) " +
                "select stddev(price) as deviation " +
                "group by symbol " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(windowExecPlan);
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                Assert.assertTrue(Math.abs((Double) inEvents[0].getData(0) - 400.09052) < 0.00001);
//                Assert.assertTrue(Math.abs((Double) inEvents[1].getData(0) - 405.11802) < 0.00001);
//                Assert.assertTrue(Math.abs((Double) inEvents[2].getData(0) - 199.96026) < 0.00001);
            }
        });

        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        execPlanRunTime.start();
        inputHandler.send(new Object[]{"WSO2", 23.3});
        inputHandler.send(new Object[]{"WSO2", 1003.2});
        inputHandler.send(new Object[]{"WSO2", 500.1});
        inputHandler.send(new Object[]{"WSO2", 10.9});
        inputHandler.send(new Object[]{"WSO2", 234.5});
        execPlanRunTime.shutdown();
    }

}
