/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.decimaltype;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.DecimalValueKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class will test the NaN, Infinity concepts of decimal type. This also includes tests for the decimal
 * builtin functions isNaN, isInfinite and isFinite.
 *
 * @since 0.991
 */
public class BDecimalNaNInfinityTest {
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/decimal/decimal_nan_infinity.bal");
    }

    @Test(description = "Test decimal zero divided by zero")
    public void testZeroDividedByZero() {
        BValue[] returns = BRunUtil.invoke(result, "testZeroDividedByZero", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertTrue(value.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test positive decimal number divided by zero")
    public void testPositiveNumberDividedByZero() {
        BValue[] returns = BRunUtil.invoke(result, "testPositiveNumberDividedByZero", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertTrue(value.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
    }

    @Test(description = "Test negative decimal number divided by zero")
    public void testNegativeNumberDividedByZero() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeNumberDividedByZero", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertTrue(value.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
    }

    @Test(description = "Test addition with LHS operand zero")
    public void testZeroAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testZeroAddition", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        for (int i = 0; i < 3; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test subtraction with LHS operand zero")
    public void testZeroSubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testZeroSubtraction", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        for (int i = 0; i < 3; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test multiplication with LHS operand zero")
    public void testZeroMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testZeroMultiplication", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        for (int i = 0; i < 3; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
            BDecimal value = (BDecimal) returns[i];
            Assert.assertTrue(value.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        }
    }

    @Test(description = "Test division with LHS operand zero")
    public void testZeroDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testZeroDivision", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        for (int i = 0; i < 4; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.ZERO, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.ZERO, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test modulo with LHS operand zero")
    public void testZeroModulo() {
        BValue[] returns = BRunUtil.invoke(result, "testZeroModulo", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        for (int i = 0; i < 4; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.ZERO, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.ZERO, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test addition with LHS operand positive infinity")
    public void testPositiveInfinityAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testPositiveInfinityAddition", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        for (int i = 0; i < 4; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test subtraction with LHS operand positive infinity")
    public void testPositiveInfinitySubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testPositiveInfinitySubtraction", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        for (int i = 0; i < 4; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test multiplication with LHS operand positive infinity")
    public void testPositiveInfinityMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testPositiveInfinityMultiplication", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        BDecimal value5 = (BDecimal) returns[4];
        BDecimal value6 = (BDecimal) returns[5];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value5.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value6.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test division with LHS operand positive infinity")
    public void testPositiveInfinityDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testPositiveInfinityDivision", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        BDecimal value5 = (BDecimal) returns[4];
        BDecimal value6 = (BDecimal) returns[5];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value5.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value6.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test modulo with LHS operand positive infinity")
    public void testPositiveInfinityModulo() {
        BValue[] returns = BRunUtil.invoke(result, "testPositiveInfinityModulo", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
            BDecimal value = (BDecimal) returns[i];
            Assert.assertTrue(value.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        }
    }

    @Test(description = "Test addition with LHS operand negative infinity")
    public void testNegativeInfinityAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeInfinityAddition", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        for (int i = 0; i < 4; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test subtraction with LHS operand negative infinity")
    public void testNegativeInfinitySubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeInfinitySubtraction", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        for (int i = 0; i < 4; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test multiplication with LHS operand negative infinity")
    public void testNegativeInfinityMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeInfinityMultiplication", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        BDecimal value5 = (BDecimal) returns[4];
        BDecimal value6 = (BDecimal) returns[5];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value5.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value6.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test division with LHS operand negative infinity")
    public void testNegativeInfinityDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeInfinityDivision", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        BDecimal value4 = (BDecimal) returns[3];
        BDecimal value5 = (BDecimal) returns[4];
        BDecimal value6 = (BDecimal) returns[5];
        Assert.assertTrue(value1.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value2.valueKind == DecimalValueKind.POSITIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value3.valueKind == DecimalValueKind.NEGATIVE_INFINITY, "Invalid decimal value returned.");
        Assert.assertTrue(value4.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value5.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        Assert.assertTrue(value6.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
    }

    @Test(description = "Test modulo with LHS operand negative infinity")
    public void testNegativeInfinityModulo() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeInfinityModulo", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
            BDecimal value = (BDecimal) returns[i];
            Assert.assertTrue(value.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        }
    }

    @Test(description = "Test mathematical operations with a NaN operand")
    public void testNaNOperations() {
        BValue[] returns = BRunUtil.invoke(result, "testNaNOperations", new BValue[]{});
        Assert.assertEquals(returns.length, 5);
        for (int i = 0; i < 5; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
            BDecimal value = (BDecimal) returns[i];
            Assert.assertTrue(value.valueKind == DecimalValueKind.NOT_A_NUMBER, "Invalid decimal value returned.");
        }
    }

    @Test(description = "Test isNaN builtin function")
    public void testIsNaN() {
        BValue[] returns = BRunUtil.invoke(result, "testIsNaN", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BBoolean.class);
            BBoolean value = (BBoolean) returns[i];
            if (i < 2) {
                Assert.assertTrue(value.booleanValue(), "Invalid boolean value returned.");
            } else {
                Assert.assertFalse(value.booleanValue(), "Invalid boolean value returned.");
            }
        }
    }

    @Test(description = "Test isInfinite function")
    public void testIsInfinite() {
        BValue[] returns = BRunUtil.invoke(result, "testIsInfinite", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BBoolean.class);
            BBoolean value = (BBoolean) returns[i];
            if (i < 3) {
                Assert.assertFalse(value.booleanValue(), "Invalid boolean value returned.");
            } else {
                Assert.assertTrue(value.booleanValue(), "Invalid boolean value returned.");
            }
        }
    }

    @Test(description = "Test isFinite function")
    public void testIsFinite() {
        BValue[] returns = BRunUtil.invoke(result, "testIsFinite", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BBoolean.class);
            BBoolean value = (BBoolean) returns[i];
            if (i < 3) {
                Assert.assertFalse(value.booleanValue(), "Invalid boolean value returned.");
            } else {
                Assert.assertTrue(value.booleanValue(), "Invalid boolean value returned.");
            }
        }
    }
}
