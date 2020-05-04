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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Class to test functionality of "===" and "!==".
 *
 * @since 0.985.0
 */
public class RefEqualAndNotEqualOperationsTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/ref_equal_and_not_equal_operation.bal");
        resultNegative = BCompileUtil.compile
                ("test-src/expressions/binaryoperations/ref_equal_and_not_equal_operation_negative.bal");
    }

    @Test(dataProvider = "equalBooleanValues")
    public void testBooleanRefEqualityPositive(boolean i, boolean j) {
        BValue[] args = {new BBoolean(i), new BBoolean(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBooleanRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected booleans to be identified as reference equal");
    }

    @Test(dataProvider = "unequalBooleanValues")
    public void testBooleanRefEqualityNegative(boolean i, boolean j) {
        BValue[] args = {new BBoolean(i), new BBoolean(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBooleanRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected booleans to be identified as not reference equal");
    }

    @Test(dataProvider = "equalIntValues")
    public void testIntRefEqualityPositive(int i, int j) {
        BValue[] args = {new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testIntRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be identified as reference equal");
    }

    @Test(dataProvider = "unequalIntValues")
    public void testIntRefEqualityNegative(int i, int j) {
        BValue[] args = {new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testIntRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected ints to be identified as not reference equal");
    }

    @Test(dataProvider = "equalByteValues")
    public void testByteRefEqualityPositive(int i, int j) {
        BValue[] args = {new BByte(i), new BByte(i)};
        BValue[] returns = BRunUtil.invoke(result, "testByteRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected bytes to be identified as reference equal");
    }

    @Test(dataProvider = "unequalByteValues")
    public void testByteRefEqualityNegative(int i, int j) {
        BValue[] args = {new BByte(i), new BByte(j)};
        BValue[] returns = BRunUtil.invoke(result, "testByteRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected bytes to be identified as not reference equal");
    }

    @Test(dataProvider = "equalFloatValues")
    public void testFloatRefEqualityPositive(double i, double j) {
        BValue[] args = {new BFloat(i), new BFloat(j)};
        BValue[] returns = BRunUtil.invoke(result, "testFloatRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected floats to be identified as reference equal");
    }

    @Test(dataProvider = "unequalFloatValues")
    public void testFloatRefEqualityNegative(double i, double j) {
        BValue[] args = {new BFloat(i), new BFloat(j)};
        BValue[] returns = BRunUtil.invoke(result, "testFloatRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected floats to be identified as not reference equal");
    }


    @Test(dataProvider = "equalStringValues")
    public void testStringRefEqualityPositive(String i, String j) {
        BValue[] args = {new BString(i), new BString(j)};
        BValue[] returns = BRunUtil.invoke(result, "testStringRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected strings to be identified as reference equal");
    }

    @Test(dataProvider = "unequalStringValues")
    public void testStringRefEqualityNegative(String i, String j) {
        BValue[] args = {new BString(i), new BString(j)};
        BValue[] returns = BRunUtil.invoke(result, "testStringRefEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected strings to be identified as not reference equal");
    }

    @Test
    public void testRefEqualityToNilPositive() {
        BValue[] args = {null};
        BValue[] returns = BRunUtil.invoke(result, "testRefEqualityToNil", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected nil values to be identified as reference equal");
    }

    @Test(dataProvider = "nonNilBValues")
    public void testRefEqualityToNilNegative(BValue b) {
        BValue[] returns = BRunUtil.invoke(result, "testRefEqualityToNil", new BValue[]{b});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected value to be identified as not reference equal to nil");
    }

    @Test
    public void testOpenRecordsRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testOpenRecordRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected open records to be identified as reference equal");
    }

    @Test
    public void testOpenRecordsRefEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testOpenRecordRefEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                          "Expected open records to be identified as not reference equal");
    }

    @Test
    public void testClosedRecordRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testClosedRecordRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected closed records to be identified as reference equal");
    }

    @Test
    public void testClosedRecordRefEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testClosedRecordRefEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected closed records to be identified as not reference equal");
    }

    @Test
    public void testArrayRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected array values to be identified as reference equal");
    }

    @Test
    public void testArrayRefEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayRefEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected array values to be identified as not reference equal");
    }

    @Test
    public void checkMapRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkMapRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected map values to be identified as reference equal");
    }

    @Test
    public void checkMapRefEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkMapRefEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected map values to be identified as not reference equal");
    }

    @Test
    public void checkTupleRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkTupleRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected tuple values to be identified as reference equal");
    }

    @Test
    public void checkTupleRefEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkTupleRefEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected tuple values to be identified as not reference equal");
    }

    @Test
    public void checkJsonRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "checkJsonRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected json values to be identified as reference equal");
    }

    @Test
    public void checkJsonRefEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "checkJsonRefEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected json values to be identified as not reference equal");
    }

    @Test
    public void testIntByteRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testIntByteRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected int and byte values to be identified as reference equal");
    }

    @Test
    public void testIntByteEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testIntByteEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected int and byte values to be identified as not reference equal");
    }

    @Test
    public void testXmlRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected xml values to be identified as reference equal");
    }

    @Test
    public void testXmlRefEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlRefEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected xml values to be identified as not reference equal");
    }

    @Test
    public void testObjectRefEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectRefEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected objects to be identified as reference equal");
    }

    @Test
    public void testObjectRefEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectRefEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected objects to be identified as not reference equal");
    }

    @Test
    public void testValueTypeAndRefTypeEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypeAndRefTypeEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected values to be identified as reference equal");
    }

    @Test
    public void testValueTypeAndRefTypeEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypeAndRefTypeEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected values to be identified as not reference equal");
    }

    @Test
    public void testValueTypesAsRefTypesEqualityPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypesAsRefTypesEqualityPositive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(),
                          "Expected values to be identified as reference equal");
    }

    @Test
    public void testValueTypesAsRefTypesEqualityNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypesAsRefTypesEqualityNegative", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(),
                           "Expected values to be identified as not reference equal");
    }

    @Test
    public void testXMLSequenceRefEquality() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLSequenceRefEquality");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testXMLSequenceRefEqualityFalse() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLSequenceRefEqualityFalse");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testXMLSequenceRefEqualityDifferentLength() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLSequenceRefEqualityDifferentLength");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testXMLSequenceRefEqualityIncludingString() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLSequenceRefEqualityIncludingString");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testXMLSequenceRefEqualityIncludingDifferentString() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLSequenceRefEqualityIncludingDifferentString");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testEmptyXMLSequencesRefEquality() {
        BValue[] returns = BRunUtil.invoke(result, "testEmptyXMLSequencesRefEquality");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test reference equal with errors")
    public void testRefEqualNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 22);
        validateError(resultNegative, 0, "operator '===' not defined for 'int' and 'string'", 20, 12);
        validateError(resultNegative, 1, "operator '!==' not defined for 'int' and 'string'", 20, 25);
        validateError(resultNegative, 2, "operator '===' not defined for 'int[2]' and 'string[2]'", 26, 21);
        validateError(resultNegative, 3, "operator '!==' not defined for 'int[2]' and 'string[2]'", 26, 34);
        validateError(resultNegative, 4, "operator '===' not defined for '(float|int)?[]' and '(boolean|xml)?[]'", 30,
                      21);
        validateError(resultNegative, 5, "operator '!==' not defined for '(float|int)?[]' and '(boolean|xml)?[]'", 30,
                      34);
        validateError(resultNegative, 6, "operator '===' not defined for 'map<int>' and 'map<float>'", 38, 21);
        validateError(resultNegative, 7, "operator '!==' not defined for 'map<int>' and 'map<float>'", 38, 34);
        validateError(resultNegative, 8, "operator '===' not defined for 'map<(string|int)>' and 'map<float>'", 42,
                      21);
        validateError(resultNegative, 9, "operator '!==' not defined for 'map<(string|int)>' and 'map<float>'", 42,
                      34);
        validateError(resultNegative, 10, "operator '===' not defined for '[string,int]' and '[boolean,float]'", 50,
                      21);
        validateError(resultNegative, 11, "operator '!==' not defined for '[string,int]' and '[boolean,float]'", 50,
                      34);
        validateError(resultNegative, 12, "operator '===' not defined for '[(float|int),int]' and '[boolean,int]'",
                      54, 21);
        validateError(resultNegative, 13, "operator '!==' not defined for '[boolean,int]' and '[(float|int),int]'",
                      54, 34);
        validateError(resultNegative, 14, "operator '===' not defined for 'Employee' and 'Person'", 62, 12);
        validateError(resultNegative, 15, "operator '!==' not defined for 'Person' and 'Employee'", 62, 25);
        validateError(resultNegative, 16, "operator '===' not defined for '[string,int]' and 'json'",
                      68, 21);
        validateError(resultNegative, 17, "operator '!==' not defined for 'json' and '[string,int]'",
                      68, 34);
        validateError(resultNegative, 18, "operator '===' not defined for '(Employee|[string,int])' and 'json'",
                      72, 21);
        validateError(resultNegative, 19, "operator '!==' not defined for '(Employee|[string,int])' and 'json'",
                      72, 34);
        validateError(resultNegative, 20, "operator '===' not defined for 'Abc' and 'Def'", 80, 12);
        validateError(resultNegative, 21, "operator '!==' not defined for 'Def' and 'Abc'", 80, 25);
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
}
