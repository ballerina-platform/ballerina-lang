/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.types.floattype;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class will test the behaviour of double values with expressions.
 * Addition
 * Multiplication
 * Division
 * Subtraction
 * <p>
 * Defining a double value
 * double b;
 * b = 10.1d;
 */
public class BFloatValueTest {
    private static final double DELTA = 0.01;
    private CompileResult result;
    private CompileResult negativeResult;
    private CompileResult negativeDiscrimination;


    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/float/float-value.bal");
        negativeResult = BCompileUtil.compile("test-src/types/float/float-value-negative.bal");
        negativeDiscrimination = BCompileUtil.compile("test-src/types/float/float-value-negative-discrimination.bal");
    }

    @Test(description = "Test double value assignment")
    public void testFloatValue() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 10.1f, DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test negative double value assignment")
    public void testNegativeFloatValue() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeFloatValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), (-10.1f), DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test double value assignment from a value returned by function")
    public void testFloatValueAssignmentByReturnValue() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatValueAssignmentByReturnValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 10.1d, "Invalid float value returned.");
    }

    @Test(description = "Test double value assignment")
    public void testFloatParameter() {
        BValue[] args = {new BFloat(3.3f)};
        BValue[] returns = BRunUtil.invoke(result, "testFloatParameter", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 3.3f, DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test double value Addition")
    public void testFloatValueAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatAddition", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 20.0d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Subtraction")
    public void testFloatValueSubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatSubtraction", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 10.0d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Multiplication")
    public void testFloatValueMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatMultiplication", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 13.75d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Division")
    public void testFloatValueDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatDivision", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat floatValue = (BFloat) returns[0];
        Assert.assertEquals(floatValue.floatValue(), 5.0d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Division")
    public void testFloatValues() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatValues");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 123.4, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1.234e2, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.4d, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 1.234e2d, "Invalid float value returned.");
    }

    @Test(description = "Test hexadecimal literal")
    public void testHexFloatingPointLiterals() {
        BValue[] returns = BRunUtil.invoke(result, "testHexFloatingPointLiterals");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4779.0, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 8.0, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 5.0, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 12.0, "Invalid float value returned.");
    }

    @Test(description = "Test int literal")
    public void testIntLiteralAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testIntLiteralAssignment");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 12.0, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 15.0, "Invalid float value returned.");
    }

    @Test(description = "Test discriminated float literal")
    public void testDiscriminatedFloatLiterals() {
        BValue[] returns = BRunUtil.invoke(result, "testDiscriminatedFloatLiteral");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.0, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1.0, "Invalid float value returned.");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 2200.0, "Invalid float value returned.");
    }

    @Test()
    public void testIntegerValue() {
        Assert.assertEquals(negativeResult.getErrorCount(), 10);
        BAssertUtil.validateError(negativeResult, 0, "leading zeros in numeric literals", 3, 9);
        BAssertUtil.validateError(negativeResult, 1, "float '999e9999999999' too large", 8, 15);
        BAssertUtil.validateError(negativeResult, 2, "float '999e-9999999999' too small", 9, 15);
        BAssertUtil.validateError(negativeResult, 3, "float '999e9999999999' too large", 10, 23);
        BAssertUtil.validateError(negativeResult, 4, "float '99.9E99999999' too large", 11, 27);
        BAssertUtil.validateError(negativeResult, 5, "float '99.9E-99999999' too small", 12, 27);
        BAssertUtil.validateError(negativeResult, 6, "float '0x9999999p999999999999999999999999' too large", 15, 10);
        BAssertUtil.validateError(negativeResult, 7, "float '0x9999999p-999999999999999999999999' too small", 17, 11);
        BAssertUtil.validateError(negativeResult, 8, "float '9999999999e9999999999999999999f' too large", 19, 10);
        BAssertUtil.validateError(negativeResult, 9, "float '9999999999e-9999999999999999999f' too small", 21, 11);
    }

    @Test(description = "Test float literal discrimination error")
    public void testFloatLiteralDiscriminationError() {
        Assert.assertEquals(negativeDiscrimination.getErrorCount(), 1);
        BAssertUtil.validateError(negativeDiscrimination, 0, "incompatible types: expected 'float', found 'decimal'",
                18, 15);
    }

    @Test(description = "Test hexa decimal literal with float type")
    public void testHexaDecimalLiteralsWithFloat() {
        BRunUtil.invoke(result, "testHexaDecimalLiteralsWithFloat");
    }


    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
        negativeDiscrimination = null;
    }
}
