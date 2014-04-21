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

public class LengthBatchWindowClusteredTestCase {

    static final Logger log = Logger.getLogger(LengthBatchWindowClusteredTestCase.class);

    private int count;
    private int totalCount;
    private boolean eventArrived;


    @Before
    public void init() {
        count = 0;
        totalCount = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("WindowClustered test1");

        String siddhiPlanId = "siddhi1-cluster-" + System.currentTimeMillis();

        //Node 1
        SiddhiManager siddhiManager1 = createInstanceQuery1("node1", siddhiPlanId);
        InputHandler inputHandler1 = siddhiManager1.getInputHandler("cseEventStream");

        //Node 2
        SiddhiManager siddhiManager2 = createInstanceQuery1("node2", siddhiPlanId);
        InputHandler inputHandler2 = siddhiManager2.getInputHandler("cseEventStream");

        try {
            for (int i = 0; i < 2; i++) {
                inputHandler1.send(new Object[]{"Handler1", 50.0f});
                inputHandler2.send(new Object[]{"Handler2", 150.0f});
                inputHandler2.send(new Object[]{"Handler2", 200.0f});
                inputHandler1.send(new Object[]{"Handler1", 250.0f});
                Thread.sleep(1000);
            }
        } finally {
            siddhiManager1.shutdown();
            siddhiManager2.shutdown();
        }


        Assert.assertEquals("In Events has to be 1 higher than Remove Events", 1, count);
        Assert.assertEquals("Total Events", 3, totalCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }

    private SiddhiManager createInstanceQuery1(String instanceId, String planId) {

        SiddhiManager siddhiManager = new SiddhiManager(new SiddhiConfiguration().setInstanceIdentifier(instanceId).setQueryPlanIdentifier(planId).setDistributedProcessing(true));

        siddhiManager.defineStream("define stream cseEventStream ( symbol string, price float)");
        String queryReference = siddhiManager.addQuery("from cseEventStream#window.lengthBatch(4) " +
                "select  price,avg(price) as avgPrice " +
                "insert into PotentialFraud for all-events");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                totalCount++;
                if (inEvents != null) {
                    if ((Double) inEvents[inEvents.length - 1].getData()[1] == 162.5) {
                        count++;
                    }

                } else if (removeEvents != null) {
                    Assert.assertEquals(0, removeEvents[removeEvents.length - 1].getData()[1]);
                    count--;
                }
                eventArrived = true;
            }


        });

        return siddhiManager;
    }


}
