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

public class FrequentWindowTestCase {
    static final Logger log = Logger.getLogger(FrequentWindowTestCase.class);

    private int inEventCount = 0;
    private int removeEventCount = 0;
    private boolean eventArrived;
    SiddhiConfiguration configuration;
    SiddhiManager siddhiManager;

    @Before
    public void initialize() {
        eventArrived = false;
        configuration = new SiddhiConfiguration();
        configuration.setAsyncProcessing(false);
        siddhiManager = new SiddhiManager(configuration);
    }

    @Test
    public void testWindowQueryAllAttributes() throws InterruptedException {
        log.info("Frequent Event Count Test Running");
        siddhiManager.defineStream("define stream purchase (cardNo string, price float) ");
        String queryReference = siddhiManager.addQuery("from purchase[price >= 30]#window.frequent(2)" +
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
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("purchase");
        inEventCount = 0;
        removeEventCount = 0;
        for (int i = 0; i < 2; i++) {
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 73.36f});
            loginSucceedEvents.send(new Object[]{"1234-3244-2432-123", 46.36f});
            loginSucceedEvents.send(new Object[]{"5768-3244-2432-5646", 48.36f});
            loginSucceedEvents.send(new Object[]{"9853-3244-2432-4125", 78.36f});
        }
        Thread.sleep(1000);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("In Event count", 8, inEventCount);
        Assert.assertEquals("Out Event count", 6, removeEventCount);
        siddhiManager.shutdown();


    }


    @Test
    public void testWindowQueryDefinedAttributes() throws InterruptedException {
        log.info("Frequent Event Count Test Running");
        siddhiManager.defineStream("define stream purchase (cardNo string, price float) ");
        String queryReference = siddhiManager.addQuery("from purchase[price >= 30]#window.frequent(2,cardNo)" +
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

        for (int i = 0; i < 2; i++) {
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 73.36f});
            loginSucceedEvents.send(new Object[]{"1234-3244-2432-123", 46.36f});
            loginSucceedEvents.send(new Object[]{"3234-3244-2432-4124", 78.36f});
            loginSucceedEvents.send(new Object[]{"1234-3244-2432-123", 86.36f});
            loginSucceedEvents.send(new Object[]{"5768-3244-2432-5646", 48.36f});
        }
        Thread.sleep(1000);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("In Event count", 8, inEventCount);
        Assert.assertEquals("Out Event count", 0, removeEventCount);
        siddhiManager.shutdown();
    }


}
