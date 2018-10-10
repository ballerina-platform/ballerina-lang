/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test functionality of not equal operators.
 *
 * todo: add tests for maps and multidimensional arrays
 */
public class EqualAndNotEqualOperationsTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/equal_and_not_equal_operation.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/binaryoperations/equal_operation_negative.bal");
    }

    @Test(description = "Test equals/unequals operation with two equal booleans", dataProvider = "equalBooleanValues")
    public void testBooleanEqualityPositive(boolean i, boolean j) {
        BValue[] args = { new BBoolean(i), new BBoolean(j) };
        BValue[] returns = BRunUtil.invoke(result, "checkBooleanEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal booleans",
            dataProvider = "unequalBooleanValues")
    public void testBooleanEqualityNegative(boolean i, boolean j) {
        BValue[] args = { new BBoolean(i), new BBoolean(j) };
        BValue[] returns = BRunUtil.invoke(result, "checkBooleanEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal ints", dataProvider = "equalIntValues")
    public void testIntEqualityPositive(int i, int j) {
        BValue[] args = { new BInteger(i), new BInteger(j) };
        BValue[] returns = BRunUtil.invoke(result, "checkIntEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal ints", dataProvider = "unequalIntValues")
    public void testIntEqualityNegative(int i, int j) {
        BValue[] args = { new BInteger(i), new BInteger(j) };
        BValue[] returns = BRunUtil.invoke(result, "checkIntEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected ints to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal floats", dataProvider = "equalFloatValues")
    public void testFloatEqualityPositive(double i, double j) {
        BValue[] args = { new BFloat(i), new BFloat(j) };
        BValue[] returns = BRunUtil.invoke(result, "checkFloatEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected floats to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal floats", dataProvider = "unequalFloatValues")
    public void testFloatEqualityNegative(double i, double j) {
        BValue[] args = { new BFloat(i), new BFloat(j) };
        BValue[] returns = BRunUtil.invoke(result, "checkFloatEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected floats to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal strings", dataProvider = "equalStringValues")
    public void testStringEqualityPositive(String i, String j) {
        BValue[] args = { new BString(i), new BString(j) };
        BValue[] returns = BRunUtil.invoke(result, "checkStringEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected strings to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal strings",
            dataProvider = "unequalStringValues")
    public void testStringEqualityNegative(String i, String j) {
        BValue[] args = { new BString(i), new BString(j) };
        BValue[] returns = BRunUtil.invoke(result, "checkStringEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected strings to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with nil")
    public void testEqualityToNilPositive() {
        BValue[] args = { null };
        BValue[] returns = BRunUtil.invoke(result, "checkEqualityToNil", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected nil values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with nil and non-nil values", dataProvider = "nonNilBValues")
    public void testStringEqualityNegative(BValue b) {
        BValue[] returns = BRunUtil.invoke(result, "checkEqualityToNil", new BValue[]{b});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected value to be identified as not equal to " +
                "nil");
    }

    @Test(description = "Test equals/unequals operation with two equal arrays", dataProvider = "equalArrayValues")
    public void test1DArrayEqualityPositive(BNewArray i, BNewArray j) {
        BValue[] args = { i, j };
        BValue[] returns = BRunUtil.invoke(result, "check1DArrayEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected array values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal arrays", dataProvider =
            "unequalArrayValues")
    public void test1DArrayEqualityNegative(BValue i, BValue j) {
        BValue[] args = { i, j };
        BValue[] returns = BRunUtil.invoke(result, "check1DArrayEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal json values", dataProvider = "equalJsonValues")
    public void testJsonEqualityPositive(BValue i, BValue j) {
        BValue[] args = { i, j };
        BValue[] returns = BRunUtil.invoke(result, "checkJsonEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal json values", dataProvider =
            "unequalJsonValues")
    public void testJsonEqualityNegative(BValue i, BValue j) {
        BValue[] args = { i, j };
        BValue[] returns = BRunUtil.invoke(result, "checkJsonEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected json values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal json arrays", dataProvider = "equalArrayValues")
    public void test1DJsonArrayEqualityPositive(BNewArray i, BNewArray j) {
        BValue[] args = { i, j };
        BValue[] returns = BRunUtil.invoke(result, "checkJsonEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected array values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal json arrays", dataProvider =
            "unequalArrayValues")
    public void test1DJsonArrayEqualityNegative(BValue i, BValue j) {
        BValue[] args = { i, j };
        BValue[] returns = BRunUtil.invoke(result, "checkJsonEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal json objects")
    public void testJsonObjectEqualityPositive() {
        BRefType jsonVal = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\"}");
        BRefType jsonValTwo = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\"}");

        BValue[] returns = BRunUtil.invoke(result, "checkJsonEquality", new BValue[]{ jsonVal, jsonValTwo });
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json objects to be identified as equal");

        jsonValTwo = JsonParser.parse("{\"helloTwo\": \"worldTwo\", \"hello\": \"world\"}");

        returns = BRunUtil.invoke(result, "checkJsonEquality", new BValue[]{ jsonVal, jsonValTwo });
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json objects to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal json values")
    public void testJsonObjectEqualityNegative() {
        BRefType jsonVal = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\"}");
        BRefType jsonValTwo = JsonParser.parse("{\"hello\": \"world\"}");

        BValue[] returns = BRunUtil.invoke(result, "checkJsonEquality", new BValue[]{ jsonVal, jsonValTwo });
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected json values to be identified as not equal");

        jsonValTwo = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\", \"helloThree\": " +
                                              "\"worldThree\"}");

        returns = BRunUtil.invoke(result, "checkJsonEquality", new BValue[]{ jsonVal, jsonValTwo });
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected json values to be identified as not equal");
    }

    @Test(description = "Test equal expression with errors")
    public void testEqualStmtNegativeCase() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "operator '==' not defined for 'int' and 'string'", 4, 12);
    }

    @DataProvider(name = "equalIntValues")
    public Object[][] equalIntValues() {
        return new Object[][] {
                { 10, 10 },
                { 20193746, 20193746 }
        };
    }

    @DataProvider(name = "unequalIntValues")
    public Object[][] unequalIntValues() {
        return new Object[][] {
                { 10, 111110 },
                { 12045966, 100000001 }
        };
    }

    @DataProvider(name = "equalFloatValues")
    public Object[][] equalFloatValues() {
        return new Object[][] {
                { 5.3, 5.3 },
                { 201937.46, 201937.46 }
        };
    }

    @DataProvider(name = "unequalFloatValues")
    public Object[][] unequalFloatValues() {
        return new Object[][] {
                { 1.234, 9.122 },
                { 1222.2, 123.2 }
        };
    }

    @DataProvider(name = "equalBooleanValues")
    public Object[][] equalBooleanValues() {
        return new Object[][] {
                { true, true },
                { false, false }
        };
    }

    @DataProvider(name = "unequalBooleanValues")
    public Object[][] unequalBooleanValues() {
        return new Object[][] {
                { true, false },
                { false, true }
        };
    }

    @DataProvider(name = "equalStringValues")
    public Object[][] equalStringValues() {
        return new Object[][] {
                { "a", "a" },
                { "Hello, from Ballerina!", "Hello, from Ballerina!" }
        };
    }

    @DataProvider(name = "unequalStringValues")
    public Object[][] unequalStringValues() {
        return new Object[][] {
                { "s", "a" },
                { "Hi here!", "Hi there!" }
        };
    }

    @DataProvider(name = "nonNilBValues")
    public Object[][] nonNilBValues() {
        return new Object[][] {
                { new BBoolean(true) },
                { new BBoolean(false) },
                { new BInteger(5) },
                { new BFloat(5.0) },
                { new BString("Hi from Ballerina!") },
                { new BMap<String, BInteger>()}
        };
    }

    @DataProvider(name = "equalArrayValues")
    public Object[][] equalArrayValues() {
        return new Object[][] {
                { new BIntArray(new long[]{1, 2, 3}), new BIntArray(new long[]{1, 2, 3})},
                { new BFloatArray(new double[]{1.11, 12.2, 3.0}), new BFloatArray(new double[]{1.11, 12.2, 3.0})},
                { new BStringArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BStringArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""})},
                { new BBooleanArray(new int[]{0, 1}), new BBooleanArray(new int[]{0, 1})}
        };
    }

    @DataProvider(name = "unequalArrayValues")
    public Object[][] unequalArrayValues() {
        return new Object[][] {
                { new BIntArray(new long[]{1, 2, 3}), new BIntArray(new long[]{3, 2, 1})},
                { new BIntArray(new long[]{1, 2, 3, 4, 5, 6}), new BIntArray(new long[]{1, 2, 3})},
                { new BIntArray(new long[]{1, 2, 3}), new BIntArray(new long[]{1, 2, 3, 4, 5, 6})},
                { new BFloatArray(new double[]{1.11, 12.2, 3.0}), new BFloatArray(new double[]{3.0, 12.2, 1.11})},
                { new BFloatArray(new double[]{1.11, 12.2, 3.0, 3.2}), new BFloatArray(new double[]{1.11, 12.2, 3.0})},
                { new BFloatArray(new double[]{1.11, 12.2, 3.0}), new BFloatArray(new double[]{1.11, 12.2, 3.0, 3.2})},
                { new BStringArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BStringArray(new String[]{"\"ballerina\"", "\"from\"", "\"hi\""})},
                { new BStringArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\"", "\"!\""}),
                        new BStringArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""})},
                { new BStringArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BStringArray(new String[]{"\"first\"", "\"hi\"", "\"from\"", "\"ballerina\""})},
                { new BBooleanArray(new int[]{0, 1}), new BBooleanArray(new int[]{1, 0})},
                { new BBooleanArray(new int[]{0, 1, 1}), new BBooleanArray(new int[]{0, 1})},
                { new BBooleanArray(new int[]{0, 1}), new BBooleanArray(new int[]{0, 1, 0})}
        };
    }

    @DataProvider(name = "equalJsonValues")
    public Object[][] equalJsonValues() {
        return new Object[][] {
                { new BInteger(1000), new BInteger(1000) },
                { new BFloat(12.34), new BFloat(12.34) },
                { new BString("Hello Ballerina"), new BString("Hello Ballerina") },
                { new BBoolean(true), new BBoolean(true) },
                { new BBoolean(false), new BBoolean(false) },
                { null, null }
        };
    }

    @DataProvider(name = "unequalJsonValues")
    public Object[][] unequalJsonValues() {
        return new Object[][] {
                { new BInteger(1000), new BInteger(50) },
                { new BFloat(12224.1), new BFloat(12.34) },
                { new BString("Hello Ballerina"), new BString("Hi Ballerina") },
                { new BBoolean(true), new BBoolean(false) },
                { new BBoolean(false), new BBoolean(true) },
        };
    }
}
