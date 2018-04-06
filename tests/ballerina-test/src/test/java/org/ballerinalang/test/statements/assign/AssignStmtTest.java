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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of assignment operation.
 */
public class AssignStmtTest {

    CompileResult result;
    CompileResult resultNegative;

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
        Assert.assertEquals(actualBoolean, true);

        // String assignment test
        args = new BValue[] { new BString("Test Value") };
        returns = BRunUtil.invoke(result, "testStringAssignStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actualString = returns[0].stringValue();
        String expectedString = "Test Value";
        Assert.assertEquals(actualString, expectedString);

        // Array index to int assignment test
        BIntArray arrayValue = new BIntArray();
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

    @Test(description = "Test assignment statement with errors")
    public void testAssignmentNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 13);
        //testIncompatibleTypeAssign
        BAssertUtil.validateError(resultNegative, 0, "incompatible types: expected 'boolean'" +
                ", found 'int'", 3, 9);
        //testAssignCountMismatch1
        BAssertUtil.validateError(resultNegative, 1, "incompatible types: expected " +
                        "'(int,string)', found '(int,string,int)'", 11,
                17);
        //testAssignCountMismatch2
        BAssertUtil.validateError(resultNegative, 2, "incompatible types: expected " +
                        "'(int,string,int,int)', found '(int,string,int)'", 21,
                23);
        //testAssignTypeMismatch1
        BAssertUtil.validateError(resultNegative, 3, "incompatible types: expected " +
                "'(int,string,int)', found '(string,string,int)'", 30, 20);
        BAssertUtil.validateError(resultNegative, 4, "incompatible types: expected " +
                "'string', found 'int'", 35, 13);
        //testAssignTypeMismatch2
        BAssertUtil.validateError(resultNegative, 5, "incompatible types: expected " +
                "'(int,int,int)', found '(int,string,int)'", 43, 20);
        BAssertUtil.validateError(resultNegative, 6, "incompatible types: expected " +
                "'string', found 'int'", 44, 16);
        //testVarRepeatedReturn1
        BAssertUtil.validateError(resultNegative, 7, "redeclared symbol 'a'", 48, 19);
        BAssertUtil.validateError(resultNegative, 8, "undefined symbol 'b'", 49, 22);
        //testVarRepeatedReturn2
        BAssertUtil.validateError(resultNegative, 9, "redeclared symbol 'name'", 53, 19);
        BAssertUtil.validateError(resultNegative, 10, "undefined symbol 'b'", 54, 22);

        BAssertUtil.validateError(resultNegative, 11, "cannot assign a value to final 'i'",
                65, 5);
        BAssertUtil.validateError(resultNegative, 12, "cannot assign a value to final 'aa'",
                71, 5);
    }

    @Test(description = "Test negative assignment statement with cast and conversion with var.", enabled = false)
    public void testCastAndConversionWithVar() {
        CompileResult result = BCompileUtil.compile("test-src/statements/assign/var-negative.bal");
        BAssertUtil.validateError(result, 0, "unknown type 'Foo'", 4, 17);
        BAssertUtil.validateError(result, 1, "undefined symbol 'bar'", 4, 22);
        BAssertUtil.validateError(result, 2, "operator '+' not defined for 'error' and 'error'", 6, 22);
        BAssertUtil.validateError(result, 3, "unknown type 'Float'", 11, 17);
        BAssertUtil.validateError(result, 4, "operator '+' not defined for 'error' and 'error'", 13, 22);
        BAssertUtil.validateError(result, 5, "undefined symbol 'foo'", 17, 25);
        BAssertUtil.validateError(result, 6, "operator '+' not defined for 'string' and 'error'", 18, 22);
        BAssertUtil.validateError(result, 7, "operator '+' not defined for 'error' and 'error'", 19, 22);
        BAssertUtil.validateError(result, 8, "unknown type 'Foo'", 23, 17);
        BAssertUtil.validateError(result, 9, "undefined symbol 'bar'", 23, 22);
        BAssertUtil.validateError(result, 10, "operator '+' not defined for 'error' and 'error'", 25, 22);
        BAssertUtil.validateError(result, 11, "unknown type 'Float'", 30, 17);
        BAssertUtil.validateError(result, 12, "operator '+' not defined for 'error' and 'error'", 32, 22);
        BAssertUtil.validateError(result, 13, "undefined symbol 'foo'", 36, 25);
        BAssertUtil.validateError(result, 14, "operator '+' not defined for 'string' and 'error'", 37, 22);
        BAssertUtil.validateError(result, 15, "operator '+' not defined for 'error' and 'error'", 38, 22);
        BAssertUtil.validateError(result, 16, "unknown type 'Float'", 42, 20);
        BAssertUtil.validateError(result, 17, "undefined symbol 'fooo'", 42, 27);
        BAssertUtil.validateError(result, 18, "assignment count mismatch: expected 3 values, but found 2", 42, 19);
        BAssertUtil.validateError(result, 19, "assignment count mismatch: expected 3 values, but found 2", 46, 19);
        BAssertUtil.validateError(result, 20, "assignment count mismatch: expected 3 values, but found 2", 51, 19);
        BAssertUtil.validateError(result, 21, "unknown type 'Foo'", 55, 14);
        BAssertUtil.validateError(result, 22, "undefined symbol 'bar'", 55, 19);
    }
}
