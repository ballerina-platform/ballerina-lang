/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 * Test class for workspace project debugging related scenarios for run command.
 *
 * @since 2201.13.0
 */
public class WorkspaceProjectRunDebugTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @Override
    @BeforeClass
    public void setup() {
        String testProjectName = "workspace-project/hello-app";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test(description = "Test to verify debug scenarios in a multi module project")
    public void testWorkspaceProjectDebugScenarios() throws BallerinaTestException {
        Path filePath1 = debugTestRunner.testProjectPath
                .resolve("main.bal");
        Path filePath2 = debugTestRunner.testProjectPath
                .getParent()
                .resolve("pkgA")
                .resolve("main.bal");
        Path filePath3 = debugTestRunner.testProjectPath
                .getParent()
                .resolve("other")
                .resolve("pkgB")
                .resolve("main.bal");

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath1, 8));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(filePath3, 10));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test initial breakpoint hit
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.getFirst());

        // Test step-in
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_IN);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(filePath2, 12));

        // Test next breakpoint hit
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(1));

        // Test step-over
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(filePath1, 9));
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
