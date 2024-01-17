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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestAssignmentCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Function invocation statement.")
    public void testArrayLengthAccessExprFunctionInvocationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestFunctionInvocationCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Variable definition statement.")
    public void testArrayLengthAccessExprVariableDefinitionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestVariableDefinitionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Array initialization statement.")
    public void testArrayLengthAccessExprArrayInitializationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestArrayInitializerCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Map initialization statement.")
    public void testArrayLengthAccessExprMapInitializationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestMapInitializerCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Return statement.")
    public void testArrayLengthAccessExprReturnExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestReturnStatementCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in multi Return statement.")
    public void testArrayLengthAccessExprMultiReturnExpressionCase() {
        Object[] args = {(100), (5)};
        Object arr = BRunUtil.invoke(result, "arrayLengthAccessTestMultiReturnStatementCase", args);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertSame(returns.get(0).getClass(), Long.class);

        long actualFirst = (long) returns.get(0);
        long actualSecond = (long) returns.get(1);
        long actualThird = (long) returns.get(2);

        long expectedFirst = 3;
        long expectedSecond = 1;
        long expectedThird = 2;

        Assert.assertEquals(actualFirst, expectedFirst);
        Assert.assertEquals(actualSecond, expectedSecond);
        Assert.assertEquals(actualThird, expectedThird);
    }

    @Test(description = "Test lengthof unary expression when present in Type cast expression.")
    public void testArrayLengthAccessExprTypeCastExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestTypeCastExpressionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in If condition.")
    public void testArrayLengthAccessExprIfConditionExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestIfConditionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Binary expression.")
    public void testArrayLengthAccessExpBinaryExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestBinaryExpressionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when present in Struct field access expression.")
    public void testArrayLengthAccessExpStructFieldAccessCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestStructFieldAccessCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test lengthof unary expression when reference point to JSON array.")
    public void testArrayLengthAccessJSONArrayCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestJSONArrayCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 6;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof unary expression when array is null.")
    public void testArrayLengthAccessExpArrayNullCase() {
        Object[] args = {(100), (5)};
        BRunUtil.invoke(resNegative, "arrayLengthAccessNullArrayCase", args);
    }

    @Test(description = "Test lengthof unary expression when reference point to null map.")
    public void testArrayLengthAccessTestMapNegativeNullCase() {
        Object[] args = {(100), (5)};
        BRunUtil.invoke(resNegative, "arrayLengthAccessNullMapCase", args);
    }

    @Test(description = "Test lengthof map")
    public void lengthOfMap() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "lengthOfMap", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 4;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof map empty")
    public void lengthOfMapEmpty() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "lengthOfMapEmpty", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test lengthof string")
    public void lengthOfString() {
        Object arr = BRunUtil.invoke(result, "lengthOfString");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0), 11L);
        Assert.assertEquals(returns.get(1), 4L);
        Assert.assertEquals(returns.get(2), 10L);
    }

    @Test(description = "Test lengthof blob")
    public void lengthOfBlob() {
        Object arr = BRunUtil.invoke(result, "lengthOfBlob");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0), 5L);
        Assert.assertEquals(returns.get(1), 0L);
    }

    @Test(description = "Test lengthof string")
    public void lengthOfNullString() {
        Object returns = BRunUtil.invoke(result, "lengthOfNullString");
        Assert.assertEquals(returns, 0L);
    }

    @Test(description = "Test lengthof JSON object")
    public void lengthOfJSONObject() {
        Object returns = BRunUtil.invoke(result, "lengthOfJSONObject");
        Assert.assertEquals(returns, 2L);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resNegative = null;
    }
}
