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

import io.ballerina.runtime.api.values.BArray;
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
        Object returns = BRunUtil.invoke(result, "testFloatValue", new Object[]{});

        Assert.assertSame(returns.getClass(), Double.class);
        double floatValue = (double) returns;
        Assert.assertEquals(floatValue, 10.1f, DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test negative double value assignment")
    public void testNegativeFloatValue() {
        Object returns = BRunUtil.invoke(result, "testNegativeFloatValue", new Object[]{});

        Assert.assertSame(returns.getClass(), Double.class);
        double floatValue = (double) returns;
        Assert.assertEquals(floatValue, (-10.1f), DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test double value assignment from a value returned by function")
    public void testFloatValueAssignmentByReturnValue() {
        Object returns = BRunUtil.invoke(result, "testFloatValueAssignmentByReturnValue", new Object[]{});

        Assert.assertSame(returns.getClass(), Double.class);
        double floatValue = (double) returns;
        Assert.assertEquals(floatValue, 10.1d, "Invalid float value returned.");
    }

    @Test(description = "Test double value assignment")
    public void testFloatParameter() {
        Object[] args = {(3.3f)};
        Object returns = BRunUtil.invoke(result, "testFloatParameter", args);

        Assert.assertSame(returns.getClass(), Double.class);
        double floatValue = (double) returns;
        Assert.assertEquals(floatValue, 3.3f, DELTA, "Invalid float value returned.");
    }

    @Test(description = "Test double value Addition")
    public void testFloatValueAddition() {
        Object returns = BRunUtil.invoke(result, "testFloatAddition", new Object[]{});

        Assert.assertSame(returns.getClass(), Double.class);
        double floatValue = (double) returns;
        Assert.assertEquals(floatValue, 20.0d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Subtraction")
    public void testFloatValueSubtraction() {
        Object returns = BRunUtil.invoke(result, "testFloatSubtraction", new Object[]{});

        Assert.assertSame(returns.getClass(), Double.class);
        double floatValue = (double) returns;
        Assert.assertEquals(floatValue, 10.0d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Multiplication")
    public void testFloatValueMultiplication() {
        Object returns = BRunUtil.invoke(result, "testFloatMultiplication", new Object[]{});

        Assert.assertSame(returns.getClass(), Double.class);
        double floatValue = (double) returns;
        Assert.assertEquals(floatValue, 13.75d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Division")
    public void testFloatValueDivision() {
        Object returns = BRunUtil.invoke(result, "testFloatDivision", new Object[]{});

        Assert.assertSame(returns.getClass(), Double.class);
        double floatValue = (double) returns;
        Assert.assertEquals(floatValue, 5.0d, "Invalid float value returned.");
    }

    @Test(description = "Test double value Division")
    public void testFloatValues() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testFloatValues");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0), 123.4, "Invalid float value returned.");
        Assert.assertEquals(returns.get(1), 1.234e2, "Invalid float value returned.");
        Assert.assertEquals(returns.get(2), 123.4d, "Invalid float value returned.");
        Assert.assertEquals(returns.get(3), 1.234e2d, "Invalid float value returned.");
    }

    @Test(description = "Test hexadecimal literal")
    public void testHexFloatingPointLiterals() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testHexFloatingPointLiterals");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0), 4779.0, "Invalid float value returned.");
        Assert.assertEquals(returns.get(1), 8.0, "Invalid float value returned.");
        Assert.assertEquals(returns.get(2), 5.0, "Invalid float value returned.");
        Assert.assertEquals(returns.get(3), 12.0, "Invalid float value returned.");
    }

    @Test(description = "Test int literal")
    public void testIntLiteralAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testIntLiteralAssignment");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0), 12.0, "Invalid float value returned.");
        Assert.assertEquals(returns.get(1), 15.0, "Invalid float value returned.");
    }

    @Test(description = "Test discriminated float literal")
    public void testDiscriminatedFloatLiterals() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDiscriminatedFloatLiteral");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 1.0, "Invalid float value returned.");
        Assert.assertEquals(returns.get(1), 1.0, "Invalid float value returned.");
        Assert.assertEquals(returns.get(2), 2200.0, "Invalid float value returned.");
    }

    @Test()
    public void testIntegerValue() {
        Assert.assertEquals(negativeResult.getErrorCount(), 17);
        BAssertUtil.validateError(negativeResult, 0, "leading zeros in numeric literals", 3, 9);
        BAssertUtil.validateError(negativeResult, 1, "float '999e9999999999' too large", 8, 15);
        BAssertUtil.validateError(negativeResult, 2, "float '999e-9999999999' too small", 9, 15);
        BAssertUtil.validateError(negativeResult, 3, "float '999e9999999999' too large", 10, 23);
        BAssertUtil.validateError(negativeResult, 4, "float '99.9E99999999' too large", 11, 27);
        BAssertUtil.validateError(negativeResult, 5, "float '99.9E-99999999' too small", 12, 27);
        BAssertUtil.validateError(negativeResult, 6, "float '0x9999999p999999999999999999999999' too large", 15, 10);
        BAssertUtil.validateError(negativeResult, 7, "float '0x9999999p-999999999999999999999999' too small", 17, 11);
        BAssertUtil.validateError(negativeResult, 8, "float '9999999999e9999999999999999999' too large", 19, 10);
        BAssertUtil.validateError(negativeResult, 9, "float '9999999999e-9999999999999999999' too small", 21, 11);
        BAssertUtil.validateError(negativeResult, 10, "float '0x999.9p999999999999999' too large", 23, 1);
        BAssertUtil.validateError(negativeResult, 11, "float '0x999.9p999999999999999' too large", 23, 29);
        BAssertUtil.validateError(negativeResult, 12, "float '9.99E6111' too large", 25, 16);
        BAssertUtil.validateError(negativeResult, 13, "float '9.99E+6111' too large", 28, 15);
        BAssertUtil.validateError(negativeResult, 14, "float '9.99E6111' too large", 29, 5);
        BAssertUtil.validateError(negativeResult, 15, "float '9.99E+6111' too large", 29, 21);
        BAssertUtil.validateError(negativeResult, 16, "float '9.99E+6111' too large", 30, 15);
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

    @Test
    public void testInvalidValuesWithFloatType() {
        CompileResult result = BCompileUtil.compile("test-src/types/float/float_type_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xFFFFFFFFFFFFFFFF' too large", 2, 15);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xabc435de769FEAB0' too large", 4, 9);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xaaaaaaaaaaaaaaa0' too large", 6, 9);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xAAAAAAAAAAAAAAA0' too large", 8, 9);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xFFFFFFFFFFFFFFFF' too large", 10, 23);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xabc435de769FEAB0' too large", 12, 9);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xaaaaaaaaaaaaaaa0' too large", 14, 9);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xAAAAAAAAAAAAAAA0' too large", 16, 9);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xFFFFFFFFFFFFFFFF' too large", 19, 12);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xabc435de769FEAB0' too large", 21, 12);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xaaaaaaaaaaaaaaa0' too large", 23, 12);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xAAAAAAAAAAAAAAA0' too large", 25, 12);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xFFFFFFFFFFFFFFFF' too large", 27, 20);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xabc435de769FEAB0' too large", 29, 20);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xaaaaaaaaaaaaaaa0' too large", 31, 20);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xAAAAAAAAAAAAAAA0' too large", 33, 20);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xFFFFFFFFFFFFFFFF' too large", 37, 16);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xFFFFFFFFFFFFFFFF' too large", 41, 17);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0xFFFFFFFFFFFFFFFF' too large", 44, 20);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0Xffffffffffffffff' too large", 45, 20);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0XFFFFFFFFFFFFFFFF' too large", 46, 21);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0x' too large", 48, 16);
        BAssertUtil.validateError(result, i++, "missing hex number after hex indicator", 48, 16);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0X' too large", 49, 10);
        BAssertUtil.validateError(result, i++, "missing hex number after hex indicator", 49, 10);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0x' too large", 51, 20);
        BAssertUtil.validateError(result, i++, "missing hex number after hex indicator", 51, 20);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0x' too large", 53, 24);
        BAssertUtil.validateError(result, i++, "missing hex number after hex indicator", 53, 24);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0X' too large", 54, 10);
        BAssertUtil.validateError(result, i++, "missing hex number after hex indicator", 54, 10);
        BAssertUtil.validateError(result, i++, "Hexadecimal '0x' too large", 56, 21);
        BAssertUtil.validateError(result, i++, "missing hex number after hex indicator", 56, 21);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
        negativeDiscrimination = null;
    }
}
