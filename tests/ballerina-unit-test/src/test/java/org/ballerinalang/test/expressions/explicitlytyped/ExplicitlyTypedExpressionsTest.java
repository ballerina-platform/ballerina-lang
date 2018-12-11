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
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;

/**
 * Class to test explicitly typed expressions.
 *
 * @since 0.985.0
 */
public class ExplicitlyTypedExpressionsTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/explicitlytyped/explicitly_typed_expr.bal");
        resultNegative = BCompileUtil.compile(
                "test-src/expressions/explicitlytyped/explicitly_typed_expr_negative.bal");
    }

    @Test(dataProvider = "stringAsStringTests")
    public void testStringAsString(String functionName, String s) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BString(s)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
    }

    @Test(dataProvider = "floatAsStringTests")
    public void testFloatAsString(String functionName, double d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), (new BFloat(d)).stringValue(), "incorrect string " +
                "representation as float");
    }

    @Test(dataProvider = "floatAsFloatTests")
    public void testFloatAsFloat(String functionName, double d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), (new BFloat(d)).floatValue(), "incorrect float " +
                "representation as float");
    }

    @Test(dataProvider = "floatAsDecimalTests")
    public void testFloatAsDecimal(String functionName, double d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(),
                            (new BDecimal(new BigDecimal(d, MathContext.DECIMAL128))).decimalValue(),
                            "incorrect float representation as decimal");
    }

    @Test(dataProvider = "floatAsIntTests")
    public void testFloatAsInt(String functionName, double d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected ints to be the same");
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), Math.round((new BFloat(d)).floatValue()), "incorrect" +
                " float representation as int");
    }

    @Test(dataProvider = "floatAsBooleanTests")
    public void testFloatAsBoolean(String functionName) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be represented as true");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue(), "expected floats to be represented as false");
    }

    @Test(dataProvider = "decimalAsStringTests")
    public void testDecimalAsString(String functionName, BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), (new BDecimal(d)).stringValue(), "incorrect decimal " +
                "representation as string");
    }

    @Test(dataProvider = "decimalAsFloatTests")
    public void testDecimalAsFloat(String functionName, BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), (new BDecimal(d)).floatValue(), "incorrect decimal " +
                "representation as float");
    }

    @Test(dataProvider = "decimalAsDecimalTests")
    public void testDecimalAsDecimal(String functionName, BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(), (new BDecimal(d)).decimalValue(), "incorrect " +
                "decimal representation as decimal");
    }

    @Test(dataProvider = "decimalAsIntTests")
    public void testDecimalAsInt(String functionName, BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected ints to be the same");
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[1]).intValue(),
                            Math.round((new BDecimal(d)).decimalValue().doubleValue()),
                            "incorrect decimal representation as int");
    }

    @Test(dataProvider = "decimalAsBooleanTests")
    public void testDecimalAsBoolean(String functionName) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be represented as true");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue(), "expected decimals to be represented as false");
    }

    @Test(dataProvider = "intAsStringTests")
    public void testIntAsString(String functionName, int i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), (new BInteger(i)).stringValue(), "incorrect int representation" +
                " as string");
    }

    @Test(dataProvider = "intAsFloatTests")
    public void testIntAsFloat(String functionName, int i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), (new BInteger(i)).floatValue(), "incorrect int " +
                "representation as float");
    }

    @Test(dataProvider = "intAsDecimalTests")
    public void testIntAsDecimal(String functionName, int i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(),
                            (new BigDecimal(i, MathContext.DECIMAL128)).setScale(1, BigDecimal.ROUND_HALF_EVEN),
                            "incorrect int representation as decimal");
    }

    @Test(dataProvider = "intAsIntTests")
    public void testIntAsInt(String functionName, int i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected ints to be the same");
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), (new BInteger(i)).intValue(), "incorrect int " +
                "representation as int");
    }

    @Test(dataProvider = "intAsBooleanTests")
    public void testIntAsBoolean(String functionName) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[0]);
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
    public void testBooleanInUnionAsString() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanInUnionAsString", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "true", "invalid boolean representation as string");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "false", "invalid boolean representation as string");
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
    public void testBooleanInUnionAsFloat() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanInUnionAsFloat", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.0, "invalid boolean representation as float");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 0.0, "invalid boolean representation as float");
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
    public void testBooleanInUnionAsDecimal() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanInUnionAsDecimal", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue(),
                            BigDecimal.ONE.setScale(1, BigDecimal.ROUND_HALF_EVEN),
                            "invalid boolean representation as decimal");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(),
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
    public void testBooleanInUnionAsInt() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanInUnionAsInt", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1, "invalid boolean representation as int");
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0, "invalid boolean representation as int");
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
    public void testBooleanInUnionAsBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanInUnionAsBoolean", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true, "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false, "invalid boolean representation as " +
                "boolean");
    }

    @Test(dataProvider = "naNFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'NaN' cannot be converted to 'int'\"\\}.*")
    public void testNaNFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "infiniteFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'Infinity' cannot be converted to 'int'\"\\}.*")
    public void testInfiniteFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "outOfRangeFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"out of " +
                    "range 'float' value '.*' cannot be converted to 'int'\"\\}.*")
    public void testOutOfRangeFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "outOfRangeDecimalAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"out of " +
                    "range 'decimal' value '.*' cannot be converted to 'int'\"}.*")
    public void testOutOfRangeDecimalAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test
    public void testExplicitlyTypedExprForExactValues() {
        BValue[] returns = BRunUtil.invoke(result, "testExplicitlyTypedExprForExactValues", new BValue[0]);
        if (returns[0] instanceof BError) {
            Assert.fail(((BError) returns[0]).getReason());
        }
    }

    @Test
    public void testTypeAssertionOnRecordLiterals() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeAssertionOnRecordLiterals");
        Assert.assertEquals(returns[0].stringValue(), "Server mode configuration");
        Assert.assertEquals(returns[1].stringValue(), "Embedded mode configuration");
        Assert.assertEquals(returns[2].stringValue(), "In-memory mode configuration");
    }

    @Test
    public void testNegativeExprs() {
        Assert.assertEquals(resultNegative.getErrorCount(), 10);
        int errIndex = 0;
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'float'",
                      21, 16);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'float'",
                      22, 18);

        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as " +
                              "'decimal'", 24, 18);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as " +
                              "'decimal'", 25, 18);

        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'int'",
                      27, 14);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'int'",
                      28, 18);

        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'byte'",
                      30, 18);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as 'byte'",
                      31, 15);

        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be explicitly typed as " +
                              "'boolean'", 33, 19);
        validateError(resultNegative, errIndex, "incompatible types: 'string' cannot be explicitly typed as 'boolean'",
                      34, 19);
    }

    @DataProvider
    public Object[][] stringAsStringTests() {
        String[] asStringTestFunctions = new String[]{"testStringAsString", "testStringInUnionAsString"};
        String[] stringValues = new String[]{"a", "", "Hello, from Ballerina!"};
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(asStringTestFunctions)
                .forEach(func -> Arrays.stream(stringValues)
                        .forEach(arg -> result.add(new Object[]{func, arg})));
        return result.toArray(new Object[result.size()][]);
    }

    @DataProvider
    public Object[][] floatAsStringTests() {
        String[] floatAsTestFunctions = new String[]{"testFloatAsString", "testFloatInUnionAsString"};
        return getFunctionAndArgArraysForFloat(floatAsTestFunctions);
    }

    @DataProvider
    public Object[][] floatAsFloatTests() {
        String[] floatAsTestFunctions = new String[]{"testFloatAsFloat", "testFloatInUnionAsFloat"};
        return getFunctionAndArgArraysForFloat(floatAsTestFunctions);
    }

    @DataProvider
    public Object[][] floatAsDecimalTests() {
        String[] floatAsTestFunctions = new String[]{"testFloatAsDecimal", "testFloatInUnionAsDecimal"};
        return getFunctionAndArgArraysForFloat(floatAsTestFunctions);
    }

    @DataProvider
    public Object[][] floatAsIntTests() {
        String[] floatAsTestFunctions = new String[]{"testFloatAsInt", "testFloatInUnionAsInt"};
        return getFunctionAndArgArraysForFloat(floatAsTestFunctions);
    }

    @DataProvider
    public Object[][] floatAsBooleanTests() {
        return new Object[][]{
                {"testFloatAsBoolean"},
                {"testFloatInUnionAsBoolean"}
        };
    }

    private Object[][] getFunctionAndArgArraysForFloat(String[] floatAsTestFunctions) {
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(floatAsTestFunctions)
                .forEach(func -> Arrays.stream(doubleValues())
                        .forEach(arg -> result.add(new Object[]{func, arg})));
        return result.toArray(new Object[result.size()][]);
    }


    private double[] doubleValues() {
        return new double[]{-1234.57, 0.0, 1.5, 53456.032};
    }

    @DataProvider
    public Object[][] decimalAsStringTests() {
        String[] decimalAsTestFunctions = new String[]{"testDecimalAsString", "testDecimalInUnionAsString"};
        return getFunctionAndArgArraysForDecimal(decimalAsTestFunctions);
    }

    @DataProvider
    public Object[][] decimalAsFloatTests() {
        String[] decimalAsTestFunctions = new String[]{"testDecimalAsFloat", "testDecimalInUnionAsFloat"};
        return getFunctionAndArgArraysForDecimal(decimalAsTestFunctions);
    }

    @DataProvider
    public Object[][] decimalAsDecimalTests() {
        String[] decimalAsTestFunctions = new String[]{"testDecimalAsDecimal", "testDecimalInUnionAsDecimal"};
        return getFunctionAndArgArraysForDecimal(decimalAsTestFunctions);
    }

    @DataProvider
    public Object[][] decimalAsIntTests() {
        String[] decimalAsTestFunctions = new String[]{"testDecimalAsInt", "testDecimalInUnionAsInt"};
        return getFunctionAndArgArraysForDecimal(decimalAsTestFunctions);
    }

    @DataProvider
    public Object[][] decimalAsBooleanTests() {
        return new Object[][]{
                {"testDecimalAsBoolean"},
                {"testDecimalInUnionAsBoolean"}
        };
    }

    private Object[][] getFunctionAndArgArraysForDecimal(String[] decimalAsTestFunctions) {
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(decimalAsTestFunctions)
                .forEach(func -> Arrays.stream(decimalValues())
                        .forEach(arg -> result.add(new Object[]{func, arg})));
        return result.toArray(new Object[result.size()][]);
    }

    private BigDecimal[] decimalValues() {
        return new BigDecimal[]{
                new BigDecimal("-1234.57", MathContext.DECIMAL128),
                new BigDecimal("53456.032", MathContext.DECIMAL128),
                new BigDecimal("0.0", MathContext.DECIMAL128),
                new BigDecimal("1.1", MathContext.DECIMAL128)
        };
    }

    @DataProvider
    public Object[][] intAsStringTests() {
        String[] intAsTestFunctions = new String[]{"testIntAsString", "testIntInUnionAsString"};
        return getFunctionAndArgArraysForInt(intAsTestFunctions);
    }

    @DataProvider
    public Object[][] intAsFloatTests() {
        String[] intAsTestFunctions = new String[]{"testIntAsFloat", "testIntInUnionAsFloat"};
        return getFunctionAndArgArraysForInt(intAsTestFunctions);
    }

    @DataProvider
    public Object[][] intAsDecimalTests() {
        String[] asIntTestFunctions = new String[]{"testIntAsDecimal", "testIntInUnionAsDecimal"};
        return getFunctionAndArgArraysForInt(asIntTestFunctions);
    }

    @DataProvider
    public Object[][] intAsIntTests() {
        String[] intAsTestFunctions = new String[]{"testIntAsInt", "testIntInUnionAsInt"};
        return getFunctionAndArgArraysForInt(intAsTestFunctions);
    }

    @DataProvider
    public Object[][] intAsBooleanTests() {
        return new Object[][]{
                {"testIntAsBoolean"},
                {"testIntInUnionAsBoolean"}
        };
    }

    private Object[][] getFunctionAndArgArraysForInt(String[] intAsTestFunctions) {
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(intAsTestFunctions)
                .forEach(func -> Arrays.stream(intValues())
                        .forEach(arg -> result.add(new Object[]{func, arg})));
        return result.toArray(new Object[result.size()][]);
    }

    private int[] intValues() {
        return new int[]{-123457, 0, 1, 53456032};
    }

    @DataProvider
    public Object[][] naNFloatAsIntTests() {
        return new Object[][]{
                {"testNaNFloatAsInt"},
                {"testNaNFloatInUnionAsInt"}
        };
    }

    @DataProvider
    public Object[][] infiniteFloatAsIntTests() {
        return new Object[][]{
                {"testInfiniteFloatAsInt"},
                {"testInfiniteInUnionFloatAsInt"}
        };
    }

    @DataProvider
    public Object[][] outOfRangeFloatAsIntTests() {
        return new Object[][]{
                {"testOutOfIntRangePositiveFloatAsInt"},
                {"testOutOfIntRangeNegativeFloatAsInt"},
                {"testOutOfIntRangePositiveFloatInUnionAsInt"},
                {"testOutOfIntRangeNegativeFloatInUnionAsInt"}
        };
    }

    @DataProvider
    public Object[][] outOfRangeDecimalAsIntTests() {
        return new Object[][]{
                {"testOutOfIntRangePositiveDecimalAsInt"},
                {"testOutOfIntRangeNegativeDecimalAsInt"},
                {"testOutOfIntRangePositiveDecimalInUnionAsInt"},
                {"testOutOfIntRangeNegativeDecimalInUnionAsInt"}
        };
    }
}
