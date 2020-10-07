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
package org.ballerinalang.test.expressions.unaryoperations;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of lengthof operator.
 */
public class LengthOfOperatorTest {

    CompileResult result;
    CompileResult resNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/unaryoperations/lengthof-operation.bal");
        resNegative = BCompileUtil.compile(
                "test-src/expressions/unaryoperations/lengthof-operation-negative.bal");
    }

    @Test(description = "Test lengthof unary expression")
    public void testArrayLengthAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestAssignmentCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Function invocation statement.")
    public void testArrayLengthAccessExprFunctionInvocationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestFunctionInvocationCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Variable definition statement.")
    public void testArrayLengthAccessExprVariableDefinitionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestVariableDefinitionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Array initialization statement.")
    public void testArrayLengthAccessExprArrayInitializationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestArrayInitializerCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Map initialization statement.")
    public void testArrayLengthAccessExprMapInitializationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestMapInitializerCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Return statement.")
    public void testArrayLengthAccessExprReturnExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestReturnStatementCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in multi Return statement.")
    public void testArrayLengthAccessExprMultiReturnExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestMultiReturnStatementCase", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actualFirst = (int) ((BInteger) returns[0]).intValue();
        int actualSecond = (int) ((BInteger) returns[1]).intValue();
        int actualThird = (int) ((BInteger) returns[2]).intValue();

        int expectedFirst = 3;
        int expectedSecond = 1;
        int expectedThird = 2;

        Assert.assertEquals(actualFirst, expectedFirst);
        Assert.assertEquals(actualSecond, expectedSecond);
        Assert.assertEquals(actualThird, expectedThird);
    }

    @Test(description = "Test lengthof unary expression when present in Type cast expression.")
    public void testArrayLengthAccessExprTypeCastExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestTypeCastExpressionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in If condition.")
    public void testArrayLengthAccessExprIfConditionExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestIfConditionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Binary expression.")
    public void testArrayLengthAccessExpBinaryExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestBinaryExpressionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Struct field access expression.")
    public void testArrayLengthAccessExpStructFieldAccessCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestStructFieldAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test lengthof unary expression when reference point to JSON array.")
    public void testArrayLengthAccessJSONArrayCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestJSONArrayCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 6;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when array is null.")
    public void testArrayLengthAccessExpArrayNullCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BRunUtil.invoke(resNegative, "arrayLengthAccessNullArrayCase", args);
    }

    @Test(description = "Test lengthof unary expression when reference point to null map.")
    public void testArrayLengthAccessTestMapNegativeNullCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BRunUtil.invoke(resNegative, "arrayLengthAccessNullMapCase", args);
    }

    @Test(description = "Test lengthof map")
    public void lengthOfMap() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "lengthOfMap", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 4;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof map empty")
    public void lengthOfMapEmpty() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "lengthOfMapEmpty", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof string")
    public void lengthOfString() {
        BValue[] returns = BRunUtil.invoke(result, "lengthOfString");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 11);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 10);
    }

    @Test(description = "Test lengthof blob")
    public void lengthOfBlob() {
        BValue[] returns = BRunUtil.invoke(result, "lengthOfBlob");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(description = "Test lengthof string")
    public void lengthOfNullString() {
        BValue[] returns = BRunUtil.invoke(result, "lengthOfNullString");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test(description = "Test lengthof JSON object")
    public void lengthOfJSONObject() {
        BValue[] returns = BRunUtil.invoke(result, "lengthOfJSONObject");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }
}
