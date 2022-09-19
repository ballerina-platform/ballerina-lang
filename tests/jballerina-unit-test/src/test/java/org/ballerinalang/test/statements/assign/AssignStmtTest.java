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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of assignment operation.
 */
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
        Object[] args = {(100)};
        Object returns = BRunUtil.invoke(result, "testIntAssignStmt", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 100;
        Assert.assertEquals(actual, expected);

        // floattype assignment test
        args = new Object[]{(2.3f)};
        returns = BRunUtil.invoke(result, "testFloatAssignStmt", args);

        Assert.assertSame(returns.getClass(), Double.class);

        double actualFloat = (double) returns;
        double expectedFloat = 2.3f;
        Assert.assertEquals(actualFloat, expectedFloat);

        // Boolean assignment test
        args = new Object[]{(true)};
        returns = BRunUtil.invoke(result, "testBooleanAssignStmt", args);

        Assert.assertSame(returns.getClass(), Boolean.class);

        boolean actualBoolean = (boolean) returns;
        Assert.assertTrue(actualBoolean);

        // String assignment test
        args = new Object[]{StringUtils.fromString("Test Value")};
        returns = BRunUtil.invoke(result, "testStringAssignStmt", args);

        Assert.assertTrue(returns instanceof BString);

        String actualString = returns.toString();
        String expectedString = "Test Value";
        Assert.assertEquals(actualString, expectedString);

        // Array index to int assignment test
        BArray arrayValue = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT));
        arrayValue.add(0, 150);
        args = new Object[]{arrayValue};
        returns = BRunUtil.invoke(result, "testArrayIndexToIntAssignStmt", args);

        Assert.assertSame(returns.getClass(), Long.class);

        actual = (long) returns;
        expected = 150;
        Assert.assertEquals(actual, expected);

        // Int to array index assignment test
        args = new Object[]{(250)};
        returns = BRunUtil.invoke(result, "testIntToArrayAssignStmt", args);

        Assert.assertSame(returns.getClass(), Long.class);

        actual = (long) returns;
        expected = 250;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test assignment statement with multi return function")
    public void testAssignmentStmtWithMultiReturnFunc() {
        // Int assignment test
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(result, "testMultiReturn", args);

        Assert.assertEquals(returns.size(), 3);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertEquals(5L, returns.get(0));
        Assert.assertEquals("john", returns.get(1).toString());
        Assert.assertEquals(6L, returns.get(2));
    }

    @Test(description = "Test assignment of int to float")
    public void testAssignmentStatementIntToFloat() {
        Object[] args = {(100)};
        Object returns = BRunUtil.invoke(result, "testIntCastFloatStmt", args);

        Assert.assertSame(returns.getClass(), Double.class);

        double actual = (double) returns;
        double expected = 100f;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test action result assignment with variable destructure")
    public void restActionResultAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "restActionResultAssignment");
        Assert.assertEquals(returns.get(0), 0L);
        Assert.assertEquals(returns.get(1), 0L);
        Assert.assertEquals(returns.get(2).toString(), "a");
        Assert.assertEquals(returns.get(3).toString(), "the error reason");
        Assert.assertEquals(returns.get(4).toString(), "foo3 error");
        Assert.assertEquals(returns.get(5).toString(), "3");
    }

    @Test
    public void testAssignmentSemanticAnalysisNegativeCases() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/statements/assign/assign_stmt_semantic_negative.bal");
        int index = 0;
        BAssertUtil.validateError(resultNegative, index++, "cannot assign a value to final 'i'", 21, 5);
        BAssertUtil.validateError(resultNegative, index++, "cannot assign a value to final 'aa'", 27, 5);
        Assert.assertEquals(index, resultNegative.getErrorCount());
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
                "incompatible types: expected 'int', found '[int,int]'", 77, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'map<string>', found 'record {| string a; anydata...; |}'", 78, 22);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid record binding pattern with type 'error'", 79, 5);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid record variable; expecting a record type but found 'error' in type definition", 79, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'any[]', found 'error[]'", 85, 15);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'error[]', found 'any[]'", 87, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'CLError?[]', found 'error?[]'", 105, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'CLError?[]', found 'error?[]'", 106, 11);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(error|int[])', found 'error[]'", 114, 21);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(int|error[])', found 'error'", 119, 21);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'function ((any|error)...) returns ()', found " +
                        "'function (any...) returns ()'", 123, 47);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected " +
                "'[\"list\",int?,Type]', found 'Type'", 133, 14);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', " +
                "found 'table<record {| |}>'", 138, 11);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', " +
                "found 'table<record {| |}>'", 141, 12);
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
    public void testAssignAnyToUnionWithErrorAndAny() {
        BRunUtil.invoke(result, "testAssignAnyToUnionWithErrorAndAny");
    }

    @Test()
    public void testAssignQueryExpressionToVar() {
        BRunUtil.invoke(result, "testAssignVarInQueryExpression");
    }

    @Test
    public void testAssignmentStmtSemanticsNegative() {
        resultNegative = BCompileUtil.compile("test-src/statements/assign/assign-stmt-semantics-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 13);
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to a type definition", 20, 5);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'typedesc<Foo>', found 'int'",
                20, 11);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to a type definition", 21, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to a type definition", 23, 6);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to a type definition", 24, 7);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to a type definition", 25, 12);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found 'typedesc<Foo>'",
                25, 12);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to a type definition", 26, 11);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found 'typedesc<Foo>'",
                26, 11);
        BAssertUtil.validateError(resultNegative, i++, "invalid rest descriptor type; expecting an array type but " +
                "found 'typedesc<Foo>'", 27, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to a type definition", 27, 9);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to a type definition", 29, 14);
        BAssertUtil.validateError(resultNegative, i, "incompatible types: expected 'error?', found 'typedesc<Foo>'",
                29, 14);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
