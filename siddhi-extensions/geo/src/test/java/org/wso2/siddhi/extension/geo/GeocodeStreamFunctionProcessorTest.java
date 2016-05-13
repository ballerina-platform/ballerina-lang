/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.geo;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.List;

public class GeocodeStreamFunctionProcessorTest {

    private static final Logger LOGGER = Logger.getLogger(GeocodeStreamFunctionProcessorTest.class);
    private static int eventCount = 0;

    @Test
    public void testProcess() throws Exception {
        LOGGER.info("Init Siddhi setUp");

        SiddhiManager siddhiManager = new SiddhiManager();
        long start = System.currentTimeMillis();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime("define stream geocodeStream (location string, level string, time string);"
                + "@info(name = 'query1') from geocodeStream#geo:geocode(location) " +
                " select latitude, longitude, formattedAddress " +
                " insert into dataOut");
        long end = System.currentTimeMillis();
        LOGGER.info(String.format("Time to add query: [%f sec]", ((end - start) / 1000f)));

        List<Object[]> data = new ArrayList<Object[]>();
        data.add(new Object[]{"gunasekara mawatha", "Regular", "Sun Nov 02 13:36:05 +0000 2014"});
        data.add(new Object[]{"hendala road", "Regular", "Sun Nov 12 13:36:05 +0000 2014"});
        data.add(new Object[]{"mt lavinia", "Regular", "Sun Nov 10 13:36:05 +0000 2014"});
        data.add(new Object[]{"duplication rd", "Regular", "Sun Nov 02 13:36:05 +0000 2014"});

        final List<Object[]> expectedResult = new ArrayList<Object[]>();
        expectedResult.add(new Object[]{5.9461591d, 80.4978628d, "Gunasekara Mawatha, Matara, Sri Lanka"});
        expectedResult.add(new Object[]{6.9955449d, 79.8832741d, "Hendala Road, Wattala, Sri Lanka"});
        expectedResult.add(new Object[]{6.8390463d, 79.8646835d, "Mount Lavinia, Sri Lanka"});
        expectedResult.add(new Object[]{6.8995244d, 79.8556202d, "R A De Mel Mawatha, Colombo, Sri Lanka"});


        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    Object[] expected = expectedResult.get(eventCount);
                    Assert.assertEquals((Double) expected[0], (Double) event.getData(0), 1e-6);
                    Assert.assertEquals((Double) expected[1], (Double) event.getData(1), 1e-6);
                    Assert.assertEquals(expected[2], event.getData(2));
                    eventCount++;
                }
            }
        });

        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("geocodeStream");
        for (Object[] dataLine : data) {
            inputHandler.send(dataLine);
        }
        Thread.sleep(2000);
    }
}