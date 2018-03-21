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
package org.ballerinalang.siddhi.core.query.extension;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.extension.util.CustomFunctionExtension;
import org.ballerinalang.siddhi.core.query.extension.util.StringConcatAggregatorString;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.config.InMemoryConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ExtensionTestCase {
    private static final Logger log = LoggerFactory.getLogger(ExtensionTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void extensionTest1() throws InterruptedException {
        log.info("extension test1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = ("@info(name = 'query1') from cseEventStream select price , custom:getAll(symbol) as toConcat " +
                "group by volume insert into mailOutput;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 3) {
                    AssertJUnit.assertEquals("WSO2ABC", inEvents[inEvents.length - 1].getData(1));
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"ABC", 60.5f, 200L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void extensionTest2() throws InterruptedException, ClassNotFoundException {
        log.info("extension test2");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("custom:plus", CustomFunctionExtension.class);
        siddhiManager.setExtension("email:getAll", StringConcatAggregatorString.class);

        String cseEventStream = "define stream cseEventStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from cseEventStream select symbol , custom:plus(price,volume) as " +
                "totalCount " +
                "insert into mailOutput;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(800L, inEvent.getData(1));
                    } else if (count == 2) {
                        AssertJUnit.assertEquals(805L, inEvent.getData(1));
                    } else if (count == 3) {
                        AssertJUnit.assertEquals(260L, inEvent.getData(1));
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700L, 100L});
        inputHandler.send(new Object[]{"WSO2", 605L, 200L});
        inputHandler.send(new Object[]{"ABC", 60L, 200L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void extensionTest3() throws InterruptedException {
        log.info("extension test3");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("custom:plus", CustomFunctionExtension.class);
        siddhiManager.setExtension("email:getAllNew", StringConcatAggregatorString.class);

        String cseEventStream = "" +
                "" +
                "define stream cseEventStream (symbol string, price float, volume long);";
        String query = ("" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select price , email:getAllNew(symbol,'') as toConcat " +
                "group by volume " +
                "insert into mailOutput;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 3) {
                    AssertJUnit.assertEquals("WSO2ABC", inEvents[inEvents.length - 1].getData(1));
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"ABC", 60.5f, 200L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void extensionTest4() throws InterruptedException, ClassNotFoundException {
        log.info("extension test4");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("custom:plus", CustomFunctionExtension.class);
        siddhiManager.setExtension("email:getAll", StringConcatAggregatorString.class);


        String cseEventStream = "define stream cseEventStream (price long, volume long);";
        String query = ("@info(name = 'query1') from cseEventStream select  custom:plus(*) as totalCount " +
                "insert into mailOutput;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        AssertJUnit.assertEquals(800L, inEvent.getData(0));
                    } else if (count == 2) {
                        AssertJUnit.assertEquals(805L, inEvent.getData(0));
                    } else if (count == 3) {
                        AssertJUnit.assertEquals(260L, inEvent.getData(0));
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{700L, 100L});
        inputHandler.send(new Object[]{605L, 200L});
        inputHandler.send(new Object[]{60L, 200L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void extensionTest5() throws InterruptedException, ClassNotFoundException {
        log.info("extension test5");
        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("email.getAllNew.append.abc", "true");
        siddhiManager.setConfigManager(new InMemoryConfigManager(configMap, null));
        siddhiManager.setExtension("custom:plus", CustomFunctionExtension.class);
        siddhiManager.setExtension("email:getAllNew", StringConcatAggregatorString.class);

        String cseEventStream = "" +
                "" +
                "define stream cseEventStream (symbol string, price float, volume long);";
        String query = ("" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select price , email:getAllNew(symbol,'') as toConcat " +
                "group by volume " +
                "insert into mailOutput;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
                if (count == 3) {
                    AssertJUnit.assertEquals("WSO2ABC-abc", inEvents[inEvents.length - 1].getData(1));
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 100L});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"ABC", 60.5f, 200L});
        Thread.sleep(100);
        AssertJUnit.assertEquals(3, count);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }
}
