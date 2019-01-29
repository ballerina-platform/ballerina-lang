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

package org.ballerinalang.siddhi.core.query.function;

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
 * Testcase for UUID function.
 */
public class UUIDFunctionTestCase {

    private static final Logger log = LoggerFactory.getLogger(UUIDFunctionTestCase.class);
    private int count;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testFunctionQuery1() throws InterruptedException {

        log.info("UUIDFunction test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String planName = "@app:name('UUIDFunction') ";
        String cseEventStream = "define stream cseEventStream (symbol string, price double, volume long , quantity " +
                "int);";
        String query = "@info(name = 'query1') " +
                "from cseEventStream " +
                "select symbol, price as price, quantity, UUID() as uniqueValue " +
                "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(planName +
                cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                AssertJUnit.assertEquals(1.56, inEvents[0].getData()[1]);
                AssertJUnit.assertNotNull("UUID is expected", inEvents[0].getData()[3]);
                AssertJUnit.assertTrue("String UUID is expected", inEvents[0].getData()[3] instanceof String);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 1.56d, 60L, 6});
        Thread.sleep(200);
        org.testng.AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }
}
