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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.DecimalValueKind;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(result, "testDecimalValue");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        Assert.assertEquals(value.value().compareTo(new BigDecimal("10.1", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test negative decimal value assignment")
    public void testNegativeDecimalValue() {
        Object returns = BRunUtil.invoke(result, "testNegativeDecimalValue");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        Assert.assertEquals(value.value().compareTo(new BigDecimal("-10.1", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal value assignment by a return value")
    public void testDecimalValueAssignmentByReturnValue() {
        Object returns = BRunUtil.invoke(result, "testDecimalValueAssignmentByReturnValue");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        Assert.assertEquals(value.value().compareTo(new BigDecimal("10.1", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal addition")
    public void testDecimalAddition() {
        Object returns = BRunUtil.invoke(result, "testDecimalAddition");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        Assert.assertEquals(value.value().compareTo(new BigDecimal("7.665", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal subtraction")
    public void testDecimalSubtraction() {
        Object returns = BRunUtil.invoke(result, "testDecimalSubtraction");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        Assert.assertEquals(value.value().compareTo(new BigDecimal("1.465", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal multiplication")
    public void testDecimalMultiplication() {
        Object returns = BRunUtil.invoke(result, "testDecimalMultiplication");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        Assert.assertEquals(value.value().compareTo(new BigDecimal("14.1515", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal division")
    public void testDecimalDivision() {
        Object returns = BRunUtil.invoke(result, "testDecimalDivision");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        BigDecimal actual = value.value();
        BigDecimal expected = new BigDecimal("1.47258064516", MathContext.DECIMAL128);
        // Check whether the difference between actual and expected is less than DELTA, which is a very small number
        Assert.assertTrue(actual.subtract(expected, MathContext.DECIMAL128).compareTo(DELTA) < 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal modulus")
    public void testDecimalModulus() {
        Object returns = BRunUtil.invoke(result, "testDecimalModulus");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        Assert.assertEquals(value.value().compareTo(new BigDecimal("1.465", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal negation")
    public void testDecimalNegation() {
        Object returns = BRunUtil.invoke(result, "testDecimalNegation");

        Assert.assertTrue(returns instanceof BDecimal);
        BDecimal value = (BDecimal) returns;
        Assert.assertEquals(value.value().compareTo(new BigDecimal("-4.565", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal comparison operations")
    public void testDecimalComparisonOperations() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalComparisonOperations");
        Assert.assertEquals(returns.size(), 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns.get(i).getClass(), Boolean.class);
            boolean val = (boolean) returns.get(i);
            if (i % 2 == 0) {
                Assert.assertFalse(val, "Invalid boolean value returned.");
            } else {
                Assert.assertTrue(val, "Invalid boolean value returned.");
            }
        }
    }

    @Test(description = "Test decimal value passed as a parameter")
    public void testDecimalParameter() {
        BigDecimal decimalArg1 = new BigDecimal("343.342", MathContext.DECIMAL128);
        BigDecimal decimalArg2 = new BigDecimal("-21.2", MathContext.DECIMAL128);
        Object[] args = {ValueCreator.createDecimalValue(decimalArg1), ValueCreator.createDecimalValue(decimalArg2)};
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalParameter", args);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BDecimal);
        Assert.assertTrue(returns.get(1) instanceof BDecimal);
        BDecimal value1 = (BDecimal) returns.get(0);
        BDecimal value2 = (BDecimal) returns.get(1);
        Assert.assertEquals(value1.value().compareTo(new BigDecimal("343.342", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
        Assert.assertEquals(value2.value().compareTo(new BigDecimal("-21.2", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test assigning an int literal to a decimal variable")
    public void testIntLiteralAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testIntLiteralAssignment");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BDecimal);
        Assert.assertTrue(returns.get(1) instanceof BDecimal);
        BDecimal value1 = (BDecimal) returns.get(0);
        BDecimal value2 = (BDecimal) returns.get(1);
        Assert.assertEquals(value1.value().compareTo(new BigDecimal("12", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
        Assert.assertEquals(value2.value().compareTo(new BigDecimal("15", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test positively signed literal assignment")
    public void testPositivelySignedLiteralAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testPositivelySignedLiteralAssignment");
        Assert.assertEquals(returns.size(), 3);
        for (Object aReturn : returns.getValues()) {
            Assert.assertTrue(aReturn instanceof BDecimal);
        }
        BDecimal value1 = (BDecimal) returns.get(0);
        BDecimal value2 = (BDecimal) returns.get(1);
        BDecimal value3 = (BDecimal) returns.get(2);
        Assert.assertEquals(value1.value().compareTo(new BigDecimal("12.23", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
        Assert.assertEquals(value2.value().compareTo(new BigDecimal("0.0", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
        Assert.assertEquals(value3.value().compareTo(new BigDecimal("+0.00", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal array literal without decimal discriminator")
    public void testDecimalArrayLiteral() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalArrayValue", new Object[]{});
        Assert.assertEquals(returns.get(0), ValueCreator.createDecimalValue("1.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(1), ValueCreator.createDecimalValue("2.0", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal array literal with decimal discriminator")
    public void testDiscriminatedDecimalArrayLiteral() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalArrayValueWithDiscriminator", new Object[]{});
        Assert.assertEquals(returns.get(0), ValueCreator.createDecimalValue("1.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(1), ValueCreator.createDecimalValue("2.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(2), ValueCreator.createDecimalValue("3000.0", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal array literal with decimal discriminator")
    public void testDiscriminatedDecimalLiteral() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDiscriminatedDecimalLiterals", new Object[]{});
        Assert.assertEquals(returns.get(0), ValueCreator.createDecimalValue("3.22", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(1), ValueCreator.createDecimalValue("0.0", DecimalValueKind.ZERO));
        Assert.assertEquals(returns.get(2),
                ValueCreator.createDecimalValue("3.141592653589793238462643383279502", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(3),
                ValueCreator.createDecimalValue("3.141592653589793238462643383279503", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal inference for binary literal expressions")
    public void testDecimalTypeInferenceInBinaryLiteralExpressions() {
        Object returns = BRunUtil.invoke(result, "testDecimalInferenceInMapContext", new Object[]{});
        BMap<String, BDecimal> map = (BMap<String, BDecimal>) returns;

        Assert.assertEquals(map.get(StringUtils.fromString("a")),
                ValueCreator.createDecimalValue("33.3", DecimalValueKind.OTHER));
        Assert.assertEquals(map.get(StringUtils.fromString("b")),
                ValueCreator.createDecimalValue("33.3", DecimalValueKind.OTHER));
        Assert.assertEquals(map.get(StringUtils.fromString("c")),
                ValueCreator.createDecimalValue("0.1", DecimalValueKind.OTHER));
        Assert.assertEquals(map.get(StringUtils.fromString("d")),
                ValueCreator.createDecimalValue("1", DecimalValueKind.OTHER));
        Assert.assertEquals(map.get(StringUtils.fromString("e")),
                ValueCreator.createDecimalValue("10000000000000000000000.123", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal inference on binary literals")
    public void testDecimalInferenceOnBinaryExpressions() {
        BArray returns = (BArray) BRunUtil.invoke(result, "decimalInferenceInLiterals");
        Assert.assertEquals(returns.get(0), ValueCreator.createDecimalValue("0.5", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(1), ValueCreator.createDecimalValue("3.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(2), ValueCreator.createDecimalValue("-1.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(3), ValueCreator.createDecimalValue("2.0", DecimalValueKind.OTHER));
        Assert.assertEquals(returns.get(4), ValueCreator.createDecimalValue("10000.5051", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal inference on binary literals")
    public void testDecimalArrayValueLoading() {
        Object returns = BRunUtil.invoke(result, "decimalArrayLoad");
        Assert.assertEquals(returns, ValueCreator.createDecimalValue("2.0", DecimalValueKind.OTHER));
    }

    @Test(description = "Test decimal filler value")
    public void testDecimalFillerValue() {
        BRunUtil.invoke(result, "testDecimalFillerValue");
    }

    @Test
    public void testDecimalValueWithExponent() {
        BRunUtil.invoke(result, "testDecimalValueWithExponent");
    }

    @Test
    public void testDecimalZeroOperations() {
        BRunUtil.invoke(result, "testDecimalZeroOperations");
    }

    @Test
    public void testDecimalValueOverflow() {
        BRunUtil.invoke(result, "testDecimalValueOverflow");
    }

    @Test()
    public void testDecimalValUsingIntLiterals() {
        BRunUtil.invoke(result, "testDecimalValUsingIntLiterals");
    }

    @Test()
    public void testDecimalTypeRef() {
        BRunUtil.invoke(result, "testDecimalTypeRef");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
