/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.test.standard.partition;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class PartitionSequenceTestCase {
    static final Logger log = Logger.getLogger(PartitionSequenceTestCase.class);
    int eventCount;

    @Before
    public void init() {
        eventCount = 0;
    }

    @Test
    public void testQuery10() throws InterruptedException, SiddhiParserException {
        log.info("testSequence10  - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.defineStream("define stream twiterStream ( symbol string, count int )");
        siddhiManager.definePartition("define partition streamPartition by cseEventStream.symbol, twiterStream.symbol ");

        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] , e2 = twiterStream [count > 10 ] " +
                                                       "select e1.price as price, e1.symbol as symbol, e2.count as count, e2.symbol as symbol2  " +
                                                       "insert into StockQuote " +
                                                       "partition by streamPartition" +
                                                       ";");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{76.6f, "IBM", 20, "IBM"}, inEvents[0].getData());
                } else if (eventCount == 1) {
                    Assert.assertArrayEquals(new Object[]{51.0f, "GOOG", 20, "GOOG"}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = siddhiManager.getInputHandler("twiterStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        cseStreamHandler.send(new Object[]{"GOOG", 51f, 101});
        cseStreamHandler.send(new Object[]{"IBM", 76.6f, 111});
        Thread.sleep(500);
        twitterStreamHandler.send(new Object[]{"IBM", 20});
        cseStreamHandler.send(new Object[]{"WSO2", 45.6f, 100});
        Thread.sleep(500);
        twitterStreamHandler.send(new Object[]{"GOOG", 20});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 2, eventCount);

    }


    @Test
    public void testQuery12() throws InterruptedException, SiddhiParserException {
        log.info("testSequence12  OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");
        siddhiManager.definePartition("define partition streamPartition by symbol ");

        String queryReference = siddhiManager.addQuery("from e1 = cseEventStream [ price >= 50 and volume > 100 ] , e2 = cseEventStream [price <= 40 ] * , e3 = cseEventStream [volume <= 70 ] " +
                                                       "select e1.symbol as symbol1,e2[0].symbol as symbol2,e3.symbol as symbol3 " +
                                                       "insert into StockQuote " +
                                                       "partition by streamPartition ;");
//                                                       " ;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (eventCount == 0) {
                    Assert.assertArrayEquals(new Object[]{"IBM", "IBM", "IBM"}, inEvents[0].getData());
                } else {
                    Assert.fail();
                }
                eventCount++;
            }
        });
        InputHandler cseStreamHandler = siddhiManager.getInputHandler("cseEventStream");

        cseStreamHandler.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(1200);
        cseStreamHandler.send(new Object[]{"GOOG", 21f, 81});
        cseStreamHandler.send(new Object[]{"IBM", 15.6f, 105});
        cseStreamHandler.send(new Object[]{"IBM", 15.6f, 65});
        cseStreamHandler.send(new Object[]{"WSO2", 176.6f, 65});
        Thread.sleep(500);

        siddhiManager.shutdown();

        Assert.assertEquals("Number of success events", 1, eventCount);

    }

}
