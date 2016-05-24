/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.query.window;

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

/**
 * @since Dec 23, 2015
 */
public class UniqueExternalTimeBatchWindowTestCase {


    private static final Logger log = Logger.getLogger(TimeWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void uniqueExternalTimeBatchWindowTest1() throws InterruptedException {
        log.info("uniqueExternalTimeBatchWindowTest test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream LoginEvents (timestamp long, ip string) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.uniqueExternalTimeBatch(ip, timestamp, 1 sec, 0, 2 sec) " +
                "select timestamp, ip, count() as total  " +
                "insert all events into uniqueIps ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        if (inEventCount == 1) {
                            Assert.assertEquals(3l, event.getData(2));
                        } else if (inEventCount == 2) {
                            Assert.assertEquals(2l, event.getData(2));
                        } else if (inEventCount == 3) {
                            Assert.assertEquals(3l, event.getData(2));
                        } else if (inEventCount == 4) {
                            Assert.assertEquals(4l, event.getData(2));
                        } else if (inEventCount == 5) {
                            Assert.assertEquals(2l, event.getData(2));
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LoginEvents");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{1366335804341l, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804599l, "192.10.1.3"});
        inputHandler.send(new Object[]{1366335804600l, "192.10.1.5"});
        inputHandler.send(new Object[]{1366335804607l, "192.10.1.6"});

        inputHandler.send(new Object[]{1366335805599l, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805600l, "192.10.1.4"});
        inputHandler.send(new Object[]{1366335805607l, "192.10.1.6"});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{1366335805606l, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335805605l, "192.10.1.8"});
        Thread.sleep(2100);
        inputHandler.send(new Object[]{1366335805606l, "192.10.1.6"});
        inputHandler.send(new Object[]{1366335805605l, "192.10.1.92"});

        inputHandler.send(new Object[]{1366335806606l, "192.10.1.9"});
        inputHandler.send(new Object[]{1366335806690l, "192.10.1.10"});
        Thread.sleep(3000);

        junit.framework.Assert.assertEquals("Event arrived", true, eventArrived);
        junit.framework.Assert.assertEquals("In Events ", 5, inEventCount);
        junit.framework.Assert.assertEquals("Remove Events ", 0, removeEventCount);
        executionPlanRuntime.shutdown();
    }

}
