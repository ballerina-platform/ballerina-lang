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
package org.ballerinalang.test.statements.assign;

import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of assignment operation.
 */
@Test(groups = { "brokenOnNewParser" })
public class AssignStmtTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/assign/assign-stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/assign/assign-stmt-negative.bal");
    }

    @Test
    public void invokeAssignmentTest() {
        BValue[] args = { new BInteger(100) };
        BValue[] returns = BRunUtil.invoke(result, "testIntAssignStmt", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 100;
        Assert.assertEquals(actual, expected);

        // floattype assignment test
        args = new BValue[] { new BFloat(2.3f) };
        returns = BRunUtil.invoke(result, "testFloatAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actualFloat = ((BFloat) returns[0]).floatValue();
        double expectedFloat = 2.3f;
        Assert.assertEquals(actualFloat, expectedFloat);

        // Boolean assignment test
        args = new BValue[] { new BBoolean(true) };
        returns = BRunUtil.invoke(result, "testBooleanAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        boolean actualBoolean = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actualBoolean);

        // String assignment test
        args = new BValue[] { new BString("Test Value") };
        returns = BRunUtil.invoke(result, "testStringAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actualString = returns[0].stringValue();
        String expectedString = "Test Value";
        Assert.assertEquals(actualString, expectedString);

        // Array index to int assignment test
        BValueArray arrayValue = new BValueArray(BTypes.typeInt);
        arrayValue.add(0, 150);
        args = new BValue[] { arrayValue };
        returns = BRunUtil.invoke(result, "testArrayIndexToIntAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 150;
        Assert.assertEquals(actual, expected);

        // Int to array index assignment test
        args = new BValue[] { new BInteger(250) };
        returns = BRunUtil.invoke(result, "testIntToArrayAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 250;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test assignment statement with multi return function")
    public void testAssignmentStmtWithMultiReturnFunc() {
        // Int assignment test
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testMultiReturn", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(5, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("john", returns[1].stringValue());
        Assert.assertEquals(6, ((BInteger) returns[2]).intValue());
    }

    @Test(description = "Test assignment of int to float")
    public void testAssignmentStatementIntToFloat() {
        BValue[] args = { new BInteger(100) };
        BValue[] returns = BRunUtil.invoke(result, "testIntCastFloatStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        double expected = 100f;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test binary expression with int and float")
    public void testBinaryExpressionIntToFloat() {
        BValue[] args = { new BInteger(100) };
        BValue[] returns = BRunUtil.invoke(result, "testBinaryExpressionIntAndFloatStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        double expected = 200f;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test action result assignment with variable destructure")
    public void restActionResultAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "restActionResultAssignment");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
        Assert.assertEquals(returns[2].stringValue(), "a");
        Assert.assertEquals(returns[3].stringValue(), "the error reason");
        Assert.assertEquals(returns[4].stringValue(), "foo3 error");
        Assert.assertEquals(returns[5].stringValue(), "3");
    }

    @Test(description = "Test assignment statement with errors")
    public void testAssignmentNegativeCases() {
        int i = 0;
        //testIncompatibleTypeAssign
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found 'int'", 3, 9);
        //testAssignCountMismatch1
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int,string]', found '[int,string,int]'", 11, 17);
        //testAssignCountMismatch2
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int,string,int,int]', found '[int,string,int]'", 21, 23);
        //testAssignTypeMismatch1
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int,string,int]', found '[string,string,int]'", 30, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found 'int'", 35, 13);
        //testAssignTypeMismatch2
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[int,int,int]', found '[int,string,int]'", 43, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found 'int'", 44, 16);
        //testVarRepeatedReturn1
        BAssertUtil.validateError(resultNegative, i++,
                "redeclared symbol 'a'", 48, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "undefined symbol 'b'", 49, 22);
        //testVarRepeatedReturn2
        BAssertUtil.validateError(resultNegative, i++,
                "redeclared symbol 'name'", 53, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "undefined symbol 'b'", 54, 22);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot assign a value to final 'i'", 65, 5);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot assign a value to final 'aa'", 71, 5);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '[int,int]'", 90, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'map<string>', found 'record {| string a; anydata...; |}'", 91, 22);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid record binding pattern with type 'error'", 92, 9);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid record variable; expecting a record type but found 'error' in type definition", 92, 20);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected 'any[]', found 'error[]'", 98, 15);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected 'error[]', found 'any[]'", 100, 26);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected '(CError|LError)?[]', found 'error?[]'", 118, 19);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected '(CError|LError)?[]', found 'error?[]'", 119, 11);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected '(error|int[])', found 'error[]'", 127, 21);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected '(int|error[])', found 'error'", 132, 21);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected 'function ((any|error)...) returns ()', found " +
                                          "'function (any...) returns ()'", 136, 47);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @Test(description = "Test negative assignment statement with cast and conversion with var.")
    public void testCastAndConversionWithVar() {
        CompileResult result = BCompileUtil.compile("test-src/statements/assign/var-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 31);
        int i = 0;
        BAssertUtil.validateError(result, i++, "unknown type 'Foo'", 4, 5);
        BAssertUtil.validateError(result, i++, "unknown type 'Foo'", 4, 20);
        BAssertUtil.validateError(result, i++, "undefined symbol 'bar'", 4, 25);
        BAssertUtil.validateError(result, i++, "unknown type 'Foo'", 5, 13);
        BAssertUtil.validateError(result, i++, "operator '+' not defined for 'error' and 'error'", 8, 26);
        BAssertUtil.validateError(result, i++, "unknown type 'Float'", 14, 5);
        BAssertUtil.validateError(result, i++, "unknown type 'Float'", 14, 22);
        BAssertUtil.validateError(result, i++, "unknown type 'Float'", 15, 13);
        BAssertUtil.validateError(result, i++, "operator '+' not defined for 'error' and 'error'", 18, 26);
        BAssertUtil.validateError(result, i++, "undefined symbol 'foo'", 23, 31);
        BAssertUtil.validateError(result, i++, "operator '+' not defined for 'string' and 'error'", 25, 26);
        BAssertUtil.validateError(result, i++, "operator '+' not defined for 'error' and 'error'", 27, 26);
        BAssertUtil.validateError(result, i++, "unknown type 'Foo'", 32, 5);
        BAssertUtil.validateError(result, i++, "unknown type 'Foo'", 32, 20);
        BAssertUtil.validateError(result, i++, "undefined symbol 'bar'", 32, 25);
        BAssertUtil.validateError(result, i++, "unknown type 'Foo'", 33, 13);
        BAssertUtil.validateError(result, i++, "operator '+' not defined for 'error' and 'error'", 36, 26);
        BAssertUtil.validateError(result, i++, "unknown type 'Float'", 42, 5);
        BAssertUtil.validateError(result, i++, "unknown type 'Float'", 42, 22);
        BAssertUtil.validateError(result, i++, "unknown type 'Float'", 43, 13);
        BAssertUtil.validateError(result, i++, "operator '+' not defined for 'error' and 'error'", 46, 26);
        BAssertUtil.validateError(result, i++, "undefined symbol 'foo'", 51, 31);
        BAssertUtil.validateError(result, i++, "operator '+' not defined for 'string' and 'error'", 53, 26);
        BAssertUtil.validateError(result, i++, "operator '+' not defined for 'error' and 'error'", 55, 26);
        BAssertUtil.validateError(result, i++, "unknown type 'Float'", 60, 5);
        BAssertUtil.validateError(result, i++, "unknown type 'Float'", 60, 25);
        BAssertUtil.validateError(result, i++, "undefined symbol 'fooo'", 60, 32);
        BAssertUtil.validateError(result, i++, "unknown type 'Foo'", 73, 14);
        BAssertUtil.validateError(result, i++, "undefined symbol 'bar'", 73, 19);
        BAssertUtil.validateError(result, i++, "unknown type 'X'", 79, 5);
        BAssertUtil.validateError(result, i, "unknown type 'V'", 81, 5);
    }

    @Test()
    public void testAssignErrorArrayToAny() {
        BRunUtil.invoke(result, "testAssignErrorArrayToAny");
    }

    @Test()
    public void testAssignIntArrayToJson() {
        BRunUtil.invoke(result, "testAssignIntArrayToJson");
    }

    @Test()
    public void testAssignIntOrStringArrayIntOrFloatOrStringUnionArray() {
        BRunUtil.invoke(result, "testAssignIntOrStringArrayIntOrFloatOrStringUnionArray");
    }

    @Test()
    public void assignAnyToUnionWithErrorAndAny() {
        BRunUtil.invoke(result, "assignAnyToUnionWithErrorAndAny");
    }
}
