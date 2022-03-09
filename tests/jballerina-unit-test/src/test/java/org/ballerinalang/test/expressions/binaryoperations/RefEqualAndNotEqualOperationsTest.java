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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

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
        Object[] args = {(i), (j)};
        Object returns = BRunUtil.invoke(result, "testBooleanRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected booleans to be identified as reference equal");
    }

    @Test(dataProvider = "unequalBooleanValues")
    public void testBooleanRefEqualityNegative(boolean i, boolean j) {
        Object[] args = {(i), (j)};
        Object returns = BRunUtil.invoke(result, "testBooleanRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected booleans to be identified as not reference equal");
    }

    @Test(dataProvider = "equalIntValues")
    public void testIntRefEqualityPositive(int i, int j) {
        Object[] args = {(i), (j)};
        Object returns = BRunUtil.invoke(result, "testIntRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns, "Expected ints to be identified as reference equal");
    }

    @Test(dataProvider = "unequalIntValues")
    public void testIntRefEqualityNegative(int i, int j) {
        Object[] args = {(i), (j)};
        Object returns = BRunUtil.invoke(result, "testIntRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected ints to be identified as not reference equal");
    }

    @Test(dataProvider = "equalByteValues")
    public void testByteRefEqualityPositive(int i, int j) {
        Object[] args = {(i), (i)};
        Object returns = BRunUtil.invoke(result, "testByteRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected bytes to be identified as reference equal");
    }

    @Test(dataProvider = "unequalByteValues")
    public void testByteRefEqualityNegative(int i, int j) {
        Object[] args = {(i), (j)};
        Object returns = BRunUtil.invoke(result, "testByteRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected bytes to be identified as not reference equal");
    }

    @Test(dataProvider = "equalFloatValues")
    public void testFloatRefEqualityPositive(double i, double j) {
        Object[] args = {(i), (j)};
        Object returns = BRunUtil.invoke(result, "testFloatRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected floats to be identified as reference equal");
    }

    @Test(dataProvider = "unequalFloatValues")
    public void testFloatRefEqualityNegative(double i, double j) {
        Object[] args = {(i), (j)};
        Object returns = BRunUtil.invoke(result, "testFloatRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected floats to be identified as not reference equal");
    }


    @Test(dataProvider = "equalStringValues")
    public void testStringRefEqualityPositive(String i, String j) {
        Object[] args = {StringUtils.fromString(i), StringUtils.fromString(j)};
        Object returns = BRunUtil.invoke(result, "testStringRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected strings to be identified as reference equal");
    }

    @Test(dataProvider = "unequalStringValues")
    public void testStringRefEqualityNegative(String i, String j) {
        Object[] args = {StringUtils.fromString(i), StringUtils.fromString(j)};
        Object returns = BRunUtil.invoke(result, "testStringRefEquality", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected strings to be identified as not reference equal");
    }

    @Test
    public void testRefEqualityToNilPositive() {
        Object[] args = {null};
        Object returns = BRunUtil.invoke(result, "testRefEqualityToNil", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected nil values to be identified as reference equal");
    }

    @Test(dataProvider = "nonNilObjects")
    public void testRefEqualityToNilNegative(Object b) {
        Object returns = BRunUtil.invoke(result, "testRefEqualityToNil", new Object[]{b});
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected value to be identified as not reference equal to nil");
    }

    @Test
    public void testOpenRecordsRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "testOpenRecordRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected open records to be identified as reference equal");
    }

    @Test
    public void testOpenRecordsRefEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "testOpenRecordRefEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                          "Expected open records to be identified as not reference equal");
    }

    @Test
    public void testClosedRecordRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "testClosedRecordRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected closed records to be identified as reference equal");
    }

    @Test
    public void testClosedRecordRefEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "testClosedRecordRefEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected closed records to be identified as not reference equal");
    }

    @Test
    public void testArrayRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "testArrayRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected array values to be identified as reference equal");
    }

    @Test
    public void testArrayRefEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "testArrayRefEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected array values to be identified as not reference equal");
    }

    @Test
    public void checkMapRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "checkMapRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected map values to be identified as reference equal");
    }

    @Test
    public void checkMapRefEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "checkMapRefEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected map values to be identified as not reference equal");
    }

    @Test
    public void checkTupleRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "checkTupleRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected tuple values to be identified as reference equal");
    }

    @Test
    public void checkTupleRefEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "checkTupleRefEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected tuple values to be identified as not reference equal");
    }

    @Test
    public void checkJsonRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "checkJsonRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected json values to be identified as reference equal");
    }

    @Test
    public void checkJsonRefEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "checkJsonRefEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected json values to be identified as not reference equal");
    }

    @Test
    public void testIntByteRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "testIntByteRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected int and byte values to be identified as reference equal");
    }

    @Test
    public void testIntByteEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "testIntByteEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected int and byte values to be identified as not reference equal");
    }

    @Test
    public void testXmlRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "testXmlRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected xml values to be identified as reference equal");
    }

    @Test
    public void testXmlRefEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "testXmlRefEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected xml values to be identified as not reference equal");
    }

    @Test
    public void testObjectRefEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "testObjectRefEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected objects to be identified as reference equal");
    }

    @Test
    public void testObjectRefEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "testObjectRefEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected objects to be identified as not reference equal");
    }

    @Test
    public void testValueTypeAndRefTypeEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "testValueTypeAndRefTypeEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected values to be identified as reference equal");
    }

    @Test
    public void testValueTypeAndRefTypeEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "testValueTypeAndRefTypeEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected values to be identified as not reference equal");
    }

    @Test
    public void testValueTypesAsRefTypesEqualityPositive() {
        Object returns = BRunUtil.invoke(result, "testValueTypesAsRefTypesEqualityPositive", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns,
                          "Expected values to be identified as reference equal");
    }

    @Test
    public void testValueTypesAsRefTypesEqualityNegative() {
        Object returns = BRunUtil.invoke(result, "testValueTypesAsRefTypesEqualityNegative", new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns,
                           "Expected values to be identified as not reference equal");
    }

    @Test
    public void testTupleJSONRefEquality() {
        BRunUtil.invoke(result, "testTupleJSONRefEquality");
    }

    @Test
    public void testIntersectingUnionRefEquality() {
        BRunUtil.invoke(result, "testIntersectingUnionRefEquality");
    }

    @Test(dataProvider = "functionsWithXmlExactEqualityChecks")
    public void testXmlExactEquality(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider
    public Object[] functionsWithXmlExactEqualityChecks() {
        return new String[] {
                "testXmlElementRefEquality",
                "testXmlCommentRefEquality",
                "testXmlProcessingInstructionRefEquality",
                "testXMLSequenceRefEquality",
                "testXMLSequenceRefEqualityFalse",
                "testXMLSequenceRefEqualityDifferentLength",
                "testXMLSequenceRefEqualityIncludingString",
                "testXMLSequenceRefEqualityIncludingDifferentString",
                "testEmptyXMLSequencesRefEquality",
                "testXmlTextRefEquality"
        };
    }

    @Test(description = "Test reference equal with errors")
    public void testRefEqualNegativeCases() {
        int i = 0;
        validateError(resultNegative, i++, "operator '===' not defined for 'int' and 'string'", 20, 12);
        validateError(resultNegative, i++, "operator '!==' not defined for 'int' and 'string'", 20, 25);
        validateError(resultNegative, i++, "operator '===' not defined for 'int[2]' and 'string[2]'", 26, 21);
        validateError(resultNegative, i++, "operator '!==' not defined for 'int[2]' and 'string[2]'", 26, 34);
        validateError(resultNegative, i++, "operator '===' not defined for '(float|int)?[]' and '(boolean|xml)?[]'", 30,
                      21);
        validateError(resultNegative, i++, "operator '!==' not defined for '(float|int)?[]' and '(boolean|xml)?[]'", 30,
                      34);
        validateError(resultNegative, i++, "operator '===' not defined for 'map<int>' and 'map<float>'", 38, 21);
        validateError(resultNegative, i++, "operator '!==' not defined for 'map<int>' and 'map<float>'", 38, 34);
        validateError(resultNegative, i++, "operator '===' not defined for 'map<(string|int)>' and 'map<float>'", 42,
                      21);
        validateError(resultNegative, i++, "operator '!==' not defined for 'map<(string|int)>' and 'map<float>'", 42,
                      34);
        validateError(resultNegative, i++, "operator '===' not defined for '[string,int]' and '[boolean,float]'", 50,
                      21);
        validateError(resultNegative, i++, "operator '!==' not defined for '[string,int]' and '[boolean,float]'", 50,
                      34);
        validateError(resultNegative, i++, "operator '===' not defined for '[(float|int),int]' and '[boolean,int]'",
                      54, 21);
        validateError(resultNegative, i++, "operator '!==' not defined for '[boolean,int]' and '[(float|int),int]'",
                      54, 34);
        validateError(resultNegative, i++, "operator '===' not defined for 'Employee' and 'Person'", 62, 12);
        validateError(resultNegative, i++, "operator '!==' not defined for 'Person' and 'Employee'", 62, 25);
        validateError(resultNegative, i++, "operator '===' not defined for '(record {| xml x; anydata...; |}|[string," +
                        "xml])' and 'json'", 68, 21);
        validateError(resultNegative, i++, "operator '!==' not defined for '(record {| xml x; anydata...; |}|[string," +
                        "xml])' and 'json'", 68, 34);
        validateError(resultNegative, i++, "operator '===' not defined for 'Abc' and 'Def'", 76, 12);
        validateError(resultNegative, i++, "operator '!==' not defined for 'Def' and 'Abc'", 76, 25);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
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

    @DataProvider(name = "nonNilObjects")
    public Object[][] nonNilObjects() {
        return new Object[][]{
                {(true)},
                {(false)},
                {(5)},
                {(5.0)},
                {StringUtils.fromString("Hi from Ballerina!")},
                {ValueCreator.createMapValue()}
        };
    }
}
