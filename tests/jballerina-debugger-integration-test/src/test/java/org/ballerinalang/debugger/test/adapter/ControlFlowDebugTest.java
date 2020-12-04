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
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.Variable;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.ballerinalang.debugger.test.utils.DebugTestRunner.DebugResumeKind;
import static org.ballerinalang.debugger.test.utils.DebugTestRunner.VariableScope;

/**
 * Test class for control flow related debug scenarios.
 */
public class ControlFlowDebugTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() {
        String testProjectName = "control-flow-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test
    public void testControlFlowDebugScenarios() throws BallerinaTestException {
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 8));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 16));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 22));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 30));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 36));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 44));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 54));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 59));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 60));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage in 'if' statement
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));

        // Test for debug engage inside 'if' statement when condition is true.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 9));

        // Test for debug engage in 'else' statement
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(1));

        // Test for debug engage in 'else-if' statement
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(2));

        // Test for debug engage inside 'else-if' statement when condition is true.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 23));

        // Test for debug engage in 'while' loop
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(3));

        // Test for debug engage inside 'while' loop when condition is true.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 31));

        // Test for debug engage in 'foreach' loop
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(4));

        // Test for debug engage inside 'foreach' loop when condition is true.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 37));

        // Test for debug engage in 'match' statement
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(5));

        // Test for debug engage inside 'match' statement when condition is true.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 45));

        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 46));

        // Test for debug engage in lambda - iterable arrow operation
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(6));

        // Test for debug engage in Asynchronous function call - Non-blocking calls
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(7));

        // Prepare variables for visibility test by adding a debug point at the end of the .bal file.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);

        Map<String, Variable> variables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), VariableScope.LOCAL);
        // Variable visibility test inside 'match' statement
        debugTestRunner.assertVariable(variables, "v07_intVar", "7", "int");
        // Variable visibility test for lambda - iterable arrow operation
        debugTestRunner.assertVariable(variables, "v09_animals", "map", "map");

        // Variable visibility test for lambda child variables
        Map<String, Variable> lamdaChildVariables = debugTestRunner.fetchChildVariables(variables.get("v09_animals"));
        debugTestRunner.assertVariable(lamdaChildVariables, "a", "ANT", "string");

        // Variable visibility test for Asynchronous function call (Non-blocking calls)
        debugTestRunner.assertVariable(variables, "v10_future", "future", "future");

        // Variable visibility test for Asynchronous function call child variables
        Map<String, Variable> asyncChildVariables = debugTestRunner.fetchChildVariables(variables.get("v10_future"));
        debugTestRunner.assertVariable(asyncChildVariables, "result", "90", "int");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
