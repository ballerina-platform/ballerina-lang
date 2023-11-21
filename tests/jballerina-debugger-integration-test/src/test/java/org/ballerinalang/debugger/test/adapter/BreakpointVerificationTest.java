/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test implementation for debug breakpoint verification scenarios.
 */
public class BreakpointVerificationTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;
    private static final int SRC_LINE_COUNT = 115; // last line number of the debug source.

    private static final int ENUM_DCLN_START = 17;
    private static final int CLASS_DEFN_START = ENUM_DCLN_START + 6;
    private static final int IF_STATEMENT_START = CLASS_DEFN_START + 25;
    private static final int WHILE_LOOP_START = IF_STATEMENT_START + 9;
    private static final int FOREACH_LOOP_START = WHILE_LOOP_START + 10;
    private static final int MATCH_STMT_START = FOREACH_LOOP_START + 10;
    private static final int DOCUMENTATION_START = MATCH_STMT_START + 8;
    private static final int WORKER_DCLN_START = DOCUMENTATION_START + 10;

    @BeforeClass
    public void setup() {
        String testProjectName = "breakpoint-verification-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test(description = "Test to assert runtime verification on breakpoints which were added before starting a " +
            "debug session")
    public void testInitialBreakpointVerification() throws BallerinaTestException {
        // Adds breakpoints for all the lines in the source.
        for (int line = 1; line <= SRC_LINE_COUNT; line++) {
            debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, line));
        }

        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 27));

        // retrieves all the 'breakpoint' events received from the server, which indicates breakpoint verification
        // status changes.
        List<BallerinaTestDebugPoint> changedBreakpoints = debugTestRunner.waitForModifiedBreakpoints(2000);
        assertBreakpointChanges(changedBreakpoints);
    }

    @Test(description = "Test to assert runtime verification on breakpoints which are getting added on-the-fly " +
            "during a debug session", enabled = false)
    public void testOnTheFlyBreakpointVerification() throws BallerinaTestException {
        // adds one initial breakpoint and run debug session until the breakpoint is reached.
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 27));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 27));

        // adds breakpoints on-the-fly to all the other source lines, once the debugger is suspended on the initial
        // breakpoint.
        Optional<SetBreakpointsResponse> breakpointResponse = Optional.empty();
        for (int line = 1; line <= SRC_LINE_COUNT; line++) {
            BallerinaTestDebugPoint debugPoint = new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, line);
            breakpointResponse = debugTestRunner.addBreakPoint(debugPoint);
        }

        Assert.assertTrue(breakpointResponse.isPresent(), "Breakpoint response should not be empty");
        List<BallerinaTestDebugPoint> breakPoints = Arrays.stream(breakpointResponse.get().getBreakpoints())
                .map(breakpoint -> new BallerinaTestDebugPoint(Path.of(breakpoint.getSource().getPath()),
                        breakpoint.getLine(), breakpoint.isVerified()))
                .collect(Collectors.toList());

        assertBreakpointChanges(breakPoints);
    }

    private void assertBreakpointChanges(List<BallerinaTestDebugPoint> breakpoints) {
        // if statement
        assertVerifiedBreakpoints(breakpoints, IF_STATEMENT_START, 7, true, true, true, true, false, true, false);
        // while statement
        assertVerifiedBreakpoints(breakpoints, WHILE_LOOP_START, 8, true, true, true, true, false, true, true, false);
        // foreach statement
        assertVerifiedBreakpoints(breakpoints, MATCH_STMT_START, 5, true, true, true, false, false);
        // documentation statement
        assertVerifiedBreakpoints(breakpoints, DOCUMENTATION_START, 4, false, false, false, false);

        // enum declaration
        assertVerifiedBreakpoints(breakpoints, ENUM_DCLN_START, 5, false, false, false, false, false);
        // class definition
        assertVerifiedBreakpoints(breakpoints, CLASS_DEFN_START, 15, true, false, false, true, true, true, false,
                false, true, true, false, false, true, true, false);
        // worker declarations
        assertVerifiedBreakpoints(breakpoints, WORKER_DCLN_START, 9, true, true, false, false, true, true, false, false,
                true);
    }

    private void assertVerifiedBreakpoints(List<BallerinaTestDebugPoint> breakpoints, int startLine, int length,
                                           boolean... expectedResults) {
        Assert.assertEquals(length, expectedResults.length, "Number of expected breakpoints should be equal to" +
                " the number of source lines");

        for (int line = startLine; line < startLine + length; line++) {
            int finalLine = line;
            Optional<BallerinaTestDebugPoint> breakpointMatch = breakpoints.stream()
                    .filter(debugPoint -> debugPoint.getLine() == finalLine)
                    .findAny();

            Assert.assertEquals(breakpointMatch.isPresent() && breakpointMatch.get().isVerified(),
                    expectedResults[line - startLine], "Mismatching breakpoint verification status at line: " + line);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
