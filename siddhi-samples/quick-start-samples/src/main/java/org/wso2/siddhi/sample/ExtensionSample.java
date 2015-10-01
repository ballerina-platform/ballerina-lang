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
 */

package org.wso2.siddhi.sample;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.sample.util.CustomFunctionExtension;

public class ExtensionSample {

    public static void main(String[] args) throws InterruptedException {

        // Creating Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("custom:plus", CustomFunctionExtension.class);


        String executionPlan = "" +
                "define stream cseEventStream (symbol string, price long, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream " +
                "select symbol , custom:plus(price,volume) as totalCount " +
                "insert into Output;";

        //Generating runtime
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        //Adding callback to retrieve output events from query
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });

        //Retrieving InputHandler to push events into Siddhi
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");

        //Starting event processing
        executionPlanRuntime.start();

        //Sending events to Siddhi
        inputHandler.send(new Object[]{"IBM", 700l, 100l});
        inputHandler.send(new Object[]{"WSO2", 605l, 200l});
        inputHandler.send(new Object[]{"GOOG", 60l, 200l});
        Thread.sleep(500);

        //Shutting down the runtime
        executionPlanRuntime.shutdown();

        //Shutting down Siddhi
        siddhiManager.shutdown();

    }
}
