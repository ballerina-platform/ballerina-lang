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

package org.wso2.siddhi.extension.ml;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class PredictStreamProcessorTestCase {

    private volatile boolean eventArrived;

    @Before
    public void init() {
        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        valueHolder.setMlProperties(new Properties());
        eventArrived = false;
    }

    @Test
    public void predictFunctionTest() throws InterruptedException, URISyntaxException {

        URL resource = PredictStreamProcessorTestCase.class.getResource("/test-model");
        String modelStorageLocation = new File(resource.toURI()).getAbsolutePath();

        SiddhiManager siddhiManager = new SiddhiManager();

        String inputStream = "define stream InputStream " +
                "(NumPregnancies double, PG2 double, DBP double, TSFT double, SI2 double, BMI double, DPF double, Age double);";

        String query = "@info(name = 'query1') " +
                "from InputStream#ml:predict('"+ modelStorageLocation +"') " +
                "select * " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inputStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    Assert.assertEquals(0.9176214029655854, inEvents[0].getData(8));
                }
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{6, 148, 72, 35, 0, 33.6, 0.627, 50});
        Thread.sleep(1000);
        junit.framework.Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void predictFunctionWithSelectedAttributesTest() throws InterruptedException, URISyntaxException {

        URL resource = PredictStreamProcessorTestCase.class.getResource("/test-model");
        String modelStorageLocation = new File(resource.toURI()).getAbsolutePath();

        SiddhiManager siddhiManager = new SiddhiManager();

        String inputStream = "define stream InputStream " +
                "(NumPregnancies double, PG2 double, DBP double, TSFT double, SI2 double, BMI double, DPF double, Age double);";

        String query = "@info(name = 'query1') " +
                "from InputStream#ml:predict('" + modelStorageLocation + "', NumPregnancies, PG2, DBP, TSFT, SI2, BMI, DPF, Age) " +
                "select prediction " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inputStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    Assert.assertEquals(0.9176214029655854, inEvents[0].getData(0));
                }
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{6, 148, 72, 35, 0, 33.6, 0.627, 50});
        Thread.sleep(1000);
        junit.framework.Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
