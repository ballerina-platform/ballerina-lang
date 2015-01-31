/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.managment;

import org.junit.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

public class PersistenceTestCase {
    static final Logger log = Logger.getLogger(PersistenceTestCase.class);
    private int count;
    private boolean eventArrived;
    private long lastValue;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
        lastValue = 0;
    }

    @Test
    public void persistenceTest1() throws InterruptedException {
        log.info("persistence test 1");

        PersistenceStore persistenceStore = new InMemoryPersistenceStore();
        String revision;

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(persistenceStore);

        String executionPlan = "" +
                "@plan:name('Test') " +
                "" +
                "define stream StockStream ( symbol string, price float, volume int );" +
                "" +
                "@info(name = 'query1')" +
                "from StockStream[price>10]#window.length(10) " +
                "select symbol, price, sum(volume) as totalVol " +
                "insert into OutStream ";

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    Assert.assertTrue("IBM".equals(inEvent.getData(0)) || "WSO2".equals(inEvent.getData(0)));
                    lastValue = (Long) inEvent.getData(2);
                }
            }
        };

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("StockStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        Thread.sleep(100);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(2, count);

        //persisting
        Thread.sleep(500);
        revision = executionPlanRuntime.persist();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //restarting execution plan
        Thread.sleep(500);
        executionPlanRuntime.shutdown();
        executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("query1", queryCallback);
        inputHandler = executionPlanRuntime.getInputHandler("StockStream");
        executionPlanRuntime.start();

        //loading
        executionPlanRuntime.restoreLastRevision();

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        //shutdown execution plan
        Thread.sleep(500);
        executionPlanRuntime.shutdown();

        Assert.assertEquals(6, count);
        Assert.assertEquals(400, lastValue);
        Assert.assertEquals(true, eventArrived);

    }

}
