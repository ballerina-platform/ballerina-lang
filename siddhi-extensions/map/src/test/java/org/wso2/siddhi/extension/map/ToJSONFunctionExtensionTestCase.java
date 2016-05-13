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

package org.wso2.siddhi.extension.map;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.map.test.util.SiddhiTestHelper;
import org.wso2.siddhi.extension.string.ConcatFunctionExtension;

import java.util.concurrent.atomic.AtomicInteger;

public class ToJSONFunctionExtensionTestCase {
    private static final Logger log = Logger.getLogger(ToJSONFunctionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testToJSONFunctionExtension() throws InterruptedException {
        log.info("ToJSONFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("str:concat", ConcatFunctionExtension.class);

        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream select "
                + "map:createFromJSON(\"{'symbol':'WSO2','price':100,'volume':100,'last5val':{'price':150,'volume':200}}\") as hashMap insert into outputStream;" +
                "from outputStream " +
                "select map:toJSON(hashMap) as jsonString " +
                "insert into outputStream2");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("outputStream2", new StreamCallback() {
            @Override
            public void receive(Event[] inEvents) {
                EventPrinter.print(inEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        Assert.assertEquals(event.getData(0) instanceof String, true);
                        try {
                            JSONAssert.assertEquals(new JSONObject("{\"volume\":100,\"symbol\":\"WSO2\",\"price\":100,\"last5val\":{\"volume\":200,\"price\":150}}"),
                                    new JSONObject((String) event.getData(0)), false);
                        } catch (JSONException e) {
                            log.error(e);
                            Assert.fail(e.getMessage());
                        }
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 100, 100l});
        inputHandler.send(new Object[]{"WSO2", 200, 200l});
        inputHandler.send(new Object[]{"XYZ", 300, 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testToJSONFunctionExtension2() throws InterruptedException {
        log.info("ToJSONFunctionExtension TestCase 2");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("str:concat", ConcatFunctionExtension.class);

        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream select "
                + "map:createFromJSON(str:concat('{symbol :',symbol,', price :',price,', volume :',volume,'}')) as hashMap insert into outputStream;" +
                "from outputStream " +
                "select map:toJSON(hashMap) as jsonString " +
                "insert into outputStream2");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("outputStream2", new StreamCallback() {
            @Override
            public void receive(Event[] inEvents) {
                EventPrinter.print(inEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        Assert.assertEquals(event.getData(0) instanceof String, true);
                        try {
                            JSONAssert.assertEquals(new JSONObject("{\"volume\":100,\"symbol\":\"IBM\",\"price\":100}"),
                                    new JSONObject((String) event.getData(0)), false);
                        } catch (JSONException e) {
                            log.error(e);
                            Assert.fail(e.getMessage());
                        }
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals(event.getData(0) instanceof String, true);
                        try {
                            JSONAssert.assertEquals(new JSONObject("{\"volume\":200,\"symbol\":\"WSO2\",\"price\":200}"),
                                    new JSONObject((String) event.getData(0)), false);
                        } catch (JSONException e) {
                            log.error(e);
                            Assert.fail(e.getMessage());
                        }
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals(event.getData(0) instanceof String, true);
                        try {
                            JSONAssert.assertEquals(new JSONObject("{\"volume\":200,\"symbol\":\"XYZ\",\"price\":300}"),
                                    new JSONObject((String) event.getData(0)), false);
                        } catch (JSONException e) {
                            log.error(e);
                            Assert.fail(e.getMessage());
                        }
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 100, 100l});
        inputHandler.send(new Object[]{"WSO2", 200, 200l});
        inputHandler.send(new Object[]{"XYZ", 300, 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
