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

package org.wso2.siddhi.performance;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import java.util.Random;

public class NoIndexingTablePerformance {

    public static void main(String[] args) throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();

        String executionPlan = "" +
//                "@plan:async" +
//                " " +
                "define stream StockCheckStream (symbol string, company string, price float, volume int, timestamp long);" +
                "define stream StockInputStream (symbol string, company string, price float, volume int); " +
                "" +
                "@IndexBy('volume')" +
                "define table StockTable (symbol string, company string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from StockInputStream " +
                "select symbol, company, price, volume " +
                "insert into StockTable ;" +
                "" +
//                "@info(name = 'query2') " +
//                "from StockCheckStream join StockTable " +
//                "on StockCheckStream.volume < StockTable.volume " +
//                "select StockCheckStream.timestamp, StockCheckStream.symbol, StockCheckStream.company, StockCheckStream.price, StockCheckStream.volume " +
//                "insert into OutputStream ;" +
//                "" +
                "@info(name = 'query2') " +
                "from StockCheckStream " +
                "select volume " +
                "delete StockTable " +
                "on volume < StockTable.volume " +
                "";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

//        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
//            public int eventCount = 0;
//            public int timeSpent = 0;
//            long startTime = System.currentTimeMillis();
//
//            @Override
//            public void receive(Event[] events) {
////                EventPrinter.print(events);
//                for (Event event : events) {
//                    eventCount++;
//                    timeSpent += (System.currentTimeMillis() - (Long) event.getData(0));
//                    if (eventCount % 100000 == 0) {
//                        System.out.println("Throughput : " + (eventCount * 1000) / ((System.currentTimeMillis()) - startTime));
//                        System.out.println("Time spent :  " + (timeSpent * 1.0 / eventCount));
//                        startTime = System.currentTimeMillis();
//                        eventCount = 0;
//                        timeSpent = 0;
//                    }
//                }
//            }
//
//        });

        InputHandler stockCheckInputHandler = executionPlanRuntime.getInputHandler("StockCheckStream");
        InputHandler stockInputInputHandler = executionPlanRuntime.getInputHandler("StockInputStream");
        executionPlanRuntime.start();

        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            stockInputInputHandler.send(new Object[]{"" + i, "" + i, random.nextFloat(), i});
        }

        for (int i = 0; i < 1; i++) {
            EventPublisher eventPublisher = new EventPublisher(stockCheckInputHandler, random);
            eventPublisher.run();
        }
        //executionPlanRuntime.shutdown();
    }


    static class EventPublisher implements Runnable {

        InputHandler inputHandler;
        private Random random;

        EventPublisher(InputHandler inputHandler, Random random) {
            this.inputHandler = inputHandler;
            this.random = random;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int number = random.nextInt(1000);
                    inputHandler.send(new Object[]{"" + number, "" + number, random.nextFloat(), number, System.currentTimeMillis()});
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
