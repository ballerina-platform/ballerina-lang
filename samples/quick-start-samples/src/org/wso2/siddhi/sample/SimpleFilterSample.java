/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.sample;

import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

/**
 * Sample demonstrating a simple filtering use-case
 */
public class SimpleFilterSample {

    public static void main(String[] args) throws InterruptedException, SiddhiParserException {

        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.addQuery("from  cseEventStream [ price >= 50 ] " +
                               "select symbol, price "+
                               "insert into StockQuote ;");

        siddhiManager.addCallback("StockQuote", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"GOOG", 50f, 100});
        inputHandler.send(new Object[]{"IBM", 76.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 45.6f, 100});
        Thread.sleep(500);

        siddhiManager.shutdown();
    }
}
