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
package org.ballerinalang.formatter.core;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Test formatting for expressions.
 *
 * @since 2.0.0
 */
public class ExpressionsTest {

    private void testFile(String sourceFilePath, String filePath) throws IOException {
        FormatterTestUtils.test(Paths.get("expressions/", sourceFilePath), Paths.get("expressions/", filePath));
    }

    @Test(description = "Test the formatting of constant expressions")
    public void testConstantExpressions() throws IOException {
        testFile("source/constant-expressions.bal", "expected/constant-expressions.bal");
    }

    @Test(description = "Test the formatting of literal expressions")
    public void testLiteralExpressions() throws IOException {
        testFile("source/literal-expressions.bal", "expected/literal-expressions.bal");
    }

    @Test(description = "Test the formatting of template expressions")
    public void testTemplateExpressions() throws IOException {
        testFile("source/template-expressions.bal", "expected/template-expressions.bal");
    }

    @Test(description = "Test the formatting of structural constructor expressions")
    public void testStructuralConstructorExpressions() throws IOException {
        testFile("source/structural-constructor-expressions.bal", "expected/structural-constructor-expressions.bal");
    }

    @Test(description = "Test the formatting of object constructor expressions")
    public void testObjectConstructorExpressions() throws IOException {
        testFile("source/object-constructor-expressions.bal", "expected/object-constructor-expressions.bal");
    }

    @Test(description = "Test the formatting of service constructor expressions")
    public void testServiceConstructorExpressions() throws IOException {
        testFile("source/service-constructor-expressions.bal", "expected/service-constructor-expressions.bal");
    }

    @Test(description = "Test the formatting of variable reference expressions")
    public void testVariableReferenceExpressions() throws IOException {
        testFile("source/variable-reference-expressions.bal", "expected/variable-reference-expressions.bal");
    }

    @Test(description = "Test the formatting of field access expressions")
    public void testFieldAccessExpressions() throws IOException {
        testFile("source/field-access-expressions.bal", "expected/field-access-expressions.bal");
    }

    @Test(description = "Test the formatting of optional field access expressions")
    public void testOptionalFieldAccessExpressions() throws IOException {
        testFile("source/optional-field-access-expressions.bal", "expected/optional-field-access-expressions.bal");
    }

    @Test(description = "Test the formatting of XML attribute access expressions")
    public void testXMLAttributeAccessExpressions() throws IOException {
        testFile("source/xml-attribute-access-expressions.bal", "expected/xml-attribute-access-expressions.bal");
    }

    @Test(description = "Test the formatting of annotation access expressions")
    public void testAnnotationAccessExpressions() throws IOException {
        testFile("source/annotation-access-expressions.bal", "expected/annotation-access-expressions.bal");
    }

    @Test(description = "Test the formatting of member access expressions")
    public void testMemberAccessExpressions() throws IOException {
        testFile("source/member-access-expressions.bal", "expected/member-access-expressions.bal");
    }

    @Test(description = "Test the formatting of function call expressions")
    public void testFunctionCallExpressions() throws IOException {
        testFile("source/function-call-expressions.bal", "expected/function-call-expressions.bal");
    }

    @Test(description = "Test the formatting of method call expressions")
    public void testMethodCallExpressions() throws IOException {
        testFile("source/method-call-expressions.bal", "expected/method-call-expressions.bal");
    }

    @Test(description = "Test the formatting of functional constructor expressions")
    public void testFunctionalConstructorExpressions() throws IOException {
        testFile("source/functional-constructor-expressions.bal", "expected/functional-constructor-expressions.bal");
    }

    @Test(description = "Test the formatting of anonymous function expressions")
    public void testAnonymousFunctionExpressions() throws IOException {
        testFile("source/anonymous-function-expressions.bal", "expected/anonymous-function-expressions.bal");
    }

    @Test(description = "Test the formatting of let expressions")
    public void testLetExpressions() throws IOException {
        testFile("source/let-expressions.bal", "expected/let-expressions.bal");
    }

    @Test(description = "Test the formatting of type cast expressions")
    public void testTypeCastExpressions() throws IOException {
        testFile("source/type-cast-expressions.bal", "expected/type-cast-expressions.bal");
    }

    @Test(description = "Test the formatting of type of expressions")
    public void testTypeOfExpressions() throws IOException {
        testFile("source/type-of-expressions.bal", "expected/type-of-expressions.bal");
    }

    @Test(description = "Test the formatting of unary expressions")
    public void testUnaryExpressions() throws IOException {
        testFile("source/unary-expressions.bal", "expected/unary-expressions.bal");
    }

    @Test(description = "Test the formatting of multiplicative expressions")
    public void testMultiplicativeExpressions() throws IOException {
        testFile("source/multiplicative-expressions.bal", "expected/multiplicative-expressions.bal");
    }

    @Test(description = "Test the formatting of additive expressions")
    public void testAdditiveExpressions() throws IOException {
        testFile("source/additive-expressions.bal", "expected/additive-expressions.bal");
    }

    @Test(description = "Test the formatting of shift expressions")
    public void testShiftExpressions() throws IOException {
        testFile("source/shift-expressions.bal", "expected/shift-expressions.bal");
    }

    @Test(description = "Test the formatting of range expressions")
    public void testRangeExpressions() throws IOException {
        testFile("source/range-expressions.bal", "expected/range-expressions.bal");
    }

    @Test(description = "Test the formatting of numerical comparison expressions")
    public void testNumericalComparisonExpressions() throws IOException {
        testFile("source/numerical-comparison-expressions.bal", "expected/numerical-comparison-expressions.bal");
    }

    @Test(description = "Test the formatting of type test expressions")
    public void testTypeTestExpressions() throws IOException {
        testFile("source/type-test-expressions.bal", "expected/type-test-expressions.bal");
    }

    @Test(description = "Test the formatting of equality expressions")
    public void testEqualityExpressions() throws IOException {
        testFile("source/equality-expressions.bal", "expected/equality-expressions.bal");
    }

    @Test(description = "Test the formatting of binary bitwise expressions")
    public void testBinaryBitwiseExpressions() throws IOException {
        testFile("source/binary-bitwise-expressions.bal", "expected/binary-bitwise-expressions.bal");
    }

    @Test(description = "Test the formatting of logical expressions")
    public void testLogicalExpressions() throws IOException {
        testFile("source/logical-expressions.bal", "expected/logical-expressions.bal");
    }

    @Test(description = "Test the formatting of conditional expressions")
    public void testConditionalExpressions() throws IOException {
        testFile("source/conditional-expressions.bal", "expected/conditional-expressions.bal");
    }

    @Test(description = "Test the formatting of checking expressions")
    public void testCheckingExpressions() throws IOException {
        testFile("source/checking-expressions.bal", "expected/checking-expressions.bal");
    }

    @Test(description = "Test the formatting of trap expressions")
    public void testTrapExpressions() throws IOException {
        testFile("source/trap-expressions.bal", "expected/trap-expressions.bal");
    }

    @Test(description = "Test the formatting of XML navigation expressions")
    public void testXMLNavigationExpressions() throws IOException {
        testFile("source/xml-navigation-expressions.bal", "expected/xml-navigation-expressions.bal");
    }
}
