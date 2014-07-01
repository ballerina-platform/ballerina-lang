/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.wso2.siddhi.sample;

import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class FrequentWindowSample {

    public static void main(String[] args)
            throws InterruptedException, SiddhiParserException {

        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float )");
        String queryReference = siddhiManager.addQuery("from  cseEventStream#window.frequent(2) " +
                                                       "select symbol, price " +
                                                       "insert into StockQuote;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 65.0f});
        inputHandler.send(new Object[]{"IBM", 65.0f});
        inputHandler.send(new Object[]{"IBM", 65.0f});
        inputHandler.send(new Object[]{"IBM", 65.0f});
        inputHandler.send(new Object[]{"IBM", 20.0f});
        inputHandler.send(new Object[]{"IBM", 20.0f});
        inputHandler.send(new Object[]{"IBM", 20.0f});
        inputHandler.send(new Object[]{"IBM", 20.0f});
        inputHandler.send(new Object[]{"GOOG", 20.0f});
        inputHandler.send(new Object[]{"IBM", 80.0f});
        inputHandler.send(new Object[]{"IBM", 80.0f});
        inputHandler.send(new Object[]{"GOOG", 40.0f});
        inputHandler.send(new Object[]{"GOOG", 40.0f});
        inputHandler.send(new Object[]{"WSO2", 64.0f});
        inputHandler.send(new Object[]{"WSO2", 64.0f});

        siddhiManager.shutdown();
    }
}
