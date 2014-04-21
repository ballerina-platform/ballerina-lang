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

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class PartitionWindowTestCase {
    static final Logger log = Logger.getLogger(PartitionWindowTestCase.class);

    private int count;
    private long value;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        value = 0;
        eventArrived = false;
    }


    @Test
    public void testPartitionWindowQuery1() throws InterruptedException {
        log.info("Partition Window test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int) ");
        siddhiManager.definePartition("define partition streamPartition by symbol ");

        String queryReference = siddhiManager.addQuery("from cseEventStream#window.time(5000) " +
                                                       "select symbol , sum(price) as sumPrice " +
                                                       "insert into StockQuote for all-events " +
                                                       "partition by streamPartition;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                    count++;
                if(removeEvents!=null&& 0.0==(Double)removeEvents[0].getData1()){
                  value++;
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(7000);
        Assert.assertEquals("6 events are expected", 6, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of 0.0s", 2, value);
        siddhiManager.shutdown();
    }

    @Test
    public void testPartitionWindowQuery2() throws InterruptedException {
        log.info("Partition Window test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int) ");
        siddhiManager.definePartition("define partition streamPartition by cseEventStream.symbol, cseEventStream1.symbol ");

        String queryReference = siddhiManager.addQuery("from cseEventStream#window.time(5000) " +
                                                       "select symbol , sum(price) as sumPrice " +
                                                       "insert into StockQuote for all-events " +
                                                       "partition by streamPartition;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if(count==5){
                    Assert.assertEquals(0.0,removeEvents[0].getData1());
                }
                eventArrived = true;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(7000);
        Assert.assertEquals("6 events are expected", 6, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();
    }
}
