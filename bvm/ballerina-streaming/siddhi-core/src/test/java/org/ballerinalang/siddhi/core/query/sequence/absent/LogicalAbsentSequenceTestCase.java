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

package org.ballerinalang.siddhi.core.query.sequence.absent;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.TestUtil;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Testcase for logical absent sequence queries.
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
public class LogicalAbsentSequenceTestCase {

    private static final Logger log = LoggerFactory.getLogger(LogicalAbsentSequenceTestCase.class);

    @Test
    public void testQueryAbsent1() throws InterruptedException {
        log.info("Test the query e1, not e2 and e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent1"})
    public void testQueryAbsent2() throws InterruptedException {
        log.info("Test the query e1, not e2 and e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent2"})
    public void testQueryAbsent3() throws InterruptedException {
        log.info("Test the query not e1 and e2, e3 with e2 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] and e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");


        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent3"})
    public void testQueryAbsent4() throws InterruptedException {
        log.info("Test the query not e1 and e2, e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] and e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent4"})
    public void testQueryAbsent5() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec and e3 with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent5"})
    public void testQueryAbsent6() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec and e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent6"})
    public void testQueryAbsent7() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec and e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testQueryAbsent7"})
    public void testQueryAbsent8() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2, e3 with e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");


        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent8"})
    public void testQueryAbsent9() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2, e3 with e2 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");


        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent9"})
    public void testQueryAbsent10() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2, e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "GOOGLE"});

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent10"})
    public void testQueryAbsent11() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec or e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent11"})
    public void testQueryAbsent12() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec or e3 with e1 and e3 with extra 1 sec to make sure no " +
                "duplicates");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent12"})
    public void testQueryAbsent13() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec or e3 with e1 only with 1 sec waiting");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                null});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent13"})
    public void testQueryAbsent14() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec or e3 with e1 only with no waiting");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }


    @Test(dependsOnMethods = {"testQueryAbsent14"})
    public void testQueryAbsent15() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec or e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testQueryAbsent15"})
    public void testQueryAbsent16() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec or e3 with e1 and e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec or e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent16"})
    public void testQueryAbsent17() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec or e2, e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec or e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream2.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent17"})
    public void testQueryAbsent18() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec or e2, e3 with e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec or e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{null,
                "GOOGLE"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent18"})
    public void testQueryAbsent19() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec or e2, e3 with e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec or e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent19"})
    public void testQueryAbsent20() throws InterruptedException {
        log.info("Test the query e1, (not e2 and e3) within 1 sec with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] and e3=Stream3[price>30]) within 1 sec " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }


    @Test(dependsOnMethods = {"testQueryAbsent20"})
    public void testQueryAbsent21() throws InterruptedException {
        log.info("Test the query e1, (not e2 and e3) within 1 sec with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] and e3=Stream3[price>30]) within 1 sec " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent21"})
    public void testQueryAbsent22() throws InterruptedException {
        log.info("Test the query e1, (not e2 and e3) within 1 sec with e1, e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] and e3=Stream3[price>30]) within 1 sec " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent22"})
    public void testQueryAbsent23() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec and e3) within 2 sec with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec and e3=Stream3[price>30]) within 2 sec" +
                " " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent23"})
    public void testQueryAbsent24() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec and e3) within 2 sec with e1 and e3 after 2 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec and e3=Stream3[price>30]) within 2 sec" +
                " " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(2100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent24"})
    public void testQueryAbsent25() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec and not e3 for 1 sec) within 2 sec with e1 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec and not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent25"})
    public void testQueryAbsent26() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec and not e3 for 1 sec) within 2 sec with e1 and e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec and not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 101});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent26"})
    public void testQueryAbsent27() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec and not e3 for 1 sec) within 2 sec with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec and not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"IBM", 35.0f, 102});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent27"})
    public void testQueryAbsent28() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec and not e3 for 1 sec) within 2 sec with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec and not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent28"})
    public void testQueryAbsent29() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec or not e3 for 1 sec) within 2 sec with e1 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec or not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent29"})
    public void testQueryAbsent30() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec or not e3 for 1 sec) within 2 sec with e1 and e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec or not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 25.0f, 101});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent30"})
    public void testQueryAbsent31() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec or not e3 for 1 sec) within 2 sec with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec or not Stream3[price>30] for 1 sec) " +
                "within 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"IBM", 35.0f, 102});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent31"})
    public void testQueryAbsent32() throws InterruptedException {
        log.info("Test the query e1, (not e2 for 1 sec or not e3 for 1 sec) within 2 sec with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], (not Stream2[price>20] for 1 sec or not Stream3[price>30] for 1 sec) " +
                "within 2 sec" +
                " " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }


    @Test(dependsOnMethods = {"testQueryAbsent32"})
    public void testQueryAbsent33() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec or not e2 for 1 sec), e3 with e3 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec), e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent33"})
    public void testQueryAbsent34() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec or not e2 for 1 sec), e3 with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec), e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"IBM", 15.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent34"})
    public void testQueryAbsent35() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec or not e2 for 1 sec), e3 with e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec), e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent35"})
    public void testQueryAbsent36() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec or not e2 for 1 sec), e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec), e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }


    @Test(dependsOnMethods = {"testQueryAbsent36"})
    public void testQueryAbsent37() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec and not e2 for 1 sec), e3 with e3 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec), e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent37"})
    public void testQueryAbsent38() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec and not e2 for 1 sec), e3 with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec), e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"IBM", 15.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent38"})
    public void testQueryAbsent39() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec and not e2 for 1 sec), e3 with e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec), e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(600);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent39"})
    public void testQueryAbsent40() throws InterruptedException {
        log.info("Test the query (not e1 for 1 sec and not e2 for 1 sec), e3 with e1, e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec), e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent40"})
    public void testQueryAbsent41() throws InterruptedException {
        log.info("Test the query e1, e2 or not e3 for 1 sec with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], e2=Stream2[price>20] or not Stream3[price>30] for 1 sec  " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOGLE", 25.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent41"})
    public void testQueryAbsent42() throws InterruptedException {
        log.info("Test the query e1, e2 or not e3 for 1 sec with e1 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], e2=Stream2[price>20] or not Stream3[price>30] for 1 sec  " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                null});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent42"})
    public void testQueryAbsent43() throws InterruptedException {
        log.info("Test the query e1 or not e2 for 1 sec, e3 with e1 and e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] or not Stream2[price>20] for 1 sec, e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent43"})
    public void testQueryAbsent44() throws InterruptedException {
        log.info("Test the query e1 or not e2 for 1 sec, e3 with e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] or not Stream2[price>20] for 1 sec, e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{null,
                "GOOGLE"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent44"})
    public void testQueryAbsent45() throws InterruptedException {
        log.info("Test the query e1 or not e2 for 1 sec, e3 with e3 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] or not Stream2[price>20] for 1 sec, e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        siddhiAppRuntime.start();

        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent45"})
    public void testQueryAbsent46() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or not e2 for 1 sec), e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"},
                new Object[]{"IBM"});

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 2, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent46"})
    public void testQueryAbsent47() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or not e2 for 1 sec), e3 with two e3s");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"},
                new Object[]{"IBM"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(1200);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1200);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 2, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent47"})
    public void testQueryAbsent48() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or not e2 for 1 sec), e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or not Stream2[price>20] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent48"})
    public void testQueryAbsent49() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and not e2 for 1 sec), e3 with two (<1 sec> e3)s");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"},
                new Object[]{"IBM"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 2, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent49"})
    public void testQueryAbsent50() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and not e2 for 1 sec), e3 with an e3 after 2 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and not Stream2[price>20] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e3.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent50"})
    public void testQueryAbsent51() throws InterruptedException {
        log.info("Test the query e1 and not e2 for 1 sec, e3 with e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e1=Stream1[price>10] and not Stream2[price>20] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "GOOGLE"}, new Object[]{"ORACLE", "MICROSOFT"});

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 2, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent51"})
    public void testQueryAbsent52() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or e2), e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or e2=Stream2[price>20]), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new
                Object[]{"MICROSOFT", "IBM"});

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent52"})
    public void testQueryAbsent53() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or e2), e3 with e3, e3, e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or e2=Stream2[price>20]), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{null,
                        "WSO2"}, new Object[]{null, "IBM"},
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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 3, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent53"})
    public void testQueryAbsent54() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec or e2), e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec or e2=Stream2[price>20]), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{null,
                "WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent54"})
    public void testQueryAbsent55() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and e2), e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and e2=Stream2[price>20]), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent55"})
    public void testQueryAbsent56() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and e2), e3 with e3, e3, e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and e2=Stream2[price>20]), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"ORACLE",
                "GOOGLE"});

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent56"})
    public void testQueryAbsent57() throws InterruptedException {
        log.info("Test the query every (not e1 for 1 sec and e2), e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (not Stream1[price>10] for 1 sec and e2=Stream2[price>20]), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent57"})
    public void testQueryAbsent58() throws InterruptedException {
        log.info("Test the query every (e1 or not e2 for 1 sec), e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] or not Stream1[price>10] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new
                Object[]{"MICROSOFT", "IBM"});

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent58"})
    public void testQueryAbsent59() throws InterruptedException {
        log.info("Test the query every (e1 or not e2 for 1 sec), e3 with e3, e3, e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] or not Stream1[price>10] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{null,
                        "WSO2"}, new Object[]{null, "IBM"},
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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 3, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent59"})
    public void testQueryAbsent60() throws InterruptedException {
        log.info("Test the query every (e1 or not e2 for 1 sec), e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] or not Stream1[price>10] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{null,
                "WSO2"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent60"})
    public void testQueryAbsent61() throws InterruptedException {
        log.info("Test the query every (e1 and not e2 for 1 sec), e3 with e1, e3 and e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] and not Stream1[price>10] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent61"})
    public void testQueryAbsent62() throws InterruptedException {
        log.info("Test the query every (e1 and not e2 for 1 sec), e3 with e3, e3, e2, e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] and not Stream1[price>10] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"ORACLE",
                "GOOGLE"});

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


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent62"})
    public void testQueryAbsent63() throws InterruptedException {
        log.info("Test the query every (e1 and not e2 for 1 sec), e3 with only e3 after 2 seconds");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every (e2=Stream2[price>20] and not Stream1[price>10] for 1 sec), " +
                "e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);


        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent63"})
    public void testQueryAbsent64() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec, not e2 and e3, e4 with e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec, not Stream2[price>20] and e3=Stream3[price>30], " +
                "e4=Stream4[price>40]" +
                "select e3.symbol as symbol3, e4.symbol as symbol4 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"GOOGLE",
                "ORACLE"});

        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");


        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"ORACLE", 45.0f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent64"})
    public void testQueryAbsent65() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec and e3 with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(500);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(600);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent65"})
    public void testQueryAbsent66() throws InterruptedException {
        log.info("Test the query e1, not e2 for 1 sec and e3 with e1 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10], not Stream2[price>20] for 1 sec and e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    @Test(dependsOnMethods = {"testQueryAbsent66"})
    public void testQueryAbsent67() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2, e3 with e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");


        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 25.0f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent67"})
    public void testQueryAbsent68() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec and e2, e3 with e1, e2 after 1 sec and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec and e2=Stream2[price>20], e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

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

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }
}
