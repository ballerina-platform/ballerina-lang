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

package org.ballerinalang.siddhi.core.stream;

import com.lmax.disruptor.ExceptionHandler;
import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.core.util.SiddhiTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ExceptionHandlerTestCase {

    private static final Logger log = LoggerFactory.getLogger(CallbackTestCase.class);
    private volatile AtomicInteger count;
    private volatile boolean eventArrived;
    private volatile AtomicInteger failedCount;
    private volatile boolean failedCaught;
    private SiddhiManager siddhiManager;

    @BeforeMethod
    public void init() {
        count = new AtomicInteger(0);
        eventArrived = false;
        failedCount = new AtomicInteger(0);
        failedCaught = false;
    }

    private SiddhiAppRuntime createTestExecutionRuntime() {
        siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "@app:name('callbackTest1') " +
                "" +
                "@async(buffer.size='2')" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "@Parallel " +
                "from StockStream[price + 0.0 > 0.0] " +
                "select symbol, price " +
                "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.addAndGet(events.length);
                eventArrived = true;
            }
        });
        return siddhiAppRuntime;
    }

    /**
     * Send 6 test events (2 valid -> 2 invalid -> 2 valid)
     *
     * @param inputHandler input handler
     * @throws Exception
     */
    private void sendTestInvalidEvents(InputHandler inputHandler) throws Exception {
        // Send 2 valid events
        inputHandler.send(new Object[]{"GOOD_0", 700.0f, 100L});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"GOOD_1", 60.5f, 200L});
        Thread.sleep(100);
        try {
            // Send 2 invalid event
            inputHandler.send(new Object[]{"BAD_2", "EBAY", 200L});
            Thread.sleep(100);
            inputHandler.send(new Object[]{"BAD_3", "WSO2", 700f});
            Thread.sleep(100);
        } catch (Exception ex) {
            AssertJUnit.fail("Disruptor exception can't be caught by try-catch");
            throw ex;
        }
        // Send 2 valid events
        inputHandler.send(new Object[]{"GOOD_4", 700.0f, 100L});
        Thread.sleep(100);
        inputHandler.send(new Object[]{"GOOD_5", 60.5f, 200L});
        Thread.sleep(100);
    }

    private void sendTestValidEvents(InputHandler inputHandler) throws Exception {
        inputHandler.send(new Object[]{"IBM", 700.0f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        inputHandler.send(new Object[]{"IBM", 700.0f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
        inputHandler.send(new Object[]{"IBM", 700.0f, 100L});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200L});
    }

    @Test
    public void callbackTestForValidEvents() throws Exception {
        log.info("callback test without exception handler");
        SiddhiAppRuntime siddhiAppRuntime = createTestExecutionRuntime();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.start();

        sendTestValidEvents(inputHandler);

        SiddhiTestHelper.waitForEvents(100, 6, count, 60000);

        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(6, count.get());
        AssertJUnit.assertFalse(failedCaught);
        AssertJUnit.assertEquals(0, failedCount.get());
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void callbackTestForInvalidEventWithExceptionHandler() throws Exception {
        log.info("callback test with exception handler");
        SiddhiAppRuntime siddhiAppRuntime = createTestExecutionRuntime();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");
        siddhiAppRuntime.handleExceptionWith(new ExceptionHandler<Object>() {
            @Override
            public void handleEventException(Throwable throwable, long l, Object o) {
                failedCount.incrementAndGet();
                failedCaught = true;
                log.info(o + ": properly handle event exception for bad event [sequence: " + l + ", failed: " +
                        failedCount + "]", throwable);
            }

            @Override
            public void handleOnStartException(Throwable throwable) {
                failedCount.incrementAndGet();
                failedCaught = true;
            }

            @Override
            public void handleOnShutdownException(Throwable throwable) {
                log.info("Properly handle shutdown exception", throwable);
                failedCount.incrementAndGet();
                failedCaught = true;
            }
        });

        siddhiAppRuntime.start();
        sendTestInvalidEvents(inputHandler);
        SiddhiTestHelper.waitForEvents(100, 2, failedCount, 60000);

        // No following events can be processed correctly
        AssertJUnit.assertTrue("Should properly process all the 4 valid events", eventArrived);
        AssertJUnit.assertEquals("Should properly process all the 4 valid events", 4, count.get());
        AssertJUnit.assertTrue("Exception is properly handled thrown by 2 invalid events", failedCaught);
        AssertJUnit.assertEquals("Exception is properly handled thrown by 2 invalid events", 2, failedCount.get());
        siddhiAppRuntime.shutdown();
    }
}
