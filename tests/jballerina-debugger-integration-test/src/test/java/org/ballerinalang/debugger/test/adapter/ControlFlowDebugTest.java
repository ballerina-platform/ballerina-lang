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
import org.ballerinalang.debugger.test.DebugAdapterBaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.debugger.test.utils.TestUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.Variable;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.ballerinalang.debugger.test.utils.TestUtils.DebugResumeKind;
import static org.ballerinalang.debugger.test.utils.TestUtils.testBreakpoints;
import static org.ballerinalang.debugger.test.utils.TestUtils.testEntryFilePath;
import static org.ballerinalang.debugger.test.utils.TestUtils.testProjectBaseDir;
import static org.ballerinalang.debugger.test.utils.TestUtils.testProjectPath;

/**
 * Test class for control flow related debug scenarios.
 */
public class ControlFlowDebugTest extends DebugAdapterBaseTestCase {

    @BeforeClass
    public void setup() {
        String testProjectName = "control-flow-tests";
        String testModuleFileName = "main.bal";
        testProjectPath = Paths.get(testProjectBaseDir.toString(), testProjectName).toString();
        testEntryFilePath = Paths.get(testProjectPath, testModuleFileName).toString();
    }

    @Test
    public void testControlFlowDebugScenarios() throws BallerinaTestException {
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 8));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 16));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 22));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 30));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 36));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 44));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 54));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 59));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 60));
        TestUtils.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage in 'if' statement
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = TestUtils.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        // Test for debug engage inside 'if' statement when condition is true.
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 9));

        // Test for debug engage in 'else' statement
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(1));

        // Test for debug engage in 'else-if' statement
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(2));

        // Test for debug engage inside 'else-if' statement when condition is true.
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 23));

        // Test for debug engage in 'while' loop
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(3));

        // Test for debug engage inside 'while' loop when condition is true.
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 31));

        // Test for debug engage in 'foreach' loop
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(4));

        // Test for debug engage inside 'foreach' loop when condition is true.
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 37));

        // Test for debug engage in 'match' statement
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(5));

        // Test for debug engage inside 'match' statement when condition is true.
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 45));

        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 46));

        // Test for debug engage in lambda - iterable arrow operation
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(6));

        // Test for debug engage in Asynchronous function call - Non-blocking calls
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(7));

        // Prepare variables for visibility test by adding a debug point at the end of the .bal file.
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);

        Map<String, Variable> variables = TestUtils.fetchVariables(debugHitInfo.getRight(),
                TestUtils.VariableScope.LOCAL);
        // Variable visibility test inside 'match' statement
        TestUtils.assertVariable(variables, "v07_intVar", "7", "int");
        // Variable visibility test for lambda - iterable arrow operation
        TestUtils.assertVariable(variables, "v09_animals", "map", "map");

        // Variable visibility test for lambda child variables
        Map<String, Variable> lambdaChildVariables = TestUtils.fetchChildVariables(variables.get("v09_animals"));
        TestUtils.assertVariable(lambdaChildVariables, "a", "ANT", "string");

        // Variable visibility test for Asynchronous function call (Non-blocking calls)
        TestUtils.assertVariable(variables, "v10_future", "future", "future");

        // Variable visibility test for Asynchronous function call child variables
        Map<String, Variable> asyncChildVariables = TestUtils.fetchChildVariables(variables.get("v10_future"));
        TestUtils.assertVariable(asyncChildVariables, "result", "90", "int");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        TestUtils.terminateDebugSession();
    }
}
