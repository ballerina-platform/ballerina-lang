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

public class SimpleFilterSyncPerformance {
    private static int count = 0;
    private static volatile long start = System.currentTimeMillis();

    public static void main(String[] args) throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = " " +
                "define stream RequestStream (messageID string, app_key string, api_key string, app_tier string, " +
                "api_tier string, user_id string, properties string, timeNow long); " +
                "define stream EligibilityStream (rule string, messageID string, isEligible bool, isLocallyThrottled " +
                "bool, throttle_key string , timeNow long); ";

        String query = "" +
                "@info(name = 'query1') " +
                "FROM RequestStream " +
                "SELECT 'sub_gold' AS rule, messageID, ( api_tier == 'Gold') AS isEligible,false as " +
                "isLocallyThrottled,  'sub_gold_TEST1TEST1Test1_key' AS throttle_key , timeNow \n" +
                "INSERT INTO EligibilityStream; " +
                "@info(name = 'query2') " +
                "FROM EligibilityStream[isEligible==false]\n" +
                "\t\tSELECT rule, messageID, false AS isThrottled , timeNow\n" +
                "\t\tINSERT INTO ThrottleStream;\n" +
                "\n" +
                "@info(name = 'query3') " +
                "FROM EligibilityStream[isEligible==true AND isLocallyThrottled==true]\n" +
                "\t\tSELECT rule, messageID, true AS isThrottled , timeNow \n" +
                "\t\tINSERT INTO ThrottleStream; \n" +
                "\n" +
                "@info(name = 'query4') " +
                "FROM EligibilityStream[isEligible==true AND isLocallyThrottled==false]\n" +
                "\t\tSELECT rule, messageID, false AS isThrottled, timeNow \n" +
                "\t\tINSERT INTO ThrottleStream;  ";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
        siddhiAppRuntime.addCallback("ThrottleStream", new StreamCallback() {
                    public int eventCount = 0;
                    public int timeSpent = 0;
                    long startTime = System.currentTimeMillis();

                    @Override
                    public void receive(Event[] events) {
                        for (Event event : events) {
                            eventCount++;
                            timeSpent += (System.currentTimeMillis() - (Long) event.getData(3));
                            if (eventCount % 1000000 == 0) {
                                System.out.println("Throughput : " + (eventCount * 1000) / ((System.currentTimeMillis
                                        ()) - startTime));
                                System.out.println("Time spent :  " + (timeSpent * 1.0 / eventCount));
                                startTime = System.currentTimeMillis();
                                eventCount = 0;
                                timeSpent = 0;
                            }
                        }
                    }
                }
        );

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("RequestStream");
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
                    inputHandler.send(new Object[]{"IBM", "TEST1", "TEST1", "TEST1", "Gold", "Test1", null, System
                            .currentTimeMillis()});
                    inputHandler.send(new Object[]{"IBM", "TEST1", "TEST1", "TEST1", "Gold", "Test1", null, System
                            .currentTimeMillis()});
                    inputHandler.send(new Object[]{"IBM", "TEST1", "TEST1", "TEST1", "Gold", "Test1", null, System
                            .currentTimeMillis()});
                    inputHandler.send(new Object[]{"IBM", "TEST1", "TEST1", "TEST1", "Gold", "Test1", null, System
                            .currentTimeMillis()});
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }


        }
    }
}
