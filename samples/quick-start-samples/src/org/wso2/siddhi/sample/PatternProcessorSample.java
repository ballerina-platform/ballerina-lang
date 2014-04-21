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
 * Sample demonstrating a pattern processing
 */
public class PatternProcessorSample {

    public static void main(String[] args) throws SiddhiParserException, InterruptedException {

        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();

        // Assign the Callback to receive the outputs

        siddhiManager.defineStream("define stream CSEStream ( symbol string, price int )");
        siddhiManager.defineStream("define stream InfoStock ( action string, symbol string )");
        siddhiManager.addQuery("from every e1 = InfoStock[action == 'buy']  " +
                               " -> e2 = CSEStream [price > 100 and symbol == e1.symbol]  " +
                               "-> e3=CSEStream [price > e2.price  and symbol == e1.symbol] " +
                               "select e1.symbol as symbol, e2.price as price1, e3.price as price2 " +
                               "insert into StockQuote;");

        siddhiManager.addCallback("StockQuote", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });
        InputHandler cseStream = siddhiManager.getInputHandler("CSEStream");
        InputHandler infoStock = siddhiManager.getInputHandler("InfoStock");
        cseStream.send(new Object[]{"IBM", 200});
        Thread.sleep(500);
        cseStream.send(new Object[]{"WSO2", 101});
        Thread.sleep(500);
        infoStock.send(new Object[]{"buy", "WSO2"});
        Thread.sleep(500);
        infoStock.send(new Object[]{"buy", "IBM"});
        Thread.sleep(500);
        cseStream.send(new Object[]{"IBM", 201});
        Thread.sleep(500);
        cseStream.send(new Object[]{"WSO2", 97});
        Thread.sleep(500);
        cseStream.send(new Object[]{"IBM", 205});
        Thread.sleep(500);
        cseStream.send(new Object[]{"WSO2", 101});
        Thread.sleep(500);
        cseStream.send(new Object[]{"IBM", 200});
        Thread.sleep(500);
        cseStream.send(new Object[]{"WSO2", 110});
        Thread.sleep(500);

        siddhiManager.shutdown();


    }
}
