/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.debugger.test.adapter.evaluation;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.annotations.Test;

import static org.ballerinalang.debugger.test.adapter.evaluation.EvaluationExceptionKind.IMPORT_RESOLVING_ERROR;

/**
 * Test implementation for debug expression evaluation negative scenarios on Ballerina single file sources.
 */
public class SingleFileEvaluationNegativeTest extends ExpressionEvaluationNegativeTest {

    @Override
    protected void prepareForEvaluation() throws BallerinaTestException {
        String testProjectName = "basic-project";
        String testSingleFileName = "evaluation_main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testSingleFileName, false);

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 406));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        this.context = debugHitInfo.getRight();

        // Enable to see all the assertion failures at once.
        // debugTestRunner.setAssertionMode(DebugTestRunner.AssertionMode.SOFT_ASSERT);
    }

    @Override
    @Test
    public void functionCallEvaluationTest() throws BallerinaTestException {
        super.functionCallEvaluationTest();

        // qualified functions (i.e. imported modules)
        debugTestRunner.assertEvaluationError(context, "other:foo(-6)",
                String.format(IMPORT_RESOLVING_ERROR.getString(), "other"));
        debugTestRunner.assertEvaluationError(context, "foo:addition(2,6)",
                String.format(IMPORT_RESOLVING_ERROR.getString(), "foo"));
    }

    @Override
    @Test
    public void typeCastEvaluationTest() throws BallerinaTestException {
        super.typeCastEvaluationTest();

        // qualified literals (i.e. imported modules)
        debugTestRunner.assertEvaluationError(context, "<other:Location> location",
                String.format(IMPORT_RESOLVING_ERROR.getString(), "other"));
    }

    @Override
    @Test
    public void typeTestEvaluationTest() throws BallerinaTestException {
        super.typeTestEvaluationTest();

        // qualified literals (i.e. imported modules)
        debugTestRunner.assertEvaluationError(context, "location is other:Location",
                String.format(IMPORT_RESOLVING_ERROR.getString(), "other"));
    }

    @Override
    @Test
    public void unaryExpressionEvaluationTest() throws BallerinaTestException {
        super.unaryExpressionEvaluationTest();

        // with qualified literals (i.e. imported modules)
        debugTestRunner.assertEvaluationError(context, "-other:constant", String.format(IMPORT_RESOLVING_ERROR
                .getString(), "other"));
    }

    @Override
    public void nameReferenceEvaluationTest() throws BallerinaTestException {
        super.nameReferenceEvaluationTest();

        // access constants in undefined modules
        debugTestRunner.assertEvaluationError(context, "other:constant",
                String.format(IMPORT_RESOLVING_ERROR.getString(), "other"));

        //access module variables in undefined modules
        debugTestRunner.assertEvaluationError(context, "other:privateModuleVariable",
                String.format(IMPORT_RESOLVING_ERROR.getString(), "other"));
    }
}
