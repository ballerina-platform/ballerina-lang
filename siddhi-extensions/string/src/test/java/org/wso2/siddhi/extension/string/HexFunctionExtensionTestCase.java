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

package org.wso2.siddhi.extension.string;

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
import org.wso2.siddhi.extension.string.test.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class HexFunctionExtensionTestCase {
    private static Logger logger = Logger.getLogger(HexFunctionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);

    protected static SiddhiManager siddhiManager;

    @Before
    public void init() {
        count.set(0);
    }

    @Test
    public void testProcess() throws Exception {
        logger.info("HexFunctionExtension TestCase");

        siddhiManager = new SiddhiManager();
        String inValueStream = "@config(async = 'true')define stream InValueStream (inValue string);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from InValueStream "
                + "select str:hex(inValue) as hexString "
                + "insert into OutMediationStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inValueStream + eventFuseExecutionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                String result;
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    result = (String) event.getData(0);
                    Assert.assertEquals("4d7953514c", result);
                }
            }
        });
        InputHandler inputHandler = executionPlanRuntime
                .getInputHandler("InValueStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"MySQL"});
        SiddhiTestHelper.waitForEvents(1000, 1, count, 60000);
        executionPlanRuntime.shutdown();
    }
}
