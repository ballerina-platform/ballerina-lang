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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Error Variable Definitions.
 *
 * @since 0.990.4
 */
public class ErrorVariableDefinitionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.
                compile("test-src/statements/variabledef/error_variable_definition_stmt.bal");
    }

    @Test(description = "Test simple error var def with string and map")
    public void testBasicErrorVariableWithMapDetails() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testBasicErrorVariableWithMapDetails");
        Assert.assertEquals(returns.size(), 8);
        Assert.assertEquals(returns.get(0).toString(), "Error One");
        Assert.assertEquals(returns.get(1).toString(), "Error One");
        Assert.assertEquals(returns.get(2).toString(), "Error Two");
        Assert.assertEquals(returns.get(3).toString(), "Error Two");
        Assert.assertEquals(returns.get(4).toString(), "{\"detail\":\"Detail Msg\"}");
        Assert.assertEquals(returns.get(5).toString(), "Msg One");
        Assert.assertEquals(((BMap) returns.get(6)).get(StringUtils.fromString("message")).toString(), "Msg Two");
        Assert.assertTrue((Boolean) ((BMap) returns.get(6)).get(StringUtils.fromString("fatal")));
        Assert.assertEquals(returns.get(7).toString(), "Msg Two");
    }

    @Test(description = "Test simple error var def with const and map")
    public void testBasicErrorVariableWithConstAndMap() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testBasicErrorVariableWithConstAndMap");
        Assert.assertEquals(returns.size(), 8);
        Assert.assertEquals(returns.get(0).toString(), "Some Error One");
        Assert.assertEquals(returns.get(1).toString(), "Some Error One");
        Assert.assertEquals(returns.get(2).toString(), "Some Error Two");
        Assert.assertEquals(returns.get(3).toString(), "Some Error Two");
        Assert.assertEquals(((BMap) returns.get(4)).get(StringUtils.fromString("message")).toString(), "Msg Three");
        Assert.assertEquals(returns.get(5).toString(), "Msg Three");
        Assert.assertEquals(((BMap) returns.get(6)).get(StringUtils.fromString("message")).toString(), "Msg Four");
        Assert.assertTrue((Boolean) ((BMap) returns.get(6)).get(StringUtils.fromString("fatal")));
        Assert.assertEquals(returns.get(7).toString(), "Msg Four");
    }

    @Test(description = "Test simple error var def with string and map declared with var")
    public void testVarBasicErrorVariableWithMapDetails() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVarBasicErrorVariableWithMapDetails");
        Assert.assertEquals(returns.size(), 8);
        Assert.assertEquals(returns.get(0).toString(), "Error One");
        Assert.assertEquals(returns.get(1).toString(), "Error One");
        Assert.assertEquals(returns.get(2).toString(), "Error Two");
        Assert.assertEquals(returns.get(3).toString(), "Error Two");
        Assert.assertEquals(((BMap) returns.get(4)).get(StringUtils.fromString("message")).toString(), "Msg One");
        Assert.assertEquals(returns.get(5).toString(), "Msg One");
        Assert.assertEquals(((BMap) returns.get(6)).get(StringUtils.fromString("message")).toString(), "Msg Two");
        Assert.assertTrue((Boolean) ((BMap) returns.get(6)).get(StringUtils.fromString("fatal")));
        Assert.assertEquals(returns.get(7).toString(), "Msg Two");
    }

    @Test(description = "Test simple error var def with const and map declared with var")
    public void testVarBasicErrorVariableWithConstAndMap() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVarBasicErrorVariableWithConstAndMap");
        Assert.assertEquals(returns.size(), 8);
        Assert.assertEquals(returns.get(0).toString(), "Some Error One");
        Assert.assertEquals(returns.get(1).toString(), "Some Error One");
        Assert.assertEquals(returns.get(2).toString(), "Some Error Two");
        Assert.assertEquals(returns.get(3).toString(), "Some Error Two");
        Assert.assertEquals(((BMap) returns.get(4)).get(StringUtils.fromString("message")).toString(), "Msg Three");
        Assert.assertEquals(returns.get(5).toString(), "Msg Three");
        Assert.assertEquals(((BMap) returns.get(6)).get(StringUtils.fromString("message")).toString(), "Msg Four");
        Assert.assertTrue((Boolean) ((BMap) returns.get(6)).get(StringUtils.fromString("fatal")));
        Assert.assertEquals(returns.get(7).toString(), "Msg Four");
    }

    @Test(description = "Test simple error var def with record as detail")
    public void testBasicErrorVariableWithRecordDetails() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testBasicErrorVariableWithRecordDetails");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0).toString(), "Error One");
        Assert.assertEquals(returns.get(1).toString(), "Error One");
        Assert.assertEquals(returns.get(2).toString(), "Something Wrong");
        Assert.assertTrue((Boolean) returns.get(3));
        Assert.assertEquals(((BMap) returns.get(4)).get(StringUtils.fromString("message")).toString(),
                "Something Wrong");
    }

    @Test(description = "Test simple error var def inside tuple")
    public void testErrorInTuple() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testErrorInTuple");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1).toString(), "Bal");
        Assert.assertEquals(returns.get(2).toString(), "Err");
        Assert.assertEquals(returns.get(3).toString(), "Something Wrong2");
        Assert.assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test simple error var def inside tuple with var")
    public void testErrorInTupleWithVar() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testErrorInTupleWithVar");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1).toString(), "Bal");
        Assert.assertEquals(returns.get(2).toString(), "Err");
        Assert.assertEquals(returns.get(3).toString(), "Something Wrong2");
        Assert.assertFalse((Boolean) returns.get(4));
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorInTupleWithDestructure() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testErrorInTupleWithDestructure");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1).toString(), "Bal");
        Assert.assertEquals(returns.get(2).toString(), "Err2");
        Assert.assertEquals(returns.get(3).toString(), "{\"message\":\"Something Wrong2\"}");
        Assert.assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorInTupleWithDestructure2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testErrorInTupleWithDestructure2");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1).toString(), "Bal");
        Assert.assertEquals(returns.get(2).toString(), "Err2");
        Assert.assertEquals(returns.get(3).toString(), "Something Wrong2");
        Assert.assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorInRecordWithDestructure() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testErrorInRecordWithDestructure");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 1000L);
        Assert.assertEquals(returns.get(1).toString(), "Err3");
        Assert.assertEquals(returns.get(2).toString(), "Something Wrong3");
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorWithAnonErrorType() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testErrorWithAnonErrorType");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "Error Code");
        Assert.assertEquals(returns.get(1).toString(), "Fatal");
    }

    @Test(description = "Test error variable with ignore as the detail variable")
    public void testErrorWithUnderscore() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testErrorWithUnderscore");
        Assert.assertEquals(returns.size(), 11);
        Assert.assertEquals(returns.get(0).toString(), "Error Code");
        Assert.assertEquals(returns.get(1).toString(), "Error Code");
        Assert.assertEquals(returns.get(2).toString(), "Error One");
        Assert.assertEquals(returns.get(3).toString(), "Error One");
        Assert.assertEquals(returns.get(4).toString(), "Error Two");
        Assert.assertEquals(returns.get(5).toString(), "Error Two");
        Assert.assertEquals(returns.get(6).toString(), "Error Two");
        Assert.assertEquals(returns.get(7).toString(), "Error Two");
        Assert.assertEquals(returns.get(8).toString(), "Msg One");
        Assert.assertEquals(returns.get(9).toString(), "Something Wrong");
        Assert.assertEquals(returns.get(10).toString(), "Something Wrong");
    }

    @Test(description = "Test named error variable def")
    public void testIndirectErrorDestructuring() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testIndirectErrorDestructuring");
        Assert.assertEquals(returns.get(0).toString(), "Msg");
        Assert.assertEquals(returns.get(1).toString(), "false");
        Assert.assertEquals(returns.get(2).toString(), "{\"other\":\"k\"}");
    }

    @Test(description = "Test error destruturing with sealed detail record")
    public void testSealedDetailDestructuring() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testSealedDetailDestructuring");
        Assert.assertEquals(returns.get(0).toString(), "sealed");
        Assert.assertEquals(returns.get(1).toString(), "{\"message\":\"Msg\"}");
    }

    @Test(description = "Test error binding pattern")
    public void testErrorBindingPattern() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testErrorBindingPattern");
        Assert.assertEquals(returns.get(0).toString(), "Detail Info");
        Assert.assertEquals(returns.get(1).toString(), "true");
        Assert.assertEquals(returns.get(2).toString(), "{\"A\":\"a\",\"B\":\"b\"}");
    }

    @Test
    public void testLocalErrorType() {
        BRunUtil.invoke(result, "testLocalErrorType");
    }

    @Test
    public void testErrorBindingPatternWithErrorDeclaredWithReadOnlyIntersection() {
        BRunUtil.invoke(result, "testErrorBindingPatternWithErrorDeclaredWithReadOnlyIntersection");
    }

    @Test
    public void testNegativeErrorVariables() {
        CompileResult resultNegative = BCompileUtil.
                compile("test-src/statements/variabledef/error_variable_definition_stmt_negative.bal");
        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, "error constructor does not accept additional detail args " +
                "'detail' when error detail type 'record {| string message?; error cause?; string...; |}' " +
                "contains individual field descriptors", 21, 60);
        BAssertUtil.validateError(resultNegative, ++i, "error constructor does not accept additional detail args " +
                "'fatal' when error detail type 'record {| string message?; error cause?; anydata...; |}' " +
                "contains individual field descriptors", 22, 60);
        BAssertUtil.validateError(resultNegative, ++i, "redeclared symbol 'reason11'", 27, 16);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'SMS', found 'SMA'", 28, 64);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 30, 26);
        BAssertUtil.validateError(resultNegative, ++i, "error constructor does not accept additional detail args " +
                "'detail' when error detail type 'record {| string message?; error cause?; string...; |}' " +
                "contains individual field descriptors", 35, 60);
        BAssertUtil.validateError(resultNegative, ++i, "error constructor does not accept additional detail args " +
                "'fatal' when error detail type 'record {| string message?; error cause?; anydata...; |}' " +
                "contains individual field descriptors", 36, 60);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'detail'",
                39, 26);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'extra'", 39, 45);
        BAssertUtil.validateError(resultNegative, ++i, "redeclared symbol 'reason11'", 41, 16);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'detail'",
                42, 26);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'extra'", 42, 45);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 44, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'string?'", 45, 28);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'message'",
                52, 26);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'detail'",
                52, 47);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'extra'", 52, 66);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'message'",
                53, 26);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'message'",
                54, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                "redeclared symbol 'message'", 54, 36);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'int', found 'map<ballerina/lang.value:0.0.0:Cloneable>'", 56, 18);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid error variable; expecting an error type but found 'int' in type definition", 57, 47);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 63, 17);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'ballerina/lang.value:0.0.0:Cloneable'", 64, 16);
        BAssertUtil.validateError(resultNegative, ++i, "cannot bind undefined error detail field 'message'",
                69, 24);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'ballerina/lang.value:0.0.0:Cloneable'", 70, 16);
        Assert.assertEquals(resultNegative.getErrorCount(), ++i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
