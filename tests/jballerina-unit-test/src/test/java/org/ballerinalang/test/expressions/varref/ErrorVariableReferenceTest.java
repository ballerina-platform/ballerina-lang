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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/varref/error_variable_reference.bal");
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
        Assert.assertEquals(returns.length, 13);
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
        Assert.assertEquals(returns[12].stringValue(), "Some Error One {message:\"Msg Three\", detail:\"Detail Msg\"}");
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

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
    public void testErrorInTupleWithDestructure() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInTupleWithDestructure");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "Bal");
        Assert.assertEquals(returns[2].stringValue(), "Err2");
        Assert.assertEquals(returns[3].stringValue(), "{\"message\":\"Something Wrong2\"}");
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
    public void testErrorInTupleWithDestructure2() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInTupleWithDestructure2");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "Bal");
        Assert.assertEquals(returns[2].stringValue(), "Err2");
        Assert.assertEquals(returns[3].stringValue(), "Something Wrong2");
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
    public void testErrorInRecordWithDestructure() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInRecordWithDestructure");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1000);
        Assert.assertEquals(returns[1].stringValue(), "Err3");
        Assert.assertEquals(returns[2].stringValue(), "Something Wrong3");
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
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

    @Test(description = "Test default error var ref with rest underscore")
    public void testDefaultErrorRefBindingPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultErrorRefBindingPattern");
        Assert.assertEquals(returns[0].stringValue(), "the reason");
    }

    @Test(description = "Test simple indirect error ref pattern")
    public void testIndirectErrorRefBindingPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testIndirectErrorRefBindingPattern");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "msg");
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test error ref binding pattern when no error reason ref is given")
    public void testNoErrorReasonGiven() {
        BValue[] returns = BRunUtil.invoke(result, "testNoErrorReasonGiven");
        Assert.assertEquals(returns[0].stringValue(), "message");
    }

    @Test(description = "Test simple indirect error ref pattern with mandatory detail fields")
    public void testIndirectErrorRefMandatoryFields() {
        BRunUtil.invoke(result, "testIndirectErrorRefMandatoryFields");
    }

    @Test(description = "Test error varref inside a tupple ref", enabled = false)
    public void testErrorDestructuringInATupleDestructuring() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorDestructuringInATupleDestructuring");
        int i = 0;
        Assert.assertEquals(returns[i++].stringValue(), "r2");
        Assert.assertEquals(returns[i++].stringValue(), "msg");
    }

    @Test(description = "Test indirect error varref inside a tupple ref", enabled = false)
    public void testIndirectErrorVarRefInTuppleRef() {
        BValue[] returns = BRunUtil.invoke(result, "testIndirectErrorVarRefInTuppleRef");
        int i = 0;
        Assert.assertEquals(returns[i++].stringValue(), "Msg One");
        Assert.assertEquals(returns[i++].stringValue(), "Detail Msg");
        Assert.assertEquals(returns[i++].stringValue(), "1");
    }

    @Test(description = "Test error ctor in tupple var ref statement", enabled = false)
    public void testErrorRefAndCtorInSameStatement() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorRefAndCtorInSameStatement");
        int i = 0;
        Assert.assertEquals(returns[i++].stringValue(), "r2");
        Assert.assertEquals(returns[i++].stringValue(), "Detail Msg");
        Assert.assertEquals(returns[i++].stringValue(), "1");
    }

    @Test
    public void testErrorVariablesSemanticsNegative() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/expressions/varref/error_variable_reference_semantics_negative.bal");
        int i = -1;
        String incompatibleTypes = "incompatible types: ";
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'string', found 'boolean'", 31, 12);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'map<int>', found 'map<(string|error)>'", 31, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'string', found 'string?'", 32, 43);
        BAssertUtil.validateError(resultNegative, ++i,
                                  incompatibleTypes + "expected 'map<string>', found 'map<(string|error|boolean)>'",
                41, 25);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'string', found '(string|boolean)?'", 42, 43);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'any', found 'ballerina/lang.value:0.0.0:Cloneable'", 43, 22);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'ballerina/lang.value:0.0.0:Cloneable'", 43, 43);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'any', found 'ballerina/lang.value:0.0.0:Cloneable'", 43, 62);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'boolean', found 'string'", 65, 18);
        BAssertUtil.validateError(resultNegative, ++i, incompatibleTypes +
                "expected '[any,string,map,[error,any]]', found '[int,string,error,[error,Foo]]'", 79, 58);
//        BAssertUtil.validateError(resultNegative, ++i, incompatibleTypes +
//                "expected 'string', found 'boolean'", 93, 20);
//        BAssertUtil.validateError(resultNegative, ++i, incompatibleTypes + "expected 'Bar', " +
//                "found 'map<(anydata|readonly)>'", 93, 32);
//        BAssertUtil.validateError(resultNegative, ++i,
//                incompatibleTypes + "expected 'string', found 'boolean'", 94, 20);
        BAssertUtil.validateError(resultNegative, ++i,
                                  "incompatible types: expected 'map', found 'map<(error|string|int)>'", 124, 35);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'ballerina/lang.value:0.0.0:Cloneable'", 134, 19);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected '(anydata|readonly)', found " +
                "'ballerina/lang.value:0.0.0:Cloneable'", 134, 38);

        Assert.assertEquals(resultNegative.getErrorCount(), i + 1);
    }

    @Test
    public void testNegativeErrorVariables() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/expressions/varref/error_variable_reference_negative.bal");
        int i = 0;
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 's'", 20, 5);
        BAssertUtil.validateError(resultNegative, i++,
                "variables in a binding pattern must be distinct; found duplicate variable 's'", 22, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "variables in a binding pattern must be distinct; found duplicate variable 's'", 22, 36);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'r'", 27, 21);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'message'", 27, 34);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'abc'", 27, 49);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'r'", 28, 11);
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'message'", 28, 24);
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'abc'", 28, 39);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'r2'", 30, 21);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'message2'", 30, 35);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'rest'", 30, 45);
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'r2'", 31, 11);
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'message2'", 31, 25);
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'rest'", 31, 38);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'r3'", 34, 30);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'message3'", 34, 44);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'abc3'", 34, 60);
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'message3'", 35, 24);
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'abc3'", 35, 40);
        Assert.assertEquals(resultNegative.getDiagnostics().length, i);
    }
}
