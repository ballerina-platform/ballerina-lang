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
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BNewArray;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Class to test functionality of "==" and "!=".
 *
 * @since 0.985.0
 */
public class EqualAndNotEqualOperationsTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/equal_and_not_equal_operation.bal");
        resultNegative = BCompileUtil.compile
                ("test-src/expressions/binaryoperations/equal_and_not_equal_operation_negative.bal");
    }

    @Test(description = "Test equals/unequals operation with two equal booleans", dataProvider = "equalBooleanValues")
    public void testBooleanEqualityPositive(boolean i, boolean j) {
        BValue[] args = {new BBoolean(i), new BBoolean(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkBooleanEqualityPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal booleans",
            dataProvider = "unequalBooleanValues")
    public void testBooleanEqualityNegative(boolean i, boolean j) {
        BValue[] args = {new BBoolean(i), new BBoolean(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkBooleanEqualityNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal ints", dataProvider = "equalIntValues")
    public void testIntEqualityPositive(int i, int j) {
        BValue[] args = {new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkIntEqualityPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal ints", dataProvider = "unequalIntValues")
    public void testIntEqualityNegative(int i, int j) {
        BValue[] args = {new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkIntEqualityNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected ints to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal bytes", dataProvider = "equalByteValues")
    public void testByteEqualityPositive(int i, int j) {
        BValue[] args = {new BByte(i), new BByte(i)};
        BValue[] returns = BRunUtil.invoke(result, "checkByteEqualityPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected bytes to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal bytes", dataProvider = "unequalByteValues")
    public void testByteEqualityNegative(int i, int j) {
        BValue[] args = {new BByte(i), new BByte(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkByteEqualityNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected bytes to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal floats", dataProvider = "equalFloatValues")
    public void testFloatEqualityPositive(double i, double j) {
        BValue[] args = {new BFloat(i), new BFloat(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkFloatEqualityPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected floats to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal floats", dataProvider = "unequalFloatValues")
    public void testFloatEqualityNegative(double i, double j) {
        BValue[] args = {new BFloat(i), new BFloat(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkFloatEqualityNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected floats to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal strings", dataProvider = "equalStringValues")
    public void testStringEqualityPositive(String i, String j) {
        BValue[] args = {new BString(i), new BString(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkStringEqualityPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected strings to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal strings",
            dataProvider = "unequalStringValues")
    public void testStringEqualityNegative(String i, String j) {
        BValue[] args = {new BString(i), new BString(j)};
        BValue[] returns = BRunUtil.invoke(result, "checkStringEqualityNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected strings to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with nil")
    public void testEqualityToNilPositive() {
        BValue[] args = {null};
        BValue[] returns = BRunUtil.invoke(result, "checkEqualityToNilPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected nil values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with nil and non-nil values", dataProvider = "nonNilBValues")
    public void testEqualityToNilNegative(BValue b) {
        BValue[] returns = BRunUtil.invoke(result, "checkEqualityToNilNegative", new BValue[]{b});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected value to be identified as not equal to " +
                "nil");
    }

    @Test(description = "Test equals/unequals operation with two equal errors")
    public void testErrorEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected errors to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal errors")
    public void testErrorEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected errors to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal open records")
    public void testOpenRecordsEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkOpenRecordEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected open records to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal open records")
    public void testOpenRecordsEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkOpenRecordEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected open records to be identified as not " +
                "equal");
    }

    @Test
    public void testOpenRecordWithOptionalFieldsEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testOpenRecordWithOptionalFieldsEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected open records to be identified as equal");
    }

    @Test
    public void testOpenRecordWithOptionalFieldsEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testOpenRecordWithOptionalFieldsEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected open records to be identified as not " +
                "equal");
    }

    @Test
    public void testClosedRecordWithOptionalFieldsEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testClosedRecordWithOptionalFieldsEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected open records to be identified as equal");
    }

    @Test
    public void testClosedRecordWithOptionalFieldsEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testClosedRecordWithOptionalFieldsEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected open records to be identified as not " +
                "equal");
    }

    @Test(description = "Test equals/unequals operation with two equal closed records")
    public void testClosedRecordsEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkClosedRecordEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected closed records to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal closed records")
    public void testClosedRecordsEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkClosedRecordEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected closed records to be identified as " +
                "not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal arrays", dataProvider = "equalArrayValues")
    public void test1DArrayEqualityPositive(BNewArray i, BNewArray j) {
        BValue[] args = {i, j};
        BValue[] returns = BRunUtil.invoke(result, "check1DArrayEqualityPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected array values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal arrays", dataProvider =
            "unequalArrayValues")
    public void test1DArrayEqualityNegative(BValue i, BValue j) {
        BValue[] args = {i, j};
        BValue[] returns = BRunUtil.invoke(result, "check1DArrayEqualityNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with equal closed arrays")
    public void test1DClosedArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "check1DClosedArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected closed array values to be identified as " +
                "equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal closed arrays")
    public void test1DClosedArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "check1DClosedArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected closed array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal any arrays")
    public void test1DAnyArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "check1DAnyArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected any array values to be identified as " +
                "equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal any arrays")
    public void test1DAnyArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "check1DAnyArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected any array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with equal open and closed arrays")
    public void testOpenClosedArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkOpenClosedArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected array values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with unequal open and closed arrays")
    public void testOpenClosedArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkOpenClosedArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected array values to be identified as " +
                "not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal 2D boolean arrays")
    public void test2DBooleanArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "check2DBooleanArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected 2D boolean array values to be identified" +
                " as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal 2D boolean arrays")
    public void test2DBooleanArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "check2DBooleanArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected 2D boolean array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal 2D int arrays")
    public void test2DIntArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "check2DIntArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected 2D int array values to be identified as " +
                "equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal 2D int arrays")
    public void test2DIntArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "check2DIntArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected 2D int array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal 2D byte arrays")
    public void test2DByteArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "check2DByteArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected 2D byte array values to be identified as" +
                " equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal 2D byte arrays")
    public void test2DByteArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "check2DByteArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected 2D byte array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal 2D float arrays")
    public void test2DFloatArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "check2DFloatArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected 2D float array values to be identified " +
                "as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal 2D float arrays")
    public void test2DFloatArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "check2DFloatArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected 2D float array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal 2D string arrays")
    public void test2DStringArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "check2DStringArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected 2D string array values to be identified " +
                "as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal 2D string arrays")
    public void test2DStringArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "check2DStringArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected 2D string array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two complex 2D arrays")
    public void testComplex2DArrayEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkComplex2DArrayEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected complex 2D array values to be identified" +
                " as equal");
    }

    @Test(description = "Test equals/unequals operation with two complex 2D arrays")
    public void testComplex2DArrayEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkComplex2DArrayEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected complex 2D array values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal map values")
    public void testMapEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkMapEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected map values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal map values")
    public void testMapEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkMapEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected map values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal complex map values")
    public void testComplexMapEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkComplexMapEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected complex map values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal map values")
    public void testComplexMapEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkComplexMapEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected complex map values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal tuple values")
    public void testTupleEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkTupleEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected tuple values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal tuple values")
    public void testTupleEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkTupleEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected tuple values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal json values", dataProvider = "equalJsonValues")
    public void testJsonEqualityPositive(BValue i, BValue j) {
        BValue[] args = {i, j};
        BValue[] returns = BRunUtil.invoke(result, "checkJsonEqualityPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal json values", dataProvider =
            "unequalJsonValues")
    public void testJsonEqualityNegative(BValue i, BValue j) {
        BValue[] args = {i, j};
        BValue[] returns = BRunUtil.invoke(result, "checkJsonEqualityNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected json values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal json arrays", dataProvider = "equalArrayValues")
    public void test1DJsonArrayEqualityPositive(BNewArray i, BNewArray j) {
        BValue[] args = {i, j};
        BValue[] returns = BRunUtil.invoke(result, "checkJsonEqualityPositive", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected array values to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal json arrays", dataProvider =
            "unequalArrayValues")
    public void test1DJsonArrayEqualityNegative(BValue i, BValue j) {
        BValue[] args = {i, j};
        BValue[] returns = BRunUtil.invoke(result, "checkJsonEqualityNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected array values to be identified as not equal");
    }

    @Test
    public void testIntByteEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testIntByteEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected int and byte values to be identified as equal");
    }

    @Test
    public void testIntByteEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testIntByteEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected int and byte values to be identified as not equal");
    }

    @Test
    public void testPrimitiveAndJsonEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testPrimitiveAndJsonEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected primitives and json values to be identified as equal");
    }

    @Test
    public void testPrimitiveAndJsonEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testPrimitiveAndJsonEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected primitives and json values to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two union constrained maps")
    public void testUnionConstrainedMapsPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkUnionConstrainedMapsPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected union constrained maps to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal union constrained maps")
    public void testUnionConstrainedMapsNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkUnionConstrainedMapsNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected union constrained maps to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two union arrays")
    public void testUnionArrayPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkUnionArrayPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected union arrays to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal union arrays")
    public void testUnionArrayNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkUnionArrayNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected union arrays to be identified as not equal");
    }


    @Test(description = "Test equals/unequals operation with two tuples with union type members")
    public void testTupleWithUnionPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkTupleWithUnionPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected tuples with union type members to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal tuples with union type members")
    public void testTupleWithUnionNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkTupleWithUnionNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected tuples with union type members to be identified as not equal");
    }

    @Test(description = "Test equals/unequals operation with two equal json objects")
    public void testJsonObjectEqualityPositive() {
        BRefType jsonVal = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\"}");
        BRefType jsonValTwo = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\"}");

        BValue[] returns = BRunUtil.invoke(result, "checkJsonEqualityPositive", new BValue[]{jsonVal, jsonValTwo});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json objects to be identified as equal");

        jsonValTwo = JsonParser.parse("{\"helloTwo\": \"worldTwo\", \"hello\": \"world\"}");

        returns = BRunUtil.invoke(result, "checkJsonEqualityPositive", new BValue[]{jsonVal, jsonValTwo});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json objects to be identified as equal");
    }

    @Test(description = "Test equals/unequals operation with two unequal json values")
    public void testJsonObjectEqualityNegative() {
        BRefType jsonVal = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\"}");
        BRefType jsonValTwo = JsonParser.parse("{\"hello\": \"world\"}");

        BValue[] returns = BRunUtil.invoke(result, "checkJsonEqualityNegative", new BValue[]{jsonVal, jsonValTwo});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected json values to be identified as not equal");

        jsonValTwo = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\", \"helloThree\": " +
                                              "\"worldThree\"}");

        returns = BRunUtil.invoke(result, "checkJsonEqualityNegative", new BValue[]{jsonVal, jsonValTwo});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected json values to be identified as not equal");
    }

    @Test
    public void testSimpleXmlPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleXmlPositive");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as equal.");
    }

    @Test
    public void testSimpleXmlNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleXmlNegative");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as not equal.");
    }

    @Test
    public void testEqualNestedXml() {
        BValue[] returns = BRunUtil.invoke(result, "testEqualNestedXml");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as equal.");
    }

    @Test
    public void testUnequalNestedXml() {
        BValue[] returns = BRunUtil.invoke(result, "testUnequalNestedXml");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }

    @Test
    public void testEqualXmlWithComments() {
        BValue[] returns = BRunUtil.invoke(result, "testEqualXmlWithComments");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as equal.");
    }

    @Test
    public void testUnequalXmlWithUnequalComment() {
        BValue[] returns = BRunUtil.invoke(result, "testUnequalXmlWithUnequalComment");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }

    @Test
    public void testEqualXmlIgnoringAttributeOrder() {
        BValue[] returns = BRunUtil.invoke(result, "testEqualXmlIgnoringAttributeOrder");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as equal.");
    }

    @Test
    public void testUnequalXmlIgnoringAttributeOrder() {
        BValue[] returns = BRunUtil.invoke(result, "testUnequalXmlIgnoringAttributeOrder");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }

    @Test
    public void testEqualXmlWithPI() {
        BValue[] returns = BRunUtil.invoke(result, "testEqualXmlWithPI");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as equal.");
    }

    @Test
    public void testUnequalXmlWithUnequalPI() {
        BValue[] returns = BRunUtil.invoke(result, "testUnequalXmlWithUnequalPI");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }
    @Test
    public void testUnequalXmlWithPIInWrongOrder() {
        BValue[] returns = BRunUtil.invoke(result, "testUnequalXmlWithPIInWrongOrder");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }

    @Test
    public void testUnequalXmlWithMultiplePIInWrongOrder() {
        BValue[] returns = BRunUtil.invoke(result, "testUnequalXmlWithMultiplePIInWrongOrder");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }
    @Test
    public void testUnequalXmlWithMissingPI() {
        BValue[] returns = BRunUtil.invoke(result, "testUnequalXmlWithMissingPI");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }

    @Test
    public void testXmlWithNamespacesPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlWithNamespacesPositive");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as equal.");
    }

    @Test
    public void testXmlWithNamespacesNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlWithNamespacesNegative");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }

    @Test
    public void testXmlSequenceAndXmlItemEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlSequenceAndXmlItemEqualityPositive");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as equal.");
    }

    @Test
    public void testXmlSequenceAndXmlItemEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlSequenceAndXmlItemEqualityNegative");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected XMLs to be identified as unequal.");
    }

    @Test
    public void testJsonRecordMapEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonRecordMapEqualityPositive");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected JSON/record/map values to be identified " +
                "as equal.");
    }

    @Test
    public void testJsonRecordMapEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonRecordMapEqualityNegative");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected JSON/record/map values to be identified" +
                " as unequal.");
    }

    @Test
    public void testArrayTupleEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayTupleEqualityPositive");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected array and tuple values to be identified" +
                " as equal.");
    }

    @Test
    public void testArrayTupleEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayTupleEqualityNegative");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected array and tuple values to be identified" +
                " as unequal.");
    }

    @Test(dataProvider = "selfAndCyclicReferencingPositiveFunctions")
    public void selfAndCyclicReferencingPositiveFunctions(String testFunctionName) {
        BValue[] returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected values to be identified as equal.");
    }

    @Test(dataProvider = "selfAndCyclicReferencingNegativeFunctions")
    public void selfAndCyclicReferencingNegativeFunctions(String testFunctionName) {
        BValue[] returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected values to be identified as unequal.");
    }

    @Test
    public void testEmptyMapAndRecordEquality() {
        BValue[] returns = BRunUtil.invoke(result, "testEmptyMapAndRecordEquality");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected values to be identified as equal.");
    }

    @Test(description = "Test equal and not equal with errors")
    public void testEqualAndNotEqualNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 35);
        validateError(resultNegative, 0, "operator '==' not defined for 'int' and 'string'", 20, 12);
        validateError(resultNegative, 1, "operator '!=' not defined for 'int' and 'string'", 20, 24);
        validateError(resultNegative, 2, "operator '==' not defined for 'int[2]' and 'string[2]'", 26, 21);
        validateError(resultNegative, 3, "operator '!=' not defined for 'int[2]' and 'string[2]'", 26, 33);
        validateError(resultNegative, 4, "operator '==' not defined for 'map<int>' and 'map<float>'", 38, 21);
        validateError(resultNegative, 5, "operator '!=' not defined for 'map<int>' and 'map<float>'", 38, 33);
        validateError(resultNegative, 6, "operator '==' not defined for 'map<(string|int)>' and 'map<float>'",
                      42, 21);
        validateError(resultNegative, 7, "operator '!=' not defined for 'map<(string|int)>' and 'map<float>'",
                      42, 33);
        validateError(resultNegative, 8, "operator '==' not defined for '[string,int]' and '[boolean,float]'",
                      50, 21);
        validateError(resultNegative, 9, "operator '!=' not defined for '[string,int]' and '[boolean,float]'",
                      50, 33);
        validateError(resultNegative, 10, "operator '==' not defined for '[(float|int),int]' and '[boolean,int]'",
                      54, 21);
        validateError(resultNegative, 11, "operator '!=' not defined for '[(float|int),int]' and '[boolean,int]'",
                      54, 33);
        validateError(resultNegative, 12, "operator '==' not defined for 'Employee' and 'Person'", 62, 17);
        validateError(resultNegative, 13, "operator '!=' not defined for 'Employee' and 'Person'", 62, 29);
        validateError(resultNegative, 14, "operator '==' not defined for 'EmployeeWithOptionalId' and " +
                "'PersonWithOptionalId'", 66, 17);
        validateError(resultNegative, 15, "operator '!=' not defined for 'EmployeeWithOptionalId' and " +
                "'PersonWithOptionalId'", 66, 31);
        validateError(resultNegative, 16, "operator '==' not defined for '[string,int]' and 'json'", 72, 21);
        validateError(resultNegative, 17, "operator '!=' not defined for '[string,int]' and 'json'", 72, 31);
        validateError(resultNegative, 18, "operator '==' not defined for '[string,int][]' and 'json'", 76, 21);
        validateError(resultNegative, 19, "operator '!=' not defined for '[string,int][]' and 'json'", 76, 31);
        validateError(resultNegative, 20, "operator '==' not defined for 'map<boolean>' and 'ClosedDept'", 87, 23);
        validateError(resultNegative, 21, "operator '!=' not defined for 'ClosedDept' and 'map<boolean>'", 87, 35);
        validateError(resultNegative, 22, "operator '==' not defined for 'int[]' and '[float,float]'", 94, 23);
        validateError(resultNegative, 23, "operator '!=' not defined for 'int[]' and '[float,float]'", 94, 35);
        validateError(resultNegative, 24, "operator '==' not defined for 'int[]' and '[int,float]'", 97, 23);
        validateError(resultNegative, 25, "operator '!=' not defined for '[int,float]' and 'int[]'", 97, 35);
        validateError(resultNegative, 26, "operator '==' not defined for '[int,map<int>]' and '[int,float]'", 111,
                      23);
        validateError(resultNegative, 27, "operator '!=' not defined for '[int,float]' and '[int,map<int>]'", 111,
                      35);
        validateError(resultNegative, 28, "operator '==' not defined for 'any' and 'int'", 115, 15);
        validateError(resultNegative, 29, "operator '!=' not defined for 'int' and 'any'", 115, 27);
        validateError(resultNegative, 30, "operator '==' not defined for 'map<(int|string)>' and 'map'", 119, 15);
        validateError(resultNegative, 31, "operator '!=' not defined for 'map' and 'map<(int|string)>'", 119, 27);

        //TODO Table remove - Fix
