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

package org.ballerinalang.siddhi.core.query.pattern.absent;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.TestUtil;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Testcase for absent pattern queries.
 * Test the patterns:
 * - A -> not B for 1 sec
 * - not A for 1 sec -> B
 */
public class AbsentPatternTestCase {

    private static final Logger log = LoggerFactory.getLogger(AbsentPatternTestCase.class);

    @Test
    public void testQueryAbsent1() throws InterruptedException {
        log.info("Test the query e1 -> not e2 without sending e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] -> not Stream2[price>e1.price] for 1 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent1"})
    public void testQueryAbsent2() throws InterruptedException {
        log.info("Test the query e1 -> not e2 sending e2 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] -> not Stream2[price>e1.price] for 1 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent2"})
    public void testQueryAbsent3() throws InterruptedException {
        log.info("Test the query e1 -> not e2 sending e2 for 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] -> not Stream2[price>e1.price] for 1 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(1000);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent3"})
    public void testQueryAbsent4() throws InterruptedException {
        log.info("Test the query e1 -> not e2 sending e2 for 1 sec but without satisfying the filter condition");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20] -> not Stream2[price>e1.price] for 1 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 50.7f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent4"})
    public void testQueryAbsent5() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 without e1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>20] for 1 sec -> e2=Stream2[price>30] " +
                "select e2.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent5"})
    public void testQueryAbsent6() throws InterruptedException {
        log.info("Test the query not e1 -> e2 with e1 and e2 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>20] for 1 sec -> e2=Stream2[price>30] " +
                "select e2.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(2100);
        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent6"})
    public void testQueryAbsent7() throws InterruptedException {
        log.info("Test the query not e1 -> e2 with e1 and e2 for 1 sec where e1 filter fails");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>20] for 1 sec -> e2=Stream2[price>30] " +
                "select e2.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 5.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent7"})
    public void testQueryAbsent8() throws InterruptedException {
        log.info("Test the query not e1 -> e2 with e1 and e2 for 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>20] for 1 sec -> e2=Stream2[price>30] " +
                "select e2.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent8"})
    public void testQueryAbsent9() throws InterruptedException {
        log.info("Test the query e1 -> e2 -> not e3 with e1, e2 and e3 for 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] -> not Stream3[price>30] for 1 sec " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent9"})
    public void testQueryAbsent10() throws InterruptedException {
        log.info("Test the query e1 -> e2 -> not e3 with e1, e2 and e3 which does not meet the condition for 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] -> not Stream3[price>30] for 1 sec " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "IBM"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 25.7f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent10"})
    public void testQueryAbsent11() throws InterruptedException {
        log.info("Test the query e1 -> e2 -> not e3 with e1, e2 and not e3 for 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] -> not Stream3[price>30] for 1 sec " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "IBM"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }


    @Test(dependsOnMethods = {"testQueryAbsent11"})
    public void testQueryAbsent12() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 with e1 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent12"})
    public void testQueryAbsent13() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 with e1, e2(condition failed) e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 8.7f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent13"})
    public void testQueryAbsent14() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 with e1, e2 e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent14"})
    public void testQueryAbsent15() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 -> e3 with e1, e2 e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent15"})
    public void testQueryAbsent16() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 -> e3 with e2 and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "GOOGLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent16"})
    public void testQueryAbsent17() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 -> e3 with e1 that fails to satisfy the condition, e2 and " +
                "e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        Thread.sleep(500);
        stream1.send(new Object[]{"WSO2", 5.6f, 100});
        Thread.sleep(600);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent17"})
    public void testQueryAbsent18() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 -> e3 with e1, e2 after 1 sec and e3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] -> e3=Stream3[price>30] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 25.6f, 100});
        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent18"})
    public void testQueryAbsent19() throws InterruptedException {
        log.info("Test the query e1 -> e2 -> e3 -> not e4 for 1 sec with e1, e2, e3 and not e4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] -> e3=Stream3[price>30] -> not Stream4[price>40] " +
                "for 1 sec  " +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "IBM", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.7f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent19"})
    public void testQueryAbsent20() throws InterruptedException {
        log.info("Test the query e1 -> e2 -> e3 -> not e4 for 1 sec with e1, e2, e3 and e4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] -> e3=Stream3[price>30] -> not Stream4[price>40] " +
                "for 1 sec  " +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 35.7f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"ORACLE", 44.7f, 100});
        Thread.sleep(1100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent20"})
    public void testQueryAbsent21() throws InterruptedException {
        log.info("Test the query e1 -> e2 -> not e3 for 1 sec -> e4 with e1, e2, and e4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] -> not Stream3[price>30] for 1 sec -> " +
                "e4=Stream4[price>40] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e4.symbol as symbol4 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "IBM", "ORACLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(1100);
        stream4.send(new Object[]{"ORACLE", 44.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent21"})
    public void testQueryAbsent22() throws InterruptedException {
        log.info("Test the query e1 -> e2 -> not e3 for 1 sec -> e4 with e1, e2, e3, and e4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> e2=Stream2[price>20] -> not Stream3[price>30] for 1 sec -> " +
                "e4=Stream4[price>40] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e4.symbol as symbol4 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 38.7f, 100});
        Thread.sleep(1100);
        stream4.send(new Object[]{"ORACLE", 44.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent22"})
    public void testQueryAbsent23() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 -> e3-> e4 with e1, e2, e3, and e4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] -> e3=Stream3[price>30] -> " +
                "e4=Stream4[price>40] " +
                "select e2.symbol as symbol2, e3.symbol as symbol3, e4.symbol as symbol4 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 38.7f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"ORACLE", 44.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent23"})
    public void testQueryAbsent24() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 -> not e3 for 1 sec-> e4 with e2 after 1 sec e4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] -> not Stream3[price>30] for 1 " +
                "sec -> e4=Stream4[price>40] " +
                "select e2.symbol as symbol2, e4.symbol as symbol4 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "ORACLE"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(1100);
        stream4.send(new Object[]{"ORACLE", 44.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent24"})
    public void testQueryAbsent25() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 -> not e3 for 1 sec-> e4 with e1, e2, e3 and e4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] -> not Stream3[price>30] for 1 " +
                "sec -> e4=Stream4[price>40] " +
                "select e2.symbol as symbol2, e4.symbol as symbol4 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 38.7f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"ORACLE", 44.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent25"})
    public void testQueryAbsent26() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 -> not e3 for 1 sec-> e4 with e2, e3 and e4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] -> not Stream3[price>30] for 1 " +
                "sec -> e4=Stream4[price>40] " +
                "select e2.symbol as symbol2, e4.symbol as symbol4 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");

        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"GOOGLE", 38.7f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"ORACLE", 44.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent26"})
    public void testQueryAbsent27() throws InterruptedException {
        log.info("Test the query not e1 -> e2 without e1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>20] for 1 sec -> e2=Stream2[price>30] " +
                "select e2.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent27"})
    public void testQueryAbsent28() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 and e4 without e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e2=Stream3[price>30] and " +
                "e3=Stream4[price>40]" +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "WSO2", "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 18.7f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"GOOGLE", 56.86f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent28"})
    public void testQueryAbsent29() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 and e4 without e2 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e2=Stream3[price>30] and " +
                "e3=Stream4[price>40]" +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 18.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"GOOGLE", 56.86f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent29"})
    public void testQueryAbsent30() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 or e4 without e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e2=Stream3[price>30] or " +
                "e3=Stream4[price>40]" +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                "WSO2", null});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 18.7f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent30"})
    public void testQueryAbsent31() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 or e4 without e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e2=Stream3[price>30] or " +
                "e3=Stream4[price>40]" +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM",
                null, "GOOGLE"});

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 18.7f, 100});
        Thread.sleep(1100);
        stream4.send(new Object[]{"GOOGLE", 56.86f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent31"})
    public void testQueryAbsent32() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 or e4 without e2 within 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e2=Stream3[price>30] or " +
                "e3=Stream4[price>40]" +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 18.7f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"GOOGLE", 56.86f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent32"})
    public void testQueryAbsent33() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 and e4 with e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e2=Stream3[price>30] and " +
                "e3=Stream4[price>40]" +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 18.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"ORACLE", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"GOOGLE", 56.86f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent33"})
    public void testQueryAbsent34() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 or e4 with e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e2=Stream3[price>30] or " +
                "e3=Stream4[price>40]" +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 18.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"ORACLE", 25.0f, 100});
        Thread.sleep(100);
        stream3.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);
        stream4.send(new Object[]{"GOOGLE", 56.86f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent34"})
    public void testQueryAbsent35() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2<2:5> with e1 and e2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20]<2:5> " +
                "select e2[0].symbol as symbol0, e2[1].symbol as symbol1, e2[2].symbol as symbol2, e2[3].symbol as " +
                "symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOGLE", 35.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"ORACLE", 45.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent35"})
    public void testQueryAbsent36() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2<2:5> with e2 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20]<2:5> " +
                "select e2[0].symbol as symbol0, e2[1].symbol as symbol1, e2[2].symbol as symbol2, e2[3].symbol as " +
                "symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2",
                "IBM", null, null});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");


        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream2.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent36"})
    public void testQueryAbsent37() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 with e2 only");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>10] for 1 sec -> e2=Stream2[price>20] " +
                "select e2.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"WSO2"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");


        siddhiAppRuntime.start();

        Thread.sleep(2100);
        stream2.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 45.0f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent37"})
    public void testQueryAbsent38() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 with e1, e2 and e3 after 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e3=Stream3[price>30] " +
                "select e1.symbol as symbol1, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream3 = siddhiAppRuntime.getInputHandler("Stream3");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 15.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 28.7f, 100});
        Thread.sleep(1100);
        stream3.send(new Object[]{"GOOGLE", 55.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent38"})
    public void testQueryAbsent39() throws InterruptedException {
        log.info("Test the query e1 -> not e2 for 1 sec -> e3 or e4 with e2 followed by 1 sec delay");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); " +
                "define stream Stream3 (symbol string, price float, volume int); " +
                "define stream Stream4 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>10] -> not Stream2[price>20] for 1 sec -> e2=Stream3[price>30] or " +
                "e3=Stream4[price>40]" +
                "select e1.symbol as symbol1, e2.symbol as symbol2, e3.symbol as symbol3 " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");
        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");
        InputHandler stream4 = siddhiAppRuntime.getInputHandler("Stream4");


        siddhiAppRuntime.start();

        stream1.send(new Object[]{"IBM", 18.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 25.5f, 100});
        Thread.sleep(1100);
        stream4.send(new Object[]{"GOOGLE", 56.86f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent39"})
    public void testQueryAbsent40() throws InterruptedException {
        log.info("Test the query not e1 -> e2 without e1 with two e2s");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>20] for 1 sec -> e2=Stream2[price>30] " +
                "select e2.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1", new Object[]{"IBM"});

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        Thread.sleep(1100);
        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);

        Thread.sleep(1100);
        stream2.send(new Object[]{"WSO2", 68.7f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent40"})
    public void testQueryAbsent41() throws InterruptedException {
        log.info("Test the query every not e1 with e1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every not Stream1[price>20] for 1 sec " +
                "select * " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream1 = siddhiAppRuntime.getInputHandler("Stream1");

        siddhiAppRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
        Thread.sleep(3000);
    }

    @Test(dependsOnMethods = {"testQueryAbsent41"})
    public void testQueryAbsent42() throws InterruptedException {
        log.info("Test the query not e1 for 1 sec -> e2 within 2 sec without e1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from not Stream1[price>20] for 1 sec -> e2=Stream2[price>30] within 2 sec " +
                "select e2.symbol as symbol " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addQueryCallback(siddhiAppRuntime, "query1");

        InputHandler stream2 = siddhiAppRuntime.getInputHandler("Stream2");

        siddhiAppRuntime.start();

        Thread.sleep(3100);
        stream2.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);

        AssertJUnit.assertEquals("Number of success events", 0, callback.getInEventCount());
        AssertJUnit.assertEquals("Number of remove events", 0, callback.getRemoveEventCount());
        AssertJUnit.assertFalse("Event not arrived", callback.isEventArrived());

        siddhiAppRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testQueryAbsent42"})
    public void testQueryAbsent43() throws InterruptedException {
        log.info("Test the partitioned query e1 -> not e2 for 1 sec");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream CustomerStream (customerId string); ";
        String query = "" +
                "partition with (customerId of CustomerStream) " +
                "begin " +
                "from e1=CustomerStream -> not CustomerStream[customerId == e1.customerId] for 1 sec " +
                "select e1.customerId " +
                "insert into OutputStream; " +
                "end ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(siddhiAppRuntime, "OutputStream", new
                Object[]{"customerA"});

        InputHandler customerStream = siddhiAppRuntime.getInputHandler("CustomerStream");

        siddhiAppRuntime.start();

        customerStream.send(new Object[]{"customerA"});
        customerStream.send(new Object[]{"customerB"});
        Thread.sleep(500);
        customerStream.send(new Object[]{"customerB"});
        Thread.sleep(600);

        siddhiAppRuntime.shutdown();

        callback.throwAssertionErrors();
        AssertJUnit.assertEquals("Number of success events", 1, callback.getInEventCount());
        AssertJUnit.assertTrue("Event not arrived", callback.isEventArrived());
    }
}
