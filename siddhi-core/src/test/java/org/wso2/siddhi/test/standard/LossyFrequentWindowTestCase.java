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
package org.wso2.siddhi.test.standard;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;


public class LossyFrequentWindowTestCase {
    static final Logger log = Logger.getLogger(LossyFrequentWindowTestCase.class);

    private int inEventCount = 0;
    private int removeEventCount = 0;
    private boolean eventArrived;
    SiddhiConfiguration configuration;
    SiddhiManager siddhiManager;

    @Before
    public void initialize() {
        inEventCount = 0;
        eventArrived = false;
        configuration = new SiddhiConfiguration();
        configuration.setAsyncProcessing(false);
        siddhiManager = new SiddhiManager(configuration);
    }

    @Test
    public void testWindowQueryAllAttributes() throws InterruptedException {
        log.info("Frequent Event Count Test Running");
        siddhiManager.defineStream("define stream purchase (cardNo string, price float) ");
        String queryReference = siddhiManager.addQuery("from purchase[price >= 30]#window.lossyFrequent(0.1,0.01)" +
                "select cardNo, price " +
                "insert into PotentialFraud for  all-events ;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
            }

        });
        inEventCount = 0;
        removeEventCount = 0;
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("purchase");

        for (int i = 0; i < 25; i++) {
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 73.36f});
            loginSucceedEvents.send(new Object[]{"1234-3244-2432-123", 46.36f});
            loginSucceedEvents.send(new Object[]{"5768-3244-2432-5646", 48.36f});
            loginSucceedEvents.send(new Object[]{"9853-3244-2432-4125", 78.36f});
        }
        loginSucceedEvents.send(new Object[]{"1124-3244-2432-4126", 78.36f});     // these events will not be picked
        loginSucceedEvents.send(new Object[]{"1124-3244-2432-4126", 78.36f});

        Thread.sleep(1000);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("In Event count", 100, inEventCount);
        Assert.assertEquals("Out Event count", 0, removeEventCount);
        siddhiManager.shutdown();
    }


    @Test
    public void testWindowQueryDeleteTest() throws InterruptedException {
        log.info("Frequent Event Count Test Running");
        siddhiManager.defineStream("define stream purchase (cardNo string, price float) ");
        String queryReference = siddhiManager.addQuery("from purchase[price >= 30]#window.lossyFrequent(0.3,0.05)" +
                "select cardNo, price " +
                "insert into PotentialFraud for  all-events ;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
            }

        });
        inEventCount = 0;
        removeEventCount = 0;
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("purchase");

        loginSucceedEvents.send(new Object[]{"3224-3244-2432-4124", 73.36f});
        for (int i = 0; i < 25; i++) {
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 73.36f});
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 78.36f});
            loginSucceedEvents.send(new Object[]{"1234-3244-2432-123", 86.36f});
            loginSucceedEvents.send(new Object[]{"5768-3244-2432-5646", 48.36f});//this event will not include in to the window during first iteration because 1+0<5*0.25
        }
        Thread.sleep(1000);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Out Event count", 1, removeEventCount);
        siddhiManager.shutdown();
    }

    @Test
    public void testWindowQueryDeleteTestWithAttr() throws InterruptedException {
        log.info("Frequent Event Count Test Running");
        siddhiManager.defineStream("define stream purchase (cardNo string, price float) ");
        String queryReference = siddhiManager.addQuery("from purchase[price >= 30]#window.lossyFrequent(0.3,0.05,cardNo)" +
                "select cardNo, price " +
                "insert into PotentialFraud for  all-events ;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                if (inEvents != null) {
                    inEventCount += inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount += removeEvents.length;
                }
            }

        });
        inEventCount = 0;
        removeEventCount = 0;
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("purchase");

        loginSucceedEvents.send(new Object[]{"3224-3244-2432-4124", 73.36f});
        for (int i = 0; i < 25; i++) {
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 73.36f});
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 78.36f});
            loginSucceedEvents.send(new Object[]{"1234-3244-2432-123", 86.36f});
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 48.36f});//this event will be included because we only consider cardNo
        }
        Thread.sleep(1000);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("In Event count", 101, inEventCount);
        Assert.assertEquals("Out Event count", 1, removeEventCount);
        siddhiManager.shutdown();
    }
}
