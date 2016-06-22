package org.wso2.siddhi.extension.reorder;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

/**
 * This is the test case for KSlackExtension.
 * Created by miyurud on 8/10/15.
 */
public class KSlackExtensionTestCase {
    static final Logger log = Logger.getLogger(KSlackExtensionTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void OrderTest() throws InterruptedException {
        log.info("KSlackExtensionTestCase TestCase 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (eventtt long, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream#reorder:kslack(eventtt) select eventtt, price, volume " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(org.wso2.siddhi.core.event.Event[] events) {
                for (org.wso2.siddhi.core.event.Event event : events) {
                    count++;

                    if (count == 1) {
                        Assert.assertEquals(1l, event.getData()[0]);
                    }

                    if (count == 2) {
                        Assert.assertEquals(4l, event.getData()[0]);
                    }

                    if (count == 3) {
                        Assert.assertEquals(3l, event.getData()[0]);
                    }

                    if (count == 4) {
                        Assert.assertEquals(5l, event.getData()[0]);
                    }

                    if (count == 5) {
                        Assert.assertEquals(6l, event.getData()[0]);
                    }

                    if (count == 6) {
                        Assert.assertEquals(7l, event.getData()[0]);
                    }

                    if (count == 7) {
                        Assert.assertEquals(8l, event.getData()[0]);
                    }

                    if (count == 8) {
                        Assert.assertEquals(9l, event.getData()[0]);
                    }

                    if (count == 9) {
                        Assert.assertEquals(10l, event.getData()[0]);
                    }

                    if (count == 10) {
                        Assert.assertEquals(13l, event.getData()[0]);
                    }
                }
            }
        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        //The following implements the out-of-order disorder handling scenario described in the
        //http://dl.acm.org/citation.cfm?doid=2675743.2771828
        inputHandler.send(new Object[]{1l, 700f, 100l});
        inputHandler.send(new Object[]{4l, 60.5f, 200l});
        inputHandler.send(new Object[]{3l, 60.5f, 200l});
        inputHandler.send(new Object[]{5l, 700f, 100l});
        inputHandler.send(new Object[]{6l, 60.5f, 200l});
        inputHandler.send(new Object[]{9l, 60.5f, 200l});
        inputHandler.send(new Object[]{7l, 700f, 100l});
        inputHandler.send(new Object[]{8l, 60.5f, 200l});
        inputHandler.send(new Object[]{10l, 60.5f, 200l});
        inputHandler.send(new Object[]{13l, 60.5f, 200l});

        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
        Assert.assertTrue("Event count is at least 9:",  count >= 9);
    }

    @Test
    public void OrderTest2() throws InterruptedException {
        log.info("KSlackExtensionTestCase TestCase 2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (eventtt long, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream#reorder:kslack(eventtt, 1000l) select eventtt, price, volume " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {

            @Override
            public void receive(org.wso2.siddhi.core.event.Event[] events) {
                for (org.wso2.siddhi.core.event.Event event : events) {
                    count++;

                    if (count == 1) {
                        Assert.assertEquals(1l, event.getData()[0]);
                    }

                    if (count == 2) {
                        Assert.assertEquals(4l, event.getData()[0]);
                    }

                    if (count == 3) {
                        Assert.assertEquals(3l, event.getData()[0]);
                    }

                    if (count == 4) {
                        Assert.assertEquals(5l, event.getData()[0]);
                    }

                    if (count == 5) {
                        Assert.assertEquals(6l, event.getData()[0]);
                    }

                    if (count == 6) {
                        Assert.assertEquals(7l, event.getData()[0]);
                    }

                    if (count == 7) {
                        Assert.assertEquals(8l, event.getData()[0]);
                    }

                    if (count == 8) {
                        Assert.assertEquals(9l, event.getData()[0]);
                    }

                    if (count == 9) {
                        Assert.assertEquals(10l, event.getData()[0]);
                    }

                    if (count == 10) {
                        Assert.assertEquals(13l, event.getData()[0]);
                    }
                }
            }
        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        //The following implements the out-of-order disorder handling scenario described in the
        //http://dl.acm.org/citation.cfm?doid=2675743.2771828
        inputHandler.send(new Object[]{1l, 700f, 100l});
        inputHandler.send(new Object[]{4l, 60.5f, 200l});
        inputHandler.send(new Object[]{3l, 60.5f, 200l});
        inputHandler.send(new Object[]{5l, 700f, 100l});
        inputHandler.send(new Object[]{6l, 60.5f, 200l});
        inputHandler.send(new Object[]{9l, 60.5f, 200l});
        inputHandler.send(new Object[]{7l, 700f, 100l});
        inputHandler.send(new Object[]{8l, 60.5f, 200l});
        inputHandler.send(new Object[]{10l, 60.5f, 200l});
        inputHandler.send(new Object[]{13l, 60.5f, 200l});

        Thread.sleep(3500);
        executionPlanRuntime.shutdown();
        Assert.assertEquals("Event count", 10, count);
    }
}