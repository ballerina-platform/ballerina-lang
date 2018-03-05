/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.debugger;

import org.ballerinalang.test.utils.debug.ExpectedResults;
import org.ballerinalang.test.utils.debug.VMDebuggerUtil;
import org.ballerinalang.test.utils.debug.WorkerResults;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.ballerinalang.test.utils.debug.Step.RESUME;
import static org.ballerinalang.test.utils.debug.Step.STEP_IN;
import static org.ballerinalang.test.utils.debug.Step.STEP_OUT;
import static org.ballerinalang.test.utils.debug.Step.STEP_OVER;

/**
 * Test Cases for {@link Debugger}.
 */
public class VMDebuggerTest {

    private static final String FILE = "test-debug.bal";
    private PrintStream original;

    @BeforeClass
    public void setup() {
        original = System.out;
        // Hiding all test System outs.
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
    }

    @AfterClass
    public void tearDown() {
        System.setOut(original);
    }

    @Test(description = "Testing Resume with break points.")
    public void testResume() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE,
                3, 16, 27, 28, 31, 33, 35, 41, 42, 43, 44, 45);
        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".", FILE,
                3, 16, 41, 28, 35, 42, 43), RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME);

        ExpectedResults expRes = new ExpectedResults(mainWorker, false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Debugger with breakpoint in non executable and not reachable lines.")
    public void testNegativeBreakPoints() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 4, 7, 51, 37);
        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".",
                FILE));

        ExpectedResults expRes = new ExpectedResults(mainWorker, false);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step In.")
    public void testStepIn() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 5, 8, 41);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".", FILE,
                5, 12, 13, 14, 15, 19, 13, 8, 41, 25, 26, 28, 29, 35, 36, 42, 43, 9),
                STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, RESUME, STEP_IN, STEP_IN,
                STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, STEP_IN, RESUME);

        ExpectedResults expRes = new ExpectedResults(mainWorker, false);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step Out.")
    public void testStepOut() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 25);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".",
                FILE, 25, 42, 9), STEP_OUT, STEP_OUT, RESUME);
        ExpectedResults expRes = new ExpectedResults(mainWorker, false);
        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step Over.")
    public void testStepOver() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 3);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".", FILE,
                3, 5, 6, 8, 9, 10), STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME);

        ExpectedResults expRes = new ExpectedResults(mainWorker, false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step over in IfCondition.")
    public void testStepOverIfStmt() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 26);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".", FILE,
                26, 28, 29, 35, 36, 42), STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME);

        ExpectedResults expRes = new ExpectedResults(mainWorker, false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing Step over in WhileStmt.")
    public void testStepOverWhileStmt() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".", FILE, 13, 19, 21);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".", FILE,
                13, 14, 19, 13, 19, 13, 19, 13, 19, 13, 21),
                STEP_OVER, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME);

        ExpectedResults expRes = new ExpectedResults(mainWorker, false);


        VMDebuggerUtil.startDebug("test-src/debugger/test-debug.bal", breakPoints, expRes);
    }

    @Test(description = "Testing while statement resume")
    public void testWhileStatementResume() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "while-statement.bal", 5);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".",
                "while-statement.bal", 5, 5, 5, 5, 5), RESUME, RESUME, RESUME, RESUME, RESUME);

        ExpectedResults expRes = new ExpectedResults(mainWorker, false);

        VMDebuggerUtil.startDebug("test-src/debugger/while-statement.bal", breakPoints, expRes);
    }

    @Test(description = "Testing try catch finally scenario for path")
    public void testTryCatchScenarioForPath() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "try-catch-finally.bal", 19);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".",
                "try-catch-finally.bal", 19, 27, 29, 31, 32, 33, 34, 35, 43, 44, 45, 50, 55, 56, 58, 60),
                STEP_IN, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER,
                STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, RESUME);

        ExpectedResults expRes = new ExpectedResults(mainWorker, false);
        VMDebuggerUtil.startDebug("test-src/debugger/try-catch-finally.bal", breakPoints, expRes);
    }

    @Test(description = "Testing debug paths in workers")
    public void testDebuggingWorkers() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "test-worker.bal", 3, 9, 10, 18, 19, 23, 46);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".",
                "test-worker.bal", 3), RESUME);
        WorkerResults worker1 = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".",
                "test-worker.bal", 9, 10, 12, 31, 13), STEP_OVER, STEP_OVER, STEP_IN, STEP_OUT, RESUME);
        WorkerResults worker2 = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".", "test-worker.bal",
                18, 19, 46, 46, 46, 46, 46, 23), STEP_OVER, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME, RESUME);
        ExpectedResults expRes = new ExpectedResults(mainWorker, true);
        expRes.addWorkerResults(worker1);
        expRes.addWorkerResults(worker2);

        VMDebuggerUtil.startDebug("test-src/debugger/test-worker.bal", breakPoints, expRes);
    }

    @Test(description = "Testing debug paths in package init")
    public void testDebuggingPackageInit() {
        BreakPointDTO[] breakPoints = VMDebuggerUtil.createBreakNodeLocations(".",
                "test-package-init.bal", 3, 7);

        WorkerResults mainWorker = new WorkerResults(VMDebuggerUtil.createWorkerBreakPoints(".",
                "test-package-init.bal", 3, 5, 14, 15, 16, 20, 14, 15, 16, 20, 14, 22, 9),
                STEP_OVER, STEP_IN, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER, STEP_OVER,
                STEP_OVER, STEP_OVER, RESUME, RESUME);
        ExpectedResults expRes = new ExpectedResults(mainWorker, false);

        VMDebuggerUtil.startDebug("test-src/debugger/test-package-init.bal", breakPoints, expRes);
    }
}
