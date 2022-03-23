/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.compoundassignment;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXmlSequence;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Class to test compound assignment statements.
 */
public class CompoundAssignmentTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/compoundassignment/compound_assignment.bal");
    }

    @Test(description = "Test compound assignment with addition.")
    public void testCompoundAssignmentAddition() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAddition");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 15L);
    }

    @Test(description = "Test compound assignment with subtraction.")
    public void testCompoundAssignmentSubtraction() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtraction");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, -5L);
    }

    @Test(description = "Test compound assignment with multiplication.")
    public void testCompoundAssignmentMultiplication() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplication");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 50L);
    }

    @Test(description = "Test compound assignment with division.")
    public void testCompoundAssignmentDivision() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentDivision");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test compound assignment with bitwise AND.")
    public void testCompoundAssignmentBitwiseAND() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentBitwiseAND");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 5L);
    }

    @Test(description = "Test compound assignment with bitwise OR.")
    public void testCompoundAssignmentBitwiseOR() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentBitwiseOR");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 15L);
    }

    @Test(description = "Test compound assignment with bitwise XOR.")
    public void testCompoundAssignmentBitwiseXOR() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentBitwiseXOR");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test compound assignment with left shift.")
    public void testCompoundAssignmentLeftShift() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentLeftShift");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 16L);
    }

    @Test(description = "Test compound assignment with right shift.")
    public void testCompoundAssignmentRightShift() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentRightShift");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 4L);
    }

    @Test(description = "Test compound assignment with logical shift.")
    public void testCompoundAssignmentLogicalShift() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentLogicalShift");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 4L);
    }

    @Test(description = "Test compound assignment with addition on array element.")
    public void testCompoundAssignmentAdditionArrayElement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionArrayElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 110L);
    }

    @Test(description = "Test compound assignment with subtraction on array element.")
    public void testCompoundAssignmentSubtractionArrayElement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtractionArrayElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 90L);
    }

    @Test(description = "Test compound assignment with multiplication on array element.")
    public void testCompoundAssignmentMultiplicationArrayElement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplicationArrayElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 1000L);
    }

    @Test(description = "Test compound assignment with division on array element.")
    public void testCompoundAssignmentDivisionArrayElement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentDivisionArrayElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test compound assignment with addition on array element where the index is an invocation.")
    public void testCompoundAssignmentAdditionArrayElementFunctionInvocation() {
        BArray returns =
                (BArray) BRunUtil.invoke(result, "testCompoundAssignmentAdditionArrayElementFunctionInvocation");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(0), 11L);
        Assert.assertEquals(returns.get(1), 1L);
        Assert.assertEquals(returns.get(2), 1L);
    }

    @Test(description = "Test compound assignment with subtraction on array element where the index is an invocation.")
    public void testCompoundAssignmentSubtractionArrayElementFunctionInvocation() {
        BArray returns =
                (BArray) BRunUtil.invoke(result, "testCompoundAssignmentSubtractionArrayElementFunctionInvocation");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(0), 1L);
        Assert.assertEquals(returns.get(1), 1L);
        Assert.assertEquals(returns.get(2), 1L);
    }

    @Test(description = "Test compound assignment with division on array element where the index is an invocation.")
    public void testCompoundAssignmentDivisionArrayElementFunctionInvocation() {
        BArray returns =
                (BArray) BRunUtil.invoke(result, "testCompoundAssignmentDivisionArrayElementFunctionInvocation");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(0), 1L);
        Assert.assertEquals(returns.get(1), 1L);
        Assert.assertEquals(returns.get(2), 1L);
    }

    @Test(description = "Test compound assignment with multiplication on array element where the index is an " +
            "invocation.")
    public void testCompoundAssignmentMultiplicationArrayElementFunctionInvocation() {
        BArray returns = (BArray) BRunUtil.invoke(result,
                "testCompoundAssignmentMultiplicationArrayElementFunctionInvocation");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(0), 30L);
        Assert.assertEquals(returns.get(1), 1L);
        Assert.assertEquals(returns.get(2), 1L);
    }

    @Test(description = "Test execution order of compound assignment on array element where the index is an " +
            "invocation.")
    public void testCompoundAssignmentArrayElementFunctionInvocationOrder() {
        BArray returns =
                (BArray) BRunUtil.invoke(result, "testCompoundAssignmentArrayElementFunctionInvocationOrder");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 30L);
        Assert.assertEquals(returns.get(1), 1L);
        Assert.assertEquals(returns.get(2), 1L);
    }

    @Test(description = "Test compound assignment with addition on struct element.")
    public void testCompoundAssignmentAdditionStructElement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionStructElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 110L);
    }

    @Test(description = "Test compound assignment with subtraction on struct element.")
    public void testCompoundAssignmentSubtractionStructElement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtractionStructElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 90L);
    }

    @Test(description = "Test compound assignment with multiplication on struct element.")
    public void testCompoundAssignmentMultiplicationStructElement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplicationStructElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 1000L);
    }

    @Test(description = "Test compound assignment with division on struct element.")
    public void testCompoundAssignmentDivisionStructElement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentDivisionStructElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test increment operator on struct element.")
    public void testIncrementOperatorArrayElement() {
        Object returns = BRunUtil.invoke(result, "testIncrementOperatorArrayElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 101L);
    }

    @Test(description = "Test decrement operator on array element.")
    public void testDecrementOperatorArrayElement() {
        Object returns = BRunUtil.invoke(result, "testDecrementOperatorArrayElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 99L);
    }

    @Test(description = "Test increment operator on struct element.")
    public void testIncrementOperatorStructElement() {
        Object returns = BRunUtil.invoke(result, "testIncrementOperatorStructElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 889L);
    }

    @Test(description = "Test increment operator on float.")
    public void testDecrementOperatorStructElement() {
        Object returns = BRunUtil.invoke(result, "testDecrementOperatorStructElement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 887L);
    }

    @Test(description = "Test increment operator on float.")
    public void testIncrementOperatorFloat() {
        Object returns = BRunUtil.invoke(result, "testIncrementOperatorFloat");
        
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals(returns, 101.0);
    }

    @Test(description = "Test decrement operator on float.")
    public void testDecrementOperatorFloat() {
        Object returns = BRunUtil.invoke(result, "testDecrementOperatorFloat");
        
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals(returns, 99.0);
    }

    @Test(description = "Test compound assignment with addition of int and string.")
    public void testStringIntCompoundAssignmentAddition() {
        Object returns = BRunUtil.invoke(result, "testStringIntCompoundAssignmentAddition");
        
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "test5");
    }

    @Test(description = "Test compound assignment with addition of int and float.")
    public void testIntFloatCompoundAssignmentAddition() {
        Object returns = BRunUtil.invoke(result, "testIntFloatCompoundAssignmentAddition");
        
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals(returns, 7.5);
    }

    @Test(description = "Test compound assignment with addition of xml attribute and string.")
    public void testXMLAttributeWithCompoundAssignment() {
        Object returns = BRunUtil.invoke(result, "testXMLAttributeWithCompoundAssignment");
        
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "bar1bar2");
    }

    @Test(description = "Test compound assignment with addition recursive integer reference.")
    public void testCompoundAssignmentAdditionRecursive() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionRecursive");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test compound assignment with addition recursive struct element reference.")
    public void testCompoundAssignmentAdditionStructElementRecursive() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionStructElementRecursive");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 200L);
    }

    @Test(description = "Test compound assignment with addition to expression.")
    public void testCompoundAssignmentAdditionWithExpression() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionWithExpression");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 19L);
    }

    @Test(description = "Test compound assignment with multiple addition.")
    public void testCompoundAssignmentAdditionMultiple() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionMultiple");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 20L);
    }

    @Test(description = "Test compound assignment with multiple addition and increments.")
    public void testCompoundAssignmentAdditionMultipleWithIncrement() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionMultipleWithIncrement");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 19L);
    }

    @Test(description = "Test compound assignment with addition with struct access expression.")
    public void testCompoundAssignmentAdditionWithStructAccess() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionWithStructAccess");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 305L);
    }

    @Test(description = "Test compound assignment with addition with function invocation expression.")
    public void testCompoundAssignmentAdditionWithFunctionInvocation() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionWithFunctionInvocation");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 205L);
    }

    @Test(description = "Test compound assignment with addition with two struct elements.")
    public void testCompoundAssignmentAdditionStructElements() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionStructElements");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 500L);
    }

    @Test(description = "Test compound assignment with addition.")
    public void testCompoundAssignmentOfXmlAndString() {
        Object returns = BRunUtil.invoke(result, "xmlCompoundAssignment");
        Assert.assertTrue(returns instanceof BXmlSequence);
        BXmlSequence sequence = (BXmlSequence) returns;
        Assert.assertEquals(sequence.size(), 3);
        Assert.assertEquals(sequence.toString(),
                "hello<hello>hi</hello>hahblah");
    }

    @Test
    public void testCompoundAssignmentAdditionRecordElementRecursive() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionRecordElementRecursive");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 200L);
    }

    @Test
    public void testCompoundAssignmentAdditionRecordElements() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionRecordElements");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 500L);
    }

    @Test
    public void testCompoundAssignmentAdditionWithRecordAccess() {
        Object returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionWithRecordAccess");
        
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 305L);
    }

    @Test(description = "Test compound operator negative cases.")
    public void testCompoundAssignmentNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/statements/compoundassignment/compound_assignment_negative.bal");
        int i = 0;
        Assert.assertEquals(compileResult.getErrorCount(), 42);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands", 5, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '+' not defined for 'any' and 'int'", 5, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands", 13, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '-' not defined for 'any' and 'int'", 13, 5);
        BAssertUtil.validateError(compileResult, i++, "invalid expr in compound assignment lhs", 20, 5);
        BAssertUtil.validateError(compileResult, i++, "invalid expr in compound assignment lhs", 25, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '+' not defined for 'string' and 'int'", 35, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '-' not defined for 'string' and 'int'", 41, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '+' not defined for 'int' and '(int|error)'", 47, 5);
        BAssertUtil.validateError(compileResult, i++, "invalid expr in compound assignment lhs", 53, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands", 59, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '+' not defined for 'json' and 'string'", 59, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '+' not defined for 'int' and 'string'", 65, 5);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected 'float', found 'int'", 73, 12);
        BAssertUtil.validateError(compileResult, i++, "operator '+' not defined for 'int' and '[int,int]'", 78, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '&' not defined for 'int' and 'string'", 90, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '|' not defined for 'int' and 'string'", 96, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '^' not defined for 'int' and 'string'", 102, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '<<' not defined for 'int' and 'string'", 108, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '>>' not defined for 'int' and 'string'", 114, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '>>>' not defined for 'int' and 'string'", 120, 5);
        BAssertUtil.validateError(compileResult, i++, "invalid expr in compound assignment lhs", 126, 5);
        BAssertUtil.validateError(compileResult, i++, "invalid token '='", 140, 17);
        BAssertUtil.validateError(compileResult, i++, "invalid token '='", 141, 17);
        BAssertUtil.validateError(compileResult, i++, "operator '&' not defined for '(int|string)' and 'int'",
                150, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '|' not defined for '(int|string)' and 'int'",
                151, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '^' not defined for '(int|string)' and 'int'",
                152, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '&' not defined for '(int|string)' and 'SomeType'",
                155, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '|' not defined for '(int|string)' and 'SomeType'",
                156, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '^' not defined for '(int|string)' and 'SomeType'",
                157, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '&' not defined for '(int|string)' and 'SomeType2'",
                160, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '|' not defined for '(int|string)' and 'SomeType2'",
                161, 5);
        BAssertUtil.validateError(compileResult, i++, "operator '^' not defined for '(int|string)' and 'SomeType2'",
                162, 5);
        BAssertUtil.validateError(compileResult, i++, "invalid operation: type 'map<int>?' does not support" +
                        " member access for assignment", 167, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands",
                170, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands",
                173, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands",
                176, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands",
                177, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands",
                180, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands",
                184, 5);
        BAssertUtil.validateError(compileResult, i++, "compound assignment not allowed with nullable operands",
                193, 5);
        BAssertUtil.validateError(compileResult, i, "invalid operation: type 'MyUnion' does not support " +
                        "member access for assignment", 196, 5);
    }

    @Test(dataProvider = "dataToTestCompoundAssignmentBinaryOpsWithTypes", description = "Test compound assignment " +
            "binary operations with types")
    public void testCompoundAssignmentBinaryOpsWithTypes(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestCompoundAssignmentBinaryOpsWithTypes() {
        return new Object[]{
                "testCompoundAssignmentAdditionWithTypes",
                "testCompoundAssignmentSubtractionWithTypes",
                "testCompoundAssignmentMultiplicationWithTypes",
                "testCompoundAssignmentDivisionWithTypes",
                "testCompoundAssignmentBitwiseLeftShift",
                "testCompoundAssignmentBitwiseRightShift",
                "testCompoundAssignmentBitwiseUnsignedRightShift"
        };
    }

    @Test(description = "Test compound assignment ")
    public void testCompoundAssignmentDataflowAnalysisNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/statements/compoundassignment/compound_assignment_dataflow_analysis_negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 2);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "cannot assign a value to final 'i'", 20, 5);
        BAssertUtil.validateError(negativeResult, i, "cannot assign a value to final 'j'", 23, 5);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
