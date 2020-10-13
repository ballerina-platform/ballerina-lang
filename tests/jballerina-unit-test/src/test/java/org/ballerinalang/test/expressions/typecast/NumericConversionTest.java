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
package org.ballerinalang.test.expressions.typecast;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueType;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Class to test numeric conversion via the type cast expression.
 *
 * @since 0.985.0
 */
public class NumericConversionTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typecast/numeric_conversion.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/typecast/numeric_conversion_negative.bal");
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

    @Test(dataProvider = "floatAsByteTests")
    public void testFloatAsByte(String functionName, double d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BFloat(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected bytes to be the same");
        Assert.assertSame(returns[1].getClass(), BByte.class);
        Assert.assertEquals(((BByte) returns[1]).byteValue(), (new BFloat(d)).byteValue(), "incorrect float " +
                "representation as byte");
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

    @Test(dataProvider = "decimalAsByteTests")
    public void testDecimalAsByte(String functionName, BigDecimal d) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BDecimal(d)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected bytes to be the same");
        Assert.assertSame(returns[1].getClass(), BByte.class);
        Assert.assertEquals(((BByte) returns[1]).byteValue(), new BDecimal(d).byteValue(),
                            "incorrect decimal representation as int");
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

    @Test(dataProvider = "intAsByteTests")
    public void testIntAsByte(String functionName, int i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected bytes to be the same");
        Assert.assertEquals(((BValueType) returns[1]).byteValue(), (new BInteger(i)).byteValue(), "incorrect int " +
                "representation as byte");
    }

    @Test(dataProvider = "byteAsFloatTests")
    public void testByteAsFloat(String functionName, long i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BByte(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), (new BByte(i)).floatValue(), "incorrect byte " +
                "representation as float");
    }

    @Test(dataProvider = "byteAsDecimalTests")
    public void testByteAsDecimal(String functionName, long i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BByte(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(), new BByte(i).decimalValue(),
                            "incorrect byte representation as decimal");
    }

    @Test(dataProvider = "byteAsIntTests")
    public void testByteAsInt(String functionName, long i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BByte(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected ints to be the same");
        Assert.assertEquals(((BValueType) returns[1]).intValue(), (new BByte(i)).intValue(), "incorrect byte " +
                "representation as int");
    }

    @Test(dataProvider = "byteAsByteTests")
    public void testByteAsByte(String functionName, long i) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BByte(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected bytes to be the same");
        Assert.assertSame(returns[1].getClass(), BByte.class);
        Assert.assertEquals(((BByte) returns[1]).byteValue(), (new BByte(i)).byteValue(), "incorrect byte " +
                "representation as byte");
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'int' " +
                    "value '.*' cannot be converted to 'byte'.*")
    public void testInvalidIntAsByte(int i) {
        BRunUtil.invoke(result, "testIntAsByte", new BValue[]{new BInteger(i)});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: 'int' cannot be cast to 'byte\\|boolean'.*")
    public void testInvalidIntAsByteInUnions(int i) {
        BRunUtil.invoke(result, "testIntAsByteInUnions", new BValue[]{new BInteger(i)});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value '.*' cannot be converted to 'byte'.*")
    public void testInvalidFloatAsByte(int i) {
        BRunUtil.invoke(result, "testFloatAsByte", new BValue[]{new BFloat(i)});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value '.*' cannot be converted to 'byte'.*")
    public void testInvalidFloatAsByteInUnions(int i) {
        BRunUtil.invoke(result, "testFloatAsByteInUnions", new BValue[]{new BFloat(i)});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'decimal'" +
                    " value '.*' cannot be converted to 'byte'.*")
    public void testInvalidDecimalAsByte(int i) {
        BRunUtil.invoke(result, "testDecimalAsByte", new BValue[]{new BDecimal(new BigDecimal(i))});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible " +
                    "types: 'decimal' cannot be cast to '(byte\\|Employee)'.*")
    public void testInvalidDecimalAsByteInUnions(int i) {
        BRunUtil.invoke(result, "testDecimalAsByteInUnions", new BValue[] { new BDecimal(new BigDecimal(i)) });
    }

    @Test(dataProvider = "naNFloatAsByteTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'NaN' cannot be converted to 'byte'.*")
    public void testNaNFloatAsByte(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "infiniteFloatAsByteTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'Infinity' cannot be converted to 'byte'.*")
    public void testInfiniteFloatAsByte(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "naNFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'NaN' cannot be converted to 'int'.*")
    public void testNaNFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "infiniteFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'Infinity' cannot be converted to 'int'.*")
    public void testInfiniteFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "outOfRangeFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"" +
                    "'float' value '.*' cannot be converted to 'int'.*")
    public void testOutOfRangeFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "outOfRangeDecimalAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"" +
                    "'decimal' value '.*' cannot be converted to 'int'.*")
    public void testOutOfRangeDecimalAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "naNDecimalAsByteTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'decimal'" +
                    " value 'NaN' cannot be converted to 'byte'.*")
    public void testNaNDecimalAsByte(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "positiveInfiniteDecimalAsByteTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'decimal'" +
                    " value 'Infinity' cannot be converted to 'byte'.*")
    public void testPositiveInfiniteDecimalAsByte(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "negativeInfiniteDecimalAsByteTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'decimal'" +
                    " value '-Infinity' cannot be converted to 'byte'.*")
    public void testNegativeInfiniteDecimalAsByte(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "naNDecimalAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'decimal'" +
                    " value 'NaN' cannot be converted to 'int'.*")
    public void testNaNDecimalAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "positiveInfiniteDecimalAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'decimal'" +
                    " value 'Infinity' cannot be converted to 'int'.*")
    public void testPositiveInfiniteDecimalAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new BValue[0]);
    }

    @Test(dataProvider = "negativeInfiniteDecimalAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'decimal'" +
                    " value '-Infinity' cannot be converted to 'int'.*")
    public void testNegativeInfiniteDecimalAsInt(String functionName) {
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
    public void testConversionFromUnionWithNumericBasicTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testConversionFromUnionWithNumericBasicTypes");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected numeric conversion to be successful");
    }

    @Test
    public void testNumericConversionFromBasicTypeToUnionType() {
        BValue[] returns = BRunUtil.invoke(result, "testNumericConversionFromBasicTypeToUnionType");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected numeric conversion to be successful");
    }

    @Test
    public void testNumericConversionFromFiniteType() {
        BValue[] returns = BRunUtil.invoke(result, "testNumericConversionFromFiniteType");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected numeric conversion to be successful");
    }

    @Test
    public void testNegativeExprs() {
        Assert.assertEquals(resultNegative.getErrorCount(), 25);
        int errIndex = 0;
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to 'float'",
                      21, 16);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to 'float'",
                      22, 18);

        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to " +
                              "'decimal'", 24, 18);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to " +
                              "'decimal'", 25, 18);

        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to 'int'",
                      27, 14);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to 'int'",
                      28, 18);

        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to 'byte'",
                      30, 18);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to 'byte'",
                      31, 15);

        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to " +
                              "'boolean'", 33, 19);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to " +
                              "'boolean'", 34, 19);
        validateError(resultNegative, errIndex++, "incompatible types: 'int' cannot be cast to 'string'",
                      44, 17);
        validateError(resultNegative, errIndex++, "incompatible types: 'int' cannot be cast to 'boolean'",
                      45, 18);
        validateError(resultNegative, errIndex++, "incompatible types: 'float' cannot be cast to 'string'",
                      47, 17);
        validateError(resultNegative, errIndex++, "incompatible types: 'float' cannot be cast to 'boolean'",
                      48, 18);
        validateError(resultNegative, errIndex++, "incompatible types: 'decimal' cannot be cast to " +
                              "'string'", 50, 17);
        validateError(resultNegative, errIndex++, "incompatible types: 'decimal' cannot be cast to " +
                              "'boolean'", 51, 18);
        validateError(resultNegative, errIndex++, "incompatible types: 'boolean' cannot be cast to 'int'",
                      53, 15);
        validateError(resultNegative, errIndex++, "incompatible types: 'boolean' cannot be cast to " +
                              "'string'", 54, 18);
        validateError(resultNegative, errIndex++, "incompatible types: 'boolean' cannot be cast to " +
                              "'float'", 55, 17);
        validateError(resultNegative, errIndex++, "incompatible types: 'boolean' cannot be cast to " +
                              "'decimal'", 56, 19);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to 'int'",
                      58, 15);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to " +
                              "'boolean'", 59, 19);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to 'float'",
                      60, 17);
        validateError(resultNegative, errIndex++, "incompatible types: 'string' cannot be cast to " +
                              "'decimal'", 61, 19);
        validateError(resultNegative, errIndex, "incompatible types: 'float' cannot be cast to " +
                              "'(int|decimal)'", 66, 24);
    }

    @DataProvider
    public Object[][] floatAsFloatTests() {
        String[] floatAsTestFunctions = new String[]{"testFloatAsFloat", "testFloatAsFloatInUnions"};
        return getFunctionAndArgArraysForFloat(floatAsTestFunctions, doubleValues());
    }

    @DataProvider
    public Object[][] floatAsDecimalTests() {
        String[] floatAsTestFunctions = new String[]{"testFloatAsDecimal", "testFloatAsDecimalInUnions"};
        return getFunctionAndArgArraysForFloat(floatAsTestFunctions, doubleValues());
    }

    @DataProvider
    public Object[][] floatAsIntTests() {
        String[] floatAsTestFunctions = new String[]{"testFloatAsInt", "testFloatAsIntInUnions"};
        return getFunctionAndArgArraysForFloat(floatAsTestFunctions, doubleValues());
    }

    @DataProvider
    public Object[][] floatAsByteTests() {
        String[] floatAsTestFunctions = new String[]{"testFloatAsByte", "testFloatAsByteInUnions"};
        return getFunctionAndArgArraysForFloat(floatAsTestFunctions, doubleValuesForAsByte());
    }

    private Object[][] getFunctionAndArgArraysForFloat(String[] floatAsTestFunctions, double[] values) {
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(floatAsTestFunctions)
                .forEach(func -> Arrays.stream(values)
                        .forEach(arg -> result.add(new Object[]{func, arg})));
        return result.toArray(new Object[result.size()][]);
    }

    private double[] doubleValues() {
        return new double[]{-1234.57, 0.0, 1.5, 53456.032};
    }

    private double[] doubleValuesForAsByte() {
        return new double[]{0.99, 0.0, 1.0, 255.4};
    }

    @DataProvider
    public Object[][] decimalAsFloatTests() {
        String[] decimalAsTestFunctions = new String[]{"testDecimalAsFloat", "testDecimalAsFloatInUnions"};
        return getFunctionAndArgArraysForDecimal(decimalAsTestFunctions, decimalValues());
    }

    @DataProvider
    public Object[][] decimalAsDecimalTests() {
        String[] decimalAsTestFunctions = new String[]{"testDecimalAsDecimal", "testDecimalAsDecimalInUnions"};
        return getFunctionAndArgArraysForDecimal(decimalAsTestFunctions, decimalValues());
    }

    @DataProvider
    public Object[][] decimalAsIntTests() {
        String[] decimalAsTestFunctions = new String[]{"testDecimalAsInt", "testDecimalAsIntInUnions"};
        return getFunctionAndArgArraysForDecimal(decimalAsTestFunctions, decimalValues());
    }

    @DataProvider
    public Object[][] decimalAsByteTests() {
        String[] decimalAsTestFunctions = new String[]{"testDecimalAsByte", "testDecimalAsByteInUnions"};
        return getFunctionAndArgArraysForDecimal(decimalAsTestFunctions, decimalValuesForAsBytes());
    }

    private Object[][] getFunctionAndArgArraysForDecimal(String[] decimalAsTestFunctions, BigDecimal[] bigDecimals) {
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(decimalAsTestFunctions)
                .forEach(func -> Arrays.stream(bigDecimals)
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

    private BigDecimal[] decimalValuesForAsBytes() {
        return new BigDecimal[]{
                new BigDecimal("0.9", MathContext.DECIMAL128),
                new BigDecimal("1.032", MathContext.DECIMAL128),
                new BigDecimal("254.30", MathContext.DECIMAL128),
                new BigDecimal("255.10", MathContext.DECIMAL128)
        };
    }

    @DataProvider
    public Object[][] intAsFloatTests() {
        String[] intAsTestFunctions = new String[]{"testIntAsFloat", "testIntAsFloatInUnions"};
        return getFunctionAndArgArraysForInt(intAsTestFunctions, intValues());
    }

    @DataProvider
    public Object[][] intAsDecimalTests() {
        String[] asIntTestFunctions = new String[]{"testIntAsDecimal", "testIntAsDecimalInUnions"};
        return getFunctionAndArgArraysForInt(asIntTestFunctions, intValues());
    }

    @DataProvider
    public Object[][] intAsIntTests() {
        String[] intAsTestFunctions = new String[]{"testIntAsInt", "testIntAsIntInUnions"};
        return getFunctionAndArgArraysForInt(intAsTestFunctions, intValues());
    }

    @DataProvider
    public Object[][] intAsByteTests() {
        String[] intAsByteTestFunctions = new String[]{"testIntAsByte", "testIntAsByteInUnions"};
        return getFunctionAndArgArraysForInt(intAsByteTestFunctions, intAsByteValues());
    }

    private Object[][] getFunctionAndArgArraysForInt(String[] intAsTestFunctions, int[] ints) {
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(intAsTestFunctions)
                .forEach(func -> Arrays.stream(ints)
                        .forEach(arg -> result.add(new Object[]{func, arg})));
        return result.toArray(new Object[result.size()][]);
    }

    private int[] intValues() {
        return new int[]{-123457, 0, 1, 53456032};
    }

    private int[] intAsByteValues() {
        return new int[]{0, 1, 254, 255};
    }

    @DataProvider
    public Object[][] byteAsFloatTests() {
        String[] intAsTestFunctions = new String[]{"testByteAsFloat", "testByteAsFloatInUnions"};
        return getFunctionAndArgArraysForByte(intAsTestFunctions);
    }

    @DataProvider
    public Object[][] byteAsDecimalTests() {
        String[] asIntTestFunctions = new String[]{"testByteAsDecimal", "testByteAsDecimalInUnions"};
        return getFunctionAndArgArraysForByte(asIntTestFunctions);
    }

    @DataProvider
    public Object[][] byteAsIntTests() {
        String[] intAsTestFunctions = new String[]{"testByteAsInt", "testByteAsIntInUnions"};
        return getFunctionAndArgArraysForByte(intAsTestFunctions);
    }

    @DataProvider
    public Object[][] byteAsByteTests() {
        String[] intAsByteTestFunctions = new String[]{"testByteAsByte", "testByteAsByteInUnions"};
        return getFunctionAndArgArraysForByte(intAsByteTestFunctions);
    }

    private Object[][] getFunctionAndArgArraysForByte(String[] intAsTestFunctions) {
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(intAsTestFunctions)
                .forEach(func -> Arrays.stream(intAsByteValues())
                        .forEach(arg -> result.add(new Object[]{func, (long) arg})));
        return result.toArray(new Object[result.size()][]);
    }

    @DataProvider
    public Object[][] naNFloatAsByteTests() {
        return new Object[][]{
                {"testNaNFloatAsByte"},
                {"testNaNFloatInUnionAsByte"}
        };
    }

    @DataProvider
    public Object[][] infiniteFloatAsByteTests() {
        return new Object[][]{
                {"testInfiniteFloatAsByte"},
                {"testInfiniteFloatInUnionAsByte"}
        };
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
                {"testInfiniteFloatInUnionAsInt"}
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

    @DataProvider
    public Object[][] naNDecimalAsByteTests() {
        return new Object[][]{
                {"testNaNDecimalAsByte"},
                {"testNaNDecimalInUnionAsByte"}
        };
    }

    @DataProvider
    public Object[][] positiveInfiniteDecimalAsByteTests() {
        return new Object[][]{
                {"testPositiveInfiniteDecimalAsByte"},
                {"testPositiveInfiniteDecimalInUnionAsByte"}
        };
    }

    @DataProvider
    public Object[][] negativeInfiniteDecimalAsByteTests() {
        return new Object[][]{
                {"testNegativeInfiniteDecimalAsByte"},
                {"testNegativeInfiniteDecimalInUnionAsByte"}
        };
    }

    @DataProvider
    public Object[][] naNDecimalAsIntTests() {
        return new Object[][]{
                {"testNaNDecimalAsInt"},
                {"testNaNDecimalInUnionAsInt"}
        };
    }

    @DataProvider
    public Object[][] positiveInfiniteDecimalAsIntTests() {
        return new Object[][]{
                {"testPositiveInfiniteDecimalAsInt"},
                {"testPositiveInfiniteDecimalInUnionAsInt"}
        };
    }

    @DataProvider
    public Object[][] negativeInfiniteDecimalAsIntTests() {
        return new Object[][]{
                {"testNegativeInfiniteDecimalAsInt"},
                {"testNegativeInfiniteDecimalInUnionAsInt"}
        };
    }

    @DataProvider
    public Object[][] invalidByteValues() {
        return new Object[][]{
                {-255},
                {-1},
                {256},
                {3055},
        };
    }
}
