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
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArgumentsCategory;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test class for log points related debug scenarios.
 */
public class LogPointTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() {
        String testProjectName = "logpoint-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test
    public void testConditionalBreakpointScenarios() throws BallerinaTestException {
        Path filePath = debugTestRunner.testEntryFilePath;
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 29));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 34, "y == 3", "LogPoint: 1"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 44, "capital == \"London\"", null));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 47, "x != 5", "LogPoint: 2"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 48, "x == 5", null));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 52, "", "LogPoint: 3"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 53, "x > 0", null));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));

        // Tests logpoint with conditional expression which evaluates to `true`.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(2));
        Pair<String, OutputEventArguments> outputInfo = debugTestRunner.waitForLastDebugOutput(1000);
        Assert.assertEquals(outputInfo.getLeft(), "LogPoint: 1" + System.lineSeparator());
        Assert.assertEquals(outputInfo.getRight().getCategory(), OutputEventArgumentsCategory.STDOUT);

        // Tests logpoint with conditional expression which evaluates to `false`. (In here, waiting for debug out
        // should throw an `BallerinaTestException` as the logpoint is supposed to be skipped if the condition evaluates
        // to `false`)
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(4));
        try {
            outputInfo = debugTestRunner.waitForLastDebugOutput(1000);
        } catch (BallerinaTestException e) {
            Assert.assertEquals(e.getMessage(), "Timeout expired waiting for the debugger output");
        }

        // Tests logpoint without any conditional expressions.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(6));
        outputInfo = debugTestRunner.waitForLastDebugOutput(1000);
        Assert.assertEquals(outputInfo.getLeft(), "LogPoint: 3" + System.lineSeparator());
        Assert.assertEquals(outputInfo.getRight().getCategory(), OutputEventArgumentsCategory.STDOUT);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
