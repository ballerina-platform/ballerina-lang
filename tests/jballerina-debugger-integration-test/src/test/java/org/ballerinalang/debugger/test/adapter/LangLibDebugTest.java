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
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test implementation for Ballerina language library debugging related test scenarios.
 *
 * @since 2.0.0
 */
public class LangLibDebugTest {

    private DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() {
        String testProjectName = "langlib-debug-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test(description = "Evaluates debug hits and step events inside ballerina language libraries")
    public void testLangLibDebugHits() throws BallerinaTestException {

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 19));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 22));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);

        // Steps into `int` language library.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_IN);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertTrue(debugHitInfo.getLeft().getSource().getPath().replaceAll("\\\\", "/")
                .endsWith("ballerina/lang.int/0.0.0/any/modules/lang.int/int.bal"));

        // Next breakpoint.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 22));

        // Steps into `float` language library.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_IN);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertTrue(debugHitInfo.getLeft().getSource().getPath().replaceAll("\\\\", "/")
                .endsWith("ballerina/lang.float/0.0.0/any/modules/lang.float/float.bal"));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
