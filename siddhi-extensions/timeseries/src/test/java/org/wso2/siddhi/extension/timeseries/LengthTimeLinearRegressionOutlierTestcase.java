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

package org.wso2.siddhi.extension.timeseries;

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

public class LengthTimeLinearRegressionOutlierTestcase {
    static final Logger logger = Logger.getLogger(LengthTimeLinearRegressionTestcase.class);
    private static SiddhiManager siddhiManager;
    private int count;
    private boolean outlier;

    @Before
    public void init() {
        count = 0;
    }

    @Test
    public void simpleOutlierTest1() throws Exception {
        logger.info("Simple Outlier TestCase");

        siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (y double, x double);";

        // Limit number of events based on length window (query):
        String executionPlan = ("@info(name = 'query1') from InputStream#timeseries:lengthTimeOutlier(20 min, 20, 1, y, x) "
                + "select * " + "insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inputStream + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                outlier = (Boolean) inEvents[inEvents.length - 1].getData(5);
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();

        System.out.println(System.currentTimeMillis());

        // Limit number of events based on length window (test case):
        inputHandler.send(new Object[] { 3300.00, 31.00 });
        inputHandler.send(new Object[] { 2600.00, 18.00 });
        inputHandler.send(new Object[] { 2500.00, 17.00 });
        inputHandler.send(new Object[] { 2475.00, 12.00 });
        inputHandler.send(new Object[] { 2313.00, 8.00 });
        inputHandler.send(new Object[] { 2175.00, 26.00 });
        inputHandler.send(new Object[] { 600.00, 14.00 });
        inputHandler.send(new Object[] { 460.00, 3.00 });
        inputHandler.send(new Object[] { 240.00, 1.00 });
        inputHandler.send(new Object[] { 200.00, 10.00 });
        inputHandler.send(new Object[] { 177.00, 0.00 });
        inputHandler.send(new Object[] { 140.00, 6.00 });
        inputHandler.send(new Object[] { 117.00, 1.00 });
        inputHandler.send(new Object[] { 115.00, 0.00 });
        inputHandler.send(new Object[] { 2600.00, 19.00 });
        inputHandler.send(new Object[] { 1907.00, 13.00 });
        inputHandler.send(new Object[] { 1190.00, 3.00 });
        inputHandler.send(new Object[] { 990.00, 16.00 });
        inputHandler.send(new Object[] { 925.00, 6.00 });
        inputHandler.send(new Object[] { 365.00, 0.00 });
        inputHandler.send(new Object[] { 302.00, 10.00 });
        inputHandler.send(new Object[] { 300.00, 6.00 });
        inputHandler.send(new Object[] { 129.00, 2.00 });
        inputHandler.send(new Object[] { 111.00, 1.00 });
        inputHandler.send(new Object[] { 6100.00, 18.00 });
        inputHandler.send(new Object[] { 4125.00, 19.00 });
        inputHandler.send(new Object[] { 3213.00, 1.00 });
        inputHandler.send(new Object[] { 2319.00, 38.00 });
        inputHandler.send(new Object[] { 2000.00, 10.00 });
        inputHandler.send(new Object[] { 1600.00, 0.00 });
        inputHandler.send(new Object[] { 1394.00, 4.00 });
        inputHandler.send(new Object[] { 935.00, 4.00 });
        inputHandler.send(new Object[] { 850.00, 0.00 });
        inputHandler.send(new Object[] { 775.00, 5.00 });
        inputHandler.send(new Object[] { 760.00, 6.00 });
        inputHandler.send(new Object[] { 629.00, 1.00 });
        inputHandler.send(new Object[] { 275.00, 6.00 });
        inputHandler.send(new Object[] { 120.00, 0.00 });
        inputHandler.send(new Object[] { 2567.00, 12.00 });
        inputHandler.send(new Object[] { 2500.00, 28.00 });
        inputHandler.send(new Object[] { 2350.00, 21.00 });
        inputHandler.send(new Object[] { 2317.00, 3.00 });
        inputHandler.send(new Object[] { 2000.00, 12.00 });
        inputHandler.send(new Object[] { 715.00, 1.00 });
        inputHandler.send(new Object[] { 660.00, 9.00 });
        inputHandler.send(new Object[] { 650.00, 0.00 });
        inputHandler.send(new Object[] { 260.00, 0.00 });
        inputHandler.send(new Object[] { 250.00, 1.00 });
        inputHandler.send(new Object[] { 200.00, 13.00 });
        inputHandler.send(new Object[] { 180.00, 6.00 });

        Thread.sleep(100);

        Assert.assertEquals("No of events: ", 50, count);
        Assert.assertEquals(true, outlier);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void simpleOutlierTest2() throws Exception {
        logger.info("Simple Outlier TestCase");

        siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (y double, x double);";

        // Limit number of events based on time window (query):
        String executionPlan = ("@info(name = 'query1') from InputStream#timeseries:lengthTimeOutlier(200, 10000, 1, y, x) "
                + "select * " + "insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inputStream + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                outlier = (Boolean) inEvents[inEvents.length - 1].getData(5);
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();

        System.out.println(System.currentTimeMillis());

        // Limit number of events based on time window (test case):
        inputHandler.send(new Object[] { 3300.00, 31.00 });
        inputHandler.send(new Object[] { 2600.00, 18.00 });
        inputHandler.send(new Object[] { 2500.00, 17.00 });
        inputHandler.send(new Object[] { 2475.00, 12.00 });
        inputHandler.send(new Object[] { 2313.00, 8.00 });
        inputHandler.send(new Object[] { 2175.00, 26.00 });
        inputHandler.send(new Object[] { 600.00, 14.00 });
        inputHandler.send(new Object[] { 460.00, 3.00 });
        inputHandler.send(new Object[] { 240.00, 1.00 });
        inputHandler.send(new Object[] { 200.00, 10.00 });
        inputHandler.send(new Object[] { 177.00, 0.00 });
        inputHandler.send(new Object[] { 140.00, 6.00 });
        inputHandler.send(new Object[] { 117.00, 1.00 });
        inputHandler.send(new Object[] { 115.00, 0.00 });
        inputHandler.send(new Object[] { 2600.00, 19.00 });
        inputHandler.send(new Object[] { 1907.00, 13.00 });
        inputHandler.send(new Object[] { 1190.00, 3.00 });
        inputHandler.send(new Object[] { 990.00, 16.00 });
        inputHandler.send(new Object[] { 925.00, 6.00 });
        inputHandler.send(new Object[] { 365.00, 0.00 });
        inputHandler.send(new Object[] { 302.00, 10.00 });
        inputHandler.send(new Object[] { 300.00, 6.00 });
        inputHandler.send(new Object[] { 129.00, 2.00 });
        inputHandler.send(new Object[] { 111.00, 1.00 });
        inputHandler.send(new Object[] { 6100.00, 18.00 });
        inputHandler.send(new Object[] { 4125.00, 19.00 });
        inputHandler.send(new Object[] { 3213.00, 1.00 });
        inputHandler.send(new Object[] { 2319.00, 38.00 });
        inputHandler.send(new Object[] { 2000.00, 10.00 });
        inputHandler.send(new Object[] { 1600.00, 0.00 });
        Thread.sleep(200);
        inputHandler.send(new Object[] { 1394.00, 4.00 });
        inputHandler.send(new Object[] { 935.00, 4.00 });
        inputHandler.send(new Object[] { 850.00, 0.00 });
        inputHandler.send(new Object[] { 775.00, 5.00 });
        inputHandler.send(new Object[] { 760.00, 6.00 });
        inputHandler.send(new Object[] { 629.00, 1.00 });
        inputHandler.send(new Object[] { 275.00, 6.00 });
        inputHandler.send(new Object[] { 120.00, 0.00 });
        inputHandler.send(new Object[] { 2567.00, 12.00 });
        inputHandler.send(new Object[] { 2500.00, 28.00 });
        inputHandler.send(new Object[] { 2350.00, 21.00 });
        inputHandler.send(new Object[] { 2317.00, 3.00 });
        inputHandler.send(new Object[] { 2000.00, 12.00 });
        inputHandler.send(new Object[] { 715.00, 1.00 });
        inputHandler.send(new Object[] { 660.00, 9.00 });
        inputHandler.send(new Object[] { 650.00, 0.00 });
        inputHandler.send(new Object[] { 260.00, 0.00 });
        inputHandler.send(new Object[] { 250.00, 1.00 });
        inputHandler.send(new Object[] { 200.00, 13.00 });
        inputHandler.send(new Object[] { 180.00, 6.00 });

        Thread.sleep(100);

        Assert.assertEquals("No of events: ", 50, count);
        Assert.assertEquals(true, outlier);

        executionPlanRuntime.shutdown();

    }
}
