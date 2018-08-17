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

package org.ballerinalang.siddhi.core.query.trigger;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.query.api.SiddhiApp;
import org.ballerinalang.siddhi.query.api.definition.TriggerDefinition;
import org.ballerinalang.siddhi.query.api.exception.DuplicateDefinitionException;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testcase for triggers.
 */
public class TriggerTestCase {
    private static final Logger log = LoggerFactory.getLogger(TriggerTestCase.class);
    private volatile int count;
    private volatile long lastTimeStamp;
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        lastTimeStamp = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testTrigger1 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        TriggerDefinition triggerDefinition = TriggerDefinition.id("cseEventStream").atEvery(Expression.Time.milliSec
                (500));

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineTrigger(triggerDefinition);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testQuery2() throws InterruptedException {
        log.info("testTrigger2 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        TriggerDefinition triggerDefinition = TriggerDefinition.id("cseEventStream").atEvery(Expression.Time.milliSec
                (500)).at("start");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineTrigger(triggerDefinition);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = DuplicateDefinitionException.class)
    public void testQuery3() throws InterruptedException {
        log.info("testTrigger3 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define trigger StockStream at 'start' ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);

        siddhiAppRuntime.start();
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("testTrigger4 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (triggered_time long); " +
                "define trigger StockStream at 'start' ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);

        siddhiAppRuntime.start();
        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testFilterQuery5() throws InterruptedException {
        log.info("testTrigger5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String plan = "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "define trigger triggerStream at 'start';";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(plan);

        siddhiAppRuntime.addCallback("triggerStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count += events.length;
                eventArrived = true;
            }
        });

        siddhiAppRuntime.start();

        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        AssertJUnit.assertEquals(true, eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testFilterQuery6() throws InterruptedException {
        log.info("testTrigger6 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String plan = "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "define trigger triggerStream at every 500 milliseconds ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(plan);

        siddhiAppRuntime.addCallback("triggerStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count += events.length;
                eventArrived = true;
            }
        });

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        AssertJUnit.assertEquals(2, count);
        AssertJUnit.assertEquals(true, eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void testFilterQuery7() throws InterruptedException {
        log.info("testTrigger7 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String plan = "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "define trigger triggerStream at '*/1 * * * * ?' ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(plan);

        siddhiAppRuntime.addCallback("triggerStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    long timestamp = event.getTimestamp();
                    count++;
                    if (count > 1) {
                        float triggerTimeDiff = timestamp / 1000 - lastTimeStamp / 1000;
                        AssertJUnit.assertTrue(1.0f == triggerTimeDiff);
                    }
                    lastTimeStamp = timestamp;
                }
                eventArrived = true;
            }
        });

        siddhiAppRuntime.start();

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
        AssertJUnit.assertEquals(true, eventArrived);

    }

}
