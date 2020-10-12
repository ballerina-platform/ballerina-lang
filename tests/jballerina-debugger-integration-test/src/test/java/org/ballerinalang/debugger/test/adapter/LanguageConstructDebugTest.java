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
 * Test class for language construct related debug scenarios.
 */
public class LanguageConstructDebugTest extends DebugAdapterBaseTestCase {

    @BeforeClass
    public void setup() {
        testProjectName = "breakpoint-tests";
        testModuleName = "languageConstruct";
        testModuleFileName = "mainLangConstruct.bal";
        testProjectPath = Paths.get(testProjectBaseDir.toString(), testProjectName).toString();
        testEntryFilePath = Paths.get(testProjectPath, "src", testModuleName, testModuleFileName).toString();
    }

    @Test(enabled = false)
    public void testLanguageConstructDebugScenarios() throws BallerinaTestException {
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 12));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 18));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 30));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 35));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 48));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage in object init method
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        // Test for debug engage in object method
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(1));

        // Test for debug engage in remote object
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(2));

        // Test for debug engage in remote function
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(3));

        // Prepare variables for visibility test by adding a debug point at the end of the .bal file.
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);

        Map<String, Variable> variables = fetchVariables(debugHitInfo.getRight(), VariableScope.LOCAL);
        // Variable visibility test for object method
        assertVariable(variables, "v02_fullName", "John Doe", "string");
        // Variable visibility test for remote object function
        assertVariable(variables, "v04_statusCode", "500", "int");
    }

    @Test(enabled = false)
    public void testWorkerScenarios() throws BallerinaTestException {
        testModuleName = "languageConstruct";
        testModuleFileName = "mainLangConstruct.bal";
        testEntryFilePath = Paths.get(testProjectPath, "src", testModuleName, testModuleFileName).toString();

        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 7));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 13));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 18));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage when worker `w1`
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        // Test for debug engage when worker `w2`
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(1));

        // Test for debug engage when worker `w3`
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(2));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        terminateDebugSession();
    }
}
