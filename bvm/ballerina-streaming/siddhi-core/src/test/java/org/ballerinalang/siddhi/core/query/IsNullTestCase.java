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

package org.ballerinalang.siddhi.core.query;

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
 * Testcase for IsNull queries.
 */
public class IsNullTestCase {
    private static final Logger log = LoggerFactory.getLogger(IsNullTestCase.class);
    private int count;
    private boolean eventArrived;
    private int inEventCount;
    private int removeEventCount;

    @BeforeMethod
    public void init() {
        count = 0;
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }


    @Test
    public void isNullTest1() throws InterruptedException {
        log.info("isNull test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:name('IsNullTest') " +
                "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[symbol is null] " +
                "select symbol, price " +
                "insert into outputStream;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        log.info("Running : " + siddhiAppRuntime.getName());

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                AssertJUnit.assertTrue(inEvents[0].getData(0) == null);
                count = count + inEvents.length;
                eventArrived = true;
            }

        });


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");

        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        inputHandler.send(new Object[]{null, 60.5f, 200L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        AssertJUnit.assertTrue(eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void isNullTest2() throws InterruptedException {
        log.info("isNull test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[(price>=e2[last].price and not e2[last-1] is null and price>=e2[last-1].price+5)  or (" +
                " e2[last-1] is null and price>=e1.price+5 )]+, " +
                "   e3=Stream1[price<e2[last].price]" +
                "select e1.price as price1, e2[0].price as price2, e2[last-2] is null as check1, e2[last-1].price as " +
                "price3, e2[last].price as price4, e3.price as price5, e2 is null as check2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                org.testng.AssertJUnit.assertArrayEquals(new Object[]{43.6f, 58.7f, true, null, 58.7f,
                                        45.6f, false}, event.getData());
                                break;
                            default:
                                org.testng.AssertJUnit.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 41.5f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 42.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 43.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 45.6f, 100});
        Thread.sleep(100);

        org.testng.AssertJUnit.assertEquals("Number of success events", 1, inEventCount);
        org.testng.AssertJUnit.assertEquals("Number of remove events", 0, removeEventCount);
        org.testng.AssertJUnit.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }

}
