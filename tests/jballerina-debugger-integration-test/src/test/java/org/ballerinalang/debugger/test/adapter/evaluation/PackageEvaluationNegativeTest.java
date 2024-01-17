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

import static org.ballerinalang.debugger.test.adapter.evaluation.EvaluationExceptionKind.CUSTOM_ERROR;
import static org.ballerinalang.debugger.test.adapter.evaluation.EvaluationExceptionKind.IMPORT_RESOLVING_ERROR;
import static org.ballerinalang.debugger.test.adapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_ACCESS;
import static org.ballerinalang.debugger.test.adapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_CLASS;
import static org.ballerinalang.debugger.test.adapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_FUNCTION;
import static org.ballerinalang.debugger.test.adapter.evaluation.EvaluationExceptionKind.QUALIFIED_VARIABLE_RESOLVING_FAILED;

/**
 * Test implementation for debug expression evaluation negative scenarios on Ballerina packages.
 */
public class PackageEvaluationNegativeTest extends ExpressionEvaluationNegativeTest {

    @Override
    protected void prepareForEvaluation() throws BallerinaTestException {
        String testProjectName = "evaluation-tests-1";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 355));
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
        debugTestRunner.assertEvaluationError(context, "other:addition(2,6)",
                String.format(NON_PUBLIC_OR_UNDEFINED_FUNCTION.getString(), "addition", "other"));
        debugTestRunner.assertEvaluationError(context, "other:foo(-6)",
                String.format(NON_PUBLIC_OR_UNDEFINED_FUNCTION.getString(), "foo", "other"));
        debugTestRunner.assertEvaluationError(context, "foo:addition(2,6)", String.format(IMPORT_RESOLVING_ERROR
                .getString(), "foo"));
    }

    @Override
    @Test
    public void newConstructorEvaluationTest() throws BallerinaTestException {
        super.newConstructorEvaluationTest();

        // Accessing non-public classes from other modules
        debugTestRunner.assertEvaluationError(context, "new other:Location(\"New York\",\"USA\")",
                String.format(NON_PUBLIC_OR_UNDEFINED_CLASS.getString(), "Location", "other"));
    }

    @Override
    @Test
    public void typeCastEvaluationTest() throws BallerinaTestException {
        super.typeCastEvaluationTest();

        // qualified literals (i.e. imported modules)
        debugTestRunner.assertEvaluationError(context, "<other:Location> location",
                String.format(NON_PUBLIC_OR_UNDEFINED_ACCESS.getString(), "other", "Location"));
    }

    @Override
    @Test
    public void typeTestEvaluationTest() throws BallerinaTestException {
        super.typeTestEvaluationTest();

        // qualified literals (i.e. imported modules)
        debugTestRunner.assertEvaluationError(context, "location is other:Location",
                String.format(CUSTOM_ERROR.getString(), "compilation error(s) found while creating executables for " +
                        "evaluation: " + System.lineSeparator() + "attempt to refer to non-accessible symbol " +
                        "'Location'" + System.lineSeparator() + "unknown type 'Location'"));
    }

    @Override
    @Test
    public void unaryExpressionEvaluationTest() throws BallerinaTestException {
        super.unaryExpressionEvaluationTest();

        // with qualified literals (i.e. imported modules)
        debugTestRunner.assertEvaluationError(context, "-other:constant",
                String.format(QUALIFIED_VARIABLE_RESOLVING_FAILED.getString(), "other", "constant"));
    }

    @Override
    @Test
    public void nameReferenceEvaluationTest() throws BallerinaTestException {
        super.nameReferenceEvaluationTest();

        // package-private constant evaluation
        debugTestRunner.assertEvaluationError(context, "other:constant",
                String.format(QUALIFIED_VARIABLE_RESOLVING_FAILED.getString(), "other", "constant"));

        // package-private module variable evaluation
        debugTestRunner.assertEvaluationError(context, "other:privateModuleVariable",
                String.format(QUALIFIED_VARIABLE_RESOLVING_FAILED.getString(), "other", "privateModuleVariable"));

        // other qualified references (i.e. types)
        debugTestRunner.assertEvaluationError(context, "other:UndefinedType",
                String.format(QUALIFIED_VARIABLE_RESOLVING_FAILED.getString(), "other", "UndefinedType"));
    }
}
