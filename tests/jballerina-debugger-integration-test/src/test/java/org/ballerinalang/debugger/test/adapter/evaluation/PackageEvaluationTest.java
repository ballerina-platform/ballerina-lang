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

/**
 * Test implementation for debug expression evaluation scenarios on Ballerina packages.
 */
public class PackageEvaluationTest extends ExpressionEvaluationTest {

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

        // with qualified literals (i.e. imported modules from the same package)
        debugTestRunner.assertExpression(context, "other:sum(2,6)", "8", "int");
        // with typedesc values as arguments
        debugTestRunner.assertExpression(context, "processTypeDesc(Student)", "evaluation_tests:Student", "typedesc");
    }

    @Override
    @Test
    public void newConstructorEvaluationTest() throws BallerinaTestException {
        super.newConstructorEvaluationTest();

        // with qualified literals (i.e. imported modules from the same package)
        debugTestRunner.assertExpression(context, "new other:Place(\"New York\",\"USA\")", "Place", "object");
    }

    @Override
    @Test
    public void queryExpressionEvaluationTest() throws BallerinaTestException {
        super.queryExpressionEvaluationTest();

        // queries with other module imports
        debugTestRunner.assertExpression(context, "from var student in studentList" +
                        "    where student is other:Kid" +
                        "    select student.firstName + \" \" + student.lastName",
                "string[0]", "array");
    }

    @Override
    @Test
    public void typeCastEvaluationTest() throws BallerinaTestException {
        super.typeCastEvaluationTest();

        // with qualified literals (i.e. imported modules from same package)
        debugTestRunner.assertExpression(context, "<other:Place> location", "Place", "object");
        debugTestRunner.assertExpression(context, "<other:Place> stringVar", "{ballerina}TypeCastError", "error");
    }

    @Override
    @Test
    public void typeTestEvaluationTest() throws BallerinaTestException {
        super.typeTestEvaluationTest();

        // with qualified literals (i.e. imported modules from same package)
        debugTestRunner.assertExpression(context, "location is other:Place", "true", "boolean");
    }

    @Override
    @Test
    public void unaryExpressionEvaluationTest() throws BallerinaTestException {
        super.unaryExpressionEvaluationTest();

        // with qualified literals (i.e. imported modules)
        debugTestRunner.assertExpression(context, "-other:publicInt", "-10", "int");
    }

    @Override
    @Test
    public void nameReferenceEvaluationTest() throws BallerinaTestException {
        super.nameReferenceEvaluationTest();

        // Todo - move to common evaluation test suite after fixing the value string
        debugTestRunner.assertExpression(context, GLOBAL_VAR_03,
                "(debug_test_resources/evaluation_tests:0:$anonType$nameMap$_0 & readonly)",
                "record");

        // qualified variable references (i.e. imported modules)
        debugTestRunner.assertExpression(context, "other:publicConstant", "\"Ballerina\"", "string");
        debugTestRunner.assertExpression(context, "other:publicModuleVariable", "\"public\"", "string");
        debugTestRunner.assertExpression(context, "other:constMap",
                "(debug_test_resources/evaluation_tests.other:0:$anonType$constMap$_0 & readonly)",
                "record");

        // other simple name references (i.e. types)
        debugTestRunner.assertExpression(context, "Student", "evaluation_tests:Student", "typedesc");
        debugTestRunner.assertExpression(context, "AnonPerson", "evaluation_tests:AnonPerson", "typedesc");

        // other qualified name references (i.e. types)
        debugTestRunner.assertExpression(context, "other:Kid", "evaluation_tests.other:Kid", "typedesc");
    }
}
