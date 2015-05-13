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

package org.wso2.siddhi.extension.timeseries;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class LinearRegressionTestcase {
    private static Logger logger = Logger.getLogger(LinearRegressionTestcase.class);
    protected static SiddhiManager siddhiManager;

    @Test
    public void testSimpleRegression() throws Exception {
        logger.info("Simple Regression TestCase");

        siddhiManager = new SiddhiManager();
        String inputStream = "@config(async = 'true')define stream InputStream (y int, x int);";

        String executionPlan = ("@info(name = 'query1') from InputStream#timeseries:regress(1, 100, 0.95, y, x) "
                + "select * "
                + "insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inputStream + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();

        System.out.println(System.currentTimeMillis());

        inputHandler.send(new Object[]{2500.00,17.00});
        inputHandler.send(new Object[]{2600.00,18.00});
        inputHandler.send(new Object[]{3300.00,31.00});
        inputHandler.send(new Object[]{2475.00,12.00});
        inputHandler.send(new Object[]{2313.00,8.00});
        inputHandler.send(new Object[]{2175.00,26.00});
        inputHandler.send(new Object[]{600.00,14.00});
        inputHandler.send(new Object[]{460.00,3.00});
        inputHandler.send(new Object[]{240.00,1.00});
        inputHandler.send(new Object[]{200.00,10.00});
        inputHandler.send(new Object[]{177.00,0.00});
        inputHandler.send(new Object[]{140.00,6.00});
        inputHandler.send(new Object[]{117.00,1.00});
        inputHandler.send(new Object[]{115.00,0.00});
        inputHandler.send(new Object[]{2600.00,19.00});
        inputHandler.send(new Object[]{1907.00,13.00});
        inputHandler.send(new Object[]{1190.00,3.00});
        inputHandler.send(new Object[]{990.00,16.00});
        inputHandler.send(new Object[]{925.00,6.00});
        inputHandler.send(new Object[]{365.00,0.00});
        inputHandler.send(new Object[]{302.00,10.00});
        inputHandler.send(new Object[]{300.00,6.00});
        inputHandler.send(new Object[]{129.00,2.00});
        inputHandler.send(new Object[]{111.00,1.00});
        inputHandler.send(new Object[]{6100.00,18.00});
        inputHandler.send(new Object[]{4125.00,19.00});
        inputHandler.send(new Object[]{3213.00,1.00});
        inputHandler.send(new Object[]{2319.00,38.00});
        inputHandler.send(new Object[]{2000.00,10.00});
        inputHandler.send(new Object[]{1600.00,0.00});
        inputHandler.send(new Object[]{1394.00,4.00});
        inputHandler.send(new Object[]{935.00,4.00});
        inputHandler.send(new Object[]{850.00,0.00});
        inputHandler.send(new Object[]{775.00,5.00});
        inputHandler.send(new Object[]{760.00,6.00});
        inputHandler.send(new Object[]{629.00,1.00});
        inputHandler.send(new Object[]{275.00,6.00});
        inputHandler.send(new Object[]{120.00,0.00});
        inputHandler.send(new Object[]{2567.00,12.00});
        inputHandler.send(new Object[]{2500.00,28.00});
        inputHandler.send(new Object[]{2350.00,21.00});
        inputHandler.send(new Object[]{2317.00,3.00});
        inputHandler.send(new Object[]{2000.00,12.00});
        inputHandler.send(new Object[]{715.00,1.00});
        inputHandler.send(new Object[]{660.00,9.00});
        inputHandler.send(new Object[]{650.00,0.00});
        inputHandler.send(new Object[]{260.00,0.00});
        inputHandler.send(new Object[]{250.00,1.00});
        inputHandler.send(new Object[]{200.00,13.00});
        inputHandler.send(new Object[]{180.00,6.00});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testMultipleRegression() throws Exception {
        logger.info("Multiple Regression TestCase");

        siddhiManager = new SiddhiManager();
        String inputStream = "@config(async = 'true')define stream InputStream (a int, b int, c int, d int, e int);";

        String eventFuseExecutionPlan = ("@info(name = 'query2') from InputStream#timeseries:regress(5, 100, 0.95, a, c, b, e) "
                + "select * "
                + "insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inputStream + eventFuseExecutionPlan);

        executionPlanRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();
        System.out.println(System.currentTimeMillis());

        inputHandler.send(new Object[]{ 3300 , 104 , 22 , 80 , 3 });
        inputHandler.send(new Object[]{ 2600 , 66 , 39 , 69 , 3 });
        inputHandler.send(new Object[]{ 2500 , 73 , 63 , 116 , 5 });
        inputHandler.send(new Object[]{ 2475 , 50 , 23 , 64 , 21 });
        inputHandler.send(new Object[]{ 2313 , 58 , 70 , 53 , 8 });
        inputHandler.send(new Object[]{ 2175 , 100 , 87 , 89 , 4 });
        inputHandler.send(new Object[]{ 600 , 38 , 15 , 45 , 10 });
        inputHandler.send(new Object[]{ 460 , 21 , 11 , 32 , 3 });
        inputHandler.send(new Object[]{ 240 , 18 , 24 , 26 , 2 });
        inputHandler.send(new Object[]{ 200 , 33 , 14 , 96 , 6 });
        inputHandler.send(new Object[]{ 177 , 10 , 5 , 18 , 7 });
        inputHandler.send(new Object[]{ 140 , 22 , 19 , 56 , 3 });
        inputHandler.send(new Object[]{ 117 , 3 , 2 , 1 , 0 });
        inputHandler.send(new Object[]{ 115 , 2 , 4 , 3 , 0 });
        inputHandler.send(new Object[]{ 2600 , 75 , 53 , 64 , 7 });
        inputHandler.send(new Object[]{ 1907 , 73 , 50 , 100 , 14 });
        inputHandler.send(new Object[]{ 1190 , 26 , 42 , 61 , 8 });
        inputHandler.send(new Object[]{ 990 , 64 , 42 , 102 , 6 });
        inputHandler.send(new Object[]{ 925 , 26 , 22 , 26 , 5 });
        inputHandler.send(new Object[]{ 365 , 15 , 14 , 30 , 6 });
        inputHandler.send(new Object[]{ 302 , 51 , 95 , 151 , 27 });
        inputHandler.send(new Object[]{ 300 , 39 , 34 , 89 , 6 });
        inputHandler.send(new Object[]{ 129 , 18 , 20 , 22 , 5 });
        inputHandler.send(new Object[]{ 111 , 8 , 1 , 18 , 0 });
        inputHandler.send(new Object[]{ 6100 , 100 , 90 , 67 , 15 });
        inputHandler.send(new Object[]{ 4125 , 96 , 55 , 74 , 7 });
        inputHandler.send(new Object[]{ 3213 , 17 , 39 , 47 , 3 });
        inputHandler.send(new Object[]{ 2319 , 117 , 78 , 120 , 31 });
        inputHandler.send(new Object[]{ 2000 , 40 , 36 , 56 , 4 });
        inputHandler.send(new Object[]{ 1600 , 31 , 50 , 69 , 15 });
        inputHandler.send(new Object[]{ 1394 , 51 , 83 , 50 , 5 });
        inputHandler.send(new Object[]{ 935 , 21 , 30 , 42 , 3 });
        inputHandler.send(new Object[]{ 850 , 54 , 75 , 38 , 20 });
        inputHandler.send(new Object[]{ 775 , 35 , 9 , 19 , 3 });
        inputHandler.send(new Object[]{ 760 , 36 , 40 , 53 , 14 });
        inputHandler.send(new Object[]{ 629 , 30 , 24 , 43 , 0 });
        inputHandler.send(new Object[]{ 275 , 34 , 33 , 57 , 8 });
        inputHandler.send(new Object[]{ 120 , 5 , 14 , 19 , 2 });
        inputHandler.send(new Object[]{ 2567 , 42 , 41 , 66 , 8 });
        inputHandler.send(new Object[]{ 2500 , 81 , 48 , 93 , 5 });
        inputHandler.send(new Object[]{ 2350 , 92 , 67 , 100 , 3 });
        inputHandler.send(new Object[]{ 2317 , 12 , 37 , 20 , 4 });
        inputHandler.send(new Object[]{ 2000 , 40 , 12 , 57 , 9 });
        inputHandler.send(new Object[]{ 715 , 11 , 16 , 36 , 3 });
        inputHandler.send(new Object[]{ 660 , 49 , 14 , 49 , 9 });
        inputHandler.send(new Object[]{ 650 , 15 , 30 , 30 , 4 });
        inputHandler.send(new Object[]{ 260 , 12 , 13 , 14 , 0 });
        inputHandler.send(new Object[]{ 250 , 11 , 2 , 26 , 2 });
        inputHandler.send(new Object[]{ 200 , 50 , 31 , 73 , 3 });
        inputHandler.send(new Object[]{ 180 , 21 , 17 , 26 , 8 });

        Thread.sleep(100);
        executionPlanRuntime.shutdown();
    }
}
