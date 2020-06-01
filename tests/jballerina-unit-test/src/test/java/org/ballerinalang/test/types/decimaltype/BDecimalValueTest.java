/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.core.model.util.DecimalValueKind;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * This test class will test the behaviour of decimal values with expressions.
 * Addition
 * Multiplication
 * Division
 * Subtraction
 * Modulus
 * Negation
 * <p>
 * Defining a decimal value
 * Decimal d;
 * d = new Decimal("10.234");
 *
 * @since 0.985.0
 */
public class BDecimalValueTest {

    private static final BigDecimal DELTA = new BigDecimal("1e-10", MathContext.DECIMAL128);
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/decimal/decimal_value.bal");
    }

    @Test(description = "Test positive decimal value assignment")
    public void testDecimalValue() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertEquals(value.decimalValue().compareTo(new BigDecimal("10.1", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test negative decimal value assignment")
    public void testNegativeDecimalValue() {
        BValue[] returns = BRunUtil.invoke(result, "testNegativeDecimalValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertEquals(value.decimalValue().compareTo(new BigDecimal("-10.1", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal value assignment by a return value")
    public void testDecimalValueAssignmentByReturnValue() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalValueAssignmentByReturnValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertEquals(value.decimalValue().compareTo(new BigDecimal("10.1", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal addition")
    public void testDecimalAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertEquals(value.decimalValue().compareTo(new BigDecimal("7.665", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal subtraction")
    public void testDecimalSubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalSubtraction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertEquals(value.decimalValue().compareTo(new BigDecimal("1.465", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal multiplication")
    public void testDecimalMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalMultiplication");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertEquals(value.decimalValue().compareTo(new BigDecimal("14.1515", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal division")
    public void testDecimalDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalDivision");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        BigDecimal actual = value.decimalValue();
        BigDecimal expected = new BigDecimal("1.47258064516", MathContext.DECIMAL128);
        // Check whether the difference between actual and expected is less than DELTA, which is a very small number
        Assert.assertTrue(actual.subtract(expected, MathContext.DECIMAL128).compareTo(DELTA) < 0,
                          "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal modulus")
    public void testDecimalModulus() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalModulus");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertEquals(value.decimalValue().compareTo(new BigDecimal("1.465", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal negation")
    public void testDecimalNegation() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalNegation");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        BDecimal value = (BDecimal) returns[0];
        Assert.assertEquals(value.decimalValue().compareTo(new BigDecimal("-4.565", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal comparison operations")
    public void testDecimalComparisonOperations() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalComparisonOperations");
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BBoolean.class);
            BBoolean val = (BBoolean) returns[i];
            if (i % 2 == 0) {
                Assert.assertFalse(val.booleanValue(), "Invalid boolean value returned.");
            } else {
                Assert.assertTrue(val.booleanValue(), "Invalid boolean value returned.");
            }
        }
    }

    @Test(description = "Test decimal value passed as a parameter")
    public void testDecimalParameter() {
        BigDecimal decimalArg1 = new BigDecimal("343.342", MathContext.DECIMAL128);
        BigDecimal decimalArg2 = new BigDecimal("-21.2", MathContext.DECIMAL128);
        BValue[] args = {new BDecimal(decimalArg1), new BDecimal(decimalArg2)};
        BValue[] returns = BRunUtil.invoke(result, "testDecimalParameter", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        Assert.assertEquals(value1.decimalValue().compareTo(new BigDecimal("343.342", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
        Assert.assertEquals(value2.decimalValue().compareTo(new BigDecimal("-21.2", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test assigning an int literal to a decimal variable")
    public void testIntLiteralAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testIntLiteralAssignment");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        Assert.assertEquals(value1.decimalValue().compareTo(new BigDecimal("12", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
        Assert.assertEquals(value2.decimalValue().compareTo(new BigDecimal("15", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test positively signed literal assignment")
    public void testPositivelySignedLiteralAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testPositivelySignedLiteralAssignment");
        Assert.assertEquals(returns.length, 3);
        for (BValue aReturn : returns) {
            Assert.assertSame(aReturn.getClass(), BDecimal.class);
        }
        BDecimal value1 = (BDecimal) returns[0];
        BDecimal value2 = (BDecimal) returns[1];
        BDecimal value3 = (BDecimal) returns[2];
        Assert.assertEquals(value1.decimalValue().compareTo(new BigDecimal("12.23", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
        Assert.assertEquals(value2.decimalValue().compareTo(new BigDecimal("0.0", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
        Assert.assertEquals(value3.decimalValue().compareTo(new BigDecimal("+0.00", MathContext.DECIMAL128)), 0,
                            "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal array literal without decimal discriminator")
    public void testDecimalArrayLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalArrayValue", new BValue[]{});
        Assert.assertEquals(returns[0], new BDecimal("1.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[1], new BDecimal("2.0", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal array literal with decimal discriminator")
    public void testDiscriminatedDecimalArrayLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalArrayValueWithDiscriminator", new BValue[]{});
        Assert.assertEquals(returns[0], new BDecimal("1.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[1], new BDecimal("2.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[2], new BDecimal("3000.0", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal array literal with decimal discriminator")
    public void testDiscriminatedDecimalLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testDiscriminatedDecimalLiterals", new BValue[]{});
        Assert.assertEquals(returns[0], new BDecimal("3.22", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[1], new BDecimal("0.0", DecimalValueKind.ZERO));
        Assert.assertEquals(returns[2], new BDecimal("3.141592653589793238462643383279502", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[3], new BDecimal("3.141592653589793238462643383279503", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal inference for binary literal expressions")
    public void testDecimalTypeInferenceInBinaryLiteralExpressions() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalInferenceInMapContext", new BValue[]{});
        BMap<String, BDecimal> map = (BMap<String, BDecimal>) returns[0];

        Assert.assertEquals(map.get("a"), new BDecimal("33.3", DecimalValueKind.OTHER));
        Assert.assertEquals(map.get("b"), new BDecimal("33.3", DecimalValueKind.OTHER));
        Assert.assertEquals(map.get("c"), new BDecimal("0.1", DecimalValueKind.OTHER));
        Assert.assertEquals(map.get("d"), new BDecimal("1", DecimalValueKind.OTHER));
        Assert.assertEquals(map.get("e"), new BDecimal("10000000000000000000000.123", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal inference on binary literals")
    public void testDecimalInferenceOnBinaryExpressions() {
        BValue[] returns = BRunUtil.invoke(result, "decimalInferenceInLiterals");
        Assert.assertEquals(returns[0], new BDecimal("0.5", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[1], new BDecimal("3.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[2], new BDecimal("-1.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[3], new BDecimal("2.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns[4], new BDecimal("10000.5051", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal inference on binary literals")
    public void testDecimalArrayValueLoading() {
        BValue[] returns = BRunUtil.invoke(result, "decimalArrayLoad");
        Assert.assertEquals(returns[0], new BDecimal("2.0", DecimalValueKind.OTHER));
    }
}
