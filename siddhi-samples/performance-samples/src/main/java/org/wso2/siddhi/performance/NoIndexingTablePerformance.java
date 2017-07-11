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

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Random;

public class NoIndexingTablePerformance {

    public static boolean output = false;
    public static int numberOfEventsStored = 1000000;
    public static int numberOfEventsChunked = 10000;

    public static void main(String[] args) throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
//                "@app:async" +
//                " " +
                "define stream StockCheckStream (symbol string, company string, price float, volume int, timestamp " +
                "long);" +
                "define stream StockInputStream (symbol string, company string, price float, volume int); " +
                "" +
                "@PrimaryKey('symbol')" +
                "@Index('volume')" +
                "define table StockTable (symbol string, company string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from StockInputStream " +
                "select symbol, company, price, volume " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockCheckStream join StockTable " +
                "on StockCheckStream.symbol == StockTable.symbol and StockCheckStream.volume == StockTable.volume  " +
                "select StockCheckStream.timestamp, StockCheckStream.symbol, StockCheckStream.company, " +
                "StockCheckStream.price, StockCheckStream.volume " +
                "insert into OutputStream ;" +
                "" +
//                "@info(name = 'query2') " +
//                "from StockCheckStream " +
//                "select volume " +
//                "delete StockTable " +
//                "on volume < StockTable.volume " +
//                "" +
//                "@info(name = 'query2') " +
//                "from StockCheckStream " +
//                "select symbol, price " +
//                "update StockTable " +
//                "on symbol == StockTable.symbol " +
//                "" +
//                "@info(name = 'query2') " +
//                "from StockCheckStream[volume < StockTable.volume in StockTable] " +
//                "select symbol, price " +
//                "insert into OutputStream; " +
                "";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        System.out.println("Throughput\tLatency\tAvg Throughput\tAvg Latency");

//        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
//            public volatile double eventCount = 0;
//            public volatile double timeSpent = 0;
//            public volatile double totalThroughput = 0;
//            public volatile double totalLatency = 0;
//            public volatile double totalIterations = 0;
//            volatile long startTime = System.currentTimeMillis();
//
//            @Override
//            public void receive(Event[] events) {
//
////                EventPrinter.print(events);
//                for (Event event : events) {
//                    if (output) {
//                        eventCount++;
//                        timeSpent += (System.currentTimeMillis() - (Long) events[0].getData(0));
//                        if (eventCount % numberOfEventsChunked == 0) {
//                            synchronized (this) {
//                                totalIterations++;
//                                double throughput = (eventCount * 1000.0) / ((System.currentTimeMillis()) -
// startTime);
//                                double latency = (timeSpent * 1.0 / eventCount);
//                                totalThroughput += throughput;
//                                totalLatency += latency;
//                                System.out.println(throughput + "\t" + latency + "\t" + totalThroughput /
// totalIterations + "\t" + totalLatency / totalIterations);
//                                startTime = System.currentTimeMillis();
//                                eventCount = 0;
//                                timeSpent = 0;
//                            }
//                        }
//                    }
//                }
//            }
//
//        });

        InputHandler stockCheckInputHandler = siddhiAppRuntime.getInputHandler("StockCheckStream");
        InputHandler stockInputInputHandler = siddhiAppRuntime.getInputHandler("StockInputStream");
        siddhiAppRuntime.start();

        Random random = new Random();
        for (int i = 0; i < numberOfEventsStored; i++) {
            stockInputInputHandler.send(new Object[]{"" + i, "" + i, i * 1.0f, i});
        }

        for (int i = 0; i < 1; i++) {
            EventPublisher eventPublisher = new EventPublisher(stockCheckInputHandler, random);
            eventPublisher.run();
        }
        //siddhiAppRuntime.shutdown();
    }


    static class EventPublisher implements Runnable {

        public volatile double eventCount = 0;
        public volatile double timeSpent = 0;
        public volatile double totalThroughput = 0;
        public volatile double totalLatency = 0;
        public volatile double totalIterations = 0;
        volatile long startTime = System.currentTimeMillis();


        InputHandler inputHandler;
        private Random random;

        EventPublisher(InputHandler inputHandler, Random random) {
            this.inputHandler = inputHandler;
            this.random = random;
        }

        @Override
        public void run() {
            int count = 100000000;
            while (count > 0) {
                count--;
                try {
                    int number = random.nextInt(numberOfEventsStored);
//                    int number = (numberOfEventsStored*99)/100;
                    long startEventTime = System.currentTimeMillis();
                    inputHandler.send(new Object[]{"" + number, "" + number, random.nextFloat(), number, System
                            .currentTimeMillis()});
                    if (!output) {
                        eventCount++;
                        timeSpent += (System.currentTimeMillis() - startEventTime);
                        if (eventCount % numberOfEventsChunked == 0) {
                            synchronized (this) {
                                totalIterations++;
                                if (totalIterations == 1001) {
                                    System.exit(0);
                                }
                                double throughput = (eventCount * 1000.0) / ((System.currentTimeMillis()) - startTime);
                                double latency = (timeSpent * 1.0 / eventCount);
                                totalThroughput += throughput;
                                totalLatency += latency;
                                System.out.println(throughput + "\t" + latency + "\t" + totalThroughput /
                                        totalIterations + "\t" + totalLatency / totalIterations);
                                startTime = System.currentTimeMillis();
                                eventCount = 0;
                                timeSpent = 0;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
