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

package org.ballerinalang.debugger.test.adapter.completions;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.CompletionItem;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

public class CompletionsDebugTest extends BaseTestCase {

    Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo;
    DebugTestRunner debugTestRunner;
    Map<String, CompletionItem> completions;

    @BeforeClass
    public void setup() {
        String testProjectName = "completions-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test
    public void testDebugCompletions() throws BallerinaTestException {
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 67));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 75));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 77));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 82));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 88));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 99));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);
        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(),"");
        Assert.assertEquals(completions.size(), 30);

        // Test for global variable completions in the beginning of the main() method.
        debugTestRunner.assertCompletions(completions, "globalVar");

        // Debug completions test where variable declaration and assignment are in the same line.
        assertCompletionSuggestions();

        // Debug completions test where variable declaration and assignment are in different line.
        assertCompletionSuggestions();

        // Debug completions test where variable declaration and assignment are along with the open curl braces `{`.
        assertCompletionSuggestions();

        // Debug completions test where debug point is at the beginning of the function block node.
        assertCompletionSuggestions();

        // Debug completions test at the end of the main() method.
        assertCompletionSuggestions();
        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(),"");
        Assert.assertEquals(completions.size(), 41);
    }

    private void assertCompletionSuggestions() throws BallerinaTestException {
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);

        // Debug completions test for variables
        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(),"");
        debugTestRunner.assertCompletions(completions, "name");
        debugTestRunner.assertCompletions(completions, "number");

        // Debug completions test for record fields
        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(),"s1.");
        debugTestRunner.assertCompletions(completions, "firstName");
        debugTestRunner.assertCompletions(completions, "lastName");
        debugTestRunner.assertCompletions(completions, "score");

        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(),"s1.fi");
        debugTestRunner.assertCompletions(completions, "firstName");
        debugTestRunner.assertCompletions(completions, "lastName");
        debugTestRunner.assertCompletions(completions, "score");

        // Debug completions test for object methods and object fields
        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(),"person.");
        Assert.assertEquals(completions.size(), 5);
        debugTestRunner.assertCompletions(completions, "getName()");
        debugTestRunner.assertCompletions(completions, "getId()");
        debugTestRunner.assertCompletions(completions, "getCar()");
        debugTestRunner.assertCompletions(completions, "id");
        debugTestRunner.assertCompletions(completions, "name");

        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(),"person.ge");
        Assert.assertEquals(completions.size(), 5);
        debugTestRunner.assertCompletions(completions, "getName()");
        debugTestRunner.assertCompletions(completions, "getId()");
        debugTestRunner.assertCompletions(completions, "getCar()");
        debugTestRunner.assertCompletions(completions, "id");
        debugTestRunner.assertCompletions(completions, "name");

        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(), "person.getCar().");
        Assert.assertEquals(completions.size(), 2);
        debugTestRunner.assertCompletions(completions, "getCarName()");
        debugTestRunner.assertCompletions(completions, "carName");

        completions = debugTestRunner.fetchCompletions(debugHitInfo.getRight(), "person.getCar().getCarName().");
        Assert.assertEquals(completions.size(), 34);
        debugTestRunner.assertCompletions(completions, "toBalString()");
    }



    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
