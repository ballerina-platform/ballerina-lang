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
        testProjectName = "language-construct-tests";
        testModuleFileName = "main.bal";
        testProjectPath = Paths.get(testProjectBaseDir.toString(), testProjectName).toString();
        testEntryFilePath = Paths.get(testProjectPath, testModuleFileName).toString();
    }

    @Test
    public void testLanguageConstructDebugScenarios() throws BallerinaTestException {
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 25));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 31));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 35));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 46));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 52));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 56));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 70));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage in object's init method
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        // Test for debug engage in object's method
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(1));

        // Test for debug engage in object's remote function
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(2));

        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);

        // Test for debug engage in mock object's init method
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(3));

        // Test for debug engage in mock object's method
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(4));

        // Test for debug engage in mock object's remote function
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(5));

        // Prepare variables for visibility test by adding a debug point at the end of the .bal file.
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);

        Map<String, Variable> variables = fetchVariables(debugHitInfo.getRight(), VariableScope.LOCAL);
        // Variable visibility test for object
        assertVariable(variables, "person", "Person", "object");
        // Variable visibility test for object's method
        assertVariable(variables, "fullName", "John Doe", "string");
        // Variable visibility test for object's remote function
        assertVariable(variables, "name", "John", "string");
        // Variable visibility test for mock object
        assertVariable(variables, "person2", "Person", "object");
        // Variable visibility test for mock object's method
        assertVariable(variables, "person2FullName", "Praveen Nada", "string");
        // Variable visibility test for mock object's remote function
        assertVariable(variables, "person2Name", "Praveen", "string");
    }

    @Test
    public void testWorkerScenarios() throws BallerinaTestException {
        testModuleName = "languageConstruct";
        testModuleFileName = "mainLangConstruct.bal";
        testEntryFilePath = Paths.get(testProjectPath, "src", testModuleName, testModuleFileName).toString();

        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 19));
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 25));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        // Test for debug engage when worker `w1`
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(0));

        // Test for debug engage when worker `w2`
        resumeProgram(debugHitInfo.getRight(), DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), testBreakpoints.get(1));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        terminateDebugSession();
    }
}
