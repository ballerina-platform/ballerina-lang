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

package org.wso2.siddhi.extension.math;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class AtanFunctionExtensionTestCase {
    private static Logger logger = Logger.getLogger(AtanFunctionExtensionTestCase.class);

    @Test
    public void testProcess1() throws Exception {
        logger.info("AtanFunctionExtension TestCase With one function parameter");

        SiddhiManager siddhiManager = new SiddhiManager();
        String inValueStream = "define stream InValueStream (inValue double);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from InValueStream "
                + "select math:atan(inValue) as atanValue "
                + "insert into OutMediationStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inValueStream + eventFuseExecutionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Double results;
                for (Event event : inEvents) {
                    results = (Double) event.getData(0);
                    Assert.assertEquals((Double) 1.4056476493802699, results);
                }
            }
        });
        InputHandler inputHandler = executionPlanRuntime
                .getInputHandler("InValueStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{6d});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testProcess2() throws Exception {
        logger.info("AtanFunctionExtension TestCase With two function parameters");

        SiddhiManager siddhiManager = new SiddhiManager();
        String inValueStream = "define stream InValueStream (inValue1 double, inValue2 double);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from InValueStream "
                + "select math:atan(inValue1, inValue2) as convertedValue "
                + "insert into OutMediationStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inValueStream + eventFuseExecutionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Double results;
                for (Event event : inEvents) {
                    results = (Double) event.getData(0);
                    Assert.assertEquals((Double) 1.1760052070951352, results);
                }
            }
        });
        InputHandler inputHandler = executionPlanRuntime
                .getInputHandler("InValueStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{12d, 5d});
        Thread.sleep(100);
        executionPlanRuntime.shutdown();
    }
}
