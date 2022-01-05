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
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.ballerinalang.debugger.test.utils.DebugTestRunner.DebugResumeKind;

/**
 * Test class for multi module related debug scenarios for run command.
 */
public class MultiModuleRunDebugTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() {
        String testProjectName = "breakpoint-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test
    public void testMultiModuleDebugScenarios() throws BallerinaTestException {
        Path filePath1 = debugTestRunner.testProjectPath.resolve("utils.bal");
        Path filePath2 = debugTestRunner.testProjectPath.resolve("modules").resolve("math").resolve("add.bal");

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 22));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 28));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));

        // Test for step over
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 23));

        // Test for break point hit
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(1));

        // Test for step in within same file
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_IN);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 37));

        // Test for step out within same file
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OUT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 28));

        // Test for step in between different file, within same module
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_IN);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(filePath1, 4));

        // Test for step in between different file, between different module
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_IN);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(filePath2, 22));

        // Test for step out between different file, between different module
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OUT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(filePath1, 5));

        // Test for step out between different file, within same module
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OUT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 29));

        // Add on-the-fly debug points
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 31));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 32));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 33));

        // Test for on-the-fly debug point add
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(2));

        // Remove on-the-fly debug point
        debugTestRunner.removeBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 32));

        // Test for on-the-fly debug point remove
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(3));
    }

    @Test
    public void testLangLibDebugScenarios() throws BallerinaTestException {
        Path filePath = debugTestRunner.testProjectPath.resolve(debugTestRunner.getBalServer().getServerHome())
                .resolve("repo").resolve("bala").resolve("ballerina").resolve("lang.query").resolve("0.0.0")
                .resolve("any").resolve("modules").resolve("lang.query").resolve("types.bal");

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath, 84));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage inside lang lib init() method
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft().getSource(), debugTestRunner.testBreakpoints.get(0).getSource());
        Assert.assertEquals(debugHitInfo.getLeft().getDAPBreakPoint().getLine(),
                debugTestRunner.testBreakpoints.get(0).getDAPBreakPoint().getLine());
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
