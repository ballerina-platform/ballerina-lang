/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.ballerinalang.debugger.test.utils.DebugTestRunner.DebugResumeKind;

/**
 * Test class for ballerina breakpoints related test scenarios.
 */
public class ModuleBreakpointTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() {
        String testProjectName = "breakpoint-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test(description = "Test to verify breakpoint hits within the default module of Ballerina packages")
    public void testMultipleBreakpointsInDefaultModule() throws BallerinaTestException {

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 19));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 33));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));

        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo2 = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo2.getLeft(), debugTestRunner.testBreakpoints.get(1));
    }

    @Test(description = "Test to verify breakpoint hits within non-default modules of Ballerina packages")
    public void testMultipleBreakpointsInNonDefaultModules() throws BallerinaTestException {

        Path otherModuleFilePath = debugTestRunner.testProjectPath.resolve("modules").resolve("math")
                .resolve("add.bal");
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(otherModuleFilePath, 22));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(otherModuleFilePath, 31));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));

        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo2 = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo2.getLeft(), debugTestRunner.testBreakpoints.get(1));
    }

    @Test(description = "Test to verify breakpoint hits within the test sources of non-default modules")
    public void testMultipleBreakpointsInNonDefaultModuleTests() throws BallerinaTestException {
        Path nonDefaultModuleTestPath = debugTestRunner.testProjectPath
                .resolve("modules")
                .resolve("math")
                .resolve("tests")
                .resolve("math_tests.bal");

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(nonDefaultModuleTestPath, 21));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(nonDefaultModuleTestPath, 22));

        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.TEST);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));

        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(1));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
