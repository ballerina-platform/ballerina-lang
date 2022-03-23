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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.test.BAssertUtil.validateError;

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
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(d)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected floats to be the same");
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), ((d)), "incorrect float " +
                "representation as float");
    }

    @Test(dataProvider = "floatAsDecimalTests")
    public void testFloatAsDecimal(String functionName, double d) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(d)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected decimals to be the same");
        Assert.assertTrue(returns.get(1) instanceof BDecimal);
        Assert.assertEquals(returns.get(1),
                (ValueCreator.createDecimalValue(new BigDecimal(d, MathContext.DECIMAL128))),
                "incorrect float representation as decimal");
    }

    @Test(dataProvider = "floatAsIntTests")
    public void testFloatAsInt(String functionName, double d) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(d)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected ints to be the same");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), Math.round(d), "incorrect float representation as int");
    }

    @Test(dataProvider = "floatAsByteTests")
    public void testFloatAsByte(String functionName, double d) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(d)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected bytes to be the same");
        Assert.assertTrue(returns.get(1) instanceof Integer);
        Assert.assertEquals(returns.get(1), (int) Math.round(d), "incorrect float " +
                "representation as byte");
    }

    @Test(dataProvider = "decimalAsFloatTests")
    public void testDecimalAsFloat(String functionName, BigDecimal d) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{ValueCreator.createDecimalValue(d)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected floats to be the same");
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), d.doubleValue(), "incorrect decimal " +
                "representation as float");
    }

    @Test(dataProvider = "decimalAsDecimalTests")
    public void testDecimalAsDecimal(String functionName, BigDecimal d) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{ValueCreator.createDecimalValue(d)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected decimals to be the same");
        Assert.assertTrue(returns.get(1) instanceof BDecimal);
        Assert.assertEquals(returns.get(1), (ValueCreator.createDecimalValue(d)), "incorrect " +
                "decimal representation as decimal");
    }

    @Test(dataProvider = "decimalAsIntTests")
    public void testDecimalAsInt(String functionName, BigDecimal d) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{ValueCreator.createDecimalValue(d)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected ints to be the same");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), Math.round(d.doubleValue()), "incorrect decimal representation as int");
    }

    @Test(dataProvider = "decimalAsByteTests")
    public void testDecimalAsByte(String functionName, BigDecimal d) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{ValueCreator.createDecimalValue(d)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected bytes to be the same");
        Assert.assertTrue(returns.get(1) instanceof Integer);
        Assert.assertEquals(returns.get(1), d.setScale(0, RoundingMode.HALF_UP).intValue(),
                "incorrect decimal representation as int");
    }

    @Test(dataProvider = "intAsFloatTests")
    public void testIntAsFloat(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected floats to be the same");
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), (double) i, "incorrect int " +
                "representation as float");
    }

    @Test(dataProvider = "intAsDecimalTests")
    public void testIntAsDecimal(String functionName, long i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected decimals to be the same");
        Assert.assertTrue(returns.get(1) instanceof BDecimal);
        Assert.assertEquals(((BDecimal) returns.get(1)).intValue(), (long) i,
                "incorrect int representation as decimal");
    }

    @Test(dataProvider = "intAsIntTests")
    public void testIntAsInt(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected ints to be the same");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), (long) i, "incorrect int " +
                "representation as int");
    }

    @Test(dataProvider = "intAsByteTests")
    public void testIntAsByte(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected bytes to be the same");
        Assert.assertEquals(returns.get(1), i, "incorrect int representation as byte");
    }

    @Test(dataProvider = "intAsByteInUnionTests")
    public void testIntAsByteInUnions(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected bytes to be the same");
        Assert.assertEquals(returns.get(1), (long) i, "incorrect int representation as byte");
    }

    @Test(dataProvider = "byteAsFloatTests")
    public void testByteAsFloat(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected floats to be the same");
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), (double) i, "incorrect byte representation as float");
    }

    @Test(dataProvider = "byteAsDecimalTests")
    public void testByteAsDecimal(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected decimals to be the same");
        Assert.assertTrue(returns.get(1) instanceof BDecimal);
        Assert.assertEquals(returns.get(1), ValueCreator.createDecimalValue(new BigDecimal(i)),
                "incorrect byte representation as decimal");
    }

    @Test(dataProvider = "byteAsIntTests")
    public void testByteAsInt(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected ints to be the same");
        Assert.assertEquals(returns.get(1), (long) i, "incorrect byte representation as int");
    }

    @Test(dataProvider = "byteAsIntInUnionTests")
    public void testByteAsIntInUnions(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected ints to be the same");
        Assert.assertEquals(returns.get(1), i, "incorrect byte representation as int");
    }

    @Test(dataProvider = "byteAsByteTests")
    public void testByteAsByte(String functionName, int i) {
        Object arr = BRunUtil.invoke(result, functionName, new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "expected bytes to be the same");
        Assert.assertTrue(returns.get(1) instanceof Integer);
        Assert.assertEquals(returns.get(1), ((i)), "incorrect byte " +
                "representation as byte");
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'int' " +
                    "value '.*' cannot be converted to 'byte'.*")
    public void testInvalidIntAsByte(int i) {
        BRunUtil.invoke(result, "testIntAsByte", new Object[]{(i)});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: 'int' cannot be cast to '\\(byte\\|boolean\\)'.*")
    public void testInvalidIntAsByteInUnions(int i) {
        BRunUtil.invoke(result, "testIntAsByteInUnions", new Object[]{(i)});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value '.*' cannot be converted to 'byte'.*")
    public void testInvalidFloatAsByte(int i) {
        BRunUtil.invoke(result, "testFloatAsByte", new Object[]{(i)});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value '.*' cannot be converted to 'byte'.*")
    public void testInvalidFloatAsByteInUnions(int i) {
        BRunUtil.invoke(result, "testFloatAsByteInUnions", new Object[]{(i)});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'decimal'" +
                    " value '.*' cannot be converted to 'byte'.*")
    public void testInvalidDecimalAsByte(int i) {
        BRunUtil.invoke(result, "testDecimalAsByte",
                new Object[]{ValueCreator.createDecimalValue(new BigDecimal(i))});
    }

    @Test(dataProvider = "invalidByteValues", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible " +
                    "types: 'decimal' cannot be cast to '\\(byte\\|Employee\\)'.*")
    public void testInvalidDecimalAsByteInUnions(int i) {
        BRunUtil.invoke(result, "testDecimalAsByteInUnions",
                new Object[]{ValueCreator.createDecimalValue(new BigDecimal(i))});
    }

    @Test(dataProvider = "naNFloatAsByteTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'NaN' cannot be converted to 'byte'.*")
    public void testNaNFloatAsByte(String functionName) {
        BRunUtil.invoke(result, functionName, new Object[0]);
    }

    @Test(dataProvider = "infiniteFloatAsByteTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'Infinity' cannot be converted to 'byte'.*")
    public void testInfiniteFloatAsByte(String functionName) {
        BRunUtil.invoke(result, functionName, new Object[0]);
    }

    @Test(dataProvider = "naNFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'NaN' cannot be converted to 'int'.*")
    public void testNaNFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new Object[0]);
    }

    @Test(dataProvider = "infiniteFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"'float' " +
                    "value 'Infinity' cannot be converted to 'int'.*")
    public void testInfiniteFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new Object[0]);
    }

    @Test(dataProvider = "outOfRangeFloatAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"" +
                    "'float' value '.*' cannot be converted to 'int'.*")
    public void testOutOfRangeFloatAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new Object[0]);
    }

    @Test(dataProvider = "outOfRangeDecimalAsIntTests", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}NumberConversionError \\{\"message\":\"" +
                    "'decimal' value '.*' cannot be converted to 'int'.*")
    public void testOutOfRangeDecimalAsInt(String functionName) {
        BRunUtil.invoke(result, functionName, new Object[0]);
    }

    @Test
    public void testExplicitlyTypedExprForExactValues() {
        Object returns = BRunUtil.invoke(result, "testExplicitlyTypedExprForExactValues", new Object[0]);
        if (returns instanceof BError) {
            Assert.fail(((BError) returns).getMessage());
        }
    }

    @Test
    public void testConversionFromUnionWithNumericBasicTypes() {
        Object returns = BRunUtil.invoke(result, "testConversionFromUnionWithNumericBasicTypes");
        Assert.assertTrue((Boolean) returns, "expected numeric conversion to be successful");
    }

    @Test
    public void testNumericConversionFromBasicTypeToUnionType() {
        Object returns = BRunUtil.invoke(result, "testNumericConversionFromBasicTypeToUnionType");
        Assert.assertTrue((Boolean) returns, "expected numeric conversion to be successful");
    }

    @Test
    public void testNumericConversionFromFiniteType() {
        Object returns = BRunUtil.invoke(result, "testNumericConversionFromFiniteType");
        Assert.assertTrue((Boolean) returns, "expected numeric conversion to be successful");
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
        String[] intAsByteTestFunctions = new String[]{"testIntAsByte"};
        return getFunctionAndArgArraysForInt(intAsByteTestFunctions, intAsByteValues());
    }

    @DataProvider
    public Object[][] intAsByteInUnionTests() {
        String[] intAsByteTestFunctions = new String[]{"testIntAsByteInUnions"};
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
        String[] intAsTestFunctions = new String[]{"testByteAsInt"};
        return getFunctionAndArgArraysForByte(intAsTestFunctions);
    }

    @DataProvider
    public Object[][] byteAsIntInUnionTests() {
        String[] intAsTestFunctions = new String[]{"testByteAsIntInUnions"};
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
                        .forEach(arg -> result.add(new Object[]{func, arg})));
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
    public Object[][] invalidByteValues() {
        return new Object[][]{
                {-255},
                {-1},
                {256},
                {3055},
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
