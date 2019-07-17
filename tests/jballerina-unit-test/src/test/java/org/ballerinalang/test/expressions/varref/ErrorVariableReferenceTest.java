/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.varref;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Test cases for error variable references.
 *
 * @since 0.990.4
 */
public class ErrorVariableReferenceTest {
    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/varref/error_variable_reference.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/varref/error_variable_reference_negative.bal");
    }

    @Test(description = "Test simple error var def with string and map")
    public void testBasicErrorVariableWithMapDetails() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorVariableWithMapDetails");
        Assert.assertEquals(returns.length, 12);
        Assert.assertEquals(returns[0].stringValue(), "Error One");
        Assert.assertEquals(returns[1].stringValue(), "Error One");
        Assert.assertEquals(returns[2].stringValue(), "Error Two");
        Assert.assertEquals(returns[3].stringValue(), "Error Two");
        Assert.assertEquals(((BMap) returns[4]).get("message").stringValue(), "Msg One");
        Assert.assertEquals(returns[5].stringValue(), "Msg One");
        Assert.assertEquals(returns[6].stringValue(), "Detail Msg");
        Assert.assertNull(returns[7]);
        Assert.assertEquals(((BMap) returns[8]).get("message").stringValue(), "Msg Two");
        Assert.assertTrue(((BBoolean) ((BMap) returns[8]).get("fatal")).booleanValue());
        Assert.assertEquals(returns[9].stringValue(), "Msg Two");
        Assert.assertNull(returns[10]);
        Assert.assertNull(returns[11]);
    }

    @Test(description = "Test simple error var def with const and map")
    public void testBasicErrorVariableWithConstAndMap() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorVariableWithConstAndMap");
        Assert.assertEquals(returns.length, 12);
        Assert.assertEquals(returns[0].stringValue(), "Some Error One");
        Assert.assertEquals(returns[1].stringValue(), "Some Error One");
        Assert.assertEquals(returns[2].stringValue(), "Some Error Two");
        Assert.assertEquals(returns[3].stringValue(), "Some Error Two");
        Assert.assertEquals(((BMap) returns[4]).get("message").stringValue(), "Msg Three");
        Assert.assertEquals(returns[5].stringValue(), "Msg Three");
        Assert.assertEquals(returns[6].stringValue(), "Detail Msg");
        Assert.assertNull(returns[7]);
        Assert.assertEquals(((BMap) returns[8]).get("message").stringValue(), "Msg Four");
        Assert.assertTrue(((BBoolean) ((BMap) returns[8]).get("fatal")).booleanValue());
        Assert.assertEquals(returns[9].stringValue(), "Msg Four");
        Assert.assertNull(returns[10]);
        Assert.assertNull(returns[11]);
    }

    @Test(description = "Test simple error var def with record as detail")
    public void testBasicErrorVariableWithRecordDetails() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorVariableWithRecordDetails");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(), "Error One");
        Assert.assertEquals(returns[1].stringValue(), "Error One");
        Assert.assertEquals(returns[2].stringValue(), "Something Wrong");
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertEquals(((BMap) returns[4]).get("message").stringValue(), "Something Wrong");
    }

    @Test(description = "Test simple error var def inside tuple")
    public void testErrorInTuple() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInTuple");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "Bal");
        Assert.assertEquals(returns[2].stringValue(), "Err");
        Assert.assertEquals(returns[3].stringValue(), "Something Wrong2");
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorInTupleWithDestructure() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInTupleWithDestructure");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "Bal");
        Assert.assertEquals(returns[2].stringValue(), "Err2");
        Assert.assertEquals(returns[3].stringValue(), "{\"message\":\"Something Wrong2\"}");
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorInTupleWithDestructure2() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInTupleWithDestructure2");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "Bal");
        Assert.assertEquals(returns[2].stringValue(), "Err2");
        Assert.assertEquals(returns[3].stringValue(), "Something Wrong2");
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorInRecordWithDestructure() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInRecordWithDestructure");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1000);
        Assert.assertEquals(returns[1].stringValue(), "Err3");
        Assert.assertEquals(returns[2].stringValue(), "Something Wrong3");
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorInRecordWithDestructure2() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInRecordWithDestructure2");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1000);
        Assert.assertEquals(returns[1].stringValue(), "Err3");
        Assert.assertEquals(returns[2].stringValue(), "Something Wrong3");
        Assert.assertNull(returns[3]);
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorWithRestParam() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorWithRestParam");
        Assert.assertEquals(returns.length, 1);
        Map<String, BValue> results = ((BMap) returns[0]).getMap();
        Assert.assertEquals(results.get("fatal").stringValue(), "true");
        Assert.assertEquals(results.get("extra").stringValue(), "extra");
    }

    @Test(description = "Test simple error var ref with underscore")
    public void testErrorWithUnderscore() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorWithUnderscore");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "Error");
        Map<String, BValue> results = ((BMap) returns[1]).getMap();
        Assert.assertEquals(results.get("message").stringValue(), "Fatal");
        Assert.assertEquals(results.get("fatal").stringValue(), "true");
    }

    @Test
    public void testNegativeRecordVariables() {
        Assert.assertEquals(resultNegative.getErrorCount(), 12);
        int i = -1;
        String incompatibleTypes = "incompatible types: ";
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'boolean', found 'string'", 31, 12);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'map<int>', found 'map<string>'", 31, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'string', found 'string?'", 32, 43);
        BAssertUtil.validateError(resultNegative, ++i,
                                  incompatibleTypes + "expected 'map<string>', found 'map<(string|boolean)>'", 41, 25);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'string', found '(string|boolean)?'", 42, 43);
        BAssertUtil.validateError(resultNegative, ++i,
                "error constructor expression is not supported for error binding pattern", 43, 81);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'boolean', found 'string'", 64, 18);
        BAssertUtil.validateError(resultNegative, ++i, incompatibleTypes +
                "expected '[any,string,map,[error,any]]', found '[int,string,error,[error,Foo]]'", 78, 58);
        BAssertUtil.validateError(resultNegative, ++i, incompatibleTypes + "expected 'Bar', " +
                "found 'record {| string message?; $error0 cause?; (anydata|error)...; |}'", 92, 32);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'boolean', found 'string'", 93, 20);
        BAssertUtil.validateError(resultNegative, ++i,
                                  "error binding pattern does not support index based assignment", 111, 39);
        BAssertUtil.validateError(resultNegative, ++i,
                                  "error binding pattern does not support index based assignment", 111, 79);
    }
}
