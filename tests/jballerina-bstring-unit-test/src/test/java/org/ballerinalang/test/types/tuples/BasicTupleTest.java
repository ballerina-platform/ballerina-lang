/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.tuples;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Basic Test cases for tuples.
 *
 * @since 0.966.0
 */
public class BasicTupleTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/tuples/tuple_basic_test.bal");
        resultNegative = BCompileUtil.compile("test-src/types/tuples/tuple_negative_test.bal");
    }


    @Test(description = "Test basics of tuple types")
    public void testTupleTypeBasics() {
        BValue[] returns = BRunUtil.invoke(result, "basicTupleTest", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), " test1 expr \n" +
                " test2 \n" +
                " test3 3 \n" +
                " test4 4 \n" +
                " test5 foo test5 \n ");
    }

    @Test(description = "Test Function invocation using tuples")
    public void testFunctionInvocationUsingTuples() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionInvocation", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "xy5.0z");
    }

    @Test(description = "Test Function Invocation return values using tuples")
    public void testFunctionReturnValue() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionReturnValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "x5.0z");

        returns = BRunUtil.invoke(result, "testFunctionReturnValue2", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "xz");
        Assert.assertEquals(returns[1].stringValue(), "5.0");
    }

    @Test(description = "Test Function Invocation return values using tuples")
    public void testIgnoredValue() {
        BValue[] returns = BRunUtil.invoke(result, "testIgnoredValue1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "foo");

        returns = BRunUtil.invoke(result, "testIgnoredValue2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "foo");

        returns = BRunUtil.invoke(result, "testIgnoredValue3");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "foo");

        returns = BRunUtil.invoke(result, "testIgnoredValue4");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "foo");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test(description = "Test index based access of tuple type")
    public void testIndexBasedAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testIndexBasedAccess");
        Assert.assertEquals(returns[0].stringValue(), "def");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test(description = "Test index based access of tuple type with records")
    public void testIndexBasedAccessOfRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testIndexBasedAccessOfRecords");
        Assert.assertEquals(returns[0].stringValue(), "NewFoo");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertEquals(returns[2].stringValue(), "NewBar");
        Assert.assertEquals(returns[3].stringValue(), "Foo");
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), 15.5);
    }

    @Test(description = "Test default values for tuple type")
    public void testDefaultValuesInTuples() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultValuesInTuples");
        Assert.assertEquals(returns[0].stringValue(), "");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BFloat) returns[3]).intValue(), 0);
    }

    @Test(description = "Test tuple to array assignment")
    public void testTupleToArrayAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleToArrayAssignment1", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "a");
        Assert.assertEquals(returns[1].stringValue(), "b");
        Assert.assertEquals(returns[2].stringValue(), "c");

        returns = BRunUtil.invoke(result, "testTupleToArrayAssignment2", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "a");
        Assert.assertEquals(returns[1].stringValue(), "b");
        Assert.assertEquals(returns[2].stringValue(), "c");
    }

    @Test(description = "Test array to tuple assignment")
    public void testArrayToTupleAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayToTupleAssignment1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", \"b\", \"c\"]");

        returns = BRunUtil.invoke(result, "testArrayToTupleAssignment2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", \"b\", \"c\"]");

        returns = BRunUtil.invoke(result, "testArrayToTupleAssignment3", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "a");
        Assert.assertEquals(returns[1].stringValue(), "[\"b\", \"c\"]");

        returns = BRunUtil.invoke(result, "testArrayToTupleAssignment4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", \"b\", \"c\"]");
    }

    @Test(description = "Test union expected type for list constructor")
    public void testTupleUnionExpectedType() {
        BRunUtil.invoke(result, "testTupleUnionExpectedType");
    }

    @Test
    public void testUnionRestDescriptor() {
        BRunUtil.invoke(result, "testUnionRestDescriptor");
    }

    @Test
    public void testAnonRecordsInTupleTypeDescriptor() {
        BRunUtil.invoke(result, "testAnonRecordsInTupleTypeDescriptor");
    }

    @Test(description = "Test negative scenarios of assigning tuple literals")
    public void testNegativeTupleLiteralAssignments() {
        Assert.assertEquals(resultNegative.getErrorCount(), 30);
        int i = 0;
        BAssertUtil.validateError(
                resultNegative, i++, "tuple and expression size does not match", 18, 32);
        BAssertUtil.validateError(
                resultNegative, i++, "tuple and expression size does not match", 19, 41);
        BAssertUtil.validateError(
                resultNegative, i++, "ambiguous type '([int,boolean,string]|[any,boolean,string])?'", 34, 63);
        BAssertUtil.validateError(
                resultNegative, i, "ambiguous type '([Person,int]|[Employee,int])?'", 38, 47);
    }

    @Test(description = "Test negative scenarios of assigning tuples and arrays")
    public void testNegativeTupleArrayAssignments() {
        int i = 4;
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'int[]', found '[string...]'", 43, 15);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'string[2]', found '[string...]'", 49, 19);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'string[3]', found '[string,string]'", 55, 19);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[int...]', found 'string[]'", 61, 18);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[int,string...]', found '(int|string)[]'", 67, 26);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[int,int]', found 'int[3]'", 73, 20);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[int,int,int]', found 'int[2]'", 79, 25);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[int,int,int...]', found 'int[]'", 91, 28);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[int,int,int]', found 'int[]'", 93, 25);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[int,int,int...]', found 'int[1]'", 96, 28);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[int,int]', found 'int[1]'", 98, 20);
        BAssertUtil.validateError(
                resultNegative, i, "incompatible types: expected '[int,int]', found 'int[3]'", 101, 20);
    }

    @Test(description = "Test negatives of index based access of tuple type")
    public void testNegativesOfTupleType() {
        int i = 16;
        BAssertUtil.validateError(resultNegative, i++, "tuple and expression size does not match", 114, 38);
        BAssertUtil.validateError(resultNegative, i++, "list index out of range: index: '-1'", 119, 16);
        BAssertUtil.validateError(resultNegative, i++, "list index out of range: index: '3'", 120, 16);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 122, 16);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 128, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(string|boolean|int)', found 'float'", 134, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found '(string|boolean|int)'", 135, 16);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(string|boolean)', found '(string|boolean|int)'", 136, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found 'S1|S2'", 154, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid list index expression: value space '3|4|5' out of range", 155, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '0|1|2|S1'", 156, 19);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected 'int', found '(0|1|2|S1|S2)'", 157, 19);
        BAssertUtil.validateError(resultNegative, i++,
                                  "invalid list index expression: value space '(3|4|5|6)' out of range", 158, 19);
        BAssertUtil.validateError(resultNegative, i, "list index out of range: index: '-1'", 165, 19);
    }
}
