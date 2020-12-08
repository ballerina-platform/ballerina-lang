/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugger.test.adapter;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.debugger.test.utils.DebugTestRunner.DebugResumeKind;

/**
 * Test class for debug hit call stack representation.
 */
public class CallStackDebugTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        String testProjectName = "callstack-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 35));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 44));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 40));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
    }

    @Test
    public void callStackTest() throws BallerinaTestException {
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        StackFrame[] frames = debugTestRunner.fetchStackFrames(debugHitInfo.getRight());

        // Call stack representation test for strand creation with 'start' keyword.
        // Created strand is invoking a remote function.
        debugTestRunner.assertCallStack(frames[0], "getName", 35, "main.bal");
        debugTestRunner.assertCallStack(frames[1], "func3", 28, "main.bal");
        debugTestRunner.assertCallStack(frames[2], "func2", 23, "main.bal");
        debugTestRunner.assertCallStack(frames[3], "func1", 19, "main.bal");
        debugTestRunner.assertCallStack(frames[4], "addition", 14, "main.bal");
        debugTestRunner.assertCallStack(frames[5], "start:f1", 2, "main.bal");

        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        frames = debugTestRunner.fetchStackFrames(debugHitInfo.getRight());

        // Call stack representation test for strand creation with 'worker' keyword.
        debugTestRunner.assertCallStack(frames[0], "multiply", 44, "main.bal");
        debugTestRunner.assertCallStack(frames[1], "w1", 6, "main.bal");
        debugTestRunner.assertCallStack(frames[2], "worker:w1", 5, "main.bal");

        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        frames = debugTestRunner.fetchStackFrames(debugHitInfo.getRight());

        // Call stack representation test for strand creation with 'start' keyword.
        // Results of the strand is not assigned to any variable. In this case frame name is assigned to 'anonymous'.
        debugTestRunner.assertCallStack(frames[0], "sayHello", 40, "main.bal");
        debugTestRunner.assertCallStack(frames[1], "start:anonymous", 10, "main.bal");
    }

    @AfterClass(alwaysRun = true)
    private void cleanup() {
        debugTestRunner.terminateDebugSession();
    }
}
