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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.table;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

public class InsertIntoTableTestCase {
    private static final Logger log = Logger.getLogger(InsertIntoTableTestCase.class);
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
    public void insertIntoTableTest1() throws InterruptedException {
        log.info("InsertIntoTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

//        executionPlanRuntime.addCallback("query1", new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                if (inEvents != null) {
//                    inEventCount = inEventCount + inEvents.length;
//                }
//                if (removeEvents != null) {
//                    removeEventCount = removeEventCount + removeEvents.length;
//                }
//                eventArrived = true;
//            }
//
//        });

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        stockStream.send(new Object[]{"IBM", 75.6f, 100l});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);

        executionPlanRuntime.shutdown();

    }


//    @Test
//    public void timeWindowTest2() throws InterruptedException {
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
//        String query = "@info(name = 'query1') from cseEventStream#window.time(1 sec) select symbol,price," +
//                "volume insert into outputStream ;";
//
//        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);
//
//        executionPlanRuntime.addCallback("query1", new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                if (inEvents != null) {
//                    inEventCount = inEventCount + inEvents.length;
//                }
//                if (removeEvents != null) {
//                    Assert.assertEquals("InEvents arrived before RemoveEvents", inEventCount - 2, removeEventCount);
//                    removeEventCount = removeEventCount + removeEvents.length;
//                }
//                eventArrived = true;
//            }
//
//        });
//
//        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
//        executionPlanRuntime.start();
//        inputHandler.send(new Object[]{"IBM", 700f, 1});
//        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
//        Thread.sleep(1100);
//        inputHandler.send(new Object[]{"IBM", 700f, 3});
//        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
//        Thread.sleep(1100);
//        inputHandler.send(new Object[]{"IBM", 700f, 5});
//        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
//        Thread.sleep(3000);
//        Assert.assertEquals(6, inEventCount);
//        Assert.assertEquals(6, removeEventCount);
//        Assert.assertTrue(eventArrived);
//        executionPlanRuntime.shutdown();
//
//    }

}
