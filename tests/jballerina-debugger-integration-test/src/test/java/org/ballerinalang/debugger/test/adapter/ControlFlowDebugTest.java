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
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.Variable;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Map;

/**
 * Test class for control flow related debug scenarios.
 */
public class ControlFlowDebugTest extends DebugAdapterBaseTestCase {

    @BeforeClass
    public void setup() {
        testProjectName = "breakpoint-tests";
        testModuleName = "controlFlow";
        testModuleFileName = "mainControlFlow.bal";
        testProjectPath = Paths.get(testProjectBaseDir.toString(), testProjectName).toString();
        testEntryFilePath = Paths.get(testProjectPath, "src", testModuleName, testModuleFileName).toString();
    }

    @Test(enabled = false)
    public void testControlFlowDebugScenarios() throws BallerinaTestException {
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 9));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 17));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 23));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 31));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 38));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 46));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 56));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 65));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 62));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage in 'if' statement
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        // Test for debug engage inside 'if' statement when condition is true.
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 10));

        // Test for debug engage in 'else' statement
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(1));

        // Test for debug engage in 'else-if' statement
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(2));

        // Test for debug engage inside 'else-if' statement when condition is true.
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 24));

        // Test for debug engage in 'while' loop
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(3));

        // Test for debug engage inside 'while' loop when condition is true.
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 32));

        // Test for debug engage in 'foreach' loop
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(4));

        // Test for debug engage inside 'foreach' loop when condition is true.
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 39));

        // Test for debug engage in 'match' statement
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(5));

        // Test for debug engage inside 'match' statement when condition is true.
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 47));

        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 48));

        // Test for debug engage in lambda - iterable arrow operation
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(6));

        // Test for debug engage in Asynchronous function call - Non-blocking calls
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(7));

        // Prepare variables for visibility test by adding a debug point at the end of the .bal file.
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);

        Map<String, Variable> variables = fetchVariables(debugHitInfo.getRight(), VariableScope.LOCAL);
        // Variable visibility test inside 'match' statement
        assertVariable(variables, "v07_intVar", "7", "int");
        // Variable visibility test for lambda - iterable arrow operation
        assertVariable(variables, "v09_animals", "map", "map");

        // Variable visibility test for lambda child variables
        Map<String, Variable> lamdaChildVariables = fetchChildVariables(variables.get("v09_animals"));
        assertVariable(lamdaChildVariables, "a", "ANT", "string");

        // Variable visibility test for Asynchronous function call (Non-blocking calls)
        assertVariable(variables, "v10_future", "future", "future");

        // Variable visibility test for Asynchronous function call child variables
        Map<String, Variable> asyncChildVariables = fetchChildVariables(variables.get("v10_future"));
        assertVariable(asyncChildVariables, "result", "90", "int");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        terminateDebugSession();
    }
}
