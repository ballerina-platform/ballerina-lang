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

public class LengthTimeLinearRegressionForecastTestcase {
    static final Logger logger = Logger.getLogger(LengthTimeLinearRegressionTestcase.class);
    private static SiddhiManager siddhiManager;
    private int count;
    private double betaOne, betaTwo, forecastY;
    private boolean outlier;

    @Before
    public void init() {
        count = 0;
    }

    @Test
    public void simpleForecastTest1() throws Exception {
        logger.info("Simple Forecast TestCase");

        siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (y double, symbol string, x double);";

        // Limit number of events based on length window (query):
        String executionPlan = ("@info(name = 'query1') from InputStream#timeseries:lengthTimeForecast(20 min, 20, x+2, 2, 0.95, y, x) "
                + "select * "
                + "insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inputStream + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                forecastY = (Double) inEvents[inEvents.length-1].getData(6);
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();

        System.out.println(System.currentTimeMillis());

        // Limit number of events based on length window (test case):
        inputHandler.send(new Object[]{3300,"IBM",31});
        inputHandler.send(new Object[]{2600,"GOOG",18});
        inputHandler.send(new Object[]{2500,"GOOG",17});
        inputHandler.send(new Object[]{2475,"APPL",12});
        inputHandler.send(new Object[]{2313,"MSFT",8});
        inputHandler.send(new Object[]{2175,"IBM",26});
        inputHandler.send(new Object[]{600,"APPL",14});
        inputHandler.send(new Object[]{460,"APPL",3});
        inputHandler.send(new Object[]{240,"IBM",1});
        inputHandler.send(new Object[]{200,"GOOG",10});
        inputHandler.send(new Object[]{177,"GOOG",0});
        inputHandler.send(new Object[]{140,"APPL",6});
        inputHandler.send(new Object[]{117,"MSFT",1});
        inputHandler.send(new Object[]{115,"IBM",0});
        inputHandler.send(new Object[]{2600,"APPL",19});
        inputHandler.send(new Object[]{1907,"APPL",13});
        inputHandler.send(new Object[]{1190,"IBM",3});
        inputHandler.send(new Object[]{990,"GOOG",16});
        inputHandler.send(new Object[]{925,"GOOG",6});
        inputHandler.send(new Object[]{365,"APPL",0});
        inputHandler.send(new Object[]{302,"MSFT",10});
        inputHandler.send(new Object[]{300,"IBM",6});
        inputHandler.send(new Object[]{129,"APPL",2});
        inputHandler.send(new Object[]{111,"APPL",1});
        inputHandler.send(new Object[]{6100,"IBM",18});
        inputHandler.send(new Object[]{4125,"GOOG",19});
        inputHandler.send(new Object[]{3213,"GOOG",1});
        inputHandler.send(new Object[]{2319,"APPL",38});
        inputHandler.send(new Object[]{2000,"MSFT",10});
        inputHandler.send(new Object[]{1600,"IBM",0});
        inputHandler.send(new Object[]{1394,"APPL",4});
        inputHandler.send(new Object[]{935,"APPL",4});
        inputHandler.send(new Object[]{850,"IBM",0});
        inputHandler.send(new Object[]{775,"GOOG",5});
        inputHandler.send(new Object[]{760,"GOOG",6});
        inputHandler.send(new Object[]{629,"APPL",1});
        inputHandler.send(new Object[]{275,"MSFT",6});
        inputHandler.send(new Object[]{120,"IBM",0});
        inputHandler.send(new Object[]{2567,"APPL",12});
        inputHandler.send(new Object[]{2500,"APPL",28});
        inputHandler.send(new Object[]{2350,"IBM",21});
        inputHandler.send(new Object[]{2317,"GOOG",3});
        inputHandler.send(new Object[]{2000,"GOOG",12});
        inputHandler.send(new Object[]{715,"APPL",1});
        inputHandler.send(new Object[]{660,"MSFT",9});
        inputHandler.send(new Object[]{650,"IBM",0});
        inputHandler.send(new Object[]{260,"APPL",0});
        inputHandler.send(new Object[]{250,"APPL",1});
        inputHandler.send(new Object[]{200,"IBM",13});
        inputHandler.send(new Object[]{180,"GOOG",6});

        Thread.sleep(100);

        Assert.assertEquals("Beta0: ", 1120.4279565868264, forecastY, 1120.4279565868264-forecastY);
        Assert.assertEquals("No of events: ", 25, count);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void simpleForecastTest2() throws Exception {
        logger.info("Simple Forecast TestCase");

        siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (y double, symbol string, x double);";

        // Limit number of events based on time window (query):
        String executionPlan = ("@info(name = 'query1') from InputStream#timeseries:lengthTimeForecast(200, 100000, x+2, 2, 0.95, y, x) "
                + "select * insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inputStream + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                forecastY = (Double) inEvents[inEvents.length-1].getData(6);
            }
        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();

        System.out.println(System.currentTimeMillis());

        // Limit number of events based on time window (test case):
        inputHandler.send(new Object[]{3300,"IBM",31});
        inputHandler.send(new Object[]{2600,"GOOG",18});
        inputHandler.send(new Object[]{2500,"GOOG",17});
        inputHandler.send(new Object[]{2475,"APPL",12});
        inputHandler.send(new Object[]{2313,"MSFT",8});
        inputHandler.send(new Object[]{2175,"IBM",26});
        inputHandler.send(new Object[]{600,"APPL",14});
        inputHandler.send(new Object[]{460,"APPL",3});
        inputHandler.send(new Object[]{240,"IBM",1});
        inputHandler.send(new Object[]{200,"GOOG",10});
        inputHandler.send(new Object[]{177,"GOOG",0});
        inputHandler.send(new Object[]{140,"APPL",6});
        inputHandler.send(new Object[]{117,"MSFT",1});
        inputHandler.send(new Object[]{115,"IBM",0});
        inputHandler.send(new Object[]{2600,"APPL",19});
        inputHandler.send(new Object[]{1907,"APPL",13});
        inputHandler.send(new Object[]{1190,"IBM",3});
        inputHandler.send(new Object[]{990,"GOOG",16});
        inputHandler.send(new Object[]{925,"GOOG",6});
        inputHandler.send(new Object[]{365,"APPL",0});
        inputHandler.send(new Object[]{302,"MSFT",10});
        inputHandler.send(new Object[]{300,"IBM",6});
        inputHandler.send(new Object[]{129,"APPL",2});
        inputHandler.send(new Object[]{111,"APPL",1});
        inputHandler.send(new Object[]{6100,"IBM",18});
        inputHandler.send(new Object[]{4125,"GOOG",19});
        inputHandler.send(new Object[]{3213,"GOOG",1});
        inputHandler.send(new Object[]{2319,"APPL",38});
        inputHandler.send(new Object[]{2000,"MSFT",10});
        inputHandler.send(new Object[]{1600,"IBM",0});
        Thread.sleep(200);
        inputHandler.send(new Object[]{1394,"APPL",4});
        inputHandler.send(new Object[]{935,"APPL",4});
        inputHandler.send(new Object[]{850,"IBM",0});
        inputHandler.send(new Object[]{775,"GOOG",5});
        inputHandler.send(new Object[]{760,"GOOG",6});
        inputHandler.send(new Object[]{629,"APPL",1});
        inputHandler.send(new Object[]{275,"MSFT",6});
        inputHandler.send(new Object[]{120,"IBM",0});
        inputHandler.send(new Object[]{2567,"APPL",12});
        inputHandler.send(new Object[]{2500,"APPL",28});
        inputHandler.send(new Object[]{2350,"IBM",21});
        inputHandler.send(new Object[]{2317,"GOOG",3});
        inputHandler.send(new Object[]{2000,"GOOG",12});
        inputHandler.send(new Object[]{715,"APPL",1});
        inputHandler.send(new Object[]{660,"MSFT",9});
        inputHandler.send(new Object[]{650,"IBM",0});
        inputHandler.send(new Object[]{260,"APPL",0});
        inputHandler.send(new Object[]{250,"APPL",1});
        inputHandler.send(new Object[]{200,"IBM",13});
        inputHandler.send(new Object[]{180,"GOOG",6});

        Thread.sleep(100);

        Assert.assertEquals("Beta0: ", 1120.4279565868264, forecastY, 1120.4279565868264-forecastY);
        Assert.assertEquals("No of events: ", 25, count);

        executionPlanRuntime.shutdown();

    }

}
