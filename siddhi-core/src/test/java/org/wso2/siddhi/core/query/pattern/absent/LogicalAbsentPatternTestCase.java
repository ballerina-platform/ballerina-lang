/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.pattern.absent;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Test the patterns:
 * - 'A -> not B and C'
 * - 'not A and B -> C'
 * - 'A -> not B for 1 sec and C'
 * - 'A -> not B for 1 sec or C'
 * - 'A -> not B for 1 sec and not C for 1 sec'
 * - 'A -> not B for 1 sec or not C for 1 sec'
 * - 'not A for 1 sec and not B for 1 sec -> C'
 * - 'not A for 1 sec or not B for 1 sec -> C'
 */
public class LogicalAbsentPatternTestCase {

    private static final Logger log = Logger.getLogger(LogicalAbsentPatternTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;
    private List<AssertionError> assertionErrors = new ArrayList<>();

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
        assertionErrors.clear();
    }

    @Test
    public void testQueryAbsent1() throws InterruptedException {
        log.info("Test the query e1 -> not e2 and e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent2() throws InterruptedException {
        log.info("Test the query e1 -> not e2 and e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent3() throws InterruptedException {
        log.info("Test the query not e1 and e2 -> e3 with e2 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] and e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"IBM", "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent4() throws InterruptedException {
        log.info("Test the query not e1 and e2 -> e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] and e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent5() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec and e3 with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent5_1() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec and e3 with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(500);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(600);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent5_2() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec and e3 with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent6() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec and e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent7() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec and e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent8() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2 -> e3 with e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"IBM", "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent8_1() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2 -> e3 with e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"IBM", "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent8_2() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2 -> e3 with e1, e2 after 1 sec and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(600);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent9() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2 -> e3 with e2 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent10() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2 -> e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"IBM", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent11() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec or e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent12() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec or e3 with e1 and e3 with extra 1 sec to make sure no " +
                "duplicates");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent13() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec or e3 with e1 only with 1 sec waiting");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", null});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent14() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec or e3 with e1 only with no waiting");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testQueryAbsent15() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec or e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testQueryAbsent16() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec or e3 with e1 and e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent17() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec or e2 -> e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec or e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream2.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent18() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec or e2 -> e3 with e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec or e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{null, "GOOGLE"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent19() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec or e2 -> e3 with e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec or e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent20() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 and e3) within 1 sec with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] and e3=Stream3[price>30]) within 1 sec " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testQueryAbsent21() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 and e3) within 1 sec with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] and e3=Stream3[price>30]) within 1 sec " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent22() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 and e3) within 1 sec with e1, e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] and e3=Stream3[price>30]) within 1 sec " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent23() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec and e3) within 2 sec with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec and e3=Stream3[price>30]) within 2 sec" +
                " " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent24() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec and e3) within 2 sec with e1 and e3 after 2 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec and e3=Stream3[price>30]) within 2 sec" +
                " " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(2100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent25() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec and not e3 for 1 sec) within 2 sec with e1 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec and not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent26() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec and not e3 for 1 sec) within 2 sec with e1 and e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec and not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 101});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent27() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec and not e3 for 1 sec) within 2 sec with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec and not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"IBM", 35.0f, 102});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent28() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec and not e3 for 1 sec) within 2 sec with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec and not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 101});
        Thread.sleep(100);
        stream3.send(new Object[]{"ORACLE", 35.0f, 102});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent29() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec or not e3 for 1 sec) within 2 sec with e1 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec or not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent30() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec or not e3 for 1 sec) within 2 sec with e1 and e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec or not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 101});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent31() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec or not e3 for 1 sec) within 2 sec with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec or not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"IBM", 35.0f, 102});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent32() throws InterruptedException {
        log.info("Test the query e1 -> (not e2 for 1 sec or not e3 for 1 sec) within 2 sec with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> (not Stream2[price>20] for 1 sec or not Stream3[price>30] for 1 sec) " +
                "within 2 " +
                "sec" +
                " " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 101});
        Thread.sleep(100);
        stream3.send(new Object[]{"ORACLE", 35.0f, 102});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testQueryAbsent33() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec or not e2 for 1 sec) -> e3 with e3 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec) -> e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent34() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec or not e2 for 1 sec) -> e3 with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec) -> e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"IBM", 15.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent35() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec or not e2 for 1 sec) -> e3 with e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec) -> e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent36() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec or not e2 for 1 sec) -> e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec) -> e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"ORACLE", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testQueryAbsent37() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec and not e2 for 1 sec) -> e3 with e3 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec) -> e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent38() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec and not e2 for 1 sec) -> e3 with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec) -> e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"IBM", 15.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent39() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec and not e2 for 1 sec) -> e3 with e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec) -> e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent40() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec and not e2 for 1 sec) -> e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec) -> e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"ORACLE", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent41() throws InterruptedException {
        log.info("Test the query e1 -> e2 or not e3 for 1 sec with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] or not Stream3[price>30] for 1 sec  " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOGLE", 25.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent42() throws InterruptedException {
        log.info("Test the query e1 -> e2 or not e3 for 1 sec with e1 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] or not Stream3[price>30] for 1 sec  " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", null});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent43() throws InterruptedException {
        log.info("Test the query e1 or not e2 for 1 sec -> e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] or not Stream2[price>20] for 1 sec -> e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent44() throws InterruptedException {
        log.info("Test the query e1 or not e2 for 1 sec -> e3 with e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] or not Stream2[price>20] for 1 sec -> e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{null, "GOOGLE"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent45() throws InterruptedException {
        log.info("Test the query e1 or not e2 for 1 sec -> e3 with e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] or not Stream2[price>20] for 1 sec -> e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent46() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or not e2 for 1 sec) -> e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"}, new Object[]{"IBM"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"ORACLE", 15.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(300);
        stream2.send(new Object[]{"MICROSOFT", 45.0f, 100});
        Thread.sleep(800);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent47() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or not e2 for 1 sec) -> e3 with two e3s");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"}, new Object[]{"WSO2"}, new Object[]{"IBM"}, new
                Object[]{"IBM"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1200);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1200);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 4, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent48() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or not e2 for 1 sec) -> e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"}, new Object[]{"WSO2"}, new Object[]{"WSO2"}, new
                Object[]{"WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 4, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent49() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and not e2 for 1 sec) -> e3 with two (<1 sec> e3)s");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"}, new Object[]{"IBM"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent50() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and not e2 for 1 sec) -> e3 with an e3 after 2 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"}, new Object[]{"WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent51() throws InterruptedException {
        log.info("Test the query e1 and not e2 for 1 sec -> e3 with e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e1=Stream1[price>10] and not Stream2[price>20] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"IBM", "GOOGLE"}, new Object[]{"ORACLE", "MICROSOFT"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream1.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});

        Thread.sleep(1100);
        stream1.send(new Object[]{"ORACLE", 45.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"MICROSOFT", 55.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent52() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or e2) -> e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or e2=Stream2[price>20]) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"MICROSOFT", "IBM"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"ORACLE", 15.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(300);
        stream2.send(new Object[]{"MICROSOFT", 45.0f, 100});
        Thread.sleep(800);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent53() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or e2) -> e3 with e3, e3, e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or e2=Stream2[price>20]) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{null, "WSO2"}, new Object[]{null, "IBM"},
                new Object[]{"ORACLE", "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1200);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1200);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"ORACLE", 65.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 75.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 3, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent54() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or e2) -> e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or e2=Stream2[price>20]) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{null, "WSO2"}, new Object[]{null, "WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent55() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and e2) -> e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and e2=Stream2[price>20]) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"MICROSOFT", "GOOGLE"}, new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"ORACLE", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"MICROSOFT", 45.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(2000);
        stream2.send(new Object[]{"WSO2", 45.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 55.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent56() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and e2) -> e3 with e3, e3, e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and e2=Stream2[price>20]) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"ORACLE", "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1200);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1200);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"ORACLE", 65.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 75.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent57() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and e2) -> e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and e2=Stream2[price>20]) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent58() throws InterruptedException {
        log.info("Test the query every (e1 or not e2 for 1 sec) -> e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] or not Stream1[price>10] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"MICROSOFT", "IBM"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"ORACLE", 15.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(300);
        stream2.send(new Object[]{"MICROSOFT", 45.0f, 100});
        Thread.sleep(800);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent59() throws InterruptedException {
        log.info("Test the query every (e1 or not e2 for 1 sec) -> e3 with e3, e3, e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] or not Stream1[price>10] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{null, "WSO2"}, new Object[]{null, "IBM"},
                new Object[]{"ORACLE", "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1200);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1200);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"ORACLE", 65.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 75.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 3, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent60() throws InterruptedException {
        log.info("Test the query every (e1 or not e2 for 1 sec) -> e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] or not Stream1[price>10] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{null, "WSO2"}, new Object[]{null, "WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent61() throws InterruptedException {
        log.info("Test the query every (e1 and not e2 for 1 sec) -> e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] and not Stream1[price>10] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"MICROSOFT", "GOOGLE"}, new Object[]{"WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"ORACLE", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"MICROSOFT", 45.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(2000);
        stream2.send(new Object[]{"WSO2", 45.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 55.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent62() throws InterruptedException {
        log.info("Test the query every (e1 and not e2 for 1 sec) -> e3 with e3, e3, e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] and not Stream1[price>10] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"ORACLE", "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1200);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1200);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"ORACLE", 65.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 75.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent63() throws InterruptedException {
        log.info("Test the query every (e1 and not e2 for 1 sec) -> e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] and not Stream1[price>10] for 1 sec) -> " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent64() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> not e2 and e3 -> e4 with e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> not Stream2[price>20] and e3=Stream3[price>30] -> " +
                "e4=Stream4[price>40]" +
                "select e3.symbol as symbol3, e4.symbol as symbol4 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"GOOGLE", "ORACLE"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"ORACLE", 45.0f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent65() throws InterruptedException {
        log.info("Test the query e1 and not e2 -> e3 with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] and not Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"IBM", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent66() throws InterruptedException {
        log.info("Test the query not e1 and e2 with e2 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>50] and e2=Stream2[price>20] " +
                "select e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1", new Object[]{"IBM"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent67() throws InterruptedException {
        log.info("Test the query not e1 and e2 with e1 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price==50.0f] and e2=Stream1[price==20.0f] " +
                "select e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        addCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 50.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 20.0f, 100});
        Thread.sleep(100);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 0, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertFalse("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQueryAbsent68() throws InterruptedException {
        log.info("Test the partitioned query e1 -> not e2 for 1 sec and not e3 for 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); ";
        String query = " " +
                "partition with (symbol of Stream1) " +
                "begin " +
                "from e1=Stream1[price==10.0f] -> not Stream1[symbol == e1.symbol and price==20.0f] for 1 sec " +
                "     and not Stream1[symbol == e1.symbol and price==20.0f] for 1 sec " +
                "select e1.symbol as symbol " +
                "insert into OutputStream; " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                eventArrived = true;

                for (Event event : events) {
                    inEventCount++;
                    try {
                        Assert.assertArrayEquals(new Object[]{"WSO2"}, event.getData());
                    } catch (AssertionError e) {
                        assertionErrors.add(e);
                    }
                }
            }
        });

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 10.0f, 20});
        stream1.send(new Object[]{"IBM", 10.0f, 21});
        Thread.sleep(500);
        stream1.send(new Object[]{"IBM", 20.0f, 15});
        Thread.sleep(600);

        for (AssertionError e : this.assertionErrors) {
            throw e;
        }
        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertTrue("Event arrived", eventArrived);

        siddhiAppRuntime.shutdown();
    }

    private void addCallback(SiddhiAppRuntime siddhiAppRuntime, String queryName, Object[]... expected) {
        final int noOfExpectedEvents = expected.length;
        siddhiAppRuntime.addCallback(queryName, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    eventArrived = true;

                    for (Event event : inEvents) {
                        inEventCount++;
                        if (noOfExpectedEvents > 0 && inEventCount <= noOfExpectedEvents) {
                            try {
                                Assert.assertArrayEquals(expected[inEventCount - 1], event.getData());
                            } catch (AssertionError e) {
                                assertionErrors.add(e);
                            }
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });
    }
}
