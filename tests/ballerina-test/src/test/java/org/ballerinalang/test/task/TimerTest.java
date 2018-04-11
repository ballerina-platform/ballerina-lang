/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.ballerinalang.test.task;

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Tests for Ballerina timer tasks.
 */
public class TimerTest {
    private static final Log log = LogFactory.getLog(TimerTest.class);

    @BeforeClass
    public void setup() {
        System.setProperty("java.util.logging.config.file",
                ClassLoader.getSystemResource("logging.properties").getPath());
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
    }

    @Test(description = "Tests running a timer and stopping it")
    public void testSimpleExecution() {
        CompileResult timerCompileResult = BCompileUtil.compileAndSetup("test-src/task/timer-simple.bal");

        printDiagnostics(timerCompileResult);

        int initialDelay = 500;
        int interval = 1000;
        BRunUtil.invokeStateful(timerCompileResult, "scheduleTimer",
                new BValue[]{new BInteger(initialDelay), new BInteger(interval)});

        await().atMost(30, SECONDS).until(() -> {
            BValue[] counts = BRunUtil.invokeStateful(timerCompileResult, "getCount");
            return ((BInteger) counts[0]).intValue() >= 5;
        });

        // Now let's try stopping the task
        BRunUtil.invokeStateful(timerCompileResult, "stopTask");

        // One more check to see whether the task really stopped
        BValue[] counts = BRunUtil.invokeStateful(timerCompileResult, "getCount");
        assertEquals(((BInteger) counts[0]).intValue(), -1, "Count hasn't been reset");
    }

    @Test(description = "Tests running a timer where the onTrigger function generates an error")
    public void testExecutionWithErrorFn() {
        CompileResult timerCompileResult = BCompileUtil.compileAndSetup("test-src/task/timer-error.bal");
        printDiagnostics(timerCompileResult);

        int initialDelay = 500;
        int interval = 1000;
        String errMsg = "Timer error";
        BRunUtil.invokeStateful(timerCompileResult, "scheduleTimerWithError",
                new BValue[]{new BInteger(initialDelay), new BInteger(interval),
                        new BString(errMsg)});

        await().atMost(5, SECONDS).until(() -> {
            BValue[] error = BRunUtil.invokeStateful(timerCompileResult, "getError");
            return error != null && error[0] != null && !error[0].stringValue().isEmpty();
        });

        // Now test whether the onError Ballerina function got called
        BValue[] error = BRunUtil.invokeStateful(timerCompileResult, "getError");
        assertNotNull(error[0], "Expected error not returned.");
        assertEquals(error[0].stringValue(), errMsg, "Expected error message not returned.");

        // Now let's try stopping the task
        BRunUtil.invokeStateful(timerCompileResult, "stopTask");
    }

    @Test(description = "Tests running a timer started within workers")
    public void testSimpleExecutionWithWorkers() {
        CompileResult timerCompileResult = BCompileUtil.compileAndSetup("test-src/task/timer-workers.bal");
        printDiagnostics(timerCompileResult);

        int w1InitialDelay = 500;
        int w1Interval = 1000;
        int w2InitialDelay = 800;
        int w2Interval = 2000;
        BRunUtil.invokeStateful(timerCompileResult, "scheduleTimer",
                new BValue[]{
                        new BInteger(w1InitialDelay), new BInteger(w1Interval),
                        new BInteger(w2InitialDelay), new BInteger(w2Interval),
                        new BString(""), new BString("")});
        await().atMost(30, SECONDS).until(() -> {
            BValue[] counts = BRunUtil.invokeStateful(timerCompileResult, "getCounts");
            return counts != null && counts[0] != null && counts[1] != null &&
                    ((BInteger) counts[0]).intValue() >= 5 && ((BInteger) counts[1]).intValue() >= 5;
        });

        // Now let's try stopping the tasks
        BRunUtil.invokeStateful(timerCompileResult, "stopTasks");

        // One more check to see whether the task really stopped
        BValue[] counts = BRunUtil.invokeStateful(timerCompileResult, "getCounts");
        assertEquals(((BInteger) counts[0]).intValue(), -1, "Count hasn't been reset");
        assertEquals(((BInteger) counts[1]).intValue(), -1, "Count hasn't been reset");
    }

    @Test(description = "Tests running a timer started within workers  where the onTrigger function generates an error")
    public void testExecutionWithWorkersAndErrorFn() {
        CompileResult timerCompileResult = BCompileUtil.compileAndSetup("test-src/task/timer-workers.bal");
        printDiagnostics(timerCompileResult);

        int w1InitialDelay = 500;
        int w1Interval = 1000;
        int w2InitialDelay = 800;
        int w2Interval = 1000;
        String w1ErrMsg = "w1: Timer error";
        String w2ErrMsg = "w2: Timer error";

        BRunUtil.invokeStateful(timerCompileResult, "scheduleTimer",
                new BValue[]{
                        new BInteger(w1InitialDelay), new BInteger(w1Interval),
                        new BInteger(w2InitialDelay), new BInteger(w2Interval),
                        new BString(w1ErrMsg), new BString(w2ErrMsg)});
        await().atMost(10, SECONDS).until(() -> {
            BValue[] errors = BRunUtil.invokeStateful(timerCompileResult, "getErrors");
            return errors != null && errors[0] != null && !errors[0].stringValue().isEmpty() &&
                    errors[1] != null && !errors[1].stringValue().isEmpty();
        });

        // Now test whether the onError Ballerina function got called
        BValue[] error = BRunUtil.invokeStateful(timerCompileResult, "getErrors");
        assertNotNull(error[0], "Expected error not returned.");
        assertEquals(error[0].stringValue(), w1ErrMsg, "Expected error message not returned.");
        assertEquals(error[1].stringValue(), w2ErrMsg, "Expected error message not returned.");

        // Now let's try stopping the tasks
        BRunUtil.invokeStateful(timerCompileResult, "stopTasks");

        // One more check to see whether the task really stopped
        BValue[] counts = BRunUtil.invokeStateful(timerCompileResult, "getCounts");
        assertEquals(((BInteger) counts[0]).intValue(), -1, "Count hasn't been reset");
        assertEquals(((BInteger) counts[1]).intValue(), -1, "Count hasn't been reset");
    }

    private void printDiagnostics(CompileResult timerCompileResult) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.getMessage() + " : " + e.getPosition()));
    }
}
