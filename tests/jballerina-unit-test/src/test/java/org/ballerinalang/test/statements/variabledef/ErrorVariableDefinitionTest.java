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
package org.ballerinalang.test.statements.variabledef;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Error Variable Definitions.
 *
 * @since 0.990.4
 */
@Test(groups = { "brokenOnNewParser" })
public class ErrorVariableDefinitionTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.
                compile("test-src/statements/variabledef/error_variable_definition_stmt.bal");
    }

    @Test(description = "Test simple error var def with string and map")
    public void testBasicErrorVariableWithMapDetails() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorVariableWithMapDetails");
        Assert.assertEquals(returns.length, 12);
        Assert.assertEquals(returns[0].stringValue(), "Error One");
        Assert.assertEquals(returns[1].stringValue(), "Error One");
        Assert.assertEquals(returns[2].stringValue(), "Error Two");
        Assert.assertEquals(returns[3].stringValue(), "Error Two");
        Assert.assertEquals(returns[4].toString(), "{\"detail\":\"Detail Msg\"}");
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

    @Test(description = "Test simple error var def with string and map declared with var")
    public void testVarBasicErrorVariableWithMapDetails() {
        BValue[] returns = BRunUtil.invoke(result, "testVarBasicErrorVariableWithMapDetails");
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

    @Test(description = "Test simple error var def with const and map declared with var")
    public void testVarBasicErrorVariableWithConstAndMap() {
        BValue[] returns = BRunUtil.invoke(result, "testVarBasicErrorVariableWithConstAndMap");
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

    @Test(description = "Test simple error var def inside tuple with var")
    public void testErrorInTupleWithVar() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInTupleWithVar");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "Bal");
        Assert.assertEquals(returns[2].stringValue(), "Err");
        Assert.assertEquals(returns[3].stringValue(), "Something Wrong2");
        Assert.assertFalse(((BBoolean) returns[4]).booleanValue());
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

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
    public void testErrorInRecordWithDestructure() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorInRecordWithDestructure");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1000);
        Assert.assertEquals(returns[1].stringValue(), "Err3");
        Assert.assertEquals(returns[2].stringValue(), "Something Wrong3");
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorWithAnonErrorType() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorWithAnonErrorType");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "Error Code");
        Assert.assertEquals(returns[1].stringValue(), "Fatal");
    }

    @Test(description = "Test error variable with ignore as the detail variable")
    public void testErrorWithUnderscore() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorWithUnderscore");
        Assert.assertEquals(returns.length, 11);
        Assert.assertEquals(returns[0].stringValue(), "Error Code");
        Assert.assertEquals(returns[1].stringValue(), "Error Code");
        Assert.assertEquals(returns[2].stringValue(), "Error One");
        Assert.assertEquals(returns[3].stringValue(), "Error One");
        Assert.assertEquals(returns[4].stringValue(), "Error Two");
        Assert.assertEquals(returns[5].stringValue(), "Error Two");
        Assert.assertEquals(returns[6].stringValue(), "Error Two");
        Assert.assertEquals(returns[7].stringValue(), "Error Two");
        Assert.assertEquals(returns[8].stringValue(), "Msg One");
        Assert.assertEquals(returns[9].stringValue(), "Something Wrong");
        Assert.assertEquals(returns[10].stringValue(), "Something Wrong");
    }

    @Test(description = "Test named error variable def")
    public void testIndirectErrorDestructuring() {
        BValue[] returns = BRunUtil.invoke(result, "testIndirectErrorDestructuring");
        Assert.assertEquals(returns[0].stringValue(), "Msg");
        Assert.assertEquals(returns[1].stringValue(), "false");
        Assert.assertEquals(returns[2].stringValue(), "{\"other\":\"k\"}");
    }

    @Test(description = "Test error destruturing with sealed detail record")
    public void testSealedDetailDestructuring() {
        BValue[] returns = BRunUtil.invoke(result, "testSealedDetailDestructuring");
        Assert.assertEquals(returns[0].stringValue(), "sealed");
        Assert.assertEquals(returns[1].stringValue(), "{\"message\":\"Msg\"}");
    }

    @Test
    public void testNegativeErrorVariables() {
        CompileResult resultNegative = BCompileUtil.
                compile("test-src/statements/variabledef/error_variable_definition_stmt_negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 13);
        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, "redeclared symbol 'reason11'", 27, 9);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'SMS', found 'SMA'", 28, 85);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 30, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'string?'", 31, 28);
        BAssertUtil.validateError(resultNegative, ++i, "redeclared symbol 'reason11'", 41, 9);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 44, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'string?'", 45, 28);
        BAssertUtil.validateError(resultNegative, ++i,
                "redeclared symbol 'message'", 54, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'int', found 'map<(anydata|readonly)>'", 56, 18);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid error variable; expecting an error type but found 'int' in type definition", 57, 47);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 63, 17);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found '(anydata|readonly)'", 64, 16);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found '(anydata|readonly)'", 70, 16);
    }
}
