/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.markov;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.concurrent.CountDownLatch;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Following scenarios will be tested here.
 * Populating matrix from file.
 * MarkovChain continues training.
 * MarkovChain discontinues training.
 */
public class MarkovChainTestcase {
    private static final Logger logger = Logger.getLogger(MarkovChainTestcase.class);
    protected static SiddhiManager siddhiManager;
    private CountDownLatch countDownLatch;
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testMarkovChainPopulatingMatrixFromFile() throws Exception {
        logger.info("MarkovChain populating matrix from file test case.");

        final int EXPECTED_NO_OF_EVENTS = 11;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (id string, state string);";

        ClassLoader classLoader = getClass().getClassLoader();
        String markovMatrixStorageLocation = classLoader.getResource("markovMatrix.csv").getPath();

        String executionPlan = ("@info(name = 'query1') "
                + "from InputStream#markov:markovChain(id, state, 60 min, 0.2, \'" + markovMatrixStorageLocation
                + "\', false) " + "select id, lastState, state, transitionProbability, notify "
                + "insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inputStream + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                        case 1:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 2:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 3:
                            Assert.assertEquals(0.3, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 4:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 5:
                            Assert.assertEquals(0.6, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 6:
                            Assert.assertEquals(0.6000000000000001, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 7:
                            Assert.assertEquals(0.6, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 8:
                            Assert.assertEquals(0.3, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 9:
                            Assert.assertEquals(0.3, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 10:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 11:
                            Assert.assertEquals(0.3, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{"1", "testState01"});
        inputHandler.send(new Object[]{"2", "testState02"});
        inputHandler.send(new Object[]{"1", "testState03"});
        inputHandler.send(new Object[]{"3", "testState01"});
        inputHandler.send(new Object[]{"3", "testState02"});
        inputHandler.send(new Object[]{"1", "testState01"});
        inputHandler.send(new Object[]{"1", "testState02"});
        inputHandler.send(new Object[]{"2", "testState01"});
        inputHandler.send(new Object[]{"2", "testState03"});
        inputHandler.send(new Object[]{"4", "testState01"});
        inputHandler.send(new Object[]{"4", "testState03"});

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals("Number of success events", 11, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testMarkovChainContinuesTraining() throws Exception {
        logger.info("MarkovChain continues training test case.");

        final int EXPECTED_NO_OF_EVENTS = 6;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (id string, state string);";

        String executionPlan = ("@info(name = 'query1') "
                + "from InputStream#markov:markovChain(id, state, 60 min, 0.2, 5) "
                + "select id, lastState, state, transitionProbability, notify "
                + "insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inputStream + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                        case 1:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(true, event.getData(4));
                            break;
                        case 2:
                            Assert.assertEquals(0.5, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 3:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(true, event.getData(4));
                            break;
                        case 4:
                            Assert.assertEquals(0.3333333333333333, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 5:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 6:
                            Assert.assertEquals(0.5, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{"1", "testState01"});
        inputHandler.send(new Object[]{"2", "testState02"});
        inputHandler.send(new Object[]{"1", "testState03"});
        inputHandler.send(new Object[]{"3", "testState01"});
        inputHandler.send(new Object[]{"3", "testState02"});
        inputHandler.send(new Object[]{"1", "testState01"});
        inputHandler.send(new Object[]{"1", "testState02"});
        inputHandler.send(new Object[]{"2", "testState01"});
        inputHandler.send(new Object[]{"2", "testState03"});
        inputHandler.send(new Object[]{"4", "testState01"});
        inputHandler.send(new Object[]{"4", "testState03"});

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals("Number of success events", 6, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testMarkovChainDiscontinuesTraining() throws Exception {
        logger.info("MarkovChain discontinues training test case");

        final int EXPECTED_NO_OF_EVENTS = 6;
        countDownLatch = new CountDownLatch(EXPECTED_NO_OF_EVENTS);
        siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (id string, state string, train bool);";

        String executionPlan = ("@info(name = 'query1') "
                + "from InputStream#markov:markovChain(id, state, 60 min, 0.2, 5, train) "
                + "select id, lastState, state, transitionProbability, notify "
                + "insert into OutputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inputStream + executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    countDownLatch.countDown();
                    count++;
                    switch (count) {
                        case 1:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(true, event.getData(4));
                            break;
                        case 2:
                            Assert.assertEquals(0.5, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 3:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(true, event.getData(4));
                            break;
                        case 4:
                            Assert.assertEquals(0.3333333333333333, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 5:
                            Assert.assertEquals(0.0, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        case 6:
                            Assert.assertEquals(0.3333333333333333, event.getData(3));
                            Assert.assertEquals(false, event.getData(4));
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("InputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{"1", "testState01", true});
        inputHandler.send(new Object[]{"2", "testState02", true});
        inputHandler.send(new Object[]{"1", "testState03", true});
        inputHandler.send(new Object[]{"3", "testState01", true});
        inputHandler.send(new Object[]{"3", "testState02", true});
        inputHandler.send(new Object[]{"1", "testState01", true});
        inputHandler.send(new Object[]{"1", "testState02", true});
        inputHandler.send(new Object[]{"2", "testState01", true});
        inputHandler.send(new Object[]{"2", "testState03", false});
        inputHandler.send(new Object[]{"4", "testState01", false});
        inputHandler.send(new Object[]{"4", "testState03", false});

        countDownLatch.await(1000, MILLISECONDS);
        Assert.assertEquals("Number of success events", 6, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        executionPlanRuntime.shutdown();
    }

}
