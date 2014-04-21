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
package org.wso2.siddhi.test.clustered;

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

public class WindowClusteredTestCase {
    static final Logger log = Logger.getLogger(WindowClusteredTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("WindowClustered test1");

        String siddhiPlanId = "siddhi1-cluster-" + System.currentTimeMillis();

        //Node 1
        SiddhiManager siddhiManager1 = createInstanceQuery1("node1");
        InputHandler inputHandler1 = siddhiManager1.getInputHandler("cseEventStream");

        //Node 2
        SiddhiManager siddhiManager2 = createInstanceQuery1("node2");
        InputHandler inputHandler2 = siddhiManager2.getInputHandler("cseEventStream");

        try {

            inputHandler1.send(new Object[]{"WSO2", 25.6f, 100});
            inputHandler2.send(new Object[]{"IBM", 75.6f, 100});
            inputHandler2.send(new Object[]{"GOOG", 175.6f, 100});
            Thread.sleep(500);
            inputHandler1.send(new Object[]{"EBAY", 57.6f, 100});
            Thread.sleep(6000);
        } finally {
            siddhiManager1.shutdown();
            siddhiManager2.shutdown();
        }


        Assert.assertEquals("In and Remove events has to be equal", 0, count);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }

    private SiddhiManager createInstanceQuery1(String instanceId) {

        SiddhiManager siddhiManager = new SiddhiManager(new SiddhiConfiguration().setInstanceIdentifier(instanceId).setDistributedProcessing(true));

        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");

        String queryReference = siddhiManager.addQuery("from cseEventStream#window.time(5000) " +
                "select symbol, price, avg(price) as avgPrice, sum(volume) as totalVolume " +
                "insert into StockQuote for all-events");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    if ("EBAY".equals(inEvents[inEvents.length - 1].getData(0))) {
                        Assert.assertEquals(42, Math.round((Double) inEvents[0].getData(2)));
                    }
                    if ((Long) inEvents[inEvents.length - 1].getData(3) == 200) {
                        count++;
                    }
                } else {
                    if ("EBAY".equals(removeEvents[removeEvents.length - 1].getData(0)) || "GOOG".equals(removeEvents[removeEvents.length - 1].getData(0))) {
                        Assert.assertEquals(0, removeEvents[0].getData(2));
                    }
                    if ((Long) removeEvents[removeEvents.length - 1].getData(3) == 0) {
                        count--;
                    }
                }
                eventArrived = true;
            }
        });
        return siddhiManager;
    }


    @Test
    public void testQuery2() throws InterruptedException {
        log.info("WindowClustered test2");

        String siddhiPlanId = "siddhi2-cluster-" + System.currentTimeMillis();

        //Node 1
        SiddhiManager siddhiManager1 = createInstanceQuery2("node1", siddhiPlanId);
        InputHandler inputHandler1 = siddhiManager1.getInputHandler("cseEventStream");

        //Node 2
        SiddhiManager siddhiManager2 = createInstanceQuery2("node2", siddhiPlanId);
        InputHandler inputHandler2 = siddhiManager2.getInputHandler("cseEventStream");

        try {

            inputHandler1.send(new Object[]{"WSO2", 25.6f, 100});
            inputHandler2.send(new Object[]{"IBM", 75.6f, 100});
            inputHandler2.send(new Object[]{"GOOG", 175.6f, 100});
            Thread.sleep(500);
            inputHandler1.send(new Object[]{"EBAY", 57.6f, 100});
            Thread.sleep(7000);

        } finally {
            siddhiManager1.shutdown();
            siddhiManager2.shutdown();
        }


        Assert.assertEquals("In and Remove events has to be equal", 0, count);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }

    private SiddhiManager createInstanceQuery2(String instanceId, String queryPlanId) {

        SiddhiManager siddhiManager = new SiddhiManager(new SiddhiConfiguration().setInstanceIdentifier(instanceId).setQueryPlanIdentifier(queryPlanId).setDistributedProcessing(true));

        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float, volume int )");

        String queryReference = siddhiManager.addQuery("from cseEventStream#window.time(5000) " +
                "select symbol, price, avg(price) as avgPrice, sum(volume) as totalVolume  " +
                "insert into StockQuote for  all-events  ");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    if ("EBAY".equals(inEvents[0].getData(0))) {
                        Assert.assertEquals(84, Math.round((Double) inEvents[0].getData(2)));
                    }
                    if ((Long) inEvents[inEvents.length - 1].getData(3) == 400) {
                        count++;
                    }
                } else {
                    if ("EBAY".equals(removeEvents[0].getData(0))) {
                        Assert.assertEquals(0, removeEvents[0].getData(2));
                    }
                    if ((Long) removeEvents[removeEvents.length - 1].getData(3) == 0) {
                        count--;
                    }
                }
                eventArrived = true;
            }
        });
        return siddhiManager;
    }

}