//        validateError(resultNegative, 32, "equality not yet supported for type 'table'", 131, 17);
//        validateError(resultNegative, 33, "equality not yet supported for type 'table'", 132, 9);
        validateError(resultNegative, 32, "operator '==' not defined for 'Employee' and '()'", 166, 9);
        validateError(resultNegative, 33, "operator '==' not defined for 'Foo' and '()'", 172, 9);
        validateError(resultNegative, 34, "operator '==' not defined for 'function () returns (string)' and '()'",
                      178, 9);
    }

    @DataProvider(name = "equalIntValues")
    public Object[][] equalIntValues() {
        return new Object[][]{
                {10, 10},
                {20193746, 20193746}
        };
    }

    @DataProvider(name = "unequalIntValues")
    public Object[][] unequalIntValues() {
        return new Object[][]{
                {10, 111110},
                {12045966, 100000001}
        };
    }

    @DataProvider(name = "equalByteValues")
    public Object[][] equalByteValues() {
        return new Object[][]{
                {0, 0},
                {10, 10},
                {255, 255}
        };
    }

    @DataProvider(name = "unequalByteValues")
    public Object[][] unequalByteValues() {
        return new Object[][]{
                {0, 255},
                {12, 122}
        };
    }

    @DataProvider(name = "equalFloatValues")
    public Object[][] equalFloatValues() {
        return new Object[][]{
                {5.3, 5.3},
                {201937.46, 201937.46}
        };
    }

    @DataProvider(name = "unequalFloatValues")
    public Object[][] unequalFloatValues() {
        return new Object[][]{
                {1.234, 9.122},
                {1222.2, 123.2}
        };
    }

    @DataProvider(name = "equalBooleanValues")
    public Object[][] equalBooleanValues() {
        return new Object[][]{
                {true, true},
                {false, false}
        };
    }

    @DataProvider(name = "unequalBooleanValues")
    public Object[][] unequalBooleanValues() {
        return new Object[][]{
                {true, false},
                {false, true}
        };
    }

    @DataProvider(name = "equalStringValues")
    public Object[][] equalStringValues() {
        return new Object[][]{
                {"a", "a"},
                {"Hello, from Ballerina!", "Hello, from Ballerina!"}
        };
    }

    @DataProvider(name = "unequalStringValues")
    public Object[][] unequalStringValues() {
        return new Object[][]{
                {"s", "a"},
                {"Hi here!", "Hi there!"}
        };
    }

    @DataProvider(name = "nonNilBValues")
    public Object[][] nonNilBValues() {
        return new Object[][]{
                {new BBoolean(true)},
                {new BBoolean(false)},
                {new BInteger(5)},
                {new BFloat(5.0)},
                {new BString("Hi from Ballerina!")},
                {new BMap<String, BInteger>()}
        };
    }

    @DataProvider(name = "equalArrayValues")
    public Object[][] equalArrayValues() {
        return new Object[][]{
                {new BValueArray(new long[]{1, 2, 3}), new BValueArray(new long[]{1, 2, 3})},
                {new BValueArray(new double[]{1.11, 12.2, 3.0}), new BValueArray(new double[]{1.11, 12.2, 3.0})},
                {new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""})},
                {new BValueArray(new int[]{0, 1}), new BValueArray(new int[]{0, 1})},
                {new BValueArray(new byte[]{0, 25, 23}), new BValueArray(new byte[]{0, 25, 23})}
        };
    }

    @DataProvider(name = "unequalArrayValues")
    public Object[][] unequalArrayValues() {
        return new Object[][]{
                {new BValueArray(new long[]{1, 2, 3}), new BValueArray(new long[]{3, 2, 1})},
                {new BValueArray(new long[]{1, 2, 3, 4, 5, 6}), new BValueArray(new long[]{1, 2, 3})},
                {new BValueArray(new long[]{1, 2, 3}), new BValueArray(new long[]{1, 2, 3, 4, 5, 6})},
                {new BValueArray(new double[]{1.11, 12.2, 3.0}), new BValueArray(new double[]{3.0, 12.2, 1.11})},
                {new BValueArray(new double[]{1.11, 12.2, 3.0, 3.2}), new BValueArray(new double[]{1.11, 12.2, 3.0})},
                {new BValueArray(new double[]{1.11, 12.2, 3.0}), new BValueArray(new double[]{1.11, 12.2, 3.0, 3.2})},
                {new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BValueArray(new String[]{"\"ballerina\"", "\"from\"", "\"hi\""})},
                {new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\"", "\"!\""}),
                        new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""})},
                {new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BValueArray(new String[]{"\"first\"", "\"hi\"", "\"from\"", "\"ballerina\""})},
                {new BValueArray(new int[]{0, 1}), new BValueArray(new int[]{1, 0})},
                {new BValueArray(new int[]{0, 1, 1}), new BValueArray(new int[]{0, 1})},
                {new BValueArray(new int[]{0, 1}), new BValueArray(new int[]{0, 1, 0})},
                {new BValueArray(new byte[]{0, 123, 22}), new BValueArray(new byte[]{0, 22, 123})},
                {new BValueArray(new byte[]{0, 123, 22, 9}), new BValueArray(new byte[]{0, 123, 22})},
                {new BValueArray(new byte[]{0, 123, 22}), new BValueArray(new byte[]{0, 123})}
        };
    }

    @DataProvider(name = "equalJsonValues")
    public Object[][] equalJsonValues() {
        return new Object[][]{
                {new BInteger(1000), new BInteger(1000)},
                {new BFloat(12.34), new BFloat(12.34)},
                {new BString("Hello Ballerina"), new BString("Hello Ballerina")},
                {new BBoolean(true), new BBoolean(true)},
                {new BBoolean(false), new BBoolean(false)},
                {null, null}
        };
    }

    @DataProvider(name = "unequalJsonValues")
    public Object[][] unequalJsonValues() {
        return new Object[][]{
                {new BInteger(1000), new BInteger(50)},
                {new BFloat(12224.1), new BFloat(12.34)},
                {new BString("Hello Ballerina"), new BString("Hi Ballerina")},
                {new BBoolean(true), new BBoolean(false)},
                {new BBoolean(false), new BBoolean(true)},
        };
    }

    @DataProvider(name = "selfAndCyclicReferencingPositiveFunctions")
    public Object[][] selfAndCyclicReferencingPositiveFunctions() {
        return new Object[][]{
                {"testSelfAndCyclicReferencingMapEqualityPositive"},
                {"testSelfAndCyclicReferencingJsonEqualityPositive"},
                {"testSelfAndCyclicReferencingArrayEqualityPositive"},
                {"testSelfAndCyclicReferencingTupleEqualityPositive"}
        };
    }

    @DataProvider(name = "selfAndCyclicReferencingNegativeFunctions")
    public Object[][] selfAndCyclicReferencingNegativeFunctions() {
        return new Object[][]{
                {"testSelfAndCyclicReferencingMapEqualityNegative"},
                {"testSelfAndCyclicReferencingJsonEqualityNegative"},
                {"testSelfAndCyclicReferencingArrayEqualityNegative"},
                {"testSelfAndCyclicReferencingTupleEqualityNegative"}
        };
    }
}
