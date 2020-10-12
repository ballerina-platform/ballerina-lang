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

package org.ballerinalang.debugger.test.adapter.evaluation;

import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test implementation for debug expression evaluation negative scenarios.
 */
public class ExpressionEvaluationNegativeTest extends ExpressionEvaluationBaseTest {

    @BeforeClass
    public void setup() throws BallerinaTestException {
        prepareForEvaluation();
    }

    @Override
    @Test
    public void literalEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void listConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void mappingConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void stringTemplateEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void xmlTemplateEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void newConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void variableReferenceEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void fieldAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void xmlAttributeAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void annotationAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void memberAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void functionCallEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void methodCallEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void errorConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void anonymousFunctionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void letExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void typeCastEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void typeOfExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void unaryExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void multiplicativeExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void additiveExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void shiftExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void rangeExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void comparisonEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void typeTestEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void equalityEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void binaryBitwiseEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void logicalEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void conditionalExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void checkingExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void trapExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void queryExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void xmlNavigationEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Test
    public void expressionEvaluationNegativeTest() throws BallerinaTestException {
        // empty expressions
        assertEvaluationError(context, "  ", EvaluationExceptionKind.EMPTY.getString());
        // unsupported expressions
        assertEvaluationError(context, "~x", String.format(EvaluationExceptionKind.UNSUPPORTED_EXPRESSION
                .getString(), "~x - UNARY_EXPRESSION"));
        // syntactically incorrect expressions (additional semi-colon)
        assertEvaluationError(context, "x + 5;;", String.format(EvaluationExceptionKind.SYNTAX_ERROR
                .getString(), "invalid token ';'"));
        // semantically incorrect expressions (addition between int + string)
        assertEvaluationError(context, String.format("%s + %s", INT_VAR, STRING_VAR),
                String.format(EvaluationExceptionKind.UNSUPPORTED_EXPRESSION.getString(),
                        "'+' operation is not supported for types: 'int' and 'string'"));
        // undefined object methods
        assertEvaluationError(context, OBJECT_VAR + ".undefined()",
                String.format(EvaluationExceptionKind.OBJECT_METHOD_NOT_FOUND.getString(), "undefined"));
        // Todo - Enable
        // assignment statements
        // assertEvaluationError(context, "int x = 5;", "");

        // Todo - Add negative tests for function invocations related errors. (invalid argument validation, etc.).
    }

    @AfterClass
    private void cleanup() {
        terminateDebugSession();
        this.context = null;
    }
}
