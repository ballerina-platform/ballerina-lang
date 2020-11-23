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

package org.ballerinalang.debugger.test.adapter.run;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.DebugAdapterBaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.debugger.test.utils.TestUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.ballerinalang.debugger.test.utils.TestUtils.DebugResumeKind;
import static org.ballerinalang.debugger.test.utils.TestUtils.testBreakpoints;
import static org.ballerinalang.debugger.test.utils.TestUtils.testEntryFilePath;
import static org.ballerinalang.debugger.test.utils.TestUtils.testProjectBaseDir;
import static org.ballerinalang.debugger.test.utils.TestUtils.testProjectPath;

/**
 * Test class for multi module related debug scenarios for run command.
 */
public class MultiModuleRunDebugTest extends DebugAdapterBaseTestCase {

    @BeforeClass
    public void setup() {
        String testProjectName = "breakpoint-tests";
        String testModuleFileName = "main.bal";
        testProjectPath = testProjectBaseDir.toString() + File.separator + testProjectName;
        testEntryFilePath = Paths.get(testProjectPath, testModuleFileName).toString();
    }

    @Test
    public void testMultiModuleDebugScenarios() throws BallerinaTestException {
        String filePath1 = Paths.get(testProjectPath, "utils.bal").toString();
        String filePath2 = Paths.get(testProjectPath, "modules", "math", "add.bal").toString();

        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 22));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 28));
        TestUtils.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = TestUtils.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        // Test for step over
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 23));

        // Test for break point hit
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(1));

        // Test for step in within same file
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_IN);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 37));

        // Test for step out within same file
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OUT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 28));

        // Test for step in between different file, within same module
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_IN);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(filePath1, 4));

        // Test for step in between different file, between different module
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_IN);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        // Todo - enable
        // Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(filePath2, 22));

        // Test for step out between different file, between different module
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OUT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(filePath1, 5));

        // Test for step out between different file, within same module
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OUT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(testEntryFilePath, 29));

        // Add on-the-fly debug points
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 31));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 32));
        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 33));

        // Test for on-the-fly debug point add
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(2));

        // Remove on-the-fly debug point
        TestUtils.removeBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 32));

        // Test for on-the-fly debug point remove
        TestUtils.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = TestUtils.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(3));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        TestUtils.terminateDebugSession();
    }
}
