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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Class to test compound assignment statements.
 */
public class CompoundAssignmentTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/compoundassignment/compound_assignment.bal");
    }

    @Test(description = "Test compound assignment with addition.")
    public void testCompoundAssignmentAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 15);
    }

    @Test(description = "Test compound assignment with subtraction.")
    public void testCompoundAssignmentSubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtraction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -5);
    }

    @Test(description = "Test compound assignment with multiplication.")
    public void testCompoundAssignmentMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplication");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
    }

    @Test(description = "Test compound assignment with division.")
    public void testCompoundAssignmentDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentDivision");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test(description = "Test increment operator.")
    public void testIncrementOperator() {
        BValue[] returns = BRunUtil.invoke(result, "testIncrementOperator");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 101);
    }

    @Test(description = "Test decrement operator.")
    public void testDecrementOperator() {
        BValue[] returns = BRunUtil.invoke(result, "testDecrementOperator");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 99);
    }

    @Test(description = "Test compound assignment with addition on array element.")
    public void testCompoundAssignmentAdditionArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 110);
    }

    @Test(description = "Test compound assignment with subtraction on array element.")
    public void testCompoundAssignmentSubtractionArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtractionArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 90);
    }

    @Test(description = "Test compound assignment with multiplication on array element.")
    public void testCompoundAssignmentMultiplicationArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplicationArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1000);
    }

    @Test(description = "Test compound assignment with division on array element.")
    public void testCompoundAssignmentDivisionArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentDivisionArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test(description = "Test compound assignment with addition on struct element.")
    public void testCompoundAssignmentAdditionStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 110);
    }

    @Test(description = "Test compound assignment with subtraction on struct element.")
    public void testCompoundAssignmentSubtractionStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtractionStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 90);
    }

    @Test(description = "Test compound assignment with multiplication on struct element.")
    public void testCompoundAssignmentMultiplicationStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplicationStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1000);
    }

    @Test(description = "Test compound assignment with division on struct element.")
    public void testCompoundAssignmentDivisionStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentDivisionStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test(description = "Test increment operator on struct element.")
    public void testIncrementOperatorArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testIncrementOperatorArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 101);
    }

    @Test(description = "Test decrement operator on array element.")
    public void testDecrementOperatorArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testDecrementOperatorArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 99);
    }

    @Test(description = "Test increment operator on struct element.")
    public void testIncrementOperatorStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testIncrementOperatorStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 889);
    }

    @Test(description = "Test increment operator on float.")
    public void testDecrementOperatorStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testDecrementOperatorStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 887);
    }

    @Test(description = "Test increment operator on float.")
    public void testIncrementOperatorFloat() {
        BValue[] returns = BRunUtil.invoke(result, "testIncrementOperatorFloat");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 101.0);
    }

    @Test(description = "Test decrement operator on float.")
    public void testDecrementOperatorFloat() {
        BValue[] returns = BRunUtil.invoke(result, "testDecrementOperatorFloat");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 99.0);
    }

    @Test(description = "Test compound assignment with addition of int and string.")
    public void testStringIntCompoundAssignmentAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testStringIntCompoundAssignmentAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "test5");
    }

    @Test(description = "Test compound assignment with addition of int and float.")
    public void testIntFloatCompoundAssignmentAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testIntFloatCompoundAssignmentAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 7.5);
    }

    @Test(description = "Test compound assignment with addition of xml attribute and string.")
    public void testXMLAttributeWithCompoundAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLAttributeWithCompoundAssignment");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "bar1bar2");
    }

    @Test(description = "Test compound assignment with addition recursive integer reference.")
    public void testCompoundAssignmentAdditionRecursive() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionRecursive");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test(description = "Test compound assignment with addition recursive struct element reference.")
    public void testCompoundAssignmentAdditionStructElementRecursive() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionStructElementRecursive");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 200);
    }

    @Test(description = "Test compound assignment with addition to expression.")
    public void testCompoundAssignmentAdditionWithExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionWithExpression");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 19);
    }

    @Test(description = "Test compound assignment with multiple addition.")
    public void testCompoundAssignmentAdditionMultiple() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionMultiple");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
    }

    @Test(description = "Test compound assignment with multiple addition and increments.")
    public void testCompoundAssignmentAdditionMultipleWithIncrement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionMultipleWithIncrement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 19);
    }

    @Test(description = "Test compound assignment with addition with struct access expression.")
    public void testCompoundAssignmentAdditionWithStructAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionWithStructAccess");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 305);
    }

    @Test(description = "Test compound assignment with addition with function invocation expression.")
    public void testCompoundAssignmentAdditionWithFunctionInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionWithFunctionInvocation");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 205);
    }

    @Test(description = "Test compound assignment with addition with two struct elements.")
    public void testCompoundAssignmentAdditionStructElements() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionStructElements");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 500);
    }

    @Test(description = "Test compound operator negative cases.")
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/statements/compoundassignment/compound_assignment_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 12);
        BAssertUtil.validateError(compileResult, 0,
                "operator '++' cannot be applied to type 'any'", 3, 5);
        BAssertUtil.validateError(compileResult, 1,
                "operator '--' cannot be applied to type 'any'", 11, 5);
        BAssertUtil.validateError(compileResult, 2,
                "operator '++' cannot be applied on variable 'getInt()'", 18, 5);
        BAssertUtil.validateError(compileResult, 3,
                "operator '--' cannot be applied on variable 'getInt()'", 23, 5);
        BAssertUtil.validateError(compileResult, 4,
                "operator '++' cannot be applied to type 'string'", 33, 5);
        BAssertUtil.validateError(compileResult, 5,
                "operator '--' cannot be applied to type 'string'", 39, 5);
//        BAssertUtil.validateError(compileResult, 6,
//                "operator '+' not defined for 'int' and 'int|error'", 45, 5);
        BAssertUtil.validateError(compileResult, 7,
                "invalid assignment in variable 'getInt()'", 51, 5);
        BAssertUtil.validateError(compileResult, 8,
                "operator '+' not defined for 'json' and 'string'", 57, 5);
        BAssertUtil.validateError(compileResult, 9,
                "incompatible types: expected 'int', found 'string'", 63, 10);
        BAssertUtil.validateError(compileResult, 10,
                "incompatible types: expected 'int', found 'float'", 70, 10);
        BAssertUtil.validateError(compileResult, 11,
                "operator '+' not defined for 'int' and '(int,int)'", 76, 5);
    }

}
