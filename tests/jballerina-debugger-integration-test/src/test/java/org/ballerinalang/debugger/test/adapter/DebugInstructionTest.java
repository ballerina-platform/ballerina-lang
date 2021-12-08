/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.Thread;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test implementation for debug instructions related scenarios.
 */
public class DebugInstructionTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() {
    }

    @Test(description = "Tests the behaviour when stepping over on a return statement")
    public void stepOverOnReturnStatementTest() throws BallerinaTestException {
        String testProjectName = "debug-instruction-tests-1";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 39));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);

        // Tests STEP_OVER instruction on a function return statement (expected behaviour would be having a debug hit
        // in the caller function.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 33));
    }

    @Test(description = "Tests whether the debugger honors the breakpoints in-between step overs")
    public void breakpointInBetweenStepOverTest() throws BallerinaTestException {
        String testProjectName = "debug-instruction-tests-1";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 30));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 46));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);

        // Debugger should hit the breakpoint inside the function invocation when trying to step over.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 46));

        // Should go back to the caller function when stepping over again.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 31));
    }

    @Test(description = "Object related debug instruction test")
    public void objectDebugInstructionTest() throws BallerinaTestException {
        String testProjectName = "debug-instruction-tests-1";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 33));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 21));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 35));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);

        // TODO - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/30739
        // Tests STEP_IN instruction on object init() method.
        // debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_IN);
        // debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        // Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath,
        //        21));

        // Tests debug hit inside object init() method.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 21));

        // Tests STEP_OVER behaviour inside object init() method.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 22));

        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 34));

        // Tests STEP_IN instruction on object method.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_IN);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 25));

        // Tests NEXT_BREAKPOINT instruction inside object method.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 35));
    }

    @Test(description = "Tests whether the debugger honors pause requests")
    public void debugPauseTest() throws BallerinaTestException {
        String testProjectName = "debug-instruction-tests-2";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
        Path mainFilePath = debugTestRunner.testEntryFilePath;

        // Initial debug hit
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(mainFilePath, 18));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(mainFilePath, 18));
        Thread[] activeThreads = debugTestRunner.fetchThreads();
        if (activeThreads.length == 0) {
            throw new BallerinaTestException("Failed to retrieve active threads in the program VM");
        }

        // Timeout Exception is expected as the program is having an infinite while loop.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        try {
            debugTestRunner.waitForDebugHit(10000);
        } catch (BallerinaTestException e) {
            if (!e.getMessage().equals("Timeout expired waiting for the debug hit")) {
                throw e;
            }
        }

        // At this point, 'pause' command should suspend the program at the infinite while loop. (can be either
        // condition line or the statement body)
        debugTestRunner.pauseProgram(activeThreads[0].getId());
        debugHitInfo = debugTestRunner.waitForDebugHit(15000);
        Assert.assertTrue(debugHitInfo.getLeft().equals(new BallerinaTestDebugPoint(mainFilePath, 20))
                || debugHitInfo.getLeft().equals(new BallerinaTestDebugPoint(mainFilePath, 21)));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
