/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
package org.wso2.siddhi.core;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ValidatorException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.Map;

public class PerformanceTestCase {

    private int count;
    private boolean eventArrived;
    Map<String, StreamDefinition> streamDefinitionMap = new HashMap<String, StreamDefinition>();
    private static volatile long start = System.currentTimeMillis();

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }


    @Test
    public void performanceQuery1() throws InterruptedException, ValidatorException {


        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 3, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count++;
                if (count % 1000000 == 0) {
                    long end = System.currentTimeMillis();
                    String tp = "," + ((1000000 * 1000.0 / (end - start)));
                    System.out.print(tp);
                    start = end;
                }
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        while (true) {
            inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        }

    }

    @Test
    public void performanceQuery2() throws InterruptedException, ValidatorException {
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[70 > price] select symbol,price,volume insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count++;
                if (count % 2000000 == 0) {
                    long end = System.currentTimeMillis();
                    String tp = "," + (2000000 * 1000.0 / (end - start));
                    System.out.print(tp);
                    start = end;
                }
            }

        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        while (true) {
            inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        }

    }

    @Test
    public void performanceQuery3() throws InterruptedException, ValidatorException {


        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[70 > price] select symbol,price+20 as incresedProce insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count++;
                if (count % 1000000 == 0) {
                    long end = System.currentTimeMillis();
                    String tp = "," + (1000000 * 1000.0 / (end - start));
                    System.out.print(tp);
                    start = end;
                }
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        while (true) {
            inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        }

    }

    @Test
    public void performanceQuery4() throws InterruptedException, ValidatorException {


        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "@info(name = 'query1') from cseEventStream[70 > price and volume > 90] select symbol,price+20 as incresedProce insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.addExecutionPlan(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback(null, "query1", 2, siddhiManager.getSiddhiContext()) {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count++;
                if (count % 1000000 == 0) {
                    long end = System.currentTimeMillis();
                    String tp = "," + (1000000 * 1000.0 / (end - start));
                    System.out.print(tp);
                    start = end;
                }
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        while (true) {
            inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
            inputHandler.send(new Object[]{"WSO2", 100f, 80});
            inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        }

    }


}
