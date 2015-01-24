/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.sample;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class PartitionSample {

    public static void main(String[] args) throws InterruptedException {

        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();


        String executionPlan = "@config(async = 'true')define stream cseEventStream (symbol string, price float,volume int);"
                + "@config(async = 'true')define stream cseEventStream1 (symbol string, price float,volume int);"
                + "partition with (symbol of cseEventStream)"
                + "begin"
                + "@info(name = 'query') from cseEventStream select symbol,sum(price) as price,volume insert into OutStockStream ;"
                + "end ";

        ExecutionPlanRuntime executionRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionRuntime.addCallback("OutStockStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionRuntime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75f, 100});
        inputHandler.send(new Object[]{"WSO2", 705f, 100});
        inputHandler.send(new Object[]{"IBM", 35f, 100});
        inputHandler.send(new Object[]{"ORACLE", 50.0f, 100});
        Thread.sleep(1000);

    }
}
