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
package org.ballerinalang.test.expressions.explicitlytyped;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;

/**
 * Class to test explicitly typed expressions.
 *
 * @since 0.985.0
 */
public class ExplicitlyTypedExpressionsTest {

    private CompileResult result;
    private CompileResult resNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/explicitlytyped/explicitly_typed_expr.bal");
        resNegative = BCompileUtil.compile("test-src/expressions/explicitlytyped/explicitly_typed_expr_negative.bal");
    }

    @Test(dataProvider = "stringValues")
    public void testStringAsString(String s) {
        BValue[] returns = BRunUtil.invoke(result, "testStringAsString", new BValue[]{new BString(s)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatAsString(double d) {
        BValue[] returns = BRunUtil.invoke(result, "testFloatAsString", new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), (new BFloat(d)).stringValue(), "incorrect string " +
                "representation as float");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatAsFloat(double d) {
        BValue[] returns = BRunUtil.invoke(result, "testFloatAsFloat", new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), (new BFloat(d)).floatValue(), "incorrect float " +
                "representation as float");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatAsDecimal(double d) {
        BValue[] returns = BRunUtil.invoke(result, "testFloatAsDecimal", new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(),
                            (new BDecimal(new BigDecimal(d, MathContext.DECIMAL128))).decimalValue(),
                            "incorrect float representation as decimal");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatAsInt(double d) {
        BValue[] returns = BRunUtil.invoke(result, "testFloatAsInt", new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected ints to be the same");
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), Math.round((new BFloat(d)).floatValue()), "incorrect" +
                " float representation as int");
    }

    @Test
    public void testFloatAsBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatAsBoolean", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be represented as true");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue(), "expected floats to be represented as false");
    }

    @Test(dataProvider = "decimalValues")
    public void testDecimalAsString(BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalAsString", new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), (new BDecimal(d)).stringValue(), "incorrect decimal " +
                "representation as string");
    }

    @Test(dataProvider = "decimalValues")
    public void testDecimalAsFloat(BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalAsFloat", new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), (new BDecimal(d)).floatValue(), "incorrect decimal " +
                "representation as float");
    }

    @Test(dataProvider = "decimalValues")
    public void testDecimalAsDecimal(BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalAsDecimal", new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(), (new BDecimal(d)).decimalValue(), "incorrect " +
                "decimal representation as decimal");
    }

    @Test(dataProvider = "decimalValues")
    public void testDecimalAsInt(BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalAsInt", new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected ints to be the same");
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[1]).intValue(),
                            Math.round((new BDecimal(d)).decimalValue().doubleValue()),
                            "incorrect decimal representation as int");
    }

    @Test
    public void testDecimalAsBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalAsBoolean", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be represented as true");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue(), "expected decimals to be represented as false");
    }

    @Test(dataProvider = "intValues")
    public void testIntAsString(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testIntAsString", new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), (new BInteger(i)).stringValue(), "incorrect int representation" +
                " as string");
    }

    @Test(dataProvider = "intValues")
    public void testIntAsFloat(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testIntAsFloat", new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), (new BInteger(i)).floatValue(), "incorrect int " +
                "representation as float");
    }

    @Test(dataProvider = "intValues")
    public void testIntAsDecimal(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testIntAsDecimal", new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(),
                            (new BigDecimal(i, MathContext.DECIMAL128)).setScale(1, BigDecimal.ROUND_HALF_EVEN),
                            "incorrect int representation as decimal");
    }

    @Test(dataProvider = "intValues")
    public void testIntAsInt(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testIntAsInt", new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected ints to be the same");
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), (new BInteger(i)).intValue(), "incorrect int " +
                "representation as int");
    }

    @Test
    public void testIntAsBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalAsBoolean", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected ints to be represented as true");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue(), "expected ints to be represented as false");
    }

    @Test
    public void testBooleanAsString() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanAsString", new BValue[0]);
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "true", "invalid boolean representation as string");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "true", "invalid boolean representation as string");
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[2].stringValue(), "false", "invalid boolean representation as string");
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "false", "invalid boolean representation as string");
    }

    @Test
    public void testBooleanAsFloat() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanAsFloat", new BValue[0]);
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.0, "invalid boolean representation as float");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1.0, "invalid boolean representation as float");
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 0.0, "invalid boolean representation as float");
        Assert.assertSame(returns[3].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 0.0, "invalid boolean representation as float");
    }

    @Test
    public void testBooleanAsDecimal() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanAsDecimal", new BValue[0]);
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue(),
                            BigDecimal.ONE.setScale(1, BigDecimal.ROUND_HALF_EVEN),
                            "invalid boolean representation as decimal");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(),
                            BigDecimal.ONE.setScale(1, BigDecimal.ROUND_HALF_EVEN),
                            "invalid boolean representation as decimal");
        Assert.assertSame(returns[2].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[2]).decimalValue(),
                            BigDecimal.ZERO.setScale(1, BigDecimal.ROUND_HALF_EVEN),
                            "invalid boolean representation as decimal");
        Assert.assertSame(returns[3].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[3]).decimalValue(),
                            BigDecimal.ZERO.setScale(1, BigDecimal.ROUND_HALF_EVEN),
                            "invalid boolean representation as decimal");
    }

    @Test
    public void testBooleanAsInt() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanAsInt", new BValue[0]);
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1, "invalid boolean representation as int");
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1, "invalid boolean representation as int");
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0, "invalid boolean representation as int");
        Assert.assertSame(returns[3].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 0, "invalid boolean representation as int");
    }

    @Test
    public void testBooleanAsBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanAsBoolean", new BValue[0]);
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true, "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true, "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false, "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[3]).booleanValue(), false, "invalid boolean representation as " +
                "boolean");
    }

    @Test
    public void testNegativeExprs() {
        Assert.assertEquals(resNegative.getErrorCount(), 10);
        int errIndex = 0;
        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'float'",
                      21, 16);
        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'float'",
                      22, 18);

        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'decimal'",
                      24, 18);
        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'decimal'",
                      25, 18);

        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'int'",
                      27, 14);
        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'int'",
                      28, 18);

        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'byte'",
                      30, 18);
        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'byte'",
                      31, 15);

        validateError(resNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'boolean'",
                      33, 19);
        validateError(resNegative, errIndex, "incompatible types: 'string' cannot be explicitly typed as 'boolean'",
                      34, 19);
    }

    @DataProvider(name = "stringValues")
    public Object[][] stringValues() {
        return new Object[][]{
                {"a"},
                {""},
                {"Hello, from Ballerina!"}
        };
    }

    @DataProvider(name = "floatValues")
    public Object[][] floatValues() {
        return new Object[][]{
                {-1234.57},
                {0.0},
                {1.5},
                {53456.032}
        };
    }

    @DataProvider(name = "decimalValues")
    public Object[][] decimalValues() {
        return new Object[][]{
                {new BigDecimal("-1234.57", MathContext.DECIMAL128)},
                {new BigDecimal("53456.032", MathContext.DECIMAL128)},
                {new BigDecimal("0.0", MathContext.DECIMAL128)},
                {new BigDecimal("1.1", MathContext.DECIMAL128)}
        };
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][]{
                {-123457},
                {0},
                {1},
                {53456032}
        };
    }
}
