/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.aggregator;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class MaxAggregatorExtensionTestCase {
    private static final Logger log = Logger.getLogger(MaxAggregatorExtensionTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }


    @Test
    public void testMaxAggregatorExtension1() throws InterruptedException {
        log.info("MaxAggregator TestCase 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (price1 double,price2 double, price3 double);";
        String query = ("@info(name = 'query1') " +
                "from inputStream#window.time(1 sec) " +
                "select max(price1) as maxForeverValue " +
                "insert all events into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                query);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;
                for (Event event : events) {
                    count++;
                    switch (count) {
                        case 1:
                            Assert.assertEquals(36.0, event.getData(0));
                            break;
                        case 2:
                            Assert.assertEquals(37.88, event.getData(0));
                            break;
                        case 3:
                            Assert.assertEquals(37.88, event.getData(0));
                            break;
                        case 4:
                            Assert.assertEquals(null, event.getData(0));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{36d, 36.75, 35.75});
        inputHandler.send(new Object[]{37.88d, 38.12, 37.62});
        Thread.sleep(2000);

        Thread.sleep(300);
        Assert.assertEquals(4, count);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

}
