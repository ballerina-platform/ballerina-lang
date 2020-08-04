/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.formatter.core.expressions;

import org.testng.annotations.Test;

/**
 * Test formatting for expressions.
 *
 * @since 2.0.0
 */
public class ExpressionTestImpl extends AbstractExpressionTest {

    @Test(description = "Test the formatting of constant expressions")
    public void testConstantExpressions() {
        testFile("source/constant-expressions.bal", "expected/constant-expressions.bal");
    }

    @Test(description = "Test the formatting of literal expressions")
    public void testLiteralExpressions() {
        testFile("source/literal-expressions.bal", "expected/literal-expressions.bal");
    }

    @Test(description = "Test the formatting of template expressions")
    public void testTemplateExpressions() {
        testFile("source/template-expressions.bal", "expected/template-expressions.bal");
    }

    @Test(description = "Test the formatting of structural constructor expressions")
    public void testStructuralConstructorExpressions() {
        testFile("source/structural-constructor-expressions.bal", "expected/structural-constructor-expressions.bal");
    }

    @Test(description = "Test the formatting of object constructor expressions")
    public void testObjectConstructorExpressions() {
        testFile("source/object-constructor-expressions.bal", "expected/object-constructor-expressions.bal");
    }

    @Test(description = "Test the formatting of service constructor expressions")
    public void testServiceConstructorExpressions() {
        testFile("source/service-constructor-expressions.bal", "expected/service-constructor-expressions.bal");
    }

    @Test(description = "Test the formatting of variable reference expressions")
    public void testVariableReferenceExpressions() {
        testFile("source/variable-reference-expressions.bal", "expected/variable-reference-expressions.bal");
    }

    @Test(description = "Test the formatting of field access expressions")
    public void testFieldAccessExpressions() {
        testFile("source/field-access-expressions.bal", "expected/field-access-expressions.bal");
    }

    @Test(description = "Test the formatting of optional field access expressions")
    public void testOptionalFieldAccessExpressions() {
        testFile("source/optional-field-access-expressions.bal", "expected/optional-field-access-expressions.bal");
    }

    @Test(description = "Test the formatting of XML attribute access expressions")
    public void testXMLAttributeAccessExpressions() {
        testFile("source/xml-attribute-access-expressions.bal", "expected/xml-attribute-access-expressions.bal");
    }

    @Test(description = "Test the formatting of annotation access expressions")
    public void testAnnotationAccessExpressions() {
        testFile("source/annotation-access-expressions.bal", "expected/annotation-access-expressions.bal");
    }

    @Test(description = "Test the formatting of member access expressions")
    public void testMemberAccessExpressions() {
        testFile("source/member-access-expressions.bal", "expected/member-access-expressions.bal");
    }

    @Test(description = "Test the formatting of function call expressions")
    public void testFunctionCallExpressions() {
        testFile("source/function-call-expressions.bal", "expected/function-call-expressions.bal");
    }

    @Test(description = "Test the formatting of method call expressions")
    public void testMethodCallExpressions() {
        testFile("source/method-call-expressions.bal", "expected/method-call-expressions.bal");
    }

    @Test(description = "Test the formatting of functional constructor expressions")
    public void testFunctionalConstructorExpressions() {
        testFile("source/functional-constructor-expressions.bal", "expected/functional-constructor-expressions.bal");
    }

    @Test(description = "Test the formatting of anonymous function expressions")
    public void testAnonymousFunctionExpressions() {
        testFile("source/anonymous-function-expressions.bal", "expected/anonymous-function-expressions.bal");
    }

    @Test(description = "Test the formatting of let expressions")
    public void testLetExpressions() {
        testFile("source/let-expressions.bal", "expected/let-expressions.bal");
    }

    @Test(description = "Test the formatting of type cast expressions")
    public void testTypeCastExpressions() {
        testFile("source/type-cast-expressions.bal", "expected/type-cast-expressions.bal");
    }

    @Test(description = "Test the formatting of type of expressions")
    public void testTypeOfExpressions() {
        testFile("source/type-of-expressions.bal", "expected/type-of-expressions.bal");
    }

    @Test(description = "Test the formatting of unary expressions")
    public void testUnaryExpressions() {
        testFile("source/unary-expressions.bal", "expected/unary-expressions.bal");
    }

    @Test(description = "Test the formatting of multiplicative expressions")
    public void testMultiplicativeExpressions() {
        testFile("source/multiplicative-expressions.bal", "expected/multiplicative-expressions.bal");
    }

    @Test(description = "Test the formatting of additive expressions")
    public void testAdditiveExpressions() {
        testFile("source/additive-expressions.bal", "expected/additive-expressions.bal");
    }

    @Test(description = "Test the formatting of shift expressions")
    public void testShiftExpressions() {
        testFile("source/shift-expressions.bal", "expected/shift-expressions.bal");
    }

    @Test(description = "Test the formatting of range expressions")
    public void testRangeExpressions() {
        testFile("source/range-expressions.bal", "expected/range-expressions.bal");
    }

    @Test(description = "Test the formatting of numerical comparison expressions")
    public void testNumericalComparisonExpressions() {
        testFile("source/numerical-comparison-expressions.bal", "expected/numerical-comparison-expressions.bal");
    }

    @Test(description = "Test the formatting of type test expressions")
    public void testTypeTestExpressions() {
        testFile("source/type-test-expressions.bal", "expected/type-test-expressions.bal");
    }

    @Test(description = "Test the formatting of equality expressions")
    public void testEqualityExpressions() {
        testFile("source/equality-expressions.bal", "expected/equality-expressions.bal");
    }

    @Test(description = "Test the formatting of binary bitwise expressions")
    public void testBinaryBitwiseExpressions() {
        testFile("source/binary-bitwise-expressions.bal", "expected/binary-bitwise-expressions.bal");
    }

    @Test(description = "Test the formatting of logical expressions")
    public void testLogicalExpressions() {
        testFile("source/logical-expressions.bal", "expected/logical-expressions.bal");
    }

    @Test(description = "Test the formatting of conditional expressions")
    public void testConditionalExpressions() {
        testFile("source/conditional-expressions.bal", "expected/conditional-expressions.bal");
    }

    @Test(description = "Test the formatting of checking expressions")
    public void testCheckingExpressions() {
        testFile("source/checking-expressions.bal", "expected/checking-expressions.bal");
    }

    @Test(description = "Test the formatting of trap expressions")
    public void testTrapExpressions() {
        testFile("source/trap-expressions.bal", "expected/trap-expressions.bal");
    }

    @Test(description = "Test the formatting of XML navigation expressions")
    public void testXMLNavigationExpressions() {
        testFile("source/xml-navigation-expressions.bal", "expected/xml-navigation-expressions.bal");
    }
}
