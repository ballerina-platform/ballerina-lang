/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugger.test.adapter.evaluation;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test implementation to validate expression evaluator functionality against Ballerina dependency sources.
 */
public class DependencyEvaluationTest extends BaseTestCase {

    protected DebugTestRunner debugTestRunner;

    @BeforeClass(alwaysRun = true)
    public void setup() throws BallerinaTestException {
        String testProjectName = "evaluation-tests-2";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test(description = "Test for expression evaluations against Ballerina lang library sources.")
    protected void testEvaluationsOnLangLibs() throws BallerinaTestException {
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 18));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));

        // Step into the lang-lib function
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_IN);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft().getSourceURI().getScheme(), BALA_URI_SCHEME);
        Assert.assertTrue(debugHitInfo.getLeft().getSource().getPath().replaceAll("\\\\", "/")
                .endsWith("ballerina/lang.int/0.0.0/any/modules/lang.int/int.bal"));

        // Evaluates various types of expressions on the Ballerina library source debug hit.
        debugTestRunner.assertExpression(debugHitInfo.getRight(), "n", "12", "int");
        debugTestRunner.assertExpression(debugHitInfo.getRight(), "n + n", "24", "int");
        debugTestRunner.assertExpression(debugHitInfo.getRight(), "n.toBalString()", "\"12\"", "string");
        debugTestRunner.assertExpression(debugHitInfo.getRight(), "fromString(\"10\")", "10", "int");
        debugTestRunner.assertExpression(debugHitInfo.getRight(), "let int x = 4 in 2 * x * n", "96", "int");
        debugTestRunner.assertExpression(debugHitInfo.getRight(), "from var i in from var j in [1, 2, 3] " +
                "select j select i", "int[3]", "array");
    }

    @Override
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
