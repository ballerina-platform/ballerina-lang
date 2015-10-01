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
 */age org.wso2.siddhi.core.query.window;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class CronWindowTestCase {
    private static final Logger log = Logger.getLogger(CronWindowTestCase.class);
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
    public void CronWindowTest1() throws InterruptedException {
        log.info("Testing cron window for current events");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.cron('*/5 * * * * ?') select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    if (event.isExpired()) {
                        removeEventCount++;
                    } else {
                        inEventCount++;
                    }
                }
                eventArrived = true;
            }
        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(7000);
        inputHandler.send(new Object[]{"IBM1", 700f, 0});
        inputHandler.send(new Object[]{"WSO22", 60.5f, 1});
        Thread.sleep(7000);
        inputHandler.send(new Object[]{"IBM43", 700f, 0});
        inputHandler.send(new Object[]{"WSO4343", 60.5f, 1});
        Thread.sleep(7000);
        Assert.assertEquals(6, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void CronWindowTest2() throws InterruptedException {
        log.info("Testing cron window for expired events");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream#window.cron('*/5 * * * * ?') select symbol,price,volume insert expired events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                        removeEventCount++;
                }
                eventArrived = true;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(7000);
        inputHandler.send(new Object[]{"IBM1", 700f, 0});
        inputHandler.send(new Object[]{"WSO22", 60.5f, 1});
        Thread.sleep(7000);
        inputHandler.send(new Object[]{"IBM43", 700f, 0});
        inputHandler.send(new Object[]{"WSO4343", 60.5f, 1});
        Thread.sleep(7000);
        Assert.assertEquals(4, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }


}
