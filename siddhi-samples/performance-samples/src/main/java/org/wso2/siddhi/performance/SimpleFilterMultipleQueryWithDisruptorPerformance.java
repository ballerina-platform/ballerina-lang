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
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

public class SimpleFilterMultipleQueryWithDisruptorPerformance {

    public static void main(String[] args) throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "" +
                "@app:async" +
                " " +
                "define stream cseEventStream (symbol string, price float, volume int, timestamp long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into outputStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from cseEventStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            public int eventCount = 0;
            public int timeSpent = 0;
            long startTime = System.currentTimeMillis();

            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventCount++;
                    timeSpent += (System.currentTimeMillis() - (Long) event.getData(3));
                    if (eventCount % 1000000 == 0) {
                        System.out.println("Throughput : " + (eventCount * 1000) / ((System.currentTimeMillis()) -
                                startTime));
                        System.out.println("Time spent :  " + (timeSpent * 1.0 / eventCount));
                        startTime = System.currentTimeMillis();
                        eventCount = 0;
                        timeSpent = 0;
                    }
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();


        for (int i = 0; i <= 100; i++) {
            EventPublisher eventPublisher = new EventPublisher(inputHandler);
            eventPublisher.run();
        }
        //siddhiAppRuntime.shutdown();
    }


    static class EventPublisher implements Runnable {

        InputHandler inputHandler;

        EventPublisher(InputHandler inputHandler) {
            this.inputHandler = inputHandler;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    inputHandler.send(new Object[]{"WSO2", 55.6f, 100, System.currentTimeMillis()});
                    inputHandler.send(new Object[]{"IBM", 75.6f, 100, System.currentTimeMillis()});
                    inputHandler.send(new Object[]{"WSO2", 100f, 80, System.currentTimeMillis()});
                    inputHandler.send(new Object[]{"IBM", 75.6f, 100, System.currentTimeMillis()});
                    inputHandler.send(new Object[]{"WSO2", 55.6f, 100, System.currentTimeMillis()});
                    inputHandler.send(new Object[]{"IBM", 75.6f, 100, System.currentTimeMillis()});
                    inputHandler.send(new Object[]{"WSO2", 100f, 80, System.currentTimeMillis()});
                    inputHandler.send(new Object[]{"IBM", 75.6f, 100, System.currentTimeMillis()});
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
