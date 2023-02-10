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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        Object arr = BRunUtil.invoke(result, "testBasicErrorVariableWithMapDetails");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 10);
        Assert.assertEquals(returns.get(0).toString(), "Error One");
        Assert.assertEquals(returns.get(1).toString(), "Error One");
        Assert.assertEquals(returns.get(2).toString(), "Error Two");
        Assert.assertEquals(returns.get(3).toString(), "Error Two");
        Assert.assertEquals(((BMap) returns.get(4)).get(StringUtils.fromString("message")).toString(), "Msg One");
        Assert.assertEquals(returns.get(5).toString(), "Msg One");
        Assert.assertEquals(returns.get(6).toString(), "Detail Msg");
        Assert.assertEquals(((BMap) returns.get(7)).get(StringUtils.fromString("message")).toString(), "Msg Two");
        Assert.assertTrue((Boolean) ((BMap) returns.get(7)).get(StringUtils.fromString("fatal")));
        Assert.assertEquals(returns.get(8).toString(), "Msg Two");
        Assert.assertTrue((Boolean) returns.get(9));
    }

    @Test(description = "Test simple error var def with const and map")
    public void testBasicErrorVariableWithConstAndMap() {
        Object arr = BRunUtil.invoke(result, "testBasicErrorVariableWithConstAndMap");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 11);
        Assert.assertEquals(returns.get(0).toString(), "Some Error One");
        Assert.assertEquals(returns.get(1).toString(), "Some Error One");
        Assert.assertEquals(returns.get(2).toString(), "Some Error Two");
        Assert.assertEquals(returns.get(3).toString(), "Some Error Two");
        Assert.assertEquals(((BMap) returns.get(4)).get(StringUtils.fromString("message")).toString(), "Msg Three");
        Assert.assertEquals(returns.get(5).toString(), "Msg Three");
        Assert.assertEquals(returns.get(6).toString(), "Detail Msg");
        Assert.assertEquals(((BMap) returns.get(7)).get(StringUtils.fromString("message")).toString(), "Msg Four");
        Assert.assertTrue((Boolean) ((BMap) returns.get(7)).get(StringUtils.fromString("fatal")));
        Assert.assertEquals(returns.get(8).toString(), "Msg Four");
        Assert.assertTrue((Boolean) returns.get(9));
        Assert.assertEquals(returns.get(10).toString(),
                "error CMS (\"Some Error One\",message=\"Msg Three\",detail=\"Detail Msg\")");
    }

    @Test(description = "Test simple error var def with record as detail")
    public void testBasicErrorVariableWithRecordDetails() {
        Object arr = BRunUtil.invoke(result, "testBasicErrorVariableWithRecordDetails");
        BArray returns = (BArray) arr;
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
        Object arr = BRunUtil.invoke(result, "testErrorInTuple");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1).toString(), "Bal");
        Assert.assertEquals(returns.get(2).toString(), "Err");
        Assert.assertEquals(returns.get(3).toString(), "Something Wrong2");
        Assert.assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
    public void testErrorInTupleWithDestructure() {
        Object arr = BRunUtil.invoke(result, "testErrorInTupleWithDestructure");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), 12);
        Assert.assertEquals(returns.get(1).toString(), "Bal");
        Assert.assertEquals(returns.get(2).toString(), "Err2");
        Assert.assertEquals(returns.get(3).toString(), "{\"message\":\"Something Wrong2\"}");
        Assert.assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
    public void testErrorInTupleWithDestructure2() {
        Object arr = BRunUtil.invoke(result, "testErrorInTupleWithDestructure2");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), 12);
        Assert.assertEquals(returns.get(1).toString(), "Bal");
        Assert.assertEquals(returns.get(2).toString(), "Err2");
        Assert.assertEquals(returns.get(3).toString(), "Something Wrong2");
        Assert.assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
    public void testErrorInRecordWithDestructure() {
        Object arr = BRunUtil.invoke(result, "testErrorInRecordWithDestructure");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 1000);
        Assert.assertEquals(returns.get(1).toString(), "Err3");
        Assert.assertEquals(returns.get(2).toString(), "Something Wrong3");
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error", enabled = false)
    public void testErrorInRecordWithDestructure2() {
        Object arr = BRunUtil.invoke(result, "testErrorInRecordWithDestructure2");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0), 1000);
        Assert.assertEquals(returns.get(1).toString(), "Err3");
        Assert.assertEquals(returns.get(2).toString(), "Something Wrong3");
        Assert.assertNull(returns.get(3));
    }

    @Test(description = "Test simple error var def inside tuple with destructuring error")
    public void testErrorWithRestParam() {
        Object returns = BRunUtil.invoke(result, "testErrorWithRestParam");
        BMap<BString, Object> results = (BMap) returns;
        Assert.assertEquals(results.get(StringUtils.fromString("fatal")).toString(), "true");
        Assert.assertEquals(results.get(StringUtils.fromString("extra")).toString(), "extra");
    }

    @Test(description = "Test simple error var ref with underscore")
    public void testErrorWithUnderscore() {
        Object arr = BRunUtil.invoke(result, "testErrorWithUnderscore");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "Error");
        BMap<BString, Object> results = (BMap) returns.get(1);
        Assert.assertEquals(results.get(StringUtils.fromString("message")).toString(), "Fatal");
        Assert.assertEquals(results.get(StringUtils.fromString("fatal")).toString(), "true");
    }

    @Test(description = "Test default error var ref with rest underscore")
    public void testDefaultErrorRefBindingPattern() {
        Object returns = BRunUtil.invoke(result, "testDefaultErrorRefBindingPattern");
        Assert.assertEquals(returns.toString(), "the reason");
    }

    @Test(description = "Test simple indirect error ref pattern")
    public void testIndirectErrorRefBindingPattern() {
        Object arr = BRunUtil.invoke(result, "testIndirectErrorRefBindingPattern");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "msg");
        Assert.assertFalse((Boolean) returns.get(1));
    }

    @Test(description = "Test error ref binding pattern when no error reason ref is given")
    public void testNoErrorReasonGiven() {
        Object returns = BRunUtil.invoke(result, "testNoErrorReasonGiven");
        Assert.assertEquals(returns.toString(), "message");
    }

    @Test(description = "Test simple indirect error ref pattern with mandatory detail fields")
    public void testIndirectErrorRefMandatoryFields() {
        BRunUtil.invoke(result, "testIndirectErrorRefMandatoryFields");
    }

    @Test(description = "Test error varref inside a tupple ref", enabled = false)
    public void testErrorDestructuringInATupleDestructuring() {
        Object arr = BRunUtil.invoke(result, "testErrorDestructuringInATupleDestructuring");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0).toString(), "r2");
        Assert.assertEquals(returns.get(1).toString(), "msg");
    }

    @Test(description = "Test indirect error varref inside a tupple ref", enabled = false)
    public void testIndirectErrorVarRefInTuppleRef() {
        Object arr = BRunUtil.invoke(result, "testIndirectErrorVarRefInTuppleRef");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0).toString(), "Msg One");
        Assert.assertEquals(returns.get(1).toString(), "Detail Msg");
        Assert.assertEquals(returns.get(2).toString(), "1");
    }

    @Test(description = "Test error ctor in tupple var ref statement", enabled = false)
    public void testErrorRefAndCtorInSameStatement() {
        Object arr = BRunUtil.invoke(result, "testErrorRefAndCtorInSameStatement");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0).toString(), "r2");
        Assert.assertEquals(returns.get(1).toString(), "Detail Msg");
        Assert.assertEquals(returns.get(2).toString(), "1");
    }

    // https://github.com/ballerina-platform/ballerina-lang/issues/38726
    @Test(enabled = false)
    public void testErrorDestructureWithErrorDeclaredWithReadOnlyIntersection() {
        BRunUtil.invoke(result, "testErrorDestructureWithErrorDeclaredWithReadOnlyIntersection");
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
                incompatibleTypes + "expected 'map<string>', found 'map<(string|error|boolean)>'",
                41, 25);
        BAssertUtil.validateError(resultNegative, ++i,
                incompatibleTypes + "expected 'string', found '(string|boolean)'", 42, 43);
        BAssertUtil.validateError(resultNegative, ++i,
                "cannot bind undefined error detail field 'message'", 43, 22);
        BAssertUtil.validateError(resultNegative, ++i,
                "cannot bind undefined error detail field 'fatal'", 43, 43);
        BAssertUtil.validateError(resultNegative, ++i,
                "cannot bind undefined error detail field 'extra'", 43, 60);
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
                "cannot bind undefined error detail field 'message'", 134, 19);
        BAssertUtil.validateError(resultNegative, ++i,
                "cannot bind undefined error detail field 'other'", 134, 38);

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
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'message3'", 35, 24);
//        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'abc3'", 35, 40);
        Assert.assertEquals(resultNegative.getDiagnostics().length, i);
    }

    @Test
    public void testErrorDetailBindingNegative() {
        CompileResult resultNegative =
                BCompileUtil.compile("test-src/expressions/varref/error_binding_pattern_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 22, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 22, 65);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 23, 38);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 23, 74);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 25, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 25, 62);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 26, 35);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 26, 71);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 28, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 28, 60);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 29, 33);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 29, 69);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'msg'", 33, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'extra'", 33, 57);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'msg'", 34, 38);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'extra'", 34, 66);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'msg'", 36, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'extra'", 36, 54);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'msg'", 37, 35);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'extra'", 37, 63);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'msg'", 39, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'extra'", 39, 52);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'msg'", 40, 33);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'extra'", 40, 61);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'identifier'", 50, 14);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'identifier'", 51, 18);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 53, 14);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 54, 18);

        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }
}
